package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.lang.UScript;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import dalvik.system.VMDebug;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.w3c.dom.traversal.NodeFilter;

public class DateIntervalInfo implements Cloneable, Freezable<DateIntervalInfo>, Serializable {
    static final String[] CALENDAR_FIELD_TO_PATTERN_LETTER = null;
    private static final String DEBUG_SKELETON = null;
    private static final ICUCache<String, DateIntervalInfo> DIICACHE = null;
    private static String EARLIEST_FIRST_PREFIX = null;
    private static String FALLBACK_STRING = null;
    private static String LATEST_FIRST_PREFIX = null;
    private static final int MINIMUM_SUPPORTED_CALENDAR_FIELD = 13;
    static final int currentSerialVersion = 1;
    private static final long serialVersionUID = 1;
    private String fFallbackIntervalPattern;
    private boolean fFirstDateInPtnIsLaterDate;
    private Map<String, Map<String, PatternInfo>> fIntervalPatterns;
    private transient boolean fIntervalPatternsReadOnly;
    private volatile transient boolean frozen;

    public static final class PatternInfo implements Cloneable, Serializable {
        static final int currentSerialVersion = 1;
        private static final long serialVersionUID = 1;
        private final boolean fFirstDateInPtnIsLaterDate;
        private final String fIntervalPatternFirstPart;
        private final String fIntervalPatternSecondPart;

        public PatternInfo(String firstPart, String secondPart, boolean firstDateInPtnIsLaterDate) {
            this.fIntervalPatternFirstPart = firstPart;
            this.fIntervalPatternSecondPart = secondPart;
            this.fFirstDateInPtnIsLaterDate = firstDateInPtnIsLaterDate;
        }

        public String getFirstPart() {
            return this.fIntervalPatternFirstPart;
        }

        public String getSecondPart() {
            return this.fIntervalPatternSecondPart;
        }

        public boolean firstDateInPtnIsLaterDate() {
            return this.fFirstDateInPtnIsLaterDate;
        }

        public boolean equals(Object a) {
            boolean z = false;
            if (!(a instanceof PatternInfo)) {
                return false;
            }
            PatternInfo patternInfo = (PatternInfo) a;
            if (Utility.objectEquals(this.fIntervalPatternFirstPart, patternInfo.fIntervalPatternFirstPart) && Utility.objectEquals(this.fIntervalPatternSecondPart, this.fIntervalPatternSecondPart) && this.fFirstDateInPtnIsLaterDate == patternInfo.fFirstDateInPtnIsLaterDate) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            int hash = this.fIntervalPatternFirstPart != null ? this.fIntervalPatternFirstPart.hashCode() : 0;
            if (this.fIntervalPatternSecondPart != null) {
                hash ^= this.fIntervalPatternSecondPart.hashCode();
            }
            if (this.fFirstDateInPtnIsLaterDate) {
                return hash ^ -1;
            }
            return hash;
        }

        @Deprecated
        public String toString() {
            return "{first=\u00ab" + this.fIntervalPatternFirstPart + "\u00bb, second=\u00ab" + this.fIntervalPatternSecondPart + "\u00bb, reversed:" + this.fFirstDateInPtnIsLaterDate + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateIntervalInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateIntervalInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateIntervalInfo.<clinit>():void");
    }

    @Deprecated
    public DateIntervalInfo() {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        this.fIntervalPatterns = new HashMap();
        this.fFallbackIntervalPattern = "{0} \u2013 {1}";
    }

    public DateIntervalInfo(ULocale locale) {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        initializeData(locale);
    }

