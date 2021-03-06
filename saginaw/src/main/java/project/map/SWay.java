package project.map;

import java.util.ArrayList;
import java.util.List;

public class SWay {

    public SWay(long[] wayNodes){
        this.wayNodes = wayNodes;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    private double length;

    public SWay(){}

    public long[] getWayNodes() {
        return wayNodes;
    }

//    public long[] getWayNodesArray() {
//        long[] asArray = new long[wayNodes.size()];
//        for(int i = 0; i < asArray.length; i++){
//            asArray[i] = wayNodes.get(i);
//        }
//        return asArray;
//    }

    public void setWayNodes(long[] wayNodes) {
        this.wayNodes = wayNodes;
    }

    public long[] wayNodes;

    public long getWayId() {
        return wayId;
    }

    public void setWayId(long wayId) {
        this.wayId = wayId;
    }

    private long wayId;

    public String print() {
        StringBuilder sb = new StringBuilder().append(String.valueOf(wayId));
        for (long n : wayNodes) {
            sb.append(String.valueOf(n) + " ");
        }
        return sb.toString();
    }

    public WayType getType() {
        return type;
    }

    public void setType(WayType type) {
        this.type = type;
    }

    WayType type;

    public RoadType getRoadType() {
        return roadType;
    }

    public void setRoadType(RoadType roadType) {
        this.roadType = roadType;
    }

    RoadType roadType;

}
