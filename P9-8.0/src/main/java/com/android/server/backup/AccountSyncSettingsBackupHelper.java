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
    private static final String STASH_FILE = (Environment.getDataDirectory() + "/backup/unadded_account_syncsettings.json");
    private static final int STATE_VERSION = 1;
    private static final int SYNC_REQUEST_LATCH_TIMEOUT_SECONDS = 1;
    private static final String TAG = "AccountSyncSettingsBackupHelper";
    private AccountManager mAccountManager = AccountManager.get(this.mContext);
    private Context mContext;

    public AccountSyncSettingsBackupHelper(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x003e A:{Splitter: B:0:0x0000, ExcHandler: org.json.JSONException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x003e A:{Splitter: B:0:0x0000, ExcHandler: org.json.JSONException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x003e, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x003f, code:
            android.util.Log.e(TAG, "Couldn't backup account sync settings\n" + r3);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput output, ParcelFileDescriptor newState) {
        try {
            byte[] dataBytes = serializeAccountSyncSettingsToJSON().toString().getBytes(JSON_FORMAT_ENCODING);
            byte[] oldMd5Checksum = readOldMd5Checksum(oldState);
            byte[] newMd5Checksum = generateMd5Checksum(dataBytes);
            if (Arrays.equals(oldMd5Checksum, newMd5Checksum)) {
                Log.i(TAG, "Old and new MD5 checksums match. Skipping backup.");
            } else {
                int dataSize = dataBytes.length;
                output.writeEntityHeader(JSON_FORMAT_HEADER_KEY, dataSize);
                output.writeEntityData(dataBytes, dataSize);
                Log.i(TAG, "Backup successful.");
            }
            writeNewMd5Checksum(newState, newMd5Checksum);
        } catch (Exception e) {
        }
    }

    private JSONObject serializeAccountSyncSettingsToJSON() throws JSONException {
        int i;
        Account[] accounts = this.mAccountManager.getAccounts();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(this.mContext.getUserId());
        HashMap<String, List<String>> accountTypeToAuthorities = new HashMap();
        int i2 = 0;
        int length = syncAdapters.length;
        while (true) {
            i = i2;
            if (i >= length) {
                break;
            }
            SyncAdapterType syncAdapter = syncAdapters[i];
            if (syncAdapter.isUserVisible()) {
                if (!accountTypeToAuthorities.containsKey(syncAdapter.accountType)) {
                    accountTypeToAuthorities.put(syncAdapter.accountType, new ArrayList());
                }
                ((List) accountTypeToAuthorities.get(syncAdapter.accountType)).add(syncAdapter.authority);
            }
            i2 = i + 1;
        }
        JSONObject backupJSON = new JSONObject();
        backupJSON.put("version", 1);
        backupJSON.put(KEY_MASTER_SYNC_ENABLED, ContentResolver.getMasterSyncAutomatically());
        JSONArray accountJSONArray = new JSONArray();
        for (Account account : accounts) {
            List<String> authorities = (List) accountTypeToAuthorities.get(account.type);
            if (!(authorities == null || authorities.isEmpty())) {
                JSONObject accountJSON = new JSONObject();
                accountJSON.put("name", account.name);
                accountJSON.put("type", account.type);
                JSONArray authoritiesJSONArray = new JSONArray();
                for (String authority : authorities) {
                    int syncState = ContentResolver.getIsSyncable(account, authority);
                    boolean syncEnabled = ContentResolver.getSyncAutomatically(account, authority);
                    JSONObject authorityJSON = new JSONObject();
                    authorityJSON.put("name", authority);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_STATE, syncState);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_ENABLED, syncEnabled);
                    authoritiesJSONArray.put(authorityJSON);
                }
                accountJSON.put("authorities", authoritiesJSONArray);
                accountJSONArray.put(accountJSON);
            }
        }
        backupJSON.put(KEY_ACCOUNTS, accountJSONArray);
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

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0043 A:{Splitter: B:1:0x0006, ExcHandler: java.io.IOException (r5_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x0043, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x0044, code:
            android.util.Log.e(TAG, "Couldn't restore account sync settings\n" + r5);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void restoreEntity(BackupDataInputStream data) {
        byte[] dataBytes = new byte[data.size()];
        boolean masterSyncEnabled;
        try {
            data.read(dataBytes);
            JSONObject dataJSON = new JSONObject(new String(dataBytes, JSON_FORMAT_ENCODING));
            masterSyncEnabled = dataJSON.getBoolean(KEY_MASTER_SYNC_ENABLED);
            JSONArray accountJSONArray = dataJSON.getJSONArray(KEY_ACCOUNTS);
            if (ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(false);
            }
            restoreFromJsonArray(accountJSONArray);
            ContentResolver.setMasterSyncAutomatically(masterSyncEnabled);
            Log.i(TAG, "Restore successful.");
        } catch (Exception e) {
        } catch (Throwable th) {
            ContentResolver.setMasterSyncAutomatically(masterSyncEnabled);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x008b A:{SYNTHETIC, Splitter: B:34:0x008b} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a3 A:{Catch:{ IOException -> 0x0091 }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0090 A:{SYNTHETIC, Splitter: B:37:0x0090} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void restoreFromJsonArray(JSONArray accountJSONArray) throws JSONException {
        IOException ioe;
        Throwable th;
        HashSet<Account> currentAccounts = getAccounts();
        JSONArray unaddedAccountsJSONArray = new JSONArray();
        for (int i = 0; i < accountJSONArray.length(); i++) {
            JSONObject accountJSON = (JSONObject) accountJSONArray.get(i);
            try {
                Account account = new Account(accountJSON.getString("name"), accountJSON.getString("type"));
                if (currentAccounts.contains(account)) {
                    restoreExistingAccountSyncSettingsFromJSON(accountJSON);
                    Account account2 = account;
                } else {
                    unaddedAccountsJSONArray.put(accountJSON);
                }
            } catch (IllegalArgumentException e) {
            }
        }
        if (unaddedAccountsJSONArray.length() > 0) {
            Throwable th2 = null;
            FileOutputStream fOutput = null;
            try {
                FileOutputStream fOutput2 = new FileOutputStream(STASH_FILE);
                try {
                    new DataOutputStream(fOutput2).writeUTF(unaddedAccountsJSONArray.toString());
                    if (fOutput2 != null) {
                        try {
                            fOutput2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            ioe = e2;
                            fOutput = fOutput2;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fOutput = fOutput2;
                    if (fOutput != null) {
                        try {
                            fOutput.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e3) {
                            ioe = e3;
                            Log.e(TAG, "unable to write the sync settings to the stash file", ioe);
                            return;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fOutput != null) {
                }
                if (th2 == null) {
                }
            }
        } else {
            File stashFile = new File(STASH_FILE);
            if (stashFile.exists()) {
                stashFile.delete();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x002c A:{SYNTHETIC, Splitter: B:21:0x002c} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0041 A:{Catch:{ FileNotFoundException -> 0x0032, IOException -> 0x003f }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0031 A:{SYNTHETIC, Splitter: B:24:0x0031} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void accountAddedInternal() {
        Throwable th;
        Throwable th2 = null;
        FileInputStream fIn = null;
        try {
            FileInputStream fIn2 = new FileInputStream(new File(STASH_FILE));
            try {
                String jsonString = new DataInputStream(fIn2).readUTF();
                if (fIn2 != null) {
                    try {
                        fIn2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (FileNotFoundException e) {
                        fIn = fIn2;
                    } catch (IOException e2) {
                        return;
                    }
                }
                try {
                    restoreFromJsonArray(new JSONArray(jsonString));
                } catch (JSONException jse) {
                    Log.e(TAG, "there was an error with the stashed sync settings", jse);
                }
            } catch (Throwable th4) {
                th = th4;
                fIn = fIn2;
                if (fIn != null) {
                    try {
                        fIn.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (FileNotFoundException e3) {
                        return;
                    } catch (IOException e4) {
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (fIn != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public static void accountAdded(Context context) {
        new AccountSyncSettingsBackupHelper(context).accountAddedInternal();
    }

    private HashSet<Account> getAccounts() {
        Account[] accounts = this.mAccountManager.getAccounts();
        HashSet<Account> accountHashSet = new HashSet();
        for (Account account : accounts) {
            accountHashSet.add(account);
        }
        return accountHashSet;
    }

    private void restoreExistingAccountSyncSettingsFromJSON(JSONObject accountJSON) throws JSONException {
        JSONArray authorities = accountJSON.getJSONArray("authorities");
        Account account = new Account(accountJSON.getString("name"), accountJSON.getString("type"));
        for (int i = 0; i < authorities.length(); i++) {
            JSONObject authority = (JSONObject) authorities.get(i);
            String authorityName = authority.getString("name");
            boolean wasSyncEnabled = authority.getBoolean(KEY_AUTHORITY_SYNC_ENABLED);
            int wasSyncable = authority.getInt(KEY_AUTHORITY_SYNC_STATE);
            ContentResolver.setSyncAutomaticallyAsUser(account, authorityName, wasSyncEnabled, 0);
            if (!wasSyncEnabled) {
                int i2;
                if (wasSyncable == 0) {
                    i2 = 0;
                } else {
                    i2 = 2;
                }
                ContentResolver.setIsSyncable(account, authorityName, i2);
            }
        }
    }

    public void writeNewStateDescription(ParcelFileDescriptor newState) {
    }
}
