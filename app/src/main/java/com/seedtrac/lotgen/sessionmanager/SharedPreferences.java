package com.seedtrac.lotgen.sessionmanager;

import android.content.Context;

import com.google.gson.Gson;

public class SharedPreferences {
    private final android.content.SharedPreferences pref;
    private final android.content.SharedPreferences.Editor editor;
    private static final String PREFER_NAME = "ACapPref";
    private static SharedPreferences sSharedPrefs;
    public static final String KEY_LOGIN_OBJ = "login_obj";

    public SharedPreferences(Context context) {
        pref = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static SharedPreferences getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new SharedPreferences(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static SharedPreferences getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        //Option 1:
        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
        //Option 2:
        // Alternatively, you can create a new instance here
        // with something like this:
        // getInstance(MyCustomApplication.getAppContext());
    }

    public void storeObject(String key, Object object) {
        Gson gson = new Gson();
        String json = null;
        if (object != null)
            json = gson.toJson(object);
        editor.putString(key, json);
        editor.commit();
    }

    public Object getObject(String key, Class cls) {
        Gson gson = new Gson();
        String json = pref.getString(key, "");
        if (!json.equals("")) {
            return gson.fromJson(json, cls);
        } else {
            return null;
        }
    }

    public void clearSharedPref(){
        editor.clear();
        editor.commit();
    }
}
