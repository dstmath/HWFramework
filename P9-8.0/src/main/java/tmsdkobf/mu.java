package tmsdkobf;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsNeighborCell;

public class mu {
    private PhoneStateListener Bm;
    private a Bn;
    private CellLocation Bo;
    private int Bp = -133;
    private int Bq = -1;
    private int Br = -1;
    private TelephonyManager mTelephonyManager;

    public interface a {
        void a(BsInput bsInput);
    }

    public mu(mt mtVar) {
    }

    public void a(a aVar) {
        this.Bn = aVar;
    }

    void fh() {
        if (this.Bn != null) {
            this.Bn.a(fi());
        }
    }

    public BsInput fi() {
        BsInput bsInput = new BsInput();
        bsInput.timeInSeconds = (int) (System.currentTimeMillis() / 1000);
        bsInput.networkType = (short) ((short) this.Bq);
        bsInput.dataState = (short) ((short) this.Br);
        if (this.Bo == null) {
            try {
                CellLocation cellLocation = this.mTelephonyManager.getCellLocation();
                if (cellLocation != null && (cellLocation instanceof GsmCellLocation)) {
                    this.Bo = (GsmCellLocation) cellLocation;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (this.Bo != null) {
            if (this.Bo instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) this.Bo;
                bsInput.cid = gsmCellLocation.getCid();
                bsInput.lac = gsmCellLocation.getLac();
            } else if (this.Bo instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) this.Bo;
                bsInput.cid = cdmaCellLocation.getBaseStationId();
                bsInput.lac = cdmaCellLocation.getNetworkId();
                bsInput.loc = (((long) cdmaCellLocation.getBaseStationLatitude()) << 32) | ((long) cdmaCellLocation.getBaseStationLongitude());
            }
        }
        bsInput.bsss = (short) ((short) this.Bp);
        try {
            String networkOperator = this.mTelephonyManager.getNetworkOperator();
            if (networkOperator != null) {
                if (networkOperator.length() >= 4) {
                    bsInput.mcc = (short) ((short) Integer.parseInt(networkOperator.substring(0, 3)));
                    bsInput.mnc = (short) ((short) Integer.parseInt(networkOperator.substring(3)));
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        List arrayList = new ArrayList();
        try {
            List<NeighboringCellInfo> neighboringCellInfo = this.mTelephonyManager.getNeighboringCellInfo();
            if (neighboringCellInfo != null) {
                for (NeighboringCellInfo neighboringCellInfo2 : neighboringCellInfo) {
                    BsNeighborCell bsNeighborCell = new BsNeighborCell();
                    bsNeighborCell.cid = neighboringCellInfo2.getCid();
                    bsNeighborCell.lac = neighboringCellInfo2.getLac();
                    bsNeighborCell.bsss = (short) ((short) ((neighboringCellInfo2.getRssi() * 2) - 113));
                    arrayList.add(bsNeighborCell);
                }
            }
        } catch (Throwable th3) {
            th3.printStackTrace();
        }
        bsInput.neighbors = arrayList;
        return bsInput;
    }

    public void u(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.Bm = new PhoneStateListener() {
            private String aZ(int i) {
                String str = "null";
                switch (i) {
                    case 0:
                        str = "DATA_DISCONNECTED";
                        break;
                    case 1:
                        str = "DATA_CONNECTING";
                        break;
                    case 2:
                        str = "DATA_CONNECTED";
                        break;
                    case 3:
                        str = "DATA_SUSPENDED";
                        break;
                    default:
                        str = "DATA_OTHER";
                        break;
                }
                return str + "(" + i + ")";
            }

            private String ba(int i) {
                String str = "null";
                switch (i) {
                    case 0:
                        str = "NETWORK_TYPE_UNKNOWN";
                        break;
                    case 1:
                        str = "NETWORK_TYPE_GPRS";
                        break;
                    case 2:
                        str = "NETWORK_TYPE_EDGE";
                        break;
                    case 3:
                        str = "NETWORK_TYPE_UMTS";
                        break;
                    case 4:
                        str = "NETWORK_TYPE_CDMA";
                        break;
                    case 5:
                        str = "NETWORK_TYPE_EVDO_0";
                        break;
                    case 6:
                        str = "NETWORK_TYPE_EVDO_A";
                        break;
                    case 7:
                        str = "NETWORK_TYPE_1xRTT";
                        break;
                    case 8:
                        str = "NETWORK_TYPE_HSDPA";
                        break;
                    case 9:
                        str = "NETWORK_TYPE_HSUPA";
                        break;
                    case 10:
                        str = "NETWORK_TYPE_HSPA";
                        break;
                    default:
                        str = "NETWORK_TYPE_OTHER--" + i;
                        break;
                }
                return str + "(" + i + ")";
            }

            public void onCellLocationChanged(CellLocation cellLocation) {
                super.onCellLocationChanged(cellLocation);
                if (cellLocation != null) {
                    mu.this.Bo = cellLocation;
                    mu.this.fh();
                }
            }

            public void onDataConnectionStateChanged(int i, int i2) {
                super.onDataConnectionStateChanged(i, i2);
                String aZ = aZ(i);
                String ba = ba(i2);
                mu.this.Bq = i2;
                mu.this.Br = i;
                mu.this.fh();
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                if (signalStrength != null) {
                    mu.this.Bp = !signalStrength.isGsm() ? signalStrength.getCdmaDbm() : (signalStrength.getGsmSignalStrength() * 2) - 113;
                    if (mu.this.Bp == 1) {
                        return;
                    }
                }
                mu.this.fh();
            }
        };
        this.mTelephonyManager.listen(this.Bm, 336);
    }

    public void v(Context context) {
        this.mTelephonyManager.listen(this.Bm, 0);
    }
}
