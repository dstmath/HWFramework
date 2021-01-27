package com.huawei.internal.telephony.vsim;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.HwVSimRilReceiveTask;
import com.huawei.internal.telephony.vsim.IHwVSimPhoneSwitchCallback;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;
import com.huawei.internal.telephony.vsim.util.AsyncResultUtil;
import java.util.Optional;
import java.util.function.Consumer;

public class HwVSimRilReceiveTask {
    private static final int EVENT_NETWORK_REJECTED_CASE = 106;
    private static final int EVENT_RADIO_STATE_CHANED = 107;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 105;
    private static final int EVENT_VSIM_AP_TRAFFIC = 103;
    private static final int EVENT_VSIM_BASE = 100;
    private static final int EVENT_VSIM_PLMN_SELINFO = 102;
    private static final int EVENT_VSIM_RDH_NEEDED = 101;
    private static final int EVENT_VSIM_TIMER_TASK_EXPIRED = 104;
    private static final String TAG = "HwVSimRilReceiveTask";
    private final IHwVSimPhoneSwitchCallback callback = new IHwVSimPhoneSwitchCallback.Stub() {
        /* class com.huawei.internal.telephony.vsim.HwVSimRilReceiveTask.AnonymousClass1 */

        @Override // com.huawei.internal.telephony.vsim.IHwVSimPhoneSwitchCallback
        public void onVsimPhoneSwitch(int phoneId) {
            synchronized (HwVSimRilReceiveTask.this.lock) {
                HwVSimRilReceiveTask.log("phone switch:  " + HwVSimRilReceiveTask.this.currentSlotId + " -> " + phoneId);
                HwVSimRilReceiveTask.this.unregisterCommend(HwVSimRilReceiveTask.this.getVSimCi());
                HwVSimRilReceiveTask.this.currentSlotId = phoneId;
                CommandsInterfaceEx newCis = HwVSimRilReceiveTask.this.getVSimCi();
                if (!HwVSimRilReceiveTask.this.start(newCis)) {
                    HwVSimRilReceiveTask.this.registerCommend(newCis);
                }
            }
        }
    };
    private final Context context;
    private volatile int currentSlotId = -1;
    private final Object lock = new Object();
    private final CommandsInterfaceEx[] mCis;
    private EventHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsNeedBroadcastVSimAbsentState = false;
    private final PhoneExt[] mPhones;

    HwVSimRilReceiveTask(PhoneExt[] phone, CommandsInterfaceEx[] ci) {
        this.mPhones = phone;
        this.mCis = ci;
        this.context = ((PhoneExt) ArrayUtils.get(phone, 0, new PhoneExt())).getContext();
        if (ArrayUtils.isEmpty(phone)) {
            loge("task can not run with no phone.");
        }
        if (ArrayUtils.isEmpty(ci)) {
            loge("task can not run with no CommandsInterfaceEx.");
        }
    }

    public IHwVSimPhoneSwitchCallback getVSimPhoneSwitchCallback() {
        return this.callback;
    }

    public void stop() {
        synchronized (this.lock) {
            if (this.mHandler == null) {
                log("stop cancel, never started");
                return;
            }
            unregisterCommend(getVSimCi());
            if (!(this.mHandlerThread == null || this.mHandlerThread.getLooper() == null)) {
                this.mHandlerThread.getLooper().quit();
            }
            this.mHandlerThread = null;
            this.mHandler = null;
            log("stop success");
        }
    }

    public int getCurrentSlotId() {
        int i;
        synchronized (this.lock) {
            i = this.currentSlotId;
        }
        return i;
    }

    public PhoneExt getVSimPhone() {
        PhoneExt phoneExt;
        synchronized (this.lock) {
            phoneExt = (PhoneExt) ArrayUtils.get(this.mPhones, this.currentSlotId, (Object) null);
        }
        return phoneExt;
    }

    public boolean isNeedBroadcastVSimAbsentState() {
        if (!this.mIsNeedBroadcastVSimAbsentState) {
            return false;
        }
        setIsNeedBroadcastVSimAbsentState(false);
        return true;
    }

    private void setIsNeedBroadcastVSimAbsentState(boolean state) {
        log("setIsNeedBroadcastVSimAbsentState, state from " + this.mIsNeedBroadcastVSimAbsentState + " -> " + state);
        this.mIsNeedBroadcastVSimAbsentState = state;
    }

