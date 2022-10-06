package mapObject;

import application.Map;
import comparator.CrossedRoadComparator;
import exception.NoRoadException;

import java.util.*;

public class Road {

    private LinkedList<Point> polyline = new LinkedList<>();
    private LinkedList<Segment> segments = new LinkedList<>();
    private LinkedList<List<Object>> crossingPoints = new LinkedList<>(); // List with crossing points. Each row has CrossingPoint object and Boolean object, which identifies which .
    private List<Road> children = new ArrayList<>();    // These roads are forward options
    private List<Road> friends = new ArrayList<>();     // List of roads which merge
    private List<Road> siblings = new ArrayList<>();    // These roads are split in the same point
    private List<Road> parents = new ArrayList<>();     // These roads are backward options
    private double length;
    private int id;
    private List<Integer> layers = new ArrayList<>();
    private boolean isBothWay;
    private boolean isCopy = false;

    /**
     * Constructor method, which lets to create object ONLY if aggregation was successful.
     * @param notAggregatedSegment Segment from which This Road will be started to aggregate itself.
     * @param id ID of this road.
     * @throws NoRoadException Throws if aggregation was not successful;
     */
    public Road(Segment notAggregatedSegment, int id) throws NoRoadException {
        if (notAggregatedSegment.selfAggregate(this)) {
            this.id = id;
            isBothWay = notAggregatedSegment.isBothWay();
        } else {
            throw new NoRoadException();
        }
    }

    /**
     * From one both way road makes two opposite direction one way roads.
     * Configures given <i>bothWayRoad</i> as one way road and creates This Road as reversed version.
     * @param bothWayRoad
     * @param newCrossingPoints
     */
    public Road(Road bothWayRoad, List<CrossingPoint> newCrossingPoints) {
        length = bothWayRoad.getLength();
        id = bothWayRoad.getId();
        isBothWay = true;
        isCopy = true;
        layers = new ArrayList<>(bothWayRoad.getLayers());
        polyline.addAll(bothWayRoad.getPolyline());
        Collections.reverse(polyline);
        for(List<Object> appointedCrossingPoint : bothWayRoad.getCrossingPoints()) {
            newCrossingPoints.add(new CrossingPoint(appointedCrossingPoint, this));
        }
        setRelations();
    }

    /**
     * @return String which represents polyline of This Road in CSV file.
     */
    @Override
    public String toString() {
        return String.format("\"MULTILINESTRING ((%s))\"%s%d%s%s%s%s%s%s%s%.0f%n", getPolylineString(polyline), Map.CSV_SPLITTER, id, Map.CSV_SPLITTER, ((isBothWay)? "B" : "F"), Map.CSV_SPLITTER, getLayersString(), Map.CSV_SPLITTER, getCrossedRoadsString(), Map.CSV_SPLITTER, length);
    }

    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    /**
     * @return String which represent labels in CSV file of roads.
     */
    public static String getLabels() {
        return String.format("%s%sroadID%s%s%s%s%scrossedRoads%slength%n", Map.COORDINATES, Map.CSV_SPLITTER, Map.CSV_SPLITTER, Map.TRAFFIC_DIRECTION, Map.CSV_SPLITTER, Map.LAYER, Map.CSV_SPLITTER, Map.CSV_SPLITTER);
    }

    /**
     * @param polyline Polyline which will be converted into String.
     * @return String which represents given polyline.
     */
    public static String getPolylineString(LinkedList<Point> polyline) {
        String string = "";
        try {
            Point lastPoint = polyline.getLast();
            polyline.removeLast();
            for (Point point : polyline) {
                string += point + ",";
            }
            polyline.add(lastPoint);
            return string + lastPoint;
        } catch (NoSuchElementException e) {
            System.err.println(e+"%n"+polyline);
            return "";
        }
    }

