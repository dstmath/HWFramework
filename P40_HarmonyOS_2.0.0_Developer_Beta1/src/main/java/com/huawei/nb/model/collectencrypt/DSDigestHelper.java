package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSDigestHelper extends AEntityHelper<DSDigest> {
    private static final DSDigestHelper INSTANCE = new DSDigestHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DSDigest dSDigest) {
        return null;
    }

    private DSDigestHelper() {
    }

    public static DSDigestHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSDigest dSDigest) {
        Integer id = dSDigest.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String title = dSDigest.getTitle();
        if (title != null) {
            statement.bindString(2, title);
        } else {
            statement.bindNull(2);
        }
        String pageUrl = dSDigest.getPageUrl();
        if (pageUrl != null) {
            statement.bindString(3, pageUrl);
        } else {
            statement.bindNull(3);
        }
        String serverId = dSDigest.getServerId();
        if (serverId != null) {
            statement.bindString(4, serverId);
        } else {
            statement.bindNull(4);
        }
        String mhtUtl = dSDigest.getMhtUtl();
        if (mhtUtl != null) {
            statement.bindString(5, mhtUtl);
        } else {
            statement.bindNull(5);
        }
        String thumbnail = dSDigest.getThumbnail();
        if (thumbnail != null) {
            statement.bindString(6, thumbnail);
        } else {
            statement.bindNull(6);
        }
        String htmlPath = dSDigest.getHtmlPath();
        if (htmlPath != null) {
            statement.bindString(7, htmlPath);
        } else {
            statement.bindNull(7);
        }
        String createdtime = dSDigest.getCreatedtime();
        if (createdtime != null) {
            statement.bindString(8, createdtime);
        } else {
            statement.bindNull(8);
        }
        String comeFrom = dSDigest.getComeFrom();
        if (comeFrom != null) {
            statement.bindString(9, comeFrom);
        } else {
            statement.bindNull(9);
        }
        String localUrl = dSDigest.getLocalUrl();
        if (localUrl != null) {
            statement.bindString(10, localUrl);
        } else {
            statement.bindNull(10);
        }
        String excerpt = dSDigest.getExcerpt();
        if (excerpt != null) {
            statement.bindString(11, excerpt);
        } else {
            statement.bindNull(11);
        }
        String uniqueFlag = dSDigest.getUniqueFlag();
        if (uniqueFlag != null) {
            statement.bindString(12, uniqueFlag);
        } else {
            statement.bindNull(12);
        }
        String isLoaded = dSDigest.getIsLoaded();
        if (isLoaded != null) {
            statement.bindString(13, isLoaded);
        } else {
            statement.bindNull(13);
        }
        Integer deleted = dSDigest.getDeleted();
        if (deleted != null) {
            statement.bindLong(14, (long) deleted.intValue());
        } else {
            statement.bindNull(14);
        }
        String thumbnailUrl = dSDigest.getThumbnailUrl();
        if (thumbnailUrl != null) {
            statement.bindString(15, thumbnailUrl);
        } else {
            statement.bindNull(15);
        }
        Integer isImgLoaded = dSDigest.getIsImgLoaded();
        if (isImgLoaded != null) {
            statement.bindLong(16, (long) isImgLoaded.intValue());
        } else {
            statement.bindNull(16);
        }
        Integer isUpload = dSDigest.getIsUpload();
        if (isUpload != null) {
            statement.bindLong(17, (long) isUpload.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer isDownload = dSDigest.getIsDownload();
        if (isDownload != null) {
            statement.bindLong(18, (long) isDownload.intValue());
        } else {
            statement.bindNull(18);
        }
        Integer syncCount = dSDigest.getSyncCount();
        if (syncCount != null) {
            statement.bindLong(19, (long) syncCount.intValue());
        } else {
            statement.bindNull(19);
        }
        String extra = dSDigest.getExtra();
        if (extra != null) {
            statement.bindString(20, extra);
        } else {
            statement.bindNull(20);
        }
        String pkgName = dSDigest.getPkgName();
        if (pkgName != null) {
            statement.bindString(21, pkgName);
        } else {
            statement.bindNull(21);
        }
        String source = dSDigest.getSource();
        if (source != null) {
            statement.bindString(22, source);
        } else {
            statement.bindNull(22);
        }
        String sourceTime = dSDigest.getSourceTime();
        if (sourceTime != null) {
            statement.bindString(23, sourceTime);
        } else {
            statement.bindNull(23);
        }
        String params = dSDigest.getParams();
        if (params != null) {
            statement.bindString(24, params);
        } else {
            statement.bindNull(24);
        }
        String isMhtHastitle = dSDigest.getIsMhtHastitle();
        if (isMhtHastitle != null) {
            statement.bindString(25, isMhtHastitle);
        } else {
            statement.bindNull(25);
        }
        String htmlDigest = dSDigest.getHtmlDigest();
        if (htmlDigest != null) {
            statement.bindString(26, htmlDigest);
        } else {
            statement.bindNull(26);
        }
        String reserved0 = dSDigest.getReserved0();
        if (reserved0 != null) {
            statement.bindString(27, reserved0);
        } else {
            statement.bindNull(27);
        }
        String reserved1 = dSDigest.getReserved1();
        if (reserved1 != null) {
            statement.bindString(28, reserved1);
        } else {
            statement.bindNull(28);
        }
        String reserved2 = dSDigest.getReserved2();
        if (reserved2 != null) {
            statement.bindString(29, reserved2);
        } else {
            statement.bindNull(29);
        }
        String reserved3 = dSDigest.getReserved3();
        if (reserved3 != null) {
            statement.bindString(30, reserved3);
        } else {
            statement.bindNull(30);
        }
        String reserved4 = dSDigest.getReserved4();
        if (reserved4 != null) {
            statement.bindString(31, reserved4);
        } else {
            statement.bindNull(31);
        }
        String reserved5 = dSDigest.getReserved5();
        if (reserved5 != null) {
            statement.bindString(32, reserved5);
        } else {
            statement.bindNull(32);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DSDigest readObject(Cursor cursor, int i) {
        return new DSDigest(cursor);
    }

    public void setPrimaryKeyValue(DSDigest dSDigest, long j) {
        dSDigest.setId(Integer.valueOf((int) j));
    }
}
