package com.nullpointers.toutmate.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nullpointers.toutmate.Model.ForecastWeather;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FullScreenImageAdapter extends PagerAdapter {

    private Activity activity;
    private Context context;
    private List<Moment> momentList = new ArrayList<>();

    public FullScreenImageAdapter(Context context, List<Moment> momentList){
        this.context = context;
        this.momentList = momentList;
        activity = (Activity) context;
    }

    @Override
    public int getCount() {
        return momentList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.full_screen_image_item, container,false);
        ImageView fullScreenImageView = view.findViewById(R.id.galleryImageView);
        int h = fullScreenImageView.getLayoutParams().height;
        int w = fullScreenImageView.getLayoutParams().width;
        Log.e("h", "instantiateItem: "+h );
        Log.e("w", "instantiateItem: "+w );
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.e("Height = ", "height: "+height );
        Log.e("Width = ", "width: "+width);
        fullScreenImageView.setMinimumHeight(height);
        fullScreenImageView.setMinimumWidth(width);

        try {
            File file = new File(momentList.get(position).getPhotoPath());
            Uri uri = Uri.fromFile(file);
            Picasso.get().load(uri).fit()
                    .into(fullScreenImageView);
            //fullScreenImageView.setImageURI(uri);
            //Bitmap bitmap = BitmapFactory.decodeFile(momentList.get(position).getPhotoPath());
            //fullScreenImageView.setImageBitmap(bitmap);
            activity.setTitle(momentList.get(position).getFileName());
        }catch (Exception e){
            e.printStackTrace();
        }

        container.addView(fullScreenImageView);

        return fullScreenImageView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View)object);
    }



}
