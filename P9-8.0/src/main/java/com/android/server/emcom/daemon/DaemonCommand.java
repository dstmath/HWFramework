package com.android.server.emcom.daemon;

import android.os.Message;
import android.os.Parcel;

public class DaemonCommand implements CommandsInterface {
    static final boolean DEBUG = false;
    static final String TAG = "DaemonCommand";
    private static DaemonCommand sInstance;
    private DaemonClientThread mDaemonClient = new DaemonClientThread();

    public interface DaemonReportCallback {
        void onReportDevFail();

        void onUpdateAppList(Parcel parcel);

        void onUpdateBrowserInfo(Parcel parcel);

        void onUpdateHttpInfo(Parcel parcel);

        void onUpdatePageId(int i);

        void onUpdateSampleWinStat(boolean z);

        void onUpdateTcpStatusInfo(Parcel parcel);
    }

    private DaemonCommand() {
    }

    public static synchronized DaemonCommand getInstance() {
        DaemonCommand daemonCommand;
        synchronized (DaemonCommand.class) {
            if (sInstance == null) {
                sInstance = new DaemonCommand();
            }
            daemonCommand = sInstance;
        }
        return daemonCommand;
    }

    public void execCloseSampleWin(Message result) {
        this.mDaemonClient.send(DaemonRequest.obtain(513, result));
    }

    public void exeBootComplete(Message result) {
        this.mDaemonClient.send(DaemonRequest.obtain(1, result));
    }

    public void exeAppForeground(int type, int uid, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(2, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(type);
        rr.mParcel.writeInt(uid);
        this.mDaemonClient.send(rr);
    }

    public void exePackageChanged(int type, String packageName, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(4, result);
        rr.mParcel.writeInt(type);
        rr.mParcel.writeString(packageName);
        this.mDaemonClient.send(rr);
    }

    public void exeScreenStatus(int type, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(3, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(type);
        this.mDaemonClient.send(rr);
    }

    public void exeStopAccelerate(int uid, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_STOP_ACC, result);
        if (rr != null) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uid);
            this.mDaemonClient.send(rr);
        }
    }

    public void exeStartAccelerate(int uid, int actionGrade, int mainCardPsStatus, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_START_ACC, result);
        if (rr != null) {
            rr.mParcel.writeInt(3);
            rr.mParcel.writeInt(uid);
            rr.mParcel.writeInt(actionGrade);
            rr.mParcel.writeInt(mainCardPsStatus);
            this.mDaemonClient.send(rr);
        }
    }

    public void exeConfigMpip(int[] UidRange, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_CONFIG_MPIP, result);
        if (rr != null) {
            if (UidRange.length > 0) {
                rr.mParcel.writeInt(UidRange.length);
                for (int writeInt : UidRange) {
                    rr.mParcel.writeInt(writeInt);
                }
            } else {
                rr.mParcel.writeInt(1);
                rr.mParcel.writeInt(0);
            }
            this.mDaemonClient.send(rr);
        }
    }

    public void exeStartMpip(String ifname, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_START_MPIP, result);
        if (rr != null) {
            rr.mParcel.writeString(ifname);
            this.mDaemonClient.send(rr);
        }
    }

    public void exeStopMpip(Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_STOP_MPIP, result);
        if (rr != null) {
            this.mDaemonClient.send(rr);
        }
    }

    public void registerDaemonCallback(DaemonReportCallback cb) {
        this.mDaemonClient.registerDaemonCallback(cb);
    }

    public void unRegisterDaemonCallback(DaemonReportCallback cb) {
        this.mDaemonClient.unRegisterDaemonCallback(cb);
    }

    public void exeConfigUpdate(Message result) {
        this.mDaemonClient.send(DaemonRequest.obtain(5, result));
    }

    public void exeSpeedCtrl(int uid, int size, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_XENGINE_SPEED_CTRL, result);
        if (rr != null) {
            rr.mParcel.writeInt(2);
            rr.mParcel.writeInt(uid);
            rr.mParcel.writeInt(size);
            this.mDaemonClient.send(rr);
        }
    }

    public void exeUdpAcc(int uid, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(260, result);
        if (rr != null) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uid);
            this.mDaemonClient.send(rr);
        }
    }

    public void exeUdpStop(int uid, Message result) {
        DaemonRequest rr = DaemonRequest.obtain(CommandsInterface.EMCOM_SD_STOP_UDP_RETRAN, result);
        if (rr != null) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uid);
            this.mDaemonClient.send(rr);
        }
    }
}
