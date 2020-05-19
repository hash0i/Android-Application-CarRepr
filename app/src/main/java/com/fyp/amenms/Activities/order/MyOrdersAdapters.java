package com.fyp.amenms.Activities.order;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.RequestHelperClass;

import java.util.ArrayList;


public class MyOrdersAdapters extends RecyclerView.Adapter<MyOrdersAdapters.CaptainDataViewHolder> {
    private ArrayList<RequestHelperClass> myOrdersList = new ArrayList<>();

    private Context _context;
    private View layoutView;
    private SharedPreferences app_date;


    public MyOrdersAdapters(Context context, ArrayList<RequestHelperClass> myOrdersList) {
        this.myOrdersList = myOrdersList;
        this._context = context;

    }

    @Override
    public MyOrdersAdapters.CaptainDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_item, parent, false);
        MyOrdersAdapters.CaptainDataViewHolder _ViewHolder = new MyOrdersAdapters.CaptainDataViewHolder(layoutView);


        return _ViewHolder;

    }

    @Override
    public void onBindViewHolder(final MyOrdersAdapters.CaptainDataViewHolder holder, final int position) {


        if (!myOrdersList.get(position).getDescription().isEmpty() || (!myOrdersList.get(position).getDescription().equalsIgnoreCase(null))) {
            holder.order_desc.setText("Description: " + myOrdersList.get(position).getDescription());
        } else {
            holder.order_desc.setText("N/A");
        }


        if (!myOrdersList.get(position).getOrderDateTime().isEmpty() || (!myOrdersList.get(position).getOrderDateTime().equalsIgnoreCase(null))) {
            holder.date_time.setText("Order Date & Time: " + myOrdersList.get(position).getOrderDateTime());
        } else {
            holder.date_time.setText("N/A");
        }


        if (!myOrdersList.get(position).getAddress().isEmpty() || (!myOrdersList.get(position).getAddress().equalsIgnoreCase(null))) {
            holder.location.setText("Location: " + myOrdersList.get(position).getAddress());
        } else {
            holder.location.setText("N/A");
        }


        if (myOrdersList.get(position).getStatus() == Constants.RequestStatus.ASSIGNED) {

            holder.order_status.setText("Order Status: Pending");
        } else if (myOrdersList.get(position).getStatus() == Constants.RequestStatus.DONE) {

            holder.order_status.setText("Order Status: Completed");
        } else if (myOrdersList.get(position).getStatus() == Constants.RequestStatus.ACCEPTED) {

            holder.order_status.setText("Order Status: Accepted");
        } else if (myOrdersList.get(position).getStatus() == Constants.RequestStatus.CANCELLED) {

            holder.order_status.setText("Order Status: Cancelled");
        } else {
            holder.order_status.setText("Order Status: N/A");

        }
        //  holder.order_status.setText("Order Status: "+myOrdersList.get(position).getOrder_status());


        holder.main_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(layoutView.getContext(), OrderDetailsActivity.class);
                i.putExtra("order", myOrdersList.get(position));
                layoutView.getContext().startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return myOrdersList.size();
    }

    class CaptainDataViewHolder extends RecyclerView.ViewHolder {

        public TextView location, date_time, order_desc, order_status;
        public LinearLayout main_container;


        public CaptainDataViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
            date_time = itemView.findViewById(R.id.date_time);
            order_desc = itemView.findViewById(R.id.order_desc);
            main_container = itemView.findViewById(R.id.main_container);
            order_status = itemView.findViewById(R.id.order_status);
        }
    }


}


