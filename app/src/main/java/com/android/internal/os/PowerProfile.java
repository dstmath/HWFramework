package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

public class PowerProfile {
    private static final String ATTR_NAME = "name";
    public static final String POWER_AUDIO = "dsp.audio";
    public static final String POWER_BATTERY_CAPACITY = "battery.capacity";
    @Deprecated
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";
    @Deprecated
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";
    public static final String POWER_BLUETOOTH_CONTROLLER_IDLE = "bluetooth.controller.idle";
    public static final String POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE = "bluetooth.controller.voltage";
    public static final String POWER_BLUETOOTH_CONTROLLER_RX = "bluetooth.controller.rx";
    public static final String POWER_BLUETOOTH_CONTROLLER_TX = "bluetooth.controller.tx";
    @Deprecated
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
    public static final String POWER_CAMERA = "camera.avg";
    @Deprecated
    public static final String POWER_CPU_ACTIVE = "cpu.active";
    public static final String POWER_CPU_AWAKE = "cpu.awake";
    private static final String POWER_CPU_CLUSTER_ACTIVE_PREFIX = "cpu.active.cluster";
    private static final String POWER_CPU_CLUSTER_CORE_COUNT = "cpu.clusters.cores";
    private static final String POWER_CPU_CLUSTER_SPEED_PREFIX = "cpu.speeds.cluster";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    @Deprecated
    public static final String POWER_CPU_SPEEDS = "cpu.speeds";
    public static final String POWER_FLASHLIGHT = "camera.flashlight";
    public static final String POWER_GPS_ON = "gps.on";
    public static final String POWER_MODEM_CONTROLLER_IDLE = "modem.controller.idle";
    public static final String POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE = "modem.controller.voltage";
    public static final String POWER_MODEM_CONTROLLER_RX = "modem.controller.rx";
    public static final String POWER_MODEM_CONTROLLER_TX = "modem.controller.tx";
    public static final String POWER_NONE = "none";
    public static final String POWER_RADIO_ACTIVE = "radio.active";
    public static final String POWER_RADIO_ON = "radio.on";
    public static final String POWER_RADIO_SCANNING = "radio.scanning";
    public static final String POWER_SCREEN_FULL = "screen.full";
    public static final String POWER_SCREEN_ON = "screen.on";
    public static final String POWER_VIDEO = "dsp.video";
    public static final String POWER_WIFI_ACTIVE = "wifi.active";
    public static final String POWER_WIFI_BATCHED_SCAN = "wifi.batchedscan";
    public static final String POWER_WIFI_CONTROLLER_IDLE = "wifi.controller.idle";
    public static final String POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE = "wifi.controller.voltage";
    public static final String POWER_WIFI_CONTROLLER_RX = "wifi.controller.rx";
    public static final String POWER_WIFI_CONTROLLER_TX = "wifi.controller.tx";
    public static final String POWER_WIFI_CONTROLLER_TX_LEVELS = "wifi.controller.tx_levels";
    public static final String POWER_WIFI_ON = "wifi.on";
    public static final String POWER_WIFI_SCAN = "wifi.scan";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    static final HashMap<String, Object> sPowerMap = null;
    private CpuClusterKey[] mCpuClusters;

    public static class CpuClusterKey {
        private final int numCpus;
        private final String powerKey;
        private final String timeKey;

        private CpuClusterKey(String timeKey, String powerKey, int numCpus) {
            this.timeKey = timeKey;
            this.powerKey = powerKey;
            this.numCpus = numCpus;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.PowerProfile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.PowerProfile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.PowerProfile.<clinit>():void");
    }

    public PowerProfile(Context context) {
        if (sPowerMap.size() == 0 && !HwFrameworkFactory.getHwPowerProfileManager().readHwPowerValuesFromXml(sPowerMap)) {
            readPowerValuesFromXml(context);
        }
        initCpuClusters();
    }

    private void readPowerValuesFromXml(Context context) {
        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(R.xml.power_profile);
        boolean parsingArray = false;
        ArrayList<Double> array = new ArrayList();
        Object arrayName = null;
        try {
            XmlUtils.beginDocument(parser, TAG_DEVICE);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                }
                if (parsingArray) {
                    if (!element.equals(TAG_ARRAYITEM)) {
                        sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                        parsingArray = false;
                    }
                }
                if (element.equals(TAG_ARRAY)) {
                    parsingArray = true;
                    array.clear();
                    arrayName = parser.getAttributeValue(null, ATTR_NAME);
                } else {
                    if (!element.equals(TAG_ITEM)) {
                        if (!element.equals(TAG_ARRAYITEM)) {
                            continue;
                        }
                    }
                    Object name = null;
                    if (!parsingArray) {
                        name = parser.getAttributeValue(null, ATTR_NAME);
                    }
                    if (parser.next() == 4) {
                        double value = 0.0d;
                        try {
                            value = Double.valueOf(parser.getText()).doubleValue();
                        } catch (NumberFormatException e) {
                        }
                        if (element.equals(TAG_ITEM)) {
                            sPowerMap.put(name, Double.valueOf(value));
                        } else if (parsingArray) {
                            array.add(Double.valueOf(value));
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (parsingArray) {
                sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
            }
            parser.close();
            int i = 8;
            int[] configResIds = new int[]{R.integer.config_bluetooth_idle_cur_ma, R.integer.config_bluetooth_rx_cur_ma, R.integer.config_bluetooth_tx_cur_ma, R.integer.config_bluetooth_operating_voltage_mv, R.integer.config_wifi_idle_receive_cur_ma, R.integer.config_wifi_active_rx_cur_ma, R.integer.config_wifi_tx_cur_ma, R.integer.config_wifi_operating_voltage_mv};
            String[] configResIdKeys = new String[]{POWER_BLUETOOTH_CONTROLLER_IDLE, POWER_BLUETOOTH_CONTROLLER_RX, POWER_BLUETOOTH_CONTROLLER_TX, POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE, POWER_WIFI_CONTROLLER_IDLE, POWER_WIFI_CONTROLLER_RX, POWER_WIFI_CONTROLLER_TX, POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE};
            int i2 = 0;
            while (true) {
                int length = configResIds.length;
                if (i2 < i) {
                    String key = configResIdKeys[i2];
                    if (sPowerMap.containsKey(key)) {
                        if (((Double) sPowerMap.get(key)).doubleValue() > 0.0d) {
                            i2++;
                        }
                    }
                    int value2 = resources.getInteger(configResIds[i2]);
                    if (value2 > 0) {
                        sPowerMap.put(key, Double.valueOf((double) value2));
                    }
                    i2++;
                } else {
                    return;
                }
            }
        } catch (XmlPullParserException e2) {
            throw new RuntimeException(e2);
        } catch (IOException e3) {
            throw new RuntimeException(e3);
        } catch (Throwable th) {
            parser.close();
        }
    }

    private void initCpuClusters() {
        Object obj = sPowerMap.get(POWER_CPU_CLUSTER_CORE_COUNT);
        if (obj == null || !(obj instanceof Double[])) {
            this.mCpuClusters = new CpuClusterKey[1];
            this.mCpuClusters[0] = new CpuClusterKey(POWER_CPU_ACTIVE, 1, null);
            return;
        }
        Double[] array = (Double[]) obj;
        this.mCpuClusters = new CpuClusterKey[array.length];
        for (int cluster = 0; cluster < array.length; cluster++) {
            this.mCpuClusters[cluster] = new CpuClusterKey(POWER_CPU_CLUSTER_ACTIVE_PREFIX + cluster, (int) Math.round(array[cluster].doubleValue()), null);
        }
    }

    public int getNumCpuClusters() {
        return this.mCpuClusters.length;
    }

    public int getNumCoresInCpuCluster(int index) {
        return this.mCpuClusters[index].numCpus;
    }

    public int getNumSpeedStepsInCpuCluster(int index) {
        Object value = sPowerMap.get(this.mCpuClusters[index].timeKey);
        if (value == null || !(value instanceof Double[])) {
            return 1;
        }
        return ((Double[]) value).length;
    }

    public double getAveragePowerForCpu(int cluster, int step) {
        if (cluster < 0 || cluster >= this.mCpuClusters.length) {
            return 0.0d;
        }
        return getAveragePower(this.mCpuClusters[cluster].powerKey, step);
    }

    public double getAveragePowerOrDefault(String type, double defaultValue) {
        if (!sPowerMap.containsKey(type)) {
            return defaultValue;
        }
        Object data = sPowerMap.get(type);
        if (data instanceof Double[]) {
            return ((Double[]) data)[0].doubleValue();
        }
        return ((Double) sPowerMap.get(type)).doubleValue();
    }

    public double getAveragePower(String type) {
        return getAveragePowerOrDefault(type, 0.0d);
    }

    public double getAveragePower(String type, int level) {
        if (!sPowerMap.containsKey(type)) {
            return 0.0d;
        }
        Object data = sPowerMap.get(type);
        if (!(data instanceof Double[])) {
            return ((Double) data).doubleValue();
        }
        Double[] values = (Double[]) data;
        if (values.length > level && level >= 0) {
            return values[level].doubleValue();
        }
        if (level < 0 || values.length == 0) {
            return 0.0d;
        }
        return values[values.length - 1].doubleValue();
    }

    public double getBatteryCapacity() {
        return getAveragePower(POWER_BATTERY_CAPACITY);
    }
}
