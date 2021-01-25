package ohos.global.icu.impl;

import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.util.RangeValueIterator;

public class TrieIterator implements RangeValueIterator {
    private static final int BMP_INDEX_LENGTH_ = 2048;
    private static final int DATA_BLOCK_LENGTH_ = 32;
    private static final int LEAD_SURROGATE_MIN_VALUE_ = 55296;
    private static final int TRAIL_SURROGATE_COUNT_ = 1024;
    private static final int TRAIL_SURROGATE_INDEX_BLOCK_LENGTH_ = 32;
    private static final int TRAIL_SURROGATE_MIN_VALUE_ = 56320;
    private int m_currentCodepoint_;
    private int m_initialValue_;
    private int m_nextBlockIndex_;
    private int m_nextBlock_;
    private int m_nextCodepoint_;
    private int m_nextIndex_;
    private int m_nextTrailIndexOffset_;
    private int m_nextValue_;
    private Trie m_trie_;

    /* access modifiers changed from: protected */
    public int extract(int i) {
        return i;
    }

    public TrieIterator(Trie trie) {
        if (trie != null) {
            this.m_trie_ = trie;
            this.m_initialValue_ = extract(this.m_trie_.getInitialValue());
            reset();
            return;
        }
        throw new IllegalArgumentException("Argument trie cannot be null");
    }

    public final boolean next(RangeValueIterator.Element element) {
        int i = this.m_nextCodepoint_;
        if (i > 1114111) {
            return false;
        }
        if (i < 65536 && calculateNextBMPElement(element)) {
            return true;
        }
        calculateNextSupplementaryElement(element);
        return true;
    }

    public final void reset() {
        this.m_currentCodepoint_ = 0;
        this.m_nextCodepoint_ = 0;
        this.m_nextIndex_ = 0;
        this.m_nextBlock_ = this.m_trie_.m_index_[0] << 2;
        if (this.m_nextBlock_ == this.m_trie_.m_dataOffset_) {
            this.m_nextValue_ = this.m_initialValue_;
        } else {
            this.m_nextValue_ = extract(this.m_trie_.getValue(this.m_nextBlock_));
        }
        this.m_nextBlockIndex_ = 0;
        this.m_nextTrailIndexOffset_ = 32;
    }

    private final void setResult(RangeValueIterator.Element element, int i, int i2, int i3) {
        element.start = i;
        element.limit = i2;
        element.value = i3;
    }

    private final boolean calculateNextBMPElement(RangeValueIterator.Element element) {
        int i = this.m_nextValue_;
        int i2 = this.m_nextCodepoint_;
        this.m_currentCodepoint_ = i2;
        this.m_nextCodepoint_ = i2 + 1;
        this.m_nextBlockIndex_++;
        if (!checkBlockDetail(i)) {
            setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
            return true;
        }
        do {
            int i3 = this.m_nextCodepoint_;
            if (i3 < 65536) {
                if (i3 == 55296) {
                    this.m_nextIndex_ = 2048;
                } else if (i3 == 56320) {
                    this.m_nextIndex_ = i3 >> 5;
                } else {
                    this.m_nextIndex_++;
                }
                this.m_nextBlockIndex_ = 0;
            } else {
                this.m_nextCodepoint_ = i3 - 1;
                this.m_nextBlockIndex_--;
                return false;
            }
        } while (checkBlock(i));
        setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
        return true;
    }

