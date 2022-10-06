package mapObject;

import application.Map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CrossingPoint {

    private Point crossingPoint;
    private Road top;
    private Road bottom;
    private double distanceFromTopRoadStart;
    private double distanceFromBottomRoadStart;
    private List<Road> connectingRoads = new ArrayList<>();
    private List<LinkedList<Point>> connectionPaths = new ArrayList<>();
    private boolean isConnectionExist = false;
    private Junction junction;

    public CrossingPoint(Point crossingPoint, Segment segmentA, Segment segmentB) {
        this.crossingPoint = crossingPoint;
        top = segmentA.getRoad();
        distanceFromTopRoadStart = getDistanceFromRoadStart(segmentA);
        bottom = segmentB.getRoad();
        distanceFromBottomRoadStart = getDistanceFromRoadStart(segmentB);
        top.addCrossingPoint(this, Boolean.TRUE);
        bottom.addCrossingPoint(this, Boolean.FALSE);
    }

    /**
     * Creates copy of given crossingPoint and changes road with given <i>newBothWayRoad</i>.
     * Given road must be same road as appointed except with opposite direction.
     * @param appointedCrossingPoint
     * @param newBothWayRoad
     */
    public CrossingPoint(List<Object> appointedCrossingPoint, Road newBothWayRoad) {
        CrossingPoint crossingPoint = (CrossingPoint) appointedCrossingPoint.get(0);
        Boolean isOldBothWayRoadTop = (Boolean) appointedCrossingPoint.get(1);
        this.crossingPoint = crossingPoint.getCrossingPoint();
        if(isOldBothWayRoadTop) {
            top = newBothWayRoad;
            distanceFromTopRoadStart = crossingPoint.getTopRoad().getLength() - getDistanceFromTopRoadStart();
            bottom = crossingPoint.getBottomRoad();
            distanceFromBottomRoadStart = crossingPoint.getDistanceFromBottomRoadStart();
        } else {
            top = crossingPoint.getTopRoad();
            distanceFromTopRoadStart = crossingPoint.getDistanceFromTopRoadStart();
            bottom = newBothWayRoad;
            distanceFromBottomRoadStart = crossingPoint.getBottomRoad().getLength() - getDistanceFromBottomRoadStart();
        }
        top.addCrossingPoint(this, Boolean.TRUE);
        bottom.addCrossingPoint(this, Boolean.FALSE);
    }

    @Override
    public String toString() {
        return String.format("\"POINT (%s)\"%s0%s%d%s%d%s%d%n", crossingPoint, Map.CSV_SPLITTER, Map.CSV_SPLITTER, top.getId(), Map.CSV_SPLITTER, bottom.getId(), Map.CSV_SPLITTER, junction.getId());
    }

    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    /**
     * @return String which represent labels in CSV file of points.
     */
    public static String getLabels() {
        return String.format("%s%s%s%stopRoad%sbottomRoad%sjunction%n", Map.COORDINATES, Map.CSV_SPLITTER, Map.LAYER, Map.CSV_SPLITTER, Map.CSV_SPLITTER, Map.CSV_SPLITTER);
    }

    private double getDistanceFromRoadStart(Segment segmentWithCrossingPoint) {
        double distance = 0D;
        for(Segment roadSegment : segmentWithCrossingPoint.getRoad().getSegments()) {
            if(segmentWithCrossingPoint == roadSegment) {
                distance += segmentWithCrossingPoint.getStart().distanceTo(crossingPoint);
                break;
            } else {
                distance += roadSegment.getLength();
            }
        }
        return distance;
    }

    public void addConnectingRoad(Road road) {
        for(Road connectingRoad : connectingRoads) {
            if(connectingRoad == road) {
                return;
            }
        }
        connectingRoads.add(road);
    }

    public void addConnectionPath(LinkedList<Road> connection) {
        connectionPaths.add(Road.merge(connection));
    }

    public double getDistanceFromBottomRoadStart() {
        return distanceFromBottomRoadStart;
    }

    public double getDistanceFromTopRoadStart() {
        return distanceFromTopRoadStart;
    }

    public double getDistanceFromRoadStart(Boolean isTopRoad) {
        if(isTopRoad) {
            return distanceFromTopRoadStart;
        } else {
            return distanceFromBottomRoadStart;
        }
    }

    public boolean isConnectionExist() {
        return isConnectionExist;
    }

    public void setConnectionExist(boolean connectionExist) {
        isConnectionExist = connectionExist;
    }

    public Point getCrossingPoint() {
        return crossingPoint;
    }

    public Road getTopRoad() {
        return top;
    }

    public Road getBottomRoad() {
        return bottom;
    }

    public boolean isJunctionSet() {
        if(junction == null) {
            return false;
        } else {
            return true;
        }
    }

    public Junction getJunction() {
        return junction;
    }

    public void setJunction(Junction junction) {
        this.junction = junction;
    }

    public List<Road> getConnectingRoads() {
        return connectingRoads;
    }

    public List<LinkedList<Point>> getConnectionPaths() {
        return connectionPaths;
    }
}
