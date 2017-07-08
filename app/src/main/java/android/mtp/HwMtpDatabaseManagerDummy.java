package android.mtp;

import android.database.Cursor;

public class HwMtpDatabaseManagerDummy implements HwMtpDatabaseManager {
    private static HwMtpDatabaseManager mHwMtpDatabaseManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.mtp.HwMtpDatabaseManagerDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.mtp.HwMtpDatabaseManagerDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.HwMtpDatabaseManagerDummy.<clinit>():void");
    }

    private HwMtpDatabaseManagerDummy() {
    }

    public static HwMtpDatabaseManager getDefault() {
        return mHwMtpDatabaseManager;
    }

    public int hwBeginSendObject(String path, Cursor c) {
        return -1;
    }

    public MtpPropertyList getObjectPropertyList(int property, int handle) {
        return null;
    }

    public MtpPropertyList getObjectPropertyList(int handle, int format, int[] proplist) {
        return null;
    }

    public MtpPropertyList getObjectPropertyList(MtpDatabase database, int handle, int format, int property, int groupCode) {
        return null;
    }

    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        return -1;
    }

    public int hwGetObjectFormat(int handle) {
        return -1;
    }

    public boolean hwGetObjectReferences(int handle) {
        return false;
    }

    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        return false;
    }

    public void hwSaveCurrentObject(Cursor c) {
    }

    public void hwClearSavedObject() {
    }

    public int hwGetSavedObjectHandle() {
        return -1;
    }
}