    private CommandsInterfaceEx getVSimCi() {
        CommandsInterfaceEx commandsInterfaceEx;
        synchronized (this.lock) {
            commandsInterfaceEx = (CommandsInterfaceEx) ArrayUtils.get(this.mCis, this.currentSlotId, (Object) null);
        }
        return commandsInterfaceEx;
    }

    private boolean start(CommandsInterfaceEx ci) {
        if (ci == null) {
            log("start fail, ci null");
            return false;
        } else if (this.mHandlerThread != null) {
            return false;
        } else {
            this.mHandlerThread = new HandlerThread("VSimSSTThread");
            this.mHandlerThread.start();
            this.mHandler = new EventHandler(this.mHandlerThread.getLooper());
            registerCommend(ci);
            if (!HwVSimConstants.DEBUG) {
                return true;
            }
            log("start success");
            return true;
        }
    }

    private void registerCommend(CommandsInterfaceEx ci) {
        EventHandler eventHandler;
        if (ci != null && (eventHandler = this.mHandler) != null) {
            ci.setOnVsimRDH(eventHandler, 101, (Object) null);
            ci.setOnVsimRegPLMNSelInfo(this.mHandler, 102, (Object) null);
            ci.setOnVsimApDsFlowInfo(this.mHandler, 103, (Object) null);
            ci.setOnVsimTimerTaskExpired(this.mHandler, (int) EVENT_VSIM_TIMER_TASK_EXPIRED, (Object) null);
            ci.registerForRplmnsStateChanged(this.mHandler, (int) EVENT_RPLMNS_STATE_CHANGED, (Object) null);
            EventHandler eventHandler2 = this.mHandler;
            eventHandler2.sendMessage(eventHandler2.obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
            ci.setOnNetReject(this.mHandler, (int) EVENT_NETWORK_REJECTED_CASE, (Object) null);
            ci.registerForRadioStateChanged(this.mHandler, (int) EVENT_RADIO_STATE_CHANED, (Object) null);
        }
    }

    private void unregisterCommend(CommandsInterfaceEx ci) {
        EventHandler eventHandler;
        if (ci != null && (eventHandler = this.mHandler) != null) {
            ci.unSetOnVsimRDH(eventHandler);
            ci.unSetOnVsimRegPLMNSelInfo(this.mHandler);
            ci.unSetOnVsimApDsFlowInfo(this.mHandler);
            ci.unSetOnVsimTimerTaskExpired(this.mHandler);
            ci.unregisterForRplmnsStateChanged(this.mHandler);
            ci.unSetOnNetReject(this.mHandler);
            ci.unregisterForRadioStateChanged(this.mHandler);
        }
    }

    public static void loge(String msg) {
        HwVSimLog.error(TAG, "[VSimSST] " + msg);
    }

    public static void log(String msg) {
        HwVSimLog.VSimLogI(TAG, "[VSimSST] " + msg);
    }

    public class EventHandler extends Handler {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        EventHandler(Looper looper) {
            super(looper);
            HwVSimRilReceiveTask.this = r1;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    HwVSimRilReceiveTask.log("EVENT_VSIM_RDH_NEEDED");
                    sendBroadcastRDHNeeded();
                    return;
                case 102:
                    HwVSimRilReceiveTask.log("EVENT_VSIM_PLMN_SELINFO");
                    handleVSimPlmnSelinfo(msg);
                    return;
                case 103:
                    HwVSimRilReceiveTask.log("EVENT_VSIM_AP_TRAFFIC");
                    sendBroadcastApTraffic((String[]) getFromMsg(msg));
                    return;
                case HwVSimRilReceiveTask.EVENT_VSIM_TIMER_TASK_EXPIRED /* 104 */:
                    HwVSimRilReceiveTask.log("EVENT_VSIM_TIMER_TASK_EXPIRED");
                    sendBroadcastTimerTaskExpired(getIntsFromMsg(msg));
                    return;
                case HwVSimRilReceiveTask.EVENT_RPLMNS_STATE_CHANGED /* 105 */:
                    HwVSimRilReceiveTask.log("EVENT_RPLMNS_STATE_CHANGED");
                    onRplmnChanged();
                    return;
                case HwVSimRilReceiveTask.EVENT_NETWORK_REJECTED_CASE /* 106 */:
                    HwVSimRilReceiveTask.log("EVENT_NETWORK_REJECTED_CASE");
                    onNetworkReject((String[]) getFromMsg(msg));
                    return;
                case HwVSimRilReceiveTask.EVENT_RADIO_STATE_CHANED /* 107 */:
                    HwVSimRilReceiveTask.log("EVENT_RADIO_STATE_CHANED");
                    onRadioStateChanged();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }

        private void handleVSimPlmnSelinfo(Message msg) {
            int[] response = getIntsFromMsg(msg);
            int size = ArrayUtils.size(response);
            if (size < 2) {
                HwVSimRilReceiveTask.loge("handleVSimPlmnSelinfo error response, size =" + size);
                return;
            }
            sendBroadcastRegPLMNSelInfo(response[0], response[1]);
        }

        private void sendBroadcastRDHNeeded() {
            HwVSimRilReceiveTask.log("sendBroadcastRDHNeeded");
            Intent intent = new Intent("com.huawei.vsim.action.NEED_NEGOTIATION");
            int subId = getPhoneId();
            intent.putExtra("subId", subId);
            HwVSimRilReceiveTask.log("subId: " + subId);
            sendBroadcast(intent);
        }

        private void sendBroadcastRejInfo(int rejectcause, String plmn) {
            HwVSimRilReceiveTask.log("sendBroadcastRejInfo");
            int subId = getPhoneId();
            Intent intent = new Intent("com.huawei.vsim.action.SIM_REJINFO_ACTION");
            intent.putExtra("subId", subId);
            intent.putExtra("errcode", rejectcause);
            intent.putExtra("plmn", plmn);
            StringBuilder sb = new StringBuilder();
            sb.append("subId: ");
            sb.append(subId);
            sb.append(", rejectcause: ");
            sb.append(rejectcause);
            sb.append(", plmn: ");
            sb.append(HwVSimConstants.DEBUG ? plmn : "***");
            HwVSimRilReceiveTask.log(sb.toString());
            sendBroadcast(intent);
        }

        private void onRplmnChanged() {
            String rplmn = SystemPropertiesEx.get("ril.operator.numeric", BuildConfig.FLAVOR);
            int subId = getPhoneId();
            Intent intent = new Intent("com.huawei.vsim.action.SIM_RESIDENT_PLMN");
            intent.putExtra("subId", subId);
            intent.putExtra("resident", rplmn);
            StringBuilder sb = new StringBuilder();
            sb.append("sendBroadcastRPLMNChanged subId: ");
            sb.append(subId);
            sb.append(" resident: ");
            sb.append(HwVSimConstants.DEBUG ? rplmn : "***");
            HwVSimRilReceiveTask.log(sb.toString());
            sendBroadcast(intent);
        }

        private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
            Intent intent = new Intent("com.huawei.vsim.action.SIM_PLMN_SELINFO");
            int subId = getPhoneId();
            intent.putExtra("subId", subId);
            intent.putExtra("flag", flag);
            intent.putExtra("res", result);
            HwVSimRilReceiveTask.log("sendBroadcastRegPLMNSelInfo subId: " + subId + " flag: " + flag + " result: " + result);
            sendBroadcast(intent);
        }

        private void sendBroadcastApTraffic(String[] response) {
            int responseSize = ArrayUtils.size(response);
            if (responseSize < 7) {
                HwVSimRilReceiveTask.loge("sendBroadcastApTraffic fail , error response size:" + responseSize);
                return;
            }
            String currDsTime = response[0];
            String txRate = response[1];
            String rxRate = response[2];
            String currTxFlow = response[3];
            String currRxFlow = response[4];
            String totalTxFlow = response[5];
            String totalRxFlow = response[6];
            Intent intent = new Intent("com.huawei.vsim.action.SIM_TRAFFIC");
            int subId = getPhoneId();
            intent.putExtra("subId", subId);
            intent.putExtra("curr_ds_time", currDsTime);
            intent.putExtra("tx_rate", txRate);
            intent.putExtra("rx_rate", rxRate);
            intent.putExtra("curr_tx_flow", currTxFlow);
            intent.putExtra("curr_rx_flow", currRxFlow);
            intent.putExtra("total_tx_flow", totalTxFlow);
            intent.putExtra("total_rx_flow", totalRxFlow);
            HwVSimRilReceiveTask.log("sendBroadcastApTraffic subId: " + subId + " curr_ds_time: " + currDsTime + " tx_rate: " + txRate + " rx_rate: " + rxRate + " curr_tx_flow: " + currTxFlow + " curr_rx_flow: " + currRxFlow + " total_tx_flow: " + totalTxFlow + " total_rx_flow: " + totalRxFlow);
            sendBroadcast(intent);
        }

        private void sendBroadcastTimerTaskExpired(int[] response) {
            if (ArrayUtils.isEmpty(response)) {
                HwVSimRilReceiveTask.log("sendBroadcastTimerTaskExpired response empty");
                return;
            }
            int type = response[0];
            Intent intent = new Intent("com.huawei.vsim.action.TIMERTASK_EXPIRED_ACTION");
            int subId = getPhoneId();
            intent.putExtra("subId", subId);
            intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, type);
            HwVSimRilReceiveTask.log("sendBroadcastTimerTaskExpired subId: " + subId + " type: " + type);
            sendBroadcast(intent);
        }

