package com.huawei.wifi2;

import android.net.wifi.IClientInterface;
import android.net.wifi.IWificond;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.wifi.HwHiLog;
import com.huawei.wifi2.HwWifi2Native;
import java.util.Arrays;

public class HwWifi2CondControl implements IBinder.DeathRecipient {
    public static final int DEFAULT_SIGNAL_POLL_PARA_LENGTH = 4;
    public static final int HW_SIGNAL_POLL_PARA_LENGTH = 13;
    private static final String TAG = "HwWifi2CondControl";
    private static final int TX_PACKET_PARA_LENGTH = 2;
    private static final String WIFICOND_SERVICE_NAME = "wificond";
    private IClientInterface mClientInterface;
    private HwWifi2Native.WificondDeathEventHandler mDeathEventHandler;
    private Handler mEventHandler;
    private String mIfaceName = "wlan1";
    private IWificond mWificond;

    HwWifi2CondControl(Looper looper) {
        this.mEventHandler = new Handler(looper);
    }

    public boolean initialize(HwWifi2Native.WificondDeathEventHandler wificondDeathEventHandler) {
        HwHiLog.i(TAG, false, "initialize enter", new Object[0]);
        if (this.mDeathEventHandler != null) {
            HwHiLog.i(TAG, false, "Wificond handle already retrieved", new Object[0]);
        }
        this.mDeathEventHandler = wificondDeathEventHandler;
        retrieveWifi2cond();
        return true;
    }

    public boolean setup(String ifaceName) {
        HwHiLog.i(TAG, false, "setup enter", new Object[0]);
        if (!retrieveWifi2cond()) {
            HwHiLog.e(TAG, false, "setup fail", new Object[0]);
            return false;
        }
        try {
            this.mClientInterface = this.mWificond.createClientInterface(ifaceName);
            IClientInterface iClientInterface = this.mClientInterface;
            if (iClientInterface == null) {
                HwHiLog.e(TAG, false, "Could not get IClientInterface instance from wificond", new Object[0]);
                return false;
            }
            Binder.allowBlocking(iClientInterface.asBinder());
            this.mIfaceName = ifaceName;
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Failed to get IClientInterface due to remote exception", new Object[0]);
            return false;
        }
    }

    public boolean tearDown() {
        HwHiLog.e(TAG, false, "tearDown enter", new Object[0]);
        IWificond iWificond = this.mWificond;
        if (iWificond == null) {
            HwHiLog.e(TAG, false, "tearDown, mWificond or mClientInterface is null", new Object[0]);
            return false;
        }
        try {
            iWificond.tearDownClientInterface(this.mIfaceName);
            this.mWificond = null;
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Failed to teardown client interface due to remote exception", new Object[0]);
            return false;
        }
    }

