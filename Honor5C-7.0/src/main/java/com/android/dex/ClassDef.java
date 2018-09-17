package com.android.dex;

public final class ClassDef {
    public static final int NO_INDEX = -1;
    private final int accessFlags;
    private final int annotationsOffset;
    private final Dex buffer;
    private final int classDataOffset;
    private final int interfacesOffset;
    private final int offset;
    private final int sourceFileIndex;
    private final int staticValuesOffset;
    private final int supertypeIndex;
    private final int typeIndex;

    public ClassDef(Dex buffer, int offset, int typeIndex, int accessFlags, int supertypeIndex, int interfacesOffset, int sourceFileIndex, int annotationsOffset, int classDataOffset, int staticValuesOffset) {
        this.buffer = buffer;
        this.offset = offset;
        this.typeIndex = typeIndex;
        this.accessFlags = accessFlags;
        this.supertypeIndex = supertypeIndex;
        this.interfacesOffset = interfacesOffset;
        this.sourceFileIndex = sourceFileIndex;
        this.annotationsOffset = annotationsOffset;
        this.classDataOffset = classDataOffset;
        this.staticValuesOffset = staticValuesOffset;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getTypeIndex() {
        return this.typeIndex;
    }

    public int getSupertypeIndex() {
        return this.supertypeIndex;
    }

    public int getInterfacesOffset() {
        return this.interfacesOffset;
    }

    public short[] getInterfaces() {
        return this.buffer.readTypeList(this.interfacesOffset).getTypes();
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }

    public int getSourceFileIndex() {
        return this.sourceFileIndex;
    }

    public int getAnnotationsOffset() {
        return this.annotationsOffset;
    }

    public int getClassDataOffset() {
        return this.classDataOffset;
    }

    public int getStaticValuesOffset() {
        return this.staticValuesOffset;
    }

    public String toString() {
        if (this.buffer == null) {
            return this.typeIndex + " " + this.supertypeIndex;
        }
        StringBuilder result = new StringBuilder();
        result.append((String) this.buffer.typeNames().get(this.typeIndex));
        if (this.supertypeIndex != NO_INDEX) {
            result.append(" extends ").append((String) this.buffer.typeNames().get(this.supertypeIndex));
        }
        return result.toString();
    }
}
