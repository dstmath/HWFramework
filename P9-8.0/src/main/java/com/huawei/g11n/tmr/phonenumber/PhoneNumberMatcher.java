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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberMatcher extends AbstractPhoneNumberMatcher {
    private static final char REPLACE_CHAR = 'A';
    private boolean flag = true;
    private PhoneNumberRule phoneNumberRule;

    public PhoneNumberMatcher(String country) {
        super(country);
        if ("CN".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_ZH_CN(country);
        } else if ("GB".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_EN_GB(country);
        } else if ("DE".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_DE_DE(country);
        } else if ("IT".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_IT_IT(country);
        } else if ("FR".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_FR_FR(country);
        } else if ("ES".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_ES_ES(country);
        } else if ("PT".equals(country)) {
            this.phoneNumberRule = new PhoneNumberRule_PT_PT(country);
        } else {
            this.flag = false;
            this.phoneNumberRule = null;
        }
    }

    public List<MatchedNumberInfo> deleteRepeatedInfo(List<MatchedNumberInfo> list) {
        List<MatchedNumberInfo> result = new ArrayList();
        for (MatchedNumberInfo info : list) {
            boolean isAdd = true;
            for (MatchedNumberInfo in : result) {
                if (info.getBegin() == in.getBegin() && info.getEnd() == in.getEnd()) {
                    isAdd = false;
                }
            }
            if (isAdd) {
                result.add(info);
            }
        }
        return result;
    }

    public int[] getMatchedPhoneNumber(String msg, String country) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberMatch match;
        int[] result;
        if (this.flag) {
            String src = convertQanChar(msg);
            String filteredString = handleNegativeRule(src, this.phoneNumberRule);
            ShortNumberInfo shortInfo = ShortNumberInfo.getInstance();
            Iterable<PhoneNumberMatch> matchesList = util.findNumbers(filteredString, country, Leniency.POSSIBLE, Long.MAX_VALUE);
            List<MatchedNumberInfo> ret = new ArrayList();
            for (PhoneNumberMatch match2 : matchesList) {
                if (handleBorderRule(match2, filteredString, this.phoneNumberRule)) {
                    PhoneNumberMatch delMatch = handleCodeRule(match2, src, this.phoneNumberRule);
                    if (delMatch != null) {
                        match2 = delMatch;
                        PhoneNumber number = delMatch.number();
                        if (util.isValidNumber(number) || shortInfo.isValidShortNumber(number)) {
                            MatchedNumberInfo info = new MatchedNumberInfo();
                            info.setBegin(delMatch.start());
                            info.setEnd(delMatch.end());
                            info.setContent(delMatch.rawString());
                            if ("CN".equals(country)) {
                                Pattern p = Pattern.compile("(?<![-\\d])\\d{5,6}[\\/|\\|]\\d{5,6}(?![-\\d])");
                                String str = "";
                                if (info.getContent().startsWith("(") || info.getContent().startsWith("[")) {
                                    str = info.getContent().substring(1);
                                } else {
                                    str = info.getContent();
                                }
                                if (!p.matcher(str).matches()) {
                                    ret.add(info);
                                }
                            } else {
                                ret.add(info);
                            }
                        }
                        List<MatchedNumberInfo> posList = handlePositiveRule(delMatch, filteredString, this.phoneNumberRule);
                        if (!(posList == null || posList.isEmpty())) {
                            ret.addAll(posList);
                        }
                    }
                }
            }
            ret = deleteRepeatedInfo(ret);
            for (MatchedNumberInfo mi : ret) {
                if (mi != null) {
                    dealNumbersWithOneBracket(mi);
                }
            }
            int length = !ret.isEmpty() ? ret.size() : 0;
            if (length != 0) {
                result = new int[((length * 2) + 1)];
                result[0] = length;
                for (int i = 0; i < length; i++) {
                    result[(i * 2) + 1] = ((MatchedNumberInfo) ret.get(i)).getBegin();
                    result[(i * 2) + 2] = ((MatchedNumberInfo) ret.get(i)).getEnd();
                }
            } else {
                result = new int[]{0};
            }
            return result;
        }
        Iterable<PhoneNumberMatch> matches = util.findNumbers(msg, country, Leniency.POSSIBLE, Long.MAX_VALUE);
        List<String> list = new ArrayList();
        int y = 0;
        for (PhoneNumberMatch match22 : matches) {
            list.add(match22.rawString());
        }
        int tnum = !list.isEmpty() ? list.size() : 0;
        if (list.isEmpty()) {
            result = new int[]{0};
        } else {
            result = new int[((tnum * 2) + 1)];
            for (PhoneNumberMatch match222 : matches) {
                result[(y * 2) + 1] = match222.start();
                result[(y * 2) + 2] = match222.end();
                y++;
            }
            result[0] = tnum;
        }
        return result;
    }

    private PhoneNumberMatch handleCodeRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<RegexRule> rules = this.phoneNumberRule.getCodesRules();
        boolean isValid = true;
        if (rules != null) {
            for (RegexRule r : rules) {
                match = r.isValid(match, msg);
                if (match == null) {
                    isValid = false;
                    break;
                }
            }
        }
        isValid = true;
        if (isValid) {
            return match;
        }
        return null;
    }

    private static boolean isNumbersWithOneBracket(String msg) {
        boolean hasRight = false;
        boolean hasLeft = false;
        if (msg != null) {
            int i = 0;
            while (i < msg.length()) {
                if (msg.charAt(i) == ')') {
                    hasRight = true;
                }
                if (msg.charAt(i) == '(' && i == 0) {
                    hasLeft = true;
                }
                if (msg.charAt(i) == ']') {
                    hasRight = true;
                }
                if (msg.charAt(i) == '[' && i == 0) {
                    hasLeft = true;
                }
                if (msg.charAt(i) == 12305) {
                    hasRight = true;
                }
                if (msg.charAt(i) == 12304 && i == 0) {
                    hasLeft = true;
                }
                i++;
            }
            if (hasLeft && !hasRight) {
                return true;
            }
        }
        return false;
    }

    private static MatchedNumberInfo dealNumbersWithOneBracket(MatchedNumberInfo info) {
        if (!isNumbersWithOneBracket(info.getContent())) {
            return info;
        }
        info.setBegin(info.getBegin() + 1);
        info.setContent(info.getContent().substring(1));
        return info;
    }

    private static String dealStringWithOneBracket(String msg) {
        if (isNumbersWithOneBracket(msg)) {
            return msg.substring(1);
        }
        return msg;
    }

    private static String convertQanChar(String instr) {
        StringBuffer retsb = new StringBuffer("");
        String fwchstr = "：／．＼∕，.！（）？﹡；：﹣—－【】－＋＝｛｝１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
        String hwchstr = ":/.\\/,.!()?*;:---[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
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

    private static String handleNegativeRule(String src, PhoneNumberRule phoneNumberRule) {
        String ret = src;
        for (RegexRule rule : phoneNumberRule.getNegativeRules()) {
            Matcher m = rule.getPattern().matcher(ret);
            while (m.find()) {
                ret = replaceSpecifiedPos(ret.toCharArray(), m.start(), m.end());
            }
        }
        return ret;
    }

    private static List<MatchedNumberInfo> handlePositiveRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule) {
        List<RegexRule> rules = phoneNumberRule.getPositiveRules();
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        List<MatchedNumberInfo> ret = new ArrayList();
        String str = dealStringWithOneBracket(match.rawString());
        for (RegexRule rule : rules) {
            if (rule.getPattern().matcher(str).find()) {
                ret.addAll(rule.handle(match, msg));
                return ret;
            } else if (rule.getPattern().matcher(msg).find()) {
                List<MatchedNumberInfo> infoList = rule.handle(match, msg);
                if (!(infoList == null || infoList.isEmpty())) {
                    ret.addAll(infoList);
                    return ret;
                }
            }
        }
        return null;
    }

    private static boolean handleBorderRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule) {
        List<RegexRule> rules = phoneNumberRule.getBorderRules();
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        int begin = match.start();
        int end = match.end();
        int bStr = begin + -10 >= 0 ? begin - 10 : 0;
        String s = msg.substring(bStr, end + 10 <= msg.length() ? end + 10 : msg.length());
        for (RegexRule rule : rules) {
            Matcher mat = rule.getPattern().matcher(s);
            int type = rule.getType();
            while (mat.find()) {
                int b = mat.start() + bStr;
                int e = mat.end() + bStr;
                boolean isdel = false;
                switch (type) {
                    case 8:
                        if ((b <= begin && end <= e) || ((b < begin && begin < e && e < end) || (begin < b && b < end && end < e))) {
                            isdel = true;
                            continue;
                        }
                    case 9:
                        if (b <= begin && end <= e) {
                            isdel = true;
                            continue;
                        }
                    default:
                        break;
                }
                if (isdel) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String replaceSpecifiedPos(char[] chs, int s, int e) {
        if (s > e) {
            return new String(chs);
        }
        int i = 0;
        while (i < chs.length) {
            if (i >= s && i < e) {
                chs[i] = REPLACE_CHAR;
            }
            i++;
        }
        return new String(chs);
    }
}
