package android.icu.util;

import android.icu.impl.Grego;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

public class VTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String COLON = ":";
    private static final String COMMA = ",";
    private static final int DEF_DSTSAVINGS = 3600000;
    private static final long DEF_TZSTARTTIME = 0;
    private static final String EQUALS_SIGN = "=";
    private static final int ERR = 3;
    private static final String ICAL_BEGIN = "BEGIN";
    private static final String ICAL_BEGIN_VTIMEZONE = "BEGIN:VTIMEZONE";
    private static final String ICAL_BYDAY = "BYDAY";
    private static final String ICAL_BYMONTH = "BYMONTH";
    private static final String ICAL_BYMONTHDAY = "BYMONTHDAY";
    private static final String ICAL_DAYLIGHT = "DAYLIGHT";
    private static final String[] ICAL_DOW_NAMES = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
    private static final String ICAL_DTSTART = "DTSTART";
    private static final String ICAL_END = "END";
    private static final String ICAL_END_VTIMEZONE = "END:VTIMEZONE";
    private static final String ICAL_FREQ = "FREQ";
    private static final String ICAL_LASTMOD = "LAST-MODIFIED";
    private static final String ICAL_RDATE = "RDATE";
    private static final String ICAL_RRULE = "RRULE";
    private static final String ICAL_STANDARD = "STANDARD";
    private static final String ICAL_TZID = "TZID";
    private static final String ICAL_TZNAME = "TZNAME";
    private static final String ICAL_TZOFFSETFROM = "TZOFFSETFROM";
    private static final String ICAL_TZOFFSETTO = "TZOFFSETTO";
    private static final String ICAL_TZURL = "TZURL";
    private static final String ICAL_UNTIL = "UNTIL";
    private static final String ICAL_VTIMEZONE = "VTIMEZONE";
    private static final String ICAL_YEARLY = "YEARLY";
    private static final String ICU_TZINFO_PROP = "X-TZINFO";
    private static String ICU_TZVERSION = null;
    private static final int INI = 0;
    private static final long MAX_TIME = Long.MAX_VALUE;
    private static final long MIN_TIME = Long.MIN_VALUE;
    private static final int[] MONTHLENGTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final String NEWLINE = "\r\n";
    private static final String SEMICOLON = ";";
    private static final int TZI = 2;
    private static final int VTZ = 1;
    private static final long serialVersionUID = -6851467294127795902L;
    private volatile transient boolean isFrozen = false;
    private Date lastmod = null;
    private String olsonzid = null;
    private BasicTimeZone tz;
    private String tzurl = null;
    private List<String> vtzlines;

    static {
        try {
            ICU_TZVERSION = TimeZone.getTZDataVersion();
        } catch (MissingResourceException e) {
            ICU_TZVERSION = null;
        }
    }

    public static VTimeZone create(String tzid) {
        BasicTimeZone basicTimeZone = TimeZone.getFrozenICUTimeZone(tzid, true);
        if (basicTimeZone == null) {
            return null;
        }
        VTimeZone vtz = new VTimeZone(tzid);
        vtz.tz = (BasicTimeZone) basicTimeZone.cloneAsThawed();
        vtz.olsonzid = vtz.tz.getID();
        return vtz;
    }

    public static VTimeZone create(Reader reader) {
        VTimeZone vtz = new VTimeZone();
        if (vtz.load(reader)) {
            return vtz;
        }
        return null;
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        return this.tz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    public void getOffset(long date, boolean local, int[] offsets) {
        this.tz.getOffset(date, local, offsets);
    }

    @Deprecated
    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        this.tz.getOffsetFromLocal(date, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
    }

    public int getRawOffset() {
        return this.tz.getRawOffset();
    }

    public boolean inDaylightTime(Date date) {
        return this.tz.inDaylightTime(date);
    }

    public void setRawOffset(int offsetMillis) {
        if (!isFrozen()) {
            this.tz.setRawOffset(offsetMillis);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
    }

    public boolean useDaylightTime() {
        return this.tz.useDaylightTime();
    }

    public boolean observesDaylightTime() {
        return this.tz.observesDaylightTime();
    }

    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (other instanceof VTimeZone) {
            return this.tz.hasSameRules(((VTimeZone) other).tz);
        }
        return this.tz.hasSameRules(other);
    }

    public String getTZURL() {
        return this.tzurl;
    }

    public void setTZURL(String url) {
        if (!isFrozen()) {
            this.tzurl = url;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
    }

    public Date getLastModified() {
        return this.lastmod;
    }

    public void setLastModified(Date date) {
        if (!isFrozen()) {
            this.lastmod = date;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
    }

    public void write(Writer writer) throws IOException {
        BufferedWriter bw = new BufferedWriter(writer);
        if (this.vtzlines != null) {
            for (String line : this.vtzlines) {
                if (line.startsWith("TZURL:")) {
                    if (this.tzurl != null) {
                        bw.write(ICAL_TZURL);
                        bw.write(COLON);
                        bw.write(this.tzurl);
                        bw.write(NEWLINE);
                    }
                } else if (!line.startsWith("LAST-MODIFIED:")) {
                    bw.write(line);
                    bw.write(NEWLINE);
                } else if (this.lastmod != null) {
                    bw.write(ICAL_LASTMOD);
                    bw.write(COLON);
                    bw.write(getUTCDateTimeString(this.lastmod.getTime()));
                    bw.write(NEWLINE);
                }
            }
            bw.flush();
            return;
        }
        String[] customProperties = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            customProperties = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "]"};
        }
        writeZone(writer, this.tz, customProperties);
    }

    public void write(Writer writer, long start) throws IOException {
        TimeZoneRule[] rules = this.tz.getTimeZoneRules(start);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) rules[0]);
        for (int i = 1; i < rules.length; i++) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            customProperties = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Partial@" + start + "]"};
        }
        writeZone(writer, rbtz, customProperties);
    }

    public void writeSimple(Writer writer, long time) throws IOException {
        TimeZoneRule[] rules = this.tz.getSimpleTimeZoneRulesNear(time);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) rules[0]);
        for (int i = 1; i < rules.length; i++) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            customProperties = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Simple@" + time + "]"};
        }
        writeZone(writer, rbtz, customProperties);
    }

    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        return this.tz.getNextTransition(base, inclusive);
    }

    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        return this.tz.getPreviousTransition(base, inclusive);
    }

    public boolean hasEquivalentTransitions(TimeZone other, long start, long end) {
        if (this == other) {
            return true;
        }
        return this.tz.hasEquivalentTransitions(other, start, end);
    }

    public TimeZoneRule[] getTimeZoneRules() {
        return this.tz.getTimeZoneRules();
    }

    public TimeZoneRule[] getTimeZoneRules(long start) {
        return this.tz.getTimeZoneRules(start);
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    private VTimeZone() {
    }

    private VTimeZone(String tzid) {
        super(tzid);
    }

    private boolean load(Reader reader) {
        try {
            this.vtzlines = new LinkedList();
            boolean eol = false;
            boolean start = false;
            boolean success = false;
            StringBuilder line = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    if (start && line.toString().startsWith(ICAL_END_VTIMEZONE)) {
                        this.vtzlines.add(line.toString());
                        success = true;
                    }
                } else if (ch != 13) {
                    if (eol) {
                        if (!(ch == 9 || ch == 32)) {
                            if (start && line.length() > 0) {
                                this.vtzlines.add(line.toString());
                            }
                            line.setLength(0);
                            if (ch != 10) {
                                line.append((char) ch);
                            }
                        }
                        eol = false;
                    } else if (ch == 10) {
                        eol = true;
                        if (start) {
                            if (line.toString().startsWith(ICAL_END_VTIMEZONE)) {
                                this.vtzlines.add(line.toString());
                                success = true;
                                break;
                            }
                        } else if (line.toString().startsWith(ICAL_BEGIN_VTIMEZONE)) {
                            this.vtzlines.add(line.toString());
                            line.setLength(0);
                            start = true;
                            eol = false;
                        }
                    } else {
                        line.append((char) ch);
                    }
                }
            }
            if (!success) {
                return false;
            }
            return parse();
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v3, resolved type: android.icu.util.AnnualTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v7, resolved type: android.icu.util.TimeArrayTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v9, resolved type: android.icu.util.TimeArrayTimeZoneRule} */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02b2, code lost:
        r5 = r40;
        r6 = r42;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x020a  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x02b9 A[SYNTHETIC] */
    private boolean parse() {
        TimeArrayTimeZoneRule timeArrayTimeZoneRule;
        List<String> dates;
        String tzname;
        String to;
        String from;
        int state;
        int state2;
        int state3;
        String from2;
        TimeZoneRule rule;
        int initialRawOffset;
        int rawOffset;
        TimeZoneRule rule2;
        int dstSavings;
        int rawOffset2;
        int fromOffset;
        TimeZoneRule rule3;
        TimeZoneRule rule4;
        int initialDSTSavings;
        if (this.vtzlines == null || this.vtzlines.size() == 0) {
            return false;
        }
        int state4 = false;
        String from3 = null;
        String to2 = null;
        String tzname2 = null;
        String dtstart = null;
        List<String> dates2 = null;
        List<TimeZoneRule> rules = new ArrayList<>();
        Iterator<String> it = this.vtzlines.iterator();
        long firstStart = Long.MAX_VALUE;
        int initialDSTSavings2 = 0;
        int initialDSTSavings3 = 0;
        boolean isRRULE = false;
        boolean dst = false;
        String tzid = null;
        while (it.hasNext()) {
            String line = it.next();
            Iterator<String> it2 = it;
            int valueSep = line.indexOf(COLON);
            if (valueSep < 0) {
                it = it2;
            } else {
                int initialRawOffset2 = initialDSTSavings3;
                int initialDSTSavings4 = initialDSTSavings2;
                String name = line.substring(0, valueSep);
                String value = line.substring(valueSep + 1);
                switch (state4) {
                    case 0:
                        state = state4;
                        from = from3;
                        to = to2;
                        if (name.equals(ICAL_BEGIN) && value.equals(ICAL_VTIMEZONE)) {
                            state2 = true;
                            break;
                        }
                    case 1:
                        state = state4;
                        from = from3;
                        if (!name.equals(ICAL_TZID)) {
                            if (name.equals(ICAL_TZURL)) {
                                this.tzurl = value;
                                to = to2;
                            } else if (name.equals(ICAL_LASTMOD)) {
                                to = to2;
                                this.lastmod = new Date(parseDateTimeString(value, 0));
                            } else {
                                to = to2;
                                if (name.equals(ICAL_BEGIN)) {
                                    boolean isDST = value.equals(ICAL_DAYLIGHT);
                                    if (value.equals(ICAL_STANDARD) || isDST) {
                                        if (tzid != null) {
                                            from3 = null;
                                            to2 = null;
                                            tzname2 = null;
                                            dst = isDST;
                                            isRRULE = false;
                                            state4 = true;
                                            dates2 = null;
                                            break;
                                        } else {
                                            state3 = 3;
                                        }
                                    } else {
                                        state3 = 3;
                                    }
                                    state2 = state3;
                                    break;
                                } else if (name.equals(ICAL_END)) {
                                }
                            }
                            state2 = state;
                            break;
                        } else {
                            tzid = value;
                            state4 = state;
                            break;
                        }
                    case 2:
                        if (!name.equals(ICAL_DTSTART)) {
                            if (!name.equals(ICAL_TZNAME)) {
                                if (!name.equals(ICAL_TZOFFSETFROM)) {
                                    if (!name.equals(ICAL_TZOFFSETTO)) {
                                        if (!name.equals(ICAL_RDATE)) {
                                            if (!name.equals(ICAL_RRULE)) {
                                                if (!name.equals(ICAL_END)) {
                                                    state = state4;
                                                    from = from3;
                                                    to = to2;
                                                    state2 = state;
                                                    break;
                                                } else {
                                                    if (dtstart == null || from3 == null) {
                                                        int i = state4;
                                                        from2 = from3;
                                                    } else if (to2 == null) {
                                                        int i2 = state4;
                                                        from2 = from3;
                                                    } else {
                                                        if (tzname2 == null) {
                                                            tzname2 = getDefaultTZName(tzid, dst);
                                                        }
                                                        try {
                                                            rawOffset = offsetStrToMillis(from3);
                                                            try {
                                                                int toOffset = offsetStrToMillis(to2);
                                                                if (dst) {
                                                                    rule2 = null;
                                                                    fromOffset = rawOffset;
                                                                    if (toOffset - fromOffset > 0) {
                                                                        rawOffset2 = fromOffset;
                                                                        dstSavings = toOffset - fromOffset;
                                                                    } else {
                                                                        rawOffset2 = toOffset - 3600000;
                                                                        dstSavings = 3600000;
                                                                    }
                                                                } else {
                                                                    rule2 = null;
                                                                    fromOffset = rawOffset;
                                                                    rawOffset2 = toOffset;
                                                                    dstSavings = 0;
                                                                }
                                                                int rawOffset3 = rawOffset2;
                                                                int dstSavings2 = dstSavings;
                                                                try {
                                                                    long start = parseDateTimeString(dtstart, fromOffset);
                                                                    if (isRRULE) {
                                                                        try {
                                                                            rule3 = createRuleByRRULE(tzname2, rawOffset3, dstSavings2, start, dates2, fromOffset);
                                                                        } catch (IllegalArgumentException e) {
                                                                            int i3 = state4;
                                                                            from = from3;
                                                                            int i4 = rawOffset3;
                                                                            rule = rule2;
                                                                            rawOffset = fromOffset;
                                                                            initialRawOffset = initialRawOffset2;
                                                                            int i5 = rawOffset;
                                                                            if (rule != null) {
                                                                            }
                                                                            from3 = from;
                                                                            if (state4 != 3) {
                                                                            }
                                                                        }
                                                                    } else {
                                                                        try {
                                                                            rule3 = createRuleByRDATE(tzname2, rawOffset3, dstSavings2, start, dates2, fromOffset);
                                                                        } catch (IllegalArgumentException e2) {
                                                                            int i6 = state4;
                                                                            from = from3;
                                                                            rule = rule2;
                                                                            rawOffset = fromOffset;
                                                                            initialRawOffset = initialRawOffset2;
                                                                            int i52 = rawOffset;
                                                                            if (rule != null) {
                                                                            }
                                                                            from3 = from;
                                                                            if (state4 != 3) {
                                                                                this.vtzlines = null;
                                                                                return false;
                                                                            }
                                                                            it = it2;
                                                                            initialDSTSavings3 = initialRawOffset2;
                                                                            initialDSTSavings2 = initialDSTSavings4;
                                                                        }
                                                                    }
                                                                    rule = rule3;
                                                                    if (rule != null) {
                                                                        from = from3;
                                                                        try {
                                                                            Date actualStart = rule.getFirstStart(fromOffset, 0);
                                                                            if (actualStart.getTime() < firstStart) {
                                                                                long firstStart2 = actualStart.getTime();
                                                                                if (dstSavings2 > 0) {
                                                                                    initialRawOffset = fromOffset;
                                                                                    rule4 = rule;
                                                                                    firstStart = firstStart2;
                                                                                    initialDSTSavings = 0;
                                                                                } else {
                                                                                    rule4 = rule;
                                                                                    if (fromOffset - toOffset == 3600000) {
                                                                                        initialRawOffset = fromOffset - 3600000;
                                                                                        initialDSTSavings = 3600000;
                                                                                    } else {
                                                                                        initialRawOffset = fromOffset;
                                                                                        initialDSTSavings = 0;
                                                                                    }
                                                                                    firstStart = firstStart2;
                                                                                }
                                                                                initialDSTSavings4 = initialDSTSavings;
                                                                                int i7 = rawOffset3;
                                                                                rule = rule4;
                                                                                if (rule != null) {
                                                                                    initialRawOffset2 = initialRawOffset;
                                                                                    state4 = 3;
                                                                                } else {
                                                                                    rules.add(rule);
                                                                                    initialRawOffset2 = initialRawOffset;
                                                                                    state4 = true;
                                                                                }
                                                                            } else {
                                                                                rule4 = rule;
                                                                            }
                                                                        } catch (IllegalArgumentException e3) {
                                                                            TimeZoneRule timeZoneRule = rule;
                                                                            int i8 = rawOffset3;
                                                                            rawOffset = fromOffset;
                                                                            initialRawOffset = initialRawOffset2;
                                                                            int i522 = rawOffset;
                                                                            if (rule != null) {
                                                                            }
                                                                            from3 = from;
                                                                            if (state4 != 3) {
                                                                            }
                                                                        }
                                                                    } else {
                                                                        rule4 = rule;
                                                                        from = from3;
                                                                    }
                                                                    initialRawOffset = initialRawOffset2;
                                                                    initialDSTSavings = initialDSTSavings4;
                                                                    initialDSTSavings4 = initialDSTSavings;
                                                                    int i72 = rawOffset3;
                                                                    rule = rule4;
                                                                } catch (IllegalArgumentException e4) {
                                                                    int i9 = state4;
                                                                    from = from3;
                                                                    rule = rule2;
                                                                    rawOffset = fromOffset;
                                                                    initialRawOffset = initialRawOffset2;
                                                                    int i5222 = rawOffset;
                                                                    if (rule != null) {
                                                                    }
                                                                    from3 = from;
                                                                    if (state4 != 3) {
                                                                    }
                                                                }
                                                            } catch (IllegalArgumentException e5) {
                                                                int i10 = state4;
                                                                from = from3;
                                                                int i11 = rawOffset;
                                                                rule = null;
                                                                initialRawOffset = initialRawOffset2;
                                                                int i52222 = rawOffset;
                                                                if (rule != null) {
                                                                }
                                                                from3 = from;
                                                                if (state4 != 3) {
                                                                }
                                                            }
                                                        } catch (IllegalArgumentException e6) {
                                                            int i12 = state4;
                                                            from = from3;
                                                            rawOffset = 0;
                                                            rule = null;
                                                            initialRawOffset = initialRawOffset2;
                                                            int i522222 = rawOffset;
                                                            if (rule != null) {
                                                            }
                                                            from3 = from;
                                                            if (state4 != 3) {
                                                            }
                                                        }
                                                        if (rule != null) {
                                                        }
                                                    }
                                                    state4 = 3;
                                                }
                                            } else if (isRRULE || dates2 == null) {
                                                if (dates2 == null) {
                                                    dates2 = new LinkedList<>();
                                                }
                                                dates2.add(value);
                                                isRRULE = true;
                                            } else {
                                                state4 = true;
                                            }
                                        } else if (isRRULE) {
                                            state4 = 3;
                                        } else {
                                            if (dates2 == null) {
                                                dates2 = new LinkedList<>();
                                            }
                                            int i13 = valueSep;
                                            StringTokenizer st = new StringTokenizer(value, COMMA);
                                            while (st.hasMoreTokens()) {
                                                dates2.add(st.nextToken());
                                            }
                                        }
                                    } else {
                                        to2 = value;
                                    }
                                } else {
                                    from3 = value;
                                }
                            } else {
                                tzname2 = value;
                            }
                        } else {
                            dtstart = value;
                        }
                    default:
                        int i14 = valueSep;
                        state = state4;
                        from = from3;
                        to = to2;
                }
            }
        }
        int i15 = state4;
        String str = from3;
        String str2 = to2;
        int initialRawOffset3 = initialDSTSavings3;
        int initialDSTSavings5 = initialDSTSavings2;
        if (rules.size() == 0) {
            return false;
        }
        int initialRawOffset4 = initialRawOffset3;
        int initialDSTSavings6 = initialDSTSavings5;
        InitialTimeZoneRule initialRule = new InitialTimeZoneRule(getDefaultTZName(tzid, false), initialRawOffset4, initialDSTSavings6);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tzid, initialRule);
        int finalRuleCount = 0;
        int finalRuleIdx = -1;
        int i16 = 0;
        while (i16 < rules.size()) {
            TimeZoneRule r = rules.get(i16);
            InitialTimeZoneRule initialRule2 = initialRule;
            if (r instanceof AnnualTimeZoneRule) {
                tzname = tzname2;
                if (((AnnualTimeZoneRule) r).getEndYear() == Integer.MAX_VALUE) {
                    finalRuleCount++;
                    finalRuleIdx = i16;
                }
            } else {
                tzname = tzname2;
            }
            i16++;
            initialRule = initialRule2;
            tzname2 = tzname;
        }
        String str3 = tzname2;
        if (finalRuleCount > 2) {
            return false;
        }
        if (finalRuleCount != 1) {
            String str4 = dtstart;
            boolean z = dst;
            List<String> list = dates2;
        } else if (rules.size() == 1) {
            rules.clear();
            int i17 = finalRuleCount;
            String str5 = dtstart;
            boolean z2 = dst;
            List<String> list2 = dates2;
        } else {
            AnnualTimeZoneRule finalRule = (AnnualTimeZoneRule) rules.get(finalRuleIdx);
            int tmpRaw = finalRule.getRawOffset();
            int tmpDST = finalRule.getDSTSavings();
            Date finalStart = finalRule.getFirstStart(initialRawOffset4, initialDSTSavings6);
            int i18 = finalRuleCount;
            Date start2 = finalStart;
            int i19 = 0;
            while (true) {
                String dtstart2 = dtstart;
                boolean dst2 = dst;
                int i20 = i19;
                if (i20 < rules.size()) {
                    if (finalRuleIdx == i20) {
                        dates = dates2;
                    } else {
                        TimeZoneRule r2 = rules.get(i20);
                        dates = dates2;
                        Date lastStart = r2.getFinalStart(tmpRaw, tmpDST);
                        if (lastStart.after(start2)) {
                            start2 = finalRule.getNextStart(lastStart.getTime(), r2.getRawOffset(), r2.getDSTSavings(), false);
                        }
                    }
                    i19 = i20 + 1;
                    dtstart = dtstart2;
                    dst = dst2;
                    dates2 = dates;
                } else {
                    if (start2 == finalStart) {
                        TimeArrayTimeZoneRule timeArrayTimeZoneRule2 = new TimeArrayTimeZoneRule(finalRule.getName(), finalRule.getRawOffset(), finalRule.getDSTSavings(), new long[]{finalStart.getTime()}, 2);
                        timeArrayTimeZoneRule = timeArrayTimeZoneRule2;
                    } else {
                        AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(finalRule.getName(), finalRule.getRawOffset(), finalRule.getDSTSavings(), finalRule.getRule(), finalRule.getStartYear(), Grego.timeToFields(start2.getTime(), null)[0]);
                        timeArrayTimeZoneRule = annualTimeZoneRule;
                    }
                    rules.set(finalRuleIdx, timeArrayTimeZoneRule);
                }
            }
        }
        for (TimeZoneRule r3 : rules) {
            rbtz.addTransitionRule(r3);
        }
        this.tz = rbtz;
        setID(tzid);
        return true;
    }

    private static String getDefaultTZName(String tzid, boolean isDST) {
        if (isDST) {
            return tzid + "(DST)";
        }
        return tzid + "(STD)";
    }

    /* JADX WARNING: type inference failed for: r0v11, types: [android.icu.util.TimeZoneRule] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private static TimeZoneRule createRuleByRRULE(String tzname, int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        int[] iArr;
        int dayOfMonth;
        DateTimeRule adtr;
        char c;
        List<String> list = dates;
        Object obj = null;
        if (list == null || dates.size() == 0) {
            int i = fromOffset;
            return null;
        }
        long[] until = new long[1];
        int[] ruleFields = parseRRULE(list.get(0), until);
        if (ruleFields == null) {
            return null;
        }
        int month = ruleFields[0];
        int dayOfWeek = ruleFields[1];
        int nthDayOfWeek = ruleFields[2];
        int dayOfMonth2 = ruleFields[3];
        int i2 = -1;
        int i3 = 7;
        if (dates.size() == 1) {
            if (ruleFields.length > 4) {
                if (ruleFields.length != 10 || month == -1 || dayOfWeek == 0) {
                    return null;
                }
                int[] days = new int[7];
                int firstDay = 31;
                for (int i4 = 0; i4 < 7; i4++) {
                    days[i4] = ruleFields[3 + i4];
                    days[i4] = days[i4] > 0 ? days[i4] : MONTHLENGTH[month] + days[i4] + 1;
                    firstDay = days[i4] < firstDay ? days[i4] : firstDay;
                }
                int i5 = 1;
                while (i5 < i3) {
                    boolean found = false;
                    int j = 0;
                    while (true) {
                        if (j >= i3) {
                            break;
                        } else if (days[j] == firstDay + i5) {
                            found = true;
                            break;
                        } else {
                            j++;
                            i3 = 7;
                        }
                    }
                    if (!found) {
                        return null;
                    }
                    i5++;
                    i3 = 7;
                }
                dayOfMonth2 = firstDay;
            }
            iArr = null;
        } else if (month == -1 || dayOfWeek == 0 || dayOfMonth2 == 0) {
            int i6 = fromOffset;
            return null;
        } else if (dates.size() > 7) {
            return null;
        } else {
            int earliestMonth = month;
            int daysCount = ruleFields.length - 3;
            int earliestDay = 31;
            for (int i7 = 0; i7 < daysCount; i7++) {
                int dom = ruleFields[3 + i7];
                int dom2 = dom > 0 ? dom : MONTHLENGTH[month] + dom + 1;
                earliestDay = dom2 < earliestDay ? dom2 : earliestDay;
            }
            int anotherMonth = -1;
            int earliestMonth2 = earliestMonth;
            int i8 = 1;
            while (i8 < dates.size()) {
                long[] unt = new long[1];
                int[] fields = parseRRULE(list.get(i8), unt);
                if (unt[0] > until[0]) {
                    until = unt;
                }
                if (fields[0] == i2 || fields[1] == 0 || fields[3] == 0) {
                    return null;
                }
                int count = fields.length - 3;
                if (daysCount + count > 7) {
                    return null;
                }
                if (fields[1] != dayOfWeek) {
                    return null;
                }
                if (fields[0] == month) {
                    c = 0;
                } else if (anotherMonth == -1) {
                    int diff = fields[0] - month;
                    if (diff == -11 || diff == -1) {
                        c = 0;
                        anotherMonth = fields[0];
                        earliestMonth2 = anotherMonth;
                        earliestDay = 31;
                    } else if (diff != 11 && diff != 1) {
                        return null;
                    } else {
                        c = 0;
                        anotherMonth = fields[0];
                    }
                } else {
                    c = 0;
                    if (!(fields[0] == month || fields[0] == anotherMonth)) {
                        return null;
                    }
                }
                if (fields[c] == earliestMonth2) {
                    for (int j2 = 0; j2 < count; j2++) {
                        int dom3 = fields[3 + j2];
                        int dom4 = dom3 > 0 ? dom3 : MONTHLENGTH[fields[0]] + dom3 + 1;
                        earliestDay = dom4 < earliestDay ? dom4 : earliestDay;
                    }
                }
                daysCount += count;
                i8++;
                list = dates;
                obj = null;
                i2 = -1;
            }
            ? r0 = obj;
            if (daysCount != 7) {
                return r0;
            }
            month = earliestMonth2;
            dayOfMonth2 = earliestDay;
            iArr = r0;
        }
        int[] dfields = Grego.timeToFields(start + ((long) fromOffset), iArr);
        int startYear = dfields[0];
        if (month == -1) {
            month = dfields[1];
        }
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth2 == 0) {
            dayOfMonth = dfields[2];
        } else {
            dayOfMonth = dayOfMonth2;
        }
        int timeInDay = dfields[5];
        int endYear = Integer.MAX_VALUE;
        if (until[0] != MIN_TIME) {
            Grego.timeToFields(until[0], dfields);
            endYear = dfields[0];
        }
        int endYear2 = endYear;
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            adtr = new DateTimeRule(month, dayOfMonth, timeInDay, 0);
        } else if (dayOfWeek != 0 && nthDayOfWeek != 0 && dayOfMonth == 0) {
            adtr = new DateTimeRule(month, nthDayOfWeek, dayOfWeek, timeInDay, 0);
        } else if (dayOfWeek == 0 || nthDayOfWeek != 0 || dayOfMonth == 0) {
            return null;
        } else {
            int i9 = timeInDay;
            adtr = new DateTimeRule(month, dayOfMonth, dayOfWeek, true, timeInDay, 0);
            AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(tzname, rawOffset, dstSavings, adtr, startYear, endYear2);
            return annualTimeZoneRule;
        }
        AnnualTimeZoneRule annualTimeZoneRule2 = new AnnualTimeZoneRule(tzname, rawOffset, dstSavings, adtr, startYear, endYear2);
        return annualTimeZoneRule2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:94:0x0160  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0166  */
    private static int[] parseRRULE(String rrule, long[] until) {
        int month;
        int[] results;
        int n;
        int sign;
        int[] dayOfMonth = null;
        long untilTime = MIN_TIME;
        boolean yearly = false;
        boolean parseError = false;
        StringTokenizer st = new StringTokenizer(rrule, SEMICOLON);
        int nthDayOfWeek = 0;
        int dayOfWeek = 0;
        int month2 = -1;
        while (true) {
            if (st.hasMoreTokens() == 0) {
                month = month2;
                boolean z = parseError;
                break;
            }
            String prop = st.nextToken();
            int sep = prop.indexOf(EQUALS_SIGN);
            if (sep == -1) {
                month = month2;
                boolean z2 = parseError;
                parseError = true;
                break;
            }
            String attr = prop.substring(0, sep);
            String value = prop.substring(sep + 1);
            if (attr.equals(ICAL_FREQ)) {
                if (!value.equals(ICAL_YEARLY)) {
                    parseError = true;
                    break;
                }
                yearly = true;
            } else if (attr.equals(ICAL_UNTIL)) {
                try {
                    untilTime = parseDateTimeString(value, 0);
                } catch (IllegalArgumentException e) {
                    IllegalArgumentException illegalArgumentException = e;
                    parseError = true;
                    month = month2;
                    if (!parseError) {
                        return null;
                    }
                    until[0] = untilTime;
                    if (dayOfMonth != null) {
                    }
                    results[0] = month;
                    results[1] = dayOfWeek;
                    results[2] = nthDayOfWeek;
                    return results;
                }
            } else if (!attr.equals(ICAL_BYMONTH)) {
                if (attr.equals(ICAL_BYDAY)) {
                    int length = value.length();
                    month = month2;
                    if (length >= 2 && length <= 4) {
                        if (length > 2) {
                            int sign2 = 1;
                            if (value.charAt(0) == 43) {
                                sign = 1;
                            } else if (value.charAt(0) == '-') {
                                sign = -1;
                            } else {
                                if (length == 4) {
                                    parseError = true;
                                    break;
                                }
                                n = Integer.parseInt(value.substring(length - 3, length - 2));
                                if (n == 0 || n > 4) {
                                    parseError = true;
                                } else {
                                    value = value.substring(length - 2);
                                    nthDayOfWeek = n * sign2;
                                }
                            }
                            sign2 = sign;
                            try {
                                n = Integer.parseInt(value.substring(length - 3, length - 2));
                                if (n == 0) {
                                    break;
                                }
                                break;
                            } catch (NumberFormatException e2) {
                                parseError = true;
                            }
                        }
                        int wday = 0;
                        while (wday < ICAL_DOW_NAMES.length && !value.equals(ICAL_DOW_NAMES[wday])) {
                            wday++;
                        }
                        if (wday >= ICAL_DOW_NAMES.length) {
                            parseError = true;
                            break;
                        }
                        dayOfWeek = wday + 1;
                    } else {
                        parseError = true;
                    }
                } else {
                    month = month2;
                    if (attr.equals(ICAL_BYMONTHDAY)) {
                        StringTokenizer days = new StringTokenizer(value, COMMA);
                        dayOfMonth = new int[days.countTokens()];
                        int index = 0;
                        while (true) {
                            int index2 = index;
                            if (!days.hasMoreTokens()) {
                                break;
                            }
                            index = index2 + 1;
                            boolean parseError2 = parseError;
                            try {
                                dayOfMonth[index2] = Integer.parseInt(days.nextToken());
                                parseError = parseError2;
                            } catch (NumberFormatException e3) {
                                parseError = true;
                            }
                        }
                    }
                    boolean z3 = parseError;
                    month2 = month;
                }
                month2 = month;
            } else if (value.length() > 2) {
                parseError = true;
                break;
            } else {
                try {
                    month2 = Integer.parseInt(value) - 1;
                    if (month2 < 0 || month2 >= 12) {
                        parseError = true;
                    }
                } catch (NumberFormatException e4) {
                    NumberFormatException numberFormatException = e4;
                    parseError = true;
                }
            }
        }
        if (!parseError || !yearly) {
            return null;
        }
        until[0] = untilTime;
        if (dayOfMonth != null) {
            results = new int[4];
            results[3] = 0;
        } else {
            results = new int[(dayOfMonth.length + 3)];
            for (int i = 0; i < dayOfMonth.length; i++) {
                results[3 + i] = dayOfMonth[i];
            }
        }
        results[0] = month;
        results[1] = dayOfWeek;
        results[2] = nthDayOfWeek;
        return results;
    }

    private static TimeZoneRule createRuleByRDATE(String tzname, int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        long[] times;
        int idx = 0;
        if (dates == null || dates.size() == 0) {
            times = new long[]{start};
        } else {
            times = new long[dates.size()];
            try {
                for (String date : dates) {
                    int idx2 = idx + 1;
                    try {
                        times[idx] = parseDateTimeString(date, fromOffset);
                        idx = idx2;
                    } catch (IllegalArgumentException e) {
                        IllegalArgumentException illegalArgumentException = e;
                        int i = idx2;
                        return null;
                    }
                }
            } catch (IllegalArgumentException e2) {
                return null;
            }
        }
        TimeArrayTimeZoneRule timeArrayTimeZoneRule = new TimeArrayTimeZoneRule(tzname, rawOffset, dstSavings, times, 2);
        return timeArrayTimeZoneRule;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v0, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v2, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v2, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v10, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v7, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v10, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v40, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r38v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v11, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r38v14, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r38v15, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v17, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r48v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v29, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v27, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v31, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v30, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v28, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v32, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v33, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v13, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v11, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v13, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v33, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v13, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v14, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v15, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v15, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v7, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r55v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r56v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v17, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v14, resolved type: int} */
    /* JADX WARNING: type inference failed for: r0v53, types: [android.icu.util.TimeZoneRule] */
    /* JADX WARNING: type inference failed for: r0v69, types: [android.icu.util.TimeZoneRule] */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00ac, code lost:
        r5 = r38;
        r6 = r39;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private void writeZone(Writer w, BasicTimeZone basictz, String[] customProperties) throws IOException {
        int stdToOffset;
        int dstToOffset;
        int dstFromOffset;
        int stdFromOffset;
        String dstName;
        String stdName;
        int dstCount;
        AnnualTimeZoneRule finalDstRule;
        AnnualTimeZoneRule finalStdRule;
        int dstToOffset2;
        boolean hasTransitions;
        boolean z;
        int dstWeekInMonth;
        int dstMonth;
        int dstFromOffset2;
        int dstWeekInMonth2;
        int stdWeekInMonth;
        int stdDayOfWeek;
        AnnualTimeZoneRule finalStdRule2;
        AnnualTimeZoneRule finalStdRule3;
        int dstDayOfWeek;
        int[] dtfields;
        int year;
        String name;
        String stdName2;
        int stdFromOffset2;
        int stdToOffset2;
        int toOffset;
        int stdMonth;
        int stdDayOfWeek2;
        int stdWeekInMonth2;
        int stdMillisInDay;
        int fromOffset;
        int stdToOffset3;
        String name2;
        int fromOffset2;
        String stdName3;
        int stdFromOffset3;
        int stdToOffset4;
        int toOffset2;
        int stdMonth2;
        int stdDayOfWeek3;
        int stdWeekInMonth3;
        int stdMillisInDay2;
        int stdCount;
        int year2;
        int toOffset3;
        int dstToOffset3;
        int dstDayOfWeek2;
        int dstMonth2;
        String name3;
        int fromOffset3;
        int dstWeekInMonth3;
        int dstMillisInDay;
        String dstName2;
        int dstFromOffset3;
        int dstCount2;
        int dstCount3;
        Writer writer = w;
        BasicTimeZone basicTimeZone = basictz;
        String[] strArr = customProperties;
        writeHeader(w);
        if (strArr != null && strArr.length > 0) {
            for (int i = 0; i < strArr.length; i++) {
                if (strArr[i] != null) {
                    writer.write(strArr[i]);
                    writer.write(NEWLINE);
                }
            }
        }
        AnnualTimeZoneRule finalDstRule2 = null;
        int stdStartYear = 0;
        long stdStartTime = DEF_TZSTARTTIME;
        long stdUntilTime = DEF_TZSTARTTIME;
        int stdCount2 = 0;
        AnnualTimeZoneRule finalStdRule4 = null;
        int[] dtfields2 = new int[6];
        int stdToOffset5 = 0;
        int stdMonth3 = 0;
        int stdDayOfWeek4 = 0;
        int stdMillisInDay3 = 0;
        int stdWeekInMonth4 = 0;
        int stdFromDSTSavings = 0;
        int dstFromDSTSavings = 0;
        int dstWeekInMonth4 = 0;
        int stdFromOffset4 = 0;
        String dstName3 = null;
        long t = Long.MIN_VALUE;
        boolean hasTransitions2 = false;
        int dstFromOffset4 = 0;
        int dstMillisInDay2 = 0;
        int dstToOffset4 = 0;
        int dstMonth3 = 0;
        int dstStartYear = 0;
        int dstDayOfWeek3 = 0;
        int dstCount4 = 0;
        String stdName4 = null;
        long dstUntilTime = 0;
        long dstStartTime = 0;
        while (true) {
            boolean hasTransitions3 = hasTransitions2;
            stdToOffset = stdToOffset5;
            TimeZoneTransition tzt = basicTimeZone.getNextTransition(t, false);
            if (tzt == null) {
                long j = t;
                int i2 = dstMillisInDay2;
                dstToOffset = dstToOffset4;
                dstFromOffset = dstFromOffset4;
                stdFromOffset = stdFromOffset4;
                dstName = dstName3;
                stdName = stdName4;
                dstCount = dstCount4;
                finalDstRule = finalDstRule2;
                finalStdRule = finalStdRule4;
                dstToOffset2 = stdMonth3;
                hasTransitions = hasTransitions3;
                z = false;
                dstWeekInMonth = dstWeekInMonth4;
                dstMonth = dstMonth3;
                dstFromOffset2 = dstDayOfWeek3;
                dstWeekInMonth2 = stdCount2;
                break;
            }
            hasTransitions = true;
            long t2 = tzt.getTime();
            String name4 = tzt.getTo().getName();
            boolean isDst = tzt.getTo().getDSTSavings() != 0;
            int fromOffset4 = tzt.getFrom().getRawOffset() + tzt.getFrom().getDSTSavings();
            int fromDSTSavings = tzt.getFrom().getDSTSavings();
            int stdFromOffset5 = stdFromOffset4;
            int toOffset4 = tzt.getTo().getDSTSavings() + tzt.getTo().getRawOffset();
            String stdName5 = stdName4;
            Grego.timeToFields(tzt.getTime() + ((long) fromOffset4), dtfields2);
            int weekInMonth = Grego.getDayOfWeekInMonth(dtfields2[0], dtfields2[1], dtfields2[2]);
            z = false;
            int year3 = dtfields2[0];
            boolean sameRule = false;
            if (!isDst) {
                int fromOffset5 = fromOffset4;
                String name5 = name4;
                int dstMillisInDay3 = dstMillisInDay2;
                int dstWeekInMonth5 = dstWeekInMonth4;
                int dstMonth4 = dstMonth3;
                int dstDayOfWeek4 = dstDayOfWeek3;
                dstToOffset = dstToOffset4;
                dstFromOffset = dstFromOffset4;
                int toOffset5 = toOffset4;
                int year4 = year3;
                int stdFromOffset6 = stdFromOffset5;
                String stdName6 = stdName5;
                dstName = dstName3;
                if (finalStdRule4 == null && (tzt.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) tzt.getTo()).getEndYear() == Integer.MAX_VALUE) {
                    finalStdRule4 = tzt.getTo();
                }
                if (stdCount2 > 0) {
                    int year5 = year4;
                    if (year5 == stdStartYear + stdCount2) {
                        name2 = name5;
                        stdName3 = stdName6;
                        if (name2.equals(stdName3)) {
                            fromOffset2 = fromOffset5;
                            stdFromOffset3 = stdFromOffset6;
                            if (stdFromOffset3 == fromOffset2) {
                                stdToOffset4 = stdToOffset;
                                toOffset2 = toOffset5;
                                if (stdToOffset4 == toOffset2) {
                                    stdMonth2 = stdMonth3;
                                    if (stdMonth2 == dtfields2[1]) {
                                        stdDayOfWeek3 = stdDayOfWeek4;
                                        if (stdDayOfWeek3 == dtfields2[3]) {
                                            stdWeekInMonth3 = stdMillisInDay3;
                                            if (stdWeekInMonth3 == weekInMonth) {
                                                stdMillisInDay2 = stdWeekInMonth4;
                                                if (stdMillisInDay2 == dtfields2[5]) {
                                                    stdUntilTime = t2;
                                                    stdCount2++;
                                                    sameRule = true;
                                                }
                                            } else {
                                                stdMillisInDay2 = stdWeekInMonth4;
                                            }
                                        } else {
                                            stdWeekInMonth3 = stdMillisInDay3;
                                            stdMillisInDay2 = stdWeekInMonth4;
                                        }
                                    } else {
                                        stdDayOfWeek3 = stdDayOfWeek4;
                                        stdWeekInMonth3 = stdMillisInDay3;
                                        stdMillisInDay2 = stdWeekInMonth4;
                                    }
                                } else {
                                    stdMonth2 = stdMonth3;
                                    stdDayOfWeek3 = stdDayOfWeek4;
                                    stdWeekInMonth3 = stdMillisInDay3;
                                    stdMillisInDay2 = stdWeekInMonth4;
                                }
                            } else {
                                stdMonth2 = stdMonth3;
                                stdDayOfWeek3 = stdDayOfWeek4;
                                stdWeekInMonth3 = stdMillisInDay3;
                                stdMillisInDay2 = stdWeekInMonth4;
                                stdToOffset4 = stdToOffset;
                                toOffset2 = toOffset5;
                            }
                        } else {
                            stdMonth2 = stdMonth3;
                            stdDayOfWeek3 = stdDayOfWeek4;
                            stdWeekInMonth3 = stdMillisInDay3;
                            stdMillisInDay2 = stdWeekInMonth4;
                            stdToOffset4 = stdToOffset;
                            fromOffset2 = fromOffset5;
                            toOffset2 = toOffset5;
                            stdFromOffset3 = stdFromOffset6;
                        }
                    } else {
                        stdMonth2 = stdMonth3;
                        stdDayOfWeek3 = stdDayOfWeek4;
                        stdWeekInMonth3 = stdMillisInDay3;
                        stdMillisInDay2 = stdWeekInMonth4;
                        stdToOffset4 = stdToOffset;
                        fromOffset2 = fromOffset5;
                        name2 = name5;
                        toOffset2 = toOffset5;
                        stdFromOffset3 = stdFromOffset6;
                        stdName3 = stdName6;
                    }
                    int stdCount3 = stdCount2;
                    if (sameRule) {
                        stdCount = stdCount3;
                        stdMillisInDay = stdMillisInDay2;
                        stdWeekInMonth2 = stdWeekInMonth3;
                        stdDayOfWeek2 = stdDayOfWeek3;
                        stdMonth = stdMonth2;
                        toOffset = toOffset2;
                        stdToOffset2 = stdToOffset4;
                        stdFromOffset2 = stdFromOffset3;
                        stdName2 = stdName3;
                        fromOffset = fromOffset2;
                        year = year5;
                        name = name2;
                    } else if (stdCount3 == 1) {
                        stdCount = stdCount3;
                        stdMillisInDay = stdMillisInDay2;
                        stdWeekInMonth2 = stdWeekInMonth3;
                        stdDayOfWeek2 = stdDayOfWeek3;
                        stdMonth = stdMonth2;
                        toOffset = toOffset2;
                        stdToOffset2 = stdToOffset4;
                        stdFromOffset2 = stdFromOffset3;
                        writeZonePropsByTime(w, false, stdName3, stdFromOffset3, stdToOffset4, stdStartTime, true);
                        stdName2 = stdName3;
                        fromOffset = fromOffset2;
                        year = year5;
                        name = name2;
                    } else {
                        stdCount = stdCount3;
                        stdMillisInDay = stdMillisInDay2;
                        stdWeekInMonth2 = stdWeekInMonth3;
                        stdDayOfWeek2 = stdDayOfWeek3;
                        stdMonth = stdMonth2;
                        toOffset = toOffset2;
                        stdToOffset2 = stdToOffset4;
                        stdFromOffset2 = stdFromOffset3;
                        stdName2 = stdName3;
                        fromOffset = fromOffset2;
                        year = year5;
                        name = name2;
                        writeZonePropsByDOW(w, false, stdName3, stdFromOffset2, stdToOffset2, stdMonth, stdWeekInMonth2, stdDayOfWeek2, stdStartTime, stdUntilTime);
                    }
                    stdCount2 = stdCount;
                } else {
                    stdMonth = stdMonth3;
                    stdToOffset2 = stdToOffset;
                    fromOffset = fromOffset5;
                    toOffset = toOffset5;
                    stdFromOffset2 = stdFromOffset6;
                    year = year4;
                    stdDayOfWeek2 = stdDayOfWeek4;
                    name = name5;
                    stdName2 = stdName6;
                    int i3 = stdWeekInMonth4;
                    stdWeekInMonth2 = stdMillisInDay3;
                    stdMillisInDay = i3;
                }
                if (!sameRule) {
                    stdFromDSTSavings = fromDSTSavings;
                    stdToOffset3 = toOffset;
                    stdUntilTime = t2;
                    stdName4 = name;
                    stdStartYear = year;
                    stdMonth = dtfields2[1];
                    stdDayOfWeek4 = dtfields2[3];
                    stdMillisInDay3 = weekInMonth;
                    stdWeekInMonth4 = dtfields2[5];
                    stdStartTime = t2;
                    stdCount2 = 1;
                    stdFromOffset4 = fromOffset;
                } else {
                    stdDayOfWeek4 = stdDayOfWeek2;
                    stdToOffset3 = stdToOffset2;
                    stdFromOffset4 = stdFromOffset2;
                    stdName4 = stdName2;
                    int i4 = stdWeekInMonth2;
                    stdWeekInMonth4 = stdMillisInDay;
                    stdMillisInDay3 = i4;
                }
                if (finalStdRule4 != null && finalDstRule2 != null) {
                    stdToOffset = stdToOffset3;
                    stdFromOffset = stdFromOffset4;
                    stdName = stdName4;
                    dstCount = dstCount4;
                    finalDstRule = finalDstRule2;
                    dstWeekInMonth2 = stdCount2;
                    finalStdRule = finalStdRule4;
                    stdDayOfWeek = stdDayOfWeek4;
                    stdWeekInMonth = stdMillisInDay3;
                    dstToOffset2 = stdMonth;
                    dstWeekInMonth = dstWeekInMonth5;
                    dstMonth = dstMonth4;
                    dstFromOffset2 = dstDayOfWeek4;
                    break;
                }
                dtfields = dtfields2;
                stdToOffset5 = stdToOffset3;
                dstFromOffset4 = dstFromOffset;
                dstName3 = dstName;
                dstMillisInDay2 = dstMillisInDay3;
                stdMonth3 = stdMonth;
                dstWeekInMonth4 = dstWeekInMonth5;
                dstMonth3 = dstMonth4;
                dstDayOfWeek3 = dstDayOfWeek4;
                dstToOffset4 = dstToOffset;
            } else {
                if (finalDstRule2 == null && (tzt.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) tzt.getTo()).getEndYear() == Integer.MAX_VALUE) {
                    finalDstRule2 = tzt.getTo();
                }
                if (dstCount4 > 0) {
                    if (year3 == dstStartYear + dstCount4 && name4.equals(dstName3) && dstFromOffset4 == fromOffset4 && dstToOffset4 == toOffset4 && dstMonth3 == dtfields2[1] && dstDayOfWeek3 == dtfields2[3] && dstWeekInMonth4 == weekInMonth && dstMillisInDay2 == dtfields2[5]) {
                        dstUntilTime = t2;
                        dstCount4++;
                        sameRule = true;
                    }
                    int dstCount5 = dstCount4;
                    if (sameRule) {
                        fromOffset3 = fromOffset4;
                        name3 = name4;
                        dstMillisInDay = dstMillisInDay2;
                        dstWeekInMonth3 = dstWeekInMonth4;
                        dstMonth2 = dstMonth3;
                        dstDayOfWeek2 = dstDayOfWeek3;
                        dstToOffset3 = dstToOffset4;
                        dstFromOffset3 = dstFromOffset4;
                        toOffset3 = toOffset4;
                        year2 = year3;
                        dstCount3 = dstCount5;
                        stdFromOffset = stdFromOffset5;
                        stdName = stdName5;
                        dstCount2 = 1;
                        dstName2 = dstName3;
                    } else if (dstCount5 == 1) {
                        dstCount3 = dstCount5;
                        dstCount2 = 1;
                        fromOffset3 = fromOffset4;
                        name3 = name4;
                        dstMillisInDay = dstMillisInDay2;
                        dstWeekInMonth3 = dstWeekInMonth4;
                        dstMonth2 = dstMonth3;
                        dstDayOfWeek2 = dstDayOfWeek3;
                        dstToOffset3 = dstToOffset4;
                        writeZonePropsByTime(w, true, dstName3, dstFromOffset4, dstToOffset4, dstStartTime, true);
                        dstFromOffset3 = dstFromOffset4;
                        toOffset3 = toOffset4;
                        year2 = year3;
                        stdFromOffset = stdFromOffset5;
                        stdName = stdName5;
                        dstName2 = dstName3;
                    } else {
                        fromOffset3 = fromOffset4;
                        name3 = name4;
                        dstMillisInDay = dstMillisInDay2;
                        dstWeekInMonth3 = dstWeekInMonth4;
                        dstMonth2 = dstMonth3;
                        dstDayOfWeek2 = dstDayOfWeek3;
                        dstToOffset3 = dstToOffset4;
                        dstCount3 = dstCount5;
                        dstCount2 = 1;
                        dstFromOffset3 = dstFromOffset4;
                        toOffset3 = toOffset4;
                        stdFromOffset = stdFromOffset5;
                        dstName2 = dstName3;
                        year2 = year3;
                        stdName = stdName5;
                        writeZonePropsByDOW(w, true, dstName3, dstFromOffset4, dstToOffset3, dstMonth3, dstWeekInMonth3, dstDayOfWeek2, dstStartTime, dstUntilTime);
                    }
                    dstCount4 = dstCount3;
                } else {
                    fromOffset3 = fromOffset4;
                    name3 = name4;
                    dstMillisInDay = dstMillisInDay2;
                    dstWeekInMonth3 = dstWeekInMonth4;
                    dstMonth2 = dstMonth3;
                    dstDayOfWeek2 = dstDayOfWeek3;
                    dstToOffset3 = dstToOffset4;
                    dstFromOffset3 = dstFromOffset4;
                    toOffset3 = toOffset4;
                    year2 = year3;
                    stdFromOffset = stdFromOffset5;
                    stdName = stdName5;
                    dstCount2 = 1;
                    dstName2 = dstName3;
                }
                if (!sameRule) {
                    dstName3 = name3;
                    dstFromOffset4 = fromOffset3;
                    dstFromDSTSavings = fromDSTSavings;
                    dstToOffset4 = toOffset3;
                    dstMonth3 = dtfields2[dstCount2];
                    dstDayOfWeek3 = dtfields2[3];
                    dstWeekInMonth4 = weekInMonth;
                    dstUntilTime = t2;
                    dstStartTime = t2;
                    dstStartYear = year2;
                    dstMillisInDay2 = dtfields2[5];
                    dstCount4 = 1;
                } else {
                    dstFromOffset4 = dstFromOffset3;
                    dstName3 = dstName2;
                    dstMillisInDay2 = dstMillisInDay;
                    dstWeekInMonth4 = dstWeekInMonth3;
                    dstMonth3 = dstMonth2;
                    dstDayOfWeek3 = dstDayOfWeek2;
                    dstToOffset4 = dstToOffset3;
                }
                if (finalStdRule4 != null && finalDstRule2 != null) {
                    int i5 = dstMillisInDay2;
                    dstWeekInMonth = dstWeekInMonth4;
                    dstToOffset = dstToOffset4;
                    dstFromOffset = dstFromOffset4;
                    dstName = dstName3;
                    dstCount = dstCount4;
                    finalDstRule = finalDstRule2;
                    dstWeekInMonth2 = stdCount2;
                    finalStdRule = finalStdRule4;
                    dstToOffset2 = stdMonth3;
                    dstMonth = dstMonth3;
                    dstFromOffset2 = dstDayOfWeek3;
                    break;
                }
                dtfields = dtfields2;
                stdToOffset5 = stdToOffset;
                stdFromOffset4 = stdFromOffset;
                stdName4 = stdName;
            }
            Writer writer2 = w;
            basicTimeZone = basictz;
            hasTransitions2 = true;
            t = t2;
            dtfields2 = dtfields;
        }
        if (!hasTransitions) {
            int offset = basictz.getOffset(DEF_TZSTARTTIME);
            boolean isDst2 = offset != basictz.getRawOffset() ? true : z;
            int offset2 = offset;
            writeZonePropsByTime(w, isDst2, getDefaultTZName(basictz.getID(), isDst2), offset2, offset2, DEF_TZSTARTTIME - ((long) offset), false);
            int i6 = dstFromOffset2;
            AnnualTimeZoneRule annualTimeZoneRule = finalStdRule;
            int i7 = dstWeekInMonth;
            int i8 = dstCount;
            int[] iArr = dtfields2;
            int dstCount6 = dstWeekInMonth2;
            int i9 = stdDayOfWeek;
            int i10 = stdWeekInMonth;
            int i11 = dstToOffset2;
            int i12 = dstMonth;
        } else {
            int stdCount4 = dstWeekInMonth2;
            int stdDayOfWeek5 = stdDayOfWeek;
            int stdWeekInMonth5 = stdWeekInMonth;
            int stdMonth4 = dstToOffset2;
            if (dstCount > 0) {
                if (finalDstRule != null) {
                    dstDayOfWeek = dstFromOffset2;
                    finalStdRule2 = finalStdRule;
                    int dstWeekInMonth6 = dstWeekInMonth;
                    int[] iArr2 = dtfields2;
                    int dstMonth5 = dstMonth;
                    if (dstCount == 1) {
                        writeFinalRule(w, true, finalDstRule, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, dstStartTime);
                    } else {
                        int dstDayOfWeek5 = dstDayOfWeek;
                        if (isEquivalentDateRule(dstMonth5, dstWeekInMonth6, dstDayOfWeek5, finalDstRule.getRule())) {
                            int i13 = dstDayOfWeek5;
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth5, dstWeekInMonth6, dstDayOfWeek5, dstStartTime, MAX_TIME);
                        } else {
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth5, dstWeekInMonth6, dstDayOfWeek5, dstStartTime, dstUntilTime);
                            Date nextStart = finalDstRule.getNextStart(dstUntilTime, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, false);
                            if (nextStart != null) {
                                writeFinalRule(w, true, finalDstRule, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, nextStart.getTime());
                            }
                        }
                    }
                } else if (dstCount == 1) {
                    writeZonePropsByTime(w, true, dstName, dstFromOffset, dstToOffset, dstStartTime, true);
                    int i14 = dstFromOffset2;
                    finalStdRule2 = finalStdRule;
                    int i15 = dstWeekInMonth;
                    int[] iArr3 = dtfields2;
                    int i16 = dstMonth;
                } else {
                    finalStdRule2 = finalStdRule;
                    dstDayOfWeek = dstFromOffset2;
                    int[] iArr4 = dtfields2;
                    int i17 = dstMonth;
                    int dstDayOfWeek6 = dstWeekInMonth;
                    writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth, dstWeekInMonth, dstFromOffset2, dstStartTime, dstUntilTime);
                }
            } else {
                int dstDayOfWeek7 = dstFromOffset2;
                finalStdRule2 = finalStdRule;
                int i18 = dstWeekInMonth;
                int[] iArr5 = dtfields2;
                int i19 = dstMonth;
            }
            int dstMonth6 = stdCount4;
            if (dstMonth6 > 0) {
                AnnualTimeZoneRule finalStdRule5 = finalStdRule2;
                if (finalStdRule5 != null) {
                    finalStdRule3 = finalStdRule5;
                    int i20 = dstCount;
                    if (dstMonth6 == 1) {
                        writeFinalRule(w, false, finalStdRule3, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, stdStartTime);
                    } else {
                        AnnualTimeZoneRule finalStdRule6 = finalStdRule3;
                        int stdDayOfWeek6 = stdDayOfWeek5;
                        int stdWeekInMonth6 = stdWeekInMonth5;
                        int stdMonth5 = stdMonth4;
                        if (isEquivalentDateRule(stdMonth5, stdWeekInMonth6, stdDayOfWeek6, finalStdRule6.getRule())) {
                            int i21 = stdDayOfWeek6;
                            int i22 = stdWeekInMonth6;
                            AnnualTimeZoneRule annualTimeZoneRule2 = finalStdRule6;
                            int i23 = stdMonth5;
                            writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset, stdMonth5, stdWeekInMonth6, stdDayOfWeek6, stdStartTime, MAX_TIME);
                        } else {
                            AnnualTimeZoneRule finalStdRule7 = finalStdRule6;
                            writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset, stdMonth5, stdWeekInMonth6, stdDayOfWeek6, stdStartTime, stdUntilTime);
                            Date nextStart2 = finalStdRule7.getNextStart(stdUntilTime, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, false);
                            if (nextStart2 != null) {
                                writeFinalRule(w, false, finalStdRule7, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, nextStart2.getTime());
                            }
                        }
                    }
                } else if (dstMonth6 == 1) {
                    writeZonePropsByTime(w, false, stdName, stdFromOffset, stdToOffset, stdStartTime, true);
                    AnnualTimeZoneRule annualTimeZoneRule3 = finalStdRule5;
                    int i24 = dstCount;
                    int i25 = stdDayOfWeek5;
                    int i26 = stdWeekInMonth5;
                    int i27 = stdMonth4;
                    int dstCount7 = dstMonth6;
                } else {
                    finalStdRule3 = finalStdRule5;
                    int i28 = dstCount;
                    int dstCount8 = dstMonth6;
                    writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset, stdMonth4, stdWeekInMonth5, stdDayOfWeek5, stdStartTime, stdUntilTime);
                }
            } else {
                int i29 = stdDayOfWeek5;
                int i30 = stdWeekInMonth5;
                int i31 = stdMonth4;
                AnnualTimeZoneRule annualTimeZoneRule4 = finalStdRule2;
                int dstCount9 = dstMonth6;
            }
        }
        writeFooter(w);
    }

    private static boolean isEquivalentDateRule(int month, int weekInMonth, int dayOfWeek, DateTimeRule dtrule) {
        if (month != dtrule.getRuleMonth() || dayOfWeek != dtrule.getRuleDayOfWeek() || dtrule.getTimeRuleType() != 0) {
            return false;
        }
        if (dtrule.getDateRuleType() == 1 && dtrule.getRuleWeekInMonth() == weekInMonth) {
            return true;
        }
        int ruleDOM = dtrule.getRuleDayOfMonth();
        if (dtrule.getDateRuleType() == 2) {
            if (ruleDOM % 7 == 1 && (ruleDOM + 6) / 7 == weekInMonth) {
                return true;
            }
            if (month != 1 && (MONTHLENGTH[month] - ruleDOM) % 7 == 6 && weekInMonth == (((MONTHLENGTH[month] - ruleDOM) + 1) / 7) * -1) {
                return true;
            }
        }
        if (dtrule.getDateRuleType() == 3) {
            if (ruleDOM % 7 == 0 && ruleDOM / 7 == weekInMonth) {
                return true;
            }
            if (month != 1 && (MONTHLENGTH[month] - ruleDOM) % 7 == 0 && weekInMonth == -1 * (((MONTHLENGTH[month] - ruleDOM) / 7) + 1)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private static void writeZonePropsByTime(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, long time, boolean withRDATE) throws IOException {
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, time);
        if (withRDATE) {
            writer.write(ICAL_RDATE);
            writer.write(COLON);
            writer.write(getDateTimeString(((long) fromOffset) + time));
            writer.write(NEWLINE);
        }
        endZoneProps(writer, isDst);
    }

    private static void writeZonePropsByDOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int dayOfMonth, long startTime, long untilTime) throws IOException {
        Writer writer2 = writer;
        beginZoneProps(writer2, isDst, tzname, fromOffset, toOffset, startTime);
        beginRRULE(writer2, month);
        writer2.write(ICAL_BYMONTHDAY);
        writer2.write(EQUALS_SIGN);
        writer2.write(Integer.toString(dayOfMonth));
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer2, getDateTimeString(untilTime + ((long) fromOffset)));
        } else {
            int i = fromOffset;
        }
        writer2.write(NEWLINE);
        endZoneProps(writer2, isDst);
    }

    private static void writeZonePropsByDOW(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int weekInMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        Writer writer2 = writer;
        beginZoneProps(writer2, isDst, tzname, fromOffset, toOffset, startTime);
        beginRRULE(writer2, month);
        writer2.write(ICAL_BYDAY);
        writer2.write(EQUALS_SIGN);
        writer2.write(Integer.toString(weekInMonth));
        writer2.write(ICAL_DOW_NAMES[dayOfWeek - 1]);
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer2, getDateTimeString(untilTime + ((long) fromOffset)));
        } else {
            int i = fromOffset;
        }
        writer2.write(NEWLINE);
        endZoneProps(writer2, isDst);
    }

    private static void writeZonePropsByDOW_GEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        int i = month;
        if (dayOfMonth % 7 == 1) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, i, (dayOfMonth + 6) / 7, dayOfWeek, startTime, untilTime);
        } else if (i == 1 || (MONTHLENGTH[i] - dayOfMonth) % 7 != 6) {
            beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);
            int startDay = dayOfMonth;
            int currentMonthDays = 7;
            int prevMonth = 11;
            if (dayOfMonth <= 0) {
                int prevMonthDays = 1 - dayOfMonth;
                currentMonthDays = 7 - prevMonthDays;
                if (i - 1 >= 0) {
                    prevMonth = i - 1;
                }
                writeZonePropsByDOW_GEQ_DOM_sub(writer, prevMonth, -prevMonthDays, dayOfWeek, prevMonthDays, MAX_TIME, fromOffset);
                startDay = 1;
            } else if (dayOfMonth + 6 > MONTHLENGTH[i]) {
                int nextMonthDays = (dayOfMonth + 6) - MONTHLENGTH[i];
                currentMonthDays = 7 - nextMonthDays;
                writeZonePropsByDOW_GEQ_DOM_sub(writer, i + 1 > 11 ? 0 : i + 1, 1, dayOfWeek, nextMonthDays, MAX_TIME, fromOffset);
            }
            writeZonePropsByDOW_GEQ_DOM_sub(writer, i, startDay, dayOfWeek, currentMonthDays, untilTime, fromOffset);
            endZoneProps(writer, isDst);
        } else {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, i, -1 * (((MONTHLENGTH[i] - dayOfMonth) + 1) / 7), dayOfWeek, startTime, untilTime);
        }
    }

    private static void writeZonePropsByDOW_GEQ_DOM_sub(Writer writer, int month, int dayOfMonth, int dayOfWeek, int numDays, long untilTime, int fromOffset) throws IOException {
        int startDayNum = dayOfMonth;
        boolean isFeb = month == 1;
        if (dayOfMonth < 0 && !isFeb) {
            startDayNum = MONTHLENGTH[month] + dayOfMonth + 1;
        }
        beginRRULE(writer, month);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_DOW_NAMES[dayOfWeek - 1]);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(startDayNum));
        for (int i = 1; i < numDays; i++) {
            writer.write(COMMA);
            writer.write(Integer.toString(startDayNum + i));
        }
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(((long) fromOffset) + untilTime));
        }
        writer.write(NEWLINE);
    }

    private static void writeZonePropsByDOW_LEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        int i = month;
        int i2 = dayOfMonth;
        if (i2 % 7 == 0) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, i, i2 / 7, dayOfWeek, startTime, untilTime);
        } else if (i != 1 && (MONTHLENGTH[i] - i2) % 7 == 0) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, i, -1 * (((MONTHLENGTH[i] - i2) / 7) + 1), dayOfWeek, startTime, untilTime);
        } else if (i == 1 && i2 == 29) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, 1, -1, dayOfWeek, startTime, untilTime);
        } else {
            writeZonePropsByDOW_GEQ_DOM(writer, isDst, tzname, fromOffset, toOffset, i, i2 - 6, dayOfWeek, startTime, untilTime);
        }
    }

    private static void writeFinalRule(Writer writer, boolean isDst, AnnualTimeZoneRule rule, int fromRawOffset, int fromDSTSavings, long startTime) throws IOException {
        long startTime2;
        int i = fromRawOffset;
        int i2 = fromDSTSavings;
        DateTimeRule dtrule = toWallTimeRule(rule.getRule(), i, i2);
        int timeInDay = dtrule.getRuleMillisInDay();
        if (timeInDay < 0) {
            startTime2 = startTime + ((long) (0 - timeInDay));
        } else if (timeInDay >= 86400000) {
            startTime2 = startTime - ((long) (timeInDay - 86399999));
        } else {
            startTime2 = startTime;
        }
        int toOffset = rule.getRawOffset() + rule.getDSTSavings();
        switch (dtrule.getDateRuleType()) {
            case 0:
                writeZonePropsByDOM(writer, isDst, rule.getName(), i + i2, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), startTime2, MAX_TIME);
                return;
            case 1:
                writeZonePropsByDOW(writer, isDst, rule.getName(), i + i2, toOffset, dtrule.getRuleMonth(), dtrule.getRuleWeekInMonth(), dtrule.getRuleDayOfWeek(), startTime2, MAX_TIME);
                return;
            case 2:
                writeZonePropsByDOW_GEQ_DOM(writer, isDst, rule.getName(), i + i2, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime2, MAX_TIME);
                return;
            case 3:
                writeZonePropsByDOW_LEQ_DOM(writer, isDst, rule.getName(), i + i2, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime2, MAX_TIME);
                return;
            default:
                return;
        }
    }

    private static DateTimeRule toWallTimeRule(DateTimeRule rule, int rawOffset, int dstSavings) {
        DateTimeRule modifiedRule;
        if (rule.getTimeRuleType() == 0) {
            return rule;
        }
        int wallt = rule.getRuleMillisInDay();
        if (rule.getTimeRuleType() == 2) {
            wallt += rawOffset + dstSavings;
        } else if (rule.getTimeRuleType() == 1) {
            wallt += dstSavings;
        }
        int dshift = 0;
        if (wallt < 0) {
            dshift = -1;
            wallt += Grego.MILLIS_PER_DAY;
        } else if (wallt >= 86400000) {
            dshift = 1;
            wallt -= Grego.MILLIS_PER_DAY;
        }
        int month = rule.getRuleMonth();
        int dom = rule.getRuleDayOfMonth();
        int dow = rule.getRuleDayOfWeek();
        int dtype = rule.getDateRuleType();
        if (dshift != 0) {
            if (dtype == 1) {
                int wim = rule.getRuleWeekInMonth();
                if (wim > 0) {
                    dtype = 2;
                    dom = ((wim - 1) * 7) + 1;
                } else {
                    dtype = 3;
                    dom = MONTHLENGTH[month] + ((wim + 1) * 7);
                }
            }
            dom += dshift;
            int i = 11;
            if (dom == 0) {
                int month2 = month - 1;
                if (month2 >= 0) {
                    i = month2;
                }
                month = i;
                dom = MONTHLENGTH[month];
            } else if (dom > MONTHLENGTH[month]) {
                int month3 = month + 1;
                month = month3 > 11 ? 0 : month3;
                dom = 1;
            }
            if (dtype != 0) {
                dow += dshift;
                if (dow < 1) {
                    dow = 7;
                } else if (dow > 7) {
                    dow = 1;
                }
            }
        }
        if (dtype == 0) {
            modifiedRule = new DateTimeRule(month, dom, wallt, 0);
        } else {
            modifiedRule = new DateTimeRule(month, dom, dow, dtype == 2, wallt, 0);
        }
        return modifiedRule;
    }

    private static void beginZoneProps(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, long startTime) throws IOException {
        writer.write(ICAL_BEGIN);
        writer.write(COLON);
        if (isDst) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);
        writer.write(ICAL_TZOFFSETTO);
        writer.write(COLON);
        writer.write(millisToOffset(toOffset));
        writer.write(NEWLINE);
        writer.write(ICAL_TZOFFSETFROM);
        writer.write(COLON);
        writer.write(millisToOffset(fromOffset));
        writer.write(NEWLINE);
        writer.write(ICAL_TZNAME);
        writer.write(COLON);
        writer.write(tzname);
        writer.write(NEWLINE);
        writer.write(ICAL_DTSTART);
        writer.write(COLON);
        writer.write(getDateTimeString(((long) fromOffset) + startTime));
        writer.write(NEWLINE);
    }

    private static void endZoneProps(Writer writer, boolean isDst) throws IOException {
        writer.write(ICAL_END);
        writer.write(COLON);
        if (isDst) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);
    }

    private static void beginRRULE(Writer writer, int month) throws IOException {
        writer.write(ICAL_RRULE);
        writer.write(COLON);
        writer.write(ICAL_FREQ);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_YEARLY);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTH);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(month + 1));
        writer.write(SEMICOLON);
    }

    private static void appendUNTIL(Writer writer, String until) throws IOException {
        if (until != null) {
            writer.write(SEMICOLON);
            writer.write(ICAL_UNTIL);
            writer.write(EQUALS_SIGN);
            writer.write(until);
        }
    }

    private void writeHeader(Writer writer) throws IOException {
        writer.write(ICAL_BEGIN);
        writer.write(COLON);
        writer.write(ICAL_VTIMEZONE);
        writer.write(NEWLINE);
        writer.write(ICAL_TZID);
        writer.write(COLON);
        writer.write(this.tz.getID());
        writer.write(NEWLINE);
        if (this.tzurl != null) {
            writer.write(ICAL_TZURL);
            writer.write(COLON);
            writer.write(this.tzurl);
            writer.write(NEWLINE);
        }
        if (this.lastmod != null) {
            writer.write(ICAL_LASTMOD);
            writer.write(COLON);
            writer.write(getUTCDateTimeString(this.lastmod.getTime()));
            writer.write(NEWLINE);
        }
    }

    private static void writeFooter(Writer writer) throws IOException {
        writer.write(ICAL_END);
        writer.write(COLON);
        writer.write(ICAL_VTIMEZONE);
        writer.write(NEWLINE);
    }

    private static String getDateTimeString(long time) {
        int[] fields = Grego.timeToFields(time, null);
        StringBuilder sb = new StringBuilder(15);
        sb.append(numToString(fields[0], 4));
        sb.append(numToString(fields[1] + 1, 2));
        sb.append(numToString(fields[2], 2));
        sb.append('T');
        int t = fields[5];
        int hour = t / 3600000;
        int t2 = t % 3600000;
        sb.append(numToString(hour, 2));
        sb.append(numToString(t2 / 60000, 2));
        sb.append(numToString((t2 % 60000) / 1000, 2));
        return sb.toString();
    }

    private static String getUTCDateTimeString(long time) {
        return getDateTimeString(time) + "Z";
    }

    private static long parseDateTimeString(String str, int offset) {
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int min = 0;
        int sec = 0;
        boolean isUTC = false;
        boolean isValid = false;
        if (str != null) {
            int length = str.length();
            if ((length == 15 || length == 16) && str.charAt(8) == 'T') {
                if (length == 16) {
                    if (str.charAt(15) == 'Z') {
                        isUTC = true;
                    }
                }
                try {
                    year = Integer.parseInt(str.substring(0, 4));
                    month = Integer.parseInt(str.substring(4, 6)) - 1;
                    day = Integer.parseInt(str.substring(6, 8));
                    hour = Integer.parseInt(str.substring(9, 11));
                    min = Integer.parseInt(str.substring(11, 13));
                    sec = Integer.parseInt(str.substring(13, 15));
                    int maxDayOfMonth = Grego.monthLength(year, month);
                    if (year >= 0 && month >= 0 && month <= 11 && day >= 1 && day <= maxDayOfMonth && hour >= 0 && hour < 24 && min >= 0 && min < 60 && sec >= 0 && sec < 60) {
                        isValid = true;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        if (isValid) {
            long time = (Grego.fieldsToDay(year, month, day) * 86400000) + ((long) ((3600000 * hour) + (60000 * min) + (sec * 1000)));
            if (!isUTC) {
                return time - ((long) offset);
            }
            return time;
        }
        throw new IllegalArgumentException("Invalid date time string format");
    }

    private static int offsetStrToMillis(String str) {
        boolean isValid = false;
        int sign = 0;
        int hour = 0;
        int min = 0;
        int sec = 0;
        if (str != null) {
            int length = str.length();
            if (length == 5 || length == 7) {
                char s = str.charAt(0);
                if (s == '+') {
                    sign = 1;
                } else if (s == '-') {
                    sign = -1;
                }
                try {
                    hour = Integer.parseInt(str.substring(1, 3));
                    min = Integer.parseInt(str.substring(3, 5));
                    if (length == 7) {
                        sec = Integer.parseInt(str.substring(5, 7));
                    }
                    isValid = true;
                } catch (NumberFormatException e) {
                }
            }
        }
        if (isValid) {
            return ((((hour * 60) + min) * 60) + sec) * sign * 1000;
        }
        throw new IllegalArgumentException("Bad offset string");
    }

    private static String millisToOffset(int millis) {
        StringBuilder sb = new StringBuilder(7);
        if (millis >= 0) {
            sb.append('+');
        } else {
            sb.append('-');
            millis = -millis;
        }
        int t = millis / 1000;
        int sec = t % 60;
        int t2 = (t - sec) / 60;
        sb.append(numToString(t2 / 60, 2));
        sb.append(numToString(t2 % 60, 2));
        sb.append(numToString(sec, 2));
        return sb.toString();
    }

    private static String numToString(int num, int width) {
        String str = Integer.toString(num);
        int len = str.length();
        if (len >= width) {
            return str.substring(len - width, len);
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = len; i < width; i++) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString();
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    public TimeZone cloneAsThawed() {
        VTimeZone vtz = (VTimeZone) super.cloneAsThawed();
        vtz.tz = (BasicTimeZone) this.tz.cloneAsThawed();
        vtz.isFrozen = false;
        return vtz;
    }
}
