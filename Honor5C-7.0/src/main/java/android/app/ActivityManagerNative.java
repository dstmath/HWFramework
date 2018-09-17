package android.app;

import android.content.IIntentSender;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Singleton;

public abstract class ActivityManagerNative extends Binder implements IActivityManager {
    private static final Singleton<IActivityManager> gDefault = null;
    static volatile boolean sSystemReady;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ActivityManagerNative.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ActivityManagerNative.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManagerNative.<clinit>():void");
    }

    public boolean onTransact(int r1, android.os.Parcel r2, android.os.Parcel r3, int r4) throws android.os.RemoteException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ActivityManagerNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ActivityManagerNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManagerNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    public static IActivityManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IActivityManager in = (IActivityManager) obj.queryLocalInterface(IActivityManager.descriptor);
        if (in != null) {
            return in;
        }
        return new ActivityManagerProxy(obj);
    }

    public static IActivityManager getDefault() {
        return (IActivityManager) gDefault.get();
    }

    public static boolean isSystemReady() {
        if (!sSystemReady) {
            sSystemReady = getDefault().testIsSystemReady();
        }
        return sSystemReady;
    }

    public static void broadcastStickyIntent(Intent intent, String permission, int userId) {
        broadcastStickyIntent(intent, permission, -1, userId);
    }

    public static void broadcastStickyIntent(Intent intent, String permission, int appOp, int userId) {
        try {
            getDefault().broadcastIntent(null, intent, null, null, -1, null, null, null, appOp, null, false, true, userId);
        } catch (RemoteException e) {
        }
    }

    public static void noteWakeupAlarm(PendingIntent ps, int sourceUid, String sourcePkg, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager iActivityManager = getDefault();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            iActivityManager.noteWakeupAlarm(iIntentSender, sourceUid, sourcePkg, tag);
        } catch (RemoteException e) {
        }
    }

    public static void noteAlarmStart(PendingIntent ps, int sourceUid, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager iActivityManager = getDefault();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            iActivityManager.noteAlarmStart(iIntentSender, sourceUid, tag);
        } catch (RemoteException e) {
        }
    }

    public static void noteAlarmFinish(PendingIntent ps, int sourceUid, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager iActivityManager = getDefault();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            iActivityManager.noteAlarmFinish(iIntentSender, sourceUid, tag);
        } catch (RemoteException e) {
        }
    }

    public ActivityManagerNative() {
        attachInterface(this, IActivityManager.descriptor);
    }

    private int[] readIntArray(Parcel data) {
        int smallestSize = data.readInt();
        if (smallestSize <= 0) {
            return null;
        }
        int[] smallest = new int[smallestSize];
        data.readIntArray(smallest);
        return smallest;
    }

    public IBinder asBinder() {
        return this;
    }
}
