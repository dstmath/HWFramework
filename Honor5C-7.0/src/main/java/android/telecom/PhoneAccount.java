package android.telecom;

import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public final class PhoneAccount implements Parcelable {
    public static final int ACTIVE = 2;
    public static final int CAPABILITY_CALL_PROVIDER = 2;
    public static final int CAPABILITY_CALL_SUBJECT = 64;
    public static final int CAPABILITY_CONNECTION_MANAGER = 1;
    public static final int CAPABILITY_EMERGENCY_CALLS_ONLY = 128;
    public static final int CAPABILITY_EMERGENCY_VIDEO_CALLING = 512;
    public static final int CAPABILITY_MULTI_USER = 32;
    public static final int CAPABILITY_PLACE_EMERGENCY_CALLS = 16;
    public static final int CAPABILITY_SIM_SUBSCRIPTION = 4;
    public static final int CAPABILITY_VIDEO_CALLING = 8;
    public static final int CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE = 256;
    public static final Creator<PhoneAccount> CREATOR = null;
    public static final String EXTRA_CALL_SUBJECT_CHARACTER_ENCODING = "android.telecom.extra.CALL_SUBJECT_CHARACTER_ENCODING";
    public static final String EXTRA_CALL_SUBJECT_MAX_LENGTH = "android.telecom.extra.CALL_SUBJECT_MAX_LENGTH";
    public static final int LCH = 1;
    public static final int LCHMUTE = 3;
    public static final int NO_HIGHLIGHT_COLOR = 0;
    public static final int NO_ICON_TINT = 0;
    public static final int NO_RESOURCE_ID = -1;
    public static final String SCHEME_SIP = "sip";
    public static final String SCHEME_SMSTO = "smsto";
    public static final String SCHEME_TEL = "tel";
    public static final String SCHEME_VOICEMAIL = "voicemail";
    private BitSet dsda;
    private final PhoneAccountHandle mAccountHandle;
    private final Uri mAddress;
    private final int mCapabilities;
    private final Bundle mExtras;
    private final int mHighlightColor;
    private final Icon mIcon;
    private boolean mIsEnabled;
    private final CharSequence mLabel;
    private final CharSequence mShortDescription;
    private final Uri mSubscriptionAddress;
    private final List<String> mSupportedUriSchemes;

    public static class Builder {
        private PhoneAccountHandle mAccountHandle;
        private Uri mAddress;
        private int mCapabilities;
        private Bundle mExtras;
        private int mHighlightColor;
        private Icon mIcon;
        private boolean mIsEnabled;
        private CharSequence mLabel;
        private CharSequence mShortDescription;
        private Uri mSubscriptionAddress;
        private List<String> mSupportedUriSchemes;

        public Builder(PhoneAccountHandle accountHandle, CharSequence label) {
            this.mHighlightColor = PhoneAccount.NO_ICON_TINT;
            this.mSupportedUriSchemes = new ArrayList();
            this.mIsEnabled = false;
            this.mAccountHandle = accountHandle;
            this.mLabel = label;
        }

        public Builder(PhoneAccount phoneAccount) {
            this.mHighlightColor = PhoneAccount.NO_ICON_TINT;
            this.mSupportedUriSchemes = new ArrayList();
            this.mIsEnabled = false;
            this.mAccountHandle = phoneAccount.getAccountHandle();
            this.mAddress = phoneAccount.getAddress();
            this.mSubscriptionAddress = phoneAccount.getSubscriptionAddress();
            this.mCapabilities = phoneAccount.getCapabilities();
            this.mHighlightColor = phoneAccount.getHighlightColor();
            this.mLabel = phoneAccount.getLabel();
            this.mShortDescription = phoneAccount.getShortDescription();
            this.mSupportedUriSchemes.addAll(phoneAccount.getSupportedUriSchemes());
            this.mIcon = phoneAccount.getIcon();
            this.mIsEnabled = phoneAccount.isEnabled();
            this.mExtras = phoneAccount.getExtras();
        }

        public Builder setAddress(Uri value) {
            this.mAddress = value;
            return this;
        }

        public Builder setSubscriptionAddress(Uri value) {
            this.mSubscriptionAddress = value;
            return this;
        }

        public Builder setCapabilities(int value) {
            this.mCapabilities = value;
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setHighlightColor(int value) {
            this.mHighlightColor = value;
            return this;
        }

        public Builder setShortDescription(CharSequence value) {
            this.mShortDescription = value;
            return this;
        }

        public Builder addSupportedUriScheme(String uriScheme) {
            if (!(TextUtils.isEmpty(uriScheme) || this.mSupportedUriSchemes.contains(uriScheme))) {
                this.mSupportedUriSchemes.add(uriScheme);
            }
            return this;
        }

        public Builder setSupportedUriSchemes(List<String> uriSchemes) {
            this.mSupportedUriSchemes.clear();
            if (!(uriSchemes == null || uriSchemes.isEmpty())) {
                for (String uriScheme : uriSchemes) {
                    addSupportedUriScheme(uriScheme);
                }
            }
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setIsEnabled(boolean isEnabled) {
            this.mIsEnabled = isEnabled;
            return this;
        }

        public PhoneAccount build() {
            if (this.mSupportedUriSchemes.isEmpty()) {
                addSupportedUriScheme(PhoneAccount.SCHEME_TEL);
            }
            return new PhoneAccount(this.mAddress, this.mSubscriptionAddress, this.mCapabilities, this.mIcon, this.mHighlightColor, this.mLabel, this.mShortDescription, this.mSupportedUriSchemes, this.mExtras, this.mIsEnabled, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.PhoneAccount.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.PhoneAccount.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.PhoneAccount.<clinit>():void");
    }

    private PhoneAccount(PhoneAccountHandle account, Uri address, Uri subscriptionAddress, int capabilities, Icon icon, int highlightColor, CharSequence label, CharSequence shortDescription, List<String> supportedUriSchemes, Bundle extras, boolean isEnabled) {
        this.dsda = new BitSet();
        this.mAccountHandle = account;
        this.mAddress = address;
        this.mSubscriptionAddress = subscriptionAddress;
        this.mCapabilities = capabilities;
        this.mIcon = icon;
        this.mHighlightColor = highlightColor;
        this.mLabel = label;
        this.mShortDescription = shortDescription;
        this.mSupportedUriSchemes = Collections.unmodifiableList(supportedUriSchemes);
        this.mExtras = extras;
        this.mIsEnabled = isEnabled;
    }

    public static Builder builder(PhoneAccountHandle accountHandle, CharSequence label) {
        return new Builder(accountHandle, label);
    }

    public void setBit(int bit) {
        this.dsda.set(bit);
    }

    public void unSetBit(int bit) {
        this.dsda.set(bit, false);
    }

    public boolean isSet(int bit) {
        return this.dsda.get(bit);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public PhoneAccountHandle getAccountHandle() {
        return this.mAccountHandle;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public Uri getSubscriptionAddress() {
        return this.mSubscriptionAddress;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public boolean hasCapabilities(int capability) {
        return (this.mCapabilities & capability) == capability;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public CharSequence getShortDescription() {
        return this.mShortDescription;
    }

    public List<String> getSupportedUriSchemes() {
        return this.mSupportedUriSchemes;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public boolean supportsUriScheme(String uriScheme) {
        if (this.mSupportedUriSchemes == null || uriScheme == null) {
            return false;
        }
        for (String scheme : this.mSupportedUriSchemes) {
            if (scheme != null && scheme.equals(uriScheme)) {
                return true;
            }
        }
        return false;
    }

    public int getHighlightColor() {
        return this.mHighlightColor;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public int describeContents() {
        return NO_ICON_TINT;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = LCH;
        if (this.mAccountHandle == null) {
            out.writeInt(NO_ICON_TINT);
        } else {
            out.writeInt(LCH);
            this.mAccountHandle.writeToParcel(out, flags);
        }
        if (this.mAddress == null) {
            out.writeInt(NO_ICON_TINT);
        } else {
            out.writeInt(LCH);
            this.mAddress.writeToParcel(out, flags);
        }
        if (this.mSubscriptionAddress == null) {
            out.writeInt(NO_ICON_TINT);
        } else {
            out.writeInt(LCH);
            this.mSubscriptionAddress.writeToParcel(out, flags);
        }
        out.writeInt(this.mCapabilities);
        out.writeInt(this.mHighlightColor);
        out.writeCharSequence(this.mLabel);
        out.writeCharSequence(this.mShortDescription);
        out.writeStringList(this.mSupportedUriSchemes);
        if (this.mIcon == null) {
            out.writeInt(NO_ICON_TINT);
        } else {
            out.writeInt(LCH);
            this.mIcon.writeToParcel(out, flags);
        }
        if (!this.mIsEnabled) {
            i = NO_ICON_TINT;
        }
        out.writeByte((byte) i);
        out.writeBundle(this.mExtras);
    }

    private PhoneAccount(Parcel in) {
        boolean z;
        this.dsda = new BitSet();
        if (in.readInt() > 0) {
            this.mAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(in);
        } else {
            this.mAccountHandle = null;
        }
        if (in.readInt() > 0) {
            this.mAddress = (Uri) Uri.CREATOR.createFromParcel(in);
        } else {
            this.mAddress = null;
        }
        if (in.readInt() > 0) {
            this.mSubscriptionAddress = (Uri) Uri.CREATOR.createFromParcel(in);
        } else {
            this.mSubscriptionAddress = null;
        }
        this.mCapabilities = in.readInt();
        this.mHighlightColor = in.readInt();
        this.mLabel = in.readCharSequence();
        this.mShortDescription = in.readCharSequence();
        this.mSupportedUriSchemes = Collections.unmodifiableList(in.createStringArrayList());
        if (in.readInt() > 0) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
        } else {
            this.mIcon = null;
        }
        if (in.readByte() == (byte) 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsEnabled = z;
        this.mExtras = in.readBundle();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder().append("[[").append(this.mIsEnabled ? 'X' : ' ').append("] PhoneAccount: ").append(this.mAccountHandle).append(" Capabilities: ").append(capabilitiesToString(this.mCapabilities)).append(" Schemes: ");
        for (String scheme : this.mSupportedUriSchemes) {
            sb.append(scheme).append(" ");
        }
        sb.append(" Extras: ");
        sb.append(this.mExtras);
        sb.append("]");
        return sb.toString();
    }

    private String capabilitiesToString(int capabilities) {
        StringBuilder sb = new StringBuilder();
        if (hasCapabilities(CAPABILITY_VIDEO_CALLING)) {
            sb.append("Video ");
        }
        if (hasCapabilities(CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE)) {
            sb.append("Presence ");
        }
        if (hasCapabilities(CAPABILITY_CALL_PROVIDER)) {
            sb.append("CallProvider ");
        }
        if (hasCapabilities(CAPABILITY_CALL_SUBJECT)) {
            sb.append("CallSubject ");
        }
        if (hasCapabilities(LCH)) {
            sb.append("ConnectionMgr ");
        }
        if (hasCapabilities(CAPABILITY_EMERGENCY_CALLS_ONLY)) {
            sb.append("EmergOnly ");
        }
        if (hasCapabilities(CAPABILITY_MULTI_USER)) {
            sb.append("MultiUser ");
        }
        if (hasCapabilities(CAPABILITY_PLACE_EMERGENCY_CALLS)) {
            sb.append("PlaceEmerg ");
        }
        if (hasCapabilities(CAPABILITY_EMERGENCY_VIDEO_CALLING)) {
            sb.append("EmergVideo ");
        }
        if (hasCapabilities(CAPABILITY_SIM_SUBSCRIPTION)) {
            sb.append("SimSub ");
        }
        return sb.toString();
    }
}
