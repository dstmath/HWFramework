package com.android.server.location;

import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceInterface;
import vendor.huawei.hardware.fusd.V1_2.IFusdLbs;

public class HiGeofenceHardware {
    private static final String ACCURACY = "accuracy";
    private static final String ADD_GEOFENCE_ERROR = "add geofence error.";
    private static final int DEFAULT_SIZE = 16;
    private static final String DEGREE_DEFAULT_VALUE = "0";
    private static final String FLOOR = "floor";
    private static final String GEOFENCE_INTERFACE_NULL = "mGeofenceInterface is null.";
    private static final int LAST_TRANSITION = 4;
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String MONITOR_TRANSITIONS = "monitorTransitions";
    private static final int MSG_FUSIONDEAMON_DIED = 9;
    private static final int NOTIFICATION_RESPONSIVENESS_IMMEDIATELY = -1;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 60000;
    private static final String PARENT_FENCE_ID = "parentFenceId";
    private static final String SUBFENCE = "subfence";
    private static final String TAG = "HiFence.HiGeofenceHardware";
    private static final int UNKNOWN_TIMER_MS = -1;
    private static final Object WATCHER_LOCK = new Object();
    private GeoFenceCallback mGeoFenceCallback;
    private IFusdGeofenceInterface mGeofenceInterface;
    private IFusdLbs mIFusdLbs;

