package android.app.backup;

import android.os.ParcelFileDescriptor;
import android.util.ArrayMap;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public abstract class BlobBackupHelper implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "BlobBackupHelper";
    private final int mCurrentBlobVersion;
    private final String[] mKeys;

    protected abstract void applyRestoredPayload(String str, byte[] bArr);

    protected abstract byte[] getBackupPayload(String str);

    public BlobBackupHelper(int currentBlobVersion, String... keys) {
        this.mCurrentBlobVersion = currentBlobVersion;
        this.mKeys = keys;
    }

    private ArrayMap<String, Long> readOldState(ParcelFileDescriptor oldStateFd) {
        ArrayMap<String, Long> state = new ArrayMap();
        DataInputStream in = new DataInputStream(new FileInputStream(oldStateFd.getFileDescriptor()));
        try {
            int version = in.readInt();
            if (version <= this.mCurrentBlobVersion) {
                int numKeys = in.readInt();
                for (int i = 0; i < numKeys; i++) {
                    state.put(in.readUTF(), Long.valueOf(in.readLong()));
                }
            } else {
                Log.w(TAG, "Prior state from unrecognized version " + version);
            }
        } catch (EOFException e) {
            state.clear();
        } catch (Exception e2) {
            Log.e(TAG, "Error examining prior backup state " + e2.getMessage());
            state.clear();
        }
        return state;
    }

    private void writeBackupState(ArrayMap<String, Long> state, ParcelFileDescriptor stateFile) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(stateFile.getFileDescriptor()));
            out.writeInt(this.mCurrentBlobVersion);
            int N = state != null ? state.size() : 0;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                String key = (String) state.keyAt(i);
                long checksum = ((Long) state.valueAt(i)).longValue();
                out.writeUTF(key);
                out.writeLong(checksum);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to write updated state", e);
        }
    }

    private byte[] deflate(byte[] data) {
        byte[] result = null;
        if (data == null) {
            return result;
        }
        try {
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            new DataOutputStream(sink).writeInt(this.mCurrentBlobVersion);
            DeflaterOutputStream out = new DeflaterOutputStream(sink);
            out.write(data);
            out.close();
            return sink.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, "Unable to process payload: " + e.getMessage());
            return result;
        }
    }

    private byte[] inflate(byte[] compressedData) {
        byte[] result = null;
        if (compressedData != null) {
            try {
                ByteArrayInputStream source = new ByteArrayInputStream(compressedData);
                int version = new DataInputStream(source).readInt();
                if (version > this.mCurrentBlobVersion) {
                    Log.w(TAG, "Saved payload from unrecognized version " + version);
                    return null;
                }
                InflaterInputStream in = new InflaterInputStream(source);
                ByteArrayOutputStream inflated = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                while (true) {
                    int nRead = in.read(buffer);
                    if (nRead <= 0) {
                        break;
                    }
                    inflated.write(buffer, 0, nRead);
                }
                in.close();
                inflated.flush();
                result = inflated.toByteArray();
            } catch (IOException e) {
                Log.w(TAG, "Unable to process restored payload: " + e.getMessage());
            }
        }
        return result;
    }

    private long checksum(byte[] buffer) {
        if (buffer != null) {
            try {
                CRC32 crc = new CRC32();
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                byte[] buf = new byte[4096];
                while (true) {
                    int nRead = bis.read(buf);
                    if (nRead < 0) {
                        return crc.getValue();
                    }
                    crc.update(buf, 0, nRead);
                }
            } catch (Exception e) {
            }
        }
        return -1;
    }

    public void performBackup(ParcelFileDescriptor oldStateFd, BackupDataOutput data, ParcelFileDescriptor newStateFd) {
        ArrayMap<String, Long> oldState = readOldState(oldStateFd);
        ArrayMap<String, Long> newState = new ArrayMap();
        try {
            for (String key : this.mKeys) {
                byte[] payload = deflate(getBackupPayload(key));
                long checksum = checksum(payload);
                newState.put(key, Long.valueOf(checksum));
                Long oldChecksum = (Long) oldState.get(key);
                if (oldChecksum == null || checksum != oldChecksum.longValue()) {
                    if (payload != null) {
                        data.writeEntityHeader(key, payload.length);
                        data.writeEntityData(payload, payload.length);
                    } else {
                        data.writeEntityHeader(key, -1);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to record notification state: " + e.getMessage());
            newState.clear();
        } finally {
            writeBackupState(newState, newStateFd);
        }
    }

    public void restoreEntity(BackupDataInputStream data) {
        String key = data.getKey();
        int which = 0;
        while (which < this.mKeys.length && !key.equals(this.mKeys[which])) {
            try {
                which++;
            } catch (Exception e) {
                Log.e(TAG, "Exception restoring entity " + key + " : " + e.getMessage());
            }
        }
        if (which >= this.mKeys.length) {
            Log.e(TAG, "Unrecognized key " + key + ", ignoring");
            return;
        }
        byte[] compressed = new byte[data.size()];
        data.read(compressed);
        applyRestoredPayload(key, inflate(compressed));
    }

    public void writeNewStateDescription(ParcelFileDescriptor newState) {
        writeBackupState(null, newState);
    }
}
