package com.android.server.locksettings;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class SP800Derive {
    private final byte[] mKeyBytes;

    SP800Derive(byte[] keyBytes) {
        this.mKeyBytes = keyBytes;
    }

    private Mac getMac() {
        try {
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(new SecretKeySpec(this.mKeyBytes, m.getAlgorithm()));
            return m;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void update32(Mac m, int v) {
        m.update(ByteBuffer.allocate(4).putInt(v).array());
    }

    public byte[] fixedInput(byte[] fixedInput) {
        Mac m = getMac();
        update32(m, 1);
        m.update(fixedInput);
        return m.doFinal();
    }

    public byte[] withContext(byte[] label, byte[] context) {
        Mac m = getMac();
        update32(m, 1);
        m.update(label);
        m.update((byte) 0);
        m.update(context);
        update32(m, context.length * 8);
        update32(m, 256);
        return m.doFinal();
    }
}
