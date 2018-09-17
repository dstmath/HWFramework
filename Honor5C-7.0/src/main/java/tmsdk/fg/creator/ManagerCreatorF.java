package tmsdk.fg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorF {
    private static volatile ManagerCreatorF Ls;
    private Context mContext;
    private HashMap<Class<? extends jg>, jg> wJ;
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.fg.creator.ManagerCreatorF.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.fg.creator.ManagerCreatorF.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.fg.creator.ManagerCreatorF.<clinit>():void");
    }

    private ManagerCreatorF(Context context) {
        this.wJ = new HashMap();
        this.wK = new HashMap();
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerF> T d(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (cls) {
                BaseManagerF baseManagerF;
                t = (BaseManagerF) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerF = (BaseManagerF) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerF = (BaseManagerF) cls.newInstance();
                        baseManagerF.onCreate(this.mContext);
                        if (baseManagerF.getSingletonType() == 1) {
                            this.wJ.put(cls, baseManagerF);
                        } else if (baseManagerF.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerF));
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

    public static <T extends BaseManagerF> T getManager(Class<T> cls) {
        return iY().d(cls);
    }

    static ManagerCreatorF iY() {
        if (Ls == null) {
            synchronized (ManagerCreatorF.class) {
                if (Ls == null) {
                    Ls = new ManagerCreatorF(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return Ls;
    }
}
