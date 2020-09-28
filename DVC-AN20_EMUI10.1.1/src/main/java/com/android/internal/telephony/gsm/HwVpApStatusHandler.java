package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwQualcommRIL;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class HwVpApStatusHandler extends Handler {
    private static final int AGPS_APP_STOPPED = 0;
    private static final boolean DBG = true;
    private static final String HWNV_CLASS = "com.huawei.android.hwnv.HWNVFuncation";
    private static final boolean IS_SURPPORT_NV_FUNC = HwModemCapability.isCapabilitySupport((int) MSG_AP_STATUS_SEND_DONE);
    private static final int MSG_AP_STATUS_CHANGED = 12;
    private static final int MSG_AP_STATUS_SEND_DONE = 13;
    private static final String TAG_STATIC = "HwVpApStatusHandler";
    private static final int VP_AGPS = 2;
    private static final int VP_BT_TETHER = 64;
    private static final int VP_ENABLE = 256;
    private static final int VP_MASK = 118;
    private static final int VP_MMS = 4;
    private static final int VP_MOBILE = 1;
    private static final int VP_SCREEN_ON = 128;
    private static final int VP_TETHER_MASK = 112;
    private static final int VP_USB_TETHER = 16;
    private static final int VP_WIFI_TETHER = 32;
    private String TAG = TAG_STATIC;
    private AgpsAppObserver mAgpsAppObserver;
    private GsmCdmaPhone mPhone;
    private EventReceiver mReceiver;
    private Method mSetVPEvent = null;
    private int mVpFlag = 0;

    static /* synthetic */ int access$072(HwVpApStatusHandler x0, int x1) {
        int i = x0.mVpFlag & x1;
        x0.mVpFlag = i;
        return i;
    }

    static /* synthetic */ int access$076(HwVpApStatusHandler x0, int x1) {
        int i = x0.mVpFlag | x1;
        x0.mVpFlag = i;
        return i;
    }

    protected HwVpApStatusHandler(IHwGsmCdmaPhoneInner phoneInner) {
        Log.i(this.TAG, "HwVpApStatusHandler.constructor!");
        GsmCdmaPhone phone = phoneInner.getGsmCdmaPhone();
        if (phone != null) {
            this.TAG += "[SUB" + phone.getPhoneId() + "]";
            this.mPhone = phone;
            this.mReceiver = new EventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction(EventReceiver.ACTION_TETHER_STATE_CHANGED);
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            this.mPhone.getContext().registerReceiver(this.mReceiver, filter);
            this.mAgpsAppObserver = new AgpsAppObserver(this);
            this.mPhone.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("agps_app_started_navigation"), false, this.mAgpsAppObserver);
            boolean screenOn = ((PowerManager) this.mPhone.getContext().getSystemService("power")).isScreenOn();
            int i = this.mVpFlag;
            this.mVpFlag = screenOn ? i | VP_SCREEN_ON : i & -129;
            this.mVpFlag |= VP_ENABLE;
            sendMessage(obtainMessage(12, this.mVpFlag, 0));
        }
    }

    public void dispose() {
        Log.i(this.TAG, "HwVpApStatusHandler.dispose!");
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone != null) {
            gsmCdmaPhone.getContext().unregisterReceiver(this.mReceiver);
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mAgpsAppObserver);
        }
    }

    public class EventReceiver extends BroadcastReceiver {
        public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
        public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
        public static final int MOBILE_TYPE_NONE = -1;

        public EventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int oldVpFlag = HwVpApStatusHandler.this.mVpFlag;
            String str = HwVpApStatusHandler.this.TAG;
            Log.d(str, "receiver:" + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                handleConnectivityAction(context, intent);
            } else if (ACTION_TETHER_STATE_CHANGED.equals(action)) {
                handleTetherstateChangedAction(context, intent);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, HwVpApStatusHandler.VP_SCREEN_ON);
                Log.e(HwVpApStatusHandler.this.TAG, "Screen on!");
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -129);
                Log.e(HwVpApStatusHandler.this.TAG, "Screen off!");
            }
            if (HwVpApStatusHandler.this.mVpFlag != oldVpFlag) {
                HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.mVpFlag | HwVpApStatusHandler.VP_ENABLE, 0));
            }
        }

        private void handleConnectivityAction(Context context, Intent intent) {
            String statInf;
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
            int type = intent.getIntExtra("networkType", -1);
            String extraInfo = intent.getStringExtra("extraInfo");
            String str = HwVpApStatusHandler.this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("extra_info:");
            sb.append(extraInfo != null ? extraInfo : "none");
            Log.i(str, sb.toString());
            NetworkInfo netinfo = mConnMgr.getNetworkInfo(type);
            String niInfo = "unknown connection ";
            if (netinfo != null) {
                NetworkInfo.State istate = netinfo.getState();
                Log.i(HwVpApStatusHandler.this.TAG, "net-state:" + istate + ",type=" + type);
                if (type == 0) {
                    niInfo = getMobileNiInfo(istate);
                } else if (type == 2) {
                    niInfo = getMobileMmsNiInfo(istate);
                } else if (type == 4) {
                    niInfo = getMobileDunNiInfo(istate);
                } else if (type != 5) {
                    if (istate == NetworkInfo.State.CONNECTED) {
                        statInf = ",connected";
                    } else {
                        statInf = ",disconnected";
                    }
                    niInfo = niInfo + ",type:" + type + statInf;
                } else {
                    niInfo = getMobileHipriNiInfo(istate);
                }
            }
            Log.e(HwVpApStatusHandler.this.TAG, "action:" + niInfo);
        }

        private String getMobileDunNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 16);
                return "+MOBILE_DUN,USB_tether connected";
            }
            HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -17);
            return "-MOBILE_DUN,USB_tether disconnected";
        }

        private String getMobileHipriNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 16);
                return "+MOBILE_HIPRI,USB_tether connected";
            }
            HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -17);
            return "-MOBILE_HIPRI,USB_tether disconnected";
        }

        private String getMobileMmsNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 4);
                return "+MOBILE_MMS, MMS connected!";
            }
            HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -5);
            return "-MOBILE_MMS, MMS disconnected";
        }

        private String getMobileNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                return "+MOBILE connected";
            }
            HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -2);
            return "-MOBILE disconnected";
        }

        private void handleTetherstateChangedAction(Context context, Intent intent) {
            int length;
            boolean usbTethered = false;
            boolean wifiTethered = false;
            boolean btTethered = false;
            ArrayList<String> active = intent.getStringArrayListExtra(EXTRA_ACTIVE_TETHER);
            if (active != null && (length = active.size()) > 0) {
                String[] tethered = (String[]) active.toArray(new String[length]);
                ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                usbTethered = HwVpApStatusHandler.this.getUsbTethered(tethered, mConnMgr);
                wifiTethered = HwVpApStatusHandler.this.getWifiTethered(tethered, mConnMgr);
                btTethered = HwVpApStatusHandler.this.getBtTethered(tethered, mConnMgr);
            }
            HwVpApStatusHandler hwVpApStatusHandler = HwVpApStatusHandler.this;
            int i = hwVpApStatusHandler.mVpFlag;
            hwVpApStatusHandler.mVpFlag = usbTethered ? i | 16 : i & -17;
            HwVpApStatusHandler hwVpApStatusHandler2 = HwVpApStatusHandler.this;
            int i2 = hwVpApStatusHandler2.mVpFlag;
            hwVpApStatusHandler2.mVpFlag = wifiTethered ? i2 | 32 : i2 & -33;
            HwVpApStatusHandler hwVpApStatusHandler3 = HwVpApStatusHandler.this;
            int i3 = hwVpApStatusHandler3.mVpFlag;
            hwVpApStatusHandler3.mVpFlag = btTethered ? i3 | 64 : i3 & -65;
            String terherInfo = "Tethering";
            if (usbTethered || wifiTethered || btTethered) {
                if (usbTethered) {
                    terherInfo = terherInfo + ":USB";
                }
                if (wifiTethered) {
                    terherInfo = terherInfo + ":Wifi";
                }
                if (btTethered) {
                    terherInfo = terherInfo + ":BT";
                }
            } else {
                terherInfo = terherInfo + ":Nothing";
            }
            Log.e(HwVpApStatusHandler.this.TAG, "tether :" + terherInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getUsbTethered(String[] tethered, ConnectivityManager mConnMgr) {
        String[] mUsbRegexs = mConnMgr.getTetherableUsbRegexs();
        if (mUsbRegexs == null || mUsbRegexs.length == 0) {
            return false;
        }
        boolean usbTethered = false;
        for (String s : tethered) {
            int length = mUsbRegexs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (s.matches(mUsbRegexs[i])) {
                    usbTethered = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        return usbTethered;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getWifiTethered(String[] tethered, ConnectivityManager mConnMgr) {
        String[] mWifiRegexs = mConnMgr.getTetherableWifiRegexs();
        if (mWifiRegexs == null || mWifiRegexs.length == 0) {
            return false;
        }
        boolean wifiTethered = false;
        for (String s : tethered) {
            int length = mWifiRegexs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (s.matches(mWifiRegexs[i])) {
                    wifiTethered = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        return wifiTethered;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getBtTethered(String[] tethered, ConnectivityManager mConnMgr) {
        String[] mBtRegexs = mConnMgr.getTetherableBluetoothRegexs();
        if (mBtRegexs == null || mBtRegexs.length == 0) {
            return false;
        }
        boolean btTethered = false;
        for (String s : tethered) {
            int length = mBtRegexs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (s.matches(mBtRegexs[i])) {
                    btTethered = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        return btTethered;
    }

    /* access modifiers changed from: private */
    public final class AgpsAppObserver extends ContentObserver {
        public AgpsAppObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int agpsAppStatus = Settings.Global.getInt(HwVpApStatusHandler.this.mPhone.getContext().getContentResolver(), "agps_app_started_navigation", -1);
            String str = HwVpApStatusHandler.this.TAG;
            Log.d(str, "AgpsAppObserver onChange(),agps App Status: " + agpsAppStatus);
            if (agpsAppStatus == 0) {
                HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -3);
            } else {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 2);
            }
            HwVpApStatusHandler hwVpApStatusHandler = HwVpApStatusHandler.this;
            HwVpApStatusHandler.this.sendMessage(hwVpApStatusHandler.obtainMessage(12, hwVpApStatusHandler.mVpFlag | HwVpApStatusHandler.VP_ENABLE, 0));
        }
    }

    /* JADX INFO: Multiple debug info for r0v2 int: [D('ar' android.os.AsyncResult), D('vpMask' int)] */
    public void handleMessage(Message msg) {
        String str = this.TAG;
        Log.i(str, "handleMessage = " + msg.what);
        int i = msg.what;
        if (i == 12) {
            int vpMask = msg.arg1;
            String str2 = this.TAG;
            Log.i(str2, "VP-bitMask=" + vpMask);
            setVpMask(vpMask);
        } else if (i == MSG_AP_STATUS_SEND_DONE) {
            Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: null pointer ");
            } else if (ar.exception != null) {
                Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: failed with exception");
            }
        }
    }

    private void setVpMask(int vp_mask) {
        if (IS_SURPPORT_NV_FUNC) {
            setVpMaskByNvFunction(vp_mask);
        } else {
            this.mPhone.mCi.setVpMask(vp_mask, obtainMessage(MSG_AP_STATUS_SEND_DONE));
        }
    }

    private void setVpMaskByNvFunction(int vp_mask) {
        if (this.mSetVPEvent == null) {
            initVPFunc();
        }
        Method method = this.mSetVPEvent;
        if (method != null) {
            try {
                int ret = ((Integer) method.invoke(null, Integer.valueOf(vp_mask))).intValue();
                String str = this.TAG;
                Log.i(str, "call com.huawei.android.hwnv.HWNVFuncation.setVPEvent()  return:" + ret);
            } catch (IllegalArgumentException e) {
                Log.e(this.TAG, "setVpMaskByNvFunction IllegalArgumentException");
            } catch (IllegalAccessException e2) {
                Log.e(this.TAG, "setVpMaskByNvFunction IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.e(this.TAG, "setVpMaskByNvFunction InvocationTargetException");
            }
        } else {
            Log.e(this.TAG, "com.huawei.android.hwnv.HWNVFuncation.setVPEvent() not found");
        }
    }

    private void initVPFunc() {
        Log.i(this.TAG, "initVPFunc()");
        try {
            Class classType = HwQualcommRIL.getHWNV();
            if (classType != null) {
                Log.i(this.TAG, "found com.huawei.android.hwnv.HWNVFuncation");
                this.mSetVPEvent = classType.getMethod("setVPEvent", Integer.TYPE);
            } else {
                Log.e(this.TAG, "No found com.huawei.android.hwnv.HWNVFuncation");
            }
            if (this.mSetVPEvent != null) {
                Log.i(this.TAG, "found setVPEvent() interface");
            } else {
                Log.e(this.TAG, "No found setVPEvent() interface");
            }
        } catch (NoSuchMethodException e) {
            Log.e(this.TAG, "initVPFunc NoSuchMethodException");
        }
    }
}
