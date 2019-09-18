package android.icu.impl.number;

import android.icu.impl.StandardPlural;

public class ParameterizedModifier {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    boolean frozen;
    final Modifier[] mods;
    private final Modifier negative;
    private final Modifier positive;

    public ParameterizedModifier(Modifier positive2, Modifier negative2) {
        this.positive = positive2;
        this.negative = negative2;
        this.mods = null;
        this.frozen = true;
    }

    public ParameterizedModifier() {
        this.positive = null;
        this.negative = null;
        this.mods = new Modifier[(2 * StandardPlural.COUNT)];
        this.frozen = false;
    }

    public void setModifier(boolean isNegative, StandardPlural plural, Modifier mod) {
        this.mods[getModIndex(isNegative, plural)] = mod;
    }

    public void freeze() {
        this.frozen = true;
    }

    public Modifier getModifier(boolean isNegative) {
        return isNegative ? this.negative : this.positive;
    }

    public Modifier getModifier(boolean isNegative, StandardPlural plural) {
        return this.mods[getModIndex(isNegative, plural)];
    }

    private static int getModIndex(boolean isNegative, StandardPlural plural) {
        return (plural.ordinal() * 2) + (isNegative);
    }
}
