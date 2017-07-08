package com.huawei.android.hardware.fmradio;

import android.util.Log;
import com.huawei.android.hardware.fmradio.IFmEventCallback.Stub;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.telephony.HuaweiTelephonyManager;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;

class FmRxEventListner {
    private static final String TAG = "FMRadio";
    FmRxEvCallbacks mCallback;
    private IFmEventCallback mEventCallback;

    private enum FmRxEvents {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void");
        }
    }

    FmRxEventListner() {
        this.mCallback = null;
        this.mEventCallback = new Stub() {
            public void onEventCallback(int event, int param1, int param2) {
                switch (event) {
                    case ViewDragHelper.STATE_IDLE /*0*/:
                        Log.d(FmRxEventListner.TAG, "Got READY_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvEnableReceiver();
                    case ViewDragHelper.STATE_DRAGGING /*1*/:
                        Log.d(FmRxEventListner.TAG, "Got TUNE_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRadioTuneStatus(param1);
                    case ViewDragHelper.STATE_SETTLING /*2*/:
                        Log.d(FmRxEventListner.TAG, "Got SEEK_COMPLETE_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvSearchComplete(param1);
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        Log.d(FmRxEventListner.TAG, "Got SCAN_NEXT_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvSearchInProgress();
                    case ViewDragHelper.EDGE_TOP /*4*/:
                        Log.d(FmRxEventListner.TAG, "Got RAW_RDS_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRdsGroupData();
                    case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                        Log.d(FmRxEventListner.TAG, "Got RT_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRdsRtInfo();
                    case Dex.DIALOG_BODY_View /*6*/:
                        Log.d(FmRxEventListner.TAG, "Got PS_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRdsPsInfo();
                    case MetricConstant.CALL_METRIC_ID /*7*/:
                        Log.d(FmRxEventListner.TAG, "Got ERROR_EVENT");
                    case ViewDragHelper.EDGE_BOTTOM /*8*/:
                        Log.d(FmRxEventListner.TAG, "Got BELOW_TH_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvServiceAvailable(false);
                    case MetricConstant.APR_STATISTICS_METRIC_ID /*9*/:
                        Log.d(FmRxEventListner.TAG, "Got ABOVE_TH_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvServiceAvailable(true);
                    case HuaweiTelephonyManager.SINGLE_MODE_SIM_CARD /*10*/:
                        Log.d(FmRxEventListner.TAG, "Got STEREO_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvStereoStatus(true);
                    case MotionType.TYPE_COUNT /*11*/:
                        Log.d(FmRxEventListner.TAG, "Got MONO_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvStereoStatus(false);
                    case MetricConstant.LOG_TRACK_METRIC_ID /*12*/:
                        Log.d(FmRxEventListner.TAG, "Got RDS_AVAL_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRdsLockStatus(true);
                    case MetricConstant.AUDIO_METRIC_ID /*13*/:
                        Log.d(FmRxEventListner.TAG, "Got RDS_NOT_AVAL_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvRdsLockStatus(false);
                    case MetricConstant.GPS_METRIC_ID /*14*/:
                        Log.d(FmRxEventListner.TAG, "Got NEW_SRCH_LIST");
                        FmRxEventListner.this.mCallback.FmRxEvSearchListComplete();
                    case ViewDragHelper.EDGE_ALL /*15*/:
                        Log.d(FmRxEventListner.TAG, "Got NEW_AF_LIST");
                        FmRxEventListner.this.mCallback.FmRxEvRdsAfInfo();
                    case MetricConstant.LEVEL_B /*16*/:
                        Log.d(FmRxEventListner.TAG, "Got SIGNAL_UPDATE_EVENT");
                        FmRxEventListner.this.mCallback.FmRxEvSignalUpdate();
                    default:
                        Log.d(FmRxEventListner.TAG, "Unknown event");
                }
            }
        };
    }

    public void startListner(int fd, FmRxEvCallbacks cb) {
        this.mCallback = cb;
        FmReceiverWrapper.startListner(fd, this.mEventCallback);
    }

    public void stopListener() {
        FmReceiverWrapper.stopListner();
    }
}
