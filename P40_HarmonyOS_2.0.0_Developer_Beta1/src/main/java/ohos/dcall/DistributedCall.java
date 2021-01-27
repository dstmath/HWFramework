package ohos.dcall;

import java.util.List;
import java.util.Objects;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

@SystemApi
public final class DistributedCall {
    public static final int CALL_STATUS_ACTIVE = 4;
    public static final int CALL_STATUS_CONNECTING = 9;
    public static final int CALL_STATUS_DIALING = 1;
    public static final int CALL_STATUS_DISCONNECTED = 7;
    public static final int CALL_STATUS_DISCONNECTING = 10;
    public static final int CALL_STATUS_HOLDING = 3;
    public static final int CALL_STATUS_NEW = 0;
    public static final int CALL_STATUS_RINGING = 2;
    public static final int CALL_STATUS_SELECT_PHONE_ACCOUNT = 8;
    public static final Sequenceable.Producer<DistributedCall> CREATOR = new Sequenceable.Producer<DistributedCall>() {
        /* class ohos.dcall.DistributedCall.AnonymousClass1 */

        public DistributedCall createFromParcel(Parcel parcel) {
            int readInt = parcel.readInt();
            HiLog.info(DistributedCall.LABEL, "callId: %{public}d", new Object[]{Integer.valueOf(readInt)});
            int readInt2 = parcel.readInt();
            DisconnectInfo disconnectInfo = (DisconnectInfo) DisconnectInfo.CREATOR.createFromParcel(parcel);
            int readInt3 = parcel.readInt();
            long readLong = parcel.readLong();
            Uri build = new Uri.Builder().scheme("tel").decodedOpaqueSsp(parcel.readString()).build();
            int readInt4 = parcel.readInt();
            String readString = parcel.readString();
            int readInt5 = parcel.readInt();
            PacMap readPacMapFromParcel = DistributedCallUtils.readPacMapFromParcel(parcel);
            PacMap readPacMapFromParcel2 = DistributedCallUtils.readPacMapFromParcel(parcel);
            readPacMapFromParcel2.putAll(readPacMapFromParcel);
            return new DistributedCall(readInt, readInt2, readInt3, disconnectInfo, readLong, build, readInt4, readString, readInt5, readPacMapFromParcel2, parcel.readLong(), parcel.readInt(), parcel.readInt());
        }
    };
    private static final HiLogLabel LABEL = new HiLogLabel(3, 0, TAG);
    private static final String TAG = "DistributedCall";
    private final int mCallId;
    private PacMap mExtraInfo;
    private Info mInfo;
    private int mStatus;

    public static abstract class PreciseObserver {
        public void onCallCompleted(DistributedCall distributedCall) {
        }

        public void onCallEventChanged(DistributedCall distributedCall, String str, PacMap pacMap) {
        }

        public void onInfoChanged(DistributedCall distributedCall, Info info) {
        }

        public void onPostDialDtmfWait(DistributedCall distributedCall, String str) {
        }

        public void onStatusChanged(DistributedCall distributedCall, int i) {
        }
    }

    private static String statusToString(int i) {
        switch (i) {
            case 0:
                return "NEW";
            case 1:
                return "DIALING";
            case 2:
                return "RINGING";
            case 3:
                return "HOLDING";
            case 4:
                return "ACTIVE";
            case 5:
            case 6:
            default:
                return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
            case 7:
                return "DISCONNECTED";
            case 8:
                return "SELECT_PHONE_ACCOUNT";
            case 9:
                return "CONNECTING";
            case 10:
                return "DISCONNECTING";
        }
    }

