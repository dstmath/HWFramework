package android_maps_conflict_avoidance.com.google.common.io;

import java.io.IOException;

public interface PersistentStore {

    public static class PersistentStoreException extends IOException {
        private final int type;

        public PersistentStoreException(String string, int type) {
            super(string);
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    void deleteAllBlocks(String str);

    boolean deleteBlock(String str);

    String[] listBlocks(String str);

    byte[] readBlock(String str);

    byte[] readPreference(String str);

    void savePreferences();

    boolean setPreference(String str, byte[] bArr);

    int writeBlock(byte[] bArr, String str);

    int writeBlockX(byte[] bArr, String str) throws PersistentStoreException;
}
