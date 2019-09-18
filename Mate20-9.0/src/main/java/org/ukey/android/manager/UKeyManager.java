package org.ukey.android.manager;

import android.content.Context;
import huawei.android.ukey.UKeyManagerImpl;

public class UKeyManager {
    private static MyUKeyManager mUKeyManager = new MyUKeyManager();

    private static class MyUKeyManager extends IUKeyManager {
        private UKeyManagerImpl uKeyManagerImpl;

        private MyUKeyManager() {
            this.uKeyManagerImpl = UKeyManagerImpl.getInstance();
        }

        public int getSDKVersion() {
            return this.uKeyManagerImpl.getSDKVersion();
        }

        public int getUKeyVersion() {
            return this.uKeyManagerImpl.getUKeyVersion();
        }

        public int getUKeyStatus(String packageName) {
            return this.uKeyManagerImpl.getUKeyStatus(packageName);
        }

        public void requestUKeyPermission(Context context, int requestCode) {
            this.uKeyManagerImpl.requestUKeyPermission(context, requestCode);
        }

        public int createUKey(String spID, String ssdAid, String sign, String timeStamp) {
            return this.uKeyManagerImpl.createUKey(spID, ssdAid, sign, timeStamp);
        }

        public int deleteUKey(String spID, String ssdAid, String sign, String timeStamp) {
            return this.uKeyManagerImpl.deleteUKey(spID, ssdAid, sign, timeStamp);
        }

        public String getUKeyID() {
            return this.uKeyManagerImpl.getUKeyID();
        }

        public int syncUKey(String spID, String sign, String timeStamp) {
            return this.uKeyManagerImpl.syncUKey(spID, sign, timeStamp);
        }
    }

    public static IUKeyManager getInstance() {
        return mUKeyManager;
    }
}
