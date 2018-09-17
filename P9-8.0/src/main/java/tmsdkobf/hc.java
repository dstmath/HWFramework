package tmsdkobf;

import android.content.Context;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;

public class hc extends BaseManagerC {
    private Context mContext;
    private final HashMap<String, jv> pl = new HashMap(5);
    private ReentrantReadWriteLock pm = new ReentrantReadWriteLock();
    private HashMap<String, Integer> pn = null;
    private HashMap<Long, kg> po = new HashMap();

    public jv a(String str, long j) {
        if (str == null) {
            return null;
        }
        String str2 = str + j;
        this.pm.readLock().lock();
        jv jvVar = (jv) this.pl.get(str2);
        this.pm.readLock().unlock();
        if (jvVar == null) {
            this.pm.writeLock().lock();
            gz ak = gy.ak(str);
            if (ak != null) {
                jvVar = new hb(j, ak.pg, str);
            }
            if (jvVar != null) {
                this.pl.put(str2, jvVar);
            }
            this.pm.writeLock().unlock();
        }
        return jvVar;
    }

    public jx b(String str, long j) {
        return jd.b(this.mContext, str);
    }

    public void onCreate(Context context) {
        this.mContext = TMSDKContext.getApplicaionContext();
    }
}
