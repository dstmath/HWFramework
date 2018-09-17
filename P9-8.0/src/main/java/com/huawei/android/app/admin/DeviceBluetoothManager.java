package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DeviceBluetoothManager {
    private static final String BLUETOOTH_BLACK_LIST = "bt-black-list";
    private static final String BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME = "disabled";
    private static final String BLUETOOTH_DISCOVERABLE = "bt-discover";
    private static final String BLUETOOTH_DISCOVER_EXTRA_DATA = "discover";
    private static final String BLUETOOTH_FILE_TRANSFER_STATE = "bt-file-transfer-state";
    private static final String BLUETOOTH_LIMITED_DISCOVERABLE = "bt-limit-discover";
    private static final String BLUETOOTH_LIMITED_EXTRA_DATA = "limite";
    private static final int BLUETOOTH_MAC_LENGTH = 17;
    private static final String BLUETOOTH_MAC_PATTERN = "^[A-F0-9]{2}(:[A-F0-9]{2}){5}$";
    private static final String BLUETOOTH_OUTGOING_CALL_STATE = "bt-outgoing-call-state";
    private static final String BLUETOOTH_PAIRING_STATE = "bt-pairing-state";
    private static final String BLUETOOTH_SECURE_PROFILE = "bt-secure-profiles";
    private static final String BLUETOOTH_WHITE_LIST = "bt-white-list";
    private static final int MAX_LIST_NUM_LIMIT = 200;
    private static final String TAG = DeviceBluetoothManager.class.getSimpleName();
    private static Pattern mCheckPattern = Pattern.compile(BLUETOOTH_MAC_PATTERN);
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean addBluetoothDevicesToBlackList(ComponentName admin, ArrayList<String> devices) {
        ArrayList<String> finalDevLists = checkBluetoothDevicesList(admin, BLUETOOTH_BLACK_LIST, devices);
        if (finalDevLists == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", finalDevLists);
        return this.mDpm.setPolicy(admin, BLUETOOTH_BLACK_LIST, bundle);
    }

    public boolean addBluetoothDevicesToWhiteList(ComponentName admin, ArrayList<String> devices) {
        ArrayList<String> finalDevLists = checkBluetoothDevicesList(admin, BLUETOOTH_WHITE_LIST, devices);
        if (finalDevLists == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", finalDevLists);
        return this.mDpm.setPolicy(admin, BLUETOOTH_WHITE_LIST, bundle);
    }

    public boolean removeBluetoothDevicesFromBlackList(ComponentName admin, ArrayList<String> devices) {
        Bundle bundle;
        if (devices == null) {
            Log.i(TAG, "remove all blacklist");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_BLACK_LIST);
        } else {
            bundle = new Bundle();
            bundle.putStringArrayList("value", devices);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_BLACK_LIST, bundle);
    }

    public boolean removeBluetoothDevicesFromWhiteList(ComponentName admin, ArrayList<String> devices) {
        Bundle bundle;
        if (devices == null) {
            Log.i(TAG, "remove all whitelist");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_WHITE_LIST);
        } else {
            bundle = new Bundle();
            bundle.putStringArrayList("value", devices);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_WHITE_LIST, bundle);
    }

    public ArrayList<String> getBluetoothDevicesFromBlackLists(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_BLACK_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public ArrayList<String> getBluetoothDevicesFromWhiteLists(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean isBlackListedDevice(ComponentName admin, String device) {
        if (!(device == null || device.length() == 0)) {
            Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_BLACK_LIST);
            if (bundle == null) {
                return false;
            }
            ArrayList<String> lists = bundle.getStringArrayList("value");
            if (lists != null) {
                return lists.contains(device);
            }
        }
        return false;
    }

    public boolean isWhiteListedDevice(ComponentName admin, String device) {
        if (!(device == null || device.length() == 0)) {
            Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_WHITE_LIST);
            if (bundle == null) {
                return false;
            }
            ArrayList<String> lists = bundle.getStringArrayList("value");
            if (lists != null) {
                return lists.contains(device);
            }
        }
        return false;
    }

    private ArrayList<String> getDeviceListsWithType(ComponentName admin, String checkType) {
        Bundle bundle = this.mDpm.getPolicy(admin, checkType);
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return null;
    }

    private boolean checkBluetoothMacTotal(ComponentName admin, String checkType, int setNum) {
        int count = 0;
        ArrayList<String> currentDevices = getDeviceListsWithType(admin, checkType);
        if (currentDevices != null) {
            count = currentDevices.size();
        }
        if (count + setNum <= 200) {
            return true;
        }
        Log.d(TAG, "too many devices to set.");
        return false;
    }

    private boolean checkBluetoothMacFormat(ArrayList<String> devices) {
        if (devices == null || devices.size() == 0) {
            return false;
        }
        for (String dev : devices) {
            if (dev.length() != 17) {
                return false;
            }
            if (!mCheckPattern.matcher(dev).find()) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<String> filterDuplicatedDevices(ComponentName admin, String checkType, ArrayList<String> devices) {
        ArrayList<String> currentDevices = getDeviceListsWithType(admin, checkType);
        ArrayList<String> filteredDevices = new ArrayList();
        ArrayList<String> tempDevices = new ArrayList();
        if (devices == null) {
            return null;
        }
        for (String d : devices) {
            if (!tempDevices.contains(d)) {
                tempDevices.add(d);
            }
        }
        if (currentDevices == null || currentDevices.size() <= 0) {
            filteredDevices.addAll(tempDevices);
        } else {
            for (String dev : tempDevices) {
                if (!currentDevices.contains(dev)) {
                    filteredDevices.add(dev);
                }
            }
        }
        if (filteredDevices.size() != 0) {
            return filteredDevices;
        }
        Log.e(TAG, "empty after filtering devices.");
        return null;
    }

    private ArrayList<String> checkBluetoothListConflict(String checkType, ArrayList<String> filteredDevices) {
        Bundle bundle = null;
        ArrayList<String> devices = new ArrayList();
        devices.addAll(filteredDevices);
        if (checkType.equals(BLUETOOTH_BLACK_LIST)) {
            bundle = this.mDpm.getPolicy(null, BLUETOOTH_WHITE_LIST);
        } else if (checkType.equals(BLUETOOTH_WHITE_LIST)) {
            bundle = this.mDpm.getPolicy(null, BLUETOOTH_BLACK_LIST);
        }
        if (bundle == null) {
            return devices;
        }
        ArrayList<String> finalDevices = new ArrayList();
        ArrayList<String> oppositeLists = bundle.getStringArrayList("value");
        if (oppositeLists == null || oppositeLists.size() == 0) {
            return devices;
        }
        for (String dev : devices) {
            if (!oppositeLists.contains(dev)) {
                finalDevices.add(dev);
            }
        }
        if (finalDevices.size() != 0) {
            return finalDevices;
        }
        Log.e(TAG, "final devs empty.");
        return null;
    }

    private ArrayList<String> checkBluetoothDevicesList(ComponentName admin, String checkType, ArrayList<String> devices) {
        ArrayList<String> filteredDevices = filterDuplicatedDevices(admin, checkType, devices);
        if (filteredDevices == null) {
            Log.e(TAG, "null after filter.");
            return null;
        } else if (!checkBluetoothMacTotal(admin, checkType, filteredDevices.size())) {
            Log.e(TAG, "beyond total num.");
            return null;
        } else if (checkBluetoothMacFormat(filteredDevices)) {
            return checkBluetoothListConflict(checkType, filteredDevices);
        } else {
            Log.e(TAG, "format invalid.");
            return null;
        }
    }

    public boolean setBluetoothDataTransferDisable(ComponentName admin, boolean isDisable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, isDisable);
        return this.mDpm.setPolicy(admin, BLUETOOTH_FILE_TRANSFER_STATE, bundle);
    }

    public boolean isBluetoothDataTransferDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_FILE_TRANSFER_STATE);
        if (bundle != null) {
            return bundle.getBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, false);
        }
        return false;
    }

    public boolean setBluetoothPairingDisable(ComponentName admin, boolean isDisable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, isDisable);
        return this.mDpm.setPolicy(admin, BLUETOOTH_PAIRING_STATE, bundle);
    }

    public boolean isBluetoothPairingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_PAIRING_STATE);
        if (bundle != null) {
            return bundle.getBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, false);
        }
        return false;
    }

    public boolean setBluetoothOutGoingCallDisable(ComponentName admin, boolean isDisable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, isDisable);
        return this.mDpm.setPolicy(admin, BLUETOOTH_OUTGOING_CALL_STATE, bundle);
    }

    public boolean isBluetoothOutGoingCallDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_OUTGOING_CALL_STATE);
        if (bundle != null) {
            return bundle.getBoolean(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, false);
        }
        return false;
    }

    public boolean setBluetoothDisabledProfiles(ComponentName admin, ArrayList<Integer> disabledProfiles) {
        if (disabledProfiles == null) {
            return false;
        }
        ArrayList<String> sProfiles = new ArrayList();
        for (Integer profile : disabledProfiles) {
            sProfiles.add(profile.toString());
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, sProfiles);
        return this.mDpm.setPolicy(admin, BLUETOOTH_SECURE_PROFILE, bundle);
    }

    public ArrayList<Integer> getBluetoothDisabledProfiles(ComponentName admin) {
        ArrayList<Integer> profilesList = new ArrayList();
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_SECURE_PROFILE);
        if (bundle != null) {
            ArrayList<String> sProfiles = bundle.getStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME);
            if (sProfiles != null) {
                for (String profile : sProfiles) {
                    profilesList.add(Integer.valueOf(Integer.parseInt(profile)));
                }
            }
        }
        Log.d(TAG, " getBluetoothDisabledProfiles : " + profilesList.toString());
        return profilesList;
    }

    public boolean removeBluetoothDisabledProfiles(ComponentName admin, ArrayList<Integer> disabledProfiles) {
        Bundle bundle = new Bundle();
        if (disabledProfiles == null) {
            Log.i(TAG, "Remove all disabled profiles");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_SECURE_PROFILE);
        } else {
            ArrayList<String> sProfiles = new ArrayList();
            for (Integer profile : disabledProfiles) {
                sProfiles.add(profile.toString());
            }
            bundle.putStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, sProfiles);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_SECURE_PROFILE, bundle);
    }

    public boolean isSecureModeProfile(int profile, String device) {
        Log.d(TAG, "isSecureModeProfile");
        if (device != null) {
            if (isBlackListedDevice(null, device)) {
                return true;
            }
            if (isWhiteListedDevice(null, device)) {
                return false;
            }
        }
        if (getBluetoothDisabledProfiles(null).contains(Integer.valueOf(profile))) {
            return true;
        }
        Log.d(TAG, " In SecureModeProfile");
        return false;
    }

    public boolean setDiscoverableDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BLUETOOTH_DISCOVER_EXTRA_DATA, disable);
        return this.mDpm.setPolicy(admin, BLUETOOTH_DISCOVERABLE, bundle);
    }

    public boolean isDiscoverableDisabled(ComponentName admin) {
        Bundle disBundle = this.mDpm.getPolicy(admin, BLUETOOTH_DISCOVERABLE);
        if (disBundle != null) {
            return disBundle.getBoolean(BLUETOOTH_DISCOVER_EXTRA_DATA);
        }
        return false;
    }

    public boolean setLimitedDiscoverableDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BLUETOOTH_LIMITED_EXTRA_DATA, disable);
        return this.mDpm.setPolicy(admin, BLUETOOTH_LIMITED_DISCOVERABLE, bundle);
    }

    public boolean isLimitedDiscoverableDisabled(ComponentName admin) {
        Bundle limiteBundle = this.mDpm.getPolicy(admin, BLUETOOTH_LIMITED_DISCOVERABLE);
        if (limiteBundle != null) {
            return limiteBundle.getBoolean(BLUETOOTH_LIMITED_EXTRA_DATA);
        }
        return false;
    }
}
