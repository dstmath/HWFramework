package defpackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

/* renamed from: al  reason: default package */
public class al {
    private String U;
    private ArrayMap<String, Object> V = new ArrayMap<>();
    protected Context context;

    public al(Context context2, String str) {
        this.context = context2;
        this.U = str;
    }

    private Object getValue(String str) {
        return this.V.get(str);
    }

    public final boolean a(String str, Object obj) {
        this.V.put(str, obj);
        ak akVar = new ak(this.context, this.U);
        if (akVar.T == null) {
            return true;
        }
        SharedPreferences.Editor edit = akVar.T.edit();
        if (obj instanceof String) {
            edit.putString(str, String.valueOf(obj));
        } else if ((obj instanceof Integer) || (obj instanceof Short) || (obj instanceof Byte)) {
            edit.putInt(str, ((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            edit.putLong(str, ((Long) obj).longValue());
        } else if (obj instanceof Float) {
            edit.putFloat(str, ((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            edit.putFloat(str, (float) ((Double) obj).doubleValue());
        } else if (obj instanceof Boolean) {
            edit.putBoolean(str, ((Boolean) obj).booleanValue());
        }
        edit.commit();
        return true;
    }

    public final Object b(String str, Object obj) {
        Object value = getValue(str);
        return value == null ? obj : value;
    }

    /* access modifiers changed from: protected */
    public final void f() {
        this.V.clear();
        ak akVar = new ak(this.context, this.U);
        this.V.putAll(akVar.T != null ? akVar.T.getAll() : new ArrayMap<>());
    }

    public final String getString(String str, String str2) {
        Object b = b(str, str2);
        return b == null ? str2 : (String) b;
    }
}
