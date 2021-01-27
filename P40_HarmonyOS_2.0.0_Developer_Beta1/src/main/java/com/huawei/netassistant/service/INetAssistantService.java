package com.huawei.netassistant.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.netassistant.common.SimCardSettingsInfo;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import java.util.List;

public interface INetAssistantService extends IInterface {
    void clearDailyWarnPreference(String str) throws RemoteException;

    void clearMonthLimitPreference(String str) throws RemoteException;

    void clearMonthWarnPreference(String str) throws RemoteException;

    List getAbnormalMobileAppList(String str) throws RemoteException;

    List getAbnormalWifiAppList() throws RemoteException;

    String getAdjustBrand(String str) throws RemoteException;

    String getAdjustCity(String str) throws RemoteException;

    long getAdjustDate(String str) throws RemoteException;

    long getAdjustPackageValue(String str) throws RemoteException;

    String getAdjustProvider(String str) throws RemoteException;

    String getAdjustProvince(String str) throws RemoteException;

    List getAppItemByUid(String str, int i, long j, long j2, long j3) throws RemoteException;

    long getBackGroundBytesByUid(String str, int i, long j, long j2, long j3) throws RemoteException;

    List getDayPerHourTraffic(String str, int i) throws RemoteException;

    long getForeGroundBytesByUid(String str, int i, long j, long j2, long j3) throws RemoteException;

    List getMonth4GMobileAppList(String str) throws RemoteException;

    long getMonthMobileTotalBytes(String str) throws RemoteException;

    List getMonthPerDayTraffic(String str, int i) throws RemoteException;

    List getMonthTrafficDailyDetailList(String str) throws RemoteException;

    long getMonthWifiBackBytes() throws RemoteException;

    long getMonthWifiForeBytes() throws RemoteException;

    long getMonthWifiTotalBytes() throws RemoteException;

    long getMonthlyTotalBytes(String str) throws RemoteException;

    int getNetworkUsageDays(String str) throws RemoteException;

    long getPeriodMobileTotalBytes(String str, long j, long j2) throws RemoteException;

    List getPeriodMobileTrafficAppList(String str, long j, long j2) throws RemoteException;

    long getPeriodWifiTotalBytes(long j, long j2) throws RemoteException;

    List getPeriodWifiTrafficAppList(long j, long j2) throws RemoteException;

    int getSettingBeginDate(String str) throws RemoteException;

    int getSettingExcessMontyType(String str) throws RemoteException;

    int getSettingNotify(String str) throws RemoteException;

    int getSettingOverMarkDay(String str) throws RemoteException;

    int getSettingOverMarkMonth(String str) throws RemoteException;

    int getSettingRegularAdjustType(String str) throws RemoteException;

    int getSettingSpeedNotify(String str) throws RemoteException;

    long getSettingTotalPackage(String str) throws RemoteException;

    int getSettingUnlockScreen(String str) throws RemoteException;

    String getSimCardOperatorName(int i) throws RemoteException;

    SimCardSettingsInfo getSimCardSettingsInfo(String str) throws RemoteException;

    Bundle getSimProfileDes() throws RemoteException;

    long getTodayMobileTotalBytes(String str) throws RemoteException;

    long getTodayWifiTotalBytes() throws RemoteException;

    boolean isUnlimitedDataSet(String str) throws RemoteException;

    void putSimProfileDes(SimProfileDes simProfileDes) throws RemoteException;

    void sendAdjustSMS(String str) throws RemoteException;

    boolean setAdjustItemInfo(String str, int i, long j) throws RemoteException;

    boolean setAppNetMode(String str, int i) throws RemoteException;

    boolean setNetAccessInfo(int i, int i2) throws RemoteException;

    boolean setOperatorInfo(String str, String str2, String str3) throws RemoteException;

    boolean setProvinceInfo(String str, String str2, String str3) throws RemoteException;

    boolean setSettingBeginDate(String str, int i) throws RemoteException;

    boolean setSettingExcessMontyType(String str, int i) throws RemoteException;

    boolean setSettingNotify(String str, int i) throws RemoteException;

    boolean setSettingOverMarkDay(String str, int i) throws RemoteException;

    boolean setSettingOverMarkMonth(String str, int i) throws RemoteException;

    boolean setSettingRegularAdjustType(String str, int i) throws RemoteException;

    boolean setSettingSpeedNotify(String str, int i) throws RemoteException;

