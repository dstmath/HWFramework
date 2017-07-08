package com.android.server.wm;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.TokenWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManagerPolicy;

public class KeyguardDisableHandler extends Handler {
    private static final int ALLOW_DISABLE_NO = 0;
    private static final int ALLOW_DISABLE_UNKNOWN = -1;
    private static final int ALLOW_DISABLE_YES = 1;
    static final int KEYGUARD_DISABLE = 1;
    static final int KEYGUARD_POLICY_CHANGED = 3;
    static final int KEYGUARD_REENABLE = 2;
    private static final String TAG = null;
    private int mAllowDisableKeyguard;
    final Context mContext;
    KeyguardTokenWatcher mKeyguardTokenWatcher;
    final WindowManagerPolicy mPolicy;

    class KeyguardTokenWatcher extends TokenWatcher {
        public KeyguardTokenWatcher(Handler handler) {
            super(handler, KeyguardDisableHandler.TAG);
        }

        public void updateAllowState() {
            int i = KeyguardDisableHandler.ALLOW_DISABLE_NO;
            DevicePolicyManager dpm = (DevicePolicyManager) KeyguardDisableHandler.this.mContext.getSystemService("device_policy");
            if (dpm != null) {
                try {
                    KeyguardDisableHandler keyguardDisableHandler = KeyguardDisableHandler.this;
                    if (dpm.getPasswordQuality(null, ActivityManagerNative.getDefault().getCurrentUser().id) == 0) {
                        i = KeyguardDisableHandler.KEYGUARD_DISABLE;
                    }
                    keyguardDisableHandler.mAllowDisableKeyguard = i;
                } catch (RemoteException e) {
                }
            }
        }

        public void acquired() {
            if (KeyguardDisableHandler.this.mAllowDisableKeyguard == KeyguardDisableHandler.ALLOW_DISABLE_UNKNOWN) {
                updateAllowState();
            }
            if (KeyguardDisableHandler.this.mAllowDisableKeyguard == KeyguardDisableHandler.KEYGUARD_DISABLE) {
                KeyguardDisableHandler.this.mPolicy.enableKeyguard(false);
            } else {
                Log.v(KeyguardDisableHandler.TAG, "Not disabling keyguard since device policy is enforced");
            }
        }

        public void released() {
            KeyguardDisableHandler.this.mPolicy.enableKeyguard(true);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.KeyguardDisableHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.KeyguardDisableHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.KeyguardDisableHandler.<clinit>():void");
    }

    public KeyguardDisableHandler(Context context, WindowManagerPolicy policy) {
        this.mAllowDisableKeyguard = ALLOW_DISABLE_UNKNOWN;
        this.mContext = context;
        this.mPolicy = policy;
    }

    public void handleMessage(Message msg) {
        if (this.mKeyguardTokenWatcher == null) {
            this.mKeyguardTokenWatcher = new KeyguardTokenWatcher(this);
        }
        switch (msg.what) {
            case KEYGUARD_DISABLE /*1*/:
                Pair<IBinder, String> pair = msg.obj;
                this.mKeyguardTokenWatcher.acquire((IBinder) pair.first, (String) pair.second);
            case KEYGUARD_REENABLE /*2*/:
                this.mKeyguardTokenWatcher.release((IBinder) msg.obj);
            case KEYGUARD_POLICY_CHANGED /*3*/:
                this.mAllowDisableKeyguard = ALLOW_DISABLE_UNKNOWN;
                if (this.mKeyguardTokenWatcher.isAcquired()) {
                    this.mKeyguardTokenWatcher.updateAllowState();
                    if (this.mAllowDisableKeyguard != KEYGUARD_DISABLE) {
                        this.mPolicy.enableKeyguard(true);
                        return;
                    }
                    return;
                }
                this.mPolicy.enableKeyguard(true);
            default:
        }
    }
}
