package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleHelper extends AEntityHelper<Rule> {
    private static final RuleHelper INSTANCE = new RuleHelper();

    private RuleHelper() {
    }

    public static RuleHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Rule object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        Long businessId = object.getBusinessId();
        if (businessId != null) {
            statement.bindLong(3, businessId.longValue());
        } else {
            statement.bindNull(3);
        }
        String ruleVersion = object.getRuleVersion();
        if (ruleVersion != null) {
            statement.bindString(4, ruleVersion);
        } else {
            statement.bindNull(4);
        }
        String systemVersion = object.getSystemVersion();
        if (systemVersion != null) {
            statement.bindString(5, systemVersion);
        } else {
            statement.bindNull(5);
        }
        Integer priority = object.getPriority();
        if (priority != null) {
            statement.bindLong(6, (long) priority.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer silenceDays = object.getSilenceDays();
        if (silenceDays != null) {
            statement.bindLong(7, (long) silenceDays.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer delayTimes = object.getDelayTimes();
        if (delayTimes != null) {
            statement.bindLong(8, (long) delayTimes.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer delayType = object.getDelayType();
        if (delayType != null) {
            statement.bindLong(9, (long) delayType.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer alwaysMatching = object.getAlwaysMatching();
        if (alwaysMatching != null) {
            statement.bindLong(10, (long) alwaysMatching.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer matchConditionGroupRelation = object.getMatchConditionGroupRelation();
        if (matchConditionGroupRelation != null) {
            statement.bindLong(11, (long) matchConditionGroupRelation.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer lifecycleConditionGroupRelation = object.getLifecycleConditionGroupRelation();
        if (lifecycleConditionGroupRelation != null) {
            statement.bindLong(12, (long) lifecycleConditionGroupRelation.intValue());
        } else {
            statement.bindNull(12);
        }
        Date createTime = object.getCreateTime();
        if (createTime != null) {
            statement.bindLong(13, createTime.getTime());
        } else {
            statement.bindNull(13);
        }
        Integer remainingDelayTimes = object.getRemainingDelayTimes();
        if (remainingDelayTimes != null) {
            statement.bindLong(14, (long) remainingDelayTimes.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer triggerTimes = object.getTriggerTimes();
        if (triggerTimes != null) {
            statement.bindLong(15, (long) triggerTimes.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer recommendCount = object.getRecommendCount();
        if (recommendCount != null) {
            statement.bindLong(16, (long) recommendCount.intValue());
        } else {
            statement.bindNull(16);
        }
        Date lastTriggerTime = object.getLastTriggerTime();
        if (lastTriggerTime != null) {
            statement.bindLong(17, lastTriggerTime.getTime());
        } else {
            statement.bindNull(17);
        }
        Date nextTriggerTime = object.getNextTriggerTime();
        if (nextTriggerTime != null) {
            statement.bindLong(18, nextTriggerTime.getTime());
        } else {
            statement.bindNull(18);
        }
        Integer lifecycleState = object.getLifecycleState();
        if (lifecycleState != null) {
            statement.bindLong(19, (long) lifecycleState.intValue());
            return;
        }
        statement.bindNull(19);
    }

    public Rule readObject(Cursor cursor, int offset) {
        return new Rule(cursor);
    }

    public void setPrimaryKeyValue(Rule object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Rule object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
