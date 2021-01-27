package org.ukey.android.manager;

import android.content.Context;
import huawei.android.ukey.UKeyManagerImpl;

public class UKeyManager {
    private static MyUKeyManager sUKeyManager = new MyUKeyManager();

    public static IUKeyManager getInstance() {
        return sUKeyManager;
    }

    private static class MyUKeyManager extends IUKeyManager {
        private UKeyManagerImpl mUKeyManagerImpl;

        private MyUKeyManager() {
            this.mUKeyManagerImpl = UKeyManagerImpl.getInstance();
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int getSDKVersion() {
            return this.mUKeyManagerImpl.getSDKVersion();
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int getUKeyVersion() {
            return this.mUKeyManagerImpl.getUKeyVersion();
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int getUKeyStatus(String packageName) {
            return this.mUKeyManagerImpl.getUKeyStatus(packageName);
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public void requestUKeyPermission(Context context, int requestCode) {
            this.mUKeyManagerImpl.requestUKeyPermission(context, requestCode);
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int createUKey(String spID, String ssdAid, String sign, String timeStamp) {
            return this.mUKeyManagerImpl.createUKey(spID, ssdAid, sign, timeStamp);
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int deleteUKey(String spID, String ssdAid, String sign, String timeStamp) {
            return this.mUKeyManagerImpl.deleteUKey(spID, ssdAid, sign, timeStamp);
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public String getUKeyID() {
            return this.mUKeyManagerImpl.getUKeyID();
        }

        @Override // org.ukey.android.manager.IUKeyManager
        public int syncUKey(String spID, String sign, String timeStamp) {
            return this.mUKeyManagerImpl.syncUKey(spID, sign, timeStamp);
        }
    }
}
