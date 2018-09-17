package tmsdkobf;

import android.content.Context;
import com.qq.taf.jce.JceStruct;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;

final class ou extends BaseManagerC {
    public static String TAG = "SharkSessionManagerImpl";
    private os IX;
    final int IY = 8000;
    private Context mContext;
    ob wS;

    ou() {
    }

    public int a(em emVar) {
        int intValue;
        final AtomicReference atomicReference = new AtomicReference(Integer.valueOf(100));
        JceStruct dtVar = new dt();
        dtVar.hR = this.IX.ht();
        dtVar.hS = this.IX.hu();
        dtVar.hX = emVar;
        this.wS.a(554, dtVar, null, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.f("jiejie-modified", "SoftList onFinish " + i3);
                synchronized (atomicReference) {
                    atomicReference.set(Integer.valueOf(i3));
                    atomicReference.notify();
                }
            }
        }, 8000);
        AtomicReference atomicReference2 = atomicReference;
        synchronized (atomicReference) {
            while (((Integer) atomicReference.get()).intValue() == 100) {
                try {
                    atomicReference.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            intValue = ((Integer) atomicReference.get()).intValue();
        }
        return intValue;
    }

    public int hw() {
        int intValue;
        final AtomicReference atomicReference = new AtomicReference(Integer.valueOf(100));
        JceStruct dsVar = new ds();
        dsVar.hR = this.IX.ht();
        dsVar.hS = this.IX.hu();
        dsVar.hT = this.IX.hv();
        f.f("jiejie-modified", "ChannelId is " + dsVar.hT.id);
        f.f("jiejie-modified", "ChannelInfo is " + dsVar.hT.toString());
        this.wS.a(553, dsVar, null, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.f("jiejie-modified", "ChannelInfo onFinish " + i3);
                synchronized (atomicReference) {
                    atomicReference.set(Integer.valueOf(i3));
                    atomicReference.notify();
                }
            }
        }, 8000);
        AtomicReference atomicReference2 = atomicReference;
        synchronized (atomicReference) {
            while (((Integer) atomicReference.get()).intValue() == 100) {
                try {
                    atomicReference.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            intValue = ((Integer) atomicReference.get()).intValue();
        }
        return intValue;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.IX = new os(this.mContext);
        this.wS = im.bK();
    }
}
