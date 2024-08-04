package com.example.awake.custom.classes;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class BitmapInfo {
    private String name;
    private Bitmap bitmap;
    List<String> resultTitles = new ArrayList<>();


    public void setBitmap(Bitmap bitmap){
        this.bitmap=bitmap;
    }

    public void addResultList(String list){
        resultTitles.add(list);
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }

    public void clearList(){
        this.resultTitles.clear();
    }
    public void setList(List<String> list){
        this.resultTitles= new ArrayList<>(list);
    }

    public  List<String> getResultTitles(){
        return this.resultTitles;
    }


    // Getter methods for name, creationDate, etc.


}