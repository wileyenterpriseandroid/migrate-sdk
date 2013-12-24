package com.migrate.browser;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import net.migrate.api.WebData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MigrateBrowserApplication extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "APP";
    private static final String DEFAULT_USER = "user";

    public static final int OBJECTS_LOADER_ID = 42;
    public static final int SCHEMAS_LOADER_ID = 43;
    public static final int SCHEMA_DETAIL_LOADER_ID = 44;
    public static final int OBJECT_DETAIL_LOADER_ID = 45;
    public static final int RESOLVE_OBJECT_LOADER_ID = 46;
    public static final int CONFLICT_LOADER_ID = 47;

    private String keyUser;
    private String user;
    private String[] schemas;

    static Map loadObject(Cursor c, JSONObject jsonSchema) {
        Map<String, Object> loadedObject = new HashMap<String, Object>();

        Iterator keyIter = jsonSchema.keys();
        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();
            try {
                JSONObject metaData = jsonSchema.getJSONObject(key);
                String type = metaData.getString(WebData.Schema.JS_TYPE);

                if (WebData.Schema.JS_STRING.equals(type)) {
                    String s = WebData.Object.getString(c, key);
                    loadedObject.put(key, s);
                } else if (WebData.Schema.JS_LONG.equals(type)) {
                    int i = WebData.Object.getInt(c, key);
                } else if (WebData.Schema.JS_INTEGER.equals(type)) {
                    int i = WebData.Object.getInt(c, key);
                    loadedObject.put(key, i);
                } else if (WebData.Schema.JS_DOUBLE.equals(type)) {
                    double d = WebData.Object.getDouble(c, key);
                    loadedObject.put(key, d);
                } else {
                    throw new IllegalArgumentException("Unsupported cursor type: " + type);
                }
            } catch (JSONException e) {
                Log.d(TAG, "Should not happen", e);
            }
        }

        return loadedObject;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Migrate browser running...");
        }

        keyUser = getString(R.string.prefs_user_key);

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        user = null;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public String getUser() {
//        synchronized (this) {
//            if (null == user) {
//                user = PreferenceManager.getDefaultSharedPreferences(this)
//                        .getString(keyUser, null);
//            }
//
//            return (null != user) ? user : DEFAULT_USER;
//        }

        return DEFAULT_USER;
    }
}
