package android.hardware.fmradio;

import android.rms.HwSysResource;
import android.security.keymaster.KeymasterDefs;
import android.service.voice.VoiceInteractionSession;
import android.speech.tts.TextToSpeech;
import android.telecom.AudioState;
import android.util.Log;

class FmTxEventListner {
    private static final String TAG = "FMTxEventListner";
    private final int EVENT_LISTEN;
    private final int RADIO_DISABLED;
    private final int READY_EVENT;
    private final int TUNE_EVENT;
    private final int TXRDSDAT_EVENT;
    private final int TXRDSDONE_EVENT;
    private Thread mThread;

    /* renamed from: android.hardware.fmradio.FmTxEventListner.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ FmTransmitterCallbacks val$cb;
        final /* synthetic */ int val$fd;

        AnonymousClass1(int val$fd, FmTransmitterCallbacks val$cb) {
            this.val$fd = val$fd;
            this.val$cb = val$cb;
        }

        public void run() {
            Log.d(FmTxEventListner.TAG, "Starting Tx Event listener " + this.val$fd);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] buff = new byte[KeymasterDefs.KM_ALGORITHM_HMAC];
                    Log.d(FmTxEventListner.TAG, "getBufferNative called");
                    int eventCount = FmReceiverJNI.getBufferNative(this.val$fd, buff, 1);
                    Log.d(FmTxEventListner.TAG, "Received event. Count: " + eventCount);
                    for (int index = 0; index < eventCount; index++) {
                        Log.d(FmTxEventListner.TAG, "Received <" + buff[index] + ">");
                        switch (buff[index]) {
                            case TextToSpeech.SUCCESS /*0*/:
                                Log.d(FmTxEventListner.TAG, "Got RADIO_ENABLED");
                                break;
                            case AudioState.ROUTE_EARPIECE /*1*/:
                                Log.d(FmTxEventListner.TAG, "Got TUNE_EVENT");
                                this.val$cb.onTuneStatusChange(FmReceiverJNI.getFreqNative(this.val$fd));
                                break;
                            case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                Log.d(FmTxEventListner.TAG, "Got TXRDSDAT_EVENT");
                                this.val$cb.onRDSGroupsAvailable();
                                break;
                            case HwSysResource.CURSOR /*17*/:
                                Log.d(FmTxEventListner.TAG, "Got TXRDSDONE_EVENT");
                                this.val$cb.onContRDSGroupsComplete();
                                break;
                            case HwSysResource.APPSERVICE /*18*/:
                                Log.d(FmTxEventListner.TAG, "Got RADIO_DISABLED");
                                FmTransceiver.release("/dev/radio0");
                                Thread.currentThread().interrupt();
                                break;
                            default:
                                Log.d(FmTxEventListner.TAG, "Unknown event");
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(FmTxEventListner.TAG, "RunningThread InterruptedException");
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(FmTxEventListner.TAG, "Came out of the while loop");
        }
    }

    FmTxEventListner() {
        this.EVENT_LISTEN = 1;
        this.TUNE_EVENT = 1;
        this.TXRDSDAT_EVENT = 16;
        this.TXRDSDONE_EVENT = 17;
        this.RADIO_DISABLED = 18;
        this.READY_EVENT = 0;
    }

    public void startListner(int fd, FmTransmitterCallbacks cb) {
        this.mThread = new AnonymousClass1(fd, cb);
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "Thread Stopped\n");
        Log.d(TAG, "stopping the Listener\n");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
        Log.d(TAG, "Thread Stopped\n");
    }
}
