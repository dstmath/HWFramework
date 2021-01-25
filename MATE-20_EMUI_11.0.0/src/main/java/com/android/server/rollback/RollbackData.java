package com.android.server.rollback;

import android.content.rollback.RollbackInfo;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class RollbackData {
    static final int ROLLBACK_STATE_AVAILABLE = 1;
    static final int ROLLBACK_STATE_COMMITTED = 3;
    static final int ROLLBACK_STATE_ENABLING = 0;
    public int apkSessionId = -1;
    public final File backupDir;
    public final RollbackInfo info;
    public boolean restoreUserDataInProgress = false;
    public final int stagedSessionId;
    public int state;
    public Instant timestamp;

    @Retention(RetentionPolicy.SOURCE)
    @interface RollbackState {
    }

    RollbackData(int rollbackId, File backupDir2, int stagedSessionId2) {
        this.info = new RollbackInfo(rollbackId, new ArrayList(), stagedSessionId2 != -1, new ArrayList(), -1);
        this.backupDir = backupDir2;
        this.stagedSessionId = stagedSessionId2;
        this.state = 0;
        this.timestamp = Instant.now();
    }

    RollbackData(RollbackInfo info2, File backupDir2, Instant timestamp2, int stagedSessionId2, int state2, int apkSessionId2, boolean restoreUserDataInProgress2) {
        this.info = info2;
        this.backupDir = backupDir2;
        this.timestamp = timestamp2;
        this.stagedSessionId = stagedSessionId2;
        this.state = state2;
        this.apkSessionId = apkSessionId2;
        this.restoreUserDataInProgress = restoreUserDataInProgress2;
    }

    public boolean isStaged() {
        return this.info.isStaged();
    }

    static String rollbackStateToString(int state2) {
        if (state2 == 0) {
            return "enabling";
        }
        if (state2 == 1) {
            return "available";
        }
        if (state2 == 3) {
            return "committed";
        }
        throw new AssertionError("Invalid rollback state: " + state2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0056 A[RETURN] */
    static int rollbackStateFromString(String state2) throws ParseException {
        char c;
        int hashCode = state2.hashCode();
        if (hashCode != -1491142788) {
            if (hashCode != -733902135) {
                if (hashCode == 1642196352 && state2.equals("enabling")) {
                    c = 0;
                    if (c != 0) {
                        return 0;
                    }
                    if (c == 1) {
                        return 1;
                    }
                    if (c == 2) {
                        return 3;
                    }
                    throw new ParseException("Invalid rollback state: " + state2, 0);
                }
            } else if (state2.equals("available")) {
                c = 1;
                if (c != 0) {
                }
            }
        } else if (state2.equals("committed")) {
            c = 2;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    public String getStateAsString() {
        return rollbackStateToString(this.state);
    }
}
