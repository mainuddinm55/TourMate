package com.nullpointers.toutmate.Model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Event implements Serializable {

    private String key;
    private String eventName;
    private String startingLocation;
    private String destinationLocation;
    private long createdDate;
    private long departureDate;
    private double budget;
    private List<Expense> expenseList = new ArrayList<>();
    private List<Moment> momentList = new ArrayList<>();

    public Event() {
        //required for firebase
    }

    public Event(String key, String eventName, String startingLocation, String destinationLocation,long createdDate, long departureDate, double budget) {
        this.key = key;
        this.eventName = eventName;
        this.startingLocation = startingLocation;
        this.destinationLocation = destinationLocation;
        this.createdDate = createdDate;
        this.departureDate = departureDate;
        this.budget = budget;
    }

    public Event(String key, String eventName, String startingLocation, String destinationLocation, long createdDate, long departureDate, double budget, List<Expense> expenseList, List<Moment> momentList) {
        this.key = key;
        this.eventName = eventName;
        this.startingLocation = startingLocation;
        this.destinationLocation = destinationLocation;
        this.createdDate = createdDate;
        this.departureDate = departureDate;
        this.budget = budget;
        this.expenseList = expenseList;
        this.momentList = momentList;
    }

    public String getEventName() {
        return eventName;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public long getDepartureDate() {
        return departureDate;
    }

    public double getBudget() {
        return budget;
    }

    public String getKey() {
        return key;
    }

    public List<Expense> getExpenseList() {
        return expenseList;
    }

    public List<Moment> getMomentList() {
        return momentList;
    }


}
