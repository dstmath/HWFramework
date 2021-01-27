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
import org.xmlpull.v1.XmlSerializer;

public final class AccountManagerBackupHelper {
    private static final String ATTR_ACCOUNT_SHA_256 = "account-sha-256";
    private static final String ATTR_DIGEST = "digest";
    private static final String ATTR_PACKAGE = "package";
    private static final long PENDING_RESTORE_TIMEOUT_MILLIS = 3600000;
    private static final String TAG = "AccountManagerBackupHelper";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSIONS = "permissions";
    private final AccountManagerInternal mAccountManagerInternal;
    private final AccountManagerService mAccountManagerService;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private Runnable mRestoreCancelCommand;
    @GuardedBy({"mLock"})
    private RestorePackageMonitor mRestorePackageMonitor;
    @GuardedBy({"mLock"})
    private List<PendingAppPermission> mRestorePendingAppPermissions;

    public AccountManagerBackupHelper(AccountManagerService accountManagerService, AccountManagerInternal accountManagerInternal) {
        this.mAccountManagerService = accountManagerService;
        this.mAccountManagerInternal = accountManagerInternal;
    }

    /* access modifiers changed from: private */
    public final class PendingAppPermission {
        private final String accountDigest;
        private final String certDigest;
        private final String packageName;
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