    public static class Info {
        public static final int ATTR_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO = 1073741824;
        public static final int ATTR_CONFERENCE = 1;
        public static final int ATTR_DISCONNECT_FROM_CONFERENCE = 2097152;
        public static final int ATTR_ENTERPRISE_CALL = 32;
        public static final int ATTR_GENERIC_CONFERENCE = 2;
        public static final int ATTR_HIGH_DEF_AUDIO = 16;
        public static final int ATTR_HOLD = 512;
        public static final int ATTR_HOLD_NOW = 256;
        public static final int ATTR_MANAGE_CONFERENCE = 32768;
        public static final int ATTR_MERGE_CONFERENCE = 1024;
        public static final int ATTR_MUTE = 16384;
        public static final int ATTR_RESPOND_VIA_TEXT = 8192;
        public static final int ATTR_SEPARATE_FROM_CONFERENCE = 1048576;
        public static final int ATTR_SWAP_CONFERENCE = 2048;
        public static final int ATTR_VT_LOCAL_BIDIRECTIONAL = 196608;
        public static final int ATTR_VT_LOCAL_RX = 65536;
        public static final int ATTR_VT_LOCAL_TX = 131072;
        public static final int ATTR_VT_REMOTE_BIDIRECTIONAL = 786432;
        public static final int ATTR_VT_REMOTE_RX = 262144;
        public static final int ATTR_VT_REMOTE_TX = 524288;
        public static final int ATTR_WIFI = 8;
        public static final int DIRECTION_INCOMING = 0;
        public static final int DIRECTION_OUTGOING = 1;
        public static final int DIRECTION_UNKNOWN = -1;
        private final int mCallAttributes;
        private final int mCallDirection;
        private final CallHandler mCallHandler;
        private final int mCallId;
        private final long mConnectTimeMillis;
        private final long mCreationTimeMillis;
        private final DisconnectInfo mDisconnectInfo;
        private final PacMap mExtraInfo;
        private final Uri mHandle;
        private final int mHandlePresentation;
        private final String mPeerDisplayName;
        private final int mVideoStatus;

        public static boolean hasAttribute(int i, int i2) {
            return (i & i2) == i2;
        }

        public static boolean supportAttribute(int i, int i2) {
            return (i & i2) == i2;
        }

        public boolean supportAttribute(int i) {
            return supportAttribute(this.mCallAttributes, i);
        }

        public static String attributesToString(int i) {
            StringBuilder sb = new StringBuilder();
            sb.append("[Attributes:");
            if (supportAttribute(i, 256)) {
                sb.append(" ATTR_HOLD_NOW");
            }
            if (supportAttribute(i, 512)) {
                sb.append(" ATTR_HOLD");
            }
            if (supportAttribute(i, 1024)) {
                sb.append(" ATTR_MERGE_CONFERENCE");
            }
            if (supportAttribute(i, 2048)) {
                sb.append(" ATTR_SWAP_CONFERENCE");
            }
            if (supportAttribute(i, 8192)) {
                sb.append(" ATTR_RESPOND_VIA_TEXT");
            }
            if (supportAttribute(i, 16384)) {
                sb.append(" ATTR_MUTE");
            }
            if (supportAttribute(i, 32768)) {
                sb.append(" ATTR_MANAGE_CONFERENCE");
            }
            if (supportAttribute(i, 65536)) {
                sb.append(" ATTR_VT_LOCAL_RX");
            }
            if (supportAttribute(i, 131072)) {
                sb.append(" ATTR_VT_LOCAL_TX");
            }
            if (supportAttribute(i, 196608)) {
                sb.append(" ATTR_VT_LOCAL_BIDIRECTIONAL");
            }
            if (supportAttribute(i, 262144)) {
                sb.append(" ATTR_VT_REMOTE_RX");
            }
            if (supportAttribute(i, 524288)) {
                sb.append(" ATTR_VT_REMOTE_TX");
            }
            if (supportAttribute(i, 1073741824)) {
                sb.append(" ATTR_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO");
            }
            if (supportAttribute(i, 786432)) {
                sb.append(" ATTR_VT_REMOTE_BIDIRECTIONAL");
            }
            if (supportAttribute(i, 1)) {
                sb.append(" ATTR_CONFERENCE");
            }
            if (supportAttribute(i, 2)) {
                sb.append(" ATTR_GENERIC_CONFERENCE");
            }
            if (supportAttribute(i, 8)) {
                sb.append(" ATTR_WIFI");
            }
            if (supportAttribute(i, 16)) {
                sb.append(" ATTR_HIGH_DEF_AUDIO");
            }
            sb.append("]");
            return sb.toString();
        }

