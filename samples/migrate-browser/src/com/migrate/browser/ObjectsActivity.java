package com.migrate.browser;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.migrate.api.SchemaManager;
import net.migrate.api.WebData;

public class ObjectsActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SchemaManager.SchemaLoaderListener
{
    public static final String KEY_URI = "ObjectsActivity.Schema_URI";
    public static final String KEY_SCHEMA_JSON = "ObjectsActivity.SCHEMA_JSON";

    private static final String TAG = "OBJECTS";

    public static final String[] PROJ = new String[] {
            BaseColumns._ID,
            WebData.Object.WD_DATA_ID,
            WebData.Object.WD_VERSION,
            WebData.Object.WD_IN_CONFLICT
    };

    private static final String[] FROM = new String[] {
            WebData.Object.WD_DATA_ID,
            WebData.Object.WD_VERSION
    };

    static {
    }

    private static final int[] TO = new int[] {
            R.id.row_object_data_id_view,
            R.id.row_object_data_version_view
    };

    private SimpleCursorAdapter listAdapter;

    private Uri objectUri;

    private String schemaId;
    private String jsonSchemaString;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (!succeeded) {
            Log.w(TAG,
                    "Failed to initialize schema: " + schemaId + " @ " + objectUri);

            // add failure handling code

            return;
        }

        getLoaderManager().initLoader(MigrateBrowserApplication.OBJECTS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating content loader: " + objectUri);
        return new CursorLoader(this, objectUri, PROJ, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // getting a null pointer exception here

        Log.d(TAG, "Content loaded: " + cursor.getCount());
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }

    private class ObjectRowAdapter extends SimpleCursorAdapter {
        private ObjectRowAdapter(Context context, int layout,
                                 Cursor c, String[] from, int[] to, int flags)
        {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            boolean inConflict = WebData.Object.inConflict(cursor);

            if (inConflict) {
                view.setBackgroundResource(R.color.conflicted);
            } else {
                view.setBackgroundResource(android.R.color.black);
            }

            super.bindView(view, context, cursor);
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_objects);

        if (null == state) {
            state = getIntent().getExtras();
        }
        String uri = null;
        if (null != state) {
            uri = state.getString(KEY_URI);
            jsonSchemaString = state.getString(KEY_SCHEMA_JSON);
        }
        if (null != uri) {
            objectUri = Uri.parse(uri);
            schemaId = WebData.Schema.getSchemaId(objectUri);
        }

        findViewById(R.id.activity_object_list_new).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDetails(null);
                    }
                });

        listAdapter = new ObjectRowAdapter(
                this,
                R.layout.object_row,
                null,
                FROM,
                TO,
                0);

        ListView listView
                = ((ListView) findViewById(R.id.activity_object_list));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                if (WebData.Object.inConflict(listAdapter.getCursor())) {
                    resolveConflict(p);
                } else {
                    showDetails(p);
                }
            }
        });

        new SchemaManager(
                this,
                schemaId,
                ((MigrateBrowserApplication) getApplication()).getUser(),
                this)
                .initSchema();
    }

    void resolveConflict(int pos) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "resolve conflicts");
        }

        Cursor c = (Cursor) listAdapter.getItem(pos);

        int di = c.getColumnIndex(WebData.Object.WD_DATA_ID);
        String dataID = c.getString(di);
        resolveConflict(dataID);
    }

    void resolveConflict(String uuid) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "resolve conflict");
        }

        Intent intent = new Intent();
        intent.setClass(this, ResolveObjectActivity.class);

        Uri dataUri = WebData.Object.objectUri(schemaId)
                .buildUpon().appendPath(uuid).build();

        if (null != dataUri) {
            intent.putExtra(ResolveObjectActivity.KEY_URI, dataUri.toString());
        }

        if (null != schemaId) {
            intent.putExtra(ObjectDetailActivity.KEY_SCHEMA_ID, schemaId);
        }

        if (null != jsonSchemaString) {
            intent.putExtra(ResolveObjectActivity.KEY_SCHEMA_JSON, jsonSchemaString);
        }

        startActivity(intent);
    }

    void showDetails(int pos) {
        Cursor c = (Cursor) listAdapter.getItem(pos);
        showDetails(objectUri.buildUpon().
                appendPath(c.getString(c.getColumnIndex(BaseColumns._ID))).build());
    }

    void showDetails(Uri uri) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "show details"); }
        Intent intent = new Intent();
        intent.setClass(this, ObjectDetailActivity.class);

        if (null != uri) {
            intent.putExtra(ObjectDetailActivity.KEY_URI, uri.toString());
        }

        if (null != schemaId) {
            intent.putExtra(ObjectDetailActivity.KEY_SCHEMA_ID, schemaId);
        }

        if (null != jsonSchemaString) {
            intent.putExtra(ObjectDetailActivity.KEY_SCHEMA_JSON, jsonSchemaString);
        }

        startActivity(intent);
    }
}
