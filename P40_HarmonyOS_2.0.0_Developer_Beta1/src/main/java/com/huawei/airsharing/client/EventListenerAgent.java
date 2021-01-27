package com.huawei.airsharing.client;

import android.os.RemoteException;
import com.huawei.airsharing.api.Event;
import com.huawei.airsharing.api.IEventListener;
import com.huawei.airsharing.api.ProjectionDevice;
import com.huawei.airsharing.client.IAidlHwListener;

public class EventListenerAgent extends IAidlHwListener.Stub {
    private IEventListener mListener;

    public EventListenerAgent(IEventListener listener) {
        this.mListener = listener;
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    public boolean onEvent(int eventId, String type) {
        return this.mListener.onEvent(eventId, type);
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    @Deprecated
    public void onDisplayUpdate(int eventId, String devName, String devAddress, int priority) {
        this.mListener.onDisplayUpdate(eventId, devName, devAddress, priority);
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    @Deprecated
    public void onMirrorUpdate(int eventId, String devName, String udn, int priority, boolean isSupportMirror) {
        this.mListener.onMirrorUpdate(eventId, devName, udn, priority, isSupportMirror);
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    public int getId() throws RemoteException {
        return this.mListener.hashCode();
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    public void onProjectionDeviceUpdate(int eventId, ProjectionDevice device) {
        this.mListener.onProjectionDeviceUpdate(eventId, device);
    }

    @Override // com.huawei.airsharing.client.IAidlHwListener
    public void onEventHandle(Event event) throws RemoteException {
        this.mListener.onEventHandle(event);
    }
}
