package com.fyp.amenms.HomeData;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.fyp.amenms.R;
import com.fyp.amenms.database.UserHelperClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationClass extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextInputEditText ET_Name_SP;
    private TextInputEditText ET_CNIC_SP;
    private TextInputEditText ET_EMAIL_SP;
    private TextInputEditText ET_PASSWORD_SP;
    private TextInputEditText ET_PHONENUMBER_SP;

    private Button register_btn;
    private FirebaseAuth fAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //check
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Registeration);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ET_Name_SP = findViewById(R.id.ET_Name_SP);
        ET_EMAIL_SP = findViewById(R.id.ET_EMAIL_SP);
        ET_PASSWORD_SP = findViewById(R.id.ET_PASSWORD_SP);
        ET_CNIC_SP = findViewById(R.id.ET_CNIC_SP);
        ET_PHONENUMBER_SP = findViewById(R.id.ET_PHONENUMBER_SP);
        register_btn = (Button) findViewById(R.id.register_btn);

        fAuth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

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


    public void RegisterUser(View view) {

        String email = ET_EMAIL_SP.getText().toString().trim();
        String password = ET_PASSWORD_SP.getText().toString().trim();

        if(!ValidateUserInput()| !ValidateFullName() | !ValidateEmail() | !ValidateCellNo() | !ValidatePassword() | !ValidateCNIC())
        {
            return;
        }
        else {
            RegisterUsers(email, password);
        }

    }

    private void RegisterUsers(String email, String password) {

        String fullName = ET_Name_SP.getText().toString().trim();
        String CNIC = ET_CNIC_SP.getText().toString().trim();
        String mobileNumber = ET_PHONENUMBER_SP.getText().toString().trim();

        fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegistrationClass.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Toast.makeText(RegistrationClass.this,"Register Successful!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationClass.this,LoginClass.class));
                    finish();
                }
                else
                {
                    Toast.makeText(RegistrationClass.this,"Register Failure ",Toast.LENGTH_SHORT).show();
                }
            }
        });


        UserHelperClass helperClass = new UserHelperClass(fullName,CNIC,email,password,mobileNumber);
        reference.child(CNIC).setValue(helperClass);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }


}
