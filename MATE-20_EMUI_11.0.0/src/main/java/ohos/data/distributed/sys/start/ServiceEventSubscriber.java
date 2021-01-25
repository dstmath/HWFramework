package ohos.data.distributed.sys.start;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.UserHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.IntentConverter;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ServiceEventSubscriber extends CommonEventSubscriber {
    private static final String ACTION_NAME = "DistributedDataMgrStarter";
    private static final String BUNDLE_NAME_DOT = ".";
    private static final Map<String, WorkServiceConnection> CONNECTIONS = new HashMap();
    private static final Object CONN_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109456, "ZDDSJ");
    private static final String PKG_NAME = "pkgName";
    private static final String SERVICE_NAME = "ServiceStarter";
    private static final String SERVICE_SHELL_SUFFIX = "ShellService";
    private Context mContext;

    public ServiceEventSubscriber(Context context, CommonEventSubscribeInfo commonEventSubscribeInfo) {
        super(commonEventSubscribeInfo);
        this.mContext = context;
    }

    @Override // ohos.event.commonevent.CommonEventSubscriber
    public void onReceiveEvent(CommonEventData commonEventData) {
        if (commonEventData == null) {
            HiLog.error(LABEL, "received event, start ServiceAbility failed, eventData is null.", new Object[0]);
            return;
        }
        Intent intent = commonEventData.getIntent();
        if (intent == null) {
            HiLog.error(LABEL, "received event, start ServiceAbility failed, intent is null.", new Object[0]);
            return;
        }
        String action = intent.getAction();
        HiLog.info(LABEL, "received event action:%{public}s", new Object[]{action});
        if (ACTION_NAME.equals(action)) {
            Object param = intent.getParams().getParam(PKG_NAME);
            String str = null;
            if (param instanceof String) {
                str = (String) String.class.cast(param);
            }
            if (str == null || "".equals(str)) {
                HiLog.error(LABEL, "received event, start ServiceAbility failed, param is null.", new Object[0]);
                return;
            }
            HiLog.info(LABEL, "received event, start ServiceAbility, param:%{public}s.", new Object[]{str});
            bindService(str);
            HiLog.info(LABEL, "start ServiceAbility end.", new Object[0]);
            return;
        }
        HiLog.info(LABEL, "received event unknown action:%{public}s", new Object[]{action});
    }

    private void bindService(String str) {
        try {
            Optional<android.content.Intent> convertIntentShell = convertIntentShell(str, SERVICE_NAME);
            synchronized (CONN_LOCK) {
                if (convertIntentShell.isPresent()) {
                    if (this.mContext != null) {
                        if (CONNECTIONS.get(str) != null) {
                            HiLog.warn(LABEL, "received event, repeated bind, %{public}s.", new Object[]{str});
                            return;
                        }
                        WorkServiceConnection workServiceConnection = new WorkServiceConnection();
                        if (this.mContext.bindServiceAsUser(convertIntentShell.get(), workServiceConnection, 1, UserHandle.CURRENT)) {
                            CONNECTIONS.put(str, workServiceConnection);
                        } else {
                            HiLog.warn(LABEL, "received event, bind failed, %{public}s.", new Object[]{str});
                        }
                        return;
                    }
                }
                HiLog.warn(LABEL, "received event, convert fail or context null.", new Object[0]);
            }
        } catch (SecurityException e) {
            HiLog.error(LABEL, "start ServiceAbility exception message:%{public}s.", new Object[]{e.getMessage()});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindService(ComponentName componentName) {
        synchronized (CONN_LOCK) {
            WorkServiceConnection workServiceConnection = CONNECTIONS.get(componentName.getPackageName());
            if (workServiceConnection == null || this.mContext == null) {
                HiLog.info(LABEL, "can't find ServiceConnection.", new Object[0]);
            } else {
                this.mContext.unbindService(workServiceConnection);
                CONNECTIONS.remove(componentName.getPackageName());
            }
        }
    }

    private Optional<android.content.Intent> convertIntentShell(String str, String str2) {
        Intent intent = new Intent();
        ElementName elementName = new ElementName();
        elementName.setBundleName(str);
        elementName.setAbilityName(str2);
        intent.setElement(elementName);
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(intent.getElement().getBundleName());
        shellInfo.setType(ShellInfo.ShellType.SERVICE);
        shellInfo.setName(intent.getElement().getBundleName() + "." + intent.getElement().getAbilityName() + "ShellService");
        return checkIntentIsValid(intent, shellInfo);
    }

    private Optional<android.content.Intent> checkIntentIsValid(Intent intent, ShellInfo shellInfo) {
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent() || createAndroidIntent.get().getComponent() == null) {
            HiLog.error(LABEL, "intent convert failed.", new Object[0]);
            return Optional.empty();
        }
        HiLog.info(LABEL, "intent convert ok", new Object[0]);
        return createAndroidIntent;
    }

    /* access modifiers changed from: private */
    public class WorkServiceConnection implements ServiceConnection {
        private WorkServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.info(ServiceEventSubscriber.LABEL, "connected, %{public}s.", new Object[]{componentName.getPackageName()});
            ServiceEventSubscriber.this.unbindService(componentName);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            HiLog.info(ServiceEventSubscriber.LABEL, "disconnected, %{public}s.", new Object[]{componentName.getPackageName()});
        }
    }
}
