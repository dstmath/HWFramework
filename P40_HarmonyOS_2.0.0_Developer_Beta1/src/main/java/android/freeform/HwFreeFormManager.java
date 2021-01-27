package android.freeform;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.freeform.adapter.FloatItem;
import android.freeform.adapter.FloatItemAdapter;
import android.freeform.adapter.QuickNavigationView;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SplitNotificationUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.android.hwext.internal.R;
import java.util.ArrayList;
import java.util.List;

public class HwFreeFormManager {
    private static final int ALPHA = 230;
    private static final int FREEFORM_WINDOW_OFF_Y = 12;
    private static final int FW_BG_GRID_LEFT_SPACE = 4;
    private static final int FW_BG_LFET_SHADOW = 8;
    private static final int FW_GRID_ITEM_WIDTH = 76;
    private static final int FW_MAX_GRID_ITEM_SIZE = 4;
    private static final int MAX_ICON_SIZE_PAGE = 12;
    private static final int MSG_HANDLE_ADD_LISTVIEW = 0;
    private static final int MSG_HANDLE_REMOVE_LISTVIEW = 1;
    private static final int MSG_HANDLE_SHOW_UNSUPPORTED_TOAST = 2;
    private static final long SHOW_TOAST_MINIMUM_INTERVAL = 1000;
    private static final String TAG = "HwFreeFormManager";
    private static HwFreeFormManager sInstance;
    private Context mContext;
    private View mFloatingListView = null;
    private List<String> mFreeFormApps = new ArrayList();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsFloatingListShow = false;
    private long mLastShowToastTime;
    private Toast mLastToast;
    private int mMaxSizeOneLine = 0;
    private final PackageManager mPackageManager;
    private QuickNavigationView mQuickNav = null;
    private final UserManager mUserManager;
    private final WindowManager mWindowManager;

    private HwFreeFormManager(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mPackageManager = context.getPackageManager();
        this.mFreeFormApps = SplitNotificationUtils.getInstance(context).getListPkgName(3);
        if (HwFreeFormUtils.isFreeFormEnable()) {
            initHandlerThread();
        }
    }

    public static synchronized HwFreeFormManager getInstance(Context context) {
        HwFreeFormManager hwFreeFormManager;
        synchronized (HwFreeFormManager.class) {
            if (sInstance == null) {
                sInstance = new HwFreeFormManager(context);
            }
            hwFreeFormManager = sInstance;
        }
        return hwFreeFormManager;
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new FreeFormHandler(this.mHandlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    public final class FreeFormHandler extends Handler {
        FreeFormHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwFreeFormManager.this.handleAddListView();
            } else if (i == 1) {
                HwFreeFormManager.this.handleRemoveListView();
            } else if (i == 2) {
                HwFreeFormManager.this.handleShowUnsupportedToast();
            }
        }
    }

    public void addFloatListView() {
        Handler handler;
        if (!this.mIsFloatingListShow && (handler = this.mHandler) != null) {
            handler.sendEmptyMessage(0);
        }
    }

    public void removeFloatListView() {
        Handler handler;
        if (this.mIsFloatingListShow && (handler = this.mHandler) != null) {
            handler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddListView() {
        if (this.mFreeFormApps.size() == 0) {
            this.mFreeFormApps = SplitNotificationUtils.getInstance(this.mContext).getListPkgName(3);
        }
        if (!prepareRootViewToShow()) {
            Log.d("dql", "handleAddListView return");
            return;
        }
        FloatItemAdapter pictureAdapter = new FloatItemAdapter(null, this.mContext);
        WindowManager.LayoutParams wl = new WindowManager.LayoutParams();
        wl.height = -2;
        wl.width = (int) (((float) ((this.mMaxSizeOneLine * 76) + 24)) * ((pictureAdapter.getDeviceDefalutDensity() * this.mContext.getResources().getDisplayMetrics().density) / pictureAdapter.getDisplayDensity()));
        wl.format = -3;
        wl.type = 2003;
        wl.flags = 262184;
        wl.privateFlags |= 16;
        wl.gravity = 81;
        wl.y = dip2px(this.mContext, 12.0f);
        wl.windowAnimations = 16973910;
        this.mWindowManager.addView(this.mFloatingListView, wl);
        this.mFloatingListView.setOnTouchListener(new View.OnTouchListener() {
            /* class android.freeform.HwFreeFormManager.AnonymousClass1 */

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() != 4) {
                    return false;
                }
                HwFreeFormManager.this.removeFloatListView();
                return false;
            }
        });
        this.mIsFloatingListShow = true;
    }

