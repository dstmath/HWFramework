package com.android.server.security.securityprofile;

import android.content.Context;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.security.securityprofile.PolicyEngine;
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

public class PolicyEngine {
    private static final String TAG = "SecurityProfileService";
    Map<String, Action> mActions = new HashMap();
    private PolicyDatabase mPolicyDatabase = null;
    Map<String, State> mStates = new HashMap();
    private Map<String, BBDNode> objectSubsystemOperationBBDNodes = new HashMap();
    private Map<String, Set<String>> subjectAndObjectLabels = new HashMap();
    /* access modifiers changed from: private */
    public Map<String, BBDNode> subsystemOperationBBDNodes = new HashMap();

    public interface Action {
        void execute(int i);
    }

    class ActionNode extends BBDNode {
        Action action;
        BBDNode afterNode;

        public ActionNode(Action a, BBDNode an) {
            super();
            this.action = a;
            this.afterNode = an;
        }

        /* access modifiers changed from: package-private */
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (sideEffectsAllowed) {
                this.action.execute(timeout);
            }
            return this.afterNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        public BBDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> map) {
            return new ActionNode(this.action, this.afterNode.optimize(subjectLabelBindings, objectLabelBindings, new HashMap()));
        }

        public void dump(String indent) {
            Slog.d(PolicyEngine.TAG, indent + String.valueOf(this.action.getClass()));
            BBDNode bBDNode = this.afterNode;
            bBDNode.dump(indent + " ");
        }

