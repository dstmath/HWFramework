package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleMarkPoint extends AManagedObject {
    public static final Parcelable.Creator<RuleMarkPoint> CREATOR = new Parcelable.Creator<RuleMarkPoint>() {
        public RuleMarkPoint createFromParcel(Parcel in) {
            return new RuleMarkPoint(in);
        }

        public RuleMarkPoint[] newArray(int size) {
            return new RuleMarkPoint[size];
        }
    };
    private String businessName;
    private Integer category;
    private Long id;
    private String itemName;
    private String operatorName;
    private Integer recommendedCount;
    private String ruleName;
    private Date timeStamp;

    public RuleMarkPoint(Cursor cursor) {
        Date date = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.ruleName = cursor.getString(2);
        this.businessName = cursor.getString(3);
        this.operatorName = cursor.getString(4);
        this.itemName = cursor.getString(5);
        this.recommendedCount = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.category = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.timeStamp = !cursor.isNull(8) ? new Date(cursor.getLong(8)) : date;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RuleMarkPoint(Parcel in) {
        super(in);
        Date date = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.ruleName = in.readByte() == 0 ? null : in.readString();
        this.businessName = in.readByte() == 0 ? null : in.readString();
        this.operatorName = in.readByte() == 0 ? null : in.readString();
        this.itemName = in.readByte() == 0 ? null : in.readString();
        this.recommendedCount = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.category = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.timeStamp = in.readByte() != 0 ? new Date(in.readLong()) : date;
    }

    private RuleMarkPoint(Long id2, String ruleName2, String businessName2, String operatorName2, String itemName2, Integer recommendedCount2, Integer category2, Date timeStamp2) {
        this.id = id2;
        this.ruleName = ruleName2;
        this.businessName = businessName2;
        this.operatorName = operatorName2;
        this.itemName = itemName2;
        this.recommendedCount = recommendedCount2;
        this.category = category2;
        this.timeStamp = timeStamp2;
    }

    public RuleMarkPoint() {
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

    public String getRuleName() {
        return this.ruleName;
    }

    public void setRuleName(String ruleName2) {
        this.ruleName = ruleName2;
        setValue();
    }

    public String getBusinessName() {
        return this.businessName;
    }

    public void setBusinessName(String businessName2) {
        this.businessName = businessName2;
        setValue();
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName2) {
        this.operatorName = operatorName2;
        setValue();
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName2) {
        this.itemName = itemName2;
        setValue();
    }

    public Integer getRecommendedCount() {
        return this.recommendedCount;
    }

    public void setRecommendedCount(Integer recommendedCount2) {
        this.recommendedCount = recommendedCount2;
        setValue();
    }

    public Integer getCategory() {
        return this.category;
    }

    public void setCategory(Integer category2) {
        this.category = category2;
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
        if (this.ruleName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.ruleName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.businessName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.businessName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.operatorName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.operatorName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.itemName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.itemName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.recommendedCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.recommendedCount.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.category != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.category.intValue());
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

    public AEntityHelper<RuleMarkPoint> getHelper() {
        return RuleMarkPointHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.RuleMarkPoint";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RuleMarkPoint { id: ").append(this.id);
        sb.append(", ruleName: ").append(this.ruleName);
        sb.append(", businessName: ").append(this.businessName);
        sb.append(", operatorName: ").append(this.operatorName);
        sb.append(", itemName: ").append(this.itemName);
        sb.append(", recommendedCount: ").append(this.recommendedCount);
        sb.append(", category: ").append(this.category);
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
