package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import huawei.android.security.securityprofile.ApkDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class PolicyEngine {
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final int DEFAULT_ACTIONS_CAPACITY = 8;
    private static final int DEFAULT_OBJECT_SUB_SYSTEM_OPS_CAPACITY = 32;
    private static final int DEFAULT_STATES_CAPACITY = 8;
    private static final int DEFAULT_SUBJECT_AND_OBJECT_LABELS_CAPACITY = 16;
    private static final int DEFAULT_SUBJECT_OR_OBJECT_LABELS_CAPACITY = 8;
    private static final int DEFAULT_SUB_SYSTEM_OPS_CAPACITY = 8;
    private static final String TAG = "SecurityProfileService";
    private Map<String, Action> mActions = new HashMap(8);
    private volatile Map<String, BDDNode> mObjectSubsystemOperationBDDNodes = new HashMap(32);
    private PolicyDatabase mPolicyDatabase;
    private Map<String, State> mStates = new HashMap(8);
    private volatile Map<String, Set<String>> mSubjectAndObjectLabels = new HashMap(16);
    private volatile Map<String, BDDNode> mSubsystemOperationBDDNodes = new HashMap(8);

    /* access modifiers changed from: package-private */
    public interface RuleToFallthrough {
        BDDNode getFallthrough(JSONObject jSONObject) throws JSONException;
    }

    /* access modifiers changed from: package-private */
    public interface RuleToKey {
        String getKey(JSONObject jSONObject) throws JSONException;
    }

    PolicyEngine(Context context, Handler handler) {
        this.mPolicyDatabase = new PolicyDatabase(context, handler);
    }

    private void addLabelToLookup(String subjectOrObject, String label) {
        if (!this.mSubjectAndObjectLabels.containsKey(subjectOrObject)) {
            this.mSubjectAndObjectLabels.put(subjectOrObject, new HashSet(16));
        }
        this.mSubjectAndObjectLabels.get(subjectOrObject).add(label);
    }

    private void addRuleSubjectAndObjectToLabelLookup(JSONObject rule) throws JSONException {
        String subject = rule.getString("subject");
        String object = rule.getString("object");
        addLabelToLookup(subject, subject);
        addLabelToLookup(object, object);
    }

    private void addRulesToLookup(JSONArray rules, RuleToFallthrough ruleToFallthrough, Map<String, BDDNode> lookup, RuleToKey ruleToKey) {
        for (int i = rules.length() - 1; i >= 0; i--) {
            try {
                String key = ruleToKey.getKey(rules.getJSONObject(i));
                if (!lookup.containsKey(key)) {
                    BDDNode fallThrough = ruleToFallthrough.getFallthrough(rules.getJSONObject(i));
                    for (int j = i; j >= 0; j--) {
                        JSONObject rule = rules.getJSONObject(j);
                        addRuleSubjectAndObjectToLabelLookup(rule);
                        String decision = rule.getString("decision");
                        String ruleSubject = rule.getString("subject");
                        String ruleObject = rule.getString("object");
                        if (key.equals(ruleToKey.getKey(rule))) {
                            fallThrough = generateSubjectNode(generateObjectNode(generateDecisionNode(rule, decision, fallThrough), ruleObject, fallThrough), ruleSubject, fallThrough);
                        }
                    }
                    lookup.put(key, fallThrough.optimize(new HashMap(8), new HashMap(8), new HashMap(8)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "addRulesToLookup err: " + e.getMessage());
            }
        }
    }

    private BDDNode generateSubjectNode(@NonNull BDDNode match, @NonNull String ruleSubject, @NonNull BDDNode fallThrough) {
        if (!ruleSubject.equals("ANY")) {
            return new SubjectNode(ruleSubject, match, fallThrough);
        }
        return match;
    }

    private BDDNode generateObjectNode(@NonNull BDDNode match, @NonNull String ruleObject, @NonNull BDDNode fallThrough) {
        if (!ruleObject.equals("ANY")) {
            return new ObjectNode(ruleObject, match, fallThrough);
        }
        return match;
    }

    @NonNull
    private BDDNode generateDecisionNode(JSONObject rule, String decision, @NonNull BDDNode fallThrough) throws JSONException {
        BDDNode match;
        if (decision.equals("deny")) {
            match = new BooleanNode(false);
        } else {
            match = new BooleanNode(true);
        }
        if (decision.equals("allowafter") || decision.equals("allowif")) {
            State state = this.mStates.get(rule.getJSONObject("conditions").getString("state"));
            if (state == null) {
                Log.e(TAG, "Missing state handler for " + rule.getJSONObject("conditions").getString("state"));
            } else {
                match = new StateNode(state, match, fallThrough);
            }
        }
        if (!decision.equals("allowafter")) {
            return match;
        }
        Action action = this.mActions.get(rule.getJSONObject("conditions").getJSONObject("action").getString("name"));
        if (action != null) {
            return new ActionNode(action, match);
        }
        Log.e(TAG, "Missing state handler for " + rule.getJSONObject("conditions").getJSONObject("action").getString("name"));
        return match;
    }

    private void createLookup() {
        synchronized (this.mPolicyDatabase) {
            this.mSubsystemOperationBDDNodes = new HashMap(8);
            this.mObjectSubsystemOperationBDDNodes = new HashMap(32);
            this.mSubjectAndObjectLabels = new HashMap(16);
            JSONObject activePolicy = this.mPolicyDatabase.getPolicy();
            try {
                addBaseRulesToLookup(activePolicy);
                JSONObject domainsPolicy = activePolicy.getJSONObject("domains");
                Iterator<String> keys = domainsPolicy.keys();
                while (keys.hasNext()) {
                    String packageName = keys.next();
                    try {
                        JSONObject packagePolicy = domainsPolicy.getJSONObject(packageName);
                        addPackageLabelsToLookup(packageName, packagePolicy);
                        addPackageRulesToLookup(packageName, packagePolicy);
                    } catch (JSONException e) {
                        Log.e(TAG, "addPackagePolicyToLookup err: " + e.getMessage());
                    }
                }
            } catch (JSONException e2) {
                Log.e(TAG, "createLookup err: " + e2.getMessage());
            }
        }
    }

    private void addPackageRulesToLookup(String packageName, JSONObject packagePolicy) throws JSONException {
        try {
            addRulesToLookup(packagePolicy.getJSONArray("rules"), new RuleToFallthrough() {
                /* class com.huawei.server.security.securityprofile.PolicyEngine.AnonymousClass1 */

                @Override // com.huawei.server.security.securityprofile.PolicyEngine.RuleToFallthrough
                public BDDNode getFallthrough(JSONObject rule) throws JSONException {
                    Map map = PolicyEngine.this.mSubsystemOperationBDDNodes;
                    return (BDDNode) map.getOrDefault(rule.getString("subsystem") + rule.getString("operation"), new BooleanNode(false));
                }
            }, this.mObjectSubsystemOperationBDDNodes, new RuleToKey() {
                /* class com.huawei.server.security.securityprofile.PolicyEngine.AnonymousClass2 */

                @Override // com.huawei.server.security.securityprofile.PolicyEngine.RuleToKey
                public String getKey(JSONObject rule) throws JSONException {
                    return rule.getString("object") + rule.getString("subsystem") + rule.getString("operation");
                }
            });
        } catch (JSONException e) {
            throw new JSONException("packageName: " + packageName + " addPackageRulesToLookup err: " + e.getMessage());
        }
    }

    private void addPackageLabelsToLookup(String packageName, JSONObject packagePolicy) throws JSONException {
        try {
            JSONArray labels = packagePolicy.getJSONArray("labels");
            int labelsLen = labels.length();
            for (int i = 0; i < labelsLen; i++) {
                addLabelToLookup(packageName, labels.getString(i));
            }
        } catch (JSONException e) {
            throw new JSONException("packageName: " + packageName + " addPackageLabelsToLookup err: " + e.getMessage());
        }
    }

    private void addBaseRulesToLookup(JSONObject policy) throws JSONException {
        try {
            addRulesToLookup(policy.getJSONArray("rules"), new RuleToFallthrough() {
                /* class com.huawei.server.security.securityprofile.PolicyEngine.AnonymousClass3 */

                @Override // com.huawei.server.security.securityprofile.PolicyEngine.RuleToFallthrough
                public BDDNode getFallthrough(JSONObject rule) throws JSONException {
                    return new BooleanNode(false);
                }
            }, this.mSubsystemOperationBDDNodes, new RuleToKey() {
                /* class com.huawei.server.security.securityprofile.PolicyEngine.AnonymousClass4 */

                @Override // com.huawei.server.security.securityprofile.PolicyEngine.RuleToKey
                public String getKey(JSONObject rule) throws JSONException {
                    return rule.getString("subsystem") + rule.getString("operation");
                }
            });
        } catch (JSONException e) {
            throw new JSONException("addBaseRulesToLookup err: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void start() {
        createLookup();
    }

    /* access modifiers changed from: package-private */
    public void addPolicy(JSONObject policy) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.addPolicy(policy);
            createLookup();
        }
    }

    /* access modifiers changed from: package-private */
    public void addBlackApp(List<String> packageList) {
        PolicyDatabase policyDatabase;
        if (packageList != null && (policyDatabase = this.mPolicyDatabase) != null) {
            synchronized (policyDatabase) {
                this.mPolicyDatabase.addLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeBlackApp(List<String> packageList) {
        PolicyDatabase policyDatabase;
        if (packageList != null && (policyDatabase = this.mPolicyDatabase) != null) {
            synchronized (policyDatabase) {
                this.mPolicyDatabase.removeLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBlackApp(List<String> packageList) {
        PolicyDatabase policyDatabase;
        if (packageList != null && (policyDatabase = this.mPolicyDatabase) != null) {
            synchronized (policyDatabase) {
                this.mPolicyDatabase.removeLabel("Black");
                this.mPolicyDatabase.addLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePackageInformation(String packageName) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.updatePackageInformation(packageName);
            createLookup();
        }
    }

    /* access modifiers changed from: package-private */
    public void setPackageSigned(String packageName, boolean isPackageSigned) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.setPackageSigned(packageName, isPackageSigned);
        }
    }

    private boolean hasLabel(String packageName, String label) {
        return this.mSubjectAndObjectLabels.getOrDefault(packageName, new HashSet(16)).contains(label);
    }

    /* access modifiers changed from: package-private */
    public void addState(String name, State state) {
        this.mStates.put(name, state);
    }

    /* access modifiers changed from: package-private */
    public void addAction(String name, Action action) {
        this.mActions.put(name, action);
    }

    private boolean findRulesAndEvaluate(String subject, PolicyObject object, PolicyAdverbial adverbial) {
        Set<String> subjectLabels = this.mSubjectAndObjectLabels.getOrDefault(subject, new HashSet(16));
        Set<String> objectLabels = this.mSubjectAndObjectLabels.getOrDefault(object.mName, new HashSet(16));
        String extraObjectLabel = object.mLabel;
        if (extraObjectLabel != null) {
            objectLabels.add(extraObjectLabel);
        }
        String objectSubsystemOperationKey = object.mName + adverbial.mSubSystem + adverbial.mOperation;
        if (this.mObjectSubsystemOperationBDDNodes.containsKey(objectSubsystemOperationKey)) {
            return this.mObjectSubsystemOperationBDDNodes.get(objectSubsystemOperationKey).evaluate(subjectLabels, objectLabels, adverbial.mSideEffectAllowed, adverbial.mTimeout);
        }
        String subsystemOperationKey = adverbial.mSubSystem + adverbial.mOperation;
        if (this.mSubsystemOperationBDDNodes.containsKey(subsystemOperationKey)) {
            return this.mSubsystemOperationBDDNodes.get(subsystemOperationKey).evaluate(subjectLabels, objectLabels, adverbial.mSideEffectAllowed, adverbial.mTimeout);
        }
        Log.w(TAG, "findRulesAndEvaluate no key: " + subsystemOperationKey);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean requestAccessWithExtraLabel(String subject, PolicyObject object, PolicyAdverbial adverbial) {
        try {
            return findRulesAndEvaluate(subject, object, adverbial);
        } catch (Exception e) {
            Log.w(TAG, "Failed to query intent interaction access!");
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public List<String> getLabels(String packageName, ApkDigest apkDigest) {
        return this.mPolicyDatabase.getLabels(packageName, apkDigest);
    }

    /* access modifiers changed from: package-private */
    public boolean isBlackApp(String packageName) {
        return hasLabel(packageName, "Black");
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedPolicyRecover() {
        return this.mPolicyDatabase.isNeedPolicyRecover();
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageSigned(String packageName) {
        return this.mPolicyDatabase.isPackageSigned(packageName);
    }

    /* access modifiers changed from: package-private */
    public void setPolicyRecoverFlag(boolean need) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.setPolicyRecoverFlag(need);
        }
    }

    /* access modifiers changed from: package-private */
    public int getActivePermissionFlags(String packageName) {
        int activePermissionFlags;
        synchronized (this.mPolicyDatabase) {
            activePermissionFlags = this.mPolicyDatabase.getActivePermissionFlags(packageName);
        }
        return activePermissionFlags;
    }

    /* access modifiers changed from: package-private */
    public static class PolicyObject {
        String mLabel;
        String mName;

        PolicyObject(String name, String label) {
            this.mName = name;
            this.mLabel = label;
        }
    }

    /* access modifiers changed from: package-private */
    public static class PolicyAdverbial {
        String mOperation;
        boolean mSideEffectAllowed;
        String mSubSystem;
        int mTimeout;

        PolicyAdverbial(String subSystem, String operation, boolean sideEffectAllowed, int timeout) {
            this.mSubSystem = subSystem;
            this.mOperation = operation;
            this.mSideEffectAllowed = sideEffectAllowed;
            this.mTimeout = timeout;
        }

        PolicyAdverbial(String subSystem, String operation, int timeout) {
            this(subSystem, operation, true, timeout);
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class State {
        @NonNull
        final String mName;

        public abstract boolean equals(Object obj);

        /* access modifiers changed from: package-private */
        public abstract boolean evaluate();

        public abstract int hashCode();

        State(@NonNull String name) {
            this.mName = name;
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class Action {
        @NonNull
        final String mName;

        public abstract boolean equals(Object obj);

        /* access modifiers changed from: package-private */
        public abstract void execute(int i);

        public abstract int hashCode();

        Action(@NonNull String name) {
            this.mName = name;
        }
    }

    /* access modifiers changed from: private */
    public abstract class BDDNode {
        /* access modifiers changed from: package-private */
        public abstract void dump(String str);

        public abstract boolean equals(Object obj);

        /* access modifiers changed from: package-private */
        public abstract boolean evaluate(Set<String> set, Set<String> set2, boolean z, int i);

        public abstract int hashCode();

        /* access modifiers changed from: package-private */
        public abstract BDDNode optimize(Map<String, Boolean> map, Map<String, Boolean> map2, Map<State, Boolean> map3);

        private BDDNode() {
        }

        /* access modifiers changed from: protected */
        public <K> Map<K, Boolean> bindValue(Map<K, Boolean> map, K key, boolean value) {
            Map<K, Boolean> result = new HashMap<>(map);
            result.put(key, Boolean.valueOf(value));
            return result;
        }
    }

    /* access modifiers changed from: private */
    public class BooleanNode extends BDDNode {
        private final boolean mValue;

        BooleanNode(boolean value) {
            super();
            this.mValue = value;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean evaluate(Set<String> set, Set<String> set2, boolean sideEffectsAllowed, int timeout) {
            return this.mValue;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public BDDNode optimize(Map<String, Boolean> map, Map<String, Boolean> map2, Map<State, Boolean> map3) {
            return this;
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public void dump(String indent) {
            if (PolicyEngine.DEBUG) {
                Log.d(PolicyEngine.TAG, indent + String.valueOf(this.mValue));
            }
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean equals(Object obj) {
            return (obj instanceof BooleanNode) && ((BooleanNode) obj).mValue == this.mValue;
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public int hashCode() {
            return Boolean.valueOf(this.mValue).hashCode();
        }
    }

    /* access modifiers changed from: private */
    public class StateNode extends BDDNode {
        @NonNull
        final BDDNode mFalseNode;
        @NonNull
        final State mState;
        @NonNull
        final BDDNode mTrueNode;

        StateNode(@NonNull State state, @NonNull BDDNode trueNode, @NonNull BDDNode falseNode) {
            super();
            this.mState = state;
            this.mTrueNode = trueNode;
            this.mFalseNode = falseNode;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (this.mState.evaluate()) {
                return this.mTrueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.mFalseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public BDDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!stateBindings.containsKey(this.mState)) {
                BDDNode trueNode = this.mTrueNode.optimize(subjectLabelBindings, objectLabelBindings, bindValue(stateBindings, this.mState, true));
                BDDNode falseNode = this.mFalseNode.optimize(subjectLabelBindings, objectLabelBindings, bindValue(stateBindings, this.mState, false));
                if (trueNode.equals(falseNode)) {
                    return trueNode;
                }
                return new StateNode(this.mState, trueNode, falseNode);
            } else if (stateBindings.get(this.mState).booleanValue()) {
                return this.mTrueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.mFalseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public void dump(String indent) {
            if (PolicyEngine.DEBUG) {
                Log.d(PolicyEngine.TAG, indent + String.valueOf(this.mState.getClass()));
            }
            BDDNode bDDNode = this.mTrueNode;
            bDDNode.dump(indent + " ");
            BDDNode bDDNode2 = this.mFalseNode;
            bDDNode2.dump(indent + " ");
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean equals(Object obj) {
            return (obj instanceof StateNode) && ((StateNode) obj).mState.equals(this.mState) && ((StateNode) obj).mTrueNode.equals(this.mTrueNode) && ((StateNode) obj).mFalseNode.equals(this.mFalseNode);
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public int hashCode() {
            return this.mState.hashCode() + this.mTrueNode.hashCode() + this.mFalseNode.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public class SubjectNode extends BDDNode {
        @NonNull
        final BDDNode mFalseNode;
        @NonNull
        final String mLabel;
        @NonNull
        final BDDNode mTrueNode;

        SubjectNode(@NonNull String label, @NonNull BDDNode trueNode, @NonNull BDDNode falseNode) {
            super();
            this.mLabel = label;
            this.mTrueNode = trueNode;
            this.mFalseNode = falseNode;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (subjectLabels.contains(this.mLabel)) {
                return this.mTrueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.mFalseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public BDDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!subjectLabelBindings.containsKey(this.mLabel)) {
                BDDNode trueNode = this.mTrueNode.optimize(bindValue(subjectLabelBindings, this.mLabel, true), objectLabelBindings, stateBindings);
                BDDNode falseNode = this.mFalseNode.optimize(bindValue(subjectLabelBindings, this.mLabel, false), objectLabelBindings, stateBindings);
                if (trueNode.equals(falseNode)) {
                    return trueNode;
                }
                return new SubjectNode(this.mLabel, trueNode, falseNode);
            } else if (subjectLabelBindings.get(this.mLabel).booleanValue()) {
                return this.mTrueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.mFalseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public void dump(String indent) {
            if (PolicyEngine.DEBUG) {
                Log.d(PolicyEngine.TAG, indent + String.valueOf(this.mLabel));
            }
            BDDNode bDDNode = this.mTrueNode;
            bDDNode.dump(indent + " ");
            BDDNode bDDNode2 = this.mFalseNode;
            bDDNode2.dump(indent + " ");
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean equals(Object obj) {
            return (obj instanceof SubjectNode) && ((SubjectNode) obj).mLabel.equals(this.mLabel) && ((SubjectNode) obj).mTrueNode.equals(this.mTrueNode) && ((SubjectNode) obj).mFalseNode.equals(this.mFalseNode);
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public int hashCode() {
            return this.mLabel.hashCode() + this.mTrueNode.hashCode() + this.mFalseNode.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public class ObjectNode extends BDDNode {
        @NonNull
        final BDDNode mFalseNode;
        @NonNull
        final String mLabel;
        @NonNull
        final BDDNode mTrueNode;

        ObjectNode(@NonNull String label, @NonNull BDDNode trueNode, @NonNull BDDNode falseNode) {
            super();
            this.mLabel = label;
            this.mTrueNode = trueNode;
            this.mFalseNode = falseNode;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (objectLabels.contains(this.mLabel)) {
                return this.mTrueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.mFalseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public BDDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!objectLabelBindings.containsKey(this.mLabel)) {
                BDDNode trueNode = this.mTrueNode.optimize(subjectLabelBindings, bindValue(objectLabelBindings, this.mLabel, true), stateBindings);
                BDDNode falseNode = this.mFalseNode.optimize(subjectLabelBindings, bindValue(objectLabelBindings, this.mLabel, false), stateBindings);
                if (trueNode.equals(falseNode)) {
                    return trueNode;
                }
                return new ObjectNode(this.mLabel, trueNode, falseNode);
            } else if (objectLabelBindings.get(this.mLabel).booleanValue()) {
                return this.mTrueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.mFalseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public void dump(String indent) {
            if (PolicyEngine.DEBUG) {
                Log.d(PolicyEngine.TAG, indent + String.valueOf(this.mLabel));
            }
            BDDNode bDDNode = this.mTrueNode;
            bDDNode.dump(indent + " ");
            BDDNode bDDNode2 = this.mFalseNode;
            bDDNode2.dump(indent + " ");
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean equals(Object obj) {
            return (obj instanceof ObjectNode) && ((ObjectNode) obj).mLabel.equals(this.mLabel) && ((ObjectNode) obj).mTrueNode.equals(this.mTrueNode) && ((ObjectNode) obj).mFalseNode.equals(this.mFalseNode);
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public int hashCode() {
            return this.mLabel.hashCode() + this.mTrueNode.hashCode() + this.mFalseNode.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public class ActionNode extends BDDNode {
        @NonNull
        final Action mAction;
        @NonNull
        final BDDNode mAfterNode;

        ActionNode(@NonNull Action action, @NonNull BDDNode afterNode) {
            super();
            this.mAction = action;
            this.mAfterNode = afterNode;
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (sideEffectsAllowed) {
                this.mAction.execute(timeout);
            }
            return this.mAfterNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public BDDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> map) {
            return new ActionNode(this.mAction, this.mAfterNode.optimize(subjectLabelBindings, objectLabelBindings, new HashMap(8)));
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public void dump(String indent) {
            if (PolicyEngine.DEBUG) {
                Log.d(PolicyEngine.TAG, indent + String.valueOf(this.mAction.getClass()));
            }
            BDDNode bDDNode = this.mAfterNode;
            bDDNode.dump(indent + " ");
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public boolean equals(Object obj) {
            return (obj instanceof ActionNode) && ((ActionNode) obj).mAction.equals(this.mAction) && ((ActionNode) obj).mAfterNode.equals(this.mAfterNode);
        }

        @Override // com.huawei.server.security.securityprofile.PolicyEngine.BDDNode
        public int hashCode() {
            return this.mAction.hashCode() + this.mAfterNode.hashCode();
        }
    }
}
