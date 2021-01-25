package ohos.media.camera.mode.utils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class JpegFileNameUtil {
    private static final int ADD = 0;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(JpegFileNameUtil.class);
    private static final int MAX_JPEG_NAME_LIST_LENGTH = 500;
    private static final int REMOVE = 1;
    private static List<String> fileNameList = new CopyOnWriteArrayList();

    private JpegFileNameUtil() {
    }

    public static synchronized void addJpegFileName(String str) {
        synchronized (JpegFileNameUtil.class) {
            LOGGER.debug("addJpegFileName: %{public}s", str);
            if (StringUtil.isEmptyString(str)) {
                LOGGER.error("addJpegFileName,fileName is empty", new Object[0]);
            } else if (checkIfJpegFileNameExist(str)) {
                LOGGER.error("jpeg file name already exist!!", new Object[0]);
            } else {
                updateFileNameList(0, str);
            }
        }
    }

    public static synchronized void removeJpegFileName(String str) {
        synchronized (JpegFileNameUtil.class) {
            LOGGER.debug("removeJpegFileName: %{public}s", str);
            if (StringUtil.isEmptyString(str)) {
                LOGGER.error("removeJpegFileName,fileName is empty", new Object[0]);
            } else {
                updateFileNameList(1, str);
            }
        }
    }

    public static synchronized boolean checkIfJpegFileNameExist(String str) {
        synchronized (JpegFileNameUtil.class) {
            LOGGER.debug("checkIfJpegFileNameExist, fileName: %{public}s", str);
            if (StringUtil.isEmptyString(str)) {
                LOGGER.error("checkIfJpegFileNameExist,fileName is empty", new Object[0]);
                return false;
            }
            return fileNameList.contains(str);
        }
    }

    private static void updateFileNameList(int i, String str) {
        if (i == 0) {
            fileNameList.add(str);
            int size = fileNameList.size();
            if (size > 500) {
                LOGGER.warn("fileNameListSize exceeds max length, size is %{public}d", Integer.valueOf(size));
                int i2 = size - 500;
                for (int i3 = 0; i3 < i2; i3++) {
                    LOGGER.error("remove the first file name", new Object[0]);
                    fileNameList.remove(0);
                }
            }
        } else if (i != 1) {
            LOGGER.error("invalid operator ", new Object[0]);
        } else {
            List<String> list = fileNameList;
            Objects.requireNonNull(str);
            list.removeIf(new Predicate(str) {
                /* class ohos.media.camera.mode.utils.$$Lambda$JpegFileNameUtil$S4BXTl5Ly3EHhXAReFCtlz2B8eo */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return this.f$0.equals((String) obj);
                }
            });
        }
        int size2 = fileNameList.size();
        LOGGER.info("fileNameList size is %{public}d", Integer.valueOf(size2));
        for (int i4 = 0; i4 < size2; i4++) {
            LOGGER.info("[%{public}d] %{public}s", Integer.valueOf(i4), fileNameList.get(i4));
        }
    }
}
