package com.android.server.updates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.util.HexDump;
import com.android.server.EventLogTags;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class ConfigUpdateInstallReceiver extends BroadcastReceiver {
    private static final String EXTRA_REQUIRED_HASH = "REQUIRED_HASH";
    private static final String EXTRA_VERSION_NUMBER = "VERSION";
    private static final String TAG = "ConfigUpdateInstallReceiver";
    protected final File updateContent;
    protected final File updateDir;
    protected final File updateVersion;

    public ConfigUpdateInstallReceiver(String updateDir, String updateContentPath, String updateMetadataPath, String updateVersionPath) {
        this.updateDir = new File(updateDir);
        this.updateContent = new File(updateDir, updateContentPath);
        this.updateVersion = new File(new File(updateDir, updateMetadataPath), updateVersionPath);
    }

    public void onReceive(final Context context, final Intent intent) {
        new Thread() {
            public void run() {
                try {
                    byte[] altContent = ConfigUpdateInstallReceiver.this.getAltContent(context, intent);
                    int altVersion = ConfigUpdateInstallReceiver.this.getVersionFromIntent(intent);
                    String altRequiredHash = ConfigUpdateInstallReceiver.this.getRequiredHashFromIntent(intent);
                    int currentVersion = ConfigUpdateInstallReceiver.this.getCurrentVersion();
                    String currentHash = ConfigUpdateInstallReceiver.getCurrentHash(ConfigUpdateInstallReceiver.this.getCurrentContent());
                    if (!ConfigUpdateInstallReceiver.this.verifyVersion(currentVersion, altVersion)) {
                        Slog.i(ConfigUpdateInstallReceiver.TAG, "Not installing, new version is <= current version");
                    } else if (ConfigUpdateInstallReceiver.this.verifyPreviousHash(currentHash, altRequiredHash)) {
                        Slog.i(ConfigUpdateInstallReceiver.TAG, "Found new update, installing...");
                        ConfigUpdateInstallReceiver.this.install(altContent, altVersion);
                        Slog.i(ConfigUpdateInstallReceiver.TAG, "Installation successful");
                        ConfigUpdateInstallReceiver.this.postInstall(context, intent);
                    } else {
                        EventLog.writeEvent(EventLogTags.CONFIG_INSTALL_FAILED, "Current hash did not match required value");
                    }
                } catch (Exception e) {
                    Slog.e(ConfigUpdateInstallReceiver.TAG, "Could not update content!", e);
                    String errMsg = e.toString();
                    if (errMsg.length() > 100) {
                        errMsg = errMsg.substring(0, 99);
                    }
                    EventLog.writeEvent(EventLogTags.CONFIG_INSTALL_FAILED, errMsg);
                }
            }
        }.start();
    }

    private Uri getContentFromIntent(Intent i) {
        Uri data = i.getData();
        if (data != null) {
            return data;
        }
        throw new IllegalStateException("Missing required content path, ignoring.");
    }

    private int getVersionFromIntent(Intent i) throws NumberFormatException {
        String extraValue = i.getStringExtra(EXTRA_VERSION_NUMBER);
        if (extraValue != null) {
            return Integer.parseInt(extraValue.trim());
        }
        throw new IllegalStateException("Missing required version number, ignoring.");
    }

    private String getRequiredHashFromIntent(Intent i) {
        String extraValue = i.getStringExtra(EXTRA_REQUIRED_HASH);
        if (extraValue != null) {
            return extraValue.trim();
        }
        throw new IllegalStateException("Missing required previous hash, ignoring.");
    }

    private int getCurrentVersion() throws NumberFormatException {
        try {
            return Integer.parseInt(IoUtils.readFileAsString(this.updateVersion.getCanonicalPath()).trim());
        } catch (IOException e) {
            Slog.i(TAG, "Couldn't find current metadata, assuming first update");
            return 0;
        }
    }

    private byte[] getAltContent(Context c, Intent i) throws IOException {
        InputStream is = c.getContentResolver().openInputStream(getContentFromIntent(i));
        try {
            byte[] readFullyNoClose = Streams.readFullyNoClose(is);
            return readFullyNoClose;
        } finally {
            is.close();
        }
    }

    private byte[] getCurrentContent() {
        try {
            return IoUtils.readFileAsByteArray(this.updateContent.getCanonicalPath());
        } catch (IOException e) {
            Slog.i(TAG, "Failed to read current content, assuming first update!");
            return null;
        }
    }

    private static String getCurrentHash(byte[] content) {
        if (content == null) {
            return "0";
        }
        try {
            return HexDump.toHexString(MessageDigest.getInstance("SHA512").digest(content), false);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    protected boolean verifyVersion(int current, int alternative) {
        return current < alternative;
    }

    private boolean verifyPreviousHash(String current, String required) {
        if (required.equals("NONE")) {
            return true;
        }
        return current.equals(required);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0030  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void writeUpdate(File dir, File file, byte[] content) throws IOException {
        Throwable th;
        AutoCloseable out = null;
        File tmp = null;
        try {
            File parent = file.getParentFile();
            parent.mkdirs();
            if (parent.exists()) {
                tmp = File.createTempFile("journal", "", dir);
                tmp.setReadable(true, false);
                FileOutputStream out2 = new FileOutputStream(tmp);
                try {
                    out2.write(content);
                    out2.getFD().sync();
                    if (tmp.renameTo(file)) {
                        if (tmp != null) {
                            tmp.delete();
                        }
                        IoUtils.closeQuietly(out2);
                        return;
                    }
                    throw new IOException("Failed to atomically rename " + file.getCanonicalPath());
                } catch (Throwable th2) {
                    th = th2;
                    Object out3 = out2;
                    if (tmp != null) {
                        tmp.delete();
                    }
                    IoUtils.closeQuietly(out3);
                    throw th;
                }
            }
            throw new IOException("Failed to create directory " + parent.getCanonicalPath());
        } catch (Throwable th3) {
            th = th3;
            if (tmp != null) {
            }
            IoUtils.closeQuietly(out3);
            throw th;
        }
    }

    protected void install(byte[] content, int version) throws IOException {
        writeUpdate(this.updateDir, this.updateContent, content);
        writeUpdate(this.updateDir, this.updateVersion, Long.toString((long) version).getBytes());
    }

    protected void postInstall(Context context, Intent intent) {
    }
}
