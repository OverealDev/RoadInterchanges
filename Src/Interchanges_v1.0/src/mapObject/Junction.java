package mapObject;

import java.util.ArrayList;
import java.util.List;

public class Junction {

    private int id;
    private List<CrossingPoint> crossingPoints = new ArrayList<>();
    private List<Road> roads = new ArrayList<>();
    private int numberOfConflictRoads = 0;
    private int numberOfIntersectionPoints = 0;
    private int numberOfStartPoints = 0;
    private int numberOfEndPoints = 0;
    private int numberOfLevels = 0;


    public Junction(CrossingPoint crossingPoint, int id) {
        this.id = id;
        crossingPoint.setJunction(this);
        crossingPoints.add(crossingPoint);
        for(Road road : crossingPoint.getConnectingRoads()) {
            road.addCrossingPointsToJunction(this);
        }
    }

    public void analyse(boolean isRightHandTraffic) {
        List<Point> setPoints = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        for (Road road : roads) {
            for (Integer layer : road.getLayers()) {
                if (!levels.contains(layer)) {
                    levels.add(layer);
                }
            }
            if (!setPoints.contains(road.getStart())) {
                road.getStart().setType(roads, isRightHandTraffic);
                setPoints.add(road.getStart());
            }
            if (!setPoints.contains(road.getEnd())) {
                road.getEnd().setType(roads, isRightHandTraffic);
                setPoints.add(road.getEnd());
            }
        }
        numberOfLevels = levels.size();
        for(Point point : setPoints) {
            switch (point.getType()) {
                case Point.INTERSECTION:    numberOfIntersectionPoints++;
                                            break;
                case Point.START:           numberOfStartPoints++;
                                            break;
                case Point.END:             numberOfEndPoints++;
                                            break;
                case Point.START_END:       numberOfStartPoints++;
                                            numberOfEndPoints++;
                                            break;
                default:                    break;
            }
        }
        for(Road road : roads) {
            if( (road.getStart().getType() == Point.JOIN && road.getEnd().getType() == Point.SPLIT) || (!road.isBothWay() && road.getStart().getType() == Point.SPLIT_JOIN && road.getEnd().getType() == Point.SPLIT_JOIN) ) {
                numberOfConflictRoads++;
            }
        }
    }

    public void addCrossingPoint(CrossingPoint newCrossingPoint) {
        if(!crossingPoints.contains(newCrossingPoint)){
            crossingPoints.add(newCrossingPoint);
        }
    }

    public void addRoad(Road newRoad) {
        if(!roads.contains(newRoad)) {
            roads.add(newRoad);
        }
    }

    public List<Road> getRoads() {
        return roads;
    }

    public int getNumberOfCrossingPoints() {
        return crossingPoints.size();
    }

    public int getNumberOfSides() {
        return Math.min(numberOfStartPoints, numberOfEndPoints);
    }

    public int getNumberOfConflictRoads() {
        return numberOfConflictRoads;
    }

    public int getNumberOfIntersectionPoints() {
        return numberOfIntersectionPoints;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public int getId() {
        return id;
    }
}
