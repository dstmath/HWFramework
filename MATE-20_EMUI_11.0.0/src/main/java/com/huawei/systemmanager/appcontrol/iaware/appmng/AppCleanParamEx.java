package com.huawei.systemmanager.appcontrol.iaware.appmng;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import java.util.ArrayList;
import java.util.List;

public class AppCleanParamEx {
    private AppCleanParam mInnerAppCleanParam;

    AppCleanParamEx(AppCleanParam appCleanParam) {
        this.mInnerAppCleanParam = appCleanParam;
    }

    public List<String> getStringList() {
        AppCleanParam appCleanParam = this.mInnerAppCleanParam;
        if (appCleanParam == null) {
            return null;
        }
        return appCleanParam.getStringList();
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

    public static class Builder {
        AppCleanParam.Builder mInnerBuilder;

        public Builder(int source) {
            this.mInnerBuilder = new AppCleanParam.Builder(source);
        }

        public AppCleanParamEx build() {
            AppCleanParam appCleanParam;
            AppCleanParam.Builder builder = this.mInnerBuilder;
            if (builder == null || (appCleanParam = builder.build()) == null) {
                return null;
            }
            return new AppCleanParamEx(appCleanParam);
        }

        public Builder action(int action) {
            AppCleanParam.Builder builder = this.mInnerBuilder;
            if (builder != null) {
                builder.action(action);
            }
            return this;
        }

        public Builder appCleanInfoList(List<AppCleanInfo> appCleanInfoList) {
            List<AppCleanParam.AppCleanInfo> innerList = new ArrayList<>();
            for (AppCleanInfo appCleanInfo : appCleanInfoList) {
                innerList.add(appCleanInfo.getInnerInfo());
            }
            AppCleanParam.Builder builder = this.mInnerBuilder;
            if (builder != null) {
                builder.appCleanInfoList(innerList);
            }
            return this;
        }
    }

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
}
