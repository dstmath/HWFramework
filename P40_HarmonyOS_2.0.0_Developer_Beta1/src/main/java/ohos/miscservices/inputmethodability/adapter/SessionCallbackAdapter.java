package ohos.miscservices.inputmethodability.adapter;

import android.content.Context;
import android.os.IBinder;
import android.view.InputChannel;
import com.android.internal.view.IInputSessionCallback;
import java.lang.ref.WeakReference;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.internal.AgentCallbackSkeleton;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class SessionCallbackAdapter {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IAgentCallback";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "SessionCallbackAdapter");
    private WeakReference<Context> mAospContextRefer;
    private InputChannel mInputChannel;
    private IInputSessionCallback mSessionCallback;

    public SessionCallbackAdapter(Context context, IRemoteObject iRemoteObject, InputChannel inputChannel) {
        this.mAospContextRefer = new WeakReference<>(context);
        Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
        if (translateToIBinder.isPresent()) {
            Object obj = translateToIBinder.get();
            if (obj instanceof IBinder) {
                this.mSessionCallback = IInputSessionCallback.Stub.asInterface((IBinder) obj);
                HiLog.debug(TAG, "get session callback proxy success.", new Object[0]);
            }
        }
        this.mInputChannel = inputChannel;
    }

    public IRemoteObject getAdaptRemoteObject() {
        return new AgentCallbackImpl(DESCRIPTOR);
    }

    class AgentCallbackImpl extends AgentCallbackSkeleton {
        AgentCallbackImpl(String str) {
            super(str);
        }

        @Override // ohos.miscservices.inputmethod.internal.IAgentCallback
        public void agentCreated(IRemoteObject iRemoteObject) throws RemoteException {
            HiLog.info(SessionCallbackAdapter.TAG, "agentCreated", new Object[0]);
            if (SessionCallbackAdapter.this.mSessionCallback == null) {
                HiLog.error(SessionCallbackAdapter.TAG, "agent callback is null.", new Object[0]);
                return;
            }
            try {
                SessionCallbackAdapter.this.mSessionCallback.sessionCreated(new InputMethodSessionAdapter((Context) SessionCallbackAdapter.this.mAospContextRefer.get(), SessionCallbackAdapter.this.mInputChannel, iRemoteObject).getInputMethodSession());
                HiLog.info(SessionCallbackAdapter.TAG, "agent created success.", new Object[0]);
            } catch (android.os.RemoteException unused) {
                HiLog.error(SessionCallbackAdapter.TAG, "mIInputSessionCallback.sessionCreated() error", new Object[0]);
            }
        }
    }
}
