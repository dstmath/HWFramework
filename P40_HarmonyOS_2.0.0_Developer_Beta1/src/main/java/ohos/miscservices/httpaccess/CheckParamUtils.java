package ohos.miscservices.httpaccess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.miscservices.httpaccess.data.FormFileData;
import ohos.miscservices.httpaccess.data.RequestData;

public class CheckParamUtils {
    private static final Set<String> FETCH_METHOD = new HashSet(Arrays.asList(HttpConstant.HTTP_METHOD_OPTIONS, HttpConstant.HTTP_METHOD_GET, HttpConstant.HTTP_METHOD_HEAD, HttpConstant.HTTP_METHOD_POST, HttpConstant.HTTP_METHOD_PUT, HttpConstant.HTTP_METHOD_DELETE, HttpConstant.HTTP_METHOD_TRACE));
    private static final int MAX_PATH_LENGTH = 4096;
    private static final Set<String> RESPONSE_TYPE = new HashSet(Arrays.asList("text", "json"));
    private static final Set<String> UPLOAD_METHOD = new HashSet(Arrays.asList(HttpConstant.HTTP_METHOD_POST, HttpConstant.HTTP_METHOD_PUT));

    private static <T> boolean checkNull(T t) {
        return t == null;
    }

    public static boolean checkDownloadRequest(RequestData requestData) {
        return isValidUrl(requestData.getUrl()) && (checkEmptyString(requestData.getData()) && checkEmptyString(requestData.getMethod()) && checkEmptyString(requestData.getResponseType()) && checkNull(requestData.getFiles()) && checkEmptyString(requestData.getToken()));
    }

    public static boolean checkFetchRequest(RequestData requestData) {
        return isValidUrl(requestData.getUrl()) && (checkEmptyString(requestData.getFileName()) && checkEmptyString(requestData.getDescription()) && checkNull(requestData.getFiles()) && checkEmptyString(requestData.getToken())) && (FETCH_METHOD.contains(requestData.getMethod()) || "".equals(requestData.getMethod())) && (RESPONSE_TYPE.contains(requestData.getResponseType()) || "".equals(requestData.getResponseType()));
    }

    public static boolean checkUploadRequest(RequestData requestData) {
        return isValidUrl(requestData.getUrl()) && (checkEmptyString(requestData.getFileName()) && checkEmptyString(requestData.getDescription()) && checkEmptyString(requestData.getResponseType()) && checkEmptyString(requestData.getToken())) && isValidUploadFile(requestData.getFiles()) && (UPLOAD_METHOD.contains(requestData.getMethod()) || "".equals(requestData.getMethod()));
    }

    public static boolean checkOnDownloadCompleteRequest(RequestData requestData) {
        boolean z;
        try {
            if (Long.parseLong(requestData.getToken()) >= 0) {
                z = true;
                return !(!checkEmptyString(requestData.getFileName()) && checkEmptyString(requestData.getDescription()) && checkEmptyString(requestData.getResponseType()) && checkNull(requestData.getFiles()) && checkEmptyString(requestData.getMethod()) && checkEmptyString(requestData.getResponseType()) && checkEmptyString(requestData.getUrl()) && checkEmptyString(requestData.getData())) && z;
            }
        } catch (NumberFormatException unused) {
        }
        z = false;
        if (!(!checkEmptyString(requestData.getFileName()) && checkEmptyString(requestData.getDescription()) && checkEmptyString(requestData.getResponseType()) && checkNull(requestData.getFiles()) && checkEmptyString(requestData.getMethod()) && checkEmptyString(requestData.getResponseType()) && checkEmptyString(requestData.getUrl()) && checkEmptyString(requestData.getData()))) {
        }
    }

    private static boolean isValidUrl(String str) {
        if (str == null) {
            return false;
        }
        return str.startsWith("http") || str.startsWith("https");
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014  */
    private static boolean isValidUploadFile(List<FormFileData> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        for (FormFileData formFileData : list) {
            String uri = formFileData.getUri();
            if (uri.length() <= 0 || uri.length() > 4096) {
                return false;
            }
            while (r3.hasNext()) {
            }
        }
        return true;
    }

    private static boolean checkEmptyString(String str) {
        return "".equals(str);
    }
}
