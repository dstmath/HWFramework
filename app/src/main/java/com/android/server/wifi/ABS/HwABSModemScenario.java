package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;

public class HwABSModemScenario {
    public static final String ACTION_HW_CRR_CONN_IND = "com.huawei.action.ACTION_HW_CRR_CONN_IND";
    public static final String HUAWEI_SIM_REG_PLMNSELINFO_ACTION = "com.huawei.action.SIM_PLMN_SELINFO";
    private static final int MODEM_EXCEPTION_INTERVAL_TIME = 10000;
    private static final int MODEM_EXCEPTION_NUM = 2;
    private static final int MODEM_EXCEPTION_REPORT_TIME = 2000;
    private static final int MSG_ENTER_CONNECT_STATE = 4;
    private static final int MSG_ENTER_SEARCH_STATE = 1;
    private static final int MSG_EXIT_CONNECT_STATE = 5;
    private static final int MSG_EXIT_SEARCH_STATE = 2;
    private static String MSG_OUTGOING_CALL = null;
    private static final int MSG_REPORT_ENTER_CONNECT_STATE = 6;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE = 3;
    private IntentFilter intentFilter;
    PhoneStateListener listener;
    private BroadcastReceiver mCallBroadcastReceiver;
    private IntentFilter mConnectedintentFilter;
    private Context mContext;
    private long mEnterConnectState;
    private int mEnterConnectStateNum;
    private long mEnterSearchState;
    private int mEnterSearchStateNum;
    private Handler mHandler;
    private BroadcastReceiver mModemBroadcastReceiver;
    private Handler mProcessHandler;
    private TelephonyManager mTelephonyManager;

