package com.migrate.browser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Schema {
    private static final String TAG = "MigrateBrowserSchema";

    public static JSONObject loadSchema(String jsonSchemaString) {
        JSONTokener jsonTokener = new JSONTokener(jsonSchemaString);
        try {
            return new JSONObject(jsonTokener);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
