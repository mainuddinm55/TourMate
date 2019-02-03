package com.nullpointers.toutmate.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.nullpointers.toutmate.adapter.MomentListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MomentsFragment extends Fragment {

    private RecyclerView momentListRecyclerView;
    private LinearLayoutManager layoutManager;
    private MomentListAdapter adapter;
    private List<Moment> momentList = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference eventRef;
    public static DatabaseReference momentRef;

    private String eventKey;

    private Event event;

    private int position;


    public MomentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        momentListRecyclerView = view.findViewById(R.id.momentListRecyclerView);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child("Event");
        layoutManager = new LinearLayoutManager(getContext());
        momentListRecyclerView.setHasFixedSize(true);
        momentListRecyclerView.setLayoutManager(layoutManager);

        getActivity().setTitle("Moments");
        Bundle bundle = getArguments();
        if (bundle!=null){
            event = (Event) bundle.getSerializable("event");
            eventKey = event.getKey();
            momentRef = eventRef.child(eventKey).child("Moment");
            momentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    momentList.clear();
                    for (DataSnapshot postData : dataSnapshot.getChildren()){
                        Moment moment = postData.getValue(Moment.class);
                        momentList.add(moment);

                    }
                    if (momentList.size()>0){
                        adapter = new MomentListAdapter(getContext(),momentList);
                        momentListRecyclerView.setAdapter(adapter);
                        registerForContextMenu(momentListRecyclerView);

                    }else {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        position = -1;
        try {
            position = ((MomentListAdapter) momentListRecyclerView.getAdapter()).getPosition();
        } catch (Exception e) {
            Log.d("Contextual menu Error", e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteMoment();
                break;
            case R.id.action_edit:
                showChangeImageDialog();

        }
        return super.onContextItemSelected(item);
    }

    private void deleteMoment() {
        Moment moment = momentList.get(position);
        if (moment!=null){
            momentRef.child(moment.getKey()).removeValue();
            Toast.makeText(getContext(), "Moment Deleted", Toast.LENGTH_SHORT).show();
        }

    }

    private void showChangeImageDialog() {
        final Moment moment = momentList.get(position);
        final String momentKey = moment.getKey();
        final AlertDialog.Builder changeImageNameDialog = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.image_name_change_dialog,null);
        final EditText editText = view.findViewById(R.id.imageNameEditText);
        Button saveBtn = view.findViewById(R.id.saveButton);
        Button cancelBtn = view.findViewById(R.id.cancelButton);
        editText.setText(moment.getFileName());
        changeImageNameDialog.setView(view);
        final AlertDialog alertDialog = changeImageNameDialog.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty()){
                    editText.setText("Name Required");
                    editText.requestFocus();
                    return;
                }
                String fileName = editText.getText().toString();
                momentRef.child(momentKey).child("fileName").setValue(fileName);
                Toast.makeText(getContext(), "Image Name changed", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
