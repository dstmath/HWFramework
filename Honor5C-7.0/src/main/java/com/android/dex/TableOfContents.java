package com.android.dex;

import android.icu.lang.UProperty;
import android.icu.text.Collator.ReorderCodes;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import libcore.icu.DateUtilsBridge;

public final class TableOfContents {
    public final Section annotationSetRefLists;
    public final Section annotationSets;
    public final Section annotations;
    public final Section annotationsDirectories;
    public int apiLevel;
    public int checksum;
    public final Section classDatas;
    public final Section classDefs;
    public final Section codes;
    public int dataOff;
    public int dataSize;
    public final Section debugInfos;
    public final Section encodedArrays;
    public final Section fieldIds;
    public int fileSize;
    public final Section header;
    public int linkOff;
    public int linkSize;
    public final Section mapList;
    public final Section methodIds;
    public final Section protoIds;
    public final Section[] sections;
    public byte[] signature;
    public final Section stringDatas;
    public final Section stringIds;
    public final Section typeIds;
    public final Section typeLists;

    public static class Section implements Comparable<Section> {
        public int byteCount;
        public int off;
        public int size;
        public final short type;

        public Section(int type) {
            this.size = 0;
            this.off = -1;
            this.byteCount = 0;
            this.type = (short) type;
        }

        public boolean exists() {
            return this.size > 0;
        }

        public int compareTo(Section section) {
            if (this.off == section.off) {
                return 0;
            }
            return this.off < section.off ? -1 : 1;
        }

        public String toString() {
            return String.format("Section[type=%#x,off=%#x,size=%#x]", new Object[]{Short.valueOf(this.type), Integer.valueOf(this.off), Integer.valueOf(this.size)});
        }
    }

    public TableOfContents() {
        this.header = new Section(0);
        this.stringIds = new Section(1);
        this.typeIds = new Section(2);
        this.protoIds = new Section(3);
        this.fieldIds = new Section(4);
        this.methodIds = new Section(5);
        this.classDefs = new Section(6);
        this.mapList = new Section(VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS);
        this.typeLists = new Section(ReorderCodes.PUNCTUATION);
        this.annotationSetRefLists = new Section(ReorderCodes.SYMBOL);
        this.annotationSets = new Section(ReorderCodes.CURRENCY);
        this.classDatas = new Section(DateUtilsBridge.FORMAT_UTC);
        this.codes = new Section(UProperty.MASK_LIMIT);
        this.stringDatas = new Section(8194);
        this.debugInfos = new Section(8195);
        this.annotations = new Section(8196);
        this.encodedArrays = new Section(8197);
        this.annotationsDirectories = new Section(8198);
        this.sections = new Section[]{this.header, this.stringIds, this.typeIds, this.protoIds, this.fieldIds, this.methodIds, this.classDefs, this.mapList, this.typeLists, this.annotationSetRefLists, this.annotationSets, this.classDatas, this.codes, this.stringDatas, this.debugInfos, this.annotations, this.encodedArrays, this.annotationsDirectories};
        this.signature = new byte[20];
    }

    public void readFrom(Dex dex) throws IOException {
        readHeader(dex.open(0));
        readMap(dex.open(this.mapList.off));
        computeSizesFromOffsets();
    }

