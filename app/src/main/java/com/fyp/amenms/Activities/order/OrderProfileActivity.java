package com.fyp.amenms.Activities.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fyp.amenms.Activities.LoginActivity;
import com.fyp.amenms.Activities.ProfileEditActivity;
import com.fyp.amenms.FirebaseHelpers.FirebaseStorageHelper;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.ProviderHelperClass;
import com.fyp.amenms.database.RequestHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextInputEditText ET_Name_SP;
    private TextInputEditText ET_CNIC_SP;
    private TextInputEditText ET_EMAIL_SP;
    private TextInputEditText ET_PASSWORD_SP;
    private TextInputEditText ET_PHONENUMBER_SP, ET_EXPERTISE_SP, ET_WORKING_HOURS_SP, ET_EXPERIENCE_SP, ET_ADDRESS_SP;
    ImageView profileImage;

    public static RequestHelperClass requestHelperClass = null;
    public static boolean isProvider;

    SessionManager sessionManager;
    FirebaseStorageHelper firebaseStorageHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderprofile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //check
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Registeration);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);

        ET_Name_SP = findViewById(R.id.ET_Name_SP);
        ET_EMAIL_SP = findViewById(R.id.ET_EMAIL_SP);
        ET_PASSWORD_SP = findViewById(R.id.ET_PASSWORD_SP);
        ET_CNIC_SP = findViewById(R.id.ET_CNIC_SP);
        ET_PHONENUMBER_SP = findViewById(R.id.ET_PHONENUMBER_SP);
        profileImage = findViewById(R.id.userProfileImage);
        ET_EXPERTISE_SP = findViewById(R.id.ET_EXPERTISE_SP);
        ET_WORKING_HOURS_SP = findViewById(R.id.ET_WORKING_HOURS_SP);
        ET_EXPERIENCE_SP = findViewById(R.id.ET_EXPERIENCE_SP);
        ET_ADDRESS_SP = findViewById(R.id.ET_ADDRESS_SP);
        if(!isProvider){
            findViewById(R.id.til_expertise).setVisibility(View.GONE);
            findViewById(R.id.til_working_hours).setVisibility(View.GONE);
            findViewById(R.id.til_experience).setVisibility(View.GONE);
            findViewById(R.id.til_address).setVisibility(View.GONE);
        }
        setProfile();

    }

    private void setProfile(){

        firebaseStorageHelper = new FirebaseStorageHelper(this);
        try {
            if(isProvider) {
                firebaseStorageHelper.displayUserPicture(profileImage, requestHelperClass.getProviderUid());
                setProviderProfile(requestHelperClass.getProviderData());
            }
            else {
                firebaseStorageHelper.displayUserPicture(profileImage, requestHelperClass.getUserUid());
                setUserProfile(requestHelperClass.getUserData());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void setUserProfile(UserHelperClass userProfile){
        ET_Name_SP.setText(userProfile.getName());
        ET_CNIC_SP.setText(userProfile.getCnic());
        ET_EMAIL_SP.setText(userProfile.getEmail());
        ET_PHONENUMBER_SP.setText(userProfile.getMobNumber());
    }

    void setProviderProfile(ProviderHelperClass providerProfile){
        ET_Name_SP.setText(providerProfile.getName());
        ET_CNIC_SP.setText(providerProfile.getCnic());
        ET_EMAIL_SP.setText(providerProfile.getEmail());
        ET_PHONENUMBER_SP.setText(providerProfile.getMobNumber());
        ET_EXPERTISE_SP.setText(providerProfile.getExpertise());
        ET_WORKING_HOURS_SP.setText(providerProfile.getWorkingHours());
        ET_EXPERIENCE_SP.setText(providerProfile.getExperience());
        ET_ADDRESS_SP.setText(providerProfile.getAddress());
    }

    public void EditUser(View view) {
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    @Override
    public void onBackPressed() {
        finish();
    }
}
