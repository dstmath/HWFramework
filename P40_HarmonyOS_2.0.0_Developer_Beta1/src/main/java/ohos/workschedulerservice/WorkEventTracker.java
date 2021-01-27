package ohos.workschedulerservice;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.workschedulerservice.controller.WorkStatus;

public final class WorkEventTracker {
    private static final int EVENT_LIST_MAX_SIZE = 100;
    private static final int FIRST_ELEMENT = 0;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkEventTracker");
    private static final int ON_SATRT_WORK_EVENT = 1;
    private static final int WORK_CONNECTION_END_EVENT = 2;
    private final List<EventInfo> eventInfos = new ArrayList();

    private String getEventCmdLabel(int i) {
        return i != 1 ? i != 2 ? "INCORRECT_LABEL" : "WORK_CONNECTION_END_EVENT" : "ON_SATRT_WORK_EVENT";
    }

    public void noteOnWorkStartEvent(WorkStatus workStatus) {
        if (workStatus == null) {
            HiLog.error(LOG_LABEL, "work input is null", new Object[0]);
            return;
        }
        offer(new EventInfo(1, workStatus.getUid(), workStatus.getWorkId(), workStatus.getBundleName(), workStatus.getWork() != null ? workStatus.getWork().getAbilityName() : ""));
        HiLog.info(LOG_LABEL, "noteOnWorkStartEvent %{public}d success", Integer.valueOf(workStatus.getWorkId()));
    }

    public void noteWorkConnectionEndEvent(WorkStatus workStatus) {
        if (workStatus == null) {
            HiLog.error(LOG_LABEL, "work input is null", new Object[0]);
            return;
        }
        offer(new EventInfo(2, workStatus.getUid(), workStatus.getWorkId(), workStatus.getBundleName(), workStatus.getWork() != null ? workStatus.getWork().getAbilityName() : ""));
        HiLog.info(LOG_LABEL, "noteWorkConnectionEndEvent %{public}d success", Integer.valueOf(workStatus.getWorkId()));
    }

    public void dumpHistory(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("Events history dump info:");
        synchronized (this.eventInfos) {
            if (this.eventInfos.isEmpty()) {
                printWriter.print(str);
                printWriter.println("<none work event info>");
                return;
            }
            for (EventInfo eventInfo : this.eventInfos) {
                printWriter.println(new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(eventInfo.getEventTime())) + str + "EventLable:" + getEventCmdLabel(eventInfo.getEventCmd()) + ", uid:" + eventInfo.getUid() + ", workId:" + eventInfo.getWorkId() + ", bundleName:" + eventInfo.getBundleName() + ", abilityName:" + eventInfo.getAbilityName());
            }
        }
    }

    private void offer(EventInfo eventInfo) {
        synchronized (this.eventInfos) {
            if (this.eventInfos.size() >= 100) {
                this.eventInfos.remove(0);
            }
            this.eventInfos.add(eventInfo);
        }
    }

    /* access modifiers changed from: private */
    public static class EventInfo {
        private final String abilityName;
        private final String bundleName;
        private final int eventCmd;
        private final long eventTime = System.currentTimeMillis();
        private final int uid;
        private final int workId;

        public EventInfo(int i, int i2, int i3, String str, String str2) {
            this.eventCmd = i;
            this.uid = i2;
            this.workId = i3;
            this.bundleName = str;
            this.abilityName = str2;
        }

        public int getEventCmd() {
            return this.eventCmd;
        }

        public int getUid() {
            return this.uid;
        }

        public int getWorkId() {
            return this.workId;
        }

        public String getBundleName() {
            return this.bundleName;
        }

        public String getAbilityName() {
            return this.abilityName;
        }

        public long getEventTime() {
            return this.eventTime;
        }
    }
}
