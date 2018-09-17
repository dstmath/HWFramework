package com.android.server.firewall;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.Environment;
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
    private static final File RULES_DIR = new File(Environment.getDataSystemDirectory(), "ifw");
    static final String TAG = "IntentFirewall";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_BROADCAST = "broadcast";
    private static final String TAG_RULES = "rules";
    private static final String TAG_SERVICE = "service";
    private static final int TYPE_ACTIVITY = 0;
    private static final int TYPE_BROADCAST = 1;
    private static final int TYPE_SERVICE = 2;
    private static final HashMap<String, FilterFactory> factoryMap;
    private FirewallIntentResolver mActivityResolver = new FirewallIntentResolver();
    private final AMSInterface mAms;
    private FirewallIntentResolver mBroadcastResolver = new FirewallIntentResolver();
    final FirewallHandler mHandler;
    private final RuleObserver mObserver;
    private FirewallIntentResolver mServiceResolver = new FirewallIntentResolver();

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

        /* synthetic */ FirewallIntentResolver(FirewallIntentResolver -this0) {
            this();
        }

        private FirewallIntentResolver() {
            this.mRulesByComponent = new ArrayMap(0);
        }

        protected boolean allowFilterResult(FirewallIntentFilter filter, List<Rule> dest) {
            return dest.contains(filter.rule) ^ 1;
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

        /* synthetic */ Rule(Rule -this0) {
            this();
        }

        private Rule() {
            this.mIntentFilters = new ArrayList(1);
            this.mComponentFilters = new ArrayList(0);
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
                IntentFirewall.this.mHandler.removeMessages(0);
                IntentFirewall.this.mHandler.sendEmptyMessageDelayed(0, 250);
            }
        }
    }

    static {
        FilterFactory[] factories = new FilterFactory[]{AndFilter.FACTORY, OrFilter.FACTORY, NotFilter.FACTORY, StringFilter.ACTION, StringFilter.COMPONENT, StringFilter.COMPONENT_NAME, StringFilter.COMPONENT_PACKAGE, StringFilter.DATA, StringFilter.HOST, StringFilter.MIME_TYPE, StringFilter.SCHEME, StringFilter.PATH, StringFilter.SSP, CategoryFilter.FACTORY, SenderFilter.FACTORY, SenderPackageFilter.FACTORY, SenderPermissionFilter.FACTORY, PortFilter.FACTORY};
        factoryMap = new HashMap((factories.length * 4) / 3);
        for (FilterFactory factory : factories) {
            factoryMap.put(factory.getTagName(), factory);
        }
    }

    public IntentFirewall(AMSInterface ams, Handler handler) {
        this.mAms = ams;
        this.mHandler = new FirewallHandler(handler.getLooper());
        File rulesDir = getRulesDir();
        rulesDir.mkdirs();
        readRulesDir(rulesDir);
        this.mObserver = new RuleObserver(rulesDir);
        this.mObserver.startWatching();
    }

    public boolean checkStartActivity(Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        return checkIntent(this.mActivityResolver, intent.getComponent(), 0, intent, callerUid, callerPid, resolvedType, resolvedApp.uid);
    }

    public boolean checkService(ComponentName resolvedService, Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        return checkIntent(this.mServiceResolver, resolvedService, 2, intent, callerUid, callerPid, resolvedType, resolvedApp.uid);
    }

    public boolean checkBroadcast(Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return checkIntent(this.mBroadcastResolver, intent.getComponent(), 1, intent, callerUid, callerPid, resolvedType, receivingUid);
    }

    public boolean checkIntent(FirewallIntentResolver resolver, ComponentName resolvedComponent, int intentType, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int log = 0;
        int block = 0;
        List<Rule> candidateRules = resolver.queryIntent(intent, resolvedType, false, 0);
        if (candidateRules == null) {
            candidateRules = new ArrayList();
        }
        resolver.queryByComponent(resolvedComponent, candidateRules);
        for (int i = 0; i < candidateRules.size(); i++) {
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
        return block ^ 1;
    }

    private static void logIntent(int intentType, Intent intent, int callerUid, String resolvedType) {
        ComponentName cn = intent.getComponent();
        String shortComponent = null;
        if (cn != null) {
            shortComponent = cn.flattenToShortString();
        }
        String callerPackages = null;
        int callerPackageCount = 0;
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
        String pkg;
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String pkg2 : packages) {
            if ((sb.length() + pkg2.length()) + 1 < 150) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(pkg2);
            } else if (sb.length() >= LOG_PACKAGES_SUFFICIENT_LENGTH) {
                return sb.toString();
            }
        }
        if (sb.length() != 0 || packages.length <= 0) {
            return null;
        }
        pkg2 = packages[0];
        return pkg2.substring((pkg2.length() - 150) + 1) + '-';
    }

    public static File getRulesDir() {
        return RULES_DIR;
    }

    private void readRulesDir(File rulesDir) {
        int i;
        FirewallIntentResolver[] resolvers = new FirewallIntentResolver[3];
        for (i = 0; i < resolvers.length; i++) {
            resolvers[i] = new FirewallIntentResolver();
        }
        File[] files = rulesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".xml")) {
                    readRules(file, resolvers);
                }
            }
        }
        Slog.i(TAG, "Read new rules (A:" + resolvers[0].filterSet().size() + " B:" + resolvers[1].filterSet().size() + " S:" + resolvers[2].filterSet().size() + ")");
        synchronized (this.mAms.getAMSLock()) {
            this.mActivityResolver = resolvers[0];
            this.mBroadcastResolver = resolvers[1];
            this.mServiceResolver = resolvers[2];
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d0 A:{Splitter: B:6:0x0024, ExcHandler: java.io.IOException (r3_2 'ex' java.io.IOException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:36:0x00ab, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:37:0x00ac, code:
            android.util.Slog.e(TAG, "Error reading an intent firewall rule from " + r21, r4);
     */
    /* JADX WARNING: Missing block: B:39:0x00d0, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            android.util.Slog.e(TAG, "Error reading intent firewall rules from " + r21, r3);
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:59:0x0153, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:60:0x0154, code:
            android.util.Slog.e(TAG, "Error while closing " + r21, r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readRules(File rulesFile, FirewallIntentResolver[] resolvers) {
        int i;
        List<List<Rule>> rulesByType = new ArrayList(3);
        for (i = 0; i < 3; i++) {
            rulesByType.add(new ArrayList());
        }
        try {
            FileInputStream fis = new FileInputStream(rulesFile);
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
                        ruleType = 0;
                    } else if (tagName.equals(TAG_BROADCAST)) {
                        ruleType = 1;
                    } else if (tagName.equals(TAG_SERVICE)) {
                        ruleType = 2;
                    }
                    if (ruleType != -1) {
                        rule = new Rule(null);
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
                for (ruleType = 0; ruleType < rulesByType.size(); ruleType++) {
                    rules = (List) rulesByType.get(ruleType);
                    FirewallIntentResolver resolver = resolvers[ruleType];
                    for (int ruleIndex = 0; ruleIndex < rules.size(); ruleIndex++) {
                        rule = (Rule) rules.get(ruleIndex);
                        for (i = 0; i < rule.getIntentFilterCount(); i++) {
                            resolver.addFilter(rule.getIntentFilter(i));
                        }
                        for (i = 0; i < rule.getComponentFilterCount(); i++) {
                            resolver.addComponentFilter(rule.getComponentFilter(i), rule);
                        }
                    }
                }
            } catch (XmlPullParserException ex2) {
                Slog.e(TAG, "Error reading intent firewall rules from " + rulesFile, ex2);
                try {
                    fis.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "Error while closing " + rulesFile, ex3);
                }
            } catch (IOException ex4) {
            } catch (Throwable th) {
                try {
                    fis.close();
                } catch (IOException ex32) {
                    Slog.e(TAG, "Error while closing " + rulesFile, ex32);
                }
                throw th;
            }
        } catch (FileNotFoundException e) {
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
