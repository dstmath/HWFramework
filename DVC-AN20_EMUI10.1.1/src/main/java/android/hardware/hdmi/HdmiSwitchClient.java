package android.hardware.hdmi;

import android.annotation.SystemApi;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiSwitchClient;
import android.hardware.hdmi.IHdmiControlCallback;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

@SystemApi
public class HdmiSwitchClient extends HdmiClient {
    private static final String TAG = "HdmiSwitchClient";

    @SystemApi
    public interface OnSelectListener {
        void onSelect(@HdmiControlManager.ControlCallbackResult int i);
    }

    HdmiSwitchClient(IHdmiControlService service) {
        super(service);
    }

    private static IHdmiControlCallback getCallbackWrapper(final OnSelectListener listener) {
        return new IHdmiControlCallback.Stub() {
            /* class android.hardware.hdmi.HdmiSwitchClient.AnonymousClass1 */

            @Override // android.hardware.hdmi.IHdmiControlCallback
            public void onComplete(int result) {
                OnSelectListener.this.onSelect(result);
            }
        };
    }

    @Override // android.hardware.hdmi.HdmiClient
    public int getDeviceType() {
        return 6;
    }

    public void selectDevice(int logicalAddress, OnSelectListener listener) {
        Preconditions.checkNotNull(listener);
        try {
            this.mService.deviceSelect(logicalAddress, getCallbackWrapper(listener));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select device: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void selectPort(int portId, OnSelectListener listener) {
        Preconditions.checkNotNull(listener);
        try {
            this.mService.portSelect(portId, getCallbackWrapper(listener));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select port: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void selectDevice(int logicalAddress, final Executor executor, final OnSelectListener listener) {
        Preconditions.checkNotNull(listener);
        try {
            this.mService.deviceSelect(logicalAddress, new IHdmiControlCallback.Stub() {
                /* class android.hardware.hdmi.HdmiSwitchClient.AnonymousClass2 */

                @Override // android.hardware.hdmi.IHdmiControlCallback
                public void onComplete(int result) {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(executor, listener, result) {
                        /* class android.hardware.hdmi.$$Lambda$HdmiSwitchClient$2$knvX6ZgANoRRFcb_fUHlUdWIjCQ */
                        private final /* synthetic */ Executor f$0;
                        private final /* synthetic */ HdmiSwitchClient.OnSelectListener f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            this.f$0.execute(new Runnable(this.f$2) {
                                /* class android.hardware.hdmi.$$Lambda$HdmiSwitchClient$2$wYF9AcLTW87bh8nh0L1O42jdg */
                                private final /* synthetic */ int f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    HdmiSwitchClient.OnSelectListener.this.onSelect(this.f$1);
                                }
                            });
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select device: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void selectPort(int portId, final Executor executor, final OnSelectListener listener) {
        Preconditions.checkNotNull(listener);
        try {
            this.mService.portSelect(portId, new IHdmiControlCallback.Stub() {
                /* class android.hardware.hdmi.HdmiSwitchClient.AnonymousClass3 */

                @Override // android.hardware.hdmi.IHdmiControlCallback
                public void onComplete(int result) {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(executor, listener, result) {
                        /* class android.hardware.hdmi.$$Lambda$HdmiSwitchClient$3$Cqxvec1NmkC6VlEdX5OEOabobXY */
                        private final /* synthetic */ Executor f$0;
                        private final /* synthetic */ HdmiSwitchClient.OnSelectListener f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            this.f$0.execute(new Runnable(this.f$2) {
                                /* class android.hardware.hdmi.$$Lambda$HdmiSwitchClient$3$apecUZ8P9DH90drOKNmw2Y8Fspg */
                                private final /* synthetic */ int f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    HdmiSwitchClient.OnSelectListener.this.onSelect(this.f$1);
                                }
                            });
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select port: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public List<HdmiDeviceInfo> getDeviceList() {
        try {
            return this.mService.getDeviceList();
        } catch (RemoteException e) {
            Log.e("TAG", "Failed to call getDeviceList():", e);
            return Collections.emptyList();
        }
    }
}
