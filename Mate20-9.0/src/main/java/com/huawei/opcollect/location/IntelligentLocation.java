package com.huawei.opcollect.location;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.huawei.opcollect.collector.servicecollection.LocationRecordAction;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

public class IntelligentLocation {
    private static final String COLUMN_NAME_CITY = "city";
    private static final String COLUMN_NAME_LAT = "lat";
    private static final String COLUMN_NAME_LNG = "lng";
    private static final String COLUMN_NAME_TIME = "time";
    private static final String INVALID_CITY = "city unknown";
    private static final int MESSAGE_ON_CHANGE = 1;
    private static final Uri POSITION_URI = Uri.parse("content://com.huawei.provider.PlaceRecognition/position");
    public static final String PROVIDE_INTELLEIGENT = "intelligent";
    private static final String TAG = "IntelligentLocation";
    private static IntelligentLocation sInstance = null;
    private Context mContext = null;
    private boolean mEnable = false;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private ContentObserver mObserver = null;

    private static class IntelligentHandler extends Handler {
        private final WeakReference<IntelligentLocation> service;

        IntelligentHandler(IntelligentLocation service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OPCollectLog.r(IntelligentLocation.TAG, "handleMessage ");
            if (msg.what == 1) {
                IntelligentLocation intelligentLocation = (IntelligentLocation) this.service.get();
                if (intelligentLocation != null) {
                    intelligentLocation.collectIntelligentData(Uri.parse((String) msg.obj));
                }
            }
        }
    }

    private static final class MyContentObserver extends ContentObserver {
        private final WeakReference<IntelligentLocation> service;

        MyContentObserver(Handler handler, IntelligentLocation service2) {
            super(handler);
            this.service = new WeakReference<>(service2);
        }

        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        public void onChange(boolean selfChange, Uri uri) {
            IntelligentLocation action = (IntelligentLocation) this.service.get();
            if (action == null) {
                OPCollectLog.e(IntelligentLocation.TAG, "action is null");
            } else if (uri == null) {
                OPCollectLog.e(IntelligentLocation.TAG, "uri is null");
            } else if (!IntelligentLocation.isLegalUri(uri.toString())) {
                OPCollectLog.r(IntelligentLocation.TAG, "uri illegal");
            } else {
                OPCollectLog.r(IntelligentLocation.TAG, "onChange.");
                if (action.mHandler != null) {
                    action.mHandler.obtainMessage(1, uri.toString()).sendToTarget();
                }
            }
        }
    }

    public static synchronized IntelligentLocation getInstance(Context context) {
        IntelligentLocation intelligentLocation;
        synchronized (IntelligentLocation.class) {
            if (sInstance == null) {
                sInstance = new IntelligentLocation(context);
            }
            intelligentLocation = sInstance;
        }
        return intelligentLocation;
    }

    private IntelligentLocation(Context context) {
        OPCollectLog.r(TAG, TAG);
        this.mContext = context;
    }

    public boolean destroy() {
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (IntelligentLocation.class) {
            sInstance = null;
        }
    }

    public void enable() {
        OPCollectLog.r(TAG, "enable position mEnable: " + this.mEnable);
        if (!this.mEnable && this.mObserver == null && this.mContext != null) {
            if (this.mHandler == null) {
                this.mHandler = new IntelligentHandler(this);
            }
            OPCollectLog.r(TAG, "enable position");
            this.mEnable = true;
            this.mObserver = new MyContentObserver(this.mHandler, this);
            try {
                this.mContext.getContentResolver().registerContentObserver(POSITION_URI, true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    public void disable() {
        OPCollectLog.r(TAG, "disable position mEnable: " + this.mEnable);
        if (this.mObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
            this.mHandler = null;
            this.mEnable = false;
        }
    }

    private static boolean isNullOrEmptyCursor(Cursor cursor) {
        if (cursor == null) {
            OPCollectLog.e(TAG, "cursor is null");
            return true;
        } else if (cursor.getCount() > 0) {
            return false;
        } else {
            OPCollectLog.e(TAG, "cursor size <= 0");
            cursor.close();
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void collectIntelligentData(Uri uri) {
        if (uri == null) {
            OPCollectLog.e(TAG, "uri is null");
        } else if (this.mContext == null) {
            OPCollectLog.e(TAG, "context is null");
        } else {
            OPCollectLog.r(TAG, "collectIntelligentData.");
            Cursor cursor = null;
            try {
                cursor = this.mContext.getContentResolver().query(uri, new String[]{COLUMN_NAME_TIME, COLUMN_NAME_LNG, COLUMN_NAME_LAT, COLUMN_NAME_CITY}, null, null, null);
            } catch (Exception e) {
                OPCollectLog.e(TAG, e.getMessage());
            }
            if (!isNullOrEmptyCursor(cursor)) {
                while (cursor.moveToNext()) {
                    long time = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIME));
                    Double lng = Double.valueOf(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LNG)));
                    Double lat = Double.valueOf(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LAT)));
                    String city = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CITY));
                    OPCollectLog.r(TAG, "time: " + time);
                    if (INVALID_CITY.equalsIgnoreCase(city)) {
                        OPCollectLog.e(TAG, INVALID_CITY);
                    } else {
                        HwLocation location = new HwLocation();
                        location.setTimestamp(time);
                        location.setLatitude(lat.doubleValue());
                        location.setLongitude(lng.doubleValue());
                        location.setCity(city);
                        location.setProvider(PROVIDE_INTELLEIGENT);
                        LocationRecordAction locationRecordAction = LocationRecordAction.getInstance(this.mContext);
                        synchronized (locationRecordAction.getLock()) {
                            Handler locationHandler = locationRecordAction.getLocationHandler();
                            if (locationHandler != null) {
                                locationHandler.obtainMessage(1, location).sendToTarget();
                            }
                        }
                    }
                }
                cursor.close();
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isLegalUri(String str) {
        return Pattern.compile("content://com.huawei.provider.PlaceRecognition/position/[0-9]+").matcher(str).matches();
    }

    public void dump(int indentNum, PrintWriter pw) {
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mObserver == null) {
                pw.println(indent + "observer is null");
            } else {
                pw.println(indent + "observer not null");
            }
        }
    }
}
