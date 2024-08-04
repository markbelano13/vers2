package com.example.awake.custom.classes;

public class DetectionLogsInfo {
    private String timestamp;
    private String detectionType, accuracy,location,inference, title, localPath,imageLink, responseTime;

    // Constructor
    public DetectionLogsInfo(String detectionType, String timestamp, String location, String accuracy, String inference, String title, String localPath, String imageLink, String responseTIme) {
        this.timestamp = timestamp;
        this.detectionType = detectionType;
        this.location =location;
        this.accuracy=accuracy;
        this.inference=inference;
        this.title=title;
        this.localPath=localPath;
        this.imageLink=imageLink;
        this.responseTime =responseTIme;
    }

    // Getter methods
    public String getTimestamp() {
        return timestamp;
    }

    public String getDetectionType() {
        return detectionType;
    }

    public String getLocation() {
        return location;
    }
    public String getAccuracy() {
        return accuracy;
    }
    public String getInference() {
        return inference;
    }
    public String getTitle() {
        return title;
    }
    public String getLocalPath(){
        return localPath;
    }
    public String getImageLink(){
        return imageLink;
    }
    public String getResponseTime(){
        return responseTime;
    }

    // Setter methods (optional)
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setDetectionType(String detectionType) {
        this.detectionType = detectionType;
    }
}