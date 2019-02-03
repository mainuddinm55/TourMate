package com.nullpointers.toutmate.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nullpointers.toutmate.Model.User;
import com.nullpointers.toutmate.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends Fragment {
    private CircularImageView userPhotoImageView;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView userMobileTextView;
    private Button userProfileUpdateButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;



    public UserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        userPhotoImageView = view.findViewById(R.id.userPhotoImageView);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        userMobileTextView = view.findViewById(R.id.userMobileTextView);
        userProfileUpdateButton = view.findViewById(R.id.updateUserProfileButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user!=null){
            getActivity().setTitle("User Information");
            rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
            userRef = rootRef.child(user.getUid());
            Log.e("userId", "User Id =  "+user.getUid() );
            final List<User> userList = new ArrayList<>();

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userList.clear();
                    for (DataSnapshot postData: dataSnapshot.getChildren()){
                        User user = postData.getValue(User.class);
                        Log.e("username", "user name: "+user.getName() );
                        Log.e("useremail", "user Email: "+user.getEmail() );
                        Log.e("usermobile", "user Mobile: "+user.getMobile() );
                        userList.add(user);
                    }
                    Picasso.get().load(userList.get(1).getPhotoPath())
                            .placeholder(R.drawable.user)
                            .fit()
                            .into(userPhotoImageView);
                    userNameTextView.setText(userList.get(1).getName());
                    userEmailTextView.setText(userList.get(1).getEmail());
                    userMobileTextView.setText(userList.get(1).getMobile());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            userProfileUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.mainLayout,new UpdateUserProfileFragment(),"updateUser");
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
        return view;
    }

}
