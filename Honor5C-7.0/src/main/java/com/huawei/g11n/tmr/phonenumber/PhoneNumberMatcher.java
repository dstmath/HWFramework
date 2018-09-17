package com.huawei.g11n.tmr.phonenumber;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ES_ES;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_FR_FR;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_PT_PT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.view.HwMotionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberMatcher extends AbstractPhoneNumberMatcher {
    private static final char REPLACE_CHAR = 'A';
    private boolean flag;
    private PhoneNumberRule phoneNumberRule;

    public PhoneNumberMatcher(String str) {
        super(str);
        this.flag = true;
        if ("CN".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_ZH_CN(str);
        } else if ("GB".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_EN_GB(str);
        } else if ("DE".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_DE_DE(str);
        } else if ("IT".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_IT_IT(str);
        } else if ("FR".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_FR_FR(str);
        } else if ("ES".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_ES_ES(str);
        } else if ("PT".equals(str)) {
            this.phoneNumberRule = new PhoneNumberRule_PT_PT(str);
        } else {
            this.flag = false;
            this.phoneNumberRule = null;
        }
    }

    public List<MatchedNumberInfo> deleteRepeatedInfo(List<MatchedNumberInfo> list) {
        List<MatchedNumberInfo> arrayList = new ArrayList();
        for (MatchedNumberInfo matchedNumberInfo : list) {
            Object obj = 1;
            for (MatchedNumberInfo matchedNumberInfo2 : arrayList) {
                if (matchedNumberInfo.getBegin() == matchedNumberInfo2.getBegin() && matchedNumberInfo.getEnd() == matchedNumberInfo2.getEnd()) {
                    obj = null;
                }
            }
            if (obj != null) {
                arrayList.add(matchedNumberInfo);
            }
        }
        return arrayList;
    }

    public int[] getMatchedPhoneNumber(String str, String str2) {
        int i = 0;
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        List arrayList;
        int size;
        int[] iArr;
        if (this.flag) {
            String convertQanChar = convertQanChar(str);
            String handleNegativeRule = handleNegativeRule(convertQanChar, this.phoneNumberRule);
            ShortNumberInfo instance2 = ShortNumberInfo.getInstance();
            Iterable<PhoneNumberMatch> findNumbers = instance.findNumbers(handleNegativeRule, str2, Leniency.POSSIBLE, Long.MAX_VALUE);
            arrayList = new ArrayList();
            for (PhoneNumberMatch phoneNumberMatch : findNumbers) {
                if (handleBorderRule(phoneNumberMatch, handleNegativeRule, this.phoneNumberRule)) {
                    PhoneNumberMatch handleCodeRule = handleCodeRule(phoneNumberMatch, convertQanChar, this.phoneNumberRule);
                    if (handleCodeRule != null) {
                        PhoneNumber number = handleCodeRule.number();
                        if (instance.isValidNumber(number) || instance2.isValidShortNumber(number)) {
                            MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                            matchedNumberInfo.setBegin(handleCodeRule.start());
                            matchedNumberInfo.setEnd(handleCodeRule.end());
                            matchedNumberInfo.setContent(handleCodeRule.rawString());
                            if ("CN".equals(str2)) {
                                CharSequence substring;
                                Pattern compile = Pattern.compile("(?<![-\\d])\\d{5,6}[\\/|\\|]\\d{5,6}(?![-\\d])");
                                String str3 = "";
                                if (matchedNumberInfo.getContent().startsWith("(") || matchedNumberInfo.getContent().startsWith("[")) {
                                    substring = matchedNumberInfo.getContent().substring(1);
                                } else {
                                    substring = matchedNumberInfo.getContent();
                                }
                                if (!compile.matcher(substring).matches()) {
                                    arrayList.add(matchedNumberInfo);
                                }
                            } else {
                                arrayList.add(matchedNumberInfo);
                            }
                        }
                        Collection handlePositiveRule = handlePositiveRule(handleCodeRule, handleNegativeRule, this.phoneNumberRule);
                        if (!(handlePositiveRule == null || handlePositiveRule.isEmpty())) {
                            arrayList.addAll(handlePositiveRule);
                        }
                    }
                }
            }
            List<MatchedNumberInfo> deleteRepeatedInfo = deleteRepeatedInfo(arrayList);
            for (MatchedNumberInfo matchedNumberInfo2 : deleteRepeatedInfo) {
                if (matchedNumberInfo2 != null) {
                    dealNumbersWithOneBracket(matchedNumberInfo2);
                }
            }
            size = !deleteRepeatedInfo.isEmpty() ? deleteRepeatedInfo.size() : 0;
            if (size != 0) {
                int[] iArr2 = new int[((size * 2) + 1)];
                iArr2[0] = size;
                while (i < size) {
                    iArr2[(i * 2) + 1] = ((MatchedNumberInfo) deleteRepeatedInfo.get(i)).getBegin();
                    iArr2[(i * 2) + 2] = ((MatchedNumberInfo) deleteRepeatedInfo.get(i)).getEnd();
                    i++;
                }
                iArr = iArr2;
            } else {
                iArr = new int[]{0};
            }
            return iArr;
        }
        findNumbers = instance.findNumbers(str, str2, Leniency.POSSIBLE, Long.MAX_VALUE);
        arrayList = new ArrayList();
        for (PhoneNumberMatch rawString : findNumbers) {
            arrayList.add(rawString.rawString());
        }
        size = !arrayList.isEmpty() ? arrayList.size() : 0;
        if (arrayList.isEmpty()) {
            iArr = new int[]{0};
        } else {
            int[] iArr3 = new int[((size * 2) + 1)];
            int i2 = 0;
            for (PhoneNumberMatch rawString2 : findNumbers) {
                iArr3[(i2 * 2) + 1] = rawString2.start();
                iArr3[(i2 * 2) + 2] = rawString2.end();
                i2++;
            }
            iArr3[0] = size;
            iArr = iArr3;
        }
        return iArr;
    }

    private PhoneNumberMatch handleCodeRule(PhoneNumberMatch phoneNumberMatch, String str, PhoneNumberRule phoneNumberRule) {
        List<RegexRule> codesRules = this.phoneNumberRule.getCodesRules();
        int i;
        if (codesRules != null) {
            for (RegexRule isValid : codesRules) {
                phoneNumberMatch = isValid.isValid(phoneNumberMatch, str);
                if (phoneNumberMatch == null) {
                    Object obj = null;
                    break;
                }
            }
            i = 1;
        } else {
            i = 1;
        }
        if (obj == null) {
            return null;
        }
        return phoneNumberMatch;
    }

    private static boolean isNumbersWithOneBracket(String str) {
        if (str != null) {
            int i = 0;
            boolean z = false;
            boolean z2 = false;
            while (i < str.length()) {
                if (str.charAt(i) == ')') {
                    z2 = true;
                }
                if (str.charAt(i) == '(' && i == 0) {
                    z = true;
                }
                if (str.charAt(i) == ']') {
                    z2 = true;
                }
                if (str.charAt(i) == '[' && i == 0) {
                    z = true;
                }
                if (str.charAt(i) == '\u3011') {
                    z2 = true;
                }
                if (str.charAt(i) == '\u3010' && i == 0) {
                    z = true;
                }
                i++;
            }
            return z && !z2;
        }
    }

    private static MatchedNumberInfo dealNumbersWithOneBracket(MatchedNumberInfo matchedNumberInfo) {
        if (!isNumbersWithOneBracket(matchedNumberInfo.getContent())) {
            return matchedNumberInfo;
        }
        matchedNumberInfo.setBegin(matchedNumberInfo.getBegin() + 1);
        matchedNumberInfo.setContent(matchedNumberInfo.getContent().substring(1));
        return matchedNumberInfo;
    }

    private static String dealStringWithOneBracket(String str) {
        if (isNumbersWithOneBracket(str)) {
            return str.substring(1);
        }
        return str;
    }

    private static String convertQanChar(String str) {
        StringBuffer stringBuffer = new StringBuffer("");
        String str2 = "\uff1a\uff0f\uff0e\uff3c\u2215\uff0c.\uff01\uff08\uff09\uff1f\ufe61\uff1b\uff1a\ufe63\u2014\uff0d\u3010\u3011\uff0d\uff0b\uff1d\uff5b\uff5d\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff10\uff41\uff42\uff43\uff44\uff45\uff46\uff47\uff48\uff49\uff4a\uff4b\uff4c\uff4d\uff4e\uff4f\uff50\uff51\uff52\uff53\uff54\uff55\uff56\uff57\uff58\uff59\uff5a\uff21\uff22\uff23\uff24\uff25\uff26\uff27\uff28\uff29\uff2a\uff2b\uff2c\uff2d\uff2e\uff2f\uff30\uff31\uff32\uff33\uff34\uff35\uff36\uff37\uff38\uff39\uff3a";
        String str3 = ":/.\\/,.!()?*;:---[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
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

    private static String handleNegativeRule(String str, PhoneNumberRule phoneNumberRule) {
        String str2 = str;
        for (RegexRule pattern : phoneNumberRule.getNegativeRules()) {
            Matcher matcher = pattern.getPattern().matcher(str2);
            String str3 = str2;
            while (matcher.find()) {
                str3 = replaceSpecifiedPos(str3.toCharArray(), matcher.start(), matcher.end());
            }
            str2 = str3;
        }
        return str2;
    }

    private static List<MatchedNumberInfo> handlePositiveRule(PhoneNumberMatch phoneNumberMatch, String str, PhoneNumberRule phoneNumberRule) {
        List<RegexRule> positiveRules = phoneNumberRule.getPositiveRules();
        if (positiveRules == null || positiveRules.isEmpty()) {
            return null;
        }
        List<MatchedNumberInfo> arrayList = new ArrayList();
        CharSequence dealStringWithOneBracket = dealStringWithOneBracket(phoneNumberMatch.rawString());
        for (RegexRule regexRule : positiveRules) {
            if (regexRule.getPattern().matcher(dealStringWithOneBracket).find()) {
                arrayList.addAll(regexRule.handle(phoneNumberMatch, str));
                return arrayList;
            } else if (regexRule.getPattern().matcher(str).find()) {
                Collection handle = regexRule.handle(phoneNumberMatch, str);
                if (!(handle == null || handle.isEmpty())) {
                    arrayList.addAll(handle);
                    return arrayList;
                }
            }
        }
        return null;
    }

    private static boolean handleBorderRule(PhoneNumberMatch phoneNumberMatch, String str, PhoneNumberRule phoneNumberRule) {
        List<RegexRule> borderRules = phoneNumberRule.getBorderRules();
        if (borderRules == null || borderRules.isEmpty()) {
            return true;
        }
        int start = phoneNumberMatch.start();
        int end = phoneNumberMatch.end();
        int i = start + -10 >= 0 ? start - 10 : 0;
        CharSequence substring = str.substring(i, end + 10 <= str.length() ? end + 10 : str.length());
        for (RegexRule regexRule : borderRules) {
            Matcher matcher = regexRule.getPattern().matcher(substring);
            int type = regexRule.getType();
            while (matcher.find()) {
                boolean z;
                int start2 = matcher.start() + i;
                int end2 = matcher.end() + i;
                switch (type) {
                    case HwMotionEvent.TOOL_TYPE_BEZEL /*8*/:
                        if (start2 > start || end > end2) {
                            if (start2 >= start || start >= end2 || end2 >= end) {
                                if (start < start2 && start2 < end) {
                                    if (end >= end2) {
                                    }
                                }
                                z = false;
                                continue;
                            }
                        }
                        z = true;
                        continue;
                    case HwDeviceAdminInfo.USES_POLICY_SET_MDM_EMAIL /*9*/:
                        if (start2 <= start && end <= end2) {
                            z = true;
                            continue;
                        } else {
                            z = false;
                            continue;
                        }
                    default:
                        z = false;
                        continue;
                }
                if (z) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String replaceSpecifiedPos(char[] cArr, int i, int i2) {
        if (i > i2) {
            return new String(cArr);
        }
        int i3 = 0;
        while (i3 < cArr.length) {
            if (i3 >= i && i3 < i2) {
                cArr[i3] = REPLACE_CHAR;
            }
            i3++;
        }
        return new String(cArr);
    }
}
