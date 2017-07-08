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
import com.android.internal.telephony.HwQualcommRIL;
import com.android.internal.telephony.vsim.HwVSimEventReport;
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
    private static final boolean mIsSurpportNvFunc = false;
    private String TAG;
    private int VP_Flag;
    private AgpsAppObserver mAgpsAppObserver;
    private GsmCdmaPhone mPhone;
    private EventReceiver mReceiver;
    private Method mSetVPEvent;

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
                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_AGPS;
            }
            HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(HwVpApStatusHandler.MSG_AP_STATUS_CHANGED, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, HwVpApStatusHandler.AGPS_APP_STOPPED));
        }
    }

    public class EventReceiver extends BroadcastReceiver {
        public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
        public static final String EXTRA_ACTIVE_TETHER = "activeArray";
        public static final int MOBILE_TYPE_NONE = -1;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int old_VP_Flag = HwVpApStatusHandler.this.VP_Flag;
            Log.d(HwVpApStatusHandler.this.TAG, "receiver:" + action);
            ConnectivityManager mConnMgr;
            HwVpApStatusHandler hwVpApStatusHandler;
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                int type = intent.getIntExtra("networkType", MOBILE_TYPE_NONE);
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
                        case HwVpApStatusHandler.AGPS_APP_STOPPED /*0*/:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -2;
                                ni_info = "-MOBILE disconnected";
                                break;
                            }
                            ni_info = "+MOBILE connected";
                            break;
                        case HwVpApStatusHandler.VP_AGPS /*2*/:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -5;
                                ni_info = "-MOBILE_MMS, MMS disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_MMS;
                            ni_info = "+MOBILE_MMS, MMS connected!";
                            break;
                        case HwVpApStatusHandler.VP_MMS /*4*/:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -17;
                                ni_info = "-MOBILE_DUN,USB_tether disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_USB_TETHER;
                            ni_info = "+MOBILE_DUN,USB_tether connected";
                            break;
                        case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                            if (istate != State.CONNECTED) {
                                hwVpApStatusHandler = HwVpApStatusHandler.this;
                                hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -17;
                                ni_info = "-MOBILE_HIPRI,USB_tether disconnected";
                                break;
                            }
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_USB_TETHER;
                            ni_info = "+MOBILE_HIPRI,USB_tether connected";
                            break;
                        default:
                            ni_info = ni_info + ",type:" + type + (istate == State.CONNECTED ? ",connected" : ",disconnected");
                            break;
                    }
                }
                Log.e(HwVpApStatusHandler.this.TAG, "action:" + ni_info);
            } else {
                if (ACTION_TETHER_STATE_CHANGED.equals(action)) {
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
                            int length2;
                            String[] tethered = (String[]) active.toArray(new String[length]);
                            mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                            String[] mUsbRegexs = mConnMgr.getTetherableUsbRegexs();
                            String[] mWifiRegexs = mConnMgr.getTetherableWifiRegexs();
                            String[] mBtRegexs = mConnMgr.getTetherableBluetoothRegexs();
                            int length3 = tethered.length;
                            for (i2 = HwVpApStatusHandler.AGPS_APP_STOPPED; i2 < length3; i2 += HwVpApStatusHandler.VP_MOBILE) {
                                s = tethered[i2];
                                length2 = mUsbRegexs.length;
                                for (i = HwVpApStatusHandler.AGPS_APP_STOPPED; i < length2; i += HwVpApStatusHandler.VP_MOBILE) {
                                    if (s.matches(mUsbRegexs[i])) {
                                        usbTethered = HwVpApStatusHandler.DBG;
                                        break;
                                    }
                                }
                            }
                            length3 = tethered.length;
                            for (i2 = HwVpApStatusHandler.AGPS_APP_STOPPED; i2 < length3; i2 += HwVpApStatusHandler.VP_MOBILE) {
                                s = tethered[i2];
                                length2 = mWifiRegexs.length;
                                for (i = HwVpApStatusHandler.AGPS_APP_STOPPED; i < length2; i += HwVpApStatusHandler.VP_MOBILE) {
                                    if (s.matches(mWifiRegexs[i])) {
                                        wifiTethered = HwVpApStatusHandler.DBG;
                                        break;
                                    }
                                }
                            }
                            length3 = tethered.length;
                            for (i2 = HwVpApStatusHandler.AGPS_APP_STOPPED; i2 < length3; i2 += HwVpApStatusHandler.VP_MOBILE) {
                                s = tethered[i2];
                                length2 = mBtRegexs.length;
                                for (i = HwVpApStatusHandler.AGPS_APP_STOPPED; i < length2; i += HwVpApStatusHandler.VP_MOBILE) {
                                    if (s.matches(mBtRegexs[i])) {
                                        btTethered = HwVpApStatusHandler.DBG;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    HwVpApStatusHandler.this.VP_Flag = usbTethered ? HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_USB_TETHER : HwVpApStatusHandler.this.VP_Flag & -17;
                    HwVpApStatusHandler.this.VP_Flag = wifiTethered ? HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_WIFI_TETHER : HwVpApStatusHandler.this.VP_Flag & -33;
                    HwVpApStatusHandler hwVpApStatusHandler2 = HwVpApStatusHandler.this;
                    if (btTethered) {
                        i = HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_BT_TETHER;
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
                } else {
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        hwVpApStatusHandler = HwVpApStatusHandler.this;
                        hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag | HwVpApStatusHandler.VP_SCREEN_ON;
                        Log.e(HwVpApStatusHandler.this.TAG, "Screen on!");
                    } else {
                        if ("android.intent.action.SCREEN_OFF".equals(action)) {
                            hwVpApStatusHandler = HwVpApStatusHandler.this;
                            hwVpApStatusHandler.VP_Flag = hwVpApStatusHandler.VP_Flag & -129;
                            Log.e(HwVpApStatusHandler.this.TAG, "Screen off!");
                        }
                    }
                }
            }
            if (HwVpApStatusHandler.this.VP_Flag != old_VP_Flag) {
                HwVpApStatusHandler.this.sendMessage(HwVpApStatusHandler.this.obtainMessage(HwVpApStatusHandler.MSG_AP_STATUS_CHANGED, HwVpApStatusHandler.this.VP_Flag | HwVpApStatusHandler.VP_ENABLE, HwVpApStatusHandler.AGPS_APP_STOPPED));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwVpApStatusHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwVpApStatusHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwVpApStatusHandler.<clinit>():void");
    }

    protected HwVpApStatusHandler(GsmCdmaPhone phone) {
        this.TAG = TAG_STATIC;
        this.mSetVPEvent = null;
        this.VP_Flag = AGPS_APP_STOPPED;
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
            sendMessage(obtainMessage(MSG_AP_STATUS_CHANGED, this.VP_Flag, AGPS_APP_STOPPED));
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
            case MSG_AP_STATUS_CHANGED /*12*/:
                int vp_mask = msg.arg1;
                Log.i(this.TAG, "VP-bitMask=" + vp_mask);
                setVpMask(vp_mask);
            case MSG_AP_STATUS_SEND_DONE /*13*/:
                Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE");
                AsyncResult ar = msg.obj;
                if (ar == null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: null pointer ");
                } else if (ar.exception != null) {
                    Log.i(this.TAG, "MSG_AP_STATUS_SEND_DONE: failed " + ar.exception);
                }
            default:
        }
    }

    private void setVpMask(int vp_mask) {
        if (mIsSurpportNvFunc) {
            setVpMaskByNvFunction(vp_mask);
        } else {
            this.mPhone.mCi.setVpMask(vp_mask, obtainMessage(MSG_AP_STATUS_SEND_DONE));
        }
    }

    private void setVpMaskByNvFunction(int vp_mask) {
        if (this.mSetVPEvent == null) {
            initVPFunc();
        }
        if (this.mSetVPEvent != null) {
            try {
                Method method = this.mSetVPEvent;
                Object[] objArr = new Object[VP_MOBILE];
                objArr[AGPS_APP_STOPPED] = Integer.valueOf(vp_mask);
                Log.i(this.TAG, "call com.huawei.android.hwnv.HWNVFuncation.setVPEvent()  return:" + ((Integer) method.invoke(null, objArr)).intValue());
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
                Class[] clsArr = new Class[VP_MOBILE];
                clsArr[AGPS_APP_STOPPED] = Integer.TYPE;
                this.mSetVPEvent = classType.getMethod("setVPEvent", clsArr);
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
