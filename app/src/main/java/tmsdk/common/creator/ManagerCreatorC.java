package tmsdk.common.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorC {
    private static volatile ManagerCreatorC AQ;
    private Context mContext;
    private final Object mLock;
    private HashMap<Class<? extends jg>, jg> wJ;
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.creator.ManagerCreatorC.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.creator.ManagerCreatorC.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.creator.ManagerCreatorC.<clinit>():void");
    }

    private ManagerCreatorC(Context context) {
        this.wJ = new HashMap();
        this.wK = new HashMap();
        this.mLock = new Object();
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerC> T c(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (this.mLock) {
                BaseManagerC baseManagerC;
                t = (BaseManagerC) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerC = (BaseManagerC) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerC = (BaseManagerC) cls.newInstance();
                        baseManagerC.onCreate(this.mContext);
                        if (baseManagerC.getSingletonType() == 1) {
                            this.wJ.put(cls, baseManagerC);
                        } else if (baseManagerC.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerC));
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return t;
        }
        throw new NullPointerException("the param of getManager can't be null.");
    }

    static ManagerCreatorC eT() {
        if (AQ == null) {
            synchronized (ManagerCreatorC.class) {
                if (AQ == null) {
                    AQ = new ManagerCreatorC(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return AQ;
    }

    public static <T extends BaseManagerC> T getManager(Class<T> cls) {
        return eT().c(cls);
    }
}
