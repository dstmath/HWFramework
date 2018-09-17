package android.icu.text;

import android.icu.impl.StandardPlural;
import android.icu.util.Freezable;
import android.icu.util.Output;
import java.util.Arrays;
import java.util.EnumSet;

@Deprecated
public final class PluralRanges implements Freezable<PluralRanges>, Comparable<PluralRanges> {
    private boolean[] explicit = new boolean[StandardPlural.COUNT];
    private volatile boolean isFrozen;
    private Matrix matrix = new Matrix();

    private static final class Matrix implements Comparable<Matrix>, Cloneable {
        private byte[] data = new byte[(StandardPlural.COUNT * StandardPlural.COUNT)];

        Matrix() {
            for (int i = 0; i < this.data.length; i++) {
                this.data[i] = (byte) -1;
            }
        }

        void set(StandardPlural start, StandardPlural end, StandardPlural result) {
            byte b;
            byte[] bArr = this.data;
            int ordinal = end.ordinal() + (start.ordinal() * StandardPlural.COUNT);
            if (result == null) {
                b = (byte) -1;
            } else {
                b = (byte) result.ordinal();
            }
            bArr[ordinal] = b;
        }

        void setIfNew(StandardPlural start, StandardPlural end, StandardPlural result) {
            byte old = this.data[(start.ordinal() * StandardPlural.COUNT) + end.ordinal()];
            if (old >= (byte) 0) {
                throw new IllegalArgumentException("Previously set value for <" + start + ", " + end + ", " + StandardPlural.VALUES.get(old) + ">");
            }
            byte b;
            byte[] bArr = this.data;
            int ordinal = end.ordinal() + (start.ordinal() * StandardPlural.COUNT);
            if (result == null) {
                b = (byte) -1;
            } else {
                b = (byte) result.ordinal();
            }
            bArr[ordinal] = b;
        }

        StandardPlural get(StandardPlural start, StandardPlural end) {
            byte result = this.data[(start.ordinal() * StandardPlural.COUNT) + end.ordinal()];
            return result < (byte) 0 ? null : (StandardPlural) StandardPlural.VALUES.get(result);
        }

        StandardPlural endSame(StandardPlural end) {
            StandardPlural first = null;
            for (StandardPlural start : StandardPlural.VALUES) {
                StandardPlural item = get(start, end);
                if (item != null) {
                    if (first == null) {
                        first = item;
                    } else if (first != item) {
                        return null;
                    }
                }
            }
            return first;
        }

        StandardPlural startSame(StandardPlural start, EnumSet<StandardPlural> endDone, Output<Boolean> emit) {
            emit.value = Boolean.valueOf(false);
            StandardPlural first = null;
            for (StandardPlural end : StandardPlural.VALUES) {
                StandardPlural item = get(start, end);
                if (item != null) {
                    if (first == null) {
                        first = item;
                    } else if (first != item) {
                        return null;
                    } else {
                        if (!endDone.contains(end)) {
                            emit.value = Boolean.valueOf(true);
                        }
                    }
                }
            }
            return first;
        }

        public int hashCode() {
            int result = 0;
            for (byte b : this.data) {
                result = (result * 37) + b;
            }
            return result;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof Matrix)) {
                return false;
            }
            if (compareTo((Matrix) other) == 0) {
                z = true;
            }
            return z;
        }

        public int compareTo(Matrix o) {
            for (int i = 0; i < this.data.length; i++) {
                int diff = this.data[i] - o.data[i];
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        }

        public Matrix clone() {
            Matrix result = new Matrix();
            result.data = (byte[]) this.data.clone();
            return result;
        }

        public String toString() {
            StringBuilder result = new StringBuilder();
            for (StandardPlural i : StandardPlural.values()) {
                for (StandardPlural j : StandardPlural.values()) {
                    StandardPlural x = get(i, j);
                    if (x != null) {
                        result.append(i).append(" & ").append(j).append(" → ").append(x).append(";\n");
                    }
                }
            }
            return result.toString();
        }
    }

    @Deprecated
    public void add(StandardPlural rangeStart, StandardPlural rangeEnd, StandardPlural result) {
        int i = 0;
        if (this.isFrozen) {
            throw new UnsupportedOperationException();
        }
        this.explicit[result.ordinal()] = true;
        int length;
        if (rangeStart == null) {
            for (StandardPlural rs : StandardPlural.values()) {
                if (rangeEnd == null) {
                    for (StandardPlural re : StandardPlural.values()) {
                        this.matrix.setIfNew(rs, re, result);
                    }
                } else {
                    this.explicit[rangeEnd.ordinal()] = true;
                    this.matrix.setIfNew(rs, rangeEnd, result);
                }
            }
        } else if (rangeEnd == null) {
            this.explicit[rangeStart.ordinal()] = true;
            StandardPlural[] values = StandardPlural.values();
            length = values.length;
            while (i < length) {
                this.matrix.setIfNew(rangeStart, values[i], result);
                i++;
            }
        } else {
            this.explicit[rangeStart.ordinal()] = true;
            this.explicit[rangeEnd.ordinal()] = true;
            this.matrix.setIfNew(rangeStart, rangeEnd, result);
        }
    }

    @Deprecated
    public StandardPlural get(StandardPlural start, StandardPlural end) {
        StandardPlural result = this.matrix.get(start, end);
        return result == null ? end : result;
    }

    @Deprecated
    public boolean isExplicit(StandardPlural start, StandardPlural end) {
        return this.matrix.get(start, end) != null;
    }

    @Deprecated
    public boolean isExplicitlySet(StandardPlural count) {
        return this.explicit[count.ordinal()];
    }

    @Deprecated
    public boolean equals(Object other) {
        boolean z = false;
        if (this == other) {
            return true;
        }
        if (!(other instanceof PluralRanges)) {
            return false;
        }
        PluralRanges otherPR = (PluralRanges) other;
        if (this.matrix.equals(otherPR.matrix)) {
            z = Arrays.equals(this.explicit, otherPR.explicit);
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        return this.matrix.hashCode();
    }

    @Deprecated
    public int compareTo(PluralRanges that) {
        return this.matrix.compareTo(that.matrix);
    }

    @Deprecated
    public boolean isFrozen() {
        return this.isFrozen;
    }

    @Deprecated
    public PluralRanges freeze() {
        this.isFrozen = true;
        return this;
    }

    @Deprecated
    public PluralRanges cloneAsThawed() {
        PluralRanges result = new PluralRanges();
        result.explicit = (boolean[]) this.explicit.clone();
        result.matrix = this.matrix.clone();
        return result;
    }

    @Deprecated
    public String toString() {
        return this.matrix.toString();
    }
}
