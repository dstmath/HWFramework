package ohos.telephony;

import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class CellularDataInfoManager {
    public static final int DATA_FLOW_TYPE_DORMANT = 4;
    public static final int DATA_FLOW_TYPE_DOWN = 1;
    public static final int DATA_FLOW_TYPE_NONE = 0;
    public static final int DATA_FLOW_TYPE_UP = 2;
    public static final int DATA_FLOW_TYPE_UPDOWN = 3;
    public static final int DATA_STATE_CONNECTED = 2;
    public static final int DATA_STATE_CONNECTING = 1;
    public static final int DATA_STATE_DISCONNECTED = 0;
    public static final int DATA_STATE_SUSPENDED = 3;
    public static final int DATA_STATE_UNKNOWN = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "CellularDataInfoManager");
    private static volatile CellularDataInfoManager sInstance;
    private final Context mContext;
    private final TelephonyProxy mTelephonyProxy = TelephonyProxy.getInstance();

    private CellularDataInfoManager(Context context) {
        this.mContext = context;
    }

    public static CellularDataInfoManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (CellularDataInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new CellularDataInfoManager(context);
                }
            }
        }
        return sInstance;
    }

    public int getCellularDataFlowType() {
        return this.mTelephonyProxy.getCellularDataFlowType();
    }

    public boolean isCellularDataEnabled() {
        return this.mTelephonyProxy.isCellularDataEnabled(getDefaultCellularDataSlotId());
    }

    @SystemApi
    public void enableCellularData(boolean z) {
        this.mTelephonyProxy.enableCellularData(getDefaultCellularDataSlotId(), z);
    }

    public boolean isCellularDataRoamingEnabled(int i) {
        return this.mTelephonyProxy.isCellularDataRoamingEnabled(i);
    }

    @SystemApi
    public void enableCellularDataRoaming(int i, boolean z) {
        this.mTelephonyProxy.enableCellularDataRoaming(i, z);
    }

    public int getCellularDataState(int i) {
        return this.mTelephonyProxy.getCellularDataState(i);
    }

    public int getDefaultCellularDataSlotId() {
        return this.mTelephonyProxy.getDefaultCellularDataSlotId();
    }

    @SystemApi
    public void setDefaultCellularDataSlotId(int i) {
        this.mTelephonyProxy.setDefaultCellularDataSlotId(i);
    }

    public void addObserver(CellularDataStateObserver cellularDataStateObserver, int i) {
        if (cellularDataStateObserver != null && i != 0) {
            this.mTelephonyProxy.addObserver(cellularDataStateObserver.slotId, cellularDataStateObserver.callback.asObject(), getCallingPackageName(), i);
        }
    }

    public void removeObserver(CellularDataStateObserver cellularDataStateObserver) {
        if (cellularDataStateObserver != null) {
            this.mTelephonyProxy.removeObserver(cellularDataStateObserver.slotId, cellularDataStateObserver.callback.asObject(), getCallingPackageName());
        }
    }

    private String getCallingPackageName() {
        Context context = this.mContext;
        return context != null ? context.getBundleName() : "";
    }
}
