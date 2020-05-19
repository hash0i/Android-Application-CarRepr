package com.fyp.amenms.Activities.order;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.RequestHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MyOrdersActivity extends AppCompatActivity {


    private ArrayList<RequestHelperClass> data;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView orders_list;
    private MyOrdersAdapters adapter;

    private ProgressDialog progressDialog;
    SharedPreferences login_data;

    private DatabaseReference requestsDbReference;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference firebaseTypeReference;

    SessionManager sessionManager;

    boolean isProvider = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_orders_layout);


        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top
        }

        login_data = getSharedPreferences(Constants.PREFS_NAME,MODE_PRIVATE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        sessionManager = new SessionManager(this);
        rootNode = FirebaseDatabase.getInstance();
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER)) {
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_PROVIDER);
            isProvider = true;
        } else {
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_USER);
            isProvider = false;
        }
        setData();
        initFirebaseListeners();
    }

    void initFirebaseListeners() {
        fAuth = FirebaseAuth.getInstance();
        requestsDbReference = rootNode.getReference(Constants.FD_REQUESTS_NOTE);
        Query query;
        if (isProvider) {
            query = requestsDbReference.orderByChild("providerUid").equalTo(fAuth.getCurrentUser().getUid());
        } else {
            query = requestsDbReference.orderByChild("userUid").equalTo(fAuth.getCurrentUser().getUid());
        }
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        RequestHelperClass request = requestSnap.getValue(RequestHelperClass.class);
                        Log.e("Request Event", request.getDescription());
                        data.add(request);
                    }
                    adapter = new MyOrdersAdapters(MyOrdersActivity.this, data);
                    orders_list.setAdapter(adapter);
                } else
                    Toast.makeText(MyOrdersActivity.this, "No record found", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void setData() {

        orders_list = (RecyclerView) findViewById(R.id.my_orders_list);
        orders_list.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(MyOrdersActivity.this);
        orders_list.setLayoutManager(linearLayoutManager);
    }

    ////get data from server ////////
    private void showDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    public void custOrderList( String client_id)  {

        /*Log.e("client_id",client_id+"");
        asHclient.setURLEncodingEnabled(false);
        asHclient.setTimeout(60000);

        asHclient.get(URLManagerClass.GET_CLIENT_ORDERS+"?client_id="+client_id, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

                showDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }

                try {
                    final String responseString = new String(responseBody, "UTF-8");

                    addDataToModel(responseString);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                    Log.e("failure",new String(responseBody));

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }


                error.printStackTrace(System.out);
            }

            @Override
            public void onRetry(int retryNo) {
                // Request was retried
            }

            @Override
            public void onFinish() {

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });

*/
    }

    public void workerOrderList( String worker_id)  {

        Log.e("worker_id",worker_id+"");
        /*asHclient.setURLEncodingEnabled(false);
        asHclient.setTimeout(60000);

        asHclient.get(URLManagerClass.GET_WORKERS_ORDERS+"?worker_id="+worker_id, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

                showDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }

                try {
                    final String responseString = new String(responseBody, "UTF-8");

                    addDataToModel(responseString);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                Log.e("failure",new String(responseBody));

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }


                error.printStackTrace(System.out);
            }

            @Override
            public void onRetry(int retryNo) {
                // Request was retried
            }

            @Override
            public void onFinish() {

                if (progressDialog.isShowing() && progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });*/


    }

    public void addDataToModel(String response) {
        /*try {
            Log.e("my_orders",response);


            JSONObject response_ = new JSONObject(response);
            int errorCode = response_.getInt("errorCode");
            String msg = response_.getString("message");

            data = new ArrayList<>();
            if (errorCode == 0) {

                JSONArray jsonArray = response_.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    MyOrdersObj myOrdersObj = new MyOrdersObj();
                    myOrdersObj.setOrder_id(jsonObject.getString("id"));
                    myOrdersObj.setOrder_desc(jsonObject.getString("request_description"));
                    myOrdersObj.setOrder_place(jsonObject.getString("order_place"));
                    myOrdersObj.setOrder_date_time(jsonObject.getString("order_datetime"));
                    myOrdersObj.setOrder_status(jsonObject.getString("order_status"));

                    data.add(myOrdersObj);
                }

                adapter = new MyOrdersAdapters(MyOrders.this, data);
                orders_list.setAdapter(adapter);


            }  else if  (errorCode == 1) {
                Constants.showAlertMessage(MyOrders.this,"",msg);


            }
            else {
                Constants.showAlertMessage(MyOrders.this,"",msg);

            }




        } catch (Exception ex) {
            Log.d("Exception", "exception " + ex.getMessage());
        }*/
    }
}

