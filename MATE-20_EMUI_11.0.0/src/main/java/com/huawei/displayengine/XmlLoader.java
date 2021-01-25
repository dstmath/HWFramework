package com.huawei.displayengine;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.huawei.displayengine.XmlData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class XmlLoader<T extends XmlData> {
    private static final int ROOT_DEPTH = 1;
    protected final boolean HW_DEBUG;
    protected final boolean HW_FLOW;
    protected final String TAG = getClass().getSimpleName();

    /* access modifiers changed from: protected */
    public abstract String getXmlPath();

    public XmlLoader() {
        boolean z = false;
        this.HW_DEBUG = Log.HWLog || (Log.HWModuleLog && Log.isLoggable(this.TAG, 3));
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(this.TAG, 4))) {
            z = true;
        }
        this.HW_FLOW = z;
    }

    /* access modifiers changed from: protected */
    public final void load(T data, XmlElement rootElement) {
        if (data == null) {
            Slog.e(this.TAG, "load() failed! input data is null");
            return;
        }
        String path = getXmlPath();
        if (path == null) {
            Slog.e(this.TAG, "load() failed! xmlPath is null");
        } else if (rootElement == null) {
            Slog.e(this.TAG, "load() failed! getElements is null");
        } else {
            load(data, path, rootElement);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0073, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0074, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0077, code lost:
        throw r4;
     */
    private void load(T data, String path, XmlElement rootElement) {
        if (this.HW_FLOW) {
            Slog.i(this.TAG, "load() begin");
        }
        String rootName = rootElement.getBranchName();
        if (rootName == null) {
            Slog.e(this.TAG, "load() failed, root name is null!");
            data.loadDefault();
            return;
        }
        try {
            FileInputStream inputStream = new FileInputStream(path);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
            int type = parser.next();
            while (true) {
                if (type != 1) {
                    if (parser.getDepth() == 1 && type == 2 && rootName.equals(parser.getName())) {
                        rootElement.parse(parser, data);
                        break;
                    }
                    type = parser.next();
                } else {
                    break;
                }
            }
            if (!rootElement.check(data)) {
                data.loadDefault();
            }
            if (this.HW_FLOW) {
                Slog.i(this.TAG, "load() end");
            }
            inputStream.close();
        } catch (XmlPullParserException e) {
            String str = this.TAG;
            Slog.e(str, "load() failed! " + e);
            e.printStackTrace();
            data.loadDefault();
        } catch (IllegalArgumentException e2) {
            String str2 = this.TAG;
            Slog.e(str2, "load() failed! " + e2);
            data.loadDefault();
        } catch (FileNotFoundException e3) {
            Slog.e(this.TAG, "load() failed!");
        } catch (IOException e4) {
            Slog.e(this.TAG, "load() failed! IOException");
        }
    }
}
