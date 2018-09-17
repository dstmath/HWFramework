package tmsdkobf;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tmsdk.common.CallerIdent;

public class fj {
    private static ReentrantReadWriteLock ms = new ReentrantReadWriteLock();
    private static HashMap<String, Object> mt = new HashMap();

    public static Object D(int i) {
        return a(i, CallerIdent.getIdent(1, 4294967296L));
    }

    private static Object a(int i, long j) {
        String str = "" + i + "-" + j;
        ms.readLock().lock();
        Object obj = mt.get(str);
        ms.readLock().unlock();
        return obj != null ? obj : b(i, j);
    }

    private static Object b(int i, long j) {
        Object obj = null;
        Object obj2 = null;
        switch (i) {
            case 4:
                obj = "" + i + "-" + j;
                ms.readLock().lock();
                obj2 = mt.get(obj);
                ms.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new ki(j, "com.tencent.meri");
                    mb.n("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 5:
                obj = "" + i + "-" + j;
                ms.readLock().lock();
                obj2 = mt.get(obj);
                ms.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new gw(j);
                    mb.n("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 9:
                obj = "" + i + "-" + j;
                ms.readLock().lock();
                obj2 = mt.get(obj);
                ms.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new hd(j);
                    mb.n("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 12:
                obj = "" + i + "-" + j;
                ms.readLock().lock();
                obj2 = mt.get(obj);
                ms.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new gl(j);
                    mb.n("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 17:
                obj = "" + i + "-" + j;
                ms.readLock().lock();
                obj2 = mt.get(obj);
                ms.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new fi(j);
                    mb.n("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
        }
        if (!(obj == null || obj2 == null)) {
            ms.writeLock().lock();
            if (mt.get(obj) == null) {
                mt.put(obj, obj2);
            }
            ms.writeLock().unlock();
        }
        return obj2;
    }
}
