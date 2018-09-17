package tmsdk.common.module.aresengine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.j;
import tmsdkobf.jq;
import tmsdkobf.jz;
import tmsdkobf.lo;
import tmsdkobf.nf;
import tmsdkobf.qz;

/* compiled from: Unknown */
public final class DefaultSysDao extends AbsSysDao {
    private static volatile DefaultSysDao Ci;
    private static final Uri Cj = null;
    private c Ck;
    private jz Cl;
    private Context mContext;
    private lo uU;

    /* compiled from: Unknown */
    private interface c {
        List<ContactEntity> fj();
    }

    /* compiled from: Unknown */
    final class a implements c {
        private final Uri Cm;
        private final Uri Cn;
        final /* synthetic */ DefaultSysDao Co;

        a(DefaultSysDao defaultSysDao) {
            this.Co = defaultSysDao;
            this.Cm = People.CONTENT_URI;
            this.Cn = Phones.CONTENT_URI;
        }

        public List<ContactEntity> fj() {
            List arrayList = new ArrayList();
            Cursor query = this.Co.uU.query(this.Cm, new String[]{"_id", "number", "display_name"}, null, null, "name asc");
            if (this.Co.d(query)) {
                while (!query.isAfterLast()) {
                    try {
                        String string = query.getString(1);
                        if (nf.cJ(string)) {
                            ContactEntity contactEntity = new ContactEntity();
                            contactEntity.id = query.getInt(0);
                            contactEntity.phonenum = string.replaceAll("[ -]+", "");
                            contactEntity.name = query.getString(2);
                            arrayList.add(contactEntity);
                        }
                        query.moveToNext();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            this.Co.e(query);
            return arrayList;
        }
    }

    /* compiled from: Unknown */
    final class b implements c {
        final /* synthetic */ DefaultSysDao Co;
        private Uri mContactUri;

        b(DefaultSysDao defaultSysDao) {
            this.Co = defaultSysDao;
            this.mContactUri = Contacts.CONTENT_URI;
        }

        public List<ContactEntity> fj() {
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            List<ContactEntity> arrayList = new ArrayList();
            synchronized (this.mContactUri) {
                int columnIndex;
                int columnIndex2;
                Cursor query = this.Co.uU.query(this.mContactUri, null, "has_phone_number=1", null, null);
                if (this.Co.d(query)) {
                    columnIndex = query.getColumnIndex("_id");
                    columnIndex2 = query.getColumnIndex("display_name");
                    while (!query.isAfterLast()) {
                        try {
                            hashMap.put(Integer.valueOf(query.getInt(columnIndex)), query.getString(columnIndex2));
                            query.moveToNext();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.Co.e(query);
            }
            synchronized (Phone.CONTENT_URI) {
                query = this.Co.uU.query(Phone.CONTENT_URI, null, null, null, null);
                if (this.Co.d(query)) {
                    columnIndex = query.getColumnIndex("data1");
                    columnIndex2 = query.getColumnIndex("contact_id");
                    while (!query.isAfterLast()) {
                        try {
                            hashMap2.put(query.getString(columnIndex), Integer.valueOf(query.getInt(columnIndex2)));
                            query.moveToNext();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                this.Co.e(query);
            }
            for (Entry entry : hashMap2.entrySet()) {
                String str = (String) entry.getKey();
                int intValue = ((Integer) entry.getValue()).intValue();
                String str2 = (String) hashMap.get(Integer.valueOf(intValue));
                if (nf.cJ(str) && str != null && str.trim().length() > 0) {
                    ContactEntity contactEntity = new ContactEntity();
                    contactEntity.id = intValue;
                    contactEntity.name = str2;
                    contactEntity.phonenum = str.replaceAll("[ -]+", "");
                    arrayList.add(contactEntity);
                }
            }
            return arrayList;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.aresengine.DefaultSysDao.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.aresengine.DefaultSysDao.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.aresengine.DefaultSysDao.<clinit>():void");
    }

    private DefaultSysDao(Context context) {
        Object obj = null;
        this.mContext = context;
        this.uU = TMServiceFactory.getSysDBService();
        if (j.iM() >= 5) {
            obj = 1;
        }
        this.Ck = obj == null ? new a(this) : new b(this);
        this.Cl = jz.cM();
    }

    private ContentValues a(SmsEntity smsEntity, boolean z) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("address", smsEntity.phonenum);
        contentValues.put("body", smsEntity.body);
        contentValues.put("date", Long.valueOf(smsEntity.date));
        contentValues.put("read", Integer.valueOf(smsEntity.read));
        contentValues.put("type", Integer.valueOf(smsEntity.type));
        contentValues.put("service_center", smsEntity.serviceCenter == null ? "" : smsEntity.serviceCenter);
        if (!z) {
            qz qzVar = jq.uh;
            if (qzVar != null) {
                Object ii = qzVar.ii();
                if (!(TextUtils.isEmpty(smsEntity.fromCard) || TextUtils.isEmpty(ii))) {
                    contentValues.put(ii, smsEntity.fromCard);
                }
                if (!TextUtils.isEmpty(smsEntity.fromCard)) {
                    try {
                        String a = qzVar.a(this.mContext, Integer.parseInt(smsEntity.fromCard));
                        String ij = qzVar.ij();
                        if (!(ij == null || a == null)) {
                            contentValues.put(ij, a);
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }
        }
        return contentValues;
    }

    private SmsEntity b(Cursor cursor) {
        int i;
        SmsEntity smsEntity = new SmsEntity();
        smsEntity.id = cursor.getInt(cursor.getColumnIndex("_id"));
        smsEntity.phonenum = cursor.getString(cursor.getColumnIndex("address"));
        if (smsEntity.phonenum != null && smsEntity.phonenum.contains(" ")) {
            String[] split = smsEntity.phonenum.trim().split("\\s+");
            String str = "";
            int length = split.length;
            if (length > 0) {
                String str2 = split[0];
                for (i = 1; i < length; i++) {
                    str2 = str2.concat(split[i]);
                }
                smsEntity.phonenum = str2;
            }
        }
        smsEntity.type = cursor.getInt(cursor.getColumnIndex("type"));
        smsEntity.body = cursor.getString(cursor.getColumnIndex("body"));
        smsEntity.date = cursor.getLong(cursor.getColumnIndex("date"));
        i = cursor.getColumnIndex("service_center");
        if (i != -1) {
            smsEntity.serviceCenter = cursor.getString(i);
        }
        qz cv = jq.cv();
        if (!(cv == null || cv.ii() == null)) {
            i = cursor.getColumnIndex(cv.ii());
            if (i > 0) {
                smsEntity.fromCard = cursor.getString(i);
            }
        }
        return smsEntity;
    }

    private CallLogEntity c(Cursor cursor) {
        CallLogEntity callLogEntity = new CallLogEntity();
        callLogEntity.id = cursor.getInt(cursor.getColumnIndex("_id"));
        callLogEntity.phonenum = cursor.getString(cursor.getColumnIndex("number")).replaceAll("[ -]+", "");
        callLogEntity.type = cursor.getInt(cursor.getColumnIndex("type"));
        callLogEntity.duration = cursor.getLong(cursor.getColumnIndex("duration"));
        callLogEntity.date = cursor.getLong(cursor.getColumnIndex("date"));
        qz cv = jq.cv();
        if (!(cv == null || cv.ik() == null)) {
            int columnIndex = cursor.getColumnIndex(cv.ik());
            if (columnIndex > -1) {
                callLogEntity.fromCard = cursor.getString(columnIndex);
            }
        }
        return callLogEntity;
    }

    private boolean d(Cursor cursor) {
        return cursor != null && cursor.moveToFirst();
    }

    private void e(Cursor cursor) {
        if (cursor != null) {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static DefaultSysDao getInstance(Context context) {
        if (Ci == null) {
            synchronized (DefaultSysDao.class) {
                if (Ci == null) {
                    Ci = new DefaultSysDao(context);
                }
            }
        }
        return Ci;
    }

    public boolean contains(String str) {
        return this.Cl.bM(str);
    }

    public List<CallLogEntity> getAllCallLog() {
        Exception e;
        Throwable th;
        List<CallLogEntity> arrayList = new ArrayList();
        synchronized (Calls.CONTENT_URI) {
            Cursor query;
            try {
                query = this.uU.query(Calls.CONTENT_URI, null, null, null, "date DESC");
                try {
                    if (d(query)) {
                        while (!query.isAfterLast()) {
                            if (nf.cJ(query.getString(query.getColumnIndex("number")))) {
                                arrayList.add(c(query));
                            }
                            query.moveToNext();
                        }
                    }
                    e(query);
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                query = null;
                try {
                    e.printStackTrace();
                    e(query);
                    return arrayList;
                } catch (Throwable th2) {
                    th = th2;
                    e(query);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                query = null;
                e(query);
                throw th;
            }
        }
        return arrayList;
    }

    public List<ContactEntity> getAllContact() {
        try {
            return this.Ck.fj();
        } catch (Exception e) {
            return new ArrayList();
        }
    }

    public CallLogEntity getLastCallLog() {
        Cursor query;
        CallLogEntity c;
        Cursor cursor;
        try {
            query = this.uU.query(Calls.CONTENT_URI, null, null, null, "_id DESC LIMIT 1");
            try {
                c = !d(query) ? null : c(query);
            } catch (Exception e) {
                cursor = query;
                query = cursor;
                c = null;
                e(query);
                if (c != null) {
                    c.phonenum = c.phonenum.length() != 1 ? c.phonenum : "null";
                }
                return c;
            }
        } catch (Exception e2) {
            cursor = null;
            query = cursor;
            c = null;
            e(query);
            if (c != null) {
                if (c.phonenum.length() != 1) {
                }
                c.phonenum = c.phonenum.length() != 1 ? c.phonenum : "null";
            }
            return c;
        }
        e(query);
        if (c != null) {
            if (c.phonenum.length() != 1) {
            }
            c.phonenum = c.phonenum.length() != 1 ? c.phonenum : "null";
        }
        return c;
    }

    public SmsEntity getLastInBoxSms(int i, int i2) {
        SmsEntity b;
        Exception exception;
        SmsEntity smsEntity = null;
        synchronized (Sms.CONTENT_URI) {
            try {
                Cursor query = this.uU.query(Sms.CONTENT_URI, null, "type=1 AND read=" + i2, null, "_id DESC");
                if (d(query)) {
                    b = b(query);
                    long currentTimeMillis = System.currentTimeMillis() - b.date;
                    if (i >= 0) {
                        if ((currentTimeMillis > ((long) (i * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : null) == null) {
                            if ((currentTimeMillis < 0 ? 1 : null) == null) {
                            }
                        }
                    }
                    e(query);
                }
                b = null;
                try {
                    e(query);
                } catch (Exception e) {
                    smsEntity = b;
                    exception = e;
                    exception.printStackTrace();
                    b = smsEntity;
                    return b;
                }
            } catch (Exception e2) {
                exception = e2;
                exception.printStackTrace();
                b = smsEntity;
                return b;
            }
        }
        return b;
    }

    @Deprecated
    public SmsEntity getLastOutBoxSms(int i) {
        return getLastSentSms(i);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsEntity getLastSentSms(int i) {
        SmsEntity b;
        Exception e;
        Cursor cursor;
        Object obj;
        Throwable th;
        Cursor cursor2 = null;
        synchronized (Sms.CONTENT_URI) {
            try {
                Cursor query = this.uU.query(Sms.CONTENT_URI, null, "type=2", null, "_id DESC");
                try {
                    if (d(query)) {
                        b = b(query);
                        long currentTimeMillis = System.currentTimeMillis() - b.date;
                        if (i >= 0) {
                            if ((currentTimeMillis > ((long) (i * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : null) == null) {
                                if ((currentTimeMillis < 0 ? 1 : null) == null) {
                                }
                            }
                        }
                        e(query);
                    }
                    b = null;
                    e(query);
                } catch (Exception e2) {
                    e = e2;
                    cursor = null;
                    cursor2 = query;
                    try {
                        e.printStackTrace();
                        e(cursor2);
                        obj = cursor;
                        return b;
                    } catch (Throwable th2) {
                        th = th2;
                        e(cursor2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    cursor2 = query;
                    e(cursor2);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                cursor = null;
                e.printStackTrace();
                e(cursor2);
                obj = cursor;
                return b;
            }
        }
        return b;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<ContactEntity> getSimContact() {
        String[] strArr = new String[]{"_id", CheckVersionField.CHECK_VERSION_NAME, "traffic"};
        List<ContactEntity> arrayList = new ArrayList();
        synchronized (Cj) {
            try {
                Cursor query = this.uU.query(Cj, strArr, null, null, null);
                if (query != null && d(query)) {
                    while (!query.isAfterLast()) {
                        ContactEntity contactEntity = new ContactEntity();
                        contactEntity.id = query.getInt(query.getColumnIndex("_id"));
                        contactEntity.name = query.getString(query.getColumnIndex(CheckVersionField.CHECK_VERSION_NAME));
                        contactEntity.phonenum = query.getString(query.getColumnIndex("traffic"));
                        contactEntity.isSimContact = true;
                        if (contactEntity.phonenum != null) {
                            arrayList.add(contactEntity);
                        }
                        query.moveToNext();
                    }
                }
                e(query);
            } catch (Exception e) {
                e.printStackTrace();
                return arrayList;
            }
        }
        return arrayList;
    }

    public synchronized Uri insert(SmsEntity smsEntity) {
        return insert(smsEntity, false);
    }

    public synchronized Uri insert(SmsEntity smsEntity, boolean z) {
        Uri uri = null;
        synchronized (this) {
            if (smsEntity.protocolType != 0) {
                if (smsEntity.protocolType != 2) {
                }
            }
            ContentValues a = a(smsEntity, z);
            synchronized (Sms.CONTENT_URI) {
                uri = this.uU.insert(Sms.CONTENT_URI, a);
                if (uri == null) {
                    uri = this.uU.insert(Uri.parse("content://sms/inbox"), a);
                }
            }
        }
        return uri;
    }

    public boolean remove(CallLogEntity callLogEntity) {
        boolean z = false;
        synchronized (Calls.CONTENT_URI) {
            if (this.uU.delete(Calls.CONTENT_URI, "_id=" + callLogEntity.id, null) > 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean remove(SmsEntity smsEntity) {
        boolean z = false;
        Uri uri = Sms.CONTENT_URI;
        if (smsEntity.protocolType == 1) {
            uri = Mms.CONTENT_URI;
        }
        synchronized (uri) {
            if (this.uU.delete(uri, "_id=" + smsEntity.id, null) > 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean supportThisPhone() {
        return false;
    }
}
