package com.huawei.opcollect.collector.pullcollection;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import com.huawei.nb.model.collectencrypt.DSContactsInfo;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;

public class ContactsAction extends Action {
    private static final int CALL_DIAL_TYPE = 2;
    private static final int CALL_RECEIVE_TYPE = 1;
    private static final Uri CALL_URI = CallLog.Calls.CONTENT_URI;
    private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
    private static final long DAY_IN_MILLISECOND = 86400000;
    private static final Object LOCK = new Object();
    private static final String TAG = "ContactsAction";
    private static ContactsAction sInstance = null;

    private ContactsAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(queryDailyRecordNum(DSContactsInfo.class));
    }

    public static ContactsAction getInstance(Context context) {
        ContactsAction contactsAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new ContactsAction(context, OPCollectConstant.CONTACTS_ACTION_NAME);
            }
            contactsAction = sInstance;
        }
        return contactsAction;
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        return collectDSContactsInfo();
    }

    private boolean collectDSContactsInfo() {
        new Thread(new Runnable() {
            public void run() {
                OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, ContactsAction.this.getDSContactsInfo()).sendToTarget();
            }
        }).start();
        return true;
    }

    /* access modifiers changed from: private */
    public DSContactsInfo getDSContactsInfo() {
        DSContactsInfo dsContactsInfo = new DSContactsInfo();
        dsContactsInfo.setContactNum(Integer.valueOf(getContactsNum()));
        setCallStatisticPerDay(dsContactsInfo);
        dsContactsInfo.setMReservedInt(0);
        dsContactsInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
        return dsContactsInfo;
    }

    private int getContactsNum() {
        int count = 0;
        if (this.mContext == null) {
            return 0;
        }
        Cursor contactsCursor = null;
        try {
            contactsCursor = this.mContext.getContentResolver().query(CONTACTS_URI, null, null, null, null);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "query contacts uri failed: " + e.getMessage());
        }
        if (contactsCursor != null) {
            count = contactsCursor.getCount();
            contactsCursor.close();
        }
        return count;
    }

    private void setCallStatisticPerDay(DSContactsInfo dsContactsInfo) {
        if (this.mContext != null) {
            int dialTime = 0;
            int receiveTime = 0;
            long duration = 0;
            long dayBeforeNow = System.currentTimeMillis() - DAY_IN_MILLISECOND;
            Cursor callCursor = null;
            try {
                callCursor = this.mContext.getContentResolver().query(CALL_URI, null, "date>?", new String[]{String.valueOf(dayBeforeNow)}, null);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "query call uri failed: " + e.getMessage());
            }
            if (callCursor == null) {
                OPCollectLog.e(TAG, "cursor is null.");
            } else if (callCursor.getCount() <= 0) {
                OPCollectLog.e(TAG, "cursor size <= 0");
                callCursor.close();
            } else {
                while (callCursor.moveToNext()) {
                    switch (callCursor.getInt(callCursor.getColumnIndex("type"))) {
                        case 1:
                            receiveTime++;
                            break;
                        case 2:
                            dialTime++;
                            break;
                    }
                    duration += callCursor.getLong(callCursor.getColumnIndex("duration"));
                }
                callCursor.close();
                dsContactsInfo.setCallDialNum(Integer.valueOf(dialTime));
                dsContactsInfo.setCallRecvNum(Integer.valueOf(receiveTime));
                dsContactsInfo.setCallDurationTime(Integer.valueOf((int) duration));
            }
        }
    }
}
