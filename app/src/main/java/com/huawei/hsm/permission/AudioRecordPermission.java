package com.huawei.hsm.permission;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Binder;
import android.util.Log;

public class AudioRecordPermission {
    private static final String TAG = "AudioRecordPermission";
    private static final byte[] mp3Data = null;
    private boolean mBlocked;
    private Context mContext;
    private int mPid;
    private int mUid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.AudioRecordPermission.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.AudioRecordPermission.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.AudioRecordPermission.<clinit>():void");
    }

    public AudioRecordPermission() {
        this.mBlocked = false;
        this.mContext = ActivityThread.currentApplication();
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
        Log.d(TAG, TAG);
    }

    public void remind() {
        if (!StubController.checkPrecondition(this.mUid)) {
            return;
        }
        if (this.mContext == null || StubController.isGlobalSwitchOn(this.mContext, PduHeaders.VALUE_YES)) {
            int selectionResult = StubController.holdForGetPermissionSelection(PduHeaders.VALUE_YES, this.mUid, this.mPid, null);
            if (selectionResult == 0) {
                Log.e(TAG, "AudioRecordPermission holdForGetPermissionSelection error");
                return;
            }
            if (2 == selectionResult) {
                this.mBlocked = true;
            }
        }
    }

    public boolean remindWithResult() {
        remind();
        Log.i(TAG, "remindWithResult:" + this.mBlocked);
        return !this.mBlocked;
    }

    public static byte[] getData() {
        return (byte[]) mp3Data.clone();
    }
}
