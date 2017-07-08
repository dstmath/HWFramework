package tmsdk.fg.module.urlcheck;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.du;
import tmsdkobf.et;
import tmsdkobf.ly;
import tmsdkobf.qt;

/* compiled from: Unknown */
final class a extends BaseManagerF {
    private ConcurrentHashMap<Long, et> OU;
    private LinkedHashMap<Long, et> OV;
    private long OW;

    a() {
    }

    private void jO() throws IOException {
        this.OU = new ConcurrentHashMap();
        this.OV = new LinkedHashMap();
    }

    private void jP() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.OW != 0) {
            if ((currentTimeMillis - this.OW >= 21600000 ? 1 : null) != null) {
                List<Long> arrayList = new ArrayList();
                synchronized (this.OV) {
                    Iterator it = this.OV.keySet().iterator();
                    while (it.hasNext()) {
                        long longValue = ((Long) it.next()).longValue();
                        this.OW = longValue;
                        if ((currentTimeMillis - longValue < 21600000 ? 1 : null) != null) {
                            break;
                        }
                        it.remove();
                        arrayList.add(Long.valueOf(longValue));
                    }
                }
                for (Long remove : arrayList) {
                    this.OU.remove(remove);
                }
                if (this.OU.size() == 0) {
                    this.OW = 0;
                }
            }
        }
    }

    public UrlCheckResult checkUrl(String str) {
        if (str == null || str.length() == 0) {
            return new UrlCheckResult(-1006);
        }
        jP();
        for (et etVar : this.OU.values()) {
            et etVar2;
            if (str.equalsIgnoreCase(etVar2.getUrl())) {
                return new UrlCheckResult(etVar2);
            }
        }
        AtomicReference atomicReference = new AtomicReference();
        int a = ((qt) ManagerCreatorC.getManager(qt.class)).a(str, atomicReference);
        if (a != 0) {
            return new UrlCheckResult(a);
        }
        etVar2 = (et) atomicReference.get();
        if (etVar2 == null) {
            return new UrlCheckResult(-5006);
        }
        long currentTimeMillis = System.currentTimeMillis();
        this.OU.put(Long.valueOf(currentTimeMillis), etVar2);
        synchronized (this.OV) {
            this.OV.put(Long.valueOf(currentTimeMillis), etVar2);
        }
        if (this.OW == 0) {
            this.OW = currentTimeMillis;
        }
        ly.ep();
        return new UrlCheckResult(etVar2);
    }

    public Map<String, UrlCheckResult> checkUrlEx(List<String> list) {
        int i = 0;
        if (list == null || list.size() == 0) {
            return new HashMap(0);
        }
        int size = list.size();
        Map<String, UrlCheckResult> hashMap = new HashMap(size);
        List arrayList = new ArrayList(1);
        int i2 = 0;
        int i3 = 0;
        while (i2 < size) {
            int i4;
            int i5;
            String str = (String) list.get(i2);
            jP();
            for (et etVar : this.OU.values()) {
                if (str.equalsIgnoreCase(etVar.getUrl())) {
                    hashMap.put(str, new UrlCheckResult(etVar));
                    i4 = 1;
                    break;
                }
            }
            i4 = i3;
            if (i4 == 0) {
                arrayList.add(str);
                i5 = i4;
            } else {
                i5 = 0;
            }
            i2++;
            i3 = i5;
        }
        if (arrayList.size() == 0) {
            return hashMap;
        }
        AtomicReference atomicReference = new AtomicReference();
        arrayList.size();
        ((qt) ManagerCreatorC.getManager(qt.class)).a(arrayList, atomicReference);
        arrayList.clear();
        du duVar = (du) atomicReference.get();
        if (duVar != null) {
            ArrayList d = duVar.d();
            if (!(d == null || d.size() == 0)) {
                i3 = d.size();
                while (i < i3) {
                    et etVar2 = (et) d.get(i);
                    hashMap.put(etVar2.url, new UrlCheckResult(etVar2));
                    long currentTimeMillis = System.currentTimeMillis();
                    this.OU.put(Long.valueOf(currentTimeMillis), etVar2);
                    synchronized (this.OV) {
                        this.OV.put(Long.valueOf(currentTimeMillis), etVar2);
                    }
                    if (this.OW == 0) {
                        this.OW = currentTimeMillis;
                    }
                    i++;
                }
                ly.ep();
                return hashMap;
            }
        }
        return hashMap;
    }

    public int getSingletonType() {
        return 0;
    }

    public void onCreate(Context context) {
        try {
            jO();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