    public HwWifi2Native.SignalPollResult signalPoll() {
        IClientInterface iClientInterface = this.mClientInterface;
        if (iClientInterface == null) {
            HwHiLog.e(TAG, false, "No valid wificond client interface handler", new Object[0]);
            return null;
        }
        try {
            int[] resultArray = iClientInterface.signalPoll();
            if (resultArray == null || !(resultArray.length == 13 || resultArray.length == 4)) {
                HwHiLog.e(TAG, false, "Invalid signal poll result from wificond", new Object[0]);
                return null;
            }
            HwWifi2Native.SignalPollResult pollResult = new HwWifi2Native.SignalPollResult();
            pollResult.currentRssi = resultArray[0];
            pollResult.txBitrate = resultArray[1];
            pollResult.associationFrequency = resultArray[2];
            pollResult.rxBitrate = resultArray[3];
            if (resultArray.length == 13) {
                pollResult.currentNoise = resultArray[4];
                pollResult.currentSnr = resultArray[5];
                pollResult.currentChload = resultArray[6];
                pollResult.currentUlDelay = resultArray[7];
                pollResult.currentTxBytes = resultArray[8];
                pollResult.currentTxPackets = resultArray[9];
                pollResult.currentTxFailed = resultArray[10];
                pollResult.currentRxBytes = resultArray[11];
                pollResult.currentRxPackets = resultArray[12];
                HwHiLog.i(TAG, false, "Noise: %{public}d, Snr: %{public}d, Chload: %{public}d, rssi: %{public}d, txBitrate: %{public}d, rxBitrate: %{public}d, frequency: %{public}d, UlDelay: %{public}d, currentTxBytes: %{public}d, currentTxPackets: %{public}d , currentTxFailed: %{public}d, currentRxBytes: %{public}d, currentRxPackets: %{public}d", new Object[]{Integer.valueOf(pollResult.currentNoise), Integer.valueOf(pollResult.currentSnr), Integer.valueOf(pollResult.currentChload), Integer.valueOf(pollResult.currentRssi), Integer.valueOf(pollResult.txBitrate), Integer.valueOf(pollResult.rxBitrate), Integer.valueOf(pollResult.associationFrequency), Integer.valueOf(pollResult.currentUlDelay), Integer.valueOf(pollResult.currentTxBytes), Integer.valueOf(pollResult.currentTxPackets), Integer.valueOf(pollResult.currentTxFailed), Integer.valueOf(pollResult.currentRxBytes), Integer.valueOf(pollResult.currentRxPackets)});
            } else {
                HwHiLog.i(TAG, false, "SignalPollResult is %{public}s", new Object[]{Arrays.toString(resultArray)});
            }
            return pollResult;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Failed to do signal polling due to remote exception", new Object[0]);
            return null;
        }
    }

    public HwWifi2Native.TxPacketCounters getTxPacketCounters() {
        HwHiLog.i(TAG, false, "getTxPacketCounters enter", new Object[0]);
        IClientInterface iClientInterface = this.mClientInterface;
        if (iClientInterface == null) {
            HwHiLog.e(TAG, false, "No valid wificond client interface handler", new Object[0]);
            return null;
        }
        try {
            int[] resultArray = iClientInterface.getPacketCounters();
            if (resultArray == null || resultArray.length != 2) {
                HwHiLog.e(TAG, false, "Invalid signal poll result from wificond", new Object[0]);
                return null;
            }
            HwWifi2Native.TxPacketCounters counters = new HwWifi2Native.TxPacketCounters();
            counters.txSucceeded = resultArray[0];
            counters.txFailed = resultArray[1];
            HwHiLog.i(TAG, false, "getTxPacketCounters: TxPacketCounters.txSucceeded is %{public}d TxPacketCounters.txFailed is %{public}d", new Object[]{Integer.valueOf(counters.txSucceeded), Integer.valueOf(counters.txFailed)});
            return counters;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Failed to do signal polling due to remote exception", new Object[0]);
            return null;
        }
    }

    private boolean retrieveWifi2cond() {
        if (this.mWificond != null) {
            HwHiLog.i(TAG, false, "retrieveWifi2cond already retrieved", new Object[0]);
            return true;
        }
        this.mWificond = IWificond.Stub.asInterface(ServiceManager.getService(WIFICOND_SERVICE_NAME));
        IWificond iWificond = this.mWificond;
        if (iWificond != null) {
            try {
                iWificond.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Failed to register death notification for wificond", new Object[0]);
                return false;
            }
        }
        return this.mWificond != null;
    }

    private void clear() {
        this.mWificond = null;
        this.mClientInterface = null;
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        this.mEventHandler.post(new Runnable() {
            /* class com.huawei.wifi2.$$Lambda$HwWifi2CondControl$NRfJ3SxCmrF6j6LS0A6nroBeUY */

            @Override // java.lang.Runnable
            public final void run() {
                HwWifi2CondControl.this.lambda$binderDied$0$HwWifi2CondControl();
            }
        });
    }

    public /* synthetic */ void lambda$binderDied$0$HwWifi2CondControl() {
        HwHiLog.e(TAG, false, "Wificond died!", new Object[0]);
        synchronized (this) {
            this.mWificond = null;
        }
        HwWifi2Native.WificondDeathEventHandler wificondDeathEventHandler = this.mDeathEventHandler;
        if (wificondDeathEventHandler != null) {
            wificondDeathEventHandler.onDeath();
        }
    }
}
