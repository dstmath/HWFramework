package tmsdk.bg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorB {
    private static volatile ManagerCreatorB wI;
    private Context mContext;
    private HashMap<Class<? extends jg>, jg> wJ;
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.creator.ManagerCreatorB.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.creator.ManagerCreatorB.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.creator.ManagerCreatorB.<clinit>():void");
    }

    private ManagerCreatorB(Context context) {
        this.wJ = new HashMap();
        this.wK = new HashMap();
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerB> T a(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (cls) {
                BaseManagerB baseManagerB;
                t = (BaseManagerB) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerB = (BaseManagerB) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerB = (BaseManagerB) cls.newInstance();
                        baseManagerB.onCreate(this.mContext);
                        if (baseManagerB.getSingletonType() == 1) {
                            synchronized (ManagerCreatorB.class) {
                                this.wJ.put(cls, baseManagerB);
                            }
                        } else if (baseManagerB.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerB));
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

    private void b(Class<? extends jg> cls) {
        synchronized (ManagerCreatorB.class) {
            this.wJ.remove(cls);
        }
    }

    static ManagerCreatorB dG() {
        if (wI == null) {
            synchronized (ManagerCreatorB.class) {
                if (wI == null) {
                    wI = new ManagerCreatorB(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return wI;
    }

    public static void destroyManager(BaseManagerB baseManagerB) {
        if (baseManagerB != null) {
            dG().b(baseManagerB.getClass());
        }
    }

    public static <T extends BaseManagerB> T getManager(Class<T> cls) {
        return dG().a(cls);
    }
}
