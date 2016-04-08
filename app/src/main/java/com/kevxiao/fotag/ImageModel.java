package com.kevxiao.fotag;

import android.media.ExifInterface;
import android.os.Parcel;
import android.os.Parcelable;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

public class ImageModel implements Parcelable, Serializable{

    // PUBLIC

    public ImageModel(String pt) {
        path = pt;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(new FileInputStream(new File(path))));
            // obtain the Exif directory
            ExifIFD0Directory directoryIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if(directoryIFD0 != null && directoryIFD0.containsTag(ExifIFD0Directory.TAG_RATING)) {
                // query the tag's value
                rating = directoryIFD0.getInt(ExifIFD0Directory.TAG_RATING);
            } else {
                rating = 0;
            }
        } catch (MetadataException | ImageProcessingException | IOException ex) {
            rating = 0;
        }

        try {
            ExifInterface exif = new ExifInterface(path);
            String comment = exif.getAttribute("UserComment");
            if(comment != null && comment.contains(RATING_STRING)) {
                int loc = comment.indexOf(RATING_STRING) + RATING_STRING.length();
                String rt = comment.substring(loc, loc + 1);
                rating = Integer.parseInt(rt);
            } else {
                setFotagExifRating();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        setFotagExifRating();
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

    private static final String RATING_STRING = "fotagRating:";

    private int rating;
    private String path;

    private void setFotagExifRating() {
        try {
            ExifInterface exif = new ExifInterface(path);
            String comment = exif.getAttribute("UserComment");
            String newComment;
            if(comment == null) {
                newComment = RATING_STRING + rating;
            } else if(comment.contains(RATING_STRING)) {
                int foundIdx = comment.indexOf(RATING_STRING);
                newComment = comment.substring(0, foundIdx) + RATING_STRING + rating + comment.substring(foundIdx + RATING_STRING.length() + 1);
            } else {
                newComment = comment + "\n" + RATING_STRING + rating;
            }
            exif.setAttribute("UserComment", newComment);
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
