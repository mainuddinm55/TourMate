package com.nullpointers.toutmate.fragment;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nullpointers.toutmate.Model.User;
import com.nullpointers.toutmate.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateUserProfileFragment extends Fragment implements View.OnClickListener {

    private CircularImageView userPhotoImageView;
    private FloatingActionButton takePhotoButton;
    private EditText userNameEditText;
    private EditText userMobileEditText;
    private CheckBox isPasswordChangeCK;
    private LinearLayout oldPasswordLinearLayout;
    private EditText oldPasswordEditText;
    private LinearLayout newPasswordLinearLayout;
    private EditText newPasswordEditText;
    private LinearLayout confirmPasswordLinearLayout;
    private EditText confirmPasswordEditText;
    private Button updateProfileButton;
    private ProgressBar imageUploadProgress;


    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private StorageReference rootStorageRef;
    private StorageTask uploadTask;


    private DatabaseReference rootRef;
    private DatabaseReference userRef;

    private List<User> userList = new ArrayList<>();

    private Uri imageUri;
    private String imageDownloadUrl;
    private String userName;
    private String userMobile;
    private String userPassword;
    private boolean isPasswordChange = false;
    private String oldPassword;
    private String userEmail;


    public UpdateUserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_update_user_profile, container, false);
        userPhotoImageView = view.findViewById(R.id.userPhotoIV);
        takePhotoButton = view.findViewById(R.id.takePhotoBtn);
        userNameEditText = view.findViewById(R.id.userNameEditText);
        userMobileEditText = view.findViewById(R.id.userMobileEditText);
        isPasswordChangeCK = view.findViewById(R.id.isPasswordChangeCheckBox);
        //oldPasswordLinearLayout = view.findViewById(R.id.oldPasswordLinearLayout);
        //oldPasswordEditText = view.findViewById(R.id.oldPasswordEditText);
        newPasswordLinearLayout = view.findViewById(R.id.newPasswordLinearLayout);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmPasswordLinearLayout = view.findViewById(R.id.confirmPasswordLinearLayout);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        updateProfileButton = view.findViewById(R.id.updateButton);
        imageUploadProgress = view.findViewById(R.id.uploadingProgress);


        userPhotoImageView.setOnClickListener(this);
        takePhotoButton.setOnClickListener(this);
        updateProfileButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        getActivity().setTitle("Update User Information");
        if (user!=null){
            rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
            userRef = rootRef.child(user.getUid());

            userEmail = user.getEmail();
            Log.e("Email", "Current User Email"+userEmail );
            rootStorageRef = FirebaseStorage.getInstance().getReference().child("User Photos");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postData: dataSnapshot.getChildren()){
                        User userInfo = postData.getValue(User.class);
                        userList.add(userInfo);
                    }

                    if (userList.size()>0){
                        userNameEditText.setText(userList.get(1).getName());
                        userMobileEditText.setText(userList.get(1).getMobile());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            isPasswordChangeCK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        isPasswordChange = true;
                        //oldPasswordLinearLayout.setVisibility(view.VISIBLE);
                        newPasswordLinearLayout.setVisibility(View.VISIBLE);
                        confirmPasswordLinearLayout.setVisibility(View.VISIBLE);
                    }else {
                        isPasswordChange = false;
                        //oldPasswordLinearLayout.setVisibility(View.GONE);
                        newPasswordLinearLayout.setVisibility(View.GONE);
                        confirmPasswordLinearLayout.setVisibility(View.GONE);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.userPhotoIV:
            case R.id.takePhotoBtn:
                openFileChooser();
                break;
            case R.id.updateButton:
                updateUser();
            break;

        }
    }

    private void updateUser() {
        if (user!=null){
            if (userNameEditText.getText().toString().trim().isEmpty()){
                userNameEditText.setError("Name Required");
                userNameEditText.requestFocus();
                return;
            }

           /* if (oldPasswordEditText.getText().toString().isEmpty()){
                oldPasswordEditText.setError("Enter old Password");
                oldPasswordEditText.requestFocus();
                return;
            }*/

            if (isPasswordChange){
                if (newPasswordEditText.getText().toString().isEmpty() || newPasswordEditText.getText().toString().length()<6) {
                    newPasswordEditText.setError("Password must be more then 6 Character");
                    newPasswordEditText.requestFocus();
                    return;
                }
                if (!confirmPasswordEditText.getText().toString().equals(newPasswordEditText.getText().toString())){
                    confirmPasswordEditText.setError("Password Does not Matched");
                    confirmPasswordEditText.requestFocus();
                    return;
                }

                userName = userNameEditText.getText().toString();
                userMobile = userMobileEditText.getText().toString();
                userPassword = newPasswordEditText.getText().toString();
                uploadFile();

            }else {
                userName = userNameEditText.getText().toString();
                userMobile = userMobileEditText.getText().toString();
                //oldPassword = oldPasswordEditText.getText().toString();
                uploadFile();

            }
        }

    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(userPhotoImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        imageUploadProgress.setVisibility(View.VISIBLE);
        if (imageUri!=null){
            StorageReference fileStorageRef = rootStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            uploadTask = fileStorageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    imageUploadProgress.setProgress(0);
                                }
                            }, 500);
                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_LONG).show();
                            imageDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                            userRef.child("UserInfo").child("name").setValue(userName);
                            userRef.child("UserInfo").child("mobile").setValue(userMobile);
                            userRef.child("UserInfo").child("photoPath").setValue(imageDownloadUrl);
                            Log.e("Password Status", "Is Password Change: "+isPasswordChange );
                            if (isPasswordChange){
                                //userEmail = user.getEmail();
                                Log.e("Email", "onSuccess: "+userEmail );
                                //firebaseAuth.signInWithEmailAndPassword(userEmail,oldPassword);
                                //user = firebaseAuth.getCurrentUser();
                                user.updatePassword(userPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            imageUploadProgress.setVisibility(View.GONE);
                                            Toast.makeText(getContext(), "User Updated", Toast.LENGTH_SHORT).show();
                                            Log.e("Changed Password", "onComplete: "+userPassword );
                                            gotoUserProfile();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        imageUploadProgress.setVisibility(View.GONE);
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Error", "onFailure: " + e.getMessage() );
                                    }
                                });
                            }else {
                                imageUploadProgress.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "User Updated", Toast.LENGTH_SHORT).show();
                                gotoUserProfile();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            imageUploadProgress.setProgress((int) progress);
                        }
                    });
        } else {
            userRef.child("UserInfo").child("name").setValue(userName);
            userRef.child("UserInfo").child("mobile").setValue(userMobile);
            userRef.child("UserInfo").child("photoPath").setValue(null);
            Log.e("Password Status", "Is Password Change: "+isPasswordChange );
            userEmail = user.getEmail();
            Log.e("Email", "onSuccess: "+userEmail );
            if (isPasswordChange){
                user.updatePassword(userPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            imageUploadProgress.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "User Updated", Toast.LENGTH_SHORT).show();
                            Log.e("Changed Password", "onComplete: "+userPassword );
                            gotoUserProfile();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imageUploadProgress.setVisibility(View.GONE);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Error", "onFailure: " + e.getMessage() );
                    }
                });
            }else {
                imageUploadProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "User Updated", Toast.LENGTH_SHORT).show();
                gotoUserProfile();
            }
            /*imageUploadProgress.setVisibility(View.GONE);
            Toast.makeText(getContext(), "User Updated", Toast.LENGTH_SHORT).show();*/
        }

    }

    private void gotoUserProfile() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainLayout,new UserProfileFragment());
        ft.commit();
    }
}
