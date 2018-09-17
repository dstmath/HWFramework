package com.huawei.android.pushagent.utils.d;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class a {
    private SharedPreferences fn;

    private SharedPreferences ry(String str, String str2) {
        File file = new File(str, str2 + ".xml");
        try {
            Constructor declaredConstructor = Class.forName("android.app.SharedPreferencesImpl").getDeclaredConstructor(new Class[]{File.class, Integer.TYPE});
            declaredConstructor.setAccessible(true);
            return (SharedPreferences) declaredConstructor.newInstance(new Object[]{file, Integer.valueOf(0)});
        } catch (ClassNotFoundException e) {
            c.sf("PushLog2951", e.toString());
            return null;
        } catch (NoSuchMethodException e2) {
            c.sf("PushLog2951", e2.toString());
            return null;
        } catch (InstantiationException e3) {
            c.sf("PushLog2951", e3.toString());
            return null;
        } catch (IllegalAccessException e4) {
            c.sf("PushLog2951", e4.toString());
            return null;
        } catch (IllegalArgumentException e5) {
            c.sf("PushLog2951", e5.toString());
            return null;
        } catch (InvocationTargetException e6) {
            c.sf("PushLog2951", e6.toString());
            return null;
        }
    }

    public boolean rx(String str, boolean z) {
        return this.fn != null ? this.fn.getBoolean(str, z) : z;
    }

    public String rt(String str) {
        return this.fn != null ? this.fn.getString(str, "") : "";
    }

    public int getInt(String str, int i) {
        return this.fn != null ? this.fn.getInt(str, i) : i;
    }

    public a(Context context, String str) {
        if (context == null) {
            throw new NullPointerException("context is null!");
        }
        this.fn = ry("/data/misc/hwpush", str);
    }

    /* JADX WARNING: Missing block: B:3:0x0007, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean ru(ContentValues contentValues) {
        if (this.fn == null || contentValues == null || this.fn.edit() == null) {
            return false;
        }
        boolean z = true;
        Iterator it = contentValues.valueSet().iterator();
        while (true) {
            boolean z2 = z;
            if (!it.hasNext()) {
                return z2;
            }
            Entry entry = (Entry) it.next();
            if (rq((String) entry.getKey(), entry.getValue())) {
                z = z2;
            } else {
                z = false;
            }
        }
    }

    public boolean rq(String str, Object obj) {
        if (this.fn == null) {
            return false;
        }
        Editor edit = this.fn.edit();
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
        return edit.commit();
    }

    public ContentValues read() {
        if (this.fn == null) {
            return null;
        }
        Map all = this.fn.getAll();
        if (all == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        for (Entry entry : all.entrySet()) {
            String str = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                contentValues.put(str, String.valueOf(value));
            } else if ((value instanceof Integer) || (value instanceof Short) || (value instanceof Byte)) {
                contentValues.put(str, (Integer) value);
            } else if (value instanceof Long) {
                contentValues.put(str, (Long) value);
            } else if (value instanceof Float) {
                contentValues.put(str, (Float) value);
            } else if (value instanceof Double) {
                contentValues.put(str, Float.valueOf((float) ((Double) value).doubleValue()));
            } else if (value instanceof Boolean) {
                contentValues.put(str, (Boolean) value);
            }
        }
        return contentValues;
    }

    public boolean rv(String str, String str2) {
        if (this.fn == null) {
            return false;
        }
        Editor edit = this.fn.edit();
        if (edit != null) {
            return edit.putString(str, str2).commit();
        }
        return false;
    }

    public void sa(String str, Long l) {
        if (this.fn != null) {
            Editor edit = this.fn.edit();
            if (edit != null) {
                edit.putLong(str, l.longValue()).commit();
            }
        }
    }

    public void rz(String str, boolean z) {
        if (this.fn != null) {
            Editor edit = this.fn.edit();
            if (edit != null) {
                edit.putBoolean(str, z).commit();
            }
        }
    }

    public void rw(Map<String, Object> map) {
        for (Entry entry : map.entrySet()) {
            rq((String) entry.getKey(), entry.getValue());
        }
    }

    public boolean rr(String str) {
        if (this.fn == null || !this.fn.contains(str)) {
            return false;
        }
        return this.fn.edit().remove(str).commit();
    }

    public boolean rp() {
        if (this.fn != null) {
            Map all = this.fn.getAll();
            if (all != null && all.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public Map<String, ?> getAll() {
        if (this.fn != null) {
            return this.fn.getAll();
        }
        return new HashMap();
    }

    public boolean rs() {
        if (this.fn != null) {
            return this.fn.edit().clear().commit();
        }
        return false;
    }
}