    private class CallBroadcastReceiver extends BroadcastReceiver {
        private CallBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)) {
                HwABSUtils.logD("MSG_OUTGOING_CALL");
                HwABSModemScenario.this.mHandler.sendEmptyMessage(7);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwABSUtils.logD("ACTION_BOOT_COMPLETED");
                HwABSModemScenario.this.mTelephonyManager.listen(HwABSModemScenario.this.listener, 33);
            }
        }
    }

    private class ModemBroadcastReceiver extends BroadcastReceiver {
        private ModemBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (HwABSModemScenario.HUAWEI_SIM_REG_PLMNSELINFO_ACTION.equals(action)) {
                int subId = intent.getIntExtra(HwABSUtils.SUB_ID, 0);
                int flag = intent.getIntExtra(HwABSUtils.FLAG, 0);
                int result = intent.getIntExtra(HwABSUtils.RES, 0);
                HwABSUtils.logD("HUAWEI_SIM_REG_PLMNSELINFO_ACTION subId = " + subId + " flag =" + flag + " result =" + result);
                Bundle data = new Bundle();
                data.putInt(HwABSUtils.SUB_ID, subId);
                data.putInt(HwABSUtils.FLAG, flag);
                data.putInt(HwABSUtils.RES, result);
                Message msg = new Message();
                if (flag == 0) {
                    msg.what = HwABSModemScenario.MSG_ENTER_SEARCH_STATE;
                } else {
                    msg.what = HwABSModemScenario.MSG_EXIT_SEARCH_STATE;
                }
                msg.setData(data);
                HwABSModemScenario.this.mProcessHandler.sendMessage(msg);
            } else if (HwABSModemScenario.ACTION_HW_CRR_CONN_IND.equals(action)) {
                int modem0 = intent.getIntExtra(HwABSUtils.MODEM0, 0);
                int modem1 = intent.getIntExtra(HwABSUtils.MODEM1, 0);
                int modem2 = intent.getIntExtra(HwABSUtils.MODEM2, 0);
                HwABSUtils.logD("ACTION_HW_CRR_CONN_IND modem0 = " + modem0 + " modem1 =" + modem1 + " modem2 =" + modem2);
                if (modem0 == 0 && modem1 == 0 && modem2 == 0) {
                    HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(HwABSModemScenario.MSG_EXIT_CONNECT_STATE);
                } else {
                    HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(HwABSModemScenario.MSG_ENTER_CONNECT_STATE);
                }
            } else if (HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED.equals(action)) {
                HwABSModemScenario.this.mHandler.sendEmptyMessage(15);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.ABS.HwABSModemScenario.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.ABS.HwABSModemScenario.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSModemScenario.<clinit>():void");
    }

    public HwABSModemScenario(Context context, Handler handler) {
        this.intentFilter = new IntentFilter();
        this.mConnectedintentFilter = new IntentFilter();
        this.mCallBroadcastReceiver = new CallBroadcastReceiver();
        this.mModemBroadcastReceiver = new ModemBroadcastReceiver();
        this.mEnterSearchState = 0;
        this.mEnterSearchStateNum = 0;
        this.mEnterConnectState = 0;
        this.mEnterConnectStateNum = 0;
        this.listener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case MessageUtil.SWITCH_TO_WIFI_AUTO /*0*/:
                        HwABSUtils.logD("CALL_STATE_IDLE");
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(8);
                    case HwABSModemScenario.MSG_ENTER_SEARCH_STATE /*1*/:
                        HwABSUtils.logD("CALL_STATE_RINGING");
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(9);
                    case HwABSModemScenario.MSG_EXIT_SEARCH_STATE /*2*/:
                        HwABSUtils.logD("CALL_STATE_OFFHOOK");
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(10);
                    default:
                }
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                int voiceState = serviceState.getState();
                HwABSUtils.logD("onServiceStateChanged  voiceState= " + voiceState);
                switch (voiceState) {
                    case HwABSModemScenario.MSG_REPORT_ENTER_SEARCH_STATE /*3*/:
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(21);
                    default:
                }
            }
        };
        this.mProcessHandler = new Handler() {
            Message enterMsg;
            Bundle mData;

            {
                this.mData = null;
                this.enterMsg = null;
            }

            public void handleMessage(Message msg) {
                HwABSModemScenario hwABSModemScenario;
                switch (msg.what) {
                    case HwABSModemScenario.MSG_ENTER_SEARCH_STATE /*1*/:
                        HwABSUtils.logD("MSG_ENTER_SEARCH_STATE");
                        this.mData = msg.getData();
                        this.enterMsg = new Message();
                        this.enterMsg.setData(this.mData);
                        if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterSearchState <= 10000) {
                            hwABSModemScenario = HwABSModemScenario.this;
                            hwABSModemScenario.mEnterSearchStateNum = hwABSModemScenario.mEnterSearchStateNum + HwABSModemScenario.MSG_ENTER_SEARCH_STATE;
                            HwABSUtils.logD("MSG_ENTER_SEARCH_STATE mEnterSearchStateNum = " + HwABSModemScenario.this.mEnterSearchStateNum);
                            if (HwABSModemScenario.this.mEnterSearchStateNum >= HwABSModemScenario.MSG_EXIT_SEARCH_STATE) {
                                this.enterMsg.what = 13;
                                HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                                return;
                            }
                            return;
                        }
                        HwABSModemScenario.this.mEnterSearchStateNum = 0;
                        HwABSModemScenario.this.mEnterSearchState = System.currentTimeMillis();
                        this.enterMsg.what = HwABSModemScenario.MSG_REPORT_ENTER_SEARCH_STATE;
                        HwABSModemScenario.this.mProcessHandler.sendMessageDelayed(this.enterMsg, 2000);
                    case HwABSModemScenario.MSG_EXIT_SEARCH_STATE /*2*/:
                        HwABSUtils.logD("MSG_EXIT_SEARCH_STATE");
                        if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterSearchState < 2000) {
                            HwABSModemScenario.this.mProcessHandler.removeMessages(HwABSModemScenario.MSG_REPORT_ENTER_SEARCH_STATE);
                        }
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(14);
                    case HwABSModemScenario.MSG_REPORT_ENTER_SEARCH_STATE /*3*/:
                        HwABSUtils.logD("MSG_REPORT_ENTER_SEARCH_STATE");
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(13);
                    case HwABSModemScenario.MSG_ENTER_CONNECT_STATE /*4*/:
                        HwABSUtils.logD("MSG_ENTER_CONNECT_STATE");
                        if (HwABSModemScenario.this.mEnterConnectState == 0) {
                            HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                            HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(HwABSModemScenario.MSG_REPORT_ENTER_CONNECT_STATE, 2000);
                        } else if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState <= 10000) {
                            hwABSModemScenario = HwABSModemScenario.this;
                            hwABSModemScenario.mEnterConnectStateNum = hwABSModemScenario.mEnterConnectStateNum + HwABSModemScenario.MSG_ENTER_SEARCH_STATE;
                            HwABSUtils.logD("MSG_ENTER_CONNECT_STATE mEnterConnectStateNum = " + HwABSModemScenario.this.mEnterConnectStateNum);
                            if (HwABSModemScenario.this.mEnterConnectStateNum >= HwABSModemScenario.MSG_EXIT_SEARCH_STATE) {
                                HwABSModemScenario.this.mHandler.sendEmptyMessage(11);
                            }
                        } else {
                            HwABSModemScenario.this.mEnterConnectStateNum = 0;
                            HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                            HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(HwABSModemScenario.MSG_REPORT_ENTER_CONNECT_STATE, 2000);
                        }
                    case HwABSModemScenario.MSG_EXIT_CONNECT_STATE /*5*/:
                        HwABSUtils.logD("ACTION_ABS_EXIT_CONNECT");
                        if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState < 2000) {
                            HwABSModemScenario.this.mProcessHandler.removeMessages(HwABSModemScenario.MSG_REPORT_ENTER_CONNECT_STATE);
                        }
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(12);
                    case HwABSModemScenario.MSG_REPORT_ENTER_CONNECT_STATE /*6*/:
                        HwABSUtils.logD("MSG_REPORT_ENTER_CONNECT_STATE");
                        this.mData = msg.getData();
                        this.enterMsg = new Message();
                        this.enterMsg.what = 11;
                        this.enterMsg.setData(this.mData);
                        HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    default:
                }
            }
        };
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        HwABSUtils.logD("registerBroadcastReceiver");
        this.intentFilter.addAction(MSG_OUTGOING_CALL);
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mCallBroadcastReceiver, this.intentFilter);
        this.mConnectedintentFilter.addAction(HUAWEI_SIM_REG_PLMNSELINFO_ACTION);
        this.mConnectedintentFilter.addAction(ACTION_HW_CRR_CONN_IND);
        this.mConnectedintentFilter.addAction(HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED);
        this.mContext.registerReceiver(this.mModemBroadcastReceiver, this.mConnectedintentFilter);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }
}
