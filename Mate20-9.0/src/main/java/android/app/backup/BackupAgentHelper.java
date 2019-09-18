package android.app.backup;

import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class BackupAgentHelper extends BackupAgent {
    static final String TAG = "BackupAgentHelper";
    BackupHelperDispatcher mDispatcher = new BackupHelperDispatcher();

    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        this.mDispatcher.performBackup(oldState, data, newState);
    }

    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        this.mDispatcher.performRestore(data, appVersionCode, newState);
    }

    public BackupHelperDispatcher getDispatcher() {
        return this.mDispatcher;
    }

    public void addHelper(String keyPrefix, BackupHelper helper) {
        this.mDispatcher.addHelper(keyPrefix, helper);
    }
}
