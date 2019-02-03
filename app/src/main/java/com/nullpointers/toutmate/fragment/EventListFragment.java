package com.nullpointers.toutmate.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.Model.TourMateDatabase;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.adapter.EventListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListFragment extends Fragment {
    private TextView eventHeadingTextView;
    private RecyclerView eventListRecyclerView;
    private FloatingActionButton addButton;
    private TextView emptyEventTextView;

    private List<Event> eventList = new ArrayList<>();

    private TourMateDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private LinearLayoutManager layoutManager;

    private EventListAdapter adapter;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference eventRef;

    public EventListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        eventHeadingTextView = view.findViewById(R.id.eventHeadingTextView);
        eventListRecyclerView = view.findViewById(R.id.eventListRecyclerView);
        addButton = view.findViewById(R.id.addEventButton);
        emptyEventTextView = view.findViewById(R.id.showEmptyEvent);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child("Event");
        //eventKey = eventRef.push().getKey();

        database = new TourMateDatabase(getContext(),user);
        layoutManager = new LinearLayoutManager(getContext());

        eventListRecyclerView.setHasFixedSize(true);
        eventListRecyclerView.setLayoutManager(layoutManager);

        getActivity().setTitle("All Events");

        eventList = database.getAllEvent();
        adapter = new EventListAdapter(getContext(), eventList);
        eventListRecyclerView.setAdapter(adapter);
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot postDataSnapshot: dataSnapshot.getChildren()){
                    Event event = postDataSnapshot.getValue(Event.class);
                    eventList.add(event);
                }

                if (eventList.size()>0){
                    eventHeadingTextView.setText("Created Events");
                    emptyEventTextView.setVisibility(View.GONE);
                    adapter = new EventListAdapter(getContext(),eventList);
                    eventListRecyclerView.setAdapter(adapter);
                }else {
                    emptyEventTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainLayout,new AddEventFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }
}