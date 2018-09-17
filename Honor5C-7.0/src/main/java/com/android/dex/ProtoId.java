package com.android.dex;

import android.icu.text.PluralRules;
import com.android.dex.Dex.Section;
import com.android.dex.util.Unsigned;

public final class ProtoId implements Comparable<ProtoId> {
    private final Dex dex;
    private final int parametersOffset;
    private final int returnTypeIndex;
    private final int shortyIndex;

    public ProtoId(Dex dex, int shortyIndex, int returnTypeIndex, int parametersOffset) {
        this.dex = dex;
        this.shortyIndex = shortyIndex;
        this.returnTypeIndex = returnTypeIndex;
        this.parametersOffset = parametersOffset;
    }

    public int compareTo(ProtoId other) {
        if (this.returnTypeIndex != other.returnTypeIndex) {
            return Unsigned.compare(this.returnTypeIndex, other.returnTypeIndex);
        }
        return Unsigned.compare(this.parametersOffset, other.parametersOffset);
    }

    public int getShortyIndex() {
        return this.shortyIndex;
    }

    public int getReturnTypeIndex() {
        return this.returnTypeIndex;
    }

    public int getParametersOffset() {
        return this.parametersOffset;
    }

    public void writeTo(Section out) {
        out.writeInt(this.shortyIndex);
        out.writeInt(this.returnTypeIndex);
        out.writeInt(this.parametersOffset);
    }

    public String toString() {
        if (this.dex == null) {
            return this.shortyIndex + " " + this.returnTypeIndex + " " + this.parametersOffset;
        }
        return ((String) this.dex.strings().get(this.shortyIndex)) + PluralRules.KEYWORD_RULE_SEPARATOR + ((String) this.dex.typeNames().get(this.returnTypeIndex)) + " " + this.dex.readTypeList(this.parametersOffset);
    }
}
