package application;

import exception.CmdException;
import exception.NoCrossingException;
import exception.NoRoadException;
import mapObject.*;
import java.io.*;
import java.util.*;

public class Map {

    public static final String COORDINATES = "WKT";
    public static final String TRAFFIC_DIRECTION = "oneway";
    public static final String LAYER = "layer";
    public static final String CLASS = "fclass";
    public static final String CSV_SPLITTER = ",";

    private HashMap<Integer, HashMap<Integer, LinkedList<Point>>> points = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, ArrayList<Segment>>> segments = new HashMap<>();
    private LinkedList<Segment> allSegments = new LinkedList<>();
    private LinkedList<CrossingPoint> crossingPoints = new LinkedList<>();
    private LinkedList<Road> roads = new LinkedList<>();
    private LinkedList<Junction> junctions = new LinkedList<>();
    private boolean isRightHandTraffic;

    public Map(String fileName, boolean isRightHandTraffic) throws IOException, CmdException {
        this.isRightHandTraffic = isRightHandTraffic;
        System.out.print("Reading source file (10k's): 0");
        long start = System.currentTimeMillis();
        read(fileName);
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println(String.format(" SUCCESS! [%.3f s]", elapsedTime/1000F));
        System.out.print("> Aggregating roads:           0");
        start = System.currentTimeMillis();
        aggregate();
        elapsedTime = System.currentTimeMillis() - start;
        System.out.println(String.format(" SUCCESS! [%.3f s]", elapsedTime/1000F));
        System.out.print("> Searching crossing points:   0");
        start = System.currentTimeMillis();
        findCrossings();
        splitBothWayRoads();
        sortCrossingPoints();
        elapsedTime = System.currentTimeMillis() - start;
        System.out.println(String.format(" SUCCESS! [%.3f s]", elapsedTime/1000F));
        System.out.print("> ");
    }

