package com.android.internal.telephony;

import android.app.AppGlobals;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.util.AtomicFile;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SmsUsageMonitor {
    private static final String ATTR_COUNTRY = "country";
    private static final String ATTR_FREE = "free";
    private static final String ATTR_PACKAGE_NAME = "name";
    private static final String ATTR_PACKAGE_SMS_POLICY = "sms-policy";
    private static final String ATTR_PATTERN = "pattern";
    private static final String ATTR_PREMIUM = "premium";
    private static final String ATTR_STANDARD = "standard";
    static final int CATEGORY_FREE_SHORT_CODE = 1;
    static final int CATEGORY_NOT_SHORT_CODE = 0;
    static final int CATEGORY_POSSIBLE_PREMIUM_SHORT_CODE = 3;
    static final int CATEGORY_PREMIUM_SHORT_CODE = 4;
    static final int CATEGORY_STANDARD_SHORT_CODE = 2;
    private static final boolean DBG = false;
    private static final int DEFAULT_SMS_CHECK_PERIOD = 60000;
    private static final int DEFAULT_SMS_MAX_COUNT = 1000;
    private static final boolean ENABLE_CUSTOM_SHORTCODES = false;
    public static final int PREMIUM_SMS_PERMISSION_ALWAYS_ALLOW = 3;
    public static final int PREMIUM_SMS_PERMISSION_ASK_USER = 1;
    public static final int PREMIUM_SMS_PERMISSION_NEVER_ALLOW = 2;
    public static final int PREMIUM_SMS_PERMISSION_UNKNOWN = 0;
    private static final String SHORT_CODE_PATH = "/data/misc/sms/codes";
    private static final String SMS_POLICY_FILE_DIRECTORY = "/data/misc/sms";
    private static final String SMS_POLICY_FILE_NAME = "premium_sms_policy.xml";
    private static final String TAG = "SmsUsageMonitor";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_SHORTCODE = "shortcode";
    private static final String TAG_SHORTCODES = "shortcodes";
    private static final String TAG_SMS_POLICY_BODY = "premium-sms-policy";
    private static final boolean VDBG = false;
    private String CUSTOM_SHORT_CODES;
    private List<String> CUSTOM_SHORT_CODES_LIST;
    private final AtomicBoolean mCheckEnabled;
    private final int mCheckPeriod;
    private final Context mContext;
    private String mCurrentCountry;
    private ShortCodePatternMatcher mCurrentPatternMatcher;
    private final int mMaxAllowed;
    private final File mPatternFile;
    private long mPatternFileLastModified;
    private AtomicFile mPolicyFile;
    private final HashMap<String, Integer> mPremiumSmsPolicy;
    private final SettingsObserverHandler mSettingsObserverHandler;
    private final HashMap<String, ArrayList<Long>> mSmsStamp;

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final AtomicBoolean mEnabled;

        SettingsObserver(Handler handler, Context context, AtomicBoolean enabled) {
            super(handler);
            this.mContext = context;
            this.mEnabled = enabled;
            onChange(SmsUsageMonitor.ENABLE_CUSTOM_SHORTCODES);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            AtomicBoolean atomicBoolean = this.mEnabled;
            if (Global.getInt(this.mContext.getContentResolver(), "sms_short_code_confirmation", SmsUsageMonitor.PREMIUM_SMS_PERMISSION_ASK_USER) == 0) {
                z = SmsUsageMonitor.ENABLE_CUSTOM_SHORTCODES;
            }
            atomicBoolean.set(z);
        }
    }

    private static class SettingsObserverHandler extends Handler {
        SettingsObserverHandler(Context context, AtomicBoolean enabled) {
            context.getContentResolver().registerContentObserver(Global.getUriFor("sms_short_code_confirmation"), SmsUsageMonitor.ENABLE_CUSTOM_SHORTCODES, new SettingsObserver(this, context, enabled));
        }
    }

    private static final class ShortCodePatternMatcher {
        private final Pattern mFreeShortCodePattern;
        private final Pattern mPremiumShortCodePattern;
        private final Pattern mShortCodePattern;
        private final Pattern mStandardShortCodePattern;

        ShortCodePatternMatcher(String shortCodeRegex, String premiumShortCodeRegex, String freeShortCodeRegex, String standardShortCodeRegex) {
            Pattern compile;
            Pattern pattern = null;
            if (shortCodeRegex != null) {
                compile = Pattern.compile(shortCodeRegex);
            } else {
                compile = null;
            }
            this.mShortCodePattern = compile;
            if (premiumShortCodeRegex != null) {
                compile = Pattern.compile(premiumShortCodeRegex);
            } else {
                compile = null;
            }
            this.mPremiumShortCodePattern = compile;
            if (freeShortCodeRegex != null) {
                compile = Pattern.compile(freeShortCodeRegex);
            } else {
                compile = null;
            }
            this.mFreeShortCodePattern = compile;
            if (standardShortCodeRegex != null) {
                pattern = Pattern.compile(standardShortCodeRegex);
            }
            this.mStandardShortCodePattern = pattern;
        }

        int getNumberCategory(String phoneNumber) {
            if (this.mFreeShortCodePattern != null && this.mFreeShortCodePattern.matcher(phoneNumber).matches()) {
                return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_ASK_USER;
            }
            if (this.mStandardShortCodePattern != null && this.mStandardShortCodePattern.matcher(phoneNumber).matches()) {
                return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_NEVER_ALLOW;
            }
            if (this.mPremiumShortCodePattern != null && this.mPremiumShortCodePattern.matcher(phoneNumber).matches()) {
                return SmsUsageMonitor.CATEGORY_PREMIUM_SHORT_CODE;
            }
            if (this.mShortCodePattern == null || !this.mShortCodePattern.matcher(phoneNumber).matches()) {
                return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_UNKNOWN;
            }
            return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_ALWAYS_ALLOW;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SmsUsageMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SmsUsageMonitor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsUsageMonitor.<clinit>():void");
    }

    public static int mergeShortCodeCategories(int type1, int type2) {
        if (type1 > type2) {
            return type1;
        }
        return type2;
    }

    public SmsUsageMonitor(Context context) {
        this.mSmsStamp = new HashMap();
        this.mCheckEnabled = new AtomicBoolean(true);
        this.mPatternFile = new File(SHORT_CODE_PATH);
        this.mPatternFileLastModified = 0;
        this.mPremiumSmsPolicy = new HashMap();
        this.CUSTOM_SHORT_CODES = null;
        this.CUSTOM_SHORT_CODES_LIST = new ArrayList();
        this.mContext = context;
        ContentResolver resolver = context.getContentResolver();
        this.mMaxAllowed = Global.getInt(resolver, "sms_outgoing_check_max_count", DEFAULT_SMS_MAX_COUNT);
        this.mCheckPeriod = Global.getInt(resolver, "sms_outgoing_check_interval_ms", DEFAULT_SMS_CHECK_PERIOD);
        if (ENABLE_CUSTOM_SHORTCODES) {
            this.CUSTOM_SHORT_CODES = Systemex.getString(context.getContentResolver(), "custom_sms_shortcodes");
            if (!(this.CUSTOM_SHORT_CODES == null || this.CUSTOM_SHORT_CODES.equals("") || !this.CUSTOM_SHORT_CODES_LIST.isEmpty())) {
                this.CUSTOM_SHORT_CODES_LIST = Arrays.asList(this.CUSTOM_SHORT_CODES.split(";"));
            }
        }
        this.mSettingsObserverHandler = new SettingsObserverHandler(this.mContext, this.mCheckEnabled);
        loadPremiumSmsPolicyDb();
    }

    private ShortCodePatternMatcher getPatternMatcherFromFile(String country) {
        XmlPullParserException e;
        Throwable th;
        FileReader fileReader = null;
        try {
            FileReader patternReader = new FileReader(this.mPatternFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(patternReader);
                ShortCodePatternMatcher patternMatcherFromXmlParser = getPatternMatcherFromXmlParser(parser, country);
                this.mPatternFileLastModified = this.mPatternFile.lastModified();
                if (patternReader != null) {
                    try {
                        patternReader.close();
                    } catch (IOException e2) {
                    }
                }
                return patternMatcherFromXmlParser;
            } catch (FileNotFoundException e3) {
                fileReader = patternReader;
                Rlog.e(TAG, "Short Code Pattern File not found");
                this.mPatternFileLastModified = this.mPatternFile.lastModified();
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            } catch (XmlPullParserException e5) {
                e = e5;
                fileReader = patternReader;
                try {
                    Rlog.e(TAG, "XML parser exception reading short code pattern file", e);
                    this.mPatternFileLastModified = this.mPatternFile.lastModified();
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    this.mPatternFileLastModified = this.mPatternFile.lastModified();
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileReader = patternReader;
                this.mPatternFileLastModified = this.mPatternFile.lastModified();
                if (fileReader != null) {
                    fileReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            Rlog.e(TAG, "Short Code Pattern File not found");
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            if (fileReader != null) {
                fileReader.close();
            }
            return null;
        } catch (XmlPullParserException e9) {
            e = e9;
            Rlog.e(TAG, "XML parser exception reading short code pattern file", e);
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            if (fileReader != null) {
                fileReader.close();
            }
            return null;
        }
    }

    private ShortCodePatternMatcher getPatternMatcherFromResource(String country) {
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = this.mContext.getResources().getXml(17891347);
            ShortCodePatternMatcher patternMatcherFromXmlParser = getPatternMatcherFromXmlParser(xmlResourceParser, country);
            return patternMatcherFromXmlParser;
        } finally {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private ShortCodePatternMatcher getPatternMatcherFromXmlParser(XmlPullParser parser, String country) {
        try {
            XmlUtils.beginDocument(parser, TAG_SHORTCODES);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                } else if (!element.equals(TAG_SHORTCODE)) {
                    Rlog.e(TAG, "Error: skipping unknown XML tag " + element);
                } else if (country.equals(parser.getAttributeValue(null, ATTR_COUNTRY))) {
                    return new ShortCodePatternMatcher(parser.getAttributeValue(null, ATTR_PATTERN), parser.getAttributeValue(null, ATTR_PREMIUM), parser.getAttributeValue(null, ATTR_FREE), parser.getAttributeValue(null, ATTR_STANDARD));
                }
            }
            Rlog.e(TAG, "Parsing pattern data found null");
        } catch (XmlPullParserException e) {
            Rlog.e(TAG, "XML parser exception reading short code patterns", e);
        } catch (IOException e2) {
            Rlog.e(TAG, "I/O exception reading short code patterns", e2);
        }
        return null;
    }

    void dispose() {
        this.mSmsStamp.clear();
    }

    public boolean check(String appName, int smsWaiting) {
        boolean isUnderLimit;
        synchronized (this.mSmsStamp) {
            removeExpiredTimestamps();
            ArrayList<Long> sentList = (ArrayList) this.mSmsStamp.get(appName);
            if (sentList == null) {
                sentList = new ArrayList();
                this.mSmsStamp.put(appName, sentList);
            }
            isUnderLimit = isUnderLimit(sentList, smsWaiting);
        }
        return isUnderLimit;
    }

    public int checkDestination(String destAddress, String countryIso) {
        synchronized (this.mSettingsObserverHandler) {
            if (PhoneNumberUtils.isEmergencyNumber(destAddress, countryIso)) {
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            } else if (this.mCheckEnabled.get()) {
                if (countryIso != null) {
                    if (!(this.mCurrentCountry != null && countryIso.equals(this.mCurrentCountry) && this.mPatternFile.lastModified() == this.mPatternFileLastModified)) {
                        if (this.mPatternFile.exists()) {
                            this.mCurrentPatternMatcher = getPatternMatcherFromFile(countryIso);
                        } else {
                            this.mCurrentPatternMatcher = getPatternMatcherFromResource(countryIso);
                        }
                        this.mCurrentCountry = countryIso;
                    }
                }
                if (this.mCurrentPatternMatcher != null) {
                    int numberCategory = this.mCurrentPatternMatcher.getNumberCategory(destAddress);
                    return numberCategory;
                }
                Rlog.e(TAG, "No patterns for \"" + countryIso + "\": using generic short code rule");
                if (destAddress.length() <= 5) {
                    return PREMIUM_SMS_PERMISSION_ALWAYS_ALLOW;
                }
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            } else {
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            }
        }
    }

    public int checkDestinationHW(String destAddress, String countryIso) {
        synchronized (this.mSettingsObserverHandler) {
            if (PhoneNumberUtils.isEmergencyNumber(destAddress, countryIso)) {
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            } else if (!this.mCheckEnabled.get()) {
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            } else if (ENABLE_CUSTOM_SHORTCODES && isSpecialPattern(destAddress)) {
                return PREMIUM_SMS_PERMISSION_ASK_USER;
            } else {
                return checkDestination(destAddress, countryIso);
            }
        }
    }

    private boolean isSpecialPattern(String destAdd) {
        boolean z = ENABLE_CUSTOM_SHORTCODES;
        if (this.CUSTOM_SHORT_CODES_LIST.isEmpty()) {
            return ENABLE_CUSTOM_SHORTCODES;
        }
        if (this.CUSTOM_SHORT_CODES_LIST.contains(destAdd)) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadPremiumSmsPolicyDb() {
        synchronized (this.mPremiumSmsPolicy) {
            if (this.mPolicyFile == null) {
                this.mPolicyFile = new AtomicFile(new File(new File(SMS_POLICY_FILE_DIRECTORY), SMS_POLICY_FILE_NAME));
                this.mPremiumSmsPolicy.clear();
                FileInputStream infile = null;
                try {
                    infile = this.mPolicyFile.openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(infile, StandardCharsets.UTF_8.name());
                    XmlUtils.beginDocument(parser, TAG_SMS_POLICY_BODY);
                    while (true) {
                        XmlUtils.nextElement(parser);
                        String element = parser.getName();
                        if (element == null) {
                            break;
                        } else if (element.equals(TAG_PACKAGE)) {
                            String packageName = parser.getAttributeValue(null, ATTR_PACKAGE_NAME);
                            String policy = parser.getAttributeValue(null, ATTR_PACKAGE_SMS_POLICY);
                            if (packageName == null) {
                                Rlog.e(TAG, "Error: missing package name attribute");
                            } else if (policy == null) {
                                Rlog.e(TAG, "Error: missing package policy attribute");
                            } else {
                                this.mPremiumSmsPolicy.put(packageName, Integer.valueOf(Integer.parseInt(policy)));
                            }
                        } else {
                            Rlog.e(TAG, "Error: skipping unknown XML tag " + element);
                        }
                    }
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (FileNotFoundException e2) {
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (IOException e4) {
                    Rlog.e(TAG, "Unable to read premium SMS policy database", e4);
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (NumberFormatException e6) {
                    Rlog.e(TAG, "Unable to parse premium SMS policy database", e6);
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e7) {
                        }
                    }
                } catch (XmlPullParserException e8) {
                    Rlog.e(TAG, "Unable to parse premium SMS policy database", e8);
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e9) {
                        }
                    }
                }
            }
        }
    }

    private void writePremiumSmsPolicyDb() {
        synchronized (this.mPremiumSmsPolicy) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = this.mPolicyFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, TAG_SMS_POLICY_BODY);
                for (Entry<String, Integer> policy : this.mPremiumSmsPolicy.entrySet()) {
                    out.startTag(null, TAG_PACKAGE);
                    out.attribute(null, ATTR_PACKAGE_NAME, (String) policy.getKey());
                    out.attribute(null, ATTR_PACKAGE_SMS_POLICY, ((Integer) policy.getValue()).toString());
                    out.endTag(null, TAG_PACKAGE);
                }
                out.endTag(null, TAG_SMS_POLICY_BODY);
                out.endDocument();
                this.mPolicyFile.finishWrite(fileOutputStream);
            } catch (IOException e) {
                Rlog.e(TAG, "Unable to write premium SMS policy database", e);
                if (fileOutputStream != null) {
                    this.mPolicyFile.failWrite(fileOutputStream);
                }
            }
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        checkCallerIsSystemOrPhoneOrSameApp(packageName);
        synchronized (this.mPremiumSmsPolicy) {
            Integer policy = (Integer) this.mPremiumSmsPolicy.get(packageName);
            if (policy == null) {
                return PREMIUM_SMS_PERMISSION_UNKNOWN;
            }
            int intValue = policy.intValue();
            return intValue;
        }
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        checkCallerIsSystemOrPhoneApp();
        if (permission < PREMIUM_SMS_PERMISSION_ASK_USER || permission > PREMIUM_SMS_PERMISSION_ALWAYS_ALLOW) {
            throw new IllegalArgumentException("invalid SMS permission type " + permission);
        }
        synchronized (this.mPremiumSmsPolicy) {
            this.mPremiumSmsPolicy.put(packageName, Integer.valueOf(permission));
        }
        new Thread(new Runnable() {
            public void run() {
                SmsUsageMonitor.this.writePremiumSmsPolicyDb();
            }
        }).start();
    }

    private static void checkCallerIsSystemOrPhoneOrSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        int appId = UserHandle.getAppId(uid);
        if (appId != DEFAULT_SMS_MAX_COUNT && appId != TelephonyEventLog.TAG_RIL_REQUEST && uid != 0) {
            try {
                ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, PREMIUM_SMS_PERMISSION_UNKNOWN, UserHandle.getCallingUserId());
                if (!UserHandle.isSameApp(ai.uid, uid)) {
                    throw new SecurityException("Calling uid " + uid + " gave package" + pkg + " which is owned by uid " + ai.uid);
                }
            } catch (RemoteException re) {
                throw new SecurityException("Unknown package " + pkg + "\n" + re);
            }
        }
    }

    private static void checkCallerIsSystemOrPhoneApp() {
        int uid = Binder.getCallingUid();
        int appId = UserHandle.getAppId(uid);
        if (appId != DEFAULT_SMS_MAX_COUNT && appId != TelephonyEventLog.TAG_RIL_REQUEST && uid != 0) {
            throw new SecurityException("Disallowed call for uid " + uid);
        }
    }

    private void removeExpiredTimestamps() {
        long beginCheckPeriod = System.currentTimeMillis() - ((long) this.mCheckPeriod);
        synchronized (this.mSmsStamp) {
            Iterator<Entry<String, ArrayList<Long>>> iter = this.mSmsStamp.entrySet().iterator();
            while (iter.hasNext()) {
                ArrayList<Long> oldList = (ArrayList) ((Entry) iter.next()).getValue();
                if (oldList.isEmpty() || ((Long) oldList.get(oldList.size() - 1)).longValue() < beginCheckPeriod) {
                    iter.remove();
                }
            }
        }
    }

    private boolean isUnderLimit(ArrayList<Long> sent, int smsWaiting) {
        Long ct = Long.valueOf(System.currentTimeMillis());
        long beginCheckPeriod = ct.longValue() - ((long) this.mCheckPeriod);
        while (!sent.isEmpty() && ((Long) sent.get(PREMIUM_SMS_PERMISSION_UNKNOWN)).longValue() < beginCheckPeriod) {
            sent.remove(PREMIUM_SMS_PERMISSION_UNKNOWN);
        }
        if (sent.size() + smsWaiting > this.mMaxAllowed) {
            return ENABLE_CUSTOM_SHORTCODES;
        }
        for (int i = PREMIUM_SMS_PERMISSION_UNKNOWN; i < smsWaiting; i += PREMIUM_SMS_PERMISSION_ASK_USER) {
            sent.add(ct);
        }
        return true;
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    public boolean isCurrentPatternMatcherNull() {
        return this.mCurrentPatternMatcher == null ? true : ENABLE_CUSTOM_SHORTCODES;
    }
}
