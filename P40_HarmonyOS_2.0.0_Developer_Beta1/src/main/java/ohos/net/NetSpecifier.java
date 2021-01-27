package ohos.net;

import java.util.Objects;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.keystore.KeyStoreConstants;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NetSpecifier implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NetSpecifier");
    public static final int REQUEST_ID_UNSET = 0;
    private static final String[] SPECIFIER_TYPE = {KeyStoreConstants.DIGEST_ALGORITHM_NONE, "LISTEN", "TRACK_DEFAULT", "REQUEST", "BACKGROUND_REQUEST"};
    public static final int TYPE_NONE = -1;
    public int legacyType;
    public NetCapabilities netCapabilities;
    public int requestId;
    private int specifierType;

    public NetSpecifier() {
    }

    public NetSpecifier(NetCapabilities netCapabilities2, int i, int i2, int i3) {
        this.requestId = i2;
        this.netCapabilities = netCapabilities2;
        this.legacyType = i;
        this.specifierType = i3;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NetSpecifier)) {
            return false;
        }
        NetSpecifier netSpecifier = (NetSpecifier) obj;
        if (this.requestId == netSpecifier.requestId && this.legacyType == netSpecifier.legacyType && this.specifierType == netSpecifier.specifierType && Objects.equals(this.netCapabilities, netSpecifier.netCapabilities)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.requestId), Integer.valueOf(this.legacyType), this.netCapabilities, Integer.valueOf(this.specifierType));
    }

    public static class Builder {
        private final NetCapabilities mNetCapabilities = new NetCapabilities();

        public NetSpecifier build() {
            NetCapabilities netCapabilities = new NetCapabilities(this.mNetCapabilities);
            netCapabilities.maybeMarkCapabilitiesRestricted();
            return new NetSpecifier(netCapabilities, -1, 0, 0);
        }

        public Builder clearCapabilities() {
            this.mNetCapabilities.clearAll();
            return this;
        }

        public Builder addCapability(int i) {
            this.mNetCapabilities.addCapability(i);
            return this;
        }

        public Builder removeCapability(int i) {
            this.mNetCapabilities.removeCapability(i);
            return this;
        }

        public Builder addBearer(int i) {
            this.mNetCapabilities.addBearer(i);
            return this;
        }

        public Builder removeBearer(int i) {
            this.mNetCapabilities.removeBearer(i);
            return this;
        }

        public Builder setBearerPrivateIdentifier(String str) {
            return (str == null || str.length() == 0) ? this : setBearerPrivateIdentifier(new StringBearerPrivateIdentifier(str));
        }

        public Builder setBearerPrivateIdentifier(BearerPrivateIdentifier bearerPrivateIdentifier) {
            if (MatchAllBearerPrivateIdentifier.checkNotMatchAllBearerPrivateIdentifier(bearerPrivateIdentifier)) {
                this.mNetCapabilities.setBearerPrivateIdentifier(bearerPrivateIdentifier);
            }
            return this;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        NetCapabilities netCapabilities2 = this.netCapabilities;
        if (netCapabilities2 == null) {
            return false;
        }
        netCapabilities2.marshalling(parcel);
        parcel.writeInt(this.legacyType);
        parcel.writeInt(this.requestId);
        int i = this.specifierType;
        if (i >= 0) {
            String[] strArr = SPECIFIER_TYPE;
            if (i < strArr.length) {
                parcel.writeString(strArr[i]);
                return true;
            }
        }
        parcel.writeString(SPECIFIER_TYPE[0]);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.netCapabilities = new NetCapabilities();
        this.netCapabilities.unmarshalling(parcel);
        this.legacyType = parcel.readInt();
        this.requestId = parcel.readInt();
        this.specifierType = convertSpecifierType(parcel.readString());
        return true;
    }

    private int convertSpecifierType(String str) {
        int length = SPECIFIER_TYPE.length;
        while (length > 0 && !Objects.equals(str, SPECIFIER_TYPE[length - 1])) {
            length--;
        }
        return length;
    }
}
