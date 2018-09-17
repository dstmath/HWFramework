package com.huawei.g11n.tmr;

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

    public Filter(String locale) {
        this.locale = locale;
        this.rlevel = new RuleLevel(locale);
    }

    public List<Match> filterOverlay(List<Match> ms) {
        if (ms == null) {
            return null;
        }
        int i;
        Match c;
        int valid;
        Iterator<Match> it;
        Match cp;
        List<Match> r2 = new ArrayList();
        for (i = 0; i < ms.size(); i++) {
            c = (Match) ms.get(i);
            valid = 1;
            it = r2.iterator();
            while (it.hasNext()) {
                cp = (Match) it.next();
                if (cp.getBegin() == c.getBegin() && cp.getEnd() == c.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        valid = -1;
                    }
                } else if (cp.getBegin() < c.getBegin() && c.getBegin() < cp.getEnd() && cp.getEnd() < c.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        valid = -1;
                    }
                } else if (c.getBegin() < cp.getBegin() && c.getEnd() > cp.getBegin() && c.getEnd() < cp.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        valid = -1;
                    }
                }
            }
            if (valid == 1) {
                r2.add(c);
            }
        }
        ms = r2;
        List<Match> r = new ArrayList();
        for (i = 0; i < r2.size(); i++) {
            c = (Match) r2.get(i);
            valid = 1;
            it = r.iterator();
            while (it.hasNext()) {
                cp = (Match) it.next();
                if (cp.getBegin() > c.getBegin() && cp.getEnd() < c.getEnd()) {
                    it.remove();
                } else if (cp.getBegin() > c.getBegin() && cp.getEnd() == c.getEnd()) {
                    it.remove();
                } else if (cp.getBegin() == c.getBegin() && cp.getEnd() < c.getEnd()) {
                    it.remove();
                } else if (cp.getBegin() <= c.getBegin() && cp.getEnd() >= c.getEnd()) {
                    valid = -1;
                } else if (cp.getBegin() < c.getBegin() && c.getBegin() < cp.getEnd() && cp.getEnd() < c.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        valid = -1;
                    }
                } else if (c.getBegin() < cp.getBegin() && c.getEnd() > cp.getBegin() && c.getEnd() < cp.getEnd()) {
                    if (this.rlevel.compare(Integer.parseInt(cp.getRegex()), Integer.parseInt(c.getRegex())) <= -1) {
                        it.remove();
                    } else {
                        valid = -1;
                    }
                }
            }
            if (valid == 1) {
                r.add(c);
            }
        }
        order(r);
        return r;
    }

    public void order(List<Match> ms) {
        Collections.sort(ms);
    }

    private int nestDealDate(String content, Match curren, List<Match> list, int pptype) {
        int result = 0;
        Match next = (Match) list.get(0);
        int type = getType(next.getRegex());
        if (type != 1 && type != 5 && type != 6) {
            return 0;
        }
        if (type == curren.getType() || type == pptype) {
            return 0;
        }
        boolean isThree = false;
        String ss = content.substring(curren.getEnd(), next.getBegin());
        if (LocaleParam.isRelDates(ss, this.locale) || ss.trim().equals("(")) {
            if (list.size() > 1) {
                if (nestDealDate(content, next, list.subList(1, list.size()), curren.getType()) == 1) {
                    isThree = true;
                }
            }
            boolean flag15 = false;
            if (ss.trim().equals("(")) {
                Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                String d = null;
                if (bcm.lookingAt()) {
                    d = bcm.group(1);
                }
                int end = !isThree ? next.getEnd() : ((Match) list.get(1)).getEnd();
                if (d == null) {
                    flag15 = false;
                } else {
                    flag15 = d.trim().equals(content.substring(next.getBegin(), end).trim());
                }
            }
            if (LocaleParam.isRelDates(ss, this.locale) || flag15) {
                result = !isThree ? 1 : 2;
            }
        }
        return result;
    }

    private List<Match> filterDate(String content, List<Match> ms) {
        List<Match> result = new ArrayList();
        int i = 0;
        while (i < ms.size()) {
            Match m = (Match) ms.get(i);
            int type = getType(m.getRegex());
            m.setType(type);
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                int hasNum = (ms.size() - 1) - i;
                List sub = null;
                if (hasNum > 1) {
                    sub = ms.subList(i + 1, i + 3);
                } else if (hasNum == 1) {
                    sub = ms.subList(i + 1, i + 2);
                }
                if (hasNum == 0 || sub == null) {
                    m.setType(1);
                    result.add(m);
                } else {
                    int status = nestDealDate(content, m, sub, -1);
                    m.setType(1);
                    if (status != 0) {
                        if (status == 1) {
                            i++;
                        } else if (status == 2) {
                            i += 2;
                        }
                        Match e = (Match) ms.get(i);
                        int add = 0;
                        int be = content.indexOf(40, m.getEnd());
                        if (be != -1 && be < e.getBegin()) {
                            int end = content.indexOf(41, e.getEnd());
                            if (end != -1) {
                                if (content.substring(e.getEnd(), end + 1).trim().equals(")")) {
                                    add = (end - e.getEnd()) + 1;
                                }
                            }
                        }
                        m.setEnd(e.getEnd() + add);
                        result.add(m);
                    } else {
                        result.add(m);
                    }
                }
            } else {
                result.add(m);
            }
            i++;
        }
        return result;
    }

    private List<Match> filterPeriod(String content, List<Match> ms, String ps) {
        Pattern rel = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + ps + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
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
            } else {
                curren = m;
            }
        }
        return ms;
    }

    private List<Match> filterDateTime(String content, List<Match> ms, String dts) {
        Pattern dt = Pattern.compile("\\s*(at|às|،‏|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|मा|এ|ຂອງວັນທີ" + dts + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
                int ctype = curren.getType();
                int type = m.getType();
                if ((ctype == 1 && type == 2) || ((ctype == 1 && type == 3 && m.isTimePeriod()) || ((ctype == 2 && type == 1) || (ctype == 3 && curren.isTimePeriod() && type == 1)))) {
                    boolean flag;
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals("")) {
                        flag = true;
                    } else {
                        flag = dt.matcher(ss).matches();
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
                        boolean change = true;
                        if (ctype == 2 && type == 1) {
                            boolean flag15;
                            int add = 0;
                            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                            String d = null;
                            if (bcm.lookingAt()) {
                                d = bcm.group(1);
                                add = bcm.group().length();
                            }
                            if (d == null) {
                                flag15 = false;
                            } else {
                                flag15 = d.trim().equals(content.substring(m.getBegin(), m.getEnd()).trim());
                            }
                            if (flag15) {
                                curren.setEnd(curren.getEnd() + add);
                                curren.setType(0);
                                it.remove();
                                change = false;
                            }
                        }
                        if (ctype == 1 && type == 2) {
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
                        if (change) {
                            curren = m;
                        }
                    }
                } else {
                    curren = m;
                }
            } else {
                curren = m;
            }
        }
        return ms;
    }

    private List<Match> filterDateTimePunc(String content, List<Match> ms) {
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
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
            } else {
                curren = m;
            }
        }
        return ms;
    }

    private List<Match> filterDatePeriod(String content, List<Match> ms, String ps, String dts) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterDate(content, ms), dts), ps));
    }

    public static int getType(String name) {
        int n = Integer.parseInt(name);
        if (n > 19999 && n < 30000) {
            if (n == 20009 || n == 20011 || n == 21026) {
                return 6;
            }
            if (n != 20010) {
                return 1;
            }
            return 5;
        } else if (n > 29999 && n < 40000) {
            return 2;
        } else {
            if (n > 9999 && n < 20000) {
                return 0;
            }
            return 3;
        }
    }

    private List<Match> filterByRules(String content, List<Match> ms, RuleInit obj) {
        List<Match> clears = obj.clear(content);
        if (clears == null || clears.isEmpty()) {
            return ms;
        }
        Iterator<Match> it = ms.iterator();
        while (it.hasNext()) {
            Match m = (Match) it.next();
            for (Match t : clears) {
                if (m.getBegin() >= t.getBegin() && m.getEnd() <= t.getEnd()) {
                    it.remove();
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
            while (it.hasNext()) {
                Match m = (Match) it.next();
                if (name.intValue() >= 200) {
                    if (p.getBegin() == m.getEnd()) {
                        it.remove();
                        break;
                    }
                } else if (p.getEnd() == m.getBegin()) {
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
