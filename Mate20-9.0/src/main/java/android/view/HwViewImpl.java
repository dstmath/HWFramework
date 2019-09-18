package android.view;

import android.content.Context;
import android.emcom.EmcomManager;
import android.emcom.XEngineAppInfo;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.FastgrabConfigReader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.huawei.pgmng.log.LogPower;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

public class HwViewImpl implements IHwView {
    private static final String ATTR_ANIMATION_VIEW = "animationRootView";
    private static final String ATTR_BOOST_VIEW = "boostRootView";
    private static final String ATTR_SWITCH = "switch";
    private static final String LAUNCHER_APP = "com.huawei.android.launcher";
    private static final int MAX_CHILD_COUNT = 12;
    private static final int MAX_KEY_LENGTH = 31;
    private static final int MAX_TREE_DEPTH = 4;
    private static final String SYSTEM_UI_APP = "com.android.systemui";
    private static final String TAG = "HwViewImpl";
    private static HwViewImpl mInstance = null;
    private XEngineAppInfo mAppInfo;
    private int mGrade;
    private boolean mHasRequestProp;
    private Stack<View> mStack = new Stack<>();
    private int mainCardPsStatus;

    public static synchronized HwViewImpl getDefault() {
        HwViewImpl hwViewImpl;
        synchronized (HwViewImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewImpl();
            }
            hwViewImpl = mInstance;
        }
        return hwViewImpl;
    }

    private HwViewImpl() {
    }

    public boolean cancelAnimation(View view, Context context) {
        if (!checkView(view, context, ATTR_ANIMATION_VIEW)) {
            return false;
        }
        AwareLog.i(TAG, "LuckyMoney Animation Canceled !");
        return true;
    }

    public void onClick(View view, Context context) {
        if (checkClick(view, context)) {
            accelerate(context);
        }
        unFreezeApp(view, context);
    }

    private void unFreezeApp(View view, Context context) {
        if (view != null && view.getRootView() != null && view.getRootView().getContext() != null && context != null) {
            String rootViewPkg = view.getRootView().getContext().getPackageName();
            if ("com.android.systemui".equals(rootViewPkg)) {
                String viewPkg = context.getPackageName();
                if (!"com.android.systemui".equals(viewPkg)) {
                    LogPower.push(148, "onClick", viewPkg);
                }
            }
            if (LAUNCHER_APP.equals(rootViewPkg)) {
                String viewPkg2 = context.getPackageName();
                if (!LAUNCHER_APP.equals(viewPkg2)) {
                    LogPower.push(148, "onClick", viewPkg2);
                }
            }
        }
    }

    private void accelerate(Context context) {
        if (context != null) {
            EmcomManager manager = EmcomManager.getInstance();
            if (manager != null) {
                manager.accelerateWithMainCardPsStatus(context, this.mGrade, this.mainCardPsStatus);
            }
        }
    }

    private boolean checkClick(View view, Context context) {
        if (!this.mHasRequestProp) {
            String pkgName = context.getPackageName();
            String key = "sys." + pkgName;
            if (key.length() > 31) {
                key = key.substring(0, 31);
            }
            if (Boolean.parseBoolean(SystemProperties.get(key))) {
                EmcomManager manager = EmcomManager.getInstance();
                if (manager != null) {
                    this.mAppInfo = manager.getAppInfo(context);
                }
            }
            this.mHasRequestProp = true;
        }
        if (this.mAppInfo == null) {
            return false;
        }
        this.mainCardPsStatus = this.mAppInfo.getMainCardPsStatus();
        return checkView(view);
    }

    private boolean checkView(View view) {
        if (view != null) {
            for (XEngineAppInfo.EventInfo event : this.mAppInfo.getEventList()) {
                if (checkRootView(view, event)) {
                    this.mGrade = event.getGrade();
                    this.mainCardPsStatus = event.getMainCardPsStatus();
                    return true;
                } else if (checkContainer(view, event) && checkKeywords(view, event)) {
                    this.mGrade = event.getGrade();
                    this.mainCardPsStatus = event.getMainCardPsStatus();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkRootView(View view, XEngineAppInfo.EventInfo event) {
        String rootView = event.getRootView();
        if (TextUtils.isEmpty(rootView)) {
            return false;
        }
        View root = view.getRootView();
        if (root != null) {
            return root.toString().contains(rootView);
        }
        return false;
    }

    private boolean checkContainer(View view, XEngineAppInfo.EventInfo event) {
        CharSequence className = event.getContainer();
        if (TextUtils.isEmpty(className)) {
            return false;
        }
        return className.equals(view.getAccessibilityClassName());
    }

    private boolean checkKeywords(View view, XEngineAppInfo.EventInfo event) {
        String keyword = event.getKeyword();
        if (TextUtils.isEmpty(keyword)) {
            return false;
        }
        if (view instanceof TextView) {
            return checkTextForTextView((TextView) view, keyword);
        }
        if (view instanceof ViewGroup) {
            return checkTextForViewGroup((ViewGroup) view, keyword, event.getTreeDepth(), event.getMaxChildCount());
        }
        return false;
    }

    private boolean checkTextForTextView(TextView textView, String keyword) {
        try {
            Method m1 = TextView.class.getDeclaredMethod("getTextForAccessibility", new Class[0]);
            m1.setAccessible(true);
            CharSequence text = (CharSequence) m1.invoke(textView, new Object[0]);
            if (!TextUtils.isEmpty(text)) {
                return text.equals(keyword);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "no such method.", e);
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "access illegal.", e2);
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "invocation error.", e3);
        }
        return false;
    }

    private boolean checkTextForViewGroup(ViewGroup view, String keyword, int depth, int count) {
        this.mStack.clear();
        int currentDepth = 0;
        this.mStack.add(view);
        int maxDepth = 4;
        if (depth < 4) {
            maxDepth = depth;
        }
        int maxCount = count < 31 ? count : 12;
        while (true) {
            if (this.mStack.isEmpty()) {
                return false;
            }
            View child = this.mStack.pop();
            if (child instanceof TextView) {
                if (checkTextForTextView((TextView) child, keyword)) {
                    return true;
                }
            } else if ((child instanceof ViewGroup) && depth <= maxDepth) {
                View[] children = getViewGroupChildren((ViewGroup) child);
                currentDepth++;
                if (children != null && children.length <= maxCount) {
                    for (View c : children) {
                        if (c != null) {
                            this.mStack.add(c);
                        }
                    }
                }
            }
        }
    }

    private View[] getViewGroupChildren(ViewGroup view) {
        try {
            Field field = ViewGroup.class.getDeclaredField("mChildren");
            field.setAccessible(true);
            return (View[]) field.get(view);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "no such field.", e);
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "access illegal.", e2);
            return null;
        }
    }

    private boolean checkView(View view, Context context, String tagName) {
        if (view == null) {
            return false;
        }
        FastgrabConfigReader fastgrabConfigReader = FastgrabConfigReader.getInstance(context);
        if (fastgrabConfigReader != null && fastgrabConfigReader.getInt(ATTR_SWITCH) == 1) {
            String rootView = fastgrabConfigReader.getString(tagName);
            if (rootView != null && !rootView.isEmpty()) {
                View root = view.getRootView();
                if (root == null || !root.toString().contains(rootView)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
