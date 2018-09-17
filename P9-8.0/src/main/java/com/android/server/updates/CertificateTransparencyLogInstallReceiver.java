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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00e8 A:{Splitter: B:15:0x00c1, ExcHandler: java.io.IOException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x00e8, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x00e9, code:
            android.os.FileUtils.deleteContentsAndDir(r10);
     */
    /* JADX WARNING: Missing block: B:22:0x00ec, code:
            throw r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void install(byte[] content, int version) throws IOException {
        this.updateDir.mkdir();
        if (!this.updateDir.isDirectory()) {
            throw new IOException("Unable to make directory " + this.updateDir.getCanonicalPath());
        } else if (this.updateDir.setReadable(true, false)) {
            File currentSymlink = new File(this.updateDir, "current");
            File newVersion = new File(this.updateDir, LOGDIR_PREFIX + String.valueOf(version));
            if (newVersion.exists()) {
                if (newVersion.getCanonicalPath().equals(currentSymlink.getCanonicalPath())) {
                    writeUpdate(this.updateDir, this.updateVersion, Long.toString((long) version).getBytes());
                    deleteOldLogDirectories();
                    return;
                }
                FileUtils.deleteContentsAndDir(newVersion);
            }
            try {
                newVersion.mkdir();
                if (!newVersion.isDirectory()) {
                    throw new IOException("Unable to make directory " + newVersion.getCanonicalPath());
                } else if (newVersion.setReadable(true, false)) {
                    JSONArray logs = new JSONObject(new String(content, StandardCharsets.UTF_8)).getJSONArray("logs");
                    for (int i = 0; i < logs.length(); i++) {
                        installLog(newVersion, logs.getJSONObject(i));
                    }
                    File tempSymlink = new File(this.updateDir, "new_symlink");
                    Os.symlink(newVersion.getCanonicalPath(), tempSymlink.getCanonicalPath());
                    tempSymlink.renameTo(currentSymlink.getAbsoluteFile());
                    Slog.i(TAG, "CT log directory updated to " + newVersion.getAbsolutePath());
                    writeUpdate(this.updateDir, this.updateVersion, Long.toString((long) version).getBytes());
                    deleteOldLogDirectories();
                } else {
                    throw new IOException("Failed to set " + newVersion.getCanonicalPath() + " readable");
                }
            } catch (ErrnoException e) {
                throw new IOException("Failed to create symlink", e);
            } catch (JSONException e2) {
                throw new IOException("Failed to parse logs", e2);
            } catch (Exception e3) {
            }
        } else {
            throw new IOException("Unable to set permissions on " + this.updateDir.getCanonicalPath());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0061 A:{SYNTHETIC, Splitter: B:24:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0072 A:{Catch:{ JSONException -> 0x004d }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0066 A:{SYNTHETIC, Splitter: B:27:0x0066} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void installLog(File directory, JSONObject logObject) throws IOException {
        Throwable th;
        Throwable th2 = null;
        try {
            File file = new File(directory, getLogFileName(logObject.getString("key")));
            OutputStreamWriter out = null;
            try {
                OutputStreamWriter out2 = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                try {
                    writeLogEntry(out2, "key", logObject.getString("key"));
                    writeLogEntry(out2, "url", logObject.getString("url"));
                    writeLogEntry(out2, "description", logObject.getString("description"));
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        throw th2;
                    } else if (!file.setReadable(true, false)) {
                        throw new IOException("Failed to set permissions on " + file.getCanonicalPath());
                    }
                } catch (Throwable th4) {
                    th = th4;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        throw th2;
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (out != null) {
                }
                if (th2 == null) {
                }
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
                public boolean accept(File file) {
                    return !currentTarget.equals(file) ? file.getName().startsWith(CertificateTransparencyLogInstallReceiver.LOGDIR_PREFIX) : false;
                }
            })) {
                FileUtils.deleteContentsAndDir(f);
            }
        }
    }
}
