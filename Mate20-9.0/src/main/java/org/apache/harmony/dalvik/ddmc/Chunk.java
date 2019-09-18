package org.apache.harmony.dalvik.ddmc;

import java.nio.ByteBuffer;

public class Chunk {
    public byte[] data;
    public int length;
    public int offset;
    public int type;

    public Chunk() {
    }

    public Chunk(int type2, byte[] data2, int offset2, int length2) {
        this.type = type2;
        this.data = data2;
        this.offset = offset2;
        this.length = length2;
    }

    public Chunk(int type2, ByteBuffer buf) {
        this.type = type2;
        this.data = buf.array();
        this.offset = buf.arrayOffset();
        this.length = buf.position();
    }
}
