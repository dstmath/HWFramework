package ohos.miscservices.httpaccess.task;

import java.util.Map;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.download.DownloadConfig;
import ohos.miscservices.download.DownloadSession;
import ohos.miscservices.httpaccess.HttpProbe;
import ohos.miscservices.httpaccess.data.RequestData;
import ohos.miscservices.httpaccess.data.ResponseData;
import ohos.utils.net.Uri;

public class HttpDownloadTask {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpDownloadTask");
    private Context context;
    private HttpProbe httpProbe;
    private RequestData requestData;

    public HttpDownloadTask(RequestData requestData2, HttpProbe httpProbe2, Context context2) {
        this.httpProbe = httpProbe2;
        this.requestData = requestData2;
        this.context = context2;
    }

    public void start() {
        int lastIndexOf;
        HiLog.debug(TAG, "start download!", new Object[0]);
        String url = this.requestData.getUrl();
        Uri parse = Uri.parse(url);
        String fileName = this.requestData.getFileName();
        if ("".equals(fileName) && (lastIndexOf = url.lastIndexOf("/")) > 0) {
            fileName = url.substring(lastIndexOf + 1);
        }
        String description = this.requestData.getDescription();
        if ("".equals(description)) {
            description = fileName;
        }
        DownloadConfig.Builder description2 = new DownloadConfig.Builder(this.context, parse).setPath(null, fileName).setDescription(description);
        if (this.requestData.getHeader() != null && !this.requestData.getHeader().isEmpty()) {
            for (Map.Entry<String, String> entry : this.requestData.getHeader().entrySet()) {
                description2.addHttpheader(entry.getKey(), entry.getValue());
            }
        }
        long start = new DownloadSession(this.context, description2.build()).start();
        if (start > 0) {
            ResponseData responseData = new ResponseData();
            responseData.setCode(8);
            responseData.setToken(start);
            this.httpProbe.onResponse(responseData);
        }
    }
}
