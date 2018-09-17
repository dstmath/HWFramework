package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings.Global;
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
    private String TAG = TAG_STATIC;
    private int VP_Flag = 0;
    private AgpsAppObserver mAgpsAppObserver;
    private GsmCdmaPhone mPhone;
    private EventReceiver mReceiver;
    private Method mSetVPEvent = null;

    private final class AgpsAppObserver extends ContentObserver {
        public AgpsAppObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int agpsAppStatus = Global.getInt(HwVpApStatusHandler.this.mPhone.getContext().getContentResolver(), "agps_app_started_navigation", -1);
            Log.d(HwVpApStatusHandler.this.TAG, "AgpsAppObserver onChange(),agps App Status: " + agpsAppStatus);
            HwVpApStatusHandler hwVpApStatusHandler;
            if (agpsAppStatus == 0) {
                hwVpApStatusHandler = HwVpApStatusHandler.this;
                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -3;
            } else {
                hwVpApStatusHandler = HwVpApStatusHandler.this;
                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | 2;
            }
            HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, 0));
        }
    }

    public class EventReceiver extends BroadcastReceiver {
        public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
        public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
        public static final int MOBILE_TYPE_NONE = -1;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int old_VP_Flag = HwVpApStatusHandler.this.VP_Flag;
            Log.d(HwVpApStatusHandler.this.TAG, "receiver:" + action);
            ConnectivityManager mConnMgr;
            HwVpApStatusHandler hwVpApStatusHandler;
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                int type = intent.getIntExtra("networkType", -1);
                String extra_info = intent.getStringExtra("extraInfo");
                String -get0 = HwVpApStatusHandler.this.TAG;
                StringBuilder append = new StringBuilder().append("extra_info:");
                if (extra_info == null) {
                    extra_info = "none";
                }
                Log.i(-get0, append.append(extra_info).toString());
                NetworkInfo netinfo = mConnMgr.getNetworkInfo(type);
                String ni_info = "unknown connection ";
                if (netinfo != null) {
                    State istate = netinfo.getState();
                    Log.i(HwVpApStatusHandler.this.TAG, "net-state:" + istate + ",type=" + type);
                    switch (type) {
                        case 0:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -2;
                                ni_info = "-MOBILE disconnected";
                                break;
                            }
                            ni_info = "+MOBILE connected";
                            break;
                        case 2:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -5;
                                ni_info = "-MOBILE_MMS, MMS disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | 4;
                            ni_info = "+MOBILE_MMS, MMS connected!";
                            break;
                        case 4:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -17;
                                ni_info = "-MOBILE_DUN,USB_tether disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | 16;
                            ni_info = "+MOBILE_DUN,USB_tether connected";
                            break;
                        case 5:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -17;
                                ni_info = "-MOBILE_HIPRI,USB_tether disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | 16;
                            ni_info = "+MOBILE_HIPRI,USB_tether connected";
                            break;
                        default:
                            ni_info = ni_info + ",type:" + type + (istate == State.CONNECTED ? ",connected" : ",disconnected");
                            break;
                    }
                }
                Log.e(HwVpApStatusHandler.this.TAG, "action:" + ni_info);
            } else if (ACTION_TETHER_STATE_CHANGED.equals(action)) {
                int i;
                boolean usbTethered = false;
                boolean wifiTethered = false;
                boolean btTethered = false;
                ArrayList<String> active = intent.getStringArrayListExtra(EXTRA_ACTIVE_TETHER);
                if (active != null) {
                    int length = active.size();
                    if (length > 0) {
                        int i2;
                        String s;
                        String[] tethered = (String[]) active.toArray(new String[length]);
                        mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                        String[] mUsbRegexs = mConnMgr.getTetherableUsbRegexs();
                        String[] mWifiRegexs = mConnMgr.getTetherableWifiRegexs();
                        String[] mBtRegexs = mConnMgr.getTetherableBluetoothRegexs();
                        i = 0;
                        int length2 = tethered.length;
                        while (true) {
                            i2 = i;
                            if (i2 >= length2) {
                                break;
                            }
                            s = tethered[i2];
                            for (String regex : mUsbRegexs) {
                                if (s.matches(regex)) {
                                    usbTethered = true;
                                    break;
                                }
                            }
                            i = i2 + 1;
                        }
                        i = 0;
                        length2 = tethered.length;
                        while (true) {
                            i2 = i;
                            if (i2 >= length2) {
                                break;
                            }
                            s = tethered[i2];
                            for (String regex2 : mWifiRegexs) {
                                if (s.matches(regex2)) {
                                    wifiTethered = true;
                                    break;
                                }
                            }
                            i = i2 + 1;
                        }
                        i = 0;
                        length2 = tethered.length;
                        while (true) {
                            i2 = i;
                            if (i2 >= length2) {
                                break;
                            }
                            s = tethered[i2];
                            for (String regex22 : mBtRegexs) {
                                if (s.matches(regex22)) {
                                    btTethered = true;
                                    break;
                                }
                            }
                            i = i2 + 1;
                        }
                    }
                }
                HwVpApStatusHandler.this.VP_Flag = usbTethered ? HwVpApStatusHandler.this.VP_Flag | 16 : HwVpApStatusHandler.this.VP_Flag & -17;
                HwVpApStatusHandler.this.VP_Flag = wifiTethered ? HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_WIFI_TETHER : HwVpApStatusHandler.this.VP_Flag & -33;
                HwVpApStatusHandler hwVpApStatusHandler2 = HwVpApStatusHandler.this;
                if (btTethered) {
                    i = HwVpApStatusHandler.this.VP_Flag | 64;
                } else {
                    i = HwVpApStatusHandler.this.VP_Flag & -65;
                }
                hwVpApStatusHandler2.VP_Flag = i;
                String terher_info = "Tethering";
                if (usbTethered || wifiTethered || btTethered) {
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
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                hwVpApStatusHandler = HwVpApStatusHandler.this;
                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_SCREEN_ON;
                Log.e(HwVpApStatusHandler.this.TAG, "Screen on!");
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                hwVpApStatusHandler = HwVpApStatusHandler.this;
                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -129;
                Log.e(HwVpApStatusHandler.this.TAG, "Screen off!");
            }
            if (HwVpApStatusHandler.this.VP_Flag != old_VP_Flag) {
                HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(12, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, 0));
            }
        }
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
            this.mPhone.getContext().getContentResolver().registerContentObserver(Global.getUriFor("agps_app_started_navigation"), false, this.mAgpsAppObserver);
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
        Log.i(this.TAG, "handleMessage = " + msg.what);
        switch (msg.what) {
            case 12:
                int vp_mask = msg.arg1;
                Log.i(this.TAG, "VP-bitMask=" + vp_mask);
                setVpMask(vp_mask);
                return;
            case 13:
                Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE");
                AsyncResult ar = msg.obj;
                if (ar == null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: null pointer ");
                    return;
                } else if (ar.exception != null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: failed " + ar.exception);
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
                Log.i(this.TAG, "call com.huawei.android.hwnv.HWNVFuncation.setVPEvent()  return:" + ((Integer) this.mSetVPEvent.invoke(null, new Object[]{Integer.valueOf(vp_mask)})).intValue());
                return;
            } catch (IllegalArgumentException e) {
                Log.e(this.TAG, ": setVpMaskByNvFunction IllegalArgumentException is " + e);
                return;
            } catch (IllegalAccessException e2) {
                Log.e(this.TAG, ": setVpMaskByNvFunction IllegalAccessException is " + e2);
                return;
            } catch (InvocationTargetException e3) {
                Log.e(this.TAG, ": setVpMaskByNvFunction InvocationTargetException is " + e3);
                return;
            }
        }
        Log.e(this.TAG, "com.huawei.android.hwnv.HWNVFuncation.setVPEvent() not found");
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
            Log.e(this.TAG, ": initVPFunc NoSuchMethodException is " + e);
        }
    }
}
