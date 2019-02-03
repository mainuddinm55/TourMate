package com.nullpointers.toutmate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nullpointers.toutmate.Model.DateConverter;
import com.nullpointers.toutmate.Model.Expense;
import com.nullpointers.toutmate.R;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListAdapter extends BaseAdapter {
    private List<Expense> expenseList = new ArrayList<>();
    private Context context;

    public int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ExpenseListAdapter(List<Expense> expenseList, Context context) {
        this.expenseList = expenseList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return expenseList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expense_list_item,parent,false);
        }
        TextView expenseNameTV = convertView.findViewById(R.id.expenseName);
        TextView expenseDateTV = convertView.findViewById(R.id.date);
        TextView expenseAmountTV = convertView.findViewById(R.id.amount);

        DateConverter converter = new DateConverter();

        expenseNameTV.setText(expenseList.get(position).getExpenseName());
        expenseDateTV.setText(converter.getDateInString(expenseList.get(position).getDate()));
        expenseAmountTV.setText(expenseList.get(position).getExpenseAmount()+" TK");
        return convertView;
    }

}
