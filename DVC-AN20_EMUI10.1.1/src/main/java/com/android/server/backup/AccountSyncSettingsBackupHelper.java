package com.android.server.backup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.security.keystore.KeyProperties;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountSyncSettingsBackupHelper implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final String JSON_FORMAT_ENCODING = "UTF-8";
    private static final String JSON_FORMAT_HEADER_KEY = "account_data";
    private static final int JSON_FORMAT_VERSION = 1;
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_ACCOUNT_AUTHORITIES = "authorities";
    private static final String KEY_ACCOUNT_NAME = "name";
    private static final String KEY_ACCOUNT_TYPE = "type";
    private static final String KEY_AUTHORITY_NAME = "name";
    private static final String KEY_AUTHORITY_SYNC_ENABLED = "syncEnabled";
    private static final String KEY_AUTHORITY_SYNC_STATE = "syncState";
    private static final String KEY_MASTER_SYNC_ENABLED = "masterSyncEnabled";
    private static final String KEY_VERSION = "version";
    private static final int MD5_BYTE_SIZE = 16;
    private static final String STASH_FILE = "/backup/unadded_account_syncsettings.json";
    private static final int STATE_VERSION = 1;
    private static final int SYNC_REQUEST_LATCH_TIMEOUT_SECONDS = 1;
    private static final String TAG = "AccountSyncSettingsBackupHelper";
    private AccountManager mAccountManager = AccountManager.get(this.mContext);
    private Context mContext;
    private final int mUserId;

    public AccountSyncSettingsBackupHelper(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
    }

    @Override // android.app.backup.BackupHelper
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput output, ParcelFileDescriptor newState) {
        try {
            byte[] dataBytes = serializeAccountSyncSettingsToJSON(this.mUserId).toString().getBytes(JSON_FORMAT_ENCODING);
            byte[] oldMd5Checksum = readOldMd5Checksum(oldState);
            byte[] newMd5Checksum = generateMd5Checksum(dataBytes);
            if (!Arrays.equals(oldMd5Checksum, newMd5Checksum)) {
                int dataSize = dataBytes.length;
                output.writeEntityHeader(JSON_FORMAT_HEADER_KEY, dataSize);
                output.writeEntityData(dataBytes, dataSize);
                Log.i(TAG, "Backup successful.");
            } else {
                Log.i(TAG, "Old and new MD5 checksums match. Skipping backup.");
            }
            writeNewMd5Checksum(newState, newMd5Checksum);
        } catch (IOException | NoSuchAlgorithmException | JSONException e) {
            Log.e(TAG, "Couldn't backup account sync settings\n" + e);
        }
    }

    private JSONObject serializeAccountSyncSettingsToJSON(int userId) throws JSONException {
        SyncAdapterType[] syncAdapters;
        Account[] accounts;
        int i = userId;
        Account[] accounts2 = this.mAccountManager.getAccountsAsUser(i);
        SyncAdapterType[] syncAdapters2 = ContentResolver.getSyncAdapterTypesAsUser(userId);
        HashMap<String, List<String>> accountTypeToAuthorities = new HashMap<>();
        int i2 = 0;
        for (SyncAdapterType syncAdapter : syncAdapters2) {
            if (syncAdapter.isUserVisible()) {
                if (!accountTypeToAuthorities.containsKey(syncAdapter.accountType)) {
                    accountTypeToAuthorities.put(syncAdapter.accountType, new ArrayList<>());
                }
                accountTypeToAuthorities.get(syncAdapter.accountType).add(syncAdapter.authority);
            }
        }
        JSONObject backupJSON = new JSONObject();
        backupJSON.put("version", 1);
        backupJSON.put(KEY_MASTER_SYNC_ENABLED, ContentResolver.getMasterSyncAutomaticallyAsUser(userId));
        JSONArray accountJSONArray = new JSONArray();
        int length = accounts2.length;
        while (i2 < length) {
            Account account = accounts2[i2];
            List<String> authorities = accountTypeToAuthorities.get(account.type);
            if (authorities == null) {
                accounts = accounts2;
                syncAdapters = syncAdapters2;
            } else if (authorities.isEmpty()) {
                accounts = accounts2;
                syncAdapters = syncAdapters2;
            } else {
                JSONObject accountJSON = new JSONObject();
                accountJSON.put("name", account.name);
                accountJSON.put("type", account.type);
                JSONArray authoritiesJSONArray = new JSONArray();
                for (String authority : authorities) {
                    int syncState = ContentResolver.getIsSyncableAsUser(account, authority, i);
                    boolean syncEnabled = ContentResolver.getSyncAutomaticallyAsUser(account, authority, i);
                    JSONObject authorityJSON = new JSONObject();
                    authorityJSON.put("name", authority);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_STATE, syncState);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_ENABLED, syncEnabled);
                    authoritiesJSONArray.put(authorityJSON);
                    i = userId;
                    accounts2 = accounts2;
                    syncAdapters2 = syncAdapters2;
                }
                accounts = accounts2;
                syncAdapters = syncAdapters2;
                accountJSON.put("authorities", authoritiesJSONArray);
                accountJSONArray.put(accountJSON);
            }
            i2++;
            i = userId;
            accounts2 = accounts;
            syncAdapters2 = syncAdapters;
        }
        backupJSON.put("accounts", accountJSONArray);
        return backupJSON;
    }

    private byte[] readOldMd5Checksum(ParcelFileDescriptor oldState) throws IOException {
        DataInputStream dataInput = new DataInputStream(new FileInputStream(oldState.getFileDescriptor()));
        byte[] oldMd5Checksum = new byte[16];
        try {
            int stateVersion = dataInput.readInt();
            if (stateVersion <= 1) {
                for (int i = 0; i < 16; i++) {
                    oldMd5Checksum[i] = dataInput.readByte();
                }
            } else {
                Log.i(TAG, "Backup state version is: " + stateVersion + " (support only up to version " + 1 + ")");
            }
        } catch (EOFException e) {
        }
        return oldMd5Checksum;
    }

    private void writeNewMd5Checksum(ParcelFileDescriptor newState, byte[] md5Checksum) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newState.getFileDescriptor())));
        dataOutput.writeInt(1);
        dataOutput.write(md5Checksum);
    }

    private byte[] generateMd5Checksum(byte[] data) throws NoSuchAlgorithmException {
        if (data == null) {
            return null;
        }
        return MessageDigest.getInstance(KeyProperties.DIGEST_MD5).digest(data);
    }

    @Override // android.app.backup.BackupHelper
    public void restoreEntity(BackupDataInputStream data) {
        byte[] dataBytes = new byte[data.size()];
        data.read(dataBytes);
        JSONObject dataJSON = new JSONObject(new String(dataBytes, JSON_FORMAT_ENCODING));
        boolean masterSyncEnabled = dataJSON.getBoolean(KEY_MASTER_SYNC_ENABLED);
        JSONArray accountJSONArray = dataJSON.getJSONArray("accounts");
        if (ContentResolver.getMasterSyncAutomaticallyAsUser(this.mUserId)) {
            ContentResolver.setMasterSyncAutomaticallyAsUser(false, this.mUserId);
        }
        try {
            restoreFromJsonArray(accountJSONArray, this.mUserId);
            ContentResolver.setMasterSyncAutomaticallyAsUser(masterSyncEnabled, this.mUserId);
            Log.i(TAG, "Restore successful.");
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Couldn't restore account sync settings\n" + e);
        } catch (Throwable th) {
            ContentResolver.setMasterSyncAutomaticallyAsUser(masterSyncEnabled, this.mUserId);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0061, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0062, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0065, code lost:
        throw r4;
     */
    private void restoreFromJsonArray(JSONArray accountJSONArray, int userId) throws JSONException {
        Set<Account> currentAccounts = getAccounts(userId);
        JSONArray unaddedAccountsJSONArray = new JSONArray();
        for (int i = 0; i < accountJSONArray.length(); i++) {
            JSONObject accountJSON = (JSONObject) accountJSONArray.get(i);
            try {
                if (currentAccounts.contains(new Account(accountJSON.getString("name"), accountJSON.getString("type")))) {
                    restoreExistingAccountSyncSettingsFromJSON(accountJSON, userId);
                } else {
                    unaddedAccountsJSONArray.put(accountJSON);
                }
            } catch (IllegalArgumentException e) {
            }
        }
        if (unaddedAccountsJSONArray.length() > 0) {
            try {
                FileOutputStream fOutput = new FileOutputStream(getStashFile(userId));
                new DataOutputStream(fOutput).writeUTF(unaddedAccountsJSONArray.toString());
                $closeResource(null, fOutput);
            } catch (IOException ioe) {
                Log.e(TAG, "unable to write the sync settings to the stash file", ioe);
            }
        } else {
            File stashFile = getStashFile(userId);
            if (stashFile.exists()) {
                stashFile.delete();
            }
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0031, code lost:
        throw r2;
     */
    private void accountAddedInternal(int userId) {
        try {
            FileInputStream fIn = new FileInputStream(getStashFile(userId));
            String jsonString = new DataInputStream(fIn).readUTF();
            $closeResource(null, fIn);
            try {
                restoreFromJsonArray(new JSONArray(jsonString), userId);
            } catch (JSONException jse) {
                Log.e(TAG, "there was an error with the stashed sync settings", jse);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
        }
    }

    public static void accountAdded(Context context, int userId) {
        new AccountSyncSettingsBackupHelper(context, userId).accountAddedInternal(userId);
    }

    private Set<Account> getAccounts(int userId) {
        Account[] accounts = this.mAccountManager.getAccountsAsUser(userId);
        Set<Account> accountHashSet = new HashSet<>();
        for (Account account : accounts) {
            accountHashSet.add(account);
        }
        return accountHashSet;
    }

    private void restoreExistingAccountSyncSettingsFromJSON(JSONObject accountJSON, int userId) throws JSONException {
        JSONArray authorities = accountJSON.getJSONArray("authorities");
        Account account = new Account(accountJSON.getString("name"), accountJSON.getString("type"));
        for (int i = 0; i < authorities.length(); i++) {
            JSONObject authority = (JSONObject) authorities.get(i);
            String authorityName = authority.getString("name");
            boolean wasSyncEnabled = authority.getBoolean(KEY_AUTHORITY_SYNC_ENABLED);
            int wasSyncable = authority.getInt(KEY_AUTHORITY_SYNC_STATE);
            ContentResolver.setSyncAutomaticallyAsUser(account, authorityName, wasSyncEnabled, userId);
            if (!wasSyncEnabled) {
                ContentResolver.setIsSyncableAsUser(account, authorityName, wasSyncable == 0 ? 0 : 2, userId);
            }
        }
    }

    @Override // android.app.backup.BackupHelper
    public void writeNewStateDescription(ParcelFileDescriptor newState) {
    }

    private static File getStashFile(int userId) {
        File baseDir;
        if (userId == 0) {
            baseDir = Environment.getDataDirectory();
        } else {
            baseDir = Environment.getDataSystemCeDirectory(userId);
        }
        return new File(baseDir, STASH_FILE);
    }
}
