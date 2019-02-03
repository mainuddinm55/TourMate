package com.nullpointers.toutmate.fragment;


import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nullpointers.toutmate.LoginActivity;
import com.nullpointers.toutmate.MainActivity;
import com.nullpointers.toutmate.Manifest;
import com.nullpointers.toutmate.Model.DateConverter;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.Model.Expense;
import com.nullpointers.toutmate.Model.Moment;
import com.nullpointers.toutmate.Model.NetworkConnectivity;
import com.nullpointers.toutmate.Model.TourMateDatabase;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.WeatherActivity;
import com.nullpointers.toutmate.adapter.ExpandableListViewAdapter;
import com.nullpointers.toutmate.adapter.ExpenseListAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private ExpandableListView eventExListView;
    private List<String> eventActionHeading = new ArrayList<>();
    private HashMap<String, List<String>> eventActionChild = new HashMap<>();

    private int pos;
    private AlertDialog expenseListDialog = null;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference eventRef;
    private DatabaseReference expenseRef;
    private DatabaseReference momentRef;

    private Event event;
    private TextView eventNameTextView;
    private TextView budgetStatusTextView;
    private TextView expensePercentTextView;
    private TextView totalBudgetPercentTextView;
    private TextView expenseDateET;
    private ProgressBar budgetStatusProgressBar;
    private double budget;

    private List<Expense>expenseList = new ArrayList<>();
    private List<Moment>momentList = new ArrayList<>();
    private double totalExpense;
    private double totalExpensePercent;
    private int progress;
    private String expensePercent = "0.0%";
    private String totalBudgetPercent = "100%";
    private String expenseKey;
    private TourMateDatabase database;

    private String eventName;
    private String eventKey;
    private String startingLocation;
    private String destinationLocation;
    private long createdDate;
    private long departureDate;


    private String fileNameWithFormat;
    private String fileName;
    private String captureDate;
    private String filePath;
    private String momentKey;

    private DateConverter converter = new DateConverter();

    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    long expenseDate;


    private LayoutInflater dialogInflater;

    public EventDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        eventExListView = view.findViewById(R.id.eventExListView);
        eventNameTextView = view.findViewById(R.id.eventNameTextView);
        budgetStatusTextView = view.findViewById(R.id.budgetStatusTextView);
        expensePercentTextView = view.findViewById(R.id.expensePercent);
        totalBudgetPercentTextView = view.findViewById(R.id.totalBudget);
        budgetStatusProgressBar = view.findViewById(R.id.budgetStatusProgressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        user  = firebaseAuth.getCurrentUser();

        if (user!=null){
            database = new TourMateDatabase(getContext(),user);
        }else {
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
        }
        rootRef = FirebaseDatabase.getInstance().getReference().child("Tour Mate");
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child("Event");


        dialogInflater = LayoutInflater.from(getContext());

        initializeData();
        ExpandableListViewAdapter expandableListViewAdapter = new ExpandableListViewAdapter(EventDetailsFragment.this.getActivity(), eventActionHeading, eventActionChild);
        eventExListView.setAdapter(expandableListViewAdapter);

        Bundle bundle = getArguments();
        if (bundle!=null){
           event  = (Event) bundle.getSerializable("Event");
           eventName = event.getEventName();
           eventKey = event.getKey();
           startingLocation = event.getStartingLocation();
           destinationLocation = event.getDestinationLocation();
           createdDate = event.getCreatedDate();
           departureDate = event.getDepartureDate();
           budget = event.getBudget();
           expenseRef = eventRef.child(eventKey).child("Expense");
           momentKey = database.getNewMomentKey(eventKey);
           momentRef = eventRef.child(eventKey).child("Moment");
           getActivity().setTitle(eventName);

            expenseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    expenseList.clear();
                    totalExpense = 0;
                    for (DataSnapshot postData: dataSnapshot.getChildren()){
                        Expense expense = postData.getValue(Expense.class);
                        expenseList.add(expense);
                        totalExpense = Math.round(totalExpense+expense.getExpenseAmount());
                    }

                    if (expenseList.size()>0){
                        totalExpensePercent = Math.round((totalExpense*100)/budget);
                        expensePercent = String.valueOf(totalExpensePercent+"%");
                        progress = (int) totalExpensePercent;
                        budgetStatusProgressBar.setProgress(progress);
                        String budgetStatus = String.format("Budget Status (%s/%s)",totalExpense,budget);
                        eventNameTextView.setText(eventName);
                        budgetStatusTextView.setText(budgetStatus);
                        expensePercentTextView.setText(expensePercent);
                        totalBudgetPercentTextView.setText(totalBudgetPercent);
                    }else {
                        budgetStatusProgressBar.setProgress(progress);
                        String budgetStatus = String.format("Budget Status (%s/%s)","0.0",budget);
                        eventNameTextView.setText(eventName);
                        budgetStatusTextView.setText(budgetStatus);
                        expensePercentTextView.setText(expensePercent);
                        totalBudgetPercentTextView.setText(totalBudgetPercent);
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



       eventExListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
           @Override
           public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

               String childClick = eventActionChild.get(eventActionHeading.get(groupPosition)).get(childPosition);

               if (childClick.equals("Add New Expense")){
                   newExpenseDialog();
               }else if (childClick.equals("View All Expense")){
                   viewAllExpenseDialog();
               }else if (childClick.equals("Add More Budget")){
                   addBudgetDialog();
               }else if (childClick.equals("Take a Photo")){
                   takePhoto();
               }else if (childClick.equals("View Gallery")){
                   viewGallery();
               }else if (childClick.equals("View All Moments")){
                   viewMoments();
               }else if (childClick.equals("Edit Event")){
                   editEvent();
               }else if (childClick.equals("Delete Event")){
                   deleteEvent();
               }

               return false;
           }
       });
        return  view;
    }

    private void deleteEvent() {

        final AlertDialog.Builder confirmDeleteDialog = new AlertDialog.Builder(getActivity());
        confirmDeleteDialog.setTitle("Delete Event.....");
        confirmDeleteDialog.setIcon(R.drawable.warning_icon);
        confirmDeleteDialog.setMessage("Do you want to delete event??");
        //final AlertDialog alertDialog = confirmDeleteDialog.create();
        confirmDeleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventRef.child(eventKey).removeValue();
                Toast.makeText(getActivity(), "Event Deleted", Toast.LENGTH_SHORT).show();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainLayout,new EventListFragment());
                ft.commit();

            }
        });
        confirmDeleteDialog.setNeutralButton("Cancel",null);
        confirmDeleteDialog.setNegativeButton("No",null);
        confirmDeleteDialog.show();

    }

    private void editEvent() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        AddEventFragment addEventFragment = new AddEventFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event",event);
        addEventFragment.setArguments(bundle);
        ft.replace(R.id.mainLayout,addEventFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void viewMoments() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        MomentsFragment momentsFragment = new MomentsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event",event);
        momentsFragment.setArguments(bundle);
        ft.replace(R.id.mainLayout,momentsFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void viewGallery() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        GalleryFragment galleryFragment = new GalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event",event);
        galleryFragment.setArguments(bundle);
        ft.replace(R.id.mainLayout,galleryFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.CAMERA},120);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager())!=null){
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file!=null){
                Uri photoUri = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider",file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                getActivity().startActivityForResult(intent,10);
            }
        }
    }

    private void addBudgetDialog() {
        View view = getLayoutInflater().inflate(R.layout.add_budget_dialog,null);
        AlertDialog.Builder addBudgetDialog = new AlertDialog.Builder(getContext());
        addBudgetDialog.setView(view);
        addBudgetDialog.setCancelable(true);
        final EditText budgetAmount = view.findViewById(R.id.addBudgetAmount);
        Button saveBtn = view.findViewById(R.id.addBudgetSaveBtn);
        Button cancelBtn = view.findViewById(R.id.addBudgetCancelBtn);
        final AlertDialog dialog = addBudgetDialog.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (budgetAmount.getText().toString().isEmpty()){
                    budgetAmount.setError("Budget Amount Required");
                    budgetAmount.requestFocus();
                    return;
                }
                double amount = Double.parseDouble(budgetAmount.getText().toString());
                budget = budget+amount;
                Event event = new Event(eventKey,eventName,startingLocation,destinationLocation,createdDate,departureDate,budget);
                Task<Void> task = database.updateEvent(eventKey,event);
                if (NetworkConnectivity.isNetworkAvailable(getContext())){
                    task.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getContext(), "Budget Added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(getContext(), "Budget Added", Toast.LENGTH_SHORT).show();
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
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void viewAllExpenseDialog() {

                AlertDialog.Builder expenseDialog = new AlertDialog.Builder(getContext());
                expenseDialog.setCancelable(false);
                View view = getLayoutInflater().inflate(R.layout.expense_list,null);
                ListView expenseListView = view.findViewById(R.id.expenseListView);
                TextView emptyExpense = view.findViewById(R.id.emptyExpense);
                ImageButton crossBtn = view.findViewById(R.id.crossButton);
                expenseDialog.setView(view);
                final AlertDialog dialog = expenseDialog.create();
                expenseListDialog = dialog;
                crossBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                if (expenseList.size()>0){
                    ExpenseListAdapter adapter = new ExpenseListAdapter(expenseList,getContext());
                    expenseListView.setAdapter(adapter);
                    registerForContextMenu(expenseListView);
                    expenseListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            pos = position;
                            return false;
                        }
                    });

                }else {
                    emptyExpense.setVisibility(View.VISIBLE);
                }
                dialog.show();

    }


    private void newExpenseDialog() {
        View view = dialogInflater.inflate(R.layout.add_new_expense,null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(view);
        dialog.setCancelable(false);
        final EditText expenseNameET = view.findViewById(R.id.expenseNameET);
        final EditText expenseAmountET = view.findViewById(R.id.expenseAmountET);
        final EditText expenseCommentET = view.findViewById(R.id.expenseCommentET);
        expenseDateET = view.findViewById(R.id.expenseDateTV);
        Button saveBtn = view.findViewById(R.id.saveBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        final AlertDialog alertDialog = dialog.create();
        expenseDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDate();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expenseNameET.getText().toString().isEmpty()){
                    expenseNameET.setError("Expense Name Required");
                    expenseNameET.requestFocus();
                    return;
                }if (expenseAmountET.getText().toString().isEmpty()){
                    expenseAmountET.setError("Amount Required");
                    expenseAmountET.requestFocus();
                    return;
                }if (expenseDateET.getText().toString().isEmpty()){
                    expenseDateET.setError("Date Required");
                    expenseDateET.requestFocus();
                    return;
                }

                String expenseName = expenseNameET.getText().toString().trim();
                double expenseAmount = Double.parseDouble(expenseAmountET.getText().toString().trim());
                String comment = expenseCommentET.getText().toString().trim();
                expenseKey = database.getNewExpenseKey(eventKey);
                Expense expense = new Expense(expenseKey,expenseName,expenseAmount,comment,expenseDate);


                totalExpense = totalExpense+ expenseAmount;

                if(totalExpense>budget)
                {
                    Toast.makeText(getContext(), "Exceeded Your Budget. Add More Budget then Try Again", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    totalExpense = 0;
                }
                else if (totalExpense < budget )
                {
                    Task<Void> task = database.addExpense(eventKey,expenseKey,expense);
                    if (NetworkConnectivity.isNetworkAvailable(getContext())){
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getContext(), "Expense Added", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        Toast.makeText(getContext(), "Expense Added", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }
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

    private void chooseDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this, year, month, day);
        datePickerDialog.show();
    }

    private void initializeData(){
        List<String> Expenditure = new ArrayList<>();
        List<String> Moments = new ArrayList<>();
        List<String> MoreOnEvent = new ArrayList<>();

        eventActionHeading = new ArrayList<>();
        eventActionHeading.add("Expenditure");
        eventActionHeading.add("Moments");
        eventActionHeading.add("More on Event");

        Expenditure.add("Add New Expense");
        Expenditure.add("View All Expense");
        Expenditure.add("Add More Budget");

        Moments.add("Take a Photo");
        Moments.add("View Gallery");
        Moments.add("View All Moments");

        MoreOnEvent.add("Edit Event");
        MoreOnEvent.add("Delete Event");

        eventActionChild.put(eventActionHeading.get(0), Expenditure);
        eventActionChild.put(eventActionHeading.get(1), Moments);
        eventActionChild.put(eventActionHeading.get(2), MoreOnEvent);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        expenseDate = converter.getDateInUnix(dayOfMonth+"/"+(month+1)+"/"+year);
        expenseDateET.setText(converter.getDateInString(expenseDate));
    }

    private File createImageFile () throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+6:00"));
        String timestamp = dateFormat.format(new Date());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:a");
        captureDate = dateFormat.format(new Date());
        String imageFileName = "JPEG_"+timestamp;
        fileNameWithFormat = imageFileName+".jpg";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir.list()!=null){
            String[] totalFiles = storageDir.list();
            int countTotalFile = totalFiles.length;
            fileName = "Capture "+countTotalFile;
        }else {
            fileName = "Capture 0";
            //Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT).show();
            Log.e("dir", "createImageFile: "+storageDir.getAbsolutePath());
        }

        File imageFile = File.createTempFile(imageFileName,".jpg",storageDir);
        filePath = imageFile.getAbsolutePath();
        Log.e("photo", "createImageFile: "+filePath);
        return imageFile;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(getActivity(), "fragment", Toast.LENGTH_SHORT).show();
        addMoment();

    }

    private void addMoment() {
        final Moment moment = new Moment(momentKey,fileName, fileNameWithFormat, captureDate, filePath);
        Task<Void> task = database.addMoment(eventKey,momentKey,moment);
        if (NetworkConnectivity.isNetworkAvailable(getContext())){
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getContext(), "Moment Added", Toast.LENGTH_SHORT).show();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        MomentsFragment momentsFragment = new MomentsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event",event);
                        momentsFragment.setArguments(bundle);
                        ft.replace(R.id.mainLayout,momentsFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }else {
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            Toast.makeText(getContext(), "Moment Added", Toast.LENGTH_SHORT).show();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            MomentsFragment momentsFragment = new MomentsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("event",event);
            momentsFragment.setArguments(bundle);
            ft.replace(R.id.mainLayout,momentsFragment);
            ft.addToBackStack(null);
            ft.commit();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu,v,menuInfo);
        menu.add(0, 0, 1, "Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deleteExpense();
                expenseListDialog.dismiss();
                return true;
            }
        });
    }


    private void deleteExpense() {
        Expense expense = expenseList.get(pos);
        expenseRef.child(expense.getKey()).removeValue();
        Toast.makeText(getActivity(), "Expense Deleted", Toast.LENGTH_SHORT).show();
    }
}