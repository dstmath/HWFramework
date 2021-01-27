package com.android.server.pm;

import com.android.server.pm.BlackListInfo;
import java.util.ArrayList;

public class BlackListInfoEx {
    private BlackListInfo mBlackListInfo = new BlackListInfo();

    public static class BlackListAppEx {
        private BlackListInfo.BlackListApp mBlackListApp;

        public BlackListInfo.BlackListApp getBlackListApp() {
            return this.mBlackListApp;
        }

        public void setBlackListApp(BlackListInfo.BlackListApp blackListApp) {
            this.mBlackListApp = blackListApp;
        }

        public String getPackageName() {
            return this.mBlackListApp.getPackageName();
        }
    }

    public BlackListInfo getBlackListInfo() {
        return this.mBlackListInfo;
    }

    public void setBlackListInfo(BlackListInfo blackListInfo) {
        this.mBlackListInfo = blackListInfo;
    }

    public int getVersionCode() {
        return this.mBlackListInfo.getVersionCode();
    }

    public void setVersionCode(int versionCode) {
        this.mBlackListInfo.setVersionCode(versionCode);
    }

    public ArrayList<BlackListAppEx> getBlacklistApps() {
        ArrayList<BlackListInfo.BlackListApp> blackListApps = this.mBlackListInfo.getBlacklistApps();
        ArrayList<BlackListAppEx> blackListAppExs = new ArrayList<>();
        for (int i = 0; i < blackListApps.size(); i++) {
            BlackListAppEx appEx = new BlackListAppEx();
            appEx.setBlackListApp(blackListApps.get(i));
            blackListAppExs.add(appEx);
        }
        return blackListAppExs;
    }
}
