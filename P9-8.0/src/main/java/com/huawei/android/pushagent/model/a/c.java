package com.huawei.android.pushagent.model.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.d.a;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class c {
    private HashMap<String, Object> g = new HashMap();
    protected Context h = null;
    private String i = "";

    public c(Context context, String str) {
        this.h = context.getApplicationContext();
        this.i = str;
    }

    public boolean setValue(String str, Object obj) {
        this.g.put(str, obj);
        new a(this.h, this.i).rq(str, obj);
        return true;
    }

    public boolean u(Map<String, Object> map) {
        this.g.putAll(map);
        z();
        return true;
    }

    private Object x(String str) {
        return this.g.get(str);
    }

    private Object getValue(String str, Object obj) {
        Object x = x(str);
        if (x == null) {
            return obj;
        }
        return x;
    }

    public HashMap<String, Object> v() {
        return this.g;
    }

    public int getInt(String str, int i) {
        Object value = getValue(str, Integer.valueOf(i));
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof Long) {
            return (int) ((Long) value).longValue();
        }
        return i;
    }

    public long getLong(String str, long j) {
        Object value = getValue(str, Long.valueOf(j));
        if (value instanceof Integer) {
            return (long) ((Integer) value).intValue();
        }
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        return j;
    }

    public boolean w(String str, boolean z) {
        Object value = getValue(str, Boolean.valueOf(z));
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return z;
    }

    public String getString(String str, String str2) {
        return String.valueOf(getValue(str, str2));
    }

    protected HashMap<String, Object> y() {
        this.g.clear();
        HashMap<String, Object> hashMap = new HashMap();
        for (Entry entry : new a(this.h, this.i).getAll().entrySet()) {
            hashMap.put((String) entry.getKey(), entry.getValue());
        }
        if (hashMap.size() != 0) {
            this.g = hashMap;
        }
        return hashMap;
    }

    private boolean z() {
        new a(this.h, this.i).rw(this.g);
        return true;
    }

    public String toString() {
        String str = " ";
        String str2 = ":";
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : this.g.entrySet()) {
            stringBuffer.append((String) entry.getKey()).append(str2).append(entry.getValue()).append(str);
        }
        return stringBuffer.toString();
    }
}
