package tmsdk.fg.module.urlcheck;

import android.content.Context;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.module.urlcheck.UrlCheckResultV3;
import tmsdk.common.utils.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.cu;
import tmsdkobf.cv;
import tmsdkobf.cw;
import tmsdkobf.im;
import tmsdkobf.jy;
import tmsdkobf.kr;
import tmsdkobf.oa;

final class b extends BaseManagerF {
    private ConcurrentHashMap<Long, UrlCheckResultV3> Rn;
    private LinkedHashMap<Long, UrlCheckResultV3> Ro;
    private long Rp;

    b() {
    }

    private void kD() {
        if (this.Rn == null) {
            this.Rn = new ConcurrentHashMap();
        }
        if (this.Ro == null) {
            this.Ro = new LinkedHashMap();
        }
    }

    private void kE() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Rp != 0) {
            if ((currentTimeMillis - this.Rp >= 21600000 ? 1 : null) != null) {
                List<Long> arrayList = new ArrayList();
                synchronized (this.Ro) {
                    Iterator it = this.Ro.keySet().iterator();
                    while (it.hasNext()) {
                        long longValue = ((Long) it.next()).longValue();
                        this.Rp = longValue;
                        if ((currentTimeMillis - longValue < 21600000 ? 1 : null) != null) {
                            break;
                        }
                        it.remove();
                        arrayList.add(Long.valueOf(longValue));
                    }
                }
                for (Long remove : arrayList) {
                    this.Rn.remove(remove);
                }
                if (this.Rn.size() == 0) {
                    this.Rp = 0;
                }
            }
        }
    }

    public boolean a(final String str, int i, final ICheckUrlCallbackV3 iCheckUrlCallbackV3) {
        if (str == null || str.length() == 0 || iCheckUrlCallbackV3 == null) {
            return false;
        }
        kD();
        kE();
        for (UrlCheckResultV3 urlCheckResultV3 : this.Rn.values()) {
            if (str.equalsIgnoreCase(urlCheckResultV3.url)) {
                iCheckUrlCallbackV3.onCheckUrlCallback(urlCheckResultV3);
                return true;
            }
        }
        JceStruct cuVar = new cu();
        cuVar.fX = 1;
        cuVar.fY = i;
        cuVar.fZ = new cw();
        cuVar.fZ.url = str;
        JceStruct cvVar = new cv();
        oa bK = im.bK();
        f.f("UrlCheckManagerV2Impl", "sendShark url = " + cuVar.fZ.url);
        bK.a(2002, cuVar, cvVar, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.f("UrlCheckManagerV2Impl", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                cv cvVar = (cv) jceStruct;
                if (cvVar == null || cvVar.gb == null) {
                    iCheckUrlCallbackV3.onCheckUrlCallback(null);
                    return;
                }
                UrlCheckResultV3 urlCheckResultV3 = new UrlCheckResultV3(str, cvVar.gb);
                iCheckUrlCallbackV3.onCheckUrlCallback(urlCheckResultV3);
                long currentTimeMillis = System.currentTimeMillis();
                b.this.Rn.put(Long.valueOf(currentTimeMillis), urlCheckResultV3);
                synchronized (b.this.Ro) {
                    b.this.Ro.put(Long.valueOf(currentTimeMillis), urlCheckResultV3);
                }
                if (b.this.Rp == 0) {
                    b.this.Rp = currentTimeMillis;
                }
            }
        });
        kr.dz();
        return true;
    }

    public int getSingletonType() {
        return 0;
    }

    public void onCreate(Context context) {
    }
}
