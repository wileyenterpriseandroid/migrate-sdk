package com.migrate.browser;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.migrate.api.SchemaManager;
import net.migrate.api.WebData;
import org.json.JSONObject;

public class SchemaDetailActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SchemaManager.SchemaLoaderListener
{
    public static final String KEY_URI = "SchemaDetailActivity.OBJECT_URI";

    private static final String TAG = "SchemaDetail";

    private Uri schemaUri;
    private Uri objectUri;

    private TextView schemaIdView;
    private String schemaId;

    private TextView schemaVersionView;
    private Button queryObjectsButton;
    private TextView schemaStatusView;
    private TextView schemaUpdateTimeView;
    private TextView dataUpdateTimeView;
    private TextView classnameView;
    private TextView namespaceView;
    private Button activateSyncButton;

    private JSONObject jsonSchema;
    private String jsonSchemaString;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (null != schemaUri) {
            // now load schema details
            getLoaderManager().initLoader(MigrateBrowserApplication.SCHEMA_DETAIL_LOADER_ID, null, this);
            queryObjectsButton.setEnabled(true);
        }
    }

    public void layoutSchemaFields(String schema, Cursor cursor) {
//        schemaVersionView = (TextView) findViewById(R.id.activity_schema_detail_version_view);
//        int version = WebData.Object.getInt(cursor, WebData.Schema.STATUS);
//        schemaVersionView.setText(version);
//
//        schemaStatusView = (TextView) findViewById(R.id.activity_schema_detail_status_view);
//        int status = WebData.Object.getInt(cursor, WebData.Schema.STATUS);
//        schemaStatusView.setText(status);
//
//        schemaUpdateTimeView = (TextView) findViewById(R.id.activity_schema_detail_schema_update_time_view);
//        long schemaUpdateTime = WebData.Object.getInt(cursor, WebData.Schema.WD_SCHEMA_UPDATE_TIME);
//        schemaUpdateTimeView.setText(String.valueOf(schemaUpdateTime));
//
//        dataUpdateTimeView = (TextView) findViewById(R.id.activity_schema_detail_data_update_time_view);
//        long dataUpateTime = WebData.Object.getInt(cursor, WebData.Schema.WD_DATA_UPDATE_TIME);
//        dataUpdateTimeView.setText(String.valueOf(dataUpateTime));
//
//        classnameView = (TextView) findViewById(R.id.activity_schema_detail_data_update_time_view);
//        String classname = WebData.Object.getString(cursor, WebData.Schema.WD_CLASSNAME);
//        classnameView.setText(classname);
//
//        namespaceView = (TextView) findViewById(R.id.activity_schema_detail_data_update_time_view);
//        String namespace = WebData.Object.getString(cursor, WebData.Schema.WD_CLASSNAME);
//        namespaceView.setText(namespace);
//
//        queryObjectsButton.setEnabled(true);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // actually do want all schema fields
        return new CursorLoader(this, schemaUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            throw new IllegalStateException("Schema detail for non-existent schema: " + schemaUri);
        }

        jsonSchemaString = WebData.Object.getString(cursor, WebData.Schema.JS_PROPERTIES);
        jsonSchema = SchemaHandler.loadSchema(jsonSchemaString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_schema_detail);

        if (null == state) {
            state = getIntent().getExtras();
        }
        String uri = null;
        if (null != state) {
            uri = state.getString(KEY_URI);
        }
        if (null != uri) {
            schemaUri = Uri.parse(uri);
            schemaId = WebData.Schema.getSchemaId(schemaUri);
            objectUri = WebData.Object.objectUri(schemaId);
        }

        schemaIdView = (TextView) findViewById(R.id.activity_schema_detail_schemaid_view);
        schemaIdView.setText(schemaId);

        activateSyncButton = (Button)
                findViewById(R.id.activity_schema_detail_activate_sync_button);
        activateSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateSync();
            }
        });

        queryObjectsButton = (Button)
                findViewById(R.id.activity_schema_detail_query_objects_button);
        queryObjectsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryObjects();
            }
        });
    }

    private void activateSync() {
        SchemaManager schemaManager = new SchemaManager(SchemaDetailActivity.this, schemaId,
                ((MigrateBrowserApplication)getApplication()).getUser(),
                this);
        schemaManager.initSchema();
    }

    void queryObjects() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "show details");
        }

        Intent intent = new Intent();
        intent.setClass(this, ObjectsActivity.class);
        if (null != schemaUri) {
            intent.putExtra(ObjectsActivity.KEY_URI, objectUri.toString());
        }

        if (null != jsonSchemaString) {
            intent.putExtra(ObjectsActivity.KEY_SCHEMA_JSON, jsonSchemaString);
        }

        startActivity(intent);
    }
}
