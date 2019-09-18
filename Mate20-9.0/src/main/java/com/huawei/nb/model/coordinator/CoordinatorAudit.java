package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorAudit extends AManagedObject {
    public static final Parcelable.Creator<CoordinatorAudit> CREATOR = new Parcelable.Creator<CoordinatorAudit>() {
        public CoordinatorAudit createFromParcel(Parcel in) {
            return new CoordinatorAudit(in);
        }

        public CoordinatorAudit[] newArray(int size) {
            return new CoordinatorAudit[size];
        }
    };
    private String appPackageName;
    private Long dataSize;
    private Integer id;
    private Long isNeedRetry;
    private boolean isRequestSuccess;
    private String netWorkState;
    private String requestDate;
    private Long successTransferTime;
    private Long successVerifyTime;
    private Long timeStamp;
    private String url;

    public CoordinatorAudit(Cursor cursor) {
        boolean z;
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.appPackageName = cursor.getString(2);
        this.url = cursor.getString(3);
        this.netWorkState = cursor.getString(4);
        this.timeStamp = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.isNeedRetry = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.successVerifyTime = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.successTransferTime = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.dataSize = !cursor.isNull(9) ? Long.valueOf(cursor.getLong(9)) : l;
        this.requestDate = cursor.getString(10);
        if (cursor.getInt(11) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isRequestSuccess = z;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CoordinatorAudit(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.appPackageName = in.readByte() == 0 ? null : in.readString();
        this.url = in.readByte() == 0 ? null : in.readString();
        this.netWorkState = in.readByte() == 0 ? null : in.readString();
        this.timeStamp = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.isNeedRetry = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.successVerifyTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.successTransferTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.dataSize = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.requestDate = in.readByte() != 0 ? in.readString() : str;
        this.isRequestSuccess = in.readByte() != 0;
    }

    private CoordinatorAudit(Integer id2, String appPackageName2, String url2, String netWorkState2, Long timeStamp2, Long isNeedRetry2, Long successVerifyTime2, Long successTransferTime2, Long dataSize2, String requestDate2, boolean isRequestSuccess2) {
        this.id = id2;
        this.appPackageName = appPackageName2;
        this.url = url2;
        this.netWorkState = netWorkState2;
        this.timeStamp = timeStamp2;
        this.isNeedRetry = isNeedRetry2;
        this.successVerifyTime = successVerifyTime2;
        this.successTransferTime = successTransferTime2;
        this.dataSize = dataSize2;
        this.requestDate = requestDate2;
        this.isRequestSuccess = isRequestSuccess2;
    }

    public CoordinatorAudit() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getAppPackageName() {
        return this.appPackageName;
    }

    public void setAppPackageName(String appPackageName2) {
        this.appPackageName = appPackageName2;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
        setValue();
    }

    public String getNetWorkState() {
        return this.netWorkState;
    }

    public void setNetWorkState(String netWorkState2) {
        this.netWorkState = netWorkState2;
        setValue();
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long timeStamp2) {
        this.timeStamp = timeStamp2;
        setValue();
    }

    public Long getIsNeedRetry() {
        return this.isNeedRetry;
    }

    public void setIsNeedRetry(Long isNeedRetry2) {
        this.isNeedRetry = isNeedRetry2;
        setValue();
    }

    public Long getSuccessVerifyTime() {
        return this.successVerifyTime;
    }

    public void setSuccessVerifyTime(Long successVerifyTime2) {
        this.successVerifyTime = successVerifyTime2;
        setValue();
    }

    public Long getSuccessTransferTime() {
        return this.successTransferTime;
    }

    public void setSuccessTransferTime(Long successTransferTime2) {
        this.successTransferTime = successTransferTime2;
        setValue();
    }

    public Long getDataSize() {
        return this.dataSize;
    }

    public void setDataSize(Long dataSize2) {
        this.dataSize = dataSize2;
        setValue();
    }

    public String getRequestDate() {
        return this.requestDate;
    }

    public void setRequestDate(String requestDate2) {
        this.requestDate = requestDate2;
        setValue();
    }

    public boolean getIsRequestSuccess() {
        return this.isRequestSuccess;
    }

    public void setIsRequestSuccess(boolean isRequestSuccess2) {
        this.isRequestSuccess = isRequestSuccess2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b = 1;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.appPackageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.appPackageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.url != null) {
            out.writeByte((byte) 1);
            out.writeString(this.url);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.netWorkState != null) {
            out.writeByte((byte) 1);
            out.writeString(this.netWorkState);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timeStamp.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isNeedRetry != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.isNeedRetry.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.successVerifyTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.successVerifyTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.successTransferTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.successTransferTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dataSize != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.dataSize.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.requestDate != null) {
            out.writeByte((byte) 1);
            out.writeString(this.requestDate);
        } else {
            out.writeByte((byte) 0);
        }
        if (!this.isRequestSuccess) {
            b = 0;
        }
        out.writeByte(b);
    }

    public AEntityHelper<CoordinatorAudit> getHelper() {
        return CoordinatorAuditHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.CoordinatorAudit";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CoordinatorAudit { id: ").append(this.id);
        sb.append(", appPackageName: ").append(this.appPackageName);
        sb.append(", url: ").append(this.url);
        sb.append(", netWorkState: ").append(this.netWorkState);
        sb.append(", timeStamp: ").append(this.timeStamp);
        sb.append(", isNeedRetry: ").append(this.isNeedRetry);
        sb.append(", successVerifyTime: ").append(this.successVerifyTime);
        sb.append(", successTransferTime: ").append(this.successTransferTime);
        sb.append(", dataSize: ").append(this.dataSize);
        sb.append(", requestDate: ").append(this.requestDate);
        sb.append(", isRequestSuccess: ").append(this.isRequestSuccess);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
