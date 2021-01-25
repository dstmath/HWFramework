package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleHelper extends AEntityHelper<Rule> {
    private static final RuleHelper INSTANCE = new RuleHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Rule rule) {
        return null;
    }

    private RuleHelper() {
    }

    public static RuleHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Rule rule) {
        Long id = rule.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = rule.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        Long businessId = rule.getBusinessId();
        if (businessId != null) {
            statement.bindLong(3, businessId.longValue());
        } else {
            statement.bindNull(3);
        }
        String ruleVersion = rule.getRuleVersion();
        if (ruleVersion != null) {
            statement.bindString(4, ruleVersion);
        } else {
            statement.bindNull(4);
        }
        String systemVersion = rule.getSystemVersion();
        if (systemVersion != null) {
            statement.bindString(5, systemVersion);
        } else {
            statement.bindNull(5);
        }
        Integer priority = rule.getPriority();
        if (priority != null) {
            statement.bindLong(6, (long) priority.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer silenceDays = rule.getSilenceDays();
        if (silenceDays != null) {
            statement.bindLong(7, (long) silenceDays.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer delayTimes = rule.getDelayTimes();
        if (delayTimes != null) {
            statement.bindLong(8, (long) delayTimes.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer delayType = rule.getDelayType();
        if (delayType != null) {
            statement.bindLong(9, (long) delayType.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer alwaysMatching = rule.getAlwaysMatching();
        if (alwaysMatching != null) {
            statement.bindLong(10, (long) alwaysMatching.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer matchConditionGroupRelation = rule.getMatchConditionGroupRelation();
        if (matchConditionGroupRelation != null) {
            statement.bindLong(11, (long) matchConditionGroupRelation.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer lifecycleConditionGroupRelation = rule.getLifecycleConditionGroupRelation();
        if (lifecycleConditionGroupRelation != null) {
            statement.bindLong(12, (long) lifecycleConditionGroupRelation.intValue());
        } else {
            statement.bindNull(12);
        }
        Date createTime = rule.getCreateTime();
        if (createTime != null) {
            statement.bindLong(13, createTime.getTime());
        } else {
            statement.bindNull(13);
        }
        Integer remainingDelayTimes = rule.getRemainingDelayTimes();
        if (remainingDelayTimes != null) {
            statement.bindLong(14, (long) remainingDelayTimes.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer triggerTimes = rule.getTriggerTimes();
        if (triggerTimes != null) {
            statement.bindLong(15, (long) triggerTimes.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer recommendCount = rule.getRecommendCount();
        if (recommendCount != null) {
            statement.bindLong(16, (long) recommendCount.intValue());
        } else {
            statement.bindNull(16);
        }
        Date lastTriggerTime = rule.getLastTriggerTime();
        if (lastTriggerTime != null) {
            statement.bindLong(17, lastTriggerTime.getTime());
        } else {
            statement.bindNull(17);
        }
        Date nextTriggerTime = rule.getNextTriggerTime();
        if (nextTriggerTime != null) {
            statement.bindLong(18, nextTriggerTime.getTime());
        } else {
            statement.bindNull(18);
        }
        Integer lifecycleState = rule.getLifecycleState();
        if (lifecycleState != null) {
            statement.bindLong(19, (long) lifecycleState.intValue());
        } else {
            statement.bindNull(19);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Rule readObject(Cursor cursor, int i) {
        return new Rule(cursor);
    }

    public void setPrimaryKeyValue(Rule rule, long j) {
        rule.setId(Long.valueOf(j));
    }
}
