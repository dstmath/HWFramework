package com.android.server.backup.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Slog;
import android.util.StringBuilderPrinter;
import com.android.server.backup.BackupManagerService;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static void writeAppManifest(PackageInfo pkg, PackageManager packageManager, File manifestFile, boolean withApk, boolean withWidgets) throws IOException {
        StringBuilder builder = new StringBuilder(4096);
        StringBuilderPrinter printer = new StringBuilderPrinter(builder);
        printer.println(Integer.toString(1));
        printer.println(pkg.packageName);
        printer.println(Long.toString(pkg.getLongVersionCode()));
        printer.println(Integer.toString(Build.VERSION.SDK_INT));
        String installerName = packageManager.getInstallerPackageName(pkg.packageName);
        printer.println(installerName != null ? installerName : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        printer.println(withApk ? "1" : "0");
        SigningInfo signingInfo = pkg.signingInfo;
        if (signingInfo == null) {
            printer.println("0");
        } else {
            Signature[] signatures = signingInfo.getApkContentsSigners();
            printer.println(Integer.toString(signatures.length));
            for (Signature sig : signatures) {
                printer.println(sig.toCharsString());
            }
        }
        FileOutputStream outstream = new FileOutputStream(manifestFile);
        outstream.write(builder.toString().getBytes());
        outstream.close();
        manifestFile.setLastModified(0);
    }
}
