package com.nullpointers.toutmate.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Moment implements Parcelable {
    private String key;
    private String fileName;
    private String formatName;
    private String date;
    private String photoPath;

    public Moment() {
        //required for firebase
    }

    public Moment(String key, String fileName, String formatName, String date, String photoPath) {
        this.key = key;
        this.fileName = fileName;
        this.formatName = formatName;
        this.date = date;
        this.photoPath = photoPath;
    }

    protected Moment(Parcel in) {
        key = in.readString();
        fileName = in.readString();
        formatName = in.readString();
        date = in.readString();
        photoPath = in.readString();
    }

    public static final Creator<Moment> CREATOR = new Creator<Moment>() {
        @Override
        public Moment createFromParcel(Parcel in) {
            return new Moment(in);
        }

        @Override
        public Moment[] newArray(int size) {
            return new Moment[size];
        }
    };

    public String getKey() {
        return key;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFormatName() {
        return formatName;
    }

    public String getDate() {
        return date;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(photoPath);
        dest.writeString(fileName);
        dest.writeString(formatName);
        dest.writeString(date);
    }
}
