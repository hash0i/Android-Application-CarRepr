package com.fyp.amenms.Utilities;

import android.app.AlertDialog;
import android.content.Context;

public class Constants {
    public static final String PREFS_NAME = "com.example.amas";
    public static final String PREFS_USER_TYPE = "prefs_user_type";
    public static final String PREFS_USER_NAME = "prefs_user_name";
    public static final String PREFS_USER_EMAIL = "prefs_user_email";
    public static final String PREFS_USER_IMG = "prefs_user_img";
    public static final String PREFS_USER_ID = "prefs_user_id";
    public static final String PREFS_USER_PHONE = "prefs_user_phone";
    public static final String PREFS_SERVICE_PROVIDER = "prefs_user_profession";
    public static final String PREFS_LOGIN_STATE = "prefs_user_login_state";

    public static final String PREFS_SERVICE_PROVIDER_HRS = "prefs_user_hrs";
    public static final String PREFS_USER_LOCATION = "prefs_user_loc";
    public static final String PREFS_USER_LAT = "prefs_user_lat";
    public static final String PREFS_USER_LNG = "prefs_user_lng";
    public static final String PREFS_USER_CITY = "prefs_user_city";

    public static final String PREFS_ORDER_ID = "prefs_order_id";
    public static final String PREFS_ORDER_STATUS = "prefs_order_status";

    public static final String PREFS_PAYMENT_ID = "prefs_payment_id";
    public static final String PREFS_PAYMENT_TOTAl = "prefs_payment_total";

    //New Constants
    public static final String LOGGED_IN_USER = "LOGGED_IN_USER";
    public static final String TYPE_USER = "users";
    public static final String TYPE_PROVIDER = "providers";


    public static void showAlertMessage(Context context, String title, String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setNegativeButton("OK", null);
            builder.show();

        } catch (Exception e) {
        }
    }
}

