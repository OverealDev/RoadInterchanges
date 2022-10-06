package mapObject;

import exception.EndOfRoadException;

import java.util.*;

public class Point {

    public static final int START = 2;
    public static final int END = 4;
    public static final int START_END = 6;
    public static final int SPLIT = 8;
    public static final int JOIN = 10;
    public static final int SPLIT_JOIN = 12;
    public static final int INTERSECTION = 14;

    private double latitude;
    private double longitude;
    private List<Segment> connectingSegments = new ArrayList<>();
    private List<Road> incomingRoads = new ArrayList<>();
    private List<Road> outgoingRoads = new ArrayList<>();
    private int latitudeBucket;
    private int longitudeBucket;
    private int type = 0; // 0 - undefined.

    /**
     * Creates Point with given GPS coordinates.
     * @param latitude Latitude (First number).
     * @param longitude Longitude (Second number).
     */
    public Point(double latitude, double longitude, int latitudeBucket, int longitudeBucket) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeBucket = latitudeBucket;
        this.longitudeBucket = longitudeBucket;
    }

    /**
     * @return String which represents This Point coordinates.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT,"%f %f", latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return false;
        }
    }

    public static int getBucketKey(double coordinate) {
        double bucket = coordinate*100D;
        int key = (int) bucket;
        if(bucket%1 == 0) {
            return key;
        } else {
            return key + 1;
        }
    }

    /**
     * @param latitude Latitude coordinate of required Point.
     * @param longitude Longitude coordinate of required Point.
     * @param latitudeBuckets List of all created Points.
     * @return Point object with given coordinates, or new Point if Point with given coordinates does not exist.
     */
    public static Point firstOrNew(double latitude, double longitude, HashMap<Integer, HashMap<Integer, LinkedList<Point>>> latitudeBuckets) {
        int latitudeBucketKey = getBucketKey(latitude);
        int longitudeBucketKey = getBucketKey(longitude);
        if(latitudeBuckets.containsKey(latitudeBucketKey)) {
            HashMap<Integer, LinkedList<Point>> longitudeBuckets = latitudeBuckets.get(latitudeBucketKey);
            if(longitudeBuckets.containsKey(longitudeBucketKey)) {
                LinkedList<Point> pointsBucket = longitudeBuckets.get(longitudeBucketKey);
                for(Point point : pointsBucket) {
                    if(point.isEqual(latitude, longitude)) {
                        return point;
                    }
                }
                // Point not found in the correct bucket. New point required.
                Point newPoint = new Point(latitude, longitude, latitudeBucketKey, longitudeBucketKey);
                pointsBucket.add(newPoint);
                return newPoint;
            } else {
                // Points with similar longitude does not exist. New point, new points bucket required.
                Point newPoint = new Point(latitude, longitude, latitudeBucketKey, longitudeBucketKey);
                LinkedList<Point> pointsBucket = new LinkedList<>();
                pointsBucket.add(newPoint);
                longitudeBuckets.put(longitudeBucketKey, pointsBucket);
                return newPoint;
            }
        } else {
            // Points with similar latitude does not exist. New point, new points bucket, new longitude buckets required.
            Point newPoint = new Point(latitude, longitude, latitudeBucketKey, longitudeBucketKey);
            LinkedList<Point> pointsBucket = new LinkedList<>();
            pointsBucket.add(newPoint);
            HashMap<Integer, LinkedList<Point>> longitudeBuckets = new HashMap<>();
            longitudeBuckets.put(longitudeBucketKey, pointsBucket);
            latitudeBuckets.put(latitudeBucketKey, longitudeBuckets);
            return newPoint;
        }
    }

    /**
     * Calculate distance between two points specified by latitude and longitude.
     * Uses Haversine formula.
     * @param pointB Distance to this object.
     * @return Distance in meters.
     */
    public double distanceTo(Point pointB) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(pointB.getLatitude() - latitude);
        double lonDistance = Math.toRadians(pointB.getLongitude() - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(pointB.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    /**
     * This Point is axis.
     * @param pointA
     * @param pointB
     * @return Angle between two vectors going thru given points and crossing at This Point.
     */
    public double getAngle(Point pointA, Point pointB) {
        if(pointA == pointB) {
            return 0D;
        } else {
            double dLonA = (pointA.getLongitude() - longitude);
            double yA = Math.sin(dLonA) * Math.cos(pointA.getLatitude());
            double xA = Math.cos(latitude) * Math.sin(pointA.getLatitude()) - Math.sin(latitude) * Math.cos(pointA.getLatitude()) * Math.cos(dLonA);
            double bearingA = 360D - ((Math.toDegrees(Math.atan2(yA, xA)) + 360) % 360);

            double dLonB = (pointB.getLongitude() - longitude);
            double yB = Math.sin(dLonB) * Math.cos(pointB.getLatitude());
            double xB = Math.cos(latitude) * Math.sin(pointB.getLatitude()) - Math.sin(latitude) * Math.cos(pointB.getLatitude()) * Math.cos(dLonB);
            double bearingB = 360D - ((Math.toDegrees(Math.atan2(yB, xB)) + 360) % 360);

            if (bearingA > bearingB) {
                return 360D - bearingA + bearingB;
            } else {
                return bearingB - bearingA;
            }
        }
    }

    /**
     * @param latitude Latitude coordinate for checking with this Point.
     * @param longitude Longitude coordinate for checking with this Point.
     * @return True - if this point is equal to given coordinates, false - if this point not equals to given coordinates.
     */
    public boolean isEqual(double latitude, double longitude) {
        if(this.latitude == latitude && this.longitude == longitude) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Adds given segment to list of segments, which are connecting to this point.
     * @param segment Segment, which will be added.
     */
    public void addConnectingSegment(Segment segment) {
        connectingSegments.add(segment);
    }

    /**
     * Checking is this point is end of road. End of road is an intersection or Point connecting to only one segment.
     * @return True - if point connects not to 2 segments or 2 connecting segments are opposite directions.
     * False - if point connects to 2 segments, which are the same direction.
     */
    public boolean isEndOfRoad() {
        if(connectingSegments.size() == 2) {                                                                // If only two segments are connected, there may not be an end of road.
            if(connectingSegments.get(0).isBothWay() != connectingSegments.get(1).isBothWay()) {            // If one road is one way and another is both way - end of road.
                return true;
            } else {                                                                                        // If both segments are one way or both way
                if ((!connectingSegments.get(0).isBothWay() && !connectingSegments.get(1).isBothWay()) &&   //      If both segments are one way
                        (connectingSegments.get(0).getEnd() == connectingSegments.get(1).getEnd() ||        //          and these two segments merge
                         connectingSegments.get(0).getStart() == connectingSegments.get(1).getStart())) {   //          or separate - end of road.
                    return true;
                } else {
                    return false;                                           // If both segments are both way or both one way segments do not merge or separate - no end of road.
                }
            }
        } else {                                                            // If at this point is intersection - end of road.
            return true;
        }
    }

    /**
     * If this point is not the end of the road, method returns neighbor segment of given segment where both of them connects to this point.
     * @param currentSegment Segment for which the neighbor segment is requested.
     * @return Neighbor segment of given segment.
     * @throws EndOfRoadException If given segment do not have neighbor, have more than one neighbors or road goes in circle.
     * @throws IllegalArgumentException If given segment is not connecting to this Point.
     */
    public Segment getNeighbor(Segment currentSegment, Point protectionPoint) throws EndOfRoadException, IllegalArgumentException {
        if(isEndOfRoad()) {
            throw new EndOfRoadException("This point is end of road. Given segment do not have neighbor or have more than one neighbors.");
        } else {
            if(this == protectionPoint) {
                throw new EndOfRoadException("Road goes in circle, in one direction and without any intersection.");
            } else {
                if(connectingSegments.get(0) == currentSegment) {       // If first connecting segment is given segment
                    return connectingSegments.get(1);                   //      second segment must be a neighbor
                } else {                                                // if first connecting segment is not given segment, so that first segment should be neighbor.
                    if(connectingSegments.get(1) != currentSegment) {   //      But maybe second connecting segment is also not given segment, meaning that...
                        throw new IllegalArgumentException("Given segment is not connected to this point.");
                    } else {
                        return connectingSegments.get(0);               // If second segment is given segment, first segment must be a neighbor.
                    }
                }
            }
        }
    }

    public void setType(List<Road> includingOnly, boolean isRightHandTraffic) {
        List<Road> inRoads = new ArrayList<>();
        List<Road> outRoads = new ArrayList<>();
        for(Road inRoad : incomingRoads) {
            if(includingOnly.contains(inRoad)) {
                inRoads.add(inRoad);
            }
        }
        for(Road outRoad : outgoingRoads) {
            if(includingOnly.contains(outRoad)) {
                outRoads.add(outRoad);
            }
        }
        setType(inRoads, outRoads, isRightHandTraffic);
    }

    private void setType(List<Road> inRoads, List<Road> outRoads, boolean isRightHandTraffic) {
        int inSize = inRoads.size();
        int outSize = outRoads.size();
        if(inSize == 2 && outSize == 2) { // Applied on all points
            Road rOut0 = outRoads.get(0);
            Road rOut1 = outRoads.get(1);
            Road rIn0 = inRoads.get(0);
            Road rIn1 = inRoads.get(1);
            Point out0 = rOut0.getPolyline().get(1);
            Point out1 = rOut1.getPolyline().get(1);
            Point in0 = rIn0.getPolyline().get(rIn0.getPolyline().size()-2);
            Point in1 = rIn1.getPolyline().get(rIn1.getPolyline().size()-2);
            double angleOut0Out1 = getAngle(out0, out1);
            double angleOut0In0 = getAngle(out0, in0);
            double angleOut0In1 = getAngle(out0, in1);
            if(rOut0.isBothWay() && (rOut0.getId() == rIn0.getId() || rOut0.getId() == rIn1.getId())) { // if out0 road is both way and if it have both way twin in road
                if(rOut1.isBothWay() && (rOut1.getId() == rIn1.getId() || rOut1.getId() == rIn0.getId())) { // If out1 road is both way and it have both way twin in road.
                    // check other roads. if one IN - START, if one OUT - END, if IN and OUT - START_END
                    if (outgoingRoads.size() == 2) {
                        type = START;
                    } else {
                        if(incomingRoads.size() == 2) {
                            type = END;
                        } else {
                            type = START_END;
                        }
                    }
                } else {    // If out1 is not both way road or it does not have its twin in road
                    // T3. At this point one both way road and two one way roads connects. Check hand and direction
                    if(angleOut0In0 == 0) {
                        // out1 and in1 are free roads
                        setTypeIfRightHandTraffic(isRightHandTraffic, angleOut0In1 < angleOut0Out1);
                    } else {
                        // out1 and in0 are free roads
                        setTypeIfRightHandTraffic(isRightHandTraffic, angleOut0In0 < angleOut0Out1);
                    }
                }
            } else {                                                                    // If out0 is not both way road or it does not have its twin in road
                if(rOut1.getId() == rIn1.getId() || rOut1.getId() == rIn0.getId()) { // If out1 road is both way and it have both way twin in road.
                    // T3. At this point one both way road and two one way roads connects. Check hand and direction
                    if(angleOut0Out1 == angleOut0In1) {
                        // out0 and in0 are free roads
                        setTypeIfRightHandTraffic(isRightHandTraffic, angleOut0In0 > angleOut0Out1);
                    } else {
                        // out0 and in1 are free roads
                        setTypeIfRightHandTraffic(isRightHandTraffic, angleOut0In1 > angleOut0Out1);
                    }
                } else { // If both out roads is not both way or they do not have twin in roads
                    // T2. All roads are free.
                    if(incomingRoads.size() > 2) {
                        if(outgoingRoads.size() > 2) {
                            type = START_END;
                        } else {
                            type = START;
                        }
                    } else {
                        if(outgoingRoads.size() > 2) {
                            type = END;
                        } else {
                            if( (angleOut0Out1 < angleOut0In0 && angleOut0Out1 < angleOut0In1) || (angleOut0Out1 > angleOut0In0 && angleOut0Out1 > angleOut0In1) ) {
                                type = INTERSECTION;
                            } else {
                                type = SPLIT_JOIN;
                            }
                        }
                    }
                }
            }
        } else {
            if(inSize >= 2 && outSize >= 2) {       // Applied on points, which don't have 2 in and 2 out roads.
                if(incomingRoads.size()-inSize > 0) {
                    if(outgoingRoads.size()-outSize > 0) {
                        type = START_END;
                    } else {
                        type = START;
                    }
                } else {
                    if(outgoingRoads.size()-outSize > 0) {
                        type = END;
                    } else {
                        type = INTERSECTION;
                    }
                }
            } else {
                if(inSize == 0) {   // Applied on points, where ( inSize=(0;1) AND outSize=[0;+X) ) OR ( inSize=[0;+X) AND outSize=(0;1) )
                    type = START;
                } else {
                    if(outSize == 0) {  // Applied on points, where ( inSize=1 AND outSize=[0;+X) ) OR ( inSize=[1;+X) AND outSize=(0;1) )
                        type = END;
                    } else {
                        if(inSize == 1) {               // Applied on points, where ( inSize=1 AND outSize=[1;+X) ) OR ( inSize=[1;+X) AND outSize=1 )
                            if(outSize == 1) {              // Applied on points, where inSize=1 AND outSize=[1;+X)
                                // check all roads - not filtered: if one out - start, if one in - end, else - star+end
                                if (outgoingRoads.size() == 1) {
                                    if(incomingRoads.size() == 1) {
                                        type = START_END;
                                    } else {
                                        type = START;
                                    }
                                } else {
                                    if(incomingRoads.size() == 1) {
                                        type = END;
                                    } else {
                                        if(outRoads.get(0).getId() == inRoads.get(0).getId()) {
                                            type = START_END;
                                        } else {
                                            if(incomingRoads.size() == 2) {
                                                if(outgoingRoads.size() == 2) {
                                                    if(outRoads.get(0).isBothWay()) {
                                                        type = END;
                                                    } else {
                                                        if(inRoads.get(0).isBothWay()) {
                                                            type = START;
                                                        } else {
                                                            type = START_END;
                                                        }
                                                    }
                                                } else {
                                                    if(outRoads.get(0).isBothWay()) {
                                                        type = END;
                                                    } else {
                                                        type = START_END;
                                                    }
                                                }
                                            } else {
                                                if(outgoingRoads.size() == 2) {
                                                    if(inRoads.get(0).isBothWay()) {
                                                        type = START;
                                                    } else {
                                                        type = START_END;
                                                    }
                                                } else {
                                                    type = START_END;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {                        // Applied on points, where inSize=1 AND outSize=[2;+X)
                                // SPLIT? But firstly, check is other roads not coming here.
                                if(incomingRoads.size() == 1) {
                                    type = SPLIT;
                                } else {        // If additional in road exist
                                    if(outgoingRoads.size() > 2) {  // If splitting into more than two roads
                                        if(outgoingRoads.size() == outSize) {   // If all out roads belongs to junction outRoads
                                            type = INTERSECTION;
                                        } else {    // If there is additional out roads
                                            type = START_END;
                                        }
                                    } else {                // There inSize=[2;+X] AND outSize=2
                                        if(incomingRoads.size() == 2) {
                                            // One additional in road
                                            //If all separate - START. If is both way road SPLIT_JOIN OR INTERSECTION. solve as In=2 AND Out=2

                                            Road rIn1;
                                            if(incomingRoads.get(0) == inRoads.get(0)) {
                                                rIn1 = incomingRoads.get(1);
                                            } else {
                                                rIn1 = incomingRoads.get(0);
                                            }
                                            Point out0 = outRoads.get(0).getPolyline().get(1);
                                            Point out1 = outRoads.get(1).getPolyline().get(1);
                                            Point in0 = inRoads.get(0).getPolyline().get(inRoads.get(0).getPolyline().size()-2);
                                            Point in1 = rIn1.getPolyline().get(rIn1.getPolyline().size()-2);

                                            if(inRoads.get(0).getId() == outRoads.get(0).getId()) { // in1 and out1 is free
                                                //double angleOut0Out1 = getAngle(out0, out1);
                                                //double angleOut0In1 = getAngle(out0, in1);
                                                //setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In1 < angleOut0Out1);
                                                type = START;
                                            } else {
                                                if(inRoads.get(0).getId() == outRoads.get(1).getId()) { // in1 and out0 is free
                                                    //double angleOut0Out1 = getAngle(out0, out1);
                                                    //double angleOut0In1 = getAngle(out0, in1);
                                                    //setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In1 > angleOut0Out1);
                                                    type = START;
                                                } else {
                                                    if(rIn1.getId() == outRoads.get(0).getId()) {    // in0 and out1 is free
                                                        double angleOut0Out1 = getAngle(out0, out1);
                                                        double angleOut0In0 = getAngle(out0, in0);
                                                        setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In0 < angleOut0Out1);
                                                    } else {
                                                        if(rIn1.getId() == outRoads.get(1).getId()) { // in0 and out0 is free
                                                            double angleOut0Out1 = getAngle(out0, out1);
                                                            double angleOut0In0 = getAngle(out0, in0);
                                                            setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In0 > angleOut0Out1);
                                                        } else {
                                                            type = START;
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            type = START;
                                        }
                                    }
                                }
                            }
                        } else {                        // Applied on points, where inSize=[2;+X) AND outSize=1
                            // JOIN? But firstly, check is other roads not coming here.
                            if(outgoingRoads.size() == 1) {
                                type = JOIN;
                            } else {        // If additional out road exist
                                if(incomingRoads.size() > 2) {    // If joining from more than two roads
                                    if(incomingRoads.size() == inSize) {   // If all in roads belongs to junction outRoads
                                        type = INTERSECTION;
                                    } else {    // If there is additional in roads
                                        type = START_END;
                                    }
                                } else {                // There inSize=2 AND outSize=[2;+X]
                                    if(outgoingRoads.size() == 2) {
                                        // One additional out road
                                        //If all separate - END. If is both way road - SPLIT_JOIN OR INTERSECTION. solve as In=2 AND Out=2

                                        Road rOut1;
                                        if(outgoingRoads.get(0) == outRoads.get(0)) {
                                            rOut1 = outgoingRoads.get(1);
                                        } else {
                                            rOut1 = outgoingRoads.get(0);
                                        }
                                        Point out0 = outRoads.get(0).getPolyline().get(1);
                                        Point out1 = rOut1.getPolyline().get(1);
                                        Point in0 = inRoads.get(0).getPolyline().get(inRoads.get(0).getPolyline().size()-2);
                                        Point in1 = inRoads.get(1).getPolyline().get(inRoads.get(1).getPolyline().size()-2);

                                        if(inRoads.get(0).getId() == outRoads.get(0).getId()) { // in1 and out1 is free
                                            //double angleOut0Out1 = getAngle(out0, out1);
                                            //double angleOut0In1 = getAngle(out0, in1);
                                            //setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In1 < angleOut0Out1);
                                            type = END;
                                        } else {
                                            if(inRoads.get(0).getId() == rOut1.getId()) { // in1 and out0 is free
                                                double angleOut0Out1 = getAngle(out0, out1);
                                                double angleOut0In1 = getAngle(out0, in1);
                                                setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In1 > angleOut0Out1);
                                            } else {
                                                if(inRoads.get(1).getId() == outRoads.get(0).getId()) {    // in0 and out1 is free
                                                    //double angleOut0Out1 = getAngle(out0, out1);
                                                    //double angleOut0In0 = getAngle(out0, in0);
                                                    //setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In0 < angleOut0Out1);
                                                    type = END;
                                                } else {
                                                    if(inRoads.get(1).getId() == rOut1.getId()) { // in0 and out0 is free
                                                        double angleOut0Out1 = getAngle(out0, out1);
                                                        double angleOut0In0 = getAngle(out0, in0);
                                                        setTypeIfRightHandTrafficSplitJoin(isRightHandTraffic, angleOut0In0 > angleOut0Out1);
                                                    } else {
                                                        type = END;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        type = END;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setTypeIfRightHandTrafficSplitJoin(boolean isRightHandTraffic, boolean condition) {
        if(condition) {
            if(isRightHandTraffic) {
                type = SPLIT_JOIN;
            } else {
                type = INTERSECTION;
            }
        } else {
            if(isRightHandTraffic) {
                type = INTERSECTION;
            } else {
                type = SPLIT_JOIN;
            }
        }
    }

    private void setTypeIfRightHandTraffic(boolean isRightHandTraffic, boolean condition) {
        if(condition) {
            if(isRightHandTraffic) {
                type = SPLIT_JOIN;
            } else {
                if(incomingRoads.size() == 2) {
                    if(outgoingRoads.size() == 2) {
                        type = INTERSECTION;
                    } else {
                        type = END;
                    }
                } else {
                    if(outgoingRoads.size() == 2) {
                        type = START;
                    } else {
                        type = START_END;
                    }
                }
            }
        } else {
            if(isRightHandTraffic) {
                if(incomingRoads.size() == 2) {
                    if(outgoingRoads.size() == 2) {
                        type = INTERSECTION;
                    } else {
                        type = END;
                    }
                } else {
                    if(outgoingRoads.size() == 2) {
                        type = START;
                    } else {
                        type = START_END;
                    }
                }
            } else {
                type = SPLIT_JOIN;
            }
        }
    }

    public int getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Road> getIncomingRoads() {
        return incomingRoads;
    }

    public List<Road> getOutgoingRoads() {
        return outgoingRoads;
    }

    public int getLatitudeBucket() {
        return latitudeBucket;
    }

    public int getLongitudeBucket() {
        return longitudeBucket;
    }
}
