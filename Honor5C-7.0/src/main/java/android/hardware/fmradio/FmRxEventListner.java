package android.hardware.fmradio;

import android.security.keymaster.KeymasterDefs;
import android.service.notification.NotificationRankerService;
import android.service.voice.VoiceInteractionSession;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
import java.util.Arrays;

class FmRxEventListner {
    private static final String TAG = "FMRadio";
    private final int EVENT_LISTEN;
    private final int STD_BUF_SIZE;
    private Thread mThread;

    /* renamed from: android.hardware.fmradio.FmRxEventListner.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ FmRxEvCallbacks val$cb;
        final /* synthetic */ int val$fd;

        AnonymousClass1(int val$fd, FmRxEvCallbacks val$cb) {
            this.val$fd = val$fd;
            this.val$cb = val$cb;
        }

        public void run() {
            byte[] buff = new byte[KeymasterDefs.KM_ALGORITHM_HMAC];
            Log.d(FmRxEventListner.TAG, "Starting listener " + this.val$fd);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Arrays.fill(buff, (byte) 0);
                    int eventCount = FmReceiverJNI.getBufferNative(this.val$fd, buff, 1);
                    Log.d(FmRxEventListner.TAG, "Received event. Count: " + eventCount);
                    for (int index = 0; index < eventCount; index++) {
                        Log.d(FmRxEventListner.TAG, "Received <" + buff[index] + ">");
                        switch (buff[index]) {
                            case TextToSpeech.SUCCESS /*0*/:
                                Log.d(FmRxEventListner.TAG, "Got READY_EVENT");
                                this.val$cb.FmRxEvEnableReceiver();
                                break;
                            case AudioState.ROUTE_EARPIECE /*1*/:
                                Log.d(FmRxEventListner.TAG, "Got TUNE_EVENT");
                                this.val$cb.FmRxEvRadioTuneStatus(FmReceiverJNI.getFreqNative(this.val$fd));
                                break;
                            case AudioState.ROUTE_BLUETOOTH /*2*/:
                                Log.d(FmRxEventListner.TAG, "Got SEEK_COMPLETE_EVENT");
                                this.val$cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(this.val$fd));
                                break;
                            case Engine.DEFAULT_STREAM /*3*/:
                                Log.d(FmRxEventListner.TAG, "Got SCAN_NEXT_EVENT");
                                this.val$cb.FmRxEvSearchInProgress();
                                break;
                            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                                Log.d(FmRxEventListner.TAG, "Got RAW_RDS_EVENT");
                                this.val$cb.FmRxEvRdsGroupData();
                                break;
                            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                                Log.d(FmRxEventListner.TAG, "Got RT_EVENT");
                                this.val$cb.FmRxEvRdsRtInfo();
                                break;
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                                Log.d(FmRxEventListner.TAG, "Got PS_EVENT");
                                this.val$cb.FmRxEvRdsPsInfo();
                                break;
                            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                                Log.d(FmRxEventListner.TAG, "Got ERROR_EVENT");
                                break;
                            case AudioState.ROUTE_SPEAKER /*8*/:
                                Log.d(FmRxEventListner.TAG, "Got BELOW_TH_EVENT");
                                this.val$cb.FmRxEvServiceAvailable(false);
                                break;
                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                                Log.d(FmRxEventListner.TAG, "Got ABOVE_TH_EVENT");
                                this.val$cb.FmRxEvServiceAvailable(true);
                                break;
                            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                                Log.d(FmRxEventListner.TAG, "Got STEREO_EVENT");
                                this.val$cb.FmRxEvStereoStatus(true);
                                break;
                            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                                Log.d(FmRxEventListner.TAG, "Got MONO_EVENT");
                                this.val$cb.FmRxEvStereoStatus(false);
                                break;
                            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                                Log.d(FmRxEventListner.TAG, "Got RDS_AVAL_EVENT");
                                this.val$cb.FmRxEvRdsLockStatus(true);
                                break;
                            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                                Log.d(FmRxEventListner.TAG, "Got RDS_NOT_AVAL_EVENT");
                                this.val$cb.FmRxEvRdsLockStatus(false);
                                break;
                            case NotificationRankerService.REASON_PACKAGE_SUSPENDED /*14*/:
                                Log.d(FmRxEventListner.TAG, "Got NEW_SRCH_LIST");
                                this.val$cb.FmRxEvSearchListComplete();
                                break;
                            case NotificationRankerService.REASON_PROFILE_TURNED_OFF /*15*/:
                                Log.d(FmRxEventListner.TAG, "Got NEW_AF_LIST");
                                this.val$cb.FmRxEvRdsAfInfo();
                                break;
                            case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                Log.d(FmRxEventListner.TAG, "Got SIGNAL_UPDATE_EVENT");
                                this.val$cb.FmRxEvSignalUpdate();
                                break;
                            default:
                                Log.d(FmRxEventListner.TAG, "Unknown event");
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(FmRxEventListner.TAG, "RunningThread InterruptedException");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private enum FmRxEvents {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fmradio.FmRxEventListner.FmRxEvents.<clinit>():void");
        }
    }

    FmRxEventListner() {
        this.EVENT_LISTEN = 1;
        this.STD_BUF_SIZE = KeymasterDefs.KM_ALGORITHM_HMAC;
    }

    public void startListner(int fd, FmRxEvCallbacks cb) {
        this.mThread = new AnonymousClass1(fd, cb);
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "stopping the Listener\n");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
    }
}
