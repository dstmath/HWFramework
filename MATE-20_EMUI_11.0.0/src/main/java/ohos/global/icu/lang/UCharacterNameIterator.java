package ohos.global.icu.lang;

import ohos.global.icu.impl.UCharacterName;
import ohos.global.icu.util.ValueIterator;

class UCharacterNameIterator implements ValueIterator {
    private static char[] GROUP_LENGTHS_ = new char[33];
    private static char[] GROUP_OFFSETS_ = new char[33];
    private int m_algorithmIndex_ = -1;
    private int m_choice_;
    private int m_current_;
    private int m_groupIndex_ = -1;
    private int m_limit_;
    private UCharacterName m_name_;
    private int m_start_;

    public boolean next(ValueIterator.Element element) {
        int algorithmLength;
        if (this.m_current_ >= this.m_limit_) {
            return false;
        }
        int i = this.m_choice_;
        if ((i == 0 || i == 2) && this.m_algorithmIndex_ < (algorithmLength = this.m_name_.getAlgorithmLength())) {
            while (true) {
                int i2 = this.m_algorithmIndex_;
                if (i2 >= algorithmLength || (i2 >= 0 && this.m_name_.getAlgorithmEnd(i2) >= this.m_current_)) {
                    break;
                }
                this.m_algorithmIndex_++;
            }
            int i3 = this.m_algorithmIndex_;
            if (i3 < algorithmLength) {
                int algorithmStart = this.m_name_.getAlgorithmStart(i3);
                if (this.m_current_ < algorithmStart) {
                    int i4 = this.m_limit_;
                    if (i4 <= algorithmStart) {
                        algorithmStart = i4;
                    }
                    if (!iterateGroup(element, algorithmStart)) {
                        this.m_current_++;
                        return true;
                    }
                }
                int i5 = this.m_current_;
                if (i5 >= this.m_limit_) {
                    return false;
                }
                element.integer = i5;
                element.value = this.m_name_.getAlgorithmName(this.m_algorithmIndex_, i5);
                this.m_groupIndex_ = -1;
                this.m_current_++;
                return true;
            }
        }
        if (!iterateGroup(element, this.m_limit_)) {
            this.m_current_++;
            return true;
        } else if (this.m_choice_ != 2 || iterateExtended(element, this.m_limit_)) {
            return false;
        } else {
            this.m_current_++;
            return true;
        }
    }

    public void reset() {
        this.m_current_ = this.m_start_;
        this.m_groupIndex_ = -1;
        this.m_algorithmIndex_ = -1;
    }

    public void setRange(int i, int i2) {
        if (i < i2) {
            if (i < 0) {
                this.m_start_ = 0;
            } else {
                this.m_start_ = i;
            }
            if (i2 > 1114112) {
                this.m_limit_ = 1114112;
            } else {
                this.m_limit_ = i2;
            }
            this.m_current_ = this.m_start_;
            return;
        }
        throw new IllegalArgumentException("start or limit has to be valid Unicode codepoints and start < limit");
    }

    protected UCharacterNameIterator(UCharacterName uCharacterName, int i) {
        if (uCharacterName != null) {
            this.m_name_ = uCharacterName;
            this.m_choice_ = i;
            this.m_start_ = 0;
            this.m_limit_ = 1114112;
            this.m_current_ = this.m_start_;
            return;
        }
        throw new IllegalArgumentException("UCharacterName name argument cannot be null. Missing unames.icu?");
    }

    private boolean iterateSingleGroup(ValueIterator.Element element, int i) {
        synchronized (GROUP_OFFSETS_) {
            synchronized (GROUP_LENGTHS_) {
                int groupLengths = this.m_name_.getGroupLengths(this.m_groupIndex_, GROUP_OFFSETS_, GROUP_LENGTHS_);
                while (this.m_current_ < i) {
                    int groupOffset = UCharacterName.getGroupOffset(this.m_current_);
                    String groupName = this.m_name_.getGroupName(GROUP_OFFSETS_[groupOffset] + groupLengths, GROUP_LENGTHS_[groupOffset], this.m_choice_);
                    if ((groupName == null || groupName.length() == 0) && this.m_choice_ == 2) {
                        groupName = this.m_name_.getExtendedName(this.m_current_);
                    }
                    if (groupName == null || groupName.length() <= 0) {
                        this.m_current_++;
                    } else {
                        element.integer = this.m_current_;
                        element.value = groupName;
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private boolean iterateGroup(ValueIterator.Element element, int i) {
        int i2;
        if (this.m_groupIndex_ < 0) {
            this.m_groupIndex_ = this.m_name_.getGroup(this.m_current_);
        }
        while (this.m_groupIndex_ < this.m_name_.m_groupcount_ && (i2 = this.m_current_) < i) {
            int codepointMSB = UCharacterName.getCodepointMSB(i2);
            int groupMSB = this.m_name_.getGroupMSB(this.m_groupIndex_);
            if (codepointMSB == groupMSB) {
                if (codepointMSB == UCharacterName.getCodepointMSB(i - 1)) {
                    return iterateSingleGroup(element, i);
                }
                if (!iterateSingleGroup(element, UCharacterName.getGroupLimit(groupMSB))) {
                    return false;
                }
                this.m_groupIndex_++;
            } else if (codepointMSB > groupMSB) {
                this.m_groupIndex_++;
            } else {
                int groupMin = UCharacterName.getGroupMin(groupMSB);
                if (groupMin > i) {
                    groupMin = i;
                }
                if (this.m_choice_ == 2 && !iterateExtended(element, groupMin)) {
                    return false;
                }
                this.m_current_ = groupMin;
            }
        }
        return true;
    }

    private boolean iterateExtended(ValueIterator.Element element, int i) {
        while (true) {
            int i2 = this.m_current_;
            if (i2 >= i) {
                return true;
            }
            String extendedOr10Name = this.m_name_.getExtendedOr10Name(i2);
            if (extendedOr10Name == null || extendedOr10Name.length() <= 0) {
                this.m_current_++;
            } else {
                element.integer = this.m_current_;
                element.value = extendedOr10Name;
                return false;
            }
        }
    }
}
