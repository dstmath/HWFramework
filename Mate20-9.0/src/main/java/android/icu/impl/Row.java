package android.icu.impl;

import android.icu.util.Freezable;

public class Row<C0, C1, C2, C3, C4> implements Comparable, Cloneable, Freezable<Row<C0, C1, C2, C3, C4>> {
    protected volatile boolean frozen;
    protected Object[] items;

    public static class R2<C0, C1> extends Row<C0, C1, C1, C1, C1> {
        public R2(C0 a, C1 b) {
            this.items = new Object[]{a, b};
        }
    }

    public static class R3<C0, C1, C2> extends Row<C0, C1, C2, C2, C2> {
        public R3(C0 a, C1 b, C2 c) {
            this.items = new Object[]{a, b, c};
        }
    }

    public static class R4<C0, C1, C2, C3> extends Row<C0, C1, C2, C3, C3> {
        public R4(C0 a, C1 b, C2 c, C3 d) {
            this.items = new Object[]{a, b, c, d};
        }
    }

    public static class R5<C0, C1, C2, C3, C4> extends Row<C0, C1, C2, C3, C4> {
        public R5(C0 a, C1 b, C2 c, C3 d, C4 e) {
            this.items = new Object[]{a, b, c, d, e};
        }
    }

    public static <C0, C1> R2<C0, C1> of(C0 p0, C1 p1) {
        return new R2<>(p0, p1);
    }

    public static <C0, C1, C2> R3<C0, C1, C2> of(C0 p0, C1 p1, C2 p2) {
        return new R3<>(p0, p1, p2);
    }

    public static <C0, C1, C2, C3> R4<C0, C1, C2, C3> of(C0 p0, C1 p1, C2 p2, C3 p3) {
        return new R4<>(p0, p1, p2, p3);
    }

    public static <C0, C1, C2, C3, C4> R5<C0, C1, C2, C3, C4> of(C0 p0, C1 p1, C2 p2, C3 p3, C4 p4) {
        R5 r5 = new R5(p0, p1, p2, p3, p4);
        return r5;
    }

    public Row<C0, C1, C2, C3, C4> set0(C0 item) {
        return set(0, item);
    }

    public C0 get0() {
        return this.items[0];
    }

    public Row<C0, C1, C2, C3, C4> set1(C1 item) {
        return set(1, item);
    }

    public C1 get1() {
        return this.items[1];
    }

    public Row<C0, C1, C2, C3, C4> set2(C2 item) {
        return set(2, item);
    }

    public C2 get2() {
        return this.items[2];
    }

    public Row<C0, C1, C2, C3, C4> set3(C3 item) {
        return set(3, item);
    }

    public C3 get3() {
        return this.items[3];
    }

    public Row<C0, C1, C2, C3, C4> set4(C4 item) {
        return set(4, item);
    }

    public C4 get4() {
        return this.items[4];
    }

    /* access modifiers changed from: protected */
    public Row<C0, C1, C2, C3, C4> set(int i, Object item) {
        if (!this.frozen) {
            this.items[i] = item;
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify frozen object");
    }

    public int hashCode() {
        int sum = this.items.length;
        for (Object item : this.items) {
            sum = (sum * 37) + Utility.checkHash(item);
        }
        return sum;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            Row<C0, C1, C2, C3, C4> that = (Row) other;
            if (this.items.length != that.items.length) {
                return false;
            }
            Object[] objArr = this.items;
            int length = objArr.length;
            int i = 0;
            int i2 = 0;
            while (i2 < length) {
                int i3 = i + 1;
                if (Utility.objectEquals(objArr[i2], that.items[i]) == 0) {
                    return false;
                }
                i2++;
                i = i3;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int compareTo(Object other) {
        Row<C0, C1, C2, C3, C4> that = (Row) other;
        int result = this.items.length - that.items.length;
        if (result != 0) {
            return result;
        }
        int i = 0;
        Object[] objArr = this.items;
        int length = objArr.length;
        int i2 = result;
        int result2 = 0;
        while (result2 < length) {
            int i3 = i + 1;
            int result3 = Utility.checkCompare((Comparable) objArr[result2], (Comparable) that.items[i]);
            if (result3 != 0) {
                return result3;
            }
            result2++;
            i = i3;
        }
        return 0;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;
        for (Object item : this.items) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(item);
        }
        result.append("]");
        return result.toString();
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public Row<C0, C1, C2, C3, C4> freeze() {
        this.frozen = true;
        return this;
    }

    public Object clone() {
        if (this.frozen) {
            return this;
        }
        try {
            Row<C0, C1, C2, C3, C4> result = (Row) super.clone();
            this.items = (Object[]) this.items.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Row<C0, C1, C2, C3, C4> cloneAsThawed() {
        try {
            Row<C0, C1, C2, C3, C4> result = (Row) super.clone();
            this.items = (Object[]) this.items.clone();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
