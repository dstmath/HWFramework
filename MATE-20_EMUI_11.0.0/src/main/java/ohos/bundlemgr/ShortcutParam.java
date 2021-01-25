package ohos.bundlemgr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.ShortcutInfo;
import ohos.bundle.ShortcutIntent;

public final class ShortcutParam {
    private static final int BUFFER_SIZE = 1024;
    private String bundleName;
    private String disableMessage;
    private String icon;
    private int iconId = -1;
    private InputStream iconStream;
    private String id;
    private List<ShortcutIntent> intents;
    private String label;
    private int labelId = -1;

    public ShortcutParam() {
    }

    public ShortcutParam(ShortcutInfo shortcutInfo) {
        if (shortcutInfo != null) {
            this.id = shortcutInfo.getId();
            this.bundleName = shortcutInfo.getBundleName();
            this.icon = shortcutInfo.getIcon();
            this.iconId = shortcutInfo.getShortcutIconId();
            if (shortcutInfo.getIconStream() != null) {
                this.iconStream = new ByteArrayInputStream(copyInputStream(shortcutInfo.getIconStream()));
            }
            this.label = shortcutInfo.getLabel();
            this.labelId = shortcutInfo.getShortcutLabelId();
            this.disableMessage = shortcutInfo.getDisableMessage();
            if (shortcutInfo.getIntents() != null) {
                this.intents = new ArrayList();
                this.intents.addAll(shortcutInfo.getIntents());
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getIcon() {
        return this.icon;
    }

    public InputStream getIconStream() {
        return this.iconStream;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDisableMessage() {
        return this.disableMessage;
    }

    public List<ShortcutIntent> getIntents() {
        return this.intents;
    }

    public int getShortcutIconId() {
        return this.iconId;
    }

    public int getShortcutLabelId() {
        return this.labelId;
    }

    private byte[] copyInputStream(InputStream inputStream) {
        Throwable th;
        byte[] bArr;
        Closeable closeable = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr2 = new byte[1024];
                while (true) {
                    int read = inputStream.read(bArr2);
                    if (read <= -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr2, 0, read);
                }
                byteArrayOutputStream.flush();
                bArr = byteArrayOutputStream.toByteArray();
                closeStream(byteArrayOutputStream);
            } catch (IOException unused) {
                closeable = byteArrayOutputStream;
                try {
                    AppLog.e("shortcut param copy inputstream failed", new Object[0]);
                    bArr = new byte[0];
                    closeStream(closeable);
                    closeStream(inputStream);
                    return bArr;
                } catch (Throwable th2) {
                    th = th2;
                    closeStream(closeable);
                    closeStream(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = byteArrayOutputStream;
                closeStream(closeable);
                closeStream(inputStream);
                throw th;
            }
        } catch (IOException unused2) {
            AppLog.e("shortcut param copy inputstream failed", new Object[0]);
            bArr = new byte[0];
            closeStream(closeable);
            closeStream(inputStream);
            return bArr;
        }
        closeStream(inputStream);
        return bArr;
    }

    private void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                AppLog.e("shortcut param close stream failed", new Object[0]);
            }
        }
    }
}
