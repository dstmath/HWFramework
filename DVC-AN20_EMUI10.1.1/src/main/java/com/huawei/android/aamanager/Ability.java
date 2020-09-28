package com.huawei.android.aamanager;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.aamanager.IAbility;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Ability extends Service {
    private static final Boolean DEBUG_LIFECYCLE = false;
    private static final Boolean DEBUG_TRANSFER = false;
    private static final String TAG = "Ability";
    private final IAbility.Stub mBinder = new IAbility.Stub() {
        /* class com.huawei.android.aamanager.Ability.AnonymousClass1 */

        @Override // com.huawei.android.aamanager.IAbility
        public String getType() {
            return Ability.this.getType();
        }

        @Override // com.huawei.android.aamanager.IAbility
        public ParcelFileDescriptor getInput() {
            return Ability.this.mExternalInput;
        }

        @Override // com.huawei.android.aamanager.IAbility
        public void config(Bundle bundle) {
            Ability.this.config(bundle);
        }

        @Override // com.huawei.android.aamanager.IAbility
        public final void connect(IAbility a) {
            Ability.this.connect(a);
        }

        @Override // com.huawei.android.aamanager.IAbility
        public void process() {
            Ability.this.process();
        }
    };
    private ParcelFileDescriptor mExternalInput;
    protected ParcelFileDescriptor mInput;
    protected ParcelFileDescriptor mOutput;

    /* access modifiers changed from: protected */
    public abstract void config(Bundle bundle);

    /* access modifiers changed from: protected */
    public abstract String getType();

    /* access modifiers changed from: protected */
    public abstract void processStream(InputStream inputStream, OutputStream outputStream);

    public void onCreate() {
        if (DEBUG_LIFECYCLE.booleanValue()) {
            Log.d(TAG, "onCreate");
        }
    }

    public void onDestroy() {
        if (DEBUG_LIFECYCLE.booleanValue()) {
            Log.d(TAG, "onDestroy");
        }
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        if (!IAbility.class.getName().equals(intent.getAction())) {
            return null;
        }
        if (DEBUG_LIFECYCLE.booleanValue()) {
            Log.d(TAG, "onBind");
        }
        return this.mBinder;
    }

    public Ability() {
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createReliablePipe();
            this.mInput = pipe[0];
            this.mExternalInput = pipe[1];
        } catch (IOException e) {
            Log.e(TAG, "Ability IOException.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connect(IAbility next) {
        try {
            this.mOutput = next.getInput();
        } catch (RemoteException e) {
            Log.e(TAG, "connect RemoteException.");
        }
    }

    public void process() {
        new Thread(getClass().getSimpleName()) {
            /* class com.huawei.android.aamanager.Ability.AnonymousClass2 */

            public void run() {
                if (Ability.DEBUG_TRANSFER.booleanValue()) {
                    Log.d(Ability.TAG, "Thread running.");
                }
                InputStream inputStream = System.in;
                OutputStream outputStream = System.out;
                if (Ability.this.mInput != null) {
                    inputStream = new ParcelFileDescriptor.AutoCloseInputStream(Ability.this.mInput);
                }
                if (Ability.this.mOutput != null) {
                    outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(Ability.this.mOutput);
                }
                Ability.this.processStream(inputStream, outputStream);
                try {
                    if (inputStream != System.in) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(Ability.TAG, "connect RemoteException.");
                }
                try {
                    if (outputStream != System.out) {
                        outputStream.close();
                    }
                } catch (IOException e2) {
                    Log.e(Ability.TAG, "connect RemoteException.");
                }
                if (Ability.DEBUG_TRANSFER.booleanValue()) {
                    Log.d(Ability.TAG, "Thread finished");
                }
            }
        }.start();
    }
}
