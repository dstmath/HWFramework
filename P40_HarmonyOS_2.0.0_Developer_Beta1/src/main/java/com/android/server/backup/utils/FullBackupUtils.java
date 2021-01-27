package com.android.server.backup.utils;

import android.os.ParcelFileDescriptor;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FullBackupUtils {
    public static void routeSocketDataToOutput(ParcelFileDescriptor inPipe, OutputStream out) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(inPipe.getFileDescriptor()));
        byte[] buffer = new byte[32768];
        while (true) {
            int readInt = in.readInt();
            int chunkTotal = readInt;
            if (readInt > 0) {
                while (true) {
                    if (chunkTotal > 0) {
                        int nRead = in.read(buffer, 0, chunkTotal > buffer.length ? buffer.length : chunkTotal);
                        if (nRead >= 0) {
                            out.write(buffer, 0, nRead);
                            chunkTotal -= nRead;
                        } else {
                            Slog.e(BackupManagerService.TAG, "Unexpectedly reached end of file while reading data");
                            throw new EOFException();
                        }
                    }
                }
            } else {
                return;
            }
        }
    }
}
