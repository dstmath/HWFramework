package com.huawei.nb.model.applets;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class AppletsCardHelper extends AEntityHelper<AppletsCard> {
    private static final AppletsCardHelper INSTANCE = new AppletsCardHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AppletsCard appletsCard) {
        return null;
    }

    public void setPrimaryKeyValue(AppletsCard appletsCard, long j) {
    }

    private AppletsCardHelper() {
    }

    public static AppletsCardHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AppletsCard appletsCard) {
        String routine_id = appletsCard.getRoutine_id();
        if (routine_id != null) {
            statement.bindString(1, routine_id);
        } else {
            statement.bindNull(1);
        }
        String card_id = appletsCard.getCard_id();
        if (card_id != null) {
            statement.bindString(2, card_id);
        } else {
            statement.bindNull(2);
        }
        Integer card_status = appletsCard.getCard_status();
        if (card_status != null) {
            statement.bindLong(3, (long) card_status.intValue());
        } else {
            statement.bindNull(3);
        }
        String card_type = appletsCard.getCard_type();
        if (card_type != null) {
            statement.bindString(4, card_type);
        } else {
            statement.bindNull(4);
        }
        String base_info = appletsCard.getBase_info();
        if (base_info != null) {
            statement.bindString(5, base_info);
        } else {
            statement.bindNull(5);
        }
        Date life_cycle_date = appletsCard.getLife_cycle_date();
        if (life_cycle_date != null) {
            statement.bindLong(6, life_cycle_date.getTime());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AppletsCard readObject(Cursor cursor, int i) {
        return new AppletsCard(cursor);
    }
}
