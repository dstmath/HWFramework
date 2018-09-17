package tmsdk.common.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.ic;

public final class ManagerCreatorC {
    private static volatile ManagerCreatorC yz = null;
    private Context mContext;
    private final Object mLock = new Object();
    private HashMap<Class<? extends ic>, ic> tN = new HashMap();
    private HashMap<Class<? extends ic>, WeakReference<? extends ic>> tO = new HashMap();

    private ManagerCreatorC(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerC> T c(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (this.mLock) {
                t = (BaseManagerC) cls.cast(this.tN.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.tO.get(cls);
                    if (weakReference != null) {
                        t = (BaseManagerC) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        t = (BaseManagerC) cls.newInstance();
                        t.onCreate(this.mContext);
                        if (t.getSingletonType() == 1) {
                            this.tN.put(cls, t);
                        } else if (t.getSingletonType() == 0) {
                            this.tO.put(cls, new WeakReference(t));
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

    static ManagerCreatorC eB() {
        if (yz == null) {
            Class cls = ManagerCreatorC.class;
            synchronized (ManagerCreatorC.class) {
                if (yz == null) {
                    yz = new ManagerCreatorC(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return yz;
    }

    public static <T extends BaseManagerC> T getManager(Class<T> cls) {
        return eB().c(cls);
    }
}
