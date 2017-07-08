package tmsdkobf;

import android.content.Context;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class if extends BaseManagerC {
    private Context mContext;
    private final HashMap<String, lc> rI;
    private ReentrantReadWriteLock rJ;
    private HashMap<String, Integer> rK;
    private HashMap<Long, lo> rL;

    public if() {
        this.rI = new HashMap(5);
        this.rJ = new ReentrantReadWriteLock();
        this.rK = null;
        this.rL = new HashMap();
    }

    public lc b(String str, long j) {
        if (str == null) {
            return null;
        }
        String str2 = str + j;
        this.rJ.readLock().lock();
        lc lcVar = (lc) this.rI.get(str2);
        this.rJ.readLock().unlock();
        if (lcVar == null) {
            this.rJ.writeLock().lock();
            ic bk = ib.bk(str);
            if (bk != null) {
                lcVar = new ie(j, bk.rD, str);
            }
            if (lcVar != null) {
                this.rI.put(str2, lcVar);
            }
            this.rJ.writeLock().unlock();
        }
        return lcVar;
    }

    public lf c(String str, long j) {
        d.d("DefaultPreferenceManager", "getDefaultPreferenceManager|caller=" + j + "|fileName=" + str);
        return kk.a(this.mContext, str);
    }

    public void onCreate(Context context) {
        this.mContext = TMSDKContext.getApplicaionContext();
    }
}
