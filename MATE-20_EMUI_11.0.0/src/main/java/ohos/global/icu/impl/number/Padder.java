package ohos.global.icu.impl.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;

public class Padder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String FALLBACK_PADDING_STRING = " ";
    public static final Padder NONE = new Padder(null, -1, null);
    String paddingString;
    PadPosition position;
    int targetWidth;

    public enum PadPosition {
        BEFORE_PREFIX,
        AFTER_PREFIX,
        BEFORE_SUFFIX,
        AFTER_SUFFIX;

        public static PadPosition fromOld(int i) {
            if (i == 0) {
                return BEFORE_PREFIX;
            }
            if (i == 1) {
                return AFTER_PREFIX;
            }
            if (i == 2) {
                return BEFORE_SUFFIX;
            }
            if (i == 3) {
                return AFTER_SUFFIX;
            }
            throw new IllegalArgumentException("Don't know how to map " + i);
        }

        public int toOld() {
            int i = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[ordinal()];
            if (i == 1) {
                return 0;
            }
            if (i == 2) {
                return 1;
            }
            if (i != 3) {
                return i != 4 ? -1 : 3;
            }
            return 2;
        }
    }

    /* renamed from: ohos.global.icu.impl.number.Padder$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition = new int[PadPosition.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[PadPosition.BEFORE_PREFIX.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[PadPosition.AFTER_PREFIX.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[PadPosition.BEFORE_SUFFIX.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[PadPosition.AFTER_SUFFIX.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public Padder(String str, int i, PadPosition padPosition) {
        this.paddingString = str == null ? " " : str;
        this.targetWidth = i;
        this.position = padPosition == null ? PadPosition.BEFORE_PREFIX : padPosition;
    }

    public static Padder none() {
        return NONE;
    }

    public static Padder codePoints(int i, int i2, PadPosition padPosition) {
        if (i2 >= 0) {
            return new Padder(String.valueOf(Character.toChars(i)), i2, padPosition);
        }
        throw new IllegalArgumentException("Padding width must not be negative");
    }

    public static Padder forProperties(DecimalFormatProperties decimalFormatProperties) {
        return new Padder(decimalFormatProperties.getPadString(), decimalFormatProperties.getFormatWidth(), decimalFormatProperties.getPadPosition());
    }

    public boolean isValid() {
        return this.targetWidth > 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0059  */
    public int padAndApply(Modifier modifier, Modifier modifier2, FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        int addPaddingHelper;
        int addPaddingHelper2;
        int codePointCount = (this.targetWidth - (modifier.getCodePointCount() + modifier2.getCodePointCount())) - formattedStringBuilder.codePointCount();
        int i3 = 0;
        if (codePointCount <= 0) {
            int apply = modifier.apply(formattedStringBuilder, i, i2) + 0;
            return apply + modifier2.apply(formattedStringBuilder, i, i2 + apply);
        }
        if (this.position == PadPosition.AFTER_PREFIX) {
            addPaddingHelper2 = addPaddingHelper(this.paddingString, codePointCount, formattedStringBuilder, i);
        } else {
            if (this.position == PadPosition.BEFORE_SUFFIX) {
                addPaddingHelper2 = addPaddingHelper(this.paddingString, codePointCount, formattedStringBuilder, i2 + 0);
            }
            int apply2 = i3 + modifier.apply(formattedStringBuilder, i, i2 + i3);
            int apply3 = apply2 + modifier2.apply(formattedStringBuilder, i, i2 + apply2);
            if (this.position != PadPosition.BEFORE_PREFIX) {
                addPaddingHelper = addPaddingHelper(this.paddingString, codePointCount, formattedStringBuilder, i);
            } else if (this.position != PadPosition.AFTER_SUFFIX) {
                return apply3;
            } else {
                addPaddingHelper = addPaddingHelper(this.paddingString, codePointCount, formattedStringBuilder, i2 + apply3);
            }
            return apply3 + addPaddingHelper;
        }
        i3 = 0 + addPaddingHelper2;
        int apply22 = i3 + modifier.apply(formattedStringBuilder, i, i2 + i3);
        int apply32 = apply22 + modifier2.apply(formattedStringBuilder, i, i2 + apply22);
        if (this.position != PadPosition.BEFORE_PREFIX) {
        }
        return apply32 + addPaddingHelper;
    }

    private static int addPaddingHelper(String str, int i, FormattedStringBuilder formattedStringBuilder, int i2) {
        for (int i3 = 0; i3 < i; i3++) {
            formattedStringBuilder.insert(i2, str, (Format.Field) null);
        }
        return str.length() * i;
    }
}
