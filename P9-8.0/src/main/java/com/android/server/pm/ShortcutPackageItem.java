package com.android.server.pm;

import com.android.internal.util.Preconditions;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

abstract class ShortcutPackageItem {
    private static final String KEY_NAME = "name";
    private static final String TAG = "ShortcutService";
    private final ShortcutPackageInfo mPackageInfo;
    private final String mPackageName;
    private final int mPackageUserId;
    protected ShortcutUser mShortcutUser;

    public abstract int getOwnerUserId();

    protected abstract void onRestoreBlocked();

    protected abstract void onRestored();

    public abstract void saveToXml(XmlSerializer xmlSerializer, boolean z) throws IOException, XmlPullParserException;

    protected ShortcutPackageItem(ShortcutUser shortcutUser, int packageUserId, String packageName, ShortcutPackageInfo packageInfo) {
        this.mShortcutUser = shortcutUser;
        this.mPackageUserId = packageUserId;
        this.mPackageName = (String) Preconditions.checkStringNotEmpty(packageName);
        this.mPackageInfo = (ShortcutPackageInfo) Preconditions.checkNotNull(packageInfo);
    }

    public void replaceUser(ShortcutUser user) {
        this.mShortcutUser = user;
    }

    public ShortcutUser getUser() {
        return this.mShortcutUser;
    }

    public int getPackageUserId() {
        return this.mPackageUserId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public ShortcutPackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }

    /* renamed from: refreshPackageSignatureAndSave */
    public void lambda$-com_android_server_pm_ShortcutService_124520() {
        if (!this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            this.mPackageInfo.refreshSignature(s, this);
            s.scheduleSaveUser(getOwnerUserId());
        }
    }

    /* renamed from: attemptToRestoreIfNeededAndSave */
    public void lambda$-com_android_server_pm_ShortcutUser_11189() {
        if (this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            if (s.isPackageInstalled(this.mPackageName, this.mPackageUserId)) {
                boolean blockRestore = false;
                if (!this.mPackageInfo.hasSignatures()) {
                    s.wtf("Attempted to restore package " + this.mPackageName + ", user=" + this.mPackageUserId + " but signatures not found in the restore data.");
                    blockRestore = true;
                }
                if (!blockRestore) {
                    if (!this.mPackageInfo.canRestoreTo(s, s.getPackageInfoWithSignatures(this.mPackageName, this.mPackageUserId))) {
                        blockRestore = true;
                    }
                }
                if (blockRestore) {
                    onRestoreBlocked();
                } else {
                    onRestored();
                }
                this.mPackageInfo.setShadow(false);
                s.scheduleSaveUser(this.mPackageUserId);
            }
        }
    }

    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        JSONObject result = new JSONObject();
        result.put(KEY_NAME, this.mPackageName);
        return result;
    }

    /* renamed from: verifyStates */
    public void -com_android_server_pm_ShortcutService-mthref-4() {
    }
}
