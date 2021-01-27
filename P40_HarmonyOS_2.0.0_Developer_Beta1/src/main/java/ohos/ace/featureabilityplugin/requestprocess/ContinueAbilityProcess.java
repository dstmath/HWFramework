package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import ohos.aafwk.ability.Ability;
import ohos.app.AbilityContext;

public class ContinueAbilityProcess {
    private static final ContinueAbilityProcess INSTANCE = new ContinueAbilityProcess();
    private static final String TAG = ContinueAbilityProcess.class.getSimpleName();

    public static ContinueAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void continueAbility(AbilityContext abilityContext, Result result) {
        if (result == null) {
            ALog.e(TAG, "continue ability result handler is null!");
        } else if (abilityContext == null) {
            ALog.e(TAG, "continue ability context is null");
            result.error(2001, "continue ability context is null");
        } else if (!(abilityContext instanceof Ability)) {
            ALog.e(TAG, "context is not instance of Ability!");
            result.error(2001, "context is not instance of Ability");
        } else {
            Ability ability = (Ability) abilityContext;
            ability.getUITaskDispatcher().asyncDispatch(new Runnable(result) {
                /* class ohos.ace.featureabilityplugin.requestprocess.$$Lambda$ContinueAbilityProcess$ky5kmfRUSXugRSY2xPqC6RC37g */
                private final /* synthetic */ Result f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ContinueAbilityProcess.lambda$continueAbility$0(Ability.this, this.f$1);
                }
            });
        }
    }

    static /* synthetic */ void lambda$continueAbility$0(Ability ability, Result result) {
        try {
            ability.continueAbility();
            result.success(null);
        } catch (IllegalStateException | UnsupportedOperationException e) {
            String str = TAG;
            ALog.e(str, "continue ability failed:" + e.getMessage());
            result.error(2013, "continue ability failed:" + e.getMessage());
        }
    }
}
