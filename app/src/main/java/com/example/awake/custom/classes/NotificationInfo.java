package com.example.awake.custom.classes;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class NotificationInfo {
    private Bitmap readUndreadNotif;
    private String notifTitle, notifDate, message, notifDateShort, documentName;
    private ArrayList<Long> drowsyList = new ArrayList<>();
    private ArrayList<Long> yawnList = new ArrayList<>();
    private ArrayList<Long> timeValues = new ArrayList<>();
    private boolean wasRead=false;


    // Constructor
    public NotificationInfo(String notifTitle, String notifDate, String message, String notiifDateShort, ArrayList drowsyList, ArrayList yawnList, ArrayList timeValues, boolean wasRead, String documentName, Bitmap readUndreadNotif) {
        this.readUndreadNotif = readUndreadNotif;
        this.notifTitle = notifTitle;
        this.notifDate =notifDate;
        this.message= message;
        this.notifDateShort = notiifDateShort;
        this.drowsyList = drowsyList;
        this.yawnList = yawnList;
        this.timeValues.addAll(timeValues);
        this.wasRead= wasRead;
        this.documentName= documentName;
    }

    // Getter methods
    public Bitmap getReadUndreadNotif() {
        return readUndreadNotif;
    }

    public String getDocumentName(){
        return documentName;
    }

    public String getNotifTitle() {
        return notifTitle;
    }

    public String getMessage(){
        return message;
    }

    public String getNotifDateShort(){
        return notifDateShort;
    }
    public ArrayList<Integer> getDrowsyList(){

        ArrayList<Integer> intList = new ArrayList<>();
        for (Long value : drowsyList) {
            intList.add(value.intValue());
        }
        return intList;
    }
    public ArrayList<Integer> getYawnList(){
        ArrayList<Integer> intList = new ArrayList<>();
        for (Long value : yawnList) {
            intList.add(value.intValue());
        }
        return intList;
    }
    public ArrayList<Long> getTimeValues(){
        return timeValues;
    }
    public boolean getWasRead(){
        return wasRead;
    }

    public String getNotifDate() {
        return notifDate;
    }

    // Setter methods (optional)

    public void setNotifTitle(String notifTitle) {
        this.notifTitle = notifTitle;
    }
    public  Bitmap getReadUnreadBitmap(){
        return readUndreadNotif;
    }

    public int getLowestTime(){
        return Math.toIntExact(timeValues.get(0)) ;
    }

    public void setWasRead(boolean trulse){
        wasRead=trulse;
    }

    public int getHighestTime(){
        return  (int)  timeValues.get(1).intValue();
    }

    public int getHighestYLength(){
        return  (int)  timeValues.get(2).intValue();
    }
}