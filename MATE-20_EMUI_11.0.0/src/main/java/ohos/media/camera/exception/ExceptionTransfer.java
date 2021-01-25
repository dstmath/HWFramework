package ohos.media.camera.exception;

import android.os.RemoteException;
import android.os.ServiceSpecificException;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ExceptionTransfer {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ExceptionTransfer.class);

    public static void trans2AccessException(Throwable th) throws AccessException {
        if (th instanceof ServiceSpecificException) {
            ServiceSpecificException serviceSpecificException = (ServiceSpecificException) th;
            LOGGER.error("Camera service throws exception, error code: %{public}d", Integer.valueOf(serviceSpecificException.errorCode));
            int i = serviceSpecificException.errorCode;
            if (i == 2) {
                LOGGER.error("Camera service returns device already exists, this should not happen", new Object[0]);
                throw new IllegalArgumentException("Camera device already exists");
            } else if (i != 3) {
                int i2 = -5;
                if (i != 4) {
                    if (i == 6) {
                        i2 = -4;
                    } else if (i == 7) {
                        i2 = -2;
                    } else if (i == 8) {
                        i2 = -3;
                    } else if (i != 10) {
                        LOGGER.error("Camera service unknown error code, this should not happen", new Object[0]);
                        i2 = -1;
                    } else {
                        LOGGER.error("Camera service returns invalid operation, this should not happen", new Object[0]);
                    }
                }
                throw new AccessException(i2);
            } else {
                LOGGER.error("Camera service returns illegal argument, this should not happen", new Object[0]);
                throw new IllegalArgumentException("Camera device argument error");
            }
        } else if (th instanceof RemoteException) {
            LOGGER.error("Camera service throws remote exception, this should not happen, %{public}s", th.getMessage());
            throw new AccessException(-6);
        } else if (!(th instanceof RuntimeException)) {
            LOGGER.error("Camera service throws unknown exception, this should not happen, %{public}s", th.getMessage());
        } else {
            LOGGER.error("Camera service throws runtime exception, this should not happen, %{public}s", th.getMessage());
            throw ((RuntimeException) th);
        }
    }
}