        private void onNetworkReject(String[] data) {
            String rejectPlmn = null;
            int rejectDomain = -1;
            int rejectCause = -1;
            int rejectRat = -1;
            int size = ArrayUtils.size(data);
            if (size >= 4) {
                try {
                    if (data[0] != null && data[0].length() > 0) {
                        rejectPlmn = data[0];
                    }
                    if (data[1] != null && data[1].length() > 0) {
                        rejectDomain = Integer.parseInt(data[1]);
                    }
                    if (data[2] != null && data[2].length() > 0) {
                        rejectCause = Integer.parseInt(data[2]);
                    }
                    if (data[3] != null && data[3].length() > 0) {
                        rejectRat = Integer.parseInt(data[3]);
                    }
                } catch (NumberFormatException e) {
                    HwVSimRilReceiveTask.loge("Number format has exception");
                } catch (Exception e2) {
                    HwVSimRilReceiveTask.loge("error parsing NetworkReject!");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("networkReject:PLMN = ");
                sb.append(HwVSimConstants.DEBUG ? rejectPlmn : "***");
                sb.append(" domain = ");
                sb.append(rejectDomain);
                sb.append(" cause = ");
                sb.append(rejectCause);
                sb.append(" RAT = ");
                sb.append(rejectRat);
                HwVSimRilReceiveTask.log(sb.toString());
                sendBroadcastRejInfo(rejectCause, rejectPlmn);
                return;
            }
            HwVSimRilReceiveTask.loge("onNetworkReject data error size:" + size);
        }

        private void onRadioStateChanged() {
            CommandsInterfaceEx ci = HwVSimRilReceiveTask.this.getVSimCi();
            if (ci == null) {
                HwVSimRilReceiveTask.log("onRadioStateChanged, vsim ci is null, return");
            } else if (ci.getRadioState() == 2) {
                HwVSimRilReceiveTask.this.setIsNeedBroadcastVSimAbsentState(true);
            }
        }

        private int getPhoneId() {
            return HwVSimRilReceiveTask.this.getCurrentSlotId();
        }

        private void sendBroadcast(Intent intent) {
            Optional.of(HwVSimRilReceiveTask.this.context).ifPresent(new Consumer(intent) {
                /* class com.huawei.internal.telephony.vsim.$$Lambda$HwVSimRilReceiveTask$EventHandler$xZ9T5mwdi4i0fUAODNZhHG9Fmx8 */
                private final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    HwVSimRilReceiveTask.EventHandler.lambda$sendBroadcast$0(this.f$0, (Context) obj);
                }
            });
        }

        private <T> T[] getFromMsg(Message msg) {
            return (T[]) AsyncResultUtil.getArrayResult(AsyncResultEx.from(msg.obj));
        }

        private int[] getIntsFromMsg(Message msg) {
            return AsyncResultUtil.getIntArrayResult(AsyncResultEx.from(msg.obj));
        }
    }
}
