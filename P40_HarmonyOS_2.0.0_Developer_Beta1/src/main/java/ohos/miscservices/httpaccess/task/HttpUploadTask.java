package ohos.miscservices.httpaccess.task;

import ohos.app.Context;
import ohos.miscservices.httpaccess.HttpAccessProxy;
import ohos.miscservices.httpaccess.HttpProbe;
import ohos.miscservices.httpaccess.data.RequestData;

public class HttpUploadTask implements Runnable {
    private HttpAccessProxy httpAccessProxy;
    private HttpProbe httpProbe;
    private RequestData requestData;

    public HttpUploadTask(RequestData requestData2, HttpProbe httpProbe2, Context context) {
        this.httpProbe = httpProbe2;
        this.requestData = requestData2;
        this.httpAccessProxy = new HttpAccessProxy(context);
    }

    @Override // java.lang.Runnable
    public void run() {
        this.httpAccessProxy.httpUrlUpload(this.requestData, this.httpProbe);
    }
}
