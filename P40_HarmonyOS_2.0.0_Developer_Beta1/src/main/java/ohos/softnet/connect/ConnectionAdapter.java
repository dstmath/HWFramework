package ohos.softnet.connect;

import com.huawei.softnet.HwConnection;
import com.huawei.softnet.connect.ConnectOption;
import com.huawei.softnet.connect.DevConfig;
import com.huawei.softnet.connect.DiscoveryOption;
import com.huawei.softnet.connect.PowerPolicy;
import com.huawei.softnet.connect.PublishOption;
import com.huawei.softnet.connect.ServiceDesc;
import com.huawei.softnet.connect.ServiceFilter;
import com.huawei.softnet.connect.Strategy;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;
import ohos.utils.zson.ZSONObject;

public class ConnectionAdapter {
    private static final int RESULT_CODE_SUCCESS = 0;
    private static final String TAG = "ConnectionAdapter";
    private HwConnection hwConnection = null;

    public ConnectionAdapter(Context context, String str) {
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            this.hwConnection = HwConnection.getInstance((android.content.Context) hostContext, str);
        } else {
            LogUtils.error(TAG, TAG, "%s Context is invalid", str);
        }
    }

    public boolean startDiscovery(String str, DiscoveryOption discoveryOption, DiscoveryCallback discoveryCallback) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "startDiscovery", "HwConnection not init yet", new Object[0]);
            return false;
        }
        Strategy strategy = ConnectDataUtils.getStrategy(discoveryOption.getStrategy());
        if (strategy == null) {
            LogUtils.error(TAG, "startDiscovery", "Strategy is invalid", new Object[0]);
            return false;
        } else if (strategy != Strategy.COAP) {
            LogUtils.error(TAG, "startDiscovery", "Do not support other strategy except COAP, abort this request", new Object[0]);
            return false;
        } else {
            List<ServiceDesc> serviceDesc = ConnectDataUtils.getServiceDesc(discoveryOption.getInfos());
            List<ServiceFilter> serviceFilter = ConnectDataUtils.getServiceFilter(discoveryOption.getServiceFilters());
            if (serviceFilter.isEmpty()) {
                LogUtils.error(TAG, "startDiscovery", "ServiceFilter is invalid", new Object[0]);
                return false;
            }
            PowerPolicy powerPolicy = ConnectDataUtils.getPowerPolicy(discoveryOption.getPowerPolicy());
            int discoveryMode = discoveryOption.getDiscoveryMode();
            if (discoveryMode == 1 || discoveryMode == 2) {
                String extInfo = discoveryOption.getExtInfo();
                if (extInfo == null) {
                    LogUtils.error(TAG, "startDiscovery", "Obligatory extInfo is null", new Object[0]);
                    return false;
                }
                int startDiscovery = this.hwConnection.startDiscovery(str, new DiscoveryOption.Builder().strategy(strategy).infos(serviceDesc).serviceFilters(serviceFilter).timeout(discoveryOption.getTimeout()).count(discoveryOption.getCount()).powerPolicy(powerPolicy).discoveryMode(discoveryMode).extInfo(extInfo).build(), ConnectDataUtils.getDiscoveryCallback(discoveryCallback));
                if (startDiscovery != 0) {
                    LogUtils.error(TAG, "startDiscovery", "start discovery failed, error = %d", Integer.valueOf(startDiscovery));
                    return false;
                }
                LogUtils.info(TAG, "startDiscovery", "start discovery success", new Object[0]);
                return true;
            }
            LogUtils.error(TAG, "startDiscovery", "Discovery mode=%d is invalid", Integer.valueOf(discoveryMode));
            return false;
        }
    }

    public boolean stopDiscovery(String str, int i) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "stopDiscovery", "HwConnection not init yet", new Object[0]);
            return false;
        } else if (i == 1 || i == 2) {
            int stopDiscovery = this.hwConnection.stopDiscovery(str, i);
            if (stopDiscovery != 0) {
                LogUtils.error(TAG, "stopDiscovery", "stop discovery failed, error = %d", Integer.valueOf(stopDiscovery));
                return false;
            }
            LogUtils.info(TAG, "stopDiscovery", "stop discovery success", new Object[0]);
            return true;
        } else {
            LogUtils.error(TAG, "startDiscovery", "Discovery mode=%d is invalid", Integer.valueOf(i));
            return false;
        }
    }

    public boolean publishService(String str, PublishOption publishOption, ConnectionCallback connectionCallback) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "publishService", "HwConnection not init yet", new Object[0]);
            return false;
        }
        Strategy strategy = ConnectDataUtils.getStrategy(publishOption.getStrategy());
        if (strategy == null) {
            LogUtils.error(TAG, "publishService", "Strategy is invalid", new Object[0]);
            return false;
        } else if (strategy != Strategy.COAP) {
            LogUtils.error(TAG, "startDiscovery", "Do not support other strategy except COAP, abort this request", new Object[0]);
            return false;
        } else {
            List<ServiceDesc> serviceDesc = ConnectDataUtils.getServiceDesc(publishOption.getInfos());
            List<ServiceFilter> serviceFilter = ConnectDataUtils.getServiceFilter(publishOption.getServiceFilters());
            if (serviceFilter.isEmpty()) {
                LogUtils.error(TAG, "publishService", "ServiceFilter is invalid", new Object[0]);
                return false;
            }
            PowerPolicy powerPolicy = ConnectDataUtils.getPowerPolicy(publishOption.getPowerPolicy());
            int publishMode = publishOption.getPublishMode();
            if (publishMode == 1 || publishMode == 2 || publishMode == 3) {
                int publishService = this.hwConnection.publishService(str, new PublishOption.Builder().strategy(strategy).infos(serviceDesc).serviceFilters(serviceFilter).timeout(publishOption.getTimeout()).count(publishOption.getCount()).powerPolicy(powerPolicy).publishMode(publishOption.getPublishMode()).extInfo(publishOption.getExtInfo()).build(), ConnectDataUtils.getConnectionCallback(connectionCallback));
                if (publishService != 0) {
                    LogUtils.error(TAG, "publishService", "publish service failed, error = %d", Integer.valueOf(publishService));
                    return false;
                }
                LogUtils.info(TAG, "publishService", "publish service success", new Object[0]);
                return true;
            }
            LogUtils.error(TAG, "publishService", "publishService mode=%d is invalid", Integer.valueOf(publishMode));
            return false;
        }
    }

    public boolean unPublishService(String str, int i) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "unPublishService", "HwConnection not init yet", new Object[0]);
            return false;
        } else if (i == 1 || i == 2 || i == 3) {
            int unPublishService = this.hwConnection.unPublishService(str, i);
            if (unPublishService != 0) {
                LogUtils.error(TAG, "unPublishService", "unpublish service failed, error = %d", Integer.valueOf(unPublishService));
                return false;
            }
            LogUtils.info(TAG, "unPublishService", "unpublish service success", new Object[0]);
            return true;
        } else {
            LogUtils.error(TAG, "publishService", "publishService mode=%d is invalid", Integer.valueOf(i));
            return false;
        }
    }

    public boolean setConfig(DevConfig devConfig) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "setConfig", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int config = this.hwConnection.setConfig(new DevConfig.Builder().netRole(ConnectDataUtils.getNetRole(devConfig.getNetRole())).build());
        if (config != 0) {
            LogUtils.error(TAG, "setConfig", "set config failed, error = %d", Integer.valueOf(config));
            return false;
        }
        LogUtils.info(TAG, "setConfig", "set config success", new Object[0]);
        return true;
    }

    private String checkValidExInfo(String str) {
        if (str == null) {
            LogUtils.error(TAG, "checkValidExInfo", "extInfo is null", new Object[0]);
            return null;
        }
        LogUtils.info(TAG, "checkValidExInfo", "extInfo before is %s", str);
        Map map = (Map) ZSONObject.stringToClass(str, Map.class);
        if (map == null) {
            LogUtils.error(TAG, "checkValidExInfo", "extInfo to zson error", new Object[0]);
            return null;
        } else if (map.get("authParams") == null) {
            LogUtils.error(TAG, "checkValidExInfo", "authParams is null", new Object[0]);
            return null;
        } else {
            Object obj = map.get("extInfo");
            if (!(obj instanceof String)) {
                LogUtils.error(TAG, "checkValidExtInfo", "inner extInfo is not a String type", new Object[0]);
                return null;
            }
            Map map2 = (Map) ZSONObject.stringToClass((String) obj, Map.class);
            if (map2 == null) {
                LogUtils.error(TAG, "checkValidExInfo", "inner extInfo to zson error", new Object[0]);
                return null;
            } else if (map2.get("encrypt") == null) {
                LogUtils.error(TAG, "checkValidExInfo", "encrypt is null", new Object[0]);
                return null;
            } else if (map.get(Attribute.PhoneFinder.TYPE) == null) {
                LogUtils.error(TAG, "checkValidExInfo", "type is null", new Object[0]);
                return null;
            } else {
                map2.put("bindServiceUnwanted", 1);
                map.put("extInfo", ZSONObject.toZSONString(map2));
                return ZSONObject.toZSONString(map);
            }
        }
    }

    public boolean connectDevice(String str, String str2, String str3, ConnectOption connectOption, ConnectionCallback connectionCallback) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "connectDevice", "HwConnection not init yet", new Object[0]);
            return false;
        }
        Strategy strategy = ConnectDataUtils.getStrategy(connectOption.getStrategy());
        if (strategy == null) {
            LogUtils.error(TAG, "connectDevice", "ConnectOption is invalid, Strategy is null", new Object[0]);
            return false;
        } else if (strategy != Strategy.COAP) {
            LogUtils.error(TAG, "startDiscovery", "Do not support other strategy except COAP, abort this request", new Object[0]);
            return false;
        } else {
            String checkValidExInfo = checkValidExInfo(connectOption.getExtInfo());
            if (checkValidExInfo == null) {
                LogUtils.error(TAG, "connectDevice", "extInfo is invalid", new Object[0]);
                return false;
            }
            LogUtils.info(TAG, "connectDevice", "extInfo is %s", checkValidExInfo);
            int connectDevice = this.hwConnection.connectDevice(str, str2, str3, new ConnectOption.Builder().serviceId(connectOption.getServiceId()).extInfo(checkValidExInfo).strategy(strategy).opt(connectOption.getOption()).build(), ConnectDataUtils.getConnectionCallback(connectionCallback));
            if (connectDevice != 0) {
                LogUtils.error(TAG, "connectDevice", "connect device failed, error = %d", Integer.valueOf(connectDevice));
                return false;
            }
            LogUtils.info(TAG, "connectDevice", "connect device success", new Object[0]);
            return true;
        }
    }

    public boolean disconnectDevice(String str, String str2, String str3) {
        HwConnection hwConnection2 = this.hwConnection;
        if (hwConnection2 == null) {
            LogUtils.error(TAG, "disconnectDevice", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int disconnectDevice = hwConnection2.disconnectDevice(str, str2, str3);
        if (disconnectDevice != 0) {
            LogUtils.error(TAG, "disconnectDevice", "disconnect device failed, error = %d", Integer.valueOf(disconnectDevice));
            return false;
        }
        LogUtils.info(TAG, "disconnectDevice", "disconnect device success", new Object[0]);
        return true;
    }

    public boolean acceptConnect(String str, String str2, String str3, DataCallback dataCallback) {
        HwConnection hwConnection2 = this.hwConnection;
        if (hwConnection2 == null) {
            LogUtils.error(TAG, "acceptConnect", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int acceptConnect = hwConnection2.acceptConnect(str, str2, str3, ConnectDataUtils.getDataCallback(dataCallback));
        if (acceptConnect != 0) {
            LogUtils.error(TAG, "acceptConnect", "accept connect device failed, error = %d", Integer.valueOf(acceptConnect));
            return false;
        }
        LogUtils.info(TAG, "acceptConnect", "accept connect device success", new Object[0]);
        return true;
    }

    public boolean rejectConnect(String str, String str2, String str3) {
        HwConnection hwConnection2 = this.hwConnection;
        if (hwConnection2 == null) {
            LogUtils.error(TAG, "rejectConnect", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int rejectConnect = hwConnection2.rejectConnect(str, str2, str3);
        if (rejectConnect != 0) {
            LogUtils.error(TAG, "rejectConnect", "rejectconnect device failed, error = %d", Integer.valueOf(rejectConnect));
            return false;
        }
        LogUtils.info(TAG, "rejectConnect", "rejectconnect device success", new Object[0]);
        return true;
    }

    public boolean sendByte(String str, String str2, String str3, byte[] bArr, int i, String str4) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "sendByte", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int sendByte = this.hwConnection.sendByte(str, str2, str3, bArr, i, (str4 == null || str4.length() <= 0) ? "unused-param" : str4);
        if (sendByte != 0) {
            LogUtils.error(TAG, "sendByte", "send byte failed, error = %d", Integer.valueOf(sendByte));
            return false;
        }
        LogUtils.info(TAG, "sendByte", "send byte success", new Object[0]);
        return true;
    }

    public boolean sendBlock(String str, String str2, String str3, byte[] bArr, int i, String str4) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "sendBlock", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int sendBlock = this.hwConnection.sendBlock(str, str2, str3, bArr, i, (str4 == null || str4.length() <= 0) ? "unused-param" : str4);
        if (sendBlock != 0) {
            LogUtils.error(TAG, "sendBlock", "send block failed, error = %d", Integer.valueOf(sendBlock));
            return false;
        }
        LogUtils.info(TAG, "sendBlock", "send block success", new Object[0]);
        return true;
    }

    public boolean sendFile(String str, String str2, String str3, String str4, String str5, String str6) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "sendFile", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int sendFile = this.hwConnection.sendFile(str, str2, str3, str4, str5, (str6 == null || str6.length() <= 0) ? "unused-param" : str6);
        if (sendFile != 0) {
            LogUtils.error(TAG, "sendFile", "send file failed, error = %d", Integer.valueOf(sendFile));
            return false;
        }
        LogUtils.info(TAG, "sendFile", "send file success", new Object[0]);
        return true;
    }

    public boolean sendStream(String str, String str2, String str3, DataPayload dataPayload, String str4) {
        if (this.hwConnection == null) {
            LogUtils.error(TAG, "sendStream", "HwConnection not init yet", new Object[0]);
            return false;
        }
        if (str4 == null || str4.length() <= 0) {
            str4 = "unused-param";
        }
        int sendStream = this.hwConnection.sendStream(str, str2, str3, ConnectDataUtils.getDataPayload(dataPayload), str4);
        if (sendStream != 0) {
            LogUtils.error(TAG, "sendStream", "send stream failed, error = %d", Integer.valueOf(sendStream));
            return false;
        }
        LogUtils.info(TAG, "sendStream", "send stream success", new Object[0]);
        return true;
    }

    public boolean disconnectAll() {
        HwConnection hwConnection2 = this.hwConnection;
        if (hwConnection2 == null) {
            LogUtils.error(TAG, "disconnectAll", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int disconnectAll = hwConnection2.disconnectAll();
        if (disconnectAll != 0) {
            LogUtils.error(TAG, "disconnectAll", "disconnect all failed, error = %d", Integer.valueOf(disconnectAll));
            return false;
        }
        LogUtils.info(TAG, "disconnectAll", "disconnect all success", new Object[0]);
        return true;
    }

    public boolean destroy() {
        HwConnection hwConnection2 = this.hwConnection;
        if (hwConnection2 == null) {
            LogUtils.error(TAG, "destroy", "HwConnection not init yet", new Object[0]);
            return false;
        }
        int destroy = hwConnection2.destroy();
        if (destroy != 0) {
            LogUtils.error(TAG, "destroy", "destroy failed, error = %d", Integer.valueOf(destroy));
            return false;
        }
        LogUtils.info(TAG, "destroy", "destroy success", new Object[0]);
        return true;
    }
}
