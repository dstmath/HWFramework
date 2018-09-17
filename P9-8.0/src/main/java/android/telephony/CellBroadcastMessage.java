package android.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateUtils;

public class CellBroadcastMessage implements Parcelable {
    public static final Creator<CellBroadcastMessage> CREATOR = new Creator<CellBroadcastMessage>() {
        public CellBroadcastMessage createFromParcel(Parcel in) {
            return new CellBroadcastMessage(in, null);
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

    /* synthetic */ CellBroadcastMessage(Parcel in, CellBroadcastMessage -this1) {
        this(in);
    }

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
        if (in.readInt() != 0) {
            z = true;
        }
        this.mIsRead = z;
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
        SmsCbCmasInfo cmasInfo;
        int geoScope = cursor.getInt(cursor.getColumnIndexOrThrow("geo_scope"));
        int serialNum = cursor.getInt(cursor.getColumnIndexOrThrow("serial_number"));
        int category = cursor.getInt(cursor.getColumnIndexOrThrow("service_category"));
        String language = cursor.getString(cursor.getColumnIndexOrThrow("language"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int format = cursor.getInt(cursor.getColumnIndexOrThrow("format"));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
        int plmnColumn = cursor.getColumnIndex("plmn");
        if (plmnColumn == -1 || (cursor.isNull(plmnColumn) ^ 1) == 0) {
            plmn = null;
        } else {
            plmn = cursor.getString(plmnColumn);
        }
        int lacColumn = cursor.getColumnIndex("lac");
        if (lacColumn == -1 || (cursor.isNull(lacColumn) ^ 1) == 0) {
            lac = -1;
        } else {
            lac = cursor.getInt(lacColumn);
        }
        int cidColumn = cursor.getColumnIndex("cid");
        if (cidColumn == -1 || (cursor.isNull(cidColumn) ^ 1) == 0) {
            cid = -1;
        } else {
            cid = cursor.getInt(cidColumn);
        }
        SmsCbLocation smsCbLocation = new SmsCbLocation(plmn, lac, cid);
        int etwsWarningTypeColumn = cursor.getColumnIndex("etws_warning_type");
        if (etwsWarningTypeColumn == -1 || (cursor.isNull(etwsWarningTypeColumn) ^ 1) == 0) {
            etwsInfo = null;
        } else {
            etwsInfo = new SmsCbEtwsInfo(cursor.getInt(etwsWarningTypeColumn), false, false, false, null);
        }
        int cmasMessageClassColumn = cursor.getColumnIndex("cmas_message_class");
        if (cmasMessageClassColumn == -1 || (cursor.isNull(cmasMessageClassColumn) ^ 1) == 0) {
            cmasInfo = null;
        } else {
            int cmasCategory;
            int responseType;
            int severity;
            int urgency;
            int certainty;
            int messageClass = cursor.getInt(cmasMessageClassColumn);
            int cmasCategoryColumn = cursor.getColumnIndex("cmas_category");
            if (cmasCategoryColumn == -1 || (cursor.isNull(cmasCategoryColumn) ^ 1) == 0) {
                cmasCategory = -1;
            } else {
                cmasCategory = cursor.getInt(cmasCategoryColumn);
            }
            int cmasResponseTypeColumn = cursor.getColumnIndex("cmas_response_type");
            if (cmasResponseTypeColumn == -1 || (cursor.isNull(cmasResponseTypeColumn) ^ 1) == 0) {
                responseType = -1;
            } else {
                responseType = cursor.getInt(cmasResponseTypeColumn);
            }
            int cmasSeverityColumn = cursor.getColumnIndex("cmas_severity");
            if (cmasSeverityColumn == -1 || (cursor.isNull(cmasSeverityColumn) ^ 1) == 0) {
                severity = -1;
            } else {
                severity = cursor.getInt(cmasSeverityColumn);
            }
            int cmasUrgencyColumn = cursor.getColumnIndex("cmas_urgency");
            if (cmasUrgencyColumn == -1 || (cursor.isNull(cmasUrgencyColumn) ^ 1) == 0) {
                urgency = -1;
            } else {
                urgency = cursor.getInt(cmasUrgencyColumn);
            }
            int cmasCertaintyColumn = cursor.getColumnIndex("cmas_certainty");
            if (cmasCertaintyColumn == -1 || (cursor.isNull(cmasCertaintyColumn) ^ 1) == 0) {
                certainty = -1;
            } else {
                certainty = cursor.getInt(cmasCertaintyColumn);
            }
            cmasInfo = new SmsCbCmasInfo(messageClass, cmasCategory, responseType, severity, urgency, certainty);
        }
        return new CellBroadcastMessage(new SmsCbMessage(format, geoScope, serialNum, smsCbLocation, category, language, body, priority, etwsInfo, cmasInfo), cursor.getLong(cursor.getColumnIndexOrThrow("date")), cursor.getInt(cursor.getColumnIndexOrThrow("read")) != 0);
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
        return etwsInfo != null ? etwsInfo.isPopupAlert() : false;
    }

    public boolean isEtwsEmergencyUserAlert() {
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        return etwsInfo != null ? etwsInfo.isEmergencyUserAlert() : false;
    }

    public boolean isEtwsTestMessage() {
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        if (etwsInfo == null || etwsInfo.getWarningType() != 3) {
            return false;
        }
        return true;
    }

    public String getDateString(Context context) {
        return DateUtils.formatDateTime(context, this.mDeliveryTime, 527121);
    }

    public String getSpokenDateString(Context context) {
        return DateUtils.formatDateTime(context, this.mDeliveryTime, 17);
    }
}
