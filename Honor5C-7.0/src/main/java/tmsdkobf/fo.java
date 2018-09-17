package tmsdkobf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class fo {
    private StringBuilder mv;
    private int mw;

    private void ad(String str) {
        for (int i = 0; i < this.mw; i++) {
            this.mv.append('\t');
        }
        if (str != null) {
            this.mv.append(str).append(": ");
        }
    }

    public fo(StringBuilder stringBuilder, int i) {
        this.mw = 0;
        this.mv = stringBuilder;
        this.mw = i;
    }

    public fo a(boolean z, String str) {
        char c;
        ad(str);
        StringBuilder stringBuilder = this.mv;
        if (z) {
            c = 'T';
        } else {
            c = 'F';
        }
        stringBuilder.append(c).append('\n');
        return this;
    }

    public fo a(byte b, String str) {
        ad(str);
        this.mv.append(b).append('\n');
        return this;
    }

    public fo a(char c, String str) {
        ad(str);
        this.mv.append(c).append('\n');
        return this;
    }

    public fo a(short s, String str) {
        ad(str);
        this.mv.append(s).append('\n');
        return this;
    }

    public fo a(int i, String str) {
        ad(str);
        this.mv.append(i).append('\n');
        return this;
    }

    public fo a(long j, String str) {
        ad(str);
        this.mv.append(j).append('\n');
        return this;
    }

    public fo a(float f, String str) {
        ad(str);
        this.mv.append(f).append('\n');
        return this;
    }

    public fo a(double d, String str) {
        ad(str);
        this.mv.append(d).append('\n');
        return this;
    }

    public fo a(String str, String str2) {
        ad(str2);
        if (str != null) {
            this.mv.append(str).append('\n');
        } else {
            this.mv.append("null").append('\n');
        }
        return this;
    }

    public fo a(byte[] bArr, String str) {
        ad(str);
        if (bArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (bArr.length != 0) {
            this.mv.append(bArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (byte a : bArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(bArr.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(short[] sArr, String str) {
        ad(str);
        if (sArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (sArr.length != 0) {
            this.mv.append(sArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (short a : sArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(sArr.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(int[] iArr, String str) {
        ad(str);
        if (iArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (iArr.length != 0) {
            this.mv.append(iArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (int a : iArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(iArr.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(long[] jArr, String str) {
        ad(str);
        if (jArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (jArr.length != 0) {
            this.mv.append(jArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (long a : jArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(jArr.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(float[] fArr, String str) {
        ad(str);
        if (fArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (fArr.length != 0) {
            this.mv.append(fArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (float a : fArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(fArr.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(double[] dArr, String str) {
        ad(str);
        if (dArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (dArr.length != 0) {
            this.mv.append(dArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (double a : dArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(dArr.length).append(", []").append('\n');
            return this;
        }
    }

    public <K, V> fo a(Map<K, V> map, String str) {
        ad(str);
        if (map == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (map.isEmpty()) {
            this.mv.append(map.size()).append(", {}").append('\n');
            return this;
        } else {
            this.mv.append(map.size()).append(", {").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            fo foVar2 = new fo(this.mv, this.mw + 2);
            for (Entry entry : map.entrySet()) {
                foVar.a('(', null);
                foVar2.a(entry.getKey(), null);
                foVar2.a(entry.getValue(), null);
                foVar.a(')', null);
            }
            a('}', null);
            return this;
        }
    }

    public <T> fo a(T[] tArr, String str) {
        ad(str);
        if (tArr == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (tArr.length != 0) {
            this.mv.append(tArr.length).append(", [").append('\n');
            fo foVar = new fo(this.mv, this.mw + 1);
            for (Object a : tArr) {
                foVar.a(a, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(tArr.length).append(", []").append('\n');
            return this;
        }
    }

    public <T> fo a(Collection<T> collection, String str) {
        if (collection != null) {
            return a(collection.toArray(), str);
        }
        ad(str);
        this.mv.append("null").append('\t');
        return this;
    }

    public <T> fo a(T t, String str) {
        if (t == null) {
            this.mv.append("null").append('\n');
        } else if (t instanceof Byte) {
            a(((Byte) t).byteValue(), str);
        } else if (t instanceof Boolean) {
            a(((Boolean) t).booleanValue(), str);
        } else if (t instanceof Short) {
            a(((Short) t).shortValue(), str);
        } else if (t instanceof Integer) {
            a(((Integer) t).intValue(), str);
        } else if (t instanceof Long) {
            a(((Long) t).longValue(), str);
        } else if (t instanceof Float) {
            a(((Float) t).floatValue(), str);
        } else if (t instanceof Double) {
            a(((Double) t).doubleValue(), str);
        } else if (t instanceof String) {
            a((String) t, str);
        } else if (t instanceof Map) {
            a((Map) t, str);
        } else if (t instanceof List) {
            a((List) t, str);
        } else if (t instanceof fs) {
            a((fs) t, str);
        } else if (t instanceof byte[]) {
            a((byte[]) t, str);
        } else if (t instanceof boolean[]) {
            a((boolean[]) t, str);
        } else if (t instanceof short[]) {
            a((short[]) t, str);
        } else if (t instanceof int[]) {
            a((int[]) t, str);
        } else if (t instanceof long[]) {
            a((long[]) t, str);
        } else if (t instanceof float[]) {
            a((float[]) t, str);
        } else if (t instanceof double[]) {
            a((double[]) t, str);
        } else if (t.getClass().isArray()) {
            a((Object[]) t, str);
        } else {
            throw new fp("write object error: unsupport type.");
        }
        return this;
    }

    public fo a(fs fsVar, String str) {
        a('{', str);
        if (fsVar != null) {
            fsVar.display(this.mv, this.mw + 1);
        } else {
            this.mv.append('\t').append("null");
        }
        a('}', null);
        return this;
    }
}
