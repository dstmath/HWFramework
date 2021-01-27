package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleMarkPointHelper extends AEntityHelper<RuleMarkPoint> {
    private static final RuleMarkPointHelper INSTANCE = new RuleMarkPointHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RuleMarkPoint ruleMarkPoint) {
        return null;
    }

    private RuleMarkPointHelper() {
    }

    public static RuleMarkPointHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RuleMarkPoint ruleMarkPoint) {
        Long id = ruleMarkPoint.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String ruleName = ruleMarkPoint.getRuleName();
        if (ruleName != null) {
            statement.bindString(2, ruleName);
        } else {
            statement.bindNull(2);
        }
        String businessName = ruleMarkPoint.getBusinessName();
        if (businessName != null) {
            statement.bindString(3, businessName);
        } else {
            statement.bindNull(3);
        }
        String operatorName = ruleMarkPoint.getOperatorName();
        if (operatorName != null) {
            statement.bindString(4, operatorName);
        } else {
            statement.bindNull(4);
        }
        String itemName = ruleMarkPoint.getItemName();
        if (itemName != null) {
            statement.bindString(5, itemName);
        } else {
            statement.bindNull(5);
        }
        Integer recommendedCount = ruleMarkPoint.getRecommendedCount();
        if (recommendedCount != null) {
            statement.bindLong(6, (long) recommendedCount.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer category = ruleMarkPoint.getCategory();
        if (category != null) {
            statement.bindLong(7, (long) category.intValue());
        } else {
            statement.bindNull(7);
        }
        Date timeStamp = ruleMarkPoint.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(8, timeStamp.getTime());
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RuleMarkPoint readObject(Cursor cursor, int i) {
        return new RuleMarkPoint(cursor);
    }

    public void setPrimaryKeyValue(RuleMarkPoint ruleMarkPoint, long j) {
        ruleMarkPoint.setId(Long.valueOf(j));
    }
}
