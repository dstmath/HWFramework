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
import tmsdk.common.utils.n;
import tmsdkobf.im;
import tmsdkobf.iu;
import tmsdkobf.kg;
import tmsdkobf.mg;
import tmsdkobf.qc;

public final class DefaultSysDao extends AbsSysDao {
    private static volatile DefaultSysDao zU;
    private static final Uri zV = Uri.parse("content://icc/adn");
    private Context mContext;
    private kg sk = TMServiceFactory.getSysDBService();
    private c zW;
    private iu zX;

    private interface c {
        List<ContactEntity> eR();
    }

    final class a implements c {
        private final Uri zY = People.CONTENT_URI;
        private final Uri zZ = Phones.CONTENT_URI;

        a() {
        }

        public List<ContactEntity> eR() {
            List arrayList = new ArrayList();
            Cursor query = DefaultSysDao.this.sk.query(this.zY, new String[]{"_id", "number", "display_name"}, null, null, "name asc");
            if (DefaultSysDao.this.d(query)) {
                while (!query.isAfterLast()) {
                    try {
                        String string = query.getString(1);
                        if (mg.bX(string)) {
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
            DefaultSysDao.this.e(query);
            return arrayList;
        }
    }

    final class b implements c {
        private Uri mContactUri = Contacts.CONTENT_URI;

        b() {
        }

        public List<ContactEntity> eR() {
            Cursor query;
            int columnIndex;
            int columnIndex2;
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            List<ContactEntity> arrayList = new ArrayList();
            synchronized (this.mContactUri) {
                query = DefaultSysDao.this.sk.query(this.mContactUri, null, "has_phone_number=1", null, null);
                if (DefaultSysDao.this.d(query)) {
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
                DefaultSysDao.this.e(query);
            }
            synchronized (Phone.CONTENT_URI) {
                query = DefaultSysDao.this.sk.query(Phone.CONTENT_URI, null, null, null, null);
                if (DefaultSysDao.this.d(query)) {
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
                DefaultSysDao.this.e(query);
            }
            for (Entry entry : hashMap2.entrySet()) {
                String str = (String) entry.getKey();
                columnIndex2 = ((Integer) entry.getValue()).intValue();
                String str2 = (String) hashMap.get(Integer.valueOf(columnIndex2));
                if (mg.bX(str) && str != null && str.trim().length() > 0) {
                    ContactEntity contactEntity = new ContactEntity();
                    contactEntity.id = columnIndex2;
                    contactEntity.name = str2;
                    contactEntity.phonenum = str.replaceAll("[ -]+", "");
                    arrayList.add(contactEntity);
                }
            }
            return arrayList;
        }
    }

    private DefaultSysDao(Context context) {
        Object obj = null;
        this.mContext = context;
        if (n.iX() >= 5) {
            obj = 1;
        }
        this.zW = obj == null ? new a() : new b();
        this.zX = iu.bY();
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
            qc qcVar = im.rE;
            if (qcVar != null) {
                Object ir = qcVar.ir();
                if (!(TextUtils.isEmpty(smsEntity.fromCard) || TextUtils.isEmpty(ir))) {
                    contentValues.put(ir, smsEntity.fromCard);
                }
                if (!TextUtils.isEmpty(smsEntity.fromCard)) {
                    try {
                        String a = qcVar.a(this.mContext, Integer.parseInt(smsEntity.fromCard));
                        String is = qcVar.is();
                        if (!(is == null || a == null)) {
                            contentValues.put(is, a);
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
        int length;
        SmsEntity smsEntity = new SmsEntity();
        smsEntity.id = cursor.getInt(cursor.getColumnIndex("_id"));
        smsEntity.phonenum = cursor.getString(cursor.getColumnIndex("address"));
        if (smsEntity.phonenum != null && smsEntity.phonenum.contains(" ")) {
            String[] split = smsEntity.phonenum.trim().split("\\s+");
            String str = "";
            length = split.length;
            if (length > 0) {
                str = split[0];
                for (int i = 1; i < length; i++) {
                    str = str.concat(split[i]);
                }
                smsEntity.phonenum = str;
            }
        }
        smsEntity.type = cursor.getInt(cursor.getColumnIndex("type"));
        smsEntity.body = cursor.getString(cursor.getColumnIndex("body"));
        smsEntity.date = cursor.getLong(cursor.getColumnIndex("date"));
        int columnIndex = cursor.getColumnIndex("service_center");
        if (columnIndex != -1) {
            smsEntity.serviceCenter = cursor.getString(columnIndex);
        }
        qc bM = im.bM();
        if (!(bM == null || bM.ir() == null)) {
            length = cursor.getColumnIndex(bM.ir());
            if (length > 0) {
                smsEntity.fromCard = cursor.getString(length);
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
        qc bM = im.bM();
        if (!(bM == null || bM.it() == null)) {
            int columnIndex = cursor.getColumnIndex(bM.it());
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
        if (zU == null) {
            Class cls = DefaultSysDao.class;
            synchronized (DefaultSysDao.class) {
                if (zU == null) {
                    zU = new DefaultSysDao(context);
                }
            }
        }
        return zU;
    }

    public boolean contains(String str) {
        return this.zX.aM(str);
    }

    public List<CallLogEntity> getAllCallLog() {
        List<CallLogEntity> arrayList = new ArrayList();
        synchronized (Calls.CONTENT_URI) {
            Cursor cursor = null;
            try {
                cursor = this.sk.query(Calls.CONTENT_URI, null, null, null, "date DESC");
                if (d(cursor)) {
                    while (!cursor.isAfterLast()) {
                        if (mg.bX(cursor.getString(cursor.getColumnIndex("number")))) {
                            arrayList.add(c(cursor));
                        }
                        cursor.moveToNext();
                    }
                }
                e(cursor);
            } catch (Exception e) {
                e.printStackTrace();
                e(cursor);
            } catch (Throwable th) {
                e(cursor);
            }
        }
        return arrayList;
    }

    public List<ContactEntity> getAllContact() {
        try {
            return this.zW.eR();
        } catch (Exception e) {
            return new ArrayList();
        }
    }

    public CallLogEntity getLastCallLog() {
        CallLogEntity callLogEntity = null;
        Cursor cursor = null;
        try {
            cursor = this.sk.query(Calls.CONTENT_URI, null, null, null, "_id DESC LIMIT 1");
            if (d(cursor)) {
                callLogEntity = c(cursor);
            }
        } catch (Exception e) {
        }
        e(cursor);
        if (callLogEntity != null) {
            callLogEntity.phonenum = callLogEntity.phonenum.length() != 1 ? callLogEntity.phonenum : "null";
        }
        return callLogEntity;
    }

    /* JADX WARNING: Missing block: B:20:0x004f, code:
            if ((r10 < 0 ? 1 : null) != null) goto L_0x0051;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsEntity getLastInBoxSms(int i, int i2) {
        SmsEntity smsEntity = null;
        synchronized (Sms.CONTENT_URI) {
            try {
                Cursor query = this.sk.query(Sms.CONTENT_URI, null, "type=1 AND read=" + i2, null, "_id DESC");
                if (d(query)) {
                    smsEntity = b(query);
                    long currentTimeMillis = System.currentTimeMillis() - smsEntity.date;
                    if (i >= 0) {
                        if ((currentTimeMillis > ((long) (i * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : null) == null) {
                        }
                        smsEntity = null;
                    }
                }
                e(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return smsEntity;
    }

    @Deprecated
    public SmsEntity getLastOutBoxSms(int i) {
        return getLastSentSms(i);
    }

    /* JADX WARNING: Missing block: B:21:0x003f, code:
            if ((r10 < 0 ? 1 : null) != null) goto L_0x0041;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsEntity getLastSentSms(int i) {
        SmsEntity smsEntity = null;
        synchronized (Sms.CONTENT_URI) {
            Cursor cursor = null;
            try {
                cursor = this.sk.query(Sms.CONTENT_URI, null, "type=2", null, "_id DESC");
                if (d(cursor)) {
                    smsEntity = b(cursor);
                    long currentTimeMillis = System.currentTimeMillis() - smsEntity.date;
                    if (i >= 0) {
                        if ((currentTimeMillis > ((long) (i * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : null) == null) {
                        }
                        smsEntity = null;
                    }
                }
                e(cursor);
            } catch (Exception e) {
                e.printStackTrace();
                e(cursor);
            } catch (Throwable th) {
                e(cursor);
            }
        }
        return smsEntity;
    }

    public List<ContactEntity> getSimContact() {
        String[] strArr = new String[]{"_id", "name", "traffic"};
        List<ContactEntity> arrayList = new ArrayList();
        synchronized (zV) {
            try {
                Cursor query = this.sk.query(zV, strArr, null, null, null);
                if (query != null && d(query)) {
                    while (!query.isAfterLast()) {
                        ContactEntity contactEntity = new ContactEntity();
                        contactEntity.id = query.getInt(query.getColumnIndex("_id"));
                        contactEntity.name = query.getString(query.getColumnIndex("name"));
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

    /* JADX WARNING: Missing block: B:16:0x001d, code:
            if (r7.protocolType != 2) goto L_0x0018;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Uri insert(SmsEntity smsEntity, boolean z) {
        Uri uri;
        uri = null;
        if (smsEntity.protocolType != 0) {
        }
        ContentValues a = a(smsEntity, z);
        synchronized (Sms.CONTENT_URI) {
            uri = this.sk.insert(Sms.CONTENT_URI, a);
            if (uri == null) {
                uri = this.sk.insert(Uri.parse("content://sms/inbox"), a);
            }
        }
        return uri;
    }

    public boolean remove(CallLogEntity callLogEntity) {
        boolean z = false;
        synchronized (Calls.CONTENT_URI) {
            if (this.sk.delete(Calls.CONTENT_URI, "_id=" + callLogEntity.id, null) > 0) {
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
        Uri uri2 = uri;
        synchronized (uri) {
            if (this.sk.delete(uri, "_id=" + smsEntity.id, null) > 0) {
                z = true;
            }
            return z;
        }
    }

    public boolean supportThisPhone() {
        return false;
    }
}
