package ohos.event.intentagent;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.event.EventConstant;
import ohos.event.commonevent.IntentConverter;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentAdapter;
import ohos.event.intentagent.IntentAgentConstant;
import ohos.eventhandler.EventHandler;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class IntentAgentAdapter {
    private static final String ABILITY_SHELL_SUFFIX = "ShellActivity";
    private static final int ERROR_FLAG = 0;
    private static final int ERROR_VALUE = -1;
    private static final IntentAgentAdapter INSTANCE = new IntentAgentAdapter();
    private static final int INTENT_SIZE = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.INTENTAGENT_DOMAIN, TAG);
    private static final int MAX_INTENT_NUM = 3;
    private static final String SERVICE_SHELL_SUFFIX = "ShellService";
    private static final String STR_POINT = ".";
    private static final String TAG = "IntentAgentAdapter";

    private IntentAgentAdapter() {
    }

    public static IntentAgentAdapter getInstance() {
        return INSTANCE;
    }

    public Optional<IntentAgent> getIntentAgent(Context context, IntentAgentInfo intentAgentInfo) {
        Optional<PendingIntent> optional;
        if (context == null || intentAgentInfo == null) {
            HiLog.error(LABEL, "IntentAgentAdapter::getIntentAgent invalid input param", new Object[0]);
            return Optional.empty();
        }
        Optional<android.content.Context> aospContext = getAospContext(context);
        if (!aospContext.isPresent()) {
            return Optional.empty();
        }
        android.content.Context context2 = aospContext.get();
        int flagsTransformer = flagsTransformer(intentAgentInfo.getFlags());
        if (flagsTransformer == 0) {
            return Optional.empty();
        }
        int operationType = intentAgentInfo.getOperationType();
        IntentParams extraInfo = intentAgentInfo.getExtraInfo();
        List<Intent> intents = intentAgentInfo.getIntents();
        Optional.empty();
        int requestCode = intentAgentInfo.getRequestCode();
        if (operationType == IntentAgentConstant.OperationType.START_ABILITY.ordinal()) {
            optional = getAbility(context2, flagsTransformer, intents, requestCode, extraInfo);
        } else if (operationType == IntentAgentConstant.OperationType.START_ABILITIES.ordinal()) {
            optional = getAbilities(context2, flagsTransformer, intents, requestCode, extraInfo);
        } else if (operationType == IntentAgentConstant.OperationType.START_SERVICE.ordinal()) {
            optional = getService(context2, flagsTransformer, intents, requestCode);
        } else if (operationType == IntentAgentConstant.OperationType.SEND_COMMON_EVENT.ordinal()) {
            optional = getCommonEvent(context2, flagsTransformer, intents, requestCode);
        } else if (operationType == IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE.ordinal()) {
            optional = getForegroundService(context2, flagsTransformer, intents, requestCode);
        } else {
            HiLog.error(LABEL, "IntentAgentAdapter::getIntentAgent operation type is error.", new Object[0]);
            return Optional.empty();
        }
        if (optional.isPresent()) {
            return Optional.of(new IntentAgent(optional.get()));
        }
        HiLog.error(LABEL, "IntentAgentAdapter::getIntentAgent the intents does not meet the requirements.", new Object[0]);
        return Optional.empty();
    }

    public void triggerIntentAgent(Context context, IntentAgent intentAgent, IntentAgent.OnCompleted onCompleted, EventHandler eventHandler, TriggerInfo triggerInfo) {
        if (context == null || intentAgent == null) {
            HiLog.error(LABEL, "IntentAgentAdapter::triggerIntentAgent invalid input param", new Object[0]);
            return;
        }
        Optional<android.content.Context> aospContext = getAospContext(context);
        if (!aospContext.isPresent()) {
            HiLog.error(LABEL, "IntentAgentAdapter::triggerIntentAgent get aosp context failed", new Object[0]);
            return;
        }
        Object object = intentAgent.getObject();
        if (object instanceof PendingIntent) {
            PendingIntent pendingIntent = (PendingIntent) object;
            IntentAgentConstant.OperationType type = getType(pendingIntent);
            CompletedDispatcher completedDispatcher = null;
            if (onCompleted != null) {
                completedDispatcher = new CompletedDispatcher(onCompleted, eventHandler, intentAgent, type);
            }
            send(aospContext.get(), pendingIntent, type, completedDispatcher, triggerInfo);
        }
    }

    public boolean judgeEquality(IntentAgent intentAgent, IntentAgent intentAgent2) {
        if (intentAgent == null && intentAgent2 == null) {
            return true;
        }
        if (intentAgent == null || intentAgent2 == null) {
            return false;
        }
        Object object = intentAgent.getObject();
        if (!(object instanceof PendingIntent)) {
            return false;
        }
        PendingIntent pendingIntent = (PendingIntent) object;
        Object object2 = intentAgent2.getObject();
        if (!(object2 instanceof PendingIntent)) {
            return false;
        }
        return pendingIntent.equals((PendingIntent) object2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0046  */
    private void send(android.content.Context context, PendingIntent pendingIntent, IntentAgentConstant.OperationType operationType, CompletedDispatcher completedDispatcher, TriggerInfo triggerInfo) {
        Bundle bundle;
        String str;
        android.content.Intent intent;
        int i;
        ShellInfo shellInfo;
        IntentParams extraInfo;
        Bundle bundle2 = null;
        if (triggerInfo != null) {
            Intent intent2 = triggerInfo.getIntent();
            if (!(intent2 == null || intent2.getElement() == null)) {
                String bundleName = intent2.getElement().getBundleName();
                String abilityName = intent2.getElement().getAbilityName();
                if (!(bundleName == null || abilityName == null)) {
                    shellInfo = getShellInfo(bundleName, abilityName, operationType);
                    Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent2, shellInfo);
                    android.content.Intent intent3 = !createAndroidIntent.isPresent() ? createAndroidIntent.get() : null;
                    String permission = triggerInfo.getPermission();
                    extraInfo = triggerInfo.getExtraInfo();
                    if (extraInfo != null) {
                        Optional<Bundle> convertIntentParamsToBundle = IntentConverter.convertIntentParamsToBundle(extraInfo.getParams());
                        if (convertIntentParamsToBundle.isPresent()) {
                            bundle2 = convertIntentParamsToBundle.get();
                        }
                    }
                    intent = intent3;
                    i = triggerInfo.getCode();
                    bundle = bundle2;
                    str = permission;
                }
            }
            shellInfo = null;
            Optional<android.content.Intent> createAndroidIntent2 = IntentConverter.createAndroidIntent(intent2, shellInfo);
            if (!createAndroidIntent2.isPresent()) {
            }
            String permission2 = triggerInfo.getPermission();
            extraInfo = triggerInfo.getExtraInfo();
            if (extraInfo != null) {
            }
            intent = intent3;
            i = triggerInfo.getCode();
            bundle = bundle2;
            str = permission2;
        } else {
            i = 0;
            intent = null;
            str = null;
            bundle = null;
        }
        try {
            pendingIntent.send(context, i, intent, completedDispatcher, null, str, bundle);
        } catch (PendingIntent.CanceledException unused) {
            HiLog.error(LABEL, "IntentAgentAdapter::triggerIntentAgent canceledException", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public IntentAgentConstant.OperationType getType(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            return IntentAgentConstant.OperationType.UNKNOWN_TYPE;
        }
        if (pendingIntent.isActivity()) {
            return IntentAgentConstant.OperationType.START_ABILITY;
        }
        if (pendingIntent.isBroadcast()) {
            return IntentAgentConstant.OperationType.SEND_COMMON_EVENT;
        }
        if (pendingIntent.isForegroundService()) {
            return IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE;
        }
        if (pendingIntent.isActivity() || pendingIntent.isBroadcast() || pendingIntent.isForegroundService()) {
            return IntentAgentConstant.OperationType.UNKNOWN_TYPE;
        }
        return IntentAgentConstant.OperationType.START_SERVICE;
    }

    private Optional<PendingIntent> getAbility(android.content.Context context, int i, List<Intent> list, int i2, IntentParams intentParams) {
        if (!isIntentMatch(list, IntentAgentConstant.OperationType.START_ABILITY)) {
            HiLog.error(LABEL, "IntentAgentAdapter::getAbility intents is not match.", new Object[0]);
            return Optional.empty();
        }
        Optional<android.content.Intent> convertZosIntentToAosIntent = convertZosIntentToAosIntent(list, IntentAgentConstant.OperationType.START_ABILITY);
        if (!convertZosIntentToAosIntent.isPresent()) {
            return Optional.empty();
        }
        convertZosIntentToAosIntent.get().setFlags(268435456);
        Bundle bundle = null;
        if (intentParams != null) {
            Optional<Bundle> convertIntentParamsToBundle = IntentConverter.convertIntentParamsToBundle(intentParams.getParams());
            if (convertIntentParamsToBundle.isPresent()) {
                bundle = convertIntentParamsToBundle.get();
            }
        }
        PendingIntent activity = PendingIntent.getActivity(context, i2, convertZosIntentToAosIntent.get(), i, bundle);
        if (activity == null) {
            return Optional.empty();
        }
        return Optional.of(activity);
    }

    private Optional<PendingIntent> getAbilities(android.content.Context context, int i, List<Intent> list, int i2, IntentParams intentParams) {
        if (!isIntentMatch(list, IntentAgentConstant.OperationType.START_ABILITIES)) {
            HiLog.error(LABEL, "IntentAgentAdapter::getAbilities intents is not match.", new Object[0]);
            return Optional.empty();
        }
        ArrayList arrayList = new ArrayList();
        for (Intent intent : list) {
            getAospIntent(intent, IntentAgentConstant.OperationType.START_ABILITIES).ifPresent(new Consumer(arrayList) {
                /* class ohos.event.intentagent.$$Lambda$oHuon0sxg27MKIHlOHNqTBxHQR4 */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.add((android.content.Intent) obj);
                }
            });
            if (arrayList.size() >= 3) {
                break;
            }
        }
        if (arrayList.isEmpty()) {
            return Optional.empty();
        }
        android.content.Intent[] intentArr = new android.content.Intent[arrayList.size()];
        arrayList.toArray(intentArr);
        Bundle bundle = null;
        if (intentParams != null) {
            Optional<Bundle> convertIntentParamsToBundle = IntentConverter.convertIntentParamsToBundle(intentParams.getParams());
            if (convertIntentParamsToBundle.isPresent()) {
                bundle = convertIntentParamsToBundle.get();
            }
        }
        PendingIntent activities = PendingIntent.getActivities(context, i2, intentArr, i, bundle);
        if (activities == null) {
            return Optional.empty();
        }
        return Optional.of(activities);
    }

    private Optional<PendingIntent> getService(android.content.Context context, int i, List<Intent> list, int i2) {
        if (!isIntentMatch(list, IntentAgentConstant.OperationType.START_SERVICE)) {
            HiLog.error(LABEL, "IntentAgentAdapter::getService intents is not match.", new Object[0]);
            return Optional.empty();
        }
        Optional<android.content.Intent> convertZosIntentToAosIntent = convertZosIntentToAosIntent(list, IntentAgentConstant.OperationType.START_SERVICE);
        if (!convertZosIntentToAosIntent.isPresent()) {
            return Optional.empty();
        }
        PendingIntent service = PendingIntent.getService(context, i2, convertZosIntentToAosIntent.get(), i);
        if (service == null) {
            return Optional.empty();
        }
        return Optional.of(service);
    }

    private Optional<PendingIntent> getForegroundService(android.content.Context context, int i, List<Intent> list, int i2) {
        if (!isIntentMatch(list, IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE)) {
            HiLog.error(LABEL, "IntentAgentAdapter::getForegroundService intents is not match.", new Object[0]);
            return Optional.empty();
        }
        Optional<android.content.Intent> convertZosIntentToAosIntent = convertZosIntentToAosIntent(list, IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE);
        if (!convertZosIntentToAosIntent.isPresent()) {
            return Optional.empty();
        }
        PendingIntent foregroundService = PendingIntent.getForegroundService(context, i2, convertZosIntentToAosIntent.get(), i);
        if (foregroundService == null) {
            return Optional.empty();
        }
        return Optional.of(foregroundService);
    }

    private Optional<PendingIntent> getCommonEvent(android.content.Context context, int i, List<Intent> list, int i2) {
        if (!isIntentMatch(list, IntentAgentConstant.OperationType.SEND_COMMON_EVENT)) {
            HiLog.error(LABEL, "IntentAgentAdapter::getCommonEvent intents is not match.", new Object[0]);
            return Optional.empty();
        }
        Optional<android.content.Intent> convertZosIntentToAosIntent = convertZosIntentToAosIntent(list, IntentAgentConstant.OperationType.SEND_COMMON_EVENT);
        if (!convertZosIntentToAosIntent.isPresent()) {
            return Optional.empty();
        }
        PendingIntent broadcast = PendingIntent.getBroadcast(context, i2, convertZosIntentToAosIntent.get(), i);
        if (broadcast == null) {
            return Optional.empty();
        }
        return Optional.of(broadcast);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.event.intentagent.IntentAgentAdapter$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType = new int[IntentAgentConstant.OperationType.values().length];

        static {
            try {
                $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[IntentAgentConstant.OperationType.START_ABILITY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[IntentAgentConstant.OperationType.START_SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[IntentAgentConstant.OperationType.SEND_COMMON_EVENT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[IntentAgentConstant.OperationType.START_ABILITIES.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    private boolean isIntentMatch(List<Intent> list, IntentAgentConstant.OperationType operationType) {
        if (list == null) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$ohos$event$intentagent$IntentAgentConstant$OperationType[operationType.ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            return list.size() == 1;
        }
        if (i != 5) {
            return false;
        }
        return !list.isEmpty();
    }

    private Optional<android.content.Intent> convertZosIntentToAosIntent(List<Intent> list, IntentAgentConstant.OperationType operationType) {
        Optional<android.content.Intent> empty = Optional.empty();
        for (Intent intent : list) {
            empty = getAospIntent(intent, operationType);
            if (!empty.isPresent()) {
                HiLog.error(LABEL, "IntentAgentAdapter::Error translating zIntent to aIntent.", new Object[0]);
                return Optional.empty();
            }
        }
        return empty;
    }

    private static int flagsTransformer(List<IntentAgentConstant.Flags> list) {
        int i;
        if (list == null) {
            return 0;
        }
        int i2 = 0;
        for (IntentAgentConstant.Flags flags : list) {
            if (flags == IntentAgentConstant.Flags.ONE_TIME_FLAG) {
                i = 1073741824;
            } else if (flags == IntentAgentConstant.Flags.NO_BUILD_FLAG) {
                i = 536870912;
            } else if (flags == IntentAgentConstant.Flags.CANCEL_PRESENT_FLAG) {
                i = 268435456;
            } else if (flags == IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG) {
                i = 134217728;
            } else if (flags == IntentAgentConstant.Flags.CONSTANT_FLAG) {
                i = 67108864;
            } else if (flags == IntentAgentConstant.Flags.REPLACE_ELEMENT) {
                i2 |= 8;
            } else if (flags == IntentAgentConstant.Flags.REPLACE_ACTION) {
                i2 |= 1;
            } else if (flags == IntentAgentConstant.Flags.REPLACE_URI) {
                i2 |= 2;
            } else if (flags == IntentAgentConstant.Flags.REPLACE_ENTITIES) {
                i2 |= 4;
            } else if (flags == IntentAgentConstant.Flags.REPLACE_BUNDLE) {
                i2 |= 16;
            } else {
                HiLog.error(LABEL, "IntentAgentAdapter::flag is error.", new Object[0]);
            }
            i2 |= i;
        }
        return i2;
    }

    private static Optional<android.content.Intent> getAospIntent(Intent intent, IntentAgentConstant.OperationType operationType) {
        if (intent == null) {
            HiLog.error(LABEL, "IntentAgentAdapter::zIntent is null.", new Object[0]);
            return Optional.empty();
        } else if (operationType == IntentAgentConstant.OperationType.SEND_COMMON_EVENT) {
            return IntentConverter.createAndroidIntent(intent, null);
        } else {
            ElementName element = intent.getElement();
            if (element == null) {
                HiLog.error(LABEL, "IntentAgentAdapter::Can't get element name from zIntent.", new Object[0]);
                return Optional.empty();
            }
            String bundleName = element.getBundleName();
            String abilityName = element.getAbilityName();
            if (bundleName != null && abilityName != null) {
                return IntentConverter.createAndroidIntent(intent, getShellInfo(bundleName, abilityName, operationType));
            }
            HiLog.error(LABEL, "IntentAgentAdapter::package name or class name are null.", new Object[0]);
            return Optional.empty();
        }
    }

    private static ShellInfo getShellInfo(String str, String str2, IntentAgentConstant.OperationType operationType) {
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(str);
        if (operationType == IntentAgentConstant.OperationType.START_ABILITY || operationType == IntentAgentConstant.OperationType.START_ABILITIES) {
            shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
            shellInfo.setName(str2 + "ShellActivity");
        } else if (operationType == IntentAgentConstant.OperationType.START_SERVICE || operationType == IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE) {
            shellInfo.setType(ShellInfo.ShellType.SERVICE);
            shellInfo.setName(str2 + "ShellService");
        } else {
            shellInfo.setType(ShellInfo.ShellType.UNKNOWN);
            shellInfo.setName(str2);
        }
        return shellInfo;
    }

    private static Optional<android.content.Context> getAospContext(Context context) {
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            return Optional.of((android.content.Context) hostContext);
        }
        HiLog.error(LABEL, "IntentAgentAdapter::Can not convert zContext to aContext.", new Object[0]);
        return Optional.empty();
    }

    static Optional<Intent> getZidaneIntent(android.content.Intent intent, IntentAgentConstant.OperationType operationType) {
        if (intent == null) {
            return Optional.empty();
        }
        if (operationType == IntentAgentConstant.OperationType.SEND_COMMON_EVENT) {
            return IntentConverter.createZidaneIntent(intent, null);
        }
        ComponentName component = intent.getComponent();
        if (component == null) {
            return Optional.empty();
        }
        String packageName = component.getPackageName();
        String shortClassName = component.getShortClassName();
        if (packageName == null || shortClassName == null) {
            return Optional.empty();
        }
        int lastIndexOf = shortClassName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            shortClassName = shortClassName.substring(lastIndexOf + 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(packageName);
        sb.append(".");
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.setBundleName(packageName);
        if (operationType == IntentAgentConstant.OperationType.START_ABILITY || operationType == IntentAgentConstant.OperationType.START_ABILITIES) {
            abilityInfo.setType(AbilityInfo.AbilityType.PAGE);
            int lastIndexOf2 = shortClassName.lastIndexOf("ShellActivity");
            if (lastIndexOf2 == -1) {
                sb.append(shortClassName);
            } else {
                sb.append(shortClassName.substring(0, lastIndexOf2));
            }
        } else if (operationType == IntentAgentConstant.OperationType.START_SERVICE || operationType == IntentAgentConstant.OperationType.START_FOREGROUND_SERVICE) {
            abilityInfo.setType(AbilityInfo.AbilityType.SERVICE);
            int lastIndexOf3 = shortClassName.lastIndexOf("ShellService");
            if (lastIndexOf3 == -1) {
                sb.append(shortClassName);
            } else {
                sb.append(shortClassName.substring(0, lastIndexOf3));
            }
        } else {
            abilityInfo.setType(AbilityInfo.AbilityType.UNKNOWN);
            sb.append(shortClassName);
        }
        abilityInfo.setClassName(sb.toString());
        return IntentConverter.createZidaneIntent(intent, abilityInfo);
    }

    /* access modifiers changed from: private */
    public static class CompletedDispatcher implements PendingIntent.OnFinished, Runnable {
        private IntentParams extraInfo = null;
        private final EventHandler handler;
        private final IntentAgent intentAgent;
        private final IntentAgent.OnCompleted onCompleted;
        private int resultCode;
        private String resultData = null;
        private final IntentAgentConstant.OperationType type;
        private Intent zIntent = null;

        public CompletedDispatcher(IntentAgent.OnCompleted onCompleted2, EventHandler eventHandler, IntentAgent intentAgent2, IntentAgentConstant.OperationType operationType) {
            this.onCompleted = onCompleted2;
            this.handler = eventHandler;
            this.intentAgent = intentAgent2;
            this.type = operationType;
        }

        @Override // android.app.PendingIntent.OnFinished
        public void onSendFinished(PendingIntent pendingIntent, android.content.Intent intent, int i, String str, Bundle bundle) {
            this.resultCode = i;
            this.resultData = str;
            IntentAgentAdapter.getZidaneIntent(intent, this.type).ifPresent(new Consumer() {
                /* class ohos.event.intentagent.$$Lambda$IntentAgentAdapter$CompletedDispatcher$YaundkysgpmaEmC7pvMq2insw */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IntentAgentAdapter.CompletedDispatcher.this.lambda$onSendFinished$0$IntentAgentAdapter$CompletedDispatcher((Intent) obj);
                }
            });
            setIntentParam(bundle);
            EventHandler eventHandler = this.handler;
            if (eventHandler == null) {
                IntentAgent.OnCompleted onCompleted2 = this.onCompleted;
                if (onCompleted2 != null) {
                    onCompleted2.onSendCompleted(this.intentAgent, this.zIntent, this.resultCode, this.resultData, this.extraInfo);
                    return;
                }
                return;
            }
            eventHandler.postTask(this);
        }

        public /* synthetic */ void lambda$onSendFinished$0$IntentAgentAdapter$CompletedDispatcher(Intent intent) {
            this.zIntent = intent;
        }

        @Override // java.lang.Runnable
        public void run() {
            IntentAgent.OnCompleted onCompleted2 = this.onCompleted;
            if (onCompleted2 != null) {
                onCompleted2.onSendCompleted(this.intentAgent, this.zIntent, this.resultCode, this.resultData, this.extraInfo);
            }
        }

        private void setIntentParam(Bundle bundle) {
            Set<String> keySet;
            if (!(bundle == null || (keySet = bundle.keySet()) == null)) {
                this.extraInfo = new IntentParams();
                for (String str : keySet) {
                    if (str != null) {
                        this.extraInfo.setParam(str, bundle.get(str));
                    }
                }
            }
        }
    }
}
