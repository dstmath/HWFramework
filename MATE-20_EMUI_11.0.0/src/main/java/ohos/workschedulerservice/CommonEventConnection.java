package ohos.workschedulerservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.UserHandle;
import java.util.HashMap;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.IntentConverter;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.workscheduler.IWorkScheduler;
import ohos.workscheduler.WorkSchedulerProxy;
import ohos.workschedulerservice.controller.CommonEventStatus;

public class CommonEventConnection {
    private static final int DESTORY_CONNECTION = 3;
    private static final int EXECUTE_EVENT = 1;
    private static final int EXECUTE_TIMEOUT = 2;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "CommonEventConnection");
    private static final long MAX_EXECUTE_TIME = 6000;
    private Context context;
    private EventConnectionHandler eventConnectionHandler;
    private final HashMap<ElementName, EventServiceConnection> eventServiceConnections = new HashMap<>();

    public CommonEventConnection(Context context2) {
        this.context = context2;
    }

    public boolean init() {
        EventRunner create;
        if (this.context == null || (create = EventRunner.create()) == null) {
            return false;
        }
        this.eventConnectionHandler = new EventConnectionHandler(create);
        return true;
    }

    public void onCommonEventTriggered(CommonEventStatus commonEventStatus) {
        if (commonEventStatus == null || this.eventConnectionHandler == null || this.context == null) {
            HiLog.error(LOG_LABEL, "onCommonEventTriggered failed, eventStatus is null", new Object[0]);
            return;
        }
        synchronized (this.eventServiceConnections) {
            if (this.eventServiceConnections.get(commonEventStatus.getElementName()) != null) {
                this.eventConnectionHandler.sendEvent(InnerEvent.get(1, commonEventStatus));
                return;
            }
            EventServiceConnection eventServiceConnection = new EventServiceConnection(commonEventStatus);
            if (!eventServiceConnection.onStartServiceConnect()) {
                HiLog.error(LOG_LABEL, "onCommonEventTriggered: connect service failed", new Object[0]);
            } else {
                this.eventServiceConnections.put(commonEventStatus.getElementName(), eventServiceConnection);
            }
        }
    }

    /* access modifiers changed from: private */
    public class EventServiceConnection implements ServiceConnection {
        private static final String BUNDLE_NAME_DOT = ".";
        private static final String SERVICE_SHELL_SUFFIX = "ShellService";
        private CommonEventStatus eventStatus;
        private IWorkScheduler workSchedulerProxy;

        public EventServiceConnection(CommonEventStatus commonEventStatus) {
            this.eventStatus = commonEventStatus;
        }

        public void setCommonEventStatus(CommonEventStatus commonEventStatus) {
            this.eventStatus = commonEventStatus;
        }

        public boolean onStartServiceConnect() {
            CommonEventStatus commonEventStatus = this.eventStatus;
            if (commonEventStatus == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "eventStatus is null", new Object[0]);
                return false;
            }
            Intent intent = commonEventStatus.getIntent();
            if (intent == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "intent is null", new Object[0]);
                return false;
            }
            ShellInfo shellInfo = new ShellInfo();
            shellInfo.setPackageName(intent.getElement().getBundleName());
            shellInfo.setType(ShellInfo.ShellType.SERVICE);
            shellInfo.setName(intent.getElement().getAbilityName() + SERVICE_SHELL_SUFFIX);
            Optional<android.content.Intent> checkIntentIsValid = checkIntentIsValid(intent, shellInfo);
            if (!checkIntentIsValid.isPresent()) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "convert android intent fail", new Object[0]);
                return false;
            }
            try {
                HiLog.info(CommonEventConnection.LOG_LABEL, "%{public}s connect start", this.eventStatus.getAbilityName());
                return CommonEventConnection.this.context.bindServiceAsUser(checkIntentIsValid.get(), this, 1, UserHandle.CURRENT);
            } catch (SecurityException unused) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "bind Service SecurityException happens!", new Object[0]);
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onCommonEventExecute() {
            if (this.workSchedulerProxy == null || this.eventStatus == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onCommonEventExecute workSchedulerProxy is null", new Object[0]);
                return;
            }
            try {
                HiLog.debug(CommonEventConnection.LOG_LABEL, "onCommonEventTriggered real start!", new Object[0]);
                this.workSchedulerProxy.onCommonEventTriggered(this.eventStatus.getIntent());
            } catch (RemoteException unused) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onCommonEventExecute RemoteException", new Object[0]);
            }
            HiLog.info(CommonEventConnection.LOG_LABEL, "onCommonEventExecute success, timeout count start!", new Object[0]);
            CommonEventConnection.this.eventConnectionHandler.sendEvent(InnerEvent.get(2, this.eventStatus.getElementName()), CommonEventConnection.MAX_EXECUTE_TIME);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onCommonEventTimeout() {
            try {
                CommonEventConnection.this.context.unbindService(this);
            } catch (SecurityException unused) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onCommonEventTimeout unbindService SecurityException!", new Object[0]);
            }
        }

        private Optional<android.content.Intent> checkIntentIsValid(Intent intent, ShellInfo shellInfo) {
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
            if (!createAndroidIntent.isPresent() || createAndroidIntent.get().getComponent() == null) {
                return Optional.empty();
            }
            HiLog.debug(CommonEventConnection.LOG_LABEL, "intent convert ok", new Object[0]);
            return createAndroidIntent;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.info(CommonEventConnection.LOG_LABEL, "onServiceConnected called", new Object[0]);
            if (componentName == null || iBinder == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onServiceConnected ComponentName or IBinder service is null", new Object[0]);
                return;
            }
            HiLog.info(CommonEventConnection.LOG_LABEL, "onServiceConnected ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            Optional<IRemoteObject> translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(iBinder);
            if (!translateToIRemoteObject.isPresent()) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "convert IRemoteObject fail", new Object[0]);
                return;
            }
            this.workSchedulerProxy = new WorkSchedulerProxy(translateToIRemoteObject.get());
            onCommonEventExecute();
            HiLog.info(CommonEventConnection.LOG_LABEL, "onServiceConnected end", new Object[0]);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (componentName == null || this.eventStatus == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onServiceDisconnected ComponentName is null", new Object[0]);
                return;
            }
            HiLog.debug(CommonEventConnection.LOG_LABEL, "onBindingDied ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            CommonEventConnection.this.eventConnectionHandler.sendEvent(InnerEvent.get(3, this.eventStatus));
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName componentName) {
            if (componentName == null || this.eventStatus == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onBindingDied ComponentName is null", new Object[0]);
                return;
            }
            HiLog.debug(CommonEventConnection.LOG_LABEL, "onBindingDied ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            CommonEventConnection.this.eventConnectionHandler.sendEvent(InnerEvent.get(3, this.eventStatus));
        }

        @Override // android.content.ServiceConnection
        public void onNullBinding(ComponentName componentName) {
            if (componentName == null || this.eventStatus == null) {
                HiLog.error(CommonEventConnection.LOG_LABEL, "onNullBinding ComponentName is null", new Object[0]);
                return;
            }
            HiLog.debug(CommonEventConnection.LOG_LABEL, "onNullBinding ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            CommonEventConnection.this.eventConnectionHandler.sendEvent(InnerEvent.get(3, this.eventStatus));
        }
    }

    /* access modifiers changed from: private */
    public final class EventConnectionHandler extends EventHandler {
        private EventConnectionHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            CommonEventConnection.super.processEvent(innerEvent);
            if (innerEvent != null) {
                int i = innerEvent.eventId;
                if (i == 1) {
                    HiLog.info(CommonEventConnection.LOG_LABEL, "EXECUTE_EVENT event process!", new Object[0]);
                    if (innerEvent.object instanceof CommonEventStatus) {
                        CommonEventStatus commonEventStatus = (CommonEventStatus) innerEvent.object;
                        synchronized (CommonEventConnection.this.eventServiceConnections) {
                            EventServiceConnection eventServiceConnection = (EventServiceConnection) CommonEventConnection.this.eventServiceConnections.get(commonEventStatus.getElementName());
                            if (!(eventServiceConnection == null || eventServiceConnection.workSchedulerProxy == null)) {
                                CommonEventConnection.this.eventConnectionHandler.removeEvent(2, commonEventStatus.getElementName());
                                eventServiceConnection.setCommonEventStatus(commonEventStatus);
                                eventServiceConnection.onCommonEventExecute();
                            }
                        }
                    }
                } else if (i == 2) {
                    HiLog.info(CommonEventConnection.LOG_LABEL, "EXECUTE_TIMEOUT event process!", new Object[0]);
                    if (innerEvent.object instanceof ElementName) {
                        ElementName elementName = (ElementName) innerEvent.object;
                        synchronized (CommonEventConnection.this.eventServiceConnections) {
                            EventServiceConnection eventServiceConnection2 = (EventServiceConnection) CommonEventConnection.this.eventServiceConnections.get(elementName);
                            if (eventServiceConnection2 != null) {
                                eventServiceConnection2.onCommonEventTimeout();
                                CommonEventConnection.this.eventServiceConnections.remove(elementName);
                            }
                        }
                    }
                } else if (i != 3) {
                    HiLog.debug(CommonEventConnection.LOG_LABEL, "processEvent default", new Object[0]);
                } else {
                    HiLog.info(CommonEventConnection.LOG_LABEL, "DESTORY_CONNECTION event process!", new Object[0]);
                    if (innerEvent.object instanceof CommonEventStatus) {
                        CommonEventStatus commonEventStatus2 = (CommonEventStatus) innerEvent.object;
                        synchronized (CommonEventConnection.this.eventServiceConnections) {
                            if (((EventServiceConnection) CommonEventConnection.this.eventServiceConnections.get(commonEventStatus2.getElementName())) != null) {
                                CommonEventConnection.this.eventConnectionHandler.removeEvent(2, commonEventStatus2.getElementName());
                                CommonEventConnection.this.eventServiceConnections.remove(commonEventStatus2.getElementName());
                            }
                        }
                    }
                }
            }
        }
    }
}
