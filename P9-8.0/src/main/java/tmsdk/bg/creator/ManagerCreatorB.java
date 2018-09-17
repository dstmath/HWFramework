package tmsdk.bg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.ic;

public final class ManagerCreatorB {
    private static volatile ManagerCreatorB tM = null;
    private Context mContext;
    private HashMap<Class<? extends ic>, ic> tN = new HashMap();
    private HashMap<Class<? extends ic>, WeakReference<? extends ic>> tO = new HashMap();

    private ManagerCreatorB(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerB> T a(Class<T> -l_3_R) {
        if (-l_3_R != null) {
            T t;
            synchronized (-l_3_R) {
                t = (BaseManagerB) -l_3_R.cast(this.tN.get(-l_3_R));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.tO.get(-l_3_R);
                    if (weakReference != null) {
                        t = (BaseManagerB) -l_3_R.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        t = (BaseManagerB) -l_3_R.newInstance();
                        t.onCreate(this.mContext);
                        if (t.getSingletonType() == 1) {
                            Class cls = ManagerCreatorB.class;
                            synchronized (ManagerCreatorB.class) {
                                this.tN.put(-l_3_R, t);
                            }
                        } else if (t.getSingletonType() == 0) {
                            this.tO.put(-l_3_R, new WeakReference(t));
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

    private void b(Class<? extends ic> cls) {
        Class cls2 = ManagerCreatorB.class;
        synchronized (ManagerCreatorB.class) {
            this.tN.remove(cls);
        }
    }

    static ManagerCreatorB cO() {
        if (tM == null) {
            Class cls = ManagerCreatorB.class;
            synchronized (ManagerCreatorB.class) {
                if (tM == null) {
                    tM = new ManagerCreatorB(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return tM;
    }

    public static void destroyManager(BaseManagerB baseManagerB) {
        if (baseManagerB != null) {
            cO().b(baseManagerB.getClass());
        }
    }

    public static <T extends BaseManagerB> T getManager(Class<T> cls) {
        return cO().a(cls);
    }
}
