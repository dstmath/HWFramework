package android.media.session;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioSystem;
import android.util.Log;

public class HwMediaSessionImpl implements HwMediaSessionManager {
    private static final String TAG = "HwMediaSessionImpl";
    private static HwMediaSessionManager mHwMediaSessionManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.session.HwMediaSessionImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.session.HwMediaSessionImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.session.HwMediaSessionImpl.<clinit>():void");
    }

    private HwMediaSessionImpl() {
    }

    public static HwMediaSessionManager getDefault() {
        return mHwMediaSessionManager;
    }

    public void updateTargetInService(PendingIntent mbr, Context context) {
        String newReceiver = null;
        if (mbr != null) {
            newReceiver = mbr.getCreatorPackage();
        }
        if (newReceiver != null && isWiredHeadsetOn() && context != null) {
            Intent intent = new Intent("com.huawei.internetaudioservice.autoaction");
            intent.setClassName("com.huawei.internetaudioservice", "com.huawei.internetaudioservice.InternetAudioService");
            Log.i(TAG, "newReceiver:" + newReceiver);
            intent.putExtra("new_target_selected", newReceiver);
            context.startService(intent);
        }
    }

    private boolean isWiredHeadsetOn() {
        if (AudioSystem.getDeviceConnectionState(4, "") == 0 && AudioSystem.getDeviceConnectionState(8, "") == 0) {
            return false;
        }
        return true;
    }
}
