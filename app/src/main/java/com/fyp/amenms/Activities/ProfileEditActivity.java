package com.fyp.amenms.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fyp.amenms.FirebaseHelpers.FirebaseStorageHelper;
import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.ProviderHelperClass;
import com.fyp.amenms.database.SessionManager;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

public class ProfileEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    SessionManager sessionManager;
    UserHelperClass userObject;
    ProviderHelperClass providerObject;
    private TextInputEditText ET_Name_SP;
    private TextInputEditText ET_CNIC_SP;
    private TextInputEditText ET_EMAIL_SP;
    private TextInputEditText ET_PASSWORD_SP;
    private TextInputEditText ET_PHONENUMBER_SP, ET_EXPERTISE_SP, ET_WORKING_HOURS_SP, ET_EXPERIENCE_SP, ET_ADDRESS_SP, ET_BCHARGES_SP;
    private Button register_btn;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference firebaseDbReference;

    Uri selectedImageUri;
    private static final String[] READ_PERMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    Calendar myCalendar;
    private static final String[] WRITE_PERMS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int RESULT_LOAD_IMG = 1;

    ImageView profileImage;

    private static final int INITIAL_REQUEST = 1337;
    private static final int READ_REQUEST = INITIAL_REQUEST + 1;

    FirebaseStorageHelper firebaseStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fAuth = FirebaseAuth.getInstance();
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
        profileImage = findViewById(R.id.userProfileImage);
        firebaseStorageHelper = new FirebaseStorageHelper(this);
        try {
            firebaseStorageHelper.displayUserPicture(profileImage, fAuth.getUid());
        }catch (Exception e){
            e.printStackTrace();
        }
        ET_EXPERTISE_SP = findViewById(R.id.ET_EXPERTISE_SP);
        ET_WORKING_HOURS_SP = findViewById(R.id.ET_WORKING_HOURS_SP);
        ET_EXPERIENCE_SP = findViewById(R.id.ET_EXPERIENCE_SP);
        ET_BCHARGES_SP = findViewById(R.id.ET_BCHARGES_SP);
        ET_ADDRESS_SP = findViewById(R.id.ET_ADDRESS_SP);
        rootNode = FirebaseDatabase.getInstance();
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)) {
            findViewById(R.id.til_expertise).setVisibility(View.GONE);
            findViewById(R.id.til_working_hours).setVisibility(View.GONE);
            findViewById(R.id.til_experience).setVisibility(View.GONE);
            findViewById(R.id.til_address).setVisibility(View.GONE);
            findViewById(R.id.til_basicCharges).setVisibility(View.GONE);
            firebaseDbReference = rootNode.getReference(Constants.TYPE_USER);
            userObject = (UserHelperClass) getIntent().getSerializableExtra(Constants.LOGGED_IN_USER);
            setUserProfile(userObject);
        } else {
            firebaseDbReference = rootNode.getReference(Constants.TYPE_PROVIDER);
            providerObject = (ProviderHelperClass) getIntent().getSerializableExtra(Constants.LOGGED_IN_USER);
            setProviderProfile(providerObject);
        }

        register_btn = findViewById(R.id.register_btn);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (canReadFile()) {
            getImage();
        }
    }

    private boolean canReadFile() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= 23) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        }
        return false;
    }

    public void loadImagefromGallery(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                if (!canReadFile()) {
                    requestPermissions(READ_PERMS, READ_REQUEST);
                } else {
                    getImage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getImage();
        }
    }

    private void getImage() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/");
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                selectedImageUri = data.getData();
                CropImage.activity(selectedImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(128, 128)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(this);

            } else if (requestCode == RESULT_LOAD_IMG) {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    selectedImageUri = result.getUri();
                    Picasso.with((this)).load(selectedImageUri)
                            .fit()
                            .error(R.drawable.user1)
                            .into(profileImage);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                }
            }/*else if (requestCode == PIC_CROP) {
                if (data != null) {
                    // get the returned data
                    Bundle extras = data.getExtras();
                    // get the cropped bitmap
                    Bitmap selectedBitmap = extras.getParcelable("data");

                    profileImage.setImageBitmap(selectedBitmap);
                }
            }*/

        } catch (Exception e) {
            Log.e("Upload Image", "Stack trace", e);
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }


    void setUserProfile(UserHelperClass userProfile) {
        ET_Name_SP.setText(userProfile.getName());
        ET_CNIC_SP.setText(userProfile.getCnic());
        ET_EMAIL_SP.setText(userProfile.getEmail());
        ET_PHONENUMBER_SP.setText(userProfile.getMobNumber());
    }

    void setProviderProfile(ProviderHelperClass providerProfile) {
        ET_Name_SP.setText(providerProfile.getName());
        ET_CNIC_SP.setText(providerProfile.getCnic());
        ET_EMAIL_SP.setText(providerProfile.getEmail());
        ET_PHONENUMBER_SP.setText(providerProfile.getMobNumber());
        ET_EXPERTISE_SP.setText(providerProfile.getExpertise());
        ET_WORKING_HOURS_SP.setText(providerProfile.getWorkingHours());
        ET_EXPERIENCE_SP.setText(providerProfile.getExperience());
        ET_ADDRESS_SP.setText(providerProfile.getAddress());
        ET_BCHARGES_SP.setText(providerProfile.getBasicCharges()+"");
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
                ".{4,}" +               //at least 4 characters
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


    public void EditUser(View view) {

        if (!ValidateFullName() | !ValidateCellNo() | !ValidateCNIC()) {
            return;
        } else if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER) && (!ValidateEmptyField(ET_EXPERTISE_SP) | !ValidateEmptyField(ET_WORKING_HOURS_SP) | !ValidateEmptyField(ET_EXPERIENCE_SP) | !ValidateEmptyField(ET_ADDRESS_SP))) {
            return;
        } else {
            RegisterUsers();
        }

    }

    private void RegisterUsers() {

        if (selectedImageUri != null) {
            firebaseStorageHelper.saveProfilePictureFromUri(selectedImageUri, fAuth.getUid());
        }

        String fullName = ET_Name_SP.getText().toString().trim();
        String CNIC = ET_CNIC_SP.getText().toString().trim();
        String mobileNumber = ET_PHONENUMBER_SP.getText().toString().trim();
        if (sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)) {
            UserHelperClass helperClass = new UserHelperClass(fullName, CNIC, userObject.getEmail(), userObject.getPassword(), mobileNumber);
            firebaseDbReference.child(fAuth.getUid()).setValue(helperClass);
        } else {
            ProviderHelperClass providerHelperClass = new ProviderHelperClass(fullName, CNIC, providerObject.getEmail(), providerObject.getPassword(), mobileNumber);
            providerHelperClass.setExpertise(ET_EXPERTISE_SP.getText().toString().trim());
            providerHelperClass.setWorkingHours(ET_WORKING_HOURS_SP.getText().toString().trim());
            providerHelperClass.setExperience(ET_EXPERIENCE_SP.getText().toString().trim());
            providerHelperClass.setAddress(ET_ADDRESS_SP.getText().toString().trim());
            providerHelperClass.setBasicCharges(Integer.parseInt(ET_BCHARGES_SP.getText().toString()));
            firebaseDbReference.child(fAuth.getUid()).setValue(providerHelperClass);
        }
        Toast.makeText(ProfileEditActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        //finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onBackPressed() {}
}
