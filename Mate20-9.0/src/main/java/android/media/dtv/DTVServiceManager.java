package android.media.dtv;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;

public class DTVServiceManager {
    private static final int RET_FAIL = -1;
    public static final String TAG = "DTVServiceManager";
    private Context mContext;
    private IDTVService mDTVService;

    public DTVServiceManager(Context context, IDTVService dtvService) {
        this.mDTVService = dtvService;
        this.mContext = context;
    }

    public int tsidProcess(int ts_id) {
        try {
            return this.mDTVService.tsidProcess(ts_id);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tsidProcess error");
            return -1;
        }
    }

    public int emmProcess(byte[] data, int len) {
        try {
            return this.mDTVService.emmProcess(data, len);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService emmProcess error");
            return -1;
        }
    }

    public int ecmProcess(byte[] data, int len) {
        try {
            return this.mDTVService.ecmProcess(data, len);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService ecmProcess error");
            return -1;
        }
    }

    public byte[] getScrambleKey(int uKeyLen) {
        try {
            return this.mDTVService.getScrambleKey(uKeyLen);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService getScrambleKey error");
            return new byte[0];
        }
    }

    public byte[] getSystemKey(int uLen) {
        try {
            return this.mDTVService.getSystemKey(uLen);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService getSystemKey error");
            return new byte[0];
        }
    }

    public byte[] getInitialCbc(int uLen) {
        try {
            return this.mDTVService.getInitialCbc(uLen);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService getInitialCbc error");
            return new byte[0];
        }
    }

    public int tunerxStart() {
        try {
            return this.mDTVService.tunerxStart();
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxStart error");
            return -1;
        }
    }

    public int tunerxTuning(int tunerBand, int ch) {
        try {
            return this.mDTVService.tunerxTuning(tunerBand, ch);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxTuning error");
            return -1;
        }
    }

    public int tunerxCsTuning(int tunerBand, int ch, int[] lockFlg) {
        try {
            return this.mDTVService.tunerxCsTuning(tunerBand, ch, lockFlg);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxCsTuning error");
            return -1;
        }
    }

    public int tunerxGetMonInfo(TunerMonitoringInfo tunerMonitoringInfo) {
        try {
            return this.mDTVService.tunerxGetMonInfo(tunerMonitoringInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetMonInfo error");
            return -1;
        }
    }

    public int tunerxGetTmccInfo(TunerTMCCInfo tunerTMCCInfo) {
        try {
            return this.mDTVService.tunerxGetTmccInfo(tunerTMCCInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetTmccInfo error");
            return -1;
        }
    }

    public int tunerxGetAc(TunerACInfo tunerACInfo, TunerSyncInfo tunerSyncInfo, long toms) {
        try {
            return this.mDTVService.tunerxGetAc(tunerACInfo, tunerSyncInfo, toms);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetAc error");
            return -1;
        }
    }

    public int tunerxGetRssiInfo(TunerRSSIInfo tunerRSSIInfo) {
        try {
            return this.mDTVService.tunerxGetRssiInfo(tunerRSSIInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetRssiInfo error");
            return -1;
        }
    }

    public int tunerxGetSyncInfo(TunerSyncInfo tunerSyncInfo) {
        try {
            return this.mDTVService.tunerxGetSyncInfo(tunerSyncInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetSyncInfo error");
            return -1;
        }
    }

    public int tunerxGetCnInfo(TunerCNInfo tunerCNInfo) {
        try {
            return this.mDTVService.tunerxGetCnInfo(tunerCNInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetCnInfo error");
            return -1;
        }
    }

    public int tunerxGetBper(TunerBperInfo tunerBperInfo) {
        try {
            return this.mDTVService.tunerxGetBper(tunerBperInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxGetBper error");
            return -1;
        }
    }

    public int tunerxTsStart(TunerDataTsif tunerDataTsif) {
        try {
            return this.mDTVService.tunerxTsStart(tunerDataTsif);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxTsStart error");
            return -1;
        }
    }

    public int tunerxTsRead(TunerChData tunerChData) {
        try {
            return this.mDTVService.tunerxTsRead(tunerChData);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxReadTs error");
            return -1;
        }
    }

    public int tunerxTsStop() {
        try {
            return this.mDTVService.tunerxTsStop();
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxTsStop error");
            return -1;
        }
    }

    public TuningTargetChannel[] tunerxCsAddch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) {
        try {
            return this.mDTVService.tunerxCsAddch(tunerBand, ch, tuningTargetChannels);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxCsAddch error");
            return new TuningTargetChannel[0];
        }
    }

    public TuningTargetChannel[] tunerxCsDelch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) {
        try {
            return this.mDTVService.tunerxCsDelch(tunerBand, ch, tuningTargetChannels);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxCsDelch error");
            return new TuningTargetChannel[0];
        }
    }

    public int tunerxSearchCh(List<TuningTargetChannel> tuningTargetChannels) {
        try {
            return this.mDTVService.tunerxSearchCh(tuningTargetChannels);
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxSearchCh error");
            return -1;
        }
    }

    public void tunerxEnd() {
        try {
            this.mDTVService.tunerxEnd();
        } catch (RemoteException e) {
            Log.e(TAG, "call DTVService tunerxEnd error");
        }
    }
}
