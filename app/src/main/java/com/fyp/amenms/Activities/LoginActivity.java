package com.fyp.amenms.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import android.widget.Toolbar;

public class LoginActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String KEY_EMPTY = "";

    private TextInputEditText et_login_email;
    private TextInputEditText et_login_password;
    private Button btnLogin;
    private ProgressDialog pDialog;
    private TextView forgot_pass;
    private SessionManager sessionManager;
    private FirebaseAuth fAuth;

    DatabaseReference firebaseDbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Login);
        setSupportActionBar(toolbar);

        Log.d("LoginActivity","Oncreate");

        et_login_email = findViewById(R.id.login_Email);
        et_login_password = findViewById(R.id.login_Password);
        btnLogin = (Button) findViewById(R.id.login_btn);
        forgot_pass = (TextView)findViewById(R.id.forgot_pass);
        forgot_pass.setOnTouchListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        et_login_email.addTextChangedListener(loginTextWatcher);
        et_login_password.addTextChangedListener(loginTextWatcher);

        fAuth = FirebaseAuth.getInstance();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        sessionManager = new SessionManager(this);
        /*if(sessionManager.isLoggedIn())
        {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }*/
        if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_USER)){
            firebaseDbReference = FirebaseDatabase.getInstance().getReference(Constants.TYPE_USER);
        } else if(sessionManager.getKey(Constants.PREFS_USER_TYPE).equals(Constants.TYPE_PROVIDER)){
            firebaseDbReference = FirebaseDatabase.getInstance().getReference(Constants.TYPE_PROVIDER);
        } else {
            finish();
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = et_login_email.getText().toString().trim();
                String Password = et_login_password.getText().toString().trim();
                checkLogin(Email,Password);
            }
        });


    }

    //validation for button login
    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String usernameInput = et_login_email.getText().toString().trim();
            String passwordInput = et_login_password.getText().toString().trim();

            btnLogin.setEnabled(!usernameInput.isEmpty() && !passwordInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void onClick_RegisterationSP(View view) {

        switch (view.getId()) {

            case R.id.register:
                Intent register =new Intent(LoginActivity.this , RegistrationActivity.class);
                startActivity(register);
                finish();
                break;

            case R.id.forgot_pass:
                Intent forget_Password =new Intent(LoginActivity.this ,ForgetPasswordClass.class);
                startActivity(forget_Password);
                finish();
                break;

        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Intent i = new Intent(getApplicationContext(), ForgetPasswordClass.class);
        startActivity(i);
        return true;
    }



    private void checkLogin(String email, String password) {
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser firebaseUser = task.getResult().getUser();

                            firebaseDbReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        if (dataSnapshot.getValue() != null) {
                                            Toast.makeText(LoginActivity.this, "Log-In Successful", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Sign in database failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {
                                    Toast.makeText(LoginActivity.this, "Sign in cancelled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else { Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_LONG).show(); }
                    }
        });

    }

    //Volley-Code
//    private void checkLogin(final String email, final String password)
//    {
//        String tag_string_req = "req_login";
//
//        Log.d("Login Re","");
//        pDialog.setMessage("Logging in...");
//        showDialog();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                Appconfig.URL_LOGIN, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//
//                Log.d("Login Response", response.toString());
//                hideDialog();
//
//                try{
//                    JSONObject jsonObject = new JSONObject(response);
//                    boolean error = jsonObject.getBoolean("error");
//
//                    if(!error){
//                        sessionManager.setLogin(true);
//                        String uid = jsonObject.getString("uid");
//
//                        JSONObject user = jsonObject.getJSONObject("user");
//                        String name = user.getString("name");
//                        String email = user.getString("email");
//                        String create_at = user.getString("created_at");
//
//                        db.addUser(name, email, uid, create_at);
//
//                        Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                    else
//                    {
//                        String errorMsg = jsonObject.getString("error_msg");
//                        Toast.makeText(getApplicationContext(),errorMsg,Toast.LENGTH_SHORT).show();
//                    }
//
//                }  catch(JSONException ex){ ex.printStackTrace();}
//            }
//        }, new Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("Login Error","OnErrorResponse");
//                hideDialog();
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("email",email);
//                params.put("password",password);
//                return params;
//            }
//        };
//
//        AppController.getInstance().addToRequestQueue(stringRequest,tag_string_req);
//    }



    private void showDialog() {
        if(!pDialog.isShowing())
        {
            pDialog.show();
        }
    }
    private void hideDialog(){
        if(pDialog.isShowing())
        {
            pDialog.dismiss();
        }
    }
}