    /**
     * @param polyline Polyline.
     * @param roadID
     * @param direction Traffic direction in <i>polyline</i>.
     * @param layer Layer of <i>polyline</i>.
     * @return String which represents given polyline as entry in CSV file of roads.
     */
    public static String getPolylineStringForCSV(LinkedList<Point> polyline, int roadID, char direction, int layer, int junctionID) {
        return String.format("\"MULTILINESTRING ((%s))\"%s%d%s%c%s%d%s%s%.0f%s%d%n", getPolylineString(polyline), Map.CSV_SPLITTER, roadID, Map.CSV_SPLITTER, direction, Map.CSV_SPLITTER, layer, Map.CSV_SPLITTER, Map.CSV_SPLITTER, Road.getPathLength(polyline), Map.CSV_SPLITTER, junctionID);
    }

    public LinkedList<Point> getReversedPolyline() {
        LinkedList<Point> reversedPolyline = new LinkedList<>(polyline);
        Collections.reverse(reversedPolyline);
        return reversedPolyline;
    }

    /**
     * @param roads List of roads, which will be connected into polyline.
     * @return All given roads connected to one polyline.
     */
    public static LinkedList<Point> merge(LinkedList<Road> roads) {
        if(roads.size() > 1) {
            LinkedList<Point> newRoadPolyline = new LinkedList<>();
            Road firstRoad = roads.getFirst();
            roads.removeFirst();

            if (firstRoad.getStart() == roads.getFirst().getStart() || firstRoad.getStart() == roads.getFirst().getEnd()) {
                newRoadPolyline.addAll(firstRoad.getReversedPolyline());
            } else {
                newRoadPolyline.addAll(firstRoad.getPolyline());
            }

            for (Road road : roads) {
                if (newRoadPolyline.getLast() == road.getStart()) {
                    newRoadPolyline.removeLast();
                    newRoadPolyline.addAll(road.getPolyline());
                } else {
                    if (newRoadPolyline.getLast() == road.getEnd()) {
                        newRoadPolyline.removeLast();
                        newRoadPolyline.addAll(road.getReversedPolyline());
                    } else {
                        break; // Two adjacent roads do not have connecting point.
                    }
                }
            }
            return newRoadPolyline;
        } else {
            return roads.getFirst().getPolyline();
        }
    }

    public static double getPathLength(List<Point> path) {
        double distance = 0D;
        if(path.size()>1) {
            for (int i = 1; i < path.size(); i++) {
                distance += path.get(i - 1).distanceTo(path.get(i));
            }
        }
        return distance;
    }

    /**
     * Method sets roads which connects to start point, notices those connecting roads
     * and then adds This Road to start point as outgoing road.
     */
    public void setStartRelations() {
        for(Road outRoad : getStart().getOutgoingRoads()) {
            siblings.add(outRoad);
            outRoad.getSiblings().add(this);
        }
        for(Road inRoad : getStart().getIncomingRoads()) {
            parents.add(inRoad);
            inRoad.getChildren().add(this);
        }
        getStart().getOutgoingRoads().add(this);
    }

    /**
     * Method sets roads which connects to end point, notices those connecting roads
     * and then adds This Road to end point as incoming road.
     */
    public void setEndRelations() {
        for(Road outRoad : getEnd().getOutgoingRoads()) {
            children.add(outRoad);
            outRoad.getParents().add(this);
        }
        for(Road inRoad : getEnd().getIncomingRoads()) {
            friends.add(inRoad);
            inRoad.getFriends().add(this);
        }
        getEnd().getIncomingRoads().add(this);
    }

    /**
     * Method sets Relations to both ends of this road.
     */
    public void setRelations() {
        setStartRelations();
        setEndRelations();
    }

    /**
     * This Road is first road of given interchange. This method finds all connections from This Road
     * to the target Road, writes them to given interchange and creates plan of given interchange.
     * @param target Another road of given interchange, to which connections will be detected.
     * @param crossingPoint Crossing between This and target Roads.
     * @param maxLength Distance from crossing point to the farthest valid point of intersection.
     * @return True - if at least one connection was found. False - if interchanging roads do not have connections.
     */
    public boolean detectConnections(Road target, CrossingPoint crossingPoint, int maxDepth, double maxLength) {
        LinkedList<Point> path = new LinkedList<>();
        LinkedList<Road> connection = new LinkedList<>();
        path.add(getStart());
        path.add(getEnd());
        boolean isConnectionThruEnd = tryAllRoads(target, crossingPoint, path, connection, true, 2, 0D, maxLength, maxDepth);
        path.clear();
        path.add(getEnd());
        path.add(getStart());
        if(tryAllRoads(target, crossingPoint, path, connection, false, 2, 0D, maxLength, maxDepth)) {
            return true;
        } else {
            return isConnectionThruEnd;
        }
    }

