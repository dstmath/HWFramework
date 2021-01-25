package android.service.voice;

import android.annotation.UnsupportedAppUsage;
import android.content.Intent;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.hardware.soundtrigger.KeyphraseMetadata;
import android.hardware.soundtrigger.SoundTrigger;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractionManagerService;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class AlwaysOnHotwordDetector {
    static final boolean DBG = false;
    public static final int MANAGE_ACTION_ENROLL = 0;
    public static final int MANAGE_ACTION_RE_ENROLL = 1;
    public static final int MANAGE_ACTION_UN_ENROLL = 2;
    private static final int MSG_AVAILABILITY_CHANGED = 1;
    private static final int MSG_DETECTION_ERROR = 3;
    private static final int MSG_DETECTION_PAUSE = 4;
    private static final int MSG_DETECTION_RESUME = 5;
    private static final int MSG_HOTWORD_DETECTED = 2;
    public static final int RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS = 2;
    public static final int RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO = 1;
    public static final int RECOGNITION_FLAG_NONE = 0;
    public static final int RECOGNITION_MODE_USER_IDENTIFICATION = 2;
    public static final int RECOGNITION_MODE_VOICE_TRIGGER = 1;
    public static final int STATE_HARDWARE_UNAVAILABLE = -2;
    private static final int STATE_INVALID = -3;
    public static final int STATE_KEYPHRASE_ENROLLED = 2;
    public static final int STATE_KEYPHRASE_UNENROLLED = 1;
    public static final int STATE_KEYPHRASE_UNSUPPORTED = -1;
    private static final int STATE_NOT_READY = 0;
    private static final int STATUS_ERROR = Integer.MIN_VALUE;
    private static final int STATUS_OK = 0;
    static final String TAG = "AlwaysOnHotwordDetector";
    private int mAvailability = 0;
    private final Callback mExternalCallback;
    private final Handler mHandler;
    private final SoundTriggerListener mInternalCallback;
    private final KeyphraseEnrollmentInfo mKeyphraseEnrollmentInfo;
    private final KeyphraseMetadata mKeyphraseMetadata;
    private final Locale mLocale;
    private final Object mLock = new Object();
    private final IVoiceInteractionManagerService mModelManagementService;
    private final String mText;
    private final IVoiceInteractionService mVoiceInteractionService;

    public static abstract class Callback {
        public abstract void onAvailabilityChanged(int i);

        public abstract void onDetected(EventPayload eventPayload);

        public abstract void onError();

        public abstract void onRecognitionPaused();

        public abstract void onRecognitionResumed();
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ManageActions {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RecognitionFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RecognitionModes {
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

        @UnsupportedAppUsage
        public Integer getCaptureSession() {
            if (this.mCaptureAvailable) {
                return Integer.valueOf(this.mCaptureSession);
            }
            return null;
        }
    }

    public AlwaysOnHotwordDetector(String text, Locale locale, Callback callback, KeyphraseEnrollmentInfo keyphraseEnrollmentInfo, IVoiceInteractionService voiceInteractionService, IVoiceInteractionManagerService modelManagementService) {
        this.mText = text;
        this.mLocale = locale;
        this.mKeyphraseEnrollmentInfo = keyphraseEnrollmentInfo;
        this.mKeyphraseMetadata = this.mKeyphraseEnrollmentInfo.getKeyphraseMetadata(text, locale);
        this.mExternalCallback = callback;
        this.mHandler = new MyHandler();
        this.mInternalCallback = new SoundTriggerListener(this.mHandler);
        this.mVoiceInteractionService = voiceInteractionService;
        this.mModelManagementService = modelManagementService;
        new RefreshAvailabiltyTask().execute(new Void[0]);
    }

    public int getSupportedRecognitionModes() {
        int supportedRecognitionModesLocked;
        synchronized (this.mLock) {
            supportedRecognitionModesLocked = getSupportedRecognitionModesLocked();
        }
        return supportedRecognitionModesLocked;
    }

    private int getSupportedRecognitionModesLocked() {
        int i = this.mAvailability;
        if (i == -3) {
            throw new IllegalStateException("getSupportedRecognitionModes called on an invalid detector");
        } else if (i == 2 || i == 1) {
            return this.mKeyphraseMetadata.recognitionModeFlags;
        } else {
            throw new UnsupportedOperationException("Getting supported recognition modes for the keyphrase is not supported");
        }
    }

    public boolean startRecognition(int recognitionFlags) {
        boolean z;
        synchronized (this.mLock) {
            if (this.mAvailability == -3) {
                throw new IllegalStateException("startRecognition called on an invalid detector");
            } else if (this.mAvailability == 2) {
                z = startRecognitionLocked(recognitionFlags) == 0;
            } else {
                throw new UnsupportedOperationException("Recognition for the given keyphrase is not supported");
            }
        }
        return z;
    }

    public boolean stopRecognition() {
        boolean z;
        synchronized (this.mLock) {
            if (this.mAvailability == -3) {
                throw new IllegalStateException("stopRecognition called on an invalid detector");
            } else if (this.mAvailability == 2) {
                z = stopRecognitionLocked() == 0;
            } else {
                throw new UnsupportedOperationException("Recognition for the given keyphrase is not supported");
            }
        }
        return z;
    }

    public Intent createEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(0);
        }
        return manageIntentLocked;
    }

    public Intent createUnEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(2);
        }
        return manageIntentLocked;
    }

    public Intent createReEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(1);
        }
        return manageIntentLocked;
    }

    private Intent getManageIntentLocked(int action) {
        int i = this.mAvailability;
        if (i == -3) {
            throw new IllegalStateException("getManageIntent called on an invalid detector");
        } else if (i == 2 || i == 1) {
            return this.mKeyphraseEnrollmentInfo.getManageKeyphraseIntent(action, this.mText, this.mLocale);
        } else {
            throw new UnsupportedOperationException("Managing the given keyphrase is not supported");
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidate() {
        synchronized (this.mLock) {
            this.mAvailability = -3;
            notifyStateChangedLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void onSoundModelsChanged() {
        synchronized (this.mLock) {
            if (!(this.mAvailability == -3 || this.mAvailability == -2)) {
                if (this.mAvailability != -1) {
                    stopRecognitionLocked();
                    new RefreshAvailabiltyTask().execute(new Void[0]);
                    return;
                }
            }
            Slog.w(TAG, "Received onSoundModelsChanged for an unsupported keyphrase/config");
        }
    }

    private int startRecognitionLocked(int recognitionFlags) {
        boolean allowMultipleTriggers = true;
        SoundTrigger.KeyphraseRecognitionExtra[] recognitionExtra = {new SoundTrigger.KeyphraseRecognitionExtra(this.mKeyphraseMetadata.id, this.mKeyphraseMetadata.recognitionModeFlags, 0, new SoundTrigger.ConfidenceLevel[0])};
        boolean captureTriggerAudio = (recognitionFlags & 1) != 0;
        if ((recognitionFlags & 2) == 0) {
            allowMultipleTriggers = false;
        }
        int code = Integer.MIN_VALUE;
        try {
            code = this.mModelManagementService.startRecognition(this.mVoiceInteractionService, this.mKeyphraseMetadata.id, this.mLocale.toLanguageTag(), this.mInternalCallback, new SoundTrigger.RecognitionConfig(captureTriggerAudio, allowMultipleTriggers, recognitionExtra, null));
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in startRecognition!", e);
        }
        if (code != 0) {
            Slog.w(TAG, "startRecognition() failed with error code " + code);
        }
        return code;
    }

    private int stopRecognitionLocked() {
        int code = Integer.MIN_VALUE;
        try {
            code = this.mModelManagementService.stopRecognition(this.mVoiceInteractionService, this.mKeyphraseMetadata.id, this.mInternalCallback);
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in stopRecognition!", e);
        }
        if (code != 0) {
            Slog.w(TAG, "stopRecognition() failed with error code " + code);
        }
        return code;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStateChangedLocked() {
        Message message = Message.obtain(this.mHandler, 1);
        message.arg1 = this.mAvailability;
        message.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public static final class SoundTriggerListener extends IRecognitionStatusCallback.Stub {
        private final Handler mHandler;

        public SoundTriggerListener(Handler handler) {
            this.mHandler = handler;
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent event) {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onDetected");
            Message.obtain(this.mHandler, 2, new EventPayload(event.triggerInData, event.captureAvailable, event.captureFormat, event.captureSession, event.data)).sendToTarget();
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent event) {
            Slog.w(AlwaysOnHotwordDetector.TAG, "Generic sound trigger event detected at AOHD: " + event);
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onError(int status) {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onError: " + status);
            this.mHandler.sendEmptyMessage(3);
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onRecognitionPaused() {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onRecognitionPaused");
            this.mHandler.sendEmptyMessage(4);
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onRecognitionResumed() {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onRecognitionResumed");
            this.mHandler.sendEmptyMessage(5);
        }
    }

    class MyHandler extends Handler {
        MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                if (AlwaysOnHotwordDetector.this.mAvailability == -3) {
                    Slog.w(AlwaysOnHotwordDetector.TAG, "Received message: " + msg.what + " for an invalid detector");
                    return;
                }
            }
            int i = msg.what;
            if (i == 1) {
                AlwaysOnHotwordDetector.this.mExternalCallback.onAvailabilityChanged(msg.arg1);
            } else if (i == 2) {
                AlwaysOnHotwordDetector.this.mExternalCallback.onDetected((EventPayload) msg.obj);
            } else if (i == 3) {
                AlwaysOnHotwordDetector.this.mExternalCallback.onError();
            } else if (i == 4) {
                AlwaysOnHotwordDetector.this.mExternalCallback.onRecognitionPaused();
            } else if (i != 5) {
                super.handleMessage(msg);
            } else {
                AlwaysOnHotwordDetector.this.mExternalCallback.onRecognitionResumed();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class RefreshAvailabiltyTask extends AsyncTask<Void, Void, Void> {
        RefreshAvailabiltyTask() {
        }

        public Void doInBackground(Void... params) {
            int availability = internalGetInitialAvailability();
            if (availability == 0 || availability == 1 || availability == 2) {
                if (!internalGetIsEnrolled(AlwaysOnHotwordDetector.this.mKeyphraseMetadata.id, AlwaysOnHotwordDetector.this.mLocale)) {
                    availability = 1;
                } else {
                    availability = 2;
                }
            }
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                AlwaysOnHotwordDetector.this.mAvailability = availability;
                AlwaysOnHotwordDetector.this.notifyStateChangedLocked();
            }
            return null;
        }

        private int internalGetInitialAvailability() {
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                if (AlwaysOnHotwordDetector.this.mAvailability == -3) {
                    return -3;
                }
            }
            SoundTrigger.ModuleProperties dspModuleProperties = null;
            try {
                dspModuleProperties = AlwaysOnHotwordDetector.this.mModelManagementService.getDspModuleProperties(AlwaysOnHotwordDetector.this.mVoiceInteractionService);
            } catch (RemoteException e) {
                Slog.w(AlwaysOnHotwordDetector.TAG, "RemoteException in getDspProperties!", e);
            }
            if (dspModuleProperties == null) {
                return -2;
            }
            if (AlwaysOnHotwordDetector.this.mKeyphraseMetadata == null) {
                return -1;
            }
            return 0;
        }

        private boolean internalGetIsEnrolled(int keyphraseId, Locale locale) {
            try {
                return AlwaysOnHotwordDetector.this.mModelManagementService.isEnrolledForKeyphrase(AlwaysOnHotwordDetector.this.mVoiceInteractionService, keyphraseId, locale.toLanguageTag());
            } catch (RemoteException e) {
                Slog.w(AlwaysOnHotwordDetector.TAG, "RemoteException in listRegisteredKeyphraseSoundModels!", e);
                return false;
            }
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            pw.print(prefix);
            pw.print("Text=");
            pw.println(this.mText);
            pw.print(prefix);
            pw.print("Locale=");
            pw.println(this.mLocale);
            pw.print(prefix);
            pw.print("Availability=");
            pw.println(this.mAvailability);
            pw.print(prefix);
            pw.print("KeyphraseMetadata=");
            pw.println(this.mKeyphraseMetadata);
            pw.print(prefix);
            pw.print("EnrollmentInfo=");
            pw.println(this.mKeyphraseEnrollmentInfo);
        }
    }
}
