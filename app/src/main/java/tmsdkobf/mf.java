package tmsdkobf;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateManager;

/* compiled from: Unknown */
public class mf {

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.mf.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ UpdateManager AF;

        AnonymousClass1(UpdateManager updateManager) {
            this.AF = updateManager;
        }

        public void run() {
            this.AF.check(290984034304L, new ICheckListener() {
                final /* synthetic */ AnonymousClass1 AG;

                {
                    this.AG = r1;
                }

                public void onCheckCanceled() {
                }

                public void onCheckEvent(int i) {
                }

                public void onCheckFinished(CheckResult checkResult) {
                    if (checkResult != null) {
                        this.AG.AF.update(checkResult.mUpdateInfoList, null);
                    }
                }

                public void onCheckStarted() {
                }
            });
        }
    }

    public static void eL() {
        if (fw.w().L().booleanValue()) {
            ((lq) fe.ad(4)).a(new AnonymousClass1((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)), "checkUpdate");
            fw.w().k(Boolean.valueOf(false));
        }
    }
}
