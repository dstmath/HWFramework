package com.huawei.kvdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import java.util.Hashtable;

public class HwKVDatabase {
    public static final int IMAGE = 1;
    private static final long INITIALCRC = -1;
    private static final long POLY64REV = -7661587058870466123L;
    public static final int VIDEO = 3;
    private static HwKVDatabase database = null;
    private static long[] sCrcTable = new long[256];
    private HwKVConnectionPool kvConnectionPool;

    static {
        for (int i = 0; i < 256; i++) {
            long part = (long) i;
            for (int j = 0; j < 8; j++) {
                part = (part >> 1) ^ ((((int) part) & 1) != 0 ? POLY64REV : 0);
            }
            sCrcTable[i] = part;
        }
    }

    private static byte[] getBytes(String path) {
        byte[] result = new byte[(path.length() * 2)];
        int output = 0;
        for (char ch : path.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & 255);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }

    private static long crc64Long(byte[] buffer) {
        long crc = INITIALCRC;
        for (byte b : buffer) {
            crc = sCrcTable[(((int) crc) ^ b) & 255] ^ (crc >> 8);
        }
        return crc;
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

    private HwKVDatabase(Context context) {
        this.kvConnectionPool = new HwKVConnectionPool(context);
    }

    public static synchronized HwKVDatabase getInstance(Context context) {
        HwKVDatabase hwKVDatabase;
        synchronized (HwKVDatabase.class) {
            if (database == null) {
                database = new HwKVDatabase(context);
            }
            hwKVDatabase = database;
        }
        return hwKVDatabase;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        this.kvConnectionPool.closeConnection();
    }

    public boolean put(long key, byte[] value, int size) throws HwKVFullException {
        if (value == null || value.length == 0 || value.length != size) {
            return false;
        }
        HwKVConnection kvConnection = this.kvConnectionPool.getWriteConnection();
        if (kvConnection == null) {
            return false;
        }
        boolean res;
        try {
            res = kvConnection.put(key, value, size);
            if (null == null) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        } catch (HwKVDatabaseDeleteException e) {
            this.kvConnectionPool.closeExceptionConnection(kvConnection, true);
            res = false;
            if (!true) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        } catch (HwKVFullException e2) {
            throw new HwKVFullException();
        } catch (Throwable th) {
            if (null == null) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        }
        return res;
    }

    public boolean remove(long key) throws HwKVFullException {
        HwKVConnection kvConnection = this.kvConnectionPool.getWriteConnection();
        if (kvConnection == null) {
            return false;
        }
        boolean res;
        try {
            res = kvConnection.remove(key);
            if (null == null) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        } catch (HwKVDatabaseDeleteException e) {
            this.kvConnectionPool.closeExceptionConnection(kvConnection, true);
            res = false;
            if (!true) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        } catch (HwKVFullException e2) {
            throw new HwKVFullException();
        } catch (Throwable th) {
            if (null == null) {
                this.kvConnectionPool.releaseWriteConnection(kvConnection);
            }
        }
        return res;
    }

    public Bitmap getBitmap(long key, Options options) {
        HwKVConnection kvConnection = this.kvConnectionPool.getReadOnlyConnection();
        if (kvConnection == null) {
            return null;
        }
        HwKVData res;
        boolean openConnection = true;
        try {
            res = kvConnection.get(key);
        } catch (HwKVDatabaseDeleteException e) {
            res = null;
            this.kvConnectionPool.closeExceptionConnection(kvConnection, false);
            openConnection = false;
        }
        Bitmap bitmap = null;
        if (res != null) {
            try {
                bitmap = BitmapFactory.decodeByteArray(res.value, 0, res.size, options);
            } catch (Throwable th) {
                if (openConnection) {
                    this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
                }
            }
        }
        if (openConnection) {
            this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
        }
        return bitmap;
    }

    public boolean hasKey(long key) {
        HwKVConnection kvConnection = this.kvConnectionPool.getReadOnlyConnection();
        if (kvConnection == null) {
            return false;
        }
        boolean res;
        try {
            res = kvConnection.hasKey(key);
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (HwKVDatabaseDeleteException e) {
            this.kvConnectionPool.closeExceptionConnection(kvConnection, false);
            res = false;
            if (!true) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (Throwable th) {
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        }
        return res;
    }

    public HwKVConnection getKVConnection() {
        return this.kvConnectionPool.getReadOnlyConnection();
    }

    public void releaseKVConnection(HwKVConnection kvConnection, boolean isDatabaseExist) {
        if (isDatabaseExist) {
            this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
        } else {
            this.kvConnectionPool.closeExceptionConnection(kvConnection, false);
        }
    }

    public int getKeyNum() {
        HwKVConnection kvConnection = this.kvConnectionPool.getReadOnlyConnection();
        if (kvConnection == null) {
            return 0;
        }
        int res;
        try {
            res = kvConnection.getKeyNum();
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (HwKVDatabaseDeleteException e) {
            res = 0;
            this.kvConnectionPool.closeExceptionConnection(kvConnection, false);
            if (!true) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (Throwable th) {
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        }
        return res;
    }

    public Hashtable<Long, Long> getAllKeys() {
        HwKVConnection kvConnection = this.kvConnectionPool.getReadOnlyConnection();
        if (kvConnection == null) {
            return null;
        }
        Hashtable<Long, Long> hashTable;
        try {
            hashTable = kvConnection.getAllKeys();
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (HwKVDatabaseDeleteException e) {
            hashTable = null;
            this.kvConnectionPool.closeExceptionConnection(kvConnection, false);
            if (!true) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        } catch (Throwable th) {
            if (null == null) {
                this.kvConnectionPool.releaseReadOnlyConnection(kvConnection);
            }
        }
        return hashTable;
    }

    public HwKVConnectionPool getKVConnectionPool() {
        return this.kvConnectionPool;
    }
}
