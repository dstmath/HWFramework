package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import com.android.server.wm.HwWmConstants;
import com.huawei.android.location.IHwHigeoCallback;
import com.huawei.android.location.IHwHigeoFunc;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import java.util.ArrayList;
import java.util.Map;

public class HwHigeoService extends IHwHigeoFunc.Stub {
    private static final int CELLBATCHING_CALLBACK_CHANGED = 0;
    private static final int CELLBATCHING_CALLBACK_FUSD_SERVICE_DIED = 1;
    private static final int CELLBATCHING_EVENT_FLUSH = 1;
    private static final int CELLBATCHING_EVENT_SEND_EXCEPTION = 2;
    private static final int CELLBATCHING_EVENT_SWITCH = 0;
    private static final int CELLFENCE_CALLBACK_ADD = 2;
    private static final int CELLFENCE_CALLBACK_CELLFENCE_CHANGED = 0;
    private static final int CELLFENCE_CALLBACK_FUSD_SERVICE_DIED = 4;
    private static final int CELLFENCE_CALLBACK_OPER = 3;
    private static final int CELLFENCE_CALLBACK_TRAJECTORY_CHANGED = 1;
    private static final int CELLFENCE_EVENT_ADD = 0;
    private static final int CELLFENCE_EVENT_ADD_EXT = 1;
    private static final int CELLFENCE_EVENT_CELL_TRAJECTORY_SWITCH = 4;
    private static final int CELLFENCE_EVENT_INJECT_GNSSFENCE_RESULT = 3;
    private static final int CELLFENCE_EVENT_OPERATE = 2;
    private static final int CELLFENCE_EVENT_REQUEST_TRAJECTORY = 5;
    private static final int GEOFENCE_CALLBACK_ADD_RESULT = 3;
    private static final int GEOFENCE_CALLBACK_FENCE_STATUS = 2;
    private static final int GEOFENCE_CALLBACK_FUSD_SERVICE_DIED = 5;
    private static final int GEOFENCE_CALLBACK_GET_CURRENT_LOCATION = 0;
    private static final int GEOFENCE_CALLBACK_REMOVE_RESULT = 4;
    private static final int GEOFENCE_CALLBACK_TRANSITION = 1;
    private static final int GEOFENCE_EVENT_ADD = 0;
    private static final int GEOFENCE_EVENT_ADD_HMS = 1;
    private static final int GEOFENCE_EVENT_ADD_POLYGON = 2;
    private static final int GEOFENCE_EVENT_GET_CURR_LOCATION = 4;
    private static final int GEOFENCE_EVENT_GET_GEOFENCE_STATE = 5;
    private static final int GEOFENCE_EVENT_REMOVE = 3;
    private static final int HIGEO_CALLBACKT_VEHICLE_DATA = 8;
    private static final int HIGEO_CALLBACK_AR_STAUS = 2;
    private static final int HIGEO_CALLBACK_CHR_DATA = 5;
    private static final int HIGEO_CALLBACK_DOWNLOAD = 7;
    private static final int HIGEO_CALLBACK_EXCEPTION = 6;
    private static final int HIGEO_CALLBACK_GPS_STATE = 1;
    private static final int HIGEO_CALLBACK_OFFLINE_DB = 9;
    private static final int HIGEO_CALLBACK_QUICK = 3;
    private static final int HIGEO_CALLBACK_REQUEST_GPS = 11;
    private static final int HIGEO_CALLBACK_WIFI_COMMAND = 0;
    private static final int HIGEO_CALLBACK_WIFI_DB = 10;
    private static final int HIGEO_CALLBACK_WIFI_SCAN = 4;
    private static final int HIGEO_EVENT_BAT_STATE = 13;
    private static final int HIGEO_EVENT_CITY_CODE = 7;
    private static final int HIGEO_EVENT_CLEAN_STATE = 3;
    private static final int HIGEO_EVENT_GNSS_MODE = 5;
    private static final int HIGEO_EVENT_INJECT = 9;
    private static final int HIGEO_EVENT_MCC_CODE = 6;
    private static final int HIGEO_EVENT_MM_RESULT = 12;
    private static final int HIGEO_EVENT_NAVIGATING_STATE = 2;
    private static final int HIGEO_EVENT_NETWORK_STATE = 1;
    private static final int HIGEO_EVENT_OFFLINE_DB = 14;
    private static final int HIGEO_EVENT_POWER_INFO = 8;
    private static final int HIGEO_EVENT_SCREEN_STATE = 0;
    private static final int HIGEO_EVENT_SEND_QTTFF = 10;
    private static final int HIGEO_EVENT_SEND_WIFI = 11;
    private static final int HIGEO_EVENT_SOURCE_STATE = 4;
    private static final int HIGEO_EVENT_TYPE_MM = 0;
    private static final int HIGEO_EVENT_TYPE_PGNSS = 2;
    private static final int HIGEO_EVENT_TYPE_SDM = 1;
    private static final int HIGEO_EVENT_TYPE_VEHICEL = 3;
    private static final int HIGEO_EVENT_WIFI_DB = 15;
    private static final String ID = "id";
    private static final String KEY_COMMAND = "command";
    private static final String KEY_DATA = "data";
    private static final String KEY_EXCEPTION_TYPE = "exceptiontype";
    private static final String KEY_GEOFENCEID = "geofenceId";
    private static final String KEY_ID = "id";
    private static final String KEY_IDS = "ids";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LOCSTATUS = "locSource";
    private static final String KEY_PENDINGINTENT = "pengdingintent";
    private static final String KEY_POSUNC = "posUnc";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_REQUEST = "request";
    private static final String KEY_RESULT = "result";
    private static final String KEY_STATUS = "status";
    private static final String KEY_TILE_ID = "tileId";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_TIMEUNC = "timeUnc";
    private static final String KEY_TRANSITION = "transition";
    private static final String KEY_TYPE = "type";
    private static final int MM_CALLBACK = 0;
    private static final String OPER_ID = "operId";
    private static final int PDM_CALLBACK = 1;
    private static final int REQUEST_CELL_DB_TYPE = 2;
    private static final int REQUEST_WIFI_DB_TYPE = 3;
    private static final int SDM_CALLBACK = 2;
    private static final int SDM_REP_CALLBACK = 3;
    private static final String TAG = "HwHigeoService";
    private static final int WIFIFENCE_CALLBACK_ADD = 0;
    private static final int WIFIFENCE_CALLBACK_FUSD_SERVICE_DIED = 6;
    private static final int WIFIFENCE_CALLBACK_PAUSE = 4;
    private static final int WIFIFENCE_CALLBACK_REMOVE = 1;
    private static final int WIFIFENCE_CALLBACK_RESUME = 5;
    private static final int WIFIFENCE_CALLBACK_STATUS = 3;
    private static final int WIFIFENCE_CALLBACK_TRANSITION = 2;
    private static final int WIFIFENCE_EVENT_ADD = 0;
    private static final int WIFIFENCE_EVENT_GET_STATE = 4;
    private static final int WIFIFENCE_EVENT_PAUSE = 2;
    private static final int WIFIFENCE_EVENT_REMOVE = 1;
    private static final int WIFIFENCE_EVENT_RESUME = 3;
    private static CellBatchingHardware sCellBatchingHardware;
    private static HiCellFenceHardware sHiCellFenceHardware;
    private static HiGeofenceHardware sHiGeofenceHardware;
    private static HiWifiFenceHardware sHiWifiFenceHardware;
    private static HigeoHardware sHigeoHardware;
    private static MmHardware sMmHardware;
    private static PgnssHardware sPgnssHardware;
    private static VehicleHardware sVehicleHardware;
    private Context mContext;
    private IHwHigeoCallback mHigeoCallback;

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.location.HwHigeoService */
    /* JADX WARN: Multi-variable type inference failed */
    public HwHigeoService(Context context) {
        this.mContext = context;
        sMmHardware = MmHardware.getInstance(this.mContext, new MapMatchingMmCallback(), new MapMatchingSdmCallback());
        sHiGeofenceHardware = new HiGeofenceHardware(new HwGeoFenceCallback());
        sCellBatchingHardware = new CellBatchingHardware(new HwCellBatchingCallback());
        sHiWifiFenceHardware = new HiWifiFenceHardware(new HwWifiFenceCallback());
        sHiCellFenceHardware = new HiCellFenceHardware(new HwCellFenceCallback());
        sHigeoHardware = HigeoHardware.getInstance(this.mContext, new HwHigeoCallback());
        sPgnssHardware = PgnssHardware.getInstance(this.mContext, new HwPgnssDataCallback());
        sVehicleHardware = VehicleHardware.getInstance(this.mContext, new HwVehicleCallback());
        ServiceManagerEx.addService("higeo_service", this);
        LBSLog.i(TAG, "HwHigeoService init completed");
    }

