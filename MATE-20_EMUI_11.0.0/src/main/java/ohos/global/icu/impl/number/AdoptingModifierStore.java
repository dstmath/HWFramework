package ohos.global.icu.impl.number;

import ohos.global.icu.impl.StandardPlural;

public class AdoptingModifierStore implements ModifierStore {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    boolean frozen;
    final Modifier[] mods;
    private final Modifier negative;
    private final Modifier positive;
    private final Modifier zero;

    public AdoptingModifierStore(Modifier modifier, Modifier modifier2, Modifier modifier3) {
        this.positive = modifier;
        this.zero = modifier2;
        this.negative = modifier3;
        this.mods = null;
        this.frozen = true;
    }

    public AdoptingModifierStore() {
        this.positive = null;
        this.zero = null;
        this.negative = null;
        this.mods = new Modifier[(StandardPlural.COUNT * 3)];
        this.frozen = false;
    }

    public void setModifier(int i, StandardPlural standardPlural, Modifier modifier) {
        this.mods[getModIndex(i, standardPlural)] = modifier;
    }

    public void freeze() {
        this.frozen = true;
    }

    public Modifier getModifierWithoutPlural(int i) {
        if (i == 0) {
            return this.zero;
        }
        return i < 0 ? this.negative : this.positive;
    }

    @Override // ohos.global.icu.impl.number.ModifierStore
    public Modifier getModifier(int i, StandardPlural standardPlural) {
        return this.mods[getModIndex(i, standardPlural)];
    }

    private static int getModIndex(int i, StandardPlural standardPlural) {
        return (standardPlural.ordinal() * 3) + i + 1;
    }
}