    private boolean prepareRootViewToShow() {
        List<FloatItem> floatItems = prepareData(this.mFreeFormApps);
        int iconSize = floatItems.size();
        if (iconSize == 0) {
            return false;
        }
        LayoutInflater lt = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (iconSize > 12) {
            return false;
        }
        this.mFloatingListView = lt.inflate(R.layout.hw_floatlist_window_layout, (ViewGroup) null);
        GridView gridView = (GridView) this.mFloatingListView.findViewById(R.id.hw_floatlist_window);
        int i = 4;
        if (iconSize <= 4) {
            i = iconSize;
        }
        this.mMaxSizeOneLine = i;
        gridView.setNumColumns(this.mMaxSizeOneLine);
        gridView.setAdapter((ListAdapter) new FloatItemAdapter(floatItems, this.mContext));
        this.mFloatingListView.getBackground().mutate().setAlpha(230);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveListView() {
        if (this.mIsFloatingListShow) {
            this.mWindowManager.removeView(this.mFloatingListView);
            this.mIsFloatingListShow = false;
        }
    }

    private List<FloatItem> prepareData(List<String> pkgNames) {
        List<FloatItem> res = new ArrayList<>();
        int currentUserId = ActivityManager.getCurrentUser();
        List<UserInfo> userInfoList = this.mUserManager.getProfiles(currentUserId);
        for (String pkg : pkgNames) {
            if (isAppInLockList(pkg)) {
                HwFreeFormUtils.log("input", "pkg:" + pkg + " is in app lock list");
            } else {
                for (UserInfo ui : userInfoList) {
                    if (ui.isClonedProfile() || ui.id == currentUserId) {
                        Intent queryIntent = new Intent();
                        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        queryIntent.setAction(Intent.ACTION_MAIN);
                        queryIntent.setPackage(pkg);
                        List<ResolveInfo> launchApps = this.mPackageManager.queryIntentActivitiesAsUser(queryIntent, 0, ui.id);
                        if (launchApps != null && launchApps.size() >= 1) {
                            ResolveInfo ri = launchApps.get(0);
                            queryIntent.setComponent(ri.activityInfo.getComponentName());
                            String lab = (String) ri.activityInfo.applicationInfo.loadLabel(this.mPackageManager);
                            Drawable icon = ri.activityInfo.applicationInfo.loadIcon(this.mPackageManager);
                            if (ui.isClonedProfile()) {
                                icon = this.mPackageManager.getUserBadgedIcon(icon, ui.getUserHandle());
                            }
                            res.add(new FloatItem(lab, icon, queryIntent, ui.id));
                        }
                    }
                }
            }
        }
        return res;
    }

    private boolean isAppInLockList(String imsPgkName) {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "app_lock_func_status", 0, -2) == 0) {
            return false;
        }
        String appLockList = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "app_lock_list", -2);
        if (!TextUtils.isEmpty(appLockList) && !TextUtils.isEmpty(imsPgkName) && appLockList.contains(imsPgkName)) {
            return true;
        }
        return false;
    }

    public void showUnsupportedToast() {
        if (SystemClock.elapsedRealtime() - this.mLastShowToastTime >= 1000) {
            Toast toast = this.mLastToast;
            if (toast != null) {
                toast.cancel();
            }
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.sendEmptyMessage(2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShowUnsupportedToast() {
        this.mLastShowToastTime = SystemClock.elapsedRealtime();
        this.mLastToast = Toast.makeText(this.mContext, (int) R.string.desktop_unsupport_freeform_and_split, 800);
        this.mLastToast.getWindowParams().privateFlags |= 16;
        this.mLastToast.show();
    }

    private int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
