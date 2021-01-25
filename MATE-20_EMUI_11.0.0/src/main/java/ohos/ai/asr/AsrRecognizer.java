package ohos.ai.asr;

import android.os.IBinder;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.ai.asr.service.AsrPluginService;
import ohos.ai.asr.taskhandler.AsrCallbackTaskHandler;
import ohos.ai.asr.taskhandler.AsrTaskHandler;
import ohos.ai.engine.handler.TaskInfo;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginServiceSkeleton;
import ohos.ai.engine.utils.HiAILog;
import ohos.ai.engine.utils.ServiceConnector;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.bundle.ElementName;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class AsrRecognizer {
    private static final String LOCAL_DEVICE_ID = "";
    private static final String TAG = AsrRecognizer.class.getSimpleName();
    private AsrListenerBinder asrListenerBinder;
    private AsrPluginService asrService;
    private Connection connection;
    private Context context;
    private final TaskDispatcher dispatcher;
    private LinkedBlockingQueue<TaskInfo> pendingTaskQueue = new LinkedBlockingQueue<>();
    private AsrTaskHandler taskHandler;

    private AsrRecognizer(Context context2) {
        this.context = context2;
        this.dispatcher = context2.createSerialTaskDispatcher(TAG, TaskPriority.DEFAULT);
    }

    public static Optional<AsrRecognizer> createAsrRecognizer(Context context2) {
        if (context2 != null) {
            return Optional.of(new AsrRecognizer(context2));
        }
        HiAILog.error(TAG, "createAsrRecognizer: context is null");
        return Optional.empty();
    }

    public synchronized void init(AsrIntent asrIntent, AsrListener asrListener) {
        HiAILog.info(TAG, "init");
        initListener(asrListener);
        connectToService();
        putTask(new TaskInfo.Builder(3).setObject(asrIntent).build());
    }

    private void initListener(AsrListener asrListener) {
        this.asrListenerBinder = new AsrListenerBinder();
        this.asrListenerBinder.setAsrCallbackTaskHandler(new AsrCallbackTaskHandler(this.dispatcher, asrListener));
    }

    private void connectToService() {
        if (this.connection == null) {
            this.connection = new Connection();
            if (!ServiceConnector.connectToService(this.context, "", this.connection)) {
                HiAILog.info(TAG, "will send error message to client");
                AsrListenerBinder asrListenerBinder2 = this.asrListenerBinder;
                if (asrListenerBinder2 != null) {
                    asrListenerBinder2.onError(5);
                }
            }
        }
    }

    public synchronized void startListening(AsrIntent asrIntent) {
        putTask(new TaskInfo.Builder(1).setObject(asrIntent).build());
    }

    public synchronized void stopListening() {
        putTask(new TaskInfo.Builder(2).build());
    }

    public synchronized void updateLexicon(AsrIntent asrIntent) {
        if (asrIntent == null) {
            HiAILog.error(TAG, "updateLexicon： asr intent is null");
        } else {
            putTask(new TaskInfo.Builder(6).setObject(asrIntent).build());
        }
    }

    public synchronized void writePcm(byte[] bArr, int i) {
        putTask(new TaskInfo.Builder(4).setObject(bArr).setArgOne(i).build());
    }

    public synchronized void cancel() {
        putTask(new TaskInfo.Builder(5).build());
    }

    public synchronized void destroy() {
        putTask(new TaskInfo.Builder(7).build());
        if (!(this.connection == null || this.context == null)) {
            ServiceConnector.unBindService(this.context, this.connection);
        }
        this.pendingTaskQueue.clear();
        this.asrService = null;
        this.asrListenerBinder = null;
        this.connection = null;
        this.context = null;
    }

    private void putTask(TaskInfo taskInfo) {
        if (this.asrService == null) {
            if (this.connection == null || !this.pendingTaskQueue.offer(taskInfo)) {
                HiAILog.warn(TAG, "connection is null or pendingTaskQueue.offer return false");
            }
        } else if (this.taskHandler != null) {
            String str = TAG;
            HiAILog.info(str, "execute starting task " + taskInfo.getTaskId());
            this.taskHandler.start(taskInfo);
        } else {
            HiAILog.error(TAG, "puttask handler has been destroyed!");
        }
    }

    /* access modifiers changed from: private */
    public class Connection implements IAbilityConnection {
        private Connection() {
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityConnectDone(ElementName elementName, final IRemoteObject iRemoteObject, int i) {
            if (AsrRecognizer.this.dispatcher != null) {
                HiAILog.info(AsrRecognizer.TAG, "dispatcher is not null");
                AsrRecognizer.this.dispatcher.asyncDispatch(new Runnable() {
                    /* class ohos.ai.asr.AsrRecognizer.Connection.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        AsrRecognizer.this.onHiAiServiceConnected(iRemoteObject);
                    }
                });
            }
            HiAILog.info(AsrRecognizer.TAG, "onAbilityConnectDone - Success");
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            if (AsrRecognizer.this.dispatcher != null) {
                AsrRecognizer.this.dispatcher.asyncDispatch(new Runnable() {
                    /* class ohos.ai.asr.AsrRecognizer.Connection.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        AsrRecognizer.this.onHiAiServiceDisconnected();
                    }
                });
            }
            HiAILog.debug(AsrRecognizer.TAG, "onAbilityDisconnectDone - Success");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void onHiAiServiceConnected(IRemoteObject iRemoteObject) {
        IPluginService orElse = PluginServiceSkeleton.asInterface(iRemoteObject).orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "pluginservice is null");
        } else if (this.asrListenerBinder == null) {
            HiAILog.error(TAG, "mAsrListener = null");
        } else {
            try {
                Optional translateToIBinder = IPCAdapter.translateToIBinder(orElse.getSplitRemoteObject(65536));
                if (!translateToIBinder.isPresent()) {
                    HiAILog.error(TAG, "get engine plugin binder is null");
                    this.asrListenerBinder.onError(30);
                    return;
                }
                if (translateToIBinder.get() instanceof IBinder) {
                    this.asrService = AsrPluginService.Stub.asInterface((IBinder) translateToIBinder.get()).orElse(null);
                }
                HiAILog.debug(TAG, "onServiceConnected getBinder.");
                if (this.asrService == null) {
                    HiAILog.error(TAG, "mService = null");
                    return;
                }
                this.taskHandler = new AsrTaskHandler(this.dispatcher, this.asrService, this.asrListenerBinder);
                executePendingTasks();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "onServiceConnected RemoteException!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void onHiAiServiceDisconnected() {
        this.asrService = null;
        this.asrListenerBinder = null;
        this.pendingTaskQueue.clear();
    }

    private void executePendingTasks() {
        if (!this.pendingTaskQueue.isEmpty() && this.taskHandler != null) {
            HiAILog.debug(TAG, "execute pending tasks");
            while (!this.pendingTaskQueue.isEmpty()) {
                TaskInfo poll = this.pendingTaskQueue.poll();
                if (poll != null) {
                    this.taskHandler.start(poll);
                }
            }
        }
    }
}
