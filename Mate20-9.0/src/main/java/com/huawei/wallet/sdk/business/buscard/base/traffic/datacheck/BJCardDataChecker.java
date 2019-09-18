package com.huawei.wallet.sdk.business.buscard.base.traffic.datacheck;

import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.TimeUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BJCardDataChecker implements DataChecker {
    private SimpleDateFormat sdf = new SimpleDateFormat(TimeUtil.YEAR_TO_SECOND_NO_LINE, Locale.getDefault());

    public BJCardDataChecker() {
        this.sdf.setLenient(false);
    }

    public void checkAmount(CardInfo info) throws AppletCardException {
        int overdraftAmount = info.getOverdraftAmount();
        int amount = info.getAmount();
        if (overdraftAmount < 0) {
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_OVERDRAFT_AMOUNT_ABNORMAL, "beijing card overdraft amount < 0. amount : " + overdraftAmount);
        } else if (amount > 0 && overdraftAmount > 0) {
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_AMOUNT_ABNORMAL, "beijing card amount > 0 && overdraftAmount > 0. amount : " + amount + " overdraftAmount : " + overdraftAmount);
        }
    }

    public void checkDate(CardInfo info) throws AppletCardException {
        try {
            SimpleDateFormat simpleDateFormat = this.sdf;
            Date enableDate = simpleDateFormat.parse(info.getEnableDate() + "000001");
            SimpleDateFormat simpleDateFormat2 = this.sdf;
            Date exDate = simpleDateFormat2.parse(info.getExpireDate() + "235959");
            Date today = new Date(System.currentTimeMillis());
            if (today.before(enableDate)) {
                throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_ENABLE_DATE_ABNORMAL, "beijing card enable date is after the current date. enableDate : " + info.getEnableDate());
            } else if (today.after(exDate)) {
                throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_OUT_OF_EXPIRE_DATE, "beijing card out of expire date.expireDate : " + info.getExpireDate());
            }
        } catch (ParseException e) {
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_DATE_FORMAT_ERROR, "beijing card date format error.enableDate : " + info.getEnableDate() + " expireDate : " + info.getExpireDate());
        }
    }
}
