package android.net.util;

import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.StringJoiner;

public class SharedLog {
    private static final String COMPONENT_DELIMITER = ".";
    private static final int DEFAULT_MAX_RECORDS = 500;
    private final String mComponent;
    private final LocalLog mLocalLog;
    private final String mTag;

    private enum Category {
        NONE,
        ERROR,
        MARK,
        WARN
    }

    public SharedLog(String tag) {
        this(500, tag);
    }

    public SharedLog(int maxRecords, String tag) {
        this(new LocalLog(maxRecords), tag, tag);
    }

    private SharedLog(LocalLog localLog, String tag, String component) {
        this.mLocalLog = localLog;
        this.mTag = tag;
        this.mComponent = component;
    }

    public SharedLog forSubComponent(String component) {
        if (!isRootLogInstance()) {
            component = this.mComponent + COMPONENT_DELIMITER + component;
        }
        return new SharedLog(this.mLocalLog, this.mTag, component);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mLocalLog.readOnlyLocalLog().dump(fd, writer, args);
    }

    public void e(Exception e) {
        Log.e(this.mTag, record(Category.ERROR, e.toString()));
    }

    public void e(String msg) {
        Log.e(this.mTag, record(Category.ERROR, msg));
    }

    public void i(String msg) {
        Log.i(this.mTag, record(Category.NONE, msg));
    }

    public void w(String msg) {
        Log.w(this.mTag, record(Category.WARN, msg));
    }

    public void log(String msg) {
        record(Category.NONE, msg);
    }

    public void mark(String msg) {
        record(Category.MARK, msg);
    }

    private String record(Category category, String msg) {
        String entry = logLine(category, msg);
        this.mLocalLog.log(entry);
        return entry;
    }

    private String logLine(Category category, String msg) {
        StringJoiner sj = new StringJoiner(" ");
        if (!isRootLogInstance()) {
            sj.add("[" + this.mComponent + "]");
        }
        if (category != Category.NONE) {
            sj.add(category.toString());
        }
        return sj.add(msg).toString();
    }

    private boolean isRootLogInstance() {
        return !TextUtils.isEmpty(this.mComponent) ? this.mComponent.equals(this.mTag) : true;
    }
}
