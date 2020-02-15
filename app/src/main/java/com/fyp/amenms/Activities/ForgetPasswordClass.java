package com.fyp.amenms.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fyp.amenms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordClass extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText forget_email;
    private Button forget_btn;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgetpassword);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ForgetPassword);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        forget_btn = (Button) findViewById(R.id.forget_btn);
        forget_email = findViewById(R.id.forget_email);

        forget_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                String forget_e = forget_email.getText().toString().trim();
                if(TextUtils.isEmpty(forget_e))
                {
                    forget_email.setError("Field cannot be empty");
                }
                else
                    if(!forget_e.matches(emailPattern))
                    {
                        forget_email.setError("Please enter a validate Email address");
                    }
                else
                {
                    firebaseAuth.sendPasswordResetEmail(forget_e).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(ForgetPasswordClass.this,"Please check Email Account if you want to reset password",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgetPasswordClass.this, LoginActivity.class));
                                finish();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ForgetPasswordClass.this,"Error occured"+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
    @Override
    public void onClick(View view) {
    }

    @Override
    public void onBackPressed() {}
}