        public boolean hasAttribute(int i) {
            return hasAttribute(this.mCallAttributes, i);
        }

        public Uri getPeerNumber() {
            return this.mHandle;
        }

        public int getPeerNumberPresentation() {
            return this.mHandlePresentation;
        }

        public String getPeerDisplayName() {
            return this.mPeerDisplayName;
        }

        public CallHandler getCallHandler() {
            return this.mCallHandler;
        }

        public int getCallAttributes() {
            return this.mCallAttributes;
        }

        public DisconnectInfo getDisconnectInfo() {
            return this.mDisconnectInfo;
        }

        public final long getConnectTimeMillis() {
            return this.mConnectTimeMillis;
        }

        public PacMap getExtraInfo() {
            return this.mExtraInfo;
        }

        public long getCreationTimeMillis() {
            return this.mCreationTimeMillis;
        }

        public int getCallDirection() {
            return this.mCallDirection;
        }

        public int getVideoStatus() {
            return this.mVideoStatus;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Info)) {
                return false;
            }
            Info info = (Info) obj;
            if (!Objects.equals(this.mHandle, info.mHandle) || !Objects.equals(Integer.valueOf(this.mHandlePresentation), Integer.valueOf(info.mHandlePresentation)) || !Objects.equals(this.mPeerDisplayName, info.mPeerDisplayName) || !Objects.equals(this.mCallHandler, info.mCallHandler) || !Objects.equals(Integer.valueOf(this.mCallAttributes), Integer.valueOf(info.mCallAttributes)) || !Objects.equals(this.mDisconnectInfo, info.mDisconnectInfo) || !Objects.equals(Long.valueOf(this.mConnectTimeMillis), Long.valueOf(info.mConnectTimeMillis)) || !DistributedCall.areBundlesEqual(this.mExtraInfo, info.mExtraInfo) || !Objects.equals(Long.valueOf(this.mCreationTimeMillis), Long.valueOf(info.mCreationTimeMillis)) || !Objects.equals(Integer.valueOf(this.mCallDirection), Integer.valueOf(info.mCallDirection)) || !Objects.equals(Integer.valueOf(this.mVideoStatus), Integer.valueOf(info.mVideoStatus))) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mHandle, Integer.valueOf(this.mHandlePresentation), this.mPeerDisplayName, this.mCallHandler, Integer.valueOf(this.mCallAttributes), this.mDisconnectInfo, Long.valueOf(this.mConnectTimeMillis), this.mExtraInfo, Long.valueOf(this.mCreationTimeMillis), Integer.valueOf(this.mCallDirection), Integer.valueOf(this.mVideoStatus));
        }

        public Info(int i, Uri uri, int i2, String str, int i3, CallHandler callHandler, int i4, DisconnectInfo disconnectInfo, long j, int i5, PacMap pacMap, long j2, int i6) {
            this.mCallId = i;
            this.mHandle = uri;
            this.mHandlePresentation = i2;
            this.mPeerDisplayName = str;
            this.mCallHandler = callHandler;
            this.mCallAttributes = i4;
            this.mDisconnectInfo = disconnectInfo;
            this.mConnectTimeMillis = j;
            this.mExtraInfo = pacMap;
            this.mCreationTimeMillis = j2;
            this.mCallDirection = i6;
            this.mVideoStatus = i5;
        }

        public String toString() {
            return "[id: " + this.mCallId + ", hdlPres: " + this.mHandlePresentation + ", attributes: " + attributesToString(this.mCallAttributes) + ", videoStatus: " + this.mVideoStatus + "]";
        }
    }

    public int accept(int i) {
        return DistributedCallProxy.getInstance().answerCall(this.mCallId, 0);
    }

    public int reject(boolean z, String str) {
        return DistributedCallProxy.getInstance().reject(this.mCallId, z, str);
    }

    public int hangup() {
        return DistributedCallProxy.getInstance().disconnect(this.mCallId);
    }

    public int startDtmf(char c) {
        return DistributedCallProxy.getInstance().startDtmfTone(this.mCallId, c);
    }

    public int stopDtmf() {
        return DistributedCallProxy.getInstance().stopDtmfTone(this.mCallId);
    }

    public int postDialContinue(boolean z) {
        return DistributedCallProxy.getInstance().postDialDtmfContinue(this.mCallId, z);
    }

    public int distributeCallEvent(String str, PacMap pacMap) {
        return DistributedCallProxy.getInstance().distributeCallEvent(this.mCallId, str, pacMap);
    }

    public int getCallId() {
        return this.mCallId;
    }

    public int getPreciseState() {
        return this.mStatus;
    }

    public Info getInfo() {
        return this.mInfo;
    }

    public void updateDetails(DistributedCall distributedCall) {
        if (distributedCall != null) {
            this.mInfo = distributedCall.getInfo();
            this.mStatus = distributedCall.getPreciseState();
        }
    }

    public int registerPreciseObserver(PreciseObserver preciseObserver) {
        HiLog.info(LABEL, "registerPreciseObserver", new Object[0]);
        if (preciseObserver == null || this.mStatus == 7) {
            return -1;
        }
        return PreciseObserverProxy.getInstance().registerPreciseObserver(this, preciseObserver);
    }

    public int unregisterPreciseObserver(PreciseObserver preciseObserver) {
        HiLog.info(LABEL, "unregisterPreciseObserver", new Object[0]);
        if (preciseObserver == null || this.mStatus == 7) {
            return -1;
        }
        return PreciseObserverProxy.getInstance().unregisterPreciseObserver(this.mCallId, preciseObserver);
    }

    public final boolean isNewCallAllowed() {
        return DistributedCallProxy.getInstance().isNewCallAllowed();
    }

    public final int setMuted(boolean z) {
        return DistributedCallProxy.getInstance().setMuted(z);
    }

    public final int setAudioDevice(int i) {
        return DistributedCallProxy.getInstance().setAudioDevice(i);
    }

    public int hold() {
        return DistributedCallProxy.getInstance().hold(this.mCallId);
    }

    public int unhold() {
        return DistributedCallProxy.getInstance().unhold(this.mCallId);
    }

    public List<String> getPredefinedRejectMessages() {
        return DistributedCallProxy.getInstance().getPredefinedRejectMessages(this.mCallId);
    }

    public String toString() {
        return "Call [id: " + this.mCallId + ", status: " + statusToString(this.mStatus) + ", info: " + this.mInfo + "]";
    }

    DistributedCall(int i, int i2, int i3, DisconnectInfo disconnectInfo, long j, Uri uri, int i4, String str, int i5, PacMap pacMap, long j2, int i6, int i7) {
        this.mCallId = i;
        this.mStatus = i2;
        this.mInfo = new Info(i, uri, i4, str, i5, null, i3, disconnectInfo, j, i7, pacMap, j2, i6);
    }

    DistributedCall(int i, int i2, int i3, DisconnectInfo disconnectInfo, long j, Uri uri, int i4, String str, int i5, PacMap pacMap, long j2, int i6) {
        this(i, i2, i3, disconnectInfo, j, uri, i4, str, i5, pacMap, j2, i6, 0);
    }

    /* access modifiers changed from: private */
    public static boolean areBundlesEqual(PacMap pacMap, PacMap pacMap2) {
        return (pacMap == null || pacMap2 == null) ? pacMap == pacMap2 : pacMap.getSize() == pacMap2.getSize();
    }
}
