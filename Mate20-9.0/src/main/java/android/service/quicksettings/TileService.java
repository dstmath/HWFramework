package android.service.quicksettings;

import android.annotation.SystemApi;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.IQSTileService;
import android.util.HwPCUtils;
import android.view.View;
import android.view.WindowManager;

public class TileService extends Service {
    public static final String ACTION_QS_TILE = "android.service.quicksettings.action.QS_TILE";
    public static final String ACTION_QS_TILE_PREFERENCES = "android.service.quicksettings.action.QS_TILE_PREFERENCES";
    public static final String ACTION_REQUEST_LISTENING = "android.service.quicksettings.action.REQUEST_LISTENING";
    public static final String EXTRA_SERVICE = "service";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_TOKEN = "token";
    public static final String META_DATA_ACTIVE_TILE = "android.service.quicksettings.ACTIVE_TILE";
    /* access modifiers changed from: private */
    public final H mHandler = new H(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public boolean mListening = false;
    /* access modifiers changed from: private */
    public IQSService mService;
    private Tile mTile;
    /* access modifiers changed from: private */
    public IBinder mTileToken;
    /* access modifiers changed from: private */
    public IBinder mToken;
    /* access modifiers changed from: private */
    public Runnable mUnlockRunnable;

    private class H extends Handler {
        private static final int MSG_START_LISTENING = 1;
        private static final int MSG_START_SUCCESS = 7;
        private static final int MSG_STOP_LISTENING = 2;
        private static final int MSG_TILE_ADDED = 3;
        private static final int MSG_TILE_CLICKED = 5;
        private static final int MSG_TILE_REMOVED = 4;
        private static final int MSG_UNLOCK_COMPLETE = 6;

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!TileService.this.mListening) {
                        boolean unused = TileService.this.mListening = true;
                        TileService.this.onStartListening();
                        return;
                    }
                    return;
                case 2:
                    if (TileService.this.mListening) {
                        boolean unused2 = TileService.this.mListening = false;
                        TileService.this.onStopListening();
                        return;
                    }
                    return;
                case 3:
                    TileService.this.onTileAdded();
                    return;
                case 4:
                    if (TileService.this.mListening) {
                        boolean unused3 = TileService.this.mListening = false;
                        TileService.this.onStopListening();
                    }
                    TileService.this.onTileRemoved();
                    return;
                case 5:
                    IBinder unused4 = TileService.this.mToken = (IBinder) msg.obj;
                    TileService.this.onClick();
                    return;
                case 6:
                    if (TileService.this.mUnlockRunnable != null) {
                        TileService.this.mUnlockRunnable.run();
                        return;
                    }
                    return;
                case 7:
                    try {
                        TileService.this.mService.onStartSuccessful(TileService.this.mTileToken);
                        return;
                    } catch (RemoteException e) {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public void onDestroy() {
        if (this.mListening) {
            onStopListening();
            this.mListening = false;
        }
        super.onDestroy();
    }

    public void onTileAdded() {
    }

    public void onTileRemoved() {
    }

    public void onStartListening() {
    }

    public void onStopListening() {
    }

    public void onClick() {
    }

    @SystemApi
    public final void setStatusIcon(Icon icon, String contentDescription) {
        if (this.mService != null) {
            try {
                this.mService.updateStatusIcon(this.mTileToken, icon, contentDescription);
            } catch (RemoteException e) {
            }
        }
    }

    public final void showDialog(Dialog dialog) {
        dialog.getWindow().getAttributes().token = this.mToken;
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_QS_DIALOG);
        dialog.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                try {
                    TileService.this.mService.onDialogHidden(TileService.this.mTileToken);
                } catch (RemoteException e) {
                }
            }
        });
        dialog.show();
        try {
            this.mService.onShowDialog(this.mTileToken);
        } catch (RemoteException e) {
        }
    }

    public final void unlockAndRun(Runnable runnable) {
        this.mUnlockRunnable = runnable;
        try {
            this.mService.startUnlockAndRun(this.mTileToken);
        } catch (RemoteException e) {
        }
    }

    public final boolean isSecure() {
        try {
            return this.mService.isSecure();
        } catch (RemoteException e) {
            return true;
        }
    }

    public final boolean isLocked() {
        try {
            return this.mService.isLocked();
        } catch (RemoteException e) {
            return true;
        }
    }

    public final void startActivityAndCollapse(Intent intent) {
        startActivity(intent);
        try {
            this.mService.onStartActivity(this.mTileToken);
        } catch (RemoteException e) {
        }
    }

    public final Tile getQsTile() {
        return this.mTile;
    }

    public IBinder onBind(Intent intent) {
        this.mService = IQSService.Stub.asInterface(intent.getIBinderExtra(EXTRA_SERVICE));
        this.mTileToken = intent.getIBinderExtra(EXTRA_TOKEN);
        try {
            this.mTile = this.mService.getTile(this.mTileToken);
            if (this.mTile != null) {
                this.mTile.setService(this.mService, this.mTileToken);
                this.mHandler.sendEmptyMessage(7);
            }
            return new IQSTileService.Stub() {
                public void onTileRemoved() throws RemoteException {
                    TileService.this.mHandler.sendEmptyMessage(4);
                }

                public void onTileAdded() throws RemoteException {
                    TileService.this.mHandler.sendEmptyMessage(3);
                }

                public void onStopListening() throws RemoteException {
                    TileService.this.mHandler.sendEmptyMessage(2);
                }

                public void onStartListening() throws RemoteException {
                    TileService.this.mHandler.sendEmptyMessage(1);
                }

                public void onClick(IBinder wtoken) throws RemoteException {
                    TileService.this.mHandler.obtainMessage(5, wtoken).sendToTarget();
                }

                public void onUnlockComplete() throws RemoteException {
                    TileService.this.mHandler.sendEmptyMessage(6);
                }
            };
        } catch (RemoteException e) {
            throw new RuntimeException("Unable to reach IQSService", e);
        }
    }

    public static boolean isQuickSettingsSupported() {
        return Resources.getSystem().getBoolean(17957005);
    }

    public static final void requestListeningState(Context context, ComponentName component) {
        Intent intent = new Intent(ACTION_REQUEST_LISTENING);
        intent.putExtra("android.intent.extra.COMPONENT_NAME", component);
        intent.setPackage(HwPCUtils.PKG_PHONE_SYSTEMUI);
        context.sendBroadcast(intent, "android.permission.BIND_QUICK_SETTINGS_TILE");
    }
}
