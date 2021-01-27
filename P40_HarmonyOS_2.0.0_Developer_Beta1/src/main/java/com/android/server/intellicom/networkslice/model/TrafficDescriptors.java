package com.android.server.intellicom.networkslice.model;

import android.os.Bundle;
import android.os.Messenger;
import android.text.TextUtils;
import com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TrafficDescriptors {
    public static final int CCT_TYPE_IMS = 1;
    public static final int CCT_TYPE_INVALID = -1;
    public static final int CCT_TYPE_MMS = 2;
    public static final int CCT_TYPE_SUPL = 4;
    private static final byte MATCH_AVAILABLE = 8;
    private static final byte MATCH_CCT = 16;
    private static final byte MATCH_DNN = 2;
    private static final byte MATCH_FQDN = 4;
    private static final int ROUTE_BITMAP_BIT4 = 4;
    private static final String SEPARATOR = ",";
    private static final String TDS_APPIDS = "appIds";
    private static final String TDS_IPV4_ADDRANDMASK = "ipv4AddrAndMask";
    private static final String TDS_IPV4_NUM = "ipv4Num";
    private static final String TDS_IPV6_ADDRANDPREFIX = "ipv6AddrAndPrefix";
    private static final String TDS_IPV6_NUM = "ipv6Num";
    private static final String TDS_PROTOCOLIDS = "protocolIds";
    private static final String TDS_REMOTEPORTS = "remotePorts";
    public static final String TDS_ROUTE_BITMAP = "routeBitmap";
    private static final String TDS_URSP_PRECEDENCE = "urspPrecedence";
    private final String mAppIds;
    private final Set<String> mAtiveTriggeringApps;
    private int mCct;
    private final String mDnn;
    private final String mFqdn;
    private final FqdnIps mFqdnIps;
    private boolean mHasAvailableUrsp;
    private final InetAddress mIp;
    private final byte[] mIpv4AddrAndMask;
    private final byte mIpv4Num;
    private final byte[] mIpv6AddrAndPrefix;
    private final byte mIpv6Num;
    private final boolean mIsIpTriad;
    private final boolean mIsMatchCct;
    private final boolean mIsMatchDnn;
    private final boolean mIsMatchFqdn;
    private final boolean mIsNeedToCreateRequest;
    private boolean mIsRequestAgain;
    private final Messenger mMessenger;
    private final String mProtocolId;
    private final String mProtocolIds;
    private final String mRemotePort;
    private final String mRemotePorts;
    private RouteBindType mRouteBindType;
    private final byte mRouteBitmap;
    private final int mUid;
    private final byte mUrspPrecedence;

    public enum RouteBindType {
        UID_TDS,
        UID_IP_TDS,
        IP_TDS,
        INVALID_TDS
    }

    /* synthetic */ TrafficDescriptors(Builder x0, AnonymousClass1 x1) {
        this(x0);
    }

    private TrafficDescriptors(Builder builder) {
        this.mAtiveTriggeringApps = new HashSet();
        this.mCct = -1;
        this.mUrspPrecedence = builder.mUrspPrecedence;
        this.mAppIds = getUnsignedAppIds(builder.mAppIds);
        this.mIpv4Num = builder.mIpv4Num;
        this.mIpv4AddrAndMask = builder.mIpv4AddrAndMask;
        this.mIpv6Num = builder.mIpv6Num;
        this.mIpv6AddrAndPrefix = builder.mIpv6AddrAndPrefix;
        this.mProtocolIds = builder.mProtocolIds;
        this.mRemotePorts = builder.mRemotePorts;
        this.mRouteBitmap = builder.mRouteBitmap;
        this.mDnn = builder.mDnn;
        this.mFqdn = builder.mFqdn;
        this.mIp = builder.mIp;
        this.mProtocolId = builder.mProtocolId;
        this.mRemotePort = builder.mRemotePort;
        this.mUid = builder.mUid;
        this.mMessenger = builder.mMessenger;
        this.mFqdnIps = builder.mFqdnIps;
        this.mCct = builder.mCct;
        this.mIsNeedToCreateRequest = builder.mIsNeedToCreateRequest;
        boolean hasAppids = false;
        this.mIsIpTriad = (this.mIpv4Num == 0 && this.mIpv6Num == 0) ? false : true;
        this.mIsMatchFqdn = (this.mRouteBitmap & 4) != 0;
        this.mIsMatchDnn = (this.mRouteBitmap & 2) != 0;
        this.mHasAvailableUrsp = (this.mRouteBitmap & 8) != 0;
        this.mIsMatchCct = (this.mRouteBitmap & 16) != 0;
        boolean hasIps = this.mIsIpTriad || this.mIsMatchFqdn;
        hasAppids = (!TextUtils.isEmpty(this.mAppIds) || this.mIsMatchDnn) ? true : hasAppids;
        this.mRouteBindType = RouteBindType.INVALID_TDS;
        if (hasAppids && hasIps) {
            this.mRouteBindType = RouteBindType.UID_IP_TDS;
        } else if (hasAppids) {
            this.mRouteBindType = RouteBindType.UID_TDS;
        } else if (hasIps) {
            this.mRouteBindType = RouteBindType.IP_TDS;
        }
    }

    public static TrafficDescriptors makeTrafficDescriptors(Bundle data) {
        if (data == null) {
            return new Builder().build();
        }
        return new Builder().setAppIds(data.getString(TDS_APPIDS, "")).setUrspPrecedence(data.getByte(TDS_URSP_PRECEDENCE, (byte) 0).byteValue()).setIpv4Num(data.getByte(TDS_IPV4_NUM, (byte) 0).byteValue()).setIpv4AddrAndMask(data.getByteArray(TDS_IPV4_ADDRANDMASK)).setIpv6Num(data.getByte(TDS_IPV6_NUM, (byte) 0).byteValue()).setIpv6AddrAndPrefix(data.getByteArray(TDS_IPV6_ADDRANDPREFIX)).setProtocolIds(data.getString(TDS_PROTOCOLIDS, "")).setRemotePorts(data.getString(TDS_REMOTEPORTS, "")).setRouteBitmap(data.getByte(TDS_ROUTE_BITMAP, (byte) 0).byteValue()).build();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.intellicom.networkslice.model.TrafficDescriptors$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType = new int[RouteBindType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[RouteBindType.UID_IP_TDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[RouteBindType.UID_TDS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public boolean isUidRouteBindType() {
        int i = AnonymousClass1.$SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[this.mRouteBindType.ordinal()];
        if (i == 1 || i == 2) {
            return true;
        }
        return false;
    }

    public boolean isMatchDnn() {
        return this.mIsMatchDnn;
    }

    public boolean isMatchNetworkCap() {
        return this.mIsMatchDnn || this.mIsMatchCct;
    }

    public boolean isMatchFqdn() {
        return this.mIsMatchFqdn;
    }

    public boolean hasAvailableUrsp() {
        return this.mHasAvailableUrsp;
    }

    public boolean isAtiveTriggeringApp(String packageName) {
        return this.mAtiveTriggeringApps.contains(packageName);
    }

    public void setRequestAgain(boolean requestAgain) {
        this.mIsRequestAgain = requestAgain;
    }

    public boolean isRequestAgain() {
        return this.mIsRequestAgain;
    }

    public RouteBindType getRouteBindType() {
        return this.mRouteBindType;
    }

    public byte getUrspPrecedence() {
        return this.mUrspPrecedence;
    }

    public String getAppIds() {
        return this.mAppIds;
    }

    public byte getIpv4Num() {
        return this.mIpv4Num;
    }

    public byte[] getIpv4AddrAndMask() {
        return this.mIpv4AddrAndMask;
    }

    public byte getIpv6Num() {
        return this.mIpv6Num;
    }

    public byte[] getIpv6AddrAndPrefix() {
        return this.mIpv6AddrAndPrefix;
    }

    public String getProtocolIds() {
        return this.mProtocolIds;
    }

    public String getRemotePorts() {
        return this.mRemotePorts;
    }

    public byte getRouteBitmap() {
        return this.mRouteBitmap;
    }

    public FqdnIps getFqdnIps() {
        return this.mFqdnIps;
    }

    public Messenger getMessenger() {
        return this.mMessenger;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getDnn() {
        return this.mDnn;
    }

    public int getCct() {
        return this.mCct;
    }

    public String getFqdn() {
        return this.mFqdn;
    }

    public InetAddress getIp() {
        return this.mIp;
    }

    public String getProtocolId() {
        return this.mProtocolId;
    }

    public String getRemotePort() {
        return this.mRemotePort;
    }

    public boolean isNeedToCreateRequest() {
        return this.mIsNeedToCreateRequest;
    }

    public boolean isIpTriad() {
        return this.mIsIpTriad;
    }

    public static final class Builder {
        private String mAppIds;
        private int mCct;
        private String mDnn;
        private String mFqdn;
        private FqdnIps mFqdnIps;
        private InetAddress mIp;
        private byte[] mIpv4AddrAndMask;
        private byte mIpv4Num;
        private byte[] mIpv6AddrAndPrefix;
        private byte mIpv6Num;
        private boolean mIsNeedToCreateRequest;
        private Messenger mMessenger;
        private String mProtocolId;
        private String mProtocolIds;
        private String mRemotePort;
        private String mRemotePorts;
        private byte mRouteBitmap;
        private int mUid;
        private byte mUrspPrecedence;

        public Builder setUrspPrecedence(byte urspPrecedence) {
            this.mUrspPrecedence = urspPrecedence;
            return this;
        }

        public Builder setAppIds(String appIds) {
            this.mAppIds = appIds;
            return this;
        }

        public Builder setIpv4Num(byte ipv4Num) {
            this.mIpv4Num = ipv4Num;
            return this;
        }

        public Builder setIpv4AddrAndMask(byte[] ipv4AddrAndMask) {
            this.mIpv4AddrAndMask = ipv4AddrAndMask;
            return this;
        }

        public Builder setIpv6Num(byte ipv6Num) {
            this.mIpv6Num = ipv6Num;
            return this;
        }

        public Builder setIpv6AddrAndPrefix(byte[] ipv6AddrAndPrefix) {
            this.mIpv6AddrAndPrefix = ipv6AddrAndPrefix;
            return this;
        }

        public Builder setProtocolIds(String protocolIds) {
            this.mProtocolIds = protocolIds;
            return this;
        }

        public Builder setRemotePorts(String remotePorts) {
            this.mRemotePorts = remotePorts;
            return this;
        }

        public Builder setRouteBitmap(byte routeBitmap) {
            this.mRouteBitmap = routeBitmap;
            return this;
        }

        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        public Builder setDnn(String dnn) {
            this.mDnn = dnn;
            return this;
        }

        public Builder setCct(int cct) {
            this.mCct = cct;
            return this;
        }

        public Builder setFqdn(String fqdn) {
            this.mFqdn = fqdn;
            return this;
        }

        public Builder setIp(InetAddress ip) {
            this.mIp = ip;
            return this;
        }

        public Builder setProtocolId(String protocolId) {
            this.mProtocolId = protocolId;
            return this;
        }

        public Builder setRemotePort(String remotePort) {
            this.mRemotePort = remotePort;
            return this;
        }

        public Builder setMessenger(Messenger messenger) {
            this.mMessenger = messenger;
            return this;
        }

        public Builder setFqdnIps(FqdnIps fqdnIps) {
            this.mFqdnIps = fqdnIps;
            return this;
        }

        public Builder setNeedToCreateRequest(boolean needToCreateRequest) {
            this.mIsNeedToCreateRequest = needToCreateRequest;
            return this;
        }

        public TrafficDescriptors build() {
            return new TrafficDescriptors(this, null);
        }
    }

    public String toString() {
        return "TrafficDescriptors{mRouteBitmap=" + ((int) this.mRouteBitmap) + ", mUrspPrecedence=" + ((int) this.mUrspPrecedence) + ", mIpv4Num=" + ((int) this.mIpv4Num) + ", mIpv6Num=" + ((int) this.mIpv6Num) + ", mProtocolIds='" + this.mProtocolIds + "', mRouteBindType=" + this.mRouteBindType + ", mDnn='" + this.mDnn + ", mProtocolId='" + this.mProtocolId + "', mRemotePort='" + this.mRemotePort + "', mMessenger=" + this.mMessenger + ", mIsNeedToCreateRequest=" + this.mIsNeedToCreateRequest + ", mIsIpTriad=" + this.mIsIpTriad + ", mIsMatchDnn=" + this.mIsMatchDnn + ", mIsMatchFqdn=" + this.mIsMatchFqdn + ", mHasAvailableUrsp=" + this.mHasAvailableUrsp + ", mIsMatchCct=" + this.mIsMatchCct + '}';
    }

    private final String getUnsignedAppIds(String appIds) {
        if (appIds == null) {
            return null;
        }
        String[] osAppIds = appIds.split(",");
        for (int i = 0; i < osAppIds.length; i++) {
            OsAppId osAppId = OsAppId.create(osAppIds[i]);
            if (osAppId != null && HwNetworkSliceSettingsObserver.getInstance().isCooperativeApp(osAppId.getAppId())) {
                this.mAtiveTriggeringApps.add(osAppId.getAppId());
                osAppIds[i] = null;
            }
        }
        return (String) Arrays.stream(osAppIds).filter($$Lambda$TrafficDescriptors$b1NeHyL1bFRXlGW69k1gU_8XwI0.INSTANCE).collect(Collectors.joining(","));
    }

    static /* synthetic */ boolean lambda$getUnsignedAppIds$0(String id) {
        return id != null;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TrafficDescriptors that = (TrafficDescriptors) obj;
        if (this.mRouteBitmap == that.mRouteBitmap && this.mUrspPrecedence == that.mUrspPrecedence && this.mIpv4Num == that.mIpv4Num && this.mIpv6Num == that.mIpv6Num && this.mIsIpTriad == that.mIsIpTriad && this.mIsMatchDnn == that.mIsMatchDnn && this.mIsMatchFqdn == that.mIsMatchFqdn && this.mHasAvailableUrsp == that.mHasAvailableUrsp && this.mIsMatchCct == that.mIsMatchCct && this.mUid == that.mUid && this.mIsNeedToCreateRequest == that.mIsNeedToCreateRequest && this.mIsRequestAgain == that.mIsRequestAgain && this.mCct == that.mCct && Objects.equals(this.mAppIds, that.mAppIds) && Arrays.equals(this.mIpv4AddrAndMask, that.mIpv4AddrAndMask) && Arrays.equals(this.mIpv6AddrAndPrefix, that.mIpv6AddrAndPrefix) && Objects.equals(this.mProtocolIds, that.mProtocolIds) && Objects.equals(this.mRemotePorts, that.mRemotePorts) && Objects.equals(this.mAtiveTriggeringApps, that.mAtiveTriggeringApps) && this.mRouteBindType == that.mRouteBindType && Objects.equals(this.mDnn, that.mDnn) && Objects.equals(this.mFqdn, that.mFqdn) && Objects.equals(this.mIp, that.mIp) && Objects.equals(this.mProtocolId, that.mProtocolId) && Objects.equals(this.mRemotePort, that.mRemotePort) && Objects.equals(this.mMessenger, that.mMessenger) && Objects.equals(this.mFqdnIps, that.mFqdnIps)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((Objects.hash(Byte.valueOf(this.mRouteBitmap), Byte.valueOf(this.mUrspPrecedence), this.mAppIds, Byte.valueOf(this.mIpv4Num), Byte.valueOf(this.mIpv6Num), this.mProtocolIds, this.mRemotePorts, Boolean.valueOf(this.mIsIpTriad), Boolean.valueOf(this.mIsMatchDnn), Boolean.valueOf(this.mIsMatchFqdn), Boolean.valueOf(this.mHasAvailableUrsp), Boolean.valueOf(this.mIsMatchCct), this.mAtiveTriggeringApps, this.mRouteBindType, Integer.valueOf(this.mUid), this.mDnn, this.mFqdn, this.mIp, this.mProtocolId, this.mRemotePort, this.mMessenger, this.mFqdnIps, Boolean.valueOf(this.mIsNeedToCreateRequest), Boolean.valueOf(this.mIsRequestAgain), Integer.valueOf(this.mCct)) * 31) + Arrays.hashCode(this.mIpv4AddrAndMask)) * 31) + Arrays.hashCode(this.mIpv6AddrAndPrefix);
    }
}
