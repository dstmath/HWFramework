package android.telephony.ims;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telecom.VideoProfile;
import android.telephony.emergency.EmergencyNumber;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@SystemApi
public final class ImsCallProfile implements Parcelable {
    public static final int CALL_RESTRICT_CAUSE_DISABLED = 2;
    public static final int CALL_RESTRICT_CAUSE_HD = 3;
    public static final int CALL_RESTRICT_CAUSE_NONE = 0;
    public static final int CALL_RESTRICT_CAUSE_RAT = 1;
    public static final int CALL_TYPE_VIDEO_N_VOICE = 3;
    public static final int CALL_TYPE_VOICE = 2;
    public static final int CALL_TYPE_VOICE_N_VIDEO = 1;
    public static final int CALL_TYPE_VS = 8;
    public static final int CALL_TYPE_VS_RX = 10;
    public static final int CALL_TYPE_VS_TX = 9;
    public static final int CALL_TYPE_VT = 4;
    public static final int CALL_TYPE_VT_NODIR = 7;
    public static final int CALL_TYPE_VT_RX = 6;
    public static final int CALL_TYPE_VT_TX = 5;
    public static final Parcelable.Creator<ImsCallProfile> CREATOR = new Parcelable.Creator<ImsCallProfile>() {
        /* class android.telephony.ims.ImsCallProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsCallProfile createFromParcel(Parcel in) {
            return new ImsCallProfile(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsCallProfile[] newArray(int size) {
            return new ImsCallProfile[size];
        }
    };
    public static final int DIALSTRING_NORMAL = 0;
    public static final int DIALSTRING_SS_CONF = 1;
    public static final int DIALSTRING_USSD = 2;
    public static final String EXTRA_ADDITIONAL_CALL_INFO = "AdditionalCallInfo";
    public static final String EXTRA_ADDITIONAL_SIP_INVITE_FIELDS = "android.telephony.ims.extra.ADDITIONAL_SIP_INVITE_FIELDS";
    public static final String EXTRA_CALL_MODE_CHANGEABLE = "call_mode_changeable";
    public static final String EXTRA_CALL_RAT_TYPE = "CallRadioTech";
    public static final String EXTRA_CALL_RAT_TYPE_ALT = "callRadioTech";
    public static final String EXTRA_CHILD_NUMBER = "ChildNum";
    public static final String EXTRA_CNA = "cna";
    public static final String EXTRA_CNAP = "cnap";
    public static final String EXTRA_CODEC = "Codec";
    public static final String EXTRA_CONFERENCE = "conference";
    public static final String EXTRA_CONFERENCE_AVAIL = "conference_avail";
    public static final String EXTRA_DIALSTRING = "dialstring";
    public static final String EXTRA_DISPLAY_TEXT = "DisplayText";
    public static final String EXTRA_EMERGENCY_CALL = "e_call";
    public static final String EXTRA_IS_CALL_PULL = "CallPull";
    public static final String EXTRA_OEM_EXTRAS = "OemCallExtras";
    public static final String EXTRA_OI = "oi";
    public static final String EXTRA_OIR = "oir";
    public static final String EXTRA_REDIRECT_NUMBER = "redirect_number";
    public static final String EXTRA_REDIRECT_NUMBER_PRESENTATION = "redirect_number_presentation";
    public static final String EXTRA_REMOTE_URI = "remote_uri";
    public static final String EXTRA_ROMOTE_VT_CAPABILITY = "remote_vt_capability";
    public static final String EXTRA_USSD = "ussd";
    public static final String EXTRA_VMS = "vms";
    public static final int OIR_DEFAULT = 0;
    public static final int OIR_PRESENTATION_NOT_RESTRICTED = 2;
    public static final int OIR_PRESENTATION_PAYPHONE = 4;
    public static final int OIR_PRESENTATION_RESTRICTED = 1;
    public static final int OIR_PRESENTATION_UNKNOWN = 3;
    public static final int SERVICE_TYPE_EMERGENCY = 2;
    public static final int SERVICE_TYPE_NONE = 0;
    public static final int SERVICE_TYPE_NORMAL = 1;
    private static final String TAG = "ImsCallProfile";
    @UnsupportedAppUsage
    public Bundle mCallExtras;
    @UnsupportedAppUsage
    public int mCallType;
    private int mEmergencyCallRouting;
    private boolean mEmergencyCallTesting;
    private int mEmergencyServiceCategories;
    private List<String> mEmergencyUrns;
    private boolean mHasKnownUserIntentEmergency;
    @UnsupportedAppUsage
    public ImsStreamMediaProfile mMediaProfile;
    @UnsupportedAppUsage
    public int mRestrictCause;
    public int mServiceType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallRestrictCause {
    }

    public ImsCallProfile(Parcel in) {
        this.mRestrictCause = 0;
        this.mEmergencyServiceCategories = 0;
        this.mEmergencyUrns = new ArrayList();
        this.mEmergencyCallRouting = 0;
        this.mEmergencyCallTesting = false;
        this.mHasKnownUserIntentEmergency = false;
        readFromParcel(in);
    }

    public ImsCallProfile() {
        this.mRestrictCause = 0;
        this.mEmergencyServiceCategories = 0;
        this.mEmergencyUrns = new ArrayList();
        this.mEmergencyCallRouting = 0;
        this.mEmergencyCallTesting = false;
        this.mHasKnownUserIntentEmergency = false;
        this.mServiceType = 1;
        this.mCallType = 1;
        this.mCallExtras = new Bundle();
        this.mMediaProfile = new ImsStreamMediaProfile();
    }

    public ImsCallProfile(int serviceType, int callType) {
        this.mRestrictCause = 0;
        this.mEmergencyServiceCategories = 0;
        this.mEmergencyUrns = new ArrayList();
        this.mEmergencyCallRouting = 0;
        this.mEmergencyCallTesting = false;
        this.mHasKnownUserIntentEmergency = false;
        this.mServiceType = serviceType;
        this.mCallType = callType;
        this.mCallExtras = new Bundle();
        this.mMediaProfile = new ImsStreamMediaProfile();
    }

    public ImsCallProfile(int serviceType, int callType, Bundle callExtras, ImsStreamMediaProfile mediaProfile) {
        this.mRestrictCause = 0;
        this.mEmergencyServiceCategories = 0;
        this.mEmergencyUrns = new ArrayList();
        this.mEmergencyCallRouting = 0;
        this.mEmergencyCallTesting = false;
        this.mHasKnownUserIntentEmergency = false;
        this.mServiceType = serviceType;
        this.mCallType = callType;
        this.mCallExtras = callExtras;
        this.mMediaProfile = mediaProfile;
    }

    public String getCallExtra(String name) {
        return getCallExtra(name, "");
    }

    public String getCallExtra(String name, String defaultValue) {
        Bundle bundle = this.mCallExtras;
        if (bundle == null) {
            return defaultValue;
        }
        return bundle.getString(name, defaultValue);
    }

    public boolean getCallExtraBoolean(String name) {
        return getCallExtraBoolean(name, false);
    }

    public boolean getCallExtraBoolean(String name, boolean defaultValue) {
        Bundle bundle = this.mCallExtras;
        if (bundle == null) {
            return defaultValue;
        }
        return bundle.getBoolean(name, defaultValue);
    }

    public int getCallExtraInt(String name) {
        return getCallExtraInt(name, -1);
    }

    public int getCallExtraInt(String name, int defaultValue) {
        Bundle bundle = this.mCallExtras;
        if (bundle == null) {
            return defaultValue;
        }
        return bundle.getInt(name, defaultValue);
    }

    public void setCallExtra(String name, String value) {
        Bundle bundle = this.mCallExtras;
        if (bundle != null) {
            bundle.putString(name, value);
        }
    }

    public void setCallExtraBoolean(String name, boolean value) {
        Bundle bundle = this.mCallExtras;
        if (bundle != null) {
            bundle.putBoolean(name, value);
        }
    }

    public void setCallExtraInt(String name, int value) {
        Bundle bundle = this.mCallExtras;
        if (bundle != null) {
            bundle.putInt(name, value);
        }
    }

    public void setCallRestrictCause(int cause) {
        this.mRestrictCause = cause;
    }

    public void updateCallType(ImsCallProfile profile) {
        this.mCallType = profile.mCallType;
    }

    public void updateCallExtras(ImsCallProfile profile) {
        this.mCallExtras.clear();
        this.mCallExtras = (Bundle) profile.mCallExtras.clone();
    }

    public void updateMediaProfile(ImsCallProfile profile) {
        this.mMediaProfile = profile.mMediaProfile;
    }

    public String toString() {
        return "{ serviceType=" + this.mServiceType + ", callType=" + this.mCallType + ", restrictCause=" + this.mRestrictCause + ", mediaProfile=" + this.mMediaProfile.toString() + ", emergencyServiceCategories=" + this.mEmergencyServiceCategories + ", emergencyUrns=" + this.mEmergencyUrns + ", emergencyCallRouting=" + this.mEmergencyCallRouting + ", emergencyCallTesting=" + this.mEmergencyCallTesting + ", hasKnownUserIntentEmergency=" + this.mHasKnownUserIntentEmergency + " }";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        Bundle filteredExtras = maybeCleanseExtras(this.mCallExtras);
        out.writeInt(this.mServiceType);
        out.writeInt(this.mCallType);
        out.writeBundle(filteredExtras);
        out.writeParcelable(this.mMediaProfile, 0);
        out.writeInt(this.mEmergencyServiceCategories);
        out.writeStringList(this.mEmergencyUrns);
        out.writeInt(this.mEmergencyCallRouting);
        out.writeBoolean(this.mEmergencyCallTesting);
        out.writeBoolean(this.mHasKnownUserIntentEmergency);
    }

    private void readFromParcel(Parcel in) {
        this.mServiceType = in.readInt();
        this.mCallType = in.readInt();
        this.mCallExtras = in.readBundle();
        this.mMediaProfile = (ImsStreamMediaProfile) in.readParcelable(ImsStreamMediaProfile.class.getClassLoader());
        this.mEmergencyServiceCategories = in.readInt();
        this.mEmergencyUrns = in.createStringArrayList();
        this.mEmergencyCallRouting = in.readInt();
        this.mEmergencyCallTesting = in.readBoolean();
        this.mHasKnownUserIntentEmergency = in.readBoolean();
    }

    public int getServiceType() {
        return this.mServiceType;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public int getRestrictCause() {
        return this.mRestrictCause;
    }

    public Bundle getCallExtras() {
        return this.mCallExtras;
    }

    public ImsStreamMediaProfile getMediaProfile() {
        return this.mMediaProfile;
    }

    public static int getVideoStateFromImsCallProfile(ImsCallProfile callProfile) {
        int videostate = getVideoStateFromCallType(callProfile.mCallType);
        if (!callProfile.isVideoPaused() || VideoProfile.isAudioOnly(videostate)) {
            return videostate & -5;
        }
        return videostate | 4;
    }

    public static int getVideoStateFromCallType(int callType) {
        if (callType == 2) {
            return 0;
        }
        if (callType == 4) {
            return 3;
        }
        if (callType == 5) {
            return 1;
        }
        if (callType != 6) {
            return 0;
        }
        return 2;
    }

    public static int getCallTypeFromVideoState(int videoState) {
        boolean videoTx = isVideoStateSet(videoState, 1);
        boolean videoRx = isVideoStateSet(videoState, 2);
        if (isVideoStateSet(videoState, 4)) {
            return 7;
        }
        if (videoTx && !videoRx) {
            return 5;
        }
        if (!videoTx && videoRx) {
            return 6;
        }
        if (!videoTx || !videoRx) {
            return 2;
        }
        return 4;
    }

    @UnsupportedAppUsage
    public static int presentationToOIR(int presentation) {
        if (presentation == 1) {
            return 2;
        }
        if (presentation == 2) {
            return 1;
        }
        if (presentation == 3) {
            return 3;
        }
        if (presentation != 4) {
            return 0;
        }
        return 4;
    }

    public static int presentationToOir(int presentation) {
        return presentationToOIR(presentation);
    }

    public static int OIRToPresentation(int oir) {
        if (oir == 1) {
            return 2;
        }
        if (oir != 2) {
            return (oir == 3 || oir != 4) ? 3 : 4;
        }
        return 1;
    }

    public boolean isVideoPaused() {
        return this.mMediaProfile.mVideoDirection == 0;
    }

    public boolean isVideoCall() {
        return VideoProfile.isVideo(getVideoStateFromCallType(this.mCallType));
    }

    private Bundle maybeCleanseExtras(Bundle extras) {
        if (extras == null) {
            return null;
        }
        int startSize = extras.size();
        Bundle filtered = extras.filterValues();
        int endSize = filtered.size();
        if (startSize != endSize) {
            Log.i(TAG, "maybeCleanseExtras: " + (startSize - endSize) + " extra values were removed - only primitive types and system parcelables are permitted.");
        }
        return filtered;
    }

    private static boolean isVideoStateSet(int videoState, int videoStateToCheck) {
        return (videoState & videoStateToCheck) == videoStateToCheck;
    }

    public void setEmergencyCallInfo(EmergencyNumber num, boolean hasKnownUserIntentEmergency) {
        setEmergencyServiceCategories(num.getEmergencyServiceCategoryBitmaskInternalDial());
        setEmergencyUrns(num.getEmergencyUrns());
        setEmergencyCallRouting(num.getEmergencyCallRouting());
        setEmergencyCallTesting(num.getEmergencyNumberSourceBitmask() == 32);
        setHasKnownUserIntentEmergency(hasKnownUserIntentEmergency);
    }

    @VisibleForTesting
    public void setEmergencyServiceCategories(int emergencyServiceCategories) {
        this.mEmergencyServiceCategories = emergencyServiceCategories;
    }

    @VisibleForTesting
    public void setEmergencyUrns(List<String> emergencyUrns) {
        this.mEmergencyUrns = emergencyUrns;
    }

    @VisibleForTesting
    public void setEmergencyCallRouting(int emergencyCallRouting) {
        this.mEmergencyCallRouting = emergencyCallRouting;
    }

    @VisibleForTesting
    public void setEmergencyCallTesting(boolean isTesting) {
        this.mEmergencyCallTesting = isTesting;
    }

    @VisibleForTesting
    public void setHasKnownUserIntentEmergency(boolean hasKnownUserIntentEmergency) {
        this.mHasKnownUserIntentEmergency = hasKnownUserIntentEmergency;
    }

    public int getEmergencyServiceCategories() {
        return this.mEmergencyServiceCategories;
    }

    public List<String> getEmergencyUrns() {
        return this.mEmergencyUrns;
    }

    public int getEmergencyCallRouting() {
        return this.mEmergencyCallRouting;
    }

    public boolean isEmergencyCallTesting() {
        return this.mEmergencyCallTesting;
    }

    public boolean hasKnownUserIntentEmergency() {
        return this.mHasKnownUserIntentEmergency;
    }
}
