package com.huawei.media.scan;

import java.util.Set;

public class CacheBlackList {
    private static final String[] BLACK_LIST = {"/mmcache/", "/AnyofficeiconDownload/", "/ShareSDK/com.sdu.didi.psnger/cache/images/", "/soufun/res/cache/splash_ads/", "/ShareSDK/com.sinovatech.unicom.ui/cache/image/", "/AnyOffice/adv/", "/carowner/h5cahce/", "/mwqi/cache/", "/Mob/com.sdu.didi.psnger/cache/images/", "/lianjia/lj_social_cache/", "/com.chenghuitong/picture/cache/", "/mwqi/cache/", "/HwRemoteController/temp/Cache/", "/Download/Screenshot/tmp/", "/183/LizhiFM/Caches/", "/com.ushaqi.zhuishush/cache/", "/CTRIP/cache/", "/DuoDuoHouse/cache/img/", "/hers/cache/", "/iCredit/tempcapture/", "/iFlyME/imagecache/", "/immomo/cache/", "/jumei/jmframe/cache/pics/", "/kuaidi100/logoCache/", "/lufax/cache/", "/netease/cloudmusic/Cache/ImageCache/", "/netease/cloudmusic/Cache/Image/", "/powerword/cache/", "/SMZDM/cache/", "/TianQiTong/cache/", "/wochachaCache/", "/xiangha/cache/", "/Yixin/img_cache/", "/Huawei/HuaweiIT/", "/HRAndroidFrame/", "/HuaweiSystem/", "/Huawei/Sns/", "/log/hiview/onekeycapture/", "/meishij/Temp/", "/人民日报/imgCache/", "/imageCache/", "/JDB/cache/", "/FeiNiu/cache/", "/时光网/tem_pic/", "/xiaokaxiu/image_cache/", "/liepin/cache/", "/4399Game/videoCache/", "/moxiu/temp/", "/mierjunshi/imgCache/", "/sina/SinaSports/cache/", "/XiaoJi/Cache/Icon/", "/DuoKan/Cache/", "/SNEPA/cache/images/", "/zongheng/cache/", "/zongheng/tempImage/", "/sub/kekeyiyu/Caches/Image/", "/pris/cache/", "/2345Browser/Cache/", "/XMEye/temp_images/", "/u17phone/cache/", "/pcauto/cache/", "/TiantianGame/cachePic/", "/WhatsLIVE/Cache/", "/VisitActivity/cache/", "/doupai/pictures/temp1/", "/lolBoxCache/", "/ojc/cache/", "/FingerCache/", "/driverhelper/cache/headImage/", "/lianjia/lj_social_cache/", "/MissFresh/cache/", "/jtjr/jiaoyoubao/data/cache/", "/gtgj_bitmapcache/", "/boc/temp/", "/Changker/imageCache/", "/youkuvr/Cache/", "/OGQ/TEDAIR/Cache/", "/Changker/imageCache/head_img/", "/Pictures/systemKTCP/"};

    public static void getBlackList(Set set) {
        if (set != null) {
            int i = 0;
            while (true) {
                String[] strArr = BLACK_LIST;
                if (i < strArr.length) {
                    set.add(strArr[i]);
                    i++;
                } else {
                    return;
                }
            }
        }
    }
}
