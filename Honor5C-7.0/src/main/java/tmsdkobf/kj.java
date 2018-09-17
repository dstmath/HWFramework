package tmsdkobf;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.util.Map;

/* compiled from: Unknown */
public class kj implements lf {
    private SharedPreferences vJ;
    private Editor vK;
    private boolean vL;

    public kj(Context context, String str, boolean z) {
        this.vL = false;
        this.vJ = context.getSharedPreferences(str, 0);
    }

    private Editor getEditor() {
        if (this.vK == null) {
            this.vK = this.vJ.edit();
        }
        return this.vK;
    }

    public void beginTransaction() {
        this.vL = true;
    }

    public void clear() {
        getEditor().clear().commit();
    }

    public boolean d(String str, long j) {
        Editor editor = getEditor();
        editor.putLong(str, j);
        return this.vL ? true : editor.commit();
    }

    public boolean d(String str, boolean z) {
        Editor editor = getEditor();
        editor.putBoolean(str, z);
        return this.vL ? true : editor.commit();
    }

    public boolean dj() {
        this.vL = false;
        return this.vK == null ? true : this.vK.commit();
    }

    public boolean e(String str, int i) {
        Editor editor = getEditor();
        editor.putInt(str, i);
        return this.vL ? true : editor.commit();
    }

    public Map<String, ?> getAll() {
        return this.vJ.getAll();
    }

    public boolean getBoolean(String str, boolean z) {
        return this.vJ.getBoolean(str, z);
    }

    public int getInt(String str, int i) {
        return this.vJ.getInt(str, i);
    }

    public long getLong(String str, long j) {
        return this.vJ.getLong(str, j);
    }

    public String getString(String str, String str2) {
        return this.vJ.getString(str, str2);
    }

    public boolean m(String str, String str2) {
        Editor editor = getEditor();
        editor.putString(str, str2);
        return this.vL ? true : editor.commit();
    }
}
