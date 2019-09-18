package com.unionpay.tsmservice.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.unionpay.tsmservice.ITsmCallback;
import com.unionpay.tsmservice.OnSafetyKeyboardCallback;
import com.unionpay.tsmservice.UPTsmAddon;
import com.unionpay.tsmservice.data.NinePatchInfo;
import com.unionpay.tsmservice.request.GetEncryptDataRequestParams;
import com.unionpay.tsmservice.request.SafetyKeyboardRequestParams;
import com.unionpay.tsmservice.result.GetEncryptDataResult;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UPSaftyKeyboard {
    private boolean A;
    private boolean B;
    private boolean C;
    private int D;
    private int E;
    private int F;
    private int G;
    private int H;
    private int I;
    private Typeface J;
    private boolean K;
    /* access modifiers changed from: private */
    public OnShowListener L;
    /* access modifiers changed from: private */
    public OnHideListener M;
    /* access modifiers changed from: private */
    public OnEditorListener N;
    /* access modifiers changed from: private */
    public OnSafetyKeyboardCallback.Stub O;
    private UPTsmAddon.UPTsmConnectionListener P;
    private Handler.Callback Q;
    /* access modifiers changed from: private */
    public final Handler R;
    private Context a;
    /* access modifiers changed from: private */
    public UPTsmAddon b;
    /* access modifiers changed from: private */
    public int c;
    /* access modifiers changed from: private */
    public int d;
    private String e;
    private int f;
    private int g;
    private int h;
    private int i;
    private int j;
    private int k;
    private int l;
    private int m;
    private int n;
    private int o;
    private int p;
    private int q;
    private int r;
    private int s;
    private int t;
    private int u;
    private int v;
    private int w;
    private int x;
    private int y;
    private boolean z;

    public interface OnEditorListener {
        void onEditorChanged(int i);
    }

    public interface OnHideListener {
        void onHide();
    }

    public interface OnShowListener {
        void onShow();
    }

    class a extends OnSafetyKeyboardCallback.Stub {
        a() {
        }

        public final void onEditorChanged(int i) throws RemoteException {
            Message obtain = Message.obtain();
            obtain.what = 2;
            obtain.arg1 = i;
            UPSaftyKeyboard.this.R.sendMessage(obtain);
        }

        public final void onHide() throws RemoteException {
            UPSaftyKeyboard.this.R.sendEmptyMessage(1);
        }

        public final void onShow() throws RemoteException {
            UPSaftyKeyboard.this.R.sendEmptyMessage(0);
        }
    }

    class b extends FutureTask<String> {

        class a extends ITsmCallback.Stub {
            a() {
            }

            public final void onError(String str, String str2) throws RemoteException {
                b.this.set("");
            }

            public final void onResult(Bundle bundle) throws RemoteException {
                bundle.setClassLoader(GetEncryptDataResult.class.getClassLoader());
                b.this.set(((GetEncryptDataResult) bundle.get("result")).getData());
            }
        }

        public b() {
            super(new Callable<String>() {
                public final /* synthetic */ Object call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
        }

        /* access modifiers changed from: private */
        public String a(TimeUnit timeUnit) {
            try {
                String str = (String) get(2000, timeUnit);
                cancel(true);
                return str;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e2) {
                e2.printStackTrace();
            } catch (TimeoutException e3) {
                e3.printStackTrace();
            } catch (Throwable th) {
                cancel(true);
                throw th;
            }
            cancel(true);
            return "";
        }

        static /* synthetic */ void a(b bVar, String str) {
            GetEncryptDataRequestParams getEncryptDataRequestParams = new GetEncryptDataRequestParams();
            getEncryptDataRequestParams.setPan(str);
            getEncryptDataRequestParams.setType(UPSaftyKeyboard.this.c);
            try {
                UPSaftyKeyboard.this.b.getEncryptData(getEncryptDataRequestParams, new a());
            } catch (RemoteException e) {
                e.printStackTrace();
                bVar.set("");
            }
        }
    }

    public UPSaftyKeyboard(Context context, int i2) throws RemoteException {
        this(context, i2, null);
    }

    public UPSaftyKeyboard(Context context, int i2, Drawable drawable) throws RemoteException {
        this.a = null;
        this.f = -1;
        this.g = -1;
        this.h = -1;
        this.i = -1;
        this.j = -1;
        this.k = -1;
        this.l = -1;
        this.m = -1;
        this.n = -1;
        this.o = -1;
        this.p = -1;
        this.q = -1;
        this.r = -1;
        this.s = -1;
        this.t = -1;
        this.u = -1;
        this.v = -1;
        this.w = 0;
        this.x = 0;
        this.y = 1;
        this.z = false;
        this.A = false;
        this.B = true;
        this.C = false;
        this.D = -1;
        this.E = -1;
        this.F = -1;
        this.G = -1;
        this.H = -1;
        this.I = -16777216;
        this.K = false;
        this.Q = new Handler.Callback() {
            public final boolean handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        if (UPSaftyKeyboard.this.L != null) {
                            UPSaftyKeyboard.this.L.onShow();
                        }
                        return true;
                    case 1:
                        if (UPSaftyKeyboard.this.M != null) {
                            UPSaftyKeyboard.this.M.onHide();
                        }
                        OnSafetyKeyboardCallback.Stub unused = UPSaftyKeyboard.this.O = null;
                        return true;
                    case 2:
                        if (UPSaftyKeyboard.this.N != null) {
                            int unused2 = UPSaftyKeyboard.this.d = message.arg1;
                            UPSaftyKeyboard.this.N.onEditorChanged(UPSaftyKeyboard.this.d);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.R = new Handler(Looper.getMainLooper(), this.Q);
        this.a = context;
        this.c = i2;
        if (i2 < 2000 || i2 > 2001) {
            throw new IllegalArgumentException("Type is error");
        }
        this.b = UPTsmAddon.getInstance(this.a);
        if (!this.b.isConnected()) {
            this.P = new UPTsmAddon.UPTsmConnectionListener() {
                public final void onTsmConnected() {
                    UPSaftyKeyboard.this.a();
                }

                public final void onTsmDisconnected() {
                }
            };
            this.b.addConnectionListener(this.P);
            this.b.bind();
        } else {
            a();
        }
        if (drawable != null) {
            try {
                setKeyboardBackground(drawable);
            } catch (KeyboardDrawableErrorException e2) {
                e2.printStackTrace();
            }
        }
    }

    private String a(String str) {
        b bVar = new b();
        b.a(bVar, str);
        return bVar.a(TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: private */
    public void a() {
        if (this.b != null) {
            try {
                this.b.clearEncryptData(this.c);
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void a(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setDoneForeBitmap(((BitmapDrawable) drawable).getBitmap());
            } else if (c2 == 1) {
                throw new KeyboardDrawableErrorException();
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    private void a(SafetyKeyboardRequestParams safetyKeyboardRequestParams) throws RemoteException {
        this.b.setSafetyKeyboardBitmap(safetyKeyboardRequestParams);
    }

    private void b(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setDelForeBitmap(((BitmapDrawable) drawable).getBitmap());
            } else if (c2 == 1) {
                throw new KeyboardDrawableErrorException();
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    private static int c(Drawable drawable) {
        if (drawable == null) {
            return -1;
        }
        if (drawable instanceof BitmapDrawable) {
            return 0;
        }
        if (drawable instanceof ColorDrawable) {
            return 1;
        }
        return drawable instanceof NinePatchDrawable ? 2 : -1;
    }

    private static NinePatchInfo d(Drawable drawable) {
        NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) drawable;
        NinePatchInfo ninePatchInfo = new NinePatchInfo();
        Rect rect = new Rect();
        ninePatchDrawable.getPadding(rect);
        ninePatchInfo.setPadding(rect);
        Drawable.ConstantState constantState = ninePatchDrawable.getConstantState();
        try {
            Field declaredField = Class.forName("android.graphics.drawable.NinePatchDrawable$NinePatchState").getDeclaredField("mNinePatch");
            declaredField.setAccessible(true);
            Bitmap bitmap = (Bitmap) Class.forName("android.graphics.NinePatch").getDeclaredMethod("getBitmap", new Class[0]).invoke(declaredField.get(constantState), new Object[0]);
            ninePatchInfo.setBitmap(bitmap);
            ninePatchInfo.setChunk(bitmap.getNinePatchChunk());
            return ninePatchInfo;
        } catch (Exception e2) {
            e2.printStackTrace();
            return ninePatchInfo;
        }
    }

    public synchronized boolean clearPwd() {
        this.d = 0;
        int i2 = -5;
        try {
            i2 = this.b.clearEncryptData(this.c);
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        return i2 == 0;
    }

    public void enableLightStatusBar(boolean z2) {
        this.K = z2;
    }

    public int getCurrentPinLength() {
        return this.d;
    }

    public String getInput() {
        return a("");
    }

    public String getInput(String str) {
        return this.c != 2000 ? "" : a(str);
    }

    public boolean hide() {
        int i2;
        try {
            i2 = this.b.hideKeyboard();
        } catch (RemoteException e2) {
            e2.printStackTrace();
            i2 = -5;
        }
        return i2 == 0;
    }

    public void setConfirmBtnOutPaddingRight(int i2) {
        this.v = i2;
    }

    public void setConfirmBtnSize(int i2, int i3) {
        this.h = i2;
        this.i = i3;
    }

    public void setDelKeyDrawable(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        if (drawable != null) {
            b(drawable);
        }
    }

    public void setDelKeyDrawable(Drawable drawable, Drawable drawable2) throws KeyboardDrawableErrorException, RemoteException {
        if (drawable != null) {
            b(drawable);
        }
        if (drawable2 != null) {
            int c2 = c(drawable2);
            if (c2 != -1) {
                SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
                if (c2 == 0) {
                    safetyKeyboardRequestParams.setDelBgBitmap(((BitmapDrawable) drawable2).getBitmap());
                    safetyKeyboardRequestParams.setDelBgColor(-1);
                } else if (c2 == 1) {
                    safetyKeyboardRequestParams.setDelBgColor(((ColorDrawable) drawable2).getColor());
                } else if (c2 == 2) {
                    safetyKeyboardRequestParams.setDelKeyBgNinePatch(d(drawable2));
                }
                a(safetyKeyboardRequestParams);
                return;
            }
            throw new KeyboardDrawableErrorException();
        }
    }

    public void setDoneKeyDrawable(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        if (drawable != null) {
            a(drawable);
        }
    }

    public void setDoneKeyDrawable(Drawable drawable, Drawable drawable2) throws KeyboardDrawableErrorException, RemoteException {
        if (drawable != null) {
            a(drawable);
        }
        if (drawable2 != null) {
            int c2 = c(drawable2);
            if (c2 != -1) {
                SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
                if (c2 == 0) {
                    safetyKeyboardRequestParams.setDoneBgBitmap(((BitmapDrawable) drawable2).getBitmap());
                    safetyKeyboardRequestParams.setDoneBgColor(-1);
                } else if (c2 == 1) {
                    safetyKeyboardRequestParams.setDoneBgColor(((ColorDrawable) drawable2).getColor());
                } else if (c2 == 2) {
                    safetyKeyboardRequestParams.setDoneKeyBgNinePatch(d(drawable2));
                }
                a(safetyKeyboardRequestParams);
                return;
            }
            throw new KeyboardDrawableErrorException();
        }
    }

    public void setDoneKeyEnable(boolean z2) {
        this.B = z2;
    }

    public void setDoneKeyRightMode(boolean z2) {
        this.A = z2;
    }

    public void setKeyAreaPadding(int i2, int i3, int i4, int i5) {
        this.q = i2;
        this.r = i3;
        this.s = i4;
        this.t = i5;
    }

    public void setKeyBoardSize(int i2, int i3) {
        this.f = i2;
        this.g = i3;
    }

    public void setKeyboardAudio(boolean z2) {
        this.z = z2;
    }

    public void setKeyboardBackground(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setKeyboardBgBitmap(((BitmapDrawable) drawable).getBitmap());
                safetyKeyboardRequestParams.setKeyboardBgColor(-1);
            } else if (c2 == 1) {
                safetyKeyboardRequestParams.setKeyboardBgColor(((ColorDrawable) drawable).getColor());
            } else if (c2 == 2) {
                safetyKeyboardRequestParams.setKeyboardBgNinePatch(d(drawable));
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setKeyboardPadding(int i2, int i3, int i4, int i5) {
        this.m = i2;
        this.n = i3;
        this.o = i4;
        this.p = i5;
    }

    public void setKeyboardStartPosition(int i2, int i3) {
        this.w = i2;
        this.x = i3;
        this.y = 0;
    }

    public void setKeyboardVibrate(boolean z2) {
        this.C = z2;
    }

    public void setNumKeyBackgroud(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setNumBgBitmap(((BitmapDrawable) drawable).getBitmap());
                safetyKeyboardRequestParams.setNumBgColor(-1);
            } else if (c2 == 1) {
                safetyKeyboardRequestParams.setNumBgColor(((ColorDrawable) drawable).getColor());
            } else if (c2 == 2) {
                safetyKeyboardRequestParams.setNumKeyBgNinePatch(d(drawable));
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setNumKeyMargin(int i2, int i3) {
        this.k = i2;
        this.l = i3;
    }

    public void setNumberKeyColor(int i2) {
        this.I = i2;
    }

    public void setNumberKeyDrawable(Drawable[] drawableArr) throws KeyboardDrawableErrorException, RemoteException {
        char c2 = 65535;
        if (drawableArr != null && drawableArr.length > 0) {
            int length = drawableArr.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    c2 = 0;
                    break;
                } else if (!(drawableArr[i2] instanceof BitmapDrawable)) {
                    break;
                } else {
                    i2++;
                }
            }
        }
        if (c2 == 0) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            ArrayList arrayList = new ArrayList();
            for (BitmapDrawable bitmapDrawable : drawableArr) {
                if (bitmapDrawable.getBitmap() != null) {
                    arrayList.add(bitmapDrawable.getBitmap());
                }
            }
            safetyKeyboardRequestParams.setNumForeBitmaps(arrayList);
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setNumberKeySize(int i2) {
        this.u = i2;
    }

    public void setOnEditorListener(OnEditorListener onEditorListener) {
        this.N = onEditorListener;
    }

    public void setOnHideListener(OnHideListener onHideListener) {
        this.M = onHideListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.L = onShowListener;
    }

    public void setTitleBackground(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setTitleBgBitmap(((BitmapDrawable) drawable).getBitmap());
                safetyKeyboardRequestParams.setTitleBgColor(-1);
            } else if (c2 == 1) {
                safetyKeyboardRequestParams.setTitleBgColor(((ColorDrawable) drawable).getColor());
            } else if (c2 == 2) {
                safetyKeyboardRequestParams.setTitleBgNinePatch(d(drawable));
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setTitleColor(int i2) {
        this.G = i2;
    }

    public void setTitleConfirmDrawable(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setTitleDropBitmap(((BitmapDrawable) drawable).getBitmap());
            } else if (c2 == 1) {
                throw new KeyboardDrawableErrorException();
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setTitleDrawable(Drawable drawable) throws KeyboardDrawableErrorException, RemoteException {
        int c2 = c(drawable);
        if (c2 != -1) {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            if (c2 == 0) {
                safetyKeyboardRequestParams.setTitleIconBitmap(((BitmapDrawable) drawable).getBitmap());
            } else if (c2 == 1) {
                throw new KeyboardDrawableErrorException();
            }
            a(safetyKeyboardRequestParams);
            return;
        }
        throw new KeyboardDrawableErrorException();
    }

    public void setTitleDrawablePadding(int i2) {
        this.F = i2;
    }

    public void setTitleDrawableSize(int i2, int i3) {
        this.D = i2;
        this.E = i3;
    }

    public void setTitleFont(Typeface typeface) {
        this.J = typeface;
    }

    public void setTitleHeight(int i2) {
        this.j = i2;
    }

    public void setTitleSize(int i2) {
        this.H = i2;
    }

    public void setTitleText(String str) {
        this.e = str;
    }

    public synchronized boolean show() {
        if (this.O != null) {
            return false;
        }
        this.O = new a();
        try {
            SafetyKeyboardRequestParams safetyKeyboardRequestParams = new SafetyKeyboardRequestParams();
            safetyKeyboardRequestParams.setTitle(this.e);
            safetyKeyboardRequestParams.setKeyboardWidth(this.f);
            safetyKeyboardRequestParams.setKeyboardHeight(this.g);
            safetyKeyboardRequestParams.setConfirmBtnWidth(this.h);
            safetyKeyboardRequestParams.setConfirmBtnHeight(this.i);
            safetyKeyboardRequestParams.setTitleHeight(this.j);
            safetyKeyboardRequestParams.setMarginRow(this.k);
            safetyKeyboardRequestParams.setMarginCol(this.l);
            safetyKeyboardRequestParams.setOutPaddingLeft(this.m);
            safetyKeyboardRequestParams.setOutPaddingRight(this.o);
            safetyKeyboardRequestParams.setOutPaddingTop(this.n);
            safetyKeyboardRequestParams.setOutPaddingBottom(this.p);
            safetyKeyboardRequestParams.setInnerPaddingLeft(this.q);
            safetyKeyboardRequestParams.setInnerPaddingRight(this.s);
            safetyKeyboardRequestParams.setInnerPaddingTop(this.r);
            safetyKeyboardRequestParams.setInnerPaddingBottom(this.t);
            safetyKeyboardRequestParams.setNumSize(this.u);
            safetyKeyboardRequestParams.setConfirmBtnOutPaddingRight(this.v);
            safetyKeyboardRequestParams.setStartX(this.w);
            safetyKeyboardRequestParams.setStartY(this.x);
            safetyKeyboardRequestParams.setDefaultPosition(this.y);
            safetyKeyboardRequestParams.setIsAudio(this.z ? 1 : 0);
            safetyKeyboardRequestParams.setDoneRight(this.A ? 1 : 0);
            safetyKeyboardRequestParams.setEnableOKBtn(this.B ? 1 : 0);
            safetyKeyboardRequestParams.setIsVibrate(this.C ? 1 : 0);
            safetyKeyboardRequestParams.setSecureWidth(this.D);
            safetyKeyboardRequestParams.setSecureHeight(this.E);
            safetyKeyboardRequestParams.setTitleDrawablePadding(this.F);
            safetyKeyboardRequestParams.setTitleColor(this.G);
            safetyKeyboardRequestParams.setTitleSize(this.H);
            safetyKeyboardRequestParams.setNumberKeyColor(this.I);
            if (this.J != null) {
                safetyKeyboardRequestParams.setTitleFont(this.J.getStyle());
            }
            safetyKeyboardRequestParams.setEnableLightStatusBar(this.K);
            if (this.b.showSafetyKeyboard(safetyKeyboardRequestParams, this.c, this.O, this.a) == 0) {
                return true;
            }
            this.O = null;
            return false;
        } catch (RemoteException e2) {
            e2.printStackTrace();
            this.O = null;
            return false;
        }
    }
}
