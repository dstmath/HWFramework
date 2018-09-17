package com.android.server.rms.resource;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.rms.HwSysResource;
import com.android.server.location.HwGnssLogHandlerMsgID;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwSysInnerResImpl implements HwSysResource {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.HwSysInnerResImpl";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.resource.HwSysInnerResImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.resource.HwSysInnerResImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.resource.HwSysInnerResImpl.<clinit>():void");
    }

    public static HwSysResource getResource(int resourceType) {
        switch (resourceType) {
            case HwGnssLogHandlerMsgID.UPDATEBINDERRORTIME /*20*/:
                return MemoryInnerResource.getInstance();
            default:
                return null;
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        return 1;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        return 1;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        return 0;
    }

    public void release(int callingUid, String pkg, int processTpye) {
    }

    public void clear(int callingUid, String pkg, int processTpye) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public Bundle query() {
        return null;
    }
}
