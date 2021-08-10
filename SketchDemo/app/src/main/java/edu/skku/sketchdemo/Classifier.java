package edu.skku.sketchdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import smile.math.distance.EuclideanDistance;

public class Classifier {

    private int K;
    private double splitRatio;
    private double accuracy = 0;
    private int featureLen = 1280;
    private int featureNum = 598; // TODO

    public ArrayList<DataPoint> listDataPoint;
    private List<DataPoint> listTestData;
    private List<DataPoint> listTestValidator;
    private List<String> listNeighbors;
    private double[][] featureSet;

    public Classifier(){
        K = 5;
        splitRatio = 0.8;
        listDataPoint = new ArrayList<>();
        listTestData = new ArrayList<>();
        listTestValidator = new ArrayList<>();
        featureSet = new double[featureNum + 1][featureLen];
    }

    public List<String> classify(DataPoint point){ // 외부에서 들어옴.
        HashMap<String, Integer> hashMap = new HashMap<>(); // 초기 용량 지정 가능!!! 나중에 전체 개수 정해지면 초기용량 ㄱㄱ
        List<DataInfo> listDistance = calculateDistances(point);
        for (int i = 0; i < K; i++){
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < listDistance.size(); j++){
                if (listDistance.get(j).getDistance() < min){
                    min = listDistance.get(j).getDistance();
                    minIndex = j;
                }
            }
            String nn = listDataPoint.get(minIndex).getFilename();
            listNeighbors.add(nn);
            DataInfo tempDataInfo = new DataInfo(null, Double.MAX_VALUE);
            listDistance.set(minIndex, tempDataInfo); // 제일 가까운 애를 맥스로 갱신 - 다음 번에는 얘는 자연스럽게 제외됨.
        }
        return listNeighbors;
    }

    private List<DataInfo> calculateDistances(DataPoint point){ // 주어진 점과 모든 점간의 거리를 계산해서 리스트로 리턴함
        List<DataInfo> listDistance = new ArrayList<>();
        for (DataPoint dataPoint:listDataPoint){
            double distance = this.calculateEuclideanDistance(point.getX(), point.getY(), dataPoint.getX(), dataPoint.getY());
            String filename = dataPoint.getFilename();
            DataInfo dataInfo = new DataInfo(filename, distance); // 이거 메모리 설정 안해줘도 되나?
            listDistance.add(dataInfo);
        }
        return listDistance;
    }

    public double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
        double xSquare = Math.pow(x1 - x2, 2);
        double ySquare = Math.pow(y1 - y2, 2);
        double distance = Math.sqrt(xSquare + ySquare);
        return distance;
    }

    public void reset() {
        listDataPoint.clear();
        listNeighbors.clear();
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

    public List<DataPoint> getListDataPoint() {
        return listDataPoint;
    }

    public void setListDataPoint(List<DataPoint> listDataPoint) {
        this.listDataPoint.clear();
        this.listDataPoint.addAll(listDataPoint);
    }

    public void setListDataPointElement(DataPoint point) {
        this.listDataPoint.add(point);
    }

    public List<DataPoint> getListTestValidator() { return listTestValidator; }

    public void setListTestValidator(List<DataPoint> listTestValidator) {
        this.listTestValidator.clear();
        this.listTestValidator = listTestValidator;
    }

    public List<String> getListNeighbors() {
        return listNeighbors;
    }

    public void setListNeighbors(List<String> listNeighbors) {
        this.listNeighbors.clear();
        this.listNeighbors = listNeighbors;
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