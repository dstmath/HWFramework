package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleMarkPointHelper extends AEntityHelper<RuleMarkPoint> {
    private static final RuleMarkPointHelper INSTANCE = new RuleMarkPointHelper();

    private RuleMarkPointHelper() {
    }

    public static RuleMarkPointHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RuleMarkPoint object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String ruleName = object.getRuleName();
        if (ruleName != null) {
            statement.bindString(2, ruleName);
        } else {
            statement.bindNull(2);
        }
        String businessName = object.getBusinessName();
        if (businessName != null) {
            statement.bindString(3, businessName);
        } else {
            statement.bindNull(3);
        }
        String operatorName = object.getOperatorName();
        if (operatorName != null) {
            statement.bindString(4, operatorName);
        } else {
            statement.bindNull(4);
        }
        String itemName = object.getItemName();
        if (itemName != null) {
            statement.bindString(5, itemName);
        } else {
            statement.bindNull(5);
        }
        Integer recommendedCount = object.getRecommendedCount();
        if (recommendedCount != null) {
            statement.bindLong(6, (long) recommendedCount.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer category = object.getCategory();
        if (category != null) {
            statement.bindLong(7, (long) category.intValue());
        } else {
            statement.bindNull(7);
        }
        Date timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(8, timeStamp.getTime());
        } else {
            statement.bindNull(8);
        }
    }

    public RuleMarkPoint readObject(Cursor cursor, int offset) {
        return new RuleMarkPoint(cursor);
    }

    public void setPrimaryKeyValue(RuleMarkPoint object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, RuleMarkPoint object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
