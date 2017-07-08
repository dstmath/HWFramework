package tmsdkobf;

import android.os.MemoryFile;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class jq {
    private static List<WeakReference<a>> uf;
    private static jr ug;
    public static volatile qz uh;
    private static MemoryFile ui;
    private static volatile boolean uj;
    public static int uk;
    private static boolean ul;
    private static boolean um;
    private static pf un;
    public static IDualPhoneInfoFetcher uo;

    /* compiled from: Unknown */
    public interface a {
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jq.<clinit>():void");
    }

    public static synchronized void a(a aVar) {
        synchronized (jq.class) {
            k(uf);
            uf.add(new WeakReference(aVar));
        }
    }

    public static void a(pf pfVar) {
        un = pfVar;
    }

    private static long bI() {
        int i = 2;
        if (1 == cw()) {
            i = 1;
        } else if (2 != cw()) {
            i = 3;
        }
        return jk.getIdent(i, UpdateConfig.UPDATE_FLAG_PAY_LIST);
    }

    public static boolean cq() {
        return ul || fw.w().H().booleanValue();
    }

    public static boolean cr() {
        try {
            byte[] bytes = TMSDKContext.class.getName().getBytes("utf-8");
            byte[] bArr = new byte[]{(byte) ((byte) bytes.length)};
            ui = new MemoryFile("tmsdk2-jni-context", SystemCallLogFilterConsts.ANONYMOUS_CALL);
            ui.writeBytes(bArr, 0, 0, 1);
            ui.writeBytes(bytes, 0, 1, bytes.length);
            cs();
            return uj;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void cs() {
        if (!uj) {
            uj = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SDK_LIBNAME));
            d.g("demo", "mIsSdkLibraryLoaded =" + uj);
        }
    }

    public static jr ct() {
        if (ug == null) {
            synchronized (jq.class) {
                if (ug == null) {
                    ug = new jr(bI(), "com.tmsdk.common");
                }
            }
        }
        return ug;
    }

    public static pf cu() {
        return un;
    }

    public static qz cv() {
        return uh;
    }

    public static int cw() {
        return uk;
    }

    public static IDualPhoneInfoFetcher cx() {
        return uo;
    }

    public static boolean getTmsliteSwitch() {
        return fw.w().G().booleanValue();
    }

    private static <T> void k(List<WeakReference<T>> list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            if (((WeakReference) it.next()).get() == null) {
                it.remove();
            }
        }
    }

    public static void setAutoConnectionSwitch(boolean z) {
        ul = z;
    }

    public static void setDualPhoneInfoFetcher(IDualPhoneInfoFetcher iDualPhoneInfoFetcher) {
        d.g("SdkContextInternal", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        d.g("TrafficCorrection", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        uo = iDualPhoneInfoFetcher;
    }
}
