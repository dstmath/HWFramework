package org.bouncycastle.operator;

import java.io.OutputStream;

public interface AADProcessor {
    OutputStream getAADStream();

    byte[] getMAC();
}
