package android.drm;

import java.util.ArrayList;
import java.util.Iterator;

public class DrmSupportInfo {
    private String mDescription = "";
    private final ArrayList<String> mFileSuffixList = new ArrayList<>();
    private final ArrayList<String> mMimeTypeList = new ArrayList<>();

    public void addMimeType(String mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("mimeType is null");
        } else if (mimeType != "") {
            this.mMimeTypeList.add(mimeType);
        } else {
            throw new IllegalArgumentException("mimeType is an empty string");
        }
    }

    public void addFileSuffix(String fileSuffix) {
        if (fileSuffix != "") {
            this.mFileSuffixList.add(fileSuffix);
            return;
        }
        throw new IllegalArgumentException("fileSuffix is an empty string");
    }

    public Iterator<String> getMimeTypeIterator() {
        return this.mMimeTypeList.iterator();
    }

    public Iterator<String> getFileSuffixIterator() {
        return this.mFileSuffixList.iterator();
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("description is null");
        } else if (description != "") {
            this.mDescription = description;
        } else {
            throw new IllegalArgumentException("description is an empty string");
        }
    }

    public String getDescriprition() {
        return this.mDescription;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public int hashCode() {
        return this.mFileSuffixList.hashCode() + this.mMimeTypeList.hashCode() + this.mDescription.hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof DrmSupportInfo)) {
            return false;
        }
        DrmSupportInfo info = (DrmSupportInfo) object;
        if (!this.mFileSuffixList.equals(info.mFileSuffixList) || !this.mMimeTypeList.equals(info.mMimeTypeList) || !this.mDescription.equals(info.mDescription)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isSupportedMimeType(String mimeType) {
        if (mimeType == null || mimeType.equals("")) {
            return false;
        }
        for (int i = 0; i < this.mMimeTypeList.size(); i++) {
            if (this.mMimeTypeList.get(i).startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSupportedFileSuffix(String fileSuffix) {
        return this.mFileSuffixList.contains(fileSuffix);
    }
}
