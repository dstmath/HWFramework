package com.android.internal.telephony;

import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import java.util.List;

public class HwCustIccProviderUtils {
    public void addURI(UriMatcher uriMatcher) {
    }

    public Cursor handleCustQuery(UriMatcher uriMatcher, Uri url, String[] selectionArgs, String[] addressColumns) {
        return null;
    }

    public void fdnCacheProcess(List<AdnRecordExt> list, int efType, long subId) {
    }
}
