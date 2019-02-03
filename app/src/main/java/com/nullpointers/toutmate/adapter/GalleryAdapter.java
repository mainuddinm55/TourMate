package com.nullpointers.toutmate.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nullpointers.toutmate.MainActivity;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.fragment.FullScreenFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryItemHolder> {
    private Context context;
    private List<Moment> momentList = new ArrayList<>();
    private MainActivity activity;


    public GalleryAdapter(Context context, List<Moment>momentList){
        this.context = context;
        this.momentList = momentList;
        activity = (MainActivity) context;
    }
    @NonNull
    @Override
    public GalleryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_list_item,parent,false);
        return new GalleryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemHolder holder, int position) {
        Moment moment = momentList.get(position);
        ImageView imageView = holder.galleryImageView;
        int height = imageView.getLayoutParams().height;
        int width = imageView.getLayoutParams().width;

        Log.e("ImageView Height", "Height: "+height);
        Log.e("ImageView Width", "Width: "+width);
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(moment.getPhotoPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/width, photoH/height);
        Log.e("ScaleFactor = ", "ScaleFactor: "+scaleFactor );

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(moment.getPhotoPath(), bmOptions);
        imageView.setImageBitmap(bitmap);


    }

    @Override
    public int getItemCount() {
        return momentList.size();
    }

    public class GalleryItemHolder extends RecyclerView.ViewHolder{
        private ImageView galleryImageView;

        public GalleryItemHolder(View itemView) {
            super(itemView);
            galleryImageView = itemView.findViewById(R.id.galleryImage);
            galleryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Bundle bundle = new Bundle();
                    FullScreenFragment fullScreenFragment = new FullScreenFragment() ;
                    bundle.putInt("position",position);
                    bundle.putParcelableArrayList("momentList", (ArrayList<? extends Parcelable>) momentList);
                    fullScreenFragment.setArguments(bundle);
                    FragmentTransaction  ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.mainLayout,fullScreenFragment,"fullScreenFragment");
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
    }
}
