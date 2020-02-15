package com.fyp.amenms.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.SessionManager;

public class SelectUserTypeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button service_seeker;
    private Button service_provider;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.user_type);

        sessionManager = new SessionManager(this);
        service_seeker = (Button) findViewById(R.id.service_seeker);
        service_seeker.setOnClickListener(this);


        service_provider = (Button) findViewById(R.id.service_provider);
        service_provider.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.service_provider:

                Log.d("SelectUserTypeActivity","ServiceProvider");
                sessionManager.putKey(Constants.PREFS_USER_TYPE,Constants.TYPE_PROVIDER);
                Intent loginVal = new Intent(SelectUserTypeActivity.this , LoginActivity.class);
                startActivity(loginVal);
                break;


            case R.id.service_seeker:
                Log.d("SelectUserTypeActivity","ServiceSeeker");
                sessionManager.putKey(Constants.PREFS_USER_TYPE,Constants.TYPE_USER);
                Intent login = new Intent(SelectUserTypeActivity.this , LoginActivity.class);
                startActivity(login);
                break;
        }
    }

    @Override
    public void onBackPressed() {}
}
