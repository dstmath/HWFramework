package com.huawei.trustcircle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.trustcircle.IEnhancedCircleListener;
import com.huawei.trustcircle.ITrustCircleService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class EnhancedCircleManager {
    private static final String ADAPTER_SERVICE_CLASS_NAME = "com.huawei.trustcircle.services.HwTrustCircleAdapterService";
    private static final int DEFAULT_SET_SIZE = 10;
    public static final int ERROR_CODE_CANCEL = 14;
    public static final int ERROR_CODE_CHOOSE_OTHER_WAY = 15;
    public static final int ERROR_CODE_COMMON = 13;
    public static final int ERROR_CODE_FAILED = 9;
    private static final int ERROR_CODE_FOR_START_ACTIVITY = 10001;
    public static final int ERROR_CODE_INVALID_PARAMS = 8;
    public static final int ERROR_CODE_IN_CIRCLE = 6;
    public static final int ERROR_CODE_LOCKED = 12;
    public static final int ERROR_CODE_NOT_IN_CIRCLE = 7;
    public static final int ERROR_CODE_NOT_LOGIN = 4;
    public static final int ERROR_CODE_NOT_OWNER = 2;
    public static final int ERROR_CODE_NOT_SUPPORT = 10;
    public static final int ERROR_CODE_NO_NETWORK = 5;
    public static final int ERROR_CODE_NO_PWD = 3;
    public static final int ERROR_CODE_OK = 1;
    public static final int ERROR_CODE_REMOTE_ERROR = -2;
    public static final int ERROR_CODE_SERVICE_DISCONNECTED = -3;
    public static final int ERROR_CODE_SERVICE_NOT_FOUND = -1;
    public static final int ERROR_CODE_VERIFY_CODE_NOT_RECEIVED = 11;
    private static final String HW_SIGNATURE_OR_SYSTEM = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final boolean IS_FEATURE_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.support_etcis", false);
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_ALLOW_TCIS_TOKEN = "allow_tcis_token";
    public static final String KEY_AUTH_RES = "auth_result";
    private static final String KEY_BUNDLE_DATA = "bundle_data";
    public static final String KEY_CALLING_PACKAGE_NAME = "calling_package_name";
    public static final String KEY_CIPHER_TEXT = "cipher_text";
    public static final String KEY_COUNTRY_CODE = "country_code";
    public static final String KEY_DERIVED_KEY = "derived_key";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_ERROR_CODE = "error_code";
    public static final String KEY_ERROR_MSG = "error_message";
    public static final String KEY_IS_ACTIVITY_CONTEXT = "is_activity_context";
    private static final String KEY_METHOD_NAME = "method_name";
    public static final String KEY_MK_VERSION = "mk_version";
    public static final String KEY_NONCE = "nonce";
    public static final String KEY_PATTERN_HASH = "pattern_hash";
    public static final String KEY_PLAIN_TEXT = "plain_text";
    public static final String KEY_SCENE_ID = "scene_id";
    public static final String KEY_UNWRAPPED_DATA = "unwrapped_data";
    public static final String KEY_UP_SITE_ID = "up_site_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_VERIFY_TDMID = "verify_tdmid";
    public static final String KEY_VERIFY_TDMSNID = "verify_tdmsnid";
    public static final String KEY_WRAPPED_DATA = "wrapped_data";
    public static final int LOCK_PATTERN_IN_CIRCLE_CANCEL = 2;
    public static final int LOCK_PATTERN_IN_CIRCLE_NEVER_CANCEL = 1;
    public static final int LOCK_PATTERN_NOT_IN_CIRCLE = 0;
    private static final String METHOD_LOCK_PATTERN_CHANGED = "lock_pattern_changed";
    private static final String METHOD_SERVICE_CONNECTION = "verify_trust_circle_service";
    private static final String NORMAL_PERMISSION_ACTIVITY_CLASS_NAME = "com.huawei.trustcircle.otherentrance.ControlAppMsgActivity";
    public static final int SCENE_ID_CIRCLE_VERIFY = 6;
    public static final int SCENE_ID_LOCAL_VERIFY = 5;
    public static final int SCENE_ID_NORMAL = 1;
    public static final int SCENE_ID_OTHER_APP = 2;
    public static final int SCENE_ID_OTHER_AUTH_START = 3;
    private static final String SERVICE_CLASS_NAME = "com.huawei.trustcircle.services.HwTrustCircleService";
    private static final String SERVICE_PACKAGE = "com.huawei.trustcircle";
    private static final String SYSTEM_PERMISSION_ACTIVITY_CLASS_NAME = "com.huawei.trustcircle.otherentrance.GetOtherAppMsgActivity";
    private static final String TAG = "EnhancedCircleManager";
    private static volatile EnhancedCircleManager sInstance;
    private volatile Activity mActivity;
    private EnhancedCircleClient mCircleClient;
    private final Context mContext;
    private final Set<ICircleConnection> mICircleConnections = new HashSet(10);
    private boolean mIsBindingService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.trustcircle.EnhancedCircleManager.AnonymousClass1 */

        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.i(EnhancedCircleManager.TAG, "service connected!");
            if (binder == null) {
                EnhancedCircleManager.this.connectClient(null);
                return;
            }
            EnhancedCircleManager.this.mTrustCircleService = ITrustCircleService.Stub.asInterface(binder);
            EnhancedCircleManager enhancedCircleManager = EnhancedCircleManager.this;
            enhancedCircleManager.mCircleClient = new EnhancedCircleClient(enhancedCircleManager.mContext, EnhancedCircleManager.this.mTrustCircleService, EnhancedCircleManager.this.isWhiteListCheckNeeded());
            if (!EnhancedCircleManager.this.isWhiteListCheckNeeded()) {
                EnhancedCircleManager enhancedCircleManager2 = EnhancedCircleManager.this;
                enhancedCircleManager2.connectClient(enhancedCircleManager2.mCircleClient);
                return;
            }
            Bundle data = new Bundle();
            if (EnhancedCircleManager.this.mContext != null) {
                data.putString(EnhancedCircleManager.KEY_CALLING_PACKAGE_NAME, EnhancedCircleManager.this.mContext.getPackageName());
            }
            data.putString(EnhancedCircleManager.KEY_METHOD_NAME, EnhancedCircleManager.METHOD_SERVICE_CONNECTION);
            try {
                EnhancedCircleManager.this.mTrustCircleService.callAsync(data, new IEnhancedCircleListener.Stub() {
                    /* class com.huawei.trustcircle.EnhancedCircleManager.AnonymousClass1.AnonymousClass1 */

                    public void onResult(Bundle bundle) {
                        Log.i(EnhancedCircleManager.TAG, "onResult");
                        if (bundle == null || bundle.getInt(EnhancedCircleManager.KEY_ERROR_CODE) != 1) {
                            Log.w(EnhancedCircleManager.TAG, "return data error");
                            EnhancedCircleManager.this.connectClient(null);
                            return;
                        }
                        EnhancedCircleManager.this.connectClient(EnhancedCircleManager.this.mCircleClient);
                    }

                    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.trustcircle.EnhancedCircleManager$1$1 */
                    /* JADX WARN: Multi-variable type inference failed */
                    public IBinder asBinder() {
                        return this;
                    }
                });
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "callAsync exception");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(EnhancedCircleManager.TAG, "service disconnected!");
            synchronized (EnhancedCircleManager.this.mICircleConnections) {
                EnhancedCircleManager.this.mCircleClient = null;
                EnhancedCircleManager.this.mIsBindingService = false;
                EnhancedCircleManager.this.mTrustCircleService = null;
                for (ICircleConnection connection : EnhancedCircleManager.this.mICircleConnections) {
                    connection.onDisconnected(-3);
                }
                EnhancedCircleManager.this.mICircleConnections.clear();
                Log.i(EnhancedCircleManager.TAG, "onServiceDisconnected: count = " + EnhancedCircleManager.this.mICircleConnections.size());
            }
        }
    };
    private ITrustCircleService mTrustCircleService;

    public interface ICircleConnection {
        void onConnected(EnhancedCircleClient enhancedCircleClient);

        void onDisconnected(int i);
    }

    public static abstract class IResultListener {
        public abstract void onResult(Bundle bundle);
    }

    private EnhancedCircleManager(Context context) {
        this.mContext = context != null ? context.getApplicationContext() : null;
    }

    public static EnhancedCircleManager getInstance(Context context) {
        EnhancedCircleManager enhancedCircleManager;
        synchronized (EnhancedCircleManager.class) {
            if (sInstance == null) {
                sInstance = new EnhancedCircleManager(context);
                Log.i(TAG, "getInstance: feature is supported ? " + IS_FEATURE_SUPPORTED);
            }
            if (context instanceof Activity) {
                sInstance.mActivity = (Activity) context;
            }
            enhancedCircleManager = sInstance;
        }
        return enhancedCircleManager;
    }

    public boolean isFeatureSupported() {
        return IS_FEATURE_SUPPORTED;
    }

    public void connect(ICircleConnection connection) {
        Log.i(TAG, "connect start");
        if (connection == null) {
            throw new IllegalArgumentException("ICircleConnection cannot be null!");
        } else if (!isFeatureSupported()) {
            connection.onConnected(null);
        } else if (this.mContext != null) {
            synchronized (this.mICircleConnections) {
                this.mICircleConnections.add(connection);
                Log.i(TAG, "connect: count = " + this.mICircleConnections.size());
                if (this.mCircleClient != null) {
                    connection.onConnected(this.mCircleClient);
                    return;
                }
                if (!this.mIsBindingService) {
                    if (!isWhiteListCheckNeeded()) {
                        this.mContext.bindService(newServiceIntent(), this.mServiceConnection, 1);
                    } else {
                        this.mContext.bindService(newAdapterServiceIntent(), this.mServiceConnection, 1);
                    }
                    this.mIsBindingService = true;
                }
            }
        } else {
            throw new IllegalArgumentException("context cannot be null!");
        }
    }

    public void disconnect(ICircleConnection connection) {
        Log.i(TAG, "disconnect start");
        if (connection == null) {
            throw new IllegalArgumentException("ICircleConnection cannot be null!");
        } else if (!isFeatureSupported()) {
            this.mActivity = null;
        } else if (this.mContext != null) {
            synchronized (this.mICircleConnections) {
                this.mICircleConnections.remove(connection);
                Log.i(TAG, "disconnect: count = " + this.mICircleConnections.size());
                if (this.mICircleConnections.isEmpty() && this.mTrustCircleService != null) {
                    this.mContext.unbindService(this.mServiceConnection);
                    this.mTrustCircleService = null;
                    this.mCircleClient = null;
                    this.mIsBindingService = false;
                    this.mActivity = null;
                }
            }
        } else {
            throw new IllegalArgumentException("context cannot be null!");
        }
    }

    public void notifyLockPatternChanged(byte[] patternHash) {
        Log.i(TAG, "notifyLockPatternChanged");
        if (this.mContext != null) {
            final byte[] hashCopy = patternHash != null ? Arrays.copyOf(patternHash, patternHash.length) : null;
            connect(new ICircleConnection() {
                /* class com.huawei.trustcircle.EnhancedCircleManager.AnonymousClass2 */

                @Override // com.huawei.trustcircle.EnhancedCircleManager.ICircleConnection
                public void onConnected(EnhancedCircleClient circleClient) {
                    Log.i(EnhancedCircleManager.TAG, "onConnected start");
                    if (circleClient == null) {
                        Log.w(EnhancedCircleManager.TAG, "ETCIS client is null!");
                        return;
                    }
                    Log.i(EnhancedCircleManager.TAG, "onConnected: start call");
                    Bundle changeData = new Bundle();
                    changeData.putString(EnhancedCircleManager.KEY_METHOD_NAME, EnhancedCircleManager.METHOD_LOCK_PATTERN_CHANGED);
                    changeData.putByteArray(EnhancedCircleManager.KEY_PATTERN_HASH, hashCopy);
                    circleClient.callAsync(changeData, new IResultListener() {
                        /* class com.huawei.trustcircle.EnhancedCircleManager.AnonymousClass2.AnonymousClass1 */

                        @Override // com.huawei.trustcircle.EnhancedCircleManager.IResultListener
                        public void onResult(Bundle result) {
                            EnhancedCircleManager.this.disconnect(this);
                        }
                    });
                }

                @Override // com.huawei.trustcircle.EnhancedCircleManager.ICircleConnection
                public void onDisconnected(int reason) {
                    Log.w(EnhancedCircleManager.TAG, "ETCIS disconnected : " + reason);
                }
            });
            return;
        }
        throw new IllegalArgumentException("context cannot be null!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectClient(EnhancedCircleClient circleClient) {
        synchronized (this.mICircleConnections) {
            Log.i(TAG, "mICircleConnections count = " + this.mICircleConnections.size());
            this.mIsBindingService = false;
            for (ICircleConnection connection : this.mICircleConnections) {
                connection.onConnected(circleClient);
            }
            if (circleClient == null) {
                this.mICircleConnections.clear();
            }
        }
    }

    private Intent newServiceIntent() {
        Intent service = new Intent();
        service.setPackage(SERVICE_PACKAGE);
        service.setClassName(SERVICE_PACKAGE, SERVICE_CLASS_NAME);
        return service;
    }

    private Intent newAdapterServiceIntent() {
        Intent service = new Intent();
        service.setPackage(SERVICE_PACKAGE);
        service.setClassName(SERVICE_PACKAGE, ADAPTER_SERVICE_CLASS_NAME);
        return service;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWhiteListCheckNeeded() {
        return this.mContext.getPackageManager().checkPermission(HW_SIGNATURE_OR_SYSTEM, this.mContext.getPackageName()) != 0;
    }

    private Bundle newBundleWithResult(int result) {
        Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_ERROR_CODE, result);
        return resultBundle;
    }

    public static class EnhancedCircleClient {
        private Context mContext;
        private boolean mIsNormalPermission;
        private ITrustCircleService mService;

        private EnhancedCircleClient(Context context, ITrustCircleService service, boolean isNormal) {
            this.mService = service;
            this.mContext = context;
            this.mIsNormalPermission = isNormal;
        }

        public void requestEnhancedRegStatus(Bundle reqParams, IResultListener resultListener) {
            Log.i(EnhancedCircleManager.TAG, "requestStatus");
            if (resultListener != null) {
                ITrustCircleService service = this.mService;
                if (service == null || this.mContext == null) {
                    resultListener.onResult(newBundleWithResult(-1));
                    return;
                }
                Bundle params = copyFromBundle(reqParams);
                try {
                    params.putString(EnhancedCircleManager.KEY_CALLING_PACKAGE_NAME, this.mContext.getPackageName());
                    service.requestStatus(params, newListener(resultListener));
                } catch (RemoteException e) {
                    Log.w(EnhancedCircleManager.TAG, "requestStatus failed!");
                    resultListener.onResult(newBundleWithResult(-2));
                }
            }
        }

        public void createOrJoinCircle(Bundle reqParams, final IResultListener resultListener) {
            Log.i(EnhancedCircleManager.TAG, "createOrJoinCircle");
            if (resultListener != null) {
                ITrustCircleService service = this.mService;
                if (service == null || this.mContext == null) {
                    resultListener.onResult(newBundleWithResult(-1));
                    return;
                }
                IEnhancedCircleListener callback = new IEnhancedCircleListener.Stub() {
                    /* class com.huawei.trustcircle.EnhancedCircleManager.EnhancedCircleClient.AnonymousClass1 */

                    public void onResult(Bundle result) {
                        if (result == null || result.getInt(EnhancedCircleManager.KEY_ERROR_CODE) != EnhancedCircleManager.ERROR_CODE_FOR_START_ACTIVITY) {
                            resultListener.onResult(result);
                            return;
                        }
                        Log.i(EnhancedCircleManager.TAG, "start activity for third party");
                        EnhancedCircleClient.this.startTcActivity();
                    }

                    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.trustcircle.EnhancedCircleManager$EnhancedCircleClient$1 */
                    /* JADX WARN: Multi-variable type inference failed */
                    public IBinder asBinder() {
                        return this;
                    }
                };
                Bundle params = copyFromBundle(reqParams);
                try {
                    params.putString(EnhancedCircleManager.KEY_CALLING_PACKAGE_NAME, this.mContext.getPackageName());
                    if (EnhancedCircleManager.sInstance.mActivity != null) {
                        params.putBoolean(EnhancedCircleManager.KEY_IS_ACTIVITY_CONTEXT, true);
                    }
                    service.createOrJoinCircle(params, callback);
                } catch (RemoteException e) {
                    Log.w(EnhancedCircleManager.TAG, "createOrJoinCircle failed!");
                    resultListener.onResult(newBundleWithResult(-2));
                }
            }
        }

        public void remoteAuth(Bundle reqParams, IResultListener resultListener) {
            Log.i(EnhancedCircleManager.TAG, "remoteAuth");
            if (resultListener != null) {
                if (this.mService == null) {
                    resultListener.onResult(newBundleWithResult(-1));
                    return;
                }
                try {
                    this.mService.remoteAuth(reqParams, newListener(resultListener));
                } catch (RemoteException e) {
                    Log.w(EnhancedCircleManager.TAG, "remoteAuth failed!");
                    resultListener.onResult(newBundleWithResult(-2));
                }
            }
        }

        public Bundle wrapData(byte[] plainText, String packageName) {
            Log.i(EnhancedCircleManager.TAG, "wrapData");
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                return newBundleWithResult(-1);
            }
            try {
                return iTrustCircleService.wrapData(plainText, packageName);
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "wrapData failed!");
                return newBundleWithResult(-2);
            }
        }

        public Bundle unwrapData(Bundle wrappedData, String packageName) {
            Log.i(EnhancedCircleManager.TAG, "unwrapData");
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                return newBundleWithResult(-1);
            }
            try {
                return iTrustCircleService.unwrapData(wrappedData, packageName);
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "unwrapData failed!");
                return newBundleWithResult(-2);
            }
        }

        public Bundle secureDerive(int keyType, byte[] deriveFactor, String packageName) {
            Log.i(EnhancedCircleManager.TAG, "secureDerive");
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                return newBundleWithResult(-1);
            }
            try {
                return iTrustCircleService.secureDerive(keyType, deriveFactor, packageName);
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "secureDerive failed!");
                return newBundleWithResult(-2);
            }
        }

        public int getLockPatternStatus(String accountId) {
            Log.i(EnhancedCircleManager.TAG, "getLockPatternStatus");
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                return -1;
            }
            try {
                return iTrustCircleService.getLockPatternStatus(accountId);
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "getLockPatternStatus failed!");
                return -2;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startTcActivity() {
            if (EnhancedCircleManager.sInstance.mActivity == null) {
                Log.e(EnhancedCircleManager.TAG, "startTcActivity: failed activity null");
                return;
            }
            Intent intent = new Intent();
            try {
                if (this.mIsNormalPermission) {
                    Log.d(EnhancedCircleManager.TAG, "normal permission");
                    intent.setClassName(EnhancedCircleManager.SERVICE_PACKAGE, EnhancedCircleManager.NORMAL_PERMISSION_ACTIVITY_CLASS_NAME);
                    Bundle bundle = new Bundle();
                    bundle.putString(EnhancedCircleManager.KEY_CALLING_PACKAGE_NAME, this.mContext.getPackageName());
                    intent.putExtra(EnhancedCircleManager.KEY_BUNDLE_DATA, bundle);
                } else {
                    Log.d(EnhancedCircleManager.TAG, "system or signature permission");
                    intent.setClassName(EnhancedCircleManager.SERVICE_PACKAGE, EnhancedCircleManager.SYSTEM_PERMISSION_ACTIVITY_CLASS_NAME);
                }
                EnhancedCircleManager.sInstance.mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(EnhancedCircleManager.TAG, "startTcActivity ActivityNotFound.");
            } catch (SecurityException e2) {
                Log.e(EnhancedCircleManager.TAG, "startTcActivity SecurityException.");
            }
        }

        private Bundle call(Bundle reqParams) {
            Log.i(EnhancedCircleManager.TAG, "inner call");
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                return newBundleWithResult(-1);
            }
            try {
                return iTrustCircleService.call(reqParams);
            } catch (RemoteException e) {
                Log.w(EnhancedCircleManager.TAG, "call failed!");
                return newBundleWithResult(-2);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void callAsync(Bundle reqParams, IResultListener resultListener) {
            Log.i(EnhancedCircleManager.TAG, "inner callAsync");
            if (resultListener == null) {
                Log.e(EnhancedCircleManager.TAG, "inner callAsync: listener is null!");
                return;
            }
            ITrustCircleService iTrustCircleService = this.mService;
            if (iTrustCircleService == null) {
                resultListener.onResult(newBundleWithResult(-1));
                return;
            }
            try {
                iTrustCircleService.callAsync(reqParams, newListener(resultListener));
            } catch (RemoteException e) {
                Log.e(EnhancedCircleManager.TAG, "inner callAsync: remote error");
                resultListener.onResult(newBundleWithResult(-2));
            }
        }

        private IEnhancedCircleListener newListener(final IResultListener resultListener) {
            return new IEnhancedCircleListener.Stub() {
                /* class com.huawei.trustcircle.EnhancedCircleManager.EnhancedCircleClient.AnonymousClass2 */

                public void onResult(Bundle status) throws RemoteException {
                    if (resultListener != null) {
                        Log.i(EnhancedCircleManager.TAG, "onResult");
                        resultListener.onResult(status);
                    }
                }

                /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.trustcircle.EnhancedCircleManager$EnhancedCircleClient$2 */
                /* JADX WARN: Multi-variable type inference failed */
                public IBinder asBinder() {
                    return this;
                }
            };
        }

        private Bundle newBundleWithResult(int result) {
            Bundle resultBundle = new Bundle();
            resultBundle.putInt(EnhancedCircleManager.KEY_ERROR_CODE, result);
            return resultBundle;
        }

        private Bundle copyFromBundle(Bundle other) {
            Bundle bundle;
            if (other != null) {
                bundle = new Bundle(other);
            }
            return bundle;
        }
    }
}
