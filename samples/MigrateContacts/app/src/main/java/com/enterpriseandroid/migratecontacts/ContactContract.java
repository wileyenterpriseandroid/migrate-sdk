/* Generated Source Code - Do not Edit! */
package com.enterpriseandroid.migratecontacts;

import android.net.Uri;
import android.provider.BaseColumns;
import net.migrate.api.WebData;

public final class ContactContract {
    private ContactContract() {}

    public static final String SCHEMA_ID = "com.enterpriseandroid.migratecontacts.Contact";

    public static final Uri SCHEMA_CONTACT_URI = WebData.Schema.schemaUri(SCHEMA_ID);
    public static final Uri OBJECT_CONTACT_URI = WebData.Object.objectUri(SCHEMA_ID);
    public static final Uri ADMIN_OBJECT_CONTACT_URI = WebData.Object.objectUri(SCHEMA_ID, true);
    public static final Uri CONFLICT_CONTACT_URI = WebData.Object.conflictUri(SCHEMA_ID);
    public static final Uri ADMIN_CONFLICT_CONTACT_URI = WebData.Object.conflictUri(SCHEMA_ID, true);

    public static final class Columns implements BaseColumns {
        private Columns() {}

        public static final String FIRSTNAME = "firstname";
        public static final String EMAIL = "email";
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String LASTNAME = "lastname";
    }
}
