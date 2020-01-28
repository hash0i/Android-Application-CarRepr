package com.fyp.amenms.HomeData;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;

public class UserTypeClass extends AppCompatActivity implements View.OnClickListener {

    private Button service_seeker;
    private Button service_provider;
    private SharedPreferences app_date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.user_type);

        service_seeker = (Button) findViewById(R.id.service_seeker);
        service_seeker.setOnClickListener(this);


        service_provider = (Button) findViewById(R.id.service_provider);
        service_provider.setOnClickListener(this);

        try
        {
            app_date = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        }
        catch (Exception e){
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.service_provider:

                Log.d("UserTypeClass","ServiceProvider");
                SharedPreferences.Editor editorVal = app_date.edit();
                editorVal.putString(Constants.PREFS_USER_TYPE,"1");
                editorVal.apply();
                Intent loginVal = new Intent(UserTypeClass.this ,LoginClass.class);
                startActivity(loginVal);
                break;


            case R.id.service_seeker:
                Log.d("UserTypeClass","ServiceSeeker");
                SharedPreferences.Editor editor = app_date.edit();
                editor.putString(Constants.PREFS_USER_TYPE,"2");
                editor.apply();
                Intent login = new Intent(UserTypeClass.this ,LoginClass.class);
                startActivity(login);
                break;



        }
    }
}
