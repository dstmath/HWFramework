package com.android.contacts.hap.numbermark.hwtencent.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.util.HwLog;

public final class TencentApiManager {
    private static final String CALL_TYPE_CALLED = "18";
    private static final String CALL_TYPE_CALLING = "17";
    private static final String CALL_TYPE_COMMON = "16";
    private static final String CONNECTION_TIMEOUT = "connect overtime";
    private static final String NUMBER_MARK_INFO_NO_ATTRIBUTE = "";
    private static final int PROP_TAG = 1;
    private static final int PROP_TAG_YELLOW = 3;
    private static final int PROP_YELLOW = 2;
    private static final String QUERY_TYPE_CLOUD = "1";
    private static final String QUERY_TYPE_LOCAL = "0";
    private static final String TAG = "TencentApiManager";
    private static final int TAG_TYPE_CRANK = 50;
    private static final int TAG_TYPE_EXPRESS = 55;
    private static final int TAG_TYPE_FRAUD = 54;
    private static final int TAG_TYPE_HOUSE_AGENT = 51;
    private static final int TAG_TYPE_PROMOTE_SALES = 53;
    private static final int TAG_TYPE_TAXI = 56;
    private static final boolean TENCENT_DEFAULT_CLOUD_MARK = true;
    private static final String TIME_OUT_LIMIT = "2000";
    private static final String URI_TENCENT = "content://com.huawei.systemmanager.BlockCheckProvider/numbermark";
    private static volatile TencentApiManager mInfoManager;
    private Context mContext;

    private TencentApiManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static TencentApiManager getInstance(Context context) {
        if (mInfoManager == null) {
            mInfoManager = new TencentApiManager(context);
        }
        return mInfoManager;
    }

    public NumberMarkInfo cloudFetchNumberInfo(String num, String callType) {
        Uri queryUri = Uri.withAppendedPath(Uri.parse(URI_TENCENT), num);
        String type = CALL_TYPE_COMMON;
        if (CALL_TYPE_CALLED.equals(callType)) {
            type = CALL_TYPE_CALLED;
        } else if (CALL_TYPE_CALLING.equals(callType)) {
            type = CALL_TYPE_CALLING;
        }
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(queryUri, new String[]{QUERY_TYPE_CLOUD, type, TIME_OUT_LIMIT}, null, null, null);
            NumberMarkInfo numberMarkInfo;
            if (cursor == null) {
                if (HwLog.HWFLOW) {
                    HwLog.d(TAG, "tencent connect timeout");
                }
                numberMarkInfo = new NumberMarkInfo("connect overtime");
                if (cursor == null) {
                    return numberMarkInfo;
                }
                cursor.close();
                return numberMarkInfo;
            }
            numberMarkInfo = revertCursorToNumberMarkInfo(cursor, num);
            if (cursor == null) {
                return numberMarkInfo;
            }
            cursor.close();
            return numberMarkInfo;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public NumberMarkInfo localFetchNumberInfo(String num) {
        Uri queryUri = Uri.withAppendedPath(Uri.parse(URI_TENCENT), num);
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(queryUri, new String[]{QUERY_TYPE_LOCAL}, null, null, null);
            if (cursor == null && HwLog.HWFLOW) {
                HwLog.i(TAG, "tencent preset db no this number info");
            }
            NumberMarkInfo revertCursorToNumberMarkInfo = revertCursorToNumberMarkInfo(cursor, num);
            if (cursor == null) {
                return revertCursorToNumberMarkInfo;
            }
            cursor.close();
            return revertCursorToNumberMarkInfo;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private NumberMarkInfo revertCursorToNumberMarkInfo(Cursor cursor, String num) {
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        int property = cursor.getInt(cursor.getColumnIndex("property"));
        int tagCount = cursor.getInt(cursor.getColumnIndex("tagCount"));
        String tagName = NUMBER_MARK_INFO_NO_ATTRIBUTE;
        int tagType = cursor.getInt(cursor.getColumnIndex("tagType"));
        String name = NUMBER_MARK_INFO_NO_ATTRIBUTE;
        switch (property) {
            case 1:
                name = cursor.getString(cursor.getColumnIndex("tagName"));
                switch (tagType) {
                    case TAG_TYPE_CRANK /*50*/:
                        tagName = "crank";
                        break;
                    case TAG_TYPE_HOUSE_AGENT /*51*/:
                        tagName = "house agent";
                        break;
                    case TAG_TYPE_PROMOTE_SALES /*53*/:
                        tagName = "promote sales";
                        break;
                    case TAG_TYPE_FRAUD /*54*/:
                        tagName = "fraud";
                        break;
                    case TAG_TYPE_EXPRESS /*55*/:
                        tagName = "express";
                        break;
                    case TAG_TYPE_TAXI /*56*/:
                        tagName = "taxi";
                        break;
                    default:
                        return null;
                }
                return new NumberMarkInfo(num, NUMBER_MARK_INFO_NO_ATTRIBUTE, name, tagName, TENCENT_DEFAULT_CLOUD_MARK, tagCount, "tencent");
            default:
                return null;
        }
    }
}
