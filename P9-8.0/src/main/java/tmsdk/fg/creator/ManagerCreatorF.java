package tmsdk.fg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.ic;

public final class ManagerCreatorF {
    private static volatile ManagerCreatorF Mi = null;
    private Context mContext;
    private HashMap<Class<? extends ic>, ic> tN = new HashMap();
    private HashMap<Class<? extends ic>, WeakReference<? extends ic>> tO = new HashMap();

    private ManagerCreatorF(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerF> T d(Class<T> -l_3_R) {
        if (-l_3_R != null) {
            T t;
            synchronized (-l_3_R) {
                t = (BaseManagerF) -l_3_R.cast(this.tN.get(-l_3_R));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.tO.get(-l_3_R);
                    if (weakReference != null) {
                        t = (BaseManagerF) -l_3_R.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        t = (BaseManagerF) -l_3_R.newInstance();
                        t.onCreate(this.mContext);
                        if (t.getSingletonType() == 1) {
                            this.tN.put(-l_3_R, t);
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

    public static <T extends BaseManagerF> T getManager(Class<T> cls) {
        return ji().d(cls);
    }

    static ManagerCreatorF ji() {
        if (Mi == null) {
            Class cls = ManagerCreatorF.class;
            synchronized (ManagerCreatorF.class) {
                if (Mi == null) {
                    Mi = new ManagerCreatorF(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return Mi;
    }
}
