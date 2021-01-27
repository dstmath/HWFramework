package ohos.bundle;

import java.io.IOException;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class LauncherUtil {
    private Context context;

    public LauncherUtil(Context context2) {
        this.context = context2;
    }

    public ResourceManager getResourceManager(String str) {
        if (str == null || str.isEmpty()) {
            AppLog.e("getResourceManager bundleName is null", new Object[0]);
            return null;
        }
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("getResourceManager context is null", new Object[0]);
            return null;
        }
        Context createBundleContext = context2.createBundleContext(str, 2);
        if (createBundleContext == null) {
            AppLog.e("remote context is null", new Object[0]);
            return null;
        }
        ResourceManager resourceManager = createBundleContext.getResourceManager();
        if (resourceManager != null) {
            return resourceManager;
        }
        AppLog.e("remoteResourceManager is null", new Object[0]);
        return null;
    }

    public Element createPixelMapDrawable(Resource resource) {
        if (resource == null) {
            AppLog.d("createPixelMapDrawable resource is null", new Object[0]);
            return null;
        }
        AppLog.d("createPixelMapDrawable by source start", new Object[0]);
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        ImageSource create = ImageSource.create(resource, sourceOptions);
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        if (create == null) {
            AppLog.e("imageSource is null", new Object[0]);
            return null;
        }
        PixelMap createPixelmap = create.createPixelmap(decodingOptions);
        if (createPixelmap == null) {
            AppLog.e("createPixelMapDrawable pixelMap by resource is null", new Object[0]);
            return null;
        }
        AppLog.i("createPixelMapDrawable by resource success", new Object[0]);
        return new PixelMapElement(createPixelmap);
    }

    public void closeStream(Resource resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException unused) {
                AppLog.e("closeStream io close exception", new Object[0]);
            }
        }
    }
}
