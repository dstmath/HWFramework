package com.android.internal.telephony.dataconnection;

import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SubscriptionController;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DataEnabledOverride {
    private static final OverrideRule OVERRIDE_RULE_ALLOW_DATA_DURING_VOICE_CALL = new OverrideRule(8356095, 3);
    private static final OverrideRule OVERRIDE_RULE_ALWAYS_ALLOW_MMS = new OverrideRule(2, 0);
    private final Set<OverrideRule> mRules = new HashSet();

    /* access modifiers changed from: private */
    public static class OverrideRule {
        private final int mApnType;
        private final OverrideConditions mRequiredConditions;

        OverrideRule(String rule) {
            String[] tokens = rule.trim().split("\\s*=\\s*");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid data enabled override rule format: " + rule);
            } else if (!TextUtils.isEmpty(tokens[0])) {
                this.mApnType = ApnSetting.getApnTypesBitmaskFromString(tokens[0]);
                if (this.mApnType != 0) {
                    this.mRequiredConditions = new OverrideConditions(tokens[1]);
                    return;
                }
                throw new IllegalArgumentException("Invalid APN type. Rule=" + rule);
            } else {
                throw new IllegalArgumentException("APN type can't be empty");
            }
        }

        private OverrideRule(int apnType, int requiredConditions) {
            this.mApnType = apnType;
            this.mRequiredConditions = new OverrideConditions(requiredConditions);
        }

        /* access modifiers changed from: package-private */
        public boolean isSatisfiedByConditions(int apnType, int providedConditions) {
            int i = this.mApnType;
            return (i == apnType || i == 8356095) && this.mRequiredConditions.allMet(providedConditions);
        }

        public String toString() {
            return ApnSetting.getApnTypeString(this.mApnType) + "=" + this.mRequiredConditions;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            OverrideRule that = (OverrideRule) o;
            if (this.mApnType != that.mApnType || !Objects.equals(this.mRequiredConditions, that.mRequiredConditions)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.mApnType), this.mRequiredConditions);
        }
    }

    /* access modifiers changed from: package-private */
    public static class OverrideConditions {
        static final int CONDITION_IN_VOICE_CALL = 2;
        static final int CONDITION_NON_DEFAULT = 1;
        static final String CONDITION_NON_DEFAULT_STRING = "nonDefault";
        static final int CONDITION_UNCONDITIONALLY = 0;
        static final String CONDITION_UNCONDITIONALLY_STRING = "unconditionally";
        static final String CONDITION_VOICE_CALL_STRING = "inVoiceCall";
        private static final Map<Integer, String> OVERRIDE_CONDITION_INT_MAP = new ArrayMap();
        private static final Map<String, Integer> OVERRIDE_CONDITION_STRING_MAP = new ArrayMap();
        private final int mConditions;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Condition {
        }

        static {
            OVERRIDE_CONDITION_INT_MAP.put(1, CONDITION_NON_DEFAULT_STRING);
            OVERRIDE_CONDITION_INT_MAP.put(2, CONDITION_VOICE_CALL_STRING);
            OVERRIDE_CONDITION_STRING_MAP.put(CONDITION_UNCONDITIONALLY_STRING, 0);
            OVERRIDE_CONDITION_STRING_MAP.put(CONDITION_NON_DEFAULT_STRING, 1);
            OVERRIDE_CONDITION_STRING_MAP.put(CONDITION_VOICE_CALL_STRING, 2);
        }

        OverrideConditions(String conditions) {
            this.mConditions = getBitmaskFromString(conditions);
        }

        OverrideConditions(int conditions) {
            this.mConditions = conditions;
        }

        private static String getStringFromBitmask(int conditions) {
            if (conditions == 0) {
                return CONDITION_UNCONDITIONALLY_STRING;
            }
            List<String> conditionsStrings = new ArrayList<>();
            for (Integer condition : OVERRIDE_CONDITION_INT_MAP.keySet()) {
                if ((condition.intValue() & conditions) == condition.intValue()) {
                    conditionsStrings.add(OVERRIDE_CONDITION_INT_MAP.get(condition));
                }
            }
            return TextUtils.join("&", conditionsStrings);
        }

        private static int getBitmaskFromString(String str) {
            if (!TextUtils.isEmpty(str)) {
                String[] conditionStrings = str.trim().split("\\s*&\\s*");
                int bitmask = 0;
                for (String conditionStr : conditionStrings) {
                    if (!TextUtils.isEmpty(conditionStr)) {
                        if (OVERRIDE_CONDITION_STRING_MAP.containsKey(conditionStr)) {
                            bitmask |= OVERRIDE_CONDITION_STRING_MAP.get(conditionStr).intValue();
                        } else {
                            throw new IllegalArgumentException("Invalid conditions: " + str);
                        }
                    }
                }
                return bitmask;
            }
            throw new IllegalArgumentException("Empty rule string");
        }

        /* access modifiers changed from: package-private */
        public boolean allMet(int providedConditions) {
            int i = this.mConditions;
            return (providedConditions & i) == i;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (this.mConditions == ((OverrideConditions) o).mConditions) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.mConditions));
        }

        public String toString() {
            return getStringFromBitmask(this.mConditions);
        }
    }

    public DataEnabledOverride(String rules) {
        updateRules(rules);
    }

    @VisibleForTesting
    public void updateRules(String newRules) {
        this.mRules.clear();
        String[] rulesString = newRules.trim().split("\\s*,\\s*");
        for (String rule : rulesString) {
            if (!TextUtils.isEmpty(rule)) {
                this.mRules.add(new OverrideRule(rule));
            }
        }
    }

    public void setAlwaysAllowMms(boolean allow) {
        if (allow) {
            this.mRules.add(OVERRIDE_RULE_ALWAYS_ALLOW_MMS);
        } else {
            this.mRules.remove(OVERRIDE_RULE_ALWAYS_ALLOW_MMS);
        }
    }

    public void setDataAllowedInVoiceCall(boolean allow) {
        if (allow) {
            this.mRules.add(OVERRIDE_RULE_ALLOW_DATA_DURING_VOICE_CALL);
        } else {
            this.mRules.remove(OVERRIDE_RULE_ALLOW_DATA_DURING_VOICE_CALL);
        }
    }

    public boolean isDataAllowedInVoiceCall() {
        return this.mRules.contains(OVERRIDE_RULE_ALLOW_DATA_DURING_VOICE_CALL);
    }

    private boolean canSatisfyAnyRule(int apnType, int providedConditions) {
        for (OverrideRule rule : this.mRules) {
            if (rule.isSatisfiedByConditions(apnType, providedConditions)) {
                return true;
            }
        }
        return false;
    }

    private int getCurrentConditions(Phone phone) {
        int conditions = 0;
        if (phone == null) {
            return 0;
        }
        if (phone.getState() != PhoneConstants.State.IDLE) {
            conditions = 0 | 2;
        }
        if (phone.getSubId() != SubscriptionController.getInstance().getDefaultDataSubId()) {
            return conditions | 1;
        }
        return conditions;
    }

    public boolean shouldOverrideDataEnabledSettings(Phone phone, int apnType) {
        return canSatisfyAnyRule(apnType, getCurrentConditions(phone));
    }

    public String getRules() {
        List<String> ruleStrings = new ArrayList<>();
        for (OverrideRule rule : this.mRules) {
            ruleStrings.add(rule.toString());
        }
        return TextUtils.join(",", ruleStrings);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.mRules.equals(((DataEnabledOverride) o).mRules);
    }

    public int hashCode() {
        return Objects.hash(this.mRules);
    }

    public String toString() {
        return "DataEnabledOverride: [rules=\"" + getRules() + "\"]";
    }
}
