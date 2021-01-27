package ohos.wifi.p2p;

import java.net.InetAddress;
import java.net.UnknownHostException;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;

@SystemApi
public class WifiP2pLinkedInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pLinkedInfo");
    private boolean isGroupFormed;
    private boolean isGroupOwner;
    private InetAddress ownerAddress;

    public boolean isGroupFormed() {
        return this.isGroupFormed;
    }

    public boolean isGroupOwner() {
        return this.isGroupOwner;
    }

    public InetAddress getOwnerAddress() {
        return this.ownerAddress;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
        if (r5.writeByteArray(r4.ownerAddress.getAddress()) != false) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        if (r5.writeByte((byte) 0) != false) goto L_0x0032;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0045 A[RETURN] */
    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        Object[] objArr;
        Object[] objArr2 = (!parcel.writeByte(this.isGroupFormed ? (byte) 1 : 0) || !parcel.writeByte(this.isGroupOwner ? (byte) 1 : 0)) ? null : 1;
        if (this.ownerAddress != null) {
            if (((objArr2 == null || !parcel.writeByte((byte) 1)) ? null : 1) != null) {
            }
            objArr = null;
            if (objArr != null) {
                return true;
            }
            parcel.reclaim();
            return false;
        }
        if (objArr2 != null) {
        }
        objArr = null;
        if (objArr != null) {
        }
        objArr = 1;
        if (objArr != null) {
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.isGroupFormed = parcel.readInt() == 1;
        this.isGroupOwner = parcel.readInt() == 1;
        if (parcel.readByte() == 1) {
            try {
                this.ownerAddress = InetAddress.getByAddress(parcel.readByteArray());
            } catch (UnknownHostException unused) {
                HiLog.warn(LABEL, "UnknownHostException in WifiP2pLinkedInfo unmarshalling!", new Object[0]);
            }
        }
        return true;
    }
}
