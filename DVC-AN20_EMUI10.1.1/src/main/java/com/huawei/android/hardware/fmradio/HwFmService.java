package com.huawei.android.hardware.fmradio;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.hardware.fmradio.IHwFmService;
import com.huawei.android.hardware.fmradio.common.BaseHwFmService;
import com.huawei.android.hardware.fmradio.common.FmUtils;
import com.huawei.android.hardware.hisifmradio.HisiFmService;
import com.huawei.android.hardware.mtkfmradio.MtkFmConfig;
import com.huawei.android.hardware.mtkfmradio.MtkFmService;
import com.huawei.android.hardware.qcomfmradio.QcomFmService;

public class HwFmService extends IHwFmService.Stub {
    private static final String FM_PERMISSION = "com.huawei.permission.ACCESS_FM";
    private final Context mContext;
    private final BaseHwFmService mService;

    public HwFmService(Context context) {
        this.mContext = context;
        if (FmUtils.isQcomPlatform()) {
            this.mService = new QcomFmService(context);
        } else if (FmUtils.isMtkPlatform()) {
            this.mService = new MtkFmService(context);
        } else {
            this.mService = new HisiFmService(context);
        }
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int acquireFd(String path) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.acquireFd(path);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int audioControl(int fd, int control, int field) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.audioControl(fd, control, field);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int cancelSearch(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.cancelSearch(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int closeFd(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.closeFd(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getFreq(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getFreq(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setFreq(int fd, int freq) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setFreq(fd, freq);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getControl(int fd, int id) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getControl(fd, id);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setControl(int fd, int id, int value) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setControl(fd, id, value);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int startSearch(int fd, int dir) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.startSearch(fd, dir);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getBuffer(int fd, byte[] buff, int index) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getBuffer(fd, buff, index);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getRSSI(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getRSSI(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setBand(int fd, int low, int high) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setBand(fd, low, high);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getLowerBand(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getLowerBand(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getUpperBand(int fd) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getUpperBand(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setMonoStereo(int fd, int val) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setMonoStereo(fd, val);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getRawRds(int fd, byte[] buff, int count) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getRawRds(fd, buff, count);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void setNotchFilter(boolean value) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.setNotchFilter(value);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getAudioQuilty(int fd, int value) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getAudioQuilty(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setFmSnrThresh(int fd, int value) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setFmSnrThresh(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int setFmRssiThresh(int fd, int value) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setFmRssiThresh(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void setFmDeviceConnectionState(int state) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.setFmDeviceConnectionState(state);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void startListner(int fd, IFmEventCallback cb) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.startListner(fd, cb);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void stopListner() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.stopListner();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean enable(MtkFmConfig configSettings, IBinder callback) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.enable(configSettings, callback);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean disable() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.disable();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean mtkCancelSearch() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.mtkCancelSearch();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.searchStations(mode, dwellPeriod, direction, pty, pi);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean setStation(int frequencyKHz) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setStation(frequencyKHz);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean setRdsOnOff(int onOff) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setRdsOnOff(onOff);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public String getPrgmServices() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getPrgmServices();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public String getRadioText() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getRadioText();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getPrgmType() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getPrgmType();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getPrgmId() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getPrgmId();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void registerListener(IBaseFmRxEvCallbacksAdaptor listener) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.registerListener(listener);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public void unregisterListener(IBaseFmRxEvCallbacksAdaptor listener) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        this.mService.unregisterListener(listener);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean setLowPwrMode(boolean val) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.setLowPwrMode(val);
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getRdsStatus() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getRdsStatus();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int getRssi() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getRssi();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public int[] getAfInfo() throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.getAfInfo();
    }

    @Override // com.huawei.android.hardware.fmradio.IHwFmService
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) throws RemoteException {
        this.mContext.enforceCallingPermission(FM_PERMISSION, "need FM permission");
        return this.mService.registerRdsGroupProcessing(fmGrpsToProc);
    }
}
