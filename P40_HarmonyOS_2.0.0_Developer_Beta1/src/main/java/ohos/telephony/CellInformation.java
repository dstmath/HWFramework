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

@SystemApi
public abstract class CellInformation implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "CellInformation");
    private int cellNetworkType;
    private boolean isCamped;
    private long nanoTimeStamp;

    public abstract SignalInformation getSignalInformation();

    protected CellInformation(int i, boolean z, long j) {
        this.cellNetworkType = i;
        this.isCamped = z;
        this.nanoTimeStamp = j;
    }

    public int getCellNetworkType() {
        return this.cellNetworkType;
    }

    public boolean isCamped() {
        return this.isCamped;
    }

    public long getTimeStamp() {
        return this.nanoTimeStamp;
    }

    public static void sendUpdateCellLocationRequest() {
        TelephonyProxy instance = TelephonyProxy.getInstance();
        if (instance != null) {
            instance.sendUpdateCellLocationRequest();
        }
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.cellNetworkType), Boolean.valueOf(this.isCamped), Long.valueOf(this.nanoTimeStamp));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CellInformation)) {
            return false;
        }
        CellInformation cellInformation = (CellInformation) obj;
        return this.isCamped == cellInformation.isCamped && this.nanoTimeStamp == cellInformation.nanoTimeStamp && this.cellNetworkType == cellInformation.cellNetworkType;
    }

    public String toString() {
        return "{ isCamped=" + this.isCamped + ", nanoTimeStamp=" + this.nanoTimeStamp + "ns";
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeInt(this.cellNetworkType);
        parcel.writeBoolean(this.isCamped);
        parcel.writeLong(this.nanoTimeStamp);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.cellNetworkType = parcel.readInt();
        this.isCamped = parcel.readBoolean();
        this.nanoTimeStamp = parcel.readLong();
        return true;
    }

    static List<CellInformation> createCellInfoListFromParcel(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt <= 0 || readInt > 100) {
            HiLog.error(TAG, "createCellInfoListFromParcel size: %{public}d", Integer.valueOf(readInt));
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
                    GsmCellInformation gsmCellInformation = new GsmCellInformation();
                    messageParcel.readSequenceable(gsmCellInformation);
                    arrayList.add(gsmCellInformation);
                    break;
                case 2:
                    CdmaCellInformation cdmaCellInformation = new CdmaCellInformation();
                    messageParcel.readSequenceable(cdmaCellInformation);
                    arrayList.add(cdmaCellInformation);
                    break;
                case 3:
                    WcdmaCellInformation wcdmaCellInformation = new WcdmaCellInformation();
                    messageParcel.readSequenceable(wcdmaCellInformation);
                    arrayList.add(wcdmaCellInformation);
                    break;
                case 4:
                    TdscdmaCellInformation tdscdmaCellInformation = new TdscdmaCellInformation();
                    messageParcel.readSequenceable(tdscdmaCellInformation);
                    arrayList.add(tdscdmaCellInformation);
                    break;
                case 5:
                    LteCellInformation lteCellInformation = new LteCellInformation();
                    messageParcel.readSequenceable(lteCellInformation);
                    arrayList.add(lteCellInformation);
                    break;
                case 6:
                    NrCellInformation nrCellInformation = new NrCellInformation();
                    messageParcel.readSequenceable(nrCellInformation);
                    arrayList.add(nrCellInformation);
                    break;
                default:
                    HiLog.error(TAG, "unkown cell radio typeFlag: %{public}d", Integer.valueOf(readInt2));
                    break;
            }
            readInt = i;
        }
    }
}
