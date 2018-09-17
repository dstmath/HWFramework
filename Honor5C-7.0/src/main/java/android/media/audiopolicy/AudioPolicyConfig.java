package android.media.audiopolicy;

import android.app.backup.FullBackup;
import android.media.AudioFormat;
import android.media.audiopolicy.AudioMix.Builder;
import android.net.ProxyInfo;
import android.os.BatteryStats.HistoryItem;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telecom.AudioState;
import android.util.Log;
import java.util.ArrayList;
import java.util.Objects;

public class AudioPolicyConfig implements Parcelable {
    public static final Creator<AudioPolicyConfig> CREATOR = null;
    private static final String TAG = "AudioPolicyConfig";
    protected int mDuckingPolicy;
    protected ArrayList<AudioMix> mMixes;
    private String mRegistrationId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.audiopolicy.AudioPolicyConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.audiopolicy.AudioPolicyConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioPolicyConfig.<clinit>():void");
    }

    protected AudioPolicyConfig(AudioPolicyConfig conf) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixes = conf.mMixes;
    }

    AudioPolicyConfig(ArrayList<AudioMix> mixes) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixes = mixes;
    }

    public void addMix(AudioMix mix) throws IllegalArgumentException {
        if (mix == null) {
            throw new IllegalArgumentException("Illegal null AudioMix argument");
        }
        this.mMixes.add(mix);
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
        for (AudioMix mix : this.mMixes) {
            dest.writeInt(mix.getRouteFlags());
            dest.writeInt(mix.mCallbackFlags);
            dest.writeInt(mix.mDeviceSystemType);
            dest.writeString(mix.mDeviceAddress);
            dest.writeInt(mix.getFormat().getSampleRate());
            dest.writeInt(mix.getFormat().getEncoding());
            dest.writeInt(mix.getFormat().getChannelMask());
            ArrayList<AudioMixMatchCriterion> criteria = mix.getRule().getCriteria();
            dest.writeInt(criteria.size());
            for (AudioMixMatchCriterion criterion : criteria) {
                criterion.writeToParcel(dest);
            }
        }
    }

    private AudioPolicyConfig(Parcel in) {
        this.mDuckingPolicy = 0;
        this.mRegistrationId = null;
        this.mMixes = new ArrayList();
        int nbMixes = in.readInt();
        for (int i = 0; i < nbMixes; i++) {
            Builder mixBuilder = new Builder();
            mixBuilder.setRouteFlags(in.readInt());
            mixBuilder.setCallbackFlags(in.readInt());
            mixBuilder.setDevice(in.readInt(), in.readString());
            int sampleRate = in.readInt();
            mixBuilder.setFormat(new AudioFormat.Builder().setSampleRate(sampleRate).setChannelMask(in.readInt()).setEncoding(in.readInt()).build());
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
        String textDump = new String("android.media.audiopolicy.AudioPolicyConfig:\n") + this.mMixes.size() + " AudioMix: " + this.mRegistrationId + "\n";
        for (AudioMix mix : this.mMixes) {
            textDump = ((((textDump + "* route flags=0x" + Integer.toHexString(mix.getRouteFlags()) + "\n") + "  rate=" + mix.getFormat().getSampleRate() + "Hz\n") + "  encoding=" + mix.getFormat().getEncoding() + "\n") + "  channels=0x") + Integer.toHexString(mix.getFormat().getChannelMask()).toUpperCase() + "\n";
            for (AudioMixMatchCriterion criterion : mix.getRule().getCriteria()) {
                switch (criterion.mRule) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        textDump = (textDump + "  match usage ") + criterion.mAttr.usageToString();
                        break;
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                        textDump = (textDump + "  match capture preset ") + criterion.mAttr.getCapturePreset();
                        break;
                    case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        textDump = (textDump + "  match UID ") + criterion.mIntProp;
                        break;
                    case HistoryItem.EVENT_PROC_START /*32769*/:
                        textDump = (textDump + "  exclude usage ") + criterion.mAttr.usageToString();
                        break;
                    case HistoryItem.EVENT_FOREGROUND_START /*32770*/:
                        textDump = (textDump + "  exclude capture preset ") + criterion.mAttr.getCapturePreset();
                        break;
                    case HistoryItem.EVENT_SYNC_START /*32772*/:
                        textDump = (textDump + "  exclude UID ") + criterion.mIntProp;
                        break;
                    default:
                        textDump = textDump + "invalid rule!";
                        break;
                }
                textDump = textDump + "\n";
            }
        }
        return textDump;
    }

    protected void setRegistration(String regId) {
        boolean isEmpty = this.mRegistrationId != null ? this.mRegistrationId.isEmpty() : true;
        boolean isEmpty2 = regId != null ? regId.isEmpty() : true;
        if (isEmpty || isEmpty2 || this.mRegistrationId.equals(regId)) {
            if (regId == null) {
                regId = ProxyInfo.LOCAL_EXCL_LIST;
            }
            this.mRegistrationId = regId;
            int mixIndex = 0;
            for (AudioMix mix : this.mMixes) {
                if (this.mRegistrationId.isEmpty()) {
                    mix.setRegistration(ProxyInfo.LOCAL_EXCL_LIST);
                } else if ((mix.getRouteFlags() & 2) == 2) {
                    int mixIndex2 = mixIndex + 1;
                    mix.setRegistration(this.mRegistrationId + "mix" + mixTypeId(mix.getMixType()) + ":" + mixIndex);
                    mixIndex = mixIndex2;
                } else if ((mix.getRouteFlags() & 1) == 1) {
                    mix.setRegistration(mix.mDeviceAddress);
                }
            }
            return;
        }
        Log.e(TAG, "Invalid registration transition from " + this.mRegistrationId + " to " + regId);
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

    protected String getRegistration() {
        return this.mRegistrationId;
    }
}
