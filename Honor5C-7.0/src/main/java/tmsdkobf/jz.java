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
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class jz {
    private static jz uN;
    private Handler mHandler;
    private jx uO;
    private b uP;
    private a uQ;
    private jr uR;
    private volatile boolean uS;
    private boolean uT;
    private lo uU;

    /* compiled from: Unknown */
    final class a extends ContentObserver {
        final /* synthetic */ jz uV;

        public a(jz jzVar, Handler handler) {
            this.uV = jzVar;
            super(handler);
        }

        public void cN() {
            try {
                TMSDKContext.getApplicaionContext().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onChange(boolean z) {
            if (this.uV.uS) {
                this.uV.uP.w(true);
                this.uV.uR.a(this.uV.uP);
                synchronized (this.uV) {
                    this.uV.uP = null;
                    this.uV.uP = new b(this.uV);
                }
                this.uV.uR.a(this.uV.uP, null);
            }
        }
    }

    /* compiled from: Unknown */
    final class b implements Runnable {
        final /* synthetic */ jz uV;
        private volatile boolean uW;
        private boolean uX;

        b(jz jzVar) {
            this(jzVar, false);
        }

        b(jz jzVar, boolean z) {
            this.uV = jzVar;
            this.uX = z;
        }

        private void cO() {
            Cursor query;
            Throwable e;
            Cursor cursor = null;
            d.e("ContactsLookupCache", "reCache() started");
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            long j;
            jx jxVar;
            String str;
            try {
                query = this.uV.uU.query(Phone.CONTENT_URI, new String[]{"data1", "contact_id"}, null, null, null);
                if (query != null) {
                    while (query.moveToNext()) {
                        try {
                            String string = query.getString(0);
                            if (!(string == null || jz.bN(string) || ((Long) hashMap.put(string, Long.valueOf(query.getLong(1)))) == null)) {
                                d.f("ContactsLookupCache", "Duplicated number " + string);
                            }
                        } catch (Exception e2) {
                            e = e2;
                        }
                    }
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Exception e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (this.uW) {
                        try {
                            cursor = this.uV.uU.query(Contacts.CONTENT_URI, new String[]{"_id", "display_name"}, "has_phone_number=1", null, null);
                            if (cursor == null) {
                                while (cursor.moveToNext()) {
                                    j = cursor.getLong(0);
                                    hashMap2.put(Long.valueOf(j), cursor.getString(1));
                                }
                                if (cursor != null) {
                                    try {
                                        cursor.close();
                                    } catch (Exception e32) {
                                        e32.printStackTrace();
                                    }
                                }
                                if (this.uW) {
                                    jxVar = new jx();
                                    for (Entry entry : hashMap.entrySet()) {
                                        str = (String) hashMap2.get((Long) entry.getValue());
                                        if (str != null) {
                                            str = "";
                                        }
                                        jxVar.k((String) entry.getKey(), str);
                                    }
                                    hashMap.clear();
                                    hashMap2.clear();
                                    if (this.uW) {
                                        synchronized (this.uV) {
                                            this.uV.uO.clear();
                                            this.uV.uO = jxVar;
                                        }
                                        d.e("ContactsLookupCache", "reCache() finished");
                                    }
                                    jxVar.clear();
                                    return;
                                }
                                hashMap.clear();
                                hashMap2.clear();
                                return;
                            }
                            d.c("ContactsLookupCache", "null nameCursor");
                            if (cursor != null) {
                                try {
                                    cursor.close();
                                } catch (Exception e322) {
                                    e322.printStackTrace();
                                }
                            }
                            return;
                        } catch (Throwable e4) {
                            d.a("ContactsLookupCache", "reCache", e4);
                            if (cursor != null) {
                                try {
                                    cursor.close();
                                } catch (Exception e3222) {
                                    e3222.printStackTrace();
                                }
                            }
                        } catch (Throwable th) {
                            if (cursor != null) {
                                try {
                                    cursor.close();
                                } catch (Exception e5) {
                                    e5.printStackTrace();
                                }
                            }
                        }
                    } else {
                        hashMap.clear();
                        return;
                    }
                }
                d.c("ContactsLookupCache", "null numberCursor");
                if (query != null) {
                    try {
                        query.close();
                    } catch (Exception e32222) {
                        e32222.printStackTrace();
                    }
                }
            } catch (Exception e6) {
                e4 = e6;
                query = null;
                try {
                    d.a("ContactsLookupCache", "reCache", e4);
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Exception e322222) {
                            e322222.printStackTrace();
                        }
                    }
                    if (this.uW) {
                        hashMap.clear();
                        return;
                    }
                    cursor = this.uV.uU.query(Contacts.CONTENT_URI, new String[]{"_id", "display_name"}, "has_phone_number=1", null, null);
                    if (cursor == null) {
                        d.c("ContactsLookupCache", "null nameCursor");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    while (cursor.moveToNext()) {
                        j = cursor.getLong(0);
                        hashMap2.put(Long.valueOf(j), cursor.getString(1));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (this.uW) {
                        hashMap.clear();
                        hashMap2.clear();
                        return;
                    }
                    jxVar = new jx();
                    for (Entry entry2 : hashMap.entrySet()) {
                        str = (String) hashMap2.get((Long) entry2.getValue());
                        if (str != null) {
                            str = "";
                        }
                        jxVar.k((String) entry2.getKey(), str);
                    }
                    hashMap.clear();
                    hashMap2.clear();
                    if (this.uW) {
                        jxVar.clear();
                        return;
                    }
                    synchronized (this.uV) {
                        this.uV.uO.clear();
                        this.uV.uO = jxVar;
                    }
                    d.e("ContactsLookupCache", "reCache() finished");
                } catch (Throwable th2) {
                    e4 = th2;
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Exception e52) {
                            e52.printStackTrace();
                        }
                    }
                    throw e4;
                }
            } catch (Throwable th3) {
                e4 = th3;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw e4;
            }
        }

        public void run() {
            w(false);
            try {
                Thread.sleep(!this.uX ? 5000 : 20000);
            } catch (InterruptedException e) {
                if (this.uW) {
                    return;
                }
            }
            cO();
            this.uV.uS = true;
        }

        void w(boolean z) {
            this.uW = z;
        }
    }

    private jz() {
        this.uT = false;
        this.uO = new jx();
        this.mHandler = new Handler(TMSDKContext.getApplicaionContext().getMainLooper());
        this.uQ = new a(this, this.mHandler);
        this.uQ.cN();
        this.uR = jq.ct();
        this.uU = TMServiceFactory.getSysDBService();
    }

    private static boolean bN(String str) {
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

    public static jz cM() {
        if (uN == null) {
            synchronized (jz.class) {
                if (uN == null) {
                    uN = new jz();
                }
            }
        }
        return uN;
    }

    public synchronized String bL(String str) {
        Throwable e;
        String str2 = null;
        synchronized (this) {
            if (str != null) {
                if (this.uS) {
                    String name = this.uO.getName(str);
                    return name;
                }
                String[] strArr = new String[]{"display_name", "number"};
                Cursor query;
                try {
                    query = this.uU.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(str)), strArr, null, null, null);
                    if (query != null) {
                        try {
                            if (query.moveToNext()) {
                                str2 = query.getString(0);
                            }
                        } catch (Exception e2) {
                            e = e2;
                            try {
                                d.a("ContactsLookupCache", "lookupName", e);
                                if (query != null) {
                                    try {
                                        query.close();
                                    } catch (Throwable e3) {
                                        d.a("ContactsLookupCache", "closing Cursor", e3);
                                    }
                                }
                                synchronized (jz.class) {
                                    if (!this.uT) {
                                        this.uP = new b(this, true);
                                        this.uR.a(1, this.uP, null);
                                        this.uT = true;
                                    }
                                }
                                return str2;
                            } catch (Throwable th) {
                                e3 = th;
                                if (query != null) {
                                    try {
                                        query.close();
                                    } catch (Throwable e4) {
                                        d.a("ContactsLookupCache", "closing Cursor", e4);
                                    }
                                }
                                throw e3;
                            }
                        }
                    }
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Throwable e32) {
                            d.a("ContactsLookupCache", "closing Cursor", e32);
                        }
                    }
                } catch (Exception e5) {
                    e32 = e5;
                    query = null;
                    d.a("ContactsLookupCache", "lookupName", e32);
                    if (query != null) {
                        query.close();
                    }
                    synchronized (jz.class) {
                        if (this.uT) {
                            this.uP = new b(this, true);
                            this.uR.a(1, this.uP, null);
                            this.uT = true;
                        }
                    }
                    return str2;
                } catch (Throwable th2) {
                    e32 = th2;
                    query = null;
                    if (query != null) {
                        query.close();
                    }
                    throw e32;
                }
                synchronized (jz.class) {
                    if (this.uT) {
                        this.uP = new b(this, true);
                        this.uR.a(1, this.uP, null);
                        this.uT = true;
                    }
                }
                return str2;
            } else {
                return null;
            }
        }
    }

    public boolean bM(String str) {
        return bL(str) != null;
    }
}
