package com.unionpay.tsmservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import com.unionpay.tsmservice.ITsmActivityCallback;
import com.unionpay.tsmservice.ITsmCallback;
import com.unionpay.tsmservice.ITsmService;
import com.unionpay.tsmservice.data.Amount;
import com.unionpay.tsmservice.data.Constant;
import com.unionpay.tsmservice.request.AcquireSEAppListRequestParams;
import com.unionpay.tsmservice.request.ActivateVendorPayRequestParams;
import com.unionpay.tsmservice.request.AddCardToVendorPayRequestParams;
import com.unionpay.tsmservice.request.AppDataUpdateRequestParams;
import com.unionpay.tsmservice.request.AppDeleteRequestParams;
import com.unionpay.tsmservice.request.AppDownloadApplyRequestParams;
import com.unionpay.tsmservice.request.AppDownloadRequestParams;
import com.unionpay.tsmservice.request.AppLockRequestParams;
import com.unionpay.tsmservice.request.AppUnlockRequestParams;
import com.unionpay.tsmservice.request.CardListStatusChangedRequestParams;
import com.unionpay.tsmservice.request.CheckSSamsungPayRequestParams;
import com.unionpay.tsmservice.request.ClearEncryptDataRequestParams;
import com.unionpay.tsmservice.request.CloseChannelRequestParams;
import com.unionpay.tsmservice.request.ECashTopUpRequestParams;
import com.unionpay.tsmservice.request.EncryptDataRequestParams;
import com.unionpay.tsmservice.request.ExecuteCmdRequestParams;
import com.unionpay.tsmservice.request.GetAccountBalanceRequestParams;
import com.unionpay.tsmservice.request.GetAccountInfoRequestParams;
import com.unionpay.tsmservice.request.GetAppDetailRequestParams;
import com.unionpay.tsmservice.request.GetAppListRequestParams;
import com.unionpay.tsmservice.request.GetAppStatusRequestParams;
import com.unionpay.tsmservice.request.GetAssociatedAppRequestParams;
import com.unionpay.tsmservice.request.GetCardInfoBySpayRequestParams;
import com.unionpay.tsmservice.request.GetCardInfoRequestParams;
import com.unionpay.tsmservice.request.GetDefaultCardRequestParams;
import com.unionpay.tsmservice.request.GetEncryptDataRequestParams;
import com.unionpay.tsmservice.request.GetMessageDetailsRequestParams;
import com.unionpay.tsmservice.request.GetSMSAuthCodeRequestParams;
import com.unionpay.tsmservice.request.GetSeAppListRequestParams;
import com.unionpay.tsmservice.request.GetSeIdRequestParams;
import com.unionpay.tsmservice.request.GetTransElementsRequestParams;
import com.unionpay.tsmservice.request.GetTransRecordRequestParams;
import com.unionpay.tsmservice.request.GetTransactionDetailsRequestParams;
import com.unionpay.tsmservice.request.GetVendorPayStatusRequestParams;
import com.unionpay.tsmservice.request.HideAppApplyRequestParams;
import com.unionpay.tsmservice.request.HideSafetyKeyboardRequestParams;
import com.unionpay.tsmservice.request.InitRequestParams;
import com.unionpay.tsmservice.request.OnlinePaymentVerifyRequestParams;
import com.unionpay.tsmservice.request.OpenChannelRequestParams;
import com.unionpay.tsmservice.request.PreDownloadRequestParams;
import com.unionpay.tsmservice.request.QueryVendorPayStatusRequestParams;
import com.unionpay.tsmservice.request.RequestParams;
import com.unionpay.tsmservice.request.SafetyKeyboardRequestParams;
import com.unionpay.tsmservice.request.SendApduRequestParams;
import com.unionpay.tsmservice.request.SetDefaultCardRequestParams;
import com.unionpay.tsmservice.request.SetSamsungDefWalletRequestParams;
import com.unionpay.tsmservice.result.AcquireSeAppListResult;
import com.unionpay.tsmservice.result.AddCardResult;
import com.unionpay.tsmservice.result.CheckSSamsungPayResult;
import com.unionpay.tsmservice.result.EncryptDataResult;
import com.unionpay.tsmservice.result.GetCardInfoBySpayResult;
import com.unionpay.tsmservice.result.GetEncryptDataResult;
import com.unionpay.tsmservice.result.GetSeAppListResult;
import com.unionpay.tsmservice.result.GetSeIdResult;
import com.unionpay.tsmservice.result.InitResult;
import com.unionpay.tsmservice.result.MessageDetailsResult;
import com.unionpay.tsmservice.result.OnlinePaymentVerifyResult;
import com.unionpay.tsmservice.result.OpenChannelResult;
import com.unionpay.tsmservice.result.SendApduResult;
import com.unionpay.tsmservice.result.TransactionDetailsResult;
import com.unionpay.tsmservice.result.VendorPayStatusResult;
import com.unionpay.tsmservice.utils.IUPJniInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class UPTsmAddon {
    private static UPTsmAddon a = null;
    private static ArrayList<UPTsmConnectionListener> b = null;
    private HashMap<String, ITsmCallback> A = new HashMap<>();
    private HashMap<String, ITsmCallback> B = new HashMap<>();
    private HashMap<String, ITsmCallback> C = new HashMap<>();
    private HashMap<String, ITsmCallback> D = new HashMap<>();
    private HashMap<String, ITsmCallback> E = new HashMap<>();
    private HashMap<String, ITsmCallback> F = new HashMap<>();
    private HashMap<String, ITsmCallback> G = new HashMap<>();
    private HashMap<String, ITsmCallback> H = new HashMap<>();
    private HashMap<String, ITsmCallback> I = new HashMap<>();
    private HashMap<String, ITsmCallback> J = new HashMap<>();
    private HashMap<String, ITsmCallback> K = new HashMap<>();
    private HashMap<String, ITsmCallback> L = new HashMap<>();
    private HashMap<String, ITsmCallback> M = new HashMap<>();
    private HashMap<String, ITsmCallback> N = new HashMap<>();
    private HashMap<String, ITsmCallback> O = new HashMap<>();
    private HashMap<String, ITsmCallback> P = new HashMap<>();
    private HashMap<String, ITsmCallback> Q = new HashMap<>();
    private HashMap<String, ITsmCallback> R = new HashMap<>();
    private HashMap<String, ITsmCallback> S = new HashMap<>();
    private HashMap<String, ITsmCallback> T = new HashMap<>();
    private HashMap<String, ITsmCallback> U = new HashMap<>();
    private HashMap<String, ITsmCallback> V = new HashMap<>();
    private HashMap<String, ITsmActivityCallback> W = new HashMap<>();
    /* access modifiers changed from: private */
    public int[] X;
    private final Handler.Callback Y = new Handler.Callback() {
        public final synchronized boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    UPTsmAddon.this.a();
                    return true;
                case 1:
                    UPTsmAddon.this.b();
                    return true;
                default:
                    return false;
            }
        }
    };
    /* access modifiers changed from: private */
    public final Handler Z = new Handler(Looper.getMainLooper(), this.Y);
    private Context c = null;
    private ServiceConnection d = null;
    /* access modifiers changed from: private */
    public ITsmService e = null;
    /* access modifiers changed from: private */
    public boolean f = false;
    private int g = 1;
    private boolean h = false;
    private HashMap<String, ITsmCallback> i = new HashMap<>();
    private HashMap<String, ITsmCallback> j = new HashMap<>();
    private HashMap<String, ITsmCallback> k = new HashMap<>();
    private HashMap<String, ITsmCallback> l = new HashMap<>();
    private HashMap<String, ITsmCallback> m = new HashMap<>();
    private HashMap<String, ITsmCallback> n = new HashMap<>();
    private HashMap<String, ITsmCallback> o = new HashMap<>();
    private HashMap<String, ITsmCallback> p = new HashMap<>();
    private HashMap<String, ITsmCallback> q = new HashMap<>();
    private HashMap<String, ITsmCallback> r = new HashMap<>();
    private HashMap<String, ITsmCallback> s = new HashMap<>();
    private HashMap<String, ITsmCallback> t = new HashMap<>();
    private HashMap<String, ITsmCallback> u = new HashMap<>();
    private HashMap<String, ITsmCallback> v = new HashMap<>();
    private HashMap<String, ITsmCallback> w = new HashMap<>();
    private HashMap<String, ITsmCallback> x = new HashMap<>();
    private HashMap<String, ITsmCallback> y = new HashMap<>();
    private HashMap<String, ITsmCallback> z = new HashMap<>();

    public interface UPTsmConnectionListener {
        void onTsmConnected();

        void onTsmDisconnected();
    }

    public class a extends ITsmActivityCallback.Stub {
        private int b = 1000;

        public a() {
        }

        public final void startActivity(String str, String str2, int i, Bundle bundle) throws RemoteException {
            UPTsmAddon.a((ITsmActivityCallback) UPTsmAddon.b(UPTsmAddon.this, this.b).get(UPTsmAddon.this.c.getPackageName()), str, str2, i, bundle);
            UPTsmAddon.b(UPTsmAddon.this, this.b).remove(UPTsmAddon.this.c.getPackageName());
        }
    }

    private class b extends ITsmCallback.Stub {
        private int b;
        private int c;

        private b(int i, int i2) {
            this.b = i;
            this.c = i2;
        }

        /* synthetic */ b(UPTsmAddon uPTsmAddon, int i, int i2, byte b2) {
            this(i, i2);
        }

        public final void onError(String str, String str2) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.KEY_ERROR_CODE, str);
            bundle.putString(Constant.KEY_ERROR_DESC, str2);
            UPTsmAddon.a((ITsmCallback) UPTsmAddon.a(UPTsmAddon.this, this.b).get(String.valueOf(this.c)), bundle);
            UPTsmAddon.a(UPTsmAddon.this, this.b).remove(String.valueOf(this.c));
            if (UPTsmAddon.a(UPTsmAddon.this, this.b).isEmpty()) {
                UPTsmAddon.this.X[this.b] = 0;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x011a, code lost:
            r1.putParcelable("result", r7);
         */
        public final void onResult(Bundle bundle) throws RemoteException {
            Parcelable parcelable;
            new Bundle();
            int i = this.b;
            Bundle bundle2 = new Bundle();
            Parcel obtain = Parcel.obtain();
            String string = bundle.getString(Constant.KEY_ERROR_CODE);
            String string2 = bundle.getString("result");
            bundle2.putString(Constant.KEY_ERROR_CODE, string);
            if (!TextUtils.isEmpty(string2)) {
                byte[] decode = Base64.decode(UPTsmAddon.this.c(string2), 0);
                if (!(decode == null || decode.length == 0)) {
                    obtain.unmarshall(decode, 0, decode.length);
                    obtain.setDataPosition(0);
                }
                if (obtain.dataSize() == 0) {
                    bundle2.putString(Constant.KEY_ERROR_CODE, "010035");
                    bundle = bundle2;
                    obtain.recycle();
                    UPTsmAddon.a((ITsmCallback) UPTsmAddon.a(UPTsmAddon.this, this.b).get(String.valueOf(this.c)), bundle);
                    UPTsmAddon.a(UPTsmAddon.this, this.b).remove(String.valueOf(this.c));
                    if (UPTsmAddon.a(UPTsmAddon.this, this.b).isEmpty()) {
                        UPTsmAddon.this.X[this.b] = 0;
                        return;
                    }
                    return;
                }
            }
            switch (i) {
                case 0:
                    parcelable = (InitResult) obtain.readParcelable(InitResult.class.getClassLoader());
                    break;
                case 3:
                    parcelable = (GetSeAppListResult) obtain.readParcelable(GetSeAppListResult.class.getClassLoader());
                    break;
                case 12:
                    parcelable = (GetSeIdResult) obtain.readParcelable(GetSeIdResult.class.getClassLoader());
                    break;
                case 20:
                    parcelable = (OpenChannelResult) obtain.readParcelable(OpenChannelResult.class.getClassLoader());
                    break;
                case 22:
                    parcelable = (SendApduResult) obtain.readParcelable(SendApduResult.class.getClassLoader());
                    break;
                case 23:
                    parcelable = (EncryptDataResult) obtain.readParcelable(EncryptDataResult.class.getClassLoader());
                    break;
                case 28:
                    parcelable = (GetCardInfoBySpayResult) obtain.readParcelable(GetCardInfoBySpayResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_CHECK_SSAMSUNGPAY:
                    parcelable = (CheckSSamsungPayResult) obtain.readParcelable(CheckSSamsungPayResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_GET_ENCRYPT_DATA:
                    parcelable = (GetEncryptDataResult) obtain.readParcelable(GetEncryptDataResult.class.getClassLoader());
                    break;
                case 36:
                case Constant.INTERFACE_QUERY_VENDOR_PAY_STATUS:
                    parcelable = (VendorPayStatusResult) obtain.readParcelable(VendorPayStatusResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_ADD_CARD_TO_VENDOR_PAY:
                    parcelable = (AddCardResult) obtain.readParcelable(AddCardResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_ONLINE_PAYMENT_VERIFY:
                    parcelable = (OnlinePaymentVerifyResult) obtain.readParcelable(OnlinePaymentVerifyResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_ACQUIRE_SE_APP_LIST:
                    parcelable = (AcquireSeAppListResult) obtain.readParcelable(AcquireSeAppListResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_GET_TRANSACTION_DETAILS:
                    parcelable = (TransactionDetailsResult) obtain.readParcelable(TransactionDetailsResult.class.getClassLoader());
                    break;
                case Constant.INTERFACE_GET_MESSAGE_DETAILS:
                    parcelable = (MessageDetailsResult) obtain.readParcelable(MessageDetailsResult.class.getClassLoader());
                    break;
            }
        }
    }

    static {
        try {
            System.loadLibrary("uptsmaddon");
        } catch (UnsatisfiedLinkError e2) {
            e2.printStackTrace();
        }
    }

    private UPTsmAddon(Context context) {
        this.c = context;
        this.X = new int[45];
        if (!a(context)) {
            throw new RuntimeException();
        }
    }

    private static int a(int i2, RequestParams requestParams, ITsmCallback iTsmCallback) throws RemoteException {
        return new SessionKeyReExchange(a, i2, requestParams, iTsmCallback).reExchangeKey();
    }

    private static int a(int i2, RequestParams requestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        SessionKeyReExchange sessionKeyReExchange = new SessionKeyReExchange(a, i2, requestParams, iTsmCallback, iTsmProgressCallback);
        return sessionKeyReExchange.reExchangeKey();
    }

    private static int a(int i2, SafetyKeyboardRequestParams safetyKeyboardRequestParams, int i3, OnSafetyKeyboardCallback onSafetyKeyboardCallback, Context context) throws RemoteException {
        SessionKeyReExchange sessionKeyReExchange = new SessionKeyReExchange(a, i2, safetyKeyboardRequestParams, i3, onSafetyKeyboardCallback, context);
        return sessionKeyReExchange.reExchangeKey();
    }

    private String a(Bundle bundle) {
        String str = "";
        Parcel obtain = Parcel.obtain();
        obtain.writeBundle(bundle);
        byte[] marshall = obtain.marshall();
        if (!(marshall == null || marshall.length == 0)) {
            str = b(Base64.encodeToString(marshall, 0));
        }
        obtain.recycle();
        return str;
    }

    static /* synthetic */ HashMap a(UPTsmAddon uPTsmAddon, int i2) {
        switch (i2) {
            case 0:
                return uPTsmAddon.i;
            case 1:
                return uPTsmAddon.j;
            case 2:
                return uPTsmAddon.l;
            case 3:
                return uPTsmAddon.k;
            case 4:
                return uPTsmAddon.n;
            case 5:
                return uPTsmAddon.m;
            case 6:
                return uPTsmAddon.A;
            case 7:
                return uPTsmAddon.u;
            case 8:
                return uPTsmAddon.v;
            case 9:
                return uPTsmAddon.o;
            case 10:
                return uPTsmAddon.t;
            case 11:
                return uPTsmAddon.r;
            case 12:
                return uPTsmAddon.z;
            case 13:
                return uPTsmAddon.y;
            case 14:
                return uPTsmAddon.E;
            case 15:
                return uPTsmAddon.p;
            case 16:
                return uPTsmAddon.C;
            case 17:
                return uPTsmAddon.D;
            case 18:
                return uPTsmAddon.q;
            case 19:
                return uPTsmAddon.s;
            case 20:
                return uPTsmAddon.w;
            case 21:
                return uPTsmAddon.F;
            case 22:
                return uPTsmAddon.x;
            case 23:
                return uPTsmAddon.B;
            case 24:
                return uPTsmAddon.G;
            case 25:
                return uPTsmAddon.H;
            case 28:
                return uPTsmAddon.I;
            case Constant.INTERFACE_CHECK_SSAMSUNGPAY:
                return uPTsmAddon.J;
            case 30:
                return uPTsmAddon.K;
            case Constant.INTERFACE_GET_ENCRYPT_DATA:
                return uPTsmAddon.L;
            case Constant.INTERFACE_CARDLIST_STATUS_CHANGED:
                return uPTsmAddon.M;
            case 36:
                return uPTsmAddon.N;
            case Constant.INTERFACE_ACTIVATE_VENDOR_PAY:
                return uPTsmAddon.O;
            case Constant.INTERFACE_ADD_CARD_TO_VENDOR_PAY:
                return uPTsmAddon.P;
            case Constant.INTERFACE_ONLINE_PAYMENT_VERIFY:
                return uPTsmAddon.Q;
            case 40:
                return uPTsmAddon.R;
            case Constant.INTERFACE_QUERY_VENDOR_PAY_STATUS:
                return uPTsmAddon.S;
            case Constant.INTERFACE_ACQUIRE_SE_APP_LIST:
                return uPTsmAddon.T;
            case Constant.INTERFACE_GET_TRANSACTION_DETAILS:
                return uPTsmAddon.U;
            case Constant.INTERFACE_GET_MESSAGE_DETAILS:
                return uPTsmAddon.V;
            default:
                return null;
        }
    }

    private static HashMap<String, String> a(HashMap<String, String> hashMap) {
        if (hashMap == null) {
            return new HashMap<>();
        }
        HashMap<String, String> hashMap2 = new HashMap<>();
        for (String next : hashMap.keySet()) {
            if (next != null) {
                String str = hashMap.get(next);
                if (str != null) {
                    hashMap2.put(new String(next), new String(str));
                }
            }
        }
        return hashMap2;
    }

    /* access modifiers changed from: private */
    public synchronized void a() {
        if (b != null && b.size() > 0) {
            Iterator<UPTsmConnectionListener> it = b.iterator();
            while (it.hasNext()) {
                UPTsmConnectionListener next = it.next();
                if (next != null) {
                    next.onTsmConnected();
                }
            }
        }
    }

    static /* synthetic */ void a(ITsmActivityCallback iTsmActivityCallback, String str, String str2, int i2, Bundle bundle) {
        if (iTsmActivityCallback != null) {
            try {
                iTsmActivityCallback.startActivity(str, str2, i2, bundle);
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }
    }

    static /* synthetic */ void a(ITsmCallback iTsmCallback, Bundle bundle) {
        if (iTsmCallback != null) {
            try {
                String string = bundle.getString(Constant.KEY_ERROR_CODE);
                if ("10000".equals(string)) {
                    iTsmCallback.onResult(bundle);
                } else {
                    iTsmCallback.onError(string, bundle.getString(Constant.KEY_ERROR_DESC));
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private static boolean a(Context context) {
        try {
            return IUPJniInterface.iJE(context);
        } catch (UnsatisfiedLinkError e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private static boolean a(String str) {
        try {
            return IUPJniInterface.cSKV(str);
        } catch (UnsatisfiedLinkError e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private String b(String str) {
        try {
            return IUPJniInterface.eMG(str, this.g);
        } catch (UnsatisfiedLinkError e2) {
            e2.printStackTrace();
            return "";
        }
    }

    static /* synthetic */ HashMap b(UPTsmAddon uPTsmAddon, int i2) {
        if (i2 != 1000) {
            return null;
        }
        return uPTsmAddon.W;
    }

    /* access modifiers changed from: private */
    public synchronized void b() {
        if (b != null && b.size() > 0) {
            Iterator<UPTsmConnectionListener> it = b.iterator();
            while (it.hasNext()) {
                UPTsmConnectionListener next = it.next();
                if (next != null) {
                    next.onTsmDisconnected();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public String c(String str) {
        try {
            return IUPJniInterface.dMG(str, this.g);
        } catch (UnsatisfiedLinkError e2) {
            e2.printStackTrace();
            return "";
        }
    }

    private boolean c() {
        String e2 = e("com.unionpay.tsmservice");
        if (e2 == null || e2.compareTo(Constant.SUPPORTED_MIN_APK_VERSION) < 0) {
            return false;
        }
        if (e2.compareTo(Constant.APK_VERSION_010018) >= 0) {
            this.g = 1;
            this.h = true;
            return true;
        } else if (e2.compareTo(Constant.APK_VERSION_010012) >= 0 && e2.compareTo(Constant.APK_VERSION_010016) <= 0) {
            this.g = 2;
            this.h = false;
            return true;
        } else if (e2.compareTo(Constant.APK_VERSION_010017) != 0 && e2.compareTo(Constant.SUPPORTED_MIN_APK_VERSION) != 0) {
            return false;
        } else {
            this.g = 1;
            this.h = false;
            return true;
        }
    }

    private boolean d(String str) {
        String e2 = e("com.unionpay.tsmservice");
        return e2 != null && e2.compareTo(str) >= 0;
    }

    private String e(String str) {
        PackageInfo packageInfo;
        try {
            packageInfo = this.c.getPackageManager().getPackageInfo(str, 0);
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
            packageInfo = null;
        }
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }

    private static String f(String str) {
        try {
            JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject() : new JSONObject(str);
            jSONObject.put(Constant.KEY_JAR_VERSION, 36);
            return jSONObject.toString();
        } catch (JSONException e2) {
            e2.printStackTrace();
            return str;
        }
    }

    private String g(String str) {
        try {
            JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject() : new JSONObject(str);
            jSONObject.put(Constant.KEY_PACKAGE_NAME, this.c.getPackageName());
            return jSONObject.toString();
        } catch (JSONException e2) {
            e2.printStackTrace();
            return str;
        }
    }

    public static UPTsmAddon getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (a == null) {
            a = new UPTsmAddon(context.getApplicationContext());
        }
        if (b == null) {
            b = new ArrayList<>();
        }
        return a;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int acquireSEAppList(AcquireSEAppListRequestParams acquireSEAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.28")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                AcquireSEAppListRequestParams acquireSEAppListRequestParams2 = new AcquireSEAppListRequestParams();
                String str = "";
                if (acquireSEAppListRequestParams != null) {
                    str = acquireSEAppListRequestParams.getReserve();
                    Bundle params = acquireSEAppListRequestParams.getParams();
                    if (params != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.KEY_ENCRYPT_DATA, a(params));
                        acquireSEAppListRequestParams2.setParams(bundle);
                    }
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    acquireSEAppListRequestParams2.setReserve(b(str));
                }
                this.T.put(String.valueOf(this.X[42]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[42];
                    iArr[42] = i3 + 1;
                    int acquireSEAppList = iTsmService.acquireSEAppList(acquireSEAppListRequestParams2, new b(this, 42, i3, (byte) 0));
                    if (acquireSEAppList != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.T;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[42] - 1;
                        iArr2[42] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != acquireSEAppList) {
                        return acquireSEAppList;
                    }
                    return a(42, acquireSEAppListRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(42, acquireSEAppListRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int activateVendorPay(ActivateVendorPayRequestParams activateVendorPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.20")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                ActivateVendorPayRequestParams activateVendorPayRequestParams2 = new ActivateVendorPayRequestParams();
                String str = "";
                if (activateVendorPayRequestParams != null) {
                    str = activateVendorPayRequestParams.getReserve();
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    activateVendorPayRequestParams2.setReserve(b(str));
                }
                this.O.put(String.valueOf(this.X[37]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[37];
                    iArr[37] = i3 + 1;
                    int activateVendorPay = iTsmService.activateVendorPay(activateVendorPayRequestParams2, new b(this, 37, i3, (byte) 0));
                    if (activateVendorPay != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.O;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[37] - 1;
                        iArr2[37] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != activateVendorPay) {
                        return activateVendorPay;
                    }
                    return a(37, activateVendorPayRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(37, activateVendorPayRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00b6, code lost:
        return r8;
     */
    public synchronized int addCardToVendorPay(AddCardToVendorPayRequestParams addCardToVendorPayRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        if (addCardToVendorPayRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.20")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                AddCardToVendorPayRequestParams addCardToVendorPayRequestParams2 = new AddCardToVendorPayRequestParams();
                Bundle params = addCardToVendorPayRequestParams.getParams();
                if (params != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.KEY_ENCRYPT_DATA, a(params));
                    addCardToVendorPayRequestParams2.setParams(bundle);
                }
                String reserve = addCardToVendorPayRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    addCardToVendorPayRequestParams2.setReserve(b(reserve));
                }
                this.P.put(String.valueOf(this.X[38]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[38];
                    iArr[38] = i3 + 1;
                    int addCardToVendorPay = iTsmService.addCardToVendorPay(addCardToVendorPayRequestParams2, new b(this, 38, i3, (byte) 0), iTsmProgressCallback);
                    if (addCardToVendorPay != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.P;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[38] - 1;
                        iArr2[38] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != addCardToVendorPay) {
                        return addCardToVendorPay;
                    }
                    return a(38, addCardToVendorPayRequestParams, iTsmCallback, iTsmProgressCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(38, addCardToVendorPayRequestParams, iTsmCallback);
            }
        }
    }

    public synchronized void addConnectionListener(UPTsmConnectionListener uPTsmConnectionListener) {
        if (uPTsmConnectionListener != null) {
            b.add(uPTsmConnectionListener);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int appDataUpdate(AppDataUpdateRequestParams appDataUpdateRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        if (appDataUpdateRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            AppDataUpdateRequestParams appDataUpdateRequestParams2 = new AppDataUpdateRequestParams();
            String reserve = appDataUpdateRequestParams.getReserve();
            AppID appID = appDataUpdateRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                appDataUpdateRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    appDataUpdateRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int appDataUpdate = this.e.appDataUpdate(appDataUpdateRequestParams2, new b(this, 18, this.X[18], (byte) 0), iTsmProgressCallback);
                if (-2 == appDataUpdate) {
                    return a(18, appDataUpdateRequestParams, iTsmCallback, iTsmProgressCallback);
                } else if (appDataUpdate == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.q;
                    int[] iArr = this.X;
                    int i3 = iArr[18];
                    iArr[18] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(18, appDataUpdateRequestParams, iTsmCallback, iTsmProgressCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009f, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a9, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01df, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01e0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01e9, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        return r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:27:0x0074, B:77:0x01aa] */
    @Deprecated
    public synchronized int appDelete(AppDeleteRequestParams appDeleteRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        AppDeleteRequestParams appDeleteRequestParams2 = appDeleteRequestParams;
        ITsmCallback iTsmCallback2 = iTsmCallback;
        ITsmProgressCallback iTsmProgressCallback2 = iTsmProgressCallback;
        synchronized (this) {
            if (appDeleteRequestParams2 == null || iTsmCallback2 == null) {
                i2 = -3;
            } else if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                AppDeleteRequestParams appDeleteRequestParams3 = new AppDeleteRequestParams();
                String reserve = appDeleteRequestParams.getReserve();
                AppID appID = appDeleteRequestParams.getAppID();
                if (!TextUtils.isEmpty(reserve)) {
                    appDeleteRequestParams3.setReserve(b(reserve));
                }
                if (appID != null) {
                    String appAid = appID.getAppAid();
                    String appVersion = appID.getAppVersion();
                    if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                        appDeleteRequestParams3.setAppID(new AppID(b(appAid), b(appVersion)));
                    }
                }
                HashMap hashMap = (HashMap) appDeleteRequestParams.getParams();
                if (hashMap == null) {
                    int appDelete = this.e.appDelete(appDeleteRequestParams3, new b(this, 17, this.X[17], (byte) 0), iTsmProgressCallback2);
                    if (-2 == appDelete) {
                        int a2 = a(17, appDeleteRequestParams2, iTsmCallback2, iTsmProgressCallback2);
                        return a2;
                    } else if (appDelete == 0) {
                        HashMap<String, ITsmCallback> hashMap2 = this.D;
                        int[] iArr = this.X;
                        int i3 = iArr[17];
                        iArr[17] = i3 + 1;
                        hashMap2.put(String.valueOf(i3), iTsmCallback2);
                    }
                } else {
                    HashMap<String, String> a3 = a((HashMap<String, String>) hashMap);
                    String str = a3.get(Constant.KEY_CARD_HOLDER_NAME);
                    String str2 = a3.get(Constant.KEY_ID_TYPE);
                    String str3 = a3.get(Constant.KEY_ID_NO);
                    String str4 = a3.get(Constant.KEY_PAN);
                    String str5 = a3.get(Constant.KEY_PIN);
                    String str6 = a3.get(Constant.KEY_EXPIRY_DATE);
                    String str7 = a3.get(Constant.KEY_CVN2);
                    String str8 = a3.get(Constant.KEY_PHONE_NUMBER);
                    String str9 = a3.get(Constant.KEY_SMS_AUTH_CODE);
                    String str10 = a3.get(Constant.KEY_BALANCE);
                    String str11 = a3.get(Constant.KEY_CARD_TYPE);
                    if (!TextUtils.isEmpty(str)) {
                        a3.put(Constant.KEY_CARD_HOLDER_NAME, b(str));
                    }
                    if (!TextUtils.isEmpty(str2)) {
                        a3.put(Constant.KEY_ID_TYPE, b(str2));
                    }
                    if (!TextUtils.isEmpty(str3)) {
                        a3.put(Constant.KEY_ID_NO, b(str3));
                    }
                    if (!TextUtils.isEmpty(str4)) {
                        a3.put(Constant.KEY_PAN, b(str4));
                    }
                    if (!TextUtils.isEmpty(str5)) {
                        a3.put(Constant.KEY_PIN, str5);
                    }
                    if (!TextUtils.isEmpty(str6)) {
                        a3.put(Constant.KEY_EXPIRY_DATE, b(str6));
                    }
                    if (!TextUtils.isEmpty(str7)) {
                        a3.put(Constant.KEY_CVN2, b(str7));
                    }
                    if (!TextUtils.isEmpty(str8)) {
                        a3.put(Constant.KEY_PHONE_NUMBER, b(str8));
                    }
                    if (!TextUtils.isEmpty(str9)) {
                        a3.put(Constant.KEY_SMS_AUTH_CODE, b(str9));
                    }
                    if (!TextUtils.isEmpty(str10)) {
                        a3.put(Constant.KEY_BALANCE, b(str10));
                    }
                    if (!TextUtils.isEmpty(str11)) {
                        a3.put(Constant.KEY_CARD_TYPE, b(str11));
                    }
                    appDeleteRequestParams3.setParams(a3);
                    int appDelete2 = this.e.appDelete(appDeleteRequestParams3, new b(this, 17, this.X[17], (byte) 0), iTsmProgressCallback2);
                    if (-2 == appDelete2) {
                        int a4 = a(17, appDeleteRequestParams, iTsmCallback, iTsmProgressCallback2);
                        return a4;
                    }
                    ITsmCallback iTsmCallback3 = iTsmCallback;
                    if (appDelete2 == 0) {
                        HashMap<String, ITsmCallback> hashMap3 = this.D;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[17];
                        iArr2[17] = i4 + 1;
                        hashMap3.put(String.valueOf(i4), iTsmCallback3);
                    }
                }
            } else {
                int a5 = a(17, appDeleteRequestParams2, iTsmCallback2, iTsmProgressCallback2);
                return a5;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int appDownload(AppDownloadRequestParams appDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        if (appDownloadRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            AppDownloadRequestParams appDownloadRequestParams2 = new AppDownloadRequestParams();
            String reserve = appDownloadRequestParams.getReserve();
            AppID appID = appDownloadRequestParams.getAppID();
            String appName = appDownloadRequestParams.getAppName();
            if (!TextUtils.isEmpty(reserve)) {
                appDownloadRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    appDownloadRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            if (!TextUtils.isEmpty(appName)) {
                appDownloadRequestParams2.setAppName(b(appName));
            }
            try {
                int appDownload = this.e.appDownload(appDownloadRequestParams2, new b(this, 16, this.X[16], (byte) 0), iTsmProgressCallback);
                if (-2 == appDownload) {
                    return a(16, appDownloadRequestParams, iTsmCallback, iTsmProgressCallback);
                } else if (appDownload == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.C;
                    int[] iArr = this.X;
                    int i3 = iArr[16];
                    iArr[16] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(16, appDownloadRequestParams, iTsmCallback, iTsmProgressCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009d, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a7, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01f4, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01f5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01fe, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        return r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:27:0x0072, B:80:0x01bf] */
    @Deprecated
    public synchronized int appDownloadApply(AppDownloadApplyRequestParams appDownloadApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        AppDownloadApplyRequestParams appDownloadApplyRequestParams2 = appDownloadApplyRequestParams;
        ITsmCallback iTsmCallback2 = iTsmCallback;
        synchronized (this) {
            if (appDownloadApplyRequestParams2 == null || iTsmCallback2 == null) {
                i2 = -3;
            } else if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                AppDownloadApplyRequestParams appDownloadApplyRequestParams3 = new AppDownloadApplyRequestParams();
                String reserve = appDownloadApplyRequestParams.getReserve();
                AppID appID = appDownloadApplyRequestParams.getAppID();
                if (!TextUtils.isEmpty(reserve)) {
                    appDownloadApplyRequestParams3.setReserve(b(reserve));
                }
                if (appID != null) {
                    String appAid = appID.getAppAid();
                    String appVersion = appID.getAppVersion();
                    if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                        appDownloadApplyRequestParams3.setAppID(new AppID(b(appAid), b(appVersion)));
                    }
                }
                HashMap hashMap = (HashMap) appDownloadApplyRequestParams.getParams();
                if (hashMap == null) {
                    int appDownloadApply = this.e.appDownloadApply(appDownloadApplyRequestParams3, new b(this, 15, this.X[15], (byte) 0));
                    if (-2 == appDownloadApply) {
                        int a2 = a(15, appDownloadApplyRequestParams2, iTsmCallback2);
                        return a2;
                    } else if (appDownloadApply == 0) {
                        HashMap<String, ITsmCallback> hashMap2 = this.p;
                        int[] iArr = this.X;
                        int i3 = iArr[15];
                        iArr[15] = i3 + 1;
                        hashMap2.put(String.valueOf(i3), iTsmCallback2);
                    }
                } else {
                    HashMap<String, String> a3 = a((HashMap<String, String>) hashMap);
                    String str = a3.get(Constant.KEY_ACCOUNT_LIMIT);
                    String str2 = a3.get(Constant.KEY_ACCOUNT_TYPE);
                    String str3 = a3.get(Constant.KEY_CARD_HOLDER_NAME);
                    String str4 = a3.get(Constant.KEY_ID_TYPE);
                    String str5 = a3.get(Constant.KEY_ID_NO);
                    String str6 = a3.get(Constant.KEY_PAN);
                    String str7 = a3.get(Constant.KEY_PIN);
                    String str8 = a3.get(Constant.KEY_EXPIRY_DATE);
                    String str9 = a3.get(Constant.KEY_CVN2);
                    String str10 = a3.get(Constant.KEY_PHONE_NUMBER);
                    String str11 = a3.get(Constant.KEY_SMS_AUTH_CODE);
                    String str12 = a3.get(Constant.KEY_CARD_TYPE);
                    if (!TextUtils.isEmpty(str)) {
                        a3.put(Constant.KEY_ACCOUNT_LIMIT, b(str));
                    }
                    if (!TextUtils.isEmpty(str2)) {
                        a3.put(Constant.KEY_ACCOUNT_TYPE, b(str2));
                    }
                    if (!TextUtils.isEmpty(str3)) {
                        a3.put(Constant.KEY_CARD_HOLDER_NAME, b(str3));
                    }
                    if (!TextUtils.isEmpty(str4)) {
                        a3.put(Constant.KEY_ID_TYPE, b(str4));
                    }
                    if (!TextUtils.isEmpty(str5)) {
                        a3.put(Constant.KEY_ID_NO, b(str5));
                    }
                    if (!TextUtils.isEmpty(str6)) {
                        a3.put(Constant.KEY_PAN, b(str6));
                    }
                    if (!TextUtils.isEmpty(str7)) {
                        a3.put(Constant.KEY_PIN, str7);
                    }
                    if (!TextUtils.isEmpty(str8)) {
                        a3.put(Constant.KEY_EXPIRY_DATE, b(str8));
                    }
                    if (!TextUtils.isEmpty(str9)) {
                        a3.put(Constant.KEY_CVN2, b(str9));
                    }
                    if (!TextUtils.isEmpty(str10)) {
                        a3.put(Constant.KEY_PHONE_NUMBER, b(str10));
                    }
                    if (!TextUtils.isEmpty(str11)) {
                        a3.put(Constant.KEY_SMS_AUTH_CODE, b(str11));
                    }
                    if (!TextUtils.isEmpty(str12)) {
                        a3.put(Constant.KEY_CARD_TYPE, b(str12));
                    }
                    appDownloadApplyRequestParams3.setParams(a3);
                    int appDownloadApply2 = this.e.appDownloadApply(appDownloadApplyRequestParams3, new b(this, 15, this.X[15], (byte) 0));
                    if (-2 == appDownloadApply2) {
                        int a4 = a(15, appDownloadApplyRequestParams, iTsmCallback);
                        return a4;
                    }
                    ITsmCallback iTsmCallback3 = iTsmCallback;
                    if (appDownloadApply2 == 0) {
                        HashMap<String, ITsmCallback> hashMap3 = this.p;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[15];
                        iArr2[15] = i4 + 1;
                        hashMap3.put(String.valueOf(i4), iTsmCallback3);
                    }
                }
            } else {
                int a5 = a(15, appDownloadApplyRequestParams2, iTsmCallback2);
                return a5;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r6;
     */
    @Deprecated
    public synchronized int appLock(AppLockRequestParams appLockRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (appLockRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            AppLockRequestParams appLockRequestParams2 = new AppLockRequestParams();
            String reserve = appLockRequestParams.getReserve();
            AppID appID = appLockRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                appLockRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    appLockRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int appLock = this.e.appLock(appLockRequestParams2, iTsmCallback);
                if (-2 != appLock) {
                    return appLock;
                }
                return a(26, appLockRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(26, appLockRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r6;
     */
    @Deprecated
    public synchronized int appUnlock(AppUnlockRequestParams appUnlockRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (appUnlockRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            AppUnlockRequestParams appUnlockRequestParams2 = new AppUnlockRequestParams();
            String reserve = appUnlockRequestParams.getReserve();
            AppID appID = appUnlockRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                appUnlockRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    appUnlockRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int appUnlock = this.e.appUnlock(appUnlockRequestParams2, iTsmCallback);
                if (-2 != appUnlock) {
                    return appUnlock;
                }
                return a(27, appUnlockRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(27, appUnlockRequestParams, iTsmCallback);
        }
    }

    public boolean bind() {
        if (this.d == null) {
            this.d = new ServiceConnection() {
                public final synchronized void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    boolean unused = UPTsmAddon.this.f = true;
                    ITsmService unused2 = UPTsmAddon.this.e = ITsmService.Stub.asInterface(iBinder);
                    UPTsmAddon.this.Z.sendEmptyMessage(0);
                }

                public final synchronized void onServiceDisconnected(ComponentName componentName) {
                    boolean unused = UPTsmAddon.this.f = false;
                    ITsmService unused2 = UPTsmAddon.this.e = null;
                    UPTsmAddon.this.Z.sendEmptyMessage(1);
                }
            };
        }
        if (this.f) {
            return true;
        }
        Intent intent = new Intent("com.unionpay.tsmservice.UPTsmService");
        intent.setPackage("com.unionpay.tsmservice");
        this.c.startService(intent);
        return this.c.bindService(intent, this.d, 1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int cardListStatusChanged(CardListStatusChangedRequestParams cardListStatusChangedRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.14")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                CardListStatusChangedRequestParams cardListStatusChangedRequestParams2 = new CardListStatusChangedRequestParams();
                String str = "";
                if (cardListStatusChangedRequestParams != null) {
                    str = cardListStatusChangedRequestParams.getReserve();
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    cardListStatusChangedRequestParams2.setReserve(b(str));
                }
                this.M.put(String.valueOf(this.X[35]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[35];
                    iArr[35] = i3 + 1;
                    int cardListStatusChanged = iTsmService.cardListStatusChanged(cardListStatusChangedRequestParams2, new b(this, 35, i3, (byte) 0));
                    if (cardListStatusChanged != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.M;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[35] - 1;
                        iArr2[35] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != cardListStatusChanged) {
                        return cardListStatusChanged;
                    }
                    return a(35, cardListStatusChangedRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(35, cardListStatusChangedRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r8;
     */
    public synchronized int checkSSamsungPay(CheckSSamsungPayRequestParams checkSSamsungPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (checkSSamsungPayRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            CheckSSamsungPayRequestParams checkSSamsungPayRequestParams2 = new CheckSSamsungPayRequestParams();
            String reserve = checkSSamsungPayRequestParams.getReserve();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                checkSSamsungPayRequestParams2.setReserve(b(reserve));
            }
            this.J.put(String.valueOf(this.X[29]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[29];
                iArr[29] = i3 + 1;
                int checkSSamsungPay = iTsmService.checkSSamsungPay(checkSSamsungPayRequestParams2, new b(this, 29, i3, (byte) 0));
                if (checkSSamsungPay != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.J;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[29] - 1;
                    iArr2[29] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != checkSSamsungPay) {
                    return checkSSamsungPay;
                }
                return a(29, checkSSamsungPayRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(29, checkSSamsungPayRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r6.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005f, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0071, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r6.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x007a, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:24:0x004f, B:30:0x0060] */
    public synchronized int clearEncryptData(int i2) throws RemoteException {
        int i3;
        int i4;
        if (i2 < 2000 || i2 > 2001) {
            i3 = -3;
        } else if (!c()) {
            i3 = -8;
        } else if (this.e == null) {
            i3 = -1;
        } else if (a(this.c.getPackageName())) {
            if (d("01.00.24")) {
                ClearEncryptDataRequestParams clearEncryptDataRequestParams = new ClearEncryptDataRequestParams();
                String str = "";
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    clearEncryptDataRequestParams.setReserve(b(str));
                }
                i4 = this.e.clearKeyboardEncryptData(clearEncryptDataRequestParams, i2);
            } else {
                i4 = this.e.clearEncryptData(i2);
            }
            if (-2 != i4) {
                return i4;
            }
            return a(33, (SafetyKeyboardRequestParams) null, i2, (OnSafetyKeyboardCallback) null, (Context) null);
        } else {
            return a(33, (SafetyKeyboardRequestParams) null, i2, (OnSafetyKeyboardCallback) null, (Context) null);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ae, code lost:
        return -3;
     */
    public synchronized int closeChannel(CloseChannelRequestParams closeChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (closeChannelRequestParams != null && iTsmCallback != null) {
            String channel = closeChannelRequestParams.getChannel();
            if (TextUtils.isEmpty(channel)) {
                return -3;
            }
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                String b2 = b(channel);
                CloseChannelRequestParams closeChannelRequestParams2 = new CloseChannelRequestParams();
                closeChannelRequestParams2.setChannel(b2);
                String reserve = closeChannelRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    closeChannelRequestParams2.setReserve(b(reserve));
                }
                this.F.put(String.valueOf(this.X[21]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[21];
                    iArr[21] = i3 + 1;
                    int closeChannel = iTsmService.closeChannel(closeChannelRequestParams2, new b(this, 21, i3, (byte) 0));
                    if (closeChannel != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.F;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[21] - 1;
                        iArr2[21] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != closeChannel) {
                        return closeChannel;
                    }
                    return a(21, closeChannelRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(21, closeChannelRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00be, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r8;
     */
    @Deprecated
    public synchronized int eCashTopUp(ECashTopUpRequestParams eCashTopUpRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (eCashTopUpRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            ECashTopUpRequestParams eCashTopUpRequestParams2 = new ECashTopUpRequestParams();
            String reserve = eCashTopUpRequestParams.getReserve();
            AppID appID = eCashTopUpRequestParams.getAppID();
            String type = eCashTopUpRequestParams.getType();
            String amount = eCashTopUpRequestParams.getAmount();
            if (!TextUtils.isEmpty(reserve)) {
                eCashTopUpRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    eCashTopUpRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            String encrpytPin = eCashTopUpRequestParams.getEncrpytPin();
            if (!TextUtils.isEmpty(encrpytPin)) {
                eCashTopUpRequestParams2.setEncrpytPin(encrpytPin);
            }
            if (!TextUtils.isEmpty(type)) {
                eCashTopUpRequestParams2.setType(b(type));
            }
            if (!TextUtils.isEmpty(amount)) {
                eCashTopUpRequestParams2.setAmount(b(amount));
            }
            try {
                int eCashTopUp = this.e.eCashTopUp(eCashTopUpRequestParams2, new b(this, 19, this.X[19], (byte) 0));
                if (-2 == eCashTopUp) {
                    return a(19, eCashTopUpRequestParams, iTsmCallback);
                } else if (eCashTopUp == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.s;
                    int[] iArr = this.X;
                    int i3 = iArr[19];
                    iArr[19] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(19, eCashTopUpRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00cc, code lost:
        return -3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        return r10;
     */
    public synchronized int encryptData(EncryptDataRequestParams encryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (encryptDataRequestParams != null && iTsmCallback != null) {
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                EncryptDataRequestParams encryptDataRequestParams2 = new EncryptDataRequestParams();
                String reserve = encryptDataRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    encryptDataRequestParams2.setReserve(b(reserve));
                }
                ArrayList arrayList = (ArrayList) encryptDataRequestParams.getData();
                if (arrayList != null) {
                    int size = arrayList.size();
                    if (size == 0) {
                        return -3;
                    }
                    ArrayList arrayList2 = new ArrayList();
                    for (int i3 = 0; i3 < size; i3++) {
                        String str = (String) arrayList.get(i3);
                        if (!TextUtils.isEmpty(str)) {
                            arrayList2.add(b(str));
                        }
                    }
                    encryptDataRequestParams2.setData(arrayList2);
                }
                this.B.put(String.valueOf(this.X[23]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i4 = iArr[23];
                    iArr[23] = i4 + 1;
                    int encryptData = iTsmService.encryptData(encryptDataRequestParams2, new b(this, 23, i4, (byte) 0));
                    if (encryptData != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.B;
                        int[] iArr2 = this.X;
                        int i5 = iArr2[23] - 1;
                        iArr2[23] = i5;
                        hashMap.remove(String.valueOf(i5));
                    }
                    if (-2 != encryptData) {
                        return encryptData;
                    }
                    return a(23, encryptDataRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(23, encryptDataRequestParams, iTsmCallback);
            }
        }
    }

    public int exchangeKey(String str, String[] strArr) throws RemoteException {
        if (TextUtils.isEmpty(str) || strArr == null || strArr.length == 0) {
            return -3;
        }
        if (!c()) {
            return -8;
        }
        if (this.e == null) {
            return -1;
        }
        try {
            return this.e.exchangeKey(str, strArr);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RemoteException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r8;
     */
    public synchronized int executeCmd(ExecuteCmdRequestParams executeCmdRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        if (executeCmdRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            ExecuteCmdRequestParams executeCmdRequestParams2 = new ExecuteCmdRequestParams();
            String reserve = executeCmdRequestParams.getReserve();
            String ssid = executeCmdRequestParams.getSsid();
            String sign = executeCmdRequestParams.getSign();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                executeCmdRequestParams2.setReserve(b(reserve));
            }
            if (!TextUtils.isEmpty(ssid)) {
                executeCmdRequestParams2.setSsid(b(ssid));
            }
            if (!TextUtils.isEmpty(sign)) {
                executeCmdRequestParams2.setSign(b(sign));
            }
            this.H.put(String.valueOf(this.X[25]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[25];
                iArr[25] = i3 + 1;
                int executeCmd = iTsmService.executeCmd(executeCmdRequestParams2, new b(this, 25, i3, (byte) 0), iTsmProgressCallback);
                if (executeCmd != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.H;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[25] - 1;
                    iArr2[25] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != executeCmd) {
                    return executeCmd;
                }
                return a(25, executeCmdRequestParams, iTsmCallback, iTsmProgressCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(25, executeCmdRequestParams, iTsmCallback, iTsmProgressCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009c, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getAccountBalance(GetAccountBalanceRequestParams getAccountBalanceRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAccountBalanceRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetAccountBalanceRequestParams getAccountBalanceRequestParams2 = new GetAccountBalanceRequestParams();
            String reserve = getAccountBalanceRequestParams.getReserve();
            AppID appID = getAccountBalanceRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                getAccountBalanceRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getAccountBalanceRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            String encryptPin = getAccountBalanceRequestParams.getEncryptPin();
            if (!TextUtils.isEmpty(encryptPin)) {
                getAccountBalanceRequestParams2.setEncryptPin(encryptPin);
            }
            try {
                int accountBalance = this.e.getAccountBalance(getAccountBalanceRequestParams2, new b(this, 8, this.X[8], (byte) 0));
                if (-2 == accountBalance) {
                    return a(8, getAccountBalanceRequestParams, iTsmCallback);
                } else if (accountBalance == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.v;
                    int[] iArr = this.X;
                    int i3 = iArr[8];
                    iArr[8] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(8, getAccountBalanceRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getAccountInfo(GetAccountInfoRequestParams getAccountInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAccountInfoRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetAccountInfoRequestParams getAccountInfoRequestParams2 = new GetAccountInfoRequestParams();
            String reserve = getAccountInfoRequestParams.getReserve();
            AppID appID = getAccountInfoRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                getAccountInfoRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getAccountInfoRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int accountInfo = this.e.getAccountInfo(getAccountInfoRequestParams2, new b(this, 7, this.X[7], (byte) 0));
                if (-2 == accountInfo) {
                    return a(7, getAccountInfoRequestParams, iTsmCallback);
                } else if (accountInfo == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.u;
                    int[] iArr = this.X;
                    int i3 = iArr[7];
                    iArr[7] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(7, getAccountInfoRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getAppDetail(GetAppDetailRequestParams getAppDetailRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAppDetailRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetAppDetailRequestParams getAppDetailRequestParams2 = new GetAppDetailRequestParams();
            String reserve = getAppDetailRequestParams.getReserve();
            AppID appID = getAppDetailRequestParams.getAppID();
            String transType = getAppDetailRequestParams.getTransType();
            if (!TextUtils.isEmpty(reserve)) {
                getAppDetailRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getAppDetailRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            if (!TextUtils.isEmpty(transType)) {
                getAppDetailRequestParams2.setTransType(b(transType));
            }
            try {
                int appDetail = this.e.getAppDetail(getAppDetailRequestParams2, new b(this, 4, this.X[4], (byte) 0));
                if (-2 == appDetail) {
                    return a(4, getAppDetailRequestParams, iTsmCallback);
                } else if (appDetail == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.n;
                    int[] iArr = this.X;
                    int i3 = iArr[4];
                    iArr[4] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(4, getAppDetailRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0097, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r9;
     */
    @Deprecated
    public synchronized int getAppList(GetAppListRequestParams getAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAppListRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetAppListRequestParams getAppListRequestParams2 = new GetAppListRequestParams();
            String reserve = getAppListRequestParams.getReserve();
            String keyword = getAppListRequestParams.getKeyword();
            String[] status = getAppListRequestParams.getStatus();
            if (!TextUtils.isEmpty(reserve)) {
                getAppListRequestParams2.setReserve(b(reserve));
            }
            if (!TextUtils.isEmpty(keyword)) {
                getAppListRequestParams2.setKeyword(b(keyword));
            }
            if (status != null) {
                int length = status.length;
                String[] strArr = new String[length];
                for (int i3 = 0; i3 < length; i3++) {
                    if (!TextUtils.isEmpty(status[i3])) {
                        strArr[i3] = b(status[i3]);
                    }
                }
                getAppListRequestParams2.setStatus(strArr);
            }
            try {
                int appList = this.e.getAppList(getAppListRequestParams2, new b(this, 2, this.X[2], (byte) 0));
                if (-2 == appList) {
                    return a(2, getAppListRequestParams, iTsmCallback);
                } else if (appList == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.l;
                    int[] iArr = this.X;
                    int i4 = iArr[2];
                    iArr[2] = i4 + 1;
                    hashMap.put(String.valueOf(i4), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(2, getAppListRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getAppStatus(GetAppStatusRequestParams getAppStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAppStatusRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetAppStatusRequestParams getAppStatusRequestParams2 = new GetAppStatusRequestParams();
            String reserve = getAppStatusRequestParams.getReserve();
            AppID appID = getAppStatusRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                getAppStatusRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getAppStatusRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int appStatus = this.e.getAppStatus(getAppStatusRequestParams2, new b(this, 5, this.X[5], (byte) 0));
                if (-2 == appStatus) {
                    return a(5, getAppStatusRequestParams, iTsmCallback);
                } else if (appStatus == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.m;
                    int[] iArr = this.X;
                    int i3 = iArr[5];
                    iArr[5] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(5, getAppStatusRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0084, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x009b, code lost:
        return -3;
     */
    @Deprecated
    public synchronized int getAssociatedApp(GetAssociatedAppRequestParams getAssociatedAppRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getAssociatedAppRequestParams != null && iTsmCallback != null) {
            String encryptPan = getAssociatedAppRequestParams.getEncryptPan();
            if (TextUtils.isEmpty(encryptPan)) {
                return -3;
            }
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                GetAssociatedAppRequestParams getAssociatedAppRequestParams2 = new GetAssociatedAppRequestParams();
                String reserve = getAssociatedAppRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    getAssociatedAppRequestParams2.setReserve(b(reserve));
                }
                getAssociatedAppRequestParams2.setEncryptPan(b(encryptPan));
                try {
                    int associatedApp = this.e.getAssociatedApp(getAssociatedAppRequestParams2, new b(this, 1, this.X[1], (byte) 0));
                    if (-2 == associatedApp) {
                        return a(1, getAssociatedAppRequestParams, iTsmCallback);
                    } else if (associatedApp == 0) {
                        HashMap<String, ITsmCallback> hashMap = this.j;
                        int[] iArr = this.X;
                        int i3 = iArr[1];
                        iArr[1] = i3 + 1;
                        hashMap.put(String.valueOf(i3), iTsmCallback);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(1, getAssociatedAppRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002a, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00a6, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00bb, code lost:
        return -3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00c0, code lost:
        return -3;
     */
    @Deprecated
    public synchronized int getCardInfo(GetCardInfoRequestParams getCardInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getCardInfoRequestParams != null && iTsmCallback != null) {
            String[] appAID = getCardInfoRequestParams.getAppAID();
            int length = appAID.length;
            if (appAID != null) {
                if (length != 0) {
                    int i3 = 0;
                    while (i3 < length && appAID[i3] == null) {
                        i3++;
                    }
                    if (i3 == length) {
                        return -3;
                    }
                    if (!c()) {
                        i2 = -8;
                    } else if (this.e == null) {
                        i2 = -1;
                    } else if (a(this.c.getPackageName())) {
                        String[] strArr = new String[length];
                        for (int i4 = 0; i4 < length; i4++) {
                            if (appAID[i4] == null) {
                                strArr[i4] = appAID[i4];
                            } else {
                                strArr[i4] = b(appAID[i4]);
                            }
                        }
                        GetCardInfoRequestParams getCardInfoRequestParams2 = new GetCardInfoRequestParams();
                        getCardInfoRequestParams2.setAppAID(strArr);
                        String reserve = getCardInfoRequestParams.getReserve();
                        if (this.h) {
                            reserve = g(f(reserve));
                        }
                        if (!TextUtils.isEmpty(reserve)) {
                            getCardInfoRequestParams2.setReserve(b(reserve));
                        }
                        try {
                            int cardInfo = this.e.getCardInfo(getCardInfoRequestParams2, new b(this, 6, this.X[6], (byte) 0));
                            if (-2 == cardInfo) {
                                return a(6, getCardInfoRequestParams, iTsmCallback);
                            } else if (cardInfo == 0) {
                                HashMap<String, ITsmCallback> hashMap = this.A;
                                int[] iArr = this.X;
                                int i5 = iArr[6];
                                iArr[6] = i5 + 1;
                                hashMap.put(String.valueOf(i5), iTsmCallback);
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            throw new RemoteException();
                        }
                    } else {
                        return a(6, getCardInfoRequestParams, iTsmCallback);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r8;
     */
    public synchronized int getCardInfoBySamsungPay(GetCardInfoBySpayRequestParams getCardInfoBySpayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getCardInfoBySpayRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetCardInfoBySpayRequestParams getCardInfoBySpayRequestParams2 = new GetCardInfoBySpayRequestParams();
            String reserve = getCardInfoBySpayRequestParams.getReserve();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                getCardInfoBySpayRequestParams2.setReserve(b(reserve));
            }
            Amount amount = getCardInfoBySpayRequestParams.getAmount();
            if (amount != null) {
                String currencyType = amount.getCurrencyType();
                String productPrice = amount.getProductPrice();
                Amount amount2 = new Amount();
                if (!TextUtils.isEmpty(currencyType)) {
                    amount2.setCurrencyType(b(currencyType));
                }
                if (!TextUtils.isEmpty(productPrice)) {
                    amount2.setProductPrice(b(productPrice));
                }
                getCardInfoBySpayRequestParams2.setAmount(amount2);
            }
            this.I.put(String.valueOf(this.X[28]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[28];
                iArr[28] = i3 + 1;
                int cardInfoBySamsungPay = iTsmService.getCardInfoBySamsungPay(getCardInfoBySpayRequestParams2, new b(this, 28, i3, (byte) 0));
                if (cardInfoBySamsungPay != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.I;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[28] - 1;
                    iArr2[28] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != cardInfoBySamsungPay) {
                    return cardInfoBySamsungPay;
                }
                return a(28, getCardInfoBySpayRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(28, getCardInfoBySpayRequestParams, iTsmCallback);
        }
    }

    public Context getContext() {
        return this.c;
    }

    public int getCryptType() {
        return this.g;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0065, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getDefaultCard(GetDefaultCardRequestParams getDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetDefaultCardRequestParams getDefaultCardRequestParams2 = new GetDefaultCardRequestParams();
            if (getDefaultCardRequestParams != null) {
                String reserve = getDefaultCardRequestParams.getReserve();
                if (!TextUtils.isEmpty(reserve)) {
                    getDefaultCardRequestParams2.setReserve(b(reserve));
                }
            }
            try {
                int defaultCard = this.e.getDefaultCard(getDefaultCardRequestParams2, new b(this, 13, this.X[13], (byte) 0));
                if (-2 == defaultCard) {
                    return a(13, getDefaultCardRequestParams, iTsmCallback);
                } else if (defaultCard == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.y;
                    int[] iArr = this.X;
                    int i3 = iArr[13];
                    iArr[13] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(13, getDefaultCardRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002c, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c0, code lost:
        return -3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c5, code lost:
        return -3;
     */
    public synchronized int getEncryptData(GetEncryptDataRequestParams getEncryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback != null && getEncryptDataRequestParams != null) {
            int type = getEncryptDataRequestParams.getType();
            String pan = getEncryptDataRequestParams.getPan();
            if (type >= 2000) {
                if (type <= 2001) {
                    if (type == 2000 && TextUtils.isEmpty(pan)) {
                        return -3;
                    }
                    if (!c()) {
                        i2 = -8;
                    } else if (this.e == null) {
                        i2 = -1;
                    } else if (a(this.c.getPackageName())) {
                        GetEncryptDataRequestParams getEncryptDataRequestParams2 = new GetEncryptDataRequestParams();
                        if (type == 2000) {
                            getEncryptDataRequestParams2.setPan(b(pan));
                        }
                        getEncryptDataRequestParams2.setType(type);
                        String reserve = getEncryptDataRequestParams.getReserve();
                        if (this.h) {
                            reserve = g(f(reserve));
                        }
                        if (!TextUtils.isEmpty(reserve)) {
                            getEncryptDataRequestParams2.setReserve(b(reserve));
                        }
                        this.L.put(String.valueOf(this.X[31]), iTsmCallback);
                        try {
                            ITsmService iTsmService = this.e;
                            int[] iArr = this.X;
                            int i3 = iArr[31];
                            iArr[31] = i3 + 1;
                            int encryptData = iTsmService.getEncryptData(getEncryptDataRequestParams2, new b(this, 31, i3, (byte) 0));
                            if (encryptData != 0) {
                                HashMap<String, ITsmCallback> hashMap = this.L;
                                int[] iArr2 = this.X;
                                int i4 = iArr2[31] - 1;
                                iArr2[31] = i4;
                                hashMap.remove(String.valueOf(i4));
                            }
                            if (-2 != encryptData) {
                                return encryptData;
                            }
                            return a(31, getEncryptDataRequestParams, iTsmCallback);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            throw new RemoteException();
                        }
                    } else {
                        return a(31, getEncryptDataRequestParams, iTsmCallback);
                    }
                }
            }
        }
    }

    public synchronized int getListenerCount() {
        if (b == null) {
            return 0;
        }
        return b.size();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int getMessageDetails(GetMessageDetailsRequestParams getMessageDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.35")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                GetMessageDetailsRequestParams getMessageDetailsRequestParams2 = new GetMessageDetailsRequestParams();
                String str = "";
                if (getMessageDetailsRequestParams != null) {
                    str = getMessageDetailsRequestParams.getReserve();
                    Bundle params = getMessageDetailsRequestParams.getParams();
                    if (params != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.KEY_ENCRYPT_DATA, a(params));
                        getMessageDetailsRequestParams2.setParams(bundle);
                    }
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    getMessageDetailsRequestParams2.setReserve(b(str));
                }
                this.V.put(String.valueOf(this.X[44]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[44];
                    iArr[44] = i3 + 1;
                    int messageDetails = iTsmService.getMessageDetails(getMessageDetailsRequestParams2, new b(this, 44, i3, (byte) 0));
                    if (messageDetails != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.V;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[44] - 1;
                        iArr2[44] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != messageDetails) {
                        return messageDetails;
                    }
                    return a(44, getMessageDetailsRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(44, getMessageDetailsRequestParams, iTsmCallback);
            }
        }
    }

    public int getPubKey(int i2, String[] strArr) throws RemoteException {
        if (strArr == null || strArr.length == 0) {
            return -3;
        }
        if (!c()) {
            return -8;
        }
        if (this.e == null) {
            return -1;
        }
        try {
            return this.e.getPubKey(i2, strArr);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RemoteException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    @Deprecated
    public synchronized int getSEAppList(GetSeAppListRequestParams getSeAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetSeAppListRequestParams getSeAppListRequestParams2 = new GetSeAppListRequestParams();
            String str = "";
            if (getSeAppListRequestParams != null) {
                str = getSeAppListRequestParams.getReserve();
            }
            if (this.h) {
                str = g(f(str));
            }
            if (!TextUtils.isEmpty(str)) {
                getSeAppListRequestParams2.setReserve(b(str));
            }
            this.k.put(String.valueOf(this.X[3]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[3];
                iArr[3] = i3 + 1;
                int sEAppList = iTsmService.getSEAppList(getSeAppListRequestParams2, new b(this, 3, i3, (byte) 0));
                if (sEAppList != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.k;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[3] - 1;
                    iArr2[3] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != sEAppList) {
                    return sEAppList;
                }
                return a(3, getSeAppListRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(3, getSeAppListRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b1, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getSMSAuthCode(GetSMSAuthCodeRequestParams getSMSAuthCodeRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getSMSAuthCodeRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetSMSAuthCodeRequestParams getSMSAuthCodeRequestParams2 = new GetSMSAuthCodeRequestParams();
            String reserve = getSMSAuthCodeRequestParams.getReserve();
            AppID appID = getSMSAuthCodeRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                getSMSAuthCodeRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getSMSAuthCodeRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            String pan = getSMSAuthCodeRequestParams.getPan();
            String msisdn = getSMSAuthCodeRequestParams.getMsisdn();
            if (!TextUtils.isEmpty(pan)) {
                getSMSAuthCodeRequestParams2.setPan(b(pan));
            }
            if (!TextUtils.isEmpty(msisdn)) {
                getSMSAuthCodeRequestParams2.setMsisdn(b(msisdn));
            }
            try {
                int sMSAuthCode = this.e.getSMSAuthCode(getSMSAuthCodeRequestParams2, new b(this, 11, this.X[11], (byte) 0));
                if (-2 == sMSAuthCode) {
                    return a(11, getSMSAuthCodeRequestParams, iTsmCallback);
                } else if (sMSAuthCode == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.r;
                    int[] iArr = this.X;
                    int i3 = iArr[11];
                    iArr[11] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(11, getSMSAuthCodeRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int getSeId(GetSeIdRequestParams getSeIdRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetSeIdRequestParams getSeIdRequestParams2 = new GetSeIdRequestParams();
            String str = "";
            if (getSeIdRequestParams != null) {
                str = getSeIdRequestParams.getReserve();
            }
            if (this.h) {
                str = g(f(str));
            }
            if (!TextUtils.isEmpty(str)) {
                getSeIdRequestParams2.setReserve(b(str));
            }
            this.z.put(String.valueOf(this.X[12]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[12];
                iArr[12] = i3 + 1;
                int sEId = iTsmService.getSEId(getSeIdRequestParams2, new b(this, 12, i3, (byte) 0));
                if (sEId != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.z;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[12] - 1;
                    iArr2[12] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != sEId) {
                    return sEId;
                }
                return a(12, getSeIdRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(12, getSeIdRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getTransElements(GetTransElementsRequestParams getTransElementsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getTransElementsRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetTransElementsRequestParams getTransElementsRequestParams2 = new GetTransElementsRequestParams();
            String reserve = getTransElementsRequestParams.getReserve();
            AppID appID = getTransElementsRequestParams.getAppID();
            String transType = getTransElementsRequestParams.getTransType();
            if (!TextUtils.isEmpty(reserve)) {
                getTransElementsRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getTransElementsRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            if (!TextUtils.isEmpty(transType)) {
                getTransElementsRequestParams2.setTransType(b(transType));
            }
            try {
                int transElements = this.e.getTransElements(getTransElementsRequestParams2, new b(this, 9, this.X[9], (byte) 0));
                if (-2 == transElements) {
                    return a(9, getTransElementsRequestParams, iTsmCallback);
                } else if (transElements == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.o;
                    int[] iArr = this.X;
                    int i3 = iArr[9];
                    iArr[9] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(9, getTransElementsRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r7;
     */
    @Deprecated
    public synchronized int getTransRecord(GetTransRecordRequestParams getTransRecordRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (getTransRecordRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            GetTransRecordRequestParams getTransRecordRequestParams2 = new GetTransRecordRequestParams();
            String reserve = getTransRecordRequestParams.getReserve();
            AppID appID = getTransRecordRequestParams.getAppID();
            if (!TextUtils.isEmpty(reserve)) {
                getTransRecordRequestParams2.setReserve(b(reserve));
            }
            if (appID != null) {
                String appAid = appID.getAppAid();
                String appVersion = appID.getAppVersion();
                if (!TextUtils.isEmpty(appAid) && !TextUtils.isEmpty(appVersion)) {
                    getTransRecordRequestParams2.setAppID(new AppID(b(appAid), b(appVersion)));
                }
            }
            try {
                int transRecord = this.e.getTransRecord(getTransRecordRequestParams2, new b(this, 10, this.X[10], (byte) 0));
                if (-2 == transRecord) {
                    return a(10, getTransRecordRequestParams, iTsmCallback);
                } else if (transRecord == 0) {
                    HashMap<String, ITsmCallback> hashMap = this.t;
                    int[] iArr = this.X;
                    int i3 = iArr[10];
                    iArr[10] = i3 + 1;
                    hashMap.put(String.valueOf(i3), iTsmCallback);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(10, getTransRecordRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int getTransactionDetails(GetTransactionDetailsRequestParams getTransactionDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.35")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                GetTransactionDetailsRequestParams getTransactionDetailsRequestParams2 = new GetTransactionDetailsRequestParams();
                String str = "";
                if (getTransactionDetailsRequestParams != null) {
                    str = getTransactionDetailsRequestParams.getReserve();
                    Bundle params = getTransactionDetailsRequestParams.getParams();
                    if (params != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.KEY_ENCRYPT_DATA, a(params));
                        getTransactionDetailsRequestParams2.setParams(bundle);
                    }
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    getTransactionDetailsRequestParams2.setReserve(b(str));
                }
                this.U.put(String.valueOf(this.X[43]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[43];
                    iArr[43] = i3 + 1;
                    int transactionDetails = iTsmService.getTransactionDetails(getTransactionDetailsRequestParams2, new b(this, 43, i3, (byte) 0));
                    if (transactionDetails != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.U;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[43] - 1;
                        iArr2[43] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != transactionDetails) {
                        return transactionDetails;
                    }
                    return a(43, getTransactionDetailsRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(43, getTransactionDetailsRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int getVendorPayStatus(GetVendorPayStatusRequestParams getVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.20")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                GetVendorPayStatusRequestParams getVendorPayStatusRequestParams2 = new GetVendorPayStatusRequestParams();
                String str = "";
                if (getVendorPayStatusRequestParams != null) {
                    str = getVendorPayStatusRequestParams.getReserve();
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    getVendorPayStatusRequestParams2.setReserve(b(str));
                }
                this.N.put(String.valueOf(this.X[36]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[36];
                    iArr[36] = i3 + 1;
                    int vendorPayStatus = iTsmService.getVendorPayStatus(getVendorPayStatusRequestParams2, new b(this, 36, i3, (byte) 0));
                    if (vendorPayStatus != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.N;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[36] - 1;
                        iArr2[36] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != vendorPayStatus) {
                        return vendorPayStatus;
                    }
                    return a(36, getVendorPayStatusRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(36, getVendorPayStatusRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0079, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0090, code lost:
        return -3;
     */
    public synchronized int hideAppApply(HideAppApplyRequestParams hideAppApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (hideAppApplyRequestParams != null && iTsmCallback != null) {
            String applyId = hideAppApplyRequestParams.getApplyId();
            if (TextUtils.isEmpty(applyId)) {
                return -3;
            }
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                String b2 = b(applyId);
                HideAppApplyRequestParams hideAppApplyRequestParams2 = new HideAppApplyRequestParams();
                hideAppApplyRequestParams2.setApplyId(b2);
                String reserve = hideAppApplyRequestParams.getReserve();
                if (!TextUtils.isEmpty(reserve)) {
                    hideAppApplyRequestParams2.setReserve(b(reserve));
                }
                try {
                    int hideAppApply = this.e.hideAppApply(hideAppApplyRequestParams2, new b(this, 24, this.X[24], (byte) 0));
                    if (-2 == hideAppApply) {
                        return a(24, hideAppApplyRequestParams, iTsmCallback);
                    } else if (hideAppApply == 0) {
                        HashMap<String, ITsmCallback> hashMap = this.G;
                        int[] iArr = this.X;
                        int i3 = iArr[24];
                        iArr[24] = i3 + 1;
                        hashMap.put(String.valueOf(i3), iTsmCallback);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(24, hideAppApplyRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0071, code lost:
        throw new android.os.RemoteException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0009, code lost:
        return r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:20:0x0046, B:26:0x0057] */
    public synchronized int hideKeyboard() throws RemoteException {
        int i2;
        int i3;
        if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            if (d("01.00.24")) {
                HideSafetyKeyboardRequestParams hideSafetyKeyboardRequestParams = new HideSafetyKeyboardRequestParams();
                String str = "";
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    hideSafetyKeyboardRequestParams.setReserve(b(str));
                }
                i3 = this.e.hideSafetyKeyboard(hideSafetyKeyboardRequestParams);
            } else {
                i3 = this.e.hideKeyboard();
            }
            if (-2 != i3) {
                return i3;
            }
            return a(34, (SafetyKeyboardRequestParams) null, 0, (OnSafetyKeyboardCallback) null, (Context) null);
        } else {
            return a(34, (SafetyKeyboardRequestParams) null, 0, (OnSafetyKeyboardCallback) null, (Context) null);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int init(InitRequestParams initRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            InitRequestParams initRequestParams2 = new InitRequestParams();
            String str = "";
            if (initRequestParams != null) {
                str = initRequestParams.getReserve();
                String signature = initRequestParams.getSignature();
                if (!TextUtils.isEmpty(signature)) {
                    initRequestParams2.setSignature(b(signature));
                }
            }
            if (this.h) {
                str = g(f(str));
            }
            if (!TextUtils.isEmpty(str)) {
                initRequestParams2.setReserve(b(str));
            }
            this.i.put(String.valueOf(this.X[0]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[0];
                iArr[0] = i3 + 1;
                int init = iTsmService.init(initRequestParams2, new b(this, 0, i3, (byte) 0));
                if (init != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.i;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[0] - 1;
                    iArr2[0] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != init) {
                    return init;
                }
                return a(0, initRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(0, initRequestParams, iTsmCallback);
        }
    }

    public boolean isConnected() {
        return this.f;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d8, code lost:
        return r8;
     */
    public synchronized int onlinePaymentVerify(OnlinePaymentVerifyRequestParams onlinePaymentVerifyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (onlinePaymentVerifyRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.21")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                OnlinePaymentVerifyRequestParams onlinePaymentVerifyRequestParams2 = new OnlinePaymentVerifyRequestParams();
                Bundle resource = onlinePaymentVerifyRequestParams.getResource();
                if (resource != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.KEY_ENCRYPT_DATA, a(resource));
                    onlinePaymentVerifyRequestParams2.setResource(bundle);
                }
                String orderNumber = onlinePaymentVerifyRequestParams.getOrderNumber();
                String aId = onlinePaymentVerifyRequestParams.getAId();
                if (!TextUtils.isEmpty(orderNumber)) {
                    onlinePaymentVerifyRequestParams2.setOrderNumber(b(orderNumber));
                }
                if (!TextUtils.isEmpty(aId)) {
                    onlinePaymentVerifyRequestParams2.setAId(b(aId));
                }
                String reserve = onlinePaymentVerifyRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    onlinePaymentVerifyRequestParams2.setReserve(b(reserve));
                }
                this.Q.put(String.valueOf(this.X[39]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[39];
                    iArr[39] = i3 + 1;
                    int onlinePaymentVerify = iTsmService.onlinePaymentVerify(onlinePaymentVerifyRequestParams2, new b(this, 39, i3, (byte) 0));
                    if (onlinePaymentVerify != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.Q;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[39] - 1;
                        iArr2[39] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != onlinePaymentVerify) {
                        return onlinePaymentVerify;
                    }
                    return a(39, onlinePaymentVerifyRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(39, onlinePaymentVerifyRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ae, code lost:
        return -3;
     */
    public synchronized int openChannel(OpenChannelRequestParams openChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (openChannelRequestParams != null && iTsmCallback != null) {
            String appAID = openChannelRequestParams.getAppAID();
            if (TextUtils.isEmpty(appAID)) {
                return -3;
            }
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                String b2 = b(appAID);
                OpenChannelRequestParams openChannelRequestParams2 = new OpenChannelRequestParams();
                openChannelRequestParams2.setAppAID(b2);
                String reserve = openChannelRequestParams.getReserve();
                if (this.h) {
                    reserve = g(f(reserve));
                }
                if (!TextUtils.isEmpty(reserve)) {
                    openChannelRequestParams2.setReserve(b(reserve));
                }
                this.w.put(String.valueOf(this.X[20]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[20];
                    iArr[20] = i3 + 1;
                    int openChannel = iTsmService.openChannel(openChannelRequestParams2, new b(this, 20, i3, (byte) 0));
                    if (openChannel != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.w;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[20] - 1;
                        iArr2[20] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != openChannel) {
                        return openChannel;
                    }
                    return a(20, openChannelRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(20, openChannelRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int preDownload(PreDownloadRequestParams preDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.26")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                PreDownloadRequestParams preDownloadRequestParams2 = new PreDownloadRequestParams();
                String str = "";
                if (preDownloadRequestParams != null) {
                    str = preDownloadRequestParams.getReserve();
                    Bundle params = preDownloadRequestParams.getParams();
                    if (params != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.KEY_ENCRYPT_DATA, a(params));
                        preDownloadRequestParams2.setParams(bundle);
                    }
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    preDownloadRequestParams2.setReserve(b(str));
                }
                this.R.put(String.valueOf(this.X[40]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[40];
                    iArr[40] = i3 + 1;
                    int preDownload = iTsmService.preDownload(preDownloadRequestParams2, new b(this, 40, i3, (byte) 0), iTsmProgressCallback);
                    if (preDownload != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.R;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[40] - 1;
                        iArr2[40] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != preDownload) {
                        return preDownload;
                    }
                    return a(40, preDownloadRequestParams, iTsmCallback, iTsmProgressCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(40, preDownloadRequestParams, iTsmCallback, iTsmProgressCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int queryVendorPayStatus(QueryVendorPayStatusRequestParams queryVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!d("01.00.27")) {
            return -8;
        } else {
            if (!c()) {
                return -8;
            }
            if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                QueryVendorPayStatusRequestParams queryVendorPayStatusRequestParams2 = new QueryVendorPayStatusRequestParams();
                String str = "";
                if (queryVendorPayStatusRequestParams != null) {
                    str = queryVendorPayStatusRequestParams.getReserve();
                }
                if (this.h) {
                    str = g(f(str));
                }
                if (!TextUtils.isEmpty(str)) {
                    queryVendorPayStatusRequestParams2.setReserve(b(str));
                }
                this.S.put(String.valueOf(this.X[41]), iTsmCallback);
                try {
                    ITsmService iTsmService = this.e;
                    int[] iArr = this.X;
                    int i3 = iArr[41];
                    iArr[41] = i3 + 1;
                    int queryVendorPayStatus = iTsmService.queryVendorPayStatus(queryVendorPayStatusRequestParams2, new b(this, 41, i3, (byte) 0));
                    if (queryVendorPayStatus != 0) {
                        HashMap<String, ITsmCallback> hashMap = this.S;
                        int[] iArr2 = this.X;
                        int i4 = iArr2[41] - 1;
                        iArr2[41] = i4;
                        hashMap.remove(String.valueOf(i4));
                    }
                    if (-2 != queryVendorPayStatus) {
                        return queryVendorPayStatus;
                    }
                    return a(41, queryVendorPayStatusRequestParams, iTsmCallback);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(41, queryVendorPayStatusRequestParams, iTsmCallback);
            }
        }
    }

    public synchronized void removeConnectionListener(UPTsmConnectionListener uPTsmConnectionListener) {
        if (uPTsmConnectionListener != null) {
            b.remove(uPTsmConnectionListener);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        return r8;
     */
    public synchronized int sendApdu(SendApduRequestParams sendApduRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (sendApduRequestParams == null || iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            SendApduRequestParams sendApduRequestParams2 = new SendApduRequestParams();
            String reserve = sendApduRequestParams.getReserve();
            String channel = sendApduRequestParams.getChannel();
            String hexApdu = sendApduRequestParams.getHexApdu();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                sendApduRequestParams2.setReserve(b(reserve));
            }
            if (!TextUtils.isEmpty(channel)) {
                sendApduRequestParams2.setChannel(b(channel));
            }
            if (!TextUtils.isEmpty(hexApdu)) {
                sendApduRequestParams2.setHexApdu(b(hexApdu));
            }
            this.x.put(String.valueOf(this.X[22]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[22];
                iArr[22] = i3 + 1;
                int sendApdu = iTsmService.sendApdu(sendApduRequestParams2, new b(this, 22, i3, (byte) 0));
                if (sendApdu != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.x;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[22] - 1;
                    iArr2[22] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != sendApdu) {
                    return sendApdu;
                }
                return a(22, sendApduRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(22, sendApduRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0079, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0090, code lost:
        return -3;
     */
    @Deprecated
    public synchronized int setDefaultCard(SetDefaultCardRequestParams setDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (setDefaultCardRequestParams != null && iTsmCallback != null) {
            String appAID = setDefaultCardRequestParams.getAppAID();
            if (TextUtils.isEmpty(appAID)) {
                return -3;
            }
            if (!c()) {
                i2 = -8;
            } else if (this.e == null) {
                i2 = -1;
            } else if (a(this.c.getPackageName())) {
                String b2 = b(appAID);
                SetDefaultCardRequestParams setDefaultCardRequestParams2 = new SetDefaultCardRequestParams();
                setDefaultCardRequestParams2.setAppAID(b2);
                String reserve = setDefaultCardRequestParams.getReserve();
                if (!TextUtils.isEmpty(reserve)) {
                    setDefaultCardRequestParams2.setReserve(b(reserve));
                }
                try {
                    int defaultCard = this.e.setDefaultCard(setDefaultCardRequestParams2, new b(this, 14, this.X[14], (byte) 0));
                    if (-2 == defaultCard) {
                        return a(14, setDefaultCardRequestParams, iTsmCallback);
                    } else if (defaultCard == 0) {
                        HashMap<String, ITsmCallback> hashMap = this.E;
                        int[] iArr = this.X;
                        int i3 = iArr[14];
                        iArr[14] = i3 + 1;
                        hashMap.put(String.valueOf(i3), iTsmCallback);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new RemoteException();
                }
            } else {
                return a(14, setDefaultCardRequestParams, iTsmCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r5;
     */
    public synchronized int setSafetyKeyboardBitmap(SafetyKeyboardRequestParams safetyKeyboardRequestParams) throws RemoteException {
        int i2;
        if (safetyKeyboardRequestParams == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            String reserve = safetyKeyboardRequestParams.getReserve();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                safetyKeyboardRequestParams.setReserve(b(reserve));
            }
            try {
                int safetyKeyboardBitmap = this.e.setSafetyKeyboardBitmap(safetyKeyboardRequestParams);
                if (-2 != safetyKeyboardBitmap) {
                    return safetyKeyboardBitmap;
                }
                return a(32, safetyKeyboardRequestParams, null);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(32, safetyKeyboardRequestParams, null);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        return r8;
     */
    public synchronized int setSamsungDefaultWallet(SetSamsungDefWalletRequestParams setSamsungDefWalletRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
        int i2;
        if (iTsmCallback == null) {
            i2 = -3;
        } else if (!c()) {
            i2 = -8;
        } else if (this.e == null) {
            i2 = -1;
        } else if (a(this.c.getPackageName())) {
            SetSamsungDefWalletRequestParams setSamsungDefWalletRequestParams2 = new SetSamsungDefWalletRequestParams();
            if (setSamsungDefWalletRequestParams != null) {
                String reserve = setSamsungDefWalletRequestParams.getReserve();
                if (!TextUtils.isEmpty(reserve)) {
                    setSamsungDefWalletRequestParams2.setReserve(b(reserve));
                }
            }
            this.K.put(String.valueOf(this.X[30]), iTsmCallback);
            try {
                ITsmService iTsmService = this.e;
                int[] iArr = this.X;
                int i3 = iArr[30];
                iArr[30] = i3 + 1;
                int samsungDefaultWallet = iTsmService.setSamsungDefaultWallet(setSamsungDefWalletRequestParams2, new b(this, 30, i3, (byte) 0));
                if (samsungDefaultWallet != 0) {
                    HashMap<String, ITsmCallback> hashMap = this.K;
                    int[] iArr2 = this.X;
                    int i4 = iArr2[30] - 1;
                    iArr2[30] = i4;
                    hashMap.remove(String.valueOf(i4));
                }
                if (-2 != samsungDefaultWallet) {
                    return samsungDefaultWallet;
                }
                return a(30, setSamsungDefWalletRequestParams, iTsmCallback);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(30, setSamsungDefWalletRequestParams, iTsmCallback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        return r5;
     */
    public synchronized int showSafetyKeyboard(SafetyKeyboardRequestParams safetyKeyboardRequestParams, int i2, OnSafetyKeyboardCallback onSafetyKeyboardCallback, Context context) throws RemoteException {
        int i3;
        if (safetyKeyboardRequestParams == null || i2 < 2000 || i2 > 2001) {
            i3 = -3;
        } else if (!c()) {
            i3 = -8;
        } else if (this.e == null) {
            i3 = -1;
        } else if (a(this.c.getPackageName())) {
            this.W.put(this.c.getPackageName(), new a(context));
            String reserve = safetyKeyboardRequestParams.getReserve();
            if (this.h) {
                reserve = g(f(reserve));
            }
            if (!TextUtils.isEmpty(reserve)) {
                safetyKeyboardRequestParams.setReserve(b(reserve));
            }
            try {
                int showSafetyKeyboard = this.e.showSafetyKeyboard(safetyKeyboardRequestParams, i2, onSafetyKeyboardCallback, new a());
                if (showSafetyKeyboard != 0) {
                    this.W.remove(this.c.getPackageName());
                }
                if (-2 != showSafetyKeyboard) {
                    return showSafetyKeyboard;
                }
                return a(1000, safetyKeyboardRequestParams, i2, onSafetyKeyboardCallback, context);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new RemoteException();
            }
        } else {
            return a(1000, safetyKeyboardRequestParams, i2, onSafetyKeyboardCallback, context);
        }
    }

    public void unbind() {
        if (this.d != null && this.f) {
            this.c.unbindService(this.d);
            this.f = false;
        }
    }
}
