package huawei.power;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ScreenAclLowerPowerImpl {
    private static final String ENABLE_VALUE = SystemProperties.get("hw.display.acl_value", "1,2");
    private static String TAG = "ScreenAclLowerPowerImpl";
    private boolean DEBUG;
    private boolean mAclActive;
    private ArrayList<String> mAclApps;
    private String mCurrentApk;

    public ScreenAclLowerPowerImpl(Context context) {
        this.mCurrentApk = null;
        this.mAclActive = false;
        this.mAclApps = new ArrayList();
        this.DEBUG = false;
        this.mCurrentApk = "com.huawei.android.launcher";
        this.mAclApps.add("10001");
        this.mAclApps.add("10008");
        this.mAclApps.add("10003");
        this.mAclApps.add("com.android.browser");
        this.mAclApps.add("com.android.email");
        this.mAclApps.add("com.android.mediacenter");
        this.mAclApps.add("com.mybook66");
        this.mAclApps.add("com.google.android.gm");
        this.mAclApps.add("com.qzone");
        this.mAclApps.add("com.tencent.mm");
        this.mAclApps.add("com.taobao.taobao");
        this.mAclApps.add("com.jingdong.app.mall");
        this.mAclApps.add("com.huawei.android.webcustomize");
        this.mAclApps.add("com.tencent.news");
        this.mAclApps.add("com.ss.android.article.news");
        this.mAclApps.add("com.tencent.mtt");
        this.mAclApps.add("com.sina.weibo");
        this.mAclApps.add("com.UCMobile");
        this.mAclApps.add("com.chaozh.ireaderfree");
        this.mAclApps.add("com.chaozh.ireaderfree15");
        this.mAclApps.add("com.supercell.boombeach");
        this.mAclApps.add("com.supercell.boombeach.HUAWEI");
        this.mAclApps.add("com.tencent.clover");
        this.mAclApps.add("com.autonavi.minimap");
        this.mAclApps.add("com.sdu.didi.psnger");
        this.mAclApps.add("com.sankuai.meituan");
        this.mAclApps.add("com.dianping.v1");
        this.mAclApps.add("ctrip.android.view");
        this.mAclApps.add("com.moji.mjweather");
        this.mAclApps.add("com.ximalaya.ting.android");
        this.mAclApps.add("com.baidu.netdisk");
        this.mAclApps.add("com.cubic.autohome");
        this.mAclApps.add("com.tencent.karaoke");
        this.mAclApps.add("com.huawei.hwireader");
        this.mAclApps.add("com.hexin.plat.android");
        this.mAclApps.add("com.baidu.BaiduMap");
        this.mAclApps.add("com.tencent.qqmusic");
        this.mAclApps.add("com.kugou.android");
        this.mAclApps.add("com.netease.cloudmusic");
        this.mAclApps.add("com.baidu.searchbox");
        this.mAclApps.add("com.tencent.qqlive");
        this.mAclApps.add("com.tencent.mobileqq");
        this.mAclApps.add("com.thestore.main");
        this.mAclApps.add("com.netease.newsreader.activity");
        this.mAclApps.add("com.ifeng.news2");
        this.mAclApps.add("com.achievo.vipshop");
        this.mAclApps.add("com.sina.news");
        this.mAclApps.add("com.alibaba.android.rimet");
        this.mAclApps.add("com.smile.gifmaker");
        this.mAclApps.add("cn.kuwo.kwmusichd");
        this.mAclApps.add("cn.kuwo.kwmusic");
        this.mAclApps.add("com.eg.android.AlipayGphone");
        this.mAclApps.add("com.tencent.androidqqmail");
        this.mAclApps.add("com.nuomi");
        this.mAclApps.add("com.baidu.tieba");
        this.mAclApps.add("com.sohu.newsclient");
        this.mAclApps.add("com.netease.mail");
        this.mAclApps.add("com.netease.mobimail");
        this.mAclApps.add("com.evernote");
        this.mAclApps.add("com.qqgame.hlddz");
        this.mAclApps.add("com.tencent.tmgp.sgame");
        this.mAclApps.add("com.happyelements.AndroidAnimal.qq");
        this.mAclApps.add("com.happyelements.AndroidAnimal");
        this.mAclApps.add("com.qwh.boya.huawei");
        this.mAclApps.add("com.tencent.pao");
        this.mAclApps.add("com.tencent.tmgp.rungame");
        this.mAclApps.add("com.youku.phone");
        this.mAclApps.add("com.qiyi.video");
        this.mAclApps.add("com.qiyi.tv");
        this.mAclApps.add("com.qunar");
        this.mAclApps.add("cn.wps.moffice_eng");
        this.mAclApps.add("com.tencent.tmgp.ttwq");
        this.mAclApps.add("com.neusoft.ebpp");
        this.mAclApps.add("com.huawei.it.iadmin");
        this.mAclApps.add("com.gombosdev.displaytester.usboys");
        this.mAclApps.add("com.duowan.mobile");
        this.mAclApps.add("air.tv.douyu.android");
        this.mAclApps.add("com.duowan.kiwi");
        this.mAclApps.add("com.meelive.ingkee");
        this.mAclApps.add("com.panda.videoliveplatform");
        this.mAclApps.add("org.fungo.fungolive");
        this.mAclApps.add("com.cmcc.migutvtwo");
        this.mAclApps.add("com.huajiao");
        this.mAclApps.add("tv.xiaoka.live");
        this.mAclApps.add("com.tencent.now");
        this.mAclApps.add("com.zhihu.android");
        this.mAclApps.add("com.google.android.youtube");
        this.mAclApps.add("com.gameloft.android.anmp.glofta8hm");
        this.mAclApps.add("com.gameloft.android.anmp.gloftg5eg");
        this.mAclApps.add("com.primatelabs.geekbench");
        this.mAclApps.add("com.whatsapp");
        this.mAclApps.add("com.facebook.orca");
        this.mAclApps.add("com.atec.superlightforfacebook");
        this.mAclApps.add("com.spotify.music");
        this.mAclApps.add("com.didi.echo");
        this.mAclApps.add("com.microsoft.office.outlook");
        this.mAclApps.add("com.android.chrome");
        this.mAclApps.add("com.facebook.katana");
        this.mAclApps.add("com.google.android.apps.plus");
        this.mAclApps.add("com.google.android.apps.maps");
        this.mAclApps.add("com.google.android.videos");
        this.mAclApps.add("com.google.android.apps.books");
        this.mAclApps.add("com.google.android.play.games");
        this.mAclApps.add("com.google.android.apps.docs");
        this.mAclApps.add("com.android.vending");
        this.mAclApps.add("com.opera.mini.android");
        this.mAclApps.add("com.tencent.king.candycrushsaga");
        this.mAclApps.add("com.tinder");
        this.mAclApps.add("com.supercell.clashofclans");
        this.mAclApps.add("com.supercell.clashroyale");
        unsetACL();
    }

    public void handlePGScene(int stateType, int eventType, int pid, String pkgName, int uid) {
        if (this.DEBUG) {
            Log.d(TAG, "handle stateType = " + stateType + ", eventType = " + eventType + ", pid = " + pid + ", pkgName = " + pkgName + ", uid = " + uid);
        }
        if (pkgName != null && eventType == 1) {
            if (this.mCurrentApk.equals(pkgName)) {
                this.mAclActive = false;
                if (this.DEBUG) {
                    Log.d(TAG, "screen state changed, reset mAclActive state to false");
                }
            } else {
                this.mCurrentApk = pkgName;
            }
            if (this.mAclApps.contains(pkgName) || this.mAclApps.contains("" + stateType)) {
                if (!this.mAclActive) {
                    if (this.DEBUG) {
                        Log.d(TAG, "will set ACL for eventType = " + eventType + ", apk = " + pkgName);
                    }
                    setACL();
                    this.mAclActive = true;
                } else if (this.DEBUG) {
                    Log.d(TAG, "no need to setACL for already set");
                }
            } else if (this.mAclActive) {
                if (this.DEBUG) {
                    Log.d(TAG, "will unset ACL for eventType = " + eventType + ", pkgName = " + pkgName);
                }
                unsetACL();
                this.mAclActive = false;
            } else if (this.DEBUG) {
                Log.d(TAG, "no need to unset ACL for unset already");
            }
        }
    }

    private boolean setACL() {
        return setACLPowerSavingEnableState(true);
    }

    private boolean unsetACL() {
        return setACLPowerSavingEnableState(false);
    }

    private boolean writeDeviceNode(String path, String node, String writeValue) {
        boolean retVal = true;
        try {
            FileOutputStream fos = new FileOutputStream(path + node);
            if (fos == null) {
                Log.w(TAG, "failed to writeDeviceNode for fos is null");
                return false;
            }
            try {
                byte[] byteValue = writeValue.getBytes(Charset.defaultCharset());
                fos.write(byteValue, 0, byteValue.length);
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.w(TAG, "failed to close fos");
                }
            } catch (IOException e2) {
                Log.w(TAG, "failed to writeDeviceNode for IOException");
                retVal = false;
                try {
                    fos.close();
                } catch (IOException e3) {
                    Log.w(TAG, "failed to close fos");
                }
            } catch (Throwable th) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    Log.w(TAG, "failed to close fos");
                }
                throw th;
            }
            return retVal;
        } catch (FileNotFoundException e5) {
            Log.w(TAG, "failed to construct FileOutputStream for ACL node");
            Log.w(TAG, "failed to writeDeviceNode for fos is null");
            return false;
        } catch (Throwable th2) {
            Log.w(TAG, "failed to writeDeviceNode for fos is null");
            return false;
        }
    }

    private boolean setACLPowerSavingEnableState(boolean enable) {
        String str;
        String enableValue = ENABLE_VALUE;
        String disableValue = "1,0";
        String path = "/sys/class/graphics/fb0/";
        String node = "amoled_acl";
        if (enable) {
            str = enableValue;
        } else {
            str = disableValue;
        }
        boolean retValue = writeDeviceNode(path, node, str);
        if (retValue) {
            str = TAG;
            StringBuilder append = new StringBuilder().append("setACL with: ");
            if (!enable) {
                enableValue = disableValue;
            }
            Log.w(str, append.append(enableValue).append(", 1,0 means disable, 1,3 means 30% for: ").append(this.mCurrentApk).toString());
        }
        return retValue;
    }
}
