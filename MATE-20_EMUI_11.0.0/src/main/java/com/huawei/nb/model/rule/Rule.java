package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Rule extends AManagedObject {
    public static final Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        /* class com.huawei.nb.model.rule.Rule.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Rule createFromParcel(Parcel parcel) {
            return new Rule(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Rule[] newArray(int i) {
            return new Rule[i];
        }
    };
    private Integer alwaysMatching;
    private Long businessId;
    private Date createTime;
    private Integer delayTimes;
    private Integer delayType;
    private Long id;
    private Date lastTriggerTime;
    private Integer lifecycleConditionGroupRelation;
    private Integer lifecycleState;
    private Integer matchConditionGroupRelation;
    private String name;
    private Date nextTriggerTime;
    private Integer priority;
    private Integer recommendCount;
    private Integer remainingDelayTimes;
    private String ruleVersion;
    private Integer silenceDays;
    private String systemVersion;
    private Integer triggerTimes;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsRule";
    }

    public String getDatabaseVersion() {
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.rule.Rule";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Rule(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.businessId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.ruleVersion = cursor.getString(4);
        this.systemVersion = cursor.getString(5);
        this.priority = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.silenceDays = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.delayTimes = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.delayType = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.alwaysMatching = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.matchConditionGroupRelation = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.lifecycleConditionGroupRelation = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.createTime = cursor.isNull(13) ? null : new Date(cursor.getLong(13));
        this.remainingDelayTimes = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.triggerTimes = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.recommendCount = cursor.isNull(16) ? null : Integer.valueOf(cursor.getInt(16));
        this.lastTriggerTime = cursor.isNull(17) ? null : new Date(cursor.getLong(17));
        this.nextTriggerTime = cursor.isNull(18) ? null : new Date(cursor.getLong(18));
        this.lifecycleState = !cursor.isNull(19) ? Integer.valueOf(cursor.getInt(19)) : num;
    }

    public Rule(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.businessId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.ruleVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.systemVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.priority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.silenceDays = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.delayTimes = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.delayType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.alwaysMatching = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.matchConditionGroupRelation = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.lifecycleConditionGroupRelation = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.createTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.remainingDelayTimes = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.triggerTimes = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.recommendCount = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.lastTriggerTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.nextTriggerTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.lifecycleState = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private Rule(Long l, String str, Long l2, String str2, String str3, Integer num, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, Integer num7, Date date, Integer num8, Integer num9, Integer num10, Date date2, Date date3, Integer num11) {
        this.id = l;
        this.name = str;
        this.businessId = l2;
        this.ruleVersion = str2;
        this.systemVersion = str3;
        this.priority = num;
        this.silenceDays = num2;
        this.delayTimes = num3;
        this.delayType = num4;
        this.alwaysMatching = num5;
        this.matchConditionGroupRelation = num6;
        this.lifecycleConditionGroupRelation = num7;
        this.createTime = date;
        this.remainingDelayTimes = num8;
        this.triggerTimes = num9;
        this.recommendCount = num10;
        this.lastTriggerTime = date2;
        this.nextTriggerTime = date3;
        this.lifecycleState = num11;
    }

    public Rule() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public Long getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(Long l) {
        this.businessId = l;
        setValue();
    }

    public String getRuleVersion() {
        return this.ruleVersion;
    }

    public void setRuleVersion(String str) {
        this.ruleVersion = str;
        setValue();
    }

    public String getSystemVersion() {
        return this.systemVersion;
    }

    public void setSystemVersion(String str) {
        this.systemVersion = str;
        setValue();
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer num) {
        this.priority = num;
        setValue();
    }

    public Integer getSilenceDays() {
        return this.silenceDays;
    }

    public void setSilenceDays(Integer num) {
        this.silenceDays = num;
        setValue();
    }

    public Integer getDelayTimes() {
        return this.delayTimes;
    }

    public void setDelayTimes(Integer num) {
        this.delayTimes = num;
        setValue();
    }

    public Integer getDelayType() {
        return this.delayType;
    }

    public void setDelayType(Integer num) {
        this.delayType = num;
        setValue();
    }

    public Integer getAlwaysMatching() {
        return this.alwaysMatching;
    }

    public void setAlwaysMatching(Integer num) {
        this.alwaysMatching = num;
        setValue();
    }

    public Integer getMatchConditionGroupRelation() {
        return this.matchConditionGroupRelation;
    }

    public void setMatchConditionGroupRelation(Integer num) {
        this.matchConditionGroupRelation = num;
        setValue();
    }

    public Integer getLifecycleConditionGroupRelation() {
        return this.lifecycleConditionGroupRelation;
    }

    public void setLifecycleConditionGroupRelation(Integer num) {
        this.lifecycleConditionGroupRelation = num;
        setValue();
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date date) {
        this.createTime = date;
        setValue();
    }

    public Integer getRemainingDelayTimes() {
        return this.remainingDelayTimes;
    }

    public void setRemainingDelayTimes(Integer num) {
        this.remainingDelayTimes = num;
        setValue();
    }

    public Integer getTriggerTimes() {
        return this.triggerTimes;
    }

    public void setTriggerTimes(Integer num) {
        this.triggerTimes = num;
        setValue();
    }

    public Integer getRecommendCount() {
        return this.recommendCount;
    }

    public void setRecommendCount(Integer num) {
        this.recommendCount = num;
        setValue();
    }

    public Date getLastTriggerTime() {
        return this.lastTriggerTime;
    }

    public void setLastTriggerTime(Date date) {
        this.lastTriggerTime = date;
        setValue();
    }

    public Date getNextTriggerTime() {
        return this.nextTriggerTime;
    }

    public void setNextTriggerTime(Date date) {
        this.nextTriggerTime = date;
        setValue();
    }

    public Integer getLifecycleState() {
        return this.lifecycleState;
    }

    public void setLifecycleState(Integer num) {
        this.lifecycleState = num;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.businessId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.businessId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ruleVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.ruleVersion);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.systemVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.systemVersion);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.priority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.priority.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.silenceDays != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.silenceDays.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.delayTimes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.delayTimes.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.delayType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.delayType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.alwaysMatching != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.alwaysMatching.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.matchConditionGroupRelation != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.matchConditionGroupRelation.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lifecycleConditionGroupRelation != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.lifecycleConditionGroupRelation.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.createTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.remainingDelayTimes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.remainingDelayTimes.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.triggerTimes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.triggerTimes.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.recommendCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.recommendCount.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lastTriggerTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.lastTriggerTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.nextTriggerTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.nextTriggerTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lifecycleState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.lifecycleState.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Rule> getHelper() {
        return RuleHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Rule { id: " + this.id + ", name: " + this.name + ", businessId: " + this.businessId + ", ruleVersion: " + this.ruleVersion + ", systemVersion: " + this.systemVersion + ", priority: " + this.priority + ", silenceDays: " + this.silenceDays + ", delayTimes: " + this.delayTimes + ", delayType: " + this.delayType + ", alwaysMatching: " + this.alwaysMatching + ", matchConditionGroupRelation: " + this.matchConditionGroupRelation + ", lifecycleConditionGroupRelation: " + this.lifecycleConditionGroupRelation + ", createTime: " + this.createTime + ", remainingDelayTimes: " + this.remainingDelayTimes + ", triggerTimes: " + this.triggerTimes + ", recommendCount: " + this.recommendCount + ", lastTriggerTime: " + this.lastTriggerTime + ", nextTriggerTime: " + this.nextTriggerTime + ", lifecycleState: " + this.lifecycleState + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
