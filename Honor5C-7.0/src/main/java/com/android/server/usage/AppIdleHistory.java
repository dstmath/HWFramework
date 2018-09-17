package com.android.server.usage;

import android.os.Environment;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;

public class AppIdleHistory {
    static final String APP_IDLE_FILENAME = "app_idle_stats.xml";
    private static final String ATTR_ELAPSED_IDLE = "elapsedIdleTime";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SCREEN_IDLE = "screenIdleTime";
    private static final int FLAG_LAST_STATE = 2;
    private static final int FLAG_PARTIAL_ACTIVE = 1;
    private static final int HISTORY_SIZE = 100;
    private static final long ONE_MINUTE = 60000;
    private static final long PERIOD_DURATION = 3600000;
    private static final String TAG = "AppIdleHistory";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PACKAGES = "packages";
    private long mElapsedDuration;
    private long mElapsedSnapshot;
    private long mElapsedTimeThreshold;
    private SparseArray<ArrayMap<String, PackageHistory>> mIdleHistory;
    private long mLastPeriod;
    private boolean mScreenOn;
    private long mScreenOnDuration;
    private long mScreenOnSnapshot;
    private long mScreenOnTimeThreshold;
    private final File mStorageDir;

    private static class PackageHistory {
        long lastUsedElapsedTime;
        long lastUsedScreenTime;
        final byte[] recent;

        private PackageHistory() {
            this.recent = new byte[AppIdleHistory.HISTORY_SIZE];
        }
    }

    AppIdleHistory(long elapsedRealtime) {
        this(Environment.getDataSystemDirectory(), elapsedRealtime);
    }

    AppIdleHistory(File storageDir, long elapsedRealtime) {
        this.mIdleHistory = new SparseArray();
        this.mLastPeriod = 0;
        this.mElapsedSnapshot = elapsedRealtime;
        this.mScreenOnSnapshot = elapsedRealtime;
        this.mStorageDir = storageDir;
        readScreenOnTimeLocked();
    }

    public void setThresholds(long elapsedTimeThreshold, long screenOnTimeThreshold) {
        this.mElapsedTimeThreshold = elapsedTimeThreshold;
        this.mScreenOnTimeThreshold = screenOnTimeThreshold;
    }

    public void updateDisplayLocked(boolean screenOn, long elapsedRealtime) {
        if (screenOn != this.mScreenOn) {
            this.mScreenOn = screenOn;
            if (this.mScreenOn) {
                this.mScreenOnSnapshot = elapsedRealtime;
            } else {
                this.mScreenOnDuration += elapsedRealtime - this.mScreenOnSnapshot;
                this.mElapsedDuration += elapsedRealtime - this.mElapsedSnapshot;
                writeScreenOnTimeLocked();
                this.mElapsedSnapshot = elapsedRealtime;
            }
        }
    }

    public long getScreenOnTimeLocked(long elapsedRealtime) {
        long screenOnTime = this.mScreenOnDuration;
        if (this.mScreenOn) {
            return screenOnTime + (elapsedRealtime - this.mScreenOnSnapshot);
        }
        return screenOnTime;
    }

    File getScreenOnTimeFile() {
        return new File(this.mStorageDir, "screen_on_time");
    }

