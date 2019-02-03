package com.nullpointers.toutmate.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nullpointers.toutmate.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tiemoon on 4/26/2018.
 */

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    Context context;
    List<String> headingData = new ArrayList<>();
    HashMap<String, List<String>> childData = new HashMap<>();

    public ExpandableListViewAdapter(Context context, List<String> headingData, HashMap<String, List<String>> childData) {
        this.context = context;
        this.headingData = headingData;
        this.childData = childData;
    }

    @Override
    public int getGroupCount() {
        return headingData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(headingData.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headingData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(headingData.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String event = (String)getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_group, parent, false);
        }
        TextView eventTextView = convertView.findViewById(R.id.eventTextView);

        eventTextView.setText(event);
        eventTextView.setTypeface(null, Typeface.BOLD);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String expense = (String) getChild(groupPosition, childPosition);

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_child, parent, false);
        }

        TextView expenseTextView = convertView.findViewById(R.id.expenseTextView);
        expenseTextView.setText(expense);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
