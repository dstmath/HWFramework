package android.media.audiopolicy;

import android.app.backup.FullBackup;
import android.media.AudioFormat;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioMixingRule;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class AudioPolicyConfig implements Parcelable {
    public static final Parcelable.Creator<AudioPolicyConfig> CREATOR = new Parcelable.Creator<AudioPolicyConfig>() {
        public AudioPolicyConfig createFromParcel(Parcel p) {
            return new AudioPolicyConfig(p);
        }

        public AudioPolicyConfig[] newArray(int size) {
            return new AudioPolicyConfig[size];
        }
    };
    private static final String TAG = "AudioPolicyConfig";
    protected int mDuckingPolicy;
    private int mMixCounter;
    protected final ArrayList<AudioMix> mMixes;
    private String mRegistrationId;

    protected AudioPolicyConfig(AudioPolicyConfig conf) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixCounter = 0;
        this.mMixes = conf.mMixes;
    }

    AudioPolicyConfig(ArrayList<AudioMix> mixes) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixCounter = 0;
        this.mMixes = mixes;
    }

    public void addMix(AudioMix mix) throws IllegalArgumentException {
        if (mix != null) {
            this.mMixes.add(mix);
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioMix argument");
    }

    public ArrayList<AudioMix> getMixes() {
        return this.mMixes;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mMixes});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMixes.size());
        Iterator<AudioMix> it = this.mMixes.iterator();
        while (it.hasNext()) {
            AudioMix mix = it.next();
            dest.writeInt(mix.getRouteFlags());
            dest.writeInt(mix.mCallbackFlags);
            dest.writeInt(mix.mDeviceSystemType);
            dest.writeString(mix.mDeviceAddress);
            dest.writeInt(mix.getFormat().getSampleRate());
            dest.writeInt(mix.getFormat().getEncoding());
            dest.writeInt(mix.getFormat().getChannelMask());
            ArrayList<AudioMixingRule.AudioMixMatchCriterion> criteria = mix.getRule().getCriteria();
            dest.writeInt(criteria.size());
            Iterator<AudioMixingRule.AudioMixMatchCriterion> it2 = criteria.iterator();
            while (it2.hasNext()) {
                it2.next().writeToParcel(dest);
            }
        }
    }

    private AudioPolicyConfig(Parcel in) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixCounter = 0;
        this.mMixes = new ArrayList<>();
        int nbMixes = in.readInt();
        for (int i = 0; i < nbMixes; i++) {
            AudioMix.Builder mixBuilder = new AudioMix.Builder();
            mixBuilder.setRouteFlags(in.readInt());
            mixBuilder.setCallbackFlags(in.readInt());
            mixBuilder.setDevice(in.readInt(), in.readString());
            mixBuilder.setFormat(new AudioFormat.Builder().setSampleRate(in.readInt()).setChannelMask(in.readInt()).setEncoding(in.readInt()).build());
            int nbRules = in.readInt();
            AudioMixingRule.Builder ruleBuilder = new AudioMixingRule.Builder();
            for (int j = 0; j < nbRules; j++) {
                ruleBuilder.addRuleFromParcel(in);
            }
            mixBuilder.setMixingRule(ruleBuilder.build());
            this.mMixes.add(mixBuilder.build());
        }
    }

    public String toLogFriendlyString() {
        String textDump;
        new String("android.media.audiopolicy.AudioPolicyConfig:\n");
        String textDump2 = textDump + this.mMixes.size() + " AudioMix: " + this.mRegistrationId + "\n";
        Iterator<AudioMix> it = this.mMixes.iterator();
        while (it.hasNext()) {
            String textDump3 = textDump2 + "* route flags=0x" + Integer.toHexString(it.next().getRouteFlags()) + "\n";
            String textDump4 = textDump3 + "  rate=" + it.next().getFormat().getSampleRate() + "Hz\n";
            String textDump5 = textDump4 + "  encoding=" + it.next().getFormat().getEncoding() + "\n";
            String textDump6 = textDump5 + "  channels=0x";
            textDump2 = textDump6 + Integer.toHexString(it.next().getFormat().getChannelMask()).toUpperCase() + "\n";
            Iterator<AudioMixingRule.AudioMixMatchCriterion> it2 = it.next().getRule().getCriteria().iterator();
            while (it2.hasNext()) {
                switch (it2.next().mRule) {
                    case 1:
                        String textDump7 = textDump2 + "  match usage ";
                        textDump = textDump7 + criterion.mAttr.usageToString();
                        break;
                    case 2:
                        String textDump8 = textDump2 + "  match capture preset ";
                        textDump = textDump8 + criterion.mAttr.getCapturePreset();
                        break;
                    case 4:
                        String textDump9 = textDump2 + "  match UID ";
                        textDump = textDump9 + criterion.mIntProp;
                        break;
                    case 32769:
                        String textDump10 = textDump2 + "  exclude usage ";
                        textDump = textDump10 + criterion.mAttr.usageToString();
                        break;
                    case 32770:
                        String textDump11 = textDump2 + "  exclude capture preset ";
                        textDump = textDump11 + criterion.mAttr.getCapturePreset();
                        break;
                    case AudioMixingRule.RULE_EXCLUDE_UID:
                        String textDump12 = textDump2 + "  exclude UID ";
                        textDump = textDump12 + criterion.mIntProp;
                        break;
                    default:
                        textDump = textDump2 + "invalid rule!";
                        break;
                }
                textDump2 = textDump + "\n";
            }
        }
        return textDump2;
    }

    /* access modifiers changed from: protected */
    public void setRegistration(String regId) {
        boolean newRegNull = true;
        boolean currentRegNull = this.mRegistrationId == null || this.mRegistrationId.isEmpty();
        if (regId != null && !regId.isEmpty()) {
            newRegNull = false;
        }
        if (currentRegNull || newRegNull || this.mRegistrationId.equals(regId)) {
            this.mRegistrationId = regId == null ? "" : regId;
            Iterator<AudioMix> it = this.mMixes.iterator();
            while (it.hasNext()) {
                setMixRegistration(it.next());
            }
            return;
        }
        Log.e(TAG, "Invalid registration transition from " + this.mRegistrationId + " to " + regId);
    }

    private void setMixRegistration(AudioMix mix) {
        if (this.mRegistrationId.isEmpty()) {
            mix.setRegistration("");
        } else if ((mix.getRouteFlags() & 2) == 2) {
            mix.setRegistration(this.mRegistrationId + "mix" + mixTypeId(mix.getMixType()) + ":" + this.mMixCounter);
        } else if ((mix.getRouteFlags() & 1) == 1) {
            mix.setRegistration(mix.mDeviceAddress);
        }
        this.mMixCounter++;
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mMixes")
    public void add(ArrayList<AudioMix> mixes) {
        Iterator<AudioMix> it = mixes.iterator();
        while (it.hasNext()) {
            AudioMix mix = it.next();
            setMixRegistration(mix);
            this.mMixes.add(mix);
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mMixes")
    public void remove(ArrayList<AudioMix> mixes) {
        Iterator<AudioMix> it = mixes.iterator();
        while (it.hasNext()) {
            this.mMixes.remove(it.next());
        }
    }

    private static String mixTypeId(int type) {
        if (type == 0) {
            return TtmlUtils.TAG_P;
        }
        if (type == 1) {
            return FullBackup.ROOT_TREE_TOKEN;
        }
        return "i";
    }

    /* access modifiers changed from: protected */
    public String getRegistration() {
        return this.mRegistrationId;
    }
}
