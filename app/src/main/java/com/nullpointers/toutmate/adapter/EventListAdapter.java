package com.nullpointers.toutmate.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nullpointers.toutmate.MainActivity;
import com.nullpointers.toutmate.Model.DateConverter;
import com.nullpointers.toutmate.Model.Event;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.fragment.EventDetailsFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventHolder> {
    private DateConverter converter;
    private Context context;
    private MainActivity activity;
    private List<Event> eventList;
    private Date date = new Date();

    public EventListAdapter(Context context, List<Event> eventList){
        this.context = context;
        this.eventList = eventList;
        this.date = Calendar.getInstance().getTime();
        converter = new DateConverter();
        activity = (MainActivity) context;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item,parent,false);
        //activity = (MainActivity) parent.getContext();
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event event = eventList.get(position);
        String name = event.getEventName();
        long createDate = event.getCreatedDate();
        long startDate = event.getDepartureDate();
        long currentDate = (date.getTime())/1000;

        int dayLeft = (int) ((startDate - currentDate)/86400);

        holder.eventNameTextView.setText(name);
        holder.dayLeftTextView.setText(dayLeft+"");
        holder.createdDateTextView.setText("Created On : "+converter.getDateInString(createDate));
        holder.startDateTextView.setText("Start On : "+converter.getDateInString(startDate));
        if (dayLeft>0){
            holder.dayLeftTextView.setText(dayLeft+" Day Left");
        } else if (dayLeft<0){
            dayLeft = Math.abs(dayLeft);
            holder.dayLeftTextView.setText(dayLeft+" Day Past");
        }else {
            holder.dayLeftTextView.setText("Today");
        }

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventHolder extends RecyclerView.ViewHolder{
        private TextView eventNameTextView;
        private TextView createdDateTextView;
        private TextView startDateTextView;
        private TextView dayLeftTextView;

        public EventHolder(final View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventName);
            createdDateTextView = itemView.findViewById(R.id.createdDate);
            startDateTextView = itemView.findViewById(R.id.startDate);
            dayLeftTextView = itemView.findViewById(R.id.dayLeft);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    EventDetailsFragment fragment = new EventDetailsFragment();
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Event",eventList.get(position));
                    fragment.setArguments(bundle);
                    ft.replace(R.id.mainLayout,fragment,"eventDetails");
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }

    }

}
