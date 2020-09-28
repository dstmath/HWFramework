package com.huawei.android.hardware.qcomfmradio;

import android.mtp.HwMtpConstants;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmReceiver;
import com.huawei.android.hardware.mtkfmradio.MtkFmReceiver;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public class FmRxEventListner {
    private static final String TAG = "FMRadio";
    private final int EVENT_LISTEN = 1;
    private final int STD_BUF_SIZE = MtkFmReceiver.RDS_EVENT_AF_LIST;
    private Thread mThread;

    private enum FmRxEvents {
        READY_EVENT,
        TUNE_EVENT,
        SEEK_COMPLETE_EVENT,
        SCAN_NEXT_EVENT,
        RAW_RDS_EVENT,
        RT_EVENT,
        PS_EVENT,
        ERROR_EVENT,
        BELOW_TH_EVENT,
        ABOVE_TH_EVENT,
        STEREO_EVENT,
        MONO_EVENT,
        RDS_AVAL_EVENT,
        RDS_NOT_AVAL_EVENT,
        TAVARUA_EVT_NEW_SRCH_LIST,
        TAVARUA_EVT_NEW_AF_LIST
    }

    FmRxEventListner() {
    }

    public void startListner(final int fd, final QcomFmRxEvCallbacksAdaptor cb) {
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
        this.mThread = new Thread() {
            /* class com.huawei.android.hardware.qcomfmradio.FmRxEventListner.AnonymousClass1 */

            public void run() {
                byte[] buff = new byte[MtkFmReceiver.RDS_EVENT_AF_LIST];
                Log.d(FmRxEventListner.TAG, "Starting listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Arrays.fill(buff, (byte) 0);
                        int eventCount = FmReceiverJNI.getBufferNative(fd, buff, 1);
                        if (eventCount >= 0) {
                            Log.d(FmRxEventListner.TAG, "Received event. Count: " + eventCount);
                        }
                        for (int index = 0; index < eventCount; index++) {
                            Log.d(FmRxEventListner.TAG, "Received <" + ((int) buff[index]) + ">");
                            switch (buff[index]) {
                                case 0:
                                    Log.d(FmRxEventListner.TAG, "Got READY_EVENT");
                                    if (FmTransceiver.getFMPowerState() != 4) {
                                        if (FmTransceiver.getFMPowerState() == 6) {
                                            FmTransceiver.setFMPowerState(0);
                                            Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
                                            FmTransceiver.release("/dev/radio0");
                                            cb.FmRxEvDisableReceiver();
                                            Thread.currentThread().interrupt();
                                            break;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        FmTransceiver.setFMPowerState(1);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMRxOn");
                                        cb.FmRxEvEnableReceiver();
                                        FmReceiverJNI.configurePerformanceParams(fd);
                                        break;
                                    }
                                case 1:
                                    Log.d(FmRxEventListner.TAG, "Got TUNE_EVENT");
                                    int freq = FmReceiverJNI.getFreqNative(fd);
                                    int state = QcomFmReceiver.getSearchState();
                                    if (state != 0) {
                                        if (state != 4) {
                                            if (freq > 0) {
                                                cb.FmRxEvRadioTuneStatus(freq);
                                            } else {
                                                Log.e(FmRxEventListner.TAG, "get frequency command failed");
                                            }
                                            break;
                                        } else {
                                            Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                            Log.v(FmRxEventListner.TAG, "Aborting on-going search command...");
                                        }
                                    }
                                    Log.v(FmRxEventListner.TAG, "Current state is " + state);
                                    QcomFmReceiver.setSearchState(3);
                                    Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                    cb.FmRxEvSearchComplete(freq);
                                case 2:
                                    Log.d(FmRxEventListner.TAG, "Got SEEK_COMPLETE_EVENT");
                                    int state2 = QcomFmReceiver.getSearchState();
                                    if (state2 == 1) {
                                        Log.v(FmRxEventListner.TAG, "Current state is " + state2);
                                        QcomFmReceiver.setSearchState(3);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE :FMRxOn");
                                        cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(fd));
                                    } else if (state2 == 4) {
                                        Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                        Log.v(FmRxEventListner.TAG, "Aborting on-going search command...");
                                        QcomFmReceiver.setSearchState(3);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                        cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(fd));
                                    }
                                    break;
                                case 3:
                                    Log.d(FmRxEventListner.TAG, "Got SCAN_NEXT_EVENT");
                                    cb.FmRxEvSearchInProgress();
                                    break;
                                case 4:
                                    Log.d(FmRxEventListner.TAG, "Got RAW_RDS_EVENT");
                                    cb.FmRxEvRdsGroupData();
                                    break;
                                case 5:
                                    Log.d(FmRxEventListner.TAG, "Got RT_EVENT");
                                    cb.FmRxEvRdsRtInfo();
                                    break;
                                case 6:
                                    Log.d(FmRxEventListner.TAG, "Got PS_EVENT");
                                    cb.FmRxEvRdsPsInfo();
                                    break;
                                case 7:
                                    Log.d(FmRxEventListner.TAG, "Got ERROR_EVENT");
                                    break;
                                case 8:
                                    Log.d(FmRxEventListner.TAG, "Got BELOW_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(false);
                                    break;
                                case 9:
                                    Log.d(FmRxEventListner.TAG, "Got ABOVE_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(true);
                                    break;
                                case HwMtpConstants.TYPE_UINT128:
                                    Log.d(FmRxEventListner.TAG, "Got STEREO_EVENT");
                                    cb.FmRxEvStereoStatus(true);
                                    break;
                                case 11:
                                    Log.d(FmRxEventListner.TAG, "Got MONO_EVENT");
                                    cb.FmRxEvStereoStatus(false);
                                    break;
                                case FmReceiver.FM_RX_SRCHLIST_MAX_STATIONS:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(true);
                                    break;
                                case 13:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_NOT_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(false);
                                    break;
                                case 14:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_SRCH_LIST");
                                    int state3 = QcomFmReceiver.getSearchState();
                                    if (state3 == 2) {
                                        Log.v(FmRxEventListner.TAG, "FmRxEventListener: Current state is AUTO_PRESET_INPROGRESS");
                                        QcomFmReceiver.setSearchState(3);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                        cb.FmRxEvSearchListComplete();
                                    } else if (state3 == 4) {
                                        Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                        Log.v(FmRxEventListner.TAG, "Aborting on-going SearchList command...");
                                        QcomFmReceiver.setSearchState(3);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                        cb.FmRxEvSearchCancelled();
                                    }
                                    break;
                                case 15:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_AF_LIST");
                                    cb.FmRxEvRdsAfInfo();
                                    break;
                                case 16:
                                case 17:
                                default:
                                    Log.d(FmRxEventListner.TAG, "Unknown event");
                                    break;
                                case 18:
                                    Log.d(FmRxEventListner.TAG, "Got RADIO_DISABLED");
                                    if (FmTransceiver.getFMPowerState() == 6) {
                                        FmTransceiver.release("/dev/radio0");
                                        FmTransceiver.setFMPowerState(0);
                                        cb.FmRxEvDisableReceiver();
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
                                        Thread.currentThread().interrupt();
                                        break;
                                    } else {
                                        Log.d(FmRxEventListner.TAG, "Unexpected RADIO_DISABLED recvd");
                                        FmTransceiver.release("/dev/radio0");
                                        cb.FmRxEvRadioReset();
                                        FmTransceiver.setFMPowerState(0);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMOff");
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                case 19:
                                    FmTransceiver.setRDSGrpMask(0);
                                    break;
                                case 20:
                                    Log.d(FmRxEventListner.TAG, "got RT plus event");
                                    cb.FmRxEvRTPlus();
                                    break;
                                case 21:
                                    Log.d(FmRxEventListner.TAG, "got eRT event");
                                    cb.FmRxEvERTInfo();
                                    break;
                                case 22:
                                    Log.d(FmRxEventListner.TAG, "got IRIS_EVT_SPUR_TBL event");
                                    QcomFmReceiver.getSpurTableData();
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Log.d(FmRxEventListner.TAG, "RunningThread InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "stopping the Listener\n");
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
    }
}
