package tmsdkobf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class fi extends fh {
    private HashMap<String, Object> lZ;
    fq mb;
    protected HashMap<String, byte[]> mc;

    public /* bridge */ /* synthetic */ void Z(String str) {
        super.Z(str);
    }

    public fi() {
        this.mc = null;
        this.lZ = new HashMap();
        this.mb = new fq();
    }

    public void n() {
        this.mc = new HashMap();
    }

    public void l() {
        this.lZ.clear();
    }

    public <T> void put(String str, T t) {
        if (this.mc == null) {
            super.put(str, t);
        } else if (str == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            fr frVar = new fr();
            frVar.ae(this.ma);
            frVar.a((Object) t, 0);
            this.mc.put(str, ft.a(frVar.t()));
        }
    }

    public <T> T a(String str, T t) throws fg {
        T b;
        if (this.mc == null) {
            if (!this.lX.containsKey(str)) {
                return null;
            }
            if (this.lZ.containsKey(str)) {
                return this.lZ.get(str);
            }
            byte[] bArr;
            byte[] bArr2 = new byte[0];
            Iterator it = ((HashMap) this.lX.get(str)).entrySet().iterator();
            if (it.hasNext()) {
                Entry entry = (Entry) it.next();
                String str2 = (String) entry.getKey();
                bArr = (byte[]) entry.getValue();
            } else {
                bArr = bArr2;
            }
            try {
                this.mb.d(bArr);
                this.mb.ae(this.ma);
                b = this.mb.b(t, 0, true);
                b(str, b);
                return b;
            } catch (Exception e) {
                throw new fg(e);
            }
        } else if (!this.mc.containsKey(str)) {
            return null;
        } else {
            if (this.lZ.containsKey(str)) {
                return this.lZ.get(str);
            }
            try {
                b = a((byte[]) this.mc.get(str), (Object) t);
                if (b != null) {
                    b(str, b);
                }
                return b;
            } catch (Exception e2) {
                throw new fg(e2);
            }
        }
    }

    private Object a(byte[] bArr, Object obj) {
        this.mb.d(bArr);
        this.mb.ae(this.ma);
        return this.mb.b(obj, 0, true);
    }

    private void b(String str, Object obj) {
        this.lZ.put(str, obj);
    }

    public byte[] m() {
        if (this.mc == null) {
            return super.m();
        }
        fr frVar = new fr(0);
        frVar.ae(this.ma);
        frVar.a(this.mc, 0);
        return ft.a(frVar.t());
    }

    public void b(byte[] bArr) {
        try {
            super.b(bArr);
        } catch (Exception e) {
            this.mb.d(bArr);
            this.mb.ae(this.ma);
            Map hashMap = new HashMap(1);
            hashMap.put("", new byte[0]);
            this.mc = this.mb.a(hashMap, 0, false);
        }
    }
}