    public byte[] backupAccountAccessPermissions(int userId) {
        Throwable th;
        IOException e;
        IOException e2;
        byte[] byteArray;
        int i;
        List<Pair<String, Integer>> allAccountGrants;
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
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                e = th3;
                                throw e;
                            }
                        } else {
                            try {
                                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                                XmlSerializer serializer = new FastXmlSerializer();
                                serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
                                serializer.startDocument(null, true);
                                serializer.startTag(null, TAG_PERMISSIONS);
                                PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
                                for (Pair<String, Integer> grant : allAccountGrants2) {
                                    String accountName = (String) grant.first;
                                    String[] packageNames = packageManager.getPackagesForUid(((Integer) grant.second).intValue());
                                    if (packageNames != null) {
                                        int length = packageNames.length;
                                        int i3 = 0;
                                        while (i3 < length) {
                                            String packageName = packageNames[i3];
                                            try {
                                            } catch (PackageManager.NameNotFoundException e3) {
                                                allAccountGrants = allAccountGrants2;
                                                i = length;
                                                Slog.i(TAG, "Skipping backup of account access grant for non-existing package: " + packageName);
                                            }
                                            try {
                                                String digest = PackageUtils.computeSignaturesSha256Digest(packageManager.getPackageInfoAsUser(packageName, 64, i2).signatures);
                                                if (digest != null) {
                                                    allAccountGrants = allAccountGrants2;
                                                    try {
                                                        serializer.startTag(null, TAG_PERMISSION);
                                                        i = length;
                                                        serializer.attribute(null, ATTR_ACCOUNT_SHA_256, PackageUtils.computeSha256Digest(accountName.getBytes()));
                                                        serializer.attribute(null, "package", packageName);
                                                        serializer.attribute(null, ATTR_DIGEST, digest);
                                                        serializer.endTag(null, TAG_PERMISSION);
                                                    } catch (IOException e4) {
                                                        e2 = e4;
                                                        Log.e(TAG, "Error backing up account access grants", e2);
                                                        return null;
                                                    }
                                                } else {
                                                    allAccountGrants = allAccountGrants2;
                                                    i = length;
                                                }
                                                i3++;
                                                i2 = userId;
                                                accounts = accounts;
                                                allAccountGrants2 = allAccountGrants;
                                                length = i;
                                            } catch (IOException e5) {
                                                e2 = e5;
                                                Log.e(TAG, "Error backing up account access grants", e2);
                                                return null;
                                            }
                                        }
                                        i2 = userId;
                                    }
                                }
                                serializer.endTag(null, TAG_PERMISSIONS);
                                serializer.endDocument();
                                serializer.flush();
                                byteArray = dataStream.toByteArray();
                            } catch (IOException e6) {
                                e2 = e6;
                                Log.e(TAG, "Error backing up account access grants", e2);
                                return null;
                            }
                            try {
                                try {
                                    return byteArray;
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                e = th5;
                                throw e;
                            }
                        }
                    } catch (Throwable th6) {
                        e = th6;
                        throw e;
                    }
                }
            } catch (Throwable th7) {
                th = th7;
                throw th;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:51:0x0040 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:53:0x0040 */
    /* JADX DEBUG: Multi-variable search result rejected for r0v3, resolved type: org.xmlpull.v1.XmlPullParser */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r13v0 */
    /* JADX WARN: Type inference failed for: r13v1, types: [com.android.server.accounts.AccountManagerBackupHelper$1, java.lang.String] */
    /* JADX WARN: Type inference failed for: r13v2 */
    /* JADX WARN: Type inference failed for: r13v3 */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void restoreAccountAccessPermissions(byte[] data, int userId) {
        try {
            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(dataStream, StandardCharsets.UTF_8.name());
            PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
            int permissionsOuterDepth = parser.getDepth();
            while (true) {
                ?? r13 = 0;
                if (!XmlUtils.nextElementWithin(parser, permissionsOuterDepth)) {
                    this.mRestoreCancelCommand = new CancelRestoreCommand();
                    this.mAccountManagerService.mHandler.postDelayed(this.mRestoreCancelCommand, 3600000);
                    return;
                } else if (TAG_PERMISSIONS.equals(parser.getName())) {
                    int permissionOuterDepth = parser.getDepth();
                    while (XmlUtils.nextElementWithin(parser, permissionOuterDepth)) {
                        if (TAG_PERMISSION.equals(parser.getName())) {
                            String accountDigest = parser.getAttributeValue(r13, ATTR_ACCOUNT_SHA_256);
                            if (TextUtils.isEmpty(accountDigest)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            String packageName = parser.getAttributeValue(r13, "package");
                            if (TextUtils.isEmpty(packageName)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            String digest = parser.getAttributeValue(r13, ATTR_DIGEST);
                            if (TextUtils.isEmpty(digest)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            PendingAppPermission pendingAppPermission = new PendingAppPermission(accountDigest, packageName, digest, userId);
                            if (!pendingAppPermission.apply(packageManager)) {
                                synchronized (this.mLock) {
                                    if (this.mRestorePackageMonitor == null) {
                                        this.mRestorePackageMonitor = new RestorePackageMonitor();
                                        this.mRestorePackageMonitor.register(this.mAccountManagerService.mContext, this.mAccountManagerService.mHandler.getLooper(), true);
                                    }
                                    if (this.mRestorePendingAppPermissions == null) {
                                        this.mRestorePendingAppPermissions = new ArrayList();
                                    }
                                    this.mRestorePendingAppPermissions.add(pendingAppPermission);
                                }
                            }
                            r13 = 0;
                        }
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Exception e2 = e;
            Log.e(TAG, "Error restoring app permissions", e2);
        }
    }

    /* access modifiers changed from: private */
    public final class RestorePackageMonitor extends PackageMonitor {
        private RestorePackageMonitor() {
        }

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
                            AccountManagerBackupHelper.this.mRestoreCancelCommand = null;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CancelRestoreCommand implements Runnable {
        private CancelRestoreCommand() {
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AccountManagerBackupHelper.this.mLock) {
                AccountManagerBackupHelper.this.mRestorePendingAppPermissions = null;
                if (AccountManagerBackupHelper.this.mRestorePackageMonitor != null) {
                    AccountManagerBackupHelper.this.mRestorePackageMonitor.unregister();
                    AccountManagerBackupHelper.this.mRestorePackageMonitor = null;
                }
            }
        }
    }
}
