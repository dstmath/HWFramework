package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountManagerInternal;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.PackageUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.accounts.AccountManagerService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AccountManagerBackupHelper {
    private static final String ATTR_ACCOUNT_SHA_256 = "account-sha-256";
    private static final String ATTR_DIGEST = "digest";
    private static final String ATTR_PACKAGE = "package";
    private static final long PENDING_RESTORE_TIMEOUT_MILLIS = 3600000;
    private static final String TAG = "AccountManagerBackupHelper";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSIONS = "permissions";
    /* access modifiers changed from: private */
    public final AccountManagerInternal mAccountManagerInternal;
    /* access modifiers changed from: private */
    public final AccountManagerService mAccountManagerService;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public Runnable mRestoreCancelCommand;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public RestorePackageMonitor mRestorePackageMonitor;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public List<PendingAppPermission> mRestorePendingAppPermissions;

    private final class CancelRestoreCommand implements Runnable {
        private CancelRestoreCommand() {
        }

        public void run() {
            synchronized (AccountManagerBackupHelper.this.mLock) {
                List unused = AccountManagerBackupHelper.this.mRestorePendingAppPermissions = null;
                if (AccountManagerBackupHelper.this.mRestorePackageMonitor != null) {
                    AccountManagerBackupHelper.this.mRestorePackageMonitor.unregister();
                    RestorePackageMonitor unused2 = AccountManagerBackupHelper.this.mRestorePackageMonitor = null;
                }
            }
        }
    }

    private final class PendingAppPermission {
        private final String accountDigest;
        private final String certDigest;
        /* access modifiers changed from: private */
        public final String packageName;
        private final int userId;

        public PendingAppPermission(String accountDigest2, String packageName2, String certDigest2, int userId2) {
            this.accountDigest = accountDigest2;
            this.packageName = packageName2;
            this.certDigest = certDigest2;
            this.userId = userId2;
        }

        public boolean apply(PackageManager packageManager) {
            Account account = null;
            AccountManagerService.UserAccounts accounts = AccountManagerBackupHelper.this.mAccountManagerService.getUserAccounts(this.userId);
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    Iterator<Account[]> it = accounts.accountCache.values().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Account[] accountsPerType = it.next();
                        int length = accountsPerType.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            Account accountPerType = accountsPerType[i];
                            if (this.accountDigest.equals(PackageUtils.computeSha256Digest(accountPerType.name.getBytes()))) {
                                account = accountPerType;
                                break;
                            }
                            i++;
                        }
                        if (account != null) {
                            break;
                        }
                    }
                }
            }
            if (account == null) {
                return false;
            }
            try {
                PackageInfo packageInfo = packageManager.getPackageInfoAsUser(this.packageName, 64, this.userId);
                String[] signaturesSha256Digests = PackageUtils.computeSignaturesSha256Digests(packageInfo.signatures);
                if (!this.certDigest.equals(PackageUtils.computeSignaturesSha256Digest(signaturesSha256Digests)) && (packageInfo.signatures.length <= 1 || !this.certDigest.equals(signaturesSha256Digests[0]))) {
                    return false;
                }
                int uid = packageInfo.applicationInfo.uid;
                if (!AccountManagerBackupHelper.this.mAccountManagerInternal.hasAccountAccess(account, uid)) {
                    AccountManagerBackupHelper.this.mAccountManagerService.grantAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid);
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

    private final class RestorePackageMonitor extends PackageMonitor {
        private RestorePackageMonitor() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0091, code lost:
            return;
         */
        public void onPackageAdded(String packageName, int uid) {
            synchronized (AccountManagerBackupHelper.this.mLock) {
                if (AccountManagerBackupHelper.this.mRestorePendingAppPermissions != null) {
                    if (UserHandle.getUserId(uid) == 0) {
                        for (int i = AccountManagerBackupHelper.this.mRestorePendingAppPermissions.size() - 1; i >= 0; i--) {
                            PendingAppPermission pendingAppPermission = (PendingAppPermission) AccountManagerBackupHelper.this.mRestorePendingAppPermissions.get(i);
                            if (pendingAppPermission.packageName.equals(packageName)) {
                                if (pendingAppPermission.apply(AccountManagerBackupHelper.this.mAccountManagerService.mContext.getPackageManager())) {
                                    AccountManagerBackupHelper.this.mRestorePendingAppPermissions.remove(i);
                                }
                            }
                        }
                        if (AccountManagerBackupHelper.this.mRestorePendingAppPermissions.isEmpty() && AccountManagerBackupHelper.this.mRestoreCancelCommand != null) {
                            AccountManagerBackupHelper.this.mAccountManagerService.mHandler.removeCallbacks(AccountManagerBackupHelper.this.mRestoreCancelCommand);
                            AccountManagerBackupHelper.this.mRestoreCancelCommand.run();
                            Runnable unused = AccountManagerBackupHelper.this.mRestoreCancelCommand = null;
                        }
                    }
                }
            }
        }
    }

    public AccountManagerBackupHelper(AccountManagerService accountManagerService, AccountManagerInternal accountManagerInternal) {
        this.mAccountManagerService = accountManagerService;
        this.mAccountManagerInternal = accountManagerInternal;
    }

    public byte[] backupAccountAccessPermissions(int userId) {
        byte[] byteArray;
        int i;
        List<Pair<String, Integer>> allAccountGrants;
        PackageInfo packageInfo;
        int i2 = userId;
        AccountManagerService.UserAccounts accounts = this.mAccountManagerService.getUserAccounts(i2);
        synchronized (accounts.dbLock) {
            try {
                synchronized (accounts.cacheLock) {
                    try {
                        List<Pair<String, Integer>> allAccountGrants2 = accounts.accountsDb.findAllAccountGrants();
                        if (allAccountGrants2.isEmpty()) {
                            try {
                                try {
                                    return null;
                                } catch (Throwable th) {
                                    th = th;
                                    AccountManagerService.UserAccounts userAccounts = accounts;
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                e = th2;
                                AccountManagerService.UserAccounts userAccounts2 = accounts;
                                throw e;
                            }
                        } else {
                            try {
                                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                                FastXmlSerializer fastXmlSerializer = new FastXmlSerializer();
                                fastXmlSerializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
                                fastXmlSerializer.startDocument(null, true);
                                fastXmlSerializer.startTag(null, TAG_PERMISSIONS);
                                PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
                                for (Pair<String, Integer> grant : allAccountGrants2) {
                                    String accountName = (String) grant.first;
                                    String[] packageNames = packageManager.getPackagesForUid(((Integer) grant.second).intValue());
                                    if (packageNames != null) {
                                        int length = packageNames.length;
                                        int i3 = 0;
                                        while (i3 < length) {
                                            AccountManagerService.UserAccounts accounts2 = accounts;
                                            String packageName = packageNames[i3];
                                            try {
                                                packageInfo = packageManager.getPackageInfoAsUser(packageName, 64, i2);
                                            } catch (PackageManager.NameNotFoundException e) {
                                                allAccountGrants = allAccountGrants2;
                                                i = length;
                                                PackageManager.NameNotFoundException nameNotFoundException = e;
                                                Slog.i(TAG, "Skipping backup of account access grant for non-existing package: " + packageName);
                                            }
                                            try {
                                                String digest = PackageUtils.computeSignaturesSha256Digest(packageInfo.signatures);
                                                if (digest != null) {
                                                    PackageInfo packageInfo2 = packageInfo;
                                                    allAccountGrants = allAccountGrants2;
                                                    try {
                                                        fastXmlSerializer.startTag(null, TAG_PERMISSION);
                                                        i = length;
                                                        fastXmlSerializer.attribute(null, ATTR_ACCOUNT_SHA_256, PackageUtils.computeSha256Digest(accountName.getBytes()));
                                                        fastXmlSerializer.attribute(null, "package", packageName);
                                                        fastXmlSerializer.attribute(null, ATTR_DIGEST, digest);
                                                        fastXmlSerializer.endTag(null, TAG_PERMISSION);
                                                    } catch (IOException e2) {
                                                        e = e2;
                                                        Log.e(TAG, "Error backing up account access grants", e);
                                                        return null;
                                                    }
                                                } else {
                                                    allAccountGrants = allAccountGrants2;
                                                    i = length;
                                                }
                                                i3++;
                                                accounts = accounts2;
                                                allAccountGrants2 = allAccountGrants;
                                                length = i;
                                                i2 = userId;
                                            } catch (IOException e3) {
                                                e = e3;
                                                List<Pair<String, Integer>> list = allAccountGrants2;
                                                Log.e(TAG, "Error backing up account access grants", e);
                                                return null;
                                            }
                                        }
                                        List<Pair<String, Integer>> list2 = allAccountGrants2;
                                        i2 = userId;
                                    }
                                }
                                List<Pair<String, Integer>> list3 = allAccountGrants2;
                                fastXmlSerializer.endTag(null, TAG_PERMISSIONS);
                                fastXmlSerializer.endDocument();
                                fastXmlSerializer.flush();
                                byteArray = dataStream.toByteArray();
                            } catch (IOException e4) {
                                e = e4;
                                AccountManagerService.UserAccounts userAccounts3 = accounts;
                                List<Pair<String, Integer>> list4 = allAccountGrants2;
                                Log.e(TAG, "Error backing up account access grants", e);
                                return null;
                            }
                            try {
                                return byteArray;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        e = th4;
                        AccountManagerService.UserAccounts userAccounts4 = accounts;
                        throw e;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                AccountManagerService.UserAccounts userAccounts5 = accounts;
                throw th;
            }
        }
    }

    public void restoreAccountAccessPermissions(byte[] data, int userId) {
        try {
            try {
                ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dataStream, StandardCharsets.UTF_8.name());
                PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
                int permissionsOuterDepth = parser.getDepth();
                while (true) {
                    int permissionsOuterDepth2 = permissionsOuterDepth;
                    if (XmlUtils.nextElementWithin(parser, permissionsOuterDepth2) != 0) {
                        if (TAG_PERMISSIONS.equals(parser.getName())) {
                            int permissionOuterDepth = parser.getDepth();
                            while (true) {
                                int permissionOuterDepth2 = permissionOuterDepth;
                                if (XmlUtils.nextElementWithin(parser, permissionOuterDepth2) == 0) {
                                    continue;
                                    break;
                                }
                                if (TAG_PERMISSION.equals(parser.getName())) {
                                    String accountDigest = parser.getAttributeValue(null, ATTR_ACCOUNT_SHA_256);
                                    if (TextUtils.isEmpty(accountDigest)) {
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                    String packageName = parser.getAttributeValue(null, "package");
                                    if (TextUtils.isEmpty(packageName)) {
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                    String digest = parser.getAttributeValue(null, ATTR_DIGEST);
                                    if (TextUtils.isEmpty(digest)) {
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                    String str = digest;
                                    String str2 = packageName;
                                    PendingAppPermission pendingAppPermission = new PendingAppPermission(accountDigest, packageName, digest, userId);
                                    PendingAppPermission pendingAppPermission2 = pendingAppPermission;
                                    if (!pendingAppPermission2.apply(packageManager)) {
                                        synchronized (this.mLock) {
                                            if (this.mRestorePackageMonitor == null) {
                                                this.mRestorePackageMonitor = new RestorePackageMonitor();
                                                this.mRestorePackageMonitor.register(this.mAccountManagerService.mContext, this.mAccountManagerService.mHandler.getLooper(), true);
                                            }
                                            if (this.mRestorePendingAppPermissions == null) {
                                                this.mRestorePendingAppPermissions = new ArrayList();
                                            }
                                            this.mRestorePendingAppPermissions.add(pendingAppPermission2);
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                                permissionOuterDepth = permissionOuterDepth2;
                            }
                        }
                        permissionsOuterDepth = permissionsOuterDepth2;
                    } else {
                        this.mRestoreCancelCommand = new CancelRestoreCommand();
                        this.mAccountManagerService.mHandler.postDelayed(this.mRestoreCancelCommand, 3600000);
                        return;
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e = e;
                Log.e(TAG, "Error restoring app permissions", e);
            }
        } catch (IOException | XmlPullParserException e2) {
            e = e2;
            byte[] bArr = data;
            Log.e(TAG, "Error restoring app permissions", e);
        }
    }
}
