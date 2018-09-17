package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigit;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitBn;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitFa;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitNe;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitZh;
import java.util.Locale;

public class StringConvert {
    private String convertQanChar(String instr) {
        StringBuffer retsb = new StringBuffer("");
        String fwchstr = "　：／．＼∕，.！（）？﹡；：【】－＋＝｛｝１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
        String hwchstr = " :/.\\/,.!()?*;:[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < instr.length(); i++) {
            String tempstr = instr.substring(i, i + 1);
            int index = fwchstr.indexOf(tempstr);
            if (index != -1) {
                retsb.append(hwchstr.substring(index, index + 1));
            } else {
                retsb.append(tempstr);
            }
        }
        return retsb.toString();
    }

    private String replaceZh(String a) {
        return a.replaceAll("礼拜", "星期").replaceAll("星期天", "星期日").replaceAll("週", "周").replaceAll("周天", "周日").replaceAll("後", "后").replaceAll("個", "个").replaceAll("兩", "两").replaceAll("鍾", "钟");
    }

    public String convertString(String c, String locale) {
        if (locale.equals("zh_hans") || locale.equals("en")) {
            return replaceZh(convertQanChar(c));
        }
        if (locale.equals("fa")) {
            c = convertDigit(c, "fa");
        } else if (locale.equals("ne")) {
            c = convertDigit(c, "ne");
        } else if (locale.equals("bn")) {
            c = convertDigit(c, "bn");
        } else if (locale.equals("as")) {
            c = convertDigit(c, "as");
        } else if (locale.equals("mr")) {
            c = convertDigit(c, "mr");
        } else if (locale.equals("mai")) {
            c = convertDigit(c, "mai");
        } else if (locale.equals("ru") || locale.equals("lt") || locale.equals("kk") || locale.equals("be")) {
            c = c.toLowerCase(new Locale(locale));
        }
        return c;
    }

    public String convertDigit(String c, String locale) {
        LocaleDigit result = getLocaleDigit(locale);
        if (result != null) {
            return result.convert(c);
        }
        return c;
    }

    public boolean isDigit(String c, String locale) {
        LocaleDigit result = getLocaleDigit(locale);
        if (result != null) {
            return result.isDigit(c);
        }
        return false;
    }

    public LocaleDigit getLocaleDigit(String locale) {
        if (locale.equals("zh_hans") || locale.equals("ja") || locale.equals("en")) {
            return new LocaleDigitZh();
        }
        if (locale.equals("fa")) {
            return new LocaleDigitFa();
        }
        if (locale.equals("ne") || locale.equals("mr") || locale.equals("mai")) {
            return new LocaleDigitNe();
        }
        if (locale.equals("bn") || locale.equals("as")) {
            return new LocaleDigitBn();
        }
        return null;
    }
}
