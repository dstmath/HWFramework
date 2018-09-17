package com.android.location.provider;

import android.hardware.location.IActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareWatcher.Stub;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

@Deprecated
public class ActivityRecognitionProviderWatcher {
    private static final String TAG = "ActivityRecognitionProviderWatcher";
    private static ActivityRecognitionProviderWatcher sWatcher;
    private static final Object sWatcherLock = null;
    private ActivityRecognitionProvider mActivityRecognitionProvider;
    private Stub mWatcherStub;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void");
    }

    private ActivityRecognitionProviderWatcher() {
        this.mWatcherStub = new Stub() {
            public void onInstanceChanged(IActivityRecognitionHardware instance) {
                int callingUid = Binder.getCallingUid();
                if (callingUid != 1000) {
                    Log.d(ActivityRecognitionProviderWatcher.TAG, "Ignoring calls from non-system server. Uid: " + callingUid);
                    return;
                }
                try {
                    ActivityRecognitionProviderWatcher.this.mActivityRecognitionProvider = new ActivityRecognitionProvider(instance);
                } catch (RemoteException e) {
                    Log.e(ActivityRecognitionProviderWatcher.TAG, "Error creating Hardware Activity-Recognition", e);
                }
            }
        };
    }

    public static ActivityRecognitionProviderWatcher getInstance() {
        ActivityRecognitionProviderWatcher activityRecognitionProviderWatcher;
        synchronized (sWatcherLock) {
            if (sWatcher == null) {
                sWatcher = new ActivityRecognitionProviderWatcher();
            }
            activityRecognitionProviderWatcher = sWatcher;
        }
        return activityRecognitionProviderWatcher;
    }

    public IBinder getBinder() {
        return this.mWatcherStub;
    }

    public ActivityRecognitionProvider getActivityRecognitionProvider() {
        return this.mActivityRecognitionProvider;
    }
}
