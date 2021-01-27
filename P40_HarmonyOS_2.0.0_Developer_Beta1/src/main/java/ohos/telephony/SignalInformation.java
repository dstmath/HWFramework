package ohos.telephony;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public abstract class SignalInformation implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "SignalInformation");
    private int mNetworkType;

    public abstract int getSignalLevel();

    @SystemApi
    public abstract int getSignalStrength();

    protected SignalInformation(int i) {
        this.mNetworkType = i;
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mNetworkType));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SignalInformation)) {
            return false;
        }
        return this.mNetworkType == ((SignalInformation) obj).mNetworkType;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeInt(this.mNetworkType);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mNetworkType = parcel.readInt();
        return true;
    }

    static List<SignalInformation> createSignalInfoListFromParcel(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt <= 0 || readInt > 100) {
            HiLog.error(TAG, "createSignalInfoListFromParcel size: %{public}d", Integer.valueOf(readInt));
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(readInt);
        while (true) {
            int i = readInt - 1;
            if (readInt <= 0) {
                return arrayList;
            }
            int readInt2 = messageParcel.readInt();
            switch (readInt2) {
                case 1:
                    GsmSignalInformation gsmSignalInformation = new GsmSignalInformation();
                    messageParcel.readSequenceable(gsmSignalInformation);
                    arrayList.add(gsmSignalInformation);
                    break;
                case 2:
                    CdmaSignalInformation cdmaSignalInformation = new CdmaSignalInformation();
                    messageParcel.readSequenceable(cdmaSignalInformation);
                    arrayList.add(cdmaSignalInformation);
                    break;
                case 3:
                    WcdmaSignalInformation wcdmaSignalInformation = new WcdmaSignalInformation();
                    messageParcel.readSequenceable(wcdmaSignalInformation);
                    arrayList.add(wcdmaSignalInformation);
                    break;
                case 4:
                    TdscdmaSignalInformation tdscdmaSignalInformation = new TdscdmaSignalInformation();
                    messageParcel.readSequenceable(tdscdmaSignalInformation);
                    arrayList.add(tdscdmaSignalInformation);
                    break;
                case 5:
                    LteSignalInformation lteSignalInformation = new LteSignalInformation();
                    messageParcel.readSequenceable(lteSignalInformation);
                    arrayList.add(lteSignalInformation);
                    break;
                case 6:
                    NrSignalInformation nrSignalInformation = new NrSignalInformation();
                    messageParcel.readSequenceable(nrSignalInformation);
                    arrayList.add(nrSignalInformation);
                    break;
                default:
                    HiLog.error(TAG, "unkown signal radio typeFlag: %{public}d", Integer.valueOf(readInt2));
                    break;
            }
            readInt = i;
        }
    }
}
