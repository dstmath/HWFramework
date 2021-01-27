package com.huawei.i18n.tmr.datetime;

import com.huawei.i18n.tmr.datetime.parse.DateConvert;
import com.huawei.i18n.tmr.datetime.utils.StringConvert;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTmrHandle extends AbstractDateTmrHandle {
    private DateConvert convert;
    private RuleInit ruleInit;

    DateTmrHandle(String locale, String localeBackup) {
        super(locale);
        this.ruleInit = new RuleInit(locale, localeBackup);
        this.convert = new DateConvert(locale);
    }

    @Override // com.huawei.i18n.tmr.datetime.AbstractDateTmrHandle
    public int[] getTime(String content) {
        List<Match> matchList = this.ruleInit.detect(strPreProcess(content));
        int length = (matchList == null || matchList.isEmpty()) ? 0 : matchList.size();
        if (length == 0) {
            return new int[]{0};
        }
        int[] result = new int[((length * 3) + 1)];
        result[0] = length;
        for (int i = 0; i < length; i++) {
            result[(i * 3) + 1] = matchList.get(i).getType();
            result[(i * 3) + 2] = matchList.get(i).getBegin();
            int end = matchList.get(i).getEnd();
            if (end > 1) {
                end--;
            }
            result[(i * 3) + 3] = end;
        }
        return result;
    }

    @Override // com.huawei.i18n.tmr.datetime.AbstractDateTmrHandle
    public Date[] convertDate(String content, long defaultDate) {
        List<Date> result = this.convert.convert(this.ruleInit.parse(strPreProcess(content), defaultDate), defaultDate);
        if (result == null || result.isEmpty()) {
            result = new ArrayList();
            result.add(new Date(defaultDate));
        }
        return (Date[]) result.toArray(new Date[0]);
    }

    private String strPreProcess(String content) {
        return new StringConvert().convertString(content, getLocale());
    }
}