    /**
     * This method recursively search for all possible connections from <i>start</i> road to <i>target</i> road.
     * Searching for connections, which are not out of interchange range and not exceeds <i>maxDepth</i>.
     * Roads, which are connections are added to the given <i>interchange</i>.
     * @param target Road of given interchange, which intersects start road.
     * @param crossingPoint Crossing between This and target Roads.
     * @param path List of roads, which indicates tried path from start road to this road.
     * @param connection
     * @param isForward
     * @param directionChange
     * @param length
     * @param maxLength
     * @param maxDepth
     * @return True - if at least one connection with target was found. False - if interchanging roads do not have connections.
     */
    private boolean tryAllRoads(Road target, CrossingPoint crossingPoint, LinkedList<Point> path, LinkedList<Road> connection, boolean isForward, int directionChange, double length, double maxLength, int maxDepth) {
        connection.add(this);
        if( (connection.size() <= maxDepth && length < 10000) || length < maxLength) {
            if(target == this) {
                crossingPoint.addConnectionPath(new LinkedList<>(connection));                      //      add copy of sequence to interchange and return true.
                connection.removeLast();
                return true;
            } else {                                                                                // When this road is not the target road,
                boolean isConnectionExist = false;
                if(isForward) {     // if going forward - child or friend roads are valid. If Friend road is used, isForward set to false and directionChange decreased by one.
                    for (Road road : children) {                         //          go to each road connecting with This Road thru given point, except all roads at which already been,
                        if(!isLoop(path, road.getEnd())) {
                            path.add(road.getEnd());
                            if (road.tryAllRoads(target, crossingPoint, path, connection, true, directionChange, length+road.getLength(), maxLength, maxDepth)) {  // and try all roads of that connecting road. <-- Recursion axis. Come backs after check
                                isConnectionExist = true;                                               //                  and check that connection from This Road exists.
                                crossingPoint.addConnectingRoad(road);
                            }
                            path.removeLast();
                        }
                    }
                    if(directionChange != 0) {
                        for (Road road : friends) {                         //          go to each road connecting with This Road thru given point, except all roads at which already been,
                            if(!isLoop(path, road.getStart())) {
                                path.add(road.getStart());
                                if (road.tryAllRoads(target, crossingPoint, path, connection, false, directionChange-1, length+road.getLength(), maxLength, maxDepth)) {  // and try all roads of that connecting road. <-- Recursion axis. Come backs after check
                                    isConnectionExist = true;                                               //                  and check that connection from This Road exists.
                                    crossingPoint.addConnectingRoad(road);
                                }
                                path.removeLast();
                            }
                        }
                    }
                } else {     // if going backward - parent or sibling roads are valid. If Sibling road is used, isForward set to true and directionChange decreased by one.
                    for (Road road : parents) {                         //          go to each road connecting with This Road thru given point, except all roads at which already been,
                        if(!isLoop(path, road.getStart())) {
                            path.add(road.getStart());
                            if (road.tryAllRoads(target, crossingPoint, path, connection, false, directionChange, length+road.getLength(), maxLength, maxDepth)) {  // and try all roads of that connecting road. <-- Recursion axis. Come backs after check
                                isConnectionExist = true;                                               //                  and check that connection from This Road exists.
                                crossingPoint.addConnectingRoad(road);
                            }
                            path.removeLast();
                        }
                    }
                    if(directionChange != 0) {
                        for (Road road : siblings) {                         //          go to each road connecting with This Road thru given point, except all roads at which already been,
                            if(!isLoop(path, road.getEnd())) {
                                path.add(road.getEnd());
                                if (road.tryAllRoads(target, crossingPoint, path, connection, true, directionChange-1, length+road.getLength(), maxLength, maxDepth)) {  // and try all roads of that connecting road. <-- Recursion axis. Come backs after check
                                    isConnectionExist = true;                                               //                  and check that connection from This Road exists.
                                    crossingPoint.addConnectingRoad(road);
                                }
                                path.removeLast();
                            }
                        }
                    }
                }
                connection.removeLast();
                return isConnectionExist;                                                       //          Return true, if This Road is part of any connection. False, if not.
            }
        } else {
            connection.removeLast();
            return false;                                                                   // Path length is exceeded.
        }
    }

