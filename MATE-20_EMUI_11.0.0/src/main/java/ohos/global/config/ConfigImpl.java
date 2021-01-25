package ohos.global.config;

import java.io.IOException;
import java.io.InputStream;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ConfigImpl extends Config {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ConfigImpl");
    private InputStream handle;

    @Override // ohos.global.config.Config
    public ConfigType getType() {
        return ConfigType.CAMERA_SOUND;
    }

    @Override // ohos.global.config.Config, java.io.InputStream
    public int available() {
        InputStream inputStream = this.handle;
        if (inputStream == null) {
            HiLog.error(LABEL, "the handle of available method is null, please check the Constructor", new Object[0]);
            return 0;
        }
        try {
            return inputStream.available();
        } catch (IOException unused) {
            HiLog.error(LABEL, "IOException occurs.", new Object[0]);
            return -1;
        }
    }

    @Override // ohos.global.config.Config, java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException, NullPointerException, IndexOutOfBoundsException {
        InputStream inputStream = this.handle;
        if (inputStream != null) {
            return inputStream.read(bArr, i, i2);
        }
        HiLog.error(LABEL, "the handle of read method is null, please check the Constructor", new Object[0]);
        return -1;
    }

    @Override // ohos.global.config.Config, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        InputStream inputStream = this.handle;
        if (inputStream == null) {
            HiLog.error(LABEL, "the handle of close method is null, please check the Constructor", new Object[0]);
            return;
        }
        inputStream.close();
        this.handle = null;
    }
}