    public HiGeofenceHardware(GeoFenceCallback geoFenceCallback) {
        this.mGeoFenceCallback = geoFenceCallback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.HiGeofenceHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
                synchronized (HiGeofenceHardware.WATCHER_LOCK) {
                    HiGeofenceHardware.this.mIFusdLbs = null;
                }
                if (HiGeofenceHardware.this.mGeoFenceCallback != null) {
                    HiGeofenceHardware.this.mGeoFenceCallback.onFusedLbsServiceDied();
                }
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                LBSLog.i(HiGeofenceHardware.TAG, "onFusedLbsServiceConnect333");
                HiGeofenceHardware.this.getIFusdLbsService();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mIFusdLbs == null) {
            LBSLog.i(TAG, "getIFusdLbsService(),mIFusdLbs = null");
            this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            if (this.mIFusdLbs == null) {
                LBSLog.i(TAG, "getIFusdLbsService(),V1_3,mIFusdLbs is null");
                this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
            }
            if (this.mIFusdLbs != null) {
                try {
                    this.mGeofenceInterface = this.mIFusdLbs.getFusdGeofenceInterface();
                } catch (RemoteException e) {
                    LBSLog.e(TAG, "register callback error");
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception setGeofenceCallback", new Object[0]);
                }
            }
        }
    }

    private ArrayList<IFusdGeofenceInterface.GeofencePolygonRequest> getHidlPolygonRequest(int[] ids, ArrayList<Map<String, Object>> geofences) {
        ArrayList<IFusdGeofenceInterface.GeofencePolygonRequest> hidlRequests = new ArrayList<>();
        int size = geofences.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> requests = geofences.get(i);
            IFusdGeofenceInterface.GeofencePolygonRequest hidlRequest = new IFusdGeofenceInterface.GeofencePolygonRequest();
            hidlRequest.geofenceId = ids[i];
            ArrayList<Map<String, Object>> points = new ArrayList<>();
            Object pointsObj = requests.get("points");
            if (pointsObj != null && (pointsObj instanceof ArrayList)) {
                points = (ArrayList) pointsObj;
            }
            for (int j = 0; j < points.size(); j++) {
                IFusdGeofenceInterface.Point point = new IFusdGeofenceInterface.Point();
                giveLatAndLongValuesToPoint(point, points.get(j));
                hidlRequest.points.add(point);
            }
            int requestAccuracy = Integer.parseInt(requests.get(ACCURACY).toString());
            giveAccuracyAndNotifyResponsivenessForPolygon(hidlRequest, requestAccuracy);
            hidlRequest.notificationResponsivenessMs = 60000;
            hidlRequest.unknownTimerMs = -1;
            hidlRequest.monitorTransitions = Integer.parseInt(requests.get(MONITOR_TRANSITIONS).toString());
            hidlRequest.lastTransition = 4;
            int requestParentFenceId = Integer.parseInt(requests.get(PARENT_FENCE_ID).toString());
            hidlRequest.parentFenceId = requestParentFenceId;
            hidlRequest.floor = Integer.parseInt(requests.get(FLOOR).toString());
            hidlRequest.reportType = 0;
            giveReportType(hidlRequest, requestAccuracy);
            if (requestParentFenceId == 0) {
                if (Integer.parseInt(requests.get(SUBFENCE).toString()) == 1) {
                    hidlRequest.reportType |= 8;
                }
            } else if (requestParentFenceId > 0) {
                hidlRequest.reportType = 4 | hidlRequest.reportType;
            } else {
                LBSLog.i(TAG, "requestParentFenceId < 0");
            }
            hidlRequests.add(hidlRequest);
        }
        return hidlRequests;
    }

    private void giveReportType(IFusdGeofenceInterface.GeofencePolygonRequest hidlRequest, int requestAccuracy) {
        if (requestAccuracy == 1) {
            hidlRequest.reportType = 1 | hidlRequest.reportType;
        } else {
            hidlRequest.reportType |= 2;
        }
    }

    private void giveLatAndLongValuesToPoint(IFusdGeofenceInterface.Point point, Map<String, Object> pointM) {
        Object latitudeObj = pointM.get(LATITUDE);
        String longitudeStr = "0";
        point.latitudeDegrees = Double.parseDouble(latitudeObj == null ? longitudeStr : latitudeObj.toString());
        Object longitudeObj = pointM.get(LONGITUDE);
        if (longitudeObj != null) {
            longitudeStr = longitudeObj.toString();
        }
        point.longitudeDegrees = Double.parseDouble(longitudeStr);
    }

    private ArrayList<IFusdGeofenceInterface.GeofenceRequest> getHidlRequest(int[] ids, ArrayList<Map<String, Object>> geofences) {
        ArrayList<IFusdGeofenceInterface.GeofenceRequest> hidlRequests = new ArrayList<>();
        int size = geofences.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> requests = geofences.get(i);
            IFusdGeofenceInterface.GeofenceRequest hidlRequest = new IFusdGeofenceInterface.GeofenceRequest();
            hidlRequest.geofenceId = ids[i];
            giveLatAndLongValues(hidlRequest, requests);
            int requestAccuracy = Integer.parseInt(requests.get(ACCURACY).toString());
            giveAccuracyAndNotifyResponsiveness(hidlRequest, requestAccuracy);
            hidlRequest.radius = Double.parseDouble(requests.get("radius").toString());
            hidlRequest.unknownTimerMs = -1;
            hidlRequest.monitorTransitions = Integer.parseInt(requests.get(MONITOR_TRANSITIONS).toString());
            hidlRequest.lastTransition = 4;
            int requestParentFenceId = Integer.parseInt(requests.get(PARENT_FENCE_ID).toString());
            hidlRequest.parentFenceId = requestParentFenceId;
            hidlRequest.floor = Integer.parseInt(requests.get(FLOOR).toString());
            hidlRequest.reportType = 0;
            if (requestAccuracy == 1) {
                hidlRequest.reportType |= 1;
            } else {
                hidlRequest.reportType |= 2;
            }
            if (requestParentFenceId == 0) {
                if (Integer.parseInt(requests.get(SUBFENCE).toString()) == 1) {
                    hidlRequest.reportType |= 8;
                }
            } else if (requestParentFenceId > 0) {
                hidlRequest.reportType = 4 | hidlRequest.reportType;
            } else {
                LBSLog.i(TAG, "requestParentFenceId<0");
            }
            hidlRequests.add(hidlRequest);
        }
        return hidlRequests;
    }

    private ArrayList<IFusdGeofenceInterface.GeofenceRequest> getHidlHmsRequest(int[] ids, ArrayList<Map<String, Object>> geofences) {
        int i;
        ArrayList<IFusdGeofenceInterface.GeofenceRequest> hidlRequests = new ArrayList<>(1);
        int size = geofences.size();
        for (int i2 = 0; i2 < size; i2++) {
            Map<String, Object> requests = geofences.get(i2);
            IFusdGeofenceInterface.GeofenceRequest hidlRequest = new IFusdGeofenceInterface.GeofenceRequest();
            hidlRequest.geofenceId = ids[i2];
            giveLatAndLongValues(hidlRequest, requests);
            int requestAccuracy = Integer.parseInt(requests.get(ACCURACY).toString());
            giveAccuracyAndNotifyResponsiveness(hidlRequest, requestAccuracy);
            hidlRequest.radius = Double.parseDouble(requests.get("radius").toString());
            hidlRequest.unknownTimerMs = -1;
            hidlRequest.monitorTransitions = Integer.parseInt(requests.get(MONITOR_TRANSITIONS).toString());
            hidlRequest.lastTransition = 4;
            int requestParentFenceId = Integer.parseInt(requests.get(PARENT_FENCE_ID).toString());
            hidlRequest.parentFenceId = requestParentFenceId;
            hidlRequest.floor = Integer.parseInt(requests.get(FLOOR).toString());
            hidlRequest.reportType = 0;
            if (requestAccuracy == 1) {
                hidlRequest.reportType |= 1;
            } else {
                hidlRequest.reportType |= 2;
            }
            if (requestParentFenceId == 0) {
                if (Integer.parseInt(requests.get(SUBFENCE).toString()) == 1) {
                    hidlRequest.reportType |= 8;
                }
            } else if (requestParentFenceId > 0) {
                hidlRequest.reportType = 4 | hidlRequest.reportType;
            } else {
                LBSLog.i(TAG, "ParentFenceId is negative.");
            }
            if ((Integer.parseInt(requests.get("coordinateType").toString()) & 1) == 1) {
                i = hidlRequest.reportType | 16;
            } else {
                i = hidlRequest.reportType;
            }
            hidlRequest.reportType = i;
            hidlRequests.add(hidlRequest);
        }
        return hidlRequests;
    }

    private void giveAccuracyAndNotifyResponsiveness(IFusdGeofenceInterface.GeofenceRequest hidlRequest, int requestAccuracy) {
        if (requestAccuracy == 1 || requestAccuracy == 2 || requestAccuracy == 3) {
            hidlRequest.accuracy = requestAccuracy;
        } else {
            hidlRequest.accuracy = 1;
        }
        if (requestAccuracy == 1) {
            hidlRequest.notificationResponsivenessMs = -1;
        } else {
            hidlRequest.notificationResponsivenessMs = 60000;
        }
    }

    private void giveAccuracyAndNotifyResponsivenessForPolygon(IFusdGeofenceInterface.GeofencePolygonRequest hidlRequest, int requestAccuracy) {
        if (requestAccuracy == 1 || requestAccuracy == 2 || requestAccuracy == 3) {
            hidlRequest.accuracy = requestAccuracy;
        } else {
            hidlRequest.accuracy = 1;
        }
        if (requestAccuracy == 1) {
            hidlRequest.notificationResponsivenessMs = -1;
        } else {
            hidlRequest.notificationResponsivenessMs = 60000;
        }
    }

    private void giveLatAndLongValues(IFusdGeofenceInterface.GeofenceRequest hidlRequest, Map<String, Object> requests) {
        HashMap<String, Object> points = new HashMap<>(16);
        Object pointObj = requests.get("point");
        if (pointObj != null && (pointObj instanceof HashMap)) {
            points = (HashMap) pointObj;
        }
        Object latitudeObj = points.get(LATITUDE);
        String longitudeStr = "0";
        hidlRequest.point.latitudeDegrees = Double.parseDouble(latitudeObj == null ? longitudeStr : latitudeObj.toString());
        Object longitudeObj = points.get(LONGITUDE);
        if (longitudeObj != null) {
            longitudeStr = longitudeObj.toString();
        }
        hidlRequest.point.longitudeDegrees = Double.parseDouble(longitudeStr);
    }

    public boolean addGeofencePolygon(int[] ids, ArrayList<Map<String, Object>> geofences) {
        if (ids == null || geofences == null) {
            return false;
        }
        getIFusdLbsService();
        IFusdGeofenceInterface iFusdGeofenceInterface = this.mGeofenceInterface;
        if (iFusdGeofenceInterface == null) {
            LBSLog.i(TAG, GEOFENCE_INTERFACE_NULL);
            return false;
        }
        try {
            iFusdGeofenceInterface.addPolygonGeofences(getHidlPolygonRequest(ids, geofences));
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, ADD_GEOFENCE_ERROR);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception addGeofencePolygon", new Object[0]);
            return false;
        }
    }

    public boolean addGeofence(int[] ids, ArrayList<Map<String, Object>> geofences) {
        if (ids == null || geofences == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mGeofenceInterface == null) {
            LBSLog.i(TAG, GEOFENCE_INTERFACE_NULL);
            return false;
        }
        try {
            LBSLog.i(TAG, "mGeofenceInterface.addGeofences");
            this.mGeofenceInterface.addGeofences(getHidlRequest(ids, geofences));
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, ADD_GEOFENCE_ERROR);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception addGeofence", new Object[0]);
            return false;
        }
    }

    public boolean addHmsGeofence(int[] ids, ArrayList<Map<String, Object>> geofences) {
        if (ids == null || geofences == null) {
            return false;
        }
        getIFusdLbsService();
        IFusdGeofenceInterface iFusdGeofenceInterface = this.mGeofenceInterface;
        if (iFusdGeofenceInterface == null) {
            LBSLog.d(TAG, GEOFENCE_INTERFACE_NULL);
            return false;
        }
        try {
            iFusdGeofenceInterface.addGeofences(getHidlHmsRequest(ids, geofences));
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "add hmsgeofence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception addHmsGeofence", new Object[0]);
            return false;
        }
    }

    public boolean removeGeofence(int[] ids) {
        if (ids == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mGeofenceInterface == null) {
            LBSLog.i(TAG, GEOFENCE_INTERFACE_NULL);
            return false;
        }
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i : ids) {
            idList.add(Integer.valueOf(i));
        }
        try {
            this.mGeofenceInterface.removeGeofences(idList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, ADD_GEOFENCE_ERROR);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception removeGeofence", new Object[0]);
            return false;
        }
    }

    public boolean getCurrentLocation() {
        getIFusdLbsService();
        IFusdGeofenceInterface iFusdGeofenceInterface = this.mGeofenceInterface;
        if (iFusdGeofenceInterface == null) {
            LBSLog.i(TAG, GEOFENCE_INTERFACE_NULL);
            return false;
        }
        try {
            iFusdGeofenceInterface.getCurrentLocation();
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "getCurrentLocation error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception getCurrentLocation", new Object[0]);
            return false;
        }
    }

    public int getGeofenceState(int geofenceId) {
        getIFusdLbsService();
        IFusdGeofenceInterface iFusdGeofenceInterface = this.mGeofenceInterface;
        if (iFusdGeofenceInterface == null) {
            LBSLog.i(TAG, GEOFENCE_INTERFACE_NULL);
            return -1;
        }
        try {
            return iFusdGeofenceInterface.getGeofenceStatus(geofenceId);
        } catch (RemoteException e) {
            LBSLog.e(TAG, ADD_GEOFENCE_ERROR);
            return -1;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception getGeofenceState", new Object[0]);
            return -1;
        }
    }

    public void sendGeofenceEventCallback(Bundle bundle) {
        if (bundle != null && this.mGeoFenceCallback != null) {
            switch (bundle.getInt("type")) {
                case 55:
                    this.mGeoFenceCallback.onGetCurrentLocationCb(bundle);
                    return;
                case IDisplayEngineService.DE_ACTION_GAME_MOVIE /* 56 */:
                    this.mGeoFenceCallback.gnssGeofenceTransitionCb(bundle);
                    return;
                case IDisplayEngineService.DE_ACTION_FULLSCREEN_CUVA /* 57 */:
                    this.mGeoFenceCallback.gnssGeofenceStatusCb(bundle);
                    return;
                case IDisplayEngineService.DE_ACTION_MAX /* 58 */:
                    this.mGeoFenceCallback.geofenceAddResultCb(bundle);
                    return;
                case 59:
                    this.mGeoFenceCallback.geofenceRemoveResultCb(bundle);
                    return;
                default:
                    return;
            }
        }
    }
}
