package defpackage;

import android.content.Context;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONObject;

/* renamed from: h */
class h {
    protected Context context;
    private String s;
    public HashMap t;

    public h(Context context, String str) {
        this.s = "";
        this.t = new HashMap();
        this.context = null;
        this.s = str;
        this.context = context;
    }

    private Object b(String str, Object obj) {
        Object value = getValue(str);
        return value == null ? obj : value;
    }

    public static HashMap b(String str) {
        HashMap hashMap = new HashMap();
        try {
            JSONObject jSONObject = new JSONObject(str);
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String valueOf = String.valueOf(keys.next());
                hashMap.put(valueOf, jSONObject.get(valueOf));
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        }
        return hashMap;
    }

    private Object getValue(String str) {
        return this.t.get(str);
    }

    public boolean a(String str, Object obj) {
        this.t.put(str, obj);
        new bt(this.context, this.s).c(str, obj);
        return true;
    }

    public int getInt(String str, int i) {
        Object b = b(str, Integer.valueOf(i));
        return b instanceof Integer ? ((Integer) b).intValue() : b instanceof Long ? (int) ((Long) b).longValue() : i;
    }

    public long getLong(String str, long j) {
        Object b = b(str, Long.valueOf(j));
        return b instanceof Integer ? (long) ((Integer) b).intValue() : b instanceof Long ? ((Long) b).longValue() : j;
    }

    public String getString(String str, String str2) {
        return String.valueOf(b(str, str2));
    }

    public HashMap h() {
        HashMap hashMap = new HashMap();
        for (Entry entry : new bt(this.context, this.s).getAll().entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        if (hashMap.size() != 0) {
            this.t = hashMap;
        }
        return hashMap;
    }

    public boolean i() {
        new bt(this.context, this.s).a(this.t);
        return true;
    }

    public String toString() {
        String str = " ";
        String str2 = ":";
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : this.t.entrySet()) {
            stringBuffer.append((String) entry.getKey()).append(str2).append(entry.getValue()).append(str);
        }
        return stringBuffer.toString();
    }
}
