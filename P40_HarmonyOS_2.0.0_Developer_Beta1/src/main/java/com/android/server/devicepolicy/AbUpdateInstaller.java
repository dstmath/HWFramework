package com.android.server.devicepolicy;

import android.app.admin.StartInstallingUpdateCallback;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.util.Log;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

class AbUpdateInstaller extends UpdateInstaller {
    private static final int DOWNLOAD_STATE_INITIALIZATION_ERROR = 20;
    private static final int OFFSET_TO_FILE_NAME = 30;
    private static final String PAYLOAD_BIN = "payload.bin";
    private static final String PAYLOAD_PROPERTIES_TXT = "payload_properties.txt";
    public static final String UNKNOWN_ERROR = "Unknown error with error code = ";
    private static final Map<Integer, Integer> errorCodesMap = buildErrorCodesMap();
    private static final Map<Integer, String> errorStringsMap = buildErrorStringsMap();
    private Enumeration<? extends ZipEntry> mEntries;
    private long mOffsetForUpdate;
    private ZipFile mPackedUpdateFile;
    private List<String> mProperties;
    private long mSizeForUpdate;
    private boolean mUpdateInstalled = false;

    private static Map<Integer, Integer> buildErrorCodesMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(20, 2);
        map.put(51, 2);
        map.put(12, 3);
        map.put(11, 3);
        map.put(6, 3);
        map.put(10, 3);
        map.put(26, 3);
        map.put(5, 1);
        map.put(7, 1);
        map.put(9, 1);
        map.put(52, 1);
        return map;
    }

    private static Map<Integer, String> buildErrorStringsMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, UNKNOWN_ERROR);
        map.put(20, "The delta update payload was targeted for another version or the source partitionwas modified after it was installed");
        map.put(5, "Failed to finish the configured postinstall works.");
        map.put(7, "Failed to open one of the partitions it tried to write to or read data from.");
        map.put(6, "Payload mismatch error.");
        map.put(9, "Failed to read the payload data from the given URL.");
        map.put(10, "Payload hash error.");
        map.put(11, "Payload size mismatch error.");
        map.put(12, "Failed to verify the signature of the payload.");
        map.put(52, "The payload has been successfully installed,but the active slot was not flipped.");
        return map;
    }

    AbUpdateInstaller(Context context, ParcelFileDescriptor updateFileDescriptor, StartInstallingUpdateCallback callback, DevicePolicyManagerService.Injector injector, DevicePolicyConstants constants) {
        super(context, updateFileDescriptor, callback, injector, constants);
    }

    @Override // com.android.server.devicepolicy.UpdateInstaller
    public void installUpdateInThread() {
        if (!this.mUpdateInstalled) {
            try {
                setState();
                applyPayload(Paths.get(this.mCopiedUpdateFile.getAbsolutePath(), new String[0]).toUri().toString());
            } catch (ZipException e) {
                Log.w("UpdateInstaller", e);
                notifyCallbackOnError(3, Log.getStackTraceString(e));
            } catch (IOException e2) {
                Log.w("UpdateInstaller", e2);
                notifyCallbackOnError(1, Log.getStackTraceString(e2));
            }
        } else {
            throw new IllegalStateException("installUpdateInThread can be called only once.");
        }
    }

    private void setState() throws IOException {
        this.mUpdateInstalled = true;
        this.mPackedUpdateFile = new ZipFile(this.mCopiedUpdateFile);
        this.mProperties = new ArrayList();
        this.mSizeForUpdate = -1;
        this.mOffsetForUpdate = 0;
        this.mEntries = this.mPackedUpdateFile.entries();
    }

    private UpdateEngine buildBoundUpdateEngine() {
        UpdateEngine updateEngine = new UpdateEngine();
        updateEngine.bind(new DelegatingUpdateEngineCallback(this, updateEngine));
        return updateEngine;
    }

    private void applyPayload(String updatePath) throws IOException {
        if (updateStateForPayload()) {
            String[] headerKeyValuePairs = (String[]) this.mProperties.stream().toArray($$Lambda$AbUpdateInstaller$jqambsFSkRKkP2tdaidkN0h_SUo.INSTANCE);
            if (this.mSizeForUpdate == -1) {
                Log.w("UpdateInstaller", "Failed to find payload entry in the given package.");
                notifyCallbackOnError(3, "Failed to find payload entry in the given package.");
                return;
            }
            try {
                buildBoundUpdateEngine().applyPayload(updatePath, this.mOffsetForUpdate, this.mSizeForUpdate, headerKeyValuePairs);
            } catch (Exception e) {
                Log.w("UpdateInstaller", "Failed to install update from file.", e);
                notifyCallbackOnError(1, "Failed to install update from file.");
            }
        }
    }

    static /* synthetic */ String[] lambda$applyPayload$0(int x$0) {
        return new String[x$0];
    }

    private boolean updateStateForPayload() throws IOException {
        long offset = 0;
        while (this.mEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) this.mEntries.nextElement();
            String name = entry.getName();
            offset += buildOffsetForEntry(entry, name);
            if (entry.isDirectory()) {
                offset -= entry.getCompressedSize();
            } else if (PAYLOAD_BIN.equals(name)) {
                if (entry.getMethod() != 0) {
                    Log.w("UpdateInstaller", "Invalid compression method.");
                    notifyCallbackOnError(3, "Invalid compression method.");
                    return false;
                }
                this.mSizeForUpdate = entry.getCompressedSize();
                this.mOffsetForUpdate = offset - entry.getCompressedSize();
            } else if (PAYLOAD_PROPERTIES_TXT.equals(name)) {
                updatePropertiesForEntry(entry);
            }
        }
        return true;
    }

    private long buildOffsetForEntry(ZipEntry entry, String name) {
        return ((long) (name.length() + 30)) + entry.getCompressedSize() + ((long) (entry.getExtra() == null ? 0 : entry.getExtra().length));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        throw r2;
     */
    private void updatePropertiesForEntry(ZipEntry entry) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.mPackedUpdateFile.getInputStream(entry)));
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                this.mProperties.add(line);
            } else {
                bufferedReader.close();
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class DelegatingUpdateEngineCallback extends UpdateEngineCallback {
        private UpdateEngine mUpdateEngine;
        private UpdateInstaller mUpdateInstaller;

        DelegatingUpdateEngineCallback(UpdateInstaller updateInstaller, UpdateEngine updateEngine) {
            this.mUpdateInstaller = updateInstaller;
            this.mUpdateEngine = updateEngine;
        }

        public void onStatusUpdate(int statusCode, float percentage) {
        }

        public void onPayloadApplicationComplete(int errorCode) {
            this.mUpdateEngine.unbind();
            if (errorCode == 0) {
                this.mUpdateInstaller.notifyCallbackOnSuccess();
                return;
            }
            UpdateInstaller updateInstaller = this.mUpdateInstaller;
            int intValue = ((Integer) AbUpdateInstaller.errorCodesMap.getOrDefault(Integer.valueOf(errorCode), 1)).intValue();
            Map map = AbUpdateInstaller.errorStringsMap;
            Integer valueOf = Integer.valueOf(errorCode);
            updateInstaller.notifyCallbackOnError(intValue, (String) map.getOrDefault(valueOf, AbUpdateInstaller.UNKNOWN_ERROR + errorCode));
        }
    }
}
