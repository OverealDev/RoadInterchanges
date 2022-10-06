package mapObject;

import application.Map;
import exception.EndOfRoadException;
import exception.NoCrossingException;

import java.util.HashMap;
import java.util.LinkedList;

public class Segment {

    private Point start;
    private Point end;
    private boolean isBothWay;
    private int layer;
    private double length;
    private boolean isAggregated = false;
    private boolean isCorrupted = false;
    private Road road;

    public Segment(Point start, Point end, boolean isBothWay, int layer) {
        this.start = start;
        this.end = end;
        this.isBothWay = isBothWay;
        this.layer = layer;
        length = start.distanceTo(end);
    }

    @Override
    public String toString() {
        return String.format("\"MULTILINESTRING ((%s,%s))\"%s%s%s%d%n", start.toString(), end.toString(), Map.CSV_SPLITTER, ((isBothWay)? "B" : "F"), Map.CSV_SPLITTER, layer);
    }

    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    /**
     * Calculates is crossing between This Segment and given segmentB exist. If exist, creates Crossing and adds it to lists of crossings.
     * @param segmentB
     * @param points
     */
    public Point findCrossing(Segment segmentB, HashMap<Integer, HashMap<Integer, LinkedList<Point>>> points) throws NoCrossingException {
        double sA_sX = start.getLatitude();
        double sA_sY = start.getLongitude();
        double sB_sX = segmentB.getStart().getLatitude();
        double sB_sY = segmentB.getStart().getLongitude();

        double sA_x, sA_y, sB_x, sB_y;
        sA_x = end.getLatitude() - sA_sX;
        sA_y = end.getLongitude() - sA_sY;
        sB_x = segmentB.getEnd().getLatitude() - sB_sX;
        sB_y = segmentB.getEnd().getLongitude() - sB_sY;

        double s, t, d;
        if ((d = (-sB_x * sA_y + sA_x * sB_y)) != 0) {
            s = (-sA_y * (sA_sX - sB_sX) + sA_x * (sA_sY - sB_sY)) / d;
            t = (sB_x * (sA_sY - sB_sY) - sB_y * (sA_sX - sB_sX)) / d;

            if (s >= 0 && s <= 1 && t >= 0 && t <= 1 && start != segmentB.getStart() && start != segmentB.getEnd() && end != segmentB.getStart() && end != segmentB.getEnd()) {
                return Point.firstOrNew(sA_sX + (t * sA_x), sA_sY + (t * sA_y), points);
            }
        }
        throw new NoCrossingException();
    }

    /**
     * If traffic on this segments is valid to both ways, start and end points does not matter, next point will be opposite to previous point.
     * If traffic is valid to only one way, next point will be given default point.
     * @param prevPoint Previous point.
     * @param defaultPoint Default point, which is used if traffic is one way.
     * @return Default Point or Point opposite to previous, depending to traffic direction.
     */
    private Point getNextPoint(Point prevPoint, Point defaultPoint) {
        if(isBothWay) {
            if (end != prevPoint) {
                return end;
            } else {
                return start;
            }
        } else {
            return defaultPoint;
        }
    }

    /**
     * Method starts aggregating from this segment into given road. Also method sets traffic direction of given road.
     * @param road Pointer to road to which segments will be aggregated.
     * @return True - if aggregated successfully. False - if failed.
     */
    public boolean selfAggregate(Road road) {
        return goToStart(road, end, end);
    }

    /**
     *
     * @param road
     * @param prevPoint
     * @param protectionPoint Start point of aggregation. If this point is reached - path of the road goes in circle.
     * @return True - if aggregated successfully. False - if failed.
     */
    private boolean goToStart(Road road, Point prevPoint, Point protectionPoint) {
        Point nextPoint = getNextPoint(prevPoint, start);           // Next point for aggregation will be start point of this segment or opposite to previous point.
        try {                                                                       // Trying to get neighboring segment thru next point,
            if(nextPoint.getNeighbor(this, protectionPoint).goToStart(road, nextPoint, protectionPoint)) {  //    and proceed this method on that neighboring segment.
                return true;
            } else {
                throw new EndOfRoadException();                                     // Aggregation at neighbor segment failed, so this segment will be end of road.
            }
        } catch (EndOfRoadException e) {                                            // If next point is the end of the road and no valid neighbors exist,
            road.getPolyline().add(nextPoint);                                      //      set that next point as start of the road and
            road.setStartRelations();
            return goToEnd(road, nextPoint);                                        //      Because this segment is first in the path (start), so now go to the end.
        } catch (IllegalArgumentException e) {                                      // If nextPoint is not connecting to given This segment, this segment and road is corrupted.
            isCorrupted = true;                                                     // Setting this segment as aggregated to ignore it later.
            System.err.println(String.format("Method: goToStart(Road, Point, Point)%nSegment: %s%nPoint: %s%n%s", toString(), nextPoint.toString(), e));
            return false;
        }
    }

    /**
     *
     * @param road
     * @param protectionPoint
     * @return
     */
    private boolean goToEnd(Road road, Point protectionPoint) {
        this.road = road;                                           // This segment is part of given road.
        isAggregated = true;                                        // This segment is aggregated.
        road.addSegment(this);                                      // Adding this segment to road path.
        try {
            if(end.getNeighbor(this, protectionPoint).goToEnd(road, protectionPoint)) {
                return true;
            } else {
                throw new EndOfRoadException();                     // Aggregation at neighbor segment failed, so this segment will be end of road.
            }
        } catch (EndOfRoadException e) {
            road.setEndRelations();
            return true;
        } catch (IllegalArgumentException e) {
            isCorrupted = true;                                                    // Setting this segment as aggregated to ignore it later.
            System.err.println(String.format("Method: goToStart(Road, Point, Point)%nSegment: %s%nPoint: %s%n%s", toString(), end, e));
            return false;
        }
    }

    public Road getRoad() {
        return road;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public boolean isBothWay() {
        return isBothWay;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public double getLength() {
        return length;
    }

    public boolean isAggregated() {
        return isAggregated;
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }
}