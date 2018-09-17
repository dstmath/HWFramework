package com.android.dex;

import com.android.dex.Dex.Section;
import com.android.dex.util.Unsigned;

public final class MethodId implements Comparable<MethodId> {
    private final int declaringClassIndex;
    private final Dex dex;
    private final int nameIndex;
    private final int protoIndex;

    public MethodId(Dex dex, int declaringClassIndex, int protoIndex, int nameIndex) {
        this.dex = dex;
        this.declaringClassIndex = declaringClassIndex;
        this.protoIndex = protoIndex;
        this.nameIndex = nameIndex;
    }

    public int getDeclaringClassIndex() {
        return this.declaringClassIndex;
    }

    public int getProtoIndex() {
        return this.protoIndex;
    }

    public int getNameIndex() {
        return this.nameIndex;
    }

    public int compareTo(MethodId other) {
        if (this.declaringClassIndex != other.declaringClassIndex) {
            return Unsigned.compare(this.declaringClassIndex, other.declaringClassIndex);
        }
        if (this.nameIndex != other.nameIndex) {
            return Unsigned.compare(this.nameIndex, other.nameIndex);
        }
        return Unsigned.compare(this.protoIndex, other.protoIndex);
    }

    public void writeTo(Section out) {
        out.writeUnsignedShort(this.declaringClassIndex);
        out.writeUnsignedShort(this.protoIndex);
        out.writeInt(this.nameIndex);
    }

    public String toString() {
        if (this.dex == null) {
            return this.declaringClassIndex + " " + this.protoIndex + " " + this.nameIndex;
        }
        return ((String) this.dex.typeNames().get(this.declaringClassIndex)) + "." + ((String) this.dex.strings().get(this.nameIndex)) + this.dex.readTypeList(((ProtoId) this.dex.protoIds().get(this.protoIndex)).getParametersOffset());
    }
}
