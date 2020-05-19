package com.fyp.amenms.Activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fyp.amenms.R;
import com.fyp.amenms.Utilities.Constants;
import com.fyp.amenms.database.SessionManager;

import java.util.List;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class CoreActivity extends AppCompatActivity {
    private static final String TAG = "CoreActivity";
    private static Context mContext;
    private static ProgressDialog pDialog;
    private ProgressDialog progressDialog;
    private Handler statusHandler;
    private Runnable statusRunnable;
    private long userLeaveTime;

    public static void showProgres() {
        hidepDialog();
        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage(mContext.getString(R.string.please_wait_msg));
        //pDialog.setCancelable(false);

        showpDialog();


    }

    public static void showpDialog() {
        if (!pDialog.isShowing() && pDialog != null) {
            pDialog.show();
        }
    }

    public static void hidepDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mContext = CoreActivity.this;


    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        userLeaveTime = System.currentTimeMillis();
        Constants.isUserLeave = true;
        Log.d(TAG, "onUserLeaveHint: ");


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStop() {
        super.onStop();
        hidepDialog();
    }

    public void initProgress(String message, boolean cancelable) {
        progressDialog = getProgressDialogInstance();
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
    }

    public ProgressDialog getProgressDialogInstance() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.color_primary_progress_dialog));
        dialog.setIndeterminate(true);
        return dialog;
    }

    public void showKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showProgressDialog() {
        try {
            if (progressDialog != null && !progressDialog.isShowing() && !isFinishing())
                progressDialog.show();
        } catch (Exception e) {
           Log.e(TAG, "showProgressDialog: ", e);
        }
    }

    public void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing() && !isFinishing())
                progressDialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "hideProgressDialog: ", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Your own code to create the view
        // ...
        Log.d(TAG, "onCreate: ");
        Constants.isUserLeave = false;
        Constants.isRecentClicked = false;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        checkForUpdates();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // ... your own onResume implementation
        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isAppRunning()) {
            if (statusHandler != null) {
                statusHandler.removeCallbacks(statusRunnable);
            }
            /*//Start our background service and check message service not running
            if (!AppUtils.isServiceRunning(CoreActivity.this, MessageService.class)){

            }*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkForCrashes() {
        //CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        //UpdateManager.register(this);
    }

    private void unregisterManagers() {
        //UpdateManager.unregister();
    }

    private boolean isAppRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    public boolean checkAudioRecordPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}
