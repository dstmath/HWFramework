package com.android.internal.telephony;

import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public class HwCustIccProviderUtils {
    public void addURI(UriMatcher uriMatcher) {
    }

    public Cursor handleCustQuery(UriMatcher uriMatcher, Uri url, String[] selectionArgs, String[] addressColumns) {
        return null;
    }

    public void fdnCacheProcess(List<AdnRecord> list, int efType, long subId) {
    }
}
