package com.migrate.browser;

import android.provider.BaseColumns;
import net.migrate.api.WebData;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaHandler {
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

    public static String[] buildObjectProj(JSONObject jsonSchema) {
        return buildObjectProj(jsonSchema, true, false);
    }

    public static String[] buildConflictProj(JSONObject jsonSchema) {
        return buildObjectProj(jsonSchema, false, true);
    }

    private static String[] buildObjectProj(JSONObject jsonSchema, boolean obj, boolean conflict) {
        Iterator keys = jsonSchema.keys();
        ArrayList<String> projList = new ArrayList<String>();
        projList.add(BaseColumns._ID);
        if (obj) {
            projList.add(WebData.Object.WD_DELETED);
            projList.add(WebData.Object.WD_IN_CONFLICT);
        }
        while (keys.hasNext()) {
            String key = (String) keys.next();
            projList.add(key);
        }
        String[] proj = projList.toArray(new String[] {});
        return proj;
    }
}
