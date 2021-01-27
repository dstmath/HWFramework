package ohos.account;

import android.os.Bundle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.event.commonevent.CommonEventBaseConverter;
import ohos.event.commonevent.CommonEventSupport;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AccountEventsConverter extends CommonEventBaseConverter {
    private static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
    private static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
    private static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    private static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final Map<String, ZAction> MAP_ACTION_CONVERTER = Collections.unmodifiableMap(new HashMap<String, ZAction>() {
        /* class ohos.account.AccountEventsConverter.AnonymousClass2 */

        {
            put("android.intent.action.USER_ADDED", new ZAction(CommonEventSupport.COMMON_EVENT_USER_ADDED, AccountEventsConverter.MAP_EXTRAS_USER_HANDLE));
            put("android.intent.action.USER_REMOVED", new ZAction(CommonEventSupport.COMMON_EVENT_USER_REMOVED, AccountEventsConverter.MAP_EXTRAS_USER_HANDLE));
            put(AccountEventsConverter.ACTION_USER_SWITCHED, new ZAction(CommonEventSupport.COMMON_EVENT_USER_SWITCHED, AccountEventsConverter.MAP_EXTRAS_USER_HANDLE));
        }
    });
    private static final Map<String, String> MAP_EXTRAS_USER_HANDLE = Collections.unmodifiableMap(new HashMap<String, String>() {
        /* class ohos.account.AccountEventsConverter.AnonymousClass1 */

        {
            put(AccountEventsConverter.EXTRA_USER_HANDLE, AccountEventsConverter.PARAM_USER_HANDLE);
        }
    });
    private static final String PARAM_USER_HANDLE = "usual.event.account.PARAM_USER_HANDLE";
    private static final String TAG = "AccountEventsConverter";

    private static final class ZAction {
        public String action;
        public Map<String, String> extrasConverter;

        public ZAction(String str, Map<String, String> map) {
            this.action = str;
            this.extrasConverter = map;
        }
    }

    @Override // ohos.event.commonevent.CommonEventBaseConverter
    public Optional<Intent> convertAospIntentToIntent(android.content.Intent intent) {
        Set<String> keySet;
        HiLog.info(LABEL, "ENTER convertAospIntentToIntent", new Object[0]);
        if (intent == null) {
            HiLog.warn(LABEL, "convertAospIntentToIntent, aIntent is null", new Object[0]);
            return Optional.empty();
        }
        String action = intent.getAction();
        ZAction zAction = MAP_ACTION_CONVERTER.get(action);
        if (zAction == null) {
            HiLog.info(LABEL, "convertAospIntentToIntent, use default converter, aAction = %{public}s", new Object[]{action});
            return super.convertAospIntentToIntent(intent);
        }
        Intent intent2 = new Intent();
        intent2.setAction(zAction.action);
        Bundle extras = intent.getExtras();
        HiLog.info(LABEL, "aAction: %{public}s, zAction: %{public}s", new Object[]{action, zAction.action});
        if (!(extras == null || (keySet = extras.keySet()) == null)) {
            IntentParams intentParams = new IntentParams();
            for (String str : keySet) {
                if (str == null) {
                    HiLog.warn(LABEL, "aExtraKey is null", new Object[0]);
                } else {
                    Object obj = extras.get(str);
                    intentParams.setParam(str, obj);
                    if (zAction.extrasConverter == null || zAction.extrasConverter.get(str) == null) {
                        HiLog.warn(LABEL, "Cannot get z key from a key: %{public}s", new Object[]{str});
                    } else {
                        intentParams.setParam(zAction.extrasConverter.get(str), obj);
                    }
                }
            }
            intent2.setParams(intentParams);
        }
        HiLog.info(LABEL, "convertAospIntentToIntent return zIntent", new Object[0]);
        return Optional.of(intent2);
    }
}
