package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigit;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitBn;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitFa;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitNe;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitZh;
import java.util.Locale;

public class StringConvert {
    private String convertQanChar(String str) {
        StringBuffer stringBuffer = new StringBuffer("");
        String str2 = "\u3000\uff1a\uff0f\uff0e\uff3c\u2215\uff0c.\uff01\uff08\uff09\uff1f\ufe61\uff1b\uff1a\u3010\u3011\uff0d\uff0b\uff1d\uff5b\uff5d\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff10\uff41\uff42\uff43\uff44\uff45\uff46\uff47\uff48\uff49\uff4a\uff4b\uff4c\uff4d\uff4e\uff4f\uff50\uff51\uff52\uff53\uff54\uff55\uff56\uff57\uff58\uff59\uff5a\uff21\uff22\uff23\uff24\uff25\uff26\uff27\uff28\uff29\uff2a\uff2b\uff2c\uff2d\uff2e\uff2f\uff30\uff31\uff32\uff33\uff34\uff35\uff36\uff37\uff38\uff39\uff3a";
        String str3 = " :/.\\/,.!()?*;:[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < str.length(); i++) {
            String substring = str.substring(i, i + 1);
            int indexOf = str2.indexOf(substring);
            if (indexOf != -1) {
                stringBuffer.append(str3.substring(indexOf, indexOf + 1));
            } else {
                stringBuffer.append(substring);
            }
        }
        return stringBuffer.toString();
    }

    private String replaceZh(String str) {
        return str.replaceAll("\u793c\u62dc", "\u661f\u671f").replaceAll("\u661f\u671f\u5929", "\u661f\u671f\u65e5").replaceAll("\u9031", "\u5468").replaceAll("\u5468\u5929", "\u5468\u65e5").replaceAll("\u5f8c", "\u540e").replaceAll("\u500b", "\u4e2a").replaceAll("\u5169", "\u4e24").replaceAll("\u937e", "\u949f");
    }

    public String convertString(String str, String str2) {
        if (str2.equals("zh_hans") || str2.equals("en")) {
            return replaceZh(convertQanChar(str));
        }
        if (str2.equals("fa")) {
            str = convertDigit(str, "fa");
        } else if (str2.equals("ne")) {
            str = convertDigit(str, "ne");
        } else if (str2.equals("bn")) {
            str = convertDigit(str, "bn");
        } else if (str2.equals("ru") || str2.equals("lt") || str2.equals("kk") || str2.equals("be")) {
            str = str.toLowerCase(new Locale(str2));
        }
        return str;
    }

    public String convertDigit(String str, String str2) {
        LocaleDigit localeDigit = getLocaleDigit(str2);
        if (localeDigit != null) {
            return localeDigit.convert(str);
        }
        return str;
    }

    public boolean isDigit(String str, String str2) {
        LocaleDigit localeDigit = getLocaleDigit(str2);
        if (localeDigit != null) {
            return localeDigit.isDigit(str);
        }
        return false;
    }

    public LocaleDigit getLocaleDigit(String str) {
        if (str.equals("zh_hans") || str.equals("ja") || str.equals("en")) {
            return new LocaleDigitZh();
        }
        if (str.equals("fa")) {
            return new LocaleDigitFa();
        }
        if (str.equals("ne")) {
            return new LocaleDigitNe();
        }
        if (str.equals("bn")) {
            return new LocaleDigitBn();
        }
        return null;
    }
}
