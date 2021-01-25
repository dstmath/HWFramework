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
import com.android.server.wifi.MSS.HwMSSUtils;

public class HwABSModemScenario {
    public static final String ACTION_HW_CRR_CONN_IND = "com.huawei.action.ACTION_HW_CRR_CONN_IND";
    public static final String HUAWEI_SIM_REG_PLMNSELINFO_ACTION = "com.huawei.action.SIM_PLMN_SELINFO";
    private static final int MODEM_EXCEPTION_INTERVAL_TIME = 10000;
    private static final int MODEM_EXCEPTION_NUM = 3;
    private static final int MODEM_EXCEPTION_REPORT_TIME = 2000;
    private static final int MSG_ENTER_CONNECT_STATE = 6;
    private static final int MSG_ENTER_SEARCH_STATE = 1;
    private static final int MSG_EXIT_CONNECT_STATE = 7;
    private static final int MSG_EXIT_SEARCH_STATE = 2;
    private static String MSG_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    private static final int MSG_REPORT_ENTER_CONNECT_STATE = 8;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_0 = 3;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_1 = 4;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_2 = 5;
    private IntentFilter intentFilter = new IntentFilter();
    PhoneStateListener listener = new PhoneStateListener() {
        /* class com.android.server.wifi.ABS.HwABSModemScenario.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (HwMSSUtils.is1105()) {
                HwABSUtils.logD(false, "in 1105 chipset, no need switch to siso in call state", new Object[0]);
            } else if (state == 0) {
                HwABSUtils.logD(false, "CALL_STATE_IDLE", new Object[0]);
                HwABSModemScenario.this.mHandler.sendEmptyMessage(8);
            } else if (state == 1) {
                HwABSUtils.logD(false, "CALL_STATE_RINGING", new Object[0]);
                HwABSModemScenario.this.mHandler.sendEmptyMessage(9);
            } else if (state == 2) {
                HwABSUtils.logD(false, "CALL_STATE_OFFHOOK", new Object[0]);
                HwABSModemScenario.this.mHandler.sendEmptyMessage(10);
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            int voiceState = serviceState.getState();
            HwABSUtils.logD(false, "onServiceStateChanged  voiceState= %{public}d", Integer.valueOf(voiceState));
            if (voiceState == 0) {
                HwABSModemScenario.this.mHandler.sendEmptyMessage(25);
            } else if (voiceState != 1 && voiceState == 3) {
                HwABSModemScenario.this.mHandler.sendEmptyMessage(22);
            }
        }
    };
    private Context mContext;
    private long mEnterConnectState = 0;
    private int mEnterConnectStateNum = 0;
    private long mEnterSearchState = 0;
    private int mEnterSearchStateNum = 0;
    private Handler mHandler;
    private BroadcastReceiver mModemBroadcastReceiver = new ModemBroadcastReceiver();
    private Handler mProcessHandler = new Handler() {
        /* class com.android.server.wifi.ABS.HwABSModemScenario.AnonymousClass2 */
        Message enterMsg = null;
        Bundle mData = null;
        int mSubID = -1;

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwABSUtils.logD(false, "MSG_ENTER_SEARCH_STATE", new Object[0]);
                    this.mData = msg.getData();
                    this.enterMsg = Message.obtain();
                    this.enterMsg.setData(this.mData);
                    if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterSearchState <= 10000) {
                        HwABSModemScenario.access$508(HwABSModemScenario.this);
                        HwABSUtils.logD(false, "MSG_ENTER_SEARCH_STATE mEnterSearchStateNum = %{public}d", Integer.valueOf(HwABSModemScenario.this.mEnterSearchStateNum));
                        if (HwABSModemScenario.this.mEnterSearchStateNum >= 3) {
                            this.enterMsg.what = 14;
                            HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                            return;
                        }
                        return;
                    }
                    HwABSModemScenario.this.mEnterSearchStateNum = 0;
                    HwABSModemScenario.this.mEnterSearchState = System.currentTimeMillis();
                    this.mSubID = this.mData.getInt(HwABSUtils.SUB_ID);
                    int i = this.mSubID;
                    if (i == 0) {
                        this.enterMsg.what = 3;
                    } else if (i == 1) {
                        this.enterMsg.what = 4;
                    } else if (i == 2) {
                        this.enterMsg.what = 5;
                    } else {
                        return;
                    }
                    sendMessageDelayed(this.enterMsg, 2000);
                    return;
                case 2:
                    this.mData = msg.getData();
                    this.enterMsg = Message.obtain();
                    this.enterMsg.setData(this.mData);
                    this.enterMsg.what = 15;
                    this.mSubID = this.mData.getInt(HwABSUtils.SUB_ID);
                    HwABSUtils.logD(false, "MSG_EXIT_SEARCH_STATE mSubID = %{public}d", Integer.valueOf(this.mSubID));
                    int i2 = this.mSubID;
                    if (i2 == 0) {
                        removeMessages(3);
                    } else if (i2 == 1) {
                        removeMessages(4);
                    } else if (i2 == 2) {
                        removeMessages(5);
                    } else {
                        return;
                    }
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    return;
                case 3:
                case 4:
                case 5:
                    HwABSUtils.logD(false, "MSG_REPORT_ENTER_SEARCH_STATE", new Object[0]);
                    this.mData = msg.getData();
                    this.enterMsg = Message.obtain();
                    this.enterMsg.setData(this.mData);
                    this.enterMsg.what = 14;
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    return;
                case 6:
                    HwABSUtils.logD(false, "MSG_ENTER_CONNECT_STATE", new Object[0]);
                    if (HwABSModemScenario.this.mEnterConnectState == 0) {
                        HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                        HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
                        return;
                    } else if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState <= 10000) {
                        HwABSModemScenario.access$708(HwABSModemScenario.this);
                        HwABSUtils.logD(false, "MSG_ENTER_CONNECT_STATE mEnterConnectStateNum = %{public}d", Integer.valueOf(HwABSModemScenario.this.mEnterConnectStateNum));
                        if (HwABSModemScenario.this.mEnterConnectStateNum >= 3) {
                            HwABSModemScenario.this.mHandler.sendEmptyMessage(12);
                            return;
                        }
                        return;
                    } else {
                        HwABSModemScenario.this.mEnterConnectStateNum = 0;
                        HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                        HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
                        return;
                    }
                case 7:
                    HwABSUtils.logD(false, "ACTION_ABS_EXIT_CONNECT", new Object[0]);
                    if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState < 2000) {
                        HwABSModemScenario.this.mProcessHandler.removeMessages(8);
                    }
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(13);
                    return;
                case 8:
                    HwABSUtils.logD(false, "MSG_REPORT_ENTER_CONNECT_STATE", new Object[0]);
                    this.mData = msg.getData();
                    this.enterMsg = Message.obtain();
                    Message message = this.enterMsg;
                    message.what = 11;
                    message.setData(this.mData);
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    return;
                default:
                    return;
            }
        }
    };
    private TelephonyManager mTelephonyManager;

    static /* synthetic */ int access$508(HwABSModemScenario x0) {
        int i = x0.mEnterSearchStateNum;
        x0.mEnterSearchStateNum = i + 1;
        return i;
    }

    static /* synthetic */ int access$708(HwABSModemScenario x0) {
        int i = x0.mEnterConnectStateNum;
        x0.mEnterConnectStateNum = i + 1;
        return i;
    }

    public HwABSModemScenario(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        HwABSUtils.logD(false, "registerBroadcastReceiver", new Object[0]);
        this.intentFilter.addAction(MSG_OUTGOING_CALL);
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction(HUAWEI_SIM_REG_PLMNSELINFO_ACTION);
        this.intentFilter.addAction(ACTION_HW_CRR_CONN_IND);
        this.intentFilter.addAction(HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED);
        this.mContext.registerReceiver(this.mModemBroadcastReceiver, this.intentFilter);
    }

    private class ModemBroadcastReceiver extends BroadcastReceiver {
        private ModemBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (HwABSModemScenario.HUAWEI_SIM_REG_PLMNSELINFO_ACTION.equals(action)) {
                    int subId = intent.getIntExtra(HwABSUtils.SUB_ID, 0);
                    int flag = intent.getIntExtra(HwABSUtils.FLAG, 0);
                    int result = intent.getIntExtra(HwABSUtils.RES, 0);
                    HwABSUtils.logD(false, "HUAWEI_SIM_REG_PLMNSELINFO_ACTION subId = %{public}d flag =%{public}d result =%{public}d", Integer.valueOf(subId), Integer.valueOf(flag), Integer.valueOf(result));
                    Bundle data = new Bundle();
                    data.putInt(HwABSUtils.SUB_ID, subId);
                    data.putInt(HwABSUtils.FLAG, flag);
                    data.putInt(HwABSUtils.RES, result);
                    Message msg = Message.obtain();
                    if (flag == 0) {
                        msg.what = 1;
                    } else {
                        msg.what = 2;
                    }
                    msg.setData(data);
                    HwABSModemScenario.this.mProcessHandler.sendMessage(msg);
                } else if (HwABSModemScenario.ACTION_HW_CRR_CONN_IND.equals(action)) {
                    int modem0 = intent.getIntExtra(HwABSUtils.MODEM0, 0);
                    int modem1 = intent.getIntExtra(HwABSUtils.MODEM1, 0);
                    int modem2 = intent.getIntExtra(HwABSUtils.MODEM2, 0);
                    HwABSUtils.logD(false, "ACTION_HW_CRR_CONN_IND modem0 = %{public}d modem1 =%{public}d modem2 =%{public}d", Integer.valueOf(modem0), Integer.valueOf(modem1), Integer.valueOf(modem2));
                    if (modem0 == 0 && modem1 == 0 && modem2 == 0) {
                        HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(7);
                    } else {
                        HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(6);
                    }
                } else if (HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED.equals(action)) {
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(16);
                } else if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)) {
                    HwABSUtils.logD(false, "MSG_OUTGOING_CALL", new Object[0]);
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(7);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwABSUtils.logD(false, "ACTION_BOOT_COMPLETED", new Object[0]);
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(37);
                    HwABSModemScenario.this.mTelephonyManager.listen(HwABSModemScenario.this.listener, 33);
                }
            }
        }
    }
}
