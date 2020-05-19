package com.fyp.amenms.Utilities.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.amenms.Activities.MapsActivity;
import com.fyp.amenms.Activities.order.PlaceOrderActivity;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.ProviderHelperClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ProvidersAutoCompleteAdapter extends RecyclerView.Adapter<ProvidersAutoCompleteAdapter.PredictionHolder> implements Filterable {
    private static final String TAG = "PlacesAutoAdapter";
    private final DatabaseReference firebaseDb;
    private ArrayList<ProviderHelperClass> mResultList = new ArrayList<>();
    private Context mContext;
    private CharacterStyle STYLE_BOLD;
    private CharacterStyle STYLE_NORMAL;
    private ClickListener clickListener;

    public ProvidersAutoCompleteAdapter(Context context) {
        mContext = context;
        STYLE_BOLD = new StyleSpan(Typeface.BOLD);
        STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);
        firebaseDb = FirebaseDatabase.getInstance().getReference(Constants.TYPE_PROVIDER);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getPredictions(constraint.toString());
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    //notifyDataSetInvalidated();
                }
            }
        };
    }

    private synchronized ArrayList<ProviderHelperClass> getPredictions(String constraint) {

        final ArrayList<ProviderHelperClass> resultList = new ArrayList<>();
        final CountDownLatch done = new CountDownLatch(1);

        Query query = firebaseDb.orderByChild("expertise").startAt(constraint)
                .endAt(constraint + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot provider : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        resultList.add(provider.getValue(ProviderHelperClass.class));
                    }
                }
                done.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                done.countDown();
            }
        });
        // This method should have been called off the main UI thread. Block and wait for at most
        // 60s for a result from the API.
        try {
            done.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultList;

    }

    @NonNull
    @Override
    public PredictionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.workers_items_layout, viewGroup, false);
        return new PredictionHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionHolder mPredictionHolder, final int i) {
        mPredictionHolder.name.setText(mResultList.get(i).getName());
        mPredictionHolder.location.setText(mResultList.get(i).getAddress());
        mPredictionHolder.category.setText(mResultList.get(i).getExpertise());
        mPredictionHolder.email.setText(mResultList.get(i).getEmail());
        mPredictionHolder.phone_num.setText(mResultList.get(i).getMobNumber());
        mPredictionHolder.category.setText(mResultList.get(i).getExpertise());
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    public ProviderHelperClass getItem(int position) {
        return mResultList.get(position);
    }

    public interface ClickListener {
        void click(ProviderHelperClass provider);
    }

    public class PredictionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, category, email, phone_num, location, location_details, place_order, rate_per_hr;
        public LinearLayout main_container;
        public ImageView imageFile;

        PredictionHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            category = itemView.findViewById(R.id.category);

            email = itemView.findViewById(R.id.email);
            phone_num = itemView.findViewById(R.id.phone_num);
            location = itemView.findViewById(R.id.location);

            rate_per_hr = itemView.findViewById(R.id.rate_per_hr);
            place_order = itemView.findViewById(R.id.place_order);

            location_details = itemView.findViewById(R.id.location_details);
            main_container = itemView.findViewById(R.id.main_container);
            imageFile = itemView.findViewById(R.id.imageFile);

            itemView.setOnClickListener(this);
            location_details.setOnClickListener(this);
            place_order.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ProviderHelperClass item = mResultList.get(getAdapterPosition());
            if (v.getId() == R.id.workerItem_parent) {
                clickListener.click(item);
            } else if (v.getId() == R.id.location_details) {
                MapsActivity.workerlat = item.getLatitude() + "";
                MapsActivity.workerlng = item.getLongitude() + "";
                MapsActivity.workerName = item.getName();

                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                v.getContext().startActivity(intent);
            } else if (v.getId() == R.id.place_order) {
                PlaceOrderActivity.provider = item;

                Intent intent = new Intent(v.getContext(), PlaceOrderActivity.class);
                v.getContext().startActivity(intent);
            }
        }
    }
}