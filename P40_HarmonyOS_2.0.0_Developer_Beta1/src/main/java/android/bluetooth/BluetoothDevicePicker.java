package android.bluetooth;

public interface BluetoothDevicePicker {
    public static final String ACTION_DEVICE_SELECTED = "android.bluetooth.devicepicker.action.DEVICE_SELECTED";
    public static final String ACTION_LAUNCH = "android.bluetooth.devicepicker.action.LAUNCH";
    public static final String EXTRA_FILTER_TYPE = "android.bluetooth.devicepicker.extra.FILTER_TYPE";
    public static final String EXTRA_LAUNCH_CLASS = "android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS";
    public static final String EXTRA_LAUNCH_PACKAGE = "android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE";
    public static final String EXTRA_NEED_AUTH = "android.bluetooth.devicepicker.extra.NEED_AUTH";
    public static final int FILTER_TYPE_ALL = 0;
    public static final int FILTER_TYPE_AUDIO = 1;
    public static final int FILTER_TYPE_NAP = 4;
    public static final int FILTER_TYPE_PANU = 3;
    public static final int FILTER_TYPE_TRANSFER = 2;
}
