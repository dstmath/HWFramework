package android.rms.iaware;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.provider.HwSettings;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppSceneRecogManager {
    private static final String ACTIVITY_CLASS_STR = "android.app.Activity";
    private static final int ACTIVITY_DEPTH_NUM = 10;
    /* access modifiers changed from: private */
    public static final Integer ACTIVITY_STATE_RESUMED = 1;
    private static final Integer ACTIVITY_STATE_STOPPED = 2;
    private static final String CHILDFRAGMENTMANAGER_FIELD_STR = "mChildFragmentManager";
    private static final long CHOREOGRAPHER_POST_DELAY = 160;
    private static final boolean CONFIG_SWITCH = SystemProperties.getBoolean("persist.sys.iaware.appscenerecog.switch", false);
    private static final String CONTAINER_FIELD_STR = "mContainer";
    private static final int DO_INIT_CLASS = 1;
    private static final String FRAGMENTACTIVITY_CLASS_STR = "android.support.v4.app.FragmentActivity";
    private static final int FRAGMENT_ACTIVITY_FRAMEWORK = 3;
    private static final int FRAGMENT_ACTIVITY_NOT = 1;
    private static final int FRAGMENT_ACTIVITY_V4 = 2;
    private static final String FRAGMENT_CLASS_STR = "android.support.v4.app.Fragment";
    private static final int FRAGMENT_DEPTH_NUM = 4;
    private static final String GETFRAGMENTS_METHOD_STR = "getFragments";
    private static final String GETSUPPORTFRAGMENTMANAGER_METHOD_STR = "getSupportFragmentManager";
    private static final String INIT_TAG = "initflag";
    private static final AppSceneRecogManager INSTANCE = new AppSceneRecogManager();
    private static final String ISVISIBLE_METHOD_STR = "isVisible";
    private static final String JSON_CLASSNAME_STR = "className";
    private static final String JSON_FRAGMENT_STR = "fragments";
    private static final String JSON_TAG_STR = "tag";
    private static final String LIST_TAG_STR = "list";
    /* access modifiers changed from: private */
    public static final boolean LOG_SWITCH = SystemProperties.getBoolean("persist.sys.iaware.appscenerecog.log.switch", false);
    private static final String MENUVISIBLE_FIELD_STR = "mMenuVisible";
    private static final int MSG_CM_CLASS_INIT = 1;
    private static final int MSG_PAUSE_ACTIVITY = 2;
    private static final int MSG_RESUME_ACTIVITY = 3;
    private static final int MSG_SCENARIO_RECOGNITION = 5;
    private static final int MSG_WINDOW_FOCUS = 4;
    private static final long RESUME_POST_DELAY = 500;
    private static final String SCENE_UNKNOWN = "unknown";
    private static final String SCENE_VEDIO = "20202";
    private static final int STATE_DISABLE = 2;
    private static final int STATE_ENABLE = 1;
    private static final int STATE_UNINITIALIZED = 0;
    private static final String TAG = "AppSceneRecogManager";
    private static final String TAG_FIELD_STR = "mTag";
    private static final String VIEW_FIELD_STR = "mView";
    /* access modifiers changed from: private */
    public volatile Activity mActivity = null;
    /* access modifiers changed from: private */
    public final Map<String, Integer> mActivityFragmentTypeMap = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public final Map<Activity, Integer> mActivityStateMap = new WeakHashMap();
    /* access modifiers changed from: private */
    public final Map<String, List<AppSceneInfo>> mAppSceneInfos = new ConcurrentHashMap();
    private Field mChildFragmentManagerField = null;
    private Field mChildFragmentManagerField2 = null;
    private ClassLoader mClassLoader = null;
    private Field mContainerField = null;
    private Field mContainerField2 = null;
    /* access modifiers changed from: private */
    public AtomicInteger mEnable = new AtomicInteger(0);
    private volatile Class<?> mFragmentActivityClass = null;
    private Class<?> mFragmentClass = null;
    private Method mGetFragmentsMethod = null;
    private Method mGetSupportFragmentManagerMethod = null;
    /* access modifiers changed from: private */
    public volatile ThirdAppHandler mHandler = null;
    private Method mIsVisibleMethod = null;
    /* access modifiers changed from: private */
    public String mLastSceneId = SCENE_UNKNOWN;
    private Field mMenuVisibleField = null;
    private Field mMenuVisibleField2 = null;
    /* access modifiers changed from: private */
    public String mPkgName = "";
    /* access modifiers changed from: private */
    public String mSysStatus = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF;
    private Field mTagField = null;
    private Field mViewField = null;

    private static class AppSceneInfo {
        List<LayoutInfo> mLayoutInfos;
        String mSceneId;
        String mSysStatus;

        public AppSceneInfo(String sceneId, List<LayoutInfo> layoutInfos, String sysStatus) {
            this.mSceneId = sceneId;
            this.mLayoutInfos = layoutInfos;
            this.mSysStatus = sysStatus;
        }
    }

    private class AppSceneRecogSDKCallback extends Binder implements IInterface {
        private static final int BINDER_TRANSACTION_END = 1;
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.AppSceneRecogSDKCallback";
        private static final int TRANSACTION_RECOGAPPSCENE = 1;
        private final List<String> sceneList = new ArrayList();

        public AppSceneRecogSDKCallback() {
            attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                return super.onTransact(code, data, reply, flags);
            }
            try {
                data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
                List<String> list = new ArrayList<>();
                data.readStringList(list);
                int binderFlag = data.readInt();
                this.sceneList.addAll(list);
                if (binderFlag == 1) {
                    if (!this.sceneList.isEmpty()) {
                        if (AppSceneRecogManager.LOG_SWITCH) {
                            Log.d(AppSceneRecogManager.TAG, "Scene data for " + Process.myPid() + ": " + list);
                        }
                        sendInitMsg();
                        if (AppSceneRecogManager.this.mEnable.compareAndSet(0, 1)) {
                            AppSceneRecogManager.this.initHandler();
                            if (AppSceneRecogManager.this.mHandler != null) {
                                Message msg = AppSceneRecogManager.this.mHandler.obtainMessage();
                                msg.what = 1;
                                ArrayList<String> tmpArrayList = new ArrayList<>(this.sceneList);
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList(AppSceneRecogManager.LIST_TAG_STR, tmpArrayList);
                                msg.setData(bundle);
                                AppSceneRecogManager.this.mHandler.sendMessage(msg);
                                this.sceneList.clear();
                            }
                        }
                    } else {
                        AppSceneRecogManager.this.mEnable.compareAndSet(0, 2);
                    }
                }
                return true;
            } catch (SecurityException e) {
                AppSceneRecogManager.this.printExceptionInfo("enforceInterface SDK_CALLBACK_DESCRIPTOR failed");
                AppSceneRecogManager.this.mEnable.compareAndSet(0, 2);
                return false;
            }
        }

        public IBinder asBinder() {
            return this;
        }

        private void sendInitMsg() {
            int initFlag = 0;
            if (AppSceneRecogManager.this.mEnable.compareAndSet(0, 1)) {
                AppSceneRecogManager.this.initHandler();
                initFlag = 1;
            }
            if (AppSceneRecogManager.this.mHandler != null) {
                Message msg = AppSceneRecogManager.this.mHandler.obtainMessage();
                msg.what = 1;
                ArrayList<String> tmpArrayList = new ArrayList<>(this.sceneList);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(AppSceneRecogManager.LIST_TAG_STR, tmpArrayList);
                bundle.putInt(AppSceneRecogManager.INIT_TAG, initFlag);
                msg.setData(bundle);
                AppSceneRecogManager.this.mHandler.sendMessage(msg);
                this.sceneList.clear();
            }
        }
    }

    private static class BaseClass {
        private BaseClass() {
        }
    }

    private static class FragmentInfo extends BaseClass {
        String mFragmentClassName;
        String mTag;

        public FragmentInfo(String fragmentName, String tag) {
            super();
            this.mFragmentClassName = fragmentName;
            this.mTag = tag;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof FragmentInfo)) {
                return false;
            }
            FragmentInfo fragmentInfo = (FragmentInfo) obj;
            if (fragmentInfo.mFragmentClassName == null || !fragmentInfo.mFragmentClassName.equals(this.mFragmentClassName) || fragmentInfo.mTag == null || !fragmentInfo.mTag.equals(this.mTag)) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return " FragmentClass=" + this.mFragmentClassName + ",Tags=" + this.mTag + ";";
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    private static class LayoutInfo extends BaseClass {
        FragmentInfo mFragmentInfo;
        List<LayoutInfo> mLayoutInfos;

        public LayoutInfo(FragmentInfo fragmentInfo) {
            super();
            this.mFragmentInfo = fragmentInfo;
        }

        public LayoutInfo(FragmentInfo fragmentInfo, List<LayoutInfo> layoutInfos) {
            super();
            this.mFragmentInfo = fragmentInfo;
            this.mLayoutInfos = layoutInfos;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof LayoutInfo)) {
                return false;
            }
            LayoutInfo layoutInfo = (LayoutInfo) obj;
            if (!this.mFragmentInfo.equals(layoutInfo.mFragmentInfo) || !RecogUtils.equalsList(layoutInfo.mLayoutInfos, this.mLayoutInfos)) {
                z = false;
            }
            return z;
        }

        public String toString() {
            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("className:");
            sBuffer.append(this.mFragmentInfo.mFragmentClassName);
            sBuffer.append(",tag:");
            sBuffer.append(this.mFragmentInfo.mTag);
            sBuffer.append(",fragments:[");
            if (this.mLayoutInfos != null) {
                int count = this.mLayoutInfos.size();
                for (int i = 0; i < count; i++) {
                    LayoutInfo tt = this.mLayoutInfos.get(i);
                    if (tt != null) {
                        sBuffer.append("{");
                        sBuffer.append(tt.toString());
                        sBuffer.append("}");
                        if (i < count - 1) {
                            sBuffer.append(",");
                        }
                    }
                }
            }
            sBuffer.append("]");
            return sBuffer.toString();
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    private static class RecogUtils {
        private RecogUtils() {
        }

        private static boolean isEmptyList(List<? extends BaseClass> list) {
            if (list == null || list.size() == 0) {
                return true;
            }
            return false;
        }

        public static boolean equalsList(List<? extends BaseClass> list1, List<? extends BaseClass> list2) {
            if (isEmptyList(list1) && isEmptyList(list2)) {
                return true;
            }
            if (list1 == null || list2 == null) {
                return false;
            }
            int list1Len = list1.size();
            int list2Len = list2.size();
            if (list1Len != list2Len) {
                return false;
            }
            for (int i = 0; i < list1Len; i++) {
                BaseClass baseClass = (BaseClass) list1.get(i);
                int j = 0;
                while (j < list2Len && !baseClass.equals(list2.get(j))) {
                    j++;
                }
                if (j == list2Len) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class ThirdAppHandler extends Handler {
        public ThirdAppHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handleInitMsg(msg);
                    return;
                case 2:
                    AppSceneRecogManager.this.printDebugLog("MSG_PAUSE_ACTIVITY: ", msg.obj);
                    handleReportRecogResult(AppSceneRecogManager.SCENE_UNKNOWN);
                    sendRecogMsg(null);
                    return;
                case 3:
                case 4:
                    handleResumeFocusChangedMsg(msg.what, msg);
                    return;
                case 5:
                    handleRecogMsg(msg);
                    return;
                default:
                    return;
            }
        }

        private void handleInitMsg(Message msg) {
            AppSceneRecogManager.this.printDebugLog("MSG_CM_CLASS_INIT");
            Bundle bundle = msg.getData();
            ArrayList<String> list = null;
            try {
                list = bundle.getStringArrayList(AppSceneRecogManager.LIST_TAG_STR);
            } catch (ArrayIndexOutOfBoundsException e) {
                AppSceneRecogManager.this.printExceptionInfo("handleInitMsg occur ArrayIndexOutOfBoundsException.");
            }
            if (list != null) {
                saveAppSceneInfo(list);
                if (bundle.getInt(AppSceneRecogManager.INIT_TAG, 0) == 1) {
                    AppSceneRecogManager.this.initClasses();
                    Log.i(AppSceneRecogManager.TAG, "Enable is:" + AppSceneRecogManager.this.mEnable.get());
                }
            }
        }

        private void handleResumeFocusChangedMsg(int what, Message msg) {
            Activity activity = (Activity) msg.obj;
            if (activity == AppSceneRecogManager.this.mActivity) {
                if (AppSceneRecogManager.LOG_SWITCH) {
                    if (what == 4) {
                        Log.d(AppSceneRecogManager.TAG, "MSG_WINDOW_FOCUS: " + activity);
                    } else {
                        Log.d(AppSceneRecogManager.TAG, "MSG_RESUME_ACTIVITY: " + activity);
                    }
                }
                if (AppSceneRecogManager.ACTIVITY_STATE_RESUMED.equals(AppSceneRecogManager.this.mActivityStateMap.get(activity))) {
                    int unused = AppSceneRecogManager.this.initAndGetFragmentType(activity);
                    handleReportRecogResult(doRecog(activity));
                }
                sendRecogMsg(activity);
            }
        }

        private void handleRecogMsg(Message msg) {
            Activity activity = (Activity) msg.obj;
            if (AppSceneRecogManager.this.mEnable.get() != 2 && activity == AppSceneRecogManager.this.mActivity) {
                if (View.sTriggerFlag) {
                    View.sTriggerFlag = false;
                    if (AppSceneRecogManager.LOG_SWITCH) {
                        Log.d(AppSceneRecogManager.TAG, "MSG_SCENARIO_RECOGNITION: " + activity + " state:" + AppSceneRecogManager.this.mActivityStateMap.get(activity));
                    }
                    if (AppSceneRecogManager.ACTIVITY_STATE_RESUMED.equals(AppSceneRecogManager.this.mActivityStateMap.get(activity))) {
                        handleReportRecogResult(doRecog(activity));
                    }
                }
                sendRecogMsg(activity);
            }
        }

        private void sendRecogMsg(Activity activity) {
            if (activity != null) {
                String activityName = activity.getClass().getName();
                if (((List) AppSceneRecogManager.this.mAppSceneInfos.get(activityName)) != null) {
                    Integer num = 1;
                    if (!num.equals(AppSceneRecogManager.this.mActivityFragmentTypeMap.get(activityName))) {
                        AppSceneRecogManager.this.mHandler.sendMessageDelayed(AppSceneRecogManager.this.mHandler.obtainMessage(5, activity), AppSceneRecogManager.CHOREOGRAPHER_POST_DELAY);
                    }
                }
            }
        }

        private String doRecog(Activity activity) {
            AppSceneInfo sceneInfo;
            if (activity != AppSceneRecogManager.this.mActivity) {
                AppSceneRecogManager.this.printDebugLog("doRecog: this activity is gone, return null.");
                return null;
            }
            String activityName = activity.getClass().getName();
            List<AppSceneInfo> appSceneInfoList = (List) AppSceneRecogManager.this.mAppSceneInfos.get(activityName);
            if (appSceneInfoList == null) {
                AppSceneRecogManager.this.printDebugLog("Current activity has no scene.");
                return null;
            }
            String sceneId = AppSceneRecogManager.SCENE_UNKNOWN;
            int i = 0;
            int total = appSceneInfoList.size();
            while (true) {
                if (i >= total) {
                    break;
                }
                sceneInfo = appSceneInfoList.get(i);
                if (sceneInfo != null && AppSceneRecogManager.this.mSysStatus.equals(sceneInfo.mSysStatus)) {
                    if (sceneInfo.mLayoutInfos == null || sceneInfo.mLayoutInfos.size() == 0) {
                        List<LayoutInfo> layoutInfos = AppSceneRecogManager.this.getActivityLayoutInfo(activityName, activity, 0);
                    } else {
                        List<LayoutInfo> layoutInfos2 = AppSceneRecogManager.this.getActivityLayoutInfo(activityName, activity);
                        AppSceneRecogManager.this.printDebugLog("Current layout: ", AppSceneRecogManager.this.getLayoutList(layoutInfos2));
                        AppSceneRecogManager.this.printDebugLog("Feature layout: ", AppSceneRecogManager.this.getLayoutList(sceneInfo.mLayoutInfos));
                        if (RecogUtils.equalsList(sceneInfo.mLayoutInfos, layoutInfos2)) {
                            sceneId = sceneInfo.mSceneId;
                            AppSceneRecogManager.this.printDebugLog("Matches with the fragments. sceneId: ", sceneId);
                            break;
                        }
                    }
                }
                i++;
            }
            List<LayoutInfo> layoutInfos3 = AppSceneRecogManager.this.getActivityLayoutInfo(activityName, activity, 0);
            if (layoutInfos3 == null || layoutInfos3.size() == 0) {
                sceneId = sceneInfo.mSceneId;
                AppSceneRecogManager.this.printDebugLog("Matches with no fragments. sceneId: ", sceneId);
            } else {
                AppSceneRecogManager.this.printDebugLog("----No Match with no fragments. sceneId:", sceneId);
            }
            AppSceneRecogManager.this.printDebugLog("-----Finish to recognize. sceneId: ", sceneId);
            return sceneId;
        }

        private void handleReportRecogResult(String currentSceneId) {
            String message = currentSceneId + " " + Process.myPid();
            if (AppSceneRecogManager.LOG_SWITCH) {
                Log.d(AppSceneRecogManager.TAG, "-----begin to report scene: " + message + " last:" + AppSceneRecogManager.this.mLastSceneId);
            }
            if (currentSceneId != null && !currentSceneId.equals(AppSceneRecogManager.this.mLastSceneId)) {
                try {
                    IAwareSdk.asyncReportData(3036, message, 0);
                    String unused = AppSceneRecogManager.this.mLastSceneId = currentSceneId;
                    AppSceneRecogManager.this.printDebugLog("-----Success to report scene: ", message);
                } catch (RuntimeException e) {
                    AppSceneRecogManager.this.printExceptionInfo("Failed to report scene.");
                }
            }
        }

        private void saveAppSceneInfo(ArrayList<String> list) {
            AppSceneRecogManager.this.mAppSceneInfos.clear();
            try {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    JSONObject jsonObject = new JSONObject(list.get(i));
                    if (AppSceneRecogManager.this.mPkgName.equals(getJsonString(jsonObject, AppTypeRecoManager.APP_PKGNAME))) {
                        String sceneId = getJsonString(jsonObject, "sceneId");
                        String activity = getJsonString(jsonObject, "activity");
                        List<LayoutInfo> layoutInfoList = getLayoutInfo(new JSONArray(getJsonString(jsonObject, "layout")));
                        List<AppSceneInfo> appSceneInfoList = (List) AppSceneRecogManager.this.mAppSceneInfos.get(activity);
                        if (appSceneInfoList == null) {
                            appSceneInfoList = new ArrayList<>();
                        }
                        appSceneInfoList.add(new AppSceneInfo(sceneId, layoutInfoList, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF));
                        AppSceneRecogManager.this.mAppSceneInfos.put(activity, appSceneInfoList);
                    }
                }
            } catch (JSONException e) {
                AppSceneRecogManager.this.printExceptionInfo("saveAppSceneInfo occur JSONException.");
            }
        }

        private String getJsonString(JSONObject entry, String name) {
            return entry.optString(name, "");
        }

        private List<LayoutInfo> getLayoutInfo(JSONArray jsonArray) throws JSONException {
            List<LayoutInfo> layoutInfoList = new ArrayList<>();
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String className = getJsonString(obj, AppSceneRecogManager.JSON_CLASSNAME_STR);
                    String strTMP = obj.toString();
                    String tag = "null";
                    if (strTMP.contains(AppSceneRecogManager.JSON_TAG_STR)) {
                        tag = getJsonString(obj, AppSceneRecogManager.JSON_TAG_STR);
                    }
                    FragmentInfo fragmentInfo = new FragmentInfo(className, tag);
                    if (strTMP.contains(AppSceneRecogManager.JSON_FRAGMENT_STR)) {
                        layoutInfoList.add(new LayoutInfo(fragmentInfo, getLayoutInfo(obj.getJSONArray(AppSceneRecogManager.JSON_FRAGMENT_STR))));
                    } else {
                        layoutInfoList.add(new LayoutInfo(fragmentInfo));
                    }
                }
            }
            return layoutInfoList;
        }
    }

    public static AppSceneRecogManager getInstance() {
        return INSTANCE;
    }

    private AppSceneRecogManager() {
    }

    public void init(Application app, String processName) {
        if (app == null || UserHandle.getAppId(Process.myUid()) < 10000) {
            this.mEnable.set(2);
        } else if (!CONFIG_SWITCH) {
            printDebugLog("AppScene ConfigSwitch is false.");
            this.mEnable.set(2);
        } else {
            try {
                Context cxt = app.getApplicationContext();
                if (cxt == null) {
                    this.mEnable.set(2);
                    return;
                }
                String pkgName = cxt.getPackageName();
                if (LOG_SWITCH) {
                    Log.d(TAG, "AppSceneRecog init. Package: " + pkgName + ", Process: " + processName + ", Pid: " + Process.myPid());
                }
                this.mPkgName = pkgName;
                IAwareSdk.asyncReportDataWithCallback(3035, pkgName, new AppSceneRecogSDKCallback(), 0);
                this.mClassLoader = cxt.getClassLoader();
            } catch (RuntimeException e) {
                printExceptionInfo("init occur RuntimeException.");
                this.mEnable.set(2);
            }
        }
    }

    public void resumeActivity(Activity activity) {
        if (this.mEnable.get() == 1 && this.mHandler != null && activity != null) {
            printDebugLog("Resume activity: ", activity);
            this.mActivity = activity;
            this.mActivityStateMap.put(activity, ACTIVITY_STATE_RESUMED);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3, activity), RESUME_POST_DELAY);
        }
    }

    public void pauseActivity(Activity activity) {
        if (this.mEnable.get() == 1 && this.mHandler != null && activity != null) {
            printDebugLog("Pause activity: ", activity);
            this.mActivity = null;
            this.mActivityStateMap.put(activity, ACTIVITY_STATE_STOPPED);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, activity));
        }
    }

    public void windowFocusChanged(Context context) {
        if (this.mEnable.get() == 1 && this.mHandler != null) {
            Activity activity = getActivity(context);
            if (activity != null) {
                printDebugLog("Window focus changed: ", activity);
                if (activity == this.mActivity && ACTIVITY_STATE_RESUMED.equals(this.mActivityStateMap.get(activity))) {
                    this.mHandler.removeMessages(3);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(4, activity));
                }
            }
        }
    }

    private Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        Activity activity = null;
        int level = 0;
        Context tmpContext = context;
        while (true) {
            int level2 = level + 1;
            if (level >= 10) {
                break;
            } else if ((tmpContext instanceof Activity) != 0) {
                activity = (Activity) tmpContext;
                break;
            } else {
                tmpContext = tmpContext instanceof ContextWrapper ? ((ContextWrapper) context).getBaseContext() : null;
                level = level2;
            }
        }
        return activity;
    }

    /* access modifiers changed from: private */
    public void initHandler() {
        HandlerThread handlerThread = new HandlerThread("ThirdAppSceneRecog", 10);
        handlerThread.start();
        if (handlerThread.getLooper() != null && this.mHandler == null) {
            this.mHandler = new ThirdAppHandler(handlerThread.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public void initClasses() {
        if (this.mFragmentActivityClass == null) {
            if (this.mClassLoader != null) {
                try {
                    this.mFragmentActivityClass = this.mClassLoader.loadClass(FRAGMENTACTIVITY_CLASS_STR);
                    if (this.mFragmentActivityClass != null) {
                        initializeV4FragmentClasses(this.mFragmentActivityClass);
                    } else {
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    printExceptionInfo("FragmentActivity is not found.");
                }
            } else {
                return;
            }
        }
        try {
            this.mMenuVisibleField2 = Fragment.class.getDeclaredField(MENUVISIBLE_FIELD_STR);
            this.mContainerField2 = Fragment.class.getDeclaredField(CONTAINER_FIELD_STR);
            this.mChildFragmentManagerField2 = Fragment.class.getDeclaredField(CHILDFRAGMENTMANAGER_FIELD_STR);
            this.mMenuVisibleField2.setAccessible(true);
            this.mContainerField2.setAccessible(true);
            this.mChildFragmentManagerField2.setAccessible(true);
        } catch (NoSuchFieldException e2) {
            printExceptionInfo("init Fragment field not found.");
        } catch (SecurityException e3) {
            printExceptionInfo("init Fragment field not found.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        initFragmentClassMethod(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        initFragmentClassMethod(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c2, code lost:
        printExceptionInfo("Failed to initialize fragment class info.");
        r6.mEnable.set(2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00c1 A[ExcHandler: NoSuchFieldException (e java.lang.NoSuchFieldException), Splitter:B:1:0x0004] */
    private void initializeV4FragmentClasses(Class<?> fragmentActivityClass) {
        try {
            this.mGetSupportFragmentManagerMethod = fragmentActivityClass.getDeclaredMethod(GETSUPPORTFRAGMENTMANAGER_METHOD_STR, new Class[0]);
            Class<?> fragmentManagerClass = this.mGetSupportFragmentManagerMethod.getReturnType();
            this.mGetFragmentsMethod = fragmentManagerClass.getDeclaredMethod(GETFRAGMENTS_METHOD_STR, new Class[0]);
            try {
                this.mFragmentClass = this.mClassLoader.loadClass(FRAGMENT_CLASS_STR);
            } catch (ClassNotFoundException e) {
                this.mFragmentClass = getFragmentClass(this.mGetFragmentsMethod);
            }
            if (this.mFragmentClass != null) {
                this.mViewField = this.mFragmentClass.getDeclaredField(VIEW_FIELD_STR);
                this.mViewField.setAccessible(true);
                this.mContainerField = this.mFragmentClass.getDeclaredField(CONTAINER_FIELD_STR);
                this.mContainerField.setAccessible(true);
                this.mMenuVisibleField = this.mFragmentClass.getDeclaredField(MENUVISIBLE_FIELD_STR);
                this.mMenuVisibleField.setAccessible(true);
                this.mTagField = this.mFragmentClass.getDeclaredField(TAG_FIELD_STR);
                this.mTagField.setAccessible(true);
                this.mIsVisibleMethod = this.mFragmentClass.getDeclaredMethod(ISVISIBLE_METHOD_STR, new Class[0]);
                this.mChildFragmentManagerField = this.mFragmentClass.getDeclaredField(CHILDFRAGMENTMANAGER_FIELD_STR);
                this.mChildFragmentManagerField.setAccessible(true);
                this.mFragmentActivityClass = fragmentActivityClass;
                return;
            }
            printDebugLog("Failed to initialize fragment class info. ", fragmentActivityClass.getName());
            this.mEnable.set(2);
        } catch (NoSuchFieldException e2) {
        } catch (NoSuchMethodException e3) {
            printExceptionInfo("Failed to initialize fragment class info.");
            this.mEnable.set(2);
        } catch (SecurityException e4) {
            printExceptionInfo("Failed to initialize fragment class info.");
            this.mEnable.set(2);
        }
    }

    private void initFragmentClassMethod(Class<?> fragmentManagerClass) {
        Object[] objs = getFragmentsMethodBySignature(fragmentManagerClass);
        if (objs.length > 1) {
            this.mGetFragmentsMethod = (Method) objs[0];
            this.mFragmentClass = (Class) objs[1];
        }
    }

    private Object[] getFragmentsMethodBySignature(Class<?> fragmentManagerClass) {
        for (Method method : fragmentManagerClass.getMethods()) {
            Class<?> fragmentClass = getFragmentClass(method);
            if (fragmentClass != null) {
                return new Object[]{method, fragmentClass};
            }
        }
        return new Object[0];
    }

    private Class<?> getFragmentClass(Method method) {
        if (List.class.isAssignableFrom(method.getReturnType())) {
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    return (Class) actualTypeArguments[0];
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public int initAndGetFragmentType(Activity activity) {
        Class clazz = activity.getClass();
        Integer knownFragmentType = this.mActivityFragmentTypeMap.get(activity.getClass().getName());
        if (knownFragmentType != null) {
            return knownFragmentType.intValue();
        }
        int fragmentType = 1;
        if (this.mFragmentActivityClass == null) {
            while (true) {
                if (clazz == Object.class) {
                    break;
                }
                Method method = null;
                try {
                    method = clazz.getDeclaredMethod(GETSUPPORTFRAGMENTMANAGER_METHOD_STR, new Class[0]);
                } catch (NoSuchMethodException e) {
                    printExceptionInfo("initAndGetFragmentType occur NoSuchMethodException");
                } catch (SecurityException e2) {
                    printExceptionInfo("initAndGetFragmentType occur SecurityException");
                }
                if (method != null) {
                    fragmentType = 2;
                    break;
                }
                clazz = clazz.getSuperclass();
            }
            if (fragmentType == 2) {
                initializeV4FragmentClasses(clazz);
            }
        } else if (this.mFragmentActivityClass.isAssignableFrom(clazz)) {
            fragmentType = 2;
        }
        if (fragmentType != 2) {
            FragmentManager manager = activity.getFragmentManager();
            if (manager != null) {
                List<Fragment> fragments = manager.getFragments();
                if (fragments != null && fragments.size() > 0) {
                    Iterator<Fragment> it = fragments.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Fragment fragment = it.next();
                        if (fragment != null) {
                            View view = fragment.getView();
                            if (view != null && view.getVisibility() == 0) {
                                fragmentType = 3;
                                break;
                            }
                        }
                    }
                }
            }
        }
        this.mActivityFragmentTypeMap.put(activity.getClass().getName(), Integer.valueOf(fragmentType));
        return fragmentType;
    }

    /* access modifiers changed from: private */
    public List<LayoutInfo> getActivityLayoutInfo(String activityName, Activity activity) {
        return getActivityLayoutInfo(activityName, activity, 4);
    }

    /* access modifiers changed from: private */
    public List<LayoutInfo> getActivityLayoutInfo(String activityName, Activity activity, int depth) {
        try {
            if (this.mFragmentActivityClass == null || !this.mFragmentActivityClass.isAssignableFrom(activity.getClass())) {
                FragmentManager manager = activity.getFragmentManager();
                if (manager != null) {
                    return getChildLayouts(manager.getFragments(), depth);
                }
                printDebugLog("android.app.fragment FragmentManager is null.");
                return null;
            } else if (this.mGetSupportFragmentManagerMethod == null || this.mGetFragmentsMethod == null) {
                printDebugLog("No getFragments method.");
                return null;
            } else {
                try {
                    Object fragmentManager = this.mGetSupportFragmentManagerMethod.invoke(activity, new Object[0]);
                    if (fragmentManager == null) {
                        printDebugLog("FragmentManager is null.");
                        return null;
                    }
                    List<Object> list = (List) this.mGetFragmentsMethod.invoke(fragmentManager, new Object[0]);
                    if (list != null) {
                        if (list.size() != 0) {
                            List<LayoutInfo> layoutInfos = new ArrayList<>();
                            for (Object fragment : list) {
                                if (checkVisible(fragment)) {
                                    LayoutInfo layoutInfo = new LayoutInfo(new FragmentInfo(getShortClassName(fragment), getTagName(fragment)), getChildLayoutsWithReflect(fragment, depth));
                                    if (!layoutInfos.contains(layoutInfo)) {
                                        layoutInfos.add(layoutInfo);
                                    }
                                }
                            }
                            return layoutInfos;
                        }
                    }
                    printDebugLog("Fragments is null.");
                    return null;
                } catch (IllegalAccessException e) {
                    printExceptionInfo("getActivityLayoutInfo occur IllegalAccessException");
                    return null;
                } catch (IllegalArgumentException e2) {
                    printExceptionInfo("getActivityLayoutInfo occur IllegalArgumentException");
                    return null;
                } catch (InvocationTargetException e3) {
                    printExceptionInfo("getActivityLayoutInfo occur InvocationTargetException");
                    return null;
                }
            }
        } catch (RuntimeException e4) {
            printExceptionInfo("getActivityLayoutInfo occur RuntimeException");
            return null;
        }
    }

    private List<LayoutInfo> getChildLayouts(List<Fragment> fragmentList, int depth) {
        if (fragmentList == null || fragmentList.size() == 0) {
            return null;
        }
        if (depth < 1) {
            printDebugLog("getChildLayouts traversal more than max num.");
            return null;
        }
        List<LayoutInfo> layoutInfos = new ArrayList<>();
        for (Fragment fragment : fragmentList) {
            if (fragment != null && checkFragmentVisible(fragment)) {
                LayoutInfo layoutInfo = new LayoutInfo(getFragmentInfo(fragment), getChildLayouts(fragment, depth));
                if (!layoutInfos.contains(layoutInfo)) {
                    layoutInfos.add(layoutInfo);
                }
            }
        }
        return layoutInfos;
    }

    private List<LayoutInfo> getChildLayouts(Fragment fragment, int depth) {
        try {
            FragmentManager childManager = (FragmentManager) this.mChildFragmentManagerField2.get(fragment);
            if (childManager != null) {
                return getChildLayouts(childManager.getFragments(), depth - 1);
            }
            printDebugLog("getChildLayouts childManager is null");
            return null;
        } catch (IllegalAccessException e) {
            printExceptionInfo("getChildLayouts occur IllegalAccessException.");
            return null;
        }
    }

    private FragmentInfo getFragmentInfo(Fragment fragment) {
        String tagString = fragment.getTag();
        if (tagString == null) {
            tagString = "null";
        }
        String nameString = fragment.getClass().getName();
        int indx = nameString.lastIndexOf(".");
        if (indx != -1) {
            nameString = nameString.substring(indx + 1);
        }
        return new FragmentInfo(nameString, tagString);
    }

    private List<LayoutInfo> getChildLayoutsWithReflect(Object fragmentObj, int depth) {
        try {
            if (this.mChildFragmentManagerField == null || this.mGetFragmentsMethod == null) {
                return null;
            }
            Object managerObject = this.mChildFragmentManagerField.get(fragmentObj);
            if (managerObject == null) {
                printDebugLog("getChildLayoutsWithReflect managerObject is null");
                return null;
            } else if (depth < 1) {
                printDebugLog("getChildLayoutsWithReflect traversal more than max num.");
                return null;
            } else {
                List<Object> list = (List) this.mGetFragmentsMethod.invoke(managerObject, new Object[0]);
                if (list != null) {
                    if (list.size() != 0) {
                        List<LayoutInfo> layoutInfos = new ArrayList<>();
                        for (Object fragment : list) {
                            if (checkVisible(fragment)) {
                                String fragmentName = getShortClassName(fragment);
                                String tag = getTagName(fragment);
                                layoutInfos.add(new LayoutInfo(new FragmentInfo(fragmentName, tag), getChildLayoutsWithReflect(fragment, depth - 1)));
                            }
                        }
                        return layoutInfos;
                    }
                }
                printDebugLog("getChildLayoutsWithReflect list empty");
                return null;
            }
        } catch (IllegalAccessException e) {
            printExceptionInfo("getChildLayoutsWithReflect occur IllegalAccessException.");
            return null;
        } catch (IllegalArgumentException e2) {
            printExceptionInfo("getChildLayoutsWithReflect occur IllegalArgumentException.");
            return null;
        } catch (InvocationTargetException e3) {
            printExceptionInfo("getChildLayoutsWithReflect occur InvocationTargetException.");
            return null;
        }
    }

    private boolean checkFragmentVisible(Fragment fragment) {
        if (!fragment.getUserVisibleHint()) {
            return false;
        }
        View view = fragment.getView();
        if (view == null || view.getVisibility() != 0 || !view.isVisibleToUser()) {
            return false;
        }
        try {
            if (!((Boolean) this.mMenuVisibleField2.get(fragment)).booleanValue()) {
                return false;
            }
            if (this.mContainerField2 != null) {
                ViewGroup container = (ViewGroup) this.mContainerField2.get(fragment);
                if (container != null && !container.isVisibleToUser()) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            printExceptionInfo("checkFragmentVisible occur IllegalAccessException.");
            return false;
        } catch (IllegalArgumentException e2) {
            printExceptionInfo("checkFragmentVisible occur IllegalArgumentException.");
            return false;
        } catch (RuntimeException e3) {
            printExceptionInfo("checkFragmentVisible occur RuntimeException");
            return false;
        }
    }

    private boolean checkVisible(Object fragmentObject) {
        if (fragmentObject == null) {
            printDebugLog("fragmentObject is null");
            return false;
        } else if (this.mViewField == null || this.mMenuVisibleField == null || this.mContainerField == null) {
            printDebugLog("Fragment has null field. Fragment: ", fragmentObject);
            return false;
        } else {
            try {
                if (!((Boolean) this.mMenuVisibleField.get(fragmentObject)).booleanValue()) {
                    printDebugLog("Fragment menu is not visible. Fragment: ", fragmentObject);
                    return false;
                }
                ViewGroup container = (ViewGroup) this.mContainerField.get(fragmentObject);
                if (container != null) {
                    if (container.isVisibleToUser()) {
                        View viewItem = (View) this.mViewField.get(fragmentObject);
                        if (viewItem != null && viewItem.getVisibility() == 0) {
                            if (viewItem.isVisibleToUser()) {
                                if (((Boolean) this.mIsVisibleMethod.invoke(fragmentObject, new Object[0])).booleanValue()) {
                                    printDebugLog("Fragment is visible. Fragment: ", fragmentObject);
                                    return true;
                                }
                                return false;
                            }
                        }
                        printDebugLog("Fragment view is not visible. Fragment: ", fragmentObject);
                        return false;
                    }
                }
                printDebugLog("Fragment container is not visible. Fragment: ", fragmentObject);
                return false;
            } catch (IllegalAccessException e) {
                printExceptionInfo("checkVisible occur IllegalAccessException");
            } catch (IllegalArgumentException e2) {
                printExceptionInfo("checkVisible occur IllegalArgumentException");
            } catch (InvocationTargetException e3) {
                printExceptionInfo("checkVisible occur InvocationTargetException");
            } catch (RuntimeException e4) {
                printExceptionInfo("checkVisible occur RuntimeException");
            }
        }
    }

    private String getShortClassName(Object cls) {
        if (cls == null) {
            return "null";
        }
        String str = cls.toString();
        int indexOfBrace = str.indexOf("{");
        if (indexOfBrace > 0) {
            return str.substring(0, indexOfBrace);
        }
        return "null";
    }

    private String getTagName(Object object) {
        try {
            if (this.mTagField == null) {
                return "null";
            }
            String tag = (String) this.mTagField.get(object);
            if (tag == null) {
                return "null";
            }
            return tag;
        } catch (IllegalAccessException e) {
            printExceptionInfo("getTagName occur IllegalAccessException");
            return "null";
        } catch (IllegalArgumentException e2) {
            printExceptionInfo("getTagName occur IllegalArgumentException");
            return "null";
        }
    }

    /* access modifiers changed from: private */
    public void printExceptionInfo(String content) {
        if (LOG_SWITCH) {
            Log.e(TAG, content);
        }
    }

    /* access modifiers changed from: private */
    public void printDebugLog(String content) {
        if (LOG_SWITCH) {
            Log.d(TAG, content);
        }
    }

    /* access modifiers changed from: private */
    public void printDebugLog(String content, Object param) {
        if (LOG_SWITCH) {
            Log.d(TAG, content + param);
        }
    }

    /* access modifiers changed from: private */
    public String getLayoutList(List<LayoutInfo> layoutInfos) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("[");
        if (layoutInfos != null) {
            int count = layoutInfos.size();
            for (int i = 0; i < count; i++) {
                sBuffer.append("{");
                LayoutInfo item = layoutInfos.get(i);
                if (item != null) {
                    sBuffer.append(item.toString());
                    sBuffer.append("}");
                    if (i < count - 1) {
                        sBuffer.append(",");
                    }
                }
            }
        }
        sBuffer.append("]");
        return sBuffer.toString();
    }
}
