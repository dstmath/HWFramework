package com.android.server.backup.transport;

import com.android.internal.backup.IBackupTransport;

public interface TransportConnectionListener {
    void onTransportConnectionResult(IBackupTransport iBackupTransport, TransportClient transportClient);
}
