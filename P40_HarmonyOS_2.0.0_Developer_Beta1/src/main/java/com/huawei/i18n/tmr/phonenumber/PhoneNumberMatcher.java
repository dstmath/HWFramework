package com.huawei.i18n.tmr.phonenumber;

import android.util.Log;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.data.ChinaPhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.FrancePhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.GermanyPhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.ItalyPhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.PortugalPhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.SpainPhoneNumberRule;
import com.huawei.i18n.tmr.phonenumber.data.UKPhoneNumberRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberMatcher extends AbstractPhoneNumberMatcher {
    private static final char REPLACE_CHAR = 'A';
    private static final String TAG = "PhoneNumberMatcher";
    private boolean isFixed = true;
    private PhoneNumberRule phoneNumberRule;

    PhoneNumberMatcher(String country) {
        super(country);
        if ("CN".equals(country)) {
            this.phoneNumberRule = new ChinaPhoneNumberRule();
        } else if ("GB".equals(country) || "UK".equals(country)) {
            this.phoneNumberRule = new UKPhoneNumberRule();
        } else if ("DE".equals(country)) {
            this.phoneNumberRule = new GermanyPhoneNumberRule();
        } else if ("IT".equals(country)) {
            this.phoneNumberRule = new ItalyPhoneNumberRule();
        } else if ("FR".equals(country)) {
            this.phoneNumberRule = new FrancePhoneNumberRule();
        } else if ("ES".equals(country)) {
            this.phoneNumberRule = new SpainPhoneNumberRule();
        } else if ("PT".equals(country)) {
            this.phoneNumberRule = new PortugalPhoneNumberRule();
        } else {
            this.isFixed = false;
            this.phoneNumberRule = null;
        }
        PhoneNumberRule phoneNumberRule2 = this.phoneNumberRule;
        if (phoneNumberRule2 != null) {
            phoneNumberRule2.init();
        }
    }

    private List<MatchedNumberInfo> deleteRepeatedInfo(List<MatchedNumberInfo> list) {
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

    @Override // com.huawei.i18n.tmr.phonenumber.AbstractPhoneNumberMatcher
    public int[] getMatchedPhoneNumber(String msg, String country) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        if (!this.isFixed) {
            return dealWithGoogle(msg, country, phoneNumberUtil);
        }
        String convertQanStr = convertQanChar(msg);
        String filteredString = handleNegativeRule(convertQanStr, this.phoneNumberRule);
        List<MatchedNumberInfo> matchedNumberInfoList = getPossibleNumberInfos(country, phoneNumberUtil, convertQanStr, filteredString);
        matchedNumberInfoList.addAll(this.phoneNumberRule.handleShortPhoneNumbers(filteredString, country));
        List<MatchedNumberInfo> matchedNumberInfoList2 = deleteRepeatedInfo(matchedNumberInfoList);
        for (MatchedNumberInfo matchedNumberInfo : matchedNumberInfoList2) {
            if (matchedNumberInfo != null) {
                dealNumbersWithOneBracket(matchedNumberInfo);
            }
        }
        return dealResult(matchedNumberInfoList2);
    }

    private int[] dealResult(List<MatchedNumberInfo> matchedNumberInfoList) {
        int length = matchedNumberInfoList.isEmpty() ? 0 : matchedNumberInfoList.size();
        if (length == 0) {
            return new int[]{0};
        }
        int[] result = new int[((length * 2) + 1)];
        result[0] = length;
        for (int i = 0; i < length; i++) {
            result[(i * 2) + 1] = matchedNumberInfoList.get(i).getBegin();
            result[(i * 2) + 2] = matchedNumberInfoList.get(i).getEnd();
        }
        return result;
    }

    private List<MatchedNumberInfo> getPossibleNumberInfos(String country, PhoneNumberUtil util, String src, String filteredString) {
        Iterable<PhoneNumberMatch> matchesList;
        String str;
        Iterable<PhoneNumberMatch> matchesList2 = util.findNumbers(filteredString, country, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        List<MatchedNumberInfo> result = new ArrayList<>();
        for (PhoneNumberMatch match : matchesList2) {
            if (handleBorderRule(match, filteredString, this.phoneNumberRule)) {
                Optional<PhoneNumberMatch> optionDelMatch = handleCodeRule(match, src, this.phoneNumberRule);
                if (optionDelMatch.isPresent()) {
                    PhoneNumberMatch match2 = optionDelMatch.get();
                    if (util.isValidNumber(match2.number())) {
                        MatchedNumberInfo info = new MatchedNumberInfo();
                        info.setBegin(match2.start());
                        info.setEnd(match2.end());
                        info.setContent(match2.rawString());
                        if ("CN".equals(country)) {
                            Pattern pattern = Pattern.compile("(?<![-\\d])\\d{5,6}[\\/|\\|]\\d{5,6}(?![-\\d])");
                            matchesList = matchesList2;
                            if (info.getContent().startsWith("(") || info.getContent().startsWith("[")) {
                                str = info.getContent().substring(1);
                            } else {
                                str = info.getContent();
                            }
                            if (!pattern.matcher(str).matches()) {
                                result.add(info);
                                matchesList2 = matchesList;
                            }
                        } else {
                            matchesList = matchesList2;
                            result.add(info);
                        }
                    } else {
                        matchesList = matchesList2;
                    }
                    List<MatchedNumberInfo> posList = handlePositiveRule(match2, filteredString, this.phoneNumberRule);
                    if (posList != null && !posList.isEmpty()) {
                        result.addAll(posList);
                    }
                    matchesList2 = matchesList;
                }
            }
        }
        return result;
    }

    private int[] dealWithGoogle(String msg, String country, PhoneNumberUtil util) {
        int[] result;
        Iterable<PhoneNumberMatch> matches = util.findNumbers(msg, country, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        List<String> list = new ArrayList<>();
        int i = 0;
        for (PhoneNumberMatch match : matches) {
            list.add(match.rawString());
        }
        int telNum = list.isEmpty() ? 0 : list.size();
        if (!list.isEmpty()) {
            result = new int[((telNum * 2) + 1)];
            for (PhoneNumberMatch match2 : matches) {
                result[(i * 2) + 1] = match2.start();
                result[(i * 2) + 2] = match2.end();
                i++;
            }
            result[0] = telNum;
        } else {
            result = new int[]{0};
        }
        return recongShortNumber(msg, country, util, result, telNum);
    }

    private int[] recongShortNumber(String msg, String country, PhoneNumberUtil util, int[] result, int tnum) {
        Pattern shortPattern;
        Pattern shortPattern2 = Pattern.compile("(?<!(\\d|\\*))\\d{2,8}(?!(\\d|\\*))");
        if ("CA".equals(country)) {
            shortPattern = Pattern.compile("(?<!(\\d|\\*))\\d{5,8}(?!(\\d|\\*))");
        } else {
            shortPattern = shortPattern2;
        }
        Matcher shortMatch = shortPattern.matcher(msg);
        List<Integer> shortList = new ArrayList<>();
        ShortNumberInfo info = ShortNumberInfo.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        while (shortMatch.find()) {
            try {
                phoneNumber = util.parseAndKeepRawInput(shortMatch.group(), country);
            } catch (NumberParseException e) {
                Log.e(TAG, "recongShortNumber NumberParseException");
            }
            if (phoneNumber != null && info.isPossibleShortNumberForRegion(phoneNumber, country)) {
                shortList.add(Integer.valueOf(shortMatch.start()));
                shortList.add(Integer.valueOf(shortMatch.end()));
            }
        }
        int shortSize = shortList.isEmpty() ? 0 : shortList.size() / 2;
        int[] resultShort = new int[(shortSize * 2)];
        for (int i = 0; i < shortSize * 2; i++) {
            resultShort[i] = shortList.get(i).intValue();
        }
        if (shortSize == 0) {
            return result;
        }
        if (result[0] != 0) {
            int[] finalResult = new int[(((tnum + shortSize) * 2) + 1)];
            System.arraycopy(result, 0, finalResult, 0, result.length);
            System.arraycopy(resultShort, 0, finalResult, result.length, resultShort.length);
            finalResult[0] = tnum + shortSize;
            return finalResult;
        }
        int[] finalResult2 = new int[((shortSize * 2) + 1)];
        finalResult2[0] = shortSize;
        System.arraycopy(resultShort, 0, finalResult2, 1, resultShort.length);
        return finalResult2;
    }

    private Optional<PhoneNumberMatch> handleCodeRule(PhoneNumberMatch phoneNumberMatch, String msg, PhoneNumberRule phoneNumberRule2) {
        PhoneNumberMatch match = phoneNumberMatch;
        List<PhoneNumberRule.RegexRule> rules = this.phoneNumberRule.getCodesRules();
        boolean isValid = true;
        if (rules != null) {
            Iterator<PhoneNumberRule.RegexRule> it = rules.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Optional<PhoneNumberMatch> optionalMatch = it.next().isValid(match, msg);
                if (!optionalMatch.isPresent()) {
                    isValid = false;
                    break;
                }
                match = optionalMatch.get();
            }
        }
        if (isValid) {
            return Optional.ofNullable(match);
        }
        return Optional.empty();
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
        StringBuffer result = new StringBuffer(StorageManagerExt.INVALID_KEY_DESC);
        for (int i = 0; i < instr.length(); i++) {
            String tempstr = instr.substring(i, i + 1);
            int index = "：／．＼∕，.！（）？﹡；：﹣—－【】－＋＝｛｝％１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ".indexOf(tempstr);
            if (index == -1) {
                result.append(tempstr);
            } else {
                result.append(":/.\\/,.!()?*;:---[]-+={}%1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(index, index + 1));
            }
        }
        return result.toString();
    }

    private static String handleNegativeRule(String src, PhoneNumberRule phoneNumberRule2) {
        String ret = src;
        for (PhoneNumberRule.RegexRule rule : phoneNumberRule2.getNegativeRules()) {
            Matcher matcher = rule.getPattern().matcher(ret);
            while (matcher.find()) {
                ret = replaceSpecifiedPos(ret.toCharArray(), matcher.start(), matcher.end());
            }
        }
        return ret;
    }

    private static List<MatchedNumberInfo> handlePositiveRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<MatchedNumberInfo> infoList;
        List<PhoneNumberRule.RegexRule> rules = phoneNumberRule2.getPositiveRules();
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
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
        return Collections.emptyList();
    }

    private static boolean handleBorderRule(PhoneNumberMatch match, String msg, PhoneNumberRule phoneNumberRule2) {
        List<PhoneNumberRule.RegexRule> rules = phoneNumberRule2.getBorderRules();
        if (rules == null) {
            return true;
        }
        if (rules.isEmpty()) {
            return true;
        }
        int begin = match.start();
        int end = match.end();
        int beginSubTen = begin + -10 < 0 ? 0 : begin - 10;
        String borderStr = msg.substring(beginSubTen, end + 10 > msg.length() ? msg.length() : end + 10);
        for (PhoneNumberRule.RegexRule rule : rules) {
            Matcher mat = rule.getPattern().matcher(borderStr);
            int type = rule.getType();
            while (true) {
                if (mat.find()) {
                    int borderBegin = mat.start() + beginSubTen;
                    int borderEnd = mat.end() + beginSubTen;
                    boolean isDel = false;
                    if (type != 8) {
                        if (type == 9 && borderBegin <= begin && end <= borderEnd) {
                            isDel = true;
                            continue;
                        }
                    } else if ((borderBegin <= begin && end <= borderEnd) || ((borderBegin < begin && begin < borderEnd && borderEnd < end) || (begin < borderBegin && borderBegin < end && end < borderEnd))) {
                        isDel = true;
                        continue;
                    }
                    if (isDel) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static String replaceSpecifiedPos(char[] chs, int start, int end) {
        if (start > end) {
            return new String(chs);
        }
        for (int i = 0; i < chs.length; i++) {
            if (i >= start && i < end) {
                chs[i] = REPLACE_CHAR;
            }
        }
        return new String(chs);
    }
}