    private boolean isLoop(List<Point> path, Point checkPoint) {
        for(Point pathPoint : path) {
            if (checkPoint == pathPoint) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method fixes given segment direction if needed and adds it to This Road.
     * Also length of this road is calculating and crossings are added.
     * @param segment Segment which will be added to This Road.
     */
    public void addSegment(Segment segment) {
        if(segment.isBothWay() && polyline.getLast() != segment.getStart()) {   // If both way segment is given and start point of that segment does not match last point.
            segment.setEnd(segment.getStart());                                 // Correcting direction of segment
            segment.setStart(polyline.getLast());
        }
        segments.add(segment);
        polyline.add(segment.getEnd());                     // Adding new point to the polyline of This Road.
        length += segment.getLength();                      // Calculating length of this road.
        if(!layers.contains(segment.getLayer())) {                    // Road layer value equals segment with highest layer value.
            layers.add(segment.getLayer());
        }
    }

    public String getCrossedRoadsString() {
        String result = "";
        for(List<Object> appointedCrossingPoint : crossingPoints) {
            CrossingPoint crossingPoint = (CrossingPoint) appointedCrossingPoint.get(0);
            Boolean isThisRoadTop = (Boolean) appointedCrossingPoint.get(1);
            if(isThisRoadTop) {
                result += crossingPoint.getBottomRoad().getId() + ";";
            } else {
                result += crossingPoint.getTopRoad().getId() + ";";
            }
        }
        if(result.length() > 0) {
            result = result.substring(0,result.length()-1);
        }
        return result;
    }

    public void addCrossingPointsToJunction(Junction junction) {
        junction.addRoad(this);
        for(List<Object> crossingPointList : getCrossingPoints()) {
            CrossingPoint crossingPoint = (CrossingPoint) crossingPointList.get(0);
            if(crossingPoint.isConnectionExist() && !crossingPoint.isJunctionSet()) {
                junction.addCrossingPoint(crossingPoint);
                crossingPoint.setJunction(junction);
                for (Road road : crossingPoint.getConnectingRoads()) {
                    road.addCrossingPointsToJunction(junction);
                }
            }
        }
    }

    public Point getStart() {
        return polyline.getFirst();
    }

    public Point getEnd() {
        return polyline.getLast();
    }

    public LinkedList<Point> getPolyline() {
        return polyline;
    }

    public double getLength() {
        return length;
    }

    public void addCrossingPoint(CrossingPoint crossingPoint, Boolean isThisTop) {
        List<Object> crossedRoad = new ArrayList<>();
        crossedRoad.add(crossingPoint);
        crossedRoad.add(isThisTop);
        crossingPoints.add(crossedRoad);
    }

    public void sortCrossingPoints() {
        Collections.sort(crossingPoints, new CrossedRoadComparator());
    }

    public LinkedList<Segment> getSegments() {
        return segments;
    }

    public LinkedList<List<Object>> getCrossingPoints() {
        return crossingPoints;
    }

    public List<Road> getChildren() {
        return children;
    }

    public List<Road> getFriends() {
        return friends;
    }

    public List<Road> getSiblings() {
        return siblings;
    }

    public List<Road> getParents() {
        return parents;
    }

    public List<Integer> getLayers() {
        return layers;
    }

    public String getLayersString() {
        String string = "";
        for(Integer layer : layers) {
            string += layer.toString()+";";
        }
        return string.substring(0,string.length()-1);
    }

    public int getId() {
        return id;
    }

    public boolean isCopy() {
        return isCopy;
    }

    public boolean isBothWay() {
        return isBothWay;
    }
}
