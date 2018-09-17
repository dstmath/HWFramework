package com.huawei.hsm.transacthandler;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;
import com.huawei.android.os.BuildEx.VERSION_CODES;
import com.huawei.hsm.IHsmMusicWatch;
import com.huawei.hsm.IHsmMusicWatch.Stub;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;
import huawei.android.widget.ViewDragHelper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class MediaProcessHandler extends AbsTransactHandler {
    private static final String TAG = "MediaProcessHandler";
    private Map<Integer, IHsmMusicWatch> mMediaStatusObservers;
    private SparseIntArray mUidPidArray;

    public MediaProcessHandler() {
        this.mUidPidArray = new SparseIntArray();
        this.mMediaStatusObservers = new HashMap();
    }

    public void handleTransactCode(int code, Parcel data, Parcel reply) {
        switch (code) {
            case MotionTypeApps.TYPE_PICKUP_REDUCE_CLOCK /*102*/:
                data.enforceInterface("com.huawei.hsm.IHsmCoreService");
                int opType = data.readInt();
                int uid = data.readInt();
                int pid = data.readInt();
                reply.writeNoException();
                reply.writeInt(processOp(opType, uid, pid) ? 1 : 0);
                executeCallBack(opType, uid, pid);
            case MotionTypeApps.TYPE_PICKUP_END_HINTS /*103*/:
                data.enforceInterface("com.huawei.hsm.IHsmCoreService");
                reply.writeNoException();
                reply.writeString(playingUids());
            case MetricConstant.CAMERA_METRIC_ID_EX /*104*/:
                musicObserverOp(data, reply);
            default:
                Log.e(TAG, "handleTransact unknown code: " + code);
        }
    }

    public String toString() {
        return super.toString() + ", uidPidMap: " + this.mUidPidArray.toString();
    }

    private boolean processOp(int opType, int uid, int pid) {
        if (uid < VERSION_CODES.CUR_DEVELOPMENT) {
            Log.w(TAG, "processOp uid " + uid + " is not concerned!");
            return false;
        }
        Log.i(TAG, "processOp opType: " + opType + ", uid: " + uid + ", pid: " + pid);
        switch (opType) {
            case ViewDragHelper.STATE_IDLE /*0*/:
                return addProcess(uid, pid);
            case ViewDragHelper.STATE_DRAGGING /*1*/:
                return removeProcess(uid, pid);
            default:
                Log.e(TAG, "processOp invalid opType: " + opType);
                return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String playingUids() {
        StringBuilder sb = new StringBuilder();
        synchronized (this.mUidPidArray) {
            int i = 0;
            while (true) {
                if (i < this.mUidPidArray.size()) {
                    sb.append(this.mUidPidArray.keyAt(i)).append("|");
                    i++;
                }
            }
        }
        Log.i(TAG, "playingUids: " + sb.toString());
        return sb.toString();
    }

    private boolean addProcess(int uid, int pid) {
        int callerUid = Binder.getCallingUid();
        int callerPid = Binder.getCallingPid();
        if (uid == callerUid && pid == callerPid) {
            synchronized (this.mUidPidArray) {
                this.mUidPidArray.put(uid, pid);
            }
            return true;
        }
        Log.w(TAG, "only the process it self can do this operation: " + uid + ", " + callerUid + ", " + pid + ", " + callerPid);
        return false;
    }

    private boolean removeProcess(int uid, int pid) {
        synchronized (this.mUidPidArray) {
            if (pid != this.mUidPidArray.get(uid)) {
                Log.w(TAG, "remove target not exist, maybe the UI process: uid: " + uid + ", pid: " + pid);
                return false;
            }
            this.mUidPidArray.delete(uid);
            return true;
        }
    }

    private boolean musicObserverOp(Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.hsm.IHsmCoreService");
        int opType = data.readInt();
        switch (opType) {
            case ViewDragHelper.STATE_IDLE /*0*/:
                return addOrRemoveMusicObserver(data, reply, true);
            case ViewDragHelper.STATE_DRAGGING /*1*/:
                return addOrRemoveMusicObserver(data, reply, false);
            default:
                Log.e(TAG, "musicObserverOp invalid opType: " + opType);
                return false;
        }
    }

    private boolean addOrRemoveMusicObserver(Parcel data, Parcel reply, boolean add) {
        IHsmMusicWatch watcher = null;
        int pid = data.readInt();
        if (add) {
            watcher = Stub.asInterface(data.readStrongBinder());
        }
        Log.i(TAG, "addOrRemoveMusicObserver ->> add: " + add + ", watcher: " + watcher + ", pid: " + pid);
        boolean optRet = setMusicObserver(watcher, pid);
        reply.writeNoException();
        reply.writeInt(optRet ? 1 : 0);
        return optRet;
    }

    private boolean setMusicObserver(IHsmMusicWatch watcher, int pid) {
        boolean optRet = true;
        synchronized (this.mMediaStatusObservers) {
            if (watcher != null) {
                this.mMediaStatusObservers.put(Integer.valueOf(pid), watcher);
            } else if (((IHsmMusicWatch) this.mMediaStatusObservers.remove(Integer.valueOf(pid))) == null) {
                optRet = false;
                Log.w(TAG, "setMusicObserver remove watcher failed, watcher is not exist.");
            }
        }
        return optRet;
    }

    private boolean executeCallBack(int opType, int uid, int pid) {
        boolean retVal = true;
        synchronized (this.mMediaStatusObservers) {
            Iterator<Entry<Integer, IHsmMusicWatch>> iter = this.mMediaStatusObservers.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, IHsmMusicWatch> obersver = (Entry) iter.next();
                int optRet = 0;
                if (opType == 0) {
                    try {
                        optRet = ((IHsmMusicWatch) obersver.getValue()).onMusicPlaying(uid, pid);
                    } catch (RemoteException e) {
                        Log.e(TAG, "executedCallBack get RemoteException: " + e.toString() + " Oberser pid: " + obersver.getKey() + " removed.");
                        iter.remove();
                        retVal = false;
                    } catch (Exception e2) {
                        Log.e(TAG, "executedCallBack ->> get Exception: " + e2.toString() + " Oberser pid: " + obersver.getKey() + " removed.");
                        iter.remove();
                        retVal = false;
                    }
                } else if (1 == opType) {
                    optRet = ((IHsmMusicWatch) obersver.getValue()).onMusicPauseOrStop(uid, pid);
                }
                if (optRet != 0) {
                    retVal = false;
                    Log.w(TAG, "executedCallBack opType: " + opType + "executed failed.");
                }
            }
        }
        return retVal;
    }
}
