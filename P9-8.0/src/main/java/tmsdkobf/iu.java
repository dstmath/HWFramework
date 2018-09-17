package tmsdkobf;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import java.util.HashMap;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.f;

public final class iu {
    private static iu sd;
    private Handler mHandler = new Handler(TMSDKContext.getApplicaionContext().getMainLooper());
    private is se = new is();
    private b sf;
    private a sg = new a(this.mHandler);
    private in sh;
    private volatile boolean si;
    private boolean sj = false;
    private kg sk;

    final class a extends ContentObserver {
        public a(Handler handler) {
            super(handler);
        }

        public void bZ() {
            try {
                TMSDKContext.getApplicaionContext().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onChange(boolean z) {
            if (iu.this.si) {
                iu.this.sf.i(true);
                iu.this.sh.a(iu.this.sf);
                synchronized (iu.this) {
                    iu.this.sf = null;
                    iu.this.sf = new b(iu.this);
                }
                iu.this.sh.addTask(iu.this.sf, null);
            }
        }
    }

    final class b implements Runnable {
        private volatile boolean sm;
        private boolean sn;

        b(iu iuVar) {
            this(false);
        }

        b(boolean z) {
            this.sn = z;
        }

        private void ca() {
            f.d("ContactsLookupCache", "reCache() started");
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            Cursor cursor = null;
            try {
                cursor = iu.this.sk.query(Phone.CONTENT_URI, new String[]{"data1", "contact_id"}, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String string = cursor.getString(0);
                        if (!(string == null || iu.aN(string) || ((Long) hashMap.put(string, Long.valueOf(cursor.getLong(1)))) == null)) {
                            f.g("ContactsLookupCache", "Duplicated number " + string);
                        }
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (this.sm) {
                        hashMap.clear();
                        return;
                    }
                    Cursor cursor2 = null;
                    try {
                        cursor2 = iu.this.sk.query(Contacts.CONTENT_URI, new String[]{"_id", "display_name"}, "has_phone_number=1", null, null);
                        if (cursor2 != null) {
                            while (cursor2.moveToNext()) {
                                long j = cursor2.getLong(0);
                                HashMap hashMap3 = hashMap2;
                                hashMap3.put(Long.valueOf(j), cursor2.getString(1));
                            }
                            if (cursor2 != null) {
                                try {
                                    cursor2.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                            if (this.sm) {
                                hashMap.clear();
                                hashMap2.clear();
                                return;
                            }
                            is isVar = new is();
                            for (Entry entry : hashMap.entrySet()) {
                                String str = (String) hashMap2.get((Long) entry.getValue());
                                if (str == null) {
                                    str = "";
                                }
                                isVar.i((String) entry.getKey(), str);
                            }
                            hashMap.clear();
                            hashMap2.clear();
                            if (this.sm) {
                                isVar.clear();
                                return;
                            }
                            synchronized (iu.this) {
                                iu.this.se.clear();
                                iu.this.se = isVar;
                            }
                            f.d("ContactsLookupCache", "reCache() finished");
                            return;
                        }
                        f.e("ContactsLookupCache", "null nameCursor");
                        if (cursor2 != null) {
                            try {
                                cursor2.close();
                            } catch (Exception e3) {
                                e3.printStackTrace();
                            }
                        }
                    } catch (Throwable e4) {
                        f.b("ContactsLookupCache", "reCache", e4);
                        if (cursor2 != null) {
                            try {
                                cursor2.close();
                            } catch (Exception e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th) {
                        if (cursor2 != null) {
                            try {
                                cursor2.close();
                            } catch (Exception e5) {
                                e5.printStackTrace();
                            }
                        }
                    }
                } else {
                    f.e("ContactsLookupCache", "null numberCursor");
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                }
            } catch (Throwable e6) {
                f.b("ContactsLookupCache", "reCache", e6);
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e7) {
                        e7.printStackTrace();
                    }
                }
            } catch (Throwable th2) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e8) {
                        e8.printStackTrace();
                    }
                }
            }
        }

        void i(boolean z) {
            this.sm = z;
        }

        public void run() {
            i(false);
            try {
                Thread.sleep(!this.sn ? 5000 : 20000);
            } catch (InterruptedException e) {
                if (this.sm) {
                    return;
                }
            }
            ca();
            iu.this.si = true;
        }
    }

    private iu() {
        this.sg.bZ();
        this.sh = im.bJ();
        this.sk = TMServiceFactory.getSysDBService();
    }

    private static boolean aN(String str) {
        boolean z = false;
        if (str == null) {
            return false;
        }
        int indexOf = str.indexOf(64);
        if (indexOf == -1 || indexOf > str.length() - 3) {
            return false;
        }
        if (str.indexOf(46, indexOf + 2) >= 0) {
            z = true;
        }
        return z;
    }

    public static iu bY() {
        if (sd == null) {
            Class cls = iu.class;
            synchronized (iu.class) {
                if (sd == null) {
                    sd = new iu();
                }
            }
        }
        return sd;
    }

    public synchronized String aL(String str) {
        if (str == null) {
            return null;
        }
        if (this.si) {
            return this.se.getName(str);
        }
        String[] strArr = new String[]{"display_name", "number"};
        Cursor cursor = null;
        String str2 = null;
        try {
            cursor = this.sk.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(str)), strArr, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    str2 = cursor.getString(0);
                }
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable e) {
                    f.b("ContactsLookupCache", "closing Cursor", e);
                }
            }
        } catch (Throwable e2) {
            f.b("ContactsLookupCache", "lookupName", e2);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable e22) {
                    f.b("ContactsLookupCache", "closing Cursor", e22);
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable e3) {
                    f.b("ContactsLookupCache", "closing Cursor", e3);
                }
            }
        }
        Class cls = iu.class;
        synchronized (iu.class) {
            if (!this.sj) {
                this.sf = new b(true);
                this.sh.a(1, this.sf, null);
                this.sj = true;
            }
            return str2;
        }
    }

    public boolean aM(String str) {
        return aL(str) != null;
    }
}