    private final void calculateNextSupplementaryElement(RangeValueIterator.Element element) {
        int i = this.m_nextValue_;
        this.m_nextCodepoint_++;
        this.m_nextBlockIndex_++;
        if (UTF16.getTrailSurrogate(this.m_nextCodepoint_) != 56320) {
            if (checkNullNextTrailIndex() || checkBlockDetail(i)) {
                this.m_nextIndex_++;
                this.m_nextTrailIndexOffset_++;
                if (!checkTrailBlock(i)) {
                    setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
                    this.m_currentCodepoint_ = this.m_nextCodepoint_;
                    return;
                }
            } else {
                setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
                this.m_currentCodepoint_ = this.m_nextCodepoint_;
                return;
            }
        }
        int leadSurrogate = UTF16.getLeadSurrogate(this.m_nextCodepoint_);
        while (leadSurrogate < 56320) {
            int i2 = this.m_trie_.m_index_[leadSurrogate >> 5] << 2;
            if (i2 == this.m_trie_.m_dataOffset_) {
                int i3 = this.m_initialValue_;
                if (i != i3) {
                    this.m_nextValue_ = i3;
                    this.m_nextBlock_ = i2;
                    this.m_nextBlockIndex_ = 0;
                    setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
                    this.m_currentCodepoint_ = this.m_nextCodepoint_;
                    return;
                }
                leadSurrogate += 32;
                this.m_nextCodepoint_ = Character.toCodePoint((char) leadSurrogate, UCharacter.MIN_LOW_SURROGATE);
            } else if (this.m_trie_.m_dataManipulate_ != null) {
                this.m_nextIndex_ = this.m_trie_.m_dataManipulate_.getFoldingOffset(this.m_trie_.getValue(i2 + (leadSurrogate & 31)));
                if (this.m_nextIndex_ <= 0) {
                    int i4 = this.m_initialValue_;
                    if (i != i4) {
                        this.m_nextValue_ = i4;
                        this.m_nextBlock_ = this.m_trie_.m_dataOffset_;
                        this.m_nextBlockIndex_ = 0;
                        setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
                        this.m_currentCodepoint_ = this.m_nextCodepoint_;
                        return;
                    }
                    this.m_nextCodepoint_ += 1024;
                } else {
                    this.m_nextTrailIndexOffset_ = 0;
                    if (!checkTrailBlock(i)) {
                        setResult(element, this.m_currentCodepoint_, this.m_nextCodepoint_, i);
                        this.m_currentCodepoint_ = this.m_nextCodepoint_;
                        return;
                    }
                }
                leadSurrogate++;
            } else {
                throw new NullPointerException("The field DataManipulate in this Trie is null");
            }
        }
        setResult(element, this.m_currentCodepoint_, 1114112, i);
    }

    private final boolean checkBlockDetail(int i) {
        while (true) {
            int i2 = this.m_nextBlockIndex_;
            if (i2 >= 32) {
                return true;
            }
            this.m_nextValue_ = extract(this.m_trie_.getValue(this.m_nextBlock_ + i2));
            if (this.m_nextValue_ != i) {
                return false;
            }
            this.m_nextBlockIndex_++;
            this.m_nextCodepoint_++;
        }
    }

    private final boolean checkBlock(int i) {
        int i2 = this.m_nextBlock_;
        this.m_nextBlock_ = this.m_trie_.m_index_[this.m_nextIndex_] << 2;
        if (this.m_nextBlock_ == i2) {
            int i3 = this.m_nextCodepoint_;
            if (i3 - this.m_currentCodepoint_ >= 32) {
                this.m_nextCodepoint_ = i3 + 32;
                return true;
            }
        }
        if (this.m_nextBlock_ == this.m_trie_.m_dataOffset_) {
            int i4 = this.m_initialValue_;
            if (i != i4) {
                this.m_nextValue_ = i4;
                this.m_nextBlockIndex_ = 0;
                return false;
            }
            this.m_nextCodepoint_ += 32;
            return true;
        } else if (!checkBlockDetail(i)) {
            return false;
        } else {
            return true;
        }
    }

    private final boolean checkTrailBlock(int i) {
        while (this.m_nextTrailIndexOffset_ < 32) {
            this.m_nextBlockIndex_ = 0;
            if (!checkBlock(i)) {
                return false;
            }
            this.m_nextTrailIndexOffset_++;
            this.m_nextIndex_++;
        }
        return true;
    }

    private final boolean checkNullNextTrailIndex() {
        if (this.m_nextIndex_ > 0) {
            return false;
        }
        this.m_nextCodepoint_ += UCharacterProperty.MAX_SCRIPT;
        char leadSurrogate = UTF16.getLeadSurrogate(this.m_nextCodepoint_);
        int i = this.m_trie_.m_index_[leadSurrogate >> 5] << 2;
        if (this.m_trie_.m_dataManipulate_ != null) {
            this.m_nextIndex_ = this.m_trie_.m_dataManipulate_.getFoldingOffset(this.m_trie_.getValue(i + (leadSurrogate & 31)));
            this.m_nextIndex_--;
            this.m_nextBlockIndex_ = 32;
            return true;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }
}
