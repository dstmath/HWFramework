package android.media.soundtrigger;

import android.annotation.SystemApi;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.ISoundTriggerService;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

@SystemApi
public final class SoundTriggerDetector {
    private static final boolean DBG = false;
    private static final int MSG_AVAILABILITY_CHANGED = 1;
    private static final int MSG_DETECTION_ERROR = 3;
    private static final int MSG_DETECTION_PAUSE = 4;
    private static final int MSG_DETECTION_RESUME = 5;
    private static final int MSG_SOUND_TRIGGER_DETECTED = 2;
    public static final int RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS = 2;
    public static final int RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO = 1;
    public static final int RECOGNITION_FLAG_NONE = 0;
    private static final String TAG = "SoundTriggerDetector";
    /* access modifiers changed from: private */
    public final Callback mCallback;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final Object mLock = new Object();
    private final RecognitionCallback mRecognitionCallback;
    private final UUID mSoundModelId;
    private final ISoundTriggerService mSoundTriggerService;

    public static abstract class Callback {
        public abstract void onAvailabilityChanged(int i);

        public abstract void onDetected(EventPayload eventPayload);

        public abstract void onError();

        public abstract void onRecognitionPaused();

        public abstract void onRecognitionResumed();
    }

    public static class EventPayload {
        private final AudioFormat mAudioFormat;
        private final boolean mCaptureAvailable;
        private final int mCaptureSession;
        private final byte[] mData;
        private final boolean mTriggerAvailable;

        private EventPayload(boolean triggerAvailable, boolean captureAvailable, AudioFormat audioFormat, int captureSession, byte[] data) {
            this.mTriggerAvailable = triggerAvailable;
            this.mCaptureAvailable = captureAvailable;
            this.mCaptureSession = captureSession;
            this.mAudioFormat = audioFormat;
            this.mData = data;
        }

        public AudioFormat getCaptureAudioFormat() {
            return this.mAudioFormat;
        }

        public byte[] getTriggerAudio() {
            if (this.mTriggerAvailable) {
                return this.mData;
            }
            return null;
        }

        public byte[] getData() {
            if (!this.mTriggerAvailable) {
                return this.mData;
            }
            return null;
        }

        public Integer getCaptureSession() {
            if (this.mCaptureAvailable) {
                return Integer.valueOf(this.mCaptureSession);
            }
            return null;
        }
    }

    private class MyHandler extends Handler {
        MyHandler() {
        }

        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (SoundTriggerDetector.this.mCallback == null) {
                Slog.w(SoundTriggerDetector.TAG, "Received message: " + msg.what + " for NULL callback.");
                return;
            }
            switch (msg.what) {
                case 2:
                    SoundTriggerDetector.this.mCallback.onDetected((EventPayload) msg.obj);
                    break;
                case 3:
                    SoundTriggerDetector.this.mCallback.onError();
                    break;
                case 4:
                    SoundTriggerDetector.this.mCallback.onRecognitionPaused();
                    break;
                case 5:
                    SoundTriggerDetector.this.mCallback.onRecognitionResumed();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private class RecognitionCallback extends IRecognitionStatusCallback.Stub {
        private RecognitionCallback() {
        }

        public void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent event) {
            Slog.d(SoundTriggerDetector.TAG, "onGenericSoundTriggerDetected()" + event);
            Handler access$100 = SoundTriggerDetector.this.mHandler;
            EventPayload eventPayload = new EventPayload(event.triggerInData, event.captureAvailable, event.captureFormat, event.captureSession, event.data);
            Message.obtain(access$100, 2, eventPayload).sendToTarget();
        }

        public void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent event) {
            Slog.e(SoundTriggerDetector.TAG, "Ignoring onKeyphraseDetected() called for " + event);
        }

        public void onError(int status) {
            Slog.d(SoundTriggerDetector.TAG, "onError()" + status);
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(3);
        }

        public void onRecognitionPaused() {
            Slog.d(SoundTriggerDetector.TAG, "onRecognitionPaused()");
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(4);
        }

        public void onRecognitionResumed() {
            Slog.d(SoundTriggerDetector.TAG, "onRecognitionResumed()");
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(5);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RecognitionFlags {
    }

    SoundTriggerDetector(ISoundTriggerService soundTriggerService, UUID soundModelId, Callback callback, Handler handler) {
        this.mSoundTriggerService = soundTriggerService;
        this.mSoundModelId = soundModelId;
        this.mCallback = callback;
        if (handler == null) {
            this.mHandler = new MyHandler();
        } else {
            this.mHandler = new MyHandler(handler.getLooper());
        }
        this.mRecognitionCallback = new RecognitionCallback();
    }

    public boolean startRecognition(int recognitionFlags) {
        boolean z = true;
        try {
            if (this.mSoundTriggerService.startRecognition(new ParcelUuid(this.mSoundModelId), this.mRecognitionCallback, new SoundTrigger.RecognitionConfig((recognitionFlags & 1) != 0, (recognitionFlags & 2) != 0, null, null)) != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean stopRecognition() {
        boolean z = false;
        try {
            if (this.mSoundTriggerService.stopRecognition(new ParcelUuid(this.mSoundModelId), this.mRecognitionCallback) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
        }
    }
}
