package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class GpsStateAction extends Action {
    private static final String TAG = "GpsStateAction";
    private static GpsStateAction sInstance = null;
    private GpsStateContentObserver mContentObserver = null;
    private Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public boolean mIsGpsOn = false;
    /* access modifiers changed from: private */
    public boolean mIsInitialized = false;
    /* access modifiers changed from: private */
    public LocationManager mLocationManager = null;

    private class GpsStateContentObserver extends ContentObserver {
        GpsStateContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (GpsStateAction.this.mLocationManager == null) {
                OPCollectLog.e("GpsStateAction", "mLocationManager is null!");
                return;
            }
            boolean enabled = false;
            try {
                enabled = GpsStateAction.this.mLocationManager.isProviderEnabled("gps");
            } catch (SecurityException e) {
                OPCollectLog.e("GpsStateAction", e.getMessage());
            }
            if (enabled) {
                if (!GpsStateAction.this.mIsGpsOn || !GpsStateAction.this.mIsInitialized) {
                    boolean unused = GpsStateAction.this.mIsGpsOn = true;
                    boolean unused2 = GpsStateAction.this.mIsInitialized = true;
                    GpsStateAction.this.perform();
                }
            } else if (GpsStateAction.this.mIsGpsOn || !GpsStateAction.this.mIsInitialized) {
                boolean unused3 = GpsStateAction.this.mIsGpsOn = false;
                boolean unused4 = GpsStateAction.this.mIsInitialized = true;
                GpsStateAction.this.perform();
            }
        }
    }

    public static synchronized GpsStateAction getInstance(Context context) {
        GpsStateAction gpsStateAction;
        synchronized (GpsStateAction.class) {
            if (sInstance == null) {
                sInstance = new GpsStateAction(context, "GpsStateAction");
            }
            gpsStateAction = sInstance;
        }
        return gpsStateAction;
    }

    private GpsStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_GPS_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_GPS_OFF));
    }

    public void enable() {
        super.enable();
        if (this.mContentObserver == null && this.mContext != null) {
            this.mContentObserver = new GpsStateContentObserver(this.mHandler);
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), false, this.mContentObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e("GpsStateAction", "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    public void disable() {
        super.disable();
        if (this.mContentObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            this.mContentObserver = null;
            this.mLocationManager = null;
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (GpsStateAction.class) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        String eventType = this.mIsGpsOn ? SysEventUtil.EVENT_GPS_ON : SysEventUtil.EVENT_GPS_OFF;
        OPCollectLog.d("GpsStateAction", eventType);
        SysEventUtil.collectSysEventData(eventType);
        return true;
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mContentObserver == null) {
                pw.println(indent + "receiver is null");
            } else {
                pw.println(indent + "receiver not null");
            }
        }
    }
}
