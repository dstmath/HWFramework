package android.scrollerboost;

import android.common.HwFrameworkFactory;
import android.rms.iaware.AwareLog;

public class ScrollerBoostManager {
    private static final String TAG = "ScrollerBoostManager";
    private static Object sLock = new Object();
    private static ScrollerBoostManager sScrollerBoostManager;
    private IScrollerBoostMgr mScrollerObj = HwFrameworkFactory.getIScrollerBoostMgr();

    private ScrollerBoostManager() {
    }

    public static ScrollerBoostManager getInstance() {
        ScrollerBoostManager scrollerBoostManager;
        synchronized (sLock) {
            if (sScrollerBoostManager == null) {
                sScrollerBoostManager = new ScrollerBoostManager();
            }
            scrollerBoostManager = sScrollerBoostManager;
        }
        return scrollerBoostManager;
    }

    public void init() {
        if (this.mScrollerObj == null) {
            AwareLog.w(TAG, "init mScrollerObj is null!");
        } else {
            this.mScrollerObj.init();
        }
    }

    public void listFling(int duration) {
        if (this.mScrollerObj == null) {
            AwareLog.w(TAG, "listFling mScrollerObj is null!");
        } else {
            this.mScrollerObj.listFling(duration);
        }
    }

    public void updateFrameJankInfo(long skippedFrames) {
        if (this.mScrollerObj == null) {
            AwareLog.w(TAG, "updateFrameJankInfo mScrollerObj is null!");
        } else {
            this.mScrollerObj.updateFrameJankInfo(skippedFrames);
        }
    }

    public void finishListFling(float currVelocity) {
        if (this.mScrollerObj == null) {
            AwareLog.w(TAG, "finishListFling mScrollerObj is null!");
        } else {
            this.mScrollerObj.finishListFling(currVelocity);
        }
    }
}
