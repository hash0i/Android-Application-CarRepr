package com.fyp.amenms.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.amenms.Activities.order.MyOrdersActivity;
import com.fyp.amenms.Activities.order.OrderDetailsActivity;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.Utilities.helper.ProvidersAutoCompleteAdapter;
import com.fyp.amenms.database.ProviderHelperClass;
import com.fyp.amenms.database.RequestHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback, ProvidersAutoCompleteAdapter.ClickListener {


    SessionManager sessionManager;
    RecyclerView providers_recycler_view;
    ProvidersAutoCompleteAdapter providersAutoCompleteAdapter;
    TextInputEditText ET_EXPERTISE_SP;
    SupportMapFragment mapFragment;
    TextWatcher filterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int clearIcon = 0;
            if (s.toString().isEmpty() == false)
                clearIcon = R.drawable.ic_clear_search;
            ET_EXPERTISE_SP.setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0);
            if (!s.toString().equals("")) {
                ET_EXPERTISE_SP.setSelection(s.toString().length());

                providersAutoCompleteAdapter.getFilter().filter(s.toString());
                if (providers_recycler_view.getVisibility() == View.GONE) {
                    providers_recycler_view.setVisibility(View.VISIBLE);
                }
            } else {
                if (providers_recycler_view.getVisibility() == View.VISIBLE) {
                    providers_recycler_view.setVisibility(View.GONE);
                }
            }
        }
    };
    UserHelperClass userObject;
    ProviderHelperClass providerObject;
    boolean isProvider = false;
    int notificationId = 0;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private TextView textView;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private int value = 0;
    private GoogleMap mMap;
    private DatabaseReference requestsDbReference;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference firebaseTypeReference;

    @Override
    public void click(ProviderHelperClass provider) {
        Toast.makeText(this, "Selected Provider: " + provider.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_ui);
        textView = findViewById(R.id.textView);
        ET_EXPERTISE_SP = findViewById(R.id.ET_EXPERTISE_SP);
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.mygradient));
        }

        providers_recycler_view = findViewById(R.id.providers_recycler_view);

        ET_EXPERTISE_SP.addTextChangedListener(filterTextWatcher);
        ET_EXPERTISE_SP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (ET_EXPERTISE_SP.getRight() - ET_EXPERTISE_SP.getCompoundPaddingRight())) {
                        ET_EXPERTISE_SP.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        providersAutoCompleteAdapter = new ProvidersAutoCompleteAdapter(this);
        providers_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        providersAutoCompleteAdapter.setClickListener(this);
        providers_recycler_view.setAdapter(providersAutoCompleteAdapter);
        providersAutoCompleteAdapter.notifyDataSetChanged();

        sessionManager = new SessionManager(this);
        rootNode = FirebaseDatabase.getInstance();
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER)) {
            findViewById(R.id.til_expertise).setVisibility(View.GONE);
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_PROVIDER);
            isProvider = true;
        } else {
            firebaseTypeReference = rootNode.getReference(Constants.TYPE_USER);
            isProvider = false;
        }
        mDrawer = findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        navigationView = findViewById(R.id.nvView);
        setupDrawerContent(navigationView);
        setUpMap();

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
                if (dataSnapshot.exists()) {
                    for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        RequestHelperClass request = requestSnap.getValue(RequestHelperClass.class);
                        Log.e("Request Event", request.getDescription());
                        if (isProvider) {
                            if (request.getStatus() == Constants.RequestStatus.ASSIGNED && !request.getNotified() && request.getStatusUpdatedBy().equals(Constants.RequestStatusUpdatedBy.USER)) {
                                sendNotification(request);
                            }
                        } else {
                            if (request.getStatus() != Constants.RequestStatus.ASSIGNED && !request.getNotified() && request.getStatusUpdatedBy().equals(Constants.RequestStatusUpdatedBy.PROVIDER))
                                sendNotification(request);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**/
    private void sendNotification(RequestHelperClass requestHelperClass) {

        String author = "Request: " + requestHelperClass.getDescription();
        if (requestHelperClass.getStatus() == Constants.RequestStatus.ASSIGNED) {
            author = "New " + author;
        } else if (requestHelperClass.getStatus() == Constants.RequestStatus.ACCEPTED) {
            author = "Provider Accepted  " + author;
        } else if (requestHelperClass.getStatus() == Constants.RequestStatus.DONE) {
            author = "Provider Completed  " + author;
        } else if (requestHelperClass.getStatus() == Constants.RequestStatus.CANCELLED) {
            author = "Cancelled  " + author;
        }
        String message = requestHelperClass.getDescription();

        Intent intent = new Intent(this, OrderDetailsActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("order", requestHelperClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final String packageName = getPackageName();


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_applog)
                .setContentTitle(author)
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_MAX)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/notifysnd.mp3"))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;


                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                String NOTIFICATION_CHANNEL_ID = "1";
                String NOTIFICATION_NAME = getResources().getString(R.string.app_name);
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_NAME, importance);
                notificationChannel.enableLights(true);

                notificationChannel.setLightColor(Color.WHITE);
                notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                notificationManager.createNotificationChannel(notificationChannel);

            }
            notificationManager.notify(notificationId++ /* ID of notification */, notificationBuilder.build());
            requestsDbReference.child(requestHelperClass.getRequestId()).child("notified").setValue(true);
        }
    }
    /**/

    public void setUpMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    public void showMyLocationIndicatorinMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
            return;
        }
        if (mMap != null) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        showMyLocationIndicatorinMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showMyLocationIndicatorinMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (value == 0) {
                    //printLog("mapGPS");
                    LatLng myLocation;
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    value++;
                    //getCompleteAddress(myLocation.latitude, myLocation.longitude);
                }


            }
        });
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.google_map_style));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Utils.log(TAG, "markers clicked");
                try {
                    if ((boolean) marker.getTag()) {
                        //moveSearchPlacesActivity(SOURCE_REQUEST_CODE, sourceLatLng.latitude, sourceLatLng.longitude);
                    } else {
                        //moveSearchPlacesActivity(DESTINATION_REQUEST_CODE, destinationLocation.getLatitude(), destinationLocation.getLongitude());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        });
    }

    private ActionBarDrawerToggle setupDrawerToggle() {

        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }

                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                break;

            case R.id.change_password:

                break;

            case R.id.my_service:
                Intent my_orders = new Intent(DashboardActivity.this, MyOrdersActivity.class);
                startActivity(my_orders);
                break;

            case R.id.my_payments:
                break;

            case R.id.logout:
                logoutUser();
                break;

            default:

        }

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();
        // Launching the login activity
        Intent intent = new Intent(DashboardActivity.this, SelectUserTypeActivity.class);
        startActivity(intent);
        finish();
    }


}
