package defpackage;

import android.content.Context;
import android.content.SharedPreferences;
import com.huawei.android.feature.BuildConfig;
import java.util.HashMap;
import java.util.Map;

/* renamed from: bd  reason: default package */
public class bd {
    protected Context X = null;
    private String aq = BuildConfig.FLAVOR;
    private HashMap<String, Object> ar = new HashMap<>();

    public bd(Context context, String str) {
        this.X = context.getApplicationContext();
        this.aq = str;
    }

    public final boolean a(String str, Object obj) {
        this.ar.put(str, obj);
        bc bcVar = new bc(this.X, this.aq);
        if (bcVar.ap != null) {
            SharedPreferences.Editor edit = bcVar.ap.edit();
            edit.putString(str, String.valueOf(obj));
            edit.commit();
        }
        return true;
    }

    public final Object b(String str, Object obj) {
        Object obj2 = this.ar.get(str);
        return obj2 == null ? obj : obj2;
    }

    /* access modifiers changed from: protected */
    public final HashMap<String, Object> l() {
        this.ar.clear();
        HashMap<String, Object> hashMap = new HashMap<>();
        bc bcVar = new bc(this.X, this.aq);
        for (Map.Entry entry : (bcVar.ap != null ? bcVar.ap.getAll() : new HashMap()).entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        if (hashMap.size() != 0) {
            this.ar = hashMap;
        }
        return hashMap;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry next : this.ar.entrySet()) {
            stringBuffer.append((String) next.getKey()).append(":").append(next.getValue()).append(" ");
        }
        return stringBuffer.toString();
    }
}