    public DateIntervalInfo(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    private void initializeData(ULocale locale) {
        String key = locale.toString();
        DateIntervalInfo dii = (DateIntervalInfo) DIICACHE.get(key);
        if (dii == null) {
            setup(locale);
            this.fIntervalPatternsReadOnly = true;
            DIICACHE.put(key, ((DateIntervalInfo) clone()).freeze());
            return;
        }
        initializeFromReadOnlyPatterns(dii);
    }

    private void initializeFromReadOnlyPatterns(DateIntervalInfo dii) {
        this.fFallbackIntervalPattern = dii.fFallbackIntervalPattern;
        this.fFirstDateInPtnIsLaterDate = dii.fFirstDateInPtnIsLaterDate;
        this.fIntervalPatterns = dii.fIntervalPatterns;
        this.fIntervalPatternsReadOnly = true;
    }

    private void setup(android.icu.util.ULocale r31) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r30 = this;
        r3 = 19;
        r27 = new java.util.HashMap;
        r0 = r27;
        r0.<init>(r3);
        r0 = r27;
        r1 = r30;
        r1.fIntervalPatterns = r0;
        r27 = "{0} \u2013 {1}";
        r0 = r27;
        r1 = r30;
        r1.fFallbackIntervalPattern = r0;
        r26 = new java.util.HashSet;
        r26.<init>();
        r6 = r31;
        r27 = "calendar";	 Catch:{ MissingResourceException -> 0x026c }
        r0 = r31;	 Catch:{ MissingResourceException -> 0x026c }
        r1 = r27;	 Catch:{ MissingResourceException -> 0x026c }
        r5 = r0.getKeywordValue(r1);	 Catch:{ MissingResourceException -> 0x026c }
        if (r5 != 0) goto L_0x003f;	 Catch:{ MissingResourceException -> 0x026c }
    L_0x002c:
        r27 = "calendar";	 Catch:{ MissingResourceException -> 0x026c }
        r28 = 1;	 Catch:{ MissingResourceException -> 0x026c }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x026c }
        r1 = r31;	 Catch:{ MissingResourceException -> 0x026c }
        r2 = r28;	 Catch:{ MissingResourceException -> 0x026c }
        r19 = android.icu.util.Calendar.getKeywordValuesForLocale(r0, r1, r2);	 Catch:{ MissingResourceException -> 0x026c }
        r27 = 0;	 Catch:{ MissingResourceException -> 0x026c }
        r5 = r19[r27];	 Catch:{ MissingResourceException -> 0x026c }
    L_0x003f:
        if (r5 != 0) goto L_0x0263;	 Catch:{ MissingResourceException -> 0x026c }
    L_0x0041:
        r5 = "gregorian";	 Catch:{ MissingResourceException -> 0x026c }
        r7 = r6;
    L_0x0045:
        r14 = r7.getName();	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r14.length();	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 != 0) goto L_0x0051;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x004f:
        r6 = r7;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0050:
        return;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0051:
        r27 = "android/icu/impl/data/icudt56b";	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r22 = android.icu.util.UResourceBundle.getBundleInstance(r0, r7);	 Catch:{ MissingResourceException -> 0x01a7 }
        r22 = (android.icu.impl.ICUResourceBundle) r22;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = new java.lang.StringBuilder;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27.<init>();	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = "calendar/";	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27.append(r28);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.append(r5);	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = "/intervalFormats";	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27.append(r28);	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27.toString();	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r22;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r12 = r0.getWithFallback(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = FALLBACK_STRING;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r9 = r12.getStringWithFallback(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r30;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0.setFallbackIntervalPattern(r9);	 Catch:{ MissingResourceException -> 0x01a7 }
        r23 = r12.getSize();	 Catch:{ MissingResourceException -> 0x01a7 }
        r10 = 0;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0093:
        r0 = r23;	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r10 >= r0) goto L_0x023e;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0097:
        r27 = r12.get(r10);	 Catch:{ MissingResourceException -> 0x01a7 }
        r24 = r27.getKey();	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = FALLBACK_STRING;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.compareTo(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 != 0) goto L_0x00ae;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00ab:
        r10 = r10 + 1;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0093;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00ae:
        r0 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r11 = r12.get(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        r11 = (android.icu.impl.ICUResourceBundle) r11;	 Catch:{ MissingResourceException -> 0x01a7 }
        r21 = r11.getSize();	 Catch:{ MissingResourceException -> 0x01a7 }
        r20 = 0;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00bc:
        r0 = r20;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r21;	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r0 >= r1) goto L_0x00ab;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00c2:
        r0 = r20;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r11.get(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        r13 = r27.getKey();	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = new java.lang.StringBuilder;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27.<init>();	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.append(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = "\u0001";	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27.append(r28);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.append(r13);	 Catch:{ MissingResourceException -> 0x01a7 }
        r25 = r27.toString();	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r26;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r25;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.contains(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x00f7;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00f4:
        r20 = r20 + 1;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x00bc;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x00f7:
        r0 = r26;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r25;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0.add(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r20;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r11.get(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        r18 = r27.getString();	 Catch:{ MissingResourceException -> 0x01a7 }
        r4 = -1;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 1;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x01ab;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0117:
        r4 = 1;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0118:
        r27 = -1;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r4 == r0) goto L_0x00f4;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x011e:
        r27 = DEBUG_SKELETON;	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x0233;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0122:
        r27 = DEBUG_SKELETON;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0.equals(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x0233;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x012e:
        r0 = r30;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r0.fIntervalPatterns;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r16 = r0.get(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r16 = (java.util.Map) r16;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r30;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r2 = r18;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0.setIntervalPatternInternally(r1, r13, r2);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r30;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r0.fIntervalPatterns;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r0;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r15 = r0.get(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r15 = (java.util.Map) r15;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r16;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = android.icu.impl.Utility.objectEquals(r0, r15);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 != 0) goto L_0x00f4;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x015f:
        r27 = java.lang.System.out;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = new java.lang.StringBuilder;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28.<init>();	 Catch:{ MissingResourceException -> 0x01a7 }
        r29 = "\n";	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r28.append(r29);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r28;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r0.append(r7);	 Catch:{ MissingResourceException -> 0x01a7 }
        r29 = ", skeleton: ";	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r28.append(r29);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r28;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r0.append(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r29 = ", oldValue: ";	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r28.append(r29);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r28;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r16;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r0.append(r1);	 Catch:{ MissingResourceException -> 0x01a7 }
        r29 = ", newValue: ";	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r28.append(r29);	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r28;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r0.append(r15);	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = r28.toString();	 Catch:{ MissingResourceException -> 0x01a7 }
        r27.println(r28);	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x00f4;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01a7:
        r8 = move-exception;	 Catch:{ MissingResourceException -> 0x01a7 }
        r6 = r7;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0050;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01ab:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 2;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x01bc;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01b9:
        r4 = 2;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01bc:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 5;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x01cd;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01ca:
        r4 = 5;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01cd:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 9;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x01df;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01db:
        r4 = 9;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01df:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 10;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x01f7;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01ed:
        r4 = 10;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 10;	 Catch:{ MissingResourceException -> 0x01a7 }
        r13 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x01f7:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 11;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x020f;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0205:
        r4 = 10;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 10;	 Catch:{ MissingResourceException -> 0x01a7 }
        r13 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x020f:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 12;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x0221;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x021d:
        r4 = 12;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0221:
        r27 = CALENDAR_FIELD_TO_PATTERN_LETTER;	 Catch:{ MissingResourceException -> 0x01a7 }
        r28 = 13;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r27[r28];	 Catch:{ MissingResourceException -> 0x01a7 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x01a7 }
        r27 = r13.equals(r0);	 Catch:{ MissingResourceException -> 0x01a7 }
        if (r27 == 0) goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x022f:
        r4 = 13;	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0118;	 Catch:{ MissingResourceException -> 0x01a7 }
    L_0x0233:
        r0 = r30;	 Catch:{ MissingResourceException -> 0x01a7 }
        r1 = r24;	 Catch:{ MissingResourceException -> 0x01a7 }
        r2 = r18;	 Catch:{ MissingResourceException -> 0x01a7 }
        r0.setIntervalPatternInternally(r1, r13, r2);	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x00f4;
    L_0x023e:
        r27 = "%%Parent";	 Catch:{ MissingResourceException -> 0x0266 }
        r0 = r22;	 Catch:{ MissingResourceException -> 0x0266 }
        r1 = r27;	 Catch:{ MissingResourceException -> 0x0266 }
        r17 = r0.get(r1);	 Catch:{ MissingResourceException -> 0x0266 }
        r6 = new android.icu.util.ULocale;	 Catch:{ MissingResourceException -> 0x0266 }
        r27 = r17.getString();	 Catch:{ MissingResourceException -> 0x0266 }
        r0 = r27;	 Catch:{ MissingResourceException -> 0x0266 }
        r6.<init>(r0);	 Catch:{ MissingResourceException -> 0x0266 }
    L_0x0254:
        if (r6 == 0) goto L_0x0050;
    L_0x0256:
        r27 = r6.getBaseName();	 Catch:{ MissingResourceException -> 0x026c }
        r28 = "root";	 Catch:{ MissingResourceException -> 0x026c }
        r27 = r27.equals(r28);	 Catch:{ MissingResourceException -> 0x026c }
        if (r27 != 0) goto L_0x0050;
    L_0x0263:
        r7 = r6;
        goto L_0x0045;
    L_0x0266:
        r8 = move-exception;
        r6 = r7.getFallback();	 Catch:{ MissingResourceException -> 0x01a7 }
        goto L_0x0254;
    L_0x026c:
        r8 = move-exception;
        goto L_0x0050;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateIntervalInfo.setup(android.icu.util.ULocale):void");
    }

    private static int splitPatternInto2Part(String intervalPattern) {
        boolean inQuote = false;
        int prevCh = 0;
        int count = 0;
        int[] patternRepeated = new int[58];
        boolean foundRepetition = false;
        int i = 0;
        while (i < intervalPattern.length()) {
            char ch = intervalPattern.charAt(i);
            if (ch != prevCh && count > 0) {
                if (patternRepeated[prevCh - 65] != 0) {
                    foundRepetition = true;
                    break;
                }
                patternRepeated[prevCh - 65] = currentSerialVersion;
                count = 0;
            }
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (i + currentSerialVersion >= intervalPattern.length() || intervalPattern.charAt(i + currentSerialVersion) != PatternTokenizer.SINGLE_QUOTE) {
                    inQuote = !inQuote;
                } else {
                    i += currentSerialVersion;
                }
            } else if (!inQuote) {
                if (ch < 'a' || ch > 'z') {
                    if (ch >= 'A' && ch <= 'Z') {
                    }
                }
                char prevCh2 = ch;
                count += currentSerialVersion;
            }
            i += currentSerialVersion;
        }
        if (count > 0 && !foundRepetition && patternRepeated[prevCh - 65] == 0) {
            count = 0;
        }
        return i - count;
    }

    public void setIntervalPattern(String skeleton, int lrgDiffCalUnit, String intervalPattern) {
        if (this.frozen) {
            throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
        } else if (lrgDiffCalUnit > MINIMUM_SUPPORTED_CALENDAR_FIELD) {
            throw new IllegalArgumentException("calendar field is larger than MINIMUM_SUPPORTED_CALENDAR_FIELD");
        } else {
            if (this.fIntervalPatternsReadOnly) {
                this.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                this.fIntervalPatternsReadOnly = false;
            }
            PatternInfo ptnInfo = setIntervalPatternInternally(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[lrgDiffCalUnit], intervalPattern);
            if (lrgDiffCalUnit == 11) {
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[9], ptnInfo);
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[10], ptnInfo);
            } else if (lrgDiffCalUnit == 5 || lrgDiffCalUnit == 7) {
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[5], ptnInfo);
            }
        }
    }

    private PatternInfo setIntervalPatternInternally(String skeleton, String lrgDiffCalUnit, String intervalPattern) {
        Map<String, PatternInfo> patternsOfOneSkeleton = (Map) this.fIntervalPatterns.get(skeleton);
        boolean emptyHash = false;
        if (patternsOfOneSkeleton == null) {
            patternsOfOneSkeleton = new HashMap();
            emptyHash = true;
        }
        boolean order = this.fFirstDateInPtnIsLaterDate;
        if (intervalPattern.startsWith(LATEST_FIRST_PREFIX)) {
            order = true;
            intervalPattern = intervalPattern.substring(LATEST_FIRST_PREFIX.length(), intervalPattern.length());
        } else if (intervalPattern.startsWith(EARLIEST_FIRST_PREFIX)) {
            order = false;
            intervalPattern = intervalPattern.substring(EARLIEST_FIRST_PREFIX.length(), intervalPattern.length());
        }
        PatternInfo itvPtnInfo = genPatternInfo(intervalPattern, order);
        patternsOfOneSkeleton.put(lrgDiffCalUnit, itvPtnInfo);
        if (emptyHash) {
            this.fIntervalPatterns.put(skeleton, patternsOfOneSkeleton);
        }
        return itvPtnInfo;
    }

    private void setIntervalPattern(String skeleton, String lrgDiffCalUnit, PatternInfo ptnInfo) {
        ((Map) this.fIntervalPatterns.get(skeleton)).put(lrgDiffCalUnit, ptnInfo);
    }

    @Deprecated
    public static PatternInfo genPatternInfo(String intervalPattern, boolean laterDateFirst) {
        int splitPoint = splitPatternInto2Part(intervalPattern);
        String firstPart = intervalPattern.substring(0, splitPoint);
        String secondPart = null;
        if (splitPoint < intervalPattern.length()) {
            secondPart = intervalPattern.substring(splitPoint, intervalPattern.length());
        }
        return new PatternInfo(firstPart, secondPart, laterDateFirst);
    }

    public PatternInfo getIntervalPattern(String skeleton, int field) {
        if (field > MINIMUM_SUPPORTED_CALENDAR_FIELD) {
            throw new IllegalArgumentException("no support for field less than SECOND");
        }
        Map<String, PatternInfo> patternsOfOneSkeleton = (Map) this.fIntervalPatterns.get(skeleton);
        if (patternsOfOneSkeleton != null) {
            PatternInfo intervalPattern = (PatternInfo) patternsOfOneSkeleton.get(CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
            if (intervalPattern != null) {
                return intervalPattern;
            }
        }
        return null;
    }

    public String getFallbackIntervalPattern() {
        return this.fFallbackIntervalPattern;
    }

    public void setFallbackIntervalPattern(String fallbackPattern) {
        if (this.frozen) {
            throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
        }
        int firstPatternIndex = fallbackPattern.indexOf("{0}");
        int secondPatternIndex = fallbackPattern.indexOf("{1}");
        if (firstPatternIndex == -1 || secondPatternIndex == -1) {
            throw new IllegalArgumentException("no pattern {0} or pattern {1} in fallbackPattern");
        }
        if (firstPatternIndex > secondPatternIndex) {
            this.fFirstDateInPtnIsLaterDate = true;
        }
        this.fFallbackIntervalPattern = fallbackPattern;
    }

    public boolean getDefaultOrder() {
        return this.fFirstDateInPtnIsLaterDate;
    }

    public Object clone() {
        if (this.frozen) {
            return this;
        }
        return cloneUnfrozenDII();
    }

    private Object cloneUnfrozenDII() {
        try {
            DateIntervalInfo other = (DateIntervalInfo) super.clone();
            other.fFallbackIntervalPattern = this.fFallbackIntervalPattern;
            other.fFirstDateInPtnIsLaterDate = this.fFirstDateInPtnIsLaterDate;
            if (this.fIntervalPatternsReadOnly) {
                other.fIntervalPatterns = this.fIntervalPatterns;
                other.fIntervalPatternsReadOnly = true;
            } else {
                other.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                other.fIntervalPatternsReadOnly = false;
            }
            other.frozen = false;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("clone is not supported", e);
        }
    }

    private static Map<String, Map<String, PatternInfo>> cloneIntervalPatterns(Map<String, Map<String, PatternInfo>> patterns) {
        Map<String, Map<String, PatternInfo>> result = new HashMap();
        for (Entry<String, Map<String, PatternInfo>> skeletonEntry : patterns.entrySet()) {
            String skeleton = (String) skeletonEntry.getKey();
            Map<String, PatternInfo> patternsOfOneSkeleton = (Map) skeletonEntry.getValue();
            Map<String, PatternInfo> oneSetPtn = new HashMap();
            for (Entry<String, PatternInfo> calEntry : patternsOfOneSkeleton.entrySet()) {
                oneSetPtn.put((String) calEntry.getKey(), (PatternInfo) calEntry.getValue());
            }
            result.put(skeleton, oneSetPtn);
        }
        return result;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public DateIntervalInfo freeze() {
        this.fIntervalPatternsReadOnly = true;
        this.frozen = true;
        return this;
    }

    public DateIntervalInfo cloneAsThawed() {
        return (DateIntervalInfo) cloneUnfrozenDII();
    }

    static void parseSkeleton(String skeleton, int[] skeletonFieldWidth) {
        for (int i = 0; i < skeleton.length(); i += currentSerialVersion) {
            int charAt = skeleton.charAt(i) - 65;
            skeletonFieldWidth[charAt] = skeletonFieldWidth[charAt] + currentSerialVersion;
        }
    }

    private static boolean stringNumeric(int fieldWidth, int anotherFieldWidth, char patternLetter) {
        if (patternLetter != 'M' || ((fieldWidth > 2 || anotherFieldWidth <= 2) && (fieldWidth <= 2 || anotherFieldWidth > 2))) {
            return false;
        }
        return true;
    }

    BestMatchInfo getBestSkeleton(String inputSkeleton) {
        String bestSkeleton = inputSkeleton;
        int[] inputSkeletonFieldWidth = new int[58];
        int[] skeletonFieldWidth = new int[58];
        boolean replaceZWithV = false;
        if (inputSkeleton.indexOf(UScript.INSCRIPTIONAL_PAHLAVI) != -1) {
            inputSkeleton = inputSkeleton.replace('z', 'v');
            replaceZWithV = true;
        }
        parseSkeleton(inputSkeleton, inputSkeletonFieldWidth);
        int bestDistance = AnnualTimeZoneRule.MAX_YEAR;
        int bestFieldDifference = 0;
        for (String skeleton : this.fIntervalPatterns.keySet()) {
            int i = 0;
            while (true) {
                int length = skeletonFieldWidth.length;
                if (i >= r0) {
                    break;
                }
                skeletonFieldWidth[i] = 0;
                i += currentSerialVersion;
            }
            parseSkeleton(skeleton, skeletonFieldWidth);
            int distance = 0;
            int fieldDifference = currentSerialVersion;
            i = 0;
            while (true) {
                length = inputSkeletonFieldWidth.length;
                if (i >= r0) {
                    break;
                }
                int inputFieldWidth = inputSkeletonFieldWidth[i];
                int fieldWidth = skeletonFieldWidth[i];
                if (inputFieldWidth != fieldWidth) {
                    if (inputFieldWidth == 0) {
                        fieldDifference = -1;
                        distance += VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS;
                    } else if (fieldWidth == 0) {
                        fieldDifference = -1;
                        distance += VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS;
                    } else {
                        if (stringNumeric(inputFieldWidth, fieldWidth, (char) (i + 65))) {
                            distance += NodeFilter.SHOW_DOCUMENT;
                        } else {
                            distance += Math.abs(inputFieldWidth - fieldWidth);
                        }
                    }
                }
                i += currentSerialVersion;
            }
            if (distance < bestDistance) {
                bestSkeleton = skeleton;
                bestDistance = distance;
                bestFieldDifference = fieldDifference;
                continue;
            }
            if (distance == 0) {
                bestFieldDifference = 0;
                break;
            }
        }
        if (replaceZWithV && bestFieldDifference != -1) {
            bestFieldDifference = 2;
        }
        return new BestMatchInfo(bestSkeleton, bestFieldDifference);
    }

    public boolean equals(Object a) {
        if (!(a instanceof DateIntervalInfo)) {
            return false;
        }
        return this.fIntervalPatterns.equals(((DateIntervalInfo) a).fIntervalPatterns);
    }

    public int hashCode() {
        return this.fIntervalPatterns.hashCode();
    }

    @Deprecated
    public Map<String, Set<String>> getPatterns() {
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap();
        for (Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            result.put((String) entry.getKey(), new LinkedHashSet(((Map) entry.getValue()).keySet()));
        }
        return result;
    }

    @Deprecated
    public Map<String, Map<String, PatternInfo>> getRawPatterns() {
        LinkedHashMap<String, Map<String, PatternInfo>> result = new LinkedHashMap();
        for (Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            result.put((String) entry.getKey(), new LinkedHashMap((Map) entry.getValue()));
        }
        return result;
    }
}
