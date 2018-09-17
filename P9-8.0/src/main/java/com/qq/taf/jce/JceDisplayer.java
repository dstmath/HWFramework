package com.qq.taf.jce;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JceDisplayer {
    private StringBuilder mS;
    private int mT = 0;

    private void F(String fieldName) {
        for (int i = 0; i < this.mT; i++) {
            this.mS.append(9);
        }
        if (fieldName != null) {
            this.mS.append(fieldName).append(": ");
        }
    }

    public JceDisplayer(StringBuilder sb, int level) {
        this.mS = sb;
        this.mT = level;
    }

    public JceDisplayer(StringBuilder sb) {
        this.mS = sb;
    }

    public JceDisplayer display(boolean b, String fieldName) {
        F(fieldName);
        this.mS.append(!b ? 'F' : 'T').append(10);
        return this;
    }

    public JceDisplayer displaySimple(boolean b, boolean bSep) {
        this.mS.append(!b ? 'F' : 'T');
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(byte n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(byte n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(char n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(char n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(short n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(short n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(int n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(int n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(long n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(long n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(float n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(float n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(double n, String fieldName) {
        F(fieldName);
        this.mS.append(n).append(10);
        return this;
    }

    public JceDisplayer displaySimple(double n, boolean bSep) {
        this.mS.append(n);
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(String s, String fieldName) {
        F(fieldName);
        if (s != null) {
            this.mS.append(s).append(10);
        } else {
            this.mS.append("null").append(10);
        }
        return this;
    }

    public JceDisplayer displaySimple(String s, boolean bSep) {
        if (s != null) {
            this.mS.append(s);
        } else {
            this.mS.append("null");
        }
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(byte[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            byte[] bArr = v;
            for (byte o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(byte[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append(a.c(v));
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(char[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            char[] cArr = v;
            for (char o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(char[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append(new String(v));
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(short[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            short[] sArr = v;
            for (short o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(short[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            short o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(int[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            int[] iArr = v;
            for (int o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(int[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            int o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(long[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            long[] jArr = v;
            for (long o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(long[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            long o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(float[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            float[] fArr = v;
            for (float o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(float[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            float o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public JceDisplayer display(double[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            double[] dArr = v;
            for (double o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public JceDisplayer displaySimple(double[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            double o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("[");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public <K, V> JceDisplayer display(Map<K, V> m, String fieldName) {
        F(fieldName);
        if (m == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (m.isEmpty()) {
            this.mS.append(m.size()).append(", {}").append(10);
            return this;
        } else {
            this.mS.append(m.size()).append(", {").append(10);
            JceDisplayer jd1 = new JceDisplayer(this.mS, this.mT + 1);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 2);
            for (Entry en : m.entrySet()) {
                jd1.display('(', null);
                jd.display(en.getKey(), null);
                jd.display(en.getValue(), null);
                jd1.display(')', null);
            }
            display('}', null);
            return this;
        }
    }

    public <K, V> JceDisplayer displaySimple(Map<K, V> m, boolean bSep) {
        if (m == null || m.isEmpty()) {
            this.mS.append("{}");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("{");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 2);
        boolean first = true;
        for (Entry en : m.entrySet()) {
            if (!first) {
                this.mS.append(",");
            }
            jd.displaySimple(en.getKey(), true);
            jd.displaySimple(en.getValue(), false);
            first = false;
        }
        this.mS.append("}");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public <T> JceDisplayer display(T[] v, String fieldName) {
        F(fieldName);
        if (v == null) {
            this.mS.append("null").append(10);
            return this;
        } else if (v.length != 0) {
            this.mS.append(v.length).append(", [").append(10);
            JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
            T[] tArr = v;
            for (Object o : v) {
                jd.display(o, null);
            }
            display(']', null);
            return this;
        } else {
            this.mS.append(v.length).append(", []").append(10);
            return this;
        }
    }

    public <T> JceDisplayer displaySimple(T[] v, boolean bSep) {
        if (v == null || v.length == 0) {
            this.mS.append("[]");
            if (bSep) {
                this.mS.append("|");
            }
            return this;
        }
        this.mS.append("[");
        JceDisplayer jd = new JceDisplayer(this.mS, this.mT + 1);
        for (int i = 0; i < v.length; i++) {
            Object o = v[i];
            if (i != 0) {
                this.mS.append("|");
            }
            jd.displaySimple(o, false);
        }
        this.mS.append("]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public <T> JceDisplayer display(Collection<T> v, String fieldName) {
        if (v != null) {
            return display(v.toArray(), fieldName);
        }
        F(fieldName);
        this.mS.append("null").append(9);
        return this;
    }

    public <T> JceDisplayer displaySimple(Collection<T> v, boolean bSep) {
        if (v != null) {
            return displaySimple(v.toArray(), bSep);
        }
        this.mS.append("[]");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }

    public <T> JceDisplayer display(T o, String fieldName) {
        if (o == null) {
            this.mS.append("null").append(10);
        } else if (o instanceof Byte) {
            display(((Byte) o).byteValue(), fieldName);
        } else if (o instanceof Boolean) {
            display(((Boolean) o).booleanValue(), fieldName);
        } else if (o instanceof Short) {
            display(((Short) o).shortValue(), fieldName);
        } else if (o instanceof Integer) {
            display(((Integer) o).intValue(), fieldName);
        } else if (o instanceof Long) {
            display(((Long) o).longValue(), fieldName);
        } else if (o instanceof Float) {
            display(((Float) o).floatValue(), fieldName);
        } else if (o instanceof Double) {
            display(((Double) o).doubleValue(), fieldName);
        } else if (o instanceof String) {
            display((String) o, fieldName);
        } else if (o instanceof Map) {
            display((Map) o, fieldName);
        } else if (o instanceof List) {
            display((List) o, fieldName);
        } else if (o instanceof JceStruct) {
            display((JceStruct) o, fieldName);
        } else if (o instanceof byte[]) {
            display((byte[]) o, fieldName);
        } else if (o instanceof boolean[]) {
            display((boolean[]) o, fieldName);
        } else if (o instanceof short[]) {
            display((short[]) o, fieldName);
        } else if (o instanceof int[]) {
            display((int[]) o, fieldName);
        } else if (o instanceof long[]) {
            display((long[]) o, fieldName);
        } else if (o instanceof float[]) {
            display((float[]) o, fieldName);
        } else if (o instanceof double[]) {
            display((double[]) o, fieldName);
        } else if (o.getClass().isArray()) {
            display((Object[]) o, fieldName);
        } else {
            throw new c("write object error: unsupport type.");
        }
        return this;
    }

    public <T> JceDisplayer displaySimple(T o, boolean bSep) {
        if (o == null) {
            this.mS.append("null").append(10);
        } else if (o instanceof Byte) {
            displaySimple(((Byte) o).byteValue(), bSep);
        } else if (o instanceof Boolean) {
            displaySimple(((Boolean) o).booleanValue(), bSep);
        } else if (o instanceof Short) {
            displaySimple(((Short) o).shortValue(), bSep);
        } else if (o instanceof Integer) {
            displaySimple(((Integer) o).intValue(), bSep);
        } else if (o instanceof Long) {
            displaySimple(((Long) o).longValue(), bSep);
        } else if (o instanceof Float) {
            displaySimple(((Float) o).floatValue(), bSep);
        } else if (o instanceof Double) {
            displaySimple(((Double) o).doubleValue(), bSep);
        } else if (o instanceof String) {
            displaySimple((String) o, bSep);
        } else if (o instanceof Map) {
            displaySimple((Map) o, bSep);
        } else if (o instanceof List) {
            displaySimple((List) o, bSep);
        } else if (o instanceof JceStruct) {
            displaySimple((JceStruct) o, bSep);
        } else if (o instanceof byte[]) {
            displaySimple((byte[]) o, bSep);
        } else if (o instanceof boolean[]) {
            displaySimple((boolean[]) o, bSep);
        } else if (o instanceof short[]) {
            displaySimple((short[]) o, bSep);
        } else if (o instanceof int[]) {
            displaySimple((int[]) o, bSep);
        } else if (o instanceof long[]) {
            displaySimple((long[]) o, bSep);
        } else if (o instanceof float[]) {
            displaySimple((float[]) o, bSep);
        } else if (o instanceof double[]) {
            displaySimple((double[]) o, bSep);
        } else if (o.getClass().isArray()) {
            displaySimple((Object[]) o, bSep);
        } else {
            throw new c("write object error: unsupport type.");
        }
        return this;
    }

    public JceDisplayer display(JceStruct v, String fieldName) {
        display('{', fieldName);
        if (v != null) {
            v.display(this.mS, this.mT + 1);
        } else {
            this.mS.append(9).append("null");
        }
        display('}', null);
        return this;
    }

    public JceDisplayer displaySimple(JceStruct v, boolean bSep) {
        this.mS.append("{");
        if (v != null) {
            v.displaySimple(this.mS, this.mT + 1);
        } else {
            this.mS.append(9).append("null");
        }
        this.mS.append("}");
        if (bSep) {
            this.mS.append("|");
        }
        return this;
    }
}
