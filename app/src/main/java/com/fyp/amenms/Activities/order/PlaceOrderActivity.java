package com.fyp.amenms.Activities.order;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.andreabaccega.widget.FormEditText;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.Utilities.ImageCompressClass;
import com.fyp.amenms.Utilities.ImagePicker;
import com.fyp.amenms.database.ProviderHelperClass;
import com.fyp.amenms.database.RequestHelperClass;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlaceOrderActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int RESULT_OK = -1;
    private static final int PICK_IMAGE_ID = 234;
    public static ProviderHelperClass provider;
    public static UserHelperClass user;
    private final int PERMISSION_REQUEST_CODE = 01010;
    File ImageFile = null;
    String selectedHrs = "";
    String selectedPaymentMethod = "";
    private FormEditText description, phone_no, alternate_contact_num, address, alter_address;
    private TextView order_date;
    private Button place_order;
    private ImageView calender_icon, profile_image;
    private String timeStamp = "", current_data = "";
    private SharedPreferences app_date;
    private ProgressDialog progressDialog;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference requestsDbReference;
    private Spinner select_hrs;
    private String[] hoursList = {"Select hours", "1", "2", "3", "4", "5", "6", "7", "8"};

    private Spinner select_payment;
    private String[] paymentList = {"Select Payment", "Cash", "Online Banking"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_order_layout);


        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top
        }

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
        initFirebaseAndData();
    }

    void initFirebaseAndData() {
        rootNode = FirebaseDatabase.getInstance();
        fAuth = FirebaseAuth.getInstance();
        requestsDbReference = rootNode.getReference(Constants.FD_REQUESTS_NOTE);

        final DatabaseReference userNode = rootNode.getReference(Constants.TYPE_USER);
        userNode.child(fAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.getValue() != null)
                        user = dataSnapshot.getValue(UserHelperClass.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                //Toast.makeText(PR.this, "Sign in cancelled", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getViews() {

        description = findViewById(R.id.description);
        phone_no = findViewById(R.id.phone_no);
        alternate_contact_num = findViewById(R.id.alternate_contact_num);
        address = findViewById(R.id.address);
        alter_address = findViewById(R.id.alter_address);
        order_date = findViewById(R.id.order_date);

        place_order = findViewById(R.id.place_order);
        place_order.setOnClickListener(this);

        calender_icon = findViewById(R.id.calender_icon);
        calender_icon.setOnClickListener(this);

        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(this);

        select_hrs = findViewById(R.id.select_hrs);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlaceOrderActivity.this,
                android.R.layout.simple_spinner_item, hoursList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_hrs.setAdapter(adapter);
        select_hrs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Log.e("selectedHrs", adapterView.getItemAtPosition(i).toString());
                selectedHrs = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        select_payment = findViewById(R.id.select_payment);
        ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(PlaceOrderActivity.this,
                android.R.layout.simple_spinner_item, paymentList);

        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_payment.setAdapter(pAdapter);
        select_payment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Log.e("selectedPaymentMethod", adapterView.getItemAtPosition(i).toString());
                selectedPaymentMethod = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void CheckValidationsAndPostData() {

        FormEditText[] allFields = {description, phone_no, alternate_contact_num, address, alter_address};


        boolean allValid = true;
        for (FormEditText field : allFields) {

            allValid = field.testValidity() && allValid;
        }

        if (allValid) {


            try {
                app_date = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);


                if (description.getText().toString().isEmpty()) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please enter order description");

                } else if (phone_no.getText().toString().isEmpty()) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please enter contact number");

                } else if (address.getText().toString().isEmpty()) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please enter address");

                } else if (order_date.getText().toString().isEmpty()) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please choose order delivery date");

                } else if (selectedHrs.equalsIgnoreCase("") || selectedHrs.equalsIgnoreCase("Select hours")) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please select hours");
                } else if (!selectedPaymentMethod.equalsIgnoreCase("Cash")) {

                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "Please select payment method(Only cash for now)");
                } else {

                    placeOrder(description.getText().toString(), order_date.getText().toString(),
                            phone_no.getText().toString(), alternate_contact_num.getText().toString(),
                            address.getText().toString(), alter_address.getText().toString(), selectedHrs, selectedPaymentMethod);
                }

            } catch (Exception e) {

            }


        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.place_order:


                CheckValidationsAndPostData();

                break;
            case R.id.profile_image:

                ImageFile = null;

                onPickImage(view);
                break;


            case R.id.calender_icon:

                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                final int mMonth = c.get(Calendar.MONTH);
                final int mDay = c.get(Calendar.DAY_OF_MONTH); // add here +1 if you don't
                final SimpleDateFormat format2 = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss", Locale.ENGLISH);

                current_data = mDay + "-" + mMonth + "-" + mYear;
                Log.e("current_data", current_data);
                Date Time = c.getTime();
                Log.e("Time", String.valueOf(Time));
                DateFormat format1_ = new SimpleDateFormat("a", Locale.ENGLISH);
                timeStamp = format1_.format(c.getTime());
                Log.e("timeStamp", timeStamp);
                DatePickerDialog dpd = new DatePickerDialog(PlaceOrderActivity.this/*android.R.style.Theme_Holo_Dialog*/,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                                final Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                cal.set(Calendar.YEAR, year);
                                if (cal.before(c)) {
                                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "select valid date.");


                                    return;
                                }
                                new TimePickerDialog(PlaceOrderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        cal.set(Calendar.MINUTE, minute);


                                        String selected_date = dayOfMonth + "-" + monthOfYear + "-" + year;
                                        Log.e("selected_date", selected_date);

                                        DateFormat format = new SimpleDateFormat("a", Locale.ENGLISH);
                                        String selected_timestamp = format.format(cal.getTime());
                                        Log.e("selected_timestamp", selected_timestamp);


                      /*  String value= String.valueOf(cal.get(cal.AM_PM));
                        Log.e("value", value);*/

                                        if (current_data.equalsIgnoreCase(selected_date)) {
                                            Log.e("current", "in");


                                            if (timeStamp.equalsIgnoreCase("AM") && selected_timestamp.equalsIgnoreCase("AM")) {

                                                if (c.get(Calendar.HOUR_OF_DAY) > cal.get(Calendar.HOUR_OF_DAY)) {
                                                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "select valid time.");

                                                    return;
                                                }
                                            } else if (timeStamp.equalsIgnoreCase("PM") && selected_timestamp.equalsIgnoreCase("PM")) {
                                                if (c.get(Calendar.HOUR_OF_DAY) > cal.get(Calendar.HOUR_OF_DAY)) {
                                                    Constants.showAlertMessage(PlaceOrderActivity.this, "", "select valid time.");


                                                    return;
                                                }
                                            } else if (timeStamp.equalsIgnoreCase("PM") && selected_timestamp.equalsIgnoreCase("AM")) {

                                                Constants.showAlertMessage(PlaceOrderActivity.this, "", "select valid time.");
                                            }
                                        }


                                        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);

                                        order_date.setText(format1.format(cal.getTime()));
                                    }
                                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();

                            }
                        }, mYear, mMonth, mDay);

                //  dpd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());

                dpd.show();


                break;

        }
    }


    ////// place order ////////

    public void placeOrder(final String request_description, final String order_datetime,
                           final String phone, final String alt_phone, final String address, final String alt_address, final String total_hours, final String paymentMethod) {

        progressDialog = ProgressDialog.show(this, "", getString(R.string.please_wait_msg), true, true);

        final RequestHelperClass requestHelperClass = new RequestHelperClass(fAuth.getCurrentUser().getUid(), provider.getUid(), request_description, order_datetime, phone,
                alt_phone, address, alt_address, total_hours, user.getLatitude(), user.getLongitude());
        requestHelperClass.setUserData(user);
        requestHelperClass.setCharges(provider.getBasicCharges());
        requestHelperClass.setPaymentMethod(paymentMethod);
        requestHelperClass.setProviderData(provider);
        String key = requestsDbReference.push().getKey();
        requestHelperClass.setRequestId(key);
        requestsDbReference.child(key).setValue(requestHelperClass).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PlaceOrderActivity.this, "Successfully placed order.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PlaceOrderActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onPickImage(View view) {
        try {


            if (ContextCompat.checkSelfPermission(PlaceOrderActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(PlaceOrderActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CODE);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_ID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_ID);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            case PICK_IMAGE_ID:

                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);

                ImageFile = ImagePicker.persistImage(bitmap, "tmp_avatar_", PlaceOrderActivity.this);
                Log.e("image_file_val", ImageFile.toString());

                String file = ImageCompressClass.compressImage(ImageFile.toString());
                ImageFile = new File(file);
                Log.e("after compression", file);

                profile_image.setImageURI(Uri.parse(file));

                bitmap.recycle();
                // TODO use bitmap
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;

        }

    }

}
