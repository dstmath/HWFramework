package ohos.global.icu.text;

import ohos.global.icu.lang.UCharacter;

public class BidiTransform {
    private Bidi bidi;
    private int reorderingOptions;
    private int shapingOptions;
    private String text;

    public enum Mirroring {
        OFF,
        ON
    }

    public enum Order {
        LOGICAL,
        VISUAL
    }

    /* access modifiers changed from: private */
    public enum ReorderingScheme {
        LOG_LTR_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsLogical(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.shapeArabic(0, 0);
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.reorder();
            }
        },
        LOG_RTL_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsLogical(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 1, 0);
                bidiTransform.reorder();
                bidiTransform.shapeArabic(0, 4);
            }
        },
        LOG_LTR_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsLogical(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.shapeArabic(0, 0);
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.reorder();
                bidiTransform.reverse();
            }
        },
        LOG_RTL_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsLogical(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 1, 0);
                bidiTransform.reorder();
                bidiTransform.shapeArabic(0, 4);
                bidiTransform.reverse();
            }
        },
        VIS_LTR_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsVisual(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.shapeArabic(0, 4);
                bidiTransform.resolve((byte) 1, 5);
                bidiTransform.reorder();
            }
        },
        VIS_RTL_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsVisual(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.reverse();
                bidiTransform.shapeArabic(0, 4);
                bidiTransform.resolve((byte) 1, 5);
                bidiTransform.reorder();
            }
        },
        VIS_LTR_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsVisual(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 0, 5);
                bidiTransform.reorder();
                bidiTransform.shapeArabic(0, 0);
            }
        },
        VIS_RTL_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsVisual(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.reverse();
                bidiTransform.resolve((byte) 0, 5);
                bidiTransform.reorder();
                bidiTransform.shapeArabic(0, 0);
            }
        },
        LOG_LTR_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsLogical(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.shapeArabic(0, 0);
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.resolve((byte) 0, 3);
                bidiTransform.reorder();
            }
        },
        LOG_RTL_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsLogical(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 1, 0);
                bidiTransform.mirror();
                bidiTransform.resolve((byte) 1, 3);
                bidiTransform.reorder();
                bidiTransform.shapeArabic(0, 0);
            }
        },
        VIS_LTR_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsVisual(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(0, 4);
                bidiTransform.reverse();
            }
        },
        VIS_RTL_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsVisual(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.reverse();
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(0, 4);
            }
        },
        LOG_LTR_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsLogical(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(0, 0);
            }
        },
        LOG_RTL_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsLogical(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsLogical(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 1, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(4, 0);
            }
        },
        VIS_LTR_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsLTR(b) && BidiTransform.IsVisual(order) && BidiTransform.IsLTR(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(0, 4);
            }
        },
        VIS_RTL_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public boolean matches(byte b, Order order, byte b2, Order order2) {
                return BidiTransform.IsRTL(b) && BidiTransform.IsVisual(order) && BidiTransform.IsRTL(b2) && BidiTransform.IsVisual(order2);
            }

            /* access modifiers changed from: package-private */
            @Override // ohos.global.icu.text.BidiTransform.ReorderingScheme
            public void doTransform(BidiTransform bidiTransform) {
                bidiTransform.reverse();
                bidiTransform.resolve((byte) 0, 0);
                bidiTransform.mirror();
                bidiTransform.shapeArabic(0, 4);
                bidiTransform.reverse();
            }
        };

        /* access modifiers changed from: package-private */
        public abstract void doTransform(BidiTransform bidiTransform);

        /* access modifiers changed from: package-private */
        public abstract boolean matches(byte b, Order order, byte b2, Order order2);
    }

    /* access modifiers changed from: private */
    public static boolean IsLTR(byte b) {
        return (b & 1) == 0;
    }

    /* access modifiers changed from: private */
    public static boolean IsRTL(byte b) {
        return (b & 1) == 1;
    }

    public String transform(CharSequence charSequence, byte b, Order order, byte b2, Order order2, Mirroring mirroring, int i) {
        if (charSequence == null || order == null || order2 == null || mirroring == null) {
            throw new IllegalArgumentException();
        }
        this.text = charSequence.toString();
        int i2 = 2;
        byte[] bArr = {b, b2};
        resolveBaseDirection(bArr);
        ReorderingScheme findMatchingScheme = findMatchingScheme(bArr[0], order, bArr[1], order2);
        if (findMatchingScheme != null) {
            this.bidi = new Bidi();
            if (!Mirroring.ON.equals(mirroring)) {
                i2 = 0;
            }
            this.reorderingOptions = i2;
            this.shapingOptions = i & -5;
            findMatchingScheme.doTransform(this);
        }
        return this.text;
    }

    private void resolveBaseDirection(byte[] bArr) {
        if (Bidi.IsDefaultLevel(bArr[0])) {
            byte baseDirection = Bidi.getBaseDirection(this.text);
            if (baseDirection == 3) {
                baseDirection = bArr[0] == Byte.MAX_VALUE ? (byte) 1 : 0;
            }
            bArr[0] = baseDirection;
        } else {
            bArr[0] = (byte) (bArr[0] & 1);
        }
        if (Bidi.IsDefaultLevel(bArr[1])) {
            bArr[1] = bArr[0];
        } else {
            bArr[1] = (byte) (bArr[1] & 1);
        }
    }

    private ReorderingScheme findMatchingScheme(byte b, Order order, byte b2, Order order2) {
        ReorderingScheme[] values = ReorderingScheme.values();
        for (ReorderingScheme reorderingScheme : values) {
            if (reorderingScheme.matches(b, order, b2, order2)) {
                return reorderingScheme;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resolve(byte b, int i) {
        this.bidi.setInverse((i & 5) != 0);
        this.bidi.setReorderingMode(i);
        this.bidi.setPara(this.text, b, (byte[]) null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reorder() {
        this.text = this.bidi.writeReordered(this.reorderingOptions);
        this.reorderingOptions = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reverse() {
        this.text = Bidi.writeReverse(this.text, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void mirror() {
        if ((this.reorderingOptions & 2) != 0) {
            StringBuffer stringBuffer = new StringBuffer(this.text);
            byte[] levels = this.bidi.getLevels();
            int i = 0;
            int length = levels.length;
            while (i < length) {
                int charAt = UTF16.charAt(stringBuffer, i);
                if ((levels[i] & 1) != 0) {
                    UTF16.setCharAt(stringBuffer, i, UCharacter.getMirror(charAt));
                }
                i += UTF16.getCharCount(charAt);
            }
            this.text = stringBuffer.toString();
            this.reorderingOptions &= -3;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void shapeArabic(int i, int i2) {
        if (i == i2) {
            shapeArabic(i | this.shapingOptions);
            return;
        }
        shapeArabic(i | (this.shapingOptions & -25));
        shapeArabic((this.shapingOptions & -225) | i2);
    }

    private void shapeArabic(int i) {
        if (i != 0) {
            try {
                this.text = new ArabicShaping(i).shape(this.text);
            } catch (ArabicShapingException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean IsLogical(Order order) {
        return Order.LOGICAL.equals(order);
    }

    /* access modifiers changed from: private */
    public static boolean IsVisual(Order order) {
        return Order.VISUAL.equals(order);
    }
}
