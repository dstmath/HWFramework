package com.huawei.g11n.tmr.phonenumber;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ES_ES;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_FR_FR;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_PT_PT;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN;
import com.huawei.uikit.effect.BuildConfig;
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

    @Override // com.huawei.g11n.tmr.phonenumber.AbstractPhoneNumberMatcher
    public int[] getMatchedPhoneNumber(String msg, String country) {
        PhoneNumberMatch delMatch;
        CharSequence filteredStringC;
        Iterable<PhoneNumberMatch> matchesList;
        String src;
        String str;
        int[] result;
        Pattern shortPattern;
        NumberParseException e;
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        int i = 1;
        int i2 = 0;
        if (!this.flag) {
            CharSequence msgChar = msg;
            PhoneNumberMatch<PhoneNumberMatch> match = util.findNumbers(msgChar, country, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
            List<String> list = new ArrayList<>();
            int y = 0;
            for (PhoneNumberMatch match2 : match) {
                list.add(match2.rawString());
                msgChar = msgChar;
                match = match;
                i = 1;
                i2 = 0;
            }
            int tnum = list.isEmpty() ? i2 : list.size();
            if (!list.isEmpty()) {
                int[] result2 = new int[((tnum * 2) + i)];
                for (PhoneNumberMatch match3 : match) {
                    result2[(y * 2) + i] = match3.start();
                    result2[(y * 2) + 2] = match3.end();
                    y++;
                }
                result2[i2] = tnum;
                result = result2;
            } else {
                int[] result3 = new int[i];
                result3[i2] = i2;
                result = result3;
            }
            List<Integer> shortList = new ArrayList<>();
            ShortNumberInfo info = ShortNumberInfo.getInstance();
            Pattern shortPattern2 = Pattern.compile("(?<!(\\d|\\*))\\d{2,8}(?!(\\d|\\*))");
            if (country.equals("CA")) {
                shortPattern = Pattern.compile("(?<!(\\d|\\*))\\d{5,8}(?!(\\d|\\*))");
            } else {
                shortPattern = shortPattern2;
            }
            Matcher shortMatch = shortPattern.matcher(msg);
            NumberParseException pn = null;
            while (shortMatch.find()) {
                try {
                    e = util.parseAndKeepRawInput(shortMatch.group(), country);
                } catch (NumberParseException e2) {
                    e2.printStackTrace();
                    e = pn;
                }
                if (info.isPossibleShortNumberForRegion(e, country)) {
                    shortList.add(Integer.valueOf(shortMatch.start()));
                    shortList.add(Integer.valueOf(shortMatch.end()));
                }
                pn = e;
                msgChar = msgChar;
                match = match;
                i2 = 0;
            }
            int short_size = shortList.isEmpty() ? i2 : shortList.size() / 2;
            int[] result_short = new int[(short_size * 2)];
            int z = 0;
            while (z < short_size * 2) {
                result_short[z] = shortList.get(z).intValue();
                z++;
                msgChar = msgChar;
            }
            if (short_size == 0) {
                return result;
            }
            if (result[0] != 0) {
                int[] final_result = new int[(((tnum + short_size) * 2) + 1)];
                System.arraycopy(result, 0, final_result, 0, result.length);
                System.arraycopy(result_short, 0, final_result, result.length, result_short.length);
                final_result[0] = tnum + short_size;
                return final_result;
            } else if (result[0] == 0) {
                int[] final_result2 = new int[((short_size * 2) + 1)];
                final_result2[0] = short_size;
                System.arraycopy(result_short, 0, final_result2, 1, result_short.length);
                return final_result2;
            }
        }
        String src2 = convertQanChar(msg);
        String filteredString = handleNegativeRule(src2, this.phoneNumberRule);
        ShortNumberInfo.getInstance();
        CharSequence filteredStringC2 = filteredString;
        Iterable<PhoneNumberMatch> matchesList2 = util.findNumbers(filteredStringC2, country, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        List<MatchedNumberInfo> ret = new ArrayList<>();
        for (PhoneNumberMatch match4 : matchesList2) {
            if (handleBorderRule(match4, filteredString, this.phoneNumberRule) && (delMatch = handleCodeRule(match4, src2, this.phoneNumberRule)) != null) {
                if (util.isValidNumber(delMatch.number())) {
                    MatchedNumberInfo info2 = new MatchedNumberInfo();
                    info2.setBegin(delMatch.start());
                    info2.setEnd(delMatch.end());
                    info2.setContent(delMatch.rawString());
                    if ("CN".equals(country)) {
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
                List<MatchedNumberInfo> posList = handlePositiveRule(delMatch, filteredString, this.phoneNumberRule);
                if (posList == null || posList.isEmpty()) {
                    src2 = src;
                    matchesList2 = matchesList;
                    filteredStringC2 = filteredStringC;
                } else {
                    ret.addAll(posList);
                    src2 = src;
                    matchesList2 = matchesList;
                    filteredStringC2 = filteredStringC;
                }
            }
        }
        ret.addAll(this.phoneNumberRule.handleShortPhoneNumbers(filteredString, country));
        List<MatchedNumberInfo> ret2 = deleteRepeatedInfo(ret);
        for (MatchedNumberInfo mi : ret2) {
            if (mi != null) {
                dealNumbersWithOneBracket(mi);
            }
        }
        int length = ret2.isEmpty() ? 0 : ret2.size();
        if (length == 0) {
            return new int[]{0};
        }
        int[] result4 = new int[((length * 2) + 1)];
        result4[0] = length;
        for (int i3 = 0; i3 < length; i3++) {
            result4[(i3 * 2) + 1] = ret2.get(i3).getBegin();
            result4[(i3 * 2) + 2] = ret2.get(i3).getEnd();
        }
        return result4;
    }

    private PhoneNumberMatch handleCodeRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = this.phoneNumberRule.getCodesRules();
        boolean isValid = true;
        if (rules != null) {
            Iterator<PhoneNumberRule.RegexRule> it = rules.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                match = it.next().isValid(match, msg);
                if (match == null) {
                    isValid = false;
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
        if (msg == null) {
            return false;
        }
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
        if (!hasLeft || hasRight) {
            return false;
        }
        return true;
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
        StringBuffer retsb = new StringBuffer(BuildConfig.FLAVOR);
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
        List<MatchedNumberInfo> infoList;
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
            } else if (rule.getPattern().matcher(msg).find() && (infoList = rule.handle(match, msg)) != null && !infoList.isEmpty()) {
                ret.addAll(infoList);
                return ret;
            }
        }
        return null;
    }

    private static boolean handleBorderRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = phoneNumberRule2.getBorderRules();
        boolean z = true;
        if (rules == null) {
            return true;
        }
        if (rules.isEmpty()) {
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
                    if (type != 8) {
                        if (type == 9 && b <= begin && end <= e) {
                            isdel = true;
                        }
                    } else if ((b <= begin && end <= e) || ((b < begin && begin < e && e < end) || (begin < b && b < end && end < e))) {
                        isdel = true;
                    }
                    if (isdel) {
                        return false;
                    }
                    z = true;
                }
            }
        }
        return z;
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
