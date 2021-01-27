package android.scrollerboostmanager;

import android.common.HwFrameworkFactory;
import android.rms.iaware.AwareLog;

public class ScrollerBoostManager {
    private static final Object LOCK = new Object();
    private static final String TAG = "ScrollerBoostManager";
    private static ScrollerBoostManager sScrollerBoostManager;
    private IScrollerBoostMgr mScrollerObj = HwFrameworkFactory.getIScrollerBoostMgr();

    private ScrollerBoostManager() {
    }

    public static ScrollerBoostManager getInstance() {
        ScrollerBoostManager scrollerBoostManager;
        synchronized (LOCK) {
            if (sScrollerBoostManager == null) {
                sScrollerBoostManager = new ScrollerBoostManager();
            }
            scrollerBoostManager = sScrollerBoostManager;
        }
        return scrollerBoostManager;
    }

    public void init() {
        IScrollerBoostMgr iScrollerBoostMgr = this.mScrollerObj;
        if (iScrollerBoostMgr == null) {
            AwareLog.w(TAG, "init mScrollerObj is null!");
        } else {
            iScrollerBoostMgr.init();
        }
    }

    public void listFling(int duration) {
        IScrollerBoostMgr iScrollerBoostMgr = this.mScrollerObj;
        if (iScrollerBoostMgr == null) {
            AwareLog.w(TAG, "listFling mScrollerObj is null!");
        } else {
            iScrollerBoostMgr.listFling(duration);
        }
    }

    public void updateFrameJankInfo(long skippedFrames) {
        IScrollerBoostMgr iScrollerBoostMgr = this.mScrollerObj;
        if (iScrollerBoostMgr == null) {
            AwareLog.w(TAG, "updateFrameJankInfo mScrollerObj is null!");
        } else {
            iScrollerBoostMgr.updateFrameJankInfo(skippedFrames);
        }
    }
}
