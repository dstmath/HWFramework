package android.service.voice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.IVoiceInteractionService.Stub;
import com.android.internal.app.IVoiceInteractionManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;

public class VoiceInteractionService extends Service {
    static final int MSG_LAUNCH_VOICE_ASSIST_FROM_KEYGUARD = 4;
    static final int MSG_READY = 1;
    static final int MSG_SHUTDOWN = 2;
    static final int MSG_SOUND_MODELS_CHANGED = 3;
    public static final String SERVICE_INTERFACE = "android.service.voice.VoiceInteractionService";
    public static final String SERVICE_META_DATA = "android.voice_interaction";
    MyHandler mHandler;
    private AlwaysOnHotwordDetector mHotwordDetector;
    IVoiceInteractionService mInterface = new Stub() {
        public void ready() {
            VoiceInteractionService.this.mHandler.sendEmptyMessage(1);
        }

        public void shutdown() {
            VoiceInteractionService.this.mHandler.sendEmptyMessage(2);
        }

        public void soundModelsChanged() {
            VoiceInteractionService.this.mHandler.sendEmptyMessage(3);
        }

        public void launchVoiceAssistFromKeyguard() throws RemoteException {
            VoiceInteractionService.this.mHandler.sendEmptyMessage(4);
        }
    };
    private KeyphraseEnrollmentInfo mKeyphraseEnrollmentInfo;
    private final Object mLock = new Object();
    IVoiceInteractionManagerService mSystemService;

    class MyHandler extends Handler {
        MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VoiceInteractionService.this.onReady();
                    return;
                case 2:
                    VoiceInteractionService.this.onShutdownInternal();
                    return;
                case 3:
                    VoiceInteractionService.this.onSoundModelsChangedInternal();
                    return;
                case 4:
                    VoiceInteractionService.this.onLaunchVoiceAssistFromKeyguard();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    public void onLaunchVoiceAssistFromKeyguard() {
    }

    public static boolean isActiveService(Context context, ComponentName service) {
        String cur = Secure.getString(context.getContentResolver(), Secure.VOICE_INTERACTION_SERVICE);
        if (cur == null || cur.isEmpty()) {
            return false;
        }
        ComponentName curComp = ComponentName.unflattenFromString(cur);
        if (curComp == null) {
            return false;
        }
        return curComp.equals(service);
    }

    public void setDisabledShowContext(int flags) {
        try {
            this.mSystemService.setDisabledShowContext(flags);
        } catch (RemoteException e) {
        }
    }

    public int getDisabledShowContext() {
        try {
            return this.mSystemService.getDisabledShowContext();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void showSession(Bundle args, int flags) {
        if (this.mSystemService == null) {
            throw new IllegalStateException("Not available until onReady() is called");
        }
        try {
            this.mSystemService.showSession(this.mInterface, args, flags);
        } catch (RemoteException e) {
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mHandler = new MyHandler();
    }

    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mInterface.asBinder();
        }
        return null;
    }

    public void onReady() {
        this.mSystemService = IVoiceInteractionManagerService.Stub.asInterface(ServiceManager.getService("voiceinteraction"));
        this.mKeyphraseEnrollmentInfo = new KeyphraseEnrollmentInfo(getPackageManager());
    }

    private void onShutdownInternal() {
        onShutdown();
        safelyShutdownHotwordDetector();
    }

    public void onShutdown() {
    }

    private void onSoundModelsChangedInternal() {
        synchronized (this) {
            if (this.mHotwordDetector != null) {
                this.mHotwordDetector.onSoundModelsChanged();
            }
        }
    }

    public final AlwaysOnHotwordDetector createAlwaysOnHotwordDetector(String keyphrase, Locale locale, Callback callback) {
        if (this.mSystemService == null) {
            throw new IllegalStateException("Not available until onReady() is called");
        }
        synchronized (this.mLock) {
            safelyShutdownHotwordDetector();
            this.mHotwordDetector = new AlwaysOnHotwordDetector(keyphrase, locale, callback, this.mKeyphraseEnrollmentInfo, this.mInterface, this.mSystemService);
        }
        return this.mHotwordDetector;
    }

    protected final KeyphraseEnrollmentInfo getKeyphraseEnrollmentInfo() {
        return this.mKeyphraseEnrollmentInfo;
    }

    private void safelyShutdownHotwordDetector() {
        try {
            synchronized (this.mLock) {
                if (this.mHotwordDetector != null) {
                    this.mHotwordDetector.stopRecognition();
                    this.mHotwordDetector.invalidate();
                    this.mHotwordDetector = null;
                }
            }
        } catch (Exception e) {
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("VOICE INTERACTION");
        synchronized (this.mLock) {
            pw.println("  AlwaysOnHotwordDetector");
            if (this.mHotwordDetector == null) {
                pw.println("    NULL");
            } else {
                this.mHotwordDetector.dump("    ", pw);
            }
        }
    }
}
