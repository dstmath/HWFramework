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
    private static final String BLACKLIST_ROOT = null;
    public static final String PUBKEY_BLACKLIST_KEY = "pubkey_blacklist";
    public static final String PUBKEY_PATH = null;
    public static final String SERIAL_BLACKLIST_KEY = "serial_blacklist";
    public static final String SERIAL_PATH = null;
    private static final String TAG = "CertBlacklister";

    private static class BlacklistObserver extends ContentObserver {
        private final ContentResolver mContentResolver;
        private final String mKey;
        private final String mName;
        private final String mPath;
        private final File mTmpDir;

        /* renamed from: com.android.server.CertBlacklister.BlacklistObserver.1 */
        class AnonymousClass1 extends Thread {
            AnonymousClass1(String $anonymous0) {
                super($anonymous0);
            }

            public void run() {
                IOException e;
                Object obj;
                Throwable th;
                synchronized (BlacklistObserver.this.mTmpDir) {
                    String blacklist = BlacklistObserver.this.getValue();
                    if (blacklist != null) {
                        Slog.i(CertBlacklister.TAG, "Certificate blacklist changed, updating...");
                        AutoCloseable out = null;
                        try {
                            File tmp = File.createTempFile("journal", "", BlacklistObserver.this.mTmpDir);
                            tmp.setReadable(true, false);
                            FileOutputStream out2 = new FileOutputStream(tmp);
                            try {
                                out2.write(blacklist.getBytes());
                                FileUtils.sync(out2);
                                tmp.renameTo(new File(BlacklistObserver.this.mPath));
                                Slog.i(CertBlacklister.TAG, "Certificate blacklist updated");
                                IoUtils.closeQuietly(out2);
                            } catch (IOException e2) {
                                e = e2;
                                obj = out2;
                                try {
                                    Slog.e(CertBlacklister.TAG, "Failed to write blacklist", e);
                                    IoUtils.closeQuietly(out);
                                } catch (Throwable th2) {
                                    th = th2;
                                    IoUtils.closeQuietly(out);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                obj = out2;
                                IoUtils.closeQuietly(out);
                                throw th;
                            }
                        } catch (IOException e3) {
                            e = e3;
                            Slog.e(CertBlacklister.TAG, "Failed to write blacklist", e);
                            IoUtils.closeQuietly(out);
                        }
                    }
                }
            }
        }

        public BlacklistObserver(String key, String name, String path, ContentResolver cr) {
            super(null);
            this.mKey = key;
            this.mName = name;
            this.mPath = path;
            this.mTmpDir = new File(this.mPath).getParentFile();
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
            new AnonymousClass1("BlacklistUpdater").start();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.CertBlacklister.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.CertBlacklister.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.CertBlacklister.<clinit>():void");
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
