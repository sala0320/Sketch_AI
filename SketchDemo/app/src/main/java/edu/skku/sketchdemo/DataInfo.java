package edu.skku.sketchdemo;

public class DataInfo {
    private String filename;
    private double distance;

    public void setFilename(String file) {
        this.filename = file;
    }
    public void setDistance(double dist) {
        this.distance = dist;
    }
    public String getFilename() {
        return filename;
    }
    public double getDistance() {
        return distance;
    }
    public DataInfo(String filename, double dist) {
        this.filename = filename;
        this.distance = dist;
    }
    public DataInfo(DataInfo info) {
        this.filename = info.getFilename();
        this.distance = info.getDistance();
    }
}