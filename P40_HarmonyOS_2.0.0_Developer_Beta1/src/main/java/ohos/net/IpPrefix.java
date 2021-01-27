package ohos.net;

import android.net.NetworkUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class IpPrefix implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "IpPrefix");
    private byte[] mAddress;
    private int mPrefixLength;

    public IpPrefix(byte[] bArr, int i) {
        if (bArr != null) {
            this.mAddress = (byte[]) bArr.clone();
            this.mPrefixLength = i;
            checkAndMaskAddressAndPrefixLength();
        }
    }

    public IpPrefix() {
        this.mPrefixLength = 0;
    }

    private void checkAndMaskAddressAndPrefixLength() {
        byte[] bArr = this.mAddress;
        if (bArr.length == 4 || bArr.length == 16) {
            NetworkUtils.maskRawAddress(this.mAddress, this.mPrefixLength);
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mAddress = parcel.readByteArray();
        this.mPrefixLength = parcel.readInt();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByteArray(this.mAddress);
        parcel.writeInt(this.mPrefixLength);
        return true;
    }

    public int getPrefixLength() {
        return this.mPrefixLength;
    }

    public Optional<InetAddress> getAddress() {
        try {
            return Optional.ofNullable(InetAddress.getByAddress(this.mAddress));
        } catch (UnknownHostException unused) {
            HiLog.warn(LABEL, "Exception to getAddress", new Object[0]);
            return Optional.empty();
        }
    }
}
