package com.example.awake.custom.classes;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationBuilder {
    ArrayList<Integer> drowsyList = new ArrayList<>();
    ArrayList<Integer> yawnList = new ArrayList<>();
    ArrayList<Double> averageDrowsyResponseList = new ArrayList<>();
    int highestTime=0;
    int lowestTime=24;
    int highestCount=0;
    boolean notifSent=false;

    public  NotificationBuilder(){
        yawnList = new ArrayList<>(Collections.nCopies(24, 0));
        drowsyList = new ArrayList<>(Collections.nCopies(24, 0));
    }

   public void addAverageResponseList(double num){
        averageDrowsyResponseList.add(num);
   }


    public ArrayList<Integer> getDrowsyList(){
        return drowsyList;
    }
    public  ArrayList<Integer> getYawnList(){
        return yawnList;
    }

    public void setDrowsyListIndex(int hour){
        drowsyList.set(hour, drowsyList.get(hour) + 1);

        if(hour>highestTime) {
            highestTime = hour + 1;
        }
        if (hour<lowestTime){
            lowestTime = hour+1;
        }
    }
    public void setYawnyListIndex(int hour){
        yawnList.set(hour, yawnList.get(hour) + 1);


        if(hour>highestTime) {
            highestTime = hour + 1;
        }
        if (hour<lowestTime){
            lowestTime = hour+1;
        }
    }

    public int  getHighestCount(){
        int maxValue = Integer.MIN_VALUE;
        for (int value : drowsyList) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        highestCount=maxValue;

        return highestCount;

    }

    public int getHighestTime(){
        return highestTime;
    }

    public int getLowestTime(){
        return lowestTime;
    }

    public void setNotifSent(boolean bool){
        this.notifSent=bool;
    }

    public int getYawnCount(){
        int sum=0;
        for (int number : yawnList) {
            sum += number;
        }
        return sum;
    }

    public int getDrowsyCount(){
        int sum=0;
        for (int number : drowsyList) {
            sum += number;
        }
        return sum;
    }

    public double getAverageDrowsyResponseSum(){
        double averageDrowsyResponseSum=0;
        for (double number : averageDrowsyResponseList) {
            if(number>0){
                averageDrowsyResponseSum += number;
            }
        }
        return averageDrowsyResponseSum/getDrowsyCount();
    }

    public void resetCountBuilder(){
        yawnList = new ArrayList<>(Collections.nCopies(24, 0));
        drowsyList = new ArrayList<>(Collections.nCopies(24, 0));
        averageDrowsyResponseList.clear();
    }



}
