package edu.skku.sketchdemo;

public class DataPoint {

    private double x;
    private double y;
    private String filename;

    public DataPoint(double x, double y, String filename){
        this.x = x;
        this.y = y;
        this.filename = filename;
    }

    public DataPoint(DataPoint dataPoint){
        this.x = dataPoint.getX();
        this.y = dataPoint.getY();
        this.filename = dataPoint.getFilename();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getFilename() {
        return filename;
    }

    public void setCategory(String filename) {
        this.filename = filename;
    }
}