package com.android.internal.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;

public final class RotationPolicy {
    private static final int CURRENT_ROTATION = -1;
    private static final boolean IS_lOCK_UNNATURAL_ORIENTATION = false;
    private static final int NATURAL_ROTATION = 0;
    private static final String TAG = "RotationPolicy";

    /* renamed from: com.android.internal.view.RotationPolicy.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ boolean val$enabled;
        final /* synthetic */ int val$rotation;

        AnonymousClass1(boolean val$enabled, int val$rotation) {
            this.val$enabled = val$enabled;
            this.val$rotation = val$rotation;
        }

        public void run() {
            try {
                IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                if (this.val$enabled) {
                    wm.freezeRotation(this.val$rotation);
                } else {
                    wm.thawRotation();
                }
            } catch (RemoteException e) {
                Log.w(RotationPolicy.TAG, "Unable to save auto-rotate setting");
            }
        }
    }

    public static abstract class RotationPolicyListener {
        final ContentObserver mObserver;

        /* renamed from: com.android.internal.view.RotationPolicy.RotationPolicyListener.1 */
        class AnonymousClass1 extends ContentObserver {
            AnonymousClass1(Handler $anonymous0) {
                super($anonymous0);
            }

            public void onChange(boolean selfChange, Uri uri) {
                RotationPolicyListener.this.onChange();
            }
        }

        public abstract void onChange();

        public RotationPolicyListener() {
            this.mObserver = new AnonymousClass1(new Handler());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.view.RotationPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.view.RotationPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.view.RotationPolicy.<clinit>():void");
    }

    private RotationPolicy() {
    }

    public static boolean isRotationSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature("android.hardware.sensor.accelerometer") && pm.hasSystemFeature("android.hardware.screen.portrait") && pm.hasSystemFeature("android.hardware.screen.landscape")) {
            return context.getResources().getBoolean(R.bool.config_supportAutoRotation);
        }
        return IS_lOCK_UNNATURAL_ORIENTATION;
    }

    public static int getRotationLockOrientation(Context context) {
        if (!IS_lOCK_UNNATURAL_ORIENTATION) {
            Point size = new Point();
            try {
                WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(NATURAL_ROTATION, size);
                return size.x < size.y ? 1 : 2;
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to get the display size");
            }
        }
        return NATURAL_ROTATION;
    }

    public static boolean isRotationLockToggleVisible(Context context) {
        if (isRotationSupported(context) && System.getIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", NATURAL_ROTATION, -2) == 0) {
            return true;
        }
        return IS_lOCK_UNNATURAL_ORIENTATION;
    }

    public static boolean isRotationLocked(Context context) {
        return System.getIntForUser(context.getContentResolver(), "accelerometer_rotation", NATURAL_ROTATION, -2) == 0 ? true : IS_lOCK_UNNATURAL_ORIENTATION;
    }

    public static void setRotationLock(Context context, boolean enabled) {
        System.putIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", NATURAL_ROTATION, -2);
        setRotationLock(enabled, IS_lOCK_UNNATURAL_ORIENTATION ? CURRENT_ROTATION : NATURAL_ROTATION);
    }

    public static void setRotationLockForAccessibility(Context context, boolean enabled) {
        System.putIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", enabled ? 1 : NATURAL_ROTATION, -2);
        setRotationLock(enabled, IS_lOCK_UNNATURAL_ORIENTATION ? CURRENT_ROTATION : NATURAL_ROTATION);
    }

    private static boolean areAllRotationsAllowed(Context context) {
        return context.getResources().getBoolean(R.bool.config_allowAllRotations);
    }

    private static void setRotationLock(boolean enabled, int rotation) {
        AsyncTask.execute(new AnonymousClass1(enabled, rotation));
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener) {
        registerRotationPolicyListener(context, listener, UserHandle.getCallingUserId());
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener, int userHandle) {
        context.getContentResolver().registerContentObserver(System.getUriFor("accelerometer_rotation"), IS_lOCK_UNNATURAL_ORIENTATION, listener.mObserver, userHandle);
        context.getContentResolver().registerContentObserver(System.getUriFor("hide_rotation_lock_toggle_for_accessibility"), IS_lOCK_UNNATURAL_ORIENTATION, listener.mObserver, userHandle);
    }

    public static void unregisterRotationPolicyListener(Context context, RotationPolicyListener listener) {
        context.getContentResolver().unregisterContentObserver(listener.mObserver);
    }
}
