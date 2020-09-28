package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

public class CellBroadcastMessage implements Parcelable {
    public static final Parcelable.Creator<CellBroadcastMessage> CREATOR = new Parcelable.Creator<CellBroadcastMessage>() {
        /* class android.telephony.CellBroadcastMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellBroadcastMessage createFromParcel(Parcel in) {
            return new CellBroadcastMessage(in);
        }

        @Override // android.os.Parcelable.Creator
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public static CellBroadcastMessage createFromCursor(Cursor cursor) {
        String plmn;
        int lac;
        int cid;
        SmsCbEtwsInfo etwsInfo;
        int lac2;
        int cidColumn;
        int cid2;
        int etwsWarningTypeColumn;
        int cmasMessageClassColumn;
        SmsCbCmasInfo cmasInfo;
        int cmasCategory;
        int responseType;
        int severity;
        int urgency;
        int certainty;
        int geoScope = cursor.getInt(cursor.getColumnIndexOrThrow("geo_scope"));
        int serialNum = cursor.getInt(cursor.getColumnIndexOrThrow("serial_number"));
        int category = cursor.getInt(cursor.getColumnIndexOrThrow("service_category"));
        String language = cursor.getString(cursor.getColumnIndexOrThrow("language"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int format = cursor.getInt(cursor.getColumnIndexOrThrow("format"));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
        int plmnColumn = cursor.getColumnIndex("plmn");
        if (plmnColumn == -1 || cursor.isNull(plmnColumn)) {
            plmn = null;
        } else {
            plmn = cursor.getString(plmnColumn);
        }
        int lacColumn = cursor.getColumnIndex("lac");
        if (lacColumn == -1 || cursor.isNull(lacColumn)) {
            lac = -1;
        } else {
            lac = cursor.getInt(lacColumn);
        }
        int cidColumn2 = cursor.getColumnIndex("cid");
        if (cidColumn2 == -1 || cursor.isNull(cidColumn2)) {
            cid = -1;
        } else {
            cid = cursor.getInt(cidColumn2);
        }
        SmsCbLocation location = new SmsCbLocation(plmn, lac, cid);
        int etwsWarningTypeColumn2 = cursor.getColumnIndex("etws_warning_type");
        if (etwsWarningTypeColumn2 == -1 || cursor.isNull(etwsWarningTypeColumn2)) {
            etwsInfo = null;
        } else {
            etwsInfo = new SmsCbEtwsInfo(cursor.getInt(etwsWarningTypeColumn2), false, false, false, (byte[]) null);
        }
        int cmasMessageClassColumn2 = cursor.getColumnIndex("cmas_message_class");
        if (cmasMessageClassColumn2 == -1 || cursor.isNull(cmasMessageClassColumn2)) {
            cmasMessageClassColumn = cmasMessageClassColumn2;
            etwsWarningTypeColumn = etwsWarningTypeColumn2;
            cid2 = cid;
            cidColumn = cidColumn2;
            lac2 = lac;
            cmasInfo = null;
        } else {
            int messageClass = cursor.getInt(cmasMessageClassColumn2);
            int cmasCategoryColumn = cursor.getColumnIndex("cmas_category");
            cmasMessageClassColumn = cmasMessageClassColumn2;
            if (cmasCategoryColumn == -1 || cursor.isNull(cmasCategoryColumn)) {
                cmasCategory = -1;
            } else {
                cmasCategory = cursor.getInt(cmasCategoryColumn);
            }
            int cmasResponseTypeColumn = cursor.getColumnIndex("cmas_response_type");
            etwsWarningTypeColumn = etwsWarningTypeColumn2;
            if (cmasResponseTypeColumn == -1 || cursor.isNull(cmasResponseTypeColumn)) {
                responseType = -1;
            } else {
                responseType = cursor.getInt(cmasResponseTypeColumn);
            }
            int cmasSeverityColumn = cursor.getColumnIndex("cmas_severity");
            cid2 = cid;
            if (cmasSeverityColumn == -1 || cursor.isNull(cmasSeverityColumn)) {
                severity = -1;
            } else {
                severity = cursor.getInt(cmasSeverityColumn);
            }
            int cmasUrgencyColumn = cursor.getColumnIndex("cmas_urgency");
            cidColumn = cidColumn2;
            if (cmasUrgencyColumn == -1 || cursor.isNull(cmasUrgencyColumn)) {
                urgency = -1;
            } else {
                urgency = cursor.getInt(cmasUrgencyColumn);
            }
            int cmasCertaintyColumn = cursor.getColumnIndex("cmas_certainty");
            lac2 = lac;
            if (cmasCertaintyColumn == -1 || cursor.isNull(cmasCertaintyColumn)) {
                certainty = -1;
            } else {
                certainty = cursor.getInt(cmasCertaintyColumn);
            }
            cmasInfo = new SmsCbCmasInfo(messageClass, cmasCategory, responseType, severity, urgency, certainty);
        }
        return new CellBroadcastMessage(new SmsCbMessage(format, geoScope, serialNum, location, category, language, body, priority, etwsInfo, cmasInfo), cursor.getLong(cursor.getColumnIndexOrThrow("date")), cursor.getInt(cursor.getColumnIndexOrThrow("read")) != 0);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public String getLanguageCode() {
        return this.mSmsCbMessage.getLanguageCode();
    }

    @UnsupportedAppUsage
    public int getServiceCategory() {
        return this.mSmsCbMessage.getServiceCategory();
    }

    @UnsupportedAppUsage
    public long getDeliveryTime() {
        return this.mDeliveryTime;
    }

    @UnsupportedAppUsage
    public String getMessageBody() {
        return this.mSmsCbMessage.getMessageBody();
    }

    @UnsupportedAppUsage
    public boolean isRead() {
        return this.mIsRead;
    }

    @UnsupportedAppUsage
    public int getSerialNumber() {
        return this.mSmsCbMessage.getSerialNumber();
    }

    public SmsCbCmasInfo getCmasWarningInfo() {
        return this.mSmsCbMessage.getCmasWarningInfo();
    }

    @UnsupportedAppUsage
    public SmsCbEtwsInfo getEtwsWarningInfo() {
        return this.mSmsCbMessage.getEtwsWarningInfo();
    }

    public boolean isPublicAlertMessage() {
        return this.mSmsCbMessage.isEmergencyMessage();
    }

    @UnsupportedAppUsage
    public boolean isEmergencyAlertMessage() {
        return this.mSmsCbMessage.isEmergencyMessage();
    }

    @UnsupportedAppUsage
    public boolean isEtwsMessage() {
        return this.mSmsCbMessage.isEtwsMessage();
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public String getSpokenDateString(Context context) {
        return DateUtils.formatDateTime(context, this.mDeliveryTime, 17);
    }
}
