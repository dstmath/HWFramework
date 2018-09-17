package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Slog;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodManager.Stub;
import com.android.internal.view.InputBindResult;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wifipro.WifiProCHRManager;
import huawei.android.view.inputmethod.HwSecImmHelper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HwInputMethodManagerService extends InputMethodManagerService {
    public static final String ACTION_SECURE_IME = "com.huawei.secime.SoftKeyboard";
    private static final Locale CHINA_LOCALE = new Locale("zh");
    private static final String DESCRIPTOR = "android.view.inputmethod.InputMethodManager";
    private static final Locale ENGLISH_LOCALE = new Locale("en");
    public static final int FLAG_SHOW_INPUT = 65536;
    private static final String INPUT_METHOD_ENABLED_FILE = "bflag";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final int SECURE_IME_NO_HIDE_FLAG = 4096;
    public static final String SECURE_IME_PACKAGENAME = "com.huawei.secime";
    public static final String SECURITY_INPUT_METHOD_ID = "com.huawei.secime/.SoftKeyboard";
    public static final String SECURITY_INPUT_SERVICE_NAME = "input_method_secure";
    public static final String SETTINGS_SECURE_KEYBOARD_CONTROL = "secure_keyboard";
    static final String TAG = "HwInputMethodManagerService";
    private static final int TRANSACTION_isUseSecureIME = 1001;
    private static final int UNBIND_SECIME_IF_SHOULD = 10000;
    private static final boolean isSupportedSecIme = IS_CHINA_AREA;
    private boolean isSecMethodUsing = false;
    private boolean isSecureIMEEnabled = false;
    private boolean isSecureIMEExist = false;
    private String mCurInputId;
    private SecureSettingsObserver mSecureSettingsObserver;
    private IInputMethodManager mSecurityInputMethodService;
    private String mWriteInputId = "com.visionobjects.stylusmobile.v3_2_huawei/com.visionobjects.stylusmobile.v3_2.StylusIMService";

    private class SecureSettingsObserver extends ContentObserver {
        boolean mRegistered = false;
        int mUserId;

        public SecureSettingsObserver() {
            super(new Handler());
        }

        public void registerContentObserverInner(int userId) {
            Slog.d(HwInputMethodManagerService.TAG, "SecureSettingsObserver mRegistered=" + this.mRegistered + " new user=" + userId + " current user=" + this.mUserId);
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = HwInputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    resolver.unregisterContentObserver(this);
                    this.mRegistered = false;
                }
                this.mUserId = userId;
                resolver.registerContentObserver(Secure.getUriFor("secure_keyboard"), false, this, userId);
                this.mRegistered = true;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                Slog.i(HwInputMethodManagerService.TAG, "SecureSettingsObserver onChange, uri = " + uri.toString());
                if (Secure.getUriFor("secure_keyboard").equals(uri)) {
                    HwInputMethodManagerService.this.isSecureIMEEnabled = HwInputMethodManagerService.this.isSecureIMEEnable();
                    Slog.i(HwInputMethodManagerService.TAG, "isSecureIMEEnabled = " + HwInputMethodManagerService.this.isSecureIMEEnabled);
                }
            }
        }
    }

    public HwInputMethodManagerService(Context context) {
        super(context);
    }

    protected void createFlagIfNecessary(int newUserId) {
        File flagFile = new File(Environment.getUserSystemDirectory(newUserId), INPUT_METHOD_ENABLED_FILE);
        boolean existFlag = false;
        try {
            if (flagFile.exists()) {
                existFlag = true;
            } else {
                flagFile.createNewFile();
            }
            synchronized (this.mEnabledFileMap) {
                this.mEnabledFileMap.put(String.valueOf(newUserId), Boolean.valueOf(existFlag));
            }
        } catch (IOException e) {
            Slog.e(TAG, "Unable to create flag file!");
        }
    }

    protected boolean isFlagExists(int userId) {
        boolean value;
        synchronized (this.mEnabledFileMap) {
            value = ((Boolean) this.mEnabledFileMap.get(String.valueOf(userId))).booleanValue();
        }
        Slog.i(TAG, "isFlagExists  value = " + value + ", userId = " + userId);
        return value;
    }

    protected void ensureEnableSystemIME(String id, InputMethodInfo p, Context context, int userId) {
        if (!isFlagExists(userId)) {
            if (isValidSystemDefaultIme(p, context) || isSystemImeThatHasEnglishSubtype(p) || isSystemImeThatHasChinaSubtype(p)) {
                Slog.i(TAG, "ensureEnableSystemIME will setInputMethodEnabledLocked");
                setInputMethodEnabledLocked(id, true);
            }
        }
    }

    private static boolean isSystemIme(InputMethodInfo inputMethod) {
        return (inputMethod.getServiceInfo().applicationInfo.flags & 1) != 0;
    }

    public void setPanWriteInputEnable(boolean isWriteInput) {
        synchronized (this.mMethodMap) {
            if (this.mSettings.getIsWriteInputEnable()) {
                if (isWriteInput) {
                    String id = this.mSettings.getSelectedInputMethod();
                    if (!(id == null || (id.equals(this.mWriteInputId) ^ 1) == 0)) {
                        this.mCurInputId = id;
                    }
                    setInputMethodLocked(this.mWriteInputId, this.mSettings.getSelectedInputMethodSubtypeId(this.mWriteInputId));
                } else {
                    if (this.mCurInputId == null) {
                        this.mCurInputId = this.mSettings.getSelectedInputMethod();
                    }
                    setInputMethodLocked(this.mCurInputId, this.mSettings.getSelectedInputMethodSubtypeId(this.mCurInputId));
                }
            }
        }
    }

    private boolean isValidSystemDefaultIme(InputMethodInfo imi, Context context) {
        if (!this.mSystemReady || !isSystemIme(imi)) {
            return false;
        }
        if (imi.getIsDefaultResourceId() != 0) {
            try {
                if (context.createPackageContext(imi.getPackageName(), 0).getResources().getBoolean(imi.getIsDefaultResourceId()) && containsSubtypeOf(imi, context.getResources().getConfiguration().locale.getLanguage())) {
                    return true;
                }
            } catch (NameNotFoundException e) {
            } catch (NotFoundException e2) {
            }
        }
        if (imi.getSubtypeCount() == 0) {
            Slog.w(TAG, "Found no subtypes in a system IME: " + imi.getPackageName());
        }
        return false;
    }

    private static boolean isSystemImeThatHasEnglishSubtype(InputMethodInfo imi) {
        if (isSystemIme(imi)) {
            return containsSubtypeOf(imi, ENGLISH_LOCALE.getLanguage());
        }
        return false;
    }

    private static boolean isSystemImeThatHasChinaSubtype(InputMethodInfo imi) {
        if (isSystemIme(imi)) {
            return containsSubtypeOf(imi, CHINA_LOCALE.getLanguage());
        }
        return false;
    }

    private static boolean containsSubtypeOf(InputMethodInfo imi, String language) {
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            if (imi.getSubtypeAt(i).getLocale().startsWith(language)) {
                return true;
            }
        }
        return false;
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        if (isSupportedSecIme) {
            this.mSecureSettingsObserver = new SecureSettingsObserver();
            this.mSecureSettingsObserver.registerContentObserverInner(this.mSettings.getCurrentUserId());
            this.isSecureIMEEnabled = isSecureIMEEnable();
            this.isSecureIMEExist = existSecureIME();
            Slog.i(TAG, "systemRunning isSecureIMEEnabled = " + this.isSecureIMEEnabled + ", isSecureIMEExist = " + this.isSecureIMEExist);
        }
        super.systemRunning(statusBar);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                data.enforceInterface(DESCRIPTOR);
                boolean result = isUseSecureIME();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private IInputMethodManager getSecurityInputMethodService() {
        if (this.mSecurityInputMethodService == null) {
            this.mSecurityInputMethodService = Stub.asInterface(ServiceManager.getService(SECURITY_INPUT_SERVICE_NAME));
        }
        return this.mSecurityInputMethodService;
    }

    private boolean isSecureIMEEnable() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "secure_keyboard", 1, this.mSettings.getCurrentUserId()) == 1;
    }

    private boolean isUseSecureIME() {
        if (this.isSecureIMEExist && this.isSecureIMEEnabled && isSupportedSecIme && getSecurityInputMethodService() != null) {
            return true;
        }
        return false;
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int uid, int pid) {
        super.addClient(client, inputContext, uid, pid);
        if (getSecurityInputMethodService() != null) {
            try {
                this.mSecurityInputMethodService.addClient(client, inputContext, uid, pid);
            } catch (RemoteException e) {
            }
        }
    }

    public void removeClient(IInputMethodClient client) {
        super.removeClient(client);
        if (getSecurityInputMethodService() != null) {
            try {
                this.mSecurityInputMethodService.removeClient(client);
            } catch (RemoteException e) {
            }
        }
    }

    public void setImeWindowStatus(IBinder token, IBinder startInputToken, int vis, int backDisposition) {
        if (!isUseSecureIME() || (token != null && this.mCurToken == token)) {
            super.setImeWindowStatus(token, startInputToken, vis, backDisposition);
            return;
        }
        try {
            this.mSecurityInputMethodService.setImeWindowStatus(token, startInputToken, vis, backDisposition);
        } catch (RemoteException e) {
            Slog.e(TAG, "setImeWindowStatus, remote exception");
        }
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken == null) {
            Slog.d(TAG, "------------startInput--------------");
            if (isUseSecureIME()) {
                boolean isHide;
                if (isPasswordType(attribute)) {
                    Slog.d(TAG, "isPasswordType(attribute) == true, using SecurityIMMS");
                    if (this.mInputShown) {
                        isHide = super.hideSoftInput(client, 0, null);
                        Slog.d(TAG, "hideNormalSoftInput = " + isHide);
                        if (isHide) {
                            controlFlags |= 65536;
                        }
                    }
                    this.isSecMethodUsing = true;
                    try {
                        return this.mSecurityInputMethodService.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
                    } catch (RemoteException e) {
                    }
                } else {
                    isHide = false;
                    try {
                        isHide = this.mSecurityInputMethodService.hideSoftInput(client, 0, null);
                    } catch (RemoteException e2) {
                    }
                    Slog.d(TAG, "hideSecSoftInput = " + isHide);
                    if (isHide) {
                        controlFlags |= 65536;
                    }
                    this.isSecMethodUsing = false;
                }
            }
        } else if (isUseSecureIME()) {
            if (isPasswordType(attribute)) {
                Slog.d(TAG, "windowGainedFocus  isPasswordType(attribute) = true");
                this.isSecMethodUsing = true;
                super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, 1, windowFlags, attribute, inputContext, missingMethods);
                super.hideSoftInput(client, 0, null);
                try {
                    return this.mSecurityInputMethodService.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
                } catch (RemoteException e3) {
                }
            } else {
                this.isSecMethodUsing = false;
                try {
                    this.mSecurityInputMethodService.hideSoftInput(client, 0, null);
                    InputBindResult inputBindRes = super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
                    this.mSecurityInputMethodService.startInputOrWindowGainedFocus(10000, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
                    return inputBindRes;
                } catch (RemoteException e4) {
                }
            }
        }
        return super.startInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
    }

    public void setInputMethod(IBinder token, String id) {
        if (!SECURITY_INPUT_METHOD_ID.equals(id)) {
            super.setInputMethod(token, id);
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        if (!isSupportedSecIme || !SECURITY_INPUT_METHOD_ID.equals(id)) {
            super.setInputMethodAndSubtype(token, id, subtype);
        }
    }

    boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        if (isSupportedSecIme && SECURITY_INPUT_METHOD_ID.equals(id)) {
            return false;
        }
        return super.setInputMethodEnabledLocked(id, enabled);
    }

    void setInputMethodLocked(String id, int subtypeId) {
        if (!isSupportedSecIme || !SECURITY_INPUT_METHOD_ID.equals(id)) {
            super.setInputMethodLocked(id, subtypeId);
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        if (isUseSecureIME() && this.isSecMethodUsing) {
            Slog.w(TAG, "SecIME is using, don't show the input method chooser dialog!");
        } else {
            super.showInputMethodPickerFromClient(client, auxiliarySubtypeMode);
        }
    }

    public void hideMySoftInput(IBinder token, int flags) {
        if (!isUseSecureIME() || (token != null && this.mCurToken == token)) {
            super.hideMySoftInput(token, flags);
        } else {
            try {
                this.mSecurityInputMethodService.hideMySoftInput(token, flags);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (isUseSecureIME() && this.isSecMethodUsing) {
            try {
                hideSoftInput(client, 4096, resultReceiver);
                return this.mSecurityInputMethodService.showSoftInput(client, flags, resultReceiver);
            } catch (RemoteException e) {
            }
        }
        return super.showSoftInput(client, flags, resultReceiver);
    }

    public void showMySoftInput(IBinder token, int flags) {
        if (!isUseSecureIME() || (token != null && this.mCurToken == token)) {
            super.showMySoftInput(token, flags);
        } else {
            try {
                this.mSecurityInputMethodService.showMySoftInput(token, flags);
            } catch (RemoteException e) {
            }
        }
    }

    private boolean isPasswordType(EditorInfo attribute) {
        if (attribute != null) {
            return HwSecImmHelper.isPasswordInputType(attribute.inputType, true);
        }
        if (this.isSecMethodUsing) {
            return true;
        }
        return false;
    }

    protected boolean isSecureIME(String packageName) {
        if (SECURE_IME_PACKAGENAME.equals(packageName)) {
            return true;
        }
        return false;
    }

    protected boolean shouldBuildInputMethodList(String packageName) {
        if (isSupportedSecIme && SECURE_IME_PACKAGENAME.equals(packageName)) {
            return false;
        }
        return true;
    }

    protected void updateSecureIMEStatus() {
        this.isSecureIMEExist = existSecureIME();
        Slog.i(TAG, "isSecureIMEExist = " + this.isSecureIMEExist);
    }

    private boolean existSecureIME() {
        List<ResolveInfo> packages = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent(ACTION_SECURE_IME), 0, this.mSettings.getCurrentUserId());
        if (packages == null || packages.size() <= 0) {
            return false;
        }
        return true;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (isUseSecureIME() && this.isSecMethodUsing) {
            if (4096 != flags) {
                try {
                    this.mSecurityInputMethodService.hideSoftInput(client, flags, resultReceiver);
                } catch (RemoteException e) {
                }
            } else {
                flags = 0;
            }
        }
        return super.hideSoftInput(client, flags, resultReceiver);
    }

    protected void switchUserExtra(int userId) {
        if (isSupportedSecIme) {
            if (this.mSecureSettingsObserver != null) {
                this.mSecureSettingsObserver.registerContentObserverInner(userId);
            }
            updateSecureIMEStatus();
            this.isSecureIMEEnabled = isSecureIMEEnable();
            Slog.i(TAG, "isSecureIMEEnabled = " + this.isSecureIMEEnabled);
            if (isUseSecureIME()) {
                HwSecureInputMethodManagerInternal mLocalServices = (HwSecureInputMethodManagerInternal) LocalServices.getService(HwSecureInputMethodManagerInternal.class);
                if (mLocalServices != null) {
                    mLocalServices.setClientActiveFlag();
                } else {
                    Slog.w(TAG, "HwSecureInputMethodManagerInternal is not exist !");
                }
            }
        }
    }

    protected int getNaviBarEnabledDefValue() {
        int defValue;
        boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
        int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
        if (FRONT_FINGERPRINT_NAVIGATION) {
            boolean isTrikeyExist = isTrikeyExist();
            if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && isTrikeyExist) {
                defValue = 0;
            } else if (SystemProperties.get("ro.config.hw_optb", "0").equals("156")) {
                defValue = 0;
            } else {
                defValue = 1;
            }
        } else {
            defValue = 1;
        }
        Slog.i(TAG, "NaviBar defValue = " + defValue);
        return defValue;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0049 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.ClassNotFoundException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0049 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.ClassNotFoundException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0049 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.ClassNotFoundException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0049 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.ClassNotFoundException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0049 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.ClassNotFoundException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0049, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004a, code:
            android.util.Slog.e(TAG, "isTrikeyExist, reflect method handle, and has exception: " + r1);
     */
    /* JADX WARNING: Missing block: B:8:?, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isTrikeyExist() {
        boolean ret = false;
        try {
            Class clazz = Class.forName("huawei.android.os.HwGeneralManager");
            return ((Boolean) clazz.getDeclaredMethod("isSupportTrikey", null).invoke(clazz.getDeclaredMethod(WifiProCHRManager.LOG_GET_INSTANCE_API_NAME, null).invoke(clazz, (Object[]) null), (Object[]) null)).booleanValue();
        } catch (Exception e) {
        } catch (Exception ex) {
            Slog.e(TAG, "isTrikeyExist, other exception: " + ex);
            return ret;
        }
    }

    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        if (!isUseSecureIME() || (token != null && this.mCurToken == token)) {
            super.reportFullscreenMode(token, fullscreen);
            return;
        }
        try {
            this.mSecurityInputMethodService.reportFullscreenMode(token, fullscreen);
        } catch (RemoteException e) {
            Slog.e(TAG, "reportFullscreenMode, remote exception");
        }
    }
}
