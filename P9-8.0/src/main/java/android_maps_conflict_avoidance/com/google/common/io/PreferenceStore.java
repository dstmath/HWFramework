package android_maps_conflict_avoidance.com.google.common.io;

import android_maps_conflict_avoidance.com.google.common.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class PreferenceStore {
    private final PersistentStore persistentStore;
    private Hashtable preferences = null;
    private boolean preferencesChanged = false;

    public PreferenceStore(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    /* JADX WARNING: Missing block: B:13:0x001b, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean setPreference(String preference, byte[] data) {
        boolean z = true;
        synchronized (this) {
            ensurePreferencesLoaded();
            this.preferencesChanged = true;
            if (data != null) {
                this.preferences.put(preference, data);
                return true;
            } else if (this.preferences.remove(preference) == null) {
                z = false;
            }
        }
    }

    public synchronized byte[] readPreference(String preference) {
        ensurePreferencesLoaded();
        return (byte[]) this.preferences.get(preference);
    }

    public static byte[] packPreferences(Hashtable prefs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput dos = new DataOutputStream(baos);
        dos.writeShort(prefs.size());
        Enumeration keys = prefs.keys();
        while (keys.hasMoreElements()) {
            String preference = (String) keys.nextElement();
            byte[] data = (byte[]) prefs.get(preference);
            if (data == null) {
                data = new byte[0];
            }
            dos.writeUTF(preference);
            dos.writeShort(data.length);
            dos.write(data);
        }
        return baos.toByteArray();
    }

    public static Hashtable unpackPreferences(byte[] bytes) {
        DataInput dis = new DataInputStream(new ByteArrayInputStream(bytes));
        Hashtable prefs = new Hashtable();
        try {
            int size = dis.readUnsignedShort();
            for (int i = 0; i < size; i++) {
                String preference = dis.readUTF();
                byte[] prefData = new byte[dis.readUnsignedShort()];
                dis.readFully(prefData);
                prefs.put(preference, prefData);
            }
        } catch (IOException e) {
            Log.logThrowable("FLASH", e);
        }
        return prefs;
    }

    private static byte[] packPreferencesBlock(Hashtable prefs) throws IOException {
        byte[] packed = packPreferences(prefs);
        if (packed.length > 2000) {
            return packed;
        }
        byte[] packedBlock = new byte[2000];
        System.arraycopy(packed, 0, packedBlock, 0, packed.length);
        return packedBlock;
    }

    private synchronized void ensurePreferencesLoaded() {
        if (this.preferences == null) {
            byte[] bytes = this.persistentStore.readBlock("Preferences");
            if (bytes == null) {
                this.persistentStore.writeBlock(new byte[2000], "Preferences");
                this.preferences = new Hashtable();
            } else {
                this.preferences = unpackPreferences(bytes);
            }
            this.preferencesChanged = false;
        }
    }

    public synchronized void savePreferences() {
        if (this.preferencesChanged) {
            try {
                this.persistentStore.writeBlock(packPreferencesBlock(this.preferences), "Preferences");
            } catch (IOException e) {
                Log.logThrowable("FLASH", e);
            }
            this.preferencesChanged = false;
            return;
        }
        return;
    }
}
