package com.android.server.usb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwUsbUserSettingsManagerEx implements IHwUsbUserSettingsManagerEx {
    private static final int READ_XML_TO_SET = 1;
    private static final String RM_USB_PERM_DIALOG_CFG_FILE_PATH = "xml/hw_usb_permission/hw_usb_permission_config.xml";
    private static final String RM_USB_PERM_DIALOG_XML_ATTRIBUTE_NAME = "name";
    private static final String RM_USB_PERM_DIALOG_XML_PKG = "package";
    private static final String TAG = HwUsbUserSettingsManagerEx.class.getSimpleName();
    private Context mContext;
    private Handler mHwHandler;
    private IHwUsbUserSettingsManagerInner mHwUsbUserSettingsManagerInner;
    private boolean mIsReadXmlToSetFinish = false;
    private HandlerThread mReadXmlThread;
    private HashSet<String> mRmUsbPermDialogSet = new HashSet<>();
    private File mUsbPermConfigXmlFile = HwCfgFilePolicy.getCfgFile(RM_USB_PERM_DIALOG_CFG_FILE_PATH, 0);

    public HwUsbUserSettingsManagerEx(IHwUsbUserSettingsManagerInner hwUsbUserSettingsManagerInner, Context context) {
        this.mHwUsbUserSettingsManagerInner = hwUsbUserSettingsManagerInner;
        this.mContext = context;
        initReadXml();
    }

    public boolean removeUsbPermissionDialog(UsbDevice device, String packageName, PendingIntent pendingIntent, int uid) {
        if (device == null || this.mRmUsbPermDialogSet.isEmpty() || !this.mIsReadXmlToSetFinish) {
            return false;
        }
        if (!this.mRmUsbPermDialogSet.contains(packageName)) {
            String str = TAG;
            Slog.i(str, packageName + " isn't in access usb device white list");
            return false;
        } else if (isSecureUsbDevice(device.getDeviceClass())) {
            return false;
        } else {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                if (isSecureUsbDevice(device.getInterface(i).getInterfaceClass())) {
                    return false;
                }
            }
            IUsbManager service = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            if (service == null) {
                Slog.e(TAG, "UsbService is null");
                return false;
            }
            try {
                service.grantDevicePermission(device, uid);
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra("device", device);
                    intent.putExtra("permission", true);
                    pendingIntent.send(this.mContext, 0, intent);
                }
            } catch (PendingIntent.CanceledException e) {
                Slog.w(TAG, "requestPermission PendingIntent was cancelled");
            } catch (RemoteException e2) {
                Slog.e(TAG, "UsbService connection failed");
                return false;
            }
            return true;
        }
    }

    private void initReadXml() {
        File file = this.mUsbPermConfigXmlFile;
        if (file != null && file.exists()) {
            this.mReadXmlThread = new HandlerThread("readXml");
            this.mReadXmlThread.start();
            this.mHwHandler = new Handler(this.mReadXmlThread.getLooper()) {
                /* class com.android.server.usb.HwUsbUserSettingsManagerEx.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        HwUsbUserSettingsManagerEx hwUsbUserSettingsManagerEx = HwUsbUserSettingsManagerEx.this;
                        hwUsbUserSettingsManagerEx.readXmlToSet(hwUsbUserSettingsManagerEx.mUsbPermConfigXmlFile);
                        HwUsbUserSettingsManagerEx.this.mReadXmlThread.quit();
                    }
                }
            };
            Handler handler = this.mHwHandler;
            handler.sendMessage(handler.obtainMessage(1));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004e, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0051, code lost:
        throw r2;
     */
    private void readXmlToSet(File xmlFile) {
        try {
            InputStream inputStream = new FileInputStream(xmlFile);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, "utf-8");
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                xmlEventType = xmlParser.next();
                if (xmlEventType == 2) {
                    if (RM_USB_PERM_DIALOG_XML_PKG.equals(xmlParser.getName())) {
                        String xmlPkgName = xmlParser.getAttributeValue(null, RM_USB_PERM_DIALOG_XML_ATTRIBUTE_NAME);
                        if (!TextUtils.isEmpty(xmlPkgName)) {
                            this.mRmUsbPermDialogSet.add(xmlPkgName);
                        }
                    }
                }
            }
            this.mIsReadXmlToSetFinish = true;
            inputStream.close();
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "readXmlToSet FileNotFoundException");
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "readXmlToSet XmlPullParserException");
        } catch (IOException e3) {
            Slog.e(TAG, "readXmlToSet IOException");
        }
    }

    private boolean isSecureUsbDevice(int usbClass) {
        if (usbClass != 1 && usbClass != 8 && usbClass != 9 && usbClass != 13) {
            return false;
        }
        String str = TAG;
        Slog.i(str, "is secure usb device, usb class = " + usbClass);
        return true;
    }
}
