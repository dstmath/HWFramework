package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class CellNetworkScanResult implements Parcelable {
    public static final Creator<CellNetworkScanResult> CREATOR = new Creator<CellNetworkScanResult>() {
        public CellNetworkScanResult createFromParcel(Parcel in) {
            return new CellNetworkScanResult(in, null);
        }

        public CellNetworkScanResult[] newArray(int size) {
            return new CellNetworkScanResult[size];
        }
    };
    public static final int STATUS_RADIO_GENERIC_FAILURE = 3;
    public static final int STATUS_RADIO_NOT_AVAILABLE = 2;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_UNKNOWN_ERROR = 4;
    private final List<OperatorInfo> mOperators;
    private final int mStatus;

    public CellNetworkScanResult(int status, List<OperatorInfo> operators) {
        this.mStatus = status;
        this.mOperators = operators;
    }

    private CellNetworkScanResult(Parcel in) {
        this.mStatus = in.readInt();
        int len = in.readInt();
        if (len > 0) {
            this.mOperators = new ArrayList();
            for (int i = 0; i < len; i++) {
                this.mOperators.add((OperatorInfo) OperatorInfo.CREATOR.createFromParcel(in));
            }
            return;
        }
        this.mOperators = null;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public List<OperatorInfo> getOperators() {
        return this.mOperators;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mStatus);
        if (this.mOperators == null || this.mOperators.size() <= 0) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mOperators.size());
        for (OperatorInfo network : this.mOperators) {
            network.writeToParcel(out, flags);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellNetworkScanResult: {");
        sb.append(" status:").append(this.mStatus);
        if (this.mOperators != null) {
            for (OperatorInfo network : this.mOperators) {
                sb.append(" network:").append(network);
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
