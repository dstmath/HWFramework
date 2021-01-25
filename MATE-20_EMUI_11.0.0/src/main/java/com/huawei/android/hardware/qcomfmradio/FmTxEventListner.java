package com.huawei.android.hardware.qcomfmradio;

import android.util.Log;

/* access modifiers changed from: package-private */
public class FmTxEventListner {
    private static final String TAG = "FMTxEventListner";
    private final int EVENT_LISTEN = 1;
    private final int RADIO_DISABLED = 18;
    private final int READY_EVENT = 0;
    private final int TUNE_EVENT = 1;
    private final int TXRDSDAT_EVENT = 16;
    private final int TXRDSDONE_EVENT = 17;
    private byte[] buff = new byte[128];
    private Thread mThread;

    FmTxEventListner() {
    }

    public void startListner(final int fd, final FmTransmitterCallbacks cb) {
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
        this.mThread = new Thread() {
            /* class com.huawei.android.hardware.qcomfmradio.FmTxEventListner.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Log.d(FmTxEventListner.TAG, "Starting Tx Event listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Log.d(FmTxEventListner.TAG, "getBufferNative called");
                        int eventCount = FmReceiverJNI.getBufferNative(fd, FmTxEventListner.this.buff, 1);
                        Log.d(FmTxEventListner.TAG, "Received event. Count: " + eventCount);
                        for (int index = 0; index < eventCount; index++) {
                            Log.d(FmTxEventListner.TAG, "Received <" + ((int) FmTxEventListner.this.buff[index]) + ">");
                            byte b = FmTxEventListner.this.buff[index];
                            if (b == 0) {
                                Log.d(FmTxEventListner.TAG, "Got RADIO_ENABLED");
                                if (FmTransceiver.getFMPowerState() == 5) {
                                    FmTransceiver.setFMPowerState(2);
                                    Log.v(FmTxEventListner.TAG, "TxEvtList: CURRENT-STATE:FMTxStarting ---> NEW-STATE : FMTxOn");
                                    cb.FmTxEvRadioEnabled();
                                }
                            } else if (b != 1) {
                                switch (b) {
                                    case 16:
                                        Log.d(FmTxEventListner.TAG, "Got TXRDSDAT_EVENT");
                                        cb.FmTxEvRDSGroupsAvailable();
                                        break;
                                    case 17:
                                        Log.d(FmTxEventListner.TAG, "Got TXRDSDONE_EVENT");
                                        cb.FmTxEvContRDSGroupsComplete();
                                        break;
                                    case 18:
                                        Log.d(FmTxEventListner.TAG, "Got RADIO_DISABLED");
                                        if (FmTransceiver.getFMPowerState() == 6) {
                                            FmTransceiver.setFMPowerState(0);
                                            Log.v(FmTxEventListner.TAG, "TxEvtList:CURRENT-STATE :FMTurningOff ---> NEW-STATE: FMOff");
                                            FmTransceiver.release("/dev/radio0");
                                            cb.FmTxEvRadioDisabled();
                                            Thread.currentThread().interrupt();
                                            break;
                                        } else {
                                            Log.d(FmTxEventListner.TAG, "Unexpected RADIO_DISABLED recvd");
                                            cb.FmTxEvRadioReset();
                                            break;
                                        }
                                    default:
                                        Log.d(FmTxEventListner.TAG, "Unknown event");
                                        break;
                                }
                            } else {
                                Log.d(FmTxEventListner.TAG, "Got TUNE_EVENT");
                                int freq = FmReceiverJNI.getFreqNative(fd);
                                if (freq > 0) {
                                    cb.FmTxEvTuneStatusChange(freq);
                                } else {
                                    Log.e(FmTxEventListner.TAG, "get frqency cmd failed");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(FmTxEventListner.TAG, "RunningThread InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                }
                Log.d(FmTxEventListner.TAG, "Came out of the while loop");
            }
        };
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "Thread Stopped\n");
        Log.d(TAG, "stopping the Listener\n");
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
        Log.d(TAG, "Thread Stopped\n");
    }
}
