package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Rule extends AManagedObject {
    public static final Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        public Rule createFromParcel(Parcel in) {
            return new Rule(in);
        }

        public Rule[] newArray(int size) {
            return new Rule[size];
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

    public Rule(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Rule(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.businessId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.ruleVersion = in.readByte() == 0 ? null : in.readString();
        this.systemVersion = in.readByte() == 0 ? null : in.readString();
        this.priority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.silenceDays = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.delayTimes = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.delayType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.alwaysMatching = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.matchConditionGroupRelation = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.lifecycleConditionGroupRelation = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.createTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.remainingDelayTimes = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.triggerTimes = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.recommendCount = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.lastTriggerTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.nextTriggerTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.lifecycleState = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private Rule(Long id2, String name2, Long businessId2, String ruleVersion2, String systemVersion2, Integer priority2, Integer silenceDays2, Integer delayTimes2, Integer delayType2, Integer alwaysMatching2, Integer matchConditionGroupRelation2, Integer lifecycleConditionGroupRelation2, Date createTime2, Integer remainingDelayTimes2, Integer triggerTimes2, Integer recommendCount2, Date lastTriggerTime2, Date nextTriggerTime2, Integer lifecycleState2) {
        this.id = id2;
        this.name = name2;
        this.businessId = businessId2;
        this.ruleVersion = ruleVersion2;
        this.systemVersion = systemVersion2;
        this.priority = priority2;
        this.silenceDays = silenceDays2;
        this.delayTimes = delayTimes2;
        this.delayType = delayType2;
        this.alwaysMatching = alwaysMatching2;
        this.matchConditionGroupRelation = matchConditionGroupRelation2;
        this.lifecycleConditionGroupRelation = lifecycleConditionGroupRelation2;
        this.createTime = createTime2;
        this.remainingDelayTimes = remainingDelayTimes2;
        this.triggerTimes = triggerTimes2;
        this.recommendCount = recommendCount2;
        this.lastTriggerTime = lastTriggerTime2;
        this.nextTriggerTime = nextTriggerTime2;
        this.lifecycleState = lifecycleState2;
    }

    public Rule() {
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

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public Long getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(Long businessId2) {
        this.businessId = businessId2;
        setValue();
    }

    public String getRuleVersion() {
        return this.ruleVersion;
    }

    public void setRuleVersion(String ruleVersion2) {
        this.ruleVersion = ruleVersion2;
        setValue();
    }

    public String getSystemVersion() {
        return this.systemVersion;
    }

    public void setSystemVersion(String systemVersion2) {
        this.systemVersion = systemVersion2;
        setValue();
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority2) {
        this.priority = priority2;
        setValue();
    }

    public Integer getSilenceDays() {
        return this.silenceDays;
    }

    public void setSilenceDays(Integer silenceDays2) {
        this.silenceDays = silenceDays2;
        setValue();
    }

    public Integer getDelayTimes() {
        return this.delayTimes;
    }

    public void setDelayTimes(Integer delayTimes2) {
        this.delayTimes = delayTimes2;
        setValue();
    }

    public Integer getDelayType() {
        return this.delayType;
    }

    public void setDelayType(Integer delayType2) {
        this.delayType = delayType2;
        setValue();
    }

    public Integer getAlwaysMatching() {
        return this.alwaysMatching;
    }

    public void setAlwaysMatching(Integer alwaysMatching2) {
        this.alwaysMatching = alwaysMatching2;
        setValue();
    }

    public Integer getMatchConditionGroupRelation() {
        return this.matchConditionGroupRelation;
    }

    public void setMatchConditionGroupRelation(Integer matchConditionGroupRelation2) {
        this.matchConditionGroupRelation = matchConditionGroupRelation2;
        setValue();
    }

    public Integer getLifecycleConditionGroupRelation() {
        return this.lifecycleConditionGroupRelation;
    }

    public void setLifecycleConditionGroupRelation(Integer lifecycleConditionGroupRelation2) {
        this.lifecycleConditionGroupRelation = lifecycleConditionGroupRelation2;
        setValue();
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime2) {
        this.createTime = createTime2;
        setValue();
    }

    public Integer getRemainingDelayTimes() {
        return this.remainingDelayTimes;
    }

    public void setRemainingDelayTimes(Integer remainingDelayTimes2) {
        this.remainingDelayTimes = remainingDelayTimes2;
        setValue();
    }

    public Integer getTriggerTimes() {
        return this.triggerTimes;
    }

    public void setTriggerTimes(Integer triggerTimes2) {
        this.triggerTimes = triggerTimes2;
        setValue();
    }

    public Integer getRecommendCount() {
        return this.recommendCount;
    }

    public void setRecommendCount(Integer recommendCount2) {
        this.recommendCount = recommendCount2;
        setValue();
    }

    public Date getLastTriggerTime() {
        return this.lastTriggerTime;
    }

    public void setLastTriggerTime(Date lastTriggerTime2) {
        this.lastTriggerTime = lastTriggerTime2;
        setValue();
    }

    public Date getNextTriggerTime() {
        return this.nextTriggerTime;
    }

    public void setNextTriggerTime(Date nextTriggerTime2) {
        this.nextTriggerTime = nextTriggerTime2;
        setValue();
    }

    public Integer getLifecycleState() {
        return this.lifecycleState;
    }

    public void setLifecycleState(Integer lifecycleState2) {
        this.lifecycleState = lifecycleState2;
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
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.businessId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.businessId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ruleVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.ruleVersion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.systemVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.systemVersion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.priority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.priority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.silenceDays != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.silenceDays.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.delayTimes != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.delayTimes.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.delayType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.delayType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.alwaysMatching != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.alwaysMatching.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.matchConditionGroupRelation != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.matchConditionGroupRelation.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lifecycleConditionGroupRelation != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.lifecycleConditionGroupRelation.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.createTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.remainingDelayTimes != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.remainingDelayTimes.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.triggerTimes != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.triggerTimes.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.recommendCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.recommendCount.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lastTriggerTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.lastTriggerTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.nextTriggerTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.nextTriggerTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lifecycleState != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.lifecycleState.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Rule> getHelper() {
        return RuleHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Rule";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Rule { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", businessId: ").append(this.businessId);
        sb.append(", ruleVersion: ").append(this.ruleVersion);
        sb.append(", systemVersion: ").append(this.systemVersion);
        sb.append(", priority: ").append(this.priority);
        sb.append(", silenceDays: ").append(this.silenceDays);
        sb.append(", delayTimes: ").append(this.delayTimes);
        sb.append(", delayType: ").append(this.delayType);
        sb.append(", alwaysMatching: ").append(this.alwaysMatching);
        sb.append(", matchConditionGroupRelation: ").append(this.matchConditionGroupRelation);
        sb.append(", lifecycleConditionGroupRelation: ").append(this.lifecycleConditionGroupRelation);
        sb.append(", createTime: ").append(this.createTime);
        sb.append(", remainingDelayTimes: ").append(this.remainingDelayTimes);
        sb.append(", triggerTimes: ").append(this.triggerTimes);
        sb.append(", recommendCount: ").append(this.recommendCount);
        sb.append(", lastTriggerTime: ").append(this.lastTriggerTime);
        sb.append(", nextTriggerTime: ").append(this.nextTriggerTime);
        sb.append(", lifecycleState: ").append(this.lifecycleState);
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
