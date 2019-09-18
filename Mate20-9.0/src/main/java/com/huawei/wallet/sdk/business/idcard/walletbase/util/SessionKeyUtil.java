package com.huawei.wallet.sdk.business.idcard.walletbase.util;

import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionKeyUtil {
    private static final Map<String, String> needCheckCmdMap = new HashMap();

    static {
        needCheckCmdMap.put("nfc.get.list.card", "cplc");
        needCheckCmdMap.put("nfc.set.card", "cplc");
        needCheckCmdMap.put("device.claimmiss", "deviceID");
        needCheckCmdMap.put("retry.device.claimmiss", "deviceID");
        needCheckCmdMap.put("nfc.report.terminal", "cplc");
        needCheckCmdMap.put("nfc.delete.card", "cplc");
        needCheckCmdMap.put("wipe.device", "cplc");
        needCheckCmdMap.put("post.event.cardenroll", "cplc");
        needCheckCmdMap.put("post.events.cardswing", "cplc");
        needCheckCmdMap.put("delete.app", "cplc");
        needCheckCmdMap.put("get.apdu", "cplc");
        needCheckCmdMap.put("refund", "cplc");
        needCheckCmdMap.put("post.event.cardmove", "newCplc");
        needCheckCmdMap.put("post.event.recharge", "cplc");
        needCheckCmdMap.put("cardmove.report.cardnumber", "cplc");
    }

    public static boolean needCheckSign(String cmd) {
        return needCheckCmdMap.containsKey(cmd);
    }

    public static String buildSignData(String data) {
        Map<String, Object> requestParamMaps = (Map) CommonUtil.fromJson(data, Map.class);
        if (CommonUtil.isNull((Map<?, ?>) requestParamMaps)) {
            return "";
        }
        List<String> requestParamKeys = new ArrayList<>(requestParamMaps.keySet());
        Collections.sort(requestParamKeys);
        NumberFormat fomate = NumberFormat.getNumberInstance();
        fomate.setGroupingUsed(false);
        StringBuilder buff = new StringBuilder();
        for (String requestParam : requestParamKeys) {
            Object requestValues = requestParamMaps.get(requestParam);
            if (!"sessionKeySign".equals(requestParam)) {
                if (requestValues instanceof Map) {
                    Map<String, Object> materialMaps = (Map) requestValues;
                    if (!CommonUtil.isNull((Map<?, ?>) materialMaps)) {
                        buff.append(requestParam);
                        buff.append("=");
                        buff.append(buildSimpleMapSignData(materialMaps, fomate));
                        buff.append(SNBConstant.FILTER);
                    }
                } else if (requestValues instanceof List) {
                    List<?> list = (List) requestValues;
                    if (!CommonUtil.isNull(list)) {
                        buff.append(requestParam);
                        buff.append("=");
                        buff.append(buildSimpleListSignData(list, fomate));
                        buff.append(SNBConstant.FILTER);
                    }
                } else if (requestValues != null) {
                    buff.append(requestParam);
                    buff.append("=");
                    if (requestValues instanceof Double) {
                        buff.append(fomate.format(requestValues));
                    } else {
                        buff.append(requestValues);
                    }
                    buff.append(SNBConstant.FILTER);
                }
            }
        }
        buff.deleteCharAt(buff.lastIndexOf(SNBConstant.FILTER));
        return buff.toString();
    }

    private static String buildSimpleMapSignData(Map<String, Object> params, NumberFormat fomat) {
        StringBuilder buff = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            Object value = params.get(key);
            if (!"sessionKeySign".equals(key)) {
                if (value instanceof Map) {
                    Map<String, Object> materialMaps = (Map) value;
                    if (!CommonUtil.isNull((Map<?, ?>) materialMaps)) {
                        buff.append(key);
                        buff.append("=");
                        buff.append(buildSimpleMapSignData(materialMaps, fomat));
                        buff.append(SNBConstant.FILTER);
                    }
                } else if (value != null) {
                    buff.append(key);
                    buff.append("=");
                    if (value instanceof Double) {
                        buff.append(fomat.format(value));
                    } else {
                        buff.append(value);
                    }
                    buff.append(SNBConstant.FILTER);
                }
            }
        }
        buff.deleteCharAt(buff.lastIndexOf(SNBConstant.FILTER));
        return buff.toString();
    }

    private static String buildSimpleListSignData(List<?> list, NumberFormat format) {
        StringBuilder buff = new StringBuilder();
        for (Object next : list) {
            if (next instanceof Map) {
                buff.append(buildSimpleMapSignData((Map) next, format));
                buff.append(",");
            }
        }
        buff.deleteCharAt(buff.lastIndexOf(","));
        return buff.toString();
    }
}