    public int toFileRoads(String fileName) {
        try(Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(fileName), "utf-8"))) {
            writer.write(Road.getLabels());
            for (Road road : roads) {
                if(!road.isCopy()) {
                    writer.write(road.toString());
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    public int toFileCrossingPoints(String fileName) {
        try(Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(fileName), "utf-8"))) {
            writer.write(CrossingPoint.getLabels());
            for(CrossingPoint crossingPoint : crossingPoints) {
                if(crossingPoint.isConnectionExist()) {
                    writer.write(crossingPoint.toString());
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    public int toFileConnections(String fileName) {
        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
            writer.write(Road.getLabels());
            for(CrossingPoint crossingPoint : crossingPoints) {
                if (crossingPoint.isConnectionExist() && crossingPoint.isJunctionSet()) {
                    int connectionID = 0;
                    for(LinkedList<Point> path : crossingPoint.getConnectionPaths()) {
                        writer.write(Road.getPolylineStringForCSV(path, connectionID++,'X',0, crossingPoint.getJunction().getId()));
                    }

                }
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    public int toFilesJunctions(String fileName, String crossingPoints, String sides, String levels, String conflictRoads, String intersections) {
        int junctionNo = 0;
        for (Junction junction : junctions) {
            if (isMeetRequirement(junction.getNumberOfCrossingPoints(), crossingPoints) && isMeetRequirement(junction.getNumberOfSides(), sides) && isMeetRequirement(junction.getNumberOfLevels(), levels) && isMeetRequirement(junction.getNumberOfConflictRoads(), conflictRoads) && isMeetRequirement(junction.getNumberOfIntersectionPoints(), intersections)) {
                try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.format("%s_s%d_l%d_x%d_c%d_i%d_%d%s", fileName.substring(0, fileName.lastIndexOf('.')), junction.getNumberOfSides(), junction.getNumberOfLevels(), junction.getNumberOfCrossingPoints(), junction.getNumberOfConflictRoads(), junction.getNumberOfIntersectionPoints(), junction.getId(), fileName.substring(fileName.lastIndexOf('.')))), "utf-8"))) {
                    writer.write(Road.getLabels());
                    for (Road road : junction.getRoads()) {
                        if(!road.isCopy()) {
                            writer.write(road.toString());
                        } else {
                            boolean isOriginalInJunction = false;
                            for(Road checkRoad : junction.getRoads()) {
                                if(!checkRoad.isCopy() && checkRoad.getId() == road.getId()) {
                                    isOriginalInJunction = true;
                                    break;
                                }
                            }
                            if(!isOriginalInJunction) {
                                writer.write(road.toString());
                            }
                        }
                    }
                    junctionNo++;
                } catch (IOException e) {
                    System.err.println("The system cannot find output folder!");
                    System.out.print("> ");
                    return -1;
                }
            }
        }
        if (junctionNo == 0) {
            System.out.println("No junctions found with given parameters!");
        } else {
            System.out.println(String.format("%d junction%s file%s created!", junctionNo, ((junctionNo == 1) ? "" : "s"), ((junctionNo == 1) ? "" : "s")));
        }
        System.out.print("> ");
        return 0;
    }

    // [</>/=][unsigned_integer] OR [unsigned_integer_from];[unsigned_integer_to]
    private boolean isMeetRequirement(int value, String requirement) {
        if(requirement == null) {
            return true;
        } else {
            if(requirement.indexOf('-') > 0) {
                String[] range = requirement.split("-");
                if(range.length == 2) {
                    int from;
                    int to;
                    try {
                        from = Integer.parseInt(range[0]);
                        to = Integer.parseInt(range[1]);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    if(from <= value && value <= to) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                int conditionValue;
                try {
                    conditionValue = Integer.parseInt(requirement.substring(1));
                } catch (NumberFormatException e) {
                    return true;
                }
                switch (requirement.substring(0,1)) {
                    case "=":   if(value == conditionValue) {
                                    return true;
                                } else {
                                    return false;
                                }
                    case ">":   if(value > conditionValue) {
                                    return true;
                                } else {
                                    return false;
                                }
                    case "<":   if(value < conditionValue) {
                                    return true;
                                } else {
                                    return false;
                                }
                    default:    return true;
                }
            }
        }
    }

    /**
     * Method reads CSV format file and creates object oriented structure.
     * @param fileName Path to CSV format file containing map data.
     */
    private void read(String fileName) throws IOException, CmdException {

        String line = "";
        int coordinatesColumn;
        int directionColumn;
        int layerColumn;
        int classColumn = -1;
        boolean filter = false;
        long fileLine = 1;
        int count10k = 0;
        long margin = 10000;

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        if((line = br.readLine()) != null) {
            String[] labels = line.split(CSV_SPLITTER);
            try {
                coordinatesColumn = searchLabel(labels, COORDINATES);
                if(coordinatesColumn != 0) {
                    throw new NoSuchElementException(COORDINATES + " column must be first!");
                }
                directionColumn = searchLabel(labels, TRAFFIC_DIRECTION);
                layerColumn = searchLabel(labels, LAYER);
            } catch (NoSuchElementException e) {
                System.err.println(String.format("At %s, line %d:%n%s", fileName, fileLine, e.getMessage()));
                System.out.println("> ");
                return;
            }
            try {
                classColumn = searchLabel(labels, CLASS);
                filter = true;
            } catch (NoSuchElementException e) {

            }
            while ((line = br.readLine()) != null) {
                if(++fileLine == margin) {
                    count10k++;
                    System.out.print(count10k%10);
                    margin += 10000L;
                }
                int coordinateStart = line.indexOf("\"");
                int coordinateEnd = line.indexOf("\"", coordinateStart + 1);
                String coordinates = line.substring(coordinateStart, coordinateEnd + 1);
                line = line.substring(coordinateEnd + 1);
                int index;
                while((index = line.indexOf("\"")) >= 0) {
                    String cleanString = line.substring(0,index);
                    int index2 = line.indexOf("\"", index + 1);
                    cleanString += line.substring(index+1, index2).replace(',','.');
                    if(index2+1 < line.length()) {
                        line = line.substring(index2+1);
                        line = cleanString + line;
                    } else {
                        line = cleanString;
                    }
                }
                String[] columns = line.split(CSV_SPLITTER);

                if(filter) {
                    if(!columns[classColumn].equals("motorway") && !columns[classColumn].equals("motorway_link") && !columns[classColumn].equals("primary") && !columns[classColumn].equals("primary_link") && !columns[classColumn].equals("secondary") && !columns[classColumn].equals("secondary_link") && !columns[classColumn].equals("tertiary") && !columns[classColumn].equals("tertiary_link") && !columns[classColumn].equals("trunk") && !columns[classColumn].equals("trunk_link") && !columns[classColumn].equals("unclassified") && !columns[classColumn].equals("unknown") && !columns[classColumn].equals("")) {
                        continue;
                    }
                }

                int layer;
                try {
                    layer = Integer.parseInt(columns[layerColumn]);
                } catch (NumberFormatException e) {
                    System.err.println(String.format("At %s, line %d:%n%s", fileName, fileLine, e));
                    System.out.print("> ");
                    continue;
                }

                coordinates = coordinates.substring(coordinates.lastIndexOf('(')+1 , coordinates.indexOf(')'));
                String[] pointsCoordinates = coordinates.split(",");
                LinkedList<Point> newPoints = new LinkedList<>();
                try {
                    for(int i=0; i<pointsCoordinates.length; i++) {
                        Point newPoint = parseCoordinates(pointsCoordinates[i]);
                        newPoints.add(newPoint);
                    }
                } catch (NumberFormatException e) {
                    System.err.println(String.format("%s%nAt %s, line %d:%n%s", line, fileName, fileLine, e));
                    System.out.print("> ");
                    continue;
                }
                char way = ' ';
                if(columns[directionColumn].length() > 0) {
                    way = columns[directionColumn].charAt(0);
                }
                while(newPoints.size() > 1) {
                    Point firstPoint = newPoints.getFirst();
                    newPoints.removeFirst();

                    Segment segment;
                    switch (way) {
                        case 'F':   segment = new Segment(firstPoint, newPoints.getFirst(), false, layer);
                            break;
                        case 'T':   segment = new Segment(newPoints.getFirst(), firstPoint, false, layer);
                            break;
                        case 'B':   segment = new Segment(firstPoint, newPoints.getFirst(), true, layer);
                            break;
                        default:    System.err.println(String.format("%s%nAt %s, line %d:%nTraffic direction value \"%c\" is unrecognizable.", line, fileName, fileLine, way));
                            System.out.print("> ");
                            continue;
                    }
                    firstPoint.addConnectingSegment(segment);
                    newPoints.getFirst().addConnectingSegment(segment);
                    allSegments.add(segment);
                    addSegmentToBucket(segment, firstPoint.getLatitudeBucket(), firstPoint.getLongitudeBucket());
                    if(firstPoint.getLatitudeBucket() != newPoints.getFirst().getLatitudeBucket() || firstPoint.getLongitudeBucket() != newPoints.getFirst().getLongitudeBucket()) {
                        addSegmentToBucket(segment, newPoints.getFirst().getLatitudeBucket(), newPoints.getFirst().getLongitudeBucket());
                    }
                }
            }
        }
    }

    private void addSegmentToBucket(Segment segment, int latitudeBucketKey, int longitudeBucketKey) {
        if(segments.containsKey(latitudeBucketKey)) {
            HashMap<Integer, ArrayList<Segment>> longitudeBuckets = segments.get(latitudeBucketKey);
            if(longitudeBuckets.containsKey(longitudeBucketKey)) {
                longitudeBuckets.get(longitudeBucketKey).add(segment);
            } else {
                ArrayList<Segment> segmentsBucket = new ArrayList<>();
                segmentsBucket.add(segment);
                longitudeBuckets.put(longitudeBucketKey, segmentsBucket);
            }
        } else {
            ArrayList<Segment> segmentsBucket = new ArrayList<>();
            segmentsBucket.add(segment);
            HashMap<Integer, ArrayList<Segment>> longitudeBuckets = new HashMap<>();
            longitudeBuckets.put(longitudeBucketKey, segmentsBucket);
            segments.put(latitudeBucketKey, longitudeBuckets);
        }
    }

    /**
     * Parse latitude and longitude coordinates which are separated by white space and are given as one String object,
     * to double values and returns Point object with those coordinates.
     * @param coordString String containing GPS coordinates.
     * @return Pointer to new or existing point with given GPS coordinates.
     */
    private Point parseCoordinates(String coordString) {
        String[] coordinates = coordString.split(" ");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        return Point.firstOrNew(latitude, longitude, points);
    }

    /**
     * Search for given label in array of strings.
     * @param labels Array of labels.
     * @param label Method is looking for this label.
     * @return Label column id or NoSuchElementException if not found.
     */
    private int searchLabel(String[] labels, String label) {
        for(int i=0; i<labels.length; i++) {
            if (labels[i].equals(label)) {
                return i;
            }
        }
        throw new NoSuchElementException(String.format("Label \"%s\" was not found.", label));
    }

    private void aggregate() {
        int roadNo = 1;
        double onePercent = (double) allSegments.size() / 100D;
        double margin = onePercent;
        int segmentNo = 0;
        int unit = 1;
        for(Segment segment : allSegments) {
            if(!segment.isAggregated() && !segment.isCorrupted()) {
                try {
                    Road newRoad = new Road(segment, roadNo);
                    roads.add(newRoad);
                    roadNo++;
                } catch (NoRoadException e) {
                    continue;
                }
            }
            if(++segmentNo >= margin) {
                System.out.print(unit%10);
                unit++;
                margin += onePercent;
            }
        }
    }

    private void findCrossings() {
        double step = (double) segments.keySet().size() / 10D;
        double margin = step;
        int bucketNo = 0;
        int unit = 1;
        List<Point> newCrossingPoints = new ArrayList<>();
        for (Integer latitudeBucket : segments.keySet()) {
            for(Integer longitudeBucket : segments.get(latitudeBucket).keySet()) {
                ArrayList<Segment> bucket = segments.get(latitudeBucket).get(longitudeBucket);
                for(int i=0; i<bucket.size()-1; i++) {
                    for(int j=i+1; j<bucket.size(); j++) {
                        Segment a = bucket.get(i);
                        Segment b = bucket.get(j);
                        if(a.getLayer() != b.getLayer() && !a.isCorrupted() && !b.isCorrupted()) {
                            try {
                                Point crossingPoint = a.findCrossing(b, points);
                                if(!newCrossingPoints.contains(crossingPoint)) {
                                    newCrossingPoints.add(crossingPoint);
                                    crossingPoints.add(new CrossingPoint(crossingPoint, a, b));
                                }
                            } catch (NoCrossingException e) {
                                continue;
                            }
                        }
                    }
                }
            }
            if(++bucketNo >= margin*unit) {
                System.out.print("1234567890");
                unit++;
            }
        }
    }

    private void splitBothWayRoads() {
        List<Road> newRoads = new ArrayList<>();
        for(Road road : roads) {
            if(road.isBothWay()) {
                List<CrossingPoint> newCrossingPoints = new ArrayList<>();
                newRoads.add(new Road(road, newCrossingPoints));
                crossingPoints.addAll(newCrossingPoints);
            }
        }
        roads.addAll(newRoads);
    }

    private void sortCrossingPoints() {
        for(Road road : roads) {
            road.sortCrossingPoints();
        }
    }

    /**
     * Method starts detection of interchanges.
     * @param maxDepth Number of roads at which maxLength exceeding is ignored.
     * @param maxLength Distance of path which connects two interchanging roads.
     */
    public void findConnections(int maxDepth, double maxLength) {
        System.out.print("Detecting junctions:         0");
        long start = System.currentTimeMillis();
        double step = (double) crossingPoints.size() / 50D;
        int segmentNo = 0;
        int unit = 1;
        for(CrossingPoint crossingPoint : crossingPoints) {
            if(crossingPoint.getBottomRoad().detectConnections(crossingPoint.getTopRoad(), crossingPoint, maxDepth, maxLength)) {
                crossingPoint.setConnectionExist(true);
                crossingPoint.addConnectingRoad(crossingPoint.getBottomRoad());
            }
            if(++segmentNo >= step*unit) {
                System.out.print(unit%10);
                unit++;
            }
        }
        groupJunctions(start);
    }

    private void groupJunctions(long start) {
        double step = (double) crossingPoints.size() / 25D;
        int segmentNo = 0;
        int unit = 1;
        int junctionNo = 0;
        for(CrossingPoint crossingPoint : crossingPoints) {
            if(crossingPoint.isConnectionExist() && !crossingPoint.isJunctionSet()) {
                Junction junction = new Junction(crossingPoint, ++junctionNo);
                junctions.add(junction);
            }
            if(++segmentNo >= step*unit) {
                System.out.print(unit%10);
                unit++;
            }
        }
        analyseJunctions(start);
    }

    private void analyseJunctions(long start) {
        double step = (double) junctions.size() / 25D;
        int segmentNo = 0;
        int unit = 1;
        for(Junction junction : junctions) {
            junction.analyse(isRightHandTraffic);
            if(++segmentNo >= step*unit) {
                System.out.print((unit+5)%10);
                unit++;
            }
        }
        long elapsedTime = (System.currentTimeMillis() - start);
        System.out.println(String.format(" SUCCESS! [%.3f s]", elapsedTime/1000F));
        System.out.print("> ");
    }
}
