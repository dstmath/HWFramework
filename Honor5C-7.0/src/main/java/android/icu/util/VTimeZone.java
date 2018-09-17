package android.icu.util;

import android.icu.impl.Grego;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import libcore.icu.RelativeDateTimeFormatter;

public class VTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private static final String[] ICAL_DOW_NAMES = null;
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
    private static final int[] MONTHLENGTH = null;
    private static final String NEWLINE = "\r\n";
    private static final String SEMICOLON = ";";
    private static final int TZI = 2;
    private static final int VTZ = 1;
    private static final long serialVersionUID = -6851467294127795902L;
    private volatile transient boolean isFrozen;
    private Date lastmod;
    private String olsonzid;
    private BasicTimeZone tz;
    private String tzurl;
    private List<String> vtzlines;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.VTimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.VTimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.VTimeZone.<clinit>():void");
    }

    public static VTimeZone create(String tzid) {
        VTimeZone vtz = new VTimeZone(tzid);
        vtz.tz = (BasicTimeZone) TimeZone.getTimeZone(tzid, INI);
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
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        this.tz.setRawOffset(offsetMillis);
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
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        this.tzurl = url;
    }

    public Date getLastModified() {
        return this.lastmod;
    }

    public void setLastModified(Date date) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        this.lastmod = date;
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
        String[] strArr = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            strArr = new String[VTZ];
            strArr[INI] = "X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "]";
        }
        writeZone(writer, this.tz, strArr);
    }

    public void write(Writer writer, long start) throws IOException {
        TimeZoneRule[] rules = this.tz.getTimeZoneRules(start);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) rules[INI]);
        for (int i = VTZ; i < rules.length; i += VTZ) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            customProperties = new String[VTZ];
            customProperties[INI] = "X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Partial@" + start + "]";
        }
        writeZone(writer, rbtz, customProperties);
    }

    public void writeSimple(Writer writer, long time) throws IOException {
        TimeZoneRule[] rules = this.tz.getSimpleTimeZoneRulesNear(time);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) rules[INI]);
        for (int i = VTZ; i < rules.length; i += VTZ) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            customProperties = new String[VTZ];
            customProperties[INI] = "X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Simple@" + time + "]";
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
        this.olsonzid = null;
        this.tzurl = null;
        this.lastmod = null;
        this.isFrozen = -assertionsDisabled;
    }

    private VTimeZone(String tzid) {
        super(tzid);
        this.olsonzid = null;
        this.tzurl = null;
        this.lastmod = null;
        this.isFrozen = -assertionsDisabled;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean load(Reader reader) {
        try {
            this.vtzlines = new LinkedList();
            boolean eol = -assertionsDisabled;
            boolean start = -assertionsDisabled;
            boolean success = -assertionsDisabled;
            StringBuilder line = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                } else if (ch != 13) {
                    if (eol) {
                        if (!(ch == 9 || ch == 32)) {
                            if (start && line.length() > 0) {
                                this.vtzlines.add(line.toString());
                            }
                            line.setLength(INI);
                            if (ch != 10) {
                                line.append((char) ch);
                            }
                        }
                        eol = -assertionsDisabled;
                    } else if (ch == 10) {
                        eol = true;
                        if (start) {
                            if (line.toString().startsWith(ICAL_END_VTIMEZONE)) {
                                break;
                            }
                        } else if (line.toString().startsWith(ICAL_BEGIN_VTIMEZONE)) {
                            this.vtzlines.add(line.toString());
                            line.setLength(INI);
                            start = true;
                            eol = -assertionsDisabled;
                        }
                    } else {
                        line.append((char) ch);
                    }
                }
                if (success) {
                    return -assertionsDisabled;
                }
                return parse();
            }
            this.vtzlines.add(line.toString());
            success = true;
            if (success) {
                return parse();
            }
            return -assertionsDisabled;
        } catch (IOException e) {
            return -assertionsDisabled;
        }
    }

    private boolean parse() {
        if (this.vtzlines == null || this.vtzlines.size() == 0) {
            return -assertionsDisabled;
        }
        String tzid = null;
        int state = INI;
        boolean dst = -assertionsDisabled;
        String from = null;
        String to = null;
        String tzname = null;
        String dtstart = null;
        boolean isRRULE = -assertionsDisabled;
        List dates = null;
        List<TimeZoneRule> rules = new ArrayList();
        int initialRawOffset = INI;
        int initialDSTSavings = INI;
        long firstStart = MAX_TIME;
        for (String line : this.vtzlines) {
            int valueSep = line.indexOf(COLON);
            if (valueSep >= 0) {
                String name = line.substring(INI, valueSep);
                String value = line.substring(valueSep + VTZ);
                switch (state) {
                    case INI /*0*/:
                        if (name.equals(ICAL_BEGIN)) {
                            if (value.equals(ICAL_VTIMEZONE)) {
                                state = VTZ;
                                break;
                            }
                        }
                        break;
                    case VTZ /*1*/:
                        if (!name.equals(ICAL_TZID)) {
                            if (!name.equals(ICAL_TZURL)) {
                                if (!name.equals(ICAL_LASTMOD)) {
                                    if (!name.equals(ICAL_BEGIN)) {
                                        if (name.equals(ICAL_END)) {
                                            break;
                                        }
                                    }
                                    boolean isDST = value.equals(ICAL_DAYLIGHT);
                                    if (value.equals(ICAL_STANDARD) || isDST) {
                                        if (tzid != null) {
                                            dates = null;
                                            isRRULE = -assertionsDisabled;
                                            from = null;
                                            to = null;
                                            tzname = null;
                                            dst = isDST;
                                            state = TZI;
                                            break;
                                        }
                                        state = ERR;
                                        break;
                                    }
                                    state = ERR;
                                    break;
                                }
                                this.lastmod = new Date(parseDateTimeString(value, INI));
                                break;
                            }
                            this.tzurl = value;
                            break;
                        }
                        tzid = value;
                        break;
                        break;
                    case TZI /*2*/:
                        if (!name.equals(ICAL_DTSTART)) {
                            if (!name.equals(ICAL_TZNAME)) {
                                if (!name.equals(ICAL_TZOFFSETFROM)) {
                                    if (!name.equals(ICAL_TZOFFSETTO)) {
                                        if (name.equals(ICAL_RDATE)) {
                                            if (!isRRULE) {
                                                if (dates == null) {
                                                    dates = new LinkedList();
                                                }
                                                StringTokenizer stringTokenizer = new StringTokenizer(value, COMMA);
                                                while (stringTokenizer.hasMoreTokens()) {
                                                    dates.add(stringTokenizer.nextToken());
                                                }
                                                break;
                                            }
                                            state = ERR;
                                            break;
                                        }
                                        if (name.equals(ICAL_RRULE)) {
                                            if (!isRRULE && dates != null) {
                                                state = ERR;
                                                break;
                                            }
                                            if (dates == null) {
                                                dates = new LinkedList();
                                            }
                                            isRRULE = true;
                                            dates.add(value);
                                            break;
                                        }
                                        if (name.equals(ICAL_END)) {
                                            if (dtstart != null && from != null && to != null) {
                                                if (tzname == null) {
                                                    tzname = getDefaultTZName(tzid, dst);
                                                }
                                                TimeZoneRule timeZoneRule = null;
                                                try {
                                                    int rawOffset;
                                                    int dstSavings;
                                                    int fromOffset = offsetStrToMillis(from);
                                                    int toOffset = offsetStrToMillis(to);
                                                    if (!dst) {
                                                        rawOffset = toOffset;
                                                        dstSavings = INI;
                                                    } else if (toOffset - fromOffset > 0) {
                                                        rawOffset = fromOffset;
                                                        dstSavings = toOffset - fromOffset;
                                                    } else {
                                                        rawOffset = toOffset - DEF_DSTSAVINGS;
                                                        dstSavings = DEF_DSTSAVINGS;
                                                    }
                                                    long start = parseDateTimeString(dtstart, fromOffset);
                                                    if (isRRULE) {
                                                        timeZoneRule = createRuleByRRULE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                                                    } else {
                                                        timeZoneRule = createRuleByRDATE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                                                    }
                                                    if (timeZoneRule != null) {
                                                        Date actualStart = timeZoneRule.getFirstStart(fromOffset, INI);
                                                        if (actualStart.getTime() < firstStart) {
                                                            firstStart = actualStart.getTime();
                                                            if (dstSavings > 0) {
                                                                initialRawOffset = fromOffset;
                                                                initialDSTSavings = INI;
                                                            } else if (fromOffset - toOffset == DEF_DSTSAVINGS) {
                                                                initialRawOffset = fromOffset - DEF_DSTSAVINGS;
                                                                initialDSTSavings = DEF_DSTSAVINGS;
                                                            } else {
                                                                initialRawOffset = fromOffset;
                                                                initialDSTSavings = INI;
                                                            }
                                                        }
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                }
                                                if (timeZoneRule != null) {
                                                    rules.add(timeZoneRule);
                                                    state = VTZ;
                                                    break;
                                                }
                                                state = ERR;
                                                break;
                                            }
                                            state = ERR;
                                            break;
                                        }
                                    }
                                    to = value;
                                    break;
                                }
                                from = value;
                                break;
                            }
                            tzname = value;
                            break;
                        }
                        dtstart = value;
                        break;
                        break;
                }
                if (state == ERR) {
                    this.vtzlines = null;
                    return -assertionsDisabled;
                }
            }
        }
        if (rules.size() == 0) {
            return -assertionsDisabled;
        }
        int i;
        BasicTimeZone ruleBasedTimeZone = new RuleBasedTimeZone(tzid, new InitialTimeZoneRule(getDefaultTZName(tzid, -assertionsDisabled), initialRawOffset, initialDSTSavings));
        int finalRuleIdx = -1;
        int finalRuleCount = INI;
        for (i = INI; i < rules.size(); i += VTZ) {
            TimeZoneRule r = (TimeZoneRule) rules.get(i);
            if ((r instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) r).getEndYear() == Integer.MAX_VALUE) {
                finalRuleCount += VTZ;
                finalRuleIdx = i;
            }
        }
        if (finalRuleCount > TZI) {
            return -assertionsDisabled;
        }
        if (finalRuleCount == VTZ) {
            if (rules.size() == VTZ) {
                rules.clear();
            } else {
                TimeZoneRule newRule;
                AnnualTimeZoneRule finalRule = (AnnualTimeZoneRule) rules.get(finalRuleIdx);
                int tmpRaw = finalRule.getRawOffset();
                int tmpDST = finalRule.getDSTSavings();
                Date finalStart = finalRule.getFirstStart(initialRawOffset, initialDSTSavings);
                Date start2 = finalStart;
                for (i = INI; i < rules.size(); i += VTZ) {
                    if (finalRuleIdx != i) {
                        r = (TimeZoneRule) rules.get(i);
                        Date lastStart = r.getFinalStart(tmpRaw, tmpDST);
                        if (lastStart.after(start2)) {
                            start2 = finalRule.getNextStart(lastStart.getTime(), r.getRawOffset(), r.getDSTSavings(), -assertionsDisabled);
                        }
                    }
                }
                if (start2 == finalStart) {
                    String name2 = finalRule.getName();
                    int rawOffset2 = finalRule.getRawOffset();
                    int dSTSavings = finalRule.getDSTSavings();
                    long[] jArr = new long[VTZ];
                    jArr[INI] = finalStart.getTime();
                    newRule = new TimeArrayTimeZoneRule(name2, rawOffset2, dSTSavings, jArr, TZI);
                } else {
                    newRule = new AnnualTimeZoneRule(finalRule.getName(), finalRule.getRawOffset(), finalRule.getDSTSavings(), finalRule.getRule(), finalRule.getStartYear(), Grego.timeToFields(start2.getTime(), null)[INI]);
                }
                rules.set(finalRuleIdx, newRule);
            }
        }
        for (TimeZoneRule addTransitionRule : rules) {
            ruleBasedTimeZone.addTransitionRule(addTransitionRule);
        }
        this.tz = ruleBasedTimeZone;
        setID(tzid);
        return true;
    }

    private static String getDefaultTZName(String tzid, boolean isDST) {
        if (isDST) {
            return tzid + "(DST)";
        }
        return tzid + "(STD)";
    }

    private static TimeZoneRule createRuleByRRULE(String tzname, int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        if (dates == null || dates.size() == 0) {
            return null;
        }
        long[] until = new long[VTZ];
        int[] ruleFields = parseRRULE((String) dates.get(INI), until);
        if (ruleFields == null) {
            return null;
        }
        DateTimeRule adtr;
        int month = ruleFields[INI];
        int dayOfWeek = ruleFields[VTZ];
        int nthDayOfWeek = ruleFields[TZI];
        int dayOfMonth = ruleFields[ERR];
        int i;
        int j;
        if (dates.size() == VTZ) {
            if (ruleFields.length > 4) {
                if (ruleFields.length != 10 || month == -1 || dayOfWeek == 0) {
                    return null;
                }
                int firstDay = 31;
                int[] days = new int[7];
                i = INI;
                while (i < 7) {
                    days[i] = ruleFields[i + ERR];
                    days[i] = days[i] > 0 ? days[i] : (MONTHLENGTH[month] + days[i]) + VTZ;
                    if (days[i] < firstDay) {
                        firstDay = days[i];
                    }
                    i += VTZ;
                }
                for (i = VTZ; i < 7; i += VTZ) {
                    boolean found = -assertionsDisabled;
                    for (j = INI; j < 7; j += VTZ) {
                        if (days[j] == firstDay + i) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return null;
                    }
                }
                dayOfMonth = firstDay;
            }
        } else if (month == -1 || dayOfWeek == 0 || dayOfMonth == 0) {
            return null;
        } else {
            if (dates.size() > 7) {
                return null;
            }
            int dom;
            int earliestMonth = month;
            int daysCount = ruleFields.length - 3;
            int earliestDay = 31;
            for (i = INI; i < daysCount; i += VTZ) {
                dom = ruleFields[i + ERR];
                if (dom <= 0) {
                    dom = (MONTHLENGTH[month] + dom) + VTZ;
                }
                if (dom < earliestDay) {
                    earliestDay = dom;
                }
            }
            int anotherMonth = -1;
            for (i = VTZ; i < dates.size(); i += VTZ) {
                long[] unt = new long[VTZ];
                int[] fields = parseRRULE((String) dates.get(i), unt);
                if (unt[INI] > until[INI]) {
                    until = unt;
                }
                if (fields[INI] == -1 || fields[VTZ] == 0 || fields[ERR] == 0) {
                    return null;
                }
                int count = fields.length - 3;
                if (daysCount + count > 7) {
                    return null;
                }
                if (fields[VTZ] != dayOfWeek) {
                    return null;
                }
                if (fields[INI] != month) {
                    if (anotherMonth == -1) {
                        int diff = fields[INI] - month;
                        if (diff == -11 || diff == -1) {
                            anotherMonth = fields[INI];
                            earliestMonth = anotherMonth;
                            earliestDay = 31;
                        } else if (diff != 11 && diff != VTZ) {
                            return null;
                        } else {
                            anotherMonth = fields[INI];
                        }
                    } else if (!(fields[INI] == month || fields[INI] == anotherMonth)) {
                        return null;
                    }
                }
                if (fields[INI] == earliestMonth) {
                    for (j = INI; j < count; j += VTZ) {
                        dom = fields[j + ERR];
                        if (dom <= 0) {
                            dom = (MONTHLENGTH[fields[INI]] + dom) + VTZ;
                        }
                        if (dom < earliestDay) {
                            earliestDay = dom;
                        }
                    }
                }
                daysCount += count;
            }
            if (daysCount != 7) {
                return null;
            }
            month = earliestMonth;
            dayOfMonth = earliestDay;
        }
        int[] dfields = Grego.timeToFields(((long) fromOffset) + start, null);
        int startYear = dfields[INI];
        if (month == -1) {
            month = dfields[VTZ];
        }
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth == 0) {
            dayOfMonth = dfields[TZI];
        }
        int timeInDay = dfields[5];
        int endYear = AnnualTimeZoneRule.MAX_YEAR;
        if (until[INI] != MIN_TIME) {
            Grego.timeToFields(until[INI], dfields);
            endYear = dfields[INI];
        }
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            adtr = new DateTimeRule(month, dayOfMonth, timeInDay, INI);
        } else if (dayOfWeek != 0 && nthDayOfWeek != 0 && dayOfMonth == 0) {
            adtr = new DateTimeRule(month, nthDayOfWeek, dayOfWeek, timeInDay, INI);
        } else if (dayOfWeek == 0 || nthDayOfWeek != 0 || dayOfMonth == 0) {
            return null;
        } else {
            DateTimeRule dateTimeRule = new DateTimeRule(month, dayOfMonth, dayOfWeek, true, timeInDay, INI);
        }
        return new AnnualTimeZoneRule(tzname, rawOffset, dstSavings, adtr, startYear, endYear);
    }

    private static int[] parseRRULE(String rrule, long[] until) {
        int month = -1;
        int dayOfWeek = INI;
        int nthDayOfWeek = INI;
        int[] dayOfMonth = null;
        long untilTime = MIN_TIME;
        boolean yearly = -assertionsDisabled;
        boolean parseError = -assertionsDisabled;
        StringTokenizer stringTokenizer = new StringTokenizer(rrule, SEMICOLON);
        while (stringTokenizer.hasMoreTokens()) {
            String prop = stringTokenizer.nextToken();
            int sep = prop.indexOf(EQUALS_SIGN);
            if (sep == -1) {
                parseError = true;
                break;
            }
            String attr = prop.substring(INI, sep);
            String value = prop.substring(sep + VTZ);
            if (attr.equals(ICAL_FREQ)) {
                if (!value.equals(ICAL_YEARLY)) {
                    parseError = true;
                    break;
                }
                yearly = true;
            } else {
                if (attr.equals(ICAL_UNTIL)) {
                    try {
                        untilTime = parseDateTimeString(value, INI);
                    } catch (IllegalArgumentException e) {
                        parseError = true;
                    }
                } else {
                    if (!attr.equals(ICAL_BYMONTH)) {
                        if (attr.equals(ICAL_BYDAY)) {
                            int length = value.length();
                            if (length >= TZI && length <= 4) {
                                if (length > TZI) {
                                    int sign = VTZ;
                                    if (value.charAt(INI) != '+') {
                                        if (value.charAt(INI) != '-') {
                                            if (length == 4) {
                                                parseError = true;
                                                break;
                                            }
                                        }
                                        sign = -1;
                                    } else {
                                        sign = VTZ;
                                    }
                                    try {
                                        int n = Integer.parseInt(value.substring(length - 3, length - 2));
                                        if (n == 0 || n > 4) {
                                            parseError = true;
                                            break;
                                        }
                                        nthDayOfWeek = n * sign;
                                        value = value.substring(length - 2);
                                    } catch (NumberFormatException e2) {
                                        parseError = true;
                                    }
                                }
                                int wday = INI;
                                while (true) {
                                    if (wday >= ICAL_DOW_NAMES.length) {
                                        break;
                                    }
                                    if (value.equals(ICAL_DOW_NAMES[wday])) {
                                        break;
                                    }
                                    wday += VTZ;
                                }
                                if (wday >= ICAL_DOW_NAMES.length) {
                                    parseError = true;
                                    break;
                                }
                                dayOfWeek = wday + VTZ;
                            } else {
                                parseError = true;
                                break;
                            }
                        }
                        if (attr.equals(ICAL_BYMONTHDAY)) {
                            StringTokenizer days = new StringTokenizer(value, COMMA);
                            dayOfMonth = new int[days.countTokens()];
                            int index = INI;
                            while (days.hasMoreTokens()) {
                                int index2 = index + VTZ;
                                try {
                                    dayOfMonth[index] = Integer.parseInt(days.nextToken());
                                    index = index2;
                                } catch (NumberFormatException e3) {
                                    parseError = true;
                                }
                            }
                        }
                    } else if (value.length() > TZI) {
                        parseError = true;
                        break;
                    } else {
                        try {
                            month = Integer.parseInt(value) - 1;
                            if (month < 0 || month >= 12) {
                                parseError = true;
                                break;
                            }
                        } catch (NumberFormatException e4) {
                            parseError = true;
                        }
                    }
                }
            }
        }
        if (parseError) {
            return null;
        }
        if (!yearly) {
            return null;
        }
        int[] results;
        until[INI] = untilTime;
        if (dayOfMonth != null) {
            results = new int[(dayOfMonth.length + ERR)];
            int i = INI;
            while (true) {
                int length2 = dayOfMonth.length;
                if (i >= r0) {
                    break;
                }
                results[i + ERR] = dayOfMonth[i];
                i += VTZ;
            }
        } else {
            results = new int[4];
            results[ERR] = INI;
        }
        results[INI] = month;
        results[VTZ] = dayOfWeek;
        results[TZI] = nthDayOfWeek;
        return results;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static TimeZoneRule createRuleByRDATE(String tzname, int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        long[] times;
        if (dates == null || dates.size() == 0) {
            times = new long[VTZ];
            times[INI] = start;
        } else {
            times = new long[dates.size()];
            try {
                int idx = INI;
                for (String date : dates) {
                    int idx2;
                    try {
                        idx2 = idx + VTZ;
                        times[idx] = parseDateTimeString(date, fromOffset);
                        idx = idx2;
                    } catch (IllegalArgumentException e) {
                        idx2 = idx;
                    }
                }
            } catch (IllegalArgumentException e2) {
            }
        }
        return new TimeArrayTimeZoneRule(tzname, rawOffset, dstSavings, times, TZI);
    }

    private void writeZone(Writer w, BasicTimeZone basictz, String[] customProperties) throws IOException {
        writeHeader(w);
        if (customProperties != null && customProperties.length > 0) {
            for (int i = INI; i < customProperties.length; i += VTZ) {
                if (customProperties[i] != null) {
                    w.write(customProperties[i]);
                    w.write(NEWLINE);
                }
            }
        }
        long t = MIN_TIME;
        String dstName = null;
        int dstFromOffset = INI;
        int dstFromDSTSavings = INI;
        int dstToOffset = INI;
        int dstStartYear = INI;
        int dstMonth = INI;
        int dstDayOfWeek = INI;
        int dstWeekInMonth = INI;
        int dstMillisInDay = INI;
        long dstStartTime = DEF_TZSTARTTIME;
        long dstUntilTime = DEF_TZSTARTTIME;
        int dstCount = INI;
        AnnualTimeZoneRule finalDstRule = null;
        String stdName = null;
        int stdFromOffset = INI;
        int stdFromDSTSavings = INI;
        int stdToOffset = INI;
        int stdStartYear = INI;
        int stdMonth = INI;
        int stdDayOfWeek = INI;
        int stdWeekInMonth = INI;
        int stdMillisInDay = INI;
        long stdStartTime = DEF_TZSTARTTIME;
        long stdUntilTime = DEF_TZSTARTTIME;
        int stdCount = INI;
        AnnualTimeZoneRule finalStdRule = null;
        int[] dtfields = new int[6];
        boolean hasTransitions = -assertionsDisabled;
        while (true) {
            TimeZoneTransition tzt = basictz.getNextTransition(t, -assertionsDisabled);
            boolean isDst;
            if (tzt != null) {
                hasTransitions = true;
                t = tzt.getTime();
                String name = tzt.getTo().getName();
                isDst = tzt.getTo().getDSTSavings() != 0 ? true : -assertionsDisabled;
                int fromOffset = tzt.getFrom().getRawOffset() + tzt.getFrom().getDSTSavings();
                int fromDSTSavings = tzt.getFrom().getDSTSavings();
                int toOffset = tzt.getTo().getRawOffset() + tzt.getTo().getDSTSavings();
                Grego.timeToFields(tzt.getTime() + ((long) fromOffset), dtfields);
                int weekInMonth = Grego.getDayOfWeekInMonth(dtfields[INI], dtfields[VTZ], dtfields[TZI]);
                int year = dtfields[INI];
                boolean sameRule = -assertionsDisabled;
                if (!isDst) {
                    if (finalStdRule == null && (tzt.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) tzt.getTo()).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                        finalStdRule = (AnnualTimeZoneRule) tzt.getTo();
                    }
                    if (stdCount > 0) {
                        if (year == stdStartYear + stdCount && name.equals(stdName) && stdFromOffset == fromOffset && stdToOffset == toOffset && stdMonth == dtfields[VTZ] && stdDayOfWeek == dtfields[ERR] && stdWeekInMonth == weekInMonth && stdMillisInDay == dtfields[5]) {
                            stdUntilTime = t;
                            stdCount += VTZ;
                            sameRule = true;
                        }
                        if (!sameRule) {
                            if (stdCount == VTZ) {
                                writeZonePropsByTime(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdStartTime, true);
                            } else {
                                writeZonePropsByDOW(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);
                            }
                        }
                    }
                    if (!sameRule) {
                        stdName = name;
                        stdFromOffset = fromOffset;
                        stdFromDSTSavings = fromDSTSavings;
                        stdToOffset = toOffset;
                        stdStartYear = year;
                        stdMonth = dtfields[VTZ];
                        stdDayOfWeek = dtfields[ERR];
                        stdWeekInMonth = weekInMonth;
                        stdMillisInDay = dtfields[5];
                        stdUntilTime = t;
                        stdStartTime = t;
                        stdCount = VTZ;
                    }
                    if (!(finalStdRule == null || finalDstRule == null)) {
                        break;
                    }
                }
                if (finalDstRule == null && (tzt.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) tzt.getTo()).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                    finalDstRule = (AnnualTimeZoneRule) tzt.getTo();
                }
                if (dstCount > 0) {
                    if (year == dstStartYear + dstCount && name.equals(dstName) && dstFromOffset == fromOffset && dstToOffset == toOffset && dstMonth == dtfields[VTZ] && dstDayOfWeek == dtfields[ERR] && dstWeekInMonth == weekInMonth && dstMillisInDay == dtfields[5]) {
                        dstUntilTime = t;
                        dstCount += VTZ;
                        sameRule = true;
                    }
                    if (!sameRule) {
                        if (dstCount == VTZ) {
                            writeZonePropsByTime(w, true, dstName, dstFromOffset, dstToOffset, dstStartTime, true);
                        } else {
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);
                        }
                    }
                }
                if (!sameRule) {
                    dstName = name;
                    dstFromOffset = fromOffset;
                    dstFromDSTSavings = fromDSTSavings;
                    dstToOffset = toOffset;
                    dstStartYear = year;
                    dstMonth = dtfields[VTZ];
                    dstDayOfWeek = dtfields[ERR];
                    dstWeekInMonth = weekInMonth;
                    dstMillisInDay = dtfields[5];
                    dstUntilTime = t;
                    dstStartTime = t;
                    dstCount = VTZ;
                }
                if (!(finalStdRule == null || r51 == null)) {
                    break;
                }
            }
            break;
        }
        if (hasTransitions) {
            Date nextStart;
            if (dstCount > 0) {
                if (finalDstRule == null) {
                    if (dstCount == VTZ) {
                        writeZonePropsByTime(w, true, dstName, dstFromOffset, dstToOffset, dstStartTime, true);
                    } else {
                        writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);
                    }
                } else if (dstCount == VTZ) {
                    writeFinalRule(w, true, finalDstRule, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, dstStartTime);
                } else {
                    if (isEquivalentDateRule(dstMonth, dstWeekInMonth, dstDayOfWeek, finalDstRule.getRule())) {
                        writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, MAX_TIME);
                    } else {
                        writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset, dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);
                        nextStart = finalDstRule.getNextStart(dstUntilTime, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, -assertionsDisabled);
                        if (!-assertionsDisabled) {
                            if ((nextStart != null ? VTZ : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        if (nextStart != null) {
                            writeFinalRule(w, true, finalDstRule, dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, nextStart.getTime());
                        }
                    }
                }
            }
            if (stdCount > 0) {
                if (finalStdRule == null) {
                    if (stdCount == VTZ) {
                        writeZonePropsByTime(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdStartTime, true);
                    } else {
                        writeZonePropsByDOW(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);
                    }
                } else if (stdCount == VTZ) {
                    writeFinalRule(w, -assertionsDisabled, finalStdRule, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, stdStartTime);
                } else {
                    if (isEquivalentDateRule(stdMonth, stdWeekInMonth, stdDayOfWeek, finalStdRule.getRule())) {
                        writeZonePropsByDOW(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, MAX_TIME);
                    } else {
                        writeZonePropsByDOW(w, -assertionsDisabled, stdName, stdFromOffset, stdToOffset, stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);
                        nextStart = finalStdRule.getNextStart(stdUntilTime, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, -assertionsDisabled);
                        if (!-assertionsDisabled) {
                            if ((nextStart != null ? VTZ : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        if (nextStart != null) {
                            writeFinalRule(w, -assertionsDisabled, finalStdRule, stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, nextStart.getTime());
                        }
                    }
                }
            }
        } else {
            int offset = basictz.getOffset(DEF_TZSTARTTIME);
            isDst = offset != basictz.getRawOffset() ? true : -assertionsDisabled;
            writeZonePropsByTime(w, isDst, getDefaultTZName(basictz.getID(), isDst), offset, offset, DEF_TZSTARTTIME - ((long) offset), -assertionsDisabled);
        }
        writeFooter(w);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isEquivalentDateRule(int month, int weekInMonth, int dayOfWeek, DateTimeRule dtrule) {
        if (month != dtrule.getRuleMonth() || dayOfWeek != dtrule.getRuleDayOfWeek() || dtrule.getTimeRuleType() != 0) {
            return -assertionsDisabled;
        }
        if (dtrule.getDateRuleType() == VTZ && dtrule.getRuleWeekInMonth() == weekInMonth) {
            return true;
        }
        int ruleDOM = dtrule.getRuleDayOfMonth();
        if (dtrule.getDateRuleType() == TZI) {
            if (ruleDOM % 7 == VTZ && (ruleDOM + 6) / 7 == weekInMonth) {
                return true;
            }
            if (month != VTZ && (MONTHLENGTH[month] - ruleDOM) % 7 == 6 && weekInMonth == (((MONTHLENGTH[month] - ruleDOM) + VTZ) / 7) * -1) {
                return true;
            }
        }
        if (dtrule.getDateRuleType() == ERR) {
            if (ruleDOM % 7 == 0 && ruleDOM / 7 == weekInMonth) {
                return true;
            }
            return (month != VTZ && (MONTHLENGTH[month] - ruleDOM) % 7 == 0 && weekInMonth == (((MONTHLENGTH[month] - ruleDOM) / 7) + VTZ) * -1) ? true : -assertionsDisabled;
        }
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
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);
        beginRRULE(writer, month);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(dayOfMonth));
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(((long) fromOffset) + untilTime));
        }
        writer.write(NEWLINE);
        endZoneProps(writer, isDst);
    }

    private static void writeZonePropsByDOW(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int weekInMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);
        beginRRULE(writer, month);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(weekInMonth));
        writer.write(ICAL_DOW_NAMES[dayOfWeek - 1]);
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(((long) fromOffset) + untilTime));
        }
        writer.write(NEWLINE);
        endZoneProps(writer, isDst);
    }

    private static void writeZonePropsByDOW_GEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        if (dayOfMonth % 7 == VTZ) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, month, (dayOfMonth + 6) / 7, dayOfWeek, startTime, untilTime);
        } else if (month == VTZ || (MONTHLENGTH[month] - dayOfMonth) % 7 != 6) {
            beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);
            int startDay = dayOfMonth;
            int currentMonthDays = 7;
            if (dayOfMonth <= 0) {
                int prevMonthDays = 1 - dayOfMonth;
                currentMonthDays = 7 - prevMonthDays;
                writeZonePropsByDOW_GEQ_DOM_sub(writer, month + -1 < 0 ? 11 : month - 1, -prevMonthDays, dayOfWeek, prevMonthDays, MAX_TIME, fromOffset);
                startDay = VTZ;
            } else if (dayOfMonth + 6 > MONTHLENGTH[month]) {
                int nextMonthDays = (dayOfMonth + 6) - MONTHLENGTH[month];
                currentMonthDays = 7 - nextMonthDays;
                writeZonePropsByDOW_GEQ_DOM_sub(writer, month + VTZ > 11 ? INI : month + VTZ, VTZ, dayOfWeek, nextMonthDays, MAX_TIME, fromOffset);
            }
            writeZonePropsByDOW_GEQ_DOM_sub(writer, month, startDay, dayOfWeek, currentMonthDays, untilTime, fromOffset);
            endZoneProps(writer, isDst);
        } else {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, month, (((MONTHLENGTH[month] - dayOfMonth) + VTZ) / 7) * -1, dayOfWeek, startTime, untilTime);
        }
    }

    private static void writeZonePropsByDOW_GEQ_DOM_sub(Writer writer, int month, int dayOfMonth, int dayOfWeek, int numDays, long untilTime, int fromOffset) throws IOException {
        int startDayNum = dayOfMonth;
        boolean isFeb = month == VTZ ? true : -assertionsDisabled;
        if (dayOfMonth < 0 && !isFeb) {
            startDayNum = (MONTHLENGTH[month] + dayOfMonth) + VTZ;
        }
        beginRRULE(writer, month);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_DOW_NAMES[dayOfWeek - 1]);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(startDayNum));
        for (int i = VTZ; i < numDays; i += VTZ) {
            writer.write(COMMA);
            writer.write(Integer.toString(startDayNum + i));
        }
        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(((long) fromOffset) + untilTime));
        }
        writer.write(NEWLINE);
    }

    private static void writeZonePropsByDOW_LEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        if (dayOfMonth % 7 == 0) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, month, dayOfMonth / 7, dayOfWeek, startTime, untilTime);
        } else if (month != VTZ && (MONTHLENGTH[month] - dayOfMonth) % 7 == 0) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, month, (((MONTHLENGTH[month] - dayOfMonth) / 7) + VTZ) * -1, dayOfWeek, startTime, untilTime);
        } else if (month == VTZ && dayOfMonth == 29) {
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset, VTZ, -1, dayOfWeek, startTime, untilTime);
        } else {
            writeZonePropsByDOW_GEQ_DOM(writer, isDst, tzname, fromOffset, toOffset, month, dayOfMonth - 6, dayOfWeek, startTime, untilTime);
        }
    }

    private static void writeFinalRule(Writer writer, boolean isDst, AnnualTimeZoneRule rule, int fromRawOffset, int fromDSTSavings, long startTime) throws IOException {
        DateTimeRule dtrule = toWallTimeRule(rule.getRule(), fromRawOffset, fromDSTSavings);
        int timeInDay = dtrule.getRuleMillisInDay();
        if (timeInDay < 0) {
            startTime += (long) (0 - timeInDay);
        } else if (timeInDay >= 86400000) {
            startTime -= (long) (timeInDay - 86399999);
        }
        int toOffset = rule.getRawOffset() + rule.getDSTSavings();
        switch (dtrule.getDateRuleType()) {
            case INI /*0*/:
                writeZonePropsByDOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), startTime, MAX_TIME);
            case VTZ /*1*/:
                writeZonePropsByDOW(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset, dtrule.getRuleMonth(), dtrule.getRuleWeekInMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            case TZI /*2*/:
                writeZonePropsByDOW_GEQ_DOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            case ERR /*3*/:
                writeZonePropsByDOW_LEQ_DOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset, dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            default:
        }
    }

    private static DateTimeRule toWallTimeRule(DateTimeRule rule, int rawOffset, int dstSavings) {
        if (rule.getTimeRuleType() == 0) {
            return rule;
        }
        DateTimeRule modifiedRule;
        int wallt = rule.getRuleMillisInDay();
        if (rule.getTimeRuleType() == TZI) {
            wallt += rawOffset + dstSavings;
        } else if (rule.getTimeRuleType() == VTZ) {
            wallt += dstSavings;
        }
        int dshift = INI;
        if (wallt < 0) {
            dshift = -1;
            wallt += Grego.MILLIS_PER_DAY;
        } else if (wallt >= Grego.MILLIS_PER_DAY) {
            dshift = VTZ;
            wallt -= Grego.MILLIS_PER_DAY;
        }
        int month = rule.getRuleMonth();
        int dom = rule.getRuleDayOfMonth();
        int dow = rule.getRuleDayOfWeek();
        int dtype = rule.getDateRuleType();
        if (dshift != 0) {
            if (dtype == VTZ) {
                int wim = rule.getRuleWeekInMonth();
                if (wim > 0) {
                    dtype = TZI;
                    dom = ((wim - 1) * 7) + VTZ;
                } else {
                    dtype = ERR;
                    dom = MONTHLENGTH[month] + ((wim + VTZ) * 7);
                }
            }
            dom += dshift;
            if (dom == 0) {
                month--;
                if (month < 0) {
                    month = 11;
                }
                dom = MONTHLENGTH[month];
            } else if (dom > MONTHLENGTH[month]) {
                month += VTZ;
                if (month > 11) {
                    month = INI;
                }
                dom = VTZ;
            }
            if (dtype != 0) {
                dow += dshift;
                if (dow < VTZ) {
                    dow = 7;
                } else if (dow > 7) {
                    dow = VTZ;
                }
            }
        }
        if (dtype == 0) {
            modifiedRule = new DateTimeRule(month, dom, wallt, INI);
        } else {
            modifiedRule = new DateTimeRule(month, dom, dow, dtype == TZI ? true : -assertionsDisabled, wallt, INI);
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
        writer.write(Integer.toString(month + VTZ));
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
        sb.append(numToString(fields[INI], 4));
        sb.append(numToString(fields[VTZ] + VTZ, TZI));
        sb.append(numToString(fields[TZI], TZI));
        sb.append('T');
        int t = fields[5];
        int hour = t / DEF_DSTSAVINGS;
        t %= DEF_DSTSAVINGS;
        int min = t / Grego.MILLIS_PER_MINUTE;
        int sec = (t % Grego.MILLIS_PER_MINUTE) / Grego.MILLIS_PER_SECOND;
        sb.append(numToString(hour, TZI));
        sb.append(numToString(min, TZI));
        sb.append(numToString(sec, TZI));
        return sb.toString();
    }

    private static String getUTCDateTimeString(long time) {
        return getDateTimeString(time) + "Z";
    }

    private static long parseDateTimeString(String str, int offset) {
        int year = INI;
        int month = INI;
        int day = INI;
        int hour = INI;
        int min = INI;
        int sec = INI;
        boolean isUTC = -assertionsDisabled;
        boolean isValid = -assertionsDisabled;
        if (str != null) {
            int length = str.length();
            if ((length == 15 || length == 16) && str.charAt(8) == 'T') {
                if (length == 16) {
                    if (str.charAt(15) == 'Z') {
                        isUTC = true;
                    }
                }
                try {
                    year = Integer.parseInt(str.substring(INI, 4));
                    month = Integer.parseInt(str.substring(4, 6)) - 1;
                    day = Integer.parseInt(str.substring(6, 8));
                    hour = Integer.parseInt(str.substring(9, 11));
                    min = Integer.parseInt(str.substring(11, 13));
                    sec = Integer.parseInt(str.substring(13, 15));
                    int maxDayOfMonth = Grego.monthLength(year, month);
                    if (year >= 0 && month >= 0 && month <= 11 && day >= VTZ && day <= maxDayOfMonth && hour >= 0 && hour < 24 && min >= 0 && min < 60 && sec >= 0 && sec < 60) {
                        isValid = true;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        if (isValid) {
            long time = (Grego.fieldsToDay(year, month, day) * RelativeDateTimeFormatter.DAY_IN_MILLIS) + ((long) (((DEF_DSTSAVINGS * hour) + (Grego.MILLIS_PER_MINUTE * min)) + (sec * Grego.MILLIS_PER_SECOND)));
            if (isUTC) {
                return time;
            }
            return time - ((long) offset);
        }
        throw new IllegalArgumentException("Invalid date time string format");
    }

    private static int offsetStrToMillis(String str) {
        boolean isValid = -assertionsDisabled;
        int sign = INI;
        int hour = INI;
        int min = INI;
        int sec = INI;
        if (str != null) {
            int length = str.length();
            if (length == 5 || length == 7) {
                char s = str.charAt(INI);
                if (s == '+') {
                    sign = VTZ;
                } else if (s == '-') {
                    sign = -1;
                }
                try {
                    hour = Integer.parseInt(str.substring(VTZ, ERR));
                    min = Integer.parseInt(str.substring(ERR, 5));
                    if (length == 7) {
                        sec = Integer.parseInt(str.substring(5, 7));
                    }
                    isValid = true;
                } catch (NumberFormatException e) {
                }
            }
        }
        if (isValid) {
            return (((((hour * 60) + min) * 60) + sec) * sign) * Grego.MILLIS_PER_SECOND;
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
        int t = millis / Grego.MILLIS_PER_SECOND;
        int sec = t % 60;
        t = (t - sec) / 60;
        int min = t % 60;
        sb.append(numToString(t / 60, TZI));
        sb.append(numToString(min, TZI));
        sb.append(numToString(sec, TZI));
        return sb.toString();
    }

    private static String numToString(int num, int width) {
        String str = Integer.toString(num);
        int len = str.length();
        if (len >= width) {
            return str.substring(len - width, len);
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = len; i < width; i += VTZ) {
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
        vtz.isFrozen = -assertionsDisabled;
        return vtz;
    }
}
