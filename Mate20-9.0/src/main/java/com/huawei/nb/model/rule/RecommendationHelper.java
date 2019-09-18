package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RecommendationHelper extends AEntityHelper<Recommendation> {
    private static final RecommendationHelper INSTANCE = new RecommendationHelper();

    private RecommendationHelper() {
    }

    public static RecommendationHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Recommendation object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long businessId = object.getBusinessId();
        if (businessId != null) {
            statement.bindLong(2, businessId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long itemId = object.getItemId();
        if (itemId != null) {
            statement.bindLong(3, itemId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long ruleId = object.getRuleId();
        if (ruleId != null) {
            statement.bindLong(4, ruleId.longValue());
        } else {
            statement.bindNull(4);
        }
        String message = object.getMessage();
        if (message != null) {
            statement.bindString(5, message);
        } else {
            statement.bindNull(5);
        }
        Date timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(6, timeStamp.getTime());
        } else {
            statement.bindNull(6);
        }
    }

    public Recommendation readObject(Cursor cursor, int offset) {
        return new Recommendation(cursor);
    }

    public void setPrimaryKeyValue(Recommendation object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Recommendation object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
