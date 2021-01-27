package com.huawei.i18n.tmr.datetime;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.detect.RuleLevel;
import com.huawei.i18n.tmr.datetime.parse.DateConvert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private String locale;
    private RuleLevel ruleLevel;

    public enum FilterType {
        TYPE_NULL(-1),
        TYPE_DATETIME(0),
        TYPE_DATE(1),
        TYPE_TIME(2),
        TYPE_TIME_PERIOD(3),
        TYPE_PERIOD(4),
        TYPE_TODAY(5),
        TYPE_WEEK(6),
        TYPE_SEVEN(7);
        
        private int type;

        private FilterType(int type2) {
            this.type = type2;
        }

        public int getValue() {
            return this.type;
        }
    }

    public Filter(String locale2) {
        this.locale = locale2;
        this.ruleLevel = new RuleLevel(locale2);
    }

    /* access modifiers changed from: package-private */
    public List<Match> filterOverlay(List<Match> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        List<Match> matchList = filterOverlay2(filterOverlay1(matches));
        order(matchList);
        return matchList;
    }

    private List<Match> filterOverlay2(List<Match> matchList) {
        List<Match> matchList2 = new ArrayList<>();
        for (int i = 0; i < matchList.size(); i++) {
            Match match = matchList.get(i);
            int valid = 1;
            Iterator<Match> matchIterator = matchList2.iterator();
            while (matchIterator.hasNext()) {
                Match compareMatch = matchIterator.next();
                if (compareMatch.getBegin() > match.getBegin() && compareMatch.getEnd() < match.getEnd()) {
                    matchIterator.remove();
                } else if (compareMatch.getBegin() > match.getBegin() && compareMatch.getEnd() == match.getEnd()) {
                    matchIterator.remove();
                } else if (compareMatch.getBegin() == match.getBegin() && compareMatch.getEnd() < match.getEnd()) {
                    matchIterator.remove();
                } else if (compareMatch.getBegin() <= match.getBegin() && compareMatch.getEnd() >= match.getEnd()) {
                    valid = -1;
                } else if ((compareMatch.getBegin() < match.getBegin() && match.getBegin() < compareMatch.getEnd() && compareMatch.getEnd() < match.getEnd()) || (match.getBegin() < compareMatch.getBegin() && match.getEnd() > compareMatch.getBegin() && match.getEnd() < compareMatch.getEnd())) {
                    if (this.ruleLevel.compare(Integer.parseInt(compareMatch.getRegex()), Integer.parseInt(match.getRegex())) > -1) {
                        valid = -1;
                    } else {
                        matchIterator.remove();
                    }
                }
            }
            if (valid == 1) {
                matchList2.add(match);
            }
        }
        return matchList2;
    }

    private List<Match> filterOverlay1(List<Match> matchList) {
        List<Match> matchList1 = new ArrayList<>();
        for (int i = 0; i < matchList.size(); i++) {
            Match match = matchList.get(i);
            int valid = 1;
            Iterator<Match> matchIterator = matchList1.iterator();
            while (matchIterator.hasNext()) {
                Match compareMatch = matchIterator.next();
                if (compareMatch.getBegin() == match.getBegin() && compareMatch.getEnd() == match.getEnd()) {
                    if (this.ruleLevel.compare(Integer.parseInt(compareMatch.getRegex()), Integer.parseInt(match.getRegex())) > -1) {
                        valid = -1;
                    } else {
                        matchIterator.remove();
                    }
                } else if (compareMatch.getBegin() >= match.getBegin() || match.getBegin() >= compareMatch.getEnd() || compareMatch.getEnd() >= match.getEnd()) {
                    if (match.getBegin() < compareMatch.getBegin() && match.getEnd() > compareMatch.getBegin() && match.getEnd() < compareMatch.getEnd()) {
                        if (this.ruleLevel.compare(Integer.parseInt(compareMatch.getRegex()), Integer.parseInt(match.getRegex())) > -1) {
                            valid = -1;
                        } else {
                            matchIterator.remove();
                        }
                    }
                } else if (this.ruleLevel.compare(Integer.parseInt(compareMatch.getRegex()), Integer.parseInt(match.getRegex())) > -1) {
                    valid = -1;
                } else {
                    matchIterator.remove();
                }
            }
            if (valid == 1) {
                matchList1.add(match);
            }
        }
        return matchList1;
    }

    private void order(List<Match> matchList) {
        Collections.sort(matchList);
    }

    private List<Match> filterDate(String content, List<Match> matchList) {
        List<Match> result = new ArrayList<>();
        int i = 0;
        while (i < matchList.size()) {
            Match match = matchList.get(i);
            int type = getType(match.getRegex());
            match.setType(type);
            if (type == FilterType.TYPE_DATE.getValue() || type == FilterType.TYPE_TODAY.getValue() || type == FilterType.TYPE_WEEK.getValue() || type == FilterType.TYPE_SEVEN.getValue()) {
                int hasNum = (matchList.size() - 1) - i;
                List<Match> sub = null;
                if (hasNum > 1) {
                    sub = matchList.subList(i + 1, i + 3);
                } else if (hasNum == 1) {
                    sub = matchList.subList(i + 1, i + 2);
                }
                if (hasNum == 0 || sub == null) {
                    match.setType(FilterType.TYPE_DATE.getValue());
                    result.add(match);
                } else {
                    int status = new DateConvert(this.locale).nestDealDate(content, match, sub, -1);
                    match.setType(FilterType.TYPE_DATE.getValue());
                    if (status == DateConvert.DateCombine.NOT_COMBINE.getValue()) {
                        result.add(match);
                    } else {
                        if (status == DateConvert.DateCombine.TWO_COMBINE.getValue()) {
                            i++;
                        } else {
                            i += 2;
                        }
                        dealMatchE(content, matchList.get(i), match);
                        result.add(match);
                    }
                }
            } else {
                result.add(match);
            }
            i++;
        }
        return result;
    }

    private void dealMatchE(String content, Match nextMatch, Match match) {
        int end;
        int add = 0;
        int leftIndex = content.indexOf(40, match.getEnd());
        if (leftIndex != -1 && leftIndex < nextMatch.getBegin() && (end = content.indexOf(41, nextMatch.getEnd())) != -1 && ")".equals(content.substring(nextMatch.getEnd(), end + 1).trim())) {
            add = (end - nextMatch.getEnd()) + 1;
        }
        match.setEnd(nextMatch.getEnd() + add);
    }

    private List<Match> filterPeriod(String content, List<Match> matchList, String ps) {
        Pattern pattern = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + ps + ")\\s*", 2);
        Iterator<Match> matchIterator = matchList.iterator();
        Match curren = null;
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            if (curren == null) {
                curren = match;
            } else {
                int ctype = curren.getType();
                int ntype = match.getType();
                if ((ctype != ntype || (ctype != FilterType.TYPE_DATE.getValue() && ctype != FilterType.TYPE_TIME.getValue() && ctype != FilterType.TYPE_DATETIME.getValue())) && (ctype != FilterType.TYPE_DATETIME.getValue() || ntype != FilterType.TYPE_TIME.getValue())) {
                    curren = match;
                } else if (pattern.matcher(content.substring(curren.getEnd(), match.getBegin())).matches()) {
                    curren.setEnd(match.getEnd());
                    curren.setType(FilterType.TYPE_TIME_PERIOD.getValue());
                    if (ctype == FilterType.TYPE_TIME.getValue()) {
                        curren.setIsTimePeriod(true);
                    }
                    matchIterator.remove();
                } else {
                    curren = match;
                }
            }
        }
        return matchList;
    }

    private List<Match> filterDateTime(String content, List<Match> matchList, String dts) {
        boolean isJoiner;
        Pattern pattern = Pattern.compile("\\s*(at|às|،‏|،|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|मा|এ|ຂອງວັນທີ" + dts + ")\\s*", 2);
        Iterator<Match> matchIterator = matchList.iterator();
        Match lastMatch = null;
        while (matchIterator.hasNext()) {
            Match currentMatch = matchIterator.next();
            if (lastMatch == null) {
                lastMatch = currentMatch;
            } else {
                int lastType = lastMatch.getType();
                int currentType = currentMatch.getType();
                if ((lastType == FilterType.TYPE_DATE.getValue() && currentType == FilterType.TYPE_TIME.getValue()) || ((lastType == FilterType.TYPE_DATE.getValue() && currentType == 3 && currentMatch.isTimePeriod()) || ((lastType == FilterType.TYPE_TIME.getValue() && currentType == FilterType.TYPE_DATE.getValue()) || (lastType == 3 && lastMatch.isTimePeriod() && currentType == FilterType.TYPE_DATE.getValue())))) {
                    String joiner = content.substring(lastMatch.getEnd(), currentMatch.getBegin());
                    if (StorageManagerExt.INVALID_KEY_DESC.equals(joiner.trim())) {
                        isJoiner = true;
                    } else {
                        isJoiner = pattern.matcher(joiner).matches();
                    }
                    if (isJoiner) {
                        lastMatch.setEnd(currentMatch.getEnd());
                        if ((lastType == FilterType.TYPE_DATE.getValue() && currentType == FilterType.TYPE_TIME.getValue()) || lastType == FilterType.TYPE_TIME.getValue()) {
                            lastMatch.setType(FilterType.TYPE_DATETIME.getValue());
                        } else {
                            lastMatch.setType(FilterType.TYPE_TIME_PERIOD.getValue());
                        }
                        matchIterator.remove();
                    } else {
                        lastMatch = dealBrackets(content, matchIterator, lastMatch, currentMatch);
                    }
                } else {
                    lastMatch = currentMatch;
                }
            }
        }
        return matchList;
    }

    private Match dealBrackets(String content, Iterator<Match> matchIterator, Match last, Match current) {
        int lastType = last.getType();
        int currentType = current.getType();
        boolean isChange = true;
        if (lastType == FilterType.TYPE_TIME.getValue()) {
            int add = 0;
            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(last.getEnd()));
            String groupStr = null;
            boolean isBrackets = true;
            if (bcm.lookingAt()) {
                groupStr = bcm.group(1);
                add = bcm.group().length();
            }
            if (groupStr == null || !groupStr.trim().equals(content.substring(current.getBegin(), current.getEnd()).trim())) {
                isBrackets = false;
            }
            if (isBrackets) {
                last.setEnd(last.getEnd() + add);
                last.setType(FilterType.TYPE_DATETIME.getValue());
                matchIterator.remove();
                isChange = false;
            }
        }
        if (lastType == FilterType.TYPE_DATE.getValue() && currentType == FilterType.TYPE_TIME.getValue()) {
            String beginStr = content.substring(0, last.getBegin());
            String endStr = content.substring(last.getEnd(), current.getBegin());
            if (beginStr.trim().endsWith("(") && ")".equals(endStr.trim())) {
                last.setBegin(beginStr.lastIndexOf(40));
                last.setEnd(current.getEnd());
                last.setType(FilterType.TYPE_DATETIME.getValue());
                matchIterator.remove();
                isChange = false;
            }
        }
        return isChange ? current : last;
    }

    private List<Match> filterDateTimePunc(String content, List<Match> matchList) {
        Iterator<Match> matchIterator = matchList.iterator();
        Match curren = null;
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            if (curren == null) {
                curren = match;
            } else {
                int ctype = curren.getType();
                int type = match.getType();
                boolean isPunc = false;
                if ((ctype == FilterType.TYPE_DATE.getValue() && type == FilterType.TYPE_TIME.getValue()) || ((ctype == FilterType.TYPE_DATE.getValue() && type == FilterType.TYPE_TIME_PERIOD.getValue() && match.isTimePeriod()) || ((ctype == FilterType.TYPE_TIME.getValue() && type == FilterType.TYPE_DATE.getValue()) || (ctype == FilterType.TYPE_TIME_PERIOD.getValue() && curren.isTimePeriod() && type == FilterType.TYPE_DATE.getValue())))) {
                    String ss = content.substring(curren.getEnd(), match.getBegin());
                    if ("，".equals(ss.trim()) || ",".equals(ss.trim())) {
                        isPunc = true;
                    }
                    if (isPunc) {
                        curren.setEnd(match.getEnd());
                        if ((ctype == FilterType.TYPE_DATE.getValue() && type == FilterType.TYPE_TIME.getValue()) || ctype == FilterType.TYPE_TIME.getValue()) {
                            curren.setType(FilterType.TYPE_DATETIME.getValue());
                        } else {
                            curren.setType(FilterType.TYPE_TIME_PERIOD.getValue());
                        }
                        matchIterator.remove();
                    } else {
                        curren = match;
                    }
                } else {
                    curren = match;
                }
            }
        }
        return matchList;
    }

    private List<Match> filterDatePeriod(String content, List<Match> matches, String periodString, String dateTimeBridge) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterDate(content, matches), dateTimeBridge), periodString));
    }

    public static int getType(String name) {
        int key = Integer.parseInt(name);
        if (key <= 19999 || key >= 30000) {
            if (key > 29999 && key < 40000) {
                return FilterType.TYPE_TIME.getValue();
            }
            if (key <= 9999 || key >= 20000) {
                return FilterType.TYPE_TIME_PERIOD.getValue();
            }
            return FilterType.TYPE_DATETIME.getValue();
        } else if (key == 20009 || key == 20011 || key == 21026) {
            return FilterType.TYPE_WEEK.getValue();
        } else {
            if (key == 20010) {
                return FilterType.TYPE_TODAY.getValue();
            }
            return FilterType.TYPE_DATE.getValue();
        }
    }

    private List<Match> filterByRules(String content, List<Match> matchList, RuleInit ruleInit) {
        List<Match> clears = ruleInit.clear(content);
        if (clears == null || clears.isEmpty()) {
            return matchList;
        }
        Iterator<Match> matchIterator = matchList.iterator();
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            Iterator<Match> it = clears.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Match clearMatch = it.next();
                if (match.getBegin() >= clearMatch.getBegin() && match.getEnd() <= clearMatch.getEnd()) {
                    matchIterator.remove();
                    break;
                }
            }
        }
        return matchList;
    }

    private List<Match> filterByPast(String content, List<Match> matchList, RuleInit obj) {
        List<Match> past = obj.pastFind(content);
        if (past == null || past.isEmpty()) {
            return matchList;
        }
        for (Match matchPast : past) {
            Integer name = Integer.valueOf(matchPast.getRegex());
            Iterator<Match> matchIterator = matchList.iterator();
            while (true) {
                if (!matchIterator.hasNext()) {
                    break;
                }
                Match match = matchIterator.next();
                if (name.intValue() < 200) {
                    if (matchPast.getEnd() == match.getBegin()) {
                        matchIterator.remove();
                        break;
                    }
                } else if (matchPast.getBegin() == match.getEnd()) {
                    matchIterator.remove();
                    break;
                }
            }
        }
        return matchList;
    }

    /* access modifiers changed from: package-private */
    public List<Match> filter(String content, List<Match> matches, RuleInit ruleInit) {
        return filterByPast(content, filterByRules(content, filterDatePeriod(content, filterOverlay(matches), ruleInit.getPeriodString(), ruleInit.getDateTimeBridge()), ruleInit), ruleInit);
    }
}
