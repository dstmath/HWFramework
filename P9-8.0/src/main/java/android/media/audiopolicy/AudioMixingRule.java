package android.media.audiopolicy;

import android.media.AudioAttributes;
import android.os.Parcel;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class AudioMixingRule {
    public static final int RULE_EXCLUDE_ATTRIBUTE_CAPTURE_PRESET = 32770;
    public static final int RULE_EXCLUDE_ATTRIBUTE_USAGE = 32769;
    public static final int RULE_EXCLUDE_UID = 32772;
    private static final int RULE_EXCLUSION_MASK = 32768;
    public static final int RULE_MATCH_ATTRIBUTE_CAPTURE_PRESET = 2;
    public static final int RULE_MATCH_ATTRIBUTE_USAGE = 1;
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

        void writeToParcel(Parcel dest) {
            dest.writeInt(this.mRule);
            int match_rule = this.mRule & -32769;
            switch (match_rule) {
                case 1:
                    dest.writeInt(this.mAttr.getUsage());
                    return;
                case 2:
                    dest.writeInt(this.mAttr.getCapturePreset());
                    return;
                case 4:
                    dest.writeInt(this.mIntProp);
                    return;
                default:
                    Log.e("AudioMixMatchCriterion", "Unknown match rule" + match_rule + " when writing to Parcel");
                    dest.writeInt(-1);
                    return;
            }
        }
    }

    public static class Builder {
        private ArrayList<AudioMixMatchCriterion> mCriteria = new ArrayList();
        private int mTargetMixType = -1;

        public Builder addRule(AudioAttributes attrToMatch, int rule) throws IllegalArgumentException {
            if (AudioMixingRule.isValidAttributesSystemApiRule(rule)) {
                return checkAddRuleObjInternal(rule, attrToMatch);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        public Builder excludeRule(AudioAttributes attrToMatch, int rule) throws IllegalArgumentException {
            if (AudioMixingRule.isValidAttributesSystemApiRule(rule)) {
                return checkAddRuleObjInternal(32768 | rule, attrToMatch);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

        public Builder addMixRule(int rule, Object property) throws IllegalArgumentException {
            if (AudioMixingRule.isValidSystemApiRule(rule)) {
                return checkAddRuleObjInternal(rule, property);
            }
            throw new IllegalArgumentException("Illegal rule value " + rule);
        }

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
            } else if (AudioMixingRule.isAudioAttributeRule(rule & -32769)) {
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

        /* JADX WARNING: Missing block: B:61:0x00e9, code:
            return r7;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Builder addRuleInternal(AudioAttributes attrToMatch, Integer intProp, int rule) throws IllegalArgumentException {
            if (this.mTargetMixType == -1) {
                if (AudioMixingRule.isPlayerRule(rule)) {
                    this.mTargetMixType = 0;
                } else {
                    this.mTargetMixType = 1;
                }
            } else if ((this.mTargetMixType == 0 && (AudioMixingRule.isPlayerRule(rule) ^ 1) != 0) || (this.mTargetMixType == 1 && AudioMixingRule.isPlayerRule(rule))) {
                throw new IllegalArgumentException("Incompatible rule for mix");
            }
            synchronized (this.mCriteria) {
                Iterator<AudioMixMatchCriterion> crIterator = this.mCriteria.iterator();
                int match_rule = rule & -32769;
                while (crIterator.hasNext()) {
                    AudioMixMatchCriterion criterion = (AudioMixMatchCriterion) crIterator.next();
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
                        case 4:
                            if (criterion.mIntProp != intProp.intValue()) {
                                break;
                            } else if (criterion.mRule == rule) {
                                return this;
                            } else {
                                throw new IllegalArgumentException("Contradictory rule exists for UID " + intProp);
                            }
                        default:
                            break;
                    }
                }
                switch (match_rule) {
                    case 1:
                    case 2:
                        this.mCriteria.add(new AudioMixMatchCriterion(attrToMatch, rule));
                        break;
                    case 4:
                        this.mCriteria.add(new AudioMixMatchCriterion(intProp, rule));
                        break;
                    default:
                        throw new IllegalStateException("Unreachable code in addRuleInternal()");
                }
            }
        }

        Builder addRuleFromParcel(Parcel in) throws IllegalArgumentException {
            int rule = in.readInt();
            AudioAttributes attr = null;
            Integer intProp = null;
            switch (rule & -32769) {
                case 1:
                    attr = new android.media.AudioAttributes.Builder().setUsage(in.readInt()).build();
                    break;
                case 2:
                    attr = new android.media.AudioAttributes.Builder().setInternalCapturePreset(in.readInt()).build();
                    break;
                case 4:
                    intProp = new Integer(in.readInt());
                    break;
                default:
                    in.readInt();
                    throw new IllegalArgumentException("Illegal rule value " + rule + " in parcel");
            }
            return addRuleInternal(attr, intProp, rule);
        }

        public AudioMixingRule build() {
            return new AudioMixingRule(this.mTargetMixType, this.mCriteria, null);
        }
    }

    /* synthetic */ AudioMixingRule(int mixType, ArrayList criteria, AudioMixingRule -this2) {
        this(mixType, criteria);
    }

    private AudioMixingRule(int mixType, ArrayList<AudioMixMatchCriterion> criteria) {
        this.mCriteria = criteria;
        this.mTargetMixType = mixType;
    }

    int getTargetMixType() {
        return this.mTargetMixType;
    }

    ArrayList<AudioMixMatchCriterion> getCriteria() {
        return this.mCriteria;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mTargetMixType), this.mCriteria});
    }

    private static boolean isValidSystemApiRule(int rule) {
        switch (rule) {
            case 1:
            case 2:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isValidAttributesSystemApiRule(int rule) {
        switch (rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    private static boolean isValidRule(int rule) {
        switch (rule & -32769) {
            case 1:
            case 2:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isPlayerRule(int rule) {
        switch (rule & -32769) {
            case 1:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean isAudioAttributeRule(int match_rule) {
        switch (match_rule) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }
}