    private void readScreenOnTimeLocked() {
        File screenOnTimeFile = getScreenOnTimeFile();
        if (screenOnTimeFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(screenOnTimeFile));
                this.mScreenOnDuration = Long.parseLong(reader.readLine());
                this.mElapsedDuration = Long.parseLong(reader.readLine());
                reader.close();
                return;
            } catch (IOException e) {
                return;
            }
        }
        writeScreenOnTimeLocked();
    }

    private void writeScreenOnTimeLocked() {
        AtomicFile screenOnTimeFile = new AtomicFile(getScreenOnTimeFile());
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = screenOnTimeFile.startWrite();
            fileOutputStream.write((Long.toString(this.mScreenOnDuration) + "\n" + Long.toString(this.mElapsedDuration) + "\n").getBytes());
            screenOnTimeFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            screenOnTimeFile.failWrite(fileOutputStream);
        }
    }

    public void writeElapsedTimeLocked() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mElapsedDuration += elapsedRealtime - this.mElapsedSnapshot;
        this.mElapsedSnapshot = elapsedRealtime;
        writeScreenOnTimeLocked();
    }

    public void reportUsageLocked(String packageName, int userId, long elapsedRealtime) {
        ArrayMap<String, PackageHistory> userHistory = getUserHistoryLocked(userId);
        PackageHistory packageHistory = getPackageHistoryLocked(userHistory, packageName, elapsedRealtime);
        shiftHistoryToNow(userHistory, elapsedRealtime);
        packageHistory.lastUsedElapsedTime = this.mElapsedDuration + (elapsedRealtime - this.mElapsedSnapshot);
        packageHistory.lastUsedScreenTime = getScreenOnTimeLocked(elapsedRealtime);
        packageHistory.recent[99] = (byte) 3;
    }

    public void setIdle(String packageName, int userId, long elapsedRealtime) {
        ArrayMap<String, PackageHistory> userHistory = getUserHistoryLocked(userId);
        PackageHistory packageHistory = getPackageHistoryLocked(userHistory, packageName, elapsedRealtime);
        shiftHistoryToNow(userHistory, elapsedRealtime);
        byte[] bArr = packageHistory.recent;
        bArr[99] = (byte) (bArr[99] & -3);
    }

    private void shiftHistoryToNow(ArrayMap<String, PackageHistory> arrayMap, long elapsedRealtime) {
        long thisPeriod = elapsedRealtime / PERIOD_DURATION;
        if (this.mLastPeriod != 0 && this.mLastPeriod < thisPeriod && thisPeriod - this.mLastPeriod < 99) {
            int diff = (int) (thisPeriod - this.mLastPeriod);
            int NUSERS = this.mIdleHistory.size();
            for (int u = 0; u < NUSERS; u += FLAG_PARTIAL_ACTIVE) {
                for (PackageHistory idleState : ((ArrayMap) this.mIdleHistory.valueAt(u)).values()) {
                    System.arraycopy(idleState.recent, diff, idleState.recent, 0, 100 - diff);
                    for (int i = 0; i < diff; i += FLAG_PARTIAL_ACTIVE) {
                        idleState.recent[(100 - i) - 1] = (byte) (idleState.recent[(100 - diff) - 1] & FLAG_LAST_STATE);
                    }
                }
            }
        }
        this.mLastPeriod = thisPeriod;
    }

    private ArrayMap<String, PackageHistory> getUserHistoryLocked(int userId) {
        ArrayMap<String, PackageHistory> userHistory = (ArrayMap) this.mIdleHistory.get(userId);
        if (userHistory != null) {
            return userHistory;
        }
        userHistory = new ArrayMap();
        this.mIdleHistory.put(userId, userHistory);
        readAppIdleTimesLocked(userId, userHistory);
        return userHistory;
    }

    private PackageHistory getPackageHistoryLocked(ArrayMap<String, PackageHistory> userHistory, String packageName, long elapsedRealtime) {
        PackageHistory packageHistory = (PackageHistory) userHistory.get(packageName);
        if (packageHistory != null) {
            return packageHistory;
        }
        packageHistory = new PackageHistory();
        packageHistory.lastUsedElapsedTime = getElapsedTimeLocked(elapsedRealtime);
        packageHistory.lastUsedScreenTime = getScreenOnTimeLocked(elapsedRealtime);
        userHistory.put(packageName, packageHistory);
        return packageHistory;
    }

    public void onUserRemoved(int userId) {
        this.mIdleHistory.remove(userId);
    }

    public boolean isIdleLocked(String packageName, int userId, long elapsedRealtime) {
        PackageHistory packageHistory = getPackageHistoryLocked(getUserHistoryLocked(userId), packageName, elapsedRealtime);
        if (packageHistory == null) {
            return false;
        }
        return hasPassedThresholdsLocked(packageHistory, elapsedRealtime);
    }

    private long getElapsedTimeLocked(long elapsedRealtime) {
        return (elapsedRealtime - this.mElapsedSnapshot) + this.mElapsedDuration;
    }

    public void setIdleLocked(String packageName, int userId, boolean idle, long elapsedRealtime) {
        PackageHistory packageHistory = getPackageHistoryLocked(getUserHistoryLocked(userId), packageName, elapsedRealtime);
        packageHistory.lastUsedElapsedTime = getElapsedTimeLocked(elapsedRealtime) - this.mElapsedTimeThreshold;
        packageHistory.lastUsedScreenTime = (getScreenOnTimeLocked(elapsedRealtime) - (idle ? this.mScreenOnTimeThreshold : 0)) - 1000;
    }

    public void clearUsageLocked(String packageName, int userId) {
        getUserHistoryLocked(userId).remove(packageName);
    }

    private boolean hasPassedThresholdsLocked(PackageHistory packageHistory, long elapsedRealtime) {
        if (packageHistory.lastUsedScreenTime > getScreenOnTimeLocked(elapsedRealtime) - this.mScreenOnTimeThreshold || packageHistory.lastUsedElapsedTime > getElapsedTimeLocked(elapsedRealtime) - this.mElapsedTimeThreshold) {
            return false;
        }
        return true;
    }

    private File getUserFile(int userId) {
        return new File(new File(new File(this.mStorageDir, SoundModelContract.KEY_USERS), Integer.toString(userId)), APP_IDLE_FILENAME);
    }

    private void readAppIdleTimesLocked(int userId, ArrayMap<String, PackageHistory> userHistory) {
        AutoCloseable autoCloseable = null;
        try {
            int type;
            autoCloseable = new AtomicFile(getUserFile(userId)).openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            while (true) {
                type = parser.next();
                if (type != FLAG_LAST_STATE) {
                    if (type == FLAG_PARTIAL_ACTIVE) {
                        break;
                    }
                }
                break;
            }
            if (type != FLAG_LAST_STATE) {
                Slog.e(TAG, "Unable to read app idle file for user " + userId);
            } else if (parser.getName().equals(TAG_PACKAGES)) {
                while (true) {
                    type = parser.next();
                    if (type == FLAG_PARTIAL_ACTIVE) {
                        break;
                    } else if (type == FLAG_LAST_STATE && parser.getName().equals(TAG_PACKAGE)) {
                        String packageName = parser.getAttributeValue(null, ATTR_NAME);
                        PackageHistory packageHistory = new PackageHistory();
                        packageHistory.lastUsedElapsedTime = Long.parseLong(parser.getAttributeValue(null, ATTR_ELAPSED_IDLE));
                        packageHistory.lastUsedScreenTime = Long.parseLong(parser.getAttributeValue(null, ATTR_SCREEN_IDLE));
                        userHistory.put(packageName, packageHistory);
                    }
                }
                IoUtils.closeQuietly(autoCloseable);
            } else {
                IoUtils.closeQuietly(autoCloseable);
            }
        } catch (IOException e) {
            Slog.e(TAG, "Unable to read app idle file for user " + userId);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public void writeAppIdleTimesLocked(int userId) {
        AtomicFile appIdleFile = new AtomicFile(getUserFile(userId));
        try {
            FileOutputStream fos = appIdleFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            FastXmlSerializer xml = new FastXmlSerializer();
            xml.setOutput(bos, StandardCharsets.UTF_8.name());
            xml.startDocument(null, Boolean.valueOf(true));
            xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xml.startTag(null, TAG_PACKAGES);
            ArrayMap<String, PackageHistory> userHistory = getUserHistoryLocked(userId);
            int N = userHistory.size();
            for (int i = 0; i < N; i += FLAG_PARTIAL_ACTIVE) {
                String packageName = (String) userHistory.keyAt(i);
                PackageHistory history = (PackageHistory) userHistory.valueAt(i);
                xml.startTag(null, TAG_PACKAGE);
                xml.attribute(null, ATTR_NAME, packageName);
                xml.attribute(null, ATTR_ELAPSED_IDLE, Long.toString(history.lastUsedElapsedTime));
                xml.attribute(null, ATTR_SCREEN_IDLE, Long.toString(history.lastUsedScreenTime));
                xml.endTag(null, TAG_PACKAGE);
            }
            xml.endTag(null, TAG_PACKAGES);
            xml.endDocument();
            appIdleFile.finishWrite(fos);
        } catch (Exception e) {
            appIdleFile.failWrite(null);
            Slog.e(TAG, "Error writing app idle file for user " + userId);
        }
    }

    public void dump(IndentingPrintWriter idpw, int userId) {
        idpw.println("Package idle stats:");
        idpw.increaseIndent();
        ArrayMap<String, PackageHistory> userHistory = (ArrayMap) this.mIdleHistory.get(userId);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long totalElapsedTime = getElapsedTimeLocked(elapsedRealtime);
        long screenOnTime = getScreenOnTimeLocked(elapsedRealtime);
        if (userHistory != null) {
            int P = userHistory.size();
            for (int p = 0; p < P; p += FLAG_PARTIAL_ACTIVE) {
                String packageName = (String) userHistory.keyAt(p);
                PackageHistory packageHistory = (PackageHistory) userHistory.valueAt(p);
                idpw.print("package=" + packageName);
                idpw.print(" lastUsedElapsed=");
                TimeUtils.formatDuration(totalElapsedTime - packageHistory.lastUsedElapsedTime, idpw);
                idpw.print(" lastUsedScreenOn=");
                TimeUtils.formatDuration(screenOnTime - packageHistory.lastUsedScreenTime, idpw);
                idpw.print(" idle=" + (isIdleLocked(packageName, userId, elapsedRealtime) ? "y" : "n"));
                idpw.println();
            }
            idpw.println();
            idpw.print("totalElapsedTime=");
            TimeUtils.formatDuration(getElapsedTimeLocked(elapsedRealtime), idpw);
            idpw.println();
            idpw.print("totalScreenOnTime=");
            TimeUtils.formatDuration(getScreenOnTimeLocked(elapsedRealtime), idpw);
            idpw.println();
            idpw.decreaseIndent();
        }
    }

    public void dumpHistory(IndentingPrintWriter idpw, int userId) {
        ArrayMap<String, PackageHistory> userHistory = (ArrayMap) this.mIdleHistory.get(userId);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (userHistory != null) {
            int P = userHistory.size();
            for (int p = 0; p < P; p += FLAG_PARTIAL_ACTIVE) {
                String packageName = (String) userHistory.keyAt(p);
                byte[] history = ((PackageHistory) userHistory.valueAt(p)).recent;
                for (int i = 0; i < HISTORY_SIZE; i += FLAG_PARTIAL_ACTIVE) {
                    idpw.print(history[i] == null ? '.' : 'A');
                }
                idpw.print(" idle=" + (isIdleLocked(packageName, userId, elapsedRealtime) ? "y" : "n"));
                idpw.print("  " + packageName);
                idpw.println();
            }
        }
    }
}
