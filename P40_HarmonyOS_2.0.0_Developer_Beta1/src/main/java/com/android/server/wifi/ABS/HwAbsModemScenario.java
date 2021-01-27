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
import com.android.server.wifi.MSS.HwMssUtils;

public class HwAbsModemScenario {
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
    private Context mContext;
    private long mEnterConnectState = 0;
    private int mEnterConnectStateNum = 0;
    private long mEnterSearchState = 0;
    private int mEnterSearchStateNum = 0;
    private Handler mHandler;
    PhoneStateListener mListener = new PhoneStateListener() {
        /* class com.android.server.wifi.ABS.HwAbsModemScenario.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (HwMssUtils.is1105()) {
                HwAbsUtils.logD(false, "in 1105 chipset, no need switch to siso in call state", new Object[0]);
            } else if (state == 0) {
                HwAbsUtils.logD(false, "CALL_STATE_IDLE", new Object[0]);
                HwAbsModemScenario.this.mHandler.sendEmptyMessage(8);
            } else if (state == 1) {
                HwAbsUtils.logD(false, "CALL_STATE_RINGING", new Object[0]);
                HwAbsModemScenario.this.mHandler.sendEmptyMessage(9);
            } else if (state == 2) {
                HwAbsUtils.logD(false, "CALL_STATE_OFFHOOK", new Object[0]);
                HwAbsModemScenario.this.mHandler.sendEmptyMessage(10);
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            int voiceState = serviceState.getState();
            HwAbsUtils.logD(false, "onServiceStateChanged  voiceState= %{public}d", Integer.valueOf(voiceState));
            if (voiceState == 0) {
                HwAbsModemScenario.this.mHandler.sendEmptyMessage(25);
            } else if (voiceState != 1 && voiceState == 3) {
                HwAbsModemScenario.this.mHandler.sendEmptyMessage(22);
            }
        }
    };
    private BroadcastReceiver mModemBroadcastReceiver = new ModemBroadcastReceiver();
    private Handler mProcessHandler = new Handler() {
        /* class com.android.server.wifi.ABS.HwAbsModemScenario.AnonymousClass2 */
        Message enterMsg = null;
        Bundle mData = null;
        int mSubID = -1;

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handleEnterSearchState(msg);
                    return;
                case 2:
                    handleExitSearchState(msg);
                    return;
                case 3:
                case 4:
                case 5:
                    handleReportEnterSearchStateModem(msg);
                    return;
                case 6:
                    handleEnterConnectState();
                    return;
                case 7:
                    handleExitConnectState();
                    return;
                case 8:
                    handleReportEnterConnectState(msg);
                    return;
                default:
                    return;
            }
        }

        private void handleReportEnterConnectState(Message msg) {
            HwAbsUtils.logD(false, "MSG_REPORT_ENTER_CONNECT_STATE", new Object[0]);
            this.mData = msg.getData();
            this.enterMsg = Message.obtain();
            Message message = this.enterMsg;
            message.what = 11;
            message.setData(this.mData);
            HwAbsModemScenario.this.mHandler.sendMessage(this.enterMsg);
        }

        private void handleExitConnectState() {
            HwAbsUtils.logD(false, "ACTION_ABS_EXIT_CONNECT", new Object[0]);
            if (System.currentTimeMillis() - HwAbsModemScenario.this.mEnterConnectState < 2000) {
                HwAbsModemScenario.this.mProcessHandler.removeMessages(8);
            }
            HwAbsModemScenario.this.mHandler.sendEmptyMessage(13);
        }

        private void handleEnterConnectState() {
            HwAbsUtils.logD(false, "MSG_ENTER_CONNECT_STATE", new Object[0]);
            if (HwAbsModemScenario.this.mEnterConnectState == 0) {
                HwAbsModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                HwAbsModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
            } else if (System.currentTimeMillis() - HwAbsModemScenario.this.mEnterConnectState <= 10000) {
                HwAbsModemScenario.access$508(HwAbsModemScenario.this);
                HwAbsUtils.logD(false, "MSG_ENTER_CONNECT_STATE mEnterConnectStateNum = %{public}d", Integer.valueOf(HwAbsModemScenario.this.mEnterConnectStateNum));
                if (HwAbsModemScenario.this.mEnterConnectStateNum >= 3) {
                    HwAbsModemScenario.this.mHandler.sendEmptyMessage(12);
                }
            } else {
                HwAbsModemScenario.this.mEnterConnectStateNum = 0;
                HwAbsModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                HwAbsModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
            }
        }

        private void handleReportEnterSearchStateModem(Message msg) {
            HwAbsUtils.logD(false, "MSG_REPORT_ENTER_SEARCH_STATE", new Object[0]);
            this.mData = msg.getData();
            this.enterMsg = Message.obtain();
            this.enterMsg.setData(this.mData);
            this.enterMsg.what = 14;
            HwAbsModemScenario.this.mHandler.sendMessage(this.enterMsg);
        }

        private void handleExitSearchState(Message msg) {
            this.mData = msg.getData();
            this.enterMsg = Message.obtain();
            this.enterMsg.setData(this.mData);
            this.enterMsg.what = 15;
            this.mSubID = this.mData.getInt(HwAbsUtils.SUB_ID);
            HwAbsUtils.logD(false, "MSG_EXIT_SEARCH_STATE mSubID = %{public}d", Integer.valueOf(this.mSubID));
            int i = this.mSubID;
            if (i == 0) {
                removeMessages(3);
            } else if (i == 1) {
                removeMessages(4);
            } else if (i == 2) {
                removeMessages(5);
            } else {
                return;
            }
            HwAbsModemScenario.this.mHandler.sendMessage(this.enterMsg);
        }

        private void handleEnterSearchState(Message msg) {
            HwAbsUtils.logD(false, "MSG_ENTER_SEARCH_STATE", new Object[0]);
            this.mData = msg.getData();
            this.enterMsg = Message.obtain();
            this.enterMsg.setData(this.mData);
            if (System.currentTimeMillis() - HwAbsModemScenario.this.mEnterSearchState <= 10000) {
                HwAbsModemScenario.access$708(HwAbsModemScenario.this);
                HwAbsUtils.logD(false, "MSG_ENTER_SEARCH_STATE mEnterSearchStateNum = %{public}d", Integer.valueOf(HwAbsModemScenario.this.mEnterSearchStateNum));
                if (HwAbsModemScenario.this.mEnterSearchStateNum >= 3) {
                    this.enterMsg.what = 14;
                    HwAbsModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    return;
                }
                return;
            }
            HwAbsModemScenario.this.mEnterSearchStateNum = 0;
            HwAbsModemScenario.this.mEnterSearchState = System.currentTimeMillis();
            this.mSubID = this.mData.getInt(HwAbsUtils.SUB_ID);
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
        }
    };
    private TelephonyManager mTelephonyManager;

    static /* synthetic */ int access$508(HwAbsModemScenario x0) {
        int i = x0.mEnterConnectStateNum;
        x0.mEnterConnectStateNum = i + 1;
        return i;
    }

    static /* synthetic */ int access$708(HwAbsModemScenario x0) {
        int i = x0.mEnterSearchStateNum;
        x0.mEnterSearchStateNum = i + 1;
        return i;
    }

    public HwAbsModemScenario(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        if (this.mContext.getSystemService("phone") instanceof TelephonyManager) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        HwAbsUtils.logD(false, "registerBroadcastReceiver", new Object[0]);
        this.intentFilter.addAction(MSG_OUTGOING_CALL);
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction(HUAWEI_SIM_REG_PLMNSELINFO_ACTION);
        this.intentFilter.addAction(ACTION_HW_CRR_CONN_IND);
        this.intentFilter.addAction(HwAbsUtils.ACTION_WIFI_ANTENNA_PREEMPTED);
        this.mContext.registerReceiver(this.mModemBroadcastReceiver, this.intentFilter);
    }

    private class ModemBroadcastReceiver extends BroadcastReceiver {
        private ModemBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (HwAbsModemScenario.HUAWEI_SIM_REG_PLMNSELINFO_ACTION.equals(action)) {
                    int subId = intent.getIntExtra(HwAbsUtils.SUB_ID, 0);
                    int flag = intent.getIntExtra(HwAbsUtils.FLAG, 0);
                    int result = intent.getIntExtra(HwAbsUtils.RES, 0);
                    HwAbsUtils.logD(false, "HUAWEI_SIM_REG_PLMNSELINFO_ACTION subId = %{public}d flag =%{public}d result =%{public}d", Integer.valueOf(subId), Integer.valueOf(flag), Integer.valueOf(result));
                    Bundle data = new Bundle();
                    data.putInt(HwAbsUtils.SUB_ID, subId);
                    data.putInt(HwAbsUtils.FLAG, flag);
                    data.putInt(HwAbsUtils.RES, result);
                    Message msg = Message.obtain();
                    if (flag == 0) {
                        msg.what = 1;
                    } else {
                        msg.what = 2;
                    }
                    msg.setData(data);
                    HwAbsModemScenario.this.mProcessHandler.sendMessage(msg);
                } else if (HwAbsModemScenario.ACTION_HW_CRR_CONN_IND.equals(action)) {
                    sendConnMsg(intent);
                } else if (HwAbsUtils.ACTION_WIFI_ANTENNA_PREEMPTED.equals(action)) {
                    HwAbsModemScenario.this.mHandler.sendEmptyMessage(16);
                } else if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)) {
                    HwAbsUtils.logD(false, "MSG_OUTGOING_CALL", new Object[0]);
                    HwAbsModemScenario.this.mHandler.sendEmptyMessage(7);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwAbsUtils.logD(false, "ACTION_BOOT_COMPLETED", new Object[0]);
                    HwAbsModemScenario.this.mHandler.sendEmptyMessage(37);
                    HwAbsModemScenario.this.mTelephonyManager.listen(HwAbsModemScenario.this.mListener, 33);
                } else {
                    HwAbsUtils.logD(false, "No processing type", new Object[0]);
                }
            }
        }

        private void sendConnMsg(Intent intent) {
            if (intent != null) {
                int modem0 = intent.getIntExtra(HwAbsUtils.MODEM0, 0);
                int modem1 = intent.getIntExtra(HwAbsUtils.MODEM1, 0);
                int modem2 = intent.getIntExtra(HwAbsUtils.MODEM2, 0);
                HwAbsUtils.logD(false, "ACTION_HW_CRR_CONN_IND modem0 = %{public}d modem1 =%{public}d modem2 =%{public}d", Integer.valueOf(modem0), Integer.valueOf(modem1), Integer.valueOf(modem2));
                if (modem0 == 0 && modem1 == 0 && modem2 == 0) {
                    HwAbsModemScenario.this.mProcessHandler.sendEmptyMessage(7);
                } else {
                    HwAbsModemScenario.this.mProcessHandler.sendEmptyMessage(6);
                }
            }
        }
    }
}
