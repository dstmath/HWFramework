package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.parse.DateConvert;
import com.huawei.g11n.tmr.datetime.utils.StringConvert;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTmrHandle extends AbstractDateTmrHandle {
    private DateConvert convert;
    private RuleInit obj;

    public DateTmrHandle(String str, String str2) {
        super(str);
        this.obj = new RuleInit(str, str2);
        this.convert = new DateConvert(str);
    }

    public int[] getTime(String str) {
        int i = 0;
        List detect = this.obj.detect(strPreProcess(str));
        int size = (detect == null || detect.isEmpty()) ? 0 : detect.size();
        if (size != 0) {
            int[] iArr = new int[((size * 3) + 1)];
            iArr[0] = size;
            while (i < size) {
                iArr[(i * 3) + 1] = ((Match) detect.get(i)).getType();
                iArr[(i * 3) + 2] = ((Match) detect.get(i)).getBegin();
                int end = ((Match) detect.get(i)).getEnd();
                if (end > 1) {
                    end--;
                }
                iArr[(i * 3) + 3] = end;
                i++;
            }
            return iArr;
        }
        return new int[]{0};
    }

    public Date[] convertDate(String str, long j) {
        List convert = this.convert.convert(this.obj.parse(strPreProcess(str), j), j);
        if (convert == null || convert.isEmpty()) {
            convert = new ArrayList();
            convert.add(new Date(j));
        }
        return (Date[]) convert.toArray(new Date[convert.size()]);
    }

    public String strPreProcess(String str) {
        return new StringConvert().convertString(str, getLocale());
    }
}
