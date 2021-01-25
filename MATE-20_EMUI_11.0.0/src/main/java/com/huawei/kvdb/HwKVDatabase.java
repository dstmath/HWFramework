package com.huawei.kvdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.internal.widget.ConstantValues;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class HwKVDatabase {
    private static final int BITS_PER_BYTE = 8;
    private static final int BYTE_PER_CHAR = 2;
    private static final int CRC_TABLE_LENGTH = 256;
    public static final int IMAGE = 1;
    private static final long INIT_IAL_CRC = -1;
    private static final Object LOCK = new Object();
    private static final Pattern PATTERN = Pattern.compile("(.*([/\\\\]{1}[\\.\\.]{1,2}|[\\.\\.]{1,2}[/\\\\]{1}|\\.\\.).*)");
    private static final String PHOTOSHARE_PATH = (Environment.getExternalStorageDirectory() + File.separator + ".photoShare");
    private static final long POLY_64_REV = -7661587058870466123L;
    private static final String TAG = "HwKVDatabase";
    public static final int VIDEO = 3;
    private static long[] sCrcTable = new long[256];
    private static Map<String, HwKVDatabase> sGeneralDatabases = new HashMap();
    private static HwKVDatabase sThumbnailDatabase = null;
    private Context mContext;
    private boolean mIsGeneralKV;
    private HwKVConnectionPool mKVConnectionPool;

    /* access modifiers changed from: private */
    public interface ReadOperator<T> {
        Optional<T> operate(HwKVConnection hwKVConnection) throws HwKVDatabaseDeleteException;
    }

    /* access modifiers changed from: private */
    public interface WriteOperator {
        boolean operate(HwKVConnection hwKVConnection) throws HwKVDatabaseDeleteException, HwKVFullException;
    }

    static {
        for (int index = 0; index < 256; index++) {
            long part = (long) index;
            for (int bit = 0; bit < 8; bit++) {
                part = (part >> 1) ^ ((((int) part) & 1) != 0 ? POLY_64_REV : 0);
            }
            sCrcTable[index] = part;
        }
    }

    private HwKVDatabase(Context context, String dbPath) {
        this.mKVConnectionPool = new HwKVConnectionPool(dbPath);
        this.mContext = context;
        this.mIsGeneralKV = true;
    }

    private HwKVDatabase(Context context) {
        this.mKVConnectionPool = new HwKVConnectionPool();
        this.mContext = context;
        this.mIsGeneralKV = false;
    }

    private static byte[] getBytes(String path) {
        byte[] result = new byte[(path.length() * 2)];
        int output = 0;
        char[] charArray = path.toCharArray();
        for (char ch : charArray) {
            int output2 = output + 1;
            result[output] = (byte) (ch & 255);
            output = output2 + 1;
            result[output2] = (byte) (ch >> '\b');
        }
        return result;
    }

    private static long crc64Long(byte[] buffer) {
        long crc = INIT_IAL_CRC;
        for (byte b : buffer) {
            crc = sCrcTable[(((int) crc) ^ b) & ConstantValues.MAX_CHANNEL_VALUE] ^ (crc >> 8);
        }
        return crc;
    }

    private static boolean isSafePath(String filePath) {
        String matchPath = filePath;
        if (filePath.startsWith(PHOTOSHARE_PATH)) {
            matchPath = filePath.substring(PHOTOSHARE_PATH.length());
        }
        boolean isSafe = !PATTERN.matcher(matchPath).matches();
        if (!isSafe) {
            Log.e(TAG, "Invalid file path : " + filePath);
        }
        return isSafe;
    }

    public static long generateKey(int id, long timeModified, int mediaType, int type) {
        String path;
        if (mediaType == 1) {
            path = "/local/image/item/" + id;
        } else if (mediaType != 3) {
            return 0;
        } else {
            path = "/local/video/item/" + id;
        }
        return crc64Long(getBytes(path + "+" + timeModified + "+" + type));
    }

    public static HwKVDatabase getInstance(Context context) {
        HwKVDatabase hwKVDatabase;
        synchronized (LOCK) {
            if (context != null) {
                try {
                    if (sThumbnailDatabase == null) {
                        Context applicationContext = context.getApplicationContext();
                        if (applicationContext == null) {
                            applicationContext = context;
                        }
                        sThumbnailDatabase = new HwKVDatabase(applicationContext);
                    }
                    if (sThumbnailDatabase.mKVConnectionPool != null) {
                        sThumbnailDatabase.mKVConnectionPool.open();
                    }
                    hwKVDatabase = sThumbnailDatabase;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("Context can not be null!");
            }
        }
        return hwKVDatabase;
    }

    public static HwKVDatabase getInstance(Context context, String dbPath) {
        synchronized (LOCK) {
            if (context == null) {
                throw new IllegalArgumentException("Context can not be null!");
            } else if (!TextUtils.isEmpty(dbPath)) {
                try {
                    String canonicalPath = new File(dbPath).getCanonicalPath();
                    if (!isSafePath(canonicalPath)) {
                        throw new IllegalArgumentException("Illegal argument: path of database is unsafe.");
                    } else if (isThumbnailCachePath(canonicalPath)) {
                        return getInstance(context);
                    } else {
                        HwKVDatabase database = sGeneralDatabases.get(canonicalPath);
                        if (database == null) {
                            Context applicationContext = context.getApplicationContext();
                            if (applicationContext == null) {
                                applicationContext = context;
                            }
                            database = new HwKVDatabase(applicationContext, canonicalPath);
                            sGeneralDatabases.put(canonicalPath, database);
                        }
                        if (database.mKVConnectionPool != null) {
                            database.mKVConnectionPool.open();
                        }
                        return database;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("unable to resolve canonical path.", e);
                }
            } else {
                throw new IllegalArgumentException("Illegal argument: path of database is null or empty.");
            }
        }
    }

    private static boolean isThumbnailCachePath(String canonicalPath) {
        return Objects.equals(canonicalPath, HwKVConnectionPool.getThumbnailAbsolutePath());
    }

    private boolean checkWritePermission() {
        Context context = this.mContext;
        return context != null && context.getPackageName().equals("com.android.providers.media");
    }

    public boolean put(long key, byte[] value, int size) throws HwKVFullException {
        if (this.mIsGeneralKV) {
            Log.w(TAG, "prohibit putting a long key to a general key-value database.");
            return false;
        } else if (value == null || value.length == 0) {
            return false;
        } else {
            if (checkWritePermission()) {
                return executeWriteOperation(new WriteOperator(key, value, Math.min(value.length, size)) {
                    /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$gDL9qzBHi9me0CpHgMYILTTJ5D0 */
                    private final /* synthetic */ long f$0;
                    private final /* synthetic */ byte[] f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$0 = r1;
                        this.f$1 = r3;
                        this.f$2 = r4;
                    }

                    @Override // com.huawei.kvdb.HwKVDatabase.WriteOperator
                    public final boolean operate(HwKVConnection hwKVConnection) {
                        return hwKVConnection.put(this.f$0, this.f$1, this.f$2);
                    }
                });
            }
            Log.w(TAG, "permission denied to put a key-value pair to database, please check.");
            return false;
        }
    }

    public boolean put(String key, byte[] value, int size) throws HwKVFullException {
        if (!this.mIsGeneralKV) {
            Log.w(TAG, "prohibit putting a string key to a thumbnail key-value database.");
            return false;
        } else if (value == null || value.length == 0) {
            return false;
        } else {
            return executeWriteOperation(new WriteOperator(key, value, Math.min(value.length, size)) {
                /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$A2ltSQbWUDSqcQBrBqlmHD3hx8 */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ byte[] f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // com.huawei.kvdb.HwKVDatabase.WriteOperator
                public final boolean operate(HwKVConnection hwKVConnection) {
                    return hwKVConnection.put(this.f$0, this.f$1, this.f$2);
                }
            });
        }
    }

    public boolean remove(long key) throws HwKVFullException {
        if (this.mIsGeneralKV) {
            Log.w(TAG, "prohibit removing a long key from a general key-value database.");
            return false;
        } else if (checkWritePermission()) {
            return executeWriteOperation(new WriteOperator(key) {
                /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$w465mtG8Y4HwKSY_qHBGsiSg4R0 */
                private final /* synthetic */ long f$0;

                {
                    this.f$0 = r1;
                }

                @Override // com.huawei.kvdb.HwKVDatabase.WriteOperator
                public final boolean operate(HwKVConnection hwKVConnection) {
                    return hwKVConnection.remove(this.f$0);
                }
            });
        } else {
            Log.w(TAG, "permission denied to remove a key-value pair to database, please check.");
            return false;
        }
    }

    public boolean remove(String key) throws HwKVFullException {
        if (this.mIsGeneralKV) {
            return executeWriteOperation(new WriteOperator(key) {
                /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$KYmU4ZD1CBTiSTtvKAN7gqmYXn0 */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // com.huawei.kvdb.HwKVDatabase.WriteOperator
                public final boolean operate(HwKVConnection hwKVConnection) {
                    return hwKVConnection.remove(this.f$0);
                }
            });
        }
        Log.w(TAG, "prohibit removing a string key from a thumbnail key-value database.");
        return false;
    }

    public byte[] get(String key) {
        return (byte[]) executeReadOperation(new ReadOperator(key) {
            /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$XiJLIobWWMBwvRuBGLbNmPabXn0 */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
            public final Optional operate(HwKVConnection hwKVConnection) {
                return Optional.ofNullable(hwKVConnection.get(this.f$0));
            }
        }).orElse(null);
    }

    public Bitmap getBitmap(long key, BitmapFactory.Options options) {
        Optional<byte[]> result = executeReadOperation(new ReadOperator(key) {
            /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$396bky2UJ1DhbW5yYPN6gpWrqdg */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
            public final Optional operate(HwKVConnection hwKVConnection) {
                return Optional.ofNullable(hwKVConnection.get(this.f$0));
            }
        });
        if (!result.isPresent()) {
            return null;
        }
        byte[] data = result.get();
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public boolean hasKey(long key) {
        return ((Boolean) executeReadOperation(new ReadOperator(key) {
            /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$NGytbenA1FT9zrLynhXJjF3LQY */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
            public final Optional operate(HwKVConnection hwKVConnection) {
                return Optional.of(Boolean.valueOf(hwKVConnection.hasKey(this.f$0)));
            }
        }).orElse(false)).booleanValue();
    }

    public boolean hasKey(String key) {
        if (this.mIsGeneralKV) {
            return ((Boolean) executeReadOperation(new ReadOperator(key) {
                /* class com.huawei.kvdb.$$Lambda$HwKVDatabase$lQD9la72_Os3Evn6gzbN3vRKjE */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
                public final Optional operate(HwKVConnection hwKVConnection) {
                    return Optional.of(Boolean.valueOf(hwKVConnection.hasKey(this.f$0)));
                }
            }).orElse(false)).booleanValue();
        }
        Log.w(TAG, "prohibit has a string key from a thumbnail key-value database.");
        return false;
    }

    public HwKVConnection getKVConnection() {
        return this.mKVConnectionPool.getReadOnlyConnection();
    }

    public void releaseKVConnection(HwKVConnection kvConnection, boolean isDatabaseExist) {
        if (isDatabaseExist) {
            this.mKVConnectionPool.releaseReadOnlyConnection(kvConnection);
        } else {
            this.mKVConnectionPool.closeExceptionConnection(kvConnection, false);
        }
    }

    public int getKeyNum() {
        return ((Integer) executeReadOperation($$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6bV5JGkYDs.INSTANCE).orElse(0)).intValue();
    }

    public Hashtable<Long, Long> getAllKeys() {
        return (Hashtable) executeReadOperation($$Lambda$HwKVDatabase$kuN0hrGGqAKpbNql_r0VlP9uk.INSTANCE).orElse(null);
    }

    public Set<String> getKeys() {
        if (this.mIsGeneralKV) {
            return (Set) executeReadOperation($$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA.INSTANCE).orElse(Collections.emptySet());
        }
        Log.w(TAG, "prohibit get String keys from a thumbnail key-value database.");
        return Collections.emptySet();
    }

    public HwKVConnectionPool getKVConnectionPool() {
        return this.mKVConnectionPool;
    }

    public void closeAllConnections() {
        HwKVConnectionPool hwKVConnectionPool = this.mKVConnectionPool;
        if (hwKVConnectionPool != null) {
            hwKVConnectionPool.closeConnection();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        if (1 != 0) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
        if (0 == 0) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r6.mKVConnectionPool.releaseReadOnlyConnection(r0);
     */
    private <T> Optional<T> executeReadOperation(ReadOperator<T> operator) {
        HwKVConnection kvConnection = this.mKVConnectionPool.getReadOnlyConnection();
        if (kvConnection == null) {
            return Optional.empty();
        }
        Optional<T> res = Optional.empty();
        try {
            res = operator.operate(kvConnection);
        } catch (HwKVDatabaseDeleteException e) {
            this.mKVConnectionPool.closeExceptionConnection(kvConnection, false);
        } catch (Throwable th) {
            if (0 == 0) {
                this.mKVConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
        if (1 != 0) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002d, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
        if (0 == 0) goto L_0x0013;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0013, code lost:
        r6.mKVConnectionPool.releaseWriteConnection(r0);
     */
    private boolean executeWriteOperation(WriteOperator operator) throws HwKVFullException {
        HwKVConnection kvConnection = this.mKVConnectionPool.getWriteConnection();
        if (kvConnection == null) {
            return false;
        }
        boolean isSuccessful = false;
        try {
            isSuccessful = operator.operate(kvConnection);
        } catch (HwKVDatabaseDeleteException e) {
            this.mKVConnectionPool.closeExceptionConnection(kvConnection, true);
        } catch (HwKVFullException e2) {
            throw new HwKVFullException();
        } catch (Throwable th) {
            if (0 == 0) {
                this.mKVConnectionPool.releaseWriteConnection(kvConnection);
            }
            throw th;
        }
    }
}
