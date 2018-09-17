package tmsdk.fg.module.urlcheck;

import android.content.Context;
import com.qq.taf.jce.JceStruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.s;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.ey;
import tmsdkobf.ez;
import tmsdkobf.im;
import tmsdkobf.jy;
import tmsdkobf.kr;
import tmsdkobf.oa;

final class a extends BaseManagerF {
    private ConcurrentHashMap<Long, ez> Rf;
    private LinkedHashMap<Long, ez> Rg;
    private long Rh;
    private UrlCheckResult Ri = null;
    private final Object lock = new Object();

    a() {
    }

    private void kB() throws IOException {
        this.Rf = new ConcurrentHashMap();
        this.Rg = new LinkedHashMap();
    }

    private void kC() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Rh != 0) {
            if ((currentTimeMillis - this.Rh >= 21600000 ? 1 : null) != null) {
                List<Long> arrayList = new ArrayList();
                synchronized (this.Rg) {
                    Iterator it = this.Rg.keySet().iterator();
                    while (it.hasNext()) {
                        long longValue = ((Long) it.next()).longValue();
                        this.Rh = longValue;
                        if ((currentTimeMillis - longValue < 21600000 ? 1 : null) != null) {
                            break;
                        }
                        it.remove();
                        arrayList.add(Long.valueOf(longValue));
                    }
                }
                for (Long remove : arrayList) {
                    this.Rf.remove(remove);
                }
                if (this.Rf.size() == 0) {
                    this.Rh = 0;
                }
            }
        }
    }

    public int a(String str, final ICheckUrlCallback iCheckUrlCallback) {
        if (str == null || str.length() == 0 || iCheckUrlCallback == null) {
            return -1006;
        }
        s.bW(64);
        kC();
        f.f("jiejie-url", "mCheckedUrlsCache size is " + this.Rf.values().size());
        for (ez ezVar : this.Rf.values()) {
            if (str.equalsIgnoreCase(ezVar.getUrl())) {
                iCheckUrlCallback.onCheckUrlCallback(new UrlCheckResult(ezVar));
                return 0;
            }
        }
        oa bK = im.bK();
        final JceStruct eyVar = new ey();
        eyVar.setUrl(str);
        JceStruct ezVar2 = new ez();
        ezVar2.setUrl(str);
        f.f("UrlCheckManager", "[GUID] " + bK.b());
        bK.a(1040, eyVar, ezVar2, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.f("UrlCheckManager", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                ez ezVar = (ez) jceStruct;
                if (ezVar != null) {
                    long currentTimeMillis = System.currentTimeMillis();
                    ezVar.setUrl(eyVar.getUrl());
                    a.this.Rf.put(Long.valueOf(currentTimeMillis), ezVar);
                    f.f("UrlCheckManager", "加入缓存, url is " + ezVar.getUrl());
                    synchronized (a.this.Rg) {
                        a.this.Rg.put(Long.valueOf(currentTimeMillis), ezVar);
                    }
                    if (a.this.Rh == 0) {
                        a.this.Rh = currentTimeMillis;
                    }
                    iCheckUrlCallback.onCheckUrlCallback(new UrlCheckResult(ezVar));
                    return;
                }
                f.f("UrlCheckManager", "response is null");
                iCheckUrlCallback.onCheckUrlCallback(null);
            }
        });
        kr.dz();
        return 0;
    }

    public UrlCheckResult dJ(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        s.bW(64);
        kC();
        f.f("UrlCheckManager", "Sync--mCheckedUrlsCache size is " + this.Rf.values().size());
        for (ez ezVar : this.Rf.values()) {
            if (str.equalsIgnoreCase(ezVar.getUrl())) {
                return new UrlCheckResult(ezVar);
            }
        }
        oa bK = im.bK();
        final JceStruct eyVar = new ey();
        eyVar.setUrl(str);
        JceStruct ezVar2 = new ez();
        ezVar2.setUrl(str);
        this.Ri = null;
        f.f("UrlCheckManager", "[GUID] " + bK.b());
        bK.a(1040, eyVar, ezVar2, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.f("UrlCheckManager", "Sync--onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                ez ezVar = (ez) jceStruct;
                if (ezVar != null) {
                    long currentTimeMillis = System.currentTimeMillis();
                    ezVar.setUrl(eyVar.getUrl());
                    a.this.Rf.put(Long.valueOf(currentTimeMillis), ezVar);
                    f.f("UrlCheckManager", "sync--加入缓存, url is " + ezVar.getUrl());
                    synchronized (a.this.Rg) {
                        a.this.Rg.put(Long.valueOf(currentTimeMillis), ezVar);
                    }
                    if (a.this.Rh == 0) {
                        a.this.Rh = currentTimeMillis;
                    }
                    a.this.Ri = new UrlCheckResult(ezVar);
                }
                synchronized (a.this.lock) {
                    f.f("UrlCheckManager", "sync--notify");
                    a.this.lock.notify();
                }
            }
        });
        synchronized (this.lock) {
            try {
                f.f("UrlCheckManager", "sync--wait");
                this.lock.wait();
            } catch (Exception e) {
            }
        }
        kr.dz();
        return this.Ri;
    }

    public int getSingletonType() {
        return 0;
    }

    public void onCreate(Context context) {
        try {
            kB();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
