package ohos.accessibility.remoteadapter;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Region;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.accessibility.ability.AccessibleAbility;
import ohos.accessibility.utils.LogUtil;
import ohos.agp.utils.Rect;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public class BarrierfreeManagerSkeleton extends RemoteObject implements IBarrierfreeManager {
    private static final int COMMAND_ABILITY_BARRIERFREE_EVENT = 2;
    private static final int COMMAND_ABILITY_CLEAR_CACHE = 5;
    private static final int COMMAND_ABILITY_DISPLAY_RESIZE = 7;
    private static final int COMMAND_ABILITY_GESTURE = 4;
    private static final int COMMAND_ABILITY_GESTURE_COMPLETED = 9;
    private static final int COMMAND_ABILITY_INIT = 1;
    private static final int COMMAND_ABILITY_INTERRUPT = 3;
    private static final int COMMAND_ABILITY_KEY_EVENT = 6;
    private static final int COMMAND_ABILITY_SOFTKEYBOARD_CHANGED = 8;
    private static final int CONNECTION_ID_FLAG = -1;
    private static final String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceClient";
    private static final String TAG = "BarrierfreeManagerSkeleton";
    private AccessibleAbility.AccessibilityCallbacks mCallbacks;
    private int mConnectionNum = -1;

    public IRemoteObject asObject() {
        return this;
    }

    public BarrierfreeManagerSkeleton(String str, AccessibleAbility.AccessibilityCallbacks accessibilityCallbacks) {
        super(str);
        this.mCallbacks = accessibilityCallbacks;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        LogUtil.info(TAG, "code is " + i);
        switch (i) {
            case 1:
                LogUtil.info(TAG, "On init");
                startInit(messageParcel);
                return true;
            case 2:
                LogUtil.info(TAG, "On accessibility event");
                startAccessibilityEvent(messageParcel);
                return true;
            case 3:
                LogUtil.info(TAG, "On interrupt");
                startInterrupt(messageParcel);
                return true;
            case 4:
                LogUtil.info(TAG, "On gesture");
                startOnGesture(messageParcel);
                return true;
            case 5:
                LogUtil.info(TAG, "On clear cache");
                return true;
            case 6:
                LogUtil.info(TAG, "On key event");
                startOnKeyEvent(messageParcel);
                return true;
            case 7:
                LogUtil.info(TAG, "On display resize");
                startDisplayResize(messageParcel);
                return true;
            case 8:
                LogUtil.info(TAG, "On soft key board show mode changed");
                startDispatchSoftKeyBoardListener(messageParcel);
                return true;
            case 9:
                LogUtil.info(TAG, "On gesture completed");
                startOnGesturePerformResult(messageParcel);
                return true;
            default:
                LogUtil.info(TAG, "Others");
                return BarrierfreeManagerSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }

    private void startInit(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startInit,error message");
            return;
        }
        Optional translateToIBinder = IPCAdapter.translateToIBinder(messageParcel.readRemoteObject());
        IBinder iBinder = null;
        if (translateToIBinder.isPresent()) {
            Object obj = translateToIBinder.get();
            if (obj instanceof IBinder) {
                iBinder = (IBinder) obj;
            }
        }
        IAccessibilityServiceConnection connection = getConnection(iBinder);
        this.mConnectionNum = messageParcel.readInt();
        messageParcel.readRemoteObject();
        startInit(connection, this.mConnectionNum);
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void startInit(IAccessibilityServiceConnection iAccessibilityServiceConnection, int i) {
        LogUtil.info(TAG, "Start execute init");
        AccessibilityInteractionClient instance = AccessibilityInteractionClient.getInstance();
        if (iAccessibilityServiceConnection == null) {
            AccessibilityInteractionClient.removeConnection(i);
            this.mConnectionNum = -1;
            instance.clearCache();
            this.mCallbacks.init(-1);
            return;
        }
        AccessibilityInteractionClient.addConnection(i, iAccessibilityServiceConnection);
        this.mCallbacks.init(i);
        this.mCallbacks.onAbilityConnected();
    }

    private void startAccessibilityEvent(MessageParcel messageParcel) {
        try {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                LogUtil.error(TAG, "startAccessibilityEvent,error message");
            } else if (messageParcel.readInt() == 0) {
                LogUtil.error(TAG, "No event info");
            } else {
                AccessibilityEvent obtain = AccessibilityEvent.obtain();
                int readInt = messageParcel.readInt();
                obtain.setEventType(messageParcel.readInt());
                obtain.setMovementGranularity(messageParcel.readInt());
                obtain.setAction(messageParcel.readInt());
                obtain.setContentChangeTypes(messageParcel.readInt());
                obtain.setWindowChanges(messageParcel.readInt());
                getStringFromParcel(messageParcel);
                messageParcel.readLong();
                int readInt2 = messageParcel.readInt();
                readAccessibilityRecordFromParcel(obtain, messageParcel);
                int readInt3 = messageParcel.readInt();
                boolean z = false;
                if (readInt3 > 0) {
                    for (int i = 0; i < readInt3; i++) {
                        AccessibilityRecord obtain2 = AccessibilityRecord.obtain();
                        readAccessibilityRecordFromParcel(obtain2, messageParcel);
                        obtain2.setConnectionId(readInt2);
                        obtain.appendRecord(obtain2);
                    }
                }
                if (readInt == 1) {
                    z = true;
                }
                obtain.setSealed(z);
                onAccessibilityEvent(obtain, true);
            }
        } catch (IllegalStateException unused) {
            LogUtil.error(TAG, "startAccessibilityEvent exception");
        }
    }

    private void readAccessibilityRecordFromParcel(AccessibilityRecord accessibilityRecord, MessageParcel messageParcel) {
        messageParcel.readInt();
        accessibilityRecord.setCurrentItemIndex(messageParcel.readInt());
        accessibilityRecord.setItemCount(messageParcel.readInt());
        accessibilityRecord.setFromIndex(messageParcel.readInt());
        accessibilityRecord.setToIndex(messageParcel.readInt());
        accessibilityRecord.setScrollX(messageParcel.readInt());
        accessibilityRecord.setScrollY(messageParcel.readInt());
        accessibilityRecord.setScrollDeltaX(messageParcel.readInt());
        accessibilityRecord.setScrollDeltaY(messageParcel.readInt());
        accessibilityRecord.setMaxScrollX(messageParcel.readInt());
        accessibilityRecord.setMaxScrollY(messageParcel.readInt());
        accessibilityRecord.setAddedCount(messageParcel.readInt());
        accessibilityRecord.setRemovedCount(messageParcel.readInt());
        accessibilityRecord.setClassName(getStringFromParcel(messageParcel));
        accessibilityRecord.setContentDescription(getStringFromParcel(messageParcel));
        accessibilityRecord.setBeforeText(getStringFromParcel(messageParcel));
        messageParcel.readString();
        messageParcel.readList();
        accessibilityRecord.setWindowId(messageParcel.readInt());
        accessibilityRecord.setSourceNodeId(messageParcel.readLong());
        messageParcel.readInt();
    }

    private String getStringFromParcel(MessageParcel messageParcel) {
        int readInt;
        int readInt2 = messageParcel.readInt();
        LogUtil.info(TAG, "getStringFromParcel,kind:" + readInt2);
        String readString = messageParcel.readString();
        if (readString != null && (readInt = messageParcel.readInt()) >= 1 && readInt <= 28) {
            messageParcel.readInt();
            messageParcel.readInt();
            messageParcel.readInt();
        }
        return readString;
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent, boolean z) {
        LogUtil.info(TAG, "Start execute onAccessibilityEvent");
        if (this.mConnectionNum != -1 && z) {
            this.mCallbacks.onAccessibilityEvent(covertEventToEventInfo(accessibilityEvent, new ArrayList()));
        }
        accessibilityEvent.recycle();
    }

    private void startOnKeyEvent(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startOnKeyEvent,error message");
        } else if (messageParcel.readInt() == 0) {
            LogUtil.error(TAG, "No key event info");
        } else {
            messageParcel.readInt();
            int readInt = messageParcel.readInt();
            int readInt2 = messageParcel.readInt();
            int readInt3 = messageParcel.readInt();
            int readInt4 = messageParcel.readInt();
            int readInt5 = messageParcel.readInt();
            int readInt6 = messageParcel.readInt();
            int readInt7 = messageParcel.readInt();
            int readInt8 = messageParcel.readInt();
            int readInt9 = messageParcel.readInt();
            long readLong = messageParcel.readLong();
            long readLong2 = messageParcel.readLong();
            String readString = messageParcel.readString();
            startOnKeyEvent(KeyEvent.obtain(readLong, readLong2, readInt4, readInt5, readInt6, readInt7, readInt, readInt8, readInt9, readInt2, readInt3, readString), messageParcel.readInt());
        }
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void startOnKeyEvent(KeyEvent keyEvent, int i) {
        LogUtil.info(TAG, "Start execute startOnKeyEvent");
        try {
            Optional createEvent = MultimodalEventFactory.createEvent(keyEvent);
            if (!createEvent.isPresent()) {
                LogUtil.error(TAG, "optionalEvent is null");
                return;
            }
            ohos.multimodalinput.event.KeyEvent keyEvent2 = (MultimodalEvent) createEvent.get();
            boolean z = false;
            if (keyEvent2 instanceof ohos.multimodalinput.event.KeyEvent) {
                z = this.mCallbacks.onKeyEvent(keyEvent2);
            }
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionNum);
            if (connection == null) {
                keyEvent.recycle();
                return;
            }
            try {
                connection.setOnKeyEventResult(z, i);
            } catch (android.os.RemoteException unused) {
                LogUtil.error(TAG, "setOnKeyEventResult exception");
            }
            keyEvent.recycle();
        } finally {
            keyEvent.recycle();
        }
    }

    private AccessibilityEventInfo covertEventToEventInfo(AccessibilityEvent accessibilityEvent, List<AccessibilityRecord> list) {
        AccessibilityEventInfo accessibilityEventInfo = new AccessibilityEventInfo(accessibilityEvent.getEventType());
        accessibilityEventInfo.setWindowChangeTypes(accessibilityEvent.getContentChangeTypes());
        accessibilityEventInfo.setTextMoveStep(accessibilityEvent.getMovementGranularity());
        accessibilityEventInfo.setViewId(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityEvent.getSourceNodeId()));
        if (accessibilityEvent.getPackageName() != null) {
            accessibilityEventInfo.setBundleName(accessibilityEvent.getPackageName());
        }
        accessibilityEventInfo.setTriggerAction(accessibilityEvent.getAction());
        for (int i = 0; i < accessibilityEvent.getRecordCount(); i++) {
            AccessibilityRecord record = accessibilityEvent.getRecord(i);
            if (!list.contains(record) && (record instanceof AccessibilityEvent)) {
                list.add(record);
                accessibilityEventInfo.addRecord(covertEventToEventInfo((AccessibilityEvent) record, list));
            }
        }
        return accessibilityEventInfo;
    }

    private void startDisplayResize(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startDisplayResize,error message");
            return;
        }
        int readInt = messageParcel.readInt();
        if (messageParcel.readInt() == 0) {
            LogUtil.error(TAG, "No resize region");
        } else {
            onMagnificationChanged(readInt, new Region(messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt()), messageParcel.readFloat(), messageParcel.readFloat(), messageParcel.readFloat());
        }
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onMagnificationChanged(int i, Region region, float f, float f2, float f3) {
        LogUtil.info(TAG, "Start execute onMagnificationChanged");
        Rect rect = new Rect();
        if (region.getBounds() != null) {
            rect.set(region.getBounds().left, region.getBounds().top, region.getBounds().right, region.getBounds().bottom);
        }
        this.mCallbacks.onDisplyResizeChanged(i, rect, f, f2, f3);
    }

    private void startInterrupt(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startInterrupt,error message");
        } else {
            onInterrupt();
        }
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onInterrupt() {
        LogUtil.info(TAG, "Start execute onInterrupt");
        if (this.mConnectionNum != -1) {
            this.mCallbacks.onInterrupt();
        }
    }

    private void startOnGesture(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startOnGesture,error message");
        } else {
            onGesture(messageParcel.readInt());
        }
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onGesture(int i) {
        LogUtil.info(TAG, "Start execute onGesture");
        this.mCallbacks.onGesture(i);
    }

    private void startDispatchSoftKeyBoardListener(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startDispatchSoftKeyBoardListener,error message");
        } else {
            onSoftKeyboardShowModeChanged(messageParcel.readInt());
        }
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onSoftKeyboardShowModeChanged(int i) {
        LogUtil.info(TAG, "Start execute onSoftKeyboardShowModeChanged");
        this.mCallbacks.onSoftKeyboardShowModeChanged(i);
    }

    private void startOnGesturePerformResult(MessageParcel messageParcel) {
        if (!isRightMessage(messageParcel.readInterfaceToken())) {
            LogUtil.error(TAG, "startOnGesturePerformResult,error message");
            return;
        }
        int readInt = messageParcel.readInt();
        boolean z = true;
        if (messageParcel.readInt() != 1) {
            z = false;
        }
        onPerformGestureResult(readInt, z);
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onPerformGestureResult(int i, boolean z) {
        LogUtil.info(TAG, "Start execute onPerformGestureResult");
        this.mCallbacks.onPerformGestureResult(i, z);
    }

    private boolean isRightMessage(String str) {
        return DESCRIPTOR.equals(str);
    }

    private IAccessibilityServiceConnection getConnection(IBinder iBinder) {
        return IAccessibilityServiceConnection.Stub.asInterface(iBinder);
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void clearAccessibilityCache() {
        LogUtil.info(TAG, "Start execute clearAccessibilityCache");
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onFingerprintCapturingGesturesChanged(boolean z) {
        LogUtil.info(TAG, "Start execute onFingerprintCapturingGesturesChanged");
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onFingerprintGesture(int i) {
        LogUtil.info(TAG, "Start execute onFingerprintGesture");
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onAccessibilityButtonClicked() {
        LogUtil.info(TAG, "Start execute onAccessibilityButtonClicked");
    }

    @Override // ohos.accessibility.remoteadapter.IBarrierfreeManager
    public void onAccessibilityButtonAvailabilityChanged(boolean z) {
        LogUtil.info(TAG, "Start execute onAccessibilityButtonAvailabilityChanged");
    }
}
