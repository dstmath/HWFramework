package com.android.server.wifi;

public class HwCHRWifiBCMCounter {
    private String mName;
    private long mValue;

    public HwCHRWifiBCMCounter(String name, long value) {
        this.mName = name;
        this.mValue = value;
    }

    public String getName() {
        return this.mName;
    }

    public long getValue() {
        return this.mValue;
    }

    public int hashCode() {
        return (this.mName == null ? 0 : this.mName.hashCode()) + 31;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HwCHRWifiBCMCounter other = (HwCHRWifiBCMCounter) obj;
        if (this.mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!this.mName.equals(other.mName)) {
            return false;
        }
        return true;
    }
}
