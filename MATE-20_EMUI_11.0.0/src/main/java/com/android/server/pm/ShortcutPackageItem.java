package com.android.server.pm;

import android.content.pm.PackageInfo;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public abstract class ShortcutPackageItem {
    private static final String KEY_NAME = "name";
    private static final String TAG = "ShortcutService";
    private final ShortcutPackageInfo mPackageInfo;
    private final String mPackageName;
    private final int mPackageUserId;
    protected ShortcutUser mShortcutUser;

    /* access modifiers changed from: protected */
    public abstract boolean canRestoreAnyVersion();

    public abstract int getOwnerUserId();

    /* access modifiers changed from: protected */
    public abstract void onRestored(int i);

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

    public void refreshPackageSignatureAndSave() {
        if (!this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            this.mPackageInfo.refreshSignature(s, this);
            s.scheduleSaveUser(getOwnerUserId());
        }
    }

    public void attemptToRestoreIfNeededAndSave() {
        int restoreBlockReason;
        if (this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            if (s.isPackageInstalled(this.mPackageName, this.mPackageUserId)) {
                if (!this.mPackageInfo.hasSignatures()) {
                    s.wtf("Attempted to restore package " + this.mPackageName + "/u" + this.mPackageUserId + " but signatures not found in the restore data.");
                    restoreBlockReason = 102;
                } else {
                    PackageInfo pi = s.getPackageInfoWithSignatures(this.mPackageName, this.mPackageUserId);
                    pi.getLongVersionCode();
                    restoreBlockReason = this.mPackageInfo.canRestoreTo(s, pi, canRestoreAnyVersion());
                }
                onRestored(restoreBlockReason);
                this.mPackageInfo.setShadow(false);
                s.scheduleSaveUser(this.mPackageUserId);
            }
        }
    }

    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", this.mPackageName);
        return result;
    }

    public void verifyStates() {
    }
}
