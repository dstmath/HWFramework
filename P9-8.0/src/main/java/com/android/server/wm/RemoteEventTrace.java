package com.android.server.wm;

import android.util.Slog;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

class RemoteEventTrace {
    private static final String TAG = "RemoteEventTrace";
    static final byte[] sigil = new byte[]{(byte) -4, (byte) -4, (byte) -4, (byte) -4};
    private final DataOutputStream mOut;
    private final WindowManagerService mService;

    RemoteEventTrace(WindowManagerService service, FileDescriptor fd) {
        this.mService = service;
        this.mOut = new DataOutputStream(new FileOutputStream(fd, false));
    }

    void openSurfaceTransaction() {
        try {
            this.mOut.writeUTF("OpenTransaction");
            writeSigil();
        } catch (Exception e) {
            logException(e);
            this.mService.disableSurfaceTrace();
        }
    }

    void closeSurfaceTransaction() {
        try {
            this.mOut.writeUTF("CloseTransaction");
            writeSigil();
        } catch (Exception e) {
            logException(e);
            this.mService.disableSurfaceTrace();
        }
    }

    private void writeSigil() throws Exception {
        this.mOut.write(sigil, 0, 4);
    }

    static void logException(Exception e) {
        Slog.i(TAG, "Exception writing to SurfaceTrace (client vanished?): " + e.toString());
    }
}
