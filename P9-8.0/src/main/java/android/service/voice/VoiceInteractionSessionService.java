package android.service.voice;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.voice.IVoiceInteractionSessionService.Stub;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class VoiceInteractionSessionService extends Service {
    static final int MSG_NEW_SESSION = 1;
    HandlerCaller mHandlerCaller;
    final Callback mHandlerCallerCallback = new Callback() {
        public void executeMessage(Message msg) {
            SomeArgs args = msg.obj;
            switch (msg.what) {
                case 1:
                    VoiceInteractionSessionService.this.doNewSession((IBinder) args.arg1, (Bundle) args.arg2, args.argi1);
                    return;
                default:
                    return;
            }
        }
    };
    IVoiceInteractionSessionService mInterface = new Stub() {
        public void newSession(IBinder token, Bundle args, int startFlags) {
            VoiceInteractionSessionService.this.mHandlerCaller.sendMessage(VoiceInteractionSessionService.this.mHandlerCaller.obtainMessageIOO(1, startFlags, token, args));
        }
    };
    VoiceInteractionSession mSession;
    IVoiceInteractionManagerService mSystemService;

    public abstract VoiceInteractionSession onNewSession(Bundle bundle);

    public void onCreate() {
        super.onCreate();
        this.mSystemService = IVoiceInteractionManagerService.Stub.asInterface(ServiceManager.getService("voiceinteraction"));
        this.mHandlerCaller = new HandlerCaller(this, Looper.myLooper(), this.mHandlerCallerCallback, true);
    }

    public IBinder onBind(Intent intent) {
        return this.mInterface.asBinder();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSession != null) {
            this.mSession.onConfigurationChanged(newConfig);
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.mSession != null) {
            this.mSession.onLowMemory();
        }
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (this.mSession != null) {
            this.mSession.onTrimMemory(level);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (this.mSession == null) {
            writer.println("(no active session)");
            return;
        }
        writer.println("VoiceInteractionSession:");
        this.mSession.dump("  ", fd, writer, args);
    }

    void doNewSession(IBinder token, Bundle args, int startFlags) {
        if (this.mSession != null) {
            this.mSession.doDestroy();
            this.mSession = null;
        }
        this.mSession = onNewSession(args);
        try {
            this.mSystemService.deliverNewSession(token, this.mSession.mSession, this.mSession.mInteractor);
            this.mSession.doCreate(this.mSystemService, token);
        } catch (RemoteException e) {
        }
    }
}
