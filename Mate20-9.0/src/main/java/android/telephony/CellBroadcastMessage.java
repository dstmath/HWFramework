package android.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

public class CellBroadcastMessage implements Parcelable {
    public static final Parcelable.Creator<CellBroadcastMessage> CREATOR = new Parcelable.Creator<CellBroadcastMessage>() {
        public CellBroadcastMessage createFromParcel(Parcel in) {
            return new CellBroadcastMessage(in);
        }

        public CellBroadcastMessage[] newArray(int size) {
            return new CellBroadcastMessage[size];
        }
    };
    public static final String SMS_CB_MESSAGE_EXTRA = "com.android.cellbroadcastreceiver.SMS_CB_MESSAGE";
    private final long mDeliveryTime;
    private boolean mIsRead;
    private final SmsCbMessage mSmsCbMessage;
    private int mSubId;

    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public CellBroadcastMessage(SmsCbMessage message) {
        this.mSubId = 0;
        this.mSmsCbMessage = message;
        this.mDeliveryTime = System.currentTimeMillis();
        this.mIsRead = false;
    }

    private CellBroadcastMessage(SmsCbMessage message, long deliveryTime, boolean isRead) {
        this.mSubId = 0;
        this.mSmsCbMessage = message;
        this.mDeliveryTime = deliveryTime;
        this.mIsRead = isRead;
    }

    private CellBroadcastMessage(Parcel in) {
        boolean z = false;
        this.mSubId = 0;
        this.mSmsCbMessage = new SmsCbMessage(in);
        this.mDeliveryTime = in.readLong();
        this.mIsRead = in.readInt() != 0 ? true : z;
        this.mSubId = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mSmsCbMessage.writeToParcel(out, flags);
        out.writeLong(this.mDeliveryTime);
        out.writeInt(this.mIsRead ? 1 : 0);
        out.writeInt(this.mSubId);
    }

    public static CellBroadcastMessage createFromCursor(Cursor cursor) {
        String plmn;
        int lac;
        int cid;
        SmsCbEtwsInfo etwsInfo;
        int cmasMessageClassColumn;
        SmsCbCmasInfo cmasInfo;
        int cmasCategory;
        int responseType;
        int severity;
        int urgency;
        Cursor cursor2 = cursor;
        int geoScope = cursor2.getInt(cursor2.getColumnIndexOrThrow("geo_scope"));
        int serialNum = cursor2.getInt(cursor2.getColumnIndexOrThrow("serial_number"));
        int category = cursor2.getInt(cursor2.getColumnIndexOrThrow("service_category"));
        String language = cursor2.getString(cursor2.getColumnIndexOrThrow("language"));
        String body = cursor2.getString(cursor2.getColumnIndexOrThrow("body"));
        int format = cursor2.getInt(cursor2.getColumnIndexOrThrow("format"));
        int priority = cursor2.getInt(cursor2.getColumnIndexOrThrow("priority"));
        int plmnColumn = cursor2.getColumnIndex("plmn");
        if (plmnColumn == -1 || cursor2.isNull(plmnColumn)) {
            plmn = null;
        } else {
            plmn = cursor2.getString(plmnColumn);
        }
        String plmn2 = plmn;
        int lacColumn = cursor2.getColumnIndex("lac");
        if (lacColumn == -1 || cursor2.isNull(lacColumn)) {
            lac = -1;
        } else {
            lac = cursor2.getInt(lacColumn);
        }
        int lac2 = lac;
        int cidColumn = cursor2.getColumnIndex("cid");
        if (cidColumn == -1 || cursor2.isNull(cidColumn)) {
            cid = -1;
        } else {
            cid = cursor2.getInt(cidColumn);
        }
        SmsCbLocation location = new SmsCbLocation(plmn2, lac2, cid);
        int etwsWarningTypeColumn = cursor2.getColumnIndex("etws_warning_type");
        if (etwsWarningTypeColumn == -1 || cursor2.isNull(etwsWarningTypeColumn)) {
            etwsInfo = null;
        } else {
            SmsCbEtwsInfo smsCbEtwsInfo = new SmsCbEtwsInfo(cursor2.getInt(etwsWarningTypeColumn), false, false, false, null);
            etwsInfo = smsCbEtwsInfo;
        }
        int cmasMessageClassColumn2 = cursor2.getColumnIndex("cmas_message_class");
        if (cmasMessageClassColumn2 == -1 || cursor2.isNull(cmasMessageClassColumn2)) {
            cmasMessageClassColumn = cmasMessageClassColumn2;
            cmasInfo = null;
        } else {
            int messageClass = cursor2.getInt(cmasMessageClassColumn2);
            int cmasCategoryColumn = cursor2.getColumnIndex("cmas_category");
            cmasMessageClassColumn = cmasMessageClassColumn2;
            if (cmasCategoryColumn == -1 || cursor2.isNull(cmasCategoryColumn) != 0) {
                cmasCategory = -1;
            } else {
                cmasCategory = cursor2.getInt(cmasCategoryColumn);
            }
            int cmasResponseTypeColumn = cursor2.getColumnIndex("cmas_response_type");
            int i = cmasCategoryColumn;
            if (cmasResponseTypeColumn == -1 || cursor2.isNull(cmasResponseTypeColumn) != 0) {
                responseType = -1;
            } else {
                responseType = cursor2.getInt(cmasResponseTypeColumn);
            }
            int cmasSeverityColumn = cursor2.getColumnIndex("cmas_severity");
            int i2 = cmasResponseTypeColumn;
            if (cmasSeverityColumn == -1 || cursor2.isNull(cmasSeverityColumn) != 0) {
                severity = -1;
            } else {
                severity = cursor2.getInt(cmasSeverityColumn);
            }
            int cmasUrgencyColumn = cursor2.getColumnIndex("cmas_urgency");
            int i3 = cmasSeverityColumn;
            if (cmasUrgencyColumn == -1 || cursor2.isNull(cmasUrgencyColumn) != 0) {
                urgency = -1;
            } else {
                urgency = cursor2.getInt(cmasUrgencyColumn);
            }
            int cmasCertaintyColumn = cursor2.getColumnIndex("cmas_certainty");
            int i4 = cmasUrgencyColumn;
            int cmasUrgencyColumn2 = -1;
            if (cmasCertaintyColumn != -1 && !cursor2.isNull(cmasCertaintyColumn)) {
                cmasUrgencyColumn2 = cursor2.getInt(cmasCertaintyColumn);
            }
            cmasInfo = new SmsCbCmasInfo(messageClass, cmasCategory, responseType, severity, urgency, cmasUrgencyColumn2);
        }
        int i5 = etwsWarningTypeColumn;
        int i6 = cmasMessageClassColumn;
        int i7 = cid;
        int i8 = cidColumn;
        int i9 = lac2;
        int i10 = lacColumn;
        String str = plmn2;
        int i11 = plmnColumn;
        SmsCbMessage msg = new SmsCbMessage(format, geoScope, serialNum, location, category, language, body, priority, etwsInfo, cmasInfo);
        return new CellBroadcastMessage(msg, cursor2.getLong(cursor2.getColumnIndexOrThrow("date")), cursor2.getInt(cursor2.getColumnIndexOrThrow("read")) != 0);
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(16);
        SmsCbMessage msg = this.mSmsCbMessage;
        cv.put("geo_scope", Integer.valueOf(msg.getGeographicalScope()));
        SmsCbLocation location = msg.getLocation();
        if (location.getPlmn() != null) {
            cv.put("plmn", location.getPlmn());
        }
        if (location.getLac() != -1) {
            cv.put("lac", Integer.valueOf(location.getLac()));
        }
        if (location.getCid() != -1) {
            cv.put("cid", Integer.valueOf(location.getCid()));
        }
        cv.put("serial_number", Integer.valueOf(msg.getSerialNumber()));
        cv.put("service_category", Integer.valueOf(msg.getServiceCategory()));
        cv.put("language", msg.getLanguageCode());
        cv.put("body", msg.getMessageBody());
        cv.put("date", Long.valueOf(this.mDeliveryTime));
        cv.put("read", Boolean.valueOf(this.mIsRead));
        cv.put("format", Integer.valueOf(msg.getMessageFormat()));
        cv.put("priority", Integer.valueOf(msg.getMessagePriority()));
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        if (etwsInfo != null) {
            cv.put("etws_warning_type", Integer.valueOf(etwsInfo.getWarningType()));
        }
        SmsCbCmasInfo cmasInfo = this.mSmsCbMessage.getCmasWarningInfo();
        if (cmasInfo != null) {
            cv.put("cmas_message_class", Integer.valueOf(cmasInfo.getMessageClass()));
            cv.put("cmas_category", Integer.valueOf(cmasInfo.getCategory()));
            cv.put("cmas_response_type", Integer.valueOf(cmasInfo.getResponseType()));
            cv.put("cmas_severity", Integer.valueOf(cmasInfo.getSeverity()));
            cv.put("cmas_urgency", Integer.valueOf(cmasInfo.getUrgency()));
            cv.put("cmas_certainty", Integer.valueOf(cmasInfo.getCertainty()));
        }
        return cv;
    }

