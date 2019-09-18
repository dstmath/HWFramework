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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class HwVpApStatusHandler extends Handler {
    private static final int AGPS_APP_STOPPED = 0;
    private static final boolean DBG = true;
    private static final String HWNV_CLASS = "com.huawei.android.hwnv.HWNVFuncation";
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
    private static final boolean mIsSurpportNvFunc = HwModemCapability.isCapabilitySupport(13);
    /* access modifiers changed from: private */
    public String TAG = TAG_STATIC;
    /* access modifiers changed from: private */
    public int VP_Flag = 0;
    private AgpsAppObserver mAgpsAppObserver;
    /* access modifiers changed from: private */
    public GsmCdmaPhone mPhone;
    private EventReceiver mReceiver;
    private Method mSetVPEvent = null;

    private final class AgpsAppObserver extends ContentObserver {
        public AgpsAppObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int agpsAppStatus = Settings.Global.getInt(HwVpApStatusHandler.this.mPhone.getContext().getContentResolver(), "agps_app_started_navigation", -1);
            String access$100 = HwVpApStatusHandler.this.TAG;
            Log.d(access$100, "AgpsAppObserver onChange(),agps App Status: " + agpsAppStatus);
            if (agpsAppStatus == 0) {
                HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -3);
            } else {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 2);
            }
            HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, 0));
        }
    }

    public class EventReceiver extends BroadcastReceiver {
        public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
        public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
        public static final int MOBILE_TYPE_NONE = -1;

        public EventReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:71:0x01d5  */
        /* JADX WARNING: Removed duplicated region for block: B:72:0x01df  */
        /* JADX WARNING: Removed duplicated region for block: B:75:0x01ef  */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x01f8  */
        /* JADX WARNING: Removed duplicated region for block: B:79:0x0207  */
        /* JADX WARNING: Removed duplicated region for block: B:80:0x0210  */
        /* JADX WARNING: Removed duplicated region for block: B:87:0x0238  */
        /* JADX WARNING: Removed duplicated region for block: B:89:0x024b  */
        /* JADX WARNING: Removed duplicated region for block: B:91:0x025e  */
        public void onReceive(Context context, Intent intent) {
            boolean usbTethered;
            Context context2 = context;
            Intent intent2 = intent;
            String action = intent.getAction();
            int old_VP_Flag = HwVpApStatusHandler.this.VP_Flag;
            Log.d(HwVpApStatusHandler.this.TAG, "receiver:" + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                ConnectivityManager mConnMgr = (ConnectivityManager) context2.getSystemService("connectivity");
                int type = intent2.getIntExtra("networkType", -1);
                String extra_info = intent2.getStringExtra("extraInfo");
                String access$100 = HwVpApStatusHandler.this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("extra_info:");
                sb.append(extra_info != null ? extra_info : "none");
                Log.i(access$100, sb.toString());
                NetworkInfo netinfo = mConnMgr.getNetworkInfo(type);
                String ni_info = "unknown connection ";
                if (netinfo != null) {
                    NetworkInfo.State istate = netinfo.getState();
                    Log.i(HwVpApStatusHandler.this.TAG, "net-state:" + istate + ",type=" + type);
                    if (type != 0) {
                        if (type != 2) {
                            switch (type) {
                                case 4:
                                    if (istate != NetworkInfo.State.CONNECTED) {
                                        HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -17);
                                        ni_info = "-MOBILE_DUN,USB_tether disconnected";
                                        break;
                                    } else {
                                        HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 16);
                                        ni_info = "+MOBILE_DUN,USB_tether connected";
                                        break;
                                    }
                                case 5:
                                    if (istate != NetworkInfo.State.CONNECTED) {
                                        HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -17);
                                        ni_info = "-MOBILE_HIPRI,USB_tether disconnected";
                                        break;
                                    } else {
                                        HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 16);
                                        ni_info = "+MOBILE_HIPRI,USB_tether connected";
                                        break;
                                    }
                                default:
                                    String stat_inf = istate == NetworkInfo.State.CONNECTED ? ",connected" : ",disconnected";
                                    ni_info = ni_info + ",type:" + type + stat_inf;
                                    break;
                            }
                        } else if (istate == NetworkInfo.State.CONNECTED) {
                            HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, 4);
                            ni_info = "+MOBILE_MMS, MMS connected!";
                        } else {
                            HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -5);
                            ni_info = "-MOBILE_MMS, MMS disconnected";
                        }
                    } else if (istate == NetworkInfo.State.CONNECTED) {
                        ni_info = "+MOBILE connected";
                    } else {
                        HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -2);
                        ni_info = "-MOBILE disconnected";
                    }
                }
                Log.e(HwVpApStatusHandler.this.TAG, "action:" + ni_info);
            } else if (ACTION_TETHER_STATE_CHANGED.equals(action)) {
                boolean wifiTethered = false;
                boolean btTethered = false;
                ArrayList<String> active = intent2.getStringArrayListExtra(EXTRA_ACTIVE_TETHER);
                if (active != null) {
                    int length = active.size();
                    if (length > 0) {
                        String[] tethered = (String[]) active.toArray(new String[length]);
                        ConnectivityManager mConnMgr2 = (ConnectivityManager) context2.getSystemService("connectivity");
                        String[] mUsbRegexs = mConnMgr2.getTetherableUsbRegexs();
                        String[] mWifiRegexs = mConnMgr2.getTetherableWifiRegexs();
                        String[] mBtRegexs = mConnMgr2.getTetherableBluetoothRegexs();
                        int length2 = tethered.length;
                        usbTethered = false;
                        int i = 0;
                        while (i < length2) {
                            String s = tethered[i];
                            int length3 = mUsbRegexs.length;
                            int i2 = length2;
                            int i3 = 0;
                            while (true) {
                                if (i3 >= length3) {
                                    break;
                                }
                                int i4 = length3;
                                if (s.matches(mUsbRegexs[i3])) {
                                    usbTethered = true;
                                    break;
                                } else {
                                    i3++;
                                    length3 = i4;
                                }
                            }
                            i++;
                            length2 = i2;
                            Context context3 = context;
                            Intent intent3 = intent;
                        }
                        int length4 = tethered.length;
                        int i5 = 0;
                        while (i5 < length4) {
                            String s2 = tethered[i5];
                            int length5 = mWifiRegexs.length;
                            int i6 = length4;
                            int i7 = 0;
                            while (true) {
                                if (i7 >= length5) {
                                    break;
                                }
                                int i8 = length5;
                                if (s2.matches(mWifiRegexs[i7])) {
                                    wifiTethered = true;
                                    break;
                                } else {
                                    i7++;
                                    length5 = i8;
                                }
                            }
                            i5++;
                            length4 = i6;
                        }
                        int length6 = tethered.length;
                        int i9 = 0;
                        while (i9 < length6) {
                            String s3 = tethered[i9];
                            int length7 = mBtRegexs.length;
                            int i10 = length6;
                            int i11 = 0;
                            while (true) {
                                if (i11 >= length7) {
                                    break;
                                }
                                int i12 = length7;
                                if (s3.matches(mBtRegexs[i11])) {
                                    btTethered = true;
                                    break;
                                } else {
                                    i11++;
                                    length7 = i12;
                                }
                            }
                            i9++;
                            length6 = i10;
                        }
                        int unused = HwVpApStatusHandler.this.VP_Flag = !usbTethered ? HwVpApStatusHandler.this.VP_Flag | 16 : HwVpApStatusHandler.this.VP_Flag & -17;
                        int unused2 = HwVpApStatusHandler.this.VP_Flag = !wifiTethered ? HwVpApStatusHandler.this.VP_Flag | 32 : HwVpApStatusHandler.this.VP_Flag & -33;
                        int unused3 = HwVpApStatusHandler.this.VP_Flag = !btTethered ? HwVpApStatusHandler.this.VP_Flag | 64 : HwVpApStatusHandler.this.VP_Flag & -65;
                        String terher_info = "Tethering";
                        if (!usbTethered || wifiTethered || btTethered) {
                            if (usbTethered) {
                                terher_info = terher_info + ":USB";
                            }
                            if (wifiTethered) {
                                terher_info = terher_info + ":Wifi";
                            }
                            if (btTethered) {
                                terher_info = terher_info + ":BT";
                            }
                        } else {
                            terher_info = terher_info + ":Nothing";
                        }
                        Log.e(HwVpApStatusHandler.this.TAG, "tether :" + terher_info);
                    }
                }
                usbTethered = false;
                int unused4 = HwVpApStatusHandler.this.VP_Flag = !usbTethered ? HwVpApStatusHandler.this.VP_Flag | 16 : HwVpApStatusHandler.this.VP_Flag & -17;
                int unused5 = HwVpApStatusHandler.this.VP_Flag = !wifiTethered ? HwVpApStatusHandler.this.VP_Flag | 32 : HwVpApStatusHandler.this.VP_Flag & -33;
                int unused6 = HwVpApStatusHandler.this.VP_Flag = !btTethered ? HwVpApStatusHandler.this.VP_Flag | 64 : HwVpApStatusHandler.this.VP_Flag & -65;
                String terher_info2 = "Tethering";
                if (!usbTethered) {
                }
                if (usbTethered) {
                }
                if (wifiTethered) {
                }
                if (btTethered) {
                }
                Log.e(HwVpApStatusHandler.this.TAG, "tether :" + terher_info2);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwVpApStatusHandler.access$076(HwVpApStatusHandler.this, HwVpApStatusHandler.VP_SCREEN_ON);
                Log.e(HwVpApStatusHandler.this.TAG, "Screen on!");
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwVpApStatusHandler.access$072(HwVpApStatusHandler.this, -129);
                Log.e(HwVpApStatusHandler.this.TAG, "Screen off!");
            }
            if (HwVpApStatusHandler.this.VP_Flag != old_VP_Flag) {
                HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, 0));
            }
        }
    }

    static /* synthetic */ int access$072(HwVpApStatusHandler x0, int x1) {
        int i = x0.VP_Flag & x1;
        x0.VP_Flag = i;
        return i;
    }

    static /* synthetic */ int access$076(HwVpApStatusHandler x0, int x1) {
        int i = x0.VP_Flag | x1;
        x0.VP_Flag = i;
        return i;
    }

    protected HwVpApStatusHandler(GsmCdmaPhone phone) {
        if (phone != null) {
            this.TAG += "[SUB" + phone.getPhoneId() + "]";
        }
        Log.i(this.TAG, "HwVpApStatusHandler.constructor!");
        if (phone != null) {
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
            this.VP_Flag = ((PowerManager) this.mPhone.getContext().getSystemService("power")).isScreenOn() ? this.VP_Flag | VP_SCREEN_ON : this.VP_Flag & -129;
            this.VP_Flag |= VP_ENABLE;
            sendMessage(obtainMessage(12, this.VP_Flag, 0));
        }
    }

    public void dispose() {
        Log.i(this.TAG, "HwVpApStatusHandler.dispose!");
        if (this.mPhone != null) {
            this.mPhone.getContext().unregisterReceiver(this.mReceiver);
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mAgpsAppObserver);
        }
    }

    public void handleMessage(Message msg) {
        String str = this.TAG;
        Log.i(str, "handleMessage = " + msg.what);
        switch (msg.what) {
            case 12:
                int vp_mask = msg.arg1;
                String str2 = this.TAG;
                Log.i(str2, "VP-bitMask=" + vp_mask);
                setVpMask(vp_mask);
                return;
            case 13:
                Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: null pointer ");
                    return;
                } else if (ar.exception != null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: failed with exception");
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private void setVpMask(int vp_mask) {
        if (mIsSurpportNvFunc) {
            setVpMaskByNvFunction(vp_mask);
        } else {
            this.mPhone.mCi.setVpMask(vp_mask, obtainMessage(13));
        }
    }

    private void setVpMaskByNvFunction(int vp_mask) {
        if (this.mSetVPEvent == null) {
            initVPFunc();
        }
        if (this.mSetVPEvent != null) {
            try {
                int ret = ((Integer) this.mSetVPEvent.invoke(null, new Object[]{Integer.valueOf(vp_mask)})).intValue();
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
                this.mSetVPEvent = classType.getMethod("setVPEvent", new Class[]{Integer.TYPE});
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
