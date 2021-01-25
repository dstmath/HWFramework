package com.android.server.display;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class DisplayModeDirector {
    private static final boolean DEBUG = false;
    private static final float EPSILON = 0.001f;
    private static final int GLOBAL_ID = -1;
    private static final int MSG_ALLOWED_MODES_CHANGED = 1;
    private static final String TAG = "DisplayModeDirector";
    private final AppRequestObserver mAppRequestObserver;
    private final Context mContext;
    private final SparseArray<Display.Mode> mDefaultModeByDisplay;
    private final DisplayObserver mDisplayObserver;
    private final DisplayModeDirectorHandler mHandler;
    private Listener mListener;
    private final Object mLock = new Object();
    private final SettingsObserver mSettingsObserver;
    private final SparseArray<Display.Mode[]> mSupportedModesByDisplay;
    private final SparseArray<SparseArray<Vote>> mVotesByDisplay;

    public interface Listener {
        void onAllowedDisplayModesChanged();
    }

    public DisplayModeDirector(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = new DisplayModeDirectorHandler(handler.getLooper());
        this.mVotesByDisplay = new SparseArray<>();
        this.mSupportedModesByDisplay = new SparseArray<>();
        this.mDefaultModeByDisplay = new SparseArray<>();
        this.mAppRequestObserver = new AppRequestObserver();
        this.mSettingsObserver = new SettingsObserver(context, handler);
        this.mDisplayObserver = new DisplayObserver(context, handler);
    }

    public void start() {
        this.mSettingsObserver.observe();
        this.mDisplayObserver.observe();
        this.mSettingsObserver.observe();
        synchronized (this.mLock) {
            notifyAllowedModesChangedLocked();
        }
    }

    public int[] getAllowedModes(int displayId) {
        synchronized (this.mLock) {
            SparseArray<Vote> votes = getVotesLocked(displayId);
            Display.Mode[] modes = this.mSupportedModesByDisplay.get(displayId);
            Display.Mode defaultMode = this.mDefaultModeByDisplay.get(displayId);
            if (modes != null) {
                if (defaultMode != null) {
                    return getAllowedModesLocked(votes, modes, defaultMode);
                }
            }
            Slog.e(TAG, "Asked about unknown display, returning empty allowed set! (id=" + displayId + ")");
            return new int[0];
        }
    }

    private SparseArray<Vote> getVotesLocked(int displayId) {
        SparseArray<Vote> votes;
        SparseArray<Vote> displayVotes = this.mVotesByDisplay.get(displayId);
        if (displayVotes != null) {
            votes = displayVotes.clone();
        } else {
            votes = new SparseArray<>();
        }
        SparseArray<Vote> globalVotes = this.mVotesByDisplay.get(-1);
        if (globalVotes != null) {
            for (int i = 0; i < globalVotes.size(); i++) {
                int priority = globalVotes.keyAt(i);
                if (votes.indexOfKey(priority) < 0) {
                    votes.put(priority, globalVotes.valueAt(i));
                }
            }
        }
        return votes;
    }

    private int[] getAllowedModesLocked(SparseArray<Vote> votes, Display.Mode[] modes, Display.Mode defaultMode) {
        for (int lowestConsideredPriority = 0; lowestConsideredPriority <= 3; lowestConsideredPriority++) {
            float minRefreshRate = 0.0f;
            float maxRefreshRate = Float.POSITIVE_INFINITY;
            int height = -1;
            int width = -1;
            for (int priority = 3; priority >= lowestConsideredPriority; priority--) {
                Vote vote = votes.get(priority);
                if (vote != null) {
                    minRefreshRate = Math.max(minRefreshRate, vote.minRefreshRate);
                    maxRefreshRate = Math.min(maxRefreshRate, vote.maxRefreshRate);
                    if (height == -1 && width == -1 && vote.height > 0 && vote.width > 0) {
                        width = vote.width;
                        height = vote.height;
                    }
                }
            }
            if (height == -1 || width == -1) {
                width = defaultMode.getPhysicalWidth();
                height = defaultMode.getPhysicalHeight();
            }
            int[] availableModes = filterModes(modes, width, height, minRefreshRate, maxRefreshRate);
            if (availableModes.length > 0) {
                return availableModes;
            }
        }
        return new int[]{defaultMode.getModeId()};
    }

    private int[] filterModes(Display.Mode[] supportedModes, int width, int height, float minRefreshRate, float maxRefreshRate) {
        ArrayList<Display.Mode> availableModes = new ArrayList<>();
        for (Display.Mode mode : supportedModes) {
            if (mode.getPhysicalWidth() == width && mode.getPhysicalHeight() == height) {
                float refreshRate = mode.getRefreshRate();
                if (refreshRate >= minRefreshRate - EPSILON && refreshRate <= EPSILON + maxRefreshRate) {
                    availableModes.add(mode);
                }
            }
        }
        int size = availableModes.size();
        int[] availableModeIds = new int[size];
        for (int i = 0; i < size; i++) {
            availableModeIds[i] = availableModes.get(i).getModeId();
        }
        return availableModeIds;
    }

    public AppRequestObserver getAppRequestObserver() {
        return this.mAppRequestObserver;
    }

    public void setListener(Listener listener) {
        synchronized (this.mLock) {
            this.mListener = listener;
        }
    }

    public void dump(PrintWriter pw) {
        pw.println(TAG);
        synchronized (this.mLock) {
            pw.println("  mSupportedModesByDisplay:");
            for (int i = 0; i < this.mSupportedModesByDisplay.size(); i++) {
                pw.println("    " + this.mSupportedModesByDisplay.keyAt(i) + " -> " + Arrays.toString(this.mSupportedModesByDisplay.valueAt(i)));
            }
            pw.println("  mDefaultModeByDisplay:");
            for (int i2 = 0; i2 < this.mDefaultModeByDisplay.size(); i2++) {
                pw.println("    " + this.mDefaultModeByDisplay.keyAt(i2) + " -> " + this.mDefaultModeByDisplay.valueAt(i2));
            }
            pw.println("  mVotesByDisplay:");
            for (int i3 = 0; i3 < this.mVotesByDisplay.size(); i3++) {
                pw.println("    " + this.mVotesByDisplay.keyAt(i3) + ":");
                SparseArray<Vote> votes = this.mVotesByDisplay.valueAt(i3);
                for (int p = 3; p >= 0; p--) {
                    Vote vote = votes.get(p);
                    if (vote != null) {
                        pw.println("      " + Vote.priorityToString(p) + " -> " + vote);
                    }
                }
            }
            this.mSettingsObserver.dumpLocked(pw);
            this.mAppRequestObserver.dumpLocked(pw);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVoteLocked(int priority, Vote vote) {
        updateVoteLocked(-1, priority, vote);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVoteLocked(int displayId, int priority, Vote vote) {
        if (priority < 0 || priority > 3) {
            Slog.w(TAG, "Received a vote with an invalid priority, ignoring: priority=" + Vote.priorityToString(priority) + ", vote=" + vote, new Throwable());
            return;
        }
        SparseArray<Vote> votes = getOrCreateVotesByDisplay(displayId);
        votes.get(priority);
        if (vote != null) {
            votes.put(priority, vote);
        } else {
            votes.remove(priority);
        }
        if (votes.size() == 0) {
            this.mVotesByDisplay.remove(displayId);
        }
        notifyAllowedModesChangedLocked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAllowedModesChangedLocked() {
        if (this.mListener != null && !this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1, this.mListener).sendToTarget();
        }
    }

    private SparseArray<Vote> getOrCreateVotesByDisplay(int displayId) {
        this.mVotesByDisplay.indexOfKey(displayId);
        if (this.mVotesByDisplay.indexOfKey(displayId) >= 0) {
            return this.mVotesByDisplay.get(displayId);
        }
        SparseArray<Vote> votes = new SparseArray<>();
        this.mVotesByDisplay.put(displayId, votes);
        return votes;
    }

    /* access modifiers changed from: private */
    public static final class DisplayModeDirectorHandler extends Handler {
        DisplayModeDirectorHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((Listener) msg.obj).onAllowedDisplayModesChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class Vote {
        public static final int INVALID_SIZE = -1;
        public static final int MAX_PRIORITY = 3;
        public static final int MIN_PRIORITY = 0;
        public static final int PRIORITY_APP_REQUEST_REFRESH_RATE = 1;
        public static final int PRIORITY_APP_REQUEST_SIZE = 2;
        public static final int PRIORITY_LOW_POWER_MODE = 3;
        public static final int PRIORITY_USER_SETTING = 0;
        public final int height;
        public final float maxRefreshRate;
        public final float minRefreshRate;
        public final int width;

        public static Vote forRefreshRates(float minRefreshRate2, float maxRefreshRate2) {
            return new Vote(-1, -1, minRefreshRate2, maxRefreshRate2);
        }

        public static Vote forSize(int width2, int height2) {
            return new Vote(width2, height2, 0.0f, Float.POSITIVE_INFINITY);
        }

        private Vote(int width2, int height2, float minRefreshRate2, float maxRefreshRate2) {
            this.width = width2;
            this.height = height2;
            this.minRefreshRate = minRefreshRate2;
            this.maxRefreshRate = maxRefreshRate2;
        }

        public static String priorityToString(int priority) {
            if (priority == 0) {
                return "PRIORITY_USER_SETTING";
            }
            if (priority == 1) {
                return "PRIORITY_APP_REQUEST_REFRESH_RATE";
            }
            if (priority == 2) {
                return "PRIORITY_APP_REQUEST_SIZE";
            }
            if (priority != 3) {
                return Integer.toString(priority);
            }
            return "PRIORITY_LOW_POWER_MODE";
        }

        public String toString() {
            return "Vote{width=" + this.width + ", height=" + this.height + ", minRefreshRate=" + this.minRefreshRate + ", maxRefreshRate=" + this.maxRefreshRate + "}";
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final float mDefaultPeakRefreshRate;
        private final Uri mLowPowerModeSetting = Settings.Global.getUriFor("low_power");
        private final Uri mRefreshRateSetting = Settings.System.getUriFor("peak_refresh_rate");

        SettingsObserver(Context context, Handler handler) {
            super(handler);
            this.mContext = context;
            this.mDefaultPeakRefreshRate = (float) context.getResources().getInteger(17694780);
        }

        public void observe() {
            ContentResolver cr = this.mContext.getContentResolver();
            cr.registerContentObserver(this.mRefreshRateSetting, false, this, 0);
            cr.registerContentObserver(this.mLowPowerModeSetting, false, this, 0);
            synchronized (DisplayModeDirector.this.mLock) {
                updateRefreshRateSettingLocked();
                updateLowPowerModeSettingLocked();
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            synchronized (DisplayModeDirector.this.mLock) {
                if (this.mRefreshRateSetting.equals(uri)) {
                    updateRefreshRateSettingLocked();
                } else if (this.mLowPowerModeSetting.equals(uri)) {
                    updateLowPowerModeSettingLocked();
                }
            }
        }

        private void updateLowPowerModeSettingLocked() {
            Vote vote;
            boolean inLowPowerMode = false;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "low_power", 0) != 0) {
                inLowPowerMode = true;
            }
            if (inLowPowerMode) {
                vote = Vote.forRefreshRates(0.0f, 60.0f);
            } else {
                vote = null;
            }
            DisplayModeDirector.this.updateVoteLocked(3, vote);
        }

        private void updateRefreshRateSettingLocked() {
            DisplayModeDirector.this.updateVoteLocked(0, Vote.forRefreshRates(0.0f, Settings.System.getFloat(this.mContext.getContentResolver(), "peak_refresh_rate", this.mDefaultPeakRefreshRate)));
        }

        public void dumpLocked(PrintWriter pw) {
            pw.println("  SettingsObserver");
            pw.println("    mDefaultPeakRefreshRate: " + this.mDefaultPeakRefreshRate);
        }
    }

    /* access modifiers changed from: package-private */
    public final class AppRequestObserver {
        private SparseArray<Display.Mode> mAppRequestedModeByDisplay = new SparseArray<>();

        AppRequestObserver() {
        }

        public void setAppRequestedMode(int displayId, int modeId) {
            synchronized (DisplayModeDirector.this.mLock) {
                setAppRequestedModeLocked(displayId, modeId);
            }
        }

        private void setAppRequestedModeLocked(int displayId, int modeId) {
            Vote refreshRateVote;
            Vote sizeVote;
            Display.Mode requestedMode = findModeByIdLocked(displayId, modeId);
            if (!Objects.equals(requestedMode, this.mAppRequestedModeByDisplay.get(displayId))) {
                if (requestedMode != null) {
                    this.mAppRequestedModeByDisplay.put(displayId, requestedMode);
                    float refreshRate = requestedMode.getRefreshRate();
                    refreshRateVote = Vote.forRefreshRates(refreshRate, refreshRate);
                    sizeVote = Vote.forSize(requestedMode.getPhysicalWidth(), requestedMode.getPhysicalHeight());
                } else {
                    this.mAppRequestedModeByDisplay.remove(displayId);
                    refreshRateVote = null;
                    sizeVote = null;
                }
                DisplayModeDirector.this.updateVoteLocked(displayId, 1, refreshRateVote);
                DisplayModeDirector.this.updateVoteLocked(displayId, 2, sizeVote);
            }
        }

        private Display.Mode findModeByIdLocked(int displayId, int modeId) {
            Display.Mode[] modes = (Display.Mode[]) DisplayModeDirector.this.mSupportedModesByDisplay.get(displayId);
            if (modes == null) {
                return null;
            }
            for (Display.Mode mode : modes) {
                if (mode.getModeId() == modeId) {
                    return mode;
                }
            }
            return null;
        }

        public void dumpLocked(PrintWriter pw) {
            pw.println("  AppRequestObserver");
            pw.println("    mAppRequestedModeByDisplay:");
            for (int i = 0; i < this.mAppRequestedModeByDisplay.size(); i++) {
                int id = this.mAppRequestedModeByDisplay.keyAt(i);
                pw.println("    " + id + " -> " + this.mAppRequestedModeByDisplay.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public final class DisplayObserver implements DisplayManager.DisplayListener {
        private final Context mContext;
        private final Handler mHandler;

        DisplayObserver(Context context, Handler handler) {
            this.mContext = context;
            this.mHandler = handler;
        }

        public void observe() {
            DisplayManager dm = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
            dm.registerDisplayListener(this, this.mHandler);
            SparseArray<Display.Mode[]> modes = new SparseArray<>();
            SparseArray<Display.Mode> defaultModes = new SparseArray<>();
            DisplayInfo info = new DisplayInfo();
            Display[] displays = dm.getDisplays();
            for (Display d : displays) {
                int displayId = d.getDisplayId();
                d.getDisplayInfo(info);
                modes.put(displayId, info.supportedModes);
                defaultModes.put(displayId, info.getDefaultMode());
            }
            synchronized (DisplayModeDirector.this.mLock) {
                int size = modes.size();
                for (int i = 0; i < size; i++) {
                    DisplayModeDirector.this.mSupportedModesByDisplay.put(modes.keyAt(i), modes.valueAt(i));
                    DisplayModeDirector.this.mDefaultModeByDisplay.put(defaultModes.keyAt(i), defaultModes.valueAt(i));
                }
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
            updateDisplayModes(displayId);
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            synchronized (DisplayModeDirector.this.mLock) {
                DisplayModeDirector.this.mSupportedModesByDisplay.remove(displayId);
                DisplayModeDirector.this.mDefaultModeByDisplay.remove(displayId);
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            updateDisplayModes(displayId);
        }

        private void updateDisplayModes(int displayId) {
            Display d = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(displayId);
            if (d != null) {
                DisplayInfo info = new DisplayInfo();
                d.getDisplayInfo(info);
                boolean changed = false;
                synchronized (DisplayModeDirector.this.mLock) {
                    if (!Arrays.equals((Object[]) DisplayModeDirector.this.mSupportedModesByDisplay.get(displayId), info.supportedModes)) {
                        DisplayModeDirector.this.mSupportedModesByDisplay.put(displayId, info.supportedModes);
                        changed = true;
                    }
                    if (!Objects.equals(DisplayModeDirector.this.mDefaultModeByDisplay.get(displayId), info.getDefaultMode())) {
                        changed = true;
                        DisplayModeDirector.this.mDefaultModeByDisplay.put(displayId, info.getDefaultMode());
                    }
                    if (changed) {
                        DisplayModeDirector.this.notifyAllowedModesChangedLocked();
                    }
                }
            }
        }
    }
}
