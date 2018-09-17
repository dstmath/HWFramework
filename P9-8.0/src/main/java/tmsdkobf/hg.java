package tmsdkobf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import java.io.File;
import java.lang.reflect.Method;

public abstract class hg {
    private static Method pA = null;
    private static Class<?> py = null;
    private static Method pz = null;
    private SQLiteDatabase mDatabase = null;
    private final String mName;
    private final int mNewVersion;
    private final CursorFactory pv;
    private final String pw;
    private boolean px = false;

    public hg(Context context, String str, CursorFactory cursorFactory, int i, String str2) {
        if (i >= 1) {
            Class cls = hg.class;
            synchronized (hg.class) {
                this.mName = str;
                this.pv = cursorFactory;
                this.mNewVersion = i;
                this.pw = str2;
                try {
                    py = Class.forName("android.database.sqlite.SQLiteDatabase");
                    pz = py.getDeclaredMethod("lock", new Class[0]);
                    pA = py.getDeclaredMethod("unlock", new Class[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        throw new IllegalArgumentException("Version must be >= 1, was " + i);
    }

    public File as(String str) {
        File file = new File(this.pw);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(this.pw + str);
    }

    public void close() {
        Class cls = hg.class;
        synchronized (hg.class) {
            if (this.px) {
                throw new IllegalStateException("Closed during initialization");
            }
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                    this.mDatabase = null;
                }
            }
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        Class cls = hg.class;
        synchronized (hg.class) {
            if (this.mDatabase != null && this.mDatabase.isOpen() && !this.mDatabase.isReadOnly()) {
                SQLiteDatabase sQLiteDatabase = this.mDatabase;
                return sQLiteDatabase;
            } else if (this.px) {
                throw new IllegalStateException("getWritableDatabase called recursively");
            } else {
                if (this.mDatabase != null) {
                    try {
                        pz.invoke(this.mDatabase, new Object[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                SQLiteDatabase sQLiteDatabase2 = null;
                try {
                    this.px = true;
                    sQLiteDatabase2 = this.mName != null ? SQLiteDatabase.openOrCreateDatabase(as(this.mName).getPath(), this.pv) : SQLiteDatabase.create(null);
                    if (sQLiteDatabase2 != null) {
                        int version = sQLiteDatabase2.getVersion();
                        if (version != this.mNewVersion) {
                            sQLiteDatabase2.beginTransaction();
                            if (version != 0) {
                                onUpgrade(sQLiteDatabase2, version, this.mNewVersion);
                            } else {
                                onCreate(sQLiteDatabase2);
                            }
                            sQLiteDatabase2.setVersion(this.mNewVersion);
                            sQLiteDatabase2.setTransactionSuccessful();
                            sQLiteDatabase2.endTransaction();
                        }
                        onOpen(sQLiteDatabase2);
                        SQLiteDatabase sQLiteDatabase3 = sQLiteDatabase2;
                        this.px = false;
                        if (this.mDatabase != null) {
                            try {
                                this.mDatabase.close();
                                pA.invoke(this.mDatabase, new Object[0]);
                            } catch (Exception e2) {
                            }
                        }
                        this.mDatabase = sQLiteDatabase2;
                        return sQLiteDatabase3;
                    }
                    this.px = false;
                    if (this.mDatabase != null) {
                        try {
                            pA.invoke(this.mDatabase, new Object[0]);
                        } catch (Exception e3) {
                        }
                    }
                    if (sQLiteDatabase2 != null) {
                        sQLiteDatabase2.close();
                    }
                    return null;
                } catch (SQLiteException e4) {
                    this.px = false;
                    if (this.mDatabase != null) {
                        try {
                            pA.invoke(this.mDatabase, new Object[0]);
                        } catch (Exception e5) {
                        }
                    }
                    if (sQLiteDatabase2 != null) {
                        sQLiteDatabase2.close();
                    }
                    return null;
                } catch (Throwable th) {
                    this.px = false;
                    if (this.mDatabase != null) {
                        try {
                            pA.invoke(this.mDatabase, new Object[0]);
                        } catch (Exception e6) {
                        }
                    }
                    if (sQLiteDatabase2 != null) {
                        sQLiteDatabase2.close();
                    }
                }
            }
        }
    }

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public void onOpen(SQLiteDatabase sQLiteDatabase) {
    }

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
}
