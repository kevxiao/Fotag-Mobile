package com.kevxiao.fotag;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageArrayAdapter extends ArrayAdapter<ImageModel>{

    // STATIC

    static class ImageHolder
    {
        ImageView imgIcon;
        RatingBar imgRating;
        String imgPath;
    }

    // PUBLIC

    public ImageArrayAdapter(Context c, int r, ArrayList<ImageModel> l, FotagModel m) {
        super(c, r, l);
        context = c;
        layoutResourceId = r;
        imageList = l;
        fotagModel = m;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImageHolder imageHolder;
        ImageModel image = imageList.get(position);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        imageHolder = new ImageHolder();
        imageHolder.imgIcon = (ImageView)row.findViewById(R.id.card_photo);
        imageHolder.imgRating = (RatingBar)row.findViewById(R.id.card_rating);
        imageHolder.imgPath = image.getPath();
        if(imageHolder.imgRating != null) {
            imageHolder.imgRating.setOnRatingBarChangeListener(new OnImgRatingChangeListener(image, fotagModel));
        }
        if(imageHolder.imgIcon != null) {
            imageHolder.imgIcon.setOnClickListener(new OnImgClickListener(image, context));
        }

        row.setTag(imageHolder);

        if((int)imageHolder.imgRating.getRating() != image.getRating() && imageHolder.imgPath.equals(image.getPath())) {
            imageHolder.imgRating.setRating(image.getRating());
        }
        imageHolder.imgIcon.setImageBitmap(decodeSampledBitmapFromPath(image.getPath(), 250, 150));

        return row;
    }

    // PRIVATE

    private Context context;
    private int layoutResourceId;
    private ArrayList<ImageModel> imageList;
    private FotagModel fotagModel;

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static class OnImgRatingChangeListener implements RatingBar.OnRatingBarChangeListener {

        // PUBLIC

        public OnImgRatingChangeListener(ImageModel img, FotagModel md) {
            super();
            image = img;
            fotagModel = md;
        }

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if(image.getRating() != rating && fromUser) {
                fotagModel.changeImageRating(image.getPath(), (int) rating);
            }
        }

        // PRIVATE

        private ImageModel image;
        private FotagModel fotagModel;
    }

    private static class OnImgClickListener implements View.OnClickListener {

        // PUBLIC

        public OnImgClickListener(ImageModel img, Context c) {
            super();
            image = img;
            context = c;
        }

        @Override
        public void onClick(View v) {
            ImageView fsImg = (ImageView)((Activity)context).findViewById(R.id.full_screen_img);
            fsImg.setClickable(true);
            fsImg.setBackground(new ColorDrawable(ContextCompat.getColor(fsImg.getContext(), android.R.color.black)));
            // fsImg.setImageResource(context.getResources().getIdentifier(image.getPath(), null, context.getPackageName()));
            File imgFile = new File(image.getPath());
            if(imgFile.exists()){
                Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                fsImg.setImageBitmap(bm);
            }
        }

        // PRIVATE

        private ImageModel image;
        private Context context;
    }
}