    public HiGeofenceHardware getGeofenceHardware() {
        return sHiGeofenceHardware;
    }

    class MapMatchingMmCallback implements MmCallback {
        MapMatchingMmCallback() {
        }

        @Override // com.android.server.location.MmCallback
        public void onMmDataRequest(double latitude, double longitude, int size) {
            LBSLog.i(HwHigeoService.TAG, false, "onMmDataRequest", new Object[0]);
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putDouble("lat", latitude);
                    bundle.putDouble("lon", longitude);
                    bundle.putInt("size", size);
                    HwHigeoService.this.mHigeoCallback.onMmDataRequest(0, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onMmDataRequest fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.MmCallback
        public void onPdrMmDataRequest(double latitude, double longitude, int size) {
            LBSLog.i(HwHigeoService.TAG, false, "onPDRMmDataRequest", new Object[0]);
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putDouble("lat", latitude);
                    bundle.putDouble("lon", longitude);
                    bundle.putInt("size", size);
                    HwHigeoService.this.mHigeoCallback.onMmDataRequest(1, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onPDRMmDataRequest fail", new Object[0]);
                }
            }
        }
    }

    class MapMatchingSdmCallback implements SdmCallback {
        MapMatchingSdmCallback() {
        }

        @Override // com.android.server.location.SdmCallback
        public void onSdmDataRequest(long tileId, int reserved) {
            LBSLog.i(HwHigeoService.TAG, false, "onSdmDataRequest , tileId = %{public}l", Long.valueOf(tileId));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putLong(HwHigeoService.KEY_TILE_ID, tileId);
                    bundle.putInt("reserved", reserved);
                    HwHigeoService.this.mHigeoCallback.onMmDataRequest(2, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onSdmDataRequest fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.SdmCallback
        public void onAckSdmDataResponse(long tileId, int sec) {
            LBSLog.i(HwHigeoService.TAG, false, "onAckSdmDataResponse , tileId = %{public}l", Long.valueOf(tileId));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putLong(HwHigeoService.KEY_TILE_ID, tileId);
                    bundle.putInt("sec", sec);
                    HwHigeoService.this.mHigeoCallback.onMmDataRequest(3, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onAckSdmDataResponse fail", new Object[0]);
                }
            }
        }
    }