        public boolean equals(Object o) {
            return (o instanceof ActionNode) && ((ActionNode) o).action.equals(this.action) && ((ActionNode) o).afterNode.equals(this.afterNode);
        }
    }

    abstract class BBDNode {
        public abstract void dump(String str);

        /* access modifiers changed from: package-private */
        public abstract boolean evaluate(Set<String> set, Set<String> set2, boolean z, int i);

        /* access modifiers changed from: package-private */
        public abstract BBDNode optimize(Map<String, Boolean> map, Map<String, Boolean> map2, Map<State, Boolean> map3);

        BBDNode() {
        }

        /* access modifiers changed from: protected */
        public Map bindValue(Map map, Object key, Boolean value) {
            Map result = new HashMap(map);
            result.put(key, value);
            return result;
        }
    }

    class BooleanNode extends BBDNode {
        boolean value;

        public BooleanNode(boolean v) {
            super();
            this.value = v;
        }

        /* access modifiers changed from: package-private */
        public boolean evaluate(Set<String> set, Set<String> set2, boolean sideEffectsAllowed, int timeout) {
            return this.value;
        }

        /* access modifiers changed from: package-private */
        public BBDNode optimize(Map<String, Boolean> map, Map<String, Boolean> map2, Map<State, Boolean> map3) {
            return this;
        }

        public void dump(String indent) {
            Slog.d(PolicyEngine.TAG, indent + String.valueOf(this.value));
        }

        public boolean equals(Object o) {
            return (o instanceof BooleanNode) && ((BooleanNode) o).value == this.value;
        }
    }

    class ObjectNode extends BBDNode {
        BBDNode falseNode;
        String label;
        BBDNode trueNode;

        public ObjectNode(String l, BBDNode tn, BBDNode fn) {
            super();
            this.label = l;
            this.trueNode = tn;
            this.falseNode = fn;
        }

        /* access modifiers changed from: package-private */
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (objectLabels.contains(this.label)) {
                return this.trueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.falseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        public BBDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!objectLabelBindings.containsKey(this.label)) {
                BBDNode t = this.trueNode.optimize(subjectLabelBindings, bindValue(objectLabelBindings, this.label, true), stateBindings);
                BBDNode f = this.falseNode.optimize(subjectLabelBindings, bindValue(objectLabelBindings, this.label, false), stateBindings);
                if (t.equals(f)) {
                    return t;
                }
                return new ObjectNode(this.label, t, f);
            } else if (objectLabelBindings.get(this.label).booleanValue()) {
                return this.trueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.falseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        public void dump(String indent) {
            Slog.d(PolicyEngine.TAG, indent + String.valueOf(this.label));
            BBDNode bBDNode = this.trueNode;
            bBDNode.dump(indent + " ");
            BBDNode bBDNode2 = this.falseNode;
            bBDNode2.dump(indent + " ");
        }

        public boolean equals(Object o) {
            return (o instanceof ObjectNode) && ((ObjectNode) o).label.equals(this.label) && ((ObjectNode) o).trueNode.equals(this.trueNode) && ((ObjectNode) o).falseNode.equals(this.falseNode);
        }
    }

    interface RuleToFallthrough {
        BBDNode getFallthrough(JSONObject jSONObject) throws JSONException;
    }

    interface RuleToKey {
        String getKey(JSONObject jSONObject) throws JSONException;
    }

    public interface State {
        boolean evaluate();
    }

    class StateNode extends BBDNode {
        BBDNode falseNode;
        State state;
        BBDNode trueNode;

        public StateNode(State s, BBDNode tn, BBDNode fn) {
            super();
            this.state = s;
            this.trueNode = tn;
            this.falseNode = fn;
        }

        /* access modifiers changed from: package-private */
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (this.state.evaluate()) {
                return this.trueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.falseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        public BBDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!stateBindings.containsKey(this.state)) {
                BBDNode t = this.trueNode.optimize(subjectLabelBindings, objectLabelBindings, bindValue(stateBindings, this.state, true));
                BBDNode f = this.falseNode.optimize(subjectLabelBindings, objectLabelBindings, bindValue(stateBindings, this.state, false));
                if (t.equals(f)) {
                    return t;
                }
                return new StateNode(this.state, t, f);
            } else if (stateBindings.get(this.state).booleanValue()) {
                return this.trueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.falseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        public void dump(String indent) {
            Slog.d(PolicyEngine.TAG, indent + String.valueOf(this.state.getClass()));
            BBDNode bBDNode = this.trueNode;
            bBDNode.dump(indent + " ");
            BBDNode bBDNode2 = this.falseNode;
            bBDNode2.dump(indent + " ");
        }

        public boolean equals(Object o) {
            return (o instanceof StateNode) && ((StateNode) o).state.equals(this.state) && ((StateNode) o).trueNode.equals(this.trueNode) && ((StateNode) o).falseNode.equals(this.falseNode);
        }
    }

    class SubjectNode extends BBDNode {
        BBDNode falseNode;
        String label;
        BBDNode trueNode;

        public SubjectNode(String l, BBDNode tn, BBDNode fn) {
            super();
            this.label = l;
            this.trueNode = tn;
            this.falseNode = fn;
        }

        /* access modifiers changed from: package-private */
        public boolean evaluate(Set<String> subjectLabels, Set<String> objectLabels, boolean sideEffectsAllowed, int timeout) {
            if (subjectLabels.contains(this.label)) {
                return this.trueNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            return this.falseNode.evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        }

        /* access modifiers changed from: package-private */
        public BBDNode optimize(Map<String, Boolean> subjectLabelBindings, Map<String, Boolean> objectLabelBindings, Map<State, Boolean> stateBindings) {
            if (!subjectLabelBindings.containsKey(this.label)) {
                BBDNode t = this.trueNode.optimize(bindValue(subjectLabelBindings, this.label, true), objectLabelBindings, stateBindings);
                BBDNode f = this.falseNode.optimize(bindValue(subjectLabelBindings, this.label, false), objectLabelBindings, stateBindings);
                if (t.equals(f)) {
                    return t;
                }
                return new SubjectNode(this.label, t, f);
            } else if (subjectLabelBindings.get(this.label).booleanValue()) {
                return this.trueNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            } else {
                return this.falseNode.optimize(subjectLabelBindings, objectLabelBindings, stateBindings);
            }
        }

        public void dump(String indent) {
            Slog.d(PolicyEngine.TAG, indent + String.valueOf(this.label));
            BBDNode bBDNode = this.trueNode;
            bBDNode.dump(indent + " ");
            BBDNode bBDNode2 = this.falseNode;
            bBDNode2.dump(indent + " ");
        }

        public boolean equals(Object o) {
            return (o instanceof SubjectNode) && ((SubjectNode) o).label.equals(this.label) && ((SubjectNode) o).trueNode.equals(this.trueNode) && ((SubjectNode) o).falseNode.equals(this.falseNode);
        }
    }

    public void setPolicyRecoverFlag(boolean need) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.setPolicyRecoverFlag(need);
        }
    }

    private void addLabeltoLookup(String subjectOrObject, String label) {
        if (!this.subjectAndObjectLabels.containsKey(subjectOrObject)) {
            this.subjectAndObjectLabels.put(subjectOrObject, new HashSet());
        }
        this.subjectAndObjectLabels.get(subjectOrObject).add(label);
    }

    private void addRuleSubjectAndObjectToLabelLookup(JSONObject rule) throws JSONException {
        String subject = rule.getString("subject");
        String object = rule.getString("object");
        addLabeltoLookup(subject, subject);
        addLabeltoLookup(object, object);
    }

    private void addRulesToLookup(JSONArray rules, RuleToFallthrough ruleToFallthrough, Map<String, BBDNode> lookup, RuleToKey ruleToKey) {
        BBDNode match;
        JSONArray jSONArray = rules;
        Map<String, BBDNode> map = lookup;
        RuleToKey ruleToKey2 = ruleToKey;
        int i = rules.length() - 1;
        while (true) {
            int i2 = i;
            if (i2 >= 0) {
                try {
                    String key = ruleToKey2.getKey(jSONArray.getJSONObject(i2));
                    if (!map.containsKey(key)) {
                        BBDNode fallThrough = ruleToFallthrough.getFallthrough(jSONArray.getJSONObject(i2));
                        int j = i2;
                        while (j >= 0) {
                            JSONObject rule = jSONArray.getJSONObject(j);
                            addRuleSubjectAndObjectToLabelLookup(rule);
                            String decision = rule.getString("decision");
                            String ruleSubject = rule.getString("subject");
                            String ruleObject = rule.getString("object");
                            if (key.equals(ruleToKey2.getKey(rule))) {
                                if (decision.equals("deny")) {
                                    match = new BooleanNode(false);
                                } else {
                                    match = new BooleanNode(true);
                                }
                                if (decision.equals("allowafter") || decision.equals("allowif")) {
                                    State state = this.mStates.get(rule.getJSONObject("conditions").getString("state"));
                                    if (state == null) {
                                        Slog.e(TAG, "Missing state handler for " + rule.getJSONObject("conditions").getString("state"));
                                    } else {
                                        match = new StateNode(state, match, fallThrough);
                                    }
                                }
                                if (decision.equals("allowafter")) {
                                    Action action = this.mActions.get(rule.getJSONObject("conditions").getJSONObject(XmlConst.PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY).getString("name"));
                                    if (action == null) {
                                        Slog.e(TAG, "Missing state handler for " + rule.getJSONObject("conditions").getJSONObject(XmlConst.PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY).getString("name"));
                                    } else {
                                        match = new ActionNode(action, match);
                                    }
                                }
                                if (!ruleObject.equals("ANY")) {
                                    match = new ObjectNode(ruleObject, match, fallThrough);
                                }
                                if (!ruleSubject.equals("ANY")) {
                                    match = new SubjectNode(ruleSubject, match, fallThrough);
                                }
                                fallThrough = match;
                            }
                            j--;
                            jSONArray = rules;
                            ruleToKey2 = ruleToKey;
                            RuleToFallthrough ruleToFallthrough2 = ruleToFallthrough;
                        }
                        map.put(key, fallThrough.optimize(new HashMap(), new HashMap(), new HashMap()));
                    }
                } catch (JSONException e) {
                    Slog.e(TAG, "addRulesToLookup err:" + e.getMessage());
                }
                i = i2 - 1;
                jSONArray = rules;
                ruleToKey2 = ruleToKey;
            } else {
                return;
            }
        }
    }

    public void createLookup() {
        synchronized (this.mPolicyDatabase) {
            this.subsystemOperationBBDNodes = new HashMap();
            this.objectSubsystemOperationBBDNodes = new HashMap();
            this.subjectAndObjectLabels = new HashMap();
            JSONObject activePolicy = this.mPolicyDatabase.getPolicy();
            try {
                addRulesToLookup(activePolicy.getJSONArray("rules"), new RuleToFallthrough() {
                    public final PolicyEngine.BBDNode getFallthrough(JSONObject jSONObject) {
                        return PolicyEngine.lambda$createLookup$0(PolicyEngine.this, jSONObject);
                    }
                }, this.subsystemOperationBBDNodes, $$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI.INSTANCE);
                try {
                    Iterator<String> keys = activePolicy.getJSONObject("domains").keys();
                    while (keys.hasNext()) {
                        String packageName = keys.next();
                        try {
                            JSONArray labels = activePolicy.getJSONObject("domains").getJSONObject(packageName).getJSONArray("labels");
                            for (int i = 0; i < labels.length(); i++) {
                                addLabeltoLookup(packageName, labels.getString(i));
                            }
                            JSONArray rules = activePolicy.getJSONObject("domains").getJSONObject(packageName).optJSONArray("rules");
                            if (rules != null) {
                                addRulesToLookup(rules, new RuleToFallthrough() {
                                    public final PolicyEngine.BBDNode getFallthrough(JSONObject jSONObject) {
                                        return PolicyEngine.this.subsystemOperationBBDNodes;
                                    }
                                }, this.objectSubsystemOperationBBDNodes, $$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM.INSTANCE);
                            }
                        } catch (JSONException e) {
                            Slog.e(TAG, "packageName:" + packageName + ",err:" + e.getMessage());
                        }
                    }
                } catch (JSONException e2) {
                    Slog.e(TAG, "rules err:" + e2.getMessage());
                }
            } catch (Exception e3) {
                Slog.e(TAG, "createLookup err:" + e3.getMessage());
            }
        }
    }

    public static /* synthetic */ BBDNode lambda$createLookup$0(PolicyEngine policyEngine, JSONObject rule) throws JSONException {
        return new BooleanNode(false);
    }

    static /* synthetic */ String lambda$createLookup$1(JSONObject rule) throws JSONException {
        return rule.getString("subsystem") + rule.getString("operation");
    }

    static /* synthetic */ String lambda$createLookup$3(JSONObject rule) throws JSONException {
        return rule.getString("object") + rule.getString("subsystem") + rule.getString("operation");
    }

    public PolicyEngine(Context context) {
        this.mPolicyDatabase = new PolicyDatabase(context);
    }

    public void start() {
        createLookup();
    }

    public void addPolicy(JSONObject policy) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.addPolicy(policy);
            createLookup();
        }
    }

    public void addBlackApp(List<String> packageList) {
        if (packageList != null && this.mPolicyDatabase != null) {
            synchronized (this.mPolicyDatabase) {
                this.mPolicyDatabase.addLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    public void removeBlackApp(List<String> packageList) {
        if (packageList != null && this.mPolicyDatabase != null) {
            synchronized (this.mPolicyDatabase) {
                this.mPolicyDatabase.removeLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    public void updateBlackApp(List<String> packageList) {
        if (packageList != null && this.mPolicyDatabase != null) {
            synchronized (this.mPolicyDatabase) {
                this.mPolicyDatabase.removeLabel("Black");
                this.mPolicyDatabase.addLabel(packageList, "Black");
                createLookup();
            }
        }
    }

    public void updatePackageInformation(String packageName) {
        synchronized (this.mPolicyDatabase) {
            this.mPolicyDatabase.updatePackageInformation(packageName);
            createLookup();
        }
    }

    /* access modifiers changed from: protected */
    public void setPackageSigned(String packageName, boolean isPackageSigned) {
        this.mPolicyDatabase.setPackageSigned(packageName, isPackageSigned);
    }

    private boolean hasLabel(String packageName, String label) {
        return this.subjectAndObjectLabels.getOrDefault(packageName, new HashSet()).contains(label);
    }

    public void addState(String name, State state) {
        this.mStates.put(name, state);
    }

    public void addAction(String name, Action action) {
        this.mActions.put(name, action);
    }

    private boolean findRulesAndEvaluate(String subject, String object, String extraObjectLabel, String subsystem, String operation, boolean sideEffectsAllowed, int timeout) {
        try {
            Set<String> subjectLabels = this.subjectAndObjectLabels.getOrDefault(subject, new HashSet());
            Set<String> objectLabels = this.subjectAndObjectLabels.getOrDefault(object, new HashSet());
            if (extraObjectLabel != null) {
                objectLabels.add(extraObjectLabel);
            }
            Map<String, BBDNode> map = this.objectSubsystemOperationBBDNodes;
            if (map.containsKey(object + subsystem + operation)) {
                Map<String, BBDNode> map2 = this.objectSubsystemOperationBBDNodes;
                return map2.get(object + subsystem + operation).evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
            }
            Map<String, BBDNode> map3 = this.subsystemOperationBBDNodes;
            if (!map3.containsKey(subsystem + operation)) {
                Slog.e(TAG, subsystem + operation);
                return true;
            }
            Map<String, BBDNode> map4 = this.subsystemOperationBBDNodes;
            return map4.get(subsystem + operation).evaluate(subjectLabels, objectLabels, sideEffectsAllowed, timeout);
        } catch (Exception e) {
            Slog.e(TAG, e.getMessage());
            return true;
        }
    }

    public boolean checkAccess(String subject, String object, String subsystem, String operation) {
        return findRulesAndEvaluate(subject, object, null, subsystem, operation, false, 0);
    }

    public boolean requestAccessWithExtraLabel(String subject, String object, String extraObjectLabel, String subsystem, String operation, int timeout) {
        return findRulesAndEvaluate(subject, object, extraObjectLabel, subsystem, operation, true, timeout);
    }

    public boolean requestAccess(String subject, String object, String subsystem, String operation, int timeout) {
        return requestAccessWithExtraLabel(subject, object, null, subsystem, operation, timeout);
    }

    public List<String> getLabels(String packageName, ApkDigest apkDigest) {
        return this.mPolicyDatabase.getLabels(packageName, apkDigest);
    }

    public boolean isBlackApp(String packageName) {
        return hasLabel(packageName, "Black");
    }

    public boolean isNeedPolicyRecover() {
        return this.mPolicyDatabase.isNeedPolicyRecover();
    }

    public boolean isPackageSigned(String packageName) {
        return this.mPolicyDatabase.isPackageSigned(packageName);
    }
}
