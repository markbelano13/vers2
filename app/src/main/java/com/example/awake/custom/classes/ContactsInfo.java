package com.example.awake.custom.classes;

import android.graphics.Bitmap;

public class ContactsInfo {
    private Bitmap contactPhoto;
    private String contactName, contactNumber;

    // Constructor
    public ContactsInfo(String contactName, String contactNumber, Bitmap contactPhoto) {
        this.contactPhoto = contactPhoto;
        this.contactName = contactName;
        this.contactNumber =contactNumber;
    }

    // Getter methods
    public Bitmap getContactPhoto() {
        return contactPhoto;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    // Setter methods (optional)

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}