package ohos.app;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import ohos.app.dispatcher.SerialTaskDispatcher;
import ohos.app.dispatcher.TaskDispatcherContext;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;

public class DumpHelper {
    private final ConcurrentMap<Object, Context> abilityRecords;
    private Date appCreateTime;
    private final ApplicationInfo applicationInfo;
    private final TaskDispatcherContext dispatcherContext;
    private final ProcessInfo processInfo;

    public DumpHelper(Application application) {
        this.applicationInfo = application.getApplicationInfo();
        this.processInfo = application.getProcessInfo();
        this.dispatcherContext = application.getTaskDispatcherContext();
        this.appCreateTime = application.getAppCreateTime();
        this.abilityRecords = application.getAbilityRecord();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        if (r9.equals("-h") != false) goto L_0x003d;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0058  */
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (strArr == null || strArr.length != 1) {
            showIllegalInformation(str, printWriter);
            return;
        }
        boolean z = false;
        String str2 = strArr[0];
        int hashCode = str2.hashCode();
        if (hashCode != -1226959101) {
            if (hashCode != 1499) {
                if (hashCode == 1455280 && str2.equals("-tdm")) {
                    z = true;
                    if (!z) {
                        showHelp(str, printWriter);
                        return;
                    } else if (z) {
                        showApplicationInfo(str, printWriter);
                        showAbilityInfo(str, printWriter);
                        return;
                    } else if (!z) {
                        showIllegalInformation(str, printWriter);
                        return;
                    } else {
                        showProcessInfo(str, printWriter);
                        showWorkerPool(str, printWriter);
                        showTaskQueue(str, printWriter);
                        return;
                    }
                }
            }
        } else if (str2.equals("-application")) {
            z = true;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    public void showHelp(String str, PrintWriter printWriter) {
        printWriter.println("Usage:");
        printWriter.println("  -ability: dump information of ability.");
        printWriter.println("  -application: dump information of user application.");
        printWriter.println("  -tdm: dump information of TDM threading model.");
    }

    public void showIllegalInformation(String str, PrintWriter printWriter) {
        printWriter.println("The arguments are illegal and you can enter '-h' for help.");
    }

    public void showApplicationInfo(String str, PrintWriter printWriter) {
        printWriter.println("application information: ");
        if (this.applicationInfo != null) {
            printWriter.print("  name: " + this.applicationInfo.getName());
            printWriter.print("  process: " + this.applicationInfo.getProcess());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        printWriter.println("  create time: " + simpleDateFormat.format(this.appCreateTime));
        printWriter.println("  running time: " + getRunningTime(this.appCreateTime));
    }

    public void showProcessInfo(String str, PrintWriter printWriter) {
        if (this.processInfo != null) {
            printWriter.println("process information: ");
            printWriter.print("  process name: " + this.processInfo.getProcessName());
            printWriter.println("  process id: " + this.processInfo.getPid());
        }
    }

    public void showAbilityInfo(String str, PrintWriter printWriter) {
        printWriter.println("ability information: ");
        for (Map.Entry<Object, Context> entry : this.abilityRecords.entrySet()) {
            AbilityInfo abilityInfo = entry.getValue().getAbilityInfo();
            if (abilityInfo != null) {
                printWriter.print("  name: " + abilityInfo.getClassName());
                printWriter.print("  bundle name: " + abilityInfo.getBundleName());
                printWriter.print("  process: " + abilityInfo.getProcess());
                printWriter.print("  targetAbility: " + abilityInfo.getTargetAbility());
                printWriter.print("  type: " + abilityInfo.getType());
                printWriter.print("  orientation: " + abilityInfo.getOrientation());
                printWriter.print("  launchMode: " + abilityInfo.getLaunchMode());
                printWriter.println("  description: " + abilityInfo.getDescription());
            }
        }
    }

    public void showWorkerPool(String str, PrintWriter printWriter) {
        printWriter.println("WorkerPool information: ");
        printWriter.print("  max thread count: " + this.dispatcherContext.getWorkerPoolConfig().getMaxThreadCount());
        printWriter.print("  core thread count: " + this.dispatcherContext.getWorkerPoolConfig().getCoreThreadCount());
        printWriter.println("  keep alive time: " + this.dispatcherContext.getWorkerPoolConfig().getKeepAliveTime());
        Map<String, Long> workerThreadsInfo = this.dispatcherContext.getWorkerThreadsInfo();
        if (!workerThreadsInfo.isEmpty()) {
            for (Map.Entry<String, Long> entry : workerThreadsInfo.entrySet()) {
                printWriter.println("  thread name: " + entry.getKey() + "  finished tasks count: " + entry.getValue());
            }
        }
    }

    public void showTaskQueue(String str, PrintWriter printWriter) {
        printWriter.println("waiting tasks information: ");
        printWriter.println("  sum of finished tasks: " + this.dispatcherContext.getTaskCounter());
        printWriter.println("  waiting tasks count of TaskExecutor: " + this.dispatcherContext.getWaitingTasksCount());
        for (Map.Entry<SerialTaskDispatcher, String> entry : this.dispatcherContext.getSerialDispatchers().entrySet()) {
            SerialTaskDispatcher key = entry.getKey();
            if (key != null) {
                printWriter.println("  serial dispatcher name: " + key.getDispatcherName() + "  waiting tasks count: " + key.getWorkingTasksSize());
            }
        }
    }

    public String getRunningTime(Date date) {
        long time = new Date().getTime() - date.getTime();
        return (time / 60000) + " minutes " + ((time % 60000) / 1000) + "seconds";
    }
}
