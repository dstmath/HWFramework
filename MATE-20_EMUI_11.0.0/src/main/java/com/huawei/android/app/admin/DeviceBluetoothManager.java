package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class DeviceBluetoothManager {
    private static final String BLUETOOTH_BLACK_LIST = "bt-black-list";
    private static final String BLUETOOTH_BLOCK_LIST = "bt-block-list";
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
    private static final String BLUETOOTH_TRUST_LIST = "bt-trust-list";
    private static final String BLUETOOTH_WHITE_LIST = "bt-white-list";
    private static final int MAX_LIST_NUM_LIMIT = 200;
    private static final String STATE_VALUE = "value";
    private static final String TAG = DeviceBluetoothManager.class.getSimpleName();
    private static Pattern mCheckPattern = Pattern.compile(BLUETOOTH_MAC_PATTERN);
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    @Deprecated
    public boolean addBluetoothDevicesToBlackList(ComponentName admin, ArrayList<String> devices) {
        return addBluetoothDevicesToCacheBlockList(admin, devices, BLUETOOTH_BLACK_LIST);
    }

    public boolean addBluetoothDevicesToBlockList(ComponentName admin, ArrayList<String> devices) {
        return addBluetoothDevicesToCacheBlockList(admin, devices, BLUETOOTH_BLOCK_LIST);
    }

    private boolean addBluetoothDevicesToCacheBlockList(ComponentName admin, ArrayList<String> devices, String policyName) {
        ArrayList<String> finalDevLists = checkBluetoothDevicesList(admin, BLUETOOTH_BLACK_LIST, devices);
        if (finalDevLists == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", finalDevLists);
        return this.mDpm.setPolicy(admin, policyName, bundle);
    }

    @Deprecated
    public boolean addBluetoothDevicesToWhiteList(ComponentName admin, ArrayList<String> devices) {
        return addBluetoothDevicesToCacheTrustList(admin, devices, BLUETOOTH_WHITE_LIST);
    }

    public boolean addBluetoothDevicesToTrustList(ComponentName admin, ArrayList<String> devices) {
        return addBluetoothDevicesToCacheTrustList(admin, devices, BLUETOOTH_TRUST_LIST);
    }

    private boolean addBluetoothDevicesToCacheTrustList(ComponentName admin, ArrayList<String> devices, String policyName) {
        ArrayList<String> finalDevLists = checkBluetoothDevicesList(admin, BLUETOOTH_WHITE_LIST, devices);
        if (finalDevLists == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", finalDevLists);
        return this.mDpm.setPolicy(admin, policyName, bundle);
    }

    @Deprecated
    public boolean removeBluetoothDevicesFromBlackList(ComponentName admin, ArrayList<String> devices) {
        return removeBluetoothDevicesFromBlockList(admin, devices);
    }

    public boolean removeBluetoothDevicesFromBlockList(ComponentName admin, ArrayList<String> devices) {
        Bundle bundle;
        if (devices == null) {
            Log.i(TAG, "remove all blocklist");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_BLACK_LIST);
        } else {
            bundle = new Bundle();
            bundle.putStringArrayList("value", devices);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_BLACK_LIST, bundle);
    }

    @Deprecated
    public boolean removeBluetoothDevicesFromWhiteList(ComponentName admin, ArrayList<String> devices) {
        return removeBluetoothDevicesFromTrustList(admin, devices);
    }

    public boolean removeBluetoothDevicesFromTrustList(ComponentName admin, ArrayList<String> devices) {
        Bundle bundle;
        if (devices == null) {
            Log.i(TAG, "remove all trustlist");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_WHITE_LIST);
        } else {
            bundle = new Bundle();
            bundle.putStringArrayList("value", devices);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_WHITE_LIST, bundle);
    }

    @Deprecated
    public ArrayList<String> getBluetoothDevicesFromBlackLists(ComponentName admin) {
        return getBluetoothDevicesFromBlockList(admin);
    }

    public ArrayList<String> getBluetoothDevicesFromBlockList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_BLACK_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getBluetoothDevicesFromBlockList exception.");
            return null;
        }
    }

    @Deprecated
    public ArrayList<String> getBluetoothDevicesFromWhiteLists(ComponentName admin) {
        return getBluetoothDevicesFromTrustList(admin);
    }

    public ArrayList<String> getBluetoothDevicesFromTrustList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getBluetoothDevicesFromTrustList exception.");
            return null;
        }
    }

    @Deprecated
    public boolean isBlackListedDevice(ComponentName admin, String device) {
        return isSpecialTypeDevice(admin, device, BLUETOOTH_BLACK_LIST);
    }

    @Deprecated
    public boolean isWhiteListedDevice(ComponentName admin, String device) {
        return isSpecialTypeDevice(admin, device, BLUETOOTH_WHITE_LIST);
    }

    public boolean isDeviceInBlockList(ComponentName admin, String device) {
        return isSpecialTypeDevice(admin, device, BLUETOOTH_BLACK_LIST);
    }

    public boolean isDeviceInTrustList(ComponentName admin, String device) {
        return isSpecialTypeDevice(admin, device, BLUETOOTH_WHITE_LIST);
    }

    private boolean isSpecialTypeDevice(ComponentName admin, String device, String type) {
        Bundle bundle;
        if (TextUtils.isEmpty(device) || (bundle = this.mDpm.getPolicy(admin, type)) == null) {
            return false;
        }
        ArrayList<String> lists = null;
        try {
            lists = bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "isSpecialTypeDevice exception.");
        }
        if (lists != null) {
            return lists.contains(device);
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

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014  */
    private boolean checkBluetoothMacFormat(ArrayList<String> devices) {
        if (devices == null || devices.size() == 0) {
            return false;
        }
        Iterator<String> it = devices.iterator();
        while (it.hasNext()) {
            String dev = it.next();
            if (dev.length() != 17 || !mCheckPattern.matcher(dev).find()) {
                return false;
            }
            while (it.hasNext()) {
            }
        }
        return true;
    }

    private ArrayList<String> filterDuplicatedDevices(ComponentName admin, String checkType, ArrayList<String> devices) {
        ArrayList<String> currentDevices = getDeviceListsWithType(admin, checkType);
        ArrayList<String> filteredDevices = new ArrayList<>();
        ArrayList<String> tempDevices = new ArrayList<>();
        if (devices == null) {
            return null;
        }
        Iterator<String> it = devices.iterator();
        while (it.hasNext()) {
            String d = it.next();
            if (!tempDevices.contains(d)) {
                tempDevices.add(d);
            }
        }
        if (currentDevices == null || currentDevices.size() <= 0) {
            filteredDevices.addAll(tempDevices);
        } else {
            Iterator<String> it2 = tempDevices.iterator();
            while (it2.hasNext()) {
                String dev = it2.next();
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
        ArrayList<String> devices = new ArrayList<>();
        devices.addAll(filteredDevices);
        if (checkType.equals(BLUETOOTH_BLACK_LIST)) {
            bundle = this.mDpm.getPolicy(null, BLUETOOTH_WHITE_LIST);
        } else if (checkType.equals(BLUETOOTH_WHITE_LIST)) {
            bundle = this.mDpm.getPolicy(null, BLUETOOTH_BLACK_LIST);
        }
        if (bundle == null) {
            return devices;
        }
        ArrayList<String> finalDevices = new ArrayList<>();
        ArrayList<String> oppositeLists = bundle.getStringArrayList("value");
        if (oppositeLists == null || oppositeLists.size() == 0) {
            return devices;
        }
        Iterator<String> it = devices.iterator();
        while (it.hasNext()) {
            String dev = it.next();
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
        ArrayList<String> sProfiles = new ArrayList<>();
        Iterator<Integer> it = disabledProfiles.iterator();
        while (it.hasNext()) {
            sProfiles.add(it.next().toString());
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, sProfiles);
        return this.mDpm.setPolicy(admin, BLUETOOTH_SECURE_PROFILE, bundle);
    }

    public ArrayList<Integer> getBluetoothDisabledProfiles(ComponentName admin) {
        ArrayList<String> sProfiles;
        ArrayList<Integer> profilesList = new ArrayList<>();
        Bundle bundle = this.mDpm.getPolicy(admin, BLUETOOTH_SECURE_PROFILE);
        if (!(bundle == null || (sProfiles = bundle.getStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME)) == null)) {
            Iterator<String> it = sProfiles.iterator();
            while (it.hasNext()) {
                try {
                    profilesList.add(Integer.valueOf(Integer.parseInt(it.next())));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "profilesList : NumberFormatException");
                }
            }
        }
        String str = TAG;
        Log.d(str, " getBluetoothDisabledProfiles : " + profilesList.toString());
        return profilesList;
    }

    public boolean removeBluetoothDisabledProfiles(ComponentName admin, ArrayList<Integer> disabledProfiles) {
        Bundle bundle = new Bundle();
        if (disabledProfiles == null) {
            Log.i(TAG, "Remove all disabled profiles");
            bundle = this.mDpm.getPolicy(admin, BLUETOOTH_SECURE_PROFILE);
        } else {
            ArrayList<String> sProfiles = new ArrayList<>();
            Iterator<Integer> it = disabledProfiles.iterator();
            while (it.hasNext()) {
                sProfiles.add(it.next().toString());
            }
            bundle.putStringArrayList(BLUETOOTH_DISABLE_PROFILE_STRUCT_NAME, sProfiles);
        }
        return this.mDpm.removePolicy(admin, BLUETOOTH_SECURE_PROFILE, bundle);
    }

    public boolean isSecureModeProfile(int profile, String device) {
        Log.d(TAG, "isSecureModeProfile");
        if (device != null) {
            if (isDeviceInBlockList(null, device)) {
                return true;
            }
            if (isDeviceInTrustList(null, device)) {
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
