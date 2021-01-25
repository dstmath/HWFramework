package ohos.global.icu.impl.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.StandardPlural;

public interface Modifier {

    public static class Parameters {
        public ModifierStore obj;
        public StandardPlural plural;
        public int signum;
    }

    int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2);

    boolean containsField(Format.Field field);

    int getCodePointCount();

    Parameters getParameters();

    int getPrefixLength();

    boolean isStrong();

    boolean semanticallyEquivalent(Modifier modifier);
}