    public void setIsRead(boolean isRead) {
        this.mIsRead = isRead;
    }

    public String getLanguageCode() {
        return this.mSmsCbMessage.getLanguageCode();
    }

    public int getServiceCategory() {
        return this.mSmsCbMessage.getServiceCategory();
    }

    public long getDeliveryTime() {
        return this.mDeliveryTime;
    }

    public String getMessageBody() {
        return this.mSmsCbMessage.getMessageBody();
    }

    public boolean isRead() {
        return this.mIsRead;
    }

    public int getSerialNumber() {
        return this.mSmsCbMessage.getSerialNumber();
    }

    public SmsCbCmasInfo getCmasWarningInfo() {
        return this.mSmsCbMessage.getCmasWarningInfo();
    }

    public SmsCbEtwsInfo getEtwsWarningInfo() {
        return this.mSmsCbMessage.getEtwsWarningInfo();
    }

    public boolean isPublicAlertMessage() {
        return this.mSmsCbMessage.isEmergencyMessage();
    }

    public boolean isEmergencyAlertMessage() {
        return this.mSmsCbMessage.isEmergencyMessage();
    }

    public boolean isEtwsMessage() {
        return this.mSmsCbMessage.isEtwsMessage();
    }

    public boolean isCmasMessage() {
        return this.mSmsCbMessage.isCmasMessage();
    }

    public int getCmasMessageClass() {
        if (this.mSmsCbMessage.isCmasMessage()) {
            return this.mSmsCbMessage.getCmasWarningInfo().getMessageClass();
        }
        return -1;
    }

    public boolean isEtwsPopupAlert() {
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        return etwsInfo != null && etwsInfo.isPopupAlert();
    }

    public boolean isEtwsEmergencyUserAlert() {
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        return etwsInfo != null && etwsInfo.isEmergencyUserAlert();
    }

    public boolean isEtwsTestMessage() {
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        return etwsInfo != null && etwsInfo.getWarningType() == 3;
    }

    public String getDateString(Context context) {
        return DateUtils.formatDateTime(context, this.mDeliveryTime, 527121);
    }

    public String getSpokenDateString(Context context) {
        return DateUtils.formatDateTime(context, this.mDeliveryTime, 17);
    }
}
