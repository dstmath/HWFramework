package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.detect.RuleLevel;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private String locale;
    private RuleLevel rlevel;

    public Filter(String locale2) {
        this.locale = locale2;
        this.rlevel = new RuleLevel(locale2);
    }

    public List<Match> filterOverlay(List<Match> ms) {
        if (ms == null) {
            return null;
        }
        List<Match> r2 = new ArrayList<>();
        for (int i = 0; i < ms.size(); i++) {
            Match c = ms.get(i);
            int valid = 1;
            Iterator<Match> it = r2.iterator();
            while (it.hasNext()) {
                Match cp = it.next();
                if (cp.getBegin() == c.getBegin() && cp.getEnd() == c.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) > -1) {
                        valid = -1;
                    } else {
                        it.remove();
                    }
                } else if (cp.getBegin() >= c.getBegin() || c.getBegin() >= cp.getEnd() || cp.getEnd() >= c.getEnd()) {
                    if (c.getBegin() < cp.getBegin() && c.getEnd() > cp.getBegin() && c.getEnd() < cp.getEnd()) {
                        if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) > -1) {
                            valid = -1;
                        } else {
                            it.remove();
                        }
                    }
                } else if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) > -1) {
                    valid = -1;
                } else {
                    it.remove();
                }
            }
            if (valid == 1) {
                r2.add(c);
            }
        }
        List<Match> r = new ArrayList<>();
        for (int i2 = 0; i2 < r2.size(); i2++) {
            Match c2 = r2.get(i2);
            int valid2 = 1;
            Iterator<Match> it2 = r.iterator();
            while (it2.hasNext()) {
                Match cp2 = it2.next();
                if (cp2.getBegin() > c2.getBegin() && cp2.getEnd() < c2.getEnd()) {
                    it2.remove();
                } else if (cp2.getBegin() > c2.getBegin() && cp2.getEnd() == c2.getEnd()) {
                    it2.remove();
                } else if (cp2.getBegin() == c2.getBegin() && cp2.getEnd() < c2.getEnd()) {
                    it2.remove();
                } else if (cp2.getBegin() <= c2.getBegin() && cp2.getEnd() >= c2.getEnd()) {
                    valid2 = -1;
                } else if (cp2.getBegin() >= c2.getBegin() || c2.getBegin() >= cp2.getEnd() || cp2.getEnd() >= c2.getEnd()) {
                    if (c2.getBegin() < cp2.getBegin() && c2.getEnd() > cp2.getBegin() && c2.getEnd() < cp2.getEnd()) {
                        if (this.rlevel.compare(Integer.parseInt(cp2.getRegex()), Integer.parseInt(c2.getRegex())) > -1) {
                            valid2 = -1;
                        } else {
                            it2.remove();
                        }
                    }
                } else if (this.rlevel.compare(Integer.parseInt(cp2.getRegex()), Integer.parseInt(c2.getRegex())) > -1) {
                    valid2 = -1;
                } else {
                    it2.remove();
                }
            }
            if (valid2 == 1) {
                r.add(c2);
            }
        }
        order(r);
        return r;
    }

    public void order(List<Match> ms) {
        Collections.sort(ms);
    }

    private int nestDealDate(String content, Match curren, List<Match> list, int pptype) {
        boolean z = false;
        Match next = list.get(0);
        int type = getType(next.getRegex());
        if (type != 1 && type != 5 && type != 6) {
            return 0;
        }
        if (type != curren.getType()) {
            if (type != pptype) {
                boolean isThree = false;
                String ss = content.substring(curren.getEnd(), next.getBegin());
                if (!LocaleParam.isRelDates(ss, this.locale) && !ss.trim().equals("(")) {
                    return 0;
                }
                if (list.size() > 1 && nestDealDate(content, next, list.subList(1, list.size()), curren.getType()) == 1) {
                    isThree = true;
                }
                boolean flag15 = false;
                if (ss.trim().equals("(")) {
                    Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                    String d = null;
                    if (bcm.lookingAt()) {
                        d = bcm.group(1);
                    }
                    int end = isThree ? list.get(1).getEnd() : next.getEnd();
                    if (d != null) {
                        z = d.trim().equals(content.substring(next.getBegin(), end).trim());
                    }
                    flag15 = z;
                }
                if (!LocaleParam.isRelDates(ss, this.locale) && !flag15) {
                    return 0;
                }
                if (isThree) {
                    return 2;
                }
                return 1;
            }
        }
        return 0;
    }

    private List<Match> filterDate(String content, List<Match> ms) {
        int i;
        int end;
        String str = content;
        List<Match> result = new ArrayList<>();
        int i2 = 0;
        while (i2 < ms.size()) {
            Match m = ms.get(i2);
            int type = getType(m.getRegex());
            m.setType(type);
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                int hasNum = (ms.size() - 1) - i2;
                List<Match> sub = null;
                if (hasNum > 1) {
                    sub = ms.subList(i2 + 1, i2 + 3);
                } else if (hasNum == 1) {
                    sub = ms.subList(i2 + 1, i2 + 2);
                }
                if (hasNum != 0) {
                    if (sub != null) {
                        int status = nestDealDate(str, m, sub, -1);
                        m.setType(1);
                        if (status == 0) {
                            result.add(m);
                            i = 1;
                        } else {
                            if (status == 1) {
                                i2++;
                            } else if (status == 2) {
                                i2 += 2;
                            }
                            Match e = ms.get(i2);
                            int add = 0;
                            int be = str.indexOf(40, m.getEnd());
                            if (be != -1 && be < e.getBegin() && (end = str.indexOf(41, e.getEnd())) != -1 && str.substring(e.getEnd(), end + 1).trim().equals(")")) {
                                add = (end - e.getEnd()) + 1;
                            }
                            m.setEnd(e.getEnd() + add);
                            result.add(m);
                            i = 1;
                        }
                    }
                }
                i = 1;
                m.setType(1);
                result.add(m);
            } else {
                result.add(m);
                i = 1;
            }
            i2 += i;
            str = content;
        }
        return result;
    }

    private List<Match> filterPeriod(String content, List<Match> ms, String ps) {
        Pattern rel = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + ps + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
                int ctype = curren.getType();
                int ntype = m.getType();
                if ((ctype != ntype || (ctype != 1 && ctype != 2 && ctype != 0)) && (ctype != 0 || ntype != 2)) {
                    curren = m;
                } else if (rel.matcher(content.substring(curren.getEnd(), m.getBegin())).matches()) {
                    curren.setEnd(m.getEnd());
                    curren.setType(3);
                    if (ctype == 2 && ntype == 2) {
                        curren.setIsTimePeriod(true);
                    }
                    it.remove();
                } else {
                    curren = m;
                }
            }
        }
        return ms;
    }

    private List<Match> filterDateTime(String content, List<Match> ms, String dts) {
        boolean flag;
        Pattern dt;
        int i;
        boolean flag15;
        int i2 = 2;
        Pattern dt2 = Pattern.compile("\\s*(at|às|،‏|،|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|मा|এ|ຂອງວັນທີ" + dts + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
                int ctype = curren.getType();
                int type = m.getType();
                if ((ctype == 1 && type == i2) || ((ctype == 1 && type == 3 && m.isTimePeriod()) || ((ctype == i2 && type == 1) || (ctype == 3 && curren.isTimePeriod() && type == 1)))) {
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals(BuildConfig.FLAVOR)) {
                        flag = true;
                    } else {
                        flag = dt2.matcher(ss).matches();
                    }
                    if (flag) {
                        curren.setEnd(m.getEnd());
                        if ((ctype == 1 && type == i2) || (ctype == i2 && type == 1)) {
                            curren.setType(0);
                        } else {
                            curren.setType(3);
                        }
                        it.remove();
                        dt = dt2;
                        i = i2;
                    } else {
                        boolean change = true;
                        if (ctype == i2 && type == 1) {
                            int add = 0;
                            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                            String d = null;
                            if (bcm.lookingAt()) {
                                d = bcm.group(1);
                                add = bcm.group().length();
                            }
                            if (d != null) {
                                dt = dt2;
                                flag15 = d.trim().equals(content.substring(m.getBegin(), m.getEnd()).trim());
                            } else {
                                dt = dt2;
                                flag15 = false;
                            }
                            if (flag15) {
                                curren.setEnd(curren.getEnd() + add);
                                curren.setType(0);
                                it.remove();
                                change = false;
                            }
                        } else {
                            dt = dt2;
                        }
                        if (ctype == 1) {
                            i = 2;
                            if (type == 2) {
                                String bStr = content.substring(0, curren.getBegin());
                                String eStr = content.substring(curren.getEnd(), m.getBegin());
                                if (bStr.trim().endsWith("(") && eStr.trim().equals(")")) {
                                    curren.setBegin(bStr.lastIndexOf(40));
                                    curren.setEnd(m.getEnd());
                                    curren.setType(0);
                                    it.remove();
                                    change = false;
                                }
                            }
                        } else {
                            i = 2;
                        }
                        if (change) {
                            curren = m;
                            i2 = i;
                            dt2 = dt;
                        }
                    }
                    i2 = i;
                    dt2 = dt;
                } else {
                    curren = m;
                    i2 = i2;
                    dt2 = dt2;
                }
            }
        }
        return ms;
    }

    private List<Match> filterDateTimePunc(String content, List<Match> ms) {
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
                int ctype = curren.getType();
                int type = m.getType();
                boolean flag = false;
                if ((ctype == 1 && type == 2) || ((ctype == 1 && type == 3 && m.isTimePeriod()) || ((ctype == 2 && type == 1) || (ctype == 3 && curren.isTimePeriod() && type == 1)))) {
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals("，") || ss.trim().equals(",")) {
                        flag = true;
                    }
                    if (flag) {
                        curren.setEnd(m.getEnd());
                        if ((ctype == 1 && type == 2) || (ctype == 2 && type == 1)) {
                            curren.setType(0);
                        } else {
                            curren.setType(3);
                        }
                        it.remove();
                    } else {
                        curren = m;
                    }
                } else {
                    curren = m;
                }
            }
        }
        return ms;
    }

    private List<Match> filterDatePeriod(String content, List<Match> ms, String ps, String dts) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterDate(content, ms), dts), ps));
    }

    public static int getType(String name) {
        int n = Integer.parseInt(name);
        if (n <= 19999 || n >= 30000) {
            if (n > 29999 && n < 40000) {
                return 2;
            }
            if (n <= 9999 || n >= 20000) {
                return 3;
            }
            return 0;
        } else if (n == 20009 || n == 20011 || n == 21026) {
            return 6;
        } else {
            if (n == 20010) {
                return 5;
            }
            return 1;
        }
    }

    private List<Match> filterByRules(String content, List<Match> ms, RuleInit obj) {
        List<Match> clears = obj.clear(content);
        if (clears == null || clears.isEmpty()) {
            return ms;
        }
        Iterator<Match> it = ms.iterator();
        while (it.hasNext()) {
            Match m = it.next();
            Iterator<Match> it2 = clears.iterator();
            while (true) {
                if (it2.hasNext()) {
                    Match t = it2.next();
                    if (m.getBegin() >= t.getBegin() && m.getEnd() <= t.getEnd()) {
                        it.remove();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return ms;
    }

    private List<Match> filterByPast(String content, List<Match> ms, RuleInit obj) {
        List<Match> past = obj.pastFind(content);
        if (past == null || past.isEmpty()) {
            return ms;
        }
        for (Match p : past) {
            Integer name = Integer.valueOf(p.getRegex());
            Iterator<Match> it = ms.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Match m = it.next();
                if (name.intValue() < 200) {
                    if (p.getEnd() == m.getBegin()) {
                        it.remove();
                        break;
                    }
                } else if (p.getBegin() == m.getEnd()) {
                    it.remove();
                    break;
                }
            }
        }
        return ms;
    }

    public List<Match> filter(String content, List<Match> ms, RuleInit obj) {
        return filterByPast(content, filterByRules(content, filterDatePeriod(content, filterOverlay(ms), obj.getPeriodString(), obj.getDTBridgeString()), obj), obj);
    }
}
