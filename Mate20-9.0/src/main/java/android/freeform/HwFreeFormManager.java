package android.freeform;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.freeform.adapter.FloatItem;
import android.freeform.adapter.FloatItemAdapter;
import android.freeform.adapter.FloatPagerAdapter;
import android.freeform.adapter.FloatViewPager;
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
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.Toast;
import com.android.internal.widget.ViewPager;
import java.util.ArrayList;
import java.util.List;

public class HwFreeFormManager {
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
    private static HwFreeFormManager mInstance;
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
    /* access modifiers changed from: private */
    public QuickNavigationView mQuickNav = null;
    private final UserManager mUserManager;
    private final WindowManager mWindowManager;

    private final class FreeFormHandler extends Handler {
        public FreeFormHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwFreeFormManager.this.handleAddListView();
                    return;
                case 1:
                    HwFreeFormManager.this.handleRemoveListView();
                    return;
                case 2:
                    HwFreeFormManager.this.handleShowUnsupportedToast();
                    return;
                default:
                    return;
            }
        }
    }

    private HwFreeFormManager(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        this.mPackageManager = context.getPackageManager();
        this.mFreeFormApps = SplitNotificationUtils.getInstance(context).getListPkgName(3);
        initHandlerThread();
    }

    public static synchronized HwFreeFormManager getInstance(Context context) {
        HwFreeFormManager hwFreeFormManager;
        synchronized (HwFreeFormManager.class) {
            if (mInstance == null) {
                mInstance = new HwFreeFormManager(context);
            }
            hwFreeFormManager = mInstance;
        }
        return hwFreeFormManager;
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new FreeFormHandler(this.mHandlerThread.getLooper());
    }

    public void addFloatListView() {
        if (!this.mIsFloatingListShow) {
            this.mHandler.sendEmptyMessage(0);
        }
    }

    public void removeFloatListView() {
        if (this.mIsFloatingListShow) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    public void handleAddListView() {
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
        wl.width = (int) (((float) (24 + (this.mMaxSizeOneLine * 76))) * ((pictureAdapter.getDeviceDefalutDensity() * this.mContext.getResources().getDisplayMetrics().density) / pictureAdapter.getDisplayDensity()));
        wl.format = -3;
        wl.type = 2003;
        wl.flags = 262184;
        wl.privateFlags |= 16;
        wl.gravity = 81;
        wl.y = dip2px(this.mContext, 12.0f);
        wl.windowAnimations = 16973910;
        this.mWindowManager.addView(this.mFloatingListView, wl);
        this.mFloatingListView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == 4) {
                    HwFreeFormManager.this.removeFloatListView();
                }
                return false;
            }
        });
        this.mIsFloatingListShow = true;
    }

    private boolean prepareRootViewToShow() {
        List<FloatItem> data = prepareData(this.mFreeFormApps);
        int iconSize = data.size();
        if (iconSize == 0) {
            return false;
        }
        LayoutInflater lt = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int i = 4;
        if (iconSize > 12) {
            List<View> viewList = new ArrayList<>();
            List<FloatItem> tempList = new ArrayList<>();
            for (int i2 = 0; i2 < iconSize; i2++) {
                if ((i2 + 1) % 12 == 0 || i2 + 1 == iconSize) {
                    tempList.add(data.get(i2));
                    List<FloatItem> pagerSource = new ArrayList<>(tempList);
                    View pageView = lt.inflate(34013301, null);
                    GridView pageGridView = (GridView) pageView.findViewById(34603222);
                    FloatItemAdapter pictureAdapter = new FloatItemAdapter(pagerSource, this.mContext);
                    pageGridView.setNumColumns(4);
                    pageGridView.setAdapter(pictureAdapter);
                    viewList.add(pageView);
                    tempList.clear();
                } else {
                    tempList.add(data.get(i2));
                }
            }
            this.mFloatingListView = lt.inflate(34013302, null);
            this.mQuickNav = (QuickNavigationView) this.mFloatingListView.findViewById(34603223);
            this.mQuickNav.setPageSize(viewList.size());
            FloatViewPager vp = (FloatViewPager) this.mFloatingListView.findViewById(34603224);
            vp.setAdapter(new FloatPagerAdapter(viewList));
            vp.setOffscreenPageLimit(2);
            vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageSelected(int arg0) {
                    HwFreeFormManager.this.mQuickNav.scrollToPage(arg0);
                }

                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                public void onPageScrollStateChanged(int arg0) {
                }
            });
            this.mMaxSizeOneLine = 4;
        } else {
            this.mFloatingListView = lt.inflate(34013303, null);
            GridView gridView = (GridView) this.mFloatingListView.findViewById(34603225);
            if (iconSize <= 4) {
                i = iconSize;
            }
            this.mMaxSizeOneLine = i;
            gridView.setNumColumns(this.mMaxSizeOneLine);
            gridView.setAdapter(new FloatItemAdapter(data, this.mContext));
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleRemoveListView() {
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
                        List<ResolveInfo> launchApp = this.mPackageManager.queryIntentActivitiesAsUser(queryIntent, 0, ui.id);
                        if (launchApp != null && launchApp.size() >= 1) {
                            ResolveInfo ri = launchApp.get(0);
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
        if (SystemClock.elapsedRealtime() - this.mLastShowToastTime >= SHOW_TOAST_MINIMUM_INTERVAL) {
            if (this.mLastToast != null) {
                this.mLastToast.cancel();
            }
            this.mHandler.sendEmptyMessage(2);
        }
    }

    /* access modifiers changed from: private */
    public void handleShowUnsupportedToast() {
        this.mLastShowToastTime = SystemClock.elapsedRealtime();
        this.mLastToast = Toast.makeText(this.mContext, 33686089, 800);
        this.mLastToast.getWindowParams().privateFlags |= 16;
        this.mLastToast.show();
    }

    private int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
