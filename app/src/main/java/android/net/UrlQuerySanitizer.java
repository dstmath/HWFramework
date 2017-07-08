package android.net;

import android.media.ToneGenerator;
import android.rms.HwSysResource;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class UrlQuerySanitizer {
    private static final ValueSanitizer sAllButNulAndAngleBracketsLegal = null;
    private static final ValueSanitizer sAllButNulLegal = null;
    private static final ValueSanitizer sAllButWhitespaceLegal = null;
    private static final ValueSanitizer sAllIllegal = null;
    private static final ValueSanitizer sAmpAndSpaceLegal = null;
    private static final ValueSanitizer sAmpLegal = null;
    private static final ValueSanitizer sSpaceLegal = null;
    private static final ValueSanitizer sURLLegal = null;
    private static final ValueSanitizer sUrlAndSpaceLegal = null;
    private boolean mAllowUnregisteredParamaters;
    private final HashMap<String, String> mEntries;
    private final ArrayList<ParameterValuePair> mEntriesList;
    private boolean mPreferFirstRepeatedParameter;
    private final HashMap<String, ValueSanitizer> mSanitizers;
    private ValueSanitizer mUnregisteredParameterValueSanitizer;

    public interface ValueSanitizer {
        String sanitize(String str);
    }

    public static class IllegalCharacterValueSanitizer implements ValueSanitizer {
        public static final int ALL_BUT_NUL_AND_ANGLE_BRACKETS_LEGAL = 1439;
        public static final int ALL_BUT_NUL_LEGAL = 1535;
        public static final int ALL_BUT_WHITESPACE_LEGAL = 1532;
        public static final int ALL_ILLEGAL = 0;
        public static final int ALL_OK = 2047;
        public static final int ALL_WHITESPACE_OK = 3;
        public static final int AMP_AND_SPACE_LEGAL = 129;
        public static final int AMP_LEGAL = 128;
        public static final int AMP_OK = 128;
        public static final int DQUOTE_OK = 8;
        public static final int GT_OK = 64;
        private static final String JAVASCRIPT_PREFIX = "javascript:";
        public static final int LT_OK = 32;
        private static final int MIN_SCRIPT_PREFIX_LENGTH = 0;
        public static final int NON_7_BIT_ASCII_OK = 4;
        public static final int NUL_OK = 512;
        public static final int OTHER_WHITESPACE_OK = 2;
        public static final int PCT_OK = 256;
        public static final int SCRIPT_URL_OK = 1024;
        public static final int SPACE_LEGAL = 1;
        public static final int SPACE_OK = 1;
        public static final int SQUOTE_OK = 16;
        public static final int URL_AND_SPACE_LEGAL = 405;
        public static final int URL_LEGAL = 404;
        private static final String VBSCRIPT_PREFIX = "vbscript:";
        private int mFlags;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void");
        }

        public IllegalCharacterValueSanitizer(int flags) {
            this.mFlags = flags;
        }

        public String sanitize(String value) {
            if (value == null) {
                return null;
            }
            int length = value.length();
            if ((this.mFlags & SCRIPT_URL_OK) != 0 && length >= MIN_SCRIPT_PREFIX_LENGTH) {
                String asLower = value.toLowerCase(Locale.ROOT);
                if (asLower.startsWith(JAVASCRIPT_PREFIX) || asLower.startsWith(VBSCRIPT_PREFIX)) {
                    return ProxyInfo.LOCAL_EXCL_LIST;
                }
            }
            if ((this.mFlags & ALL_WHITESPACE_OK) == 0) {
                value = trimWhitespace(value);
                length = value.length();
            }
            StringBuilder stringBuilder = new StringBuilder(length);
            for (int i = MIN_SCRIPT_PREFIX_LENGTH; i < length; i += SPACE_OK) {
                char c = value.charAt(i);
                if (!characterIsLegal(c)) {
                    if ((this.mFlags & SPACE_OK) != 0) {
                        c = ' ';
                    } else {
                        c = '_';
                    }
                }
                stringBuilder.append(c);
            }
            return stringBuilder.toString();
        }

        private String trimWhitespace(String value) {
            int start = MIN_SCRIPT_PREFIX_LENGTH;
            int last = value.length() - 1;
            int end = last;
            while (start <= last && isWhitespace(value.charAt(start))) {
                start += SPACE_OK;
            }
            while (end >= start && isWhitespace(value.charAt(end))) {
                end--;
            }
            if (start == 0 && end == last) {
                return value;
            }
            return value.substring(start, end + SPACE_OK);
        }

        private boolean isWhitespace(char c) {
            switch (c) {
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                case LT_OK /*32*/:
                    return true;
                default:
                    return false;
            }
        }

        private boolean characterIsLegal(char c) {
            boolean z = true;
            switch (c) {
                case MIN_SCRIPT_PREFIX_LENGTH /*0*/:
                    if ((this.mFlags & NUL_OK) == 0) {
                        z = false;
                    }
                    return z;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                    if ((this.mFlags & OTHER_WHITESPACE_OK) == 0) {
                        z = false;
                    }
                    return z;
                case LT_OK /*32*/:
                    if ((this.mFlags & SPACE_OK) == 0) {
                        z = false;
                    }
                    return z;
                case HwSysResource.APPMNGWHITELIST /*34*/:
                    if ((this.mFlags & DQUOTE_OK) == 0) {
                        z = false;
                    }
                    return z;
                case HwSysResource.ORDEREDBROADCAST /*37*/:
                    if ((this.mFlags & PCT_OK) == 0) {
                        z = false;
                    }
                    return z;
                case HwSysResource.TOTAL /*38*/:
                    if ((this.mFlags & AMP_OK) == 0) {
                        z = false;
                    }
                    return z;
                case ConnectivityManager.TYPE_MOBILE_BIP1 /*39*/:
                    if ((this.mFlags & SQUOTE_OK) == 0) {
                        z = false;
                    }
                    return z;
                case NetworkAgent.WIFI_BASE_SCORE /*60*/:
                    if ((this.mFlags & LT_OK) == 0) {
                        z = false;
                    }
                    return z;
                case ToneGenerator.TONE_CDMA_HIGH_SS_2 /*62*/:
                    if ((this.mFlags & GT_OK) == 0) {
                        z = false;
                    }
                    return z;
                default:
                    if ((c < ' ' || c >= '\u007f') && (c < '\u0080' || (this.mFlags & NON_7_BIT_ASCII_OK) == 0)) {
                        z = false;
                    }
                    return z;
            }
        }
    }

    public class ParameterValuePair {
        public String mParameter;
        public String mValue;
        final /* synthetic */ UrlQuerySanitizer this$0;

        public ParameterValuePair(UrlQuerySanitizer this$0, String parameter, String value) {
            this.this$0 = this$0;
            this.mParameter = parameter;
            this.mValue = value;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.UrlQuerySanitizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.UrlQuerySanitizer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.<clinit>():void");
    }

    public ValueSanitizer getUnregisteredParameterValueSanitizer() {
        return this.mUnregisteredParameterValueSanitizer;
    }

    public void setUnregisteredParameterValueSanitizer(ValueSanitizer sanitizer) {
        this.mUnregisteredParameterValueSanitizer = sanitizer;
    }

    public static final ValueSanitizer getAllIllegal() {
        return sAllIllegal;
    }

    public static final ValueSanitizer getAllButNulLegal() {
        return sAllButNulLegal;
    }

    public static final ValueSanitizer getAllButWhitespaceLegal() {
        return sAllButWhitespaceLegal;
    }

    public static final ValueSanitizer getUrlLegal() {
        return sURLLegal;
    }

    public static final ValueSanitizer getUrlAndSpaceLegal() {
        return sUrlAndSpaceLegal;
    }

    public static final ValueSanitizer getAmpLegal() {
        return sAmpLegal;
    }

    public static final ValueSanitizer getAmpAndSpaceLegal() {
        return sAmpAndSpaceLegal;
    }

    public static final ValueSanitizer getSpaceLegal() {
        return sSpaceLegal;
    }

    public static final ValueSanitizer getAllButNulAndAngleBracketsLegal() {
        return sAllButNulAndAngleBracketsLegal;
    }

    public UrlQuerySanitizer() {
        this.mSanitizers = new HashMap();
        this.mEntries = new HashMap();
        this.mEntriesList = new ArrayList();
        this.mUnregisteredParameterValueSanitizer = getAllIllegal();
    }

    public UrlQuerySanitizer(String url) {
        this.mSanitizers = new HashMap();
        this.mEntries = new HashMap();
        this.mEntriesList = new ArrayList();
        this.mUnregisteredParameterValueSanitizer = getAllIllegal();
        setAllowUnregisteredParamaters(true);
        parseUrl(url);
    }

    public void parseUrl(String url) {
        String query;
        int queryIndex = url.indexOf(63);
        if (queryIndex >= 0) {
            query = url.substring(queryIndex + 1);
        } else {
            query = ProxyInfo.LOCAL_EXCL_LIST;
        }
        parseQuery(query);
    }

    public void parseQuery(String query) {
        clear();
        StringTokenizer tokenizer = new StringTokenizer(query, "&");
        while (tokenizer.hasMoreElements()) {
            String attributeValuePair = tokenizer.nextToken();
            if (attributeValuePair.length() > 0) {
                int assignmentIndex = attributeValuePair.indexOf(61);
                if (assignmentIndex < 0) {
                    parseEntry(attributeValuePair, ProxyInfo.LOCAL_EXCL_LIST);
                } else {
                    parseEntry(attributeValuePair.substring(0, assignmentIndex), attributeValuePair.substring(assignmentIndex + 1));
                }
            }
        }
    }

    public Set<String> getParameterSet() {
        return this.mEntries.keySet();
    }

    public List<ParameterValuePair> getParameterList() {
        return this.mEntriesList;
    }

    public boolean hasParameter(String parameter) {
        return this.mEntries.containsKey(parameter);
    }

    public String getValue(String parameter) {
        return (String) this.mEntries.get(parameter);
    }

    public void registerParameter(String parameter, ValueSanitizer valueSanitizer) {
        if (valueSanitizer == null) {
            this.mSanitizers.remove(parameter);
        }
        this.mSanitizers.put(parameter, valueSanitizer);
    }

    public void registerParameters(String[] parameters, ValueSanitizer valueSanitizer) {
        for (Object put : parameters) {
            this.mSanitizers.put(put, valueSanitizer);
        }
    }

    public void setAllowUnregisteredParamaters(boolean allowUnregisteredParamaters) {
        this.mAllowUnregisteredParamaters = allowUnregisteredParamaters;
    }

    public boolean getAllowUnregisteredParamaters() {
        return this.mAllowUnregisteredParamaters;
    }

    public void setPreferFirstRepeatedParameter(boolean preferFirstRepeatedParameter) {
        this.mPreferFirstRepeatedParameter = preferFirstRepeatedParameter;
    }

    public boolean getPreferFirstRepeatedParameter() {
        return this.mPreferFirstRepeatedParameter;
    }

    protected void parseEntry(String parameter, String value) {
        String unescapedParameter = unescape(parameter);
        ValueSanitizer valueSanitizer = getEffectiveValueSanitizer(unescapedParameter);
        if (valueSanitizer != null) {
            addSanitizedEntry(unescapedParameter, valueSanitizer.sanitize(unescape(value)));
        }
    }

    protected void addSanitizedEntry(String parameter, String value) {
        this.mEntriesList.add(new ParameterValuePair(this, parameter, value));
        if (!this.mPreferFirstRepeatedParameter || !this.mEntries.containsKey(parameter)) {
            this.mEntries.put(parameter, value);
        }
    }

    public ValueSanitizer getValueSanitizer(String parameter) {
        return (ValueSanitizer) this.mSanitizers.get(parameter);
    }

    public ValueSanitizer getEffectiveValueSanitizer(String parameter) {
        ValueSanitizer sanitizer = getValueSanitizer(parameter);
        if (sanitizer == null && this.mAllowUnregisteredParamaters) {
            return getUnregisteredParameterValueSanitizer();
        }
        return sanitizer;
    }

    public String unescape(String string) {
        int firstEscape = string.indexOf(37);
        if (firstEscape < 0) {
            firstEscape = string.indexOf(43);
            if (firstEscape < 0) {
                return string;
            }
        }
        int length = string.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        stringBuilder.append(string.substring(0, firstEscape));
        int i = firstEscape;
        while (i < length) {
            char c = string.charAt(i);
            if (c == '+') {
                c = ' ';
            } else if (c == '%' && i + 2 < length) {
                char c1 = string.charAt(i + 1);
                char c2 = string.charAt(i + 2);
                if (isHexDigit(c1) && isHexDigit(c2)) {
                    c = (char) ((decodeHexDigit(c1) * 16) + decodeHexDigit(c2));
                    i += 2;
                }
            }
            stringBuilder.append(c);
            i++;
        }
        return stringBuilder.toString();
    }

    protected boolean isHexDigit(char c) {
        return decodeHexDigit(c) >= 0;
    }

    protected int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c < 'a' || c > 'f') {
            return -1;
        }
        return (c - 97) + 10;
    }

    protected void clear() {
        this.mEntries.clear();
        this.mEntriesList.clear();
    }
}
