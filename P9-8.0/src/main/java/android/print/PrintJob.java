package android.print;

import java.util.Objects;

public final class PrintJob {
    private PrintJobInfo mCachedInfo;
    private final PrintManager mPrintManager;

    PrintJob(PrintJobInfo info, PrintManager printManager) {
        this.mCachedInfo = info;
        this.mPrintManager = printManager;
    }

    public PrintJobId getId() {
        return this.mCachedInfo.getId();
    }

    public PrintJobInfo getInfo() {
        if (isInImmutableState()) {
            return this.mCachedInfo;
        }
        PrintJobInfo info = this.mPrintManager.getPrintJobInfo(this.mCachedInfo.getId());
        if (info != null) {
            this.mCachedInfo = info;
        }
        return this.mCachedInfo;
    }

    public void cancel() {
        int state = getInfo().getState();
        if (state == 2 || state == 3 || state == 4 || state == 6) {
            this.mPrintManager.cancelPrintJob(this.mCachedInfo.getId());
        }
    }

    public void restart() {
        if (isFailed()) {
            this.mPrintManager.restartPrintJob(this.mCachedInfo.getId());
        }
    }

    public boolean isQueued() {
        return getInfo().getState() == 2;
    }

    public boolean isStarted() {
        return getInfo().getState() == 3;
    }

    public boolean isBlocked() {
        return getInfo().getState() == 4;
    }

    public boolean isCompleted() {
        return getInfo().getState() == 5;
    }

    public boolean isFailed() {
        return getInfo().getState() == 6;
    }

    public boolean isCancelled() {
        return getInfo().getState() == 7;
    }

    private boolean isInImmutableState() {
        int state = this.mCachedInfo.getState();
        if (state == 5 || state == 7) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.mCachedInfo.getId(), ((PrintJob) obj).mCachedInfo.getId());
    }

    public int hashCode() {
        PrintJobId printJobId = this.mCachedInfo.getId();
        if (printJobId == null) {
            return 0;
        }
        return printJobId.hashCode();
    }
}
