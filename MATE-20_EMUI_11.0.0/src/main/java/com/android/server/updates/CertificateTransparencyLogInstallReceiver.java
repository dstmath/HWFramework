package com.android.server.updates;

import android.os.FileUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.util.Slog;
import com.android.internal.util.HexDump;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CertificateTransparencyLogInstallReceiver extends ConfigUpdateInstallReceiver {
    private static final String LOGDIR_PREFIX = "logs-";
    private static final String TAG = "CTLogInstallReceiver";

    public CertificateTransparencyLogInstallReceiver() {
        super("/data/misc/keychain/trusted_ct_logs/", "ct_logs", "metadata/", "version");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.updates.ConfigUpdateInstallReceiver
    public void install(byte[] content, int version) throws IOException {
        this.updateDir.mkdir();
        if (this.updateDir.isDirectory()) {
            if (this.updateDir.setReadable(true, false)) {
                File currentSymlink = new File(this.updateDir, "current");
                File file = this.updateDir;
                File newVersion = new File(file, LOGDIR_PREFIX + String.valueOf(version));
                if (newVersion.exists()) {
                    if (newVersion.getCanonicalPath().equals(currentSymlink.getCanonicalPath())) {
                        writeUpdate(this.updateDir, this.updateVersion, Long.toString((long) version).getBytes());
                        deleteOldLogDirectories();
                        return;
                    }
                    FileUtils.deleteContentsAndDir(newVersion);
                }
                newVersion.mkdir();
                if (!newVersion.isDirectory()) {
                    throw new IOException("Unable to make directory " + newVersion.getCanonicalPath());
                } else if (newVersion.setReadable(true, false)) {
                    try {
                        JSONArray logs = new JSONObject(new String(content, StandardCharsets.UTF_8)).getJSONArray("logs");
                        for (int i = 0; i < logs.length(); i++) {
                            installLog(newVersion, logs.getJSONObject(i));
                        }
                        try {
                            File tempSymlink = new File(this.updateDir, "new_symlink");
                            try {
                                Os.symlink(newVersion.getCanonicalPath(), tempSymlink.getCanonicalPath());
                                tempSymlink.renameTo(currentSymlink.getAbsoluteFile());
                                Slog.i(TAG, "CT log directory updated to " + newVersion.getAbsolutePath());
                                writeUpdate(this.updateDir, this.updateVersion, Long.toString((long) version).getBytes());
                                deleteOldLogDirectories();
                            } catch (ErrnoException e) {
                                throw new IOException("Failed to create symlink", e);
                            }
                        } catch (IOException | RuntimeException e2) {
                            FileUtils.deleteContentsAndDir(newVersion);
                            throw e2;
                        }
                    } catch (JSONException e3) {
                        throw new IOException("Failed to parse logs", e3);
                    }
                } else {
                    throw new IOException("Failed to set " + newVersion.getCanonicalPath() + " readable");
                }
            } else {
                throw new IOException("Unable to set permissions on " + this.updateDir.getCanonicalPath());
            }
        } else {
            throw new IOException("Unable to make directory " + this.updateDir.getCanonicalPath());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0060, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0065, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0066, code lost:
        r0.addSuppressed(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0069, code lost:
        throw r1;
     */
    private void installLog(File directory, JSONObject logObject) throws IOException {
        try {
            File file = new File(directory, getLogFileName(logObject.getString("key")));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writeLogEntry(out, "key", logObject.getString("key"));
            writeLogEntry(out, "url", logObject.getString("url"));
            writeLogEntry(out, "description", logObject.getString("description"));
            out.close();
            if (!file.setReadable(true, false)) {
                throw new IOException("Failed to set permissions on " + file.getCanonicalPath());
            }
        } catch (JSONException e) {
            throw new IOException("Failed to parse log", e);
        }
    }

    private String getLogFileName(String base64PublicKey) {
        try {
            return HexDump.toHexString(MessageDigest.getInstance("SHA-256").digest(Base64.decode(base64PublicKey, 0)), false);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeLogEntry(OutputStreamWriter out, String key, String value) throws IOException {
        out.write(key + ":" + value + "\n");
    }

    private void deleteOldLogDirectories() throws IOException {
        if (this.updateDir.exists()) {
            final File currentTarget = new File(this.updateDir, "current").getCanonicalFile();
            for (File f : this.updateDir.listFiles(new FileFilter() {
                /* class com.android.server.updates.CertificateTransparencyLogInstallReceiver.AnonymousClass1 */

                @Override // java.io.FileFilter
                public boolean accept(File file) {
                    return !currentTarget.equals(file) && file.getName().startsWith(CertificateTransparencyLogInstallReceiver.LOGDIR_PREFIX);
                }
            })) {
                FileUtils.deleteContentsAndDir(f);
            }
        }
    }
}
