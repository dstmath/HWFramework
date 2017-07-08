package com.huawei.android.launcher;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.launcher.compat.LauncherActivityInfoCompat;
import com.android.launcher.compat.LauncherAppsCompat;
import com.huawei.android.launcher.DropTarget.DragObject;
import com.huawei.android.launcher.Workspace.State;
import com.huawei.android.launcher.report.LauncherReporter;
import java.io.File;

public class ShareDropTarget extends ButtonDropTarget {
    private static final int ACCEPTED_DRAGTO_ICON_SIZE = 1;
    private String TAG;
    private Context mContext;
    private TransitionDrawable mDrawable;
    private ImageView mImageView;
    private boolean mIsExsitApp;
    private LinearLayout mShareBg;
    private TextView mTextView;

    public ShareDropTarget(Context context) {
        this(context, null);
    }

    public ShareDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "ShareDropTarget ";
        this.mIsExsitApp = true;
        this.mContext = context;
        init();
    }

    private void init() {
        this.mHoverColor = getResources().getColor(2131361793);
        int MAGIN_TO_LEFT = (int) this.mContext.getResources().getDimension(2131165585);
        int MAGIN_TO_RIGHT = (int) this.mContext.getResources().getDimension(2131165586);
        int MAGIN_TO_TOP = (int) this.mContext.getResources().getDimension(2131165565);
        this.mShareBg = new LinearLayout(this.mContext);
        LayoutParams lp = new LayoutParams(-2, -2);
        this.mShareBg.setLayoutParams(lp);
        this.mShareBg.setGravity(17);
        this.mShareBg.setOrientation(0);
        addView(this.mShareBg, lp);
        this.mImageView = new ImageView(this.mContext);
        this.mImageView.setBackgroundResource(2130837759);
        this.mTextView = new TextView(this.mContext);
        this.mTextView.setText(2131558605);
        this.mTextView.setTextColor(this.mContext.getResources().getColor(2131361859));
        this.mTextView.setTextSize(0, (float) this.mContext.getResources().getDimensionPixelSize(2131165572));
        LayoutParams lp1 = new LayoutParams(-2, -2);
        lp1.setMargins(MAGIN_TO_RIGHT, MAGIN_TO_TOP, 0, MAGIN_TO_TOP);
        this.mImageView.setLayoutParams(lp1);
        this.mShareBg.addView(this.mImageView, lp1);
        LayoutParams lp2 = new LayoutParams(-2, -2);
        lp2.setMargins(MAGIN_TO_LEFT, MAGIN_TO_TOP, MAGIN_TO_RIGHT, MAGIN_TO_TOP);
        this.mTextView.setGravity(17);
        this.mTextView.setLayoutParams(lp2);
        this.mShareBg.addView(this.mTextView, lp2);
        this.mDrawable = (TransitionDrawable) this.mImageView.getBackground();
        this.mDrawable.setCrossFadeEnabled(true);
    }

    public boolean acceptDrop(DragObject d) {
        ComponentName componentName = getDragObjectComponentName(d);
        if (componentName != null) {
            String packageNameString = componentName.getPackageName();
            if (!packageNameString.equals("")) {
                try {
                    String pathString = ((LauncherActivityInfoCompat) LauncherAppsCompat.getInstance(LauncherApplication.getActiveInstance()).getActivityList(packageNameString, ((ItemInfo) d.dragInfo).user).get(0)).getApplicationInfo().sourceDir;
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("application/vnd.android.package-archive");
                    intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(pathString)));
                    if (LogHelper.HWLOG) {
                        LogHelper.i(this.TAG + "surr get Activity path is " + pathString);
                        LogHelper.i(this.TAG + "surr get Activity intent is " + intent);
                    }
                    Intent intent2 = null;
                    try {
                        intent2 = Intent.createChooser(intent, this.mLauncher.getResources().getString(2131558605));
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogHelper.w(this.TAG + "acceptDrop() Intent.createChooser failed");
                    }
                    if (intent2 != null) {
                        intent = intent2;
                    }
                    this.mLauncher.startActivity(intent);
                    if (Settings.isDesktopBigDataStatistics()) {
                        LauncherReporter.reportData(this.mContext, 1025, "", new Object[0]);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    LogHelper.e(this.TAG + "startActivity err is " + e2);
                }
            }
        }
        if (this.mLauncher.getWorkspace().getState() == State.EDIT_MODE && (d.dragSource instanceof WidgetCustomizeContainer)) {
            this.mLauncher.showCardHatTip(true);
        }
        this.mLauncher.exitSpringLoaded();
        return false;
    }

    private ComponentName getDragObjectComponentName(DragObject d) {
        if (d.dragInfo instanceof ApplicationInfo) {
            return ((ApplicationInfo) d.dragInfo).componentName;
        }
        if ((d.dragInfo instanceof ShortcutInfo) && !(d.dragInfo instanceof DownloadCutInfo)) {
            return ((ShortcutInfo) d.dragInfo).intent.getComponent();
        }
        if (d.dragInfo instanceof PendingAddItemInfo) {
            return ((PendingAddItemInfo) d.dragInfo).componentName;
        }
        if (d.dragInfo instanceof PendingAddWidgetInfo) {
            return ((PendingAddWidgetInfo) d.dragInfo).componentName;
        }
        if (!(d.dragInfo instanceof LauncherAppWidgetInfo)) {
            return null;
        }
        ComponentName componentName = ((LauncherAppWidgetInfo) d.dragInfo).providerName;
        if (componentName == null) {
            return AppWidgetManager.getInstance(this.mContext).getAppWidgetInfo(((LauncherAppWidgetInfo) d.dragInfo).appWidgetId).provider;
        }
        return componentName;
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
        if (LogHelper.HWLOG) {
            LogHelper.i(this.TAG + "onDragStart ");
        }
        boolean isToShare = false;
        if ((info instanceof ShortcutInfo) && !(info instanceof DownloadCutInfo)) {
            try {
                if (LauncherAppsCompat.getInstance(LauncherApplication.getActiveInstance()).resolveActivity(((ShortcutInfo) info).intent, ((ShortcutInfo) info).user) == null) {
                    this.mIsExsitApp = false;
                } else {
                    this.mIsExsitApp = true;
                }
            } catch (Exception e) {
                this.mIsExsitApp = false;
            }
            if (!this.mIsExsitApp || ((ShortcutInfo) info).itemType == ACCEPTED_DRAGTO_ICON_SIZE) {
                isToShare = false;
            } else {
                isToShare = ((ShortcutInfo) info).isSharedApp();
            }
        } else if (info instanceof FolderInfo) {
            isToShare = false;
        } else if (Utilities.isAllAppsWidget(source, info) || (info instanceof LauncherAppWidgetInfo)) {
            isToShare = this.mLauncher.isToShareWidget(source, info);
        }
        if (this.mLauncher.getMutilSelectedIcons().size() > ACCEPTED_DRAGTO_ICON_SIZE || (source instanceof FloatingIconsPanel)) {
            isToShare = false;
        }
        if (isToShare) {
            this.mActive = true;
            setClearNormalHoloBg();
            setVisibility(0);
            return;
        }
        this.mActive = false;
        setVisibility(8);
    }

    private void setClearActiveHoloBg() {
        this.mShareBg.setBackgroundResource(2130837602);
    }

    private void setClearNormalHoloBg() {
        this.mHatDropTargetBar.setNormalBackground();
        this.mShareBg.setBackground(null);
    }

    public void onDragEnd() {
        super.onDragEnd();
        if (LogHelper.HWLOG) {
            LogHelper.i(this.TAG + "onDragEnd ");
        }
        this.mActive = false;
    }

    public void onDragEnter(DragObject d) {
        super.onDragEnter(d);
        if (LogHelper.HWLOG) {
            LogHelper.i(this.TAG + "onDragEnter ");
        }
        Utilities.sendAccessibilityEvent(this.mLauncher.getString(2131558605));
        this.mDrawable.startTransition(this.mTransitionDuration);
        setClearActiveHoloBg();
    }

    public void onDragExit(DragObject d) {
        super.onDragExit(d);
        if (LogHelper.HWLOG) {
            LogHelper.i(this.TAG + "onDragExit ");
        }
        this.mDrawable.resetTransition();
        setClearNormalHoloBg();
    }

    public int getDropTargetType() {
        return 0;
    }
}
