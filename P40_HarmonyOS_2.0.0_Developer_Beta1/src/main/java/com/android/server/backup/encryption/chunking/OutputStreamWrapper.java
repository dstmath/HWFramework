package com.android.server.backup.encryption.chunking;

import java.io.OutputStream;

public interface OutputStreamWrapper {
    OutputStream wrap(OutputStream outputStream);
}
