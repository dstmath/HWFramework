package android.media.audiopolicy;

import android.annotation.SystemApi;
import android.media.AudioAttributes;
import android.os.Parcel;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

@SystemApi
public class AudioMixingRule {
    public static final int RULE_EXCLUDE_ATTRIBUTE_CAPTURE_PRESET = 32770;
    public static final int RULE_EXCLUDE_ATTRIBUTE_USAGE = 32769;
    public static final int RULE_EXCLUDE_UID = 32772;
    private static final int RULE_EXCLUSION_MASK = 32768;
    @SystemApi
    public static final int RULE_MATCH_ATTRIBUTE_CAPTURE_PRESET = 2;
    @SystemApi
    public static final int RULE_MATCH_ATTRIBUTE_USAGE = 1;
    @SystemApi
    public static final int RULE_MATCH_UID = 4;
    private final ArrayList<AudioMixMatchCriterion> mCriteria;
    private final int mTargetMixType;

    static final class AudioMixMatchCriterion {
        final AudioAttributes mAttr;
        final int mIntProp;
        final int mRule;

        AudioMixMatchCriterion(AudioAttributes attributes, int rule) {
            this.mAttr = attributes;
            this.mIntProp = Integer.MIN_VALUE;
            this.mRule = rule;
        }

        AudioMixMatchCriterion(Integer intProp, int rule) {
            this.mAttr = null;
            this.mIntProp = intProp.intValue();
            this.mRule = rule;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mAttr, Integer.valueOf(this.mIntProp), Integer.valueOf(this.mRule)});
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel dest) {
            dest.writeInt(this.mRule);
            int match_rule = this.mRule & -32769;
            if (match_rule != 4) {
                switch (match_rule) {
                    case 1:
                        dest.writeInt(this.mAttr.getUsage());
                        return;
                    case 2:
                        dest.writeInt(this.mAttr.getCapturePreset());
                        return;
                    default:
                        Log.e("AudioMixMatchCriterion", "Unknown match rule" + match_rule + " when writing to Parcel");
                        dest.writeInt(-1);
                        return;
                }
            } else {
                dest.writeInt(this.mIntProp);
            }
        }
    }

    @SystemApi
    public static class Builder {
        private ArrayList<AudioMixMatchCriterion> mCriteria = new ArrayList<>();
        private int mTargetMixType = -1;

        @SystemApi
        public Builder addRule(AudioAttributes attrToMatch, int rule) throws IllegalArgumentException {
            if (AudioMixingRule.isValidAttributesSystemApiRule(rule)) {
                return checkAddRuleObjInternal(rule, attrToMatch);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        @SystemApi
        public Builder excludeRule(AudioAttributes attrToMatch, int rule) throws IllegalArgumentException {
            if (AudioMixingRule.isValidAttributesSystemApiRule(rule)) {
                return checkAddRuleObjInternal(32768 | rule, attrToMatch);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        @SystemApi
        public Builder addMixRule(int rule, Object property) throws IllegalArgumentException {
            if (AudioMixingRule.isValidSystemApiRule(rule)) {
                return checkAddRuleObjInternal(rule, property);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        @SystemApi
        public Builder excludeMixRule(int rule, Object property) throws IllegalArgumentException {
            if (AudioMixingRule.isValidSystemApiRule(rule)) {
                return checkAddRuleObjInternal(32768 | rule, property);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        private Builder checkAddRuleObjInternal(int rule, Object property) throws IllegalArgumentException {
            if (property == null) {
                throw new IllegalArgumentException("Illegal null argument for mixing rule");
            } else if (!AudioMixingRule.isValidRule(rule)) {
                throw new IllegalArgumentException("Illegal rule value " + rule);
            } else if (AudioMixingRule.isAudioAttributeRule(-32769 & rule)) {
                if (property instanceof AudioAttributes) {
                    return addRuleInternal((AudioAttributes) property, null, rule);
                }
                throw new IllegalArgumentException("Invalid AudioAttributes argument");
            } else if (property instanceof Integer) {
                return addRuleInternal(null, (Integer) property, rule);
            } else {
                throw new IllegalArgumentException("Invalid Integer argument");
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ee, code lost:
            return r7;
         */
        private Builder addRuleInternal(AudioAttributes attrToMatch, Integer intProp, int rule) throws IllegalArgumentException {
            if (this.mTargetMixType == -1) {
                if (AudioMixingRule.isPlayerRule(rule)) {
                    this.mTargetMixType = 0;
                } else {
                    this.mTargetMixType = 1;
                }
            } else if ((this.mTargetMixType == 0 && !AudioMixingRule.isPlayerRule(rule)) || (this.mTargetMixType == 1 && AudioMixingRule.isPlayerRule(rule))) {
                throw new IllegalArgumentException("Incompatible rule for mix");
            }
            synchronized (this.mCriteria) {
                Iterator<AudioMixMatchCriterion> crIterator = this.mCriteria.iterator();
                int match_rule = -32769 & rule;
                while (crIterator.hasNext()) {
                    AudioMixMatchCriterion criterion = crIterator.next();
                    if (match_rule != 4) {
                        switch (match_rule) {
                            case 1:
                                if (criterion.mAttr.getUsage() != attrToMatch.getUsage()) {
                                    break;
                                } else if (criterion.mRule == rule) {
                                    return this;
                                } else {
                                    throw new IllegalArgumentException("Contradictory rule exists for " + attrToMatch);
                                }
                            case 2:
                                if (criterion.mAttr.getCapturePreset() != attrToMatch.getCapturePreset()) {
                                    break;
                                } else if (criterion.mRule == rule) {
                                    return this;
                                } else {
                                    throw new IllegalArgumentException("Contradictory rule exists for " + attrToMatch);
                                }
                        }
                    } else if (criterion.mIntProp == intProp.intValue()) {
                        if (criterion.mRule == rule) {
                            return this;
                        }
                        throw new IllegalArgumentException("Contradictory rule exists for UID " + intProp);
                    }
                }
                if (match_rule != 4) {
                    switch (match_rule) {
                        case 1:
                        case 2:
                            this.mCriteria.add(new AudioMixMatchCriterion(attrToMatch, rule));
                            break;
                        default:
                            throw new IllegalStateException("Unreachable code in addRuleInternal()");
                    }
                } else {
                    this.mCriteria.add(new AudioMixMatchCriterion(intProp, rule));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public Builder addRuleFromParcel(Parcel in) throws IllegalArgumentException {
            int rule = in.readInt();
            int match_rule = -32769 & rule;
            AudioAttributes attr = null;
            Integer intProp = null;
            if (match_rule != 4) {
                switch (match_rule) {
                    case 1:
                        attr = new AudioAttributes.Builder().setUsage(in.readInt()).build();
                        break;
                    case 2:
                        attr = new AudioAttributes.Builder().setInternalCapturePreset(in.readInt()).build();
                        break;
                    default:
                        in.readInt();
                        throw new IllegalArgumentException("Illegal rule value " + rule + " in parcel");
                }
            } else {
                intProp = new Integer(in.readInt());
            }
            return addRuleInternal(attr, intProp, rule);
        }

        public AudioMixingRule build() {
            return new AudioMixingRule(this.mTargetMixType, this.mCriteria);
        }
    }

    private AudioMixingRule(int mixType, ArrayList<AudioMixMatchCriterion> criteria) {
        this.mCriteria = criteria;
        this.mTargetMixType = mixType;
    }

    /* access modifiers changed from: package-private */
    public boolean isAffectingUsage(int usage) {
        Iterator<AudioMixMatchCriterion> it = this.mCriteria.iterator();
        while (it.hasNext()) {
            AudioMixMatchCriterion criterion = it.next();
            if ((criterion.mRule & 1) != 0 && criterion.mAttr != null && criterion.mAttr.getUsage() == usage) {
                return true;
            }
        }
        return false;
    }

    private static boolean areCriteriaEquivalent(ArrayList<AudioMixMatchCriterion> cr1, ArrayList<AudioMixMatchCriterion> cr2) {
        boolean z = false;
        if (cr1 == null || cr2 == null) {
            return false;
        }
        if (cr1 == cr2) {
            return true;
        }
        if (cr1.size() != cr2.size()) {
            return false;
        }
        if (cr1.hashCode() == cr2.hashCode()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public int getTargetMixType() {
        return this.mTargetMixType;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<AudioMixMatchCriterion> getCriteria() {
        return this.mCriteria;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioMixingRule that = (AudioMixingRule) o;
        if (this.mTargetMixType != that.mTargetMixType || !areCriteriaEquivalent(this.mCriteria, that.mCriteria)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mTargetMixType), this.mCriteria});
    }

    /* access modifiers changed from: private */
    public static boolean isValidSystemApiRule(int rule) {
        if (rule != 4) {
            switch (rule) {
                case 1:
                case 2:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean isValidAttributesSystemApiRule(int rule) {
        switch (rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    public static boolean isValidRule(int rule) {
        int match_rule = -32769 & rule;
        if (match_rule != 4) {
            switch (match_rule) {
                case 1:
                case 2:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean isPlayerRule(int rule) {
        int match_rule = -32769 & rule;
        if (match_rule == 1 || match_rule == 4) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean isAudioAttributeRule(int match_rule) {
        switch (match_rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }
}
