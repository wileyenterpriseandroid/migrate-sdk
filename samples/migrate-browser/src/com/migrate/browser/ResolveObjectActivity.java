package com.migrate.browser;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import net.migrate.api.WebData;
import org.json.JSONObject;

import java.util.*;

public class ResolveObjectActivity extends Activity {
    private static final String TAG = "";

    public static final String KEY_URI = "object_uri";
    public static final String KEY_SCHEMA_JSON = "json_schema";

    private Uri objectDataUri;
    private Uri conflictUri;
    private Uri conflictDataUri;

    private Button resolveButton;

    private String dataId;

    private int conflictVersion;

    private Map<String, Object> object;
    private Map<String, Object> conflict;
    private Map<String, Object> resolved  = new HashMap<String, Object>();

    private GridLayout fieldGrid;
    public ArrayList checkBoxes = new ArrayList();

    public ResolveObjectActivity() {
        super();
    }

    private JSONObject jsonSchema;
    private String jsonSchemaString;
    private String[] objectProj;
    private String[] conflictProj;

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
        }

        if (null != jsonSchemaString) {
            jsonSchema = SchemaHandler.loadSchema(jsonSchemaString);
            objectProj = SchemaHandler.buildObjectProj(jsonSchema);
            conflictProj = SchemaHandler.buildConflictProj(jsonSchema);
        }

        if (null != uri) {
            objectDataUri = Uri.parse(uri);

            String schemaId = WebData.Schema.getSchemaId(objectDataUri);
            conflictUri = WebData.Object.conflictUri(schemaId);
        }

        setContentView(R.layout.activity_resolve_object);

        resolveButton = (Button) findViewById(R.id.activity_resolve_button);
        resolveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (allChecked(checkBoxes)) {
                    resolveConflict();
                } else {
                    Toast.makeText(ResolveObjectActivity.this,
                            R.string.activity_resolve_need_all_fields, Toast.LENGTH_LONG).show();
                }
            }
        });

        fieldGrid = (GridLayout) findViewById(R.id.activity_resolve_field_grid);

        if (null != conflictUri) {
            getLoaderManager().initLoader(MigrateBrowserApplication.RESOLVE_OBJECT_LOADER_ID,
                    null, new ObjectLoader());
        }
    }

    private boolean allChecked(List<CheckBox> checkBoxes) {
        Iterator<CheckBox> checkIter = checkBoxes.iterator();
        while (checkIter.hasNext()) {
            CheckBox checkBox = checkIter.next();
            if (!checkBox.isChecked()) {
                return false;
            }
        }
        return true;
    }

    private void resolveConflict() {
        ContentValues resolvedValues = new ContentValues();

        Iterator resolvedIter = resolved.entrySet().iterator();
        while (resolvedIter.hasNext()) {
            Map.Entry resolvedEntry = (Map.Entry) resolvedIter.next();
            String name = (String) resolvedEntry.getKey();
            Object value = resolvedEntry.getValue();

            if (value != null) {
                if (value instanceof Double) {
                    resolvedValues.put(name, (Double)value);
                } else if (value instanceof Integer) {
                    resolvedValues.put(name, (Integer)value);
                } else if (value instanceof String) {
                    resolvedValues.put(name, (String)value);
                } else {
                    throw new IllegalStateException("Unsupported type: " + value);
                }
            }
        }

        resolvedValues.put(WebData.Object.WD_VERSION, conflictVersion);

        ResolveConflictTask resolveTask = new ResolveConflictTask(resolvedValues);
        resolveTask.doInBackground();
    }

    private class ResolveConflictTask extends AsyncTask
    {
        private final ContentValues resolvedValues;

        ResolveConflictTask(ContentValues resolvedValues) {
            this.resolvedValues = resolvedValues;
        }

        @Override
        protected Object doInBackground(Object... params) {
            getContentResolver().update(conflictDataUri, resolvedValues, null, null);
            ResolveObjectActivity.this.finish();
            return null;
        }
    }

    private class ObjectLoader implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "Loader for conflict data: " + objectDataUri);
            return new CursorLoader(
                    ResolveObjectActivity.this,
                    objectDataUri,
                    objectProj,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToNext()) {
                return;
            }

            String s = WebData.Object.getString(data, WebData.Object.WD_DATA_ID);
            dataId = (TextUtils.isEmpty(s)) ? "" : s;

            conflictDataUri = conflictUri.buildUpon().appendPath(dataId).build();

            object = MigrateBrowserApplication.loadObject(data, jsonSchema);

            if (null != conflictUri) {
                getLoaderManager().initLoader(MigrateBrowserApplication.CONFLICT_LOADER_ID, null, new ConflictLoader());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private class ConflictLoader implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "Loader for object data: " + conflictUri);
            return new CursorLoader(
                    ResolveObjectActivity.this,
                    conflictUri,
                    conflictProj,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            populateConflictGrid(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private void populateConflictGrid(Cursor c) {
        if (!c.moveToNext()) {
            return;
        }

        String s = WebData.Object.getString(c, WebData.Object.WD_VERSION);
        conflictVersion = Integer.parseInt((TextUtils.isEmpty(s)) ? "" : s);
        conflict = MigrateBrowserApplication.loadObject(c, jsonSchema);

        Iterator<Map.Entry<String, Object>> objectIter = object.entrySet().iterator();
        Iterator<Map.Entry<String, Object>> conflictIter = conflict.entrySet().iterator();

        while (objectIter.hasNext()) {
            Map.Entry<String, Object> entry = objectIter.next();
            Map.Entry<String, Object> conflictEntry = conflictIter.next();

            TextView label = new TextView(this);
            CheckBox checkBox = new CheckBox(this);
            checkBoxes.add(checkBox);
            Spinner spinner = new Spinner(this);

            String name = entry.getKey();
            Object value = entry.getValue();
            Object conflictValue = conflictEntry.getValue();

            populateGridField(name, value, conflictValue, label, spinner, checkBox);
        }
    }

    private void populateGridField(String name, Object value, Object conflictValue,
                                   TextView alreadyResolvedTextView,
                                   final Spinner spinner,
                                   final CheckBox conflictCheckBox)
    {
        fieldGrid.addView(conflictCheckBox);

        LinearLayout conflictLayout = new LinearLayout(this);
        conflictLayout.addView(alreadyResolvedTextView);
        conflictLayout.addView(spinner);
        fieldGrid.addView(conflictLayout);

        if ((value != null) && (conflictValue == null)) {
            populateConflictField(name, value, conflictValue, conflictCheckBox, alreadyResolvedTextView, spinner);

        } else if ((value == null) && (conflictValue != null)) {
            populateConflictField(name, value, conflictValue, conflictCheckBox, alreadyResolvedTextView, spinner);

        } else if ((null == value) /*&& (null == conflictFname)*/ ) {
            // both null
            populateResolvedField(value, alreadyResolvedTextView, spinner, conflictCheckBox);

            // nothing to set
        } else if (value.equals(conflictValue)) {
            populateResolvedField(value, alreadyResolvedTextView, spinner, conflictCheckBox);
        } else {
            populateConflictField(name, value, conflictValue, conflictCheckBox, alreadyResolvedTextView, spinner);
        }
    }

    private void populateConflictField(final String name, Object value, Object conflictValue,
                                       final CheckBox conflictCheckBox, TextView alreadyResolvedTextView,
                                       final Spinner spinner)
    {
        conflictCheckBox.setEnabled(true);
        alreadyResolvedTextView.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // spinner seems to acquire color of selected - instead want color of resolved
                setSpinnerColor(spinner, conflictCheckBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        conflictCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private String fieldName = name;
            private Spinner conflictSpinner = spinner;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSpinnerColor(spinner, conflictCheckBox);
                if (isChecked) {
                    conflictSpinner.setEnabled(false);
                    Object selectedItem = conflictSpinner.getSelectedItem(); // make spinner inactive
                    resolved.put(fieldName, selectedItem);
                } else {
                    conflictSpinner.setEnabled(true);
                    resolved.put(fieldName, null);  // make spinner active
                }
            }
        });

        String stringValue = toString(value);
        String stringConflictValue = toString(conflictValue);

        ArrayAdapter<String> conflictAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_conflict_text, new String[] {
                        stringValue, stringConflictValue
                });
        spinner.setAdapter(conflictAdapter);
    }

    private String toString(Object value) {
        return (value == null ? "" : value.toString());
    }

    private void populateResolvedField(Object value, TextView alreadyResolvedTextView,
                                       Spinner spinner, CheckBox conflictCheckBox)
    {
        alreadyResolvedTextView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        conflictCheckBox.setChecked(true);
        conflictCheckBox.setEnabled(false);
        if (value != null) {
            String stringValue = toString(value);
            alreadyResolvedTextView.setText(stringValue);
        }
    }

    private void setSpinnerColor(Spinner spinner, CheckBox conflictCheckBox) {
        TextView spinnerTextView = (TextView) spinner.findViewById(R.id.conflict_spinner_text_view);
        if (conflictCheckBox.isChecked()) {
            spinnerTextView.setTextColor(getResources().getColor(R.color.resolved));
        } else {
            spinnerTextView.setTextColor(getResources().getColor(R.color.conflicted));
        }
    }
}
