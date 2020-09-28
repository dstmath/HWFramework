package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
import android.provider.Settings;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SmsUsageMonitor extends AbstractSmsUsageMonitor {
    private static final String ATTR_COUNTRY = "country";
    private static final String ATTR_FREE = "free";
    private static final String ATTR_PACKAGE_NAME = "name";
    private static final String ATTR_PACKAGE_SMS_POLICY = "sms-policy";
    private static final String ATTR_PATTERN = "pattern";
    private static final String ATTR_PREMIUM = "premium";
    private static final String ATTR_STANDARD = "standard";
    private static final boolean DBG = false;
    private static final int DEFAULT_SMS_CHECK_PERIOD = 60000;
    private static final int DEFAULT_SMS_MAX_COUNT = 1000;
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
    private final AtomicBoolean mCheckEnabled = new AtomicBoolean(true);
    private final int mCheckPeriod;
    private final Context mContext;
    private String mCurrentCountry;
    private ShortCodePatternMatcher mCurrentPatternMatcher;
    private final int mMaxAllowed;
    private final File mPatternFile = new File(SHORT_CODE_PATH);
    private long mPatternFileLastModified = 0;
    private AtomicFile mPolicyFile;
    private final HashMap<String, Integer> mPremiumSmsPolicy = new HashMap<>();
    private final SettingsObserverHandler mSettingsObserverHandler;
    private final HashMap<String, ArrayList<Long>> mSmsStamp = new HashMap<>();

    public static int mergeShortCodeCategories(int type1, int type2) {
        if (type1 > type2) {
            return type1;
        }
        return type2;
    }

    /* access modifiers changed from: private */
    public static final class ShortCodePatternMatcher {
        private final Pattern mFreeShortCodePattern;
        private final Pattern mPremiumShortCodePattern;
        private final Pattern mShortCodePattern;
        private final Pattern mStandardShortCodePattern;

        ShortCodePatternMatcher(String shortCodeRegex, String premiumShortCodeRegex, String freeShortCodeRegex, String standardShortCodeRegex) {
            Pattern pattern = null;
            this.mShortCodePattern = shortCodeRegex != null ? Pattern.compile(shortCodeRegex) : null;
            this.mPremiumShortCodePattern = premiumShortCodeRegex != null ? Pattern.compile(premiumShortCodeRegex) : null;
            this.mFreeShortCodePattern = freeShortCodeRegex != null ? Pattern.compile(freeShortCodeRegex) : null;
            this.mStandardShortCodePattern = standardShortCodeRegex != null ? Pattern.compile(standardShortCodeRegex) : pattern;
        }

        /* access modifiers changed from: package-private */
        public int getNumberCategory(String phoneNumber) {
            Pattern pattern = this.mFreeShortCodePattern;
            if (pattern != null && pattern.matcher(phoneNumber).matches()) {
                return 1;
            }
            Pattern pattern2 = this.mStandardShortCodePattern;
            if (pattern2 != null && pattern2.matcher(phoneNumber).matches()) {
                return 2;
            }
            Pattern pattern3 = this.mPremiumShortCodePattern;
            if (pattern3 != null && pattern3.matcher(phoneNumber).matches()) {
                return 4;
            }
            Pattern pattern4 = this.mShortCodePattern;
            if (pattern4 == null || !pattern4.matcher(phoneNumber).matches()) {
                return 0;
            }
            return 3;
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final AtomicBoolean mEnabled;

        SettingsObserver(Handler handler, Context context, AtomicBoolean enabled) {
            super(handler);
            this.mContext = context;
            this.mEnabled = enabled;
            onChange(false);
        }

        public void onChange(boolean selfChange) {
            AtomicBoolean atomicBoolean = this.mEnabled;
            boolean z = true;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "sms_short_code_confirmation", 1) == 0) {
                z = false;
            }
            atomicBoolean.set(z);
        }
    }

    private static class SettingsObserverHandler extends Handler {
        SettingsObserverHandler(Context context, AtomicBoolean enabled) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sms_short_code_confirmation"), false, new SettingsObserver(this, context, enabled));
        }
    }

    @UnsupportedAppUsage
    public SmsUsageMonitor(Context context) {
        this.mContext = context;
        ContentResolver resolver = context.getContentResolver();
        this.mMaxAllowed = Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 1000);
        this.mCheckPeriod = Settings.Global.getInt(resolver, "sms_outgoing_check_interval_ms", 60000);
        this.mSettingsObserverHandler = new SettingsObserverHandler(this.mContext, this.mCheckEnabled);
        loadPremiumSmsPolicyDb();
    }

    private ShortCodePatternMatcher getPatternMatcherFromFile(String country) {
        FileReader patternReader = null;
        patternReader = null;
        try {
            patternReader = new FileReader(this.mPatternFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(patternReader);
            ShortCodePatternMatcher patternMatcherFromXmlParser = getPatternMatcherFromXmlParser(parser, country);
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            try {
                patternReader.close();
            } catch (IOException e) {
            }
            return patternMatcherFromXmlParser;
        } catch (FileNotFoundException e2) {
            Rlog.e(TAG, "Short Code Pattern File not found");
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            if (patternReader == null) {
                return null;
            }
            patternReader.close();
            return null;
        } catch (XmlPullParserException e3) {
            Rlog.e(TAG, "XML parser exception reading short code pattern file", e3);
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            if (patternReader == null) {
                return null;
            }
            try {
                patternReader.close();
                return null;
            } catch (IOException e4) {
                return null;
            }
        } catch (Throwable th) {
            this.mPatternFileLastModified = this.mPatternFile.lastModified();
            if (patternReader != null) {
                try {
                    patternReader.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }

    private ShortCodePatternMatcher getPatternMatcherFromResource(String country) {
        XmlResourceParser parser = null;
        try {
            parser = this.mContext.getResources().getXml(18284565);
            return getPatternMatcherFromXmlParser(parser, country);
        } finally {
            if (parser != null) {
                parser.close();
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
                    Rlog.e(TAG, "Parsing pattern data found null");
                    break;
                } else if (!element.equals(TAG_SHORTCODE)) {
                    Rlog.e(TAG, "Error: skipping unknown XML tag " + element);
                } else if (country.equals(parser.getAttributeValue(null, ATTR_COUNTRY))) {
                    return new ShortCodePatternMatcher(parser.getAttributeValue(null, ATTR_PATTERN), parser.getAttributeValue(null, ATTR_PREMIUM), parser.getAttributeValue(null, ATTR_FREE), parser.getAttributeValue(null, ATTR_STANDARD));
                }
            }
        } catch (XmlPullParserException e) {
            Rlog.e(TAG, "XML parser exception reading short code patterns", e);
        } catch (IOException e2) {
            Rlog.e(TAG, "I/O exception reading short code patterns", e2);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        this.mSmsStamp.clear();
    }

    @UnsupportedAppUsage
    public boolean check(String appName, int smsWaiting) {
        boolean isUnderLimit;
        synchronized (this.mSmsStamp) {
            removeExpiredTimestamps();
            ArrayList<Long> sentList = this.mSmsStamp.get(appName);
            if (sentList == null) {
                sentList = new ArrayList<>();
                this.mSmsStamp.put(appName, sentList);
            }
            isUnderLimit = isUnderLimit(sentList, smsWaiting);
        }
        return isUnderLimit;
    }

    public int checkDestination(String destAddress, String countryIso) {
        synchronized (this.mSettingsObserverHandler) {
            if (PhoneNumberUtils.isEmergencyNumber(destAddress, countryIso)) {
                return 0;
            }
            if (!this.mCheckEnabled.get()) {
                return 0;
            }
            if (countryIso != null && (this.mCurrentCountry == null || !countryIso.equals(this.mCurrentCountry) || this.mPatternFile.lastModified() != this.mPatternFileLastModified)) {
                if (this.mPatternFile.exists()) {
                    this.mCurrentPatternMatcher = getPatternMatcherFromFile(countryIso);
                } else {
                    this.mCurrentPatternMatcher = getPatternMatcherFromResource(countryIso);
                }
                this.mCurrentCountry = countryIso;
            }
            if (this.mCurrentPatternMatcher == null || destAddress == null) {
                Rlog.e(TAG, "No patterns for \"" + countryIso + "\": using generic short code rule");
                if (destAddress == null || destAddress.length() > 5) {
                    return 0;
                }
                return 3;
            }
            return this.mCurrentPatternMatcher.getNumberCategory(destAddress);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b3, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        android.telephony.Rlog.e(com.android.internal.telephony.SmsUsageMonitor.TAG, "Unable to parse premium SMS policy database", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00bc, code lost:
        if (r2 != null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00be, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00c2, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c3, code lost:
        android.telephony.Rlog.e(com.android.internal.telephony.SmsUsageMonitor.TAG, "Unable to parse premium SMS policy database", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00cb, code lost:
        if (0 != 0) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00cd, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d1, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d2, code lost:
        android.telephony.Rlog.e(com.android.internal.telephony.SmsUsageMonitor.TAG, "Unable to read premium SMS policy database", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00da, code lost:
        if (r2 != null) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00dc, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ea, code lost:
        if (r2 != null) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ec, code lost:
        r2.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b3 A[ExcHandler: XmlPullParserException (r3v4 'e' org.xmlpull.v1.XmlPullParserException A[CUSTOM_DECLARE]), PHI: r2 
      PHI: (r2v6 'infile' java.io.FileInputStream) = (r2v3 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream) binds: [B:7:0x0022, B:21:0x0074, B:22:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:7:0x0022] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d1 A[ExcHandler: IOException (r3v2 'e' java.io.IOException A[CUSTOM_DECLARE]), PHI: r2 
      PHI: (r2v5 'infile' java.io.FileInputStream) = (r2v3 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream) binds: [B:7:0x0022, B:21:0x0074, B:22:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:7:0x0022] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e9 A[ExcHandler: FileNotFoundException (e java.io.FileNotFoundException), PHI: r2 
      PHI: (r2v4 'infile' java.io.FileInputStream) = (r2v3 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream), (r2v7 'infile' java.io.FileInputStream) binds: [B:7:0x0022, B:21:0x0074, B:22:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:7:0x0022] */
    private void loadPremiumSmsPolicyDb() {
        String policy;
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
                            policy = parser.getAttributeValue(null, ATTR_PACKAGE_SMS_POLICY);
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
                } catch (NumberFormatException e2) {
                    Rlog.e(TAG, "Error: non-numeric policy type " + policy);
                } catch (FileNotFoundException e3) {
                } catch (IOException e4) {
                } catch (XmlPullParserException e5) {
                } catch (Throwable th) {
                    if (infile != null) {
                        try {
                            infile.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writePremiumSmsPolicyDb() {
        synchronized (this.mPremiumSmsPolicy) {
            try {
                FileOutputStream outfile = this.mPolicyFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(outfile, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.startTag(null, TAG_SMS_POLICY_BODY);
                for (Map.Entry<String, Integer> policy : this.mPremiumSmsPolicy.entrySet()) {
                    out.startTag(null, TAG_PACKAGE);
                    out.attribute(null, ATTR_PACKAGE_NAME, policy.getKey());
                    out.attribute(null, ATTR_PACKAGE_SMS_POLICY, policy.getValue().toString());
                    out.endTag(null, TAG_PACKAGE);
                }
                out.endTag(null, TAG_SMS_POLICY_BODY);
                out.endDocument();
                this.mPolicyFile.finishWrite(outfile);
            } catch (IOException e) {
                Rlog.e(TAG, "Unable to write premium SMS policy database", e);
                if (0 != 0) {
                    this.mPolicyFile.failWrite(null);
                }
            }
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        checkCallerIsSystemOrPhoneOrSameApp(packageName);
        synchronized (this.mPremiumSmsPolicy) {
            Integer policy = this.mPremiumSmsPolicy.get(packageName);
            if (policy == null) {
                return 0;
            }
            return policy.intValue();
        }
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        checkCallerIsSystemOrPhoneApp();
        if (permission < 1 || permission > 3) {
            throw new IllegalArgumentException("invalid SMS permission type " + permission);
        }
        synchronized (this.mPremiumSmsPolicy) {
            this.mPremiumSmsPolicy.put(packageName, Integer.valueOf(permission));
        }
        new Thread(new Runnable() {
            /* class com.android.internal.telephony.SmsUsageMonitor.AnonymousClass1 */

            public void run() {
                SmsUsageMonitor.this.writePremiumSmsPolicyDb();
            }
        }).start();
    }

    private static void checkCallerIsSystemOrPhoneOrSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        int appId = UserHandle.getAppId(uid);
        if (appId != 1000 && appId != 1001 && uid != 0) {
            try {
                ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
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
        if (appId != 1000 && appId != 1001 && uid != 0) {
            throw new SecurityException("Disallowed call for uid " + uid);
        }
    }

    private void removeExpiredTimestamps() {
        long beginCheckPeriod = System.currentTimeMillis() - ((long) this.mCheckPeriod);
        synchronized (this.mSmsStamp) {
            Iterator<Map.Entry<String, ArrayList<Long>>> iter = this.mSmsStamp.entrySet().iterator();
            while (iter.hasNext()) {
                ArrayList<Long> oldList = iter.next().getValue();
                if (oldList.isEmpty() || oldList.get(oldList.size() - 1).longValue() < beginCheckPeriod) {
                    iter.remove();
                }
            }
        }
    }

    private boolean isUnderLimit(ArrayList<Long> sent, int smsWaiting) {
        Long ct = Long.valueOf(System.currentTimeMillis());
        long beginCheckPeriod = ct.longValue() - ((long) this.mCheckPeriod);
        while (!sent.isEmpty() && sent.get(0).longValue() < beginCheckPeriod) {
            sent.remove(0);
        }
        if (sent.size() + smsWaiting > this.mMaxAllowed) {
            return false;
        }
        for (int i = 0; i < smsWaiting; i++) {
            sent.add(ct);
        }
        return true;
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    public boolean isCurrentPatternMatcherNull() {
        return this.mCurrentPatternMatcher == null;
    }

    /* access modifiers changed from: protected */
    public AtomicBoolean getmCheckEnabledHw() {
        return this.mCheckEnabled;
    }

    /* access modifiers changed from: protected */
    public SettingsObserverHandler getmSettingsObserverHandlerHw() {
        return this.mSettingsObserverHandler;
    }
}
