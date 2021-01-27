package ohos.telephony;

import java.util.List;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class NetworkSearchResult implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "NetworkSearchResult");
    private boolean isNetworkSearchSuccess = false;
    private List<NetworkInformation> mNetworkSearchResult;

    public NetworkSearchResult(boolean z, List<NetworkInformation> list) {
        this.isNetworkSearchSuccess = z;
        this.mNetworkSearchResult = list;
    }

    public boolean getNetworkSearchStatus() {
        return this.isNetworkSearchSuccess;
    }

    public List<NetworkInformation> getNetworkSearchResult() {
        return this.mNetworkSearchResult;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        List<NetworkInformation> list;
        if (parcel == null || (list = this.mNetworkSearchResult) == null || list.size() == 0) {
            return false;
        }
        parcel.writeBoolean(this.isNetworkSearchSuccess);
        int size = this.mNetworkSearchResult.size();
        parcel.writeInt(size);
        for (int i = 0; i < size; i++) {
            this.mNetworkSearchResult.get(i).marshalling(parcel);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (!(parcel == null || this.mNetworkSearchResult == null)) {
            this.isNetworkSearchSuccess = parcel.readBoolean();
            int readInt = parcel.readInt();
            if (readInt <= 0 || readInt > 100) {
                HiLog.error(TAG, "network information size: %{public}d", Integer.valueOf(readInt));
            } else {
                for (int i = 0; i < readInt; i++) {
                    NetworkInformation networkInformation = new NetworkInformation();
                    networkInformation.marshalling(parcel);
                    this.mNetworkSearchResult.add(networkInformation);
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(NetworkSearchResult.class.getSimpleName());
        sb.append(":isNetworkSearchSuccess = ");
        sb.append(this.isNetworkSearchSuccess);
        if (this.mNetworkSearchResult != null) {
            sb.append(" ");
            for (NetworkInformation networkInformation : this.mNetworkSearchResult) {
                sb.append(networkInformation.toString());
            }
        }
        return sb.toString();
    }
}
