package ohos.ace.featureabilityplugin.requestparse;

import com.huawei.ace.runtime.ALog;
import java.util.ArrayList;
import java.util.List;
import ohos.bundle.ProfileConstants;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;

public class RequestParse {
    private static final int REMOTE_ABILITY = 0;
    private static final int SYNC_REQ = 0;
    private static final String TAG = RequestParse.class.getSimpleName();
    private static RequestParse instance = new RequestParse();

    public static RequestParse getInstance() {
        return instance;
    }

    public boolean checkAndParseRequest(List<Object> list, ParsedJsRequest parsedJsRequest, int i) {
        boolean z;
        if (parsedJsRequest == null) {
            ALog.e(TAG, "parsed Js request is null");
            return false;
        }
        parsedJsRequest.setParseErrorMessage("Js parse default error");
        if (list == null) {
            setAndLogParseError(parsedJsRequest, "Js request is null");
            return false;
        } else if (list.size() != 1) {
            setAndLogParseError(parsedJsRequest, "Incorrect number of parameters:" + list.size());
            return false;
        } else {
            Object obj = list.get(0);
            if (!(obj instanceof String)) {
                setAndLogParseError(parsedJsRequest, "Js request is not json string");
                return false;
            }
            switch (i) {
                case 1:
                    z = parseNormalAbilityRequest((String) obj, parsedJsRequest, true);
                    break;
                case 2:
                case 3:
                    z = parseNormalAbilityRequest((String) obj, parsedJsRequest, false);
                    break;
                case 4:
                    z = parseQueryAbilityRequest((String) obj, parsedJsRequest);
                    break;
                case 5:
                case 6:
                    z = parseStartAbilityRequest((String) obj, parsedJsRequest);
                    break;
                case 7:
                    z = parseFinishAbilityWithResultRequest((String) obj, parsedJsRequest);
                    break;
                default:
                    ALog.e(TAG, "requestType not support");
                    return false;
            }
            if (z) {
                return true;
            }
            ALog.e(TAG, "parse json request failed");
            return false;
        }
    }

    private boolean parseNormalAbilityRequest(String str, ParsedJsRequest parsedJsRequest, boolean z) {
        try {
            JSONObject parseObject = JSONObject.parseObject(str);
            if (parseObject == null) {
                setAndLogParseError(parsedJsRequest, "parse json request failed");
                return false;
            }
            Object obj = parseObject.get(ProfileConstants.BUNDLE_NAME);
            Object obj2 = parseObject.get("abilityName");
            Object obj3 = parseObject.get("messageCode");
            Object obj4 = parseObject.get("abilityType");
            if (obj instanceof String) {
                parsedJsRequest.setBundleName((String) obj);
            }
            if (obj4 instanceof Integer) {
                parsedJsRequest.setAbilityType(((Integer) obj4).intValue());
            }
            if (!(obj2 instanceof String)) {
                setAndLogParseError(parsedJsRequest, "abilityName is not String type");
                return false;
            } else if (!(obj3 instanceof Integer)) {
                setAndLogParseError(parsedJsRequest, "messageCode is absent or not Integer type");
                return false;
            } else {
                parsedJsRequest.setAbilityName((String) obj2);
                if (((Integer) obj4).intValue() == 0 && ((String) obj) != null && !"".equals((String) obj) && !((String) obj2).contains(".")) {
                    parsedJsRequest.setAbilityName(((String) obj) + "." + ((String) obj2));
                }
                parsedJsRequest.setMessageCode(((Integer) obj3).intValue());
                if (!parseJsSyncOption(parseObject, parsedJsRequest)) {
                    ALog.e(TAG, "parse js sync option failed");
                    return false;
                } else if (parseJsData(parseObject, parsedJsRequest, z)) {
                    return true;
                } else {
                    ALog.e(TAG, "parse js data failed");
                    return false;
                }
            }
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "normal ability parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseJsSyncOption(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (jSONObject.containsKey("syncOption")) {
                Object obj = jSONObject.get("syncOption");
                if (!(obj instanceof Integer)) {
                    setAndLogParseError(parsedJsRequest, "syncOption is not Integer type");
                    return false;
                }
                parsedJsRequest.setSyncOption(((Integer) obj).intValue());
                return true;
            }
            parsedJsRequest.setSyncOption(0);
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "js sync parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseJsData(JSONObject jSONObject, ParsedJsRequest parsedJsRequest, boolean z) {
        if (z) {
            try {
                if (jSONObject.containsKey("data")) {
                    Object obj = jSONObject.get("data");
                    if (!(obj instanceof JSON)) {
                        setAndLogParseError(parsedJsRequest, "data is not JSON type");
                        return false;
                    }
                    parsedJsRequest.setRequestData(JSON.toJSONString(obj));
                    return true;
                }
            } catch (JSONException e) {
                setAndLogParseError(parsedJsRequest, "js data parse exception: " + e.getLocalizedMessage());
                return false;
            }
        }
        parsedJsRequest.setRequestData(null);
        return true;
    }

    private void setAndLogParseError(ParsedJsRequest parsedJsRequest, String str) {
        ALog.e(TAG, str);
        parsedJsRequest.setParseErrorMessage(str);
    }

