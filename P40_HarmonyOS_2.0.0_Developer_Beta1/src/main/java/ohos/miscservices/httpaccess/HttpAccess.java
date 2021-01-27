package ohos.miscservices.httpaccess;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.data.RequestData;
import ohos.miscservices.httpaccess.data.ResponseData;
import ohos.miscservices.httpaccess.task.HttpDownloadTask;
import ohos.miscservices.httpaccess.task.HttpFetchTask;
import ohos.miscservices.httpaccess.task.HttpUploadTask;
import ohos.miscservices.httpaccess.task.OnDownloadCompleteHandler;

public class HttpAccess {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpAccess");
    private ExecutorService executorService = Executors.newSingleThreadExecutor(new HttpThreadFactory("HttpAccess"));

    public void fetch(RequestData requestData, HttpProbe httpProbe, Context context) {
        if (!CheckParamUtils.checkFetchRequest(requestData)) {
            HiLog.error(TAG, "fetch request param error!", new Object[0]);
            ResponseData responseData = new ResponseData();
            responseData.setCode(202);
            httpProbe.onResponse(responseData);
            return;
        }
        this.executorService.execute(new HttpFetchTask(requestData, httpProbe, context));
    }

    public void upload(RequestData requestData, HttpProbe httpProbe, Context context) {
        if (!CheckParamUtils.checkUploadRequest(requestData)) {
            HiLog.error(TAG, "upload request param error!", new Object[0]);
            ResponseData responseData = new ResponseData();
            responseData.setCode(202);
            httpProbe.onResponse(responseData);
            return;
        }
        this.executorService.execute(new HttpUploadTask(requestData, httpProbe, context));
    }

    public void download(RequestData requestData, HttpProbe httpProbe, Context context) {
        if (!CheckParamUtils.checkDownloadRequest(requestData)) {
            HiLog.error(TAG, "download request param error!", new Object[0]);
            ResponseData responseData = new ResponseData();
            responseData.setCode(202);
            httpProbe.onResponse(responseData);
            return;
        }
        new HttpDownloadTask(requestData, httpProbe, context).start();
    }

    public void onDownloadComplete(RequestData requestData, HttpProbe httpProbe, Context context) {
        if (!CheckParamUtils.checkOnDownloadCompleteRequest(requestData)) {
            HiLog.error(TAG, "onDownloadComplete request param error!", new Object[0]);
            ResponseData responseData = new ResponseData();
            responseData.setCode(202);
            httpProbe.onResponse(responseData);
            return;
        }
        new OnDownloadCompleteHandler(requestData, httpProbe, context).handle();
    }
}
