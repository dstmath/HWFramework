package com.huawei.opcollect.collector.servicecollection;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.huawei.nb.model.collectencrypt.RawLocationRecord;
import com.huawei.nb.query.Query;
import com.huawei.opcollect.location.HwLocation;
import com.huawei.opcollect.location.ILocationListener;
import com.huawei.opcollect.location.IntelligentLocation;
import com.huawei.opcollect.location.SystemLocation;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.AbsActionParam;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationRecordAction extends Action {
    private static final int FIVE_MINUTES_IN_MILLISECOND = 300000;
    private static final double INVALID_ALTITUTE = -1.0d;
    private static final double INVALID_LATITUTE = -1.0d;
    private static final double INVALID_LONGITUTE = -1.0d;
    private static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final String ODMF_API_VERSION_2_4_4 = "2.4.4";
    public static final int POSITION_CHANGE_INTELLIGENT = 1;
    public static final int POSITION_CHANGE_SYSTEM = 2;
    public static final int POSITION_CHANGE_WIFI = 3;
    private static final String TAG = "LocationRecordAction";
    private static LocationRecordAction sInstance = null;
    private ConnectivityManager mConnectivityManager = null;
    private Geocoder mGeocoder = null;
    private LocationHandler mHandler = null;
    private HashMap<String, ILocationListener> mLocationListeners = null;
    private final Object mLock = new Object();
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager = null;

    public enum GeodeticSystem {
        WGS84,
        GCJ02,
        BD09ll,
        GS_INVALID
    }

    private static class LocationActionParam extends AbsActionParam {
        private HwLocation hwLocation;

        LocationActionParam(HwLocation hwLocation2, boolean checkMinInterval) {
            super(checkMinInterval);
            this.hwLocation = hwLocation2;
        }

        public HwLocation getHwLocation() {
            return this.hwLocation;
        }

        public void setHwLocation(HwLocation hwLocation2) {
            this.hwLocation = hwLocation2;
        }

        public String toString() {
            return "LocationActionParam{hwLocation=" + this.hwLocation + "} " + super.toString();
        }
    }

    private static class LocationHandler extends Handler {
        private final WeakReference<LocationRecordAction> service;

        LocationHandler(LocationRecordAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OPCollectLog.r(LocationRecordAction.TAG, "handleMessage ");
            LocationRecordAction action = (LocationRecordAction) this.service.get();
            if (action != null) {
                HwLocation location = null;
                LocationActionParam actionParam = null;
                if (msg.what == 1) {
                    location = (HwLocation) msg.obj;
                    actionParam = new LocationActionParam(location, false);
                } else if (msg.what == 2) {
                    location = (HwLocation) msg.obj;
                    actionParam = new LocationActionParam(location, true);
                } else if (msg.what == 3) {
                    action.insertRecordWithoutGPS((String) msg.obj);
                    return;
                }
                if (location != null) {
                    boolean unused = action.performWithArgs(actionParam);
                }
            }
        }
    }

    public enum RecordType {
        INTELLIGENT('0'),
        GPS('1'),
        NET('2'),
        WIFI('3');
        
        private Character chr;

        private RecordType(Character chr2) {
            this.chr = chr2;
        }

        public Character getChr() {
            return this.chr;
        }
    }

    public static synchronized LocationRecordAction getInstance(Context context) {
        LocationRecordAction locationRecordAction;
        synchronized (LocationRecordAction.class) {
            if (sInstance == null) {
                sInstance = new LocationRecordAction(OPCollectConstant.LOCATION_ACTION_NAME, context);
            }
            locationRecordAction = sInstance;
        }
        return locationRecordAction;
    }

    private LocationRecordAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(queryDailyRecordNum(RawLocationRecord.class));
        if (this.mContext == null) {
            OPCollectLog.e(TAG, "context is null");
            return;
        }
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mLocationListeners = new HashMap<>();
        this.mGeocoder = new Geocoder(this.mContext, Locale.CHINA);
    }

    public void enable() {
        super.enable();
        SystemLocation.getInstance(this.mContext).enable();
        IntelligentLocation.getInstance(this.mContext).enable();
        synchronized (this.mLock) {
            if (this.mHandler == null) {
                this.mHandler = new LocationHandler(this);
            }
        }
    }

    public void disable() {
        super.disable();
        IntelligentLocation.getInstance(this.mContext).disable();
        synchronized (this.mLock) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler.removeMessages(2);
                this.mHandler = null;
            }
        }
    }

    public boolean destroy() {
        super.destroy();
        IntelligentLocation.getInstance(this.mContext).destroy();
        if (this.mLocationListeners != null) {
            this.mLocationListeners.clear();
            this.mLocationListeners = null;
        }
        this.mTelephonyManager = null;
        this.mConnectivityManager = null;
        this.mWifiManager = null;
        this.mGeocoder = null;
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (LocationRecordAction.class) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean executeWithArgs(AbsActionParam absActionParam) {
        OPCollectLog.r(TAG, "executeWithArgs");
        if (absActionParam == null || !(absActionParam instanceof LocationActionParam)) {
            return false;
        }
        HwLocation location = ((LocationActionParam) absActionParam).getHwLocation();
        if (location == null) {
            return false;
        }
        RawLocationRecord rawLocationRecord = new RawLocationRecord();
        long timestamp = location.getTimestamp();
        OPCollectLog.r(TAG, "execute: " + timestamp);
        if (timestamp == -1) {
            rawLocationRecord.setMTimeStamp(new Date());
        } else {
            RawLocationRecord querySingleManageObject = OdmfCollectScheduler.getInstance().getOdmfHelper().querySingleManageObject(Query.select(RawLocationRecord.class).equalTo("mTimeStamp", Long.valueOf(timestamp)));
            if (querySingleManageObject != null) {
                querySingleManageObject.setMLongitude(Double.valueOf(location.getLongitude()));
                querySingleManageObject.setMLatitude(Double.valueOf(location.getLatitude()));
                OdmfCollectScheduler.getInstance().getOdmfHelper().updateManageObject(querySingleManageObject);
                OPCollectLog.r(TAG, "update record: " + timestamp);
                return false;
            } else if (Math.abs(System.currentTimeMillis() - timestamp) > 300000) {
                OPCollectLog.r(TAG, timestamp + " data is old, ignore.");
                return false;
            } else {
                rawLocationRecord.setMTimeStamp(new Date(location.getTimestamp()));
            }
        }
        locationToGeocode(location);
        getLocation(rawLocationRecord, location);
        getWifiInfo(rawLocationRecord);
        getCellInfo(rawLocationRecord);
        rawLocationRecord.setMReservedText(OPCollectUtils.formatCurrentTime());
        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, rawLocationRecord).sendToTarget();
        if (this.mLocationListeners != null) {
            OPCollectLog.i(TAG, "listener size: " + this.mLocationListeners.size());
            for (ILocationListener listener : this.mLocationListeners.values()) {
                if (listener != null) {
                    listener.onLocationSuccess(location);
                }
            }
        }
        return true;
    }

    private void getLocation(RawLocationRecord rawLocationRecord, HwLocation hwLocation) {
        boolean geodeticSystemExsit = OPCollectUtils.checkODMFApiVersion(this.mContext, ODMF_API_VERSION_2_4_4);
        rawLocationRecord.setMLongitude(Double.valueOf(hwLocation.getLongitude()));
        rawLocationRecord.setMLatitude(Double.valueOf(hwLocation.getLatitude()));
        rawLocationRecord.setMCity(hwLocation.getCity());
        rawLocationRecord.setMAltitude(Double.valueOf(hwLocation.getAltitude()));
        rawLocationRecord.setMCountry(hwLocation.getCountry());
        rawLocationRecord.setMProvince(hwLocation.getProvince());
        rawLocationRecord.setMReservedInt(Integer.valueOf(hwLocation.getAccuracy()));
        if (geodeticSystemExsit) {
            rawLocationRecord.setGeodeticSystem(GeodeticSystem.WGS84.name());
        }
        if ("gps".equals(hwLocation.getProvider())) {
            rawLocationRecord.setMLocationType(RecordType.GPS.getChr());
        } else if ("network".equals(hwLocation.getProvider())) {
            rawLocationRecord.setMLocationType(RecordType.NET.getChr());
        } else if (IntelligentLocation.PROVIDE_INTELLEIGENT.equals(hwLocation.getProvider())) {
            if (geodeticSystemExsit) {
                rawLocationRecord.setGeodeticSystem(GeodeticSystem.GCJ02.name());
            }
            rawLocationRecord.setMLocationType(RecordType.INTELLIGENT.getChr());
        } else {
            OPCollectLog.e(TAG, "unknown provider");
        }
    }

    private void getWifiInfo(RawLocationRecord rawLocationRecord) {
        if (isWifiConnected() && this.mWifiManager != null) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                rawLocationRecord.setMWifiBSSID(wifiInfo.getBSSID());
                rawLocationRecord.setMWifiLevel(Integer.valueOf(wifiInfo.getRssi()));
                return;
            }
            rawLocationRecord.setMWifiBSSID(null);
            rawLocationRecord.setMWifiLevel(-1);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v0, resolved type: android.telephony.CellInfoWcdma} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: android.telephony.CellInfoCdma} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v0, resolved type: android.telephony.CellInfoGsm} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v0, resolved type: android.telephony.CellInfoLte} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void getCellInfo(RawLocationRecord rawLocationRecord) {
        CellInfo info;
        int cell_mcc = -1;
        int cell_mnc = -1;
        int cell_id = -1;
        int cell_lac = -1;
        if (this.mTelephonyManager == null) {
            OPCollectLog.e(TAG, "TelephonyManager is null");
            return;
        }
        List<CellInfo> list = null;
        try {
            list = this.mTelephonyManager.getAllCellInfo();
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "getAllCellInfo failed: " + e.getMessage());
        }
        if (list == null || list.size() == 0) {
            OPCollectLog.e(TAG, "list is null");
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (CellInfo cellInfo : list) {
            if (cellInfo != null && cellInfo.isRegistered()) {
                arrayList.add(cellInfo);
            }
        }
        int size = arrayList.size();
        if (size <= 0) {
            OPCollectLog.e(TAG, "registeredList size is 0");
            return;
        }
        int main_slot = -1;
        if (Build.VERSION.SDK_INT > 23) {
            main_slot = SubscriptionManager.getDefaultDataSubscriptionId();
        }
        OPCollectLog.r(TAG, "slot: " + main_slot + " size: " + size + " : " + list.size());
        if (1 == size || main_slot <= -1 || main_slot >= size) {
            info = (CellInfo) arrayList.get(0);
        } else {
            info = (CellInfo) arrayList.get(main_slot);
        }
        if (info != null) {
            if (info instanceof CellInfoLte) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) info).getCellIdentity();
                if (cellIdentityLte != null) {
                    cell_mcc = cellIdentityLte.getMcc();
                    cell_mnc = checkCellMnc(cellIdentityLte.getMnc());
                    cell_id = cellIdentityLte.getCi();
                    cell_lac = cellIdentityLte.getTac();
                } else {
                    return;
                }
            } else if (info instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) info).getCellIdentity();
                if (cellIdentityGsm != null) {
                    cell_mcc = cellIdentityGsm.getMcc();
                    cell_mnc = checkCellMnc(cellIdentityGsm.getMnc());
                    cell_id = cellIdentityGsm.getCid();
                    cell_lac = cellIdentityGsm.getLac();
                } else {
                    return;
                }
            } else if (info instanceof CellInfoCdma) {
                CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) info).getCellIdentity();
                if (cellIdentityCdma != null) {
                    String network_operator = this.mTelephonyManager.getNetworkOperator();
                    if (network_operator != null && network_operator.length() > 2) {
                        try {
                            cell_mcc = Integer.parseInt(network_operator.substring(0, 3));
                        } catch (NumberFormatException e2) {
                            OPCollectLog.e(TAG, e2.getMessage());
                        }
                    }
                    cell_mnc = checkCellMnc(cellIdentityCdma.getSystemId());
                    cell_id = cellIdentityCdma.getBasestationId();
                    cell_lac = cellIdentityCdma.getNetworkId();
                } else {
                    return;
                }
            } else if (info instanceof CellInfoWcdma) {
                CellIdentityWcdma cellWcdmaCellIdentity = ((CellInfoWcdma) info).getCellIdentity();
                if (cellWcdmaCellIdentity != null) {
                    cell_mcc = cellWcdmaCellIdentity.getMcc();
                    cell_mnc = checkCellMnc(cellWcdmaCellIdentity.getMnc());
                    cell_id = cellWcdmaCellIdentity.getCid();
                    cell_lac = cellWcdmaCellIdentity.getLac();
                } else {
                    return;
                }
            }
        }
        rawLocationRecord.setMCellID(Integer.valueOf(cell_id));
        rawLocationRecord.setMCellLAC(Integer.valueOf(cell_lac));
        rawLocationRecord.setMCellMCC(Integer.valueOf(cell_mcc));
        rawLocationRecord.setMCellMNC(Integer.valueOf(cell_mnc));
    }

    private int checkCellMnc(int cell_mnc) {
        if (cell_mnc == Integer.MAX_VALUE) {
            return -1;
        }
        return cell_mnc;
    }

    private boolean isWifiConnected() {
        boolean z = true;
        if (this.mConnectivityManager == null) {
            return false;
        }
        NetworkInfo info = this.mConnectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected() || info.getType() != 1) {
            z = false;
        }
        return z;
    }

    public void addLocationListener(String key, ILocationListener listener) {
        if (this.mLocationListeners != null) {
            OPCollectLog.i(TAG, "add listener: " + listener);
            this.mLocationListeners.put(key, listener);
        }
    }

    public void removeLocationListener(String key, ILocationListener listener) {
        if (this.mLocationListeners != null) {
            OPCollectLog.i(TAG, "remove listener: " + listener);
            this.mLocationListeners.remove(key);
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        SystemLocation.getInstance(this.mContext).dump(indentNum, pw);
        IntelligentLocation.getInstance(this.mContext).dump(indentNum, pw);
    }

    public Handler getLocationHandler() {
        return this.mHandler;
    }

    public Object getLock() {
        return this.mLock;
    }

    /* access modifiers changed from: private */
    public void insertRecordWithoutGPS(String wifiStr) {
        OPCollectLog.r(TAG, "insertRecordWithoutGPS");
        if (wifiStr == null || "".equals(wifiStr.trim())) {
            OPCollectLog.r(TAG, "invalid wifi info.");
            return;
        }
        RawLocationRecord rawLocationRecord = new RawLocationRecord();
        rawLocationRecord.setMTimeStamp(new Date());
        rawLocationRecord.setMLocationType(RecordType.WIFI.getChr());
        rawLocationRecord.setMAltitude(Double.valueOf(-1.0d));
        rawLocationRecord.setMLatitude(Double.valueOf(-1.0d));
        rawLocationRecord.setMLongitude(Double.valueOf(-1.0d));
        if (OPCollectUtils.checkODMFApiVersion(this.mContext, ODMF_API_VERSION_2_4_4)) {
            rawLocationRecord.setGeodeticSystem(GeodeticSystem.GS_INVALID.name());
        }
        JSONObject object = null;
        try {
            object = new JSONObject(wifiStr);
        } catch (JSONException e) {
            OPCollectLog.e(TAG, "exception: " + e.getMessage());
        }
        if (object != null) {
            try {
                rawLocationRecord.setMWifiBSSID(object.getString(OPCollectConstant.WIFI_BSSID));
                rawLocationRecord.setMWifiLevel(Integer.valueOf(object.getInt(OPCollectConstant.WIFI_LEVEL)));
            } catch (JSONException e2) {
                OPCollectLog.e(TAG, "exception: " + e2.getMessage());
            }
        }
        getCellInfo(rawLocationRecord);
        rawLocationRecord.setMReservedText(OPCollectUtils.formatCurrentTime());
        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, rawLocationRecord).sendToTarget();
    }

    private void locationToGeocode(HwLocation location) {
        try {
            if (this.mGeocoder != null) {
                List<Address> addresses = this.mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses == null || addresses.size() <= 0) {
                    OPCollectLog.e(TAG, "Geocoder exception!");
                    return;
                }
                Address address = addresses.get(0);
                if (address != null) {
                    location.setCityCode(address.getLocality());
                    location.setCountry(address.getCountryCode());
                    location.setCity(address.getLocality());
                    location.setDetailAddress(address.toString());
                    location.setDistrict(address.getSubLocality());
                    location.setProvince(address.getAdminArea());
                }
            }
        } catch (IOException e) {
            OPCollectLog.e(TAG, "getFromLocation io failed: " + e.getMessage());
        } catch (IllegalArgumentException e2) {
            OPCollectLog.e(TAG, "getFromLocation illegal failed: " + e2.getMessage());
        }
    }
}
