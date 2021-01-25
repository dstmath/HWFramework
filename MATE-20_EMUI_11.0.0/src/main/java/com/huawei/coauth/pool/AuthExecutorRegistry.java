package com.huawei.coauth.pool;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.coauth.pool.helper.AuthAttributes;
import com.huawei.coauth.pool.helper.AuthMessage;
import com.huawei.coauthservice.pool.IExecutorMessenger;
import com.huawei.coauthservice.pool.IExecutorProcessor;
import com.huawei.coauthservice.pool.IExecutorRegistry;
import com.huawei.coauthservice.pool.IExecutorRegistryCallback;

public class AuthExecutorRegistry {
    private static final String COAUTH_PACKAGE_NAME = "com.huawei.coauthservice";
    private static final String PACKAGE = "package";
    private static final int RESPOOL_FAILED = -1;
    private static final String START_REGISTER_INTENT = "huawei.security.startreg";
    private static final String TAG = "RES_POOL|AuthExecutorRegistry";
    private static final String USE_AUTH_PERMISSION = "com.huawei.permission.coauth.USE_AUTH";
    private ExecutorRegistryCallback binder = new ExecutorRegistryCallback();
    private Connection connection;
    private Context context;
    private IExecutorRegistry registry;

    public interface Connection {
        void onResourcePoolConnected();

        void onResourcePoolDisconnected();
    }

    public AuthExecutorRegistry(Context context2, Connection connection2) {
        if (context2 != null) {
            this.context = context2.getApplicationContext();
        }
        this.connection = connection2;
    }

    private AuthExecutorRegistry(Context context2, IExecutorRegistry registry2) {
        if (context2 != null) {
            this.context = context2.getApplicationContext();
        }
        this.registry = registry2;
    }

    public final int registerToHwCoAuth(AuthExecutor info, ExecutorCallback callback) {
        if (!isConnected()) {
            return -1;
        }
        if (info == null) {
            try {
                Log.e(TAG, "info is null, registerToHwCoAuth fail");
                return -1;
            } catch (RemoteException e) {
                return -1;
            }
        } else if (this.registry == null) {
            Log.e(TAG, "registry is null, registerToHwCoAuth fail");
            return -1;
        } else {
            return this.registry.register(info.toBundle(), new ExecutorCallbackWrapper(callback));
        }
    }

    public final int unRegisterFromHwCoAuth(AuthExecutor info) {
        if (!isConnected()) {
            return -1;
        }
        if (info == null) {
            try {
                Log.e(TAG, "info is null, unRegisterFromHwCoAuth fail");
                return -1;
            } catch (RemoteException e) {
                return -1;
            }
        } else if (this.registry != null) {
            return this.registry.deregister(info.toBundle());
        } else {
            Log.e(TAG, "registry is null, unRegisterFromHwCoAuth fail");
            return -1;
        }
    }

    public final boolean isConnected() {
        IExecutorRegistry iExecutorRegistry = this.registry;
        if (iExecutorRegistry == null) {
            return false;
        }
        return iExecutorRegistry.asBinder().isBinderAlive();
    }

    public final Binder asBinder() {
        return this.binder;
    }

    public void triggerResourcePoolToConnect() {
        triggerResourcePoolToConnect(this.context);
    }

    public static void triggerResourcePoolToConnect(Context context2) {
        Log.i(TAG, "triggerResourcePoolToConnect: start");
        Intent intent = new Intent(START_REGISTER_INTENT);
        intent.setPackage("com.huawei.coauthservice");
        if (context2 == null) {
            Log.e(TAG, "context is null, triggerResourcePoolToConnect fail");
            return;
        }
        intent.putExtra(PACKAGE, context2.getPackageName());
        context2.sendBroadcast(intent, USE_AUTH_PERMISSION);
        Log.i(TAG, "triggerResourcePoolToConnect: end");
    }

