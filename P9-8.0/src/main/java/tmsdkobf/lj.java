package tmsdkobf;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateManager;

public class lj {
    public static void ez() {
        if (gf.S().ad().booleanValue()) {
            final UpdateManager updateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    updateManager.check(290984034304L, new ICheckListener() {
                        public void onCheckCanceled() {
                        }

                        public void onCheckEvent(int i) {
                        }

                        public void onCheckFinished(final CheckResult checkResult) {
                            if (checkResult != null) {
                                ((ki) fj.D(4)).addTask(new Runnable() {
                                    public void run() {
                                        updateManager.update(checkResult.mUpdateInfoList, null);
                                    }
                                }, "Update");
                            }
                        }

                        public void onCheckStarted() {
                        }
                    }, -1);
                }
            }, "checkUpdate");
            gf.S().k(Boolean.valueOf(false));
        }
    }
}
