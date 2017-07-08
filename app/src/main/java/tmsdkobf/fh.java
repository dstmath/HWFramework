package tmsdkobf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class fh {
    protected HashMap<String, HashMap<String, byte[]>> lX;
    protected HashMap<String, Object> lY;
    private HashMap<String, Object> lZ;
    protected String ma;
    fq mb;

    fh() {
        this.lX = new HashMap();
        this.lY = new HashMap();
        this.lZ = new HashMap();
        this.ma = "GBK";
        this.mb = new fq();
    }

    public void Z(String str) {
        this.ma = str;
    }

    public void l() {
        this.lZ.clear();
    }

    public <T> void put(String str, T t) {
        if (str == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            fr frVar = new fr();
            frVar.ae(this.ma);
            frVar.a((Object) t, 0);
            Object a = ft.a(frVar.t());
            HashMap hashMap = new HashMap(1);
            ArrayList arrayList = new ArrayList(1);
            a(arrayList, t);
            hashMap.put(ff.j(arrayList), a);
            this.lZ.remove(str);
            this.lX.put(str, hashMap);
        }
    }

    private void a(ArrayList<String> arrayList, Object obj) {
        if (obj.getClass().isArray()) {
            if (!obj.getClass().getComponentType().toString().equals("byte")) {
                throw new IllegalArgumentException("only byte[] is supported");
            } else if (Array.getLength(obj) <= 0) {
                arrayList.add("Array");
                arrayList.add("?");
            } else {
                arrayList.add("java.util.List");
                a(arrayList, Array.get(obj, 0));
            }
        } else if (obj instanceof Array) {
            throw new IllegalArgumentException("can not support Array, please use List");
        } else if (obj instanceof List) {
            arrayList.add("java.util.List");
            List list = (List) obj;
            if (list.size() <= 0) {
                arrayList.add("?");
            } else {
                a(arrayList, list.get(0));
            }
        } else if (obj instanceof Map) {
            arrayList.add("java.util.Map");
            Map map = (Map) obj;
            if (map.size() <= 0) {
                arrayList.add("?");
                arrayList.add("?");
                return;
            }
            Object next = map.keySet().iterator().next();
            Object obj2 = map.get(next);
            arrayList.add(next.getClass().getName());
            a(arrayList, obj2);
        } else {
            arrayList.add(obj.getClass().getName());
        }
    }

    public byte[] m() {
        fr frVar = new fr(0);
        frVar.ae(this.ma);
        frVar.a(this.lX, 0);
        return ft.a(frVar.t());
    }

    public void b(byte[] bArr) {
        this.mb.d(bArr);
        this.mb.ae(this.ma);
        Map hashMap = new HashMap(1);
        HashMap hashMap2 = new HashMap(1);
        hashMap2.put("", new byte[0]);
        hashMap.put("", hashMap2);
        this.lX = this.mb.a(hashMap, 0, false);
    }
}
