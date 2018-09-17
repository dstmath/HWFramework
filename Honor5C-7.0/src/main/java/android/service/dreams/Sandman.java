package android.service.dreams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.service.dreams.IDreamManager.Stub;
import android.util.Slog;

public final class Sandman {
    private static final ComponentName SOMNAMBULATOR_COMPONENT = null;
    private static final String TAG = "Sandman";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.dreams.Sandman.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.dreams.Sandman.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.Sandman.<clinit>():void");
    }

    private Sandman() {
    }

    public static boolean shouldStartDockApp(Context context, Intent intent) {
        ComponentName name = intent.resolveActivity(context.getPackageManager());
        if (name == null || name.equals(SOMNAMBULATOR_COMPONENT)) {
            return false;
        }
        return true;
    }

    public static void startDreamByUserRequest(Context context) {
        startDream(context, false);
    }

    public static void startDreamWhenDockedIfAppropriate(Context context) {
        if (isScreenSaverEnabled(context) && isScreenSaverActivatedOnDock(context)) {
            startDream(context, true);
        } else {
            Slog.i(TAG, "Dreams currently disabled for docks.");
        }
    }

    private static void startDream(Context context, boolean docked) {
        try {
            IDreamManager dreamManagerService = Stub.asInterface(ServiceManager.getService(DreamService.DREAM_SERVICE));
            if (dreamManagerService != null && !dreamManagerService.isDreaming()) {
                if (docked) {
                    Slog.i(TAG, "Activating dream while docked.");
                    ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).wakeUp(SystemClock.uptimeMillis(), "android.service.dreams:DREAM");
                } else {
                    Slog.i(TAG, "Activating dream by user request.");
                }
                dreamManagerService.dream();
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "Could not start dream when docked.", ex);
        }
    }

    private static boolean isScreenSaverEnabled(Context context) {
        if (Secure.getIntForUser(context.getContentResolver(), Secure.SCREENSAVER_ENABLED, context.getResources().getBoolean(17956974) ? 1 : 0, -2) != 0) {
            return true;
        }
        return false;
    }

    private static boolean isScreenSaverActivatedOnDock(Context context) {
        if (Secure.getIntForUser(context.getContentResolver(), Secure.SCREENSAVER_ACTIVATE_ON_DOCK, context.getResources().getBoolean(17956975) ? 1 : 0, -2) != 0) {
            return true;
        }
        return false;
    }
}
