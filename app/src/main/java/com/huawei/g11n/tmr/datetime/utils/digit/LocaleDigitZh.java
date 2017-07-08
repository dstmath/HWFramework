package com.huawei.g11n.tmr.datetime.utils.digit;

import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleDigitZh extends LocaleDigit {
    public LocaleDigitZh() {
        this.pattern = "[0-9\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u4e24\u6574\u534a\u79d1\u949f\u937e\u5169]+";
    }

    public String convert(String str) {
        Object replaceAll = str.replaceAll("\u534a", "30").replaceAll("\u949f", "00").replaceAll("\u937e", "00").replaceAll("\u6574", "00").replaceAll("\u4e00\u523b", "15").replaceAll("\u4e09\u523b", "45");
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('\u4e00'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('\u4e8c'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('\u4e09'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('\u56db'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('\u4e94'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('\u516d'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('\u4e03'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('\u516b'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('\u4e5d'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('\u96f6'), Integer.valueOf(0));
        hashMap.put(Character.valueOf('\u5341'), Integer.valueOf(10));
        hashMap.put(Character.valueOf('\u4e24'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('\u5169'), Integer.valueOf(2));
        Matcher matcher = Pattern.compile("[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u4e24\u5169]{1,10}").matcher(replaceAll);
        StringBuffer stringBuffer = new StringBuffer(replaceAll);
        while (matcher.find()) {
            int intValue;
            String group = matcher.group();
            switch (group.length()) {
                case HwFragmentContainer.TRANSITION_FADE /*1*/:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue();
                    break;
                case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                    if (group.charAt(0) != '\u5341') {
                        if (group.charAt(1) == '\u5341') {
                            if (group.charAt(0) != '\u96f6') {
                                intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10;
                                break;
                            }
                            intValue = 10;
                            break;
                        }
                        intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() + (((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10);
                        break;
                    }
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() + 10;
                    break;
                case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(2)))).intValue() + (((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10);
                    break;
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(3)))).intValue() + (((((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 1000) + (((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() * 100)) + (((Integer) hashMap.get(Character.valueOf(group.charAt(2)))).intValue() * 10));
                    break;
                default:
                    intValue = 0;
                    break;
            }
            stringBuffer.replace(stringBuffer.indexOf(group), group.length() + stringBuffer.indexOf(group), "" + intValue);
        }
        return stringBuffer.toString();
    }
}
