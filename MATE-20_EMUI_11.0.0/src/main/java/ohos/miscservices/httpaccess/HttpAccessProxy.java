package ohos.miscservices.httpaccess;

import ohos.app.Context;
import ohos.miscservices.httpaccess.data.RequestData;

public class HttpAccessProxy {
    private Context context;

    public HttpAccessProxy(Context context2) {
        this.context = context2;
    }

    public void httpUrlUpload(RequestData requestData, HttpProbe httpProbe) {
        new HttpUploadImpl(this.context).httpUrlUpload(requestData, httpProbe);
    }

    public void httpUrlFetch(RequestData requestData, HttpProbe httpProbe) {
        new HttpFetchImpl(this.context).httpUrlFetch(requestData, httpProbe);
    }
}
