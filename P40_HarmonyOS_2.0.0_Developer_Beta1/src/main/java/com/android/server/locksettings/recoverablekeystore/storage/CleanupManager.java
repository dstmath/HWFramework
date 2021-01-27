package com.android.server.locksettings.recoverablekeystore.storage;

import android.content.Context;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanupManager {
    private static final String TAG = "CleanupManager";
    private final ApplicationKeyStorage mApplicationKeyStorage;
    private final Context mContext;
    private final RecoverableKeyStoreDb mDatabase;
    private Map<Integer, Long> mSerialNumbers;
    private final RecoverySnapshotStorage mSnapshotStorage;
    private final UserManager mUserManager;

    public static CleanupManager getInstance(Context context, RecoverySnapshotStorage snapshotStorage, RecoverableKeyStoreDb recoverableKeyStoreDb, ApplicationKeyStorage applicationKeyStorage) {
        return new CleanupManager(context, snapshotStorage, recoverableKeyStoreDb, UserManager.get(context), applicationKeyStorage);
    }

    @VisibleForTesting
    CleanupManager(Context context, RecoverySnapshotStorage snapshotStorage, RecoverableKeyStoreDb recoverableKeyStoreDb, UserManager userManager, ApplicationKeyStorage applicationKeyStorage) {
        this.mContext = context;
        this.mSnapshotStorage = snapshotStorage;
        this.mDatabase = recoverableKeyStoreDb;
        this.mUserManager = userManager;
        this.mApplicationKeyStorage = applicationKeyStorage;
    }

    public synchronized void registerRecoveryAgent(int userId, int uid) {
        if (this.mSerialNumbers == null) {
            verifyKnownUsers();
        }
        Long storedSerialNumber = this.mSerialNumbers.get(Integer.valueOf(userId));
        if (storedSerialNumber == null) {
            storedSerialNumber = -1L;
        }
        if (storedSerialNumber.longValue() == -1) {
            long currentSerialNumber = this.mUserManager.getSerialNumberForUser(UserHandle.of(userId));
            if (currentSerialNumber != -1) {
                storeUserSerialNumber(userId, currentSerialNumber);
            }
        }
    }

    public synchronized void verifyKnownUsers() {
        this.mSerialNumbers = this.mDatabase.getUserSerialNumbers();
        List<Integer> deletedUserIds = new ArrayList<Integer>() {
            /* class com.android.server.locksettings.recoverablekeystore.storage.CleanupManager.AnonymousClass1 */
        };
        for (Map.Entry<Integer, Long> entry : this.mSerialNumbers.entrySet()) {
            Integer userId = entry.getKey();
            Long storedSerialNumber = entry.getValue();
            if (storedSerialNumber == null) {
                storedSerialNumber = -1L;
            }
            long currentSerialNumber = this.mUserManager.getSerialNumberForUser(UserHandle.of(userId.intValue()));
            if (currentSerialNumber == -1) {
                deletedUserIds.add(userId);
                removeDataForUser(userId.intValue());
            } else if (storedSerialNumber.longValue() == -1) {
                storeUserSerialNumber(userId.intValue(), currentSerialNumber);
            } else if (storedSerialNumber.longValue() != currentSerialNumber) {
                deletedUserIds.add(userId);
                removeDataForUser(userId.intValue());
                storeUserSerialNumber(userId.intValue(), currentSerialNumber);
            }
        }
        for (Integer deletedUser : deletedUserIds) {
            this.mSerialNumbers.remove(deletedUser);
        }
    }

    private void storeUserSerialNumber(int userId, long userSerialNumber) {
        Log.d(TAG, "Storing serial number for user " + userId + ".");
        this.mSerialNumbers.put(Integer.valueOf(userId), Long.valueOf(userSerialNumber));
        this.mDatabase.setUserSerialNumber(userId, userSerialNumber);
    }

    private void removeDataForUser(int userId) {
        Log.d(TAG, "Removing data for user " + userId + ".");
        for (Integer uid : this.mDatabase.getRecoveryAgents(userId)) {
            this.mSnapshotStorage.remove(uid.intValue());
            removeAllKeysForRecoveryAgent(userId, uid.intValue());
        }
        this.mDatabase.removeUserFromAllTables(userId);
    }

    private void removeAllKeysForRecoveryAgent(int userId, int uid) {
        for (String alias : this.mDatabase.getAllKeys(userId, uid, this.mDatabase.getPlatformKeyGenerationId(userId)).keySet()) {
            try {
                this.mApplicationKeyStorage.deleteEntry(userId, uid, alias);
            } catch (ServiceSpecificException e) {
                Log.e(TAG, "Error while removing recoverable key " + alias + " : " + e);
            }
        }
    }
}
