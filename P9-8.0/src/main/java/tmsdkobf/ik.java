package tmsdkobf;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.DataEntity;
import tmsdkobf.ih.b;

public final class ik extends b {
    private static ConcurrentLinkedQueue<ii> rA = new ConcurrentLinkedQueue();
    private static volatile ik rB = null;

    private ik() {
    }

    public static boolean a(ii iiVar) {
        return rA.add(iiVar);
    }

    public static ik bF() {
        if (rB == null) {
            Class cls = ik.class;
            synchronized (ik.class) {
                if (rB == null) {
                    rB = new ik();
                }
            }
        }
        return rB;
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = rA.iterator();
        while (it.hasNext()) {
            ii iiVar = (ii) it.next();
            if (iiVar.isMatch(what)) {
                return iiVar.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
