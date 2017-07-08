package android.rms.resource;

import android.os.RemoteException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.util.Log;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.util.ArrayList;

public final class BroadcastResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "BroadcastResource";
    private static BroadcastResource mBroadcastResource;
    private int mOverloadPeroid;
    private ResourceConfig[] mResourceConfig;
    private ResourceFlowControl mResourceFlowControl;
    private HwSysResManager mResourceManger;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;
    private ArrayList<String> mWhiteListSend;
    private ArrayList<String> mWhiteListSendAction;

    public BroadcastResource() {
        this.mWhiteListSend = null;
        this.mWhiteListSendAction = null;
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                ArrayList<String> tempWhiteListForSend = BroadcastResource.this.getResWhiteList(11, 1);
                if (tempWhiteListForSend.size() != 0) {
                    BroadcastResource.this.mWhiteListSend.clear();
                    BroadcastResource.this.mWhiteListSend = (ArrayList) tempWhiteListForSend.clone();
                    if (Log.HWLog) {
                        Log.d(BroadcastResource.TAG, "BroadcastResource update send nameList success");
                    }
                }
                BroadcastResource.this.updateWhiteListForSendAction();
            }
        };
        if (!registerResourceCallback(this.mUpdateWhiteListCallback)) {
            Log.e(TAG, "BroadcastResource register callback failed");
        }
        getConfig(11);
        this.mWhiteListSend = super.getResWhiteList(11, 1);
        this.mWhiteListSendAction = super.getResWhiteList(11, 2);
        this.mResourceFlowControl = new ResourceFlowControl();
    }

    public static synchronized BroadcastResource getInstance() {
        BroadcastResource broadcastResource;
        synchronized (BroadcastResource.class) {
            if (mBroadcastResource == null) {
                mBroadcastResource = new BroadcastResource();
            }
            broadcastResource = mBroadcastResource;
        }
        return broadcastResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        if (this.mResourceConfig == null) {
            return 1;
        }
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (isResourceSpeedOverload(callingUid, pkg, typeID)) {
            return getSpeedOverloadStrategy(typeID);
        }
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        switch (type) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
                if (super.isInWhiteList(key, this.mWhiteListSend)) {
                    return 1;
                }
                return 4;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                if (super.isInWhiteList(key, this.mWhiteListSendAction)) {
                    return 1;
                }
                return 4;
            default:
                return 0;
        }
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 11);
    }

    private void getConfig(int resourceType) {
        this.mResourceManger = HwSysResManager.getInstance();
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        if (this.mResourceConfig == null) {
        }
    }

    private int getSpeedOverloadStrategy(int typeID) {
        int strategy = this.mResourceConfig[typeID].getResourceStrategy();
        if (this.mOverloadPeroid >= this.mResourceConfig[typeID].getResourceMaxPeroid()) {
            return strategy;
        }
        return 1;
    }

    private boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        int maxPeroid = config.getResourceMaxPeroid();
        if (!this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            return DEBUG;
        }
        this.mOverloadPeroid = this.mResourceFlowControl.getOverloadPeroid(id);
        if (this.mOverloadPeroid >= maxPeroid) {
            int overloadNum = this.mResourceFlowControl.getOverloadNumber(id);
            if (Log.HWINFO) {
                Log.i(TAG, "isResourceSpeedOverload id=" + id + " threshold=" + threshold + " OverloadNum=" + overloadNum + " OverloadPeroid=" + this.mOverloadPeroid);
            }
            this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 11, overloadNum, this.mOverloadPeroid, 0);
        }
        return true;
    }

    private void updateWhiteListForSendAction() {
        ArrayList<String> tempWhiteListForSendAction = getResWhiteList(11, 2);
        if (tempWhiteListForSendAction.size() != 0) {
            this.mWhiteListSendAction.clear();
            this.mWhiteListSendAction = (ArrayList) tempWhiteListForSendAction.clone();
            if (Log.HWINFO) {
                Log.d(TAG, "BroadcastResource update SendAction nameList success");
            }
        }
    }
}
