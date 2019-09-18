package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Recommendation extends AManagedObject {
    public static final Parcelable.Creator<Recommendation> CREATOR = new Parcelable.Creator<Recommendation>() {
        public Recommendation createFromParcel(Parcel in) {
            return new Recommendation(in);
        }

        public Recommendation[] newArray(int size) {
            return new Recommendation[size];
        }
    };
    private Long businessId;
    private Long id;
    private Long itemId;
    private String message;
    private Long ruleId;
    private Date timeStamp;

    public Recommendation(Cursor cursor) {
        Date date = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.businessId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.itemId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.ruleId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.message = cursor.getString(5);
        this.timeStamp = !cursor.isNull(6) ? new Date(cursor.getLong(6)) : date;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Recommendation(Parcel in) {
        super(in);
        Date date = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.businessId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.itemId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.ruleId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.message = in.readByte() == 0 ? null : in.readString();
        this.timeStamp = in.readByte() != 0 ? new Date(in.readLong()) : date;
    }

    private Recommendation(Long id2, Long businessId2, Long itemId2, Long ruleId2, String message2, Date timeStamp2) {
        this.id = id2;
        this.businessId = businessId2;
        this.itemId = itemId2;
        this.ruleId = ruleId2;
        this.message = message2;
        this.timeStamp = timeStamp2;
    }

    public Recommendation() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public Long getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(Long businessId2) {
        this.businessId = businessId2;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId2) {
        this.itemId = itemId2;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long ruleId2) {
        this.ruleId = ruleId2;
        setValue();
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message2) {
        this.message = message2;
        setValue();
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date timeStamp2) {
        this.timeStamp = timeStamp2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.businessId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.businessId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.itemId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.ruleId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.message != null) {
            out.writeByte((byte) 1);
            out.writeString(this.message);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timeStamp.getTime());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Recommendation> getHelper() {
        return RecommendationHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Recommendation";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Recommendation { id: ").append(this.id);
        sb.append(", businessId: ").append(this.businessId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", ruleId: ").append(this.ruleId);
        sb.append(", message: ").append(this.message);
        sb.append(", timeStamp: ").append(this.timeStamp);
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
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