    private boolean parseQueryAbilityRequest(String str, ParsedJsRequest parsedJsRequest) {
        try {
            JSONObject parseObject = JSONObject.parseObject(str);
            if (parseObject == null) {
                setAndLogParseError(parsedJsRequest, "query ability parse json request failed");
                return false;
            } else if (parseDistributeCommonFields(parseObject, parsedJsRequest)) {
                return true;
            } else {
                ALog.e(TAG, "query ability parse distribute common fields failed");
                return false;
            }
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "query ability parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseStartAbilityRequest(String str, ParsedJsRequest parsedJsRequest) {
        try {
            JSONObject parseObject = JSONObject.parseObject(str);
            if (parseObject == null) {
                setAndLogParseError(parsedJsRequest, "start ability parse json request failed");
                return false;
            } else if (!parseDistributeCommonFields(parseObject, parsedJsRequest)) {
                ALog.e(TAG, "start ability parse distribute common fields failed");
                return false;
            } else if (!parseDeviceId(parseObject, parsedJsRequest)) {
                ALog.e(TAG, "start ability parse device Id failed");
                return false;
            } else if (!parseDeviceType(parseObject, parsedJsRequest)) {
                ALog.e(TAG, "start ability parse device type failed");
                return false;
            } else if (!parseFlag(parseObject, parsedJsRequest)) {
                ALog.e(TAG, "start ability parse flag failed");
                return false;
            } else if (parseJsData(parseObject, parsedJsRequest, true)) {
                return true;
            } else {
                ALog.e(TAG, "parse js data failed");
                return false;
            }
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "start ability parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseFinishAbilityWithResultRequest(String str, ParsedJsRequest parsedJsRequest) {
        try {
            JSONObject parseObject = JSONObject.parseObject(str);
            if (parseObject == null) {
                setAndLogParseError(parsedJsRequest, "finish ability parse json request failed");
                return false;
            }
            Object obj = parseObject.get("code");
            if (!(obj instanceof Integer)) {
                setAndLogParseError(parsedJsRequest, "code is absent or not Integer type");
                return false;
            }
            parsedJsRequest.setFinishAbilityResultCode(((Integer) obj).intValue());
            if (parseObject.containsKey("result")) {
                Object obj2 = parseObject.get("result");
                if (!(obj2 instanceof JSON)) {
                    setAndLogParseError(parsedJsRequest, "result is not String type");
                    return false;
                }
                parsedJsRequest.setFinishAbilityResultData(JSON.toJSONString(obj2));
                return true;
            }
            parsedJsRequest.setFinishAbilityResultData(null);
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "finish ability parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseDistributeCommonFields(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (jSONObject.containsKey(ProfileConstants.BUNDLE_NAME) && jSONObject.containsKey("abilityName")) {
                Object obj = jSONObject.get(ProfileConstants.BUNDLE_NAME);
                Object obj2 = jSONObject.get("abilityName");
                if (!(obj instanceof String)) {
                    setAndLogParseError(parsedJsRequest, "bundleName is not String type");
                    return false;
                } else if (!(obj2 instanceof String)) {
                    setAndLogParseError(parsedJsRequest, "abilityName is not String type");
                    return false;
                } else {
                    parsedJsRequest.setBundleName((String) obj);
                    parsedJsRequest.setAbilityName((String) obj2);
                    parsedJsRequest.setIntentType(true);
                }
            } else if (jSONObject.containsKey("action")) {
                Object obj3 = jSONObject.get("action");
                if (!(obj3 instanceof String)) {
                    setAndLogParseError(parsedJsRequest, "action is not String type");
                    return false;
                } else if (!parseEntities(jSONObject, parsedJsRequest)) {
                    ALog.e(TAG, "parse entities failed");
                    return false;
                } else {
                    parsedJsRequest.setAction((String) obj3);
                    parsedJsRequest.setIntentType(false);
                }
            } else {
                setAndLogParseError(parsedJsRequest, "neither explicit intent nor implicit intent");
                return false;
            }
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "distribute common fields parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseEntities(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (jSONObject.containsKey(Constants.DOM_ENTITIES)) {
                Object obj = jSONObject.get(Constants.DOM_ENTITIES);
                if (!(obj instanceof List)) {
                    setAndLogParseError(parsedJsRequest, "entities is not List type");
                    return false;
                }
                ArrayList arrayList = new ArrayList();
                for (Object obj2 : (List) obj) {
                    arrayList.add((String) String.class.cast(obj2));
                }
                parsedJsRequest.setEntities(arrayList);
                return true;
            }
            parsedJsRequest.setEntities(null);
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "entities parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseDeviceId(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (jSONObject.containsKey("deviceId")) {
                Object obj = jSONObject.get("deviceId");
                if (!(obj instanceof String)) {
                    setAndLogParseError(parsedJsRequest, "device Id is not String type");
                    return false;
                }
                parsedJsRequest.setDeviceId((String) obj);
                return true;
            }
            parsedJsRequest.setDeviceId("");
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "device id parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseFlag(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (!jSONObject.containsKey("flag")) {
                return true;
            }
            Object obj = jSONObject.get("flag");
            if (!(obj instanceof Integer)) {
                setAndLogParseError(parsedJsRequest, "flag is not int type");
                return false;
            }
            parsedJsRequest.setFlag(((Integer) obj).intValue());
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "flag id parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean parseDeviceType(JSONObject jSONObject, ParsedJsRequest parsedJsRequest) {
        try {
            if (jSONObject.containsKey("deviceType")) {
                Object obj = jSONObject.get("deviceType");
                if (!(obj instanceof Integer)) {
                    setAndLogParseError(parsedJsRequest, "device Id is not Integer type");
                    return false;
                }
                parsedJsRequest.setStartAbilityDeviceType(((Integer) obj).intValue());
                return true;
            }
            parsedJsRequest.setStartAbilityDeviceType(0);
            return true;
        } catch (JSONException e) {
            setAndLogParseError(parsedJsRequest, "device type parse exception: " + e.getLocalizedMessage());
            return false;
        }
    }
}
