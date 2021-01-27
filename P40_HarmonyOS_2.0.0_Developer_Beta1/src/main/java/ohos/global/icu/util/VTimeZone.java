package ohos.global.icu.util;

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
import ohos.global.icu.impl.Grego;
import ohos.workscheduler.WorkInfo;
import ohos.workschedulerservice.controller.WorkStatus;

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

    public static VTimeZone create(String str) {
        BasicTimeZone frozenICUTimeZone = TimeZone.getFrozenICUTimeZone(str, true);
        if (frozenICUTimeZone == null) {
            return null;
        }
        VTimeZone vTimeZone = new VTimeZone(str);
        vTimeZone.tz = (BasicTimeZone) frozenICUTimeZone.cloneAsThawed();
        vTimeZone.olsonzid = vTimeZone.tz.getID();
        return vTimeZone;
    }

    public static VTimeZone create(Reader reader) {
        VTimeZone vTimeZone = new VTimeZone();
        if (vTimeZone.load(reader)) {
            return vTimeZone;
        }
        return null;
    }

    @Override // ohos.global.icu.util.TimeZone
    public int getOffset(int i, int i2, int i3, int i4, int i5, int i6) {
        return this.tz.getOffset(i, i2, i3, i4, i5, i6);
    }

    @Override // ohos.global.icu.util.TimeZone
    public void getOffset(long j, boolean z, int[] iArr) {
        this.tz.getOffset(j, z, iArr);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    @Deprecated
    public void getOffsetFromLocal(long j, int i, int i2, int[] iArr) {
        this.tz.getOffsetFromLocal(j, i, i2, iArr);
    }

    @Override // ohos.global.icu.util.TimeZone
    public int getRawOffset() {
        return this.tz.getRawOffset();
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean inDaylightTime(Date date) {
        return this.tz.inDaylightTime(date);
    }

    @Override // ohos.global.icu.util.TimeZone
    public void setRawOffset(int i) {
        if (!isFrozen()) {
            this.tz.setRawOffset(i);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean useDaylightTime() {
        return this.tz.useDaylightTime();
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean observesDaylightTime() {
        return this.tz.observesDaylightTime();
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean hasSameRules(TimeZone timeZone) {
        if (this == timeZone) {
            return true;
        }
        if (timeZone instanceof VTimeZone) {
            return this.tz.hasSameRules(((VTimeZone) timeZone).tz);
        }
        return this.tz.hasSameRules(timeZone);
    }

    public String getTZURL() {
        return this.tzurl;
    }

    public void setTZURL(String str) {
        if (!isFrozen()) {
            this.tzurl = str;
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
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        List<String> list = this.vtzlines;
        if (list != null) {
            for (String str : list) {
                if (str.startsWith("TZURL:")) {
                    if (this.tzurl != null) {
                        bufferedWriter.write(ICAL_TZURL);
                        bufferedWriter.write(COLON);
                        bufferedWriter.write(this.tzurl);
                        bufferedWriter.write(NEWLINE);
                    }
                } else if (!str.startsWith("LAST-MODIFIED:")) {
                    bufferedWriter.write(str);
                    bufferedWriter.write(NEWLINE);
                } else if (this.lastmod != null) {
                    bufferedWriter.write(ICAL_LASTMOD);
                    bufferedWriter.write(COLON);
                    bufferedWriter.write(getUTCDateTimeString(this.lastmod.getTime()));
                    bufferedWriter.write(NEWLINE);
                }
            }
            bufferedWriter.flush();
            return;
        }
        String[] strArr = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            strArr = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "]"};
        }
        writeZone(writer, this.tz, strArr);
    }

    public void write(Writer writer, long j) throws IOException {
        TimeZoneRule[] timeZoneRules = this.tz.getTimeZoneRules(j);
        RuleBasedTimeZone ruleBasedTimeZone = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) timeZoneRules[0]);
        for (int i = 1; i < timeZoneRules.length; i++) {
            ruleBasedTimeZone.addTransitionRule(timeZoneRules[i]);
        }
        String[] strArr = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            strArr = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Partial@" + j + "]"};
        }
        writeZone(writer, ruleBasedTimeZone, strArr);
    }

    public void writeSimple(Writer writer, long j) throws IOException {
        TimeZoneRule[] simpleTimeZoneRulesNear = this.tz.getSimpleTimeZoneRulesNear(j);
        RuleBasedTimeZone ruleBasedTimeZone = new RuleBasedTimeZone(this.tz.getID(), (InitialTimeZoneRule) simpleTimeZoneRulesNear[0]);
        for (int i = 1; i < simpleTimeZoneRulesNear.length; i++) {
            ruleBasedTimeZone.addTransitionRule(simpleTimeZoneRulesNear[i]);
        }
        String[] strArr = null;
        if (!(this.olsonzid == null || ICU_TZVERSION == null)) {
            strArr = new String[]{"X-TZINFO:" + this.olsonzid + "[" + ICU_TZVERSION + "/Simple@" + j + "]"};
        }
        writeZone(writer, ruleBasedTimeZone, strArr);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneTransition getNextTransition(long j, boolean z) {
        return this.tz.getNextTransition(j, z);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneTransition getPreviousTransition(long j, boolean z) {
        return this.tz.getPreviousTransition(j, z);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public boolean hasEquivalentTransitions(TimeZone timeZone, long j, long j2) {
        if (this == timeZone) {
            return true;
        }
        return this.tz.hasEquivalentTransitions(timeZone, j, j2);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneRule[] getTimeZoneRules() {
        return this.tz.getTimeZoneRules();
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneRule[] getTimeZoneRules(long j) {
        return this.tz.getTimeZoneRules(j);
    }

    @Override // ohos.global.icu.util.TimeZone, java.lang.Object
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    static {
        try {
            ICU_TZVERSION = TimeZone.getTZDataVersion();
        } catch (MissingResourceException unused) {
            ICU_TZVERSION = null;
        }
    }

    private VTimeZone() {
    }

    private VTimeZone(String str) {
        super(str);
    }

    private boolean load(Reader reader) {
        boolean z;
        try {
            this.vtzlines = new LinkedList();
            StringBuilder sb = new StringBuilder();
            boolean z2 = false;
            boolean z3 = false;
            while (true) {
                int read = reader.read();
                z = true;
                if (read == -1) {
                    if (!z2 || !sb.toString().startsWith(ICAL_END_VTIMEZONE)) {
                        z = false;
                    } else {
                        this.vtzlines.add(sb.toString());
                    }
                } else if (read != 13) {
                    if (z3) {
                        if (!(read == 9 || read == 32)) {
                            if (z2 && sb.length() > 0) {
                                this.vtzlines.add(sb.toString());
                            }
                            sb.setLength(0);
                            if (read != 10) {
                                sb.append((char) read);
                            }
                        }
                        z3 = false;
                    } else if (read == 10) {
                        if (z2) {
                            if (sb.toString().startsWith(ICAL_END_VTIMEZONE)) {
                                this.vtzlines.add(sb.toString());
                                break;
                            }
                        } else if (sb.toString().startsWith(ICAL_BEGIN_VTIMEZONE)) {
                            this.vtzlines.add(sb.toString());
                            sb.setLength(0);
                            z3 = false;
                            z2 = true;
                        }
                        z3 = true;
                    } else {
                        sb.append((char) read);
                    }
                }
            }
            if (!z) {
                return false;
            }
            return parse();
        } catch (IOException unused) {
            return false;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v6, types: [ohos.global.icu.util.TimeArrayTimeZoneRule] */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0200, code lost:
        if (r6.equals(ohos.global.icu.util.VTimeZone.ICAL_VTIMEZONE) != false) goto L_0x0202;
     */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0210 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x017b  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x017d  */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean parse() {
        AnnualTimeZoneRule annualTimeZoneRule;
        int i;
        Iterator<String> it;
        char c;
        String str;
        boolean z;
        String str2;
        TimeZoneRule timeZoneRule;
        int i2;
        int i3;
        int i4;
        List<String> list = this.vtzlines;
        boolean z2 = false;
        if (list == null || list.size() == 0) {
            return false;
        }
        ArrayList<TimeZoneRule> arrayList = new ArrayList();
        Iterator<String> it2 = this.vtzlines.iterator();
        int i5 = 0;
        char c2 = 0;
        boolean z3 = false;
        boolean z4 = false;
        long j = Long.MAX_VALUE;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        LinkedList linkedList = null;
        String str6 = null;
        String str7 = null;
        int i6 = 0;
        while (it2.hasNext()) {
            String next = it2.next();
            int indexOf = next.indexOf(COLON);
            if (indexOf >= 0) {
                int i7 = z2 ? 1 : 0;
                int i8 = z2 ? 1 : 0;
                int i9 = z2 ? 1 : 0;
                int i10 = z2 ? 1 : 0;
                String substring = next.substring(i7, indexOf);
                String substring2 = next.substring(indexOf + 1);
                if (c2 != 0) {
                    if (c2 != 1) {
                        if (c2 == 2) {
                            if (substring.equals(ICAL_DTSTART)) {
                                it = it2;
                                str5 = substring2;
                            } else if (substring.equals(ICAL_TZNAME)) {
                                it = it2;
                                str4 = substring2;
                            } else if (substring.equals(ICAL_TZOFFSETFROM)) {
                                it = it2;
                                str6 = substring2;
                            } else if (substring.equals(ICAL_TZOFFSETTO)) {
                                it = it2;
                                str7 = substring2;
                            } else {
                                if (substring.equals(ICAL_RDATE)) {
                                    if (!z3) {
                                        if (linkedList == null) {
                                            linkedList = new LinkedList();
                                        }
                                        StringTokenizer stringTokenizer = new StringTokenizer(substring2, COMMA);
                                        while (stringTokenizer.hasMoreTokens()) {
                                            linkedList.add(stringTokenizer.nextToken());
                                        }
                                        linkedList = linkedList;
                                        it = it2;
                                    }
                                } else if (substring.equals(ICAL_RRULE)) {
                                    if (z3 || linkedList == null) {
                                        if (linkedList == null) {
                                            linkedList = new LinkedList();
                                        }
                                        linkedList.add(substring2);
                                        linkedList = linkedList;
                                        it = it2;
                                        c = 3;
                                        z3 = true;
                                        if (c2 == c) {
                                            this.vtzlines = null;
                                            return false;
                                        }
                                        it2 = it;
                                        z2 = false;
                                    }
                                } else if (substring.equals(ICAL_END)) {
                                    if (str5 == null || str6 == null || str7 == null) {
                                        it = it2;
                                        z = z4;
                                        str = str5;
                                    } else {
                                        if (str4 == null) {
                                            str2 = getDefaultTZName(str3, z4);
                                        } else {
                                            str2 = str4;
                                        }
                                        try {
                                            int offsetStrToMillis = offsetStrToMillis(str6);
                                            int offsetStrToMillis2 = offsetStrToMillis(str7);
                                            if (z4) {
                                                int i11 = offsetStrToMillis2 - offsetStrToMillis;
                                                if (i11 > 0) {
                                                    i3 = offsetStrToMillis;
                                                    i2 = i11;
                                                } else {
                                                    i3 = offsetStrToMillis2 - DEF_DSTSAVINGS;
                                                    i2 = DEF_DSTSAVINGS;
                                                }
                                            } else {
                                                i3 = offsetStrToMillis2;
                                                i2 = 0;
                                            }
                                            long parseDateTimeString = parseDateTimeString(str5, offsetStrToMillis);
                                            if (z3) {
                                                it = it2;
                                                z = z4;
                                                i4 = DEF_DSTSAVINGS;
                                                str = str5;
                                                try {
                                                    timeZoneRule = createRuleByRRULE(str2, i3, i2, parseDateTimeString, linkedList, offsetStrToMillis);
                                                } catch (IllegalArgumentException unused) {
                                                    timeZoneRule = null;
                                                    if (timeZoneRule == null) {
                                                    }
                                                }
                                            } else {
                                                it = it2;
                                                z = z4;
                                                i4 = DEF_DSTSAVINGS;
                                                str = str5;
                                                timeZoneRule = createRuleByRDATE(str2, i3, i2, parseDateTimeString, linkedList, offsetStrToMillis);
                                            }
                                            if (timeZoneRule != null) {
                                                try {
                                                    Date firstStart = timeZoneRule.getFirstStart(offsetStrToMillis, 0);
                                                    if (firstStart.getTime() < j) {
                                                        long time = firstStart.getTime();
                                                        if (i2 <= 0 && offsetStrToMillis - offsetStrToMillis2 == i4) {
                                                            i5 = i4;
                                                            i6 = offsetStrToMillis - i4;
                                                            j = time;
                                                        } else {
                                                            i6 = offsetStrToMillis;
                                                            j = time;
                                                            i5 = 0;
                                                        }
                                                    }
                                                } catch (IllegalArgumentException unused2) {
                                                }
                                            }
                                        } catch (IllegalArgumentException unused3) {
                                            it = it2;
                                            z = z4;
                                            str = str5;
                                            timeZoneRule = null;
                                            if (timeZoneRule == null) {
                                            }
                                        }
                                        if (timeZoneRule == null) {
                                            str4 = str2;
                                        } else {
                                            arrayList.add(timeZoneRule);
                                            str4 = str2;
                                        }
                                    }
                                }
                                it = it2;
                                c = 3;
                                c2 = 3;
                                if (c2 == c) {
                                }
                            }
                            c = 3;
                            if (c2 == c) {
                            }
                        }
                        it = it2;
                        z = z4;
                        str = str5;
                        z4 = z;
                        str5 = str;
                        c = 3;
                        if (c2 == c) {
                        }
                    } else {
                        it = it2;
                        z = z4;
                        str = str5;
                        if (substring.equals(ICAL_TZID)) {
                            str3 = substring2;
                        } else if (substring.equals(ICAL_TZURL)) {
                            this.tzurl = substring2;
                        } else if (substring.equals(ICAL_LASTMOD)) {
                            this.lastmod = new Date(parseDateTimeString(substring2, 0));
                        } else if (substring.equals(ICAL_BEGIN)) {
                            boolean equals = substring2.equals(ICAL_DAYLIGHT);
                            if ((substring2.equals(ICAL_STANDARD) || equals) && str3 != null) {
                                z4 = equals;
                                str5 = str;
                                c = 3;
                                c2 = 2;
                                z3 = false;
                                str4 = null;
                                linkedList = null;
                                str6 = null;
                                str7 = null;
                                if (c2 == c) {
                                }
                            }
                        } else {
                            substring.equals(ICAL_END);
                        }
                        z4 = z;
                        str5 = str;
                        c = 3;
                        if (c2 == c) {
                        }
                    }
                    z4 = z;
                    str5 = str;
                    c = 3;
                    c2 = 3;
                    if (c2 == c) {
                    }
                } else {
                    it = it2;
                    z = z4;
                    str = str5;
                    if (substring.equals(ICAL_BEGIN)) {
                    }
                    z4 = z;
                    str5 = str;
                    c = 3;
                    if (c2 == c) {
                    }
                }
                z4 = z;
                str5 = str;
                c = 3;
                c2 = 1;
                if (c2 == c) {
                }
            }
        }
        if (arrayList.size() == 0) {
            return z2;
        }
        RuleBasedTimeZone ruleBasedTimeZone = new RuleBasedTimeZone(str3, new InitialTimeZoneRule(getDefaultTZName(str3, z2), i6, i5));
        int i12 = -1;
        int i13 = 0;
        for (int i14 = 0; i14 < arrayList.size(); i14++) {
            TimeZoneRule timeZoneRule2 = (TimeZoneRule) arrayList.get(i14);
            if ((timeZoneRule2 instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) timeZoneRule2).getEndYear() == Integer.MAX_VALUE) {
                i13++;
                i12 = i14;
            }
        }
        if (i13 > 2) {
            return false;
        }
        if (i13 == 1) {
            if (arrayList.size() == 1) {
                arrayList.clear();
            } else {
                AnnualTimeZoneRule annualTimeZoneRule2 = (AnnualTimeZoneRule) arrayList.get(i12);
                int rawOffset = annualTimeZoneRule2.getRawOffset();
                int dSTSavings = annualTimeZoneRule2.getDSTSavings();
                Date firstStart2 = annualTimeZoneRule2.getFirstStart(i6, i5);
                Date date = firstStart2;
                int i15 = 0;
                while (i15 < arrayList.size()) {
                    if (i12 != i15) {
                        TimeZoneRule timeZoneRule3 = (TimeZoneRule) arrayList.get(i15);
                        Date finalStart = timeZoneRule3.getFinalStart(rawOffset, dSTSavings);
                        if (finalStart.after(date)) {
                            i = dSTSavings;
                            date = annualTimeZoneRule2.getNextStart(finalStart.getTime(), timeZoneRule3.getRawOffset(), timeZoneRule3.getDSTSavings(), false);
                            i15++;
                            dSTSavings = i;
                        }
                    }
                    i = dSTSavings;
                    i15++;
                    dSTSavings = i;
                }
                if (date == firstStart2) {
                    annualTimeZoneRule = new TimeArrayTimeZoneRule(annualTimeZoneRule2.getName(), annualTimeZoneRule2.getRawOffset(), annualTimeZoneRule2.getDSTSavings(), new long[]{firstStart2.getTime()}, 2);
                } else {
                    annualTimeZoneRule = new AnnualTimeZoneRule(annualTimeZoneRule2.getName(), annualTimeZoneRule2.getRawOffset(), annualTimeZoneRule2.getDSTSavings(), annualTimeZoneRule2.getRule(), annualTimeZoneRule2.getStartYear(), Grego.timeToFields(date.getTime(), (int[]) null)[0]);
                }
                arrayList.set(i12, annualTimeZoneRule);
            }
        }
        for (TimeZoneRule timeZoneRule4 : arrayList) {
            ruleBasedTimeZone.addTransitionRule(timeZoneRule4);
        }
        this.tz = ruleBasedTimeZone;
        setID(str3);
        return true;
    }

    private static String getDefaultTZName(String str, boolean z) {
        if (z) {
            return str + "(DST)";
        }
        return str + "(STD)";
    }

    private static TimeZoneRule createRuleByRRULE(String str, int i, int i2, long j, List<String> list, int i3) {
        int i4;
        int[] iArr;
        DateTimeRule dateTimeRule;
        int length;
        int i5;
        boolean z;
        if (list == null || list.size() == 0) {
            return null;
        }
        long[] jArr = new long[1];
        int[] parseRRULE = parseRRULE(list.get(0), jArr);
        if (parseRRULE == null) {
            return null;
        }
        int i6 = parseRRULE[0];
        int i7 = parseRRULE[1];
        int i8 = parseRRULE[2];
        int i9 = 3;
        int i10 = parseRRULE[3];
        int i11 = 7;
        int i12 = -1;
        if (list.size() == 1) {
            if (parseRRULE.length > 4) {
                if (parseRRULE.length != 10 || i6 == -1 || i7 == 0) {
                    return null;
                }
                int[] iArr2 = new int[7];
                i10 = 31;
                for (int i13 = 0; i13 < 7; i13++) {
                    iArr2[i13] = parseRRULE[i13 + 3];
                    iArr2[i13] = iArr2[i13] > 0 ? iArr2[i13] : MONTHLENGTH[i6] + iArr2[i13] + 1;
                    if (iArr2[i13] < i10) {
                        i10 = iArr2[i13];
                    }
                }
                for (int i14 = 1; i14 < 7; i14++) {
                    int i15 = 0;
                    while (true) {
                        if (i15 >= 7) {
                            z = false;
                            break;
                        } else if (iArr2[i15] == i10 + i14) {
                            z = true;
                            break;
                        } else {
                            i15++;
                        }
                    }
                    if (!z) {
                        return null;
                    }
                }
            }
            iArr = null;
            i4 = i3;
        } else if (i6 == -1 || i7 == 0 || i10 == 0) {
            return null;
        } else {
            if (list.size() > 7) {
                return null;
            }
            int length2 = parseRRULE.length - 3;
            int i16 = 31;
            for (int i17 = 0; i17 < length2; i17++) {
                int i18 = parseRRULE[i17 + 3];
                if (i18 <= 0) {
                    i18 = MONTHLENGTH[i6] + i18 + 1;
                }
                if (i18 < i16) {
                    i16 = i18;
                }
            }
            int i19 = 1;
            int i20 = i6;
            int i21 = -1;
            while (i19 < list.size()) {
                long[] jArr2 = new long[1];
                int[] parseRRULE2 = parseRRULE(list.get(i19), jArr2);
                if (jArr2[0] > jArr[0]) {
                    jArr = jArr2;
                }
                if (parseRRULE2[0] == i12 || parseRRULE2[1] == 0 || parseRRULE2[i9] == 0 || (length2 = length2 + (length = parseRRULE2.length - i9)) > 7 || parseRRULE2[1] != i7) {
                    return null;
                }
                if (parseRRULE2[0] != i6) {
                    if (i21 == -1) {
                        int i22 = parseRRULE2[0] - i6;
                        if (i22 == -11 || i22 == -1) {
                            i5 = parseRRULE2[0];
                            i20 = i5;
                            i16 = 31;
                        } else if (i22 != 11 && i22 != 1) {
                            return null;
                        } else {
                            i5 = parseRRULE2[0];
                        }
                        i21 = i5;
                    } else if (!(parseRRULE2[0] == i6 || parseRRULE2[0] == i21)) {
                        return null;
                    }
                }
                if (parseRRULE2[0] == i20) {
                    for (int i23 = 0; i23 < length; i23++) {
                        int i24 = parseRRULE2[i23 + 3];
                        if (i24 <= 0) {
                            i24 = MONTHLENGTH[parseRRULE2[0]] + i24 + 1;
                        }
                        if (i24 < i16) {
                            i16 = i24;
                        }
                    }
                }
                i19++;
                i9 = 3;
                i11 = 7;
                i12 = -1;
            }
            iArr = null;
            if (length2 != i11) {
                return null;
            }
            i4 = i3;
            i6 = i20;
            i10 = i16;
        }
        int[] timeToFields = Grego.timeToFields(j + ((long) i4), iArr);
        int i25 = timeToFields[0];
        int i26 = i6 == -1 ? timeToFields[1] : i6;
        if (i7 == 0 && i8 == 0 && i10 == 0) {
            i10 = timeToFields[2];
        }
        int i27 = timeToFields[5];
        int i28 = Integer.MAX_VALUE;
        if (jArr[0] != MIN_TIME) {
            Grego.timeToFields(jArr[0], timeToFields);
            i28 = timeToFields[0];
        }
        if (i7 == 0 && i8 == 0 && i10 != 0) {
            dateTimeRule = new DateTimeRule(i26, i10, i27, 0);
        } else if (i7 != 0 && i8 != 0 && i10 == 0) {
            dateTimeRule = new DateTimeRule(i26, i8, i7, i27, 0);
        } else if (i7 == 0 || i8 != 0 || i10 == 0) {
            return null;
        } else {
            dateTimeRule = new DateTimeRule(i26, i10, i7, true, i27, 0);
        }
        return new AnnualTimeZoneRule(str, i, i2, dateTimeRule, i25, i28);
    }

    private static int[] parseRRULE(String str, long[] jArr) {
        int[] iArr;
        int i;
        int parseInt;
        StringTokenizer stringTokenizer = new StringTokenizer(str, SEMICOLON);
        int i2 = -1;
        int i3 = 0;
        int i4 = 0;
        boolean z = false;
        boolean z2 = false;
        long j = Long.MIN_VALUE;
        int[] iArr2 = null;
        int i5 = -1;
        while (true) {
            if (!stringTokenizer.hasMoreTokens()) {
                break;
            }
            String nextToken = stringTokenizer.nextToken();
            int indexOf = nextToken.indexOf(EQUALS_SIGN);
            if (indexOf == i2) {
                break;
            }
            String substring = nextToken.substring(0, indexOf);
            String substring2 = nextToken.substring(indexOf + 1);
            if (substring.equals(ICAL_FREQ)) {
                if (!substring2.equals(ICAL_YEARLY)) {
                    break;
                }
                z = true;
            } else if (substring.equals(ICAL_UNTIL)) {
                try {
                    j = parseDateTimeString(substring2, 0);
                } catch (IllegalArgumentException unused) {
                }
            } else if (substring.equals(ICAL_BYMONTH)) {
                if (substring2.length() > 2 || Integer.parseInt(substring2) - 1 < 0 || i5 >= 12) {
                    break;
                }
            } else if (substring.equals(ICAL_BYDAY)) {
                int length = substring2.length();
                if (length < 2 || length > 4) {
                    break;
                }
                if (length > 2) {
                    if (substring2.charAt(0) != '+') {
                        if (substring2.charAt(0) != '-') {
                            if (length == 4) {
                                break;
                            }
                        } else {
                            i = -1;
                            int i6 = length - 3;
                            int i7 = length - 2;
                            parseInt = Integer.parseInt(substring2.substring(i6, i7));
                            if (parseInt == 0 || parseInt > 4) {
                                break;
                            }
                            substring2 = substring2.substring(i7);
                            i4 = parseInt * i;
                        }
                    }
                    i = 1;
                    int i62 = length - 3;
                    int i72 = length - 2;
                    parseInt = Integer.parseInt(substring2.substring(i62, i72));
                    substring2 = substring2.substring(i72);
                    i4 = parseInt * i;
                }
                int i8 = 0;
                while (true) {
                    String[] strArr = ICAL_DOW_NAMES;
                    if (i8 >= strArr.length || substring2.equals(strArr[i8])) {
                        break;
                    }
                    i8++;
                }
                if (i8 >= ICAL_DOW_NAMES.length) {
                    break;
                }
                i3 = i8 + 1;
            } else if (substring.equals(ICAL_BYMONTHDAY)) {
                StringTokenizer stringTokenizer2 = new StringTokenizer(substring2, COMMA);
                int[] iArr3 = new int[stringTokenizer2.countTokens()];
                int i9 = 0;
                while (stringTokenizer2.hasMoreTokens()) {
                    int i10 = i9 + 1;
                    try {
                        iArr3[i9] = Integer.parseInt(stringTokenizer2.nextToken());
                        i9 = i10;
                    } catch (NumberFormatException unused2) {
                        iArr2 = iArr3;
                        z2 = true;
                    }
                }
                iArr2 = iArr3;
            }
            i2 = -1;
        }
        z2 = true;
        if (z2 || !z) {
            return null;
        }
        jArr[0] = j;
        if (iArr2 == null) {
            iArr = new int[4];
            iArr[3] = 0;
        } else {
            iArr = new int[(iArr2.length + 3)];
            for (int i11 = 0; i11 < iArr2.length; i11++) {
                iArr[i11 + 3] = iArr2[i11];
            }
        }
        iArr[0] = i5;
        iArr[1] = i3;
        iArr[2] = i4;
        return iArr;
    }

    private static TimeZoneRule createRuleByRDATE(String str, int i, int i2, long j, List<String> list, int i3) {
        long[] jArr;
        int i4 = 0;
        if (list == null || list.size() == 0) {
            jArr = new long[]{j};
        } else {
            long[] jArr2 = new long[list.size()];
            try {
                for (String str2 : list) {
                    int i5 = i4 + 1;
                    jArr2[i4] = parseDateTimeString(str2, i3);
                    i4 = i5;
                }
                jArr = jArr2;
            } catch (IllegalArgumentException unused) {
                return null;
            }
        }
        return new TimeArrayTimeZoneRule(str, i, i2, jArr, 2);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r18v7 */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x046e  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0151  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02fa  */
    private void writeZone(Writer writer, BasicTimeZone basicTimeZone, String[] strArr) throws IOException {
        int i;
        int i2;
        int i3;
        String str;
        String str2;
        boolean z;
        int i4;
        int i5;
        long j;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        long j2;
        boolean z2;
        int i12;
        boolean z3;
        int i13;
        int i14;
        BasicTimeZone basicTimeZone2;
        int i15;
        int i16;
        String str3;
        int i17;
        int i18;
        int i19;
        int i20;
        int i21;
        int i22;
        boolean z4;
        int i23;
        boolean z5;
        String str4;
        int i24;
        String str5;
        int i25;
        int i26;
        int i27;
        int i28;
        int i29;
        int i30;
        int i31;
        int i32;
        int i33;
        String str6;
        int i34;
        int i35;
        String str7;
        int i36;
        int i37;
        int i38;
        boolean z6;
        int i39;
        int i40;
        int i41;
        int i42;
        int i43;
        int i44;
        int i45;
        int i46;
        int i47;
        int i48;
        BasicTimeZone basicTimeZone3 = basicTimeZone;
        writeHeader(writer);
        boolean z7 = false;
        if (strArr != null && strArr.length > 0) {
            for (int i49 = 0; i49 < strArr.length; i49++) {
                if (strArr[i49] != null) {
                    writer.write(strArr[i49]);
                    writer.write(NEWLINE);
                }
            }
        }
        long j3 = MIN_TIME;
        int[] iArr = new int[6];
        String str8 = null;
        String str9 = null;
        AnnualTimeZoneRule annualTimeZoneRule = null;
        AnnualTimeZoneRule annualTimeZoneRule2 = null;
        boolean z8 = false;
        int i50 = 0;
        int i51 = 0;
        int i52 = 0;
        int i53 = 0;
        int i54 = 0;
        int i55 = 0;
        int i56 = 0;
        int i57 = 0;
        int i58 = 0;
        int i59 = 0;
        int i60 = 0;
        int i61 = 0;
        int i62 = 0;
        int i63 = 0;
        int i64 = 0;
        int i65 = 0;
        int i66 = 0;
        int i67 = 0;
        long j4 = 0;
        long j5 = 0;
        long j6 = 0;
        long j7 = 0;
        while (true) {
            TimeZoneTransition nextTransition = basicTimeZone3.getNextTransition(j3, z7);
            if (nextTransition == null) {
                i = i51;
                i2 = i52;
                i3 = i53;
                str = str8;
                str2 = str9;
                z = z7;
                i4 = i54;
                i5 = i56;
                j = j4;
                i6 = i59;
                i7 = i60;
                i8 = i61;
                i9 = i63;
                i10 = i64;
                i11 = i65;
                j2 = j7;
                z2 = true;
                i12 = i50;
                z3 = z8;
                break;
            }
            long time = nextTransition.getTime();
            String name = nextTransition.getTo().getName();
            boolean z9 = nextTransition.getTo().getDSTSavings() != 0 ? true : z7;
            int rawOffset = nextTransition.getFrom().getRawOffset() + nextTransition.getFrom().getDSTSavings();
            int dSTSavings = nextTransition.getFrom().getDSTSavings();
            int rawOffset2 = nextTransition.getTo().getRawOffset() + nextTransition.getTo().getDSTSavings();
            Grego.timeToFields(nextTransition.getTime() + ((long) rawOffset), iArr);
            z = false;
            int dayOfWeekInMonth = Grego.getDayOfWeekInMonth(iArr[0], iArr[1], iArr[2]);
            int i68 = iArr[0];
            if (!z9) {
                String str10 = name;
                i3 = i53;
                str = str8;
                int i69 = i68;
                i14 = i50;
                if (annualTimeZoneRule == null && (nextTransition.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) nextTransition.getTo()).getEndYear() == Integer.MAX_VALUE) {
                    annualTimeZoneRule = (AnnualTimeZoneRule) nextTransition.getTo();
                }
                if (i54 > 0) {
                    if (i69 == i55 + i54) {
                        str4 = str10;
                        str5 = str9;
                        if (str4.equals(str5)) {
                            i25 = i52;
                            i24 = rawOffset;
                            if (i25 == i24) {
                                i27 = i51;
                                if (i27 == rawOffset2) {
                                    i26 = i63;
                                    if (i26 == iArr[1]) {
                                        i28 = i64;
                                        i29 = i65;
                                        if (i28 == iArr[3] && i29 == dayOfWeekInMonth) {
                                            i30 = i66;
                                            if (i30 == iArr[5]) {
                                                i31 = i54 + 1;
                                                j7 = time;
                                                z5 = true;
                                                if (!z5) {
                                                    i32 = i31;
                                                    i22 = i30;
                                                    i65 = i29;
                                                    i20 = i28;
                                                    i19 = i27;
                                                    i18 = i26;
                                                    i17 = i25;
                                                } else if (i31 == 1) {
                                                    i32 = i31;
                                                    i22 = i30;
                                                    i65 = i29;
                                                    i20 = i28;
                                                    i19 = i27;
                                                    i18 = i26;
                                                    i17 = i25;
                                                    writeZonePropsByTime(writer, false, str5, i25, i27, j6, true);
                                                } else {
                                                    i32 = i31;
                                                    i22 = i30;
                                                    i65 = i29;
                                                    i20 = i28;
                                                    i19 = i27;
                                                    i18 = i26;
                                                    i17 = i25;
                                                    str3 = str5;
                                                    i21 = i24;
                                                    str10 = str4;
                                                    i16 = dayOfWeekInMonth;
                                                    z2 = true;
                                                    writeZonePropsByDOW(writer, false, str5, i17, i19, i18, i65, i20, j6, j7);
                                                    i23 = i32;
                                                    z4 = z5;
                                                }
                                                str3 = str5;
                                                i21 = i24;
                                                str10 = str4;
                                                i16 = dayOfWeekInMonth;
                                                z2 = true;
                                                i23 = i32;
                                                z4 = z5;
                                            }
                                        }
                                        i30 = i66;
                                    }
                                } else {
                                    i26 = i63;
                                }
                                i28 = i64;
                                i29 = i65;
                                i30 = i66;
                            } else {
                                i26 = i63;
                                i28 = i64;
                                i29 = i65;
                                i30 = i66;
                                i27 = i51;
                            }
                        } else {
                            i26 = i63;
                            i28 = i64;
                            i29 = i65;
                            i30 = i66;
                            i27 = i51;
                            i25 = i52;
                            i24 = rawOffset;
                        }
                    } else {
                        i26 = i63;
                        i28 = i64;
                        i29 = i65;
                        i30 = i66;
                        str4 = str10;
                        i27 = i51;
                        i25 = i52;
                        i24 = rawOffset;
                        str5 = str9;
                    }
                    i31 = i54;
                    z5 = false;
                    if (!z5) {
                    }
                    str3 = str5;
                    i21 = i24;
                    str10 = str4;
                    i16 = dayOfWeekInMonth;
                    z2 = true;
                    i23 = i32;
                    z4 = z5;
                } else {
                    i18 = i63;
                    i22 = i66;
                    i19 = i51;
                    i17 = i52;
                    i21 = rawOffset;
                    str3 = str9;
                    i16 = dayOfWeekInMonth;
                    i20 = i64;
                    z2 = true;
                    i23 = i54;
                    z4 = false;
                }
                if (!z4) {
                    char c = z2 ? 1 : 0;
                    char c2 = z2 ? 1 : 0;
                    char c3 = z2 ? 1 : 0;
                    char c4 = z2 ? 1 : 0;
                    char c5 = z2 ? 1 : 0;
                    char c6 = z2 ? 1 : 0;
                    char c7 = z2 ? 1 : 0;
                    char c8 = z2 ? 1 : 0;
                    int i70 = iArr[c];
                    i64 = iArr[3];
                    i66 = iArr[5];
                    i54 = z2;
                    i51 = rawOffset2;
                    i52 = i21;
                    j6 = time;
                    j7 = j6;
                    i62 = dSTSavings;
                    str9 = str10;
                    i65 = i16;
                    i63 = i70;
                } else {
                    i54 = i23;
                    i69 = i55;
                    i64 = i20;
                    i51 = i19;
                    i63 = i18;
                    i52 = i17;
                    str9 = str3;
                    i66 = i22;
                }
                if (!(annualTimeZoneRule == null || annualTimeZoneRule2 == null)) {
                    i = i51;
                    i2 = i52;
                    str2 = str9;
                    boolean z10 = z2 ? 1 : 0;
                    boolean z11 = z2 ? 1 : 0;
                    boolean z12 = z2 ? 1 : 0;
                    boolean z13 = z2 ? 1 : 0;
                    boolean z14 = z2 ? 1 : 0;
                    boolean z15 = z2 ? 1 : 0;
                    boolean z16 = z2 ? 1 : 0;
                    z3 = z10;
                    i4 = i54;
                    i5 = i56;
                    j = j4;
                    i7 = i60;
                    i9 = i63;
                    i10 = i64;
                    i11 = i65;
                    j2 = j7;
                    i12 = i14;
                    i8 = i61;
                    i6 = i59;
                    break;
                }
                basicTimeZone2 = basicTimeZone;
                i55 = i69;
                i15 = i67;
                i61 = i61;
                i59 = i59;
                i53 = i3;
                str8 = str;
            } else {
                if (annualTimeZoneRule2 == null && (nextTransition.getTo() instanceof AnnualTimeZoneRule) && ((AnnualTimeZoneRule) nextTransition.getTo()).getEndYear() == Integer.MAX_VALUE) {
                    annualTimeZoneRule2 = (AnnualTimeZoneRule) nextTransition.getTo();
                }
                if (i56 > 0) {
                    if (i68 == i57 + i56 && name.equals(str8) && i53 == rawOffset && i50 == rawOffset2) {
                        i45 = i59;
                        if (i45 == iArr[1]) {
                            if (i60 == iArr[3]) {
                                i44 = i61;
                                if (i61 == dayOfWeekInMonth) {
                                    i60 = i60;
                                    i46 = i67;
                                    if (i46 == iArr[5]) {
                                        i47 = i56 + 1;
                                        j5 = time;
                                        z6 = true;
                                        if (z6) {
                                            i38 = i46;
                                            if (i47 == 1) {
                                                i36 = i44;
                                                i48 = i47;
                                                str7 = name;
                                                i37 = i50;
                                                i = i51;
                                                i35 = i45;
                                                i2 = i52;
                                                i34 = i53;
                                                str6 = str8;
                                                writeZonePropsByTime(writer, true, str8, i53, i37, j4, true);
                                                i33 = rawOffset;
                                                str2 = str9;
                                                i40 = dayOfWeekInMonth;
                                                i39 = i68;
                                            } else {
                                                i = i51;
                                                str7 = name;
                                                i37 = i50;
                                                i35 = i45;
                                                i2 = i52;
                                                i34 = i53;
                                                str6 = str8;
                                                i36 = i44;
                                                i48 = i47;
                                                i33 = rawOffset;
                                                str2 = str9;
                                                i40 = dayOfWeekInMonth;
                                                i39 = i68;
                                                writeZonePropsByDOW(writer, true, str6, i34, i37, i35, i36, i60, j4, j5);
                                            }
                                        } else {
                                            i = i51;
                                            i38 = i46;
                                            str7 = name;
                                            i37 = i50;
                                            i35 = i45;
                                            i2 = i52;
                                            i34 = i53;
                                            str6 = str8;
                                            i33 = rawOffset;
                                            str2 = str9;
                                            i40 = dayOfWeekInMonth;
                                            i39 = i68;
                                            i36 = i44;
                                            i48 = i47;
                                        }
                                        i41 = i48;
                                    }
                                    i47 = i56;
                                    z6 = false;
                                    if (z6) {
                                    }
                                    i41 = i48;
                                } else {
                                    i60 = i60;
                                    i46 = i67;
                                    i47 = i56;
                                    z6 = false;
                                    if (z6) {
                                    }
                                    i41 = i48;
                                }
                            } else {
                                i60 = i60;
                            }
                        }
                    } else {
                        i45 = i59;
                    }
                    i44 = i61;
                    i46 = i67;
                    i47 = i56;
                    z6 = false;
                    if (z6) {
                    }
                    i41 = i48;
                } else {
                    i = i51;
                    str7 = name;
                    i2 = i52;
                    i34 = i53;
                    str6 = str8;
                    i33 = rawOffset;
                    str2 = str9;
                    i40 = dayOfWeekInMonth;
                    i39 = i68;
                    i35 = i59;
                    i36 = i61;
                    i38 = i67;
                    i37 = i50;
                    i41 = i56;
                    z6 = false;
                }
                if (!z6) {
                    int i71 = iArr[1];
                    int i72 = iArr[3];
                    i42 = iArr[5];
                    i59 = i71;
                    i60 = i72;
                    i56 = 1;
                    i61 = i40;
                    i43 = rawOffset2;
                    j4 = time;
                    j5 = j4;
                    i58 = dSTSavings;
                    str8 = str7;
                    i53 = i33;
                } else {
                    i56 = i41;
                    i39 = i57;
                    i43 = i37;
                    i59 = i35;
                    i53 = i34;
                    str8 = str6;
                    i42 = i38;
                    i61 = i36;
                }
                if (!(annualTimeZoneRule == null || annualTimeZoneRule2 == null)) {
                    i12 = i43;
                    i3 = i53;
                    str = str8;
                    z3 = true;
                    z2 = true;
                    i4 = i54;
                    i5 = i56;
                    j = j4;
                    i6 = i59;
                    i7 = i60;
                    i8 = i61;
                    i9 = i63;
                    i10 = i64;
                    i11 = i65;
                    j2 = j7;
                    break;
                }
                basicTimeZone2 = basicTimeZone;
                z2 = true;
                i57 = i39;
                i15 = i42;
                i51 = i;
                i52 = i2;
                str9 = str2;
                i14 = i43;
            }
            basicTimeZone3 = basicTimeZone2;
            boolean z17 = z2 ? 1 : 0;
            boolean z18 = z2 ? 1 : 0;
            z8 = z17;
            i50 = i14;
            z7 = false;
            i67 = i15;
            j3 = time;
        }
        if (!z3) {
            int offset = basicTimeZone.getOffset(0);
            if (offset == basicTimeZone.getRawOffset()) {
                z2 = z;
            }
            writeZonePropsByTime(writer, z2, getDefaultTZName(basicTimeZone.getID(), z2), offset, offset, 0 - ((long) offset), false);
        } else {
            if (i5 > 0) {
                if (annualTimeZoneRule2 != null) {
                    i13 = i11;
                    if (i5 == z2) {
                        writeFinalRule(writer, true, annualTimeZoneRule2, i3 - i58, i58, j);
                    } else if (isEquivalentDateRule(i6, i8, i7, annualTimeZoneRule2.getRule())) {
                        writeZonePropsByDOW(writer, true, str, i3, i12, i6, i8, i7, j, MAX_TIME);
                    } else {
                        writeZonePropsByDOW(writer, true, str, i3, i12, i6, i8, i7, j, j5);
                        int i73 = i3 - i58;
                        Date nextStart = annualTimeZoneRule2.getNextStart(j5, i73, i58, false);
                        if (nextStart != null) {
                            writeFinalRule(writer, true, annualTimeZoneRule2, i73, i58, nextStart.getTime());
                        }
                    }
                } else if (i5 == z2) {
                    writeZonePropsByTime(writer, true, str, i3, i12, j, true);
                } else {
                    i13 = i11;
                    writeZonePropsByDOW(writer, true, str, i3, i12, i6, i8, i7, j, j5);
                }
                if (i4 > 0) {
                    if (annualTimeZoneRule == null) {
                        if (i4 == z2) {
                            writeZonePropsByTime(writer, false, str2, i2, i, j6, true);
                        } else {
                            writeZonePropsByDOW(writer, false, str2, i2, i, i9, i13, i10, j6, j2);
                        }
                    } else if (i4 == z2) {
                        writeFinalRule(writer, false, annualTimeZoneRule, i2 - i62, i62, j6);
                    } else if (isEquivalentDateRule(i9, i13, i10, annualTimeZoneRule.getRule())) {
                        writeZonePropsByDOW(writer, false, str2, i2, i, i9, i13, i10, j6, MAX_TIME);
                    } else {
                        writeZonePropsByDOW(writer, false, str2, i2, i, i9, i13, i10, j6, j2);
                        int i74 = i2 - i62;
                        Date nextStart2 = annualTimeZoneRule.getNextStart(j2, i74, i62, false);
                        if (nextStart2 != null) {
                            writeFinalRule(writer, false, annualTimeZoneRule, i74, i62, nextStart2.getTime());
                        }
                    }
                }
            }
            i13 = i11;
            if (i4 > 0) {
            }
        }
        writeFooter(writer);
    }

    private static boolean isEquivalentDateRule(int i, int i2, int i3, DateTimeRule dateTimeRule) {
        if (i != dateTimeRule.getRuleMonth() || i3 != dateTimeRule.getRuleDayOfWeek() || dateTimeRule.getTimeRuleType() != 0) {
            return false;
        }
        if (dateTimeRule.getDateRuleType() == 1 && dateTimeRule.getRuleWeekInMonth() == i2) {
            return true;
        }
        int ruleDayOfMonth = dateTimeRule.getRuleDayOfMonth();
        if (dateTimeRule.getDateRuleType() == 2) {
            if (ruleDayOfMonth % 7 == 1 && (ruleDayOfMonth + 6) / 7 == i2) {
                return true;
            }
            if (i != 1) {
                int[] iArr = MONTHLENGTH;
                if ((iArr[i] - ruleDayOfMonth) % 7 == 6 && i2 == (((iArr[i] - ruleDayOfMonth) + 1) / 7) * -1) {
                    return true;
                }
            }
        }
        if (dateTimeRule.getDateRuleType() == 3) {
            if (ruleDayOfMonth % 7 == 0 && ruleDayOfMonth / 7 == i2) {
                return true;
            }
            if (i != 1) {
                int[] iArr2 = MONTHLENGTH;
                if ((iArr2[i] - ruleDayOfMonth) % 7 == 0 && i2 == (((iArr2[i] - ruleDayOfMonth) / 7) + 1) * -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void writeZonePropsByTime(Writer writer, boolean z, String str, int i, int i2, long j, boolean z2) throws IOException {
        beginZoneProps(writer, z, str, i, i2, j);
        if (z2) {
            writer.write(ICAL_RDATE);
            writer.write(COLON);
            writer.write(getDateTimeString(j + ((long) i)));
            writer.write(NEWLINE);
        }
        endZoneProps(writer, z);
    }

    private static void writeZonePropsByDOM(Writer writer, boolean z, String str, int i, int i2, int i3, int i4, long j, long j2) throws IOException {
        beginZoneProps(writer, z, str, i, i2, j);
        beginRRULE(writer, i3);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(i4));
        if (j2 != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(j2 + ((long) i)));
        }
        writer.write(NEWLINE);
        endZoneProps(writer, z);
    }

    private static void writeZonePropsByDOW(Writer writer, boolean z, String str, int i, int i2, int i3, int i4, int i5, long j, long j2) throws IOException {
        beginZoneProps(writer, z, str, i, i2, j);
        beginRRULE(writer, i3);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(i4));
        writer.write(ICAL_DOW_NAMES[i5 - 1]);
        if (j2 != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(j2 + ((long) i)));
        }
        writer.write(NEWLINE);
        endZoneProps(writer, z);
    }

    private static void writeZonePropsByDOW_GEQ_DOM(Writer writer, boolean z, String str, int i, int i2, int i3, int i4, int i5, long j, long j2) throws IOException {
        int i6;
        int i7;
        int i8 = 7;
        if (i4 % 7 == 1) {
            writeZonePropsByDOW(writer, z, str, i, i2, i3, (i4 + 6) / 7, i5, j, j2);
            return;
        }
        if (i3 != 1) {
            int[] iArr = MONTHLENGTH;
            if ((iArr[i3] - i4) % 7 == 6) {
                writeZonePropsByDOW(writer, z, str, i, i2, i3, (((iArr[i3] - i4) + 1) / 7) * -1, i5, j, j2);
                return;
            }
        }
        beginZoneProps(writer, z, str, i, i2, j);
        if (i4 <= 0) {
            int i9 = 1 - i4;
            int i10 = 7 - i9;
            int i11 = i3 - 1;
            writeZonePropsByDOW_GEQ_DOM_sub(writer, i11 < 0 ? 11 : i11, -i9, i5, i9, MAX_TIME, i);
            i6 = i10;
            i7 = 1;
        } else {
            int i12 = i4 + 6;
            int[] iArr2 = MONTHLENGTH;
            if (i12 > iArr2[i3]) {
                int i13 = i12 - iArr2[i3];
                i8 = 7 - i13;
                int i14 = i3 + 1;
                writeZonePropsByDOW_GEQ_DOM_sub(writer, i14 > 11 ? 0 : i14, 1, i5, i13, MAX_TIME, i);
            }
            i6 = i8;
            i7 = i4;
        }
        writeZonePropsByDOW_GEQ_DOM_sub(writer, i3, i7, i5, i6, j2, i);
        endZoneProps(writer, z);
    }

    private static void writeZonePropsByDOW_GEQ_DOM_sub(Writer writer, int i, int i2, int i3, int i4, long j, int i5) throws IOException {
        boolean z = i == 1;
        if (i2 < 0 && !z) {
            i2 = MONTHLENGTH[i] + i2 + 1;
        }
        beginRRULE(writer, i);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_DOW_NAMES[i3 - 1]);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(i2));
        for (int i6 = 1; i6 < i4; i6++) {
            writer.write(COMMA);
            writer.write(Integer.toString(i2 + i6));
        }
        if (j != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(j + ((long) i5)));
        }
        writer.write(NEWLINE);
    }

    private static void writeZonePropsByDOW_LEQ_DOM(Writer writer, boolean z, String str, int i, int i2, int i3, int i4, int i5, long j, long j2) throws IOException {
        if (i4 % 7 == 0) {
            writeZonePropsByDOW(writer, z, str, i, i2, i3, i4 / 7, i5, j, j2);
            return;
        }
        if (i3 != 1) {
            int[] iArr = MONTHLENGTH;
            if ((iArr[i3] - i4) % 7 == 0) {
                writeZonePropsByDOW(writer, z, str, i, i2, i3, (((iArr[i3] - i4) / 7) + 1) * -1, i5, j, j2);
                return;
            }
        }
        if (i3 == 1 && i4 == 29) {
            writeZonePropsByDOW(writer, z, str, i, i2, 1, -1, i5, j, j2);
        } else {
            writeZonePropsByDOW_GEQ_DOM(writer, z, str, i, i2, i3, i4 - 6, i5, j, j2);
        }
    }

    private static void writeFinalRule(Writer writer, boolean z, AnnualTimeZoneRule annualTimeZoneRule, int i, int i2, long j) throws IOException {
        DateTimeRule wallTimeRule = toWallTimeRule(annualTimeZoneRule.getRule(), i, i2);
        int ruleMillisInDay = wallTimeRule.getRuleMillisInDay();
        long j2 = ruleMillisInDay < 0 ? j + ((long) (0 - ruleMillisInDay)) : ruleMillisInDay >= 86400000 ? j - ((long) (ruleMillisInDay - 86399999)) : j;
        int rawOffset = annualTimeZoneRule.getRawOffset() + annualTimeZoneRule.getDSTSavings();
        int dateRuleType = wallTimeRule.getDateRuleType();
        if (dateRuleType == 0) {
            writeZonePropsByDOM(writer, z, annualTimeZoneRule.getName(), i + i2, rawOffset, wallTimeRule.getRuleMonth(), wallTimeRule.getRuleDayOfMonth(), j2, MAX_TIME);
        } else if (dateRuleType == 1) {
            writeZonePropsByDOW(writer, z, annualTimeZoneRule.getName(), i + i2, rawOffset, wallTimeRule.getRuleMonth(), wallTimeRule.getRuleWeekInMonth(), wallTimeRule.getRuleDayOfWeek(), j2, MAX_TIME);
        } else if (dateRuleType == 2) {
            writeZonePropsByDOW_GEQ_DOM(writer, z, annualTimeZoneRule.getName(), i + i2, rawOffset, wallTimeRule.getRuleMonth(), wallTimeRule.getRuleDayOfMonth(), wallTimeRule.getRuleDayOfWeek(), j2, MAX_TIME);
        } else if (dateRuleType == 3) {
            writeZonePropsByDOW_LEQ_DOM(writer, z, annualTimeZoneRule.getName(), i + i2, rawOffset, wallTimeRule.getRuleMonth(), wallTimeRule.getRuleDayOfMonth(), wallTimeRule.getRuleDayOfWeek(), j2, MAX_TIME);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0093  */
    private static DateTimeRule toWallTimeRule(DateTimeRule dateTimeRule, int i, int i2) {
        int i3;
        int i4;
        int dateRuleType;
        int i5;
        int i6;
        if (dateTimeRule.getTimeRuleType() == 0) {
            return dateTimeRule;
        }
        int ruleMillisInDay = dateTimeRule.getRuleMillisInDay();
        if (dateTimeRule.getTimeRuleType() == 2) {
            ruleMillisInDay += i + i2;
        } else if (dateTimeRule.getTimeRuleType() == 1) {
            ruleMillisInDay += i2;
        }
        if (ruleMillisInDay < 0) {
            ruleMillisInDay += 86400000;
            i3 = -1;
        } else if (ruleMillisInDay >= 86400000) {
            i4 = ruleMillisInDay - 86400000;
            i3 = 1;
            int ruleMonth = dateTimeRule.getRuleMonth();
            int ruleDayOfMonth = dateTimeRule.getRuleDayOfMonth();
            int ruleDayOfWeek = dateTimeRule.getRuleDayOfWeek();
            dateRuleType = dateTimeRule.getDateRuleType();
            if (i3 == 0) {
                if (dateRuleType == 1) {
                    int ruleWeekInMonth = dateTimeRule.getRuleWeekInMonth();
                    if (ruleWeekInMonth > 0) {
                        ruleDayOfMonth = ((ruleWeekInMonth - 1) * 7) + 1;
                        dateRuleType = 2;
                    } else {
                        dateRuleType = 3;
                        ruleDayOfMonth = ((ruleWeekInMonth + 1) * 7) + MONTHLENGTH[ruleMonth];
                    }
                }
                int i7 = ruleDayOfMonth + i3;
                if (i7 == 0) {
                    ruleMonth--;
                    if (ruleMonth < 0) {
                        ruleMonth = 11;
                    }
                    i7 = MONTHLENGTH[ruleMonth];
                } else if (i7 > MONTHLENGTH[ruleMonth]) {
                    int i8 = ruleMonth + 1;
                    ruleMonth = i8 > 11 ? 0 : i8;
                    i7 = 1;
                }
                if (dateRuleType != 0) {
                    int i9 = i3 + ruleDayOfWeek;
                    if (i9 < 1) {
                        i5 = 7;
                        i6 = i7;
                    } else if (i9 > 7) {
                        i6 = i7;
                        i5 = 1;
                    } else {
                        i6 = i7;
                        i5 = i9;
                    }
                    if (dateRuleType == 0) {
                        return new DateTimeRule(ruleMonth, i6, i4, 0);
                    }
                    return new DateTimeRule(ruleMonth, i6, i5, dateRuleType == 2, i4, 0);
                }
                i6 = i7;
            } else {
                i6 = ruleDayOfMonth;
            }
            i5 = ruleDayOfWeek;
            if (dateRuleType == 0) {
            }
        } else {
            i3 = 0;
        }
        i4 = ruleMillisInDay;
        int ruleMonth2 = dateTimeRule.getRuleMonth();
        int ruleDayOfMonth2 = dateTimeRule.getRuleDayOfMonth();
        int ruleDayOfWeek2 = dateTimeRule.getRuleDayOfWeek();
        dateRuleType = dateTimeRule.getDateRuleType();
        if (i3 == 0) {
        }
        i5 = ruleDayOfWeek2;
        if (dateRuleType == 0) {
        }
    }

    private static void beginZoneProps(Writer writer, boolean z, String str, int i, int i2, long j) throws IOException {
        writer.write(ICAL_BEGIN);
        writer.write(COLON);
        if (z) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);
        writer.write(ICAL_TZOFFSETTO);
        writer.write(COLON);
        writer.write(millisToOffset(i2));
        writer.write(NEWLINE);
        writer.write(ICAL_TZOFFSETFROM);
        writer.write(COLON);
        writer.write(millisToOffset(i));
        writer.write(NEWLINE);
        writer.write(ICAL_TZNAME);
        writer.write(COLON);
        writer.write(str);
        writer.write(NEWLINE);
        writer.write(ICAL_DTSTART);
        writer.write(COLON);
        writer.write(getDateTimeString(j + ((long) i)));
        writer.write(NEWLINE);
    }

    private static void endZoneProps(Writer writer, boolean z) throws IOException {
        writer.write(ICAL_END);
        writer.write(COLON);
        if (z) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);
    }

    private static void beginRRULE(Writer writer, int i) throws IOException {
        writer.write(ICAL_RRULE);
        writer.write(COLON);
        writer.write(ICAL_FREQ);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_YEARLY);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTH);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(i + 1));
        writer.write(SEMICOLON);
    }

    private static void appendUNTIL(Writer writer, String str) throws IOException {
        if (str != null) {
            writer.write(SEMICOLON);
            writer.write(ICAL_UNTIL);
            writer.write(EQUALS_SIGN);
            writer.write(str);
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

    private static String getDateTimeString(long j) {
        int[] timeToFields = Grego.timeToFields(j, (int[]) null);
        StringBuilder sb = new StringBuilder(15);
        sb.append(numToString(timeToFields[0], 4));
        sb.append(numToString(timeToFields[1] + 1, 2));
        sb.append(numToString(timeToFields[2], 2));
        sb.append('T');
        int i = timeToFields[5];
        int i2 = i / DEF_DSTSAVINGS;
        int i3 = i % DEF_DSTSAVINGS;
        int i4 = i3 / WorkInfo.MIN_IDLE_WAIT_TIME_MS;
        sb.append(numToString(i2, 2));
        sb.append(numToString(i4, 2));
        sb.append(numToString((i3 % WorkInfo.MIN_IDLE_WAIT_TIME_MS) / 1000, 2));
        return sb.toString();
    }

    private static String getUTCDateTimeString(long j) {
        return getDateTimeString(j) + "Z";
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00ba  */
    private static long parseDateTimeString(String str, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        boolean z;
        int length;
        boolean z2 = false;
        if (str != null && (((length = str.length()) == 15 || length == 16) && str.charAt(8) == 'T')) {
            if (length != 16) {
                z = false;
            } else if (str.charAt(15) == 'Z') {
                z = true;
            }
            try {
                i5 = Integer.parseInt(str.substring(0, 4));
                try {
                    i7 = Integer.parseInt(str.substring(4, 6)) - 1;
                    try {
                        i6 = Integer.parseInt(str.substring(6, 8));
                    } catch (NumberFormatException unused) {
                        i6 = 0;
                        i4 = i6;
                        i3 = i4;
                        i2 = 0;
                        if (!z2) {
                        }
                    }
                } catch (NumberFormatException unused2) {
                    i7 = 0;
                    i6 = 0;
                    i4 = i6;
                    i3 = i4;
                    i2 = 0;
                    if (!z2) {
                    }
                }
                try {
                    i4 = Integer.parseInt(str.substring(9, 11));
                    try {
                        i3 = Integer.parseInt(str.substring(11, 13));
                    } catch (NumberFormatException unused3) {
                        i3 = 0;
                        i2 = 0;
                        if (!z2) {
                        }
                    }
                    try {
                        i2 = Integer.parseInt(str.substring(13, 15));
                        int monthLength = Grego.monthLength(i5, i7);
                        if (i5 >= 0 && i7 >= 0 && i7 <= 11 && i6 >= 1 && i6 <= monthLength && i4 >= 0 && i4 < 24 && i3 >= 0 && i3 < 60 && i2 >= 0 && i2 < 60) {
                            z2 = true;
                        }
                    } catch (NumberFormatException unused4) {
                        i2 = 0;
                        if (!z2) {
                        }
                    }
                } catch (NumberFormatException unused5) {
                    i4 = 0;
                    i3 = i4;
                    i2 = 0;
                    if (!z2) {
                    }
                }
            } catch (NumberFormatException unused6) {
                i7 = 0;
                i6 = 0;
                i5 = 0;
                i4 = 0;
                i3 = i4;
                i2 = 0;
                if (!z2) {
                }
            }
            if (!z2) {
                long fieldsToDay = (Grego.fieldsToDay(i5, i7, i6) * WorkStatus.RARE_DELAY_TIME) + ((long) ((i4 * DEF_DSTSAVINGS) + (i3 * WorkInfo.MIN_IDLE_WAIT_TIME_MS) + (i2 * 1000)));
                return !z ? fieldsToDay - ((long) i) : fieldsToDay;
            }
            throw new IllegalArgumentException("Invalid date time string format");
        }
        i2 = 0;
        z = false;
        i7 = 0;
        i6 = 0;
        i5 = 0;
        i4 = 0;
        i3 = 0;
        if (!z2) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0054  */
    private static int offsetStrToMillis(String str) {
        int i;
        int i2;
        int i3;
        int i4;
        int length;
        int i5 = 0;
        if (str != null && ((length = str.length()) == 5 || length == 7)) {
            char charAt = str.charAt(0);
            if (charAt == '+') {
                i4 = 1;
            } else if (charAt == '-') {
                i4 = -1;
            }
            try {
                i2 = Integer.parseInt(str.substring(1, 3));
                try {
                    i3 = Integer.parseInt(str.substring(3, 5));
                    if (length == 7) {
                        try {
                            i5 = Integer.parseInt(str.substring(5, 7));
                        } catch (NumberFormatException unused) {
                            i = 0;
                            if (i5 != 0) {
                            }
                        }
                    }
                    i = i5;
                    i5 = 1;
                } catch (NumberFormatException unused2) {
                    i3 = 0;
                    i = 0;
                    if (i5 != 0) {
                    }
                }
            } catch (NumberFormatException unused3) {
                i3 = 0;
                i2 = 0;
                i = 0;
                if (i5 != 0) {
                }
            }
            if (i5 != 0) {
                return i4 * ((((i2 * 60) + i3) * 60) + i) * 1000;
            }
            throw new IllegalArgumentException("Bad offset string");
        }
        i = 0;
        i4 = 0;
        i3 = 0;
        i2 = 0;
        if (i5 != 0) {
        }
    }

    private static String millisToOffset(int i) {
        StringBuilder sb = new StringBuilder(7);
        if (i >= 0) {
            sb.append('+');
        } else {
            sb.append('-');
            i = -i;
        }
        int i2 = i / 1000;
        int i3 = i2 % 60;
        int i4 = (i2 - i3) / 60;
        sb.append(numToString(i4 / 60, 2));
        sb.append(numToString(i4 % 60, 2));
        sb.append(numToString(i3, 2));
        return sb.toString();
    }

    private static String numToString(int i, int i2) {
        String num = Integer.toString(i);
        int length = num.length();
        if (length >= i2) {
            return num.substring(length - i2, length);
        }
        StringBuilder sb = new StringBuilder(i2);
        while (length < i2) {
            sb.append('0');
            length++;
        }
        sb.append(num);
        return sb.toString();
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return this.isFrozen;
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public TimeZone cloneAsThawed() {
        VTimeZone vTimeZone = (VTimeZone) super.cloneAsThawed();
        vTimeZone.tz = (BasicTimeZone) this.tz.cloneAsThawed();
        vTimeZone.isFrozen = false;
        return vTimeZone;
    }
}