    public static AuthExecutorRegistry fromExecutorRegistry(Context context2, IBinder binder2) {
        return new AuthExecutorRegistry(context2, IExecutorRegistry.Stub.asInterface(binder2));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnected(IExecutorRegistry executorRegistry) {
        Log.i(TAG, "onConnected begin");
        this.registry = executorRegistry;
        Connection connection2 = this.connection;
        if (connection2 != null && this.context != null) {
            try {
                connection2.onResourcePoolConnected();
            } catch (Exception e) {
                Log.e(TAG, "onConnected RemoteException occurs when onResourcePoolConnected");
            }
        }
    }

    private static class ExecutorCallbackWrapper extends IExecutorProcessor.Stub {
        private ExecutorCallback callback;
        private final Object lock = new Object();

        ExecutorCallbackWrapper(ExecutorCallback callback2) {
            this.callback = callback2;
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public void onMessengerReady(IExecutorMessenger messenger) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "onMessengerReady");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, onMessengerReady fail");
                        return;
                    }
                    this.callback.onMessengerReady(new ExecutorMessengerWrapper(messenger));
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "onMessengerReady RemoteException occurs when onMessengerReady");
                    throw new RemoteException("callback.onMessengerReady Exception");
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public int prepareExecute(long sessionId, byte[] publicKey, Bundle params) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "prepareExecute");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, prepareExecute fail");
                        return -1;
                    }
                    AuthAttributes attributes = AuthAttributes.getTlvfromBundle(params);
                    return this.callback.prepareExecute(sessionId, publicKey, attributes.getExpectAttrTypes(), attributes);
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "prepareExecute RemoteException occurs when prepareExecute");
                    throw new RemoteException("callback.prepareExecute Exception");
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public int beginExecute(long sessionId, byte[] publicKey, Bundle params) throws RemoteException {
            Log.i(AuthExecutorRegistry.TAG, "beginExecute");
            synchronized (this.lock) {
                if (params == null) {
                    try {
                        Log.e(AuthExecutorRegistry.TAG, "params is null, beginExecute fail");
                        return -1;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(AuthExecutorRegistry.TAG, "ArrayIndexOutOfBoundsException in beginExecute");
                        return -1;
                    } catch (Exception e2) {
                        Log.e(AuthExecutorRegistry.TAG, "beginExecute RemoteException occurs when beginExecute");
                        throw new RemoteException("callback.onBeginExecute Exception");
                    }
                } else if (this.callback == null) {
                    Log.e(AuthExecutorRegistry.TAG, "callback is null, beginExecute fail");
                    return -1;
                } else {
                    if (publicKey == null) {
                        Log.d(AuthExecutorRegistry.TAG, "beginExecute:public key is null ");
                    }
                    return this.callback.onBeginExecute(sessionId, publicKey, new int[0], AuthAttributes.getTlvfromBundle(params));
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public int endExecute(long sessionId, Bundle params) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "endExecute");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, endExecute fail");
                        return -1;
                    }
                    return this.callback.onEndExecute(sessionId, AuthAttributes.fromBundle(params));
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "endExecute RemoteException occurs when endExecute");
                    throw new RemoteException("callback.onEndExecute Exception");
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public int setProperty(Bundle params) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "setProperty start");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, setProperty fail");
                        return -1;
                    }
                    AuthAttributes values = AuthAttributes.getTlvfromBundle(params);
                    return this.callback.onSetProperty(values.getExpectAttrTypes(), values);
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "setProperty RemoteException occurs when setProperty");
                    throw new RemoteException("callback.onSetProperty Exception");
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public Bundle getProperty(Bundle property) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "getProperty");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, getProperty fail");
                        return new Bundle();
                    }
                    AuthAttributes input = AuthAttributes.getTlvfromBundle(property);
                    AuthAttributes result = this.callback.onGetProperty(input.getExpectAttrTypes(), input);
                    if (result == null) {
                        return new Bundle();
                    }
                    return result.setTlvtoBundle();
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "getProperty RemoteException occurs when getProperty");
                    throw new RemoteException("callback.onGetProperty Exception");
                }
            }
        }

        @Override // com.huawei.coauthservice.pool.IExecutorProcessor
        public int receiveData(long sessionId, long transNum, int srcType, int dstType, Bundle params) throws RemoteException {
            Log.d(AuthExecutorRegistry.TAG, "receiveData");
            synchronized (this.lock) {
                try {
                    if (this.callback == null) {
                        Log.e(AuthExecutorRegistry.TAG, "callback is null, receiveData fail");
                        return -1;
                    }
                    return this.callback.onReceiveData(sessionId, transNum, srcType, dstType, AuthMessage.fromBundle(params));
                } catch (Exception e) {
                    Log.e(AuthExecutorRegistry.TAG, "receiveData RemoteException occurs when receiveData");
                    throw new RemoteException("callback.onReceiveData Exception");
                }
            }
        }
    }

    private static class ExecutorMessengerWrapper implements ExecutorMessenger {
        private final Object lock = new Object();
        private final IExecutorMessenger messenger;

        ExecutorMessengerWrapper(IExecutorMessenger messenger2) {
            this.messenger = messenger2;
        }

        @Override // com.huawei.coauth.pool.ExecutorMessenger
        public int sendData(long sessionId, long transNum, int srcType, int dstType, AuthMessage msgData) {
            Log.d(AuthExecutorRegistry.TAG, "sendData");
            synchronized (this.lock) {
                try {
                    if (this.messenger == null) {
                        Log.e(AuthExecutorRegistry.TAG, "messenger is null, send sendData message fail");
                        return -1;
                    } else if (msgData == null) {
                        Log.e(AuthExecutorRegistry.TAG, "msgData is null, send sendData message fail");
                        return -1;
                    } else {
                        return this.messenger.sendData(sessionId, transNum, srcType, dstType, msgData.toBundle());
                    }
                } catch (RemoteException e) {
                    Log.e(AuthExecutorRegistry.TAG, "sendData RemoteException occurs when sendData");
                    return 0;
                }
            }
        }

        @Override // com.huawei.coauth.pool.ExecutorMessenger
        public int progress(long sessionId, int srcType, int progress, AuthAttributes intermediateResult) {
            synchronized (this.lock) {
                try {
                    if (this.messenger == null) {
                        Log.e(AuthExecutorRegistry.TAG, "messenger is null, send progress message fail");
                        return -1;
                    } else if (intermediateResult == null) {
                        Log.e(AuthExecutorRegistry.TAG, "intermediateResult is null, send progress message fail");
                        return -1;
                    } else {
                        return this.messenger.progress(sessionId, srcType, progress, intermediateResult.setTlvtoBundle());
                    }
                } catch (RemoteException e) {
                    Log.e(AuthExecutorRegistry.TAG, "progress RemoteException occurs when progress");
                    return 0;
                }
            }
        }

        @Override // com.huawei.coauth.pool.ExecutorMessenger
        public int finish(long sessionId, int srcType, int resultCode, AuthAttributes finalResult) {
            synchronized (this.lock) {
                try {
                    if (this.messenger == null) {
                        Log.e(AuthExecutorRegistry.TAG, "messenger is null, send finish message fail");
                        return -1;
                    } else if (finalResult == null) {
                        Log.e(AuthExecutorRegistry.TAG, "finalResult is null, send finish message fail");
                        return -1;
                    } else {
                        Bundle bundle = finalResult.setTlvtoBundle();
                        Log.i(AuthExecutorRegistry.TAG, "AuthExecutorRegistry finish");
                        return this.messenger.finish(sessionId, srcType, resultCode, bundle);
                    }
                } catch (RemoteException e) {
                    Log.e(AuthExecutorRegistry.TAG, "finish RemoteException occurs when finish");
                    return 0;
                }
            }
        }

        @Override // com.huawei.coauth.pool.ExecutorMessenger
        public int notify(AuthAttributes status) {
            synchronized (this.lock) {
                try {
                    if (this.messenger == null) {
                        Log.e(AuthExecutorRegistry.TAG, "messenger is null, send notify message fail");
                        return -1;
                    } else if (status == null) {
                        Log.e(AuthExecutorRegistry.TAG, "status is null, send notify message fail");
                        return -1;
                    } else {
                        return this.messenger.notify(status.setTlvtoBundle());
                    }
                } catch (RemoteException e) {
                    Log.e(AuthExecutorRegistry.TAG, "notify RemoteException occurs when notify");
                    return 0;
                }
            }
        }
    }

    private class ExecutorRegistryCallback extends IExecutorRegistryCallback.Stub {
        private ExecutorRegistryCallback() {
        }

        @Override // com.huawei.coauthservice.pool.IExecutorRegistryCallback
        public void executorSecureRegistryCallback(IExecutorRegistry executorRegistry) throws RemoteException {
            AuthExecutorRegistry.this.onConnected(executorRegistry);
        }
    }
}
