package android.icu.text;

import android.icu.lang.UCharacter;

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

    private enum ReorderingScheme {
        LOG_LTR_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.shapeArabic(0, 0);
                transform.resolve((byte) 0, 0);
                transform.reorder();
            }
        },
        LOG_RTL_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 1, 0);
                transform.reorder();
                transform.shapeArabic(0, 4);
            }
        },
        LOG_LTR_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.shapeArabic(0, 0);
                transform.resolve((byte) 0, 0);
                transform.reorder();
                transform.reverse();
            }
        },
        LOG_RTL_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 1, 0);
                transform.reorder();
                transform.shapeArabic(0, 4);
                transform.reverse();
            }
        },
        VIS_LTR_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.shapeArabic(0, 4);
                transform.resolve((byte) 1, 5);
                transform.reorder();
            }
        },
        VIS_RTL_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.shapeArabic(0, 4);
                transform.resolve((byte) 1, 5);
                transform.reorder();
            }
        },
        VIS_LTR_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 0, 5);
                transform.reorder();
                transform.shapeArabic(0, 0);
            }
        },
        VIS_RTL_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve((byte) 0, 5);
                transform.reorder();
                transform.shapeArabic(0, 0);
            }
        },
        LOG_LTR_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.shapeArabic(0, 0);
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.resolve((byte) 0, 3);
                transform.reorder();
            }
        },
        LOG_RTL_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 1, 0);
                transform.mirror();
                transform.resolve((byte) 1, 3);
                transform.reorder();
                transform.shapeArabic(0, 0);
            }
        },
        VIS_LTR_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.shapeArabic(0, 4);
                transform.reverse();
            }
        },
        VIS_RTL_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.shapeArabic(0, 4);
            }
        },
        LOG_LTR_TO_LOG_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.shapeArabic(0, 0);
            }
        },
        LOG_RTL_TO_LOG_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsLogical(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsLogical(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 1, 0);
                transform.mirror();
                transform.shapeArabic(4, 0);
            }
        },
        VIS_LTR_TO_VIS_LTR {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsLTR(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsLTR(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.shapeArabic(0, 4);
            }
        },
        VIS_RTL_TO_VIS_RTL {
            /* access modifiers changed from: package-private */
            public boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return BidiTransform.IsRTL(inLevel) && BidiTransform.IsVisual(inOrder) && BidiTransform.IsRTL(outLevel) && BidiTransform.IsVisual(outOrder);
            }

            /* access modifiers changed from: package-private */
            public void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve((byte) 0, 0);
                transform.mirror();
                transform.shapeArabic(0, 4);
                transform.reverse();
            }
        };

        /* access modifiers changed from: package-private */
        public abstract void doTransform(BidiTransform bidiTransform);

        /* access modifiers changed from: package-private */
        public abstract boolean matches(byte b, Order order, byte b2, Order order2);
    }

    public String transform(CharSequence text2, byte inParaLevel, Order inOrder, byte outParaLevel, Order outOrder, Mirroring doMirroring, int shapingOptions2) {
        if (text2 == null || inOrder == null || outOrder == null || doMirroring == null) {
            throw new IllegalArgumentException();
        }
        this.text = text2.toString();
        int i = 2;
        byte[] levels = {inParaLevel, outParaLevel};
        resolveBaseDirection(levels);
        ReorderingScheme currentScheme = findMatchingScheme(levels[0], inOrder, levels[1], outOrder);
        if (currentScheme != null) {
            this.bidi = new Bidi();
            if (!Mirroring.ON.equals(doMirroring)) {
                i = 0;
            }
            this.reorderingOptions = i;
            this.shapingOptions = shapingOptions2 & -5;
            currentScheme.doTransform(this);
        }
        return this.text;
    }

    private void resolveBaseDirection(byte[] levels) {
        if (Bidi.IsDefaultLevel(levels[0])) {
            byte level = Bidi.getBaseDirection(this.text);
            levels[0] = level != 3 ? level : levels[0] == Byte.MAX_VALUE ? (byte) 1 : 0;
        } else {
            levels[0] = (byte) (levels[0] & 1);
        }
        if (Bidi.IsDefaultLevel(levels[1])) {
            levels[1] = levels[0];
        } else {
            levels[1] = (byte) (levels[1] & 1);
        }
    }

    private ReorderingScheme findMatchingScheme(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
        for (ReorderingScheme scheme : ReorderingScheme.values()) {
            if (scheme.matches(inLevel, inOrder, outLevel, outOrder)) {
                return scheme;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void resolve(byte level, int options) {
        this.bidi.setInverse((options & 5) != 0);
        this.bidi.setReorderingMode(options);
        this.bidi.setPara(this.text, level, (byte[]) null);
    }

    /* access modifiers changed from: private */
    public void reorder() {
        this.text = this.bidi.writeReordered(this.reorderingOptions);
        this.reorderingOptions = 0;
    }

    /* access modifiers changed from: private */
    public void reverse() {
        this.text = Bidi.writeReverse(this.text, 0);
    }

    /* access modifiers changed from: private */
    public void mirror() {
        if ((this.reorderingOptions & 2) != 0) {
            StringBuffer sb = new StringBuffer(this.text);
            byte[] levels = this.bidi.getLevels();
            int i = 0;
            int n = levels.length;
            while (i < n) {
                int ch = UTF16.charAt(sb, i);
                if ((levels[i] & 1) != 0) {
                    UTF16.setCharAt(sb, i, UCharacter.getMirror(ch));
                }
                i += UTF16.getCharCount(ch);
            }
            this.text = sb.toString();
            this.reorderingOptions &= -3;
        }
    }

    /* access modifiers changed from: private */
    public void shapeArabic(int digitsDir, int lettersDir) {
        if (digitsDir == lettersDir) {
            shapeArabic(this.shapingOptions | digitsDir);
            return;
        }
        shapeArabic((this.shapingOptions & -25) | digitsDir);
        shapeArabic((this.shapingOptions & -225) | lettersDir);
    }

    private void shapeArabic(int options) {
        if (options != 0) {
            try {
                this.text = new ArabicShaping(options).shape(this.text);
            } catch (ArabicShapingException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean IsLTR(byte level) {
        return (level & 1) == 0;
    }

    /* access modifiers changed from: private */
    public static boolean IsRTL(byte level) {
        return (level & 1) == 1;
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