    boolean setSettingTotalPackage(String str, long j) throws RemoteException;

    boolean setSettingUnlockScreen(String str, int i) throws RemoteException;

    void startSpeedUpdate() throws RemoteException;

    void stopSpeedUpdate() throws RemoteException;

    public static class Default implements INetAssistantService {
        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getMonthMobileTotalBytes(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getMonthlyTotalBytes(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getMonthWifiTotalBytes() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getTodayMobileTotalBytes(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getTodayWifiTotalBytes() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getAbnormalMobileAppList(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getAbnormalWifiAppList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getMonthWifiBackBytes() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getMonthWifiForeBytes() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getPeriodMobileTotalBytes(String imsi, long start, long end) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getPeriodWifiTotalBytes(long start, long end) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setAdjustItemInfo(String imsi, int adjustType, long value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setProvinceInfo(String imsi, String provinceCode, String cityId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setOperatorInfo(String imsi, String providerCode, String brandCode) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingTotalPackage(String imsi, long value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingBeginDate(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingRegularAdjustType(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingExcessMontyType(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingOverMarkMonth(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingOverMarkDay(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingUnlockScreen(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingNotify(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setSettingSpeedNotify(String imsi, int value) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getSettingTotalPackage(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingBeginDate(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingRegularAdjustType(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingExcessMontyType(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingOverMarkMonth(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingOverMarkDay(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingUnlockScreen(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingNotify(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getSettingSpeedNotify(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getAdjustPackageValue(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getAdjustDate(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public String getAdjustProvince(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public String getAdjustCity(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public String getAdjustProvider(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public String getAdjustBrand(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public SimCardSettingsInfo getSimCardSettingsInfo(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setNetAccessInfo(int uid, int setNetAccessInfos) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void sendAdjustSMS(String imsi) throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getMonth4GMobileAppList(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getPeriodMobileTrafficAppList(String imsi, long startTime, long endTime) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getPeriodWifiTrafficAppList(long startTime, long endTime) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getMonthTrafficDailyDetailList(String imsi) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getMonthPerDayTraffic(String template, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getDayPerHourTraffic(String template, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getForeGroundBytesByUid(String template, int uid, long start, long end, long now) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public long getBackGroundBytesByUid(String template, int uid, long start, long end, long now) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public List getAppItemByUid(String template, int uid, long start, long end, long now) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public String getSimCardOperatorName(int slot) throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void startSpeedUpdate() throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void stopSpeedUpdate() throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void clearDailyWarnPreference(String imsi) throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void clearMonthLimitPreference(String imsi) throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void clearMonthWarnPreference(String imsi) throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public void putSimProfileDes(SimProfileDes info) throws RemoteException {
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public Bundle getSimProfileDes() throws RemoteException {
            return null;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public int getNetworkUsageDays(String imsi) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean setAppNetMode(String pkgName, int mode) throws RemoteException {
            return false;
        }

        @Override // com.huawei.netassistant.service.INetAssistantService
        public boolean isUnlimitedDataSet(String imsi) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetAssistantService {
        private static final String DESCRIPTOR = "com.huawei.netassistant.service.INetAssistantService";
        static final int TRANSACTION_clearDailyWarnPreference = 54;
        static final int TRANSACTION_clearMonthLimitPreference = 55;
        static final int TRANSACTION_clearMonthWarnPreference = 56;
        static final int TRANSACTION_getAbnormalMobileAppList = 6;
        static final int TRANSACTION_getAbnormalWifiAppList = 7;
        static final int TRANSACTION_getAdjustBrand = 38;
        static final int TRANSACTION_getAdjustCity = 36;
        static final int TRANSACTION_getAdjustDate = 34;
        static final int TRANSACTION_getAdjustPackageValue = 33;
        static final int TRANSACTION_getAdjustProvider = 37;
        static final int TRANSACTION_getAdjustProvince = 35;
        static final int TRANSACTION_getAppItemByUid = 50;
        static final int TRANSACTION_getBackGroundBytesByUid = 49;
        static final int TRANSACTION_getDayPerHourTraffic = 47;
        static final int TRANSACTION_getForeGroundBytesByUid = 48;
        static final int TRANSACTION_getMonth4GMobileAppList = 42;
        static final int TRANSACTION_getMonthMobileTotalBytes = 1;
        static final int TRANSACTION_getMonthPerDayTraffic = 46;
        static final int TRANSACTION_getMonthTrafficDailyDetailList = 45;
        static final int TRANSACTION_getMonthWifiBackBytes = 8;
        static final int TRANSACTION_getMonthWifiForeBytes = 9;
        static final int TRANSACTION_getMonthWifiTotalBytes = 3;
        static final int TRANSACTION_getMonthlyTotalBytes = 2;
        static final int TRANSACTION_getNetworkUsageDays = 59;
        static final int TRANSACTION_getPeriodMobileTotalBytes = 10;
        static final int TRANSACTION_getPeriodMobileTrafficAppList = 43;
        static final int TRANSACTION_getPeriodWifiTotalBytes = 11;
        static final int TRANSACTION_getPeriodWifiTrafficAppList = 44;
        static final int TRANSACTION_getSettingBeginDate = 25;
        static final int TRANSACTION_getSettingExcessMontyType = 27;
        static final int TRANSACTION_getSettingNotify = 31;
        static final int TRANSACTION_getSettingOverMarkDay = 29;
        static final int TRANSACTION_getSettingOverMarkMonth = 28;
        static final int TRANSACTION_getSettingRegularAdjustType = 26;
        static final int TRANSACTION_getSettingSpeedNotify = 32;
        static final int TRANSACTION_getSettingTotalPackage = 24;
        static final int TRANSACTION_getSettingUnlockScreen = 30;
        static final int TRANSACTION_getSimCardOperatorName = 51;
        static final int TRANSACTION_getSimCardSettingsInfo = 39;
        static final int TRANSACTION_getSimProfileDes = 58;
        static final int TRANSACTION_getTodayMobileTotalBytes = 4;
        static final int TRANSACTION_getTodayWifiTotalBytes = 5;
        static final int TRANSACTION_isUnlimitedDataSet = 61;
        static final int TRANSACTION_putSimProfileDes = 57;
        static final int TRANSACTION_sendAdjustSMS = 41;
        static final int TRANSACTION_setAdjustItemInfo = 12;
        static final int TRANSACTION_setAppNetMode = 60;
        static final int TRANSACTION_setNetAccessInfo = 40;
        static final int TRANSACTION_setOperatorInfo = 14;
        static final int TRANSACTION_setProvinceInfo = 13;
        static final int TRANSACTION_setSettingBeginDate = 16;
        static final int TRANSACTION_setSettingExcessMontyType = 18;
        static final int TRANSACTION_setSettingNotify = 22;
        static final int TRANSACTION_setSettingOverMarkDay = 20;
        static final int TRANSACTION_setSettingOverMarkMonth = 19;
        static final int TRANSACTION_setSettingRegularAdjustType = 17;
        static final int TRANSACTION_setSettingSpeedNotify = 23;
        static final int TRANSACTION_setSettingTotalPackage = 15;
        static final int TRANSACTION_setSettingUnlockScreen = 21;
        static final int TRANSACTION_startSpeedUpdate = 52;
        static final int TRANSACTION_stopSpeedUpdate = 53;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetAssistantService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetAssistantService)) {
                return new Proxy(obj);
            }
            return (INetAssistantService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SimProfileDes _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = getMonthMobileTotalBytes(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = getMonthlyTotalBytes(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = getMonthWifiTotalBytes();
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getTodayMobileTotalBytes(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result5 = getTodayWifiTotalBytes();
                        reply.writeNoException();
                        reply.writeLong(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List _result6 = getAbnormalMobileAppList(data.readString());
                        reply.writeNoException();
                        reply.writeList(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List _result7 = getAbnormalWifiAppList();
                        reply.writeNoException();
                        reply.writeList(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        long _result8 = getMonthWifiBackBytes();
                        reply.writeNoException();
                        reply.writeLong(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = getMonthWifiForeBytes();
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        long _result10 = getPeriodMobileTotalBytes(data.readString(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeLong(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        long _result11 = getPeriodWifiTotalBytes(data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeLong(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean adjustItemInfo = setAdjustItemInfo(data.readString(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(adjustItemInfo ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean provinceInfo = setProvinceInfo(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(provinceInfo ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean operatorInfo = setOperatorInfo(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(operatorInfo ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingTotalPackage = setSettingTotalPackage(data.readString(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(settingTotalPackage ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingBeginDate = setSettingBeginDate(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingBeginDate ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingRegularAdjustType = setSettingRegularAdjustType(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingRegularAdjustType ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingExcessMontyType = setSettingExcessMontyType(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingExcessMontyType ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingOverMarkMonth = setSettingOverMarkMonth(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingOverMarkMonth ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingOverMarkDay = setSettingOverMarkDay(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingOverMarkDay ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingUnlockScreen = setSettingUnlockScreen(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingUnlockScreen ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingNotify = setSettingNotify(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingNotify ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean settingSpeedNotify = setSettingSpeedNotify(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(settingSpeedNotify ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        long _result12 = getSettingTotalPackage(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result12);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getSettingBeginDate(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getSettingRegularAdjustType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getSettingExcessMontyType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getSettingOverMarkMonth(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getSettingOverMarkDay(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getSettingUnlockScreen(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = getSettingNotify(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getSettingSpeedNotify(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        long _result21 = getAdjustPackageValue(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result21);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        long _result22 = getAdjustDate(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result22);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _result23 = getAdjustProvince(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result23);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getAdjustCity(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String _result25 = getAdjustProvider(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result25);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        String _result26 = getAdjustBrand(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result26);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        SimCardSettingsInfo _result27 = getSimCardSettingsInfo(data.readString());
                        reply.writeNoException();
                        if (_result27 != null) {
                            reply.writeInt(1);
                            _result27.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean netAccessInfo = setNetAccessInfo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(netAccessInfo ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        sendAdjustSMS(data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        List _result28 = getMonth4GMobileAppList(data.readString());
                        reply.writeNoException();
                        reply.writeList(_result28);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        List _result29 = getPeriodMobileTrafficAppList(data.readString(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeList(_result29);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        List _result30 = getPeriodWifiTrafficAppList(data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeList(_result30);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        List _result31 = getMonthTrafficDailyDetailList(data.readString());
                        reply.writeNoException();
                        reply.writeList(_result31);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        List _result32 = getMonthPerDayTraffic(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeList(_result32);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        List _result33 = getDayPerHourTraffic(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeList(_result33);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        long _result34 = getForeGroundBytesByUid(data.readString(), data.readInt(), data.readLong(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeLong(_result34);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        long _result35 = getBackGroundBytesByUid(data.readString(), data.readInt(), data.readLong(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeLong(_result35);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        List _result36 = getAppItemByUid(data.readString(), data.readInt(), data.readLong(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeList(_result36);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        String _result37 = getSimCardOperatorName(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result37);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        startSpeedUpdate();
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        stopSpeedUpdate();
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        clearDailyWarnPreference(data.readString());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        clearMonthLimitPreference(data.readString());
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        clearMonthWarnPreference(data.readString());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SimProfileDes.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        putSimProfileDes(_arg0);
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result38 = getSimProfileDes();
                        reply.writeNoException();
                        if (_result38 != null) {
                            reply.writeInt(1);
                            _result38.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getNetworkUsageDays /* 59 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result39 = getNetworkUsageDays(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result39);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        boolean appNetMode = setAppNetMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(appNetMode ? 1 : 0);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUnlimitedDataSet = isUnlimitedDataSet(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isUnlimitedDataSet ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetAssistantService {
            public static INetAssistantService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getMonthMobileTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthMobileTotalBytes(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getMonthlyTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthlyTotalBytes(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getMonthWifiTotalBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthWifiTotalBytes();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getTodayMobileTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTodayMobileTotalBytes(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getTodayWifiTotalBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTodayWifiTotalBytes();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getAbnormalMobileAppList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAbnormalMobileAppList(imsi);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getAbnormalWifiAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAbnormalWifiAppList();
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getMonthWifiBackBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthWifiBackBytes();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getMonthWifiForeBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthWifiForeBytes();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getPeriodMobileTotalBytes(String imsi, long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeriodMobileTotalBytes(imsi, start, end);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getPeriodWifiTotalBytes(long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeriodWifiTotalBytes(start, end);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setAdjustItemInfo(String imsi, int adjustType, long value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(adjustType);
                    _data.writeLong(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAdjustItemInfo(imsi, adjustType, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setProvinceInfo(String imsi, String provinceCode, String cityId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(provinceCode);
                    _data.writeString(cityId);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProvinceInfo(imsi, provinceCode, cityId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setOperatorInfo(String imsi, String providerCode, String brandCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(providerCode);
                    _data.writeString(brandCode);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOperatorInfo(imsi, providerCode, brandCode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingTotalPackage(String imsi, long value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingTotalPackage(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingBeginDate(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingBeginDate(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingRegularAdjustType(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingRegularAdjustType(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingExcessMontyType(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingExcessMontyType(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingOverMarkMonth(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingOverMarkMonth(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingOverMarkDay(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingOverMarkDay(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingUnlockScreen(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingUnlockScreen(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingNotify(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingNotify(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setSettingSpeedNotify(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSettingSpeedNotify(imsi, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getSettingTotalPackage(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingTotalPackage(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingBeginDate(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingBeginDate(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingRegularAdjustType(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingRegularAdjustType(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingExcessMontyType(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingExcessMontyType(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingOverMarkMonth(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingOverMarkMonth(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingOverMarkDay(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingOverMarkDay(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingUnlockScreen(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingUnlockScreen(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingNotify(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingNotify(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getSettingSpeedNotify(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSettingSpeedNotify(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getAdjustPackageValue(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustPackageValue(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getAdjustDate(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustDate(imsi);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public String getAdjustProvince(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustProvince(imsi);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public String getAdjustCity(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustCity(imsi);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public String getAdjustProvider(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustProvider(imsi);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public String getAdjustBrand(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdjustBrand(imsi);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public SimCardSettingsInfo getSimCardSettingsInfo(String imsi) throws RemoteException {
                SimCardSettingsInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimCardSettingsInfo(imsi);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SimCardSettingsInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setNetAccessInfo(int uid, int setNetAccessInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(setNetAccessInfos);
                    boolean _result = false;
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNetAccessInfo(uid, setNetAccessInfos);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void sendAdjustSMS(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendAdjustSMS(imsi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getMonth4GMobileAppList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonth4GMobileAppList(imsi);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getPeriodMobileTrafficAppList(String imsi, long startTime, long endTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeriodMobileTrafficAppList(imsi, startTime, endTime);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getPeriodWifiTrafficAppList(long startTime, long endTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeriodWifiTrafficAppList(startTime, endTime);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getMonthTrafficDailyDetailList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthTrafficDailyDetailList(imsi);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getMonthPerDayTraffic(String template, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(template);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonthPerDayTraffic(template, uid);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getDayPerHourTraffic(String template, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(template);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDayPerHourTraffic(template, uid);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getForeGroundBytesByUid(String template, int uid, long start, long end, long now) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(template);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(start);
                        _data.writeLong(end);
                        _data.writeLong(now);
                        if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            long _result = _reply.readLong();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        long foreGroundBytesByUid = Stub.getDefaultImpl().getForeGroundBytesByUid(template, uid, start, end, now);
                        _reply.recycle();
                        _data.recycle();
                        return foreGroundBytesByUid;
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public long getBackGroundBytesByUid(String template, int uid, long start, long end, long now) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(template);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(start);
                        _data.writeLong(end);
                        _data.writeLong(now);
                        if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            long _result = _reply.readLong();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        long backGroundBytesByUid = Stub.getDefaultImpl().getBackGroundBytesByUid(template, uid, start, end, now);
                        _reply.recycle();
                        _data.recycle();
                        return backGroundBytesByUid;
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public List getAppItemByUid(String template, int uid, long start, long end, long now) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(template);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(start);
                        _data.writeLong(end);
                        _data.writeLong(now);
                        if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            List _result = _reply.readArrayList(getClass().getClassLoader());
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        List appItemByUid = Stub.getDefaultImpl().getAppItemByUid(template, uid, start, end, now);
                        _reply.recycle();
                        _data.recycle();
                        return appItemByUid;
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public String getSimCardOperatorName(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimCardOperatorName(slot);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void startSpeedUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startSpeedUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void stopSpeedUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopSpeedUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void clearDailyWarnPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearDailyWarnPreference(imsi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void clearMonthLimitPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearMonthLimitPreference(imsi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void clearMonthWarnPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearMonthWarnPreference(imsi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public void putSimProfileDes(SimProfileDes info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().putSimProfileDes(info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public Bundle getSimProfileDes() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimProfileDes();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public int getNetworkUsageDays(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNetworkUsageDays, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkUsageDays(imsi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean setAppNetMode(String pkgName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAppNetMode(pkgName, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.netassistant.service.INetAssistantService
            public boolean isUnlimitedDataSet(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    boolean _result = false;
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUnlimitedDataSet(imsi);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetAssistantService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetAssistantService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
