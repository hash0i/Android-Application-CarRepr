package com.fyp.amenms.Activities.order;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.fyp.amenms.Activities.MapsActivity;
import com.fyp.amenms.FirebaseHelpers.FirebaseStorageHelper;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.RequestHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    RequestHelperClass order;
    SessionManager sessionManager;
    boolean isProvider = false;
    FirebaseStorageHelper firebaseStorageHelper;
    private TextView order_date, description, phone_no, alternate_contact_num, address, alter_address, name, providerName, orderCharges, paymentMethod;
    private Button accept_order, reject_order, complete_order;
    private SharedPreferences app_date;
    private CircleImageView profile_image;
    private LinearLayout num_container, alt_num_container;
    private String lat = "", lng = "", workerName = "";
    private ProgressDialog progressDialog;
    private DatabaseReference requestsDbReference;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference firebaseTypeReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details);

        app_date = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        sessionManager = new SessionManager(this);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top
        }
        rootNode = FirebaseDatabase.getInstance();
        fAuth = FirebaseAuth.getInstance();

        requestsDbReference = rootNode.getReference(Constants.FD_REQUESTS_NOTE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        getViews();

        Intent intent = getIntent();
        order = (RequestHelperClass) intent.getSerializableExtra("order");
        orderDetails(order);

        firebaseStorageHelper = new FirebaseStorageHelper(this);

        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER)) {
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_PROVIDER);
            isProvider = true;
            try {
                firebaseStorageHelper.displayUserPicture(profile_image, order.getUserUid());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (order.getStatus() == Constants.RequestStatus.ACCEPTED) {
                accept_order.setVisibility(View.GONE);
                complete_order.setVisibility(View.VISIBLE);
            }
        } else {
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_USER);
            isProvider = false;
            try {
                firebaseStorageHelper.displayUserPicture(profile_image, order.getProviderUid());
            } catch (Exception e) {
                e.printStackTrace();
            }
            accept_order.setVisibility(View.GONE);
        }
        if (order.getStatus() == Constants.RequestStatus.DONE || order.getStatus() == Constants.RequestStatus.CANCELLED) {
            accept_order.setVisibility(View.GONE);
            reject_order.setVisibility(View.GONE);
            complete_order.setVisibility(View.GONE);
        }
    }


    private void getViews() {

        description = findViewById(R.id.description);
        phone_no = findViewById(R.id.phone_no);
        alternate_contact_num = findViewById(R.id.alternate_contact_num);
        address = findViewById(R.id.address);
        alter_address = findViewById(R.id.alter_address);
        order_date = findViewById(R.id.order_date);
        orderCharges = findViewById(R.id.orderCharges);
        paymentMethod = findViewById(R.id.paymentMethod);
        name = findViewById(R.id.name);

        providerName = findViewById(R.id.providerName);

        profile_image = findViewById(R.id.profile_image);

        accept_order = findViewById(R.id.accept_order);
        accept_order.setOnClickListener(this);

        reject_order = findViewById(R.id.reject_order);
        reject_order.setOnClickListener(this);

        complete_order = findViewById(R.id.complete_order);
        complete_order.setOnClickListener(this);

        num_container = findViewById(R.id.num_container);
        alt_num_container = findViewById(R.id.alt_num_container);

        num_container.setVisibility(View.GONE);
        alt_num_container.setVisibility(View.GONE);

        address.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {


        switch (view.getId()) {

            case R.id.address:


                try {

                    MapsActivity.workerlat = lat;
                    MapsActivity.workerlng = lng;
                    MapsActivity.workerName = workerName;

                    Intent intent = new Intent(OrderDetailsActivity.this, MapsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {

                }
                break;

            case R.id.orderCharges:
                if(isProvider) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                    ViewGroup viewGroup = findViewById(android.R.id.content);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_charges, viewGroup, false);
                    builder.setView(dialogView);

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    final TextInputEditText chargesET = dialogView.findViewById(R.id.ET_CHARGES_SP);
                    chargesET.setText(order.getCharges() + "");
                    Button buttonOk = dialogView.findViewById(R.id.buttonOk);
                    buttonOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (order.getCharges() != Integer.parseInt(chargesET.getText().toString()) && Integer.parseInt(chargesET.getText().toString()) > 0) {
                                order.setCharges(Integer.parseInt(chargesET.getText().toString()));
                                requestsDbReference.child(order.getRequestId()).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(OrderDetailsActivity.this, "Successfully updated order.", Toast.LENGTH_SHORT).show();
                                        orderCharges.setText(chargesET.getText().toString());
                                        alertDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(OrderDetailsActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "Only provider can update this", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.name:


                try {

                    OrderProfileActivity.isProvider = false;
                    OrderProfileActivity.requestHelperClass = order;

                    Intent intent = new Intent(OrderDetailsActivity.this, OrderProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {

                }
                break;

            case R.id.providerName:


                try {

                    OrderProfileActivity.isProvider = true;
                    OrderProfileActivity.requestHelperClass = order;

                    Intent intent = new Intent(OrderDetailsActivity.this, OrderProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {

                }
                break;


            case R.id.accept_order:
                updateOrder(Constants.RequestStatus.ACCEPTED);
                break;
            case R.id.reject_order:

                updateOrder(Constants.RequestStatus.CANCELLED);

                break;
            case R.id.complete_order:




                collectCashPayment();

                break;

        }

    }

    public void collectCashPayment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_done, viewGroup, false);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);

        final TextInputEditText chargesET = dialogView.findViewById(R.id.ET_CHARGES_SP);
        chargesET.setText(order.getCharges() + "");
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(chargesET.getText().toString()) > 0) {

                    order.setCharges(Integer.parseInt(chargesET.getText().toString()));
                    alertDialog.dismiss();
                    updateOrder(Constants.RequestStatus.DONE);
                }
            }
        });
    }


    //////get order details////////

    public void orderDetails(RequestHelperClass order) {
        order_date.setText(order.getOrderDateTime());
        description.setText(order.getDescription());
        alternate_contact_num.setText(order.getAlternateMobNumber());
        address.setText(order.getAddress());
        alter_address.setText(order.getAlternateAddress());
        name.setText(order.getUserData().getName());
        orderCharges.setText(order.getCharges() + "");
        paymentMethod.setText(order.getPaymentMethod());
        providerName.setText(order.getProviderData().getName());
        phone_no.setText(order.getMobNumber());
        lat = order.getLatitude() + "";
        lng = order.getLongitude() + "";
        workerName = order.getAddress();
    }


    //////accept Order////////

    public void updateOrder(int status) {
        order.setStatus(status);
        order.setNotified(false);
        if (isProvider)
            order.setStatusUpdatedBy(Constants.RequestStatusUpdatedBy.PROVIDER);
        else
            order.setStatusUpdatedBy(Constants.RequestStatusUpdatedBy.USER);
        requestsDbReference.child(order.getRequestId()).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(OrderDetailsActivity.this, "Successfully updated order.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderDetailsActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
