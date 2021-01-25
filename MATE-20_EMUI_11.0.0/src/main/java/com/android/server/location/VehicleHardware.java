package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_4.IFusdLbs;
import vendor.huawei.hardware.fusd.V1_4.IRemoteVehicleCallback;
import vendor.huawei.hardware.fusd.V1_4.IRemoteVehicleInterface;

public class VehicleHardware {
    private static final int DEFAULT_LIST_SIZE = 10;
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final String EAST_DATA_TAG = "E";
    private static final int EAST_FLAG = 0;
    private static final float FLOAT_DEFAULT_VALUE = 888888.7f;
    private static final String GPACC_DATA_TAG = "GPACC";
    private static final String GPGGA_DATA_TAG = "GPGGA";
    private static final int GPGGA_LEN_MAX = 15;
    private static final int GPGGA_LEN_MID = 14;
    private static final int GPGGA_LEN_MIN = 13;
    private static final String GPRMC_DATA_TAG = "GPRMC";
    private static final String GPVAI_DATA_TAG = "GPVAI";
    private static final int HEAD_SENSOR_LEN = 3;
    private static final int INT_DEFAULT_VALUE = -1;
    private static final String NORTH_DATA_TAG = "N";
    private static final int NORTH_FLAG = 0;
    private static final int PACKAGE_VALUE = 0;
    private static final int SINGLE_SENSOR_LEN = 5;
    private static final String SOUTH_DATA_TAG = "S";
    private static final int SOUTH_FLAG = 1;
    private static final String SYNC_DATA_TAG = "SYNC";
    private static final String SYNC_RESP_DATA_TAG = "SYNCRESP";
    private static final String TAG = "VehicleHardware";
    private static final int UTC_TRANSFER = 1000;
    private static final int VEHICLE_IS_ENABLE = 1;
    private static final int VEHICLE_NOT_ENABLE = 0;
    private static final Object WATCHER_LOCK = new Object();
    private static final String WEST_DATA_TAG = "W";
    private static final int WEST_FLAG = 1;
    private static VehicleHardware sVehicleHardware;
    private long currentTimeFirst = System.currentTimeMillis();
    private long currentTimeFourth = System.currentTimeMillis();
    private long currentTimeSecond = System.currentTimeMillis();
    private long currentTimeThird = System.currentTimeMillis();
    private VehicleCallback mCallback;
    private IFusdLbs mFusdLbs;
    private IRemoteVehicleInterface mIremoteVehicleInterface;
    private IRemoteVehicleInterface.RemoteGnssLocation mRemoteGnssLocation;
    private IRemoteVehicleInterface.RemoteSensorData mRemoteSensorData;
    private IRemoteVehicleInterface.RemoteVehicleData mRemoteVehicleData;

    public static VehicleHardware getInstance(Context context, VehicleCallback callback) {
        VehicleHardware vehicleHardware;
        synchronized (WATCHER_LOCK) {
            if (sVehicleHardware == null) {
                sVehicleHardware = new VehicleHardware(context, callback);
            }
            vehicleHardware = sVehicleHardware;
        }
        return vehicleHardware;
    }

