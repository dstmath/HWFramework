package com.huawei.g11n.tmr.phonenumber;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ES_ES;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_FR_FR;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_PT_PT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN;
import java.util.ArrayList;
import java.util.Iterator;
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
        } else if ("GB".equals(country) || "UK".equals(country)) {
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
        List<MatchedNumberInfo> result = new ArrayList<>();
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
        int[] result;
        CharSequence filteredStringC;
        Iterable<PhoneNumberMatch> matchesList;
        String src;
        String str;
        int[] result2;
        Phonenumber.PhoneNumber pn;
        String str2 = country;
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        int i = 1;
        int i2 = 2;
        int i3 = 0;
        if (!this.flag) {
            CharSequence msgChar = msg;
            Iterable<PhoneNumberMatch> matches = util.findNumbers(msgChar, str2, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
            List<String> list = new ArrayList<>();
            int y = 0;
            for (PhoneNumberMatch match : matches) {
                list.add(match.rawString());
                matches = matches;
                msgChar = msgChar;
                i = 1;
                i3 = 0;
            }
            int tnum = list.isEmpty() ? i3 : list.size();
            if (list.isEmpty() == 0) {
                int[] result3 = new int[((2 * tnum) + i)];
                for (PhoneNumberMatch match2 : matches) {
                    result3[(2 * y) + i] = match2.start();
                    result3[(2 * y) + 2] = match2.end();
                    y++;
                }
                result3[i3] = tnum;
                result2 = result3;
            } else {
                int[] result4 = new int[i];
                result4[i3] = i3;
                result2 = result4;
            }
            List<Integer> shortList = new ArrayList<>();
            ShortNumberInfo info = ShortNumberInfo.getInstance();
            Pattern shortPattern = Pattern.compile("(?<!(\\d|\\*))\\d{2,8}(?!(\\d|\\*))");
            if (str2.equals("CA")) {
                shortPattern = Pattern.compile("(?<!(\\d|\\*))\\d{5,8}(?!(\\d|\\*))");
            }
            Pattern shortPattern2 = shortPattern;
            Matcher shortMatch = shortPattern2.matcher(msg);
            Phonenumber.PhoneNumber pn2 = null;
            while (shortMatch.find()) {
                Iterable<PhoneNumberMatch> matches2 = matches;
                CharSequence msgChar2 = msgChar;
                Pattern shortPattern3 = shortPattern2;
                try {
                    pn = util.parseAndKeepRawInput(shortMatch.group(), str2);
                } catch (NumberParseException e) {
                    NumberParseException numberParseException = e;
                    e.printStackTrace();
                    pn = pn2;
                }
                if (info.isPossibleShortNumberForRegion(pn, str2)) {
                    shortList.add(Integer.valueOf(shortMatch.start()));
                    shortList.add(Integer.valueOf(shortMatch.end()));
                }
                pn2 = pn;
                matches = matches2;
                msgChar = msgChar2;
                shortPattern2 = shortPattern3;
                String str3 = msg;
            }
            int short_size = shortList.isEmpty() ? 0 : shortList.size() / 2;
            Iterable<PhoneNumberMatch> iterable = matches;
            int[] result_short = new int[(2 * short_size)];
            CharSequence charSequence = msgChar;
            int z = 0;
            while (z < 2 * short_size) {
                result_short[z] = shortList.get(z).intValue();
                z++;
                String str4 = msg;
            }
            if (short_size == 0) {
                return result2;
            }
            if (result2[0] != 0) {
                int[] final_result = new int[((2 * (tnum + short_size)) + 1)];
                System.arraycopy(result2, 0, final_result, 0, result2.length);
                Pattern pattern = shortPattern2;
                System.arraycopy(result_short, 0, final_result, result2.length, result_short.length);
                final_result[0] = tnum + short_size;
                return final_result;
            }
            if (result2[0] == 0) {
                int[] final_result2 = new int[((2 * short_size) + 1)];
                final_result2[0] = short_size;
                System.arraycopy(result_short, 0, final_result2, 1, result_short.length);
                return final_result2;
            }
        }
        String src2 = convertQanChar(msg);
        String filteredString = handleNegativeRule(src2, this.phoneNumberRule);
        ShortNumberInfo instance = ShortNumberInfo.getInstance();
        CharSequence filteredStringC2 = filteredString;
        Iterable<PhoneNumberMatch> matchesList2 = util.findNumbers(filteredStringC2, str2, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        List<MatchedNumberInfo> ret = new ArrayList<>();
        for (PhoneNumberMatch match3 : matchesList2) {
            if (handleBorderRule(match3, filteredString, this.phoneNumberRule)) {
                PhoneNumberMatch delMatch = handleCodeRule(match3, src2, this.phoneNumberRule);
                if (delMatch != null) {
                    PhoneNumberMatch match4 = delMatch;
                    if (util.isValidNumber(match4.number())) {
                        MatchedNumberInfo info2 = new MatchedNumberInfo();
                        info2.setBegin(match4.start());
                        info2.setEnd(match4.end());
                        info2.setContent(match4.rawString());
                        if ("CN".equals(str2)) {
                            src = src2;
                            Pattern p = Pattern.compile("(?<![-\\d])\\d{5,6}[\\/|\\|]\\d{5,6}(?![-\\d])");
                            matchesList = matchesList2;
                            filteredStringC = filteredStringC2;
                            if (info2.getContent().startsWith("(") || info2.getContent().startsWith("[")) {
                                str = info2.getContent().substring(1);
                            } else {
                                str = info2.getContent();
                            }
                            if (!p.matcher(str).matches()) {
                                ret.add(info2);
                                src2 = src;
                                matchesList2 = matchesList;
                                filteredStringC2 = filteredStringC;
                                i2 = 2;
                            }
                        } else {
                            src = src2;
                            matchesList = matchesList2;
                            filteredStringC = filteredStringC2;
                            ret.add(info2);
                        }
                    } else {
                        src = src2;
                        matchesList = matchesList2;
                        filteredStringC = filteredStringC2;
                    }
                    List<MatchedNumberInfo> posList = handlePositiveRule(match4, filteredString, this.phoneNumberRule);
                    if (posList != null && !posList.isEmpty()) {
                        ret.addAll(posList);
                    }
                    src2 = src;
                    matchesList2 = matchesList;
                    filteredStringC2 = filteredStringC;
                    i2 = 2;
                }
            }
        }
        ret.addAll(this.phoneNumberRule.handleShortPhoneNumbers(filteredString, str2));
        List<MatchedNumberInfo> ret2 = deleteRepeatedInfo(ret);
        for (MatchedNumberInfo mi : ret2) {
            if (mi != null) {
                dealNumbersWithOneBracket(mi);
            }
        }
        int length = ret2.isEmpty() ? 0 : ret2.size();
        if (length == 0) {
            result = new int[]{0};
        } else {
            result = new int[((length * 2) + 1)];
            result[0] = length;
            for (int i4 = 0; i4 < length; i4++) {
                result[(i4 * 2) + 1] = ret2.get(i4).getBegin();
                result[(i4 * 2) + i2] = ret2.get(i4).getEnd();
            }
        }
        return result;
    }

    private PhoneNumberMatch handleCodeRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = this.phoneNumberRule.getCodesRules();
        boolean isValid = true;
        if (rules != null) {
            Iterator<PhoneNumberRule.RegexRule> it = rules.iterator();
            while (true) {
                if (it.hasNext()) {
                    match = it.next().isValid(match, msg);
                    if (match == null) {
                        isValid = false;
                        break;
                    }
                } else {
                    break;
                }
            }
        } else {
            isValid = true;
        }
        if (isValid) {
            return match;
        }
        return null;
    }

    private static boolean isNumbersWithOneBracket(String msg) {
        boolean hasRight = false;
        boolean hasLeft = false;
        if (msg != null) {
            for (int i = 0; i < msg.length(); i++) {
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
        for (int i = 0; i < instr.length(); i++) {
            String tempstr = instr.substring(i, i + 1);
            int index = "：／．＼∕，.！（）？﹡；：﹣—－【】－＋＝｛｝１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ".indexOf(tempstr);
            if (index == -1) {
                retsb.append(tempstr);
            } else {
                retsb.append(":/.\\/,.!()?*;:---[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(index, index + 1));
            }
        }
        return retsb.toString();
    }

    private static String handleNegativeRule(String src, PhoneNumberRule phoneNumberRule2) {
        String ret = src;
        for (PhoneNumberRule.RegexRule rule : phoneNumberRule2.getNegativeRules()) {
            Matcher m = rule.getPattern().matcher(ret);
            while (m.find()) {
                ret = replaceSpecifiedPos(ret.toCharArray(), m.start(), m.end());
            }
        }
        return ret;
    }

    private static List<MatchedNumberInfo> handlePositiveRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = phoneNumberRule2.getPositiveRules();
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        List<MatchedNumberInfo> ret = new ArrayList<>();
        String str = dealStringWithOneBracket(match.rawString());
        for (PhoneNumberRule.RegexRule rule : rules) {
            if (rule.getPattern().matcher(str).find()) {
                ret.addAll(rule.handle(match, msg));
                return ret;
            } else if (rule.getPattern().matcher(msg).find()) {
                List<MatchedNumberInfo> infoList = rule.handle(match, msg);
                if (infoList != null && !infoList.isEmpty()) {
                    ret.addAll(infoList);
                    return ret;
                }
            }
        }
        return null;
    }

    private static boolean handleBorderRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = phoneNumberRule2.getBorderRules();
        if (rules == null || rules.isEmpty()) {
            String str = msg;
            return true;
        }
        int begin = match.start();
        int end = match.end();
        int bStr = begin + -10 < 0 ? 0 : begin - 10;
        String s = msg.substring(bStr, end + 10 > msg.length() ? msg.length() : end + 10);
        for (PhoneNumberRule.RegexRule rule : rules) {
            Matcher mat = rule.getPattern().matcher(s);
            int type = rule.getType();
            while (true) {
                if (mat.find()) {
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
                    }
                    if (isdel) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static String replaceSpecifiedPos(char[] chs, int s, int e) {
        if (s > e) {
            return new String(chs);
        }
        for (int i = 0; i < chs.length; i++) {
            if (i >= s && i < e) {
                chs[i] = REPLACE_CHAR;
            }
        }
        return new String(chs);
    }
}
