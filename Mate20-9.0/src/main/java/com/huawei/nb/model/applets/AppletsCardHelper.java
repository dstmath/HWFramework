package com.huawei.nb.model.applets;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class AppletsCardHelper extends AEntityHelper<AppletsCard> {
    private static final AppletsCardHelper INSTANCE = new AppletsCardHelper();

    private AppletsCardHelper() {
    }

    public static AppletsCardHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AppletsCard object) {
        String routine_id = object.getRoutine_id();
        if (routine_id != null) {
            statement.bindString(1, routine_id);
        } else {
            statement.bindNull(1);
        }
        String card_id = object.getCard_id();
        if (card_id != null) {
            statement.bindString(2, card_id);
        } else {
            statement.bindNull(2);
        }
        Integer card_status = object.getCard_status();
        if (card_status != null) {
            statement.bindLong(3, (long) card_status.intValue());
        } else {
            statement.bindNull(3);
        }
        String card_type = object.getCard_type();
        if (card_type != null) {
            statement.bindString(4, card_type);
        } else {
            statement.bindNull(4);
        }
        String base_info = object.getBase_info();
        if (base_info != null) {
            statement.bindString(5, base_info);
        } else {
            statement.bindNull(5);
        }
        Date life_cycle_date = object.getLife_cycle_date();
        if (life_cycle_date != null) {
            statement.bindLong(6, life_cycle_date.getTime());
        } else {
            statement.bindNull(6);
        }
    }

    public AppletsCard readObject(Cursor cursor, int offset) {
        return new AppletsCard(cursor);
    }

    public void setPrimaryKeyValue(AppletsCard object, long value) {
    }

    public Object getRelationshipObject(String field, AppletsCard object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
