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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.migrate.api.SchemaManager;
import net.migrate.api.WebData;

public class SchemasActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String TAG = "SCHEMAS";

    public static final String[] PROJ = new String[] {
            WebData.Schema._ID,
            WebData.Schema.WD_SCHEMA_ID
    };

    public static final String[] FROM = new String[] {
            WebData.Schema.WD_SCHEMA_ID,
    };

    private static final int[] TO = new int[] {
            R.id.row_schema_schema_id
    };

    private SimpleCursorAdapter listAdapter;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_schemas);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.schema_row,
                null,
                FROM,
                TO,
                0);

        ListView listView
                = ((ListView) findViewById(R.id.activity_schemas_list));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                showDetails(p);
            }
        });

        SchemaManager adminManager = new SchemaManager(this, "*", "userNotSupportedYet",
                new SchemaManager.SchemaLoaderListener() {
                    @Override
                    public void onSchemaLoaded(String s, boolean b) {
                    }
                }, true);
        adminManager.initSchema();

        getLoaderManager().initLoader(MigrateBrowserApplication.SCHEMAS_LOADER_ID, null,
                SchemasActivity.this);
    }

    void showDetails(int pos) {
        Cursor c = (Cursor) listAdapter.getItem(pos);
        String schemaID = WebData.Object.getString(c, WebData.Schema.WD_SCHEMA_ID);
        showDetails(WebData.Schema.schemaUri(schemaID));
    }

    void showDetails(Uri uri) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "show details"); }
        Intent intent = new Intent();
        intent.setClass(this, SchemaDetailActivity.class);
        if (null != uri) {
            intent.putExtra(SchemaDetailActivity.KEY_URI, uri.toString());
        }
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating all schemas loader: " + WebData.Schema.ADMIN_SCHEMA_CONTENT_URI);
        return new CursorLoader(
                this,
                WebData.Schema.SCHEMA_CONTENT_URI,
//                WebData.Schema.ADMIN_SCHEMA_CONTENT_URI,
                PROJ,
                null,
                null,
                WebData.Schema.WD_SCHEMA_ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // getting a null pointer exception here

        if (cursor == null) {
            Log.d(TAG, "Content loaded: null");
        } else {
            Log.d(TAG, "Content loaded: " + cursor.getCount());
        }
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }
}
