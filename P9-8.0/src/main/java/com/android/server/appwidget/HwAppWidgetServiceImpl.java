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
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.util.HashSet;
import java.util.Iterator;

class HwAppWidgetServiceImpl extends AppWidgetServiceImpl {
    private static final boolean DEBUG = false;
    private static final int SMCS_APP_WIDGET_SERVICE_GET_BY_USERID = 2;
    private static final int SMCS_APP_WIDGET_SERVICE_GET_VISIBLE = 1;
    static final String TAG = "HwAppWidgetServiceImpl";
    private static final int WIDGET_INVISIBLE = 0;
    private static final int WIDGET_VISIBLE = 1;
    private Context mContext = null;

    public HwAppWidgetServiceImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (data == null || reply == null) {
            return false;
        }
        if (1599297111 == code) {
            try {
                switch (data.readInt()) {
                    case 1:
                        handleGetVisibleWidgets(data, reply);
                        return true;
                    case 2:
                        handleGetVisibleWidgetsByUserId(data.readInt(), reply);
                        return true;
                }
            } catch (Exception e) {
                return false;
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
            return false;
        }
        synchronized (this.mWidgets) {
            int N = this.mWidgets.size();
            for (int i = 0; i < N; i++) {
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

    private void handleGetVisibleWidgetsByUserId(int userId, Parcel reply) {
        if (Binder.getCallingUid() == 1000) {
            Widget widget;
            HashSet<Widget> widgets = new HashSet();
            synchronized (this.mWidgets) {
                for (Widget widget2 : this.mWidgets) {
                    if (!isWidgetAbnormal(widget2) && widget2.provider.getUserId() == userId) {
                        widgets.add(widget2);
                    }
                }
            }
            reply.writeInt(widgets.size());
            Iterator<Widget> it = widgets.iterator();
            while (it.hasNext()) {
                widget2 = (Widget) it.next();
                reply.writeInt(widget2.appWidgetId);
                reply.writeString(widget2.provider.info.provider.getPackageName());
                reply.writeInt(AwareAppAssociate.getInstance().isWidgetVisible(widget2.options) ? 1 : 0);
            }
        }
    }

    private boolean isWidgetAbnormal(Widget widget) {
        if (widget == null || widget.provider == null || widget.provider.info == null || widget.provider.info.provider == null) {
            return true;
        }
        return false;
    }

    protected void addWidgetReport(int userId, Widget widget) {
        addOrRemoveWidgetReport(userId, widget, 5);
    }

    protected void removeWidgetReport(int userId, Widget widget) {
        addOrRemoveWidgetReport(userId, widget, 6);
    }

    private void addOrRemoveWidgetReport(int userId, Widget widget, int action) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && !isWidgetAbnormal(widget)) {
            Bundle args = new Bundle();
            args.putString("widget", widget.provider.info.provider.getPackageName());
            args.putInt("widgetId", widget.appWidgetId);
            args.putBundle("widgetOpt", widget.options);
            args.putInt("userid", userId);
            args.putInt("relationType", action);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
            long origId = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected void updateWidgetOptionsReport(int userId, Widget widget) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && !isWidgetAbnormal(widget)) {
            Bundle args = new Bundle();
            args.putInt("widgetId", widget.appWidgetId);
            args.putString("widget", widget.provider.info.provider.getPackageName());
            args.putBundle("widgetOpt", widget.options);
            args.putInt("userid", userId);
            args.putInt("relationType", 24);
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
