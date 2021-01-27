package com.android.server.backup.encryption.chunk;

import android.util.proto.ProtoInputStream;
import java.io.IOException;

public class Chunk {
    private byte[] mHash = new byte[0];
    private int mLength = 0;

    static Chunk readFromProto(ProtoInputStream inputStream) throws IOException {
        Chunk result = new Chunk();
        while (inputStream.nextField() != -1) {
            int fieldNumber = inputStream.getFieldNumber();
            if (fieldNumber == 1) {
                result.mHash = inputStream.readBytes(1151051235329L);
            } else if (fieldNumber == 2) {
                result.mLength = inputStream.readInt(1120986464258L);
            }
        }
        return result;
    }

    private Chunk() {
    }

    public int getLength() {
        return this.mLength;
    }

    public byte[] getHash() {
        return this.mHash;
    }
}
