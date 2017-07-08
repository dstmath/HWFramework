package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.android.internal.telephony.HbpcdLookup.ArbitraryMccSidMatch;
import com.android.internal.telephony.HbpcdLookup.MccIdd;
import com.android.internal.telephony.HbpcdLookup.MccLookup;
import com.android.internal.telephony.HbpcdLookup.MccSidConflicts;
import com.android.internal.telephony.HbpcdLookup.MccSidRange;

public final class HbpcdUtils {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HbpcdUtils";
    private ContentResolver resolver;

    public HbpcdUtils(Context context) {
        this.resolver = null;
        this.resolver = context.getContentResolver();
    }

    public int getMcc(int sid, int tz, int DSTflag, boolean isNitzTimeZone) {
        Cursor c2 = this.resolver.query(ArbitraryMccSidMatch.CONTENT_URI, new String[]{MccSidRange.MCC}, "SID=" + sid, null, null);
        if (c2 != null) {
            if (c2.getCount() == 1) {
                c2.moveToFirst();
                int tmpMcc = c2.getInt(0);
                c2.close();
                return tmpMcc;
            }
            c2.close();
        }
        Cursor c3 = this.resolver.query(MccSidConflicts.CONTENT_URI, new String[]{MccSidRange.MCC}, "SID_Conflict=" + sid + " and (((" + MccLookup.GMT_OFFSET_LOW + "<=" + tz + ") and (" + tz + "<=" + MccLookup.GMT_OFFSET_HIGH + ") and (" + "0=" + DSTflag + ")) or ((" + MccLookup.GMT_DST_LOW + "<=" + tz + ") and (" + tz + "<=" + MccLookup.GMT_DST_HIGH + ") and (" + "1=" + DSTflag + ")))", null, null);
        if (c3 != null) {
            int c3Counter = c3.getCount();
            if (c3Counter > 0) {
                if (c3Counter > 1) {
                    Log.w(LOG_TAG, "something wrong, get more results for 1 conflict SID: " + c3);
                }
                c3.moveToFirst();
                tmpMcc = c3.getInt(0);
                if (isNitzTimeZone) {
                    return tmpMcc;
                }
                return 0;
            }
            c3.close();
        }
        Cursor c5 = this.resolver.query(MccSidRange.CONTENT_URI, new String[]{MccSidRange.MCC}, "SID_Range_Low<=" + sid + " and " + MccSidRange.RANGE_HIGH + ">=" + sid, null, null);
        if (c5 != null) {
            if (c5.getCount() > 0) {
                c5.moveToFirst();
                tmpMcc = c5.getInt(0);
                c5.close();
                return tmpMcc;
            }
            c5.close();
        }
        return 0;
    }

    public String getIddByMcc(int mcc) {
        String idd = "";
        Cursor cur = this.resolver.query(MccIdd.CONTENT_URI, new String[]{MccIdd.IDD}, "MCC=" + mcc, null, null);
        if (cur != null) {
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                idd = cur.getString(0);
            }
            cur.close();
        }
        return idd;
    }
}
