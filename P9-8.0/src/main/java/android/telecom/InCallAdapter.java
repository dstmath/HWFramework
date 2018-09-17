package android.telecom;

import android.os.Bundle;
import android.os.RemoteException;
import com.android.internal.telecom.IInCallAdapter;
import java.util.List;

public final class InCallAdapter {
    private final IInCallAdapter mAdapter;

    public InCallAdapter(IInCallAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void answerCall(String callId, int videoState) {
        try {
            this.mAdapter.answerCall(callId, videoState);
        } catch (RemoteException e) {
        }
    }

    public boolean updateRcsPreCallInfo(String callId, Bundle extras) {
        try {
            this.mAdapter.updateRcsPreCallInfo(callId, extras);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) {
        try {
            this.mAdapter.rejectCall(callId, rejectWithMessage, textMessage);
        } catch (RemoteException e) {
        }
    }

    public void disconnectCall(String callId) {
        try {
            this.mAdapter.disconnectCall(callId);
        } catch (RemoteException e) {
        }
    }

    public void holdCall(String callId) {
        try {
            this.mAdapter.holdCall(callId);
        } catch (RemoteException e) {
        }
    }

    public void unholdCall(String callId) {
        try {
            this.mAdapter.unholdCall(callId);
        } catch (RemoteException e) {
        }
    }

    public void mute(boolean shouldMute) {
        try {
            this.mAdapter.mute(shouldMute);
        } catch (RemoteException e) {
        }
    }

    public void setAudioRoute(int route) {
        try {
            this.mAdapter.setAudioRoute(route);
        } catch (RemoteException e) {
        }
    }

    public void setBluetoothAudioRoute(String address) {
        try {
            this.mAdapter.setBluetoothAudioRoute(address);
        } catch (RemoteException e) {
        }
    }

    public void playDtmfTone(String callId, char digit) {
        try {
            this.mAdapter.playDtmfTone(callId, digit);
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone(String callId) {
        try {
            this.mAdapter.stopDtmfTone(callId);
        } catch (RemoteException e) {
        }
    }

    public void postDialContinue(String callId, boolean proceed) {
        try {
            this.mAdapter.postDialContinue(callId, proceed);
        } catch (RemoteException e) {
        }
    }

    public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle, boolean setDefault) {
        try {
            this.mAdapter.phoneAccountSelected(callId, accountHandle, setDefault);
        } catch (RemoteException e) {
        }
    }

    public void conference(String callId, String otherCallId) {
        try {
            this.mAdapter.conference(callId, otherCallId);
        } catch (RemoteException e) {
        }
    }

    public void splitFromConference(String callId) {
        try {
            this.mAdapter.splitFromConference(callId);
        } catch (RemoteException e) {
        }
    }

    public void mergeConference(String callId) {
        try {
            this.mAdapter.mergeConference(callId);
        } catch (RemoteException e) {
        }
    }

    public void swapConference(String callId) {
        try {
            this.mAdapter.swapConference(callId);
        } catch (RemoteException e) {
        }
    }

    public void pullExternalCall(String callId) {
        try {
            this.mAdapter.pullExternalCall(callId);
        } catch (RemoteException e) {
        }
    }

    public void sendCallEvent(String callId, String event, Bundle extras) {
        try {
            this.mAdapter.sendCallEvent(callId, event, extras);
        } catch (RemoteException e) {
        }
    }

    public void putExtras(String callId, Bundle extras) {
        try {
            this.mAdapter.putExtras(callId, extras);
        } catch (RemoteException e) {
        }
    }

    public void putExtra(String callId, String key, boolean value) {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean(key, value);
            this.mAdapter.putExtras(callId, bundle);
        } catch (RemoteException e) {
        }
    }

    public void putExtra(String callId, String key, int value) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt(key, value);
            this.mAdapter.putExtras(callId, bundle);
        } catch (RemoteException e) {
        }
    }

    public void putExtra(String callId, String key, String value) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(key, value);
            this.mAdapter.putExtras(callId, bundle);
        } catch (RemoteException e) {
        }
    }

    public void removeExtras(String callId, List<String> keys) {
        try {
            this.mAdapter.removeExtras(callId, keys);
        } catch (RemoteException e) {
        }
    }

    public void turnProximitySensorOn() {
        try {
            this.mAdapter.turnOnProximitySensor();
        } catch (RemoteException e) {
        }
    }

    public void turnProximitySensorOff(boolean screenOnImmediately) {
        try {
            this.mAdapter.turnOffProximitySensor(screenOnImmediately);
        } catch (RemoteException e) {
        }
    }

    public void switchToOtherActiveSub(String sub, boolean retainLch) {
        try {
            this.mAdapter.switchToOtherActiveSub(sub, retainLch);
        } catch (RemoteException e) {
        }
    }

    public void sendRttRequest(String callId) {
        try {
            this.mAdapter.sendRttRequest(callId);
        } catch (RemoteException e) {
        }
    }

    public void respondToRttRequest(String callId, int id, boolean accept) {
        try {
            this.mAdapter.respondToRttRequest(callId, id, accept);
        } catch (RemoteException e) {
        }
    }

    public void stopRtt(String callId) {
        try {
            this.mAdapter.stopRtt(callId);
        } catch (RemoteException e) {
        }
    }

    public void setRttMode(String callId, int mode) {
        try {
            this.mAdapter.setRttMode(callId, mode);
        } catch (RemoteException e) {
        }
    }
}
