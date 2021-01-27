package com.android.server.notification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.IoThread;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwNotificationManagerServiceEx implements IHwNotificationManagerServiceEx {
    private static final String ACTION_UPDATE_BLOCK_LIST = "com.huawei.hwpush.action.NOTIFICATION_BLOCK_LIST_UPDATED";
    private static final String ATTR_VERSION_BLOCK_LIST = "version";
    private static final String BLOCK_LIST = "notification_block_list";
    private static final String BLOCK_LIST_FILE = "notification_block_list.xml";
    private static final int DB_VERSION = 1;
    private static final String DIALER_PKGNAME = "com.android.dialer";
    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String GMS_SCREEN_LOCK_CMP = "com.google.android.gms/.trustagent";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final boolean IS_CUST_DIALER_ENABLE = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String NEW_PACKAGE_NAME_DESKCLOCK = "com.huawei.deskclock";
    private static final int NOTIFICATION_BLOCK_RULE_CLOSE = 0;
    private static final int NOTIFICATION_BLOCK_RULE_OPEN = 1;
    private static final int NOTIFICATION_BLOCK_RULE_STATE = SystemProperties.getInt("hw_sc.notification.block_rule.enabled", 0);
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    private static final String OLD_PACKAGE_NAME_DESKCLOCK = "com.android.deskclock";
    private static final String PACKAGE_NAME_SYSTEM = "android";
    private static final String PERMISSION_RECEIVE_NOTIFICATION_BLOCK_LIST = "com.huawei.permission.RECEIVE_NOTIFICATION_BLOCK_LIST";
    private static final String SPECIAL_PATTERN = "##.*~~.*##";
    private static final String SPECIAL_PREFIX_SUFFIX_PATTERN = "##";
    private static final int SPECIAL_SPLIT_ARRAY_LENGTH = 2;
    private static final String SPECIAL_SPLIT_PATTERN = "~~";
    private static final String TAG = "HwNotificationManagerServiceEx";
    private static final String TAG_NOTIFICATION_BLOCK_LIST = "notification-block-list";
    private static final String TAG_PACKAGE_NAME = "package";
    private AtomicFile mBlockListFile;
    private BroadcastReceiver mBlockListReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.HwNotificationManagerServiceEx.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (HwNotificationManagerServiceEx.ACTION_UPDATE_BLOCK_LIST.equals(action)) {
                    ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra(HwNotificationManagerServiceEx.BLOCK_LIST);
                    StringBuilder sb = new StringBuilder();
                    sb.append("onReceive: action=");
                    sb.append(action);
                    sb.append(";list=");
                    sb.append(stringArrayListExtra != null ? stringArrayListExtra.size() : 0);
                    Log.i(HwNotificationManagerServiceEx.TAG, sb.toString());
                    synchronized (HwNotificationManagerServiceEx.this.mBlockedList) {
                        HwNotificationManagerServiceEx.this.mBlockedList.clear();
                        if (stringArrayListExtra != null && stringArrayListExtra.size() > 0) {
                            HwNotificationManagerServiceEx.this.mBlockedList.addAll(stringArrayListExtra);
                        }
                    }
                    HwNotificationManagerServiceEx.this.handleSaveBlockListFile();
                }
            }
        }
    };
    private final List<String> mBlockedList = new ArrayList();

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public void init(Context context) {
        if (context != null && NOTIFICATION_BLOCK_RULE_STATE == 1) {
            this.mBlockListFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), BLOCK_LIST_FILE), TAG_NOTIFICATION_BLOCK_LIST);
            loadBlockListFile();
            context.registerReceiverAsUser(this.mBlockListReceiver, UserHandle.ALL, new IntentFilter(ACTION_UPDATE_BLOCK_LIST), PERMISSION_RECEIVE_NOTIFICATION_BLOCK_LIST, null);
        }
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getHwOpPkg(StatusBarNotification sbn) {
        String pkg = sbn.getOpPkg();
        if (NOTIFICATION_CENTER_PKG.equals(pkg)) {
            return sbn.getPackageName();
        }
        return pkg;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isPushSpecialRequest(String pkg, String token) {
        return NOTIFICATION_CENTER_PKG.equals(pkg) && (!TextUtils.isEmpty(token) && token.matches(SPECIAL_PATTERN));
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestPkg(String pkg, String tag) {
        String[] array = tag.split(SPECIAL_SPLIT_PATTERN);
        if (!isPushTokenInvailid(array)) {
            return array[0].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        }
        Log.w(TAG, "getPushSpecialRequestPkg is invallid: " + tag);
        return null;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestTag(String tag) {
        String[] array = tag.split(SPECIAL_SPLIT_PATTERN);
        if (isPushTokenInvailid(array)) {
            Log.w(TAG, "getPushSpecialRequestTag is invallid: " + tag);
            return null;
        }
        String tagFromPush = array[1].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        if (TextUtils.isEmpty(tagFromPush)) {
            return null;
        }
        return tagFromPush;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestChannel(String channelId) {
        String[] array = channelId.split(SPECIAL_SPLIT_PATTERN);
        if (!isPushTokenInvailid(array)) {
            return array[1].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        }
        Log.w(TAG, "getPushSpecialRequestChannel is invallid: " + channelId);
        return null;
    }

    private boolean isPushTokenInvailid(String[] array) {
        return array == null || array.length != 2 || TextUtils.isEmpty(array[0]) || TextUtils.isEmpty(array[1]);
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isBanNotification(String pkg, Notification notification) {
        if (pkg == null || notification == null) {
            return false;
        }
        if (!this.mBlockedList.contains(pkg) || notification.contentView == null) {
            boolean isBanGMSInCN = pkg.equals(GMS_PACKAGE) && IS_CHINA_AREA;
            boolean isIntentForTrustAgent = false;
            long identity = Binder.clearCallingIdentity();
            if (isBanGMSInCN) {
                try {
                    if (!(notification.contentIntent == null || notification.contentIntent.getIntent() == null || notification.contentIntent.getIntent().getComponent() == null)) {
                        isIntentForTrustAgent = notification.contentIntent.getIntent().getComponent().flattenToShortString().contains(GMS_SCREEN_LOCK_CMP);
                        Log.w(TAG, "isBanNotification intent = " + notification.contentIntent.getIntent());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "method isBanNotification has Exception");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(identity);
            if (!isBanGMSInCN || !isIntentForTrustAgent) {
                return false;
            }
            return true;
        }
        Log.w(TAG, "isBanNotification: in block list! pkg=" + pkg);
        return true;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public void adjustNotificationGroupIfNeeded(Notification notification, int id) {
        if (notification != null && notification.getGroup() == null && notification.getSortKey() == null) {
            if (((notification.flags & 2) != 0) || notification.isForegroundService() || notification.isMediaNotification()) {
                notification.setGroup("ranker_group" + id);
            }
        }
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isSendNotificationDisable(int callingUid, String pkg, Notification notification) {
        return !(isUidSystemOrPhone(callingUid) || "android".equals(pkg) || isCustDialer(pkg)) && !isAlarmNotification(pkg, notification) && HwDeviceManager.disallowOp(33);
    }

    private boolean isUidSystemOrPhone(int uid) {
        int appid = UserHandle.getAppId(uid);
        return appid == 1000 || appid == 1001 || uid == 0;
    }

    private boolean isCustDialer(String packageName) {
        return IS_CUST_DIALER_ENABLE && DIALER_PKGNAME.equals(packageName);
    }

    private boolean isAlarmNotification(String pkg, Notification notification) {
        if (notification == null) {
            return false;
        }
        if ((NEW_PACKAGE_NAME_DESKCLOCK.equals(pkg) || OLD_PACKAGE_NAME_DESKCLOCK.equals(pkg)) && "alarm".equals(notification.category)) {
            return true;
        }
        return false;
    }

    private void loadBlockListFile() {
        synchronized (this.mBlockListFile) {
            InputStream infile = null;
            try {
                infile = this.mBlockListFile.openRead();
                readBlockListXml(infile);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Unable to find bolck list file");
            } catch (IOException e2) {
                Log.e(TAG, "Unable to read bolck list file");
            } catch (NumberFormatException e3) {
                Log.e(TAG, "Unable to parse bolck list file1");
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "Unable to parse bolck list file2");
            } finally {
                IoUtils.closeQuietly(infile);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSaveBlockListFile() {
        IoThread.getHandler().post(new Runnable() {
            /* class com.android.server.notification.$$Lambda$HwNotificationManagerServiceEx$C6dk6RC5zbUh8RE6OfDzOs2aFIA */

            @Override // java.lang.Runnable
            public final void run() {
                HwNotificationManagerServiceEx.this.lambda$handleSaveBlockListFile$0$HwNotificationManagerServiceEx();
            }
        });
    }

    public /* synthetic */ void lambda$handleSaveBlockListFile$0$HwNotificationManagerServiceEx() {
        synchronized (this.mBlockListFile) {
            try {
                FileOutputStream stream = this.mBlockListFile.startWrite();
                try {
                    writeBlockListXml(stream);
                    this.mBlockListFile.finishWrite(stream);
                } catch (IOException e) {
                    Log.w(TAG, "Failed to save block list file, restoring backup");
                    this.mBlockListFile.failWrite(stream);
                } catch (Throwable th) {
                    throw th;
                }
            } catch (IOException e2) {
                Log.w(TAG, "Failed to save block list file");
            }
        }
    }

    private void writeBlockListXml(OutputStream stream) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.startTag(null, TAG_NOTIFICATION_BLOCK_LIST);
        out.attribute(null, ATTR_VERSION_BLOCK_LIST, Integer.toString(1));
        synchronized (this.mBlockedList) {
            for (String pkg : this.mBlockedList) {
                out.startTag(null, "package");
                out.text(pkg);
                out.endTag(null, "package");
            }
        }
        out.endTag(null, TAG_NOTIFICATION_BLOCK_LIST);
        out.endDocument();
    }

    private void readBlockListXml(InputStream stream) throws XmlPullParserException, IOException {
        String pkg;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        List<String> blockList = new ArrayList<>();
        while (parser.next() != 1) {
            if (parser.getEventType() == 2 && "package".equals(parser.getName()) && (pkg = parser.nextText()) != null && !pkg.isEmpty()) {
                blockList.add(pkg);
            }
        }
        synchronized (this.mBlockedList) {
            this.mBlockedList.clear();
            this.mBlockedList.addAll(blockList);
        }
    }
}
