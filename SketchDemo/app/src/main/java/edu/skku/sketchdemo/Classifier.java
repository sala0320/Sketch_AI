package edu.skku.sketchdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Classifier {

    private int K;
    private double splitRatio;
    private double accuracy = 0;
    private int featureLen = 1280;
    private int featureNum = 537;

    public ArrayList<DataPoint> dataPointList;
    private List<String> neighborList;
    private double[][] featureSet;

    public Classifier(){
        K = 5;
        splitRatio = 0.8;
        dataPointList = new ArrayList<>();
        neighborList = new ArrayList<>();
        featureSet = new double[featureNum + 1][featureLen];
    }

    public List<String> classify(DataPoint point){
        HashMap<String, Integer> hashMap = new HashMap<>(); // 초기 용량 지정 가능!!! 나중에 전체 개수 정해지면 초기용량 ㄱㄱ
        neighborList.clear();
        List<DataInfo> distanceList = calculateDistances(point);
        List<Double> minDistanceList = new ArrayList<>();
        int distanceListLen = distanceList.size();
        for (int i = 0; i < K; i++){
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < distanceListLen; j++){
                if (distanceList.get(j).getDistance() < min){
                    min = distanceList.get(j).getDistance();
                    minIndex = j;
                }
            }
            String minFileName = distanceList.get(minIndex).getFilename();
            minDistanceList.add(min);
            neighborList.add(minFileName);
            DataInfo newMinDataInfo = new DataInfo(distanceList.get(minIndex).getFilename(), Double.MAX_VALUE); // set min data distance to max value
            distanceList.set(minIndex, newMinDataInfo); // exclude min data
        }
        System.out.println("minDistanceList: " + minDistanceList);
        System.out.println("neighborList: " + neighborList);
        return neighborList;
    }

    private List<DataInfo> calculateDistances(DataPoint point){
        List<DataInfo> distanceList = new ArrayList<>();
        for (DataPoint dataPoint: dataPointList){
            double distance = this.calculateEuclideanDistance(point.getX(), point.getY(), point.getZ(), dataPoint.getX(), dataPoint.getY(), dataPoint.getZ());
            String filename = dataPoint.getFilename();
            DataInfo dataInfo = new DataInfo(filename, distance);
            distanceList.add(dataInfo);
        }
        return distanceList;
    }

    public double calculateEuclideanDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double xSquare = Math.pow(x1 - x2, 2);
        double ySquare = Math.pow(y1 - y2, 2);
        double zSquare = Math.pow(z1 - z2, 2);
        double distance = Math.sqrt(xSquare + ySquare + zSquare);
        return distance;
    }
    public double calculateManhattenDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double x = Math.abs(x1 - x2);
        double y = Math.abs(y1 - y2);
        double z = Math.abs(z1 - z2);
        double distance = x + y + z;
        return distance;
    }
    public void reset() {
        dataPointList.clear();
        neighborList.clear();
    }

    public int getK() {
        return K;
    }

    public void setK(int k) {
        K = k;
    }

    public double getSplitRatio() {
        return splitRatio;
    }

    public void setSplitRatio(double splitRatio) {
        this.splitRatio = splitRatio;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getFeatureLen() { return featureLen; }

    public void setFeatureLen(int featureLen) { this.featureLen = featureLen; }

    public int getFeatureNum() { return featureNum; }

    public void setFeatureNum(int featureNum) { this.featureNum = featureNum; }

    public List<DataPoint> getDataPointList() {
        return dataPointList;
    }

    public void setDataPointList(List<DataPoint> dataPointList) {
        this.dataPointList.clear();
        this.dataPointList.addAll(dataPointList);
    }
    public void setDataPointListElement(int idx, double x, double y) {
        DataPoint dp = getListDataPointElement(idx);
        dp.setX(x);
        dp.setY(y);
    }

    public void setDataPointListElement(DataPoint point) {
        this.dataPointList.add(point);
    }
    public DataPoint getListDataPointElement(int idx) {
        return this.dataPointList.get(idx);
    }

    public List<String> getListNeighbors() {
        return neighborList;
    }

    public void setListNeighbors(List<String> listNeighbors) {
        this.neighborList.clear();
        this.neighborList = listNeighbors;
    }

    public double[][] getFeatureSet() {
        return featureSet;
    }

    public void setFeatureSet(double[][] featureSet) {
        this.featureSet = featureSet;
    }

    public double[] getFeatureSetRow(int index) { return featureSet[index]; }

    public void setFeatureSetRow(double[] featureSet, int index) {
        this.featureSet[index] = featureSet;
    }

    public void setFeatureSetElement(double value, int row, int col) {
        this.featureSet[row][col] = value;
    }

}