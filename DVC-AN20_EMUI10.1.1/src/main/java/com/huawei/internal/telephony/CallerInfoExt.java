package com.huawei.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.android.internal.telephony.CallerInfo;

public class CallerInfoExt {
    public static final long USER_TYPE_CURRENT = 0;
    public static final long USER_TYPE_WORK = 1;
    private CallerInfo mCallerInfo = null;

    public CallerInfoExt() {
        if (this.mCallerInfo == null) {
            this.mCallerInfo = new CallerInfo();
        }
    }

    public CallerInfoExt(CallerInfo info) {
        if (this.mCallerInfo == null) {
            this.mCallerInfo = info;
        }
    }

    public static CallerInfoExt getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        return new CallerInfoExt(CallerInfo.getCallerInfo(context, contactRef, cursor));
    }

    public CallerInfo getCallerInfoValue() {
        return this.mCallerInfo;
    }

    public void setName(String name) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.name = name;
        }
    }

    public String getName() {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            return callerInfo.name;
        }
        return null;
    }

    public void setPhoneNumber(String phoneNumber) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.phoneNumber = phoneNumber;
        }
    }

    public String getPhoneNumber() {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            return callerInfo.phoneNumber;
        }
        return null;
    }

    public void setNormalizedNumber(String normalizedNumber) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.normalizedNumber = normalizedNumber;
        }
    }

    public void setContactExists(boolean contactExists) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.contactExists = contactExists;
        }
    }

    public void setPhoneLabel(String phoneLabel) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.phoneLabel = phoneLabel;
        }
    }

    public void setNumberType(int numberType) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.numberType = numberType;
        }
    }

    public void setNumberLabel(String numberLabel) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.numberLabel = numberLabel;
        }
    }

    public int getNumberType() {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            return callerInfo.numberType;
        }
        return 0;
    }

    public String getNumberLabel() {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            return callerInfo.numberLabel;
        }
        return null;
    }

    public void setPhotoResource(int photoResource) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.photoResource = photoResource;
        }
    }

    public void setContactIdOrZero(long contactIdOrZero) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.contactIdOrZero = contactIdOrZero;
        }
    }

    public void setNeedUpdate(boolean needUpdate) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.needUpdate = needUpdate;
        }
    }

    public void setContactRefUri(Uri contactRefUri) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.contactRefUri = contactRefUri;
        }
    }

    public void setContactRingtoneUri(Uri contactRingtoneUri) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.contactRingtoneUri = contactRingtoneUri;
        }
    }

    public void setShouldSendToVoicemail(boolean shouldSendToVoicemail) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.shouldSendToVoicemail = shouldSendToVoicemail;
        }
    }

    public void setCachedPhoto(Drawable cachedPhoto) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.cachedPhoto = cachedPhoto;
        }
    }

    public void setCachedPhotoCurrent(boolean cachedPhotoCurrent) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.isCachedPhotoCurrent = cachedPhotoCurrent;
        }
    }

    public void setVoipDeviceType(int voipDeviceType) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.mVoipDeviceType = voipDeviceType;
        }
    }

    public void setUserType(long userType) {
        CallerInfo callerInfo = this.mCallerInfo;
        if (callerInfo != null) {
            callerInfo.userType = userType;
        }
    }
}
