package com.android.server.audio;

import android.media.AudioFormat;
import android.media.AudioFormat.Builder;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.AudioSystem.AudioRecordingCallback;
import android.media.IRecordingConfigDispatcher;
import android.media.MediaRecorder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class RecordingActivityMonitor implements AudioRecordingCallback {
    public static final String TAG = "AudioService.RecordingActivityMonitor";
    private ArrayList<RecMonitorClient> mClients = new ArrayList();
    private HashMap<Integer, AudioRecordingConfiguration> mRecordConfigs = new HashMap();

    private static final class RecMonitorClient implements DeathRecipient {
        static RecordingActivityMonitor sMonitor;
        final IRecordingConfigDispatcher mDispatcherCb;

        RecMonitorClient(IRecordingConfigDispatcher rcdb) {
            this.mDispatcherCb = rcdb;
        }

        public void binderDied() {
            Log.w(RecordingActivityMonitor.TAG, "client died");
            sMonitor.unregisterRecordingCallback(this.mDispatcherCb);
        }

        boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(RecordingActivityMonitor.TAG, "Could not link to client death", e);
                return false;
            }
        }

        void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    RecordingActivityMonitor() {
        RecMonitorClient.sMonitor = this;
    }

    public void onRecordingConfigurationChanged(int event, int session, int source, int[] recordingInfo) {
        if (!MediaRecorder.isSystemOnlyAudioSource(source)) {
            List<AudioRecordingConfiguration> configs = updateSnapshot(event, session, source, recordingInfo);
            if (configs != null) {
                synchronized (this.mClients) {
                    Iterator<RecMonitorClient> clientIterator = this.mClients.iterator();
                    while (clientIterator.hasNext()) {
                        try {
                            ((RecMonitorClient) clientIterator.next()).mDispatcherCb.dispatchRecordingConfigChange(configs);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not call dispatchRecordingConfigChange() on client", e);
                        }
                    }
                }
            }
        }
    }

    void initMonitor() {
        AudioSystem.setRecordingCallback(this);
    }

    void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                RecMonitorClient rmc = new RecMonitorClient(rcdb);
                if (rmc.init()) {
                    this.mClients.add(rmc);
                }
            }
        }
    }

    void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                Iterator<RecMonitorClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    RecMonitorClient rmc = (RecMonitorClient) clientIterator.next();
                    if (rcdb.equals(rmc.mDispatcherCb)) {
                        rmc.release();
                        clientIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        List arrayList;
        synchronized (this.mRecordConfigs) {
            arrayList = new ArrayList(this.mRecordConfigs.values());
        }
        return arrayList;
    }

    private List<AudioRecordingConfiguration> updateSnapshot(int event, int session, int source, int[] recordingInfo) {
        List<AudioRecordingConfiguration> configs;
        synchronized (this.mRecordConfigs) {
            boolean configChanged;
            switch (event) {
                case 0:
                    if (this.mRecordConfigs.remove(new Integer(session)) == null) {
                        configChanged = false;
                        break;
                    }
                    configChanged = true;
                    break;
                case 1:
                    AudioFormat clientFormat = new Builder().setEncoding(recordingInfo[0]).setChannelMask(recordingInfo[1]).setSampleRate(recordingInfo[2]).build();
                    AudioFormat deviceFormat = new Builder().setEncoding(recordingInfo[3]).setChannelMask(recordingInfo[4]).setSampleRate(recordingInfo[5]).build();
                    int patchHandle = recordingInfo[6];
                    Integer sessionKey = new Integer(session);
                    if (!this.mRecordConfigs.containsKey(sessionKey)) {
                        this.mRecordConfigs.put(sessionKey, new AudioRecordingConfiguration(session, source, clientFormat, deviceFormat, patchHandle));
                        configChanged = true;
                        break;
                    }
                    AudioRecordingConfiguration updatedConfig = new AudioRecordingConfiguration(session, source, clientFormat, deviceFormat, patchHandle);
                    if (!updatedConfig.equals(this.mRecordConfigs.get(sessionKey))) {
                        this.mRecordConfigs.remove(sessionKey);
                        this.mRecordConfigs.put(sessionKey, updatedConfig);
                        configChanged = true;
                        break;
                    }
                    configChanged = false;
                    break;
                default:
                    Log.e(TAG, String.format("Unknown event %d for session %d, source %d", new Object[]{Integer.valueOf(event), Integer.valueOf(session), Integer.valueOf(source)}));
                    configChanged = false;
                    break;
            }
            if (configChanged) {
                configs = new ArrayList(this.mRecordConfigs.values());
            } else {
                configs = null;
            }
        }
        return configs;
    }
}
