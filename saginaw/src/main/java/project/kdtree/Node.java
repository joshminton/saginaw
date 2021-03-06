package project.kdtree;

import javafx.util.Pair;
import project.map.Graph;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable{
    private double[] point;
    private int id;
    private Node left;
    private Node right;
    private int depth;
    private boolean leaf;
    private ArrayList<Integer> ids;

    public void setPoint(double[] point) {
        this.point = point;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    protected Node(int id, double[] point, int depth, boolean isLeaf){
        this.point = point;
        this.depth = depth;
        if(isLeaf){
            this.ids = new ArrayList<>();
            ids.add(id);
            leaf = true;
        } else {
            this.id = id;
            leaf = false;
        }
        this.left = null;
        this.right = null;
    }

    public boolean isLeaf(){
        return leaf;
    }

    public Node getLeft(){
        return left;
    }

    public Node getRight() {
        return right;
    }

    public double[] getPoint(){
        return point;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        if(leaf) {
            ids.add(id);
        } else {
            this.id = id;
        }
    }

    public int getDepth() {
        return depth;
    }

    public Pair<Integer, Double> findClosest(double[] searchLoc, ArrayList<double[]> dictionary){
        int closest = ids.get(0);
        double closestDistance = Double.MAX_VALUE;
        double thisDist;
        for(int id : ids){
            thisDist = Graph.haversineDistance(searchLoc, dictionary.get(id));
            if(thisDist < closestDistance){
                closest = id;
                closestDistance = thisDist;
            }
        }
        System.out.println("Closest: " + closestDistance);
        return new Pair<>(closest, closestDistance);
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public int size(){
        int size = 1;
        if(isLeaf()){
            return ids.size();
        } else {
            if(getLeft() != null){
                size += getLeft().size();
            }
            if(getRight() != null){
                size += getRight().size();
            }
        }
        return size;
    }
}