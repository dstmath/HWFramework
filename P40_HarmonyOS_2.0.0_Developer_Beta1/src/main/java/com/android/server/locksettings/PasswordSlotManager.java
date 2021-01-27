package com.android.server.locksettings;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PasswordSlotManager {
    private static final String GSI_RUNNING_PROP = "ro.gsid.image_running";
    private static final String SLOT_MAP_DIR = "/metadata/password_slots";
    private static final String TAG = "PasswordSlotManager";
    private Set<Integer> mActiveSlots;
    private Map<Integer, String> mSlotMap;

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public String getSlotMapDir() {
        return SLOT_MAP_DIR;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getGsiImageNumber() {
        return SystemProperties.getInt(GSI_RUNNING_PROP, 0);
    }

    public void refreshActiveSlots(Set<Integer> activeSlots) throws RuntimeException {
        if (this.mSlotMap == null) {
            this.mActiveSlots = new HashSet(activeSlots);
            return;
        }
        HashSet<Integer> slotsToDelete = new HashSet<>();
        for (Map.Entry<Integer, String> entry : this.mSlotMap.entrySet()) {
            if (entry.getValue().equals(getMode())) {
                slotsToDelete.add(entry.getKey());
            }
        }
        Iterator<Integer> it = slotsToDelete.iterator();
        while (it.hasNext()) {
            this.mSlotMap.remove(it.next());
        }
        for (Integer slot : activeSlots) {
            this.mSlotMap.put(slot, getMode());
        }
        saveSlotMap();
    }

    public void markSlotInUse(int slot) throws RuntimeException {
        ensureSlotMapLoaded();
        if (!this.mSlotMap.containsKey(Integer.valueOf(slot)) || this.mSlotMap.get(Integer.valueOf(slot)).equals(getMode())) {
            this.mSlotMap.put(Integer.valueOf(slot), getMode());
            saveSlotMap();
            return;
        }
        throw new RuntimeException("password slot " + slot + " is not available");
    }

    public void markSlotDeleted(int slot) throws RuntimeException {
        ensureSlotMapLoaded();
        if (!this.mSlotMap.containsKey(Integer.valueOf(slot)) || this.mSlotMap.get(Integer.valueOf(slot)) == getMode()) {
            this.mSlotMap.remove(Integer.valueOf(slot));
            saveSlotMap();
            return;
        }
        throw new RuntimeException("password slot " + slot + " cannot be deleted");
    }

    public Set<Integer> getUsedSlots() {
        ensureSlotMapLoaded();
        return Collections.unmodifiableSet(this.mSlotMap.keySet());
    }

    private File getSlotMapFile() {
        return Paths.get(getSlotMapDir(), "slot_map").toFile();
    }

    private String getMode() {
        int gsiIndex = getGsiImageNumber();
        if (gsiIndex <= 0) {
            return WatchlistLoggingHandler.WatchlistEventKeys.HOST;
        }
        return "gsi" + gsiIndex;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Map<Integer, String> loadSlotMap(InputStream stream) throws IOException {
        HashMap<Integer, String> map = new HashMap<>();
        Properties props = new Properties();
        props.load(stream);
        for (String slotString : props.stringPropertyNames()) {
            int slot = Integer.parseInt(slotString);
            map.put(Integer.valueOf(slot), props.getProperty(slotString));
        }
        return map;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001b, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
        throw r3;
     */
    private Map<Integer, String> loadSlotMap() {
        File file = getSlotMapFile();
        if (file.exists()) {
            try {
                FileInputStream stream = new FileInputStream(file);
                Map<Integer, String> loadSlotMap = loadSlotMap(stream);
                $closeResource(null, stream);
                return loadSlotMap;
            } catch (Exception e) {
                Slog.e(TAG, "Could not load slot map file", e);
            }
        }
        return new HashMap();
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private void ensureSlotMapLoaded() {
        if (this.mSlotMap == null) {
            this.mSlotMap = loadSlotMap();
            Set<Integer> set = this.mActiveSlots;
            if (set != null) {
                refreshActiveSlots(set);
                this.mActiveSlots = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void saveSlotMap(OutputStream stream) throws IOException {
        if (this.mSlotMap != null) {
            Properties props = new Properties();
            for (Map.Entry<Integer, String> entry : this.mSlotMap.entrySet()) {
                props.setProperty(entry.getKey().toString(), entry.getValue());
            }
            props.store(stream, "");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0046, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        $closeResource(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004a, code lost:
        throw r3;
     */
    private void saveSlotMap() {
        if (this.mSlotMap != null) {
            if (!getSlotMapFile().getParentFile().exists()) {
                Slog.w(TAG, "Not saving slot map, " + getSlotMapDir() + " does not exist");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(getSlotMapFile());
                saveSlotMap(fos);
                $closeResource(null, fos);
            } catch (IOException e) {
                Slog.e(TAG, "failed to save password slot map", e);
            }
        }
    }
}
