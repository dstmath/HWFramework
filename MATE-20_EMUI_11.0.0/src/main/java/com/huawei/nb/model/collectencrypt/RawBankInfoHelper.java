package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBankInfoHelper extends AEntityHelper<RawBankInfo> {
    private static final RawBankInfoHelper INSTANCE = new RawBankInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawBankInfo rawBankInfo) {
        return null;
    }

    private RawBankInfoHelper() {
    }

    public static RawBankInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawBankInfo rawBankInfo) {
        Integer mId = rawBankInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawBankInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mBankInfo = rawBankInfo.getMBankInfo();
        if (mBankInfo != null) {
            statement.bindString(3, mBankInfo);
        } else {
            statement.bindNull(3);
        }
        String mLastNo = rawBankInfo.getMLastNo();
        if (mLastNo != null) {
            statement.bindString(4, mLastNo);
        } else {
            statement.bindNull(4);
        }
        Date mRepaymentDate = rawBankInfo.getMRepaymentDate();
        if (mRepaymentDate != null) {
            statement.bindLong(5, mRepaymentDate.getTime());
        } else {
            statement.bindNull(5);
        }
        Double mRepaymentAmountCNY = rawBankInfo.getMRepaymentAmountCNY();
        if (mRepaymentAmountCNY != null) {
            statement.bindDouble(6, mRepaymentAmountCNY.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Double mRepayLowestCNY = rawBankInfo.getMRepayLowestCNY();
        if (mRepayLowestCNY != null) {
            statement.bindDouble(7, mRepayLowestCNY.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double mRepayAmountUSD = rawBankInfo.getMRepayAmountUSD();
        if (mRepayAmountUSD != null) {
            statement.bindDouble(8, mRepayAmountUSD.doubleValue());
        } else {
            statement.bindNull(8);
        }
        Double mRepayLowestUSD = rawBankInfo.getMRepayLowestUSD();
        if (mRepayLowestUSD != null) {
            statement.bindDouble(9, mRepayLowestUSD.doubleValue());
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = rawBankInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = rawBankInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawBankInfo readObject(Cursor cursor, int i) {
        return new RawBankInfo(cursor);
    }

    public void setPrimaryKeyValue(RawBankInfo rawBankInfo, long j) {
        rawBankInfo.setMId(Integer.valueOf((int) j));
    }
}
