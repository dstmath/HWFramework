package android.icu.impl.number;

public interface Modifier {
    int apply(NumberStringBuilder numberStringBuilder, int i, int i2);

    int getCodePointCount();

    int getPrefixLength();

    boolean isStrong();
}
