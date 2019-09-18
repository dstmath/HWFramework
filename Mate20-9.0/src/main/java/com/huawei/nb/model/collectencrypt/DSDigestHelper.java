package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSDigestHelper extends AEntityHelper<DSDigest> {
    private static final DSDigestHelper INSTANCE = new DSDigestHelper();

    private DSDigestHelper() {
    }

    public static DSDigestHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSDigest object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String title = object.getTitle();
        if (title != null) {
            statement.bindString(2, title);
        } else {
            statement.bindNull(2);
        }
        String pageUrl = object.getPageUrl();
        if (pageUrl != null) {
            statement.bindString(3, pageUrl);
        } else {
            statement.bindNull(3);
        }
        String serverId = object.getServerId();
        if (serverId != null) {
            statement.bindString(4, serverId);
        } else {
            statement.bindNull(4);
        }
        String mhtUtl = object.getMhtUtl();
        if (mhtUtl != null) {
            statement.bindString(5, mhtUtl);
        } else {
            statement.bindNull(5);
        }
        String thumbnail = object.getThumbnail();
        if (thumbnail != null) {
            statement.bindString(6, thumbnail);
        } else {
            statement.bindNull(6);
        }
        String htmlPath = object.getHtmlPath();
        if (htmlPath != null) {
            statement.bindString(7, htmlPath);
        } else {
            statement.bindNull(7);
        }
        String createdtime = object.getCreatedtime();
        if (createdtime != null) {
            statement.bindString(8, createdtime);
        } else {
            statement.bindNull(8);
        }
        String comeFrom = object.getComeFrom();
        if (comeFrom != null) {
            statement.bindString(9, comeFrom);
        } else {
            statement.bindNull(9);
        }
        String localUrl = object.getLocalUrl();
        if (localUrl != null) {
            statement.bindString(10, localUrl);
        } else {
            statement.bindNull(10);
        }
        String excerpt = object.getExcerpt();
        if (excerpt != null) {
            statement.bindString(11, excerpt);
        } else {
            statement.bindNull(11);
        }
        String uniqueFlag = object.getUniqueFlag();
        if (uniqueFlag != null) {
            statement.bindString(12, uniqueFlag);
        } else {
            statement.bindNull(12);
        }
        String isLoaded = object.getIsLoaded();
        if (isLoaded != null) {
            statement.bindString(13, isLoaded);
        } else {
            statement.bindNull(13);
        }
        Integer deleted = object.getDeleted();
        if (deleted != null) {
            statement.bindLong(14, (long) deleted.intValue());
        } else {
            statement.bindNull(14);
        }
        String thumbnailUrl = object.getThumbnailUrl();
        if (thumbnailUrl != null) {
            statement.bindString(15, thumbnailUrl);
        } else {
            statement.bindNull(15);
        }
        Integer isImgLoaded = object.getIsImgLoaded();
        if (isImgLoaded != null) {
            statement.bindLong(16, (long) isImgLoaded.intValue());
        } else {
            statement.bindNull(16);
        }
        Integer isUpload = object.getIsUpload();
        if (isUpload != null) {
            statement.bindLong(17, (long) isUpload.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer isDownload = object.getIsDownload();
        if (isDownload != null) {
            statement.bindLong(18, (long) isDownload.intValue());
        } else {
            statement.bindNull(18);
        }
        Integer syncCount = object.getSyncCount();
        if (syncCount != null) {
            statement.bindLong(19, (long) syncCount.intValue());
        } else {
            statement.bindNull(19);
        }
        String extra = object.getExtra();
        if (extra != null) {
            statement.bindString(20, extra);
        } else {
            statement.bindNull(20);
        }
        String pkgName = object.getPkgName();
        if (pkgName != null) {
            statement.bindString(21, pkgName);
        } else {
            statement.bindNull(21);
        }
        String source = object.getSource();
        if (source != null) {
            statement.bindString(22, source);
        } else {
            statement.bindNull(22);
        }
        String sourceTime = object.getSourceTime();
        if (sourceTime != null) {
            statement.bindString(23, sourceTime);
        } else {
            statement.bindNull(23);
        }
        String params = object.getParams();
        if (params != null) {
            statement.bindString(24, params);
        } else {
            statement.bindNull(24);
        }
        String isMhtHastitle = object.getIsMhtHastitle();
        if (isMhtHastitle != null) {
            statement.bindString(25, isMhtHastitle);
        } else {
            statement.bindNull(25);
        }
        String htmlDigest = object.getHtmlDigest();
        if (htmlDigest != null) {
            statement.bindString(26, htmlDigest);
        } else {
            statement.bindNull(26);
        }
        String reserved0 = object.getReserved0();
        if (reserved0 != null) {
            statement.bindString(27, reserved0);
        } else {
            statement.bindNull(27);
        }
        String reserved1 = object.getReserved1();
        if (reserved1 != null) {
            statement.bindString(28, reserved1);
        } else {
            statement.bindNull(28);
        }
        String reserved2 = object.getReserved2();
        if (reserved2 != null) {
            statement.bindString(29, reserved2);
        } else {
            statement.bindNull(29);
        }
        String reserved3 = object.getReserved3();
        if (reserved3 != null) {
            statement.bindString(30, reserved3);
        } else {
            statement.bindNull(30);
        }
        String reserved4 = object.getReserved4();
        if (reserved4 != null) {
            statement.bindString(31, reserved4);
        } else {
            statement.bindNull(31);
        }
        String reserved5 = object.getReserved5();
        if (reserved5 != null) {
            statement.bindString(32, reserved5);
        } else {
            statement.bindNull(32);
        }
    }

    public DSDigest readObject(Cursor cursor, int offset) {
        return new DSDigest(cursor);
    }

    public void setPrimaryKeyValue(DSDigest object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSDigest object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
