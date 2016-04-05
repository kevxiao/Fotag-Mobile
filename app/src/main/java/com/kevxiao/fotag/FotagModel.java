package com.kevxiao.fotag;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Observable;

public class FotagModel extends Observable implements Parcelable {

    // PUBLIC

    public FotagModel() {
        this.displayMode = false;
        this.filterMode = 0;
        this.searchMode = "";
        this.images = new ArrayList<>();
    }

    public static final Creator<FotagModel> CREATOR = new Creator<FotagModel>() {
        @Override
        public FotagModel createFromParcel(Parcel in) {
            return new FotagModel(in);
        }

        @Override
        public FotagModel[] newArray(int size) {
            return new FotagModel[size];
        }
    };

    public ArrayList<ImageModel> getImages() {
        return this.images;
    }

    public boolean getDisplayMode() {
        return this.displayMode;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public ImageModel getImage(String path) {
        for(ImageModel img : images) {
            if(img.getPath().equals(path)) {
                return img;
            }
        }
        return null;
    }

    public void addImage(ImageModel img) {
        this.images.add(img);
        this.setChanged();
        this.notifyObservers();
    }

    public void changeDisplayMode(boolean mode) {
        this.displayMode = mode;
        this.setChanged();
        this.notifyObservers();
    }

    public void changeFilterMode(int mode) {
        this.filterMode = mode;
        this.setChanged();
        this.notifyObservers();
    }

    public void changeSearchMode(String search) {
        this.searchMode = search;
        this.setChanged();
        this.notifyObservers();
    }

    public void clearSearchAndRating() {
        this.searchMode = "";
        this.filterMode = 0;
        this.setChanged();
        this.notifyObservers();
    }

    public void changeImageRating(String path, int rt) {
        for(ImageModel img : images) {
            if(img.getPath().equals(path) && img.getRating() != rt) {
                img.changeRating(rt);
                this.setChanged();
                this.notifyObservers();
            }
        }
    }

    public void clearImages() {
        this.images.clear();
        this.setChanged();
        this.notifyObservers();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(images);
        dest.writeByte((byte) (displayMode ? 1 : 0));
        dest.writeInt(filterMode);
        dest.writeString(searchMode);
    }

    // PROTECTED

    protected FotagModel(Parcel in) {
        images = in.createTypedArrayList(ImageModel.CREATOR);
        displayMode = in.readByte() != 0;
        filterMode = in.readInt();
        searchMode = in.readString();
    }

    // PRIVATE

    private ArrayList<ImageModel> images;
    private boolean displayMode; // false is grid, true is list
    private int filterMode;
    private String searchMode;
}
