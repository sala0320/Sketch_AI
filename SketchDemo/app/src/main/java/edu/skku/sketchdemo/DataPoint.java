package edu.skku.sketchdemo;

public class DataPoint {

    private double x;
    private double y;
    private double z;
    private String filename;

    public DataPoint(double x, double y, double z, String filename){
        this.x = x;
        this.y = y;
        this.z = z;
        this.filename = filename;
    }

    public DataPoint(DataPoint dataPoint){
        this.x = dataPoint.getX();
        this.y = dataPoint.getY();
        this.z = dataPoint.getY();
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

    public double getZ() { return z; }

    public void setZ(double z) { this.z = z; }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}