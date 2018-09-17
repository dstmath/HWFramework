package com.tencent.tcuser.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;
import tmsdkobf.nd;

/* compiled from: Unknown */
public class Encode {
    private static String TAG;
    private static boolean isLoaded;

    /* compiled from: Unknown */
    public static class ProcessInfo {
        public String name;
        public int pid;
        public int ppid;
        public int uid;

        public ProcessInfo() {
            this.pid = 0;
            this.ppid = 0;
            this.name = null;
            this.uid = 0;
        }

        public ProcessInfo(int i, int i2, String str, int i3) {
            this.pid = i;
            this.ppid = i2;
            this.name = str;
            this.uid = i3;
        }

        public String toString() {
            return "PID=" + this.pid + " PPID=" + this.ppid + " NAME=" + this.name + " UID=" + this.uid;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.tcuser.util.Encode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.tcuser.util.Encode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.tencent.tcuser.util.Encode.<clinit>():void");
    }

    public static final native String cs(String str);

    private static synchronized void loadLib() {
        synchronized (Encode.class) {
            try {
                System.loadLibrary("xy");
                isLoaded = true;
            } catch (Throwable th) {
                nd.a(new Thread(), th, "System.loadLibrary error", null);
                d.f(TAG, th);
            }
        }
    }

    private static final native void nativePs(List<String> list, List<ProcessInfo> list2);

    public static final synchronized List<ProcessInfo> ps(List<String> list) {
        List<ProcessInfo> arrayList;
        synchronized (Encode.class) {
            if (!isLoaded) {
                loadLib();
            }
            arrayList = new ArrayList();
            nativePs(list, arrayList);
        }
        return arrayList;
    }

    public static final native int pu(int i);

    public static native byte[] x(Context context, byte[] bArr);

    public static native byte[] y(Context context, byte[] bArr);
}
