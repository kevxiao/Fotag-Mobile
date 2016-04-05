package com.kevxiao.fotag;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ImageModel implements Parcelable, Serializable{

    // PUBLIC

    public ImageModel(int rt, String pt) {
        rating = rt;
        path = pt;
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    public void changeRating(int newRating) {
        if(newRating < 0) {
            newRating = 0;
        } else if(newRating > 5) {
            newRating = 5;
        }
        rating = newRating;
    }

    public int getRating() {
        return rating;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        return !((obj == null) || (getClass() != obj.getClass())) && ((ImageModel) obj).path.equals(this.path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rating);
        dest.writeString(path);
    }

    // PROTECTED

    protected ImageModel(Parcel in) {
        rating = in.readInt();
        path = in.readString();
    }

    // PRIVATE

    private int rating;
    private String path;
}
