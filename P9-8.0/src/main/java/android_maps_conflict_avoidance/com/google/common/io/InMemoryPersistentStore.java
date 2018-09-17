package android_maps_conflict_avoidance.com.google.common.io;

import android_maps_conflict_avoidance.com.google.common.io.PersistentStore.PersistentStoreException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class InMemoryPersistentStore implements PersistentStore {
    private Hashtable blocks = new Hashtable();
    private final Hashtable prefs = new Hashtable();

    public synchronized boolean deleteBlock(String name) {
        if (!this.blocks.containsKey(name)) {
            return false;
        }
        this.blocks.remove(name);
        return true;
    }

    public synchronized void deleteAllBlocks(String namePrefix) {
        Enumeration keys = this.blocks.keys();
        Hashtable newBlocks = new Hashtable();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (!key.startsWith(namePrefix)) {
                newBlocks.put(key, this.blocks.get(key));
            }
        }
        this.blocks = newBlocks;
    }

    public int writeBlockX(byte[] data, String name) throws PersistentStoreException {
        return writeBlock(data, name);
    }

    public synchronized int writeBlock(byte[] data, String name) {
        Object data2;
        if (data2 == null) {
            data2 = new byte[0];
        }
        this.blocks.put(name, data2);
        return data2.length;
    }

    public byte[] readBlock(String name) {
        return (byte[]) this.blocks.get(name);
    }

    public synchronized String[] listBlocks(String namePrefix) {
        String[] results;
        Vector resultVector = new Vector();
        Enumeration keys = this.blocks.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith(namePrefix)) {
                resultVector.addElement(key);
            }
        }
        results = new String[resultVector.size()];
        resultVector.copyInto(results);
        return results;
    }

    public boolean setPreference(String name, byte[] data) {
        if (data != null) {
            this.prefs.put(name, data);
        } else {
            this.prefs.remove(name);
        }
        return true;
    }

    public byte[] readPreference(String name) {
        return (byte[]) this.prefs.get(name);
    }

    public void savePreferences() {
    }
}
