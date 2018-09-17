package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.parse.DateConvert;
import com.huawei.g11n.tmr.datetime.utils.StringConvert;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTmrHandle extends AbstractDateTmrHandle {
    private DateConvert convert;
    private RuleInit obj;

    public DateTmrHandle(String locale, String localeBk) {
        super(locale);
        this.obj = new RuleInit(locale, localeBk);
        this.convert = new DateConvert(locale);
    }

    public int[] getTime(String msg) {
        List<Match> ms = this.obj.detect(strPreProcess(msg));
        int length = (ms == null || ms.isEmpty()) ? 0 : ms.size();
        if (length != 0) {
            int[] result = new int[((length * 3) + 1)];
            result[0] = length;
            for (int i = 0; i < length; i++) {
                result[(i * 3) + 1] = ((Match) ms.get(i)).getType();
                result[(i * 3) + 2] = ((Match) ms.get(i)).getBegin();
                int end = ((Match) ms.get(i)).getEnd();
                if (end > 1) {
                    end--;
                }
                result[(i * 3) + 3] = end;
            }
            return result;
        }
        return new int[]{0};
    }

    public Date[] convertDate(String msg, long defaultDate) {
        List<Date> result = this.convert.convert(this.obj.parse(strPreProcess(msg), defaultDate), defaultDate);
        if (result == null || result.isEmpty()) {
            result = new ArrayList();
            result.add(new Date(defaultDate));
        }
        return (Date[]) result.toArray(new Date[result.size()]);
    }

    public String strPreProcess(String content) {
        return new StringConvert().convertString(content, getLocale());
    }
}
