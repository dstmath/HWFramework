package com.huawei.indexsearch;

import android.database.Cursor;
import android.net.Uri;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class IndexSearchInfo {
    private static final String TAG = "IndexSearchInfo";
    private String[] mDocFields;
    private int[] mFieldIndexStatus;
    private int[] mFieldStoreStatus;
    private IntentBuilder mIntentBuilder;
    private String mPkgName;
    private String mQuerySQLStatement;
    private String[] mTables;

    public static final class IntentBuilder {
        public static final int DEFAULT_MODE = 0;
        public static final int SPECIAL_MODE_1 = 1;
        private LinkedHashMap<String, String> intentExtraMatch = new LinkedHashMap();
        private int mode;
        private HashMap<String, String> queryMatch = new HashMap();
        private Uri uri;

        public IntentBuilder(Uri mUri, HashMap<String, String> queryMatch) {
            this.uri = mUri;
            this.queryMatch = queryMatch;
            this.mode = 0;
        }

        public IntentBuilder(Uri mUri, HashMap<String, String> queryMatch, int mode) {
            this.uri = mUri;
            this.queryMatch = queryMatch;
            this.mode = mode;
        }

        public IntentBuilder(Uri mUri, HashMap<String, String> queryMatch, LinkedHashMap<String, String> intentExtraMatch, int mode) {
            this.uri = mUri;
            this.queryMatch = queryMatch;
            this.intentExtraMatch = intentExtraMatch;
            this.mode = mode;
        }

        public String buildIntent(Cursor c) {
            Uri intentUri = this.uri;
            switch (this.mode) {
                case 0:
                    for (Entry<String, String> entry : this.queryMatch.entrySet()) {
                        intentUri = intentUri.buildUpon().appendQueryParameter((String) entry.getKey(), c.getString(c.getColumnIndex((String) entry.getValue()))).build();
                    }
                    break;
                case 1:
                    for (Entry<String, String> entry2 : this.queryMatch.entrySet()) {
                        intentUri = intentUri.buildUpon().appendPath((String) entry2.getKey()).appendPath(c.getString(c.getColumnIndex((String) entry2.getValue()))).build();
                    }
                    break;
            }
            return intentUri.toString();
        }

        public String buildIntentExtraData(Cursor c) {
            switch (this.mode) {
                case 1:
                    StringBuilder sb = new StringBuilder();
                    for (Entry<String, String> entry : this.intentExtraMatch.entrySet()) {
                        if (c.getColumnIndex((String) entry.getValue()) != -1) {
                            sb.append(c.getString(c.getColumnIndex((String) entry.getValue()))).append("/");
                        } else {
                            sb.append((String) entry.getValue()).append("/");
                        }
                    }
                    return sb.toString();
                default:
                    return null;
            }
        }

        public String getUriString() {
            return this.uri.toString();
        }

        public Uri getUriBuilder() {
            return this.uri;
        }
    }

    public IndexSearchInfo(String pkgName, String querySQLStatement, String[] tables, String[] docFields, int[] fieldStoreStatus, int[] fieldIndexStatus) {
        this.mPkgName = pkgName;
        this.mQuerySQLStatement = querySQLStatement;
        this.mTables = (String[]) tables.clone();
        this.mDocFields = (String[]) docFields.clone();
        this.mFieldStoreStatus = (int[]) fieldStoreStatus.clone();
        this.mFieldIndexStatus = (int[]) fieldIndexStatus.clone();
    }

    public IndexSearchInfo(String pkgName, String querySQLStatement, String[] tables, String[] docFields, int[] fieldStoreStatus, int[] fieldIndexStatus, IntentBuilder intentBuilder) {
        this.mPkgName = pkgName;
        this.mQuerySQLStatement = querySQLStatement;
        this.mTables = (String[]) tables.clone();
        this.mDocFields = (String[]) docFields.clone();
        this.mFieldStoreStatus = (int[]) fieldStoreStatus.clone();
        this.mFieldIndexStatus = (int[]) fieldIndexStatus.clone();
        this.mIntentBuilder = intentBuilder;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public void setPackageName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getQuerySQLStatement() {
        return this.mQuerySQLStatement;
    }

    public void setQuerySQLStatement(String mQuerySQLStatement) {
        this.mQuerySQLStatement = mQuerySQLStatement;
    }

    public String[] getTables() {
        return (String[]) this.mTables.clone();
    }

    public void setTables(String[] mTables) {
        this.mTables = (String[]) mTables.clone();
    }

    public String[] getDocFields() {
        return (String[]) this.mDocFields.clone();
    }

    public void setDocFields(String[] mDocFields) {
        this.mDocFields = (String[]) mDocFields.clone();
    }

    public int[] getFieldStoreStatus() {
        return (int[]) this.mFieldStoreStatus.clone();
    }

    public void setFieldStoreStatus(int[] mFieldStoreStatus) {
        this.mFieldStoreStatus = (int[]) mFieldStoreStatus.clone();
    }

    public int[] getFieldIndexStatus() {
        return (int[]) this.mFieldIndexStatus.clone();
    }

    public void setFieldIndexStatus(int[] mFieldIndexStatus) {
        this.mFieldIndexStatus = (int[]) mFieldIndexStatus.clone();
    }

    public IntentBuilder getIntentBuilder() {
        return this.mIntentBuilder;
    }

    public String getUriString() {
        return this.mIntentBuilder.getUriString();
    }

    public String buildIntent(Cursor c) {
        return this.mIntentBuilder.buildIntent(c);
    }
}
