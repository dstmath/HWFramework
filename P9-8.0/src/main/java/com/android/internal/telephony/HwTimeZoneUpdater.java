package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Display;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.ArrayList;
import java.util.TimeZone;

public class HwTimeZoneUpdater {
    private static final int EVENT_AIRPLANE_MODE_CHANGED = 5;
    private static final int EVENT_AUTO_TIME_ZONE_CHANGED = 1;
    private static final int EVENT_MCC_CHANGED = 6;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final int EVENT_SCREEN_ON = 4;
    private static final long NOT_SET = -1;
    private static final String TAG = "HwTimeZoneUpdater";
    private static final boolean USE_LOCATION_TIME_ZONE = SystemProperties.getBoolean("ro.config.location_time_zone", true);
    private boolean hasRegisteredScreenon = false;
    private AirplaneModeObserver mAirplaneModeObserver;
    private Context mContext;
    private Display mDisplay;
    private DisplayManager mDisplayManager;
    private Handler mHandler;
    private HwLocationBasedTimeZoneUpdater mHwLocTzUpdater;
    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwTimeZoneUpdater.this.mHandler.obtainMessage(4).sendToTarget();
            }
        }
    };
    private SettingsObserver mSettingsObserver;
    private long pollMessageDelay = HwVSimConstants.WAIT_FOR_NV_CFG_MATCH_TIMEOUT;

    private static class AirplaneModeObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        AirplaneModeObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        void observe(Context context) {
            context.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), false, this);
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            Log.d(HwTimeZoneUpdater.TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case 1:
                    Log.d(HwTimeZoneUpdater.TAG, "auto time zone changed");
                    if (HwTimeZoneUpdater.this.isAutomaticTimeZoneRequested()) {
                        HwTimeZoneUpdater.this.registerForScreenOnIntents();
                        return;
                    } else {
                        HwTimeZoneUpdater.this.unregisterForScreenOnIntents();
                        return;
                    }
                case 2:
                    HwTimeZoneUpdater.this.onPollTimeZone(msg.what);
                    return;
                case 4:
                    if (!hasMessages(2)) {
                        sendMessageDelayed(obtainMessage(2), HwTimeZoneUpdater.this.pollMessageDelay);
                        return;
                    }
                    return;
                case 5:
                    Log.d(HwTimeZoneUpdater.TAG, "air plane mode changed");
                    if (HwTimeZoneUpdater.this.isAirplaneModeOn()) {
                        Log.d(HwTimeZoneUpdater.TAG, "air plane mode on");
                        HwTimeZoneUpdater.this.unregisterForScreenOnIntents();
                        return;
                    }
                    Log.d(HwTimeZoneUpdater.TAG, "air plane mode off");
                    HwTimeZoneUpdater.this.registerForScreenOnIntents();
                    return;
                case 6:
                    HwTimeZoneUpdater.this.registerForScreenOnIntents();
                    return;
                default:
                    Log.d(HwTimeZoneUpdater.TAG, "wrong message");
                    return;
            }
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        SettingsObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        void observe(Context context) {
            context.getContentResolver().registerContentObserver(Global.getUriFor("auto_time_zone"), false, this);
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    public HwTimeZoneUpdater(Context context) {
        this.mContext = context;
        init();
        if (USE_LOCATION_TIME_ZONE) {
            this.mHwLocTzUpdater = HwLocationBasedTimeZoneUpdater.init(context);
            this.mHwLocTzUpdater.start();
        }
        HwDualCardsLocationTimeZoneUpdate.init(context);
    }

    private void init() {
        Log.d(TAG, "init...");
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 1);
        this.mSettingsObserver.observe(this.mContext);
        this.mDisplayManager = new DisplayManager(this.mContext);
        this.mDisplay = this.mDisplayManager.getDisplay(0);
        this.mAirplaneModeObserver = new AirplaneModeObserver(this.mHandler, 5);
        this.mAirplaneModeObserver.observe(this.mContext);
        PhoneFactory.getPhone(0).registerForMccChanged(this.mHandler, 6, null);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            PhoneFactory.getPhone(1).registerForMccChanged(this.mHandler, 6, null);
        }
    }

    private void registerForScreenOnIntents() {
        if (!isAutomaticTimeZoneRequested() || isAirplaneModeOn()) {
            Log.d(TAG, "auto timezone false ,or isAirplaneModeOn, skip registerForScreenOnIntents");
        } else if (TelephonyManager.getDefault().getPhoneType() == 2) {
            Log.d(TAG, "default sub phonetype is cdma, skip registerForScreenOnIntents");
        } else if (hasIccCard()) {
            Log.d(TAG, "has icc card, skip registerForScreenOnIntents");
        } else {
            if (!this.hasRegisteredScreenon) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(this.mScreenOnReceiver, intentFilter);
                this.hasRegisteredScreenon = true;
                Log.d(TAG, "registerForScreenOnIntents");
            }
            if (this.mDisplay.getState() == 2) {
                this.mHandler.obtainMessage(4).sendToTarget();
            }
        }
    }

    private void unregisterForScreenOnIntents() {
        if (this.hasRegisteredScreenon) {
            this.hasRegisteredScreenon = false;
            this.mContext.unregisterReceiver(this.mScreenOnReceiver);
            if (this.mHandler.hasMessages(2)) {
                this.mHandler.removeMessages(2);
            }
            Log.d(TAG, "unregisterForScreenOnIntents");
        }
    }

    private boolean hasIccCard() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (TelephonyManager.getDefault().hasIccCard(0) || TelephonyManager.getDefault().hasIccCard(1)) {
                return true;
            }
            return false;
        } else if (TelephonyManager.getDefault().hasIccCard()) {
            return true;
        } else {
            return false;
        }
    }

    private void onPollTimeZone(int event) {
        Log.d(TAG, "onPollTimeZone");
        if (isAutomaticTimeZoneRequested()) {
            String plmn = "";
            Phone mPhone0 = PhoneFactory.getPhone(0);
            boolean isRadio1Off = mPhone0.getServiceState().getState() == 3;
            String newRplmn = mPhone0.getServiceStateTracker().getRplmn();
            boolean isSUB1Invalid = TelephonyManager.getDefault().isMultiSimEnabled() ? isRadio1Off || TelephonyManager.getDefault().getCurrentPhoneType(0) == 2 : false;
            if (isSUB1Invalid) {
                newRplmn = PhoneFactory.getPhone(1).getServiceStateTracker().getRplmn();
            }
            Log.d(TAG, "rplmn : " + newRplmn);
            String mcc = "";
            String iso = "";
            if (hasIccCard()) {
                Log.d(TAG, "hasicccard,use networkoperator as plmn to update timezone. so return. ");
                unregisterForScreenOnIntents();
                return;
            }
            plmn = newRplmn;
            Log.d(TAG, "no icccard,use resident plmn to update timezone. rplmn: " + plmn);
            if (TextUtils.isEmpty(plmn)) {
                Log.d(TAG, "onPollTimeZone:plmn is empty");
                return;
            }
            try {
                mcc = plmn.substring(0, 3);
                iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
            } catch (NumberFormatException e) {
                Log.e(TAG, "updateTimeZoneNoneNitz: countryCodeForMcc error");
            } catch (StringIndexOutOfBoundsException e2) {
                Log.e(TAG, "updateTimeZoneNoneNitz: countryCodeForMcc error");
            }
            if (!TextUtils.isEmpty(iso)) {
                boolean isCurrentZoneCorrect = false;
                ArrayList<TimeZone> isoZones = TimeUtils.getTimeZones(iso);
                TimeZone currentZone = TimeZone.getDefault();
                if (isoZones != null) {
                    int list_size = isoZones.size();
                    for (int i = 0; i < list_size; i++) {
                        if (currentZone.getID().equals(((TimeZone) isoZones.get(i)).getID())) {
                            isCurrentZoneCorrect = true;
                            Log.d(TAG, "current zone is in isozone list, basically zone is right.");
                            break;
                        }
                    }
                }
                if (isCurrentZoneCorrect) {
                    Log.d(TAG, "current zone is right,no need to update zone.");
                    unregisterForScreenOnIntents();
                    return;
                }
                fixUniqueZones(TimeUtils.getTimeZonesWithUniqueOffsets(iso), "460".equals(mcc));
                unregisterForScreenOnIntents();
            }
            return;
        }
        Log.d(TAG, "user disable auto timezone ,so return");
    }

    private void fixUniqueZones(ArrayList<TimeZone> uniqueZones, boolean isChinaMCC) {
        TimeZone zone = null;
        if (uniqueZones.size() != 1 && !isChinaMCC) {
            Log.d(TAG, "uniqueZones more than one.");
        } else if (isChinaMCC) {
            zone = TimeZone.getTimeZone("Asia/Shanghai");
        } else {
            zone = (TimeZone) uniqueZones.get(0);
        }
        if (zone != null) {
            String zoneId = zone.getID();
            Log.d(TAG, "zoneId:" + zoneId);
            setTimeZone(zoneId);
        }
    }

    private void setTimeZone(String zoneId) {
        Log.d(TAG, "setTimeZone: setTimeZone=" + zoneId);
        ((AlarmManager) this.mContext.getSystemService("alarm")).setTimeZone(zoneId);
    }

    private boolean isAutomaticTimeZoneRequested() {
        if (Global.getInt(this.mContext.getContentResolver(), "auto_time_zone", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean isAirplaneModeOn() {
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            return true;
        }
        return false;
    }
}