    private VehicleHardware(Context context, VehicleCallback callback) {
        getIFusdLbsService();
        this.mCallback = callback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.VehicleHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                VehicleHardware.this.getIFusdLbsService();
            }
        });
        LBSLog.i(TAG, false, "PgnssHardware init completed.", new Object[0]);
    }

    public boolean sendVehicleData(String nmeaData) {
        if (nmeaData == null) {
            LBSLog.e(TAG, false, "sendVehicleData data is null.", new Object[0]);
            return false;
        }
        getIFusdLbsService();
        return sendNmeaData(nmeaData);
    }

    private boolean sendNmeaData(String nmeaData) {
        if (nmeaData.indexOf(SYNC_RESP_DATA_TAG) != -1) {
            LBSLog.i(TAG, false, "VehicleData is SYNCRESP", new Object[0]);
            sendOffsetTime(nmeaData);
            return true;
        } else if (nmeaData.indexOf(GPVAI_DATA_TAG) != -1) {
            LBSLog.i(TAG, false, "VehicleData is GPVAI", new Object[0]);
            sendRemoteVehicleData(nmeaData);
            return true;
        } else if (nmeaData.indexOf(GPGGA_DATA_TAG) != -1 || nmeaData.indexOf(GPRMC_DATA_TAG) != -1) {
            LBSLog.i(TAG, false, "VehicleData is GPGGA or GPRMC", new Object[0]);
            IRemoteVehicleInterface.RemoteGnssLocation gnssLocationData = splitGnssLocationData(nmeaData);
            try {
                if (this.mIremoteVehicleInterface == null || gnssLocationData == null) {
                    return true;
                }
                this.mIremoteVehicleInterface.sendRemoteGnssLocation(gnssLocationData);
                this.mRemoteGnssLocation = null;
                return true;
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendRemoteGnssLocation error:", new Object[0]);
                return false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendRemoteGnssLocation", new Object[0]);
                return false;
            }
        } else if (nmeaData.indexOf(GPACC_DATA_TAG) != -1) {
            LBSLog.i(TAG, false, "VehicleData is GPACC ", new Object[0]);
            sendRemoteSensorData(nmeaData);
            return true;
        } else {
            try {
                if (this.mIremoteVehicleInterface == null) {
                    return true;
                }
                this.mIremoteVehicleInterface.sendMsgToPhone(nmeaData);
                LBSLog.i(TAG, false, "String contains other msg just send it", new Object[0]);
                return true;
            } catch (RemoteException e3) {
                LBSLog.e(TAG, false, "mIRemoteVehicleInterface sendMsgToPhone error", new Object[0]);
                return false;
            } catch (NoSuchElementException e4) {
                LBSLog.e(TAG, false, "No Such Element Exception sendMsgToPhone", new Object[0]);
                return false;
            }
        }
    }

    private void sendOffsetTime(String resTime) {
        String[] mTimeDataStrings;
        if (resTime == null || resTime.length() <= 0) {
            LBSLog.i(TAG, false, "resTime is empty", new Object[0]);
            return;
        }
        LBSLog.i(TAG, false, "resTime length is %{public}d", Integer.valueOf(resTime.length()));
        String[] sourceStrArrays = resTime.split("\\$");
        try {
            if (sourceStrArrays[1].indexOf("*") != -1) {
                mTimeDataStrings = sourceStrArrays[1].split("\\*")[0].split(",");
            } else {
                mTimeDataStrings = sourceStrArrays[1].split(",");
            }
            this.currentTimeSecond = Long.valueOf(mTimeDataStrings[1]).longValue();
            this.currentTimeThird = Long.valueOf(mTimeDataStrings[2]).longValue();
            this.currentTimeFourth = System.currentTimeMillis();
            String offset = String.valueOf(((this.currentTimeSecond - this.currentTimeFirst) - (this.currentTimeFourth - this.currentTimeThird)) / 2);
            IRemoteVehicleInterface iRemoteVehicleInterface = this.mIremoteVehicleInterface;
            iRemoteVehicleInterface.sendMsgToPhone("$SYNCRESP," + offset);
        } catch (IndexOutOfBoundsException e) {
            LBSLog.e(TAG, false, "OffsetTime OutOfBoundsException", new Object[0]);
        } catch (ClassCastException e2) {
            LBSLog.e(TAG, false, "OffsetTime ClassCastException", new Object[0]);
        } catch (NumberFormatException e3) {
            LBSLog.e(TAG, false, "OffsetTime NumberFormatException", new Object[0]);
        } catch (RemoteException e4) {
            LBSLog.e(TAG, false, "mIRemoteVehicleInterface sendMsgToPhone error", new Object[0]);
        } catch (NoSuchElementException e5) {
            LBSLog.e(TAG, false, "No Such Element Exception sendMsgToPhone", new Object[0]);
        }
    }

    private void sendRemoteVehicleData(String nmeaData) {
        ArrayList<IRemoteVehicleInterface.RemoteVehicleData> vehicleDataList = splitVehicleData(nmeaData);
        try {
            if (this.mIremoteVehicleInterface != null && vehicleDataList != null && vehicleDataList.size() > 0) {
                this.mIremoteVehicleInterface.sendRemoteVehicleData(vehicleDataList);
                this.mRemoteVehicleData = null;
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "sendRemoteVehicleData error", new Object[0]);
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception sendRemoteVehicleData", new Object[0]);
        }
    }

    private void sendRemoteSensorData(String sensorData) {
        String[] mGpaccDatas;
        int count = 0;
        int freq = 0;
        int type = 0;
        if (sensorData.indexOf("*") != -1) {
            mGpaccDatas = sensorData.split("\\*")[0].split(",");
        } else {
            mGpaccDatas = sensorData.split(",");
        }
        try {
            count = Integer.parseInt(mGpaccDatas[1]);
            freq = Integer.parseInt(mGpaccDatas[2]);
            type = Integer.parseInt(mGpaccDatas[3]);
        } catch (NumberFormatException e) {
            LBSLog.e(TAG, false, "parseInt error, NumberFormatException", new Object[0]);
        } catch (ClassCastException e2) {
            LBSLog.e(TAG, false, "parseInt error, lassCastException", new Object[0]);
        }
        ArrayList<IRemoteVehicleInterface.RemoteSensorData> mGpaccDatasArray = splitSensorData(count, mGpaccDatas);
        if (mGpaccDatasArray.isEmpty()) {
            LBSLog.i(TAG, false, "empty sensorDatas", new Object[0]);
        }
        try {
            if (this.mIremoteVehicleInterface != null && mGpaccDatasArray.size() > 0) {
                LBSLog.i(TAG, false, "VehicleData sendRemoteSensorData", new Object[0]);
                this.mIremoteVehicleInterface.sendRemoteSensorData(freq, type, mGpaccDatasArray);
                this.mRemoteSensorData = null;
            }
        } catch (RemoteException e3) {
            LBSLog.e(TAG, false, "sendRemoteSensorData error", new Object[0]);
        } catch (NoSuchElementException e4) {
            LBSLog.e(TAG, false, "No Such Element Exception sendRemoteGnssLocation", new Object[0]);
        }
    }

    private ArrayList<IRemoteVehicleInterface.RemoteVehicleData> splitVehicleData(String nmea) {
        String[] mVehicleDataStrings;
        String[] sourceStrArrays = nmea.split("\\$");
        if (sourceStrArrays.length > 0) {
            ArrayList<IRemoteVehicleInterface.RemoteVehicleData> vehicleDataList = new ArrayList<>(sourceStrArrays.length - 1);
            LBSLog.i(TAG, false, "VehicleDatalength: %{public}d", Integer.valueOf(sourceStrArrays.length));
            for (int i = 1; i < sourceStrArrays.length; i++) {
                try {
                    if (sourceStrArrays[i].indexOf("*") != -1) {
                        mVehicleDataStrings = sourceStrArrays[i].split("\\*")[0].split(",");
                    } else {
                        mVehicleDataStrings = sourceStrArrays[i].split(",");
                    }
                    this.mRemoteVehicleData = new IRemoteVehicleInterface.RemoteVehicleData();
                    assignVehicleData(mVehicleDataStrings);
                    vehicleDataList.add(this.mRemoteVehicleData);
                } catch (IndexOutOfBoundsException e) {
                    LBSLog.e(TAG, false, "vehicleDataList IndexOutOfBoundsException", new Object[0]);
                } catch (ClassCastException e2) {
                    LBSLog.e(TAG, false, "vehicleDataList ClassCastException", new Object[0]);
                } catch (NumberFormatException e3) {
                    LBSLog.e(TAG, false, "vehicleDataList NumberFormatException", new Object[0]);
                }
            }
            if (vehicleDataList.isEmpty()) {
                LBSLog.d(TAG, false, "empty vehicleDataList", new Object[0]);
            }
            return vehicleDataList;
        }
        LBSLog.i(TAG, false, "sourceStrArrays is empty", new Object[0]);
        return new ArrayList<>();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mFusdLbs == null) {
            this.mFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_4();
            if (this.mFusdLbs != null) {
                try {
                    this.mIremoteVehicleInterface = this.mFusdLbs.getRemoteVehicleInterface();
                    if (this.mIremoteVehicleInterface != null) {
                        this.mIremoteVehicleInterface.registerRemoteVehicleCallback(new VehicleDataCallback());
                        LBSLog.i(TAG, false, "LbsService registerRemoteVehicleCallback", new Object[0]);
                    } else {
                        LBSLog.e(TAG, false, "mIRemoteVehicleInterface is null...", new Object[0]);
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "register callback error", new Object[0]);
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception registerRemoteVehicle", new Object[0]);
                }
            }
        }
    }

    private void assignVehicleData(String[] vehicleDatas) {
        if (vehicleDatas[1].isEmpty()) {
            this.mRemoteVehicleData.utc = -1;
        } else {
            this.mRemoteVehicleData.utc = (int) (Float.parseFloat(vehicleDatas[1]) * 1000.0f);
        }
        if (vehicleDatas[2].isEmpty()) {
            this.mRemoteVehicleData.gear = -1;
        } else {
            this.mRemoteVehicleData.gear = Integer.parseInt(vehicleDatas[2]);
        }
        if (vehicleDatas[3].isEmpty()) {
            this.mRemoteVehicleData.steeringWheel = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.steeringWheel = Float.parseFloat(vehicleDatas[3]);
        }
        if (vehicleDatas[4].isEmpty()) {
            this.mRemoteVehicleData.leftFrontSpeed = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.leftFrontSpeed = Float.parseFloat(vehicleDatas[4]);
        }
        if (vehicleDatas[5].isEmpty()) {
            this.mRemoteVehicleData.rightFrontSpeed = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.rightFrontSpeed = Float.parseFloat(vehicleDatas[5]);
        }
        if (vehicleDatas[6].isEmpty()) {
            this.mRemoteVehicleData.leftRearSpeed = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.leftRearSpeed = Float.parseFloat(vehicleDatas[6]);
        }
        if (vehicleDatas[7].isEmpty()) {
            this.mRemoteVehicleData.rightRearSpeed = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.rightRearSpeed = Float.parseFloat(vehicleDatas[7]);
        }
        if (vehicleDatas[8].isEmpty()) {
            this.mRemoteVehicleData.vehicleSpeed = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteVehicleData.vehicleSpeed = Float.parseFloat(vehicleDatas[8]);
        }
        if (vehicleDatas[9].isEmpty()) {
            this.mRemoteVehicleData.date = -1;
        } else {
            this.mRemoteVehicleData.date = Integer.parseInt(vehicleDatas[9]);
        }
        LBSLog.i(TAG, false, "The VehicleData is %{public}d", Integer.valueOf(this.mRemoteVehicleData.utc + this.mRemoteVehicleData.date));
    }

    private void assignGpggaData(String[] gpggaDatas) {
        if (gpggaDatas[1].isEmpty()) {
            this.mRemoteGnssLocation.utc = -1;
        } else {
            this.mRemoteGnssLocation.utc = (int) (Float.parseFloat(gpggaDatas[1]) * 1000.0f);
        }
        if (gpggaDatas[2].isEmpty()) {
            this.mRemoteGnssLocation.latitude = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.latitude = Float.parseFloat(gpggaDatas[2]);
        }
        if (gpggaDatas[3].isEmpty()) {
            this.mRemoteGnssLocation.directionOfLat = -1;
        } else if (gpggaDatas[3].equals(NORTH_DATA_TAG)) {
            this.mRemoteGnssLocation.directionOfLat = 0;
        } else if (gpggaDatas[3].equals(SOUTH_DATA_TAG)) {
            this.mRemoteGnssLocation.directionOfLat = 1;
        } else {
            return;
        }
        if (gpggaDatas[4].isEmpty()) {
            this.mRemoteGnssLocation.longitude = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.longitude = Float.parseFloat(gpggaDatas[4]);
        }
        if (gpggaDatas[5].isEmpty()) {
            this.mRemoteGnssLocation.directionOfLong = -1;
        } else if (gpggaDatas[5].equals(EAST_DATA_TAG)) {
            this.mRemoteGnssLocation.directionOfLong = 0;
        } else if (gpggaDatas[5].equals(WEST_DATA_TAG)) {
            this.mRemoteGnssLocation.directionOfLong = 1;
        } else {
            return;
        }
        if (gpggaDatas[6].isEmpty()) {
            this.mRemoteGnssLocation.gpsQualityIndicator = -1;
        } else {
            this.mRemoteGnssLocation.gpsQualityIndicator = Integer.parseInt(gpggaDatas[6]);
        }
        assignGpggaDataExtra(gpggaDatas);
    }

    private void assignGpggaDataExtra(String[] gpggaDatas) {
        if (gpggaDatas[7].isEmpty()) {
            this.mRemoteGnssLocation.numOfInUse = -1;
        } else {
            this.mRemoteGnssLocation.numOfInUse = Integer.parseInt(gpggaDatas[7]);
        }
        if (gpggaDatas[8].isEmpty()) {
            this.mRemoteGnssLocation.hdop = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.hdop = Float.parseFloat(gpggaDatas[8]);
        }
        if (gpggaDatas[9].isEmpty()) {
            this.mRemoteGnssLocation.orthometriHeight = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.orthometriHeight = Float.parseFloat(gpggaDatas[9]);
        }
        if (gpggaDatas[11].isEmpty()) {
            this.mRemoteGnssLocation.geoidSeparation = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.geoidSeparation = Float.parseFloat(gpggaDatas[11]);
        }
        if (gpggaDatas.length == 13) {
            IRemoteVehicleInterface.RemoteGnssLocation remoteGnssLocation = this.mRemoteGnssLocation;
            remoteGnssLocation.ageOfDiffGpsData = -1;
            remoteGnssLocation.referenceStationId = -1;
        }
        if (gpggaDatas.length == 14) {
            this.mRemoteGnssLocation.ageOfDiffGpsData = Integer.parseInt(gpggaDatas[13]);
            this.mRemoteGnssLocation.referenceStationId = -1;
        }
        if (gpggaDatas.length == 15) {
            this.mRemoteGnssLocation.ageOfDiffGpsData = Integer.parseInt(gpggaDatas[13]);
            this.mRemoteGnssLocation.referenceStationId = Integer.parseInt(gpggaDatas[14]);
        }
    }

    private void assignGprmcData(String[] gprmcDatas) {
        if (gprmcDatas[7].isEmpty()) {
            this.mRemoteGnssLocation.speedOverGround = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.speedOverGround = Float.parseFloat(gprmcDatas[7]);
        }
        if (gprmcDatas[8].isEmpty()) {
            this.mRemoteGnssLocation.trackAngle = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.trackAngle = Float.parseFloat(gprmcDatas[8]);
        }
        if (gprmcDatas[9].isEmpty()) {
            this.mRemoteGnssLocation.date = -1;
        } else {
            this.mRemoteGnssLocation.date = Integer.parseInt(gprmcDatas[9]);
        }
        if (gprmcDatas[10].isEmpty()) {
            this.mRemoteGnssLocation.magneticVariation = FLOAT_DEFAULT_VALUE;
        } else {
            this.mRemoteGnssLocation.magneticVariation = Float.parseFloat(gprmcDatas[10]);
        }
        if (gprmcDatas[11].isEmpty()) {
            this.mRemoteGnssLocation.directionOfMag = -1;
        } else if (EAST_DATA_TAG.equals(gprmcDatas[11])) {
            this.mRemoteGnssLocation.directionOfMag = 0;
        } else if (WEST_DATA_TAG.equals(gprmcDatas[11])) {
            this.mRemoteGnssLocation.directionOfMag = 1;
        }
    }

    private IRemoteVehicleInterface.RemoteGnssLocation splitGnssLocationData(String gnssLocation) {
        String[] mGpggaDatas;
        String[] mGprmcDatas;
        this.mRemoteGnssLocation = new IRemoteVehicleInterface.RemoteGnssLocation();
        if (gnssLocation.length() > 0) {
            LBSLog.i(TAG, false, "gnssLocationstringlength: %{public}d", Integer.valueOf(gnssLocation.length()));
            String[] sourceStrArrays = gnssLocation.split("\\$");
            if (sourceStrArrays[1].indexOf("*") != -1) {
                String[] mGpggaMidValues = sourceStrArrays[1].split("\\*");
                LBSLog.i(TAG, false, "mGpggaMidValues[0] = %{public}s", mGpggaMidValues[0]);
                mGpggaDatas = mGpggaMidValues[0].split(",");
            } else {
                mGpggaDatas = sourceStrArrays[1].split(",");
            }
            if (sourceStrArrays[2].indexOf("*") != -1) {
                String[] mGprmcMidValues = sourceStrArrays[2].split("\\*");
                LBSLog.i(TAG, false, "mGprmcMidValues[0] = %{public}s", mGprmcMidValues[0]);
                mGprmcDatas = mGprmcMidValues[0].split(",");
            } else {
                mGprmcDatas = sourceStrArrays[2].split(",");
            }
            try {
                assignGpggaData(mGpggaDatas);
                assignGprmcData(mGprmcDatas);
                LBSLog.i(TAG, false, "GnssLocationThe Utctime is %{public}d", Integer.valueOf(this.mRemoteGnssLocation.utc));
            } catch (ClassCastException e) {
                LBSLog.e(TAG, false, "spilt GPRMC error : ClassCastException", new Object[0]);
                this.mRemoteGnssLocation = new IRemoteVehicleInterface.RemoteGnssLocation();
            } catch (NumberFormatException e2) {
                LBSLog.e(TAG, false, "spilt GPRMC error : NumberFormatException", new Object[0]);
                this.mRemoteGnssLocation = new IRemoteVehicleInterface.RemoteGnssLocation();
            }
            return this.mRemoteGnssLocation;
        }
        LBSLog.e(TAG, false, "gnssLocation string is null", new Object[0]);
        return this.mRemoteGnssLocation;
    }

    private ArrayList<IRemoteVehicleInterface.RemoteSensorData> splitSensorData(int count, String[] sensorDatas) {
        ArrayList<IRemoteVehicleInterface.RemoteSensorData> sensorDataList = new ArrayList<>(count);
        if (sensorDatas.length - 3 < count * 5) {
            LBSLog.e(TAG, false, "sensorDate is not right", new Object[0]);
            return sensorDataList;
        }
        for (int i = 0; i < count; i++) {
            try {
                this.mRemoteSensorData = new IRemoteVehicleInterface.RemoteSensorData();
                if (sensorDatas[(i * 5) + 3 + 1].isEmpty()) {
                    this.mRemoteSensorData.utc = -1;
                } else {
                    this.mRemoteSensorData.utc = (int) (Float.parseFloat(sensorDatas[(i * 5) + 3 + 1]) * 1000.0f);
                }
                if (sensorDatas[(i * 5) + 3 + 2].isEmpty()) {
                    this.mRemoteSensorData.date = -1;
                } else {
                    this.mRemoteSensorData.date = Integer.parseInt(sensorDatas[(i * 5) + 3 + 2]);
                }
                if (sensorDatas[(i * 5) + 3 + 3].isEmpty()) {
                    this.mRemoteSensorData.x = FLOAT_DEFAULT_VALUE;
                } else {
                    this.mRemoteSensorData.x = Float.parseFloat(sensorDatas[(i * 5) + 3 + 3]);
                }
                if (sensorDatas[(i * 5) + 3 + 4].isEmpty()) {
                    this.mRemoteSensorData.y = FLOAT_DEFAULT_VALUE;
                } else {
                    this.mRemoteSensorData.y = Float.parseFloat(sensorDatas[(i * 5) + 3 + 4]);
                }
                if (sensorDatas[(i * 5) + 3 + 5].isEmpty()) {
                    this.mRemoteSensorData.z = FLOAT_DEFAULT_VALUE;
                } else {
                    this.mRemoteSensorData.z = Float.parseFloat(sensorDatas[(i * 5) + 3 + 5]);
                }
                sensorDataList.add(this.mRemoteSensorData);
            } catch (IndexOutOfBoundsException e) {
                LBSLog.e(TAG, false, "splitSensorData OutOfBoundsException", new Object[0]);
            } catch (ClassCastException e2) {
                LBSLog.e(TAG, false, "splitSensorData ClassCastException", new Object[0]);
            } catch (NumberFormatException e3) {
                LBSLog.e(TAG, false, "splitSensorData NumberFormatException", new Object[0]);
            }
        }
        return sensorDataList;
    }

    /* access modifiers changed from: package-private */
    public class VehicleDataCallback extends IRemoteVehicleCallback.Stub {
        VehicleDataCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IRemoteVehicleCallback
        public void sendMsgToVehCallback(String message) {
            if (VehicleHardware.this.mCallback == null) {
                return;
            }
            if (message == null) {
                LBSLog.w(VehicleHardware.TAG, false, "msg is null", new Object[0]);
                return;
            }
            if (message.indexOf(VehicleHardware.SYNC_DATA_TAG) != -1) {
                VehicleHardware.this.currentTimeFirst = System.currentTimeMillis();
                LBSLog.i(VehicleHardware.TAG, false, "VehicleData is SYNC", new Object[0]);
            }
            VehicleHardware.this.mCallback.sendMsgToVehCallback(message);
        }
    }
}
