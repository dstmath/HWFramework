package com.huawei.systemmanager.appcontrol.iaware.appmng;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import java.util.ArrayList;
import java.util.List;

public class AppCleanParamEx {
    private AppCleanParam mInnerAppCleanParam;

    public static class AppCleanInfo {
        private AppCleanParam.AppCleanInfo mInnerInfo;

        public AppCleanInfo(String pkgName, Integer userid, Integer cleanType) {
            this.mInnerInfo = new AppCleanParam.AppCleanInfo(pkgName, userid, cleanType);
        }

        public void setTaskId(Integer taskId) {
            this.mInnerInfo.setTaskId(taskId);
        }

        public AppCleanParam.AppCleanInfo getInnerInfo() {
            return this.mInnerInfo;
        }
    }

    public static class Builder {
        AppCleanParam.Builder mInnerBuilder;

        public Builder(int source) {
            this.mInnerBuilder = new AppCleanParam.Builder(source);
        }

        public AppCleanParamEx build() {
            if (this.mInnerBuilder == null) {
                return null;
            }
            AppCleanParam appCleanParam = this.mInnerBuilder.build();
            if (appCleanParam == null) {
                return null;
            }
            return new AppCleanParamEx(appCleanParam);
        }

        public Builder action(int action) {
            if (this.mInnerBuilder != null) {
                this.mInnerBuilder.action(action);
            }
            return this;
        }

        public Builder appCleanInfoList(List<AppCleanInfo> appCleanInfoList) {
            List<AppCleanParam.AppCleanInfo> innerList = new ArrayList<>();
            for (AppCleanInfo appCleanInfo : appCleanInfoList) {
                innerList.add(appCleanInfo.getInnerInfo());
            }
            if (this.mInnerBuilder != null) {
                this.mInnerBuilder.appCleanInfoList(innerList);
            }
            return this;
        }
    }

    AppCleanParamEx(AppCleanParam appCleanParam) {
        this.mInnerAppCleanParam = appCleanParam;
    }

    public List<String> getStringList() {
        if (this.mInnerAppCleanParam == null) {
            return null;
        }
        return this.mInnerAppCleanParam.getStringList();
    }

    public AppCleanParam getInnerAppCleanParam() {
        return this.mInnerAppCleanParam;
    }

    public static AppCleanParamEx getCleanParam(List<AppCleanInfo> appCleanInfos) {
        return new Builder(AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()).action(0).appCleanInfoList(appCleanInfos).build();
    }

    public static AppCleanParamEx getAppListParm() {
        return new Builder(AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()).action(1).build();
    }
}
