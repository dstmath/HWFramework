package com.huawei.server.pc.decision;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.common.service.IDecision;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DecisionUtil {
    private static final String CATEGORY_KEY = "category";
    private static final String ENTER_PROJECTION_EVENT = "com.huawei.desktop.intent.action.PcScreen";
    private static final String ENTER_PROJECTION_TYPE = "ENTER_PROJECTION_TYPE";
    private static final String ID_KEY = "id";
    private static final int OPER_SUCCESS = 0;
    private static final String OP_TYPE_CANCEL = "cancel";
    private static final String OP_TYPE_GOTO = "goto";
    public static final String PC_TARGET_ACTION = "com.huawei.desktop.intent.action.LMT_PcScreen";
    public static final String RECOMMEND_FEATURE_ID = "SF-10045857_f001";
    private static final String REPORT_EVENT = "com.huawei.tips.intent.action.UE_DATA_USER_OPERATION";
    private static final String REPORT_TYPE = "REPORT_TYPE";
    private static final String TAG = DecisionUtil.class.getSimpleName();
    private static ConcurrentHashMap<String, DecisionCallback> mCallbackList = new ConcurrentHashMap<>();
    private static Context mContext;
    private static IDecision mDecisionApi = null;
    private static ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(DecisionUtil.TAG, "service connected.");
            IDecision unused = DecisionUtil.mDecisionApi = IDecision.Stub.asInterface(service);
            if (DecisionUtil.REPORT_TYPE.equals(DecisionUtil.mType)) {
                Map<String, Object> extras = new ArrayMap<>();
                extras.put("feature_id", DecisionUtil.RECOMMEND_FEATURE_ID);
                extras.put("type", "dlg");
                extras.put("op", DecisionUtil.mOpType);
                DecisionUtil.executeEvent(DecisionUtil.REPORT_EVENT, extras);
            } else if (DecisionUtil.ENTER_PROJECTION_TYPE.equals(DecisionUtil.mType)) {
                DecisionUtil.executeEvent(DecisionUtil.ENTER_PROJECTION_EVENT);
            }
            if (DecisionUtil.mContext != null) {
                DecisionUtil.unbindService(DecisionUtil.mContext);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.mDecisionApi = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };
    private static Handler mHander = null;
    private static String mOpType = null;
    private static AlertDialog mRecommendDialog;
    private static String mType = null;

    private DecisionUtil() {
    }

    public static void bindService(Context context, String type) {
        mType = type;
        mContext = context;
        if (context == null || mDecisionApi != null) {
            Log.i(TAG, "service already binded");
            return;
        }
        if (mHander == null) {
            mHander = new Handler(context.getMainLooper());
        }
        Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
        actionService.setPackage("com.huawei.recsys");
        try {
            context.bindService(actionService, mDecisionConnection, 1);
        } catch (Exception e) {
            Log.e(TAG, "bindService Exception.");
        }
    }

    public static void unbindService(Context context) {
        if (context != null) {
            try {
                context.unbindService(mDecisionConnection);
            } catch (Exception e) {
                Log.e(TAG, "unbindService fail");
            }
            mDecisionApi = null;
        }
    }

    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, (String) null, (Map<String, Object>) null, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras) {
        return executeEvent(eventName, (String) null, extras, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, DecisionCallback callback) {
        return executeEvent(eventName, (String) null, (Map<String, Object>) null, callback);
    }

    public static boolean executeEvent(String eventName, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, null, null, callback, timeout);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, (String) null, extras, callback);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, null, extras, callback, timeout);
    }

    public static boolean executeEvent(String eventName, String dataId) {
        return executeEvent(eventName, dataId, (Map<String, Object>) null, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras) {
        return executeEvent(eventName, dataId, extras, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, String dataId, DecisionCallback callback) {
        return executeEvent(eventName, dataId, (Map<String, Object>) null, callback);
    }

    public static boolean executeEvent(String eventName, String dataId, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, dataId, null, callback, timeout);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, dataId, extras, callback, -1);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        final String key;
        if (mDecisionApi == null) {
            return false;
        }
        Map<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : BuildConfig.FLAVOR);
        if (!TextUtils.isEmpty(eventName) && !eventName.equals(dataId)) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        if (callback != null) {
            key = callback.toString();
        } else {
            key = null;
        }
        DecisionCallback innerCallback = new DecisionCallback() {
            /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass2 */

            @Override // com.huawei.server.pc.decision.DecisionCallback
            public void onResult(Map result) throws RemoteException {
                if (key != null) {
                    DecisionUtil.mCallbackList.remove(key);
                }
                if (this.mReversed1 != null) {
                    try {
                        this.mReversed1.onResult(result);
                    } catch (RemoteException e) {
                    } catch (Exception e2) {
                    }
                }
            }
        };
        innerCallback.setReversed1(callback);
        setTimeoutProcess(callback, timeout, key, innerCallback);
        try {
            mDecisionApi.executeEvent(extra2, innerCallback);
            return true;
        } catch (RemoteException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    private static void setTimeoutProcess(DecisionCallback callback, long timeout, final String key, final DecisionCallback innerCallback) {
        if (callback != null && timeout > 0) {
            mCallbackList.put(key, callback);
            mHander.postDelayed(new Runnable() {
                /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    DecisionCallback userCallback = (DecisionCallback) DecisionUtil.mCallbackList.remove(key);
                    if (userCallback != null) {
                        innerCallback.clearReversed1();
                        try {
                            userCallback.onTimeout();
                        } catch (Exception e) {
                        }
                    }
                }
            }, timeout);
        }
    }

    public static boolean insertBusinessData(String category) {
        return insertBusinessData(category, null, null);
    }

    public static boolean insertBusinessData(String category, String dataId) {
        return insertBusinessData(category, dataId, null);
    }

    public static boolean insertBusinessData(String category, Map<String, Object> extras) {
        return insertBusinessData(category, null, extras);
    }

    public static boolean insertBusinessData(String category, String dataId, Map<String, Object> extras) {
        if (mDecisionApi == null) {
            return false;
        }
        Map<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : BuildConfig.FLAVOR);
        if (!TextUtils.isEmpty(category) && !category.equals(dataId)) {
            extra2.put(CATEGORY_KEY, category);
        }
        try {
            if (mDecisionApi.insertBusinessData(extra2) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    public static boolean removeBusinessData(String category) {
        return removeBusinessData(category, null);
    }

    public static boolean removeBusinessData(String category, String dataId) {
        IDecision iDecision = mDecisionApi;
        if (iDecision == null) {
            return false;
        }
        try {
            if (iDecision.removeBusinessData(category, dataId) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    public static void showPCRecommendDialog(Context context) {
        mContext = context;
        dismissPCRecommendDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 33947691);
        View contentView = LayoutInflater.from(context).inflate(HwPartResourceUtils.getResourceId("pc_decision_dialog"), (ViewGroup) null);
        mRecommendDialog = builder.setPositiveButton(HwPartResourceUtils.getResourceId("pc_recommend_dialog_open"), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass5 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                DecisionUtil.dismissPCRecommendDialog();
                DecisionUtil.startSearch(DecisionUtil.mContext);
                DecisionUtil.executeReportEvent(DecisionUtil.mContext, DecisionUtil.OP_TYPE_GOTO);
            }
        }).setNegativeButton(HwPartResourceUtils.getResourceId("pc_recommend_dialog_ignore"), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass4 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DecisionUtil.executeReportEvent(DecisionUtil.mContext, DecisionUtil.OP_TYPE_CANCEL);
            }
        }).setView(contentView).create();
        ((ImageView) contentView.findViewById(HwPartResourceUtils.getResourceId("pc_decision_dialog_image"))).getDrawable().setAutoMirrored(true);
        String linkStr = context.getResources().getString(HwPartResourceUtils.getResourceId("pc_recommend_dialog_learn"));
        String tmpInfo = context.getResources().getString(HwPartResourceUtils.getResourceId("pc_recommend_dialog_desc_new"));
        int start = tmpInfo.length();
        StringBuilder agreeInfo = new StringBuilder(tmpInfo);
        agreeInfo.append(" ");
        agreeInfo.append(linkStr);
        setClickableSpanForTextView((TextView) contentView.findViewById(HwPartResourceUtils.getResourceId("pc_decision_dialog_content")), new NoUnderLineClickSpan(), agreeInfo.toString(), start, agreeInfo.length());
        mRecommendDialog.getWindow().setType(2008);
        mRecommendDialog.show();
    }

    /* access modifiers changed from: private */
    public static class NoUnderLineClickSpan extends ClickableSpan {
        private NoUnderLineClickSpan() {
        }

        @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            if (DecisionUtil.mContext != null) {
                ds.setColor(DecisionUtil.mContext.getColor(33882525));
                ds.setTypeface(Typeface.create("HwChinese-medium", 0));
                ds.setUnderlineText(false);
            }
        }

        @Override // android.text.style.ClickableSpan
        public void onClick(View widget) {
            if (widget instanceof TextView) {
                DecisionUtil.gotoPlayingSkills();
            }
        }
    }

    private static void setClickableSpanForTextView(TextView tv, ClickableSpan clickableSpan, String text, int start, int end) {
        if (start < 0 || start >= end || end > text.length()) {
            tv.setText(text);
            return;
        }
        SpannableString sp = new SpannableString(text);
        sp.setSpan(clickableSpan, start, end, 33);
        tv.setText(sp);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setFocusable(false);
    }

    public static void dismissPCRecommendDialog() {
        AlertDialog alertDialog = mRecommendDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            mRecommendDialog.dismiss();
            mRecommendDialog = null;
        }
    }

    /* access modifiers changed from: private */
    public static void gotoPlayingSkills() {
        if (mContext != null) {
            dismissPCRecommendDialog();
            Intent intent = new Intent("com.huawei.tips.JUMP_TO_TIPS");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setPackage("com.huawei.tips");
            intent.putExtra("featureID", RECOMMEND_FEATURE_ID);
            intent.setFlags(268435456);
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "PlayingSkills app not found.");
            }
        }
    }

    public static void startSearch(Context context) {
        if (context != null) {
            try {
                Intent intent = new Intent();
                intent.addFlags(268435456);
                intent.setComponent(new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.EasyProjection"));
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "Airsharing not found.");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void executeReportEvent(final Context context, String type) {
        mOpType = type;
        new Handler().post(new Runnable() {
            /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                DecisionUtil.bindService(context, DecisionUtil.REPORT_TYPE);
            }
        });
    }

    public static void executeEnterProjectionEvent(final Context context) {
        new Handler().post(new Runnable() {
            /* class com.huawei.server.pc.decision.DecisionUtil.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                DecisionUtil.bindService(context, DecisionUtil.ENTER_PROJECTION_TYPE);
            }
        });
    }
}
