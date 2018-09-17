package com.android.server.backup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.os.ParcelFileDescriptor;
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
    private static final String STASH_FILE = null;
    private static final int STATE_VERSION = 1;
    private static final int SYNC_REQUEST_LATCH_TIMEOUT_SECONDS = 1;
    private static final String TAG = "AccountSyncSettingsBackupHelper";
    private AccountManager mAccountManager;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.AccountSyncSettingsBackupHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.AccountSyncSettingsBackupHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.AccountSyncSettingsBackupHelper.<clinit>():void");
    }

    public AccountSyncSettingsBackupHelper(Context context) {
        this.mContext = context;
        this.mAccountManager = AccountManager.get(this.mContext);
    }

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
            Log.e(TAG, "Couldn't backup account sync settings\n" + e);
        }
    }

    private JSONObject serializeAccountSyncSettingsToJSON() throws JSONException {
        int i;
        Account[] accounts = this.mAccountManager.getAccounts();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(this.mContext.getUserId());
        HashMap<String, List<String>> accountTypeToAuthorities = new HashMap();
        int length = syncAdapters.length;
        for (i = 0; i < length; i += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
            SyncAdapterType syncAdapter = syncAdapters[i];
            if (syncAdapter.isUserVisible()) {
                if (!accountTypeToAuthorities.containsKey(syncAdapter.accountType)) {
                    accountTypeToAuthorities.put(syncAdapter.accountType, new ArrayList());
                }
                ((List) accountTypeToAuthorities.get(syncAdapter.accountType)).add(syncAdapter.authority);
            }
        }
        JSONObject backupJSON = new JSONObject();
        backupJSON.put(KEY_VERSION, SYNC_REQUEST_LATCH_TIMEOUT_SECONDS);
        backupJSON.put(KEY_MASTER_SYNC_ENABLED, ContentResolver.getMasterSyncAutomatically());
        JSONArray accountJSONArray = new JSONArray();
        i = accounts.length;
        for (int i2 = 0; i2 < i; i2 += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
            Account account = accounts[i2];
            List<String> authorities = (List) accountTypeToAuthorities.get(account.type);
            if (!(authorities == null || authorities.isEmpty())) {
                JSONObject accountJSON = new JSONObject();
                accountJSON.put(KEY_AUTHORITY_NAME, account.name);
                accountJSON.put(KEY_ACCOUNT_TYPE, account.type);
                JSONArray authoritiesJSONArray = new JSONArray();
                for (String authority : authorities) {
                    int syncState = ContentResolver.getIsSyncable(account, authority);
                    boolean syncEnabled = ContentResolver.getSyncAutomatically(account, authority);
                    JSONObject authorityJSON = new JSONObject();
                    authorityJSON.put(KEY_AUTHORITY_NAME, authority);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_STATE, syncState);
                    authorityJSON.put(KEY_AUTHORITY_SYNC_ENABLED, syncEnabled);
                    authoritiesJSONArray.put(authorityJSON);
                }
                accountJSON.put(KEY_ACCOUNT_AUTHORITIES, authoritiesJSONArray);
                accountJSONArray.put(accountJSON);
            }
        }
        backupJSON.put(KEY_ACCOUNTS, accountJSONArray);
        return backupJSON;
    }

    private byte[] readOldMd5Checksum(ParcelFileDescriptor oldState) throws IOException {
        DataInputStream dataInput = new DataInputStream(new FileInputStream(oldState.getFileDescriptor()));
        byte[] oldMd5Checksum = new byte[MD5_BYTE_SIZE];
        try {
            int stateVersion = dataInput.readInt();
            if (stateVersion <= SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
                for (int i = 0; i < MD5_BYTE_SIZE; i += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
                    oldMd5Checksum[i] = dataInput.readByte();
                }
            } else {
                Log.i(TAG, "Backup state version is: " + stateVersion + " (support only up to version " + SYNC_REQUEST_LATCH_TIMEOUT_SECONDS + ")");
            }
        } catch (EOFException e) {
        }
        return oldMd5Checksum;
    }

    private void writeNewMd5Checksum(ParcelFileDescriptor newState, byte[] md5Checksum) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newState.getFileDescriptor())));
        dataOutput.writeInt(SYNC_REQUEST_LATCH_TIMEOUT_SECONDS);
        dataOutput.write(md5Checksum);
    }

    private byte[] generateMd5Checksum(byte[] data) throws NoSuchAlgorithmException {
        if (data == null) {
            return null;
        }
        return MessageDigest.getInstance("MD5").digest(data);
    }

    public void restoreEntity(BackupDataInputStream data) {
        byte[] dataBytes = new byte[data.size()];
        boolean masterSyncEnabled;
        try {
            data.read(dataBytes);
            JSONObject dataJSON = new JSONObject(new String(dataBytes, JSON_FORMAT_ENCODING));
            masterSyncEnabled = dataJSON.getBoolean(KEY_MASTER_SYNC_ENABLED);
            JSONArray accountJSONArray = dataJSON.getJSONArray(KEY_ACCOUNTS);
            if (ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(DEBUG);
            }
            restoreFromJsonArray(accountJSONArray);
            ContentResolver.setMasterSyncAutomatically(masterSyncEnabled);
            Log.i(TAG, "Restore successful.");
        } catch (Exception e) {
            Log.e(TAG, "Couldn't restore account sync settings\n" + e);
        } catch (Throwable th) {
            ContentResolver.setMasterSyncAutomatically(masterSyncEnabled);
        }
    }

    private void restoreFromJsonArray(JSONArray accountJSONArray) throws JSONException {
        IOException ioe;
        Throwable th;
        HashSet<Account> currentAccounts = getAccounts();
        JSONArray unaddedAccountsJSONArray = new JSONArray();
        for (int i = 0; i < accountJSONArray.length(); i += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
            JSONObject accountJSON = (JSONObject) accountJSONArray.get(i);
            try {
                Account account = new Account(accountJSON.getString(KEY_AUTHORITY_NAME), accountJSON.getString(KEY_ACCOUNT_TYPE));
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
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream fOutput = new FileOutputStream(STASH_FILE);
                try {
                    new DataOutputStream(fOutput).writeUTF(unaddedAccountsJSONArray.toString());
                    if (fOutput != null) {
                        try {
                            fOutput.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            ioe = e2;
                            fileOutputStream = fOutput;
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fileOutputStream = fOutput;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
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
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (th2 == null) {
                    throw th;
                }
                throw th2;
            }
        }
        File stashFile = new File(STASH_FILE);
        if (stashFile.exists()) {
            stashFile.delete();
        }
    }

    private void accountAddedInternal() {
        Throwable th;
        Throwable th2 = null;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fIn = new FileInputStream(new File(STASH_FILE));
            try {
                String jsonString = new DataInputStream(fIn).readUTF();
                if (fIn != null) {
                    try {
                        fIn.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (FileNotFoundException e) {
                        fileInputStream = fIn;
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
                fileInputStream = fIn;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
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
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    public static void accountAdded(Context context) {
        new AccountSyncSettingsBackupHelper(context).accountAddedInternal();
    }

    private HashSet<Account> getAccounts() {
        Account[] accounts = this.mAccountManager.getAccounts();
        HashSet<Account> accountHashSet = new HashSet();
        int length = accounts.length;
        for (int i = 0; i < length; i += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
            accountHashSet.add(accounts[i]);
        }
        return accountHashSet;
    }

    private void restoreExistingAccountSyncSettingsFromJSON(JSONObject accountJSON) throws JSONException {
        JSONArray authorities = accountJSON.getJSONArray(KEY_ACCOUNT_AUTHORITIES);
        Account account = new Account(accountJSON.getString(KEY_AUTHORITY_NAME), accountJSON.getString(KEY_ACCOUNT_TYPE));
        for (int i = 0; i < authorities.length(); i += SYNC_REQUEST_LATCH_TIMEOUT_SECONDS) {
            JSONObject authority = (JSONObject) authorities.get(i);
            String authorityName = authority.getString(KEY_AUTHORITY_NAME);
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
