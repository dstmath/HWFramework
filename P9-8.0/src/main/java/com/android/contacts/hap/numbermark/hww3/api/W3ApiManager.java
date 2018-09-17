package com.android.contacts.hap.numbermark.hww3.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.welink.WeLinkManager;
import com.android.contacts.util.HwLog;

public final class W3ApiManager {
    private static final String CONNECTION_TIMEOUT = "connect overtime";
    private static final String LOG_W3 = "w3";
    private static final String LOG_WELINK = "welink";
    private static final String NUMBER_MARK_INFO_NO_ATTRIBUTE = "";
    private static final int RETURN_CODE_BUSINESS_EXCEPTION = 103;
    private static final int RETURN_CODE_MUTI_RESULT = 101;
    private static final int RETURN_CODE_PARAM_ERROR = 105;
    private static final int RETURN_CODE_SUCCESS = 100;
    private static final int RETURN_CODE_TIME_OUT = 102;
    private static final int RETURN_CODE_W3_LOG_OUT = 104;
    private static final String TAG = "W3ApiManager";
    private static final boolean W3_DEFAULT_CLOUD_MARK = true;
    private static final String W3_DEFAULT_MARK_CLASSIFY = "w3";
    private static final int W3_DEFAULT_MARK_COUNT = -1;
    private static final String W3_QUERY_URI = "content://huawei.w3.contact/query/";
    private static final String W3_TIMEOUT_LIMIT = "&2";
    private static final String WELINK_QUERY_URI = "content://com.huawei.works.contact/query/";
    private static volatile W3ApiManager mInfoManager;
    private Context mContext;

    private W3ApiManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static W3ApiManager getInstance(Context context) {
        if (mInfoManager == null) {
            mInfoManager = new W3ApiManager(context);
        }
        return mInfoManager;
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NumberMarkInfo getMarkInfoFromW3Server(String num) {
        if (TextUtils.isEmpty(num)) {
            return null;
        }
        boolean isSuppotWeLink = WeLinkManager.isSuppotWeLink();
        String logApp = isSuppotWeLink ? LOG_WELINK : "w3";
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse((isSuppotWeLink ? WELINK_QUERY_URI : W3_QUERY_URI) + (num + W3_TIMEOUT_LIMIT)), null, null, null, null);
            if (cursor == null) {
                if (cursor == null) {
                    return null;
                }
                cursor.close();
                return null;
            } else if (cursor.moveToFirst()) {
                NumberMarkInfo numberMarkInfo;
                switch (cursor.getInt(cursor.getColumnIndex("code"))) {
                    case RETURN_CODE_SUCCESS /*100*/:
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String account = cursor.getString(cursor.getColumnIndex("account"));
                        String department = cursor.getString(cursor.getColumnIndex("department"));
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(account)) {
                            numberMarkInfo = new NumberMarkInfo(num, NUMBER_MARK_INFO_NO_ATTRIBUTE, name + " " + account, "w3", W3_DEFAULT_CLOUD_MARK, W3_DEFAULT_MARK_COUNT, "w3", department);
                            if (cursor == null) {
                                return numberMarkInfo;
                            }
                            cursor.close();
                            return numberMarkInfo;
                        } else if (cursor == null) {
                            return null;
                        } else {
                            cursor.close();
                            return null;
                        }
                    case RETURN_CODE_MUTI_RESULT /*101*/:
                        w3log(logApp + " muti result error");
                        break;
                    case RETURN_CODE_TIME_OUT /*102*/:
                        w3log(logApp + " time out error");
                        numberMarkInfo = new NumberMarkInfo("connect overtime");
                        if (cursor == null) {
                            return numberMarkInfo;
                        }
                        cursor.close();
                        return numberMarkInfo;
                    case RETURN_CODE_BUSINESS_EXCEPTION /*103*/:
                        w3log(logApp + " business error");
                        break;
                    case RETURN_CODE_W3_LOG_OUT /*104*/:
                        w3log(logApp + " log out error");
                        break;
                    case RETURN_CODE_PARAM_ERROR /*105*/:
                        w3log(logApp + " param error");
                        break;
                }
                if (cursor == null) {
                    return null;
                }
                cursor.close();
                return null;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void w3log(String msg) {
        HwLog.i(TAG, msg);
    }
}
