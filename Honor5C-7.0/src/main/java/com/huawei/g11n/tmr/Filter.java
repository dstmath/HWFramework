package com.huawei.g11n.tmr;

import com.huawei.connectivitylog.ConnectivityLogManager;
import com.huawei.g11n.tmr.datetime.detect.RuleLevel;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private String locale;
    private RuleLevel rlevel;

    public Filter(String str) {
        this.locale = str;
        this.rlevel = new RuleLevel(str);
    }

    public List<Match> filterOverlay(List<Match> list) {
        int i = 0;
        if (list == null) {
            return null;
        }
        List arrayList = new ArrayList();
        for (int i2 = 0; i2 < list.size(); i2++) {
            Match match = (Match) list.get(i2);
            Iterator it = arrayList.iterator();
            Object obj = 1;
            while (it.hasNext()) {
                Match match2 = (Match) it.next();
                if (match2.getBegin() == match.getBegin() && match2.getEnd() == match.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(match2.getRegex()), Integer.parseInt(match.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        obj = -1;
                    }
                } else if (match2.getBegin() < match.getBegin() && match.getBegin() < match2.getEnd() && match2.getEnd() < match.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(match2.getRegex()), Integer.parseInt(match.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        obj = -1;
                    }
                } else if (match.getBegin() < match2.getBegin() && match.getEnd() > match2.getBegin() && match.getEnd() < match2.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(match2.getRegex()), Integer.parseInt(match.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        obj = -1;
                    }
                }
            }
            if (obj == 1) {
                arrayList.add(match);
            }
        }
        List<Match> arrayList2 = new ArrayList();
        while (i < arrayList.size()) {
            match = (Match) arrayList.get(i);
            it = arrayList2.iterator();
            obj = 1;
            while (it.hasNext()) {
                match2 = (Match) it.next();
                if (match2.getBegin() > match.getBegin() && match2.getEnd() < match.getEnd()) {
                    it.remove();
                } else if (match2.getBegin() > match.getBegin() && match2.getEnd() == match.getEnd()) {
                    it.remove();
                } else if (match2.getBegin() == match.getBegin() && match2.getEnd() < match.getEnd()) {
                    it.remove();
                } else if (match2.getBegin() <= match.getBegin() && match2.getEnd() >= match.getEnd()) {
                    obj = -1;
                } else if (match2.getBegin() < match.getBegin() && match.getBegin() < match2.getEnd() && match2.getEnd() < match.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(match2.getRegex()), Integer.parseInt(match.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        obj = -1;
                    }
                } else if (match.getBegin() < match2.getBegin() && match.getEnd() > match2.getBegin() && match.getEnd() < match2.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(match2.getRegex()), Integer.parseInt(match.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        obj = -1;
                    }
                }
            }
            if (obj == 1) {
                arrayList2.add(match);
            }
            i++;
        }
        order(arrayList2);
        return arrayList2;
    }

    public void order(List<Match> list) {
        Collections.sort(list);
    }

    private int nestDealDate(String str, Match match, List<Match> list, int i) {
        int i2 = 0;
        Match match2 = (Match) list.get(0);
        int type = getType(match2.getRegex());
        if ((type != 1 && type != 5 && type != 6) || type == match.getType() || type == i) {
            return 0;
        }
        String substring = str.substring(match.getEnd(), match2.getBegin());
        if (LocaleParam.isRelDates(substring, this.locale) || substring.trim().equals("(")) {
            type = list.size() <= 1 ? 0 : nestDealDate(str, match2, list.subList(1, list.size()), match.getType()) != 1 ? 0 : 1;
            boolean equals;
            if (substring.trim().equals("(")) {
                String group;
                Matcher matcher = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(match.getEnd()));
                if (matcher.lookingAt()) {
                    group = matcher.group(1);
                } else {
                    group = null;
                }
                equals = group == null ? false : group.trim().equals(str.substring(match2.getBegin(), type == 0 ? match2.getEnd() : ((Match) list.get(1)).getEnd()).trim());
            } else {
                equals = false;
            }
            if (LocaleParam.isRelDates(substring, this.locale) || r0) {
                i2 = type == 0 ? 1 : 2;
            }
        }
        return i2;
    }

    private List<Match> filterDate(String str, List<Match> list) {
        List<Match> arrayList = new ArrayList();
        int i = 0;
        while (i < list.size()) {
            Match match = (Match) list.get(i);
            int type = getType(match.getRegex());
            match.setType(type);
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                List subList;
                int size = (list.size() - 1) - i;
                if (size > 1) {
                    subList = list.subList(i + 1, i + 3);
                } else if (size != 1) {
                    subList = null;
                } else {
                    subList = list.subList(i + 1, i + 2);
                }
                if (size == 0 || subList == null) {
                    match.setType(1);
                    arrayList.add(match);
                } else {
                    type = nestDealDate(str, match, subList, -1);
                    match.setType(1);
                    if (type != 0) {
                        Match match2;
                        if (type == 1) {
                            i++;
                        } else if (type == 2) {
                            type = i + 2;
                            match2 = (Match) list.get(type);
                            size = str.indexOf(40, match.getEnd());
                            if (size != -1 && size < match2.getBegin()) {
                                size = str.indexOf(41, match2.getEnd());
                                if (size == -1) {
                                    size = 0;
                                } else if (str.substring(match2.getEnd(), size + 1).trim().equals(")")) {
                                    size = 0;
                                } else {
                                    size = (size - match2.getEnd()) + 1;
                                }
                            } else {
                                size = 0;
                            }
                            match.setEnd(match2.getEnd() + size);
                            arrayList.add(match);
                            i = type;
                        }
                        type = i;
                        match2 = (Match) list.get(type);
                        size = str.indexOf(40, match.getEnd());
                        if (size != -1) {
                            size = str.indexOf(41, match2.getEnd());
                            if (size == -1) {
                                size = 0;
                            } else if (str.substring(match2.getEnd(), size + 1).trim().equals(")")) {
                                size = (size - match2.getEnd()) + 1;
                            } else {
                                size = 0;
                            }
                            match.setEnd(match2.getEnd() + size);
                            arrayList.add(match);
                            i = type;
                        }
                        size = 0;
                        match.setEnd(match2.getEnd() + size);
                        arrayList.add(match);
                        i = type;
                    } else {
                        arrayList.add(match);
                    }
                }
            } else {
                arrayList.add(match);
            }
            i++;
        }
        return arrayList;
    }

    private List<Match> filterPeriod(String str, List<Match> list, String str2) {
        Pattern compile = Pattern.compile("\\.?\\s*(-{1,2}|~|\u8d77?\u81f3|\u5230|au|\u2013|\u2014|\uff5e|\u062a\u0627|\u0926\u0947\u0916\u093f|\u0434\u0430|\u043f\u0430|\u09a5\u09c7\u0995\u09c7|\u0eab\u0eb2" + str2 + ")\\s*", 2);
        Iterator it = list.iterator();
        Match match = null;
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (match != null) {
                int type = match.getType();
                int type2 = match2.getType();
                if (type == type2) {
                    if (!(type == 1 || type == 2)) {
                        if (type == 0) {
                        }
                    }
                    if (compile.matcher(str.substring(match.getEnd(), match2.getBegin())).matches()) {
                        match = match2;
                    } else {
                        match.setEnd(match2.getEnd());
                        match.setType(3);
                        if (type == 2 && type2 == 2) {
                            match.setIsTimePeriod(true);
                        }
                        it.remove();
                    }
                }
                if (type == 0) {
                    if (type2 != 2) {
                    }
                    if (compile.matcher(str.substring(match.getEnd(), match2.getBegin())).matches()) {
                        match.setEnd(match2.getEnd());
                        match.setType(3);
                        match.setIsTimePeriod(true);
                        it.remove();
                    } else {
                        match = match2;
                    }
                }
                match = match2;
            } else {
                match = match2;
            }
        }
        return list;
    }

    private List<Match> filterDateTime(String str, List<Match> list, String str2) {
        Pattern compile = Pattern.compile("\\s*(at|\u00e0s|\u060c\u200f|,|u|kl\\.|\u0915\u094b|\u7684|o|\u00e0|a\\s+les|ve|la|pada|kl|\u03c3\u03c4\u03b9\u03c2|alle|jam|\u092e\u093e|\u098f|\u0e82\u0ead\u0e87\u0ea7\u0eb1\u0e99\u0e97\u0eb5" + str2 + ")\\s*", 2);
        Iterator it = list.iterator();
        Match match = null;
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (match != null) {
                boolean z;
                int type = match.getType();
                int type2 = match2.getType();
                if (!(type == 1 && type2 == 2)) {
                    if (type != 1 || type2 != 3 || !match2.isTimePeriod()) {
                        if (type != 2 || type2 != 1) {
                            if (type == 3 && match.isTimePeriod()) {
                                if (type2 != 1) {
                                }
                            }
                            match = match2;
                        }
                    }
                }
                CharSequence substring = str.substring(match.getEnd(), match2.getBegin());
                if (substring.trim().equals("")) {
                    z = true;
                } else {
                    z = compile.matcher(substring).matches();
                }
                if (z) {
                    match.setEnd(match2.getEnd());
                    if (!(type == 1 && type2 == 2)) {
                        if (type == 2) {
                            if (type2 != 1) {
                            }
                        }
                        match.setType(3);
                        it.remove();
                    }
                    match.setType(0);
                    it.remove();
                } else {
                    Object obj;
                    if (type == 2 && type2 == 1) {
                        int i = 0;
                        Matcher matcher = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(match.getEnd()));
                        String str3 = null;
                        if (matcher.lookingAt()) {
                            str3 = matcher.group(1);
                            i = matcher.group().length();
                        }
                        if (str3 == null ? false : str3.trim().equals(str.substring(match2.getBegin(), match2.getEnd()).trim())) {
                            match.setEnd(match.getEnd() + i);
                            match.setType(0);
                            it.remove();
                            obj = null;
                        } else {
                            int i2 = 1;
                        }
                    } else {
                        obj = 1;
                    }
                    if (type == 1 && type2 == 2) {
                        String substring2 = str.substring(0, match.getBegin());
                        String substring3 = str.substring(match.getEnd(), match2.getBegin());
                        if (substring2.trim().endsWith("(") && substring3.trim().equals(")")) {
                            match.setBegin(substring2.lastIndexOf(40));
                            match.setEnd(match2.getEnd());
                            match.setType(0);
                            it.remove();
                            obj = null;
                        }
                    }
                    if (obj != null) {
                        match = match2;
                    }
                }
            } else {
                match = match2;
            }
        }
        return list;
    }

    private List<Match> filterDateTimePunc(String str, List<Match> list) {
        Iterator it = list.iterator();
        Match match = null;
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (match != null) {
                int type = match.getType();
                int type2 = match2.getType();
                if (!(type == 1 && type2 == 2)) {
                    if (type != 1 || type2 != 3 || !match2.isTimePeriod()) {
                        if (type != 2 || type2 != 1) {
                            if (type == 3 && match.isTimePeriod()) {
                                if (type2 != 1) {
                                }
                            }
                            match = match2;
                        }
                    }
                }
                String substring = str.substring(match.getEnd(), match2.getBegin());
                int i = (substring.trim().equals("\uff0c") || substring.trim().equals(",")) ? 1 : 0;
                if (i == 0) {
                    match = match2;
                } else {
                    match.setEnd(match2.getEnd());
                    if (!(type == 1 && type2 == 2)) {
                        if (type == 2) {
                            if (type2 != 1) {
                            }
                        }
                        match.setType(3);
                        it.remove();
                    }
                    match.setType(0);
                    it.remove();
                }
            } else {
                match = match2;
            }
        }
        return list;
    }

    private List<Match> filterDatePeriod(String str, List<Match> list, String str2, String str3) {
        return filterDateTimePunc(str, filterPeriod(str, filterDateTime(str, filterDate(str, list), str3), str2));
    }

    public static int getType(String str) {
        int parseInt = Integer.parseInt(str);
        if (parseInt > 19999 && parseInt < 30000) {
            if (parseInt == 20009 || parseInt == 20011 || parseInt == 21026) {
                return 6;
            }
            if (parseInt != 20010) {
                return 1;
            }
            return 5;
        } else if (parseInt > 29999 && parseInt < 40000) {
            return 2;
        } else {
            return (parseInt > 9999 && parseInt < 20000) ? 0 : 3;
        }
    }

    private List<Match> filterByRules(String str, List<Match> list, RuleInit ruleInit) {
        List<Match> clear = ruleInit.clear(str);
        if (clear == null || clear.isEmpty()) {
            return list;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Match match = (Match) it.next();
            for (Match match2 : clear) {
                if (match.getBegin() >= match2.getBegin() && match.getEnd() <= match2.getEnd()) {
                    it.remove();
                    break;
                }
            }
        }
        return list;
    }

    private List<Match> filterByPast(String str, List<Match> list, RuleInit ruleInit) {
        List<Match> pastFind = ruleInit.pastFind(str);
        if (pastFind == null || pastFind.isEmpty()) {
            return list;
        }
        for (Match match : pastFind) {
            Integer valueOf = Integer.valueOf(match.getRegex());
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Match match2 = (Match) it.next();
                if (valueOf.intValue() >= ConnectivityLogManager.WIFI_HAL_DRIVER_DEVICE_EXCEPTION) {
                    if (match.getBegin() == match2.getEnd()) {
                        it.remove();
                        break;
                    }
                } else if (match.getEnd() == match2.getBegin()) {
                    it.remove();
                    break;
                }
            }
        }
        return list;
    }

    public List<Match> filter(String str, List<Match> list, RuleInit ruleInit) {
        return filterByPast(str, filterByRules(str, filterDatePeriod(str, filterOverlay(list), ruleInit.getPeriodString(), ruleInit.getDTBridgeString()), ruleInit), ruleInit);
    }
}
