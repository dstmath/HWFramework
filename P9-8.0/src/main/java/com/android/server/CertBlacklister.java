package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.FileUtils;
import android.provider.Settings.Secure;
import android.util.Slog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public class CertBlacklister extends Binder {
    private static final String BLACKLIST_ROOT = (System.getenv("ANDROID_DATA") + "/misc/keychain/");
    public static final String PUBKEY_BLACKLIST_KEY = "pubkey_blacklist";
    public static final String PUBKEY_PATH = (BLACKLIST_ROOT + "pubkey_blacklist.txt");
    public static final String SERIAL_BLACKLIST_KEY = "serial_blacklist";
    public static final String SERIAL_PATH = (BLACKLIST_ROOT + "serial_blacklist.txt");
    private static final String TAG = "CertBlacklister";

    private static class BlacklistObserver extends ContentObserver {
        private final ContentResolver mContentResolver;
        private final String mKey;
        private final String mName;
        private final String mPath;
        private final File mTmpDir = new File(this.mPath).getParentFile();

        public BlacklistObserver(String key, String name, String path, ContentResolver cr) {
            super(null);
            this.mKey = key;
            this.mName = name;
            this.mPath = path;
            this.mContentResolver = cr;
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            writeBlacklist();
        }

        public String getValue() {
            return Secure.getString(this.mContentResolver, this.mKey);
        }

        private void writeBlacklist() {
            new Thread("BlacklistUpdater") {
                public void run() {
                    IOException e;
                    Object out;
                    Throwable th;
                    synchronized (BlacklistObserver.this.mTmpDir) {
                        String blacklist = BlacklistObserver.this.getValue();
                        if (blacklist != null) {
                            Slog.i(CertBlacklister.TAG, "Certificate blacklist changed, updating...");
                            AutoCloseable out2 = null;
                            try {
                                File tmp = File.createTempFile("journal", "", BlacklistObserver.this.mTmpDir);
                                tmp.setReadable(true, false);
                                FileOutputStream out3 = new FileOutputStream(tmp);
                                try {
                                    out3.write(blacklist.getBytes());
                                    FileUtils.sync(out3);
                                    tmp.renameTo(new File(BlacklistObserver.this.mPath));
                                    Slog.i(CertBlacklister.TAG, "Certificate blacklist updated");
                                    IoUtils.closeQuietly(out3);
                                } catch (IOException e2) {
                                    e = e2;
                                    out2 = out3;
                                    try {
                                        Slog.e(CertBlacklister.TAG, "Failed to write blacklist", e);
                                        IoUtils.closeQuietly(out2);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        IoUtils.closeQuietly(out2);
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    out2 = out3;
                                    IoUtils.closeQuietly(out2);
                                    throw th;
                                }
                            } catch (IOException e3) {
                                e = e3;
                                Slog.e(CertBlacklister.TAG, "Failed to write blacklist", e);
                                IoUtils.closeQuietly(out2);
                            }
                        }
                    }
                }
            }.start();
        }
    }

    public CertBlacklister(Context context) {
        registerObservers(context.getContentResolver());
    }

    private BlacklistObserver buildPubkeyObserver(ContentResolver cr) {
        return new BlacklistObserver(PUBKEY_BLACKLIST_KEY, "pubkey", PUBKEY_PATH, cr);
    }

    private BlacklistObserver buildSerialObserver(ContentResolver cr) {
        return new BlacklistObserver(SERIAL_BLACKLIST_KEY, "serial", SERIAL_PATH, cr);
    }

    private void registerObservers(ContentResolver cr) {
        cr.registerContentObserver(Secure.getUriFor(PUBKEY_BLACKLIST_KEY), true, buildPubkeyObserver(cr));
        cr.registerContentObserver(Secure.getUriFor(SERIAL_BLACKLIST_KEY), true, buildSerialObserver(cr));
    }
}
