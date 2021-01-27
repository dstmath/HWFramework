package ohos.global.icu.text;

import java.util.Arrays;
import java.util.EnumSet;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.Output;

@Deprecated
public final class PluralRanges implements Freezable<PluralRanges>, Comparable<PluralRanges> {
    private boolean[] explicit = new boolean[StandardPlural.COUNT];
    private volatile boolean isFrozen;
    private Matrix matrix = new Matrix();

    /* access modifiers changed from: private */
    public static final class Matrix implements Comparable<Matrix>, Cloneable {
        private byte[] data = new byte[(StandardPlural.COUNT * StandardPlural.COUNT)];

        Matrix() {
            int i = 0;
            while (true) {
                byte[] bArr = this.data;
                if (i < bArr.length) {
                    bArr[i] = -1;
                    i++;
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void set(StandardPlural standardPlural, StandardPlural standardPlural2, StandardPlural standardPlural3) {
            byte b;
            byte[] bArr = this.data;
            int ordinal = (standardPlural.ordinal() * StandardPlural.COUNT) + standardPlural2.ordinal();
            if (standardPlural3 == null) {
                b = -1;
            } else {
                b = (byte) standardPlural3.ordinal();
            }
            bArr[ordinal] = b;
        }

        /* access modifiers changed from: package-private */
        public void setIfNew(StandardPlural standardPlural, StandardPlural standardPlural2, StandardPlural standardPlural3) {
            byte b;
            byte b2 = this.data[(standardPlural.ordinal() * StandardPlural.COUNT) + standardPlural2.ordinal()];
            if (b2 < 0) {
                byte[] bArr = this.data;
                int ordinal = (standardPlural.ordinal() * StandardPlural.COUNT) + standardPlural2.ordinal();
                if (standardPlural3 == null) {
                    b = -1;
                } else {
                    b = (byte) standardPlural3.ordinal();
                }
                bArr[ordinal] = b;
                return;
            }
            throw new IllegalArgumentException("Previously set value for <" + standardPlural + ", " + standardPlural2 + ", " + StandardPlural.VALUES.get(b2) + ">");
        }

        /* access modifiers changed from: package-private */
        public StandardPlural get(StandardPlural standardPlural, StandardPlural standardPlural2) {
            byte b = this.data[(standardPlural.ordinal() * StandardPlural.COUNT) + standardPlural2.ordinal()];
            if (b < 0) {
                return null;
            }
            return (StandardPlural) StandardPlural.VALUES.get(b);
        }

        /* access modifiers changed from: package-private */
        public StandardPlural endSame(StandardPlural standardPlural) {
            StandardPlural standardPlural2 = null;
            for (StandardPlural standardPlural3 : StandardPlural.VALUES) {
                StandardPlural standardPlural4 = get(standardPlural3, standardPlural);
                if (standardPlural4 != null) {
                    if (standardPlural2 == null) {
                        standardPlural2 = standardPlural4;
                    } else if (standardPlural2 != standardPlural4) {
                        return null;
                    }
                }
            }
            return standardPlural2;
        }

        /* access modifiers changed from: package-private */
        public StandardPlural startSame(StandardPlural standardPlural, EnumSet<StandardPlural> enumSet, Output<Boolean> output) {
            output.value = (T) false;
            StandardPlural standardPlural2 = null;
            for (StandardPlural standardPlural3 : StandardPlural.VALUES) {
                StandardPlural standardPlural4 = get(standardPlural, standardPlural3);
                if (standardPlural4 != null) {
                    if (standardPlural2 == null) {
                        standardPlural2 = standardPlural4;
                    } else if (standardPlural2 != standardPlural4) {
                        return null;
                    } else {
                        if (!enumSet.contains(standardPlural3)) {
                            output.value = (T) true;
                        }
                    }
                }
            }
            return standardPlural2;
        }

        @Override // java.lang.Object
        public int hashCode() {
            int i = 0;
            int i2 = 0;
            while (true) {
                byte[] bArr = this.data;
                if (i >= bArr.length) {
                    return i2;
                }
                i2 = (i2 * 37) + bArr[i];
                i++;
            }
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            if ((obj instanceof Matrix) && compareTo((Matrix) obj) == 0) {
                return true;
            }
            return false;
        }

        public int compareTo(Matrix matrix) {
            int i = 0;
            while (true) {
                byte[] bArr = this.data;
                if (i >= bArr.length) {
                    return 0;
                }
                int i2 = bArr[i] - matrix.data[i];
                if (i2 != 0) {
                    return i2;
                }
                i++;
            }
        }

        @Override // java.lang.Object
        public Matrix clone() {
            Matrix matrix = new Matrix();
            matrix.data = (byte[]) this.data.clone();
            return matrix;
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder();
            StandardPlural[] values = StandardPlural.values();
            for (StandardPlural standardPlural : values) {
                StandardPlural[] values2 = StandardPlural.values();
                for (StandardPlural standardPlural2 : values2) {
                    StandardPlural standardPlural3 = get(standardPlural, standardPlural2);
                    if (standardPlural3 != null) {
                        sb.append(standardPlural + " & " + standardPlural2 + " → " + standardPlural3 + ";\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    @Deprecated
    public void add(StandardPlural standardPlural, StandardPlural standardPlural2, StandardPlural standardPlural3) {
        if (!this.isFrozen) {
            this.explicit[standardPlural3.ordinal()] = true;
            if (standardPlural == null) {
                StandardPlural[] values = StandardPlural.values();
                for (StandardPlural standardPlural4 : values) {
                    if (standardPlural2 == null) {
                        for (StandardPlural standardPlural5 : StandardPlural.values()) {
                            this.matrix.setIfNew(standardPlural4, standardPlural5, standardPlural3);
                        }
                    } else {
                        this.explicit[standardPlural2.ordinal()] = true;
                        this.matrix.setIfNew(standardPlural4, standardPlural2, standardPlural3);
                    }
                }
            } else if (standardPlural2 == null) {
                this.explicit[standardPlural.ordinal()] = true;
                for (StandardPlural standardPlural6 : StandardPlural.values()) {
                    this.matrix.setIfNew(standardPlural, standardPlural6, standardPlural3);
                }
            } else {
                this.explicit[standardPlural.ordinal()] = true;
                this.explicit[standardPlural2.ordinal()] = true;
                this.matrix.setIfNew(standardPlural, standardPlural2, standardPlural3);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Deprecated
    public StandardPlural get(StandardPlural standardPlural, StandardPlural standardPlural2) {
        StandardPlural standardPlural3 = this.matrix.get(standardPlural, standardPlural2);
        return standardPlural3 == null ? standardPlural2 : standardPlural3;
    }

    @Deprecated
    public boolean isExplicit(StandardPlural standardPlural, StandardPlural standardPlural2) {
        return this.matrix.get(standardPlural, standardPlural2) != null;
    }

    @Deprecated
    public boolean isExplicitlySet(StandardPlural standardPlural) {
        return this.explicit[standardPlural.ordinal()];
    }

    @Override // java.lang.Object
    @Deprecated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PluralRanges)) {
            return false;
        }
        PluralRanges pluralRanges = (PluralRanges) obj;
        return this.matrix.equals(pluralRanges.matrix) && Arrays.equals(this.explicit, pluralRanges.explicit);
    }

    @Override // java.lang.Object
    @Deprecated
    public int hashCode() {
        return this.matrix.hashCode();
    }

    @Deprecated
    public int compareTo(PluralRanges pluralRanges) {
        return this.matrix.compareTo(pluralRanges.matrix);
    }

    @Override // ohos.global.icu.util.Freezable
    @Deprecated
    public boolean isFrozen() {
        return this.isFrozen;
    }

    @Override // ohos.global.icu.util.Freezable
    @Deprecated
    public PluralRanges freeze() {
        this.isFrozen = true;
        return this;
    }

    @Override // ohos.global.icu.util.Freezable
    @Deprecated
    public PluralRanges cloneAsThawed() {
        PluralRanges pluralRanges = new PluralRanges();
        pluralRanges.explicit = (boolean[]) this.explicit.clone();
        pluralRanges.matrix = this.matrix.clone();
        return pluralRanges;
    }

    @Override // java.lang.Object
    @Deprecated
    public String toString() {
        return this.matrix.toString();
    }
}
