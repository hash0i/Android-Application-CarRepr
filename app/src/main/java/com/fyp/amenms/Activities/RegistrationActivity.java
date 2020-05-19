package com.fyp.amenms.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.ProviderHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    SessionManager sessionManager;
    private TextInputEditText ET_Name_SP;
    private TextInputEditText ET_CNIC_SP;
    private TextInputEditText ET_EMAIL_SP;
    private TextInputEditText ET_PASSWORD_SP;
    private TextInputEditText ET_PHONENUMBER_SP, ET_EXPERTISE_SP, ET_WORKING_HOURS_SP, ET_EXPERIENCE_SP, ET_ADDRESS_SP;
    private Button register_btn;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //check
        Toolbar toolbar = findViewById(R.id.toolbar_Registeration);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);

        ET_Name_SP = findViewById(R.id.ET_Name_SP);
        ET_EMAIL_SP = findViewById(R.id.ET_EMAIL_SP);
        ET_PASSWORD_SP = findViewById(R.id.ET_PASSWORD_SP);
        ET_CNIC_SP = findViewById(R.id.ET_CNIC_SP);
        ET_PHONENUMBER_SP = findViewById(R.id.ET_PHONENUMBER_SP);

        ET_EXPERTISE_SP = findViewById(R.id.ET_EXPERTISE_SP);
        ET_WORKING_HOURS_SP = findViewById(R.id.ET_WORKING_HOURS_SP);
        ET_EXPERIENCE_SP = findViewById(R.id.ET_EXPERIENCE_SP);
        ET_ADDRESS_SP = findViewById(R.id.ET_ADDRESS_SP);
        rootNode = FirebaseDatabase.getInstance();
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)) {
            findViewById(R.id.til_expertise).setVisibility(View.GONE);
            findViewById(R.id.til_working_hours).setVisibility(View.GONE);
            findViewById(R.id.til_experience).setVisibility(View.GONE);
            findViewById(R.id.til_address).setVisibility(View.GONE);
            reference = rootNode.getReference(Constants.TYPE_USER);
        } else {
            reference = rootNode.getReference(Constants.TYPE_PROVIDER);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
                return;
            }
        }

        register_btn = findViewById(R.id.register_btn);

        fAuth = FirebaseAuth.getInstance();

    }

    private boolean ValidateUserInput() {
        String email = ET_EMAIL_SP.getText().toString().trim();
        String password = ET_PASSWORD_SP.getText().toString().trim();

        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (!email.matches(noWhiteSpace) && !password.matches(noWhiteSpace)) {
            ET_EMAIL_SP.setError("White spaces are not allowed!");
            ET_PASSWORD_SP.setError("White spaces are not allowed!");
            return false;
        } else {
            return true;
        }

    }

    private boolean ValidateFullName() {
        String Name = ET_Name_SP.getText().toString().trim();
        if (Name.isEmpty()) {
            ET_Name_SP.setError("Field cannot be empty");
            return false;
        } else {
            ET_Name_SP.setError(null);
            return true;
        }
    }

    private boolean ValidateEmptyField(TextInputEditText textInputEditText) {
        String Name = textInputEditText.getText().toString().trim();
        if (Name.isEmpty()) {
            textInputEditText.setError("Field cannot be empty");
            return false;
        } else {
            textInputEditText.setError(null);
            return true;
        }
    }

    private boolean ValidateEmail() {
        String email = ET_EMAIL_SP.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.isEmpty()) {
            ET_EMAIL_SP.setError("Field cannot be empty");
            return false;
        } else if (!email.matches(emailPattern)) {
            ET_EMAIL_SP.setError("Invalid Email Address");
            return false;
        } else {
            ET_EMAIL_SP.setError(null);
            return true;
        }
    }

    private boolean ValidatePassword() {
        String Password = ET_PASSWORD_SP.getText().toString().trim();
        String passwordVal = "^" +
                //"(?.*[0=9])" +      //at least 1 digit
                //"(?=.*[a-z])" +     //at least 1 lower case letter
                //"(?=.*[A-Z])" +    //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +   //any letter
                "(?=.*[@#$%^&+=])" +    //at least  1 special character
                "(?=\\s+$)" +           // no white space
                ".{8,}" +               //at least 8 characters
                "$";

        if (Password.isEmpty()) {
            ET_PASSWORD_SP.setError("Field cannot be empty");
            return false;
        } else if (Password.matches(passwordVal)) {
            ET_PASSWORD_SP.setError("Password is too weak");
            return false;
        } else {
            ET_PASSWORD_SP.setError(null);
            return true;
        }
    }

    private boolean ValidateCellNo() {
        String PhoneNumber = ET_PHONENUMBER_SP.getText().toString().trim();
        if (PhoneNumber.isEmpty()) {
            ET_PHONENUMBER_SP.setError("Field cannot be empty");
            return false;
        } else {
            ET_PHONENUMBER_SP.setError(null);
            return true;
        }
    }

    private boolean ValidateCNIC() {
        String CNIC = ET_CNIC_SP.getText().toString().trim();
        if (CNIC.length() < 13) {
            ET_CNIC_SP.setError("Must contain 13 digits");
            return false;
        } else {
            ET_CNIC_SP.setError(null);
            return true;
        }
    }

    public void RegisterUser(View view) {

        String email = ET_EMAIL_SP.getText().toString().trim();
        String password = ET_PASSWORD_SP.getText().toString().trim();
        boolean val1, val2;

        if (!ValidateUserInput() | !ValidateFullName() | !ValidateEmail() | !ValidateCellNo() | !ValidatePassword() | !ValidateCNIC()) {
            val1 = false;
            return;
        } else {
            val1 = true;
        }
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER) && (!ValidateEmptyField(ET_EXPERTISE_SP) | !ValidateEmptyField(ET_WORKING_HOURS_SP) | !ValidateEmptyField(ET_EXPERIENCE_SP))) {
            val2 = false;
            return;
        } else {
            val2 = true;
        }
        if ((val1) && (val2)) {
            RegisterUsers(email, password);
        } else {
            Toast.makeText(RegistrationActivity.this, "Register problem!", Toast.LENGTH_SHORT).show();
        }
    }

    private void RegisterUsers(final String email, final String password) {

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String fullName = ET_Name_SP.getText().toString().trim();
                    String CNIC = ET_CNIC_SP.getText().toString().trim();
                    String mobileNumber = ET_PHONENUMBER_SP.getText().toString().trim();

                    if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)) {
                        UserHelperClass helperClass = new UserHelperClass(fullName, CNIC, email, password, mobileNumber);
                        reference.child(task.getResult().getUser().getUid()).setValue(helperClass);
                        //Toast.makeText(RegistrationActivity.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistrationActivity.this, AddressMapsActivity.class);
                        //intent.putExtra(Constants.PREFS_USER_TYPE, Constants.TYPE_USER);
                        startActivity(intent);
                        finish();
                    } else {
                        ProviderHelperClass providerHelperClass = new ProviderHelperClass(fullName, CNIC, email, password, mobileNumber);
                        providerHelperClass.setExpertise(ET_EXPERTISE_SP.getText().toString().trim());
                        providerHelperClass.setWorkingHours(ET_WORKING_HOURS_SP.getText().toString().trim());
                        providerHelperClass.setExperience(ET_EXPERIENCE_SP.getText().toString().trim());
                        providerHelperClass.setAddress(ET_ADDRESS_SP.getText().toString().trim());
                        providerHelperClass.setUid(task.getResult().getUser().getUid());
                        reference.child(task.getResult().getUser().getUid()).setValue(providerHelperClass);

                        //Toast.makeText(RegistrationActivity.this, "Register Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegistrationActivity.this, AddressMapsActivity.class);
                        //intent.putExtra(Constants.PREFS_USER_TYPE, Constants.TYPE_USER);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Toast.makeText(RegistrationActivity.this, "Register Failure ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onBackPressed() {
    }
}
