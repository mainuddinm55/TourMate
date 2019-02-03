package com.nullpointers.toutmate.fragment;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nullpointers.toutmate.LoginActivity;
import com.nullpointers.toutmate.Model.DateConverter;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.Model.Expense;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.Model.NetworkConnectivity;
import com.nullpointers.toutmate.Model.TourMateDatabase;
import com.nullpointers.toutmate.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddEventFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private EditText eventNameEditText;
    private EditText startLocationEditText;
    private EditText destLocationEditText;
    private EditText budgetEditText;
    private TextView departureDateTextView;
    private ImageButton departureDateButton;
    private Button createEventButton;

    private List<Expense> expenseList = new ArrayList<>();
    private List<Moment>momentList = new ArrayList<>();

    private long createdDate;
    private long departureDate;

    private DateConverter dateConverter;

    private TourMateDatabase database;
    private FirebaseUser user;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference eventRef;
    private DatabaseReference expenseRef;
    private DatabaseReference momentRef;

    private Bundle bundle;
    private Event event;
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    String eventName;
    String startLocation;
    String destLocation;
    String eventKey;
    double budget;

    public AddEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        eventNameEditText = view.findViewById(R.id.eventNameEditText);
        startLocationEditText = view.findViewById(R.id.startLocationEditText);
        destLocationEditText = view.findViewById(R.id.destLocationEditText);
        budgetEditText = view.findViewById(R.id.budgetEditText);
        departureDateTextView = view.findViewById(R.id.departureDateTextView);
        departureDateButton = view.findViewById(R.id.departureDateButton);
        createEventButton = view.findViewById(R.id.createEventButton);

        departureDateButton.setOnClickListener(this);
        departureDateTextView.setOnClickListener(this);
        createEventButton.setOnClickListener(this);

        dateConverter = new DateConverter();

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user!=null){
            database = new TourMateDatabase(getContext(),user);
        }else {
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
        }
        rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child("Event");

        getActivity().setTitle("Create New Event");

        bundle = getArguments();
        if (bundle!=null){
            event = (Event) bundle.getSerializable("event");
            eventKey = event.getKey();
            eventName = event.getEventName();
            startLocation = event.getStartingLocation();
            destLocation = event.getDestinationLocation();
            budget = event.getBudget();
            createdDate = event.getCreatedDate();
            departureDate = event.getDepartureDate();

            expenseRef = eventRef.child(eventKey).child("Expense");
            momentRef = eventRef.child(eventKey).child("Moment");

            eventNameEditText.setText(eventName);
            destLocationEditText.setText(destLocation);
            startLocationEditText.setText(startLocation);
            departureDateTextView.setText(dateConverter.getDateInString(departureDate));
            budgetEditText.setText(budget + "");
            createEventButton.setText("Update Event");
            getActivity().setTitle(eventName);

            expenseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    expenseList.clear();
                    for (DataSnapshot postData: dataSnapshot.getChildren()){
                        Expense expense = postData.getValue(Expense.class);
                        expenseList.add(expense);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


            momentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    momentList.clear();
                    for (DataSnapshot postData : dataSnapshot.getChildren()){
                        Moment moment = postData.getValue(Moment.class);
                        momentList.add(moment);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.departureDateButton:
            case R.id.departureDateTextView:
                chooseStartDate();
                break;
            case R.id.createEventButton:
                createEvent();
                break;
        }
    }

    private void createEvent() {
        if (eventNameEditText.getText().toString().isEmpty()){
            eventNameEditText.setError("Event Name Required");
            eventNameEditText.requestFocus();
            return;
        }
        if (destLocationEditText.getText().toString().isEmpty()){
            destLocationEditText.setError("Destination Location Required");
            destLocationEditText.requestFocus();
            return;
        }
        if (budgetEditText.getText().toString().isEmpty()){
            budget = 0;
        }else {
            budget = Double.parseDouble(budgetEditText.getText().toString().trim());
        }

        if (bundle!=null && eventKey!=null){
            eventName = eventNameEditText.getText().toString().trim();
            startLocation = startLocationEditText.getText().toString().trim();
            destLocation = destLocationEditText.getText().toString().trim();
            createdDate = event.getCreatedDate();

            event = new Event(eventKey,eventName,destLocation,startLocation,createdDate,departureDate,budget);
            Task<Void> task = database.updateEvent(eventKey,event);
            if (NetworkConnectivity.isNetworkAvailable(getContext())){
                task.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(), "Event Updated", Toast.LENGTH_SHORT).show();
                            FragmentTransaction ft = getActivity().
                                    getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.mainLayout,new EventListFragment());
                            ft.commit();
                        }else {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                Toast.makeText(getContext(), "Event Updated", Toast.LENGTH_SHORT).show();
                FragmentTransaction ft = getActivity().
                        getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainLayout,new EventListFragment());
                ft.commit();
            }

            if (expenseList.size()>0){
                for (int i = 0; i<expenseList.size(); i++){
                    Expense expense = expenseList.get(i);
                    database.addExpense(eventKey,expense.getKey(),expense);
                }
            }
            if (momentList.size()>0){
                for (int i = 0; i<momentList.size(); i++){
                    Moment moment = momentList.get(i);
                    database.addMoment(eventKey,moment.getKey(),moment);
                }
            }
        }else if (bundle==null){
            eventName = eventNameEditText.getText().toString().trim();
            startLocation = startLocationEditText.getText().toString().trim();
            destLocation = destLocationEditText.getText().toString().trim();
            eventKey = database.getNewEventKey();
            event = new Event(eventKey,eventName,startLocation,destLocation,createdDate,departureDate,budget);

            Task<Void> task = database.addEvent(event);
            if (NetworkConnectivity.isNetworkAvailable(getContext())){
                task.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(getContext(), "Event Created", Toast.LENGTH_SHORT).show();
                            FragmentTransaction ft = getActivity().
                                    getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.mainLayout,new EventListFragment());
                            ft.commit();
                        }else {
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                Toast.makeText(getContext(), "Event Created", Toast.LENGTH_SHORT).show();
                FragmentTransaction ft = getActivity().
                        getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainLayout,new EventListFragment());
                ft.commit();
            }
        }



    }

    private void chooseStartDate() {
        createdDate = dateConverter.getDateInUnix(day+"/"+(month+1)+"/"+year);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this,year,month,day);
        datePickerDialog.show();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        departureDate = dateConverter.getDateInUnix(dayOfMonth+"/"+(month+1)+"/"+year);
        departureDateTextView.setText(dayOfMonth+"/"+(month+1)+"/"+year);
    }
}
