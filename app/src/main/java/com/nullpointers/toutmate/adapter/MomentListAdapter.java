package com.nullpointers.toutmate.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.nullpointers.toutmate.MainActivity;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.fragment.FullScreenFragment;
import com.nullpointers.toutmate.fragment.MomentsFragment;

import java.util.ArrayList;
import java.util.List;

public class MomentListAdapter extends RecyclerView.Adapter<MomentListAdapter.MomentHolder> {

    Context context;
    List<Moment>momentList = new ArrayList<>();
    int pos;
    DatabaseReference momentRef;
    MainActivity activity;

    public int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public MomentListAdapter(Context context, List<Moment> momentList){
        this.context = context;
        this.momentList = momentList;
        momentRef = MomentsFragment.momentRef;
        activity = (MainActivity) context;
    }



    @NonNull
    @Override
    public MomentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.moment_list_item,parent,false);
        return new MomentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MomentHolder holder, final int position) {
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(position);
                return false;
            }
        });
        Moment moment = momentList.get(position);
        ImageView imageView = holder.photoImageView;
        int height = imageView.getLayoutParams().height;
        int width = imageView.getLayoutParams().width;
        Log.e("ImageView Height", "Height: "+height );
        Log.e("ImageView Width", "Width: "+width );
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

        holder.photoNameTextView.setText(moment.getFileName());
        holder.photoNameWithFormatTextView.setText(moment.getFormatName());
        holder.dateTextView.setText(moment.getDate());
    }


    @Override
    public int getItemCount() {
        return momentList.size();
    }

    public class MomentHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView photoImageView;
        private TextView photoNameTextView;
        private TextView photoNameWithFormatTextView;
        private TextView dateTextView;

        public MomentHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            photoNameTextView = itemView.findViewById(R.id.fileNameTextView);
            photoNameWithFormatTextView = itemView.findViewById(R.id.fileNameWithFormat);
            dateTextView = itemView.findViewById(R.id.captureDateTextView);
            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Bundle bundle = new Bundle();
                    FullScreenFragment fullScreenFragment = new FullScreenFragment() ;
                    bundle.putInt("position",position);
                    bundle.putParcelableArrayList("momentList", (ArrayList<? extends Parcelable>) momentList);
                    fullScreenFragment.setArguments(bundle);
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.mainLayout,fullScreenFragment,"fullScreenFragment");
                    ft.addToBackStack(null);
                    ft.commit();
                    //Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            activity.getMenuInflater().inflate(R.menu.delete_edit_item_menu,menu);
        }
    }


}
