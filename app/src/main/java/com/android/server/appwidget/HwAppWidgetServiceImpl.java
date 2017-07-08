package com.android.server.appwidget;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import com.android.server.appwidget.AppWidgetServiceImpl.Provider;
import com.android.server.appwidget.AppWidgetServiceImpl.Widget;
import java.util.HashSet;
import java.util.Iterator;

class HwAppWidgetServiceImpl extends AppWidgetServiceImpl {
    private static final boolean DEBUG = false;
    private static final int SMCS_APP_WIDGET_SERVICE_GET_VISIBLE = 1;
    static final String TAG = "HwAppWidgetServiceImpl";
    private Context mContext;

    public HwAppWidgetServiceImpl(Context context) {
        super(context);
        this.mContext = null;
        this.mContext = context;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (1599297111 == code) {
            try {
                switch (data.readInt()) {
                    case SMCS_APP_WIDGET_SERVICE_GET_VISIBLE /*1*/:
                        handleGetVisibleWidgets(data, reply);
                        return true;
                }
            } catch (Exception e) {
                return DEBUG;
            }
        }
        return super.onTransact(code, data, reply, flags);
    }

    private void handleGetVisibleWidgets(Parcel data, Parcel reply) {
        HashSet<String> visibleWidgets = new HashSet();
        if (this.mContext.checkCallingPermission("huawei.permission.IBINDER_APP_WIDGET_SERVICE") == 0) {
            getVisibleWidgets_hwHsm(visibleWidgets);
            reply.writeInt(visibleWidgets.size());
            Iterator<String> it = visibleWidgets.iterator();
            while (it.hasNext()) {
                reply.writeString((String) it.next());
            }
        }
    }

    boolean getVisibleWidgets_hwHsm(HashSet<String> sPkgs) {
        if (sPkgs == null) {
            return DEBUG;
        }
        synchronized (this.mWidgets) {
            int N = this.mWidgets.size();
            for (int i = 0; i < N; i += SMCS_APP_WIDGET_SERVICE_GET_VISIBLE) {
                Widget aid = (Widget) this.mWidgets.get(i);
                if (aid != null) {
                    Provider provider = aid.provider;
                    if (provider != null) {
                        AppWidgetProviderInfo info = provider.info;
                        if (info != null) {
                            ComponentName cn = info.provider;
                            if (cn != null) {
                                sPkgs.add(cn.getPackageName());
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    protected void addWidgetReport(int userId, String pkgName) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && pkgName != null) {
            Bundle args = new Bundle();
            args.putString("widget", pkgName);
            args.putInt("userid", userId);
            args.putInt("relationType", 5);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
            long origId = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected void removeWidgetReport(int userId, String pkgName) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && pkgName != null) {
            Bundle args = new Bundle();
            args.putString("widget", pkgName);
            args.putInt("userid", userId);
            args.putInt("relationType", 6);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
            long origId = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected void clearWidgetReport() {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
            Bundle args = new Bundle();
            args.putInt("relationType", 7);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
            long origId = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }
}
