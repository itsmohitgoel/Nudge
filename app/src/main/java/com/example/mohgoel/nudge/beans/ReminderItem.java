package com.example.mohgoel.nudge.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class ReminderItem implements Parcelable {

    // member variables
    private String id;
    private String name;
    private String description;
    private String createdOn;
    private String remindOn;
    private ArrayList<String> reminderImages;

    public ReminderItem() {
    }


    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public ArrayList<String> getReminderImages() {
        return reminderImages;
    }

    public void setReminderImages(ArrayList<String> reminderImages) {
        this.reminderImages = reminderImages;
    }

    public String getRemindOn() {
        return remindOn;
    }

    public void setRemindOn(String remindOn) {
        this.remindOn = remindOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(createdOn);
        parcel.writeString(remindOn);
        parcel.writeStringList(reminderImages);
    }


    protected ReminderItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        createdOn = in.readString();
        remindOn = in.readString();
        reminderImages = in.createStringArrayList();
    }

    public static final Creator<ReminderItem> CREATOR = new Creator<ReminderItem>() {
        @Override
        public ReminderItem createFromParcel(Parcel in) {
            return new ReminderItem(in);
        }

        @Override
        public ReminderItem[] newArray(int size) {
            return new ReminderItem[size];
        }
    };

}
