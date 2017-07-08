package android.telephony.cdma;

import android.os.Bundle;
import android.telephony.CellLocation;
import android.util.PtmLog;

public class CdmaCellLocation extends CellLocation {
    public static final int INVALID_LAT_LONG = Integer.MAX_VALUE;
    private int mBaseStationId;
    private int mBaseStationLatitude;
    private int mBaseStationLongitude;
    private int mNetworkId;
    private int mSystemId;

    public CdmaCellLocation() {
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
    }

    public CdmaCellLocation(Bundle bundle) {
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
        this.mBaseStationId = bundle.getInt("baseStationId", this.mBaseStationId);
        this.mBaseStationLatitude = bundle.getInt("baseStationLatitude", this.mBaseStationLatitude);
        this.mBaseStationLongitude = bundle.getInt("baseStationLongitude", this.mBaseStationLongitude);
        this.mSystemId = bundle.getInt("systemId", this.mSystemId);
        this.mNetworkId = bundle.getInt("networkId", this.mNetworkId);
    }

    public int getBaseStationId() {
        return this.mBaseStationId;
    }

    public int getBaseStationLatitude() {
        return this.mBaseStationLatitude;
    }

    public int getBaseStationLongitude() {
        return this.mBaseStationLongitude;
    }

    public int getSystemId() {
        return this.mSystemId;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public void setStateInvalid() {
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
    }

    public void setCellLocationData(int baseStationId, int baseStationLatitude, int baseStationLongitude) {
        this.mBaseStationId = baseStationId;
        this.mBaseStationLatitude = baseStationLatitude;
        this.mBaseStationLongitude = baseStationLongitude;
    }

    public void setCellLocationData(int baseStationId, int baseStationLatitude, int baseStationLongitude, int systemId, int networkId) {
        this.mBaseStationId = baseStationId;
        this.mBaseStationLatitude = baseStationLatitude;
        this.mBaseStationLongitude = baseStationLongitude;
        this.mSystemId = systemId;
        this.mNetworkId = networkId;
    }

    public int hashCode() {
        return (((this.mBaseStationId ^ this.mBaseStationLatitude) ^ this.mBaseStationLongitude) ^ this.mSystemId) ^ this.mNetworkId;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            CdmaCellLocation s = (CdmaCellLocation) o;
            if (o == null) {
                return false;
            }
            if (equalsHandlesNulls(Integer.valueOf(this.mBaseStationId), Integer.valueOf(s.mBaseStationId)) && equalsHandlesNulls(Integer.valueOf(this.mBaseStationLatitude), Integer.valueOf(s.mBaseStationLatitude)) && equalsHandlesNulls(Integer.valueOf(this.mBaseStationLongitude), Integer.valueOf(s.mBaseStationLongitude)) && equalsHandlesNulls(Integer.valueOf(this.mSystemId), Integer.valueOf(s.mSystemId))) {
                z = equalsHandlesNulls(Integer.valueOf(this.mNetworkId), Integer.valueOf(s.mNetworkId));
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "[" + this.mBaseStationId + PtmLog.PAIRE_DELIMETER + this.mBaseStationLatitude + PtmLog.PAIRE_DELIMETER + this.mBaseStationLongitude + PtmLog.PAIRE_DELIMETER + this.mSystemId + PtmLog.PAIRE_DELIMETER + this.mNetworkId + "]";
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    public void fillInNotifierBundle(Bundle bundleToFill) {
        bundleToFill.putInt("baseStationId", this.mBaseStationId);
        bundleToFill.putInt("baseStationLatitude", this.mBaseStationLatitude);
        bundleToFill.putInt("baseStationLongitude", this.mBaseStationLongitude);
        bundleToFill.putInt("systemId", this.mSystemId);
        bundleToFill.putInt("networkId", this.mNetworkId);
    }

    public boolean isEmpty() {
        if (this.mBaseStationId == -1 && this.mBaseStationLatitude == INVALID_LAT_LONG && this.mBaseStationLongitude == INVALID_LAT_LONG && this.mSystemId == -1 && this.mNetworkId == -1) {
            return true;
        }
        return false;
    }

    public static double convertQuartSecToDecDegrees(int quartSec) {
        if (!Double.isNaN((double) quartSec) && quartSec >= -2592000 && quartSec <= 2592000) {
            return ((double) quartSec) / 14400.0d;
        }
        throw new IllegalArgumentException("Invalid coordiante value:" + quartSec);
    }
}
