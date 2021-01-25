package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import java.util.ArrayList;

public class HwVpApStatusHandler extends Handler {
    private static final int AGPS_APP_STOPPED = 0;
    private static final boolean DBG = true;
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
    private PhoneExt mPhone;
    private EventReceiver mReceiver;
    private int mVpFlag = 0;

    protected HwVpApStatusHandler(IHwGsmCdmaPhoneInner phoneInner) {
        RlogEx.i(this.TAG, "HwVpApStatusHandler.constructor!");
        PhoneExt phone = phoneInner.getGsmCdmaPhone();
        if (phone != null) {
            this.TAG += "[SUB" + phone.getPhoneId() + "]";
            this.mPhone = phone;
            this.mReceiver = new EventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction(EventReceiver.ACTION_TETHER_STATE_CHANGED);
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            Context context = this.mPhone.getContext();
            if (context != null) {
                context.registerReceiver(this.mReceiver, filter);
                this.mAgpsAppObserver = new AgpsAppObserver(this);
                this.mPhone.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("agps_app_started_navigation"), false, this.mAgpsAppObserver);
                boolean screenOn = ((PowerManager) this.mPhone.getContext().getSystemService("power")).isScreenOn();
                int i = this.mVpFlag;
                this.mVpFlag = screenOn ? i | VP_SCREEN_ON : i & -129;
                this.mVpFlag |= VP_ENABLE;
            }
            sendMessage(obtainMessage(12, this.mVpFlag, 0));
        }
    }

    public void dispose() {
        RlogEx.i(this.TAG, "HwVpApStatusHandler.dispose!");
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt != null && phoneExt.getContext() != null) {
            this.mPhone.getContext().unregisterReceiver(this.mReceiver);
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mAgpsAppObserver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getUsbTethered(String[] tethered) {
        String[] mUsbRegexs;
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null || (mUsbRegexs = ConnectivityManagerEx.getTetherableUsbRegexs(phoneExt.getContext())) == null || mUsbRegexs.length == 0) {
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
    private boolean getWifiTethered(String[] tethered) {
        String[] mWifiRegexs;
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null || (mWifiRegexs = ConnectivityManagerEx.getTetherableWifiRegexs(phoneExt.getContext())) == null || mWifiRegexs.length == 0) {
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
    private boolean getBtTethered(String[] tethered) {
        String[] mBtRegexs;
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null || (mBtRegexs = ConnectivityManagerEx.getTetherableBluetoothRegexs(phoneExt.getContext())) == null || mBtRegexs.length == 0) {
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

    /* JADX INFO: Multiple debug info for r0v2 int: [D('ar' com.huawei.android.os.AsyncResultEx), D('vpMask' int)] */
    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        String str = this.TAG;
        RlogEx.i(str, "handleMessage = " + msg.what);
        int i = msg.what;
        if (i == 12) {
            int vpMask = msg.arg1;
            String str2 = this.TAG;
            RlogEx.i(str2, "VP-bitMask=" + vpMask);
            setVpMask(vpMask);
        } else if (i == MSG_AP_STATUS_SEND_DONE) {
            RlogEx.i(this.TAG, "MSG_AP_STATUS_SEND_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                RlogEx.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: null pointer ");
            } else if (ar.getException() != null) {
                RlogEx.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: failed with exception");
            }
        }
    }

    private void setVpMask(int vp_mask) {
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (ci != null) {
            ci.setVpMask(vp_mask, obtainMessage(MSG_AP_STATUS_SEND_DONE));
        }
    }

    public class EventReceiver extends BroadcastReceiver {
        public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
        public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
        public static final int MOBILE_TYPE_NONE = -1;

        public EventReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                int oldVpFlag = HwVpApStatusHandler.this.mVpFlag;
                RlogEx.d(HwVpApStatusHandler.this.TAG, "receiver:" + action);
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    handleConnectivityAction(context, intent);
                } else if (ACTION_TETHER_STATE_CHANGED.equals(action)) {
                    handleTetherstateChangedAction(context, intent);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwVpApStatusHandler.this.mVpFlag |= HwVpApStatusHandler.VP_SCREEN_ON;
                    RlogEx.e(HwVpApStatusHandler.this.TAG, "Screen on!");
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwVpApStatusHandler.this.mVpFlag &= -129;
                    RlogEx.e(HwVpApStatusHandler.this.TAG, "Screen off!");
                }
                if (HwVpApStatusHandler.this.mVpFlag != oldVpFlag) {
                    HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.mVpFlag | HwVpApStatusHandler.VP_ENABLE, 0));
                }
            }
        }

        private void handleConnectivityAction(Context context, Intent intent) {
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
            int type = intent.getIntExtra("networkType", -1);
            String extraInfo = intent.getStringExtra("extraInfo");
            String str = HwVpApStatusHandler.this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("extra_info:");
            sb.append(extraInfo != null ? extraInfo : "none");
            RlogEx.i(str, sb.toString());
            NetworkInfo netinfo = mConnMgr.getNetworkInfo(type);
            String niInfo = "unknown connection ";
            if (netinfo != null) {
                NetworkInfo.State istate = netinfo.getState();
                RlogEx.i(HwVpApStatusHandler.this.TAG, "net-state:" + istate + ",type=" + type);
                if (type == 0) {
                    niInfo = getMobileNiInfo(istate);
                } else if (type == 2) {
                    niInfo = getMobileMmsNiInfo(istate);
                } else if (type == 4) {
                    niInfo = getMobileDunNiInfo(istate);
                } else if (type != 5) {
                    niInfo = niInfo + ",type:" + type + (istate == NetworkInfo.State.CONNECTED ? ",connected" : ",disconnected");
                } else {
                    niInfo = getMobileHipriNiInfo(istate);
                }
            }
            RlogEx.e(HwVpApStatusHandler.this.TAG, "action:" + niInfo);
        }

        private String getMobileDunNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.this.mVpFlag |= 16;
                return "+MOBILE_DUN,USB_tether connected";
            }
            HwVpApStatusHandler.this.mVpFlag &= -17;
            return "-MOBILE_DUN,USB_tether disconnected";
        }

        private String getMobileHipriNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.this.mVpFlag |= 16;
                return "+MOBILE_HIPRI,USB_tether connected";
            }
            HwVpApStatusHandler.this.mVpFlag &= -17;
            return "-MOBILE_HIPRI,USB_tether disconnected";
        }

        private String getMobileMmsNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                HwVpApStatusHandler.this.mVpFlag |= 4;
                return "+MOBILE_MMS, MMS connected!";
            }
            HwVpApStatusHandler.this.mVpFlag &= -5;
            return "-MOBILE_MMS, MMS disconnected";
        }

        private String getMobileNiInfo(NetworkInfo.State istate) {
            if (istate == NetworkInfo.State.CONNECTED) {
                return "+MOBILE connected";
            }
            HwVpApStatusHandler.this.mVpFlag &= -2;
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
                usbTethered = HwVpApStatusHandler.this.getUsbTethered(tethered);
                wifiTethered = HwVpApStatusHandler.this.getWifiTethered(tethered);
                btTethered = HwVpApStatusHandler.this.getBtTethered(tethered);
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
            RlogEx.e(HwVpApStatusHandler.this.TAG, "tether :" + terherInfo);
        }
    }

    /* access modifiers changed from: private */
    public final class AgpsAppObserver extends ContentObserver {
        public AgpsAppObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            int agpsAppStatus = -1;
            Context context = HwVpApStatusHandler.this.mPhone.getContext();
            if (context != null) {
                agpsAppStatus = Settings.Global.getInt(context.getContentResolver(), "agps_app_started_navigation", -1);
            }
            RlogEx.d(HwVpApStatusHandler.this.TAG, "AgpsAppObserver onChange(),agps App Status: " + agpsAppStatus);
            if (agpsAppStatus == 0) {
                HwVpApStatusHandler.this.mVpFlag &= -3;
            } else {
                HwVpApStatusHandler.this.mVpFlag |= 2;
            }
            HwVpApStatusHandler hwVpApStatusHandler = HwVpApStatusHandler.this;
            HwVpApStatusHandler.this.sendMessage(hwVpApStatusHandler.obtainMessage(12, hwVpApStatusHandler.mVpFlag | HwVpApStatusHandler.VP_ENABLE, 0));
        }
    }
}
