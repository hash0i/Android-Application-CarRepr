package com.fyp.amenms.Activities;

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

import com.fyp.amenms.FirebaseHelpers.FirebaseStorageHelper;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextInputEditText ET_Name_SP;
    private TextInputEditText ET_CNIC_SP;
    private TextInputEditText ET_EMAIL_SP;
    private TextInputEditText ET_PASSWORD_SP;
    private TextInputEditText ET_PHONENUMBER_SP, ET_EXPERTISE_SP, ET_WORKING_HOURS_SP, ET_EXPERIENCE_SP, ET_ADDRESS_SP;
    ImageView profileImage;
    private Button register_btn;

    UserHelperClass userObject;
    ProviderHelperClass providerObject;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference firebaseDbReference;
    boolean editMode = false;

    SessionManager sessionManager;
    FirebaseStorageHelper firebaseStorageHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
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
        rootNode = FirebaseDatabase.getInstance();
        if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)){
            findViewById(R.id.til_expertise).setVisibility(View.GONE);
            findViewById(R.id.til_working_hours).setVisibility(View.GONE);
            findViewById(R.id.til_experience).setVisibility(View.GONE);
            findViewById(R.id.til_address).setVisibility(View.GONE);
            firebaseDbReference = rootNode.getReference(Constants.TYPE_USER);
        } else {
            firebaseDbReference = rootNode.getReference(Constants.TYPE_PROVIDER);
        }

        register_btn = (Button) findViewById(R.id.register_btn);

        fAuth = FirebaseAuth.getInstance();
        setProfile();

    }

    private void setProfile(){

        firebaseStorageHelper = new FirebaseStorageHelper(this);
        try {
            firebaseStorageHelper.displayUserPicture(profileImage, fAuth.getUid());
        }catch (Exception e){
            e.printStackTrace();
        }

        firebaseDbReference.child(fAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.getValue() != null) {
                        if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)){
                            UserHelperClass userHelperClass = dataSnapshot.getValue(UserHelperClass.class);
                            setUserProfile(userHelperClass);
                        } else {
                            ProviderHelperClass providerHelperClass = dataSnapshot.getValue(ProviderHelperClass.class);
                            setProviderProfile(providerHelperClass);
                        }
                    } else {
                        //Toast.makeText(LoginActivity.this, "Sign in database failed", Toast.LENGTH_LONG).show();
                    }
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

    void setUserProfile(UserHelperClass userProfile){
        userObject = userProfile;
        ET_Name_SP.setText(userProfile.getName());
        ET_CNIC_SP.setText(userProfile.getCnic());
        ET_EMAIL_SP.setText(userProfile.getEmail());
        ET_PHONENUMBER_SP.setText(userProfile.getMobNumber());
    }

    void setProviderProfile(ProviderHelperClass providerProfile){
        providerObject = providerProfile;
        ET_Name_SP.setText(providerProfile.getName());
        ET_CNIC_SP.setText(providerProfile.getCnic());
        ET_EMAIL_SP.setText(providerProfile.getEmail());
        ET_PHONENUMBER_SP.setText(providerProfile.getMobNumber());
        ET_EXPERTISE_SP.setText(providerProfile.getExpertise());
        ET_WORKING_HOURS_SP.setText(providerProfile.getWorkingHours());
        ET_EXPERIENCE_SP.setText(providerProfile.getExperience());
        ET_ADDRESS_SP.setText(providerProfile.getAddress());
    }

    private boolean ValidateUserInput() {
        String email = ET_EMAIL_SP.getText().toString().trim();
        String password = ET_PASSWORD_SP.getText().toString().trim();

        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if(!email.matches(noWhiteSpace) && !password.matches(noWhiteSpace))
        {
            ET_EMAIL_SP.setError("White spaces are not allowed!");
            ET_PASSWORD_SP.setError("White spaces are not allowed!");
            return false;
        }
        else { return true; }

    }
    private boolean ValidateFullName() {
        String Name = ET_Name_SP.getText().toString().trim();
        if(Name.isEmpty())
        {
            ET_Name_SP.setError("Field cannot be empty");
            return false;
        }
        else
        {
            ET_Name_SP.setError(null);
            return true;
        }
    }

    private boolean ValidateEmptyField(TextInputEditText textInputEditText) {
        String Name = textInputEditText.getText().toString().trim();
        if(Name.isEmpty())
        {
            textInputEditText.setError("Field cannot be empty");
            return false;
        }
        else
        {
            textInputEditText.setError(null);
            return true;
        }
    }

    private boolean ValidateEmail(){
        String email = ET_EMAIL_SP.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(email.isEmpty())
        {
            ET_EMAIL_SP.setError("Field cannot be empty");
            return false;
        }
        else
            if (!email.matches(emailPattern))
            {
                ET_EMAIL_SP.setError("Invalid Email Address");
            return false;
            }
         else {
                ET_EMAIL_SP.setError(null);
                return true;
            }
    }
    private boolean ValidatePassword() {
        String Password = ET_PASSWORD_SP.getText().toString().trim();
        String passwordVal ="^" +
                //"(?.*[0=9])" +      //at least 1 digit
                //"(?=.*[a-z])" +     //at least 1 lower case letter
                //"(?=.*[A-Z])" +    //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +   //any letter
                "(?=.*[@#$%^&+=])" +    //at least  1 special character
                "(?=\\s+$)" +           // no white space
                ".{4,}" +               //at least 4 characters
                "$";

        if(Password.isEmpty())
        {
            ET_PASSWORD_SP.setError("Field cannot be empty");
            return false;
        }
        else
        if(Password.matches(passwordVal))
        {
            ET_PASSWORD_SP.setError("Password is too weak");
            return false;
        }
        else
        {
            ET_PASSWORD_SP.setError(null);
            return true;
        }
    }
    private boolean ValidateCellNo() {
        String PhoneNumber = ET_PHONENUMBER_SP.getText().toString().trim();
        if(PhoneNumber.isEmpty())
        {
            ET_PHONENUMBER_SP.setError("Field cannot be empty");
            return false;
        }
        else
        {
            ET_PHONENUMBER_SP.setError(null);
            return true;
        }
    }
    private boolean ValidateCNIC() {
        String CNIC = ET_CNIC_SP.getText().toString().trim();
        if(CNIC.length()<13)
        {
            ET_CNIC_SP.setError("Must contain 13 digits");
        return false;
        }
        else{
            ET_CNIC_SP.setError(null);
            return true;
        }
    }


    public void EditUser(View view) {
        Intent intent = new Intent(this, ProfileEditActivity.class);
        if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)){
            intent.putExtra(Constants.LOGGED_IN_USER, userObject);
        } else {
            intent.putExtra(Constants.LOGGED_IN_USER, providerObject);
        }

        startActivityForResult(intent, 100);

        /*String email = ET_EMAIL_SP.getText().toString().trim();
        String password = ET_PASSWORD_SP.getText().toString().trim();

        if(!ValidateUserInput()| !ValidateFullName() | !ValidateEmail() | !ValidateCellNo() | !ValidatePassword() | !ValidateCNIC())
        {
            return;
        } else if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER)&&(!ValidateEmptyField(ET_EXPERTISE_SP) | !ValidateEmptyField(ET_WORKING_HOURS_SP) | !ValidateEmptyField(ET_EXPERIENCE_SP) | !ValidateEmptyField(ET_ADDRESS_SP))){
            return;
        }
        else {
            RegisterUsers(email, password);
        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == RESULT_OK){
                setProfile();
                Toast.makeText(this, "Updated Profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void RegisterUsers(final String email, final String password) {

        fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String fullName = ET_Name_SP.getText().toString().trim();
                    String CNIC = ET_CNIC_SP.getText().toString().trim();
                    String mobileNumber = ET_PHONENUMBER_SP.getText().toString().trim();
                    if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)) {
                        UserHelperClass helperClass = new UserHelperClass(fullName, CNIC, email, password, mobileNumber);
                        firebaseDbReference.child(task.getResult().getUser().getUid()).setValue(helperClass);
                    } else {
                        ProviderHelperClass providerHelperClass = new ProviderHelperClass(fullName, CNIC, email, password, mobileNumber);
                        providerHelperClass.setExpertise(ET_EXPERTISE_SP.getText().toString().trim());
                        providerHelperClass.setWorkingHours(ET_WORKING_HOURS_SP.getText().toString().trim());
                        providerHelperClass.setExperience(ET_EXPERIENCE_SP.getText().toString().trim());
                        providerHelperClass.setAddress(ET_ADDRESS_SP.getText().toString().trim());
                        firebaseDbReference.child(task.getResult().getUser().getUid()).setValue(providerHelperClass);
                    }
                    Toast.makeText(ProfileActivity.this,"Register Successful!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    //intent.putExtra(Constants.PREFS_USER_TYPE, Constants.TYPE_USER);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,"Register Failure ",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    @Override
    public void onBackPressed() {}
}
