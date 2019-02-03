package com.nullpointers.toutmate.Model;

public class Expense {
    private String key;
    private String expenseName;
    private double expenseAmount;
    private String comment;
    private long date;

    public Expense(String key, String expenseName, double expenseAmount, String comment, long date) {
        this.key = key;
        this.expenseName = expenseName;
        this.expenseAmount = expenseAmount;
        this.comment = comment;
        this.date = date;
    }


    public Expense() {
        //required for Firebase database
    }

    public String getKey() {
        return key;
    }

    public String getExpenseName() {
        return expenseName;
    }

    public double getExpenseAmount() {
        return expenseAmount;
    }

    public String getComment() {
        return comment;
    }

    public long getDate() {
        return date;
    }

}