    private void readHeader(com.android.dex.Dex.Section headerIn) throws UnsupportedEncodingException {
        byte[] magic = headerIn.readByteArray(8);
        if (DexFormat.isSupportedDexMagic(magic)) {
            this.apiLevel = DexFormat.magicToApi(magic);
            this.checksum = headerIn.readInt();
            this.signature = headerIn.readByteArray(20);
            this.fileSize = headerIn.readInt();
            int headerSize = headerIn.readInt();
            if (headerSize != Opcodes.OP_INVOKE_DIRECT) {
                throw new DexException("Unexpected header: 0x" + Integer.toHexString(headerSize));
            }
            int endianTag = headerIn.readInt();
            if (endianTag != DexFormat.ENDIAN_TAG) {
                throw new DexException("Unexpected endian tag: 0x" + Integer.toHexString(endianTag));
            }
            this.linkSize = headerIn.readInt();
            this.linkOff = headerIn.readInt();
            this.mapList.off = headerIn.readInt();
            if (this.mapList.off == 0) {
                throw new DexException("Cannot merge dex files that do not contain a map");
            }
            this.stringIds.size = headerIn.readInt();
            this.stringIds.off = headerIn.readInt();
            this.typeIds.size = headerIn.readInt();
            this.typeIds.off = headerIn.readInt();
            this.protoIds.size = headerIn.readInt();
            this.protoIds.off = headerIn.readInt();
            this.fieldIds.size = headerIn.readInt();
            this.fieldIds.off = headerIn.readInt();
            this.methodIds.size = headerIn.readInt();
            this.methodIds.off = headerIn.readInt();
            this.classDefs.size = headerIn.readInt();
            this.classDefs.off = headerIn.readInt();
            this.dataSize = headerIn.readInt();
            this.dataOff = headerIn.readInt();
            return;
        }
        throw new DexException("Unexpected magic: " + Arrays.toString(magic));
    }

    private void readMap(com.android.dex.Dex.Section in) throws IOException {
        int mapSize = in.readInt();
        Section previous = null;
        int i = 0;
        while (i < mapSize) {
            short type = in.readShort();
            in.readShort();
            Section section = getSection(type);
            int size = in.readInt();
            int offset = in.readInt();
            if ((section.size == 0 || section.size == size) && (section.off == -1 || section.off == offset)) {
                section.size = size;
                section.off = offset;
                if (previous == null || previous.off <= section.off) {
                    previous = section;
                    i++;
                } else {
                    throw new DexException("Map is unsorted at " + previous + ", " + section);
                }
            }
            throw new DexException("Unexpected map value for 0x" + Integer.toHexString(type));
        }
        Arrays.sort(this.sections);
    }

    public void computeSizesFromOffsets() {
        int end = this.dataOff + this.dataSize;
        for (int i = this.sections.length - 1; i >= 0; i--) {
            Section section = this.sections[i];
            if (section.off != -1) {
                if (section.off > end) {
                    throw new DexException("Map is unsorted at " + section);
                }
                section.byteCount = end - section.off;
                end = section.off;
            }
        }
    }

    private Section getSection(short type) {
        for (Section section : this.sections) {
            if (section.type == type) {
                return section;
            }
        }
        throw new IllegalArgumentException("No such map item: " + type);
    }

    public void writeHeader(com.android.dex.Dex.Section out, int api) throws IOException {
        out.write(DexFormat.apiToMagic(api).getBytes("UTF-8"));
        out.writeInt(this.checksum);
        out.write(this.signature);
        out.writeInt(this.fileSize);
        out.writeInt(Opcodes.OP_INVOKE_DIRECT);
        out.writeInt(DexFormat.ENDIAN_TAG);
        out.writeInt(this.linkSize);
        out.writeInt(this.linkOff);
        out.writeInt(this.mapList.off);
        out.writeInt(this.stringIds.size);
        out.writeInt(this.stringIds.off);
        out.writeInt(this.typeIds.size);
        out.writeInt(this.typeIds.off);
        out.writeInt(this.protoIds.size);
        out.writeInt(this.protoIds.off);
        out.writeInt(this.fieldIds.size);
        out.writeInt(this.fieldIds.off);
        out.writeInt(this.methodIds.size);
        out.writeInt(this.methodIds.off);
        out.writeInt(this.classDefs.size);
        out.writeInt(this.classDefs.off);
        out.writeInt(this.dataSize);
        out.writeInt(this.dataOff);
    }

    public void writeMap(com.android.dex.Dex.Section out) throws IOException {
        int count = 0;
        for (Section section : this.sections) {
            if (section.exists()) {
                count++;
            }
        }
        out.writeInt(count);
        for (Section section2 : this.sections) {
            if (section2.exists()) {
                out.writeShort(section2.type);
                out.writeShort((short) 0);
                out.writeInt(section2.size);
                out.writeInt(section2.off);
            }
        }
    }
}
