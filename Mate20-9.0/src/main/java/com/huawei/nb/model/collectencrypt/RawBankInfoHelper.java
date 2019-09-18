package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBankInfoHelper extends AEntityHelper<RawBankInfo> {
    private static final RawBankInfoHelper INSTANCE = new RawBankInfoHelper();

    private RawBankInfoHelper() {
    }

    public static RawBankInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawBankInfo object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mBankInfo = object.getMBankInfo();
        if (mBankInfo != null) {
            statement.bindString(3, mBankInfo);
        } else {
            statement.bindNull(3);
        }
        String mLastNo = object.getMLastNo();
        if (mLastNo != null) {
            statement.bindString(4, mLastNo);
        } else {
            statement.bindNull(4);
        }
        Date mRepaymentDate = object.getMRepaymentDate();
        if (mRepaymentDate != null) {
            statement.bindLong(5, mRepaymentDate.getTime());
        } else {
            statement.bindNull(5);
        }
        Double mRepaymentAmountCNY = object.getMRepaymentAmountCNY();
        if (mRepaymentAmountCNY != null) {
            statement.bindDouble(6, mRepaymentAmountCNY.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Double mRepayLowestCNY = object.getMRepayLowestCNY();
        if (mRepayLowestCNY != null) {
            statement.bindDouble(7, mRepayLowestCNY.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double mRepayAmountUSD = object.getMRepayAmountUSD();
        if (mRepayAmountUSD != null) {
            statement.bindDouble(8, mRepayAmountUSD.doubleValue());
        } else {
            statement.bindNull(8);
        }
        Double mRepayLowestUSD = object.getMRepayLowestUSD();
        if (mRepayLowestUSD != null) {
            statement.bindDouble(9, mRepayLowestUSD.doubleValue());
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    public RawBankInfo readObject(Cursor cursor, int offset) {
        return new RawBankInfo(cursor);
    }

    public void setPrimaryKeyValue(RawBankInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawBankInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