    class HwPgnssDataCallback implements PgnssDataCallback {
        HwPgnssDataCallback() {
        }

        @Override // com.android.server.location.PgnssDataCallback
        public void downloadRequestCb() {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(7, new Bundle());
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "downloadRequestCb fail", new Object[0]);
                }
            }
        }
    }

    class HwVehicleCallback implements VehicleCallback {
        HwVehicleCallback() {
        }

        @Override // com.android.server.location.VehicleCallback
        public void sendMsgToVehCallback(String message) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", message);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(8, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "sendMsgToVehCallback fail", new Object[0]);
                }
            }
        }
    }

    class HwHigeoCallback implements HigeoCallback {
        HwHigeoCallback() {
        }

        @Override // com.android.server.location.HigeoCallback
        public void wifiCommandCallback(int command) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt(HwHigeoService.KEY_COMMAND, command);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(0, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "wifiCommandCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void gpsStateCallback(int state) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("state", state);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(1, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "gpsStateCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void arStatusCallback(long timeStamp, int eventType, int activity, int arSource) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putLong("timeStamp", timeStamp);
                    bundle.putInt(AwarenessConstants.DATA_EVENT_TYPE, eventType);
                    bundle.putInt("activity", activity);
                    bundle.putInt("arSource", arSource);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(2, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "arStatusCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void quickTtffCommandCallback(int command) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt(HwHigeoService.KEY_COMMAND, command);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(3, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "quickTtffCommandCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void wlanScanCommandCallback(int command, int inteval, int amount) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt(HwHigeoService.KEY_COMMAND, command);
                    bundle.putInt("inteval", inteval);
                    bundle.putInt("amount", amount);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(4, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "quickTtffCommandCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void setChrDataCallback(int eventId, int subEventId, String content) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("eventId", eventId);
                    bundle.putInt("subEventId", subEventId);
                    bundle.putString("content", content);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(5, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "setChrDataCallback fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void onSendExcept2Lbs(int type) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", type);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(6, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onSendExcept2Lbs fail", new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void onRequestCellDb(Bundle bundle) {
            if (HwHigeoService.this.mHigeoCallback == null) {
                return;
            }
            if (bundle == null) {
                try {
                    LBSLog.e(HwHigeoService.TAG, false, "bundle is null", new Object[0]);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onRequestCellDb fail", new Object[0]);
                }
            } else {
                int typeFlag = bundle.getInt(HwWmConstants.FLAG_STR);
                LBSLog.i(HwHigeoService.TAG, false, "typeFlag = " + typeFlag, new Object[0]);
                if (typeFlag == 2) {
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(9, bundle);
                } else if (typeFlag == 3) {
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(10, bundle);
                } else {
                    LBSLog.i(HwHigeoService.TAG, false, "not one typeFlag = " + typeFlag, new Object[0]);
                }
            }
        }

        @Override // com.android.server.location.HigeoCallback
        public void onGpsIntervalChangeCb(Bundle bundle) {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    LBSLog.i(HwHigeoService.TAG, false, "onGpsIntervalChangeCb start", new Object[0]);
                    HwHigeoService.this.mHigeoCallback.onHigeoEventCallback(11, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, false, "onGpsIntervalChangeCb fail", new Object[0]);
                }
            }
        }
    }

    class HwCellBatchingCallback implements CellBatchingCallback {
        HwCellBatchingCallback() {
        }

        @Override // com.android.server.location.CellBatchingCallback
        public void onCellBatchingChanged(ArrayList<Map<String, String>> clientResults) {
            LBSLog.i(HwHigeoService.TAG, "onCellBatchingChanged!");
            if (clientResults == null || clientResults.size() == 0) {
                LBSLog.e(HwHigeoService.TAG, "clientResults is null!");
            } else if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("CellTrajectoryData", clientResults);
                    HwHigeoService.this.mHigeoCallback.onCellBatchingCallback(0, bundle);
                    LBSLog.i(HwHigeoService.TAG, "send MSG_CELLBATCHING_CHANGED.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "geofenceAddResultCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.CellBatchingCallback
        public void onFusedLbsServiceDied() {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onCellBatchingCallback(1, new Bundle());
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onFusedLbsServiceDied fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }
    }

    class HwWifiFenceCallback implements WifiFenceCallback {
        HwWifiFenceCallback() {
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififenceAddCb(int operId, int result) {
            LBSLog.i(HwHigeoService.TAG, false, "onAddWififenceCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", operId);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(0, bundle);
                    LBSLog.d(HwHigeoService.TAG, "onAddWififenceCb end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onWififenceAddCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififenceRemoveCb(int operId, int result) {
            LBSLog.i(HwHigeoService.TAG, false, "onRemoveWififenceCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", operId);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(1, bundle);
                    LBSLog.d(HwHigeoService.TAG, "onWififenceRemoveCb end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onWififenceRemoveCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififenceTransitionCb(int wififenceId, Location location, int transition, long timestamp) {
            LBSLog.i(HwHigeoService.TAG, "onWififenceTransitionCb!");
            if (location != null) {
                if (HwHigeoService.this.mHigeoCallback != null) {
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(HwHigeoService.KEY_LOCATION, location);
                        bundle.putLong(HwHigeoService.KEY_TIMESTAMP, timestamp);
                        bundle.putInt("wififenceId", wififenceId);
                        bundle.putInt(HwHigeoService.KEY_TRANSITION, transition);
                        HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(2, bundle);
                        LBSLog.i(HwHigeoService.TAG, "onWififenceTransitionCb end.");
                    } catch (RemoteException e) {
                        LBSLog.e(HwHigeoService.TAG, "onWififenceTransitionCb fail");
                    }
                } else {
                    LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
                }
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififenceStatusCb(int status, Location location) {
            LBSLog.i(HwHigeoService.TAG, "onWififenceStatusCb!");
            if (location != null) {
                if (HwHigeoService.this.mHigeoCallback != null) {
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", status);
                        bundle.putParcelable(HwHigeoService.KEY_LOCATION, location);
                        HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(3, bundle);
                        LBSLog.i(HwHigeoService.TAG, "onWififenceStatusCb end.");
                    } catch (RemoteException e) {
                        LBSLog.e(HwHigeoService.TAG, "onWifiFenceCallback fail");
                    }
                } else {
                    LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
                }
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififencePauseCb(int operId, int result) {
            LBSLog.i(HwHigeoService.TAG, false, "onWififencePauseCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", operId);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(4, bundle);
                    LBSLog.i(HwHigeoService.TAG, "onWififencePauseCb end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onWifiFenceCallback fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onWififenceResumeCb(int operId, int result) {
            LBSLog.d(HwHigeoService.TAG, false, "onWififenceResumeCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", operId);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(5, bundle);
                    LBSLog.i(HwHigeoService.TAG, "onWififenceResumeCb end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onWifiFenceCallback fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.WifiFenceCallback
        public void onFusedLbsServiceDied() {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onWifiFenceCallback(6, new Bundle());
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onFusedLbsServiceDied fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }
    }

    class HwCellFenceCallback implements CellFenceCallback {
        HwCellFenceCallback() {
        }

        @Override // com.android.server.location.CellFenceCallback
        public void onCellFenceChanged(ArrayList<Map<String, String>> fenceStatusList) {
            LBSLog.i(HwHigeoService.TAG, "onCellFenceChanged!");
            if (fenceStatusList == null || fenceStatusList.size() == 0) {
                LBSLog.e(HwHigeoService.TAG, "fenceStatusList is null!");
            } else if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("fences", fenceStatusList);
                    HwHigeoService.this.mHigeoCallback.onCellFenceCallback(0, bundle);
                    LBSLog.i(HwHigeoService.TAG, "onCellFenceChanged end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onCellFenceChanged fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.CellFenceCallback
        public void onCellTrajectoryChanged(ArrayList<Map<String, String>> cellList) {
            LBSLog.i(HwHigeoService.TAG, "onCellTrajectoryChanged!");
            if (cellList == null || cellList.size() == 0) {
                LBSLog.e(HwHigeoService.TAG, "cellList is null!");
            } else if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(HwHigeoService.KEY_DATA, cellList);
                    HwHigeoService.this.mHigeoCallback.onCellFenceCallback(1, bundle);
                    LBSLog.i(HwHigeoService.TAG, "onCellTrajectoryChanged end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onCellTrajectoryChanged fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.CellFenceCallback
        public void onCellfenceAdd(int id, int result) {
            LBSLog.i(HwHigeoService.TAG, false, "onCellfenceAdd, id = %{public}d, result = %{public}d", Integer.valueOf(id), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onCellFenceCallback(2, bundle);
                    LBSLog.d(HwHigeoService.TAG, "onCellfenceAdd end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onCellfenceAdd fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.CellFenceCallback
        public void onCellfenceOper(int id, int result) {
            LBSLog.i(HwHigeoService.TAG, false, "onCellfenceOper, id = %{public}d, result = %{public}d", Integer.valueOf(id), Integer.valueOf(result));
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    bundle.putInt(HwHigeoService.KEY_RESULT, result);
                    HwHigeoService.this.mHigeoCallback.onCellFenceCallback(3, bundle);
                    LBSLog.i(HwHigeoService.TAG, "onCellfenceOper end.");
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onCellfenceOper fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.CellFenceCallback
        public void onFusedLbsServiceDied() {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onCellFenceCallback(4, new Bundle());
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onFusedLbsServiceDied fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }
    }

    class HwGeoFenceCallback implements GeoFenceCallback {
        HwGeoFenceCallback() {
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void onGetCurrentLocationCb(Bundle bundle) {
            LBSLog.i(HwHigeoService.TAG, "onGetCurrentLocationCb");
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(0, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onGetCurrentLocationCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void gnssGeofenceTransitionCb(Bundle bundle) {
            LBSLog.i(HwHigeoService.TAG, "gnssGeofenceTransitionCb!");
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(1, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "gnssGeofenceTransitionCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void gnssGeofenceStatusCb(Bundle bundle) {
            LBSLog.i(HwHigeoService.TAG, "gnssGeofenceStatusCb!");
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(2, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "gnssGeofenceStatusCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void geofenceAddResultCb(Bundle bundle) {
            LBSLog.i(HwHigeoService.TAG, "geofenceAddResultCb");
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(3, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "geofenceAddResultCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void geofenceRemoveResultCb(Bundle bundle) {
            LBSLog.i(HwHigeoService.TAG, "gnssGeofenceRemoveCb!");
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(4, bundle);
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "geofenceRemoveResultCb fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void gnssGeofencePauseCb(Bundle bundle) {
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void gnssGeofenceResumeCb(Bundle bundle) {
        }

        @Override // com.android.server.location.GeoFenceCallback
        public void onFusedLbsServiceDied() {
            if (HwHigeoService.this.mHigeoCallback != null) {
                try {
                    HwHigeoService.this.mHigeoCallback.onGeoFenceCallback(5, new Bundle());
                } catch (RemoteException e) {
                    LBSLog.e(HwHigeoService.TAG, "onFusedLbsServiceDied fail");
                }
            } else {
                LBSLog.e(HwHigeoService.TAG, "mHigeoCallback is null");
            }
        }
    }

    public boolean sendMmData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendMmData type = %{public}d", Integer.valueOf(type));
        if (bundle == null) {
            LBSLog.e(TAG, false, "param is null", new Object[0]);
            return false;
        } else if (type == 0) {
            return sMmHardware.sendMmData(bundle.getByteArray(KEY_DATA), bundle.getInt("type"));
        } else if (type == 1) {
            return sMmHardware.sendSDMData(bundle.getString(KEY_TILE_ID), bundle.getByteArray(KEY_DATA), bundle.getInt("bufLen"), bundle.getInt("totalLen"));
        } else {
            if (type == 2) {
                return sPgnssHardware.sendEeData(bundle.getString("eEData"));
            }
            if (type == 3) {
                return sVehicleHardware.sendVehicleData(bundle.getString("nmea"));
            }
            LBSLog.e(TAG, false, "data from HIGEO_EVENT_TYPE_MAX_ID error", new Object[0]);
            return false;
        }
    }

    public boolean sendHigeoData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendHigeoData type = %{public}d", Integer.valueOf(type));
        HigeoHardware higeoHardware = sHigeoHardware;
        if (higeoHardware == null || bundle == null) {
            return false;
        }
        switch (type) {
            case 0:
                return sHigeoHardware.sendScreenState(bundle.getInt("screenState"));
            case 1:
                return sHigeoHardware.sendNetworkRoamingState(bundle.getInt("roamingState"));
            case 2:
                return sHigeoHardware.sendMapNavigatingState(bundle.getInt("navigatingState"));
            case 3:
                return sHigeoHardware.sendCleanUpHifence(bundle.getInt("cleanState"));
            case 4:
                return sHigeoHardware.sendLocationSourceStatus(bundle.getInt("sourceType"), bundle.getInt("sourceStatus"));
            case 5:
                return sHigeoHardware.sendRtkGnssMode(bundle.getInt("gnssMode"));
            case 6:
                return sHigeoHardware.sendMccCode(bundle.getInt("mccCode"));
            case 7:
                return sHigeoHardware.sendCityCode(bundle.getInt("cityCode"));
            case 8:
                int powerType = bundle.getInt("powerType");
                int[] modeDatas = null;
                try {
                    modeDatas = bundle.getIntArray("modeData");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
                }
                return sHigeoHardware.sendPowerInfoToHdGnss(powerType, modeDatas);
            case 9:
                return sHigeoHardware.injectLocation(bundle.getInt("source"), (Location) bundle.getParcelable("injectLocation"));
            case 10:
                return higeoHardware.sendQuickLocation(bundle);
            case 11:
                return higeoHardware.sendHereWifiLocation(bundle);
            case 12:
                return higeoHardware.sendMapMatchingResult(bundle);
            case 13:
                return sHigeoHardware.sendBatteryState(bundle.getInt("state"));
            case 14:
                return higeoHardware.sendOfflineDb(bundle);
            case 15:
                return higeoHardware.sendOfflineDb(bundle);
            default:
                LBSLog.w(TAG, false, "sengHigeoData max event id = %{public}d", Integer.valueOf(type));
                return false;
        }
    }

    public boolean sendCellBatchingData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendCellBatchingData type = %{public}d", Integer.valueOf(type));
        CellBatchingHardware cellBatchingHardware = sCellBatchingHardware;
        if (cellBatchingHardware == null || bundle == null) {
            return false;
        }
        if (type == 0) {
            return sCellBatchingHardware.cellBatchingSwitch(bundle.getInt("status"), bundle.getInt("interval"), bundle.getInt("reserved1"), bundle.getInt("reserved2"));
        }
        if (type == 1) {
            return cellBatchingHardware.flushCellBatching();
        }
        if (type == 2) {
            return sCellBatchingHardware.sendException2Cellbatching(bundle.getInt("interval"), bundle.getInt("status"));
        }
        LBSLog.w(TAG, false, "sendCellBatchingData max event id = %{public}d", Integer.valueOf(type));
        return false;
    }

    public int sendWifiFenceData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendWifiFenceData type = %{public}d", Integer.valueOf(type));
        if (sHiWifiFenceHardware == null || bundle == null) {
            return 0;
        }
        if (type == 0) {
            return sHiWifiFenceHardware.addWififence(bundle.getInt("id"), getSerialListFromBundle(bundle, KEY_REQUEST)) ? 1 : 0;
        }
        if (type == 1) {
            int operIdRemove = bundle.getInt(OPER_ID);
            int[] removedIds = null;
            try {
                removedIds = bundle.getIntArray(KEY_IDS);
            } catch (ArrayIndexOutOfBoundsException e) {
                LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
            }
            return sHiWifiFenceHardware.removeWififence(operIdRemove, removedIds) ? 1 : 0;
        } else if (type == 2) {
            int operIdPause = bundle.getInt(OPER_ID);
            int[] pausedIds = null;
            try {
                pausedIds = bundle.getIntArray(KEY_IDS);
            } catch (ArrayIndexOutOfBoundsException e2) {
                LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
            }
            return sHiWifiFenceHardware.pauseWififence(operIdPause, pausedIds) ? 1 : 0;
        } else if (type == 3) {
            return sendResumeWififence(bundle) ? 1 : 0;
        } else {
            if (type == 4) {
                return sHiWifiFenceHardware.getWififenceState(bundle.getInt("wififenceId"));
            }
            LBSLog.w(TAG, false, "sendWifiFenceData max event id = %{public}d", Integer.valueOf(type));
            return 0;
        }
    }

    private boolean sendResumeWififence(Bundle bundle) {
        int operIdResume = bundle.getInt(OPER_ID);
        int[] resumedIds = null;
        try {
            resumedIds = bundle.getIntArray(KEY_IDS);
        } catch (ArrayIndexOutOfBoundsException e) {
            LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
        }
        return sHiWifiFenceHardware.resumeWififence(operIdResume, resumedIds);
    }

    public boolean sendCellFenceData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendCellFenceData type = %{public}d", Integer.valueOf(type));
        HiCellFenceHardware hiCellFenceHardware = sHiCellFenceHardware;
        if (hiCellFenceHardware == null || bundle == null) {
            return false;
        }
        if (type == 0) {
            return sHiCellFenceHardware.addCellFence(bundle.getInt("id"), getSerialListFromBundle(bundle, KEY_REQUEST));
        }
        if (type == 1) {
            int idAddExt = bundle.getInt("id");
            ArrayList<Map<String, Object>> addExtRequests = getSerialListFromBundle(bundle, KEY_REQUEST);
            if (addExtRequests != null) {
                LBSLog.i(TAG, false, "addCellFenceExt = %{public}d", Integer.valueOf(addExtRequests.size()));
            }
            return sHiCellFenceHardware.addCellFenceExt(idAddExt, addExtRequests);
        } else if (type == 2) {
            return sHiCellFenceHardware.operateCellfence(bundle.getInt("id"), bundle.getInt("fidOrType"), getSerialListIntegerFromBundle(bundle, KEY_IDS));
        } else {
            if (type == 3) {
                return sHiCellFenceHardware.injectGnssFenceResult(getSerialListIntFromBundle(bundle, KEY_REQUEST));
            }
            if (type == 4) {
                return sHiCellFenceHardware.cellTrajectorySwitch(bundle.getInt("status"));
            }
            if (type == 5) {
                return hiCellFenceHardware.requestTrajectory();
            }
            LBSLog.w(TAG, false, "sendCellFenceData max event id = %{public}d", Integer.valueOf(type));
            return false;
        }
    }

    public int sendGeoFenceData(int type, Bundle bundle) {
        LBSLog.i(TAG, false, "sendGeoFenceData type = %{public}d", Integer.valueOf(type));
        HiGeofenceHardware hiGeofenceHardware = sHiGeofenceHardware;
        if (hiGeofenceHardware == null || bundle == null) {
            return 0;
        }
        if (type == 0) {
            int[] addIds = null;
            try {
                addIds = bundle.getIntArray(KEY_IDS);
            } catch (ArrayIndexOutOfBoundsException e) {
                LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
            }
            return sHiGeofenceHardware.addGeofence(addIds, getSerialListFromBundle(bundle, KEY_REQUEST)) ? 1 : 0;
        } else if (type == 1) {
            return sendAddHmsGeofence(bundle) ? 1 : 0;
        } else {
            if (type == 2) {
                return sendAddGeofencePolygon(bundle) ? 1 : 0;
            }
            if (type == 3) {
                int[] removeIds = null;
                try {
                    removeIds = bundle.getIntArray(KEY_IDS);
                } catch (ArrayIndexOutOfBoundsException e2) {
                    LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
                }
                return sHiGeofenceHardware.removeGeofence(removeIds) ? 1 : 0;
            } else if (type == 4) {
                return hiGeofenceHardware.getCurrentLocation() ? 1 : 0;
            } else {
                if (type == 5) {
                    return sHiGeofenceHardware.getGeofenceState(bundle.getInt("id"));
                }
                LBSLog.w(TAG, false, "sendGeoFenceData max event id = %{public}d", Integer.valueOf(type));
                return 0;
            }
        }
    }

    private boolean sendAddHmsGeofence(Bundle bundle) {
        int[] addHmsIds = null;
        try {
            addHmsIds = bundle.getIntArray(KEY_IDS);
        } catch (ArrayIndexOutOfBoundsException e) {
            LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
        }
        return sHiGeofenceHardware.addHmsGeofence(addHmsIds, getSerialListFromBundle(bundle, KEY_REQUEST));
    }

    private boolean sendAddGeofencePolygon(Bundle bundle) {
        int[] polygonIds = null;
        try {
            polygonIds = bundle.getIntArray(KEY_IDS);
        } catch (ArrayIndexOutOfBoundsException e) {
            LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
        }
        return sHiGeofenceHardware.addGeofencePolygon(polygonIds, getSerialListFromBundle(bundle, KEY_REQUEST));
    }

    private ArrayList<Map<String, Object>> getSerialListFromBundle(Bundle bundle, String key) {
        Object serializableObj;
        ArrayList<Map<String, Object>> serializableArrayList = new ArrayList<>();
        if (bundle == null || (serializableObj = bundle.getSerializable(key)) == null || !(serializableObj instanceof ArrayList)) {
            return serializableArrayList;
        }
        return (ArrayList) serializableObj;
    }

    private ArrayList<Map<String, Integer>> getSerialListIntFromBundle(Bundle bundle, String key) {
        Object serializableObj;
        ArrayList<Map<String, Integer>> serializableArrayList = new ArrayList<>();
        if (bundle == null || (serializableObj = bundle.getSerializable(key)) == null || !(serializableObj instanceof ArrayList)) {
            return serializableArrayList;
        }
        return (ArrayList) serializableObj;
    }

    private ArrayList<Integer> getSerialListIntegerFromBundle(Bundle bundle, String key) {
        ArrayList<Integer> serializableArrayList = new ArrayList<>();
        Object serializableObj = bundle.getSerializable(key);
        if (serializableObj == null || !(serializableObj instanceof ArrayList)) {
            return serializableArrayList;
        }
        return (ArrayList) serializableObj;
    }

    public boolean registerHigeoCallback(IHwHigeoCallback higeoCallback) {
        LBSLog.i(TAG, false, "higeoserve registerHigeoCallback", new Object[0]);
        boolean isSuccess = true;
        if (higeoCallback == null) {
            isSuccess = false;
        }
        this.mHigeoCallback = higeoCallback;
        return isSuccess;
    }
}
