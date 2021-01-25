package ohos.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadsetClientCall;
import android.os.Bundle;
import android.os.ParcelUuid;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.event.commonevent.CommonEventBaseConverter;
import ohos.event.commonevent.CommonEventSupport;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.SequenceUuid;

public class AdapterStateChangeEvt extends CommonEventBaseConverter {
    private static final Map<String, String> ACTION_MAP;
    private static final Map<String, String> PARA_MAP_EXT;
    private static final Set<String> PARA_WITH_CALL_SET;
    private static final Set<String> PARA_WITH_CLASS_SET;
    private static final Set<String> PARA_WITH_DEVICE_SET;
    private static final Set<String> PARA_WITH_UUID_SET;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "AdapterStateChange");

    static {
        HashMap hashMap = new HashMap();
        hashMap.put("android.bluetooth.adapter.action.STATE_CHANGED", "usual.event.bluetooth.host.STATE_UPDATE");
        hashMap.put("android.bluetooth.adapter.action.REQUEST_ENABLE", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HOST_REQ_ENABLE);
        hashMap.put("android.bluetooth.adapter.action.REQUEST_DISABLE", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HOST_REQ_DISABLE);
        hashMap.put("android.bluetooth.adapter.action.SCAN_MODE_CHANGED", "usual.event.bluetooth.host.SCAN_MODE_UPDATE");
        hashMap.put("android.bluetooth.adapter.action.DISCOVERY_STARTED", "usual.event.bluetooth.host.DISCOVERY_STARTED");
        hashMap.put("android.bluetooth.adapter.action.DISCOVERY_FINISHED", "usual.event.bluetooth.host.DISCOVERY_FINISHED");
        hashMap.put("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED", "usual.event.bluetooth.host.NAME_UPDATE");
        hashMap.put("android.bluetooth.device.action.FOUND", "usual.event.bluetooth.remotedevice.DISCOVERED");
        hashMap.put("android.bluetooth.device.action.CLASS_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CLASS_VALUE_UPDATE);
        hashMap.put("android.bluetooth.device.action.ACL_CONNECTED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_ACL_CONNECTED);
        hashMap.put("android.bluetooth.device.action.ACL_DISCONNECTED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_ACL_DISCONNECTED);
        hashMap.put("android.bluetooth.device.action.NAME_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_NAME_UPDATE);
        hashMap.put("android.bluetooth.device.action.BOND_STATE_CHANGED", "usual.event.bluetooth.remotedevice.PAIR_STATE");
        hashMap.put("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_BATTERY_VALUE_UPDATE);
        hashMap.put("android.bluetooth.device.action.SDP_RECORD", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_SDP_RESULT);
        hashMap.put("android.bluetooth.device.action.UUID", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_UUID_VALUE);
        hashMap.put("android.bluetooth.device.action.PAIRING_REQUEST", "usual.event.bluetooth.remotedevice.PAIRING_REQ");
        hashMap.put("android.bluetooth.device.action.PAIRING_CANCEL", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_PAIRING_CANCEL);
        hashMap.put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_REQ);
        hashMap.put("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_REPLY);
        hashMap.put("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_CANCEL);
        hashMap.put("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CONNECT_STATE_UPDATE);
        hashMap.put("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CURRENT_DEVICE_UPDATE);
        hashMap.put("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_PLAYING_STATE_UPDATE);
        hashMap.put("android.bluetooth.a2dp.profile.action.AVRCP_CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_AVRCP_CONNECT_STATE_UPDATE);
        hashMap.put("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CODEC_VALUE_UPDATE);
        hashMap.put("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREE_AG_CONNECT_STATE_UPDATE);
        hashMap.put("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREE_AG_CURRENT_DEVICE_UPDATE);
        hashMap.put("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED", "usual.event.bluetooth.handsfree.ag.AUDIO_STATE_UPDATE");
        hashMap.put("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED", "usual.event.bluetooth.handsfreeunit.CONNECT_STATE_UPDATE");
        hashMap.put("android.bluetooth.headsetclient.profile.action.AUDIO_STATE_CHANGED", "usual.event.bluetooth.handsfreeunit.AUDIO_STATE_UPDATE");
        hashMap.put("android.bluetooth.headsetclient.profile.action.AG_EVENT", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREEUNIT_AG_COMMON_EVENT);
        hashMap.put("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED", "usual.event.bluetooth.handsfreeunit.AG_CALL_STATE_UPDATE");
        hashMap.put("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSINK_CONNECT_STATE_UPDATE);
        ACTION_MAP = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap();
        hashMap2.put("android.bluetooth.adapter.extra.PREVIOUS_STATE", "usual.event.bluetoothhost.PARAM_PRE_STATE");
        hashMap2.put("android.bluetooth.adapter.extra.STATE", BluetoothHost.HOST_PARAM_CUR_STATE);
        hashMap2.put("android.bluetooth.adapter.extra.SCAN_MODE", BluetoothHost.HOST_PARAM_SCAN_METHOD);
        hashMap2.put("android.bluetooth.adapter.extra.LOCAL_NAME", BluetoothHost.HOST_PARAM_HOST_NAME);
        hashMap2.put("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", BluetoothHost.HOST_PARAM_DISCOVERABLE_TERM);
        hashMap2.put("android.bluetooth.device.extra.DEVICE", BluetoothRemoteDevice.REMOTE_DEVICE_PARAM_DEVICE);
        hashMap2.put("android.bluetooth.device.extra.CLASS", "usual.event.remotedevice.PARAM_CAT");
        hashMap2.put("android.bluetooth.device.extra.NAME", "usual.event.remotedevice.PARAM_NAME");
        hashMap2.put("android.bluetooth.device.extra.MANUFACTURER_SPECIFIC_DATA", "usual.event.remotedevice.PARAM_MANUFACTURER_CUSTOM_DATA");
        hashMap2.put("android.bluetooth.device.extra.BOND_STATE", BluetoothRemoteDevice.REMOTE_DEVICE_PARAM_PAIR_STATE);
        hashMap2.put("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", BluetoothRemoteDevice.REMOTE_DEVICE_PARAM_PREV_PAIR_STATE);
        hashMap2.put("android.bluetooth.device.extra.BATTERY_LEVEL", "usual.event.remotedevice.PARAM_BATTERY");
        hashMap2.put("android.bluetooth.device.extra.SDP_SEARCH_STATUS", "usual.event.remotedevice.PARAM_SDP_DISCOVER_STATE");
        hashMap2.put("android.bluetooth.device.extra.UUID", "usual.event.remotedevice.PARAM_PARAM_UUID");
        hashMap2.put("android.bluetooth.device.extra.PAIRING_KEY", BluetoothRemoteDevice.REMOTE_DEVICE_PARAM_PAIRING_KEY);
        hashMap2.put("android.bluetooth.device.extra.PAIRING_VARIANT", BluetoothRemoteDevice.REMOTE_DEVICE_PARAM_PAIRING_FORMAT);
        hashMap2.put("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", "usual.event.remotedevice.PARAM_REQ_TYPE");
        hashMap2.put("android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT", "usual.event.remotedevice.PARAM_CONNCT_RES");
        hashMap2.put("android.bluetooth.device.extra.ALWAYS_ALLOWED", "usual.event.remotedevice.PARAM_KEEP_ALLOWED");
        hashMap2.put("android.bluetooth.device.extra.RSSI", "usual.event.remotedevice.PARAM_RSSI");
        hashMap2.put("android.bluetooth.device.extra.REASON", "usual.event.remotedevice.PARAM_REASON");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_3WAY_CALLING", "usual.event.handsfreeunit.PARAM_3WAY_MEETING");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_VOICE_RECOGNITION", "usual.event.handsfreeunit.PARAM_AG_VOICE_RECOGNITION");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_REJECT_CALL", "usual.event.handsfreeunit.PARAM_AG_REJECT_CALL");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ECC", "usual.event.handsfreeunit.PARAM_AG_ECC");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL", "usual.event.handsfreeunit.PARAM_AG_ENTER_HELD_OR_WAITING_CALL");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL", "usual.event.handsfreeunit.PARAM_AG_EXIT_HELD_OR_WAITING_CALL");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT", "usual.event.handsfreeunit.PARAM_AG_EXIT_AND_ENTER");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE", "usual.event.handsfreeunit.PARAM_AG_COMBINE");
        hashMap2.put("android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE_AND_DETACH", "usual.event.handsfreeunit.PARAM_AG_COMBINE_AND_DETACH");
        hashMap2.put("android.bluetooth.headsetclient.extra.AUDIO_WBS", "usual.event.handsfreeunit.PARAM_AUDIO_WBS");
        hashMap2.put("android.bluetooth.headsetclient.extra.NETWORK_STATUS", "usual.event.handsfreeunit.PARAM_NETWORK_STATUS");
        hashMap2.put("android.bluetooth.headsetclient.extra.OPERATOR_NAME", "usual.event.handsfreeunit.PARAM_OPERATOR_NAME");
        hashMap2.put("android.bluetooth.headsetclient.extra.NETWORK_ROAMING", "usual.event.handsfreeunit.PARAM_NETWORK_ROAMING");
        hashMap2.put("android.bluetooth.headsetclient.extra.NETWORK_SIGNAL_STRENGTH", "usual.event.handsfreeunit.PARAM_NETWORK_SIGNAL_STRENGTH");
        hashMap2.put("android.bluetooth.headsetclient.extra.BATTERY_LEVEL", "usual.event.handsfreeunit.PARAM_BATTERY_LEVEL");
        hashMap2.put("android.bluetooth.headsetclient.extra.VOICE_RECOGNITION", "usual.event.handsfreeunit.PARAM_VOICE_RECOGNITION");
        hashMap2.put("android.bluetooth.headsetclient.extra.SUBSCRIBER_INFO", "usual.event.handsfreeunit.PARAM_SUBSCRIBER_INFO");
        hashMap2.put("android.bluetooth.headsetclient.extra.IN_BAND_RING", "usual.event.handsfreeunit.PARAM_IN_BAND_RING");
        hashMap2.put("android.bluetooth.headsetclient.extra.CALL", "usual.event.handsfreeunit.PARAM_CALL");
        hashMap2.put("android.bluetooth.profile.extra.PREVIOUS_STATE", "usual.event.bluetooth.profile.PARAM_PRE_STATE");
        hashMap2.put("android.bluetooth.profile.extra.STATE", ProfileBase.PROFILE_PARAM_CUR_STATE);
        PARA_MAP_EXT = Collections.unmodifiableMap(hashMap2);
        HashSet hashSet = new HashSet();
        hashSet.add("android.bluetooth.device.action.FOUND");
        hashSet.add("android.bluetooth.device.action.CLASS_CHANGED");
        hashSet.add("android.bluetooth.device.action.ACL_CONNECTED");
        hashSet.add("android.bluetooth.device.action.ACL_DISCONNECTED");
        hashSet.add("android.bluetooth.device.action.NAME_CHANGED");
        hashSet.add("android.bluetooth.device.action.BOND_STATE_CHANGED");
        hashSet.add("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        hashSet.add("android.bluetooth.device.action.SDP_RECORD");
        hashSet.add("android.bluetooth.device.action.UUID");
        hashSet.add("android.bluetooth.device.action.PAIRING_REQUEST");
        hashSet.add("android.bluetooth.device.action.PAIRING_CANCEL");
        hashSet.add("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST");
        hashSet.add("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        hashSet.add("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        hashSet.add("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        hashSet.add("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED");
        hashSet.add("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED");
        hashSet.add("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED");
        hashSet.add("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        hashSet.add("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED");
        hashSet.add("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        hashSet.add("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED");
        hashSet.add("android.bluetooth.headsetclient.profile.action.AUDIO_STATE_CHANGED");
        hashSet.add("android.bluetooth.headsetclient.profile.action.AG_EVENT");
        hashSet.add("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED");
        hashSet.add("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED");
        PARA_WITH_DEVICE_SET = Collections.unmodifiableSet(hashSet);
        HashSet hashSet2 = new HashSet();
        hashSet2.add("android.bluetooth.device.action.FOUND");
        hashSet2.add("android.bluetooth.device.action.CLASS_CHANGED");
        PARA_WITH_CLASS_SET = Collections.unmodifiableSet(hashSet2);
        HashSet hashSet3 = new HashSet();
        hashSet3.add("android.bluetooth.device.action.SDP_RECORD");
        hashSet3.add("android.bluetooth.device.action.UUID");
        PARA_WITH_UUID_SET = Collections.unmodifiableSet(hashSet3);
        HashSet hashSet4 = new HashSet();
        hashSet4.add("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED");
        PARA_WITH_CALL_SET = Collections.unmodifiableSet(hashSet4);
    }

    @Override // ohos.event.commonevent.CommonEventBaseConverter
    public Optional<Intent> convertAospIntentToIntent(android.content.Intent intent) {
        android.content.Intent intent2 = new android.content.Intent(intent);
        Intent intent3 = new Intent();
        boolean fillAction = fillAction(intent2, intent3) & true;
        if (intent3.getAction() == null) {
            HiLog.error(TAG, "acting is not filled", new Object[0]);
            return Optional.ofNullable(intent3);
        }
        HiLog.debug(TAG, "receiving action: %{public}s", new Object[]{intent3.getAction()});
        if (PARA_WITH_DEVICE_SET.contains(intent.getAction())) {
            fillAction &= fillRemoteDevice(intent2, intent3);
        }
        if (PARA_WITH_CLASS_SET.contains(intent.getAction())) {
            fillAction &= fillRemoteDeviceClass(intent2, intent3);
        }
        if (PARA_WITH_UUID_SET.contains(intent.getAction())) {
            fillAction &= fillUuid(intent2, intent3);
        }
        if (PARA_WITH_CALL_SET.contains(intent.getAction())) {
            fillAction &= fillHandsFreeUnitCall(intent2, intent3);
        }
        if (!(fillAction & fillCommonExtras(intent2, intent3)) || !fillFlags(intent2, intent3)) {
            HiLog.warn(TAG, "some of the content from origin action: %{public}s is not converted", new Object[]{intent3.getAction()});
        }
        return Optional.ofNullable(intent3);
    }

    private boolean fillAction(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in converting action", new Object[0]);
            return false;
        }
        String action = intent.getAction();
        if (action == null) {
            HiLog.error(TAG, "null parameters", new Object[0]);
            return false;
        }
        String str = ACTION_MAP.get(action);
        if (str == null) {
            HiLog.warn(TAG, "action is not registerd", new Object[0]);
            return false;
        }
        intent2.setAction(str);
        return true;
    }

    private static boolean fillCommonExtras(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in convertAction", new Object[0]);
            return false;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return true;
        }
        IntentParams params = intent2.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        Set<String> keySet = extras.keySet();
        if (keySet != null) {
            for (String str : keySet) {
                params.setParam(PARA_MAP_EXT.get(str), extras.get(str));
            }
        }
        intent2.setParams(params);
        return true;
    }

    private static boolean fillFlags(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in convertAction", new Object[0]);
            return false;
        }
        int flags = intent.getFlags();
        if (flags == 0) {
            return true;
        }
        intent2.setFlags(flags);
        return true;
    }

    private static boolean fillRemoteDevice(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in convertAction", new Object[0]);
            return false;
        }
        BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        intent.removeExtra("android.bluetooth.device.extra.DEVICE");
        if (bluetoothDevice == null) {
            return true;
        }
        BluetoothRemoteDevice bluetoothRemoteDevice = new BluetoothRemoteDevice(bluetoothDevice.getAddress());
        IntentParams params = intent2.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        String str = PARA_MAP_EXT.get("android.bluetooth.device.extra.DEVICE");
        if (str == null) {
            HiLog.error(TAG, "can not find corresponding zidane parameter", new Object[0]);
            return false;
        }
        params.setParam(str, bluetoothRemoteDevice);
        intent2.setParams(params);
        return true;
    }

    private static boolean fillHandsFreeUnitCall(android.content.Intent intent, Intent intent2) {
        BluetoothRemoteDevice bluetoothRemoteDevice;
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in converting actions", new Object[0]);
            return false;
        }
        BluetoothHeadsetClientCall parcelableExtra = intent.getParcelableExtra("android.bluetooth.headsetclient.extra.CALL");
        intent.removeExtra("android.bluetooth.headsetclient.extra.CALL");
        if (parcelableExtra == null) {
            return true;
        }
        int id = parcelableExtra.getId();
        UUID uuid = parcelableExtra.getUUID();
        int state = parcelableExtra.getState();
        String number = parcelableExtra.getNumber();
        boolean isMultiParty = parcelableExtra.isMultiParty();
        boolean isOutgoing = parcelableExtra.isOutgoing();
        boolean isInBandRing = parcelableExtra.isInBandRing();
        BluetoothDevice device = parcelableExtra.getDevice();
        if (device != null) {
            bluetoothRemoteDevice = new BluetoothRemoteDevice(device.getAddress());
        } else {
            bluetoothRemoteDevice = new BluetoothRemoteDevice();
        }
        HandsFreeUnitCall handsFreeUnitCall = new HandsFreeUnitCall(id, uuid, state, number, isMultiParty, isOutgoing, isInBandRing, bluetoothRemoteDevice);
        IntentParams params = intent2.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        String str = PARA_MAP_EXT.get("android.bluetooth.headsetclient.extra.CALL");
        if (str == null) {
            HiLog.error(TAG, "can not find corresponding zidane parameter", new Object[0]);
            return false;
        }
        params.setParam(str, handsFreeUnitCall);
        intent2.setParams(params);
        return true;
    }

    private static boolean fillRemoteDeviceClass(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in convertAction", new Object[0]);
            return false;
        }
        BluetoothClass bluetoothClass = (BluetoothClass) intent.getParcelableExtra("android.bluetooth.device.extra.CLASS");
        intent.removeExtra("android.bluetooth.device.extra.CLASS");
        if (bluetoothClass == null) {
            return true;
        }
        int deviceClass = bluetoothClass.getDeviceClass();
        IntentParams params = intent2.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        String str = PARA_MAP_EXT.get("android.bluetooth.device.extra.CLASS");
        if (str == null) {
            HiLog.error(TAG, "can not find corresponding zidane parameter", new Object[0]);
            return false;
        }
        params.setParam(str, Integer.valueOf(deviceClass));
        intent2.setParams(params);
        return true;
    }

    private static boolean fillUuid(android.content.Intent intent, Intent intent2) {
        if (intent == null || intent2 == null) {
            HiLog.error(TAG, "null input intents in convertAction", new Object[0]);
            return false;
        }
        ParcelUuid parcelUuid = (ParcelUuid) intent.getParcelableExtra("android.bluetooth.device.extra.UUID");
        intent.removeExtra("android.bluetooth.device.extra.UUID");
        if (parcelUuid == null) {
            return true;
        }
        SequenceUuid sequenceUuid = new SequenceUuid(parcelUuid.getUuid());
        IntentParams params = intent2.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        String str = PARA_MAP_EXT.get("android.bluetooth.device.extra.UUID");
        if (str == null) {
            HiLog.error(TAG, "can not find corresponding zidane parameter", new Object[0]);
            return false;
        }
        params.setParam(str, sequenceUuid);
        intent2.setParams(params);
        return true;
    }
}
