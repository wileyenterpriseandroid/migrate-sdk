package com.migrate.browser;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.migrate.api.SchemaManager;
import net.migrate.api.WebData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ObjectDetailActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SchemaManager.SchemaLoaderListener
{
    public static final String KEY_URI = "ObjectDetailActivity.OBJECT_URI";
    public static final String KEY_SCHEMA_ID = "ObjectDetailActivity.SCHEMA_ID";
    public static final String KEY_SCHEMA_JSON = "ObjectDetailActivity.SCHEMA_JSON";

    private static final String TAG = "DETAILS";

    private String schemaId;
    private JSONObject jsonSchema;
    private String jsonSchemaString;

    private Map<String, EditText> fieldEditTextMap = new HashMap<String, EditText>();

    private static final String[] PROJ = new String[] {
            BaseColumns._ID
    };

    class UpdateObject extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;
        private final ContentValues vals;
        private final Uri objectUri;

        public UpdateObject(ContentResolver resolver, Uri objectUri, ContentValues vals) {
            this.resolver = resolver;
            this.objectUri = objectUri;
            this.vals = vals;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            Uri uri = args[0];
            if (null != uri) {
                resolver.update(uri, vals, null, null);
            } else {
                resolver.insert(newObjectUri, vals);
            }
            return null;
        }
    }

    static class DeleteObject extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;

        public DeleteObject(ContentResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            resolver.delete(args[0], null, null);
            return null;
        }
    }

    private Uri objectUri;
    private Uri newObjectUri;

    private Button updateButton;
    private Button deleteButton;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (!succeeded) {
            Log.w(TAG, "Failed to initialize schema: " + schema);

            return;
        }

        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);

        if (null != objectUri) {
            getLoaderManager().initLoader(MigrateBrowserApplication.OBJECT_DETAIL_LOADER_ID, null, this);
        }

        layoutFieldViews();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, objectUri, PROJ, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateView(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (null == state) {
            state = getIntent().getExtras();
        }

        String uri = null;
        if (null != state) {
            uri = state.getString(KEY_URI);
            jsonSchemaString = state.getString(KEY_SCHEMA_JSON);
            schemaId = state.getString(KEY_SCHEMA_ID);
            newObjectUri = WebData.Object.objectUri(schemaId);
        }

        if (null != jsonSchemaString) {
            jsonSchema = Schema.loadSchema(jsonSchemaString);
        }

        if (null != uri) {
            objectUri = Uri.parse(uri);
        }

        setContentView(R.layout.activity_object_detail);

        updateButton = ((Button) findViewById(R.id.activity_detail_update));
        updateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { update(); }
                });
        updateButton.setEnabled(false);

        deleteButton = ((Button) findViewById(R.id.activity_detail_delete));
        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { delete( ); }
                });

        deleteButton.setEnabled(false);

        new SchemaManager(
                this,
                schemaId,
                ((MigrateBrowserApplication) getApplication()).getUser(),
                this)
                .initSchema();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (null != objectUri) {
            state.putString(KEY_URI, objectUri.toString());
        }
    }

    void delete() {
        if (null != objectUri) {
            new DeleteObject(getContentResolver()).execute(objectUri);
        }
        goToObjects();
    }

    void update() {
//        if (TextUtils.isEmpty(fnameView.getText().toString())) {
//            Toast.makeText(
//                    this,
//                    R.string.name_required,
//                    Toast.LENGTH_SHORT)
//                    .show();
//            return;
//        }

        ContentValues vals = new ContentValues();
        Set<Map.Entry<String, EditText>> editEntries = fieldEditTextMap.entrySet();
        Iterator entryIter = editEntries.iterator();
        while (entryIter.hasNext()) {
            Map.Entry<String, EditText> editEntry = (Map.Entry<String, EditText>) entryIter.next();
            String name = editEntry.getKey();

            if (!name.startsWith("wd_")) {
                EditText editText = editEntry.getValue();
                String text = editText.getText().toString();
                vals.put(name, text);
            }
        }

        new UpdateObject(getContentResolver(), objectUri, vals).execute(objectUri);

        goToObjects();
    }

    private void layoutFieldViews() {
        LinearLayout fieldsLayout = (LinearLayout) findViewById(R.id.activity_object_detail_field_layout);

        Iterator fieldKeys = jsonSchema.keys();
        ArrayList<String> fieldNameArray = toArray(fieldKeys);
        Iterator fieldNamesIter = fieldNameArray.iterator();

        while (fieldNamesIter.hasNext()) {
            String fieldName = (String) fieldNamesIter.next();
            try {
                JSONObject jsonObject = jsonSchema.getJSONObject(fieldName);
                String type = (String) jsonObject.get(WebData.Schema.JS_TYPE);

                layoutFieldView(fieldName, type, fieldsLayout);
            } catch (JSONException e) {
                throw new IllegalStateException("Malformed json schema: ", e);
            }
        }
    }

    private void layoutFieldView(String fieldName, String type, LinearLayout fieldsLayout) {
        LinearLayout fieldLayout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.object_detail_field, null);
        fieldsLayout.addView(fieldLayout);

        TextView fieldLabel = (TextView) fieldLayout.findViewById(R.id.object_detail_field_label);
        fieldLabel.setText(fieldName + ":");
        EditText fieldEditText = (EditText) fieldLayout.findViewById(R.id.object_detail_field_edit_text);
        if (fieldName.startsWith("wd_")) {
            fieldEditText.setEnabled(false);
        } else {
            fieldEditText.setHint(fieldName);
        }

        fieldEditTextMap.put(fieldName, fieldEditText);
    }

    private ArrayList<String> toArray(Iterator fieldNames) {
        ArrayList<String> nameList = new ArrayList();
        while (fieldNames.hasNext()) {
            String fieldName = (String) fieldNames.next();
            nameList.add(fieldName);
        }

        Collections.sort(nameList);

        return nameList;
    }

    private void populateView(Cursor c) {
        if (!c.moveToNext()) {
            return;
        }

        String[] cn = c.getColumnNames();
        for (String n : cn) {
            populateField(n, c);
        }
    }

    private void populateField(String n, Cursor c) {

    }

    private void goToObjects() {
        // TODO: should use finish also?

        Intent intent = new Intent(this, ObjectsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private String getString(Cursor c, String col) {
        return c.getString(c.getColumnIndex(col));
    }

    private void addString(
            TextView view,
            String oldVal,
            ContentValues vals,
            String col)
    {
        String s = view.getText().toString();
        if (!oldVal.equals(s)) { vals.put(col, s); }
    }
}
