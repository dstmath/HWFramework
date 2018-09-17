package android.app.backup;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class BackupHelperDispatcher {
    private static final String TAG = "BackupHelperDispatcher";
    TreeMap<String, BackupHelper> mHelpers = new TreeMap();

    private static class Header {
        int chunkSize;
        String keyPrefix;

        /* synthetic */ Header(Header -this0) {
            this();
        }

        private Header() {
        }
    }

    private static native int allocateHeader_native(Header header, FileDescriptor fileDescriptor);

    private static native int readHeader_native(Header header, FileDescriptor fileDescriptor);

    private static native int skipChunk_native(FileDescriptor fileDescriptor, int i);

    private static native int writeHeader_native(Header header, FileDescriptor fileDescriptor, int i);

    public void addHelper(String keyPrefix, BackupHelper helper) {
        this.mHelpers.put(keyPrefix, helper);
    }

    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        Header header = new Header();
        TreeMap<String, BackupHelper> helpers = (TreeMap) this.mHelpers.clone();
        if (oldState != null) {
            FileDescriptor oldStateFD = oldState.getFileDescriptor();
            while (true) {
                int err = readHeader_native(header, oldStateFD);
                if (err < 0) {
                    break;
                } else if (err == 0) {
                    BackupHelper helper = (BackupHelper) helpers.get(header.keyPrefix);
                    Log.d(TAG, "handling existing helper '" + header.keyPrefix + "' " + helper);
                    if (helper != null) {
                        doOneBackup(oldState, data, newState, header, helper);
                        helpers.remove(header.keyPrefix);
                    } else {
                        skipChunk_native(oldStateFD, header.chunkSize);
                    }
                }
            }
        }
        for (Entry<String, BackupHelper> entry : helpers.entrySet()) {
            header.keyPrefix = (String) entry.getKey();
            Log.d(TAG, "handling new helper '" + header.keyPrefix + "'");
            doOneBackup(oldState, data, newState, header, (BackupHelper) entry.getValue());
        }
    }

    private void doOneBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState, Header header, BackupHelper helper) throws IOException {
        FileDescriptor newStateFD = newState.getFileDescriptor();
        int pos = allocateHeader_native(header, newStateFD);
        if (pos < 0) {
            throw new IOException("allocateHeader_native failed (error " + pos + ")");
        }
        data.setKeyPrefix(header.keyPrefix);
        helper.performBackup(oldState, data, newState);
        int err = writeHeader_native(header, newStateFD, pos);
        if (err != 0) {
            throw new IOException("writeHeader_native failed (error " + err + ")");
        }
    }

    public void performRestore(BackupDataInput input, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        BackupHelper helper;
        boolean alreadyComplained = false;
        BackupDataInputStream stream = new BackupDataInputStream(input);
        while (input.readNextHeader()) {
            String rawKey = input.getKey();
            int pos = rawKey.indexOf(58);
            if (pos > 0) {
                helper = (BackupHelper) this.mHelpers.get(rawKey.substring(0, pos));
                if (helper != null) {
                    stream.dataSize = input.getDataSize();
                    stream.key = rawKey.substring(pos + 1);
                    helper.restoreEntity(stream);
                } else if (!alreadyComplained) {
                    Log.w(TAG, "Couldn't find helper for: '" + rawKey + "'");
                    alreadyComplained = true;
                }
            } else if (!alreadyComplained) {
                Log.w(TAG, "Entity with no prefix: '" + rawKey + "'");
                alreadyComplained = true;
            }
            input.skipEntityData();
        }
        for (BackupHelper helper2 : this.mHelpers.values()) {
            helper2.writeNewStateDescription(newState);
        }
    }
}
