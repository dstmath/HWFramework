package com.android.server.firewall;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.EventLogTags;
import com.android.server.IntentResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class IntentFirewall {
    private static final int LOG_PACKAGES_MAX_LENGTH = 150;
    private static final int LOG_PACKAGES_SUFFICIENT_LENGTH = 125;
    private static final File RULES_DIR = null;
    static final String TAG = "IntentFirewall";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_BROADCAST = "broadcast";
    private static final String TAG_RULES = "rules";
    private static final String TAG_SERVICE = "service";
    private static final int TYPE_ACTIVITY = 0;
    private static final int TYPE_BROADCAST = 1;
    private static final int TYPE_SERVICE = 2;
    private static final HashMap<String, FilterFactory> factoryMap = null;
    private FirewallIntentResolver mActivityResolver;
    private final AMSInterface mAms;
    private FirewallIntentResolver mBroadcastResolver;
    final FirewallHandler mHandler;
    private final RuleObserver mObserver;
    private FirewallIntentResolver mServiceResolver;

    public interface AMSInterface {
        int checkComponentPermission(String str, int i, int i2, int i3, boolean z);

        Object getAMSLock();
    }

    private final class FirewallHandler extends Handler {
        public FirewallHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            IntentFirewall.this.readRulesDir(IntentFirewall.getRulesDir());
        }
    }

    private static class FirewallIntentFilter extends IntentFilter {
        private final Rule rule;

        public FirewallIntentFilter(Rule rule) {
            this.rule = rule;
        }
    }

    private static class FirewallIntentResolver extends IntentResolver<FirewallIntentFilter, Rule> {
        private final ArrayMap<ComponentName, Rule[]> mRulesByComponent;

        private FirewallIntentResolver() {
            this.mRulesByComponent = new ArrayMap(IntentFirewall.TYPE_ACTIVITY);
        }

        protected boolean allowFilterResult(FirewallIntentFilter filter, List<Rule> dest) {
            return !dest.contains(filter.rule);
        }

        protected boolean isPackageForFilter(String packageName, FirewallIntentFilter filter) {
            return true;
        }

        protected FirewallIntentFilter[] newArray(int size) {
            return new FirewallIntentFilter[size];
        }

        protected Rule newResult(FirewallIntentFilter filter, int match, int userId) {
            return filter.rule;
        }

        protected void sortResults(List<Rule> list) {
        }

        public void queryByComponent(ComponentName componentName, List<Rule> candidateRules) {
            Rule[] rules = (Rule[]) this.mRulesByComponent.get(componentName);
            if (rules != null) {
                candidateRules.addAll(Arrays.asList(rules));
            }
        }

        public void addComponentFilter(ComponentName componentName, Rule rule) {
            this.mRulesByComponent.put(componentName, (Rule[]) ArrayUtils.appendElement(Rule.class, (Rule[]) this.mRulesByComponent.get(componentName), rule));
        }
    }

    private static class Rule extends AndFilter {
        private static final String ATTR_BLOCK = "block";
        private static final String ATTR_LOG = "log";
        private static final String ATTR_NAME = "name";
        private static final String TAG_COMPONENT_FILTER = "component-filter";
        private static final String TAG_INTENT_FILTER = "intent-filter";
        private boolean block;
        private boolean log;
        private final ArrayList<ComponentName> mComponentFilters;
        private final ArrayList<FirewallIntentFilter> mIntentFilters;

        private Rule() {
            this.mIntentFilters = new ArrayList(IntentFirewall.TYPE_BROADCAST);
            this.mComponentFilters = new ArrayList(IntentFirewall.TYPE_ACTIVITY);
        }

        public Rule readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            this.block = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_BLOCK));
            this.log = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_LOG));
            super.readFromXml(parser);
            return this;
        }

        protected void readChild(XmlPullParser parser) throws IOException, XmlPullParserException {
            String currentTag = parser.getName();
            if (currentTag.equals(TAG_INTENT_FILTER)) {
                FirewallIntentFilter intentFilter = new FirewallIntentFilter(this);
                intentFilter.readFromXml(parser);
                this.mIntentFilters.add(intentFilter);
            } else if (currentTag.equals(TAG_COMPONENT_FILTER)) {
                String componentStr = parser.getAttributeValue(null, ATTR_NAME);
                if (componentStr == null) {
                    throw new XmlPullParserException("Component name must be specified.", parser, null);
                }
                ComponentName componentName = ComponentName.unflattenFromString(componentStr);
                if (componentName == null) {
                    throw new XmlPullParserException("Invalid component name: " + componentStr);
                }
                this.mComponentFilters.add(componentName);
            } else {
                super.readChild(parser);
            }
        }

        public int getIntentFilterCount() {
            return this.mIntentFilters.size();
        }

        public FirewallIntentFilter getIntentFilter(int index) {
            return (FirewallIntentFilter) this.mIntentFilters.get(index);
        }

        public int getComponentFilterCount() {
            return this.mComponentFilters.size();
        }

        public ComponentName getComponentFilter(int index) {
            return (ComponentName) this.mComponentFilters.get(index);
        }

        public boolean getBlock() {
            return this.block;
        }

        public boolean getLog() {
            return this.log;
        }
    }

    private class RuleObserver extends FileObserver {
        private static final int MONITORED_EVENTS = 968;

        public RuleObserver(File monitoredDir) {
            super(monitoredDir.getAbsolutePath(), MONITORED_EVENTS);
        }

        public void onEvent(int event, String path) {
            if (path.endsWith(".xml")) {
                IntentFirewall.this.mHandler.removeMessages(IntentFirewall.TYPE_ACTIVITY);
                IntentFirewall.this.mHandler.sendEmptyMessageDelayed(IntentFirewall.TYPE_ACTIVITY, 250);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.firewall.IntentFirewall.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.firewall.IntentFirewall.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.IntentFirewall.<clinit>():void");
    }

    public IntentFirewall(AMSInterface ams, Handler handler) {
        this.mActivityResolver = new FirewallIntentResolver();
        this.mBroadcastResolver = new FirewallIntentResolver();
        this.mServiceResolver = new FirewallIntentResolver();
        this.mAms = ams;
        this.mHandler = new FirewallHandler(handler.getLooper());
        File rulesDir = getRulesDir();
        rulesDir.mkdirs();
        readRulesDir(rulesDir);
        this.mObserver = new RuleObserver(rulesDir);
        this.mObserver.startWatching();
    }

    public boolean checkStartActivity(Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        return checkIntent(this.mActivityResolver, intent.getComponent(), TYPE_ACTIVITY, intent, callerUid, callerPid, resolvedType, resolvedApp.uid);
    }

    public boolean checkService(ComponentName resolvedService, Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        return checkIntent(this.mServiceResolver, resolvedService, TYPE_SERVICE, intent, callerUid, callerPid, resolvedType, resolvedApp.uid);
    }

    public boolean checkBroadcast(Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return checkIntent(this.mBroadcastResolver, intent.getComponent(), TYPE_BROADCAST, intent, callerUid, callerPid, resolvedType, receivingUid);
    }

    public boolean checkIntent(FirewallIntentResolver resolver, ComponentName resolvedComponent, int intentType, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int log = TYPE_ACTIVITY;
        int block = TYPE_ACTIVITY;
        List<Rule> candidateRules = resolver.queryIntent(intent, resolvedType, false, TYPE_ACTIVITY);
        if (candidateRules == null) {
            candidateRules = new ArrayList();
        }
        resolver.queryByComponent(resolvedComponent, candidateRules);
        for (int i = TYPE_ACTIVITY; i < candidateRules.size(); i += TYPE_BROADCAST) {
            Rule rule = (Rule) candidateRules.get(i);
            if (rule.matches(this, resolvedComponent, intent, callerUid, callerPid, resolvedType, receivingUid)) {
                block |= rule.getBlock();
                log |= rule.getLog();
                if (!(block == 0 || log == 0)) {
                    break;
                }
            }
        }
        if (log) {
            logIntent(intentType, intent, callerUid, resolvedType);
        }
        if (block != 0) {
            return false;
        }
        return true;
    }

    private static void logIntent(int intentType, Intent intent, int callerUid, String resolvedType) {
        ComponentName cn = intent.getComponent();
        String shortComponent = null;
        if (cn != null) {
            shortComponent = cn.flattenToShortString();
        }
        String callerPackages = null;
        int callerPackageCount = TYPE_ACTIVITY;
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm != null) {
            try {
                String[] callerPackagesArray = pm.getPackagesForUid(callerUid);
                if (callerPackagesArray != null) {
                    callerPackageCount = callerPackagesArray.length;
                    callerPackages = joinPackages(callerPackagesArray);
                }
            } catch (RemoteException ex) {
                Slog.e(TAG, "Remote exception while retrieving packages", ex);
            }
        }
        EventLogTags.writeIfwIntentMatched(intentType, shortComponent, callerUid, callerPackageCount, callerPackages, intent.getAction(), resolvedType, intent.getDataString(), intent.getFlags());
    }

    private static String joinPackages(String[] packages) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (int i = TYPE_ACTIVITY; i < packages.length; i += TYPE_BROADCAST) {
            String pkg = packages[i];
            if ((sb.length() + pkg.length()) + TYPE_BROADCAST < LOG_PACKAGES_MAX_LENGTH) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(pkg);
            } else if (sb.length() >= LOG_PACKAGES_SUFFICIENT_LENGTH) {
                return sb.toString();
            }
        }
        if (sb.length() != 0 || packages.length <= 0) {
            return null;
        }
        pkg = packages[TYPE_ACTIVITY];
        return pkg.substring((pkg.length() - 150) + TYPE_BROADCAST) + '-';
    }

    public static File getRulesDir() {
        return RULES_DIR;
    }

    private void readRulesDir(File rulesDir) {
        int i;
        FirewallIntentResolver[] resolvers = new FirewallIntentResolver[3];
        for (i = TYPE_ACTIVITY; i < resolvers.length; i += TYPE_BROADCAST) {
            resolvers[i] = new FirewallIntentResolver();
        }
        File[] files = rulesDir.listFiles();
        if (files != null) {
            for (i = TYPE_ACTIVITY; i < files.length; i += TYPE_BROADCAST) {
                File file = files[i];
                if (file.getName().endsWith(".xml")) {
                    readRules(file, resolvers);
                }
            }
        }
        Slog.i(TAG, "Read new rules (A:" + resolvers[TYPE_ACTIVITY].filterSet().size() + " B:" + resolvers[TYPE_BROADCAST].filterSet().size() + " S:" + resolvers[TYPE_SERVICE].filterSet().size() + ")");
        synchronized (this.mAms.getAMSLock()) {
            this.mActivityResolver = resolvers[TYPE_ACTIVITY];
            this.mBroadcastResolver = resolvers[TYPE_BROADCAST];
            this.mServiceResolver = resolvers[TYPE_SERVICE];
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readRules(File rulesFile, FirewallIntentResolver[] resolvers) {
        int i;
        List<List<Rule>> rulesByType = new ArrayList(3);
        for (i = TYPE_ACTIVITY; i < 3; i += TYPE_BROADCAST) {
            rulesByType.add(new ArrayList());
        }
        try {
            FileInputStream fis = new FileInputStream(rulesFile);
        } catch (FileNotFoundException e) {
            return;
        }
        try {
            int ruleType;
            Rule rule;
            List<Rule> rules;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, null);
            XmlUtils.beginDocument(parser, TAG_RULES);
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                ruleType = -1;
                String tagName = parser.getName();
                if (tagName.equals(TAG_ACTIVITY)) {
                    ruleType = TYPE_ACTIVITY;
                } else if (tagName.equals(TAG_BROADCAST)) {
                    ruleType = TYPE_BROADCAST;
                } else if (tagName.equals(TAG_SERVICE)) {
                    ruleType = TYPE_SERVICE;
                }
                if (ruleType != -1) {
                    rule = new Rule();
                    rules = (List) rulesByType.get(ruleType);
                    rule.readFromXml(parser);
                    rules.add(rule);
                }
            }
            try {
                fis.close();
            } catch (IOException ex) {
                Slog.e(TAG, "Error while closing " + rulesFile, ex);
            }
            for (ruleType = TYPE_ACTIVITY; ruleType < rulesByType.size(); ruleType += TYPE_BROADCAST) {
                rules = (List) rulesByType.get(ruleType);
                FirewallIntentResolver resolver = resolvers[ruleType];
                for (int ruleIndex = TYPE_ACTIVITY; ruleIndex < rules.size(); ruleIndex += TYPE_BROADCAST) {
                    rule = (Rule) rules.get(ruleIndex);
                    for (i = TYPE_ACTIVITY; i < rule.getIntentFilterCount(); i += TYPE_BROADCAST) {
                        resolver.addFilter(rule.getIntentFilter(i));
                    }
                    for (i = TYPE_ACTIVITY; i < rule.getComponentFilterCount(); i += TYPE_BROADCAST) {
                        resolver.addComponentFilter(rule.getComponentFilter(i), rule);
                    }
                }
            }
        } catch (XmlPullParserException ex2) {
            Slog.e(TAG, "Error reading intent firewall rules from " + rulesFile, ex2);
        } catch (IOException ex3) {
            Slog.e(TAG, "Error reading intent firewall rules from " + rulesFile, ex3);
            try {
                fis.close();
            } catch (IOException ex32) {
                Slog.e(TAG, "Error while closing " + rulesFile, ex32);
            }
        } catch (Throwable th) {
            try {
                fis.close();
            } catch (IOException ex322) {
                Slog.e(TAG, "Error while closing " + rulesFile, ex322);
            }
        }
    }

    static Filter parseFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
        String elementName = parser.getName();
        FilterFactory factory = (FilterFactory) factoryMap.get(elementName);
        if (factory != null) {
            return factory.newFilter(parser);
        }
        throw new XmlPullParserException("Unknown element in filter list: " + elementName);
    }

    boolean checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
        return this.mAms.checkComponentPermission(permission, pid, uid, owningUid, exported) == 0;
    }

    boolean signaturesMatch(int uid1, int uid2) {
        boolean z = false;
        try {
            if (AppGlobals.getPackageManager().checkUidSignatures(uid1, uid2) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException ex) {
            Slog.e(TAG, "Remote exception while checking signatures", ex);
            return false;
        }
    }
}
