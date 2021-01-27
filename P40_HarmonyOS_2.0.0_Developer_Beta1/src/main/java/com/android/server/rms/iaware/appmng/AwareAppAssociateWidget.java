package com.android.server.rms.iaware.appmng;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.huawei.android.os.ServiceManagerEx;
import java.util.Set;

public class AwareAppAssociateWidget {
    private static final int SMCS_APP_WIDGET_SERVICE_GET_BY_USERID = 2;
    private static final String TAG = "RMS.AwareAppAssociate";
    private static final int WIDGET_INVISIBLE = 0;
    private static final int WIDGET_VISIBLE = 1;
    private final SparseArray<SparseArray<Widget>> mWidgets = new SparseArray<>();

    /* access modifiers changed from: protected */
    public void updateWidgets(int userId) {
        if (AwareAppAssociate.isDebugEnabled()) {
            AwareLog.i(TAG, "updateWidgets, userId: " + userId);
        }
        IBinder service = ServiceManagerEx.getService("appwidget");
        if (service != null) {
            SparseArray<Widget> widgets = new SparseArray<>();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInt(2);
            data.writeInt(userId);
            try {
                service.transact(1599297111, data, reply, 0);
                int size = reply.readInt();
                if (AwareAppAssociate.isDebugEnabled()) {
                    AwareLog.i(TAG, "updateWidgets, transact finish, widgets size: " + size);
                }
                for (int i = 0; i < size; i++) {
                    int id = reply.readInt();
                    String pkg = reply.readString();
                    boolean visibleB = true;
                    if (reply.readInt() != 1) {
                        visibleB = false;
                    }
                    if (pkg != null && pkg.length() > 0) {
                        widgets.put(id, new Widget(id, pkg, visibleB));
                    }
                    if (AwareAppAssociate.isDebugEnabled()) {
                        AwareLog.i(TAG, "updateWidgets, widget: " + id + ", " + pkg + ", " + visibleB);
                    }
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getWidgetsPkg, transact error!");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            synchronized (this.mWidgets) {
                this.mWidgets.put(userId, widgets);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> getWidgetsPkg(int userId) {
        Set<String> widgets = new ArraySet<>();
        synchronized (this.mWidgets) {
            getWidgetsPkgLocked(widgets, userId);
        }
        return widgets;
    }

    private void getWidgetsPkgLocked(Set<String> widgets, int userId) {
        SparseArray<Widget> widgetMap = this.mWidgets.get(userId);
        if (widgetMap != null) {
            for (int i = widgetMap.size() - 1; i >= 0; i--) {
                Widget widget = widgetMap.valueAt(i);
                if (widget.isVisible) {
                    widgets.add(widget.pkgName);
                }
                if (AwareAppAssociate.isDebugEnabled()) {
                    AwareLog.i(TAG, "getWidgetsPkg:" + widget.appWidgetId + ", " + widget.pkgName + ", " + widget.isVisible);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndInitWidgetObj(int userId) {
        synchronized (this.mWidgets) {
            if (this.mWidgets.get(userId) == null) {
                this.mWidgets.put(userId, new SparseArray<>());
            }
        }
    }

    private void addWidget(int userId, int widgetId, String pkgName, Bundle options) {
        if (pkgName != null) {
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.i(TAG, "addWidget, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName + ", vis: " + isWidgetVisible(options));
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                if (this.mWidgets.get(userId).indexOfKey(widgetId) < 0) {
                    this.mWidgets.get(userId).put(widgetId, new Widget(widgetId, pkgName, isWidgetVisible(options)));
                }
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
            }
        }
    }

    private void removeWidget(int userId, int widgetId, String pkgName) {
        if (pkgName != null) {
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.i(TAG, "removeWidget, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName);
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                this.mWidgets.get(userId).delete(widgetId);
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
            }
        }
    }

    private void updateWidgetOptions(int userId, int widgetId, String pkgName, Bundle options) {
        if (widgetId >= 0 && pkgName != null) {
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.i(TAG, "updateWidgetOptions, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName + ", options: " + options);
            }
            boolean visible = isWidgetVisible(options);
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                SparseArray<Widget> widgetMap = this.mWidgets.get(userId);
                if (widgetMap.get(widgetId) != null) {
                    widgetMap.get(widgetId).isVisible = visible;
                } else {
                    widgetMap.put(widgetId, new Widget(widgetId, pkgName, visible));
                }
                if (userId == 0) {
                    AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
                }
            }
        }
    }

    private void updateWidgetFlush(int userId, String pkgName) {
        AwareIntelligentRecg.getInstance().widgetTrigUpdate(pkgName);
    }

    /* access modifiers changed from: protected */
    public boolean isWidgetVisible(Bundle options) {
        if (options == null) {
            return false;
        }
        int maxHeight = options.getInt("appWidgetMaxHeight");
        int maxWidth = options.getInt("appWidgetMaxWidth");
        int minHeight = options.getInt("appWidgetMinHeight");
        int minWidth = options.getInt("appWidgetMinWidth");
        if (maxHeight == 0 && maxWidth == 0 && minHeight == 0 && minWidth == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void clearWidget() {
        if (AwareAppAssociate.isDebugEnabled()) {
            AwareLog.d(TAG, "clearWidget");
        }
        synchronized (this.mWidgets) {
            for (int i = this.mWidgets.size() - 1; i >= 0; i--) {
                SparseArray<Widget> userWdigets = this.mWidgets.valueAt(i);
                if (userWdigets != null) {
                    userWdigets.clear();
                }
            }
        }
        AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(0), null);
    }

    /* access modifiers changed from: protected */
    public void report(int eventId, Bundle bundleArgs) {
        if (bundleArgs != null) {
            if (eventId == 5) {
                addWidget(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"), bundleArgs.getBundle("widgetOpt"));
            } else if (eventId == 6) {
                removeWidget(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"));
            } else if (eventId == 7) {
                clearWidget();
            } else if (eventId == 24) {
                updateWidgetOptions(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"), bundleArgs.getBundle("widgetOpt"));
            } else if (eventId == 32) {
                updateWidgetFlush(bundleArgs.getInt("userid"), bundleArgs.getString("widget"));
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class Widget {
        int appWidgetId;
        boolean isVisible;
        String pkgName;

        private Widget(int appWidgetId2, String pkgName2, boolean isVisible2) {
            this.pkgName = "";
            this.isVisible = false;
            this.appWidgetId = appWidgetId2;
            this.pkgName = pkgName2;
            this.isVisible = isVisible2;
        }
    }
}
