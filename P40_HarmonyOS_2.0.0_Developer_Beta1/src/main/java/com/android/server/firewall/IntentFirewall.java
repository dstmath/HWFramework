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

    static {
        FilterFactory[] factories = {AndFilter.FACTORY, OrFilter.FACTORY, NotFilter.FACTORY, StringFilter.ACTION, StringFilter.COMPONENT, StringFilter.COMPONENT_NAME, StringFilter.COMPONENT_PACKAGE, StringFilter.DATA, StringFilter.HOST, StringFilter.MIME_TYPE, StringFilter.SCHEME, StringFilter.PATH, StringFilter.SSP, CategoryFilter.FACTORY, SenderFilter.FACTORY, SenderPackageFilter.FACTORY, SenderPermissionFilter.FACTORY, PortFilter.FACTORY};
        factoryMap = new HashMap<>((factories.length * 4) / 3);
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
        List<Rule> candidateRules;
        List<Rule> candidateRules2 = resolver.queryIntent(intent, resolvedType, false, 0);
        if (candidateRules2 == null) {
            candidateRules = new ArrayList<>();
        } else {
            candidateRules = candidateRules2;
        }
        resolver.queryByComponent(resolvedComponent, candidateRules);
        boolean log = false;
        boolean block = false;
        for (int i = 0; i < candidateRules.size(); i++) {
            Rule rule = candidateRules.get(i);
            if (rule.matches(this, resolvedComponent, intent, callerUid, callerPid, resolvedType, receivingUid)) {
                block |= rule.getBlock();
                log |= rule.getLog();
                if (block && log) {
                    break;
                }
            }
        }
        if (log) {
            logIntent(intentType, intent, callerUid, resolvedType);
        }
        if (!block) {
            return true;
        }
        return false;
    }

    private static void logIntent(int intentType, Intent intent, int callerUid, String resolvedType) {
        String shortComponent;
        String callerPackages;
        int callerPackageCount;
        ComponentName cn = intent.getComponent();
        if (cn != null) {
            shortComponent = cn.flattenToShortString();
        } else {
            shortComponent = null;
        }
        String callerPackages2 = null;
        int callerPackageCount2 = 0;
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm != null) {
            try {
                String[] callerPackagesArray = pm.getPackagesForUid(callerUid);
                if (callerPackagesArray != null) {
                    callerPackageCount2 = callerPackagesArray.length;
                    callerPackages2 = joinPackages(callerPackagesArray);
                }
                callerPackages = callerPackages2;
                callerPackageCount = callerPackageCount2;
            } catch (RemoteException ex) {
                Slog.e(TAG, "Remote exception while retrieving packages", ex);
                callerPackages = null;
                callerPackageCount = 0;
            }
        } else {
            callerPackages = null;
            callerPackageCount = 0;
        }
        EventLogTags.writeIfwIntentMatched(intentType, shortComponent, callerUid, callerPackageCount, callerPackages, intent.getAction(), resolvedType, intent.getDataString(), intent.getFlags());
    }

    private static String joinPackages(String[] packages) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String pkg : packages) {
            if (sb.length() + pkg.length() + 1 < 150) {
                if (!first) {
                    sb.append(',');
                } else {
                    first = false;
                }
                sb.append(pkg);
            } else if (sb.length() >= 125) {
                return sb.toString();
            }
        }
        if (sb.length() != 0 || packages.length <= 0) {
            return null;
        }
        String pkg2 = packages[0];
        return pkg2.substring((pkg2.length() - 150) + 1) + '-';
    }

    public static File getRulesDir() {
        return RULES_DIR;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readRulesDir(File rulesDir) {
        FirewallIntentResolver[] resolvers = new FirewallIntentResolver[3];
        for (int i = 0; i < resolvers.length; i++) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        android.util.Slog.e(com.android.server.firewall.IntentFirewall.TAG, "Error reading intent firewall rules from " + r17, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0111, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0112, code lost:
        android.util.Slog.e(com.android.server.firewall.IntentFirewall.TAG, "Error while closing " + r17, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0127, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0128, code lost:
        android.util.Slog.e(com.android.server.firewall.IntentFirewall.TAG, "Error reading intent firewall rules from " + r17, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x013f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0140, code lost:
        android.util.Slog.e(com.android.server.firewall.IntentFirewall.TAG, "Error while closing " + r17, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00f9 A[ExcHandler: IOException (r0v10 'ex' java.io.IOException A[CUSTOM_DECLARE]), Splitter:B:6:0x0024] */
    private void readRules(File rulesFile, FirewallIntentResolver[] resolvers) {
        int ruleType;
        List<List<Rule>> rulesByType = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            rulesByType.add(new ArrayList<>());
        }
        try {
            FileInputStream fis = new FileInputStream(rulesFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, null);
                XmlUtils.beginDocument(parser, TAG_RULES);
                int outerDepth = parser.getDepth();
                while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                    String tagName = parser.getName();
                    if (tagName.equals(TAG_ACTIVITY)) {
                        ruleType = 0;
                    } else if (tagName.equals("broadcast")) {
                        ruleType = 1;
                    } else if (tagName.equals(TAG_SERVICE)) {
                        ruleType = 2;
                    } else {
                        ruleType = -1;
                    }
                    if (ruleType != -1) {
                        Rule rule = new Rule();
                        List<Rule> rules = rulesByType.get(ruleType);
                        rule.readFromXml(parser);
                        rules.add(rule);
                    }
                }
                try {
                    fis.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "Error while closing " + rulesFile, ex);
                }
                for (int ruleType2 = 0; ruleType2 < rulesByType.size(); ruleType2++) {
                    List<Rule> rules2 = rulesByType.get(ruleType2);
                    FirewallIntentResolver resolver = resolvers[ruleType2];
                    for (int ruleIndex = 0; ruleIndex < rules2.size(); ruleIndex++) {
                        Rule rule2 = rules2.get(ruleIndex);
                        for (int i2 = 0; i2 < rule2.getIntentFilterCount(); i2++) {
                            resolver.addFilter(rule2.getIntentFilter(i2));
                        }
                        for (int i3 = 0; i3 < rule2.getComponentFilterCount(); i3++) {
                            resolver.addComponentFilter(rule2.getComponentFilter(i3), rule2);
                        }
                    }
                }
            } catch (XmlPullParserException ex2) {
                Slog.e(TAG, "Error reading an intent firewall rule from " + rulesFile, ex2);
            } catch (IOException ex3) {
            } catch (Throwable ex4) {
                try {
                    fis.close();
                } catch (IOException ex5) {
                    Slog.e(TAG, "Error while closing " + rulesFile, ex5);
                }
                throw ex4;
            }
        } catch (FileNotFoundException e) {
        }
    }

    static Filter parseFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
        String elementName = parser.getName();
        FilterFactory factory = factoryMap.get(elementName);
        if (factory != null) {
            return factory.newFilter(parser);
        }
        throw new XmlPullParserException("Unknown element in filter list: " + elementName);
    }

    /* access modifiers changed from: private */
    public static class Rule extends AndFilter {
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
            this.mIntentFilters = new ArrayList<>(1);
            this.mComponentFilters = new ArrayList<>(0);
        }

        @Override // com.android.server.firewall.FilterList
        public Rule readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            this.block = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_BLOCK));
            this.log = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_LOG));
            super.readFromXml(parser);
            return this;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.firewall.FilterList
        public void readChild(XmlPullParser parser) throws IOException, XmlPullParserException {
            String currentTag = parser.getName();
            if (currentTag.equals(TAG_INTENT_FILTER)) {
                FirewallIntentFilter intentFilter = new FirewallIntentFilter(this);
                intentFilter.readFromXml(parser);
                this.mIntentFilters.add(intentFilter);
            } else if (currentTag.equals(TAG_COMPONENT_FILTER)) {
                String componentStr = parser.getAttributeValue(null, "name");
                if (componentStr != null) {
                    ComponentName componentName = ComponentName.unflattenFromString(componentStr);
                    if (componentName != null) {
                        this.mComponentFilters.add(componentName);
                        return;
                    }
                    throw new XmlPullParserException("Invalid component name: " + componentStr);
                }
                throw new XmlPullParserException("Component name must be specified.", parser, null);
            } else {
                super.readChild(parser);
            }
        }

        public int getIntentFilterCount() {
            return this.mIntentFilters.size();
        }

        public FirewallIntentFilter getIntentFilter(int index) {
            return this.mIntentFilters.get(index);
        }

        public int getComponentFilterCount() {
            return this.mComponentFilters.size();
        }

        public ComponentName getComponentFilter(int index) {
            return this.mComponentFilters.get(index);
        }

        public boolean getBlock() {
            return this.block;
        }

        public boolean getLog() {
            return this.log;
        }
    }

    /* access modifiers changed from: private */
    public static class FirewallIntentFilter extends IntentFilter {
        private final Rule rule;

        public FirewallIntentFilter(Rule rule2) {
            this.rule = rule2;
        }
    }

    /* access modifiers changed from: private */
    public static class FirewallIntentResolver extends IntentResolver<FirewallIntentFilter, Rule> {
        private final ArrayMap<ComponentName, Rule[]> mRulesByComponent;

        private FirewallIntentResolver() {
            this.mRulesByComponent = new ArrayMap<>(0);
        }

        /* access modifiers changed from: protected */
        public boolean allowFilterResult(FirewallIntentFilter filter, List<Rule> dest) {
            return !dest.contains(filter.rule);
        }

        /* access modifiers changed from: protected */
        public boolean isPackageForFilter(String packageName, FirewallIntentFilter filter) {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public FirewallIntentFilter[] newArray(int size) {
            return new FirewallIntentFilter[size];
        }

        /* access modifiers changed from: protected */
        public Rule newResult(FirewallIntentFilter filter, int match, int userId) {
            return filter.rule;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void sortResults(List<Rule> list) {
        }

        public void queryByComponent(ComponentName componentName, List<Rule> candidateRules) {
            Rule[] rules = this.mRulesByComponent.get(componentName);
            if (rules != null) {
                candidateRules.addAll(Arrays.asList(rules));
            }
        }

        public void addComponentFilter(ComponentName componentName, Rule rule) {
            this.mRulesByComponent.put(componentName, (Rule[]) ArrayUtils.appendElement(Rule.class, this.mRulesByComponent.get(componentName), rule));
        }
    }

    private final class FirewallHandler extends Handler {
        public FirewallHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            IntentFirewall.this.readRulesDir(IntentFirewall.getRulesDir());
        }
    }

    private class RuleObserver extends FileObserver {
        private static final int MONITORED_EVENTS = 968;

        public RuleObserver(File monitoredDir) {
            super(monitoredDir.getAbsolutePath(), (int) MONITORED_EVENTS);
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            if (path.endsWith(".xml")) {
                IntentFirewall.this.mHandler.removeMessages(0);
                IntentFirewall.this.mHandler.sendEmptyMessageDelayed(0, 250);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
        return this.mAms.checkComponentPermission(permission, pid, uid, owningUid, exported) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean signaturesMatch(int uid1, int uid2) {
        try {
            if (AppGlobals.getPackageManager().checkUidSignatures(uid1, uid2) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException ex) {
            Slog.e(TAG, "Remote exception while checking signatures", ex);
            return false;
        }
    }
}
