package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.ace.featureabilityplugin.requestparse.ParsedJsRequest;
import ohos.ace.featureabilityplugin.requestparse.RequestParse;
import ohos.app.AbilityContext;
import ohos.bundle.ElementName;

public class StartAbilityProcess {
    private static final StartAbilityProcess INSTANCE = new StartAbilityProcess();
    private static final int LOCAL_DEVICE_TYPE = 1;
    private static final int MAX_REQUEST_CODE = 65535;
    private static final String START_PARAMS_KEY = "__startParams";
    private static final String TAG = StartAbilityProcess.class.getSimpleName();
    private static Map<Integer, Result> abilityResult = new HashMap();
    private int requestCode = 0;

    public static StartAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void startAbility(AbilityContext abilityContext, List<Object> list, Result result, boolean z) {
        if (result == null) {
            ALog.e(TAG, "start ability result handler is null!");
        } else if (abilityContext == null || list == null) {
            ALog.e(TAG, "start ability context or arguments is null");
            result.error(2001, "start ability context or arguments is null");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 5)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            Intent intent = new Intent();
            setIntent(intent, parsedJsRequest);
            if (!(abilityContext instanceof Ability)) {
                ALog.e(TAG, "context is not instance of Ability!");
                result.error(2001, "context is not instance of Ability");
                return;
            }
            Ability ability = (Ability) abilityContext;
            ability.getUITaskDispatcher().asyncDispatch(new Runnable(z, ability, intent, result) {
                /* class ohos.ace.featureabilityplugin.requestprocess.$$Lambda$StartAbilityProcess$82SWDI5J8JFpsZZxU00gFu7jSy4 */
                private final /* synthetic */ boolean f$1;
                private final /* synthetic */ Ability f$2;
                private final /* synthetic */ Intent f$3;
                private final /* synthetic */ Result f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StartAbilityProcess.this.lambda$startAbility$0$StartAbilityProcess(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }
    }

    public /* synthetic */ void lambda$startAbility$0$StartAbilityProcess(boolean z, Ability ability, Intent intent, Result result) {
        if (!z) {
            try {
                ability.startAbility(intent);
                result.success(null);
            } catch (IllegalArgumentException | IllegalStateException e) {
                ALog.e(TAG, "Start ability failed:" + e.getMessage());
                result.error(2012, "Start ability failed:" + e.getMessage());
            }
        } else {
            intent.removeFlags(256);
            if (this.requestCode == 65535) {
                this.requestCode = 0;
            } else {
                this.requestCode++;
            }
            ability.startAbilityForResult(intent, this.requestCode);
            abilityResult.put(Integer.valueOf(this.requestCode), result);
        }
    }

    public void onAbilityResultCallback(int i, int i2, Intent intent) {
        ALog.i(TAG, "on Ability Result Callback");
        if (abilityResult.containsKey(Integer.valueOf(i))) {
            Result result = abilityResult.get(Integer.valueOf(i));
            if (result == null) {
                ALog.e(TAG, "resultHandler is null");
            } else if (intent == null || !intent.hasParameter("resultData")) {
                ALog.w(TAG, "ability result callback null or doesn't contain param resultData!");
                if (i2 == 0) {
                    result.success(null);
                } else {
                    result.error(i2, null);
                }
                abilityResult.remove(Integer.valueOf(i));
            } else {
                if (i2 == 0) {
                    result.success(intent.getStringParam("resultData"));
                } else if (i2 == -1) {
                    result.success(null);
                } else {
                    result.error(i2, intent.getStringParam("resultData"));
                }
                abilityResult.remove(Integer.valueOf(i));
            }
        } else {
            String str = TAG;
            ALog.e(str, "requestCode not found:" + i);
        }
    }

    private void setIntent(Intent intent, ParsedJsRequest parsedJsRequest) {
        if (parsedJsRequest.getStartAbilityDeviceType() != 1) {
            intent.addFlags(256);
        }
        intent.addFlags(parsedJsRequest.getFlag());
        if (parsedJsRequest.getRequestData() != null) {
            intent.setParam(START_PARAMS_KEY, parsedJsRequest.getRequestData());
        }
        if (parsedJsRequest.getIntentType()) {
            intent.setElement(new ElementName(parsedJsRequest.getDeviceId(), parsedJsRequest.getBundleName(), parsedJsRequest.getAbilityName()));
            return;
        }
        intent.setAction(parsedJsRequest.getAction());
        List<String> entities = parsedJsRequest.getEntities();
        if (entities != null) {
            for (String str : entities) {
                intent.addEntity(str);
            }
        }
    }
}
