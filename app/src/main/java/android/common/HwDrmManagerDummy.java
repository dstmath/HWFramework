package android.common;

import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import java.io.FileInputStream;
import java.io.IOException;

public class HwDrmManagerDummy implements HwDrmManager {
    private static HwDrmManager mHwDrmManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.common.HwDrmManagerDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.common.HwDrmManagerDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.common.HwDrmManagerDummy.<clinit>():void");
    }

    private HwDrmManagerDummy() {
    }

    public static HwDrmManager getDefault() {
        if (mHwDrmManager == null) {
            mHwDrmManager = new HwDrmManagerDummy();
        }
        return mHwDrmManager;
    }

    public void addHwDrmFileType() {
    }

    public void setNtpTime(long cachedNtpTime, long cachedElapsedTime) {
    }

    public void updateOmaMimeType(String uri, ContentValues values) {
    }

    public BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream stream, String pathName, boolean isShareable) throws IOException {
        return null;
    }
}
