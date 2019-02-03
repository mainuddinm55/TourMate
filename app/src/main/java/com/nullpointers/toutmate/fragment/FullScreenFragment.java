package com.nullpointers.toutmate.fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.adapter.FullScreenImageAdapter;
import com.nullpointers.toutmate.adapter.GalleryAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FullScreenFragment extends Fragment {

    private List<Moment> momentList = new ArrayList<>();

    private ViewPager viewPager;
    private FullScreenImageAdapter adapter;

    public FullScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_full_screen, container, false);

        viewPager = view.findViewById(R.id.galleryViewPager);


        Bundle bundle = getArguments();
        if (bundle!=null){
            int position = bundle.getInt("position");
            momentList = bundle.getParcelableArrayList("momentList");
            adapter = new FullScreenImageAdapter(getContext(),momentList);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position);

        }
        return view;
    }


}
