package android.icu.util;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public final class StringTokenizer implements Enumeration<Object> {
    private static final UnicodeSet DEFAULT_DELIMITERS_ = new UnicodeSet(9, 10, 12, 13, 32, 32);
    private static final UnicodeSet EMPTY_DELIMITER_ = UnicodeSet.EMPTY;
    private static final int TOKEN_SIZE_ = 100;
    private boolean[] delims;
    private boolean m_coalesceDelimiters_;
    private UnicodeSet m_delimiters_;
    private int m_length_;
    private int m_nextOffset_;
    private boolean m_returnDelimiters_;
    private String m_source_;
    private int[] m_tokenLimit_;
    private int m_tokenOffset_;
    private int m_tokenSize_;
    private int[] m_tokenStart_;

    public StringTokenizer(String str, UnicodeSet delim, boolean returndelims) {
        this(str, delim, returndelims, false);
    }

    @Deprecated
    public StringTokenizer(String str, UnicodeSet delim, boolean returndelims, boolean coalescedelims) {
        this.m_source_ = str;
        this.m_length_ = str.length();
        if (delim == null) {
            this.m_delimiters_ = EMPTY_DELIMITER_;
        } else {
            this.m_delimiters_ = delim;
        }
        this.m_returnDelimiters_ = returndelims;
        this.m_coalesceDelimiters_ = coalescedelims;
        this.m_tokenOffset_ = -1;
        this.m_tokenSize_ = -1;
        if (this.m_length_ == 0) {
            this.m_nextOffset_ = -1;
            return;
        }
        this.m_nextOffset_ = 0;
        if (!returndelims) {
            this.m_nextOffset_ = getNextNonDelimiter(0);
        }
    }

    public StringTokenizer(String str, UnicodeSet delim) {
        this(str, delim, false, false);
    }

    public StringTokenizer(String str, String delim, boolean returndelims) {
        this(str, delim, returndelims, false);
    }

    @Deprecated
    public StringTokenizer(String str, String delim, boolean returndelims, boolean coalescedelims) {
        this.m_delimiters_ = EMPTY_DELIMITER_;
        if (delim != null && delim.length() > 0) {
            this.m_delimiters_ = new UnicodeSet();
            this.m_delimiters_.addAll((CharSequence) delim);
            checkDelimiters();
        }
        this.m_coalesceDelimiters_ = coalescedelims;
        this.m_source_ = str;
        this.m_length_ = str.length();
        this.m_returnDelimiters_ = returndelims;
        this.m_tokenOffset_ = -1;
        this.m_tokenSize_ = -1;
        if (this.m_length_ == 0) {
            this.m_nextOffset_ = -1;
            return;
        }
        this.m_nextOffset_ = 0;
        if (!returndelims) {
            this.m_nextOffset_ = getNextNonDelimiter(0);
        }
    }

    public StringTokenizer(String str, String delim) {
        this(str, delim, false, false);
    }

    public StringTokenizer(String str) {
        this(str, DEFAULT_DELIMITERS_, false, false);
    }

    public boolean hasMoreTokens() {
        return this.m_nextOffset_ >= 0;
    }

    public String nextToken() {
        String result;
        if (this.m_tokenOffset_ < 0) {
            int tokenlimit;
            if (this.m_nextOffset_ < 0) {
                throw new NoSuchElementException("No more tokens in String");
            } else if (this.m_returnDelimiters_) {
                int c = UTF16.charAt(this.m_source_, this.m_nextOffset_);
                boolean contains = this.delims == null ? this.m_delimiters_.contains(c) : c < this.delims.length ? this.delims[c] : false;
                if (!contains) {
                    tokenlimit = getNextDelimiter(this.m_nextOffset_);
                } else if (this.m_coalesceDelimiters_) {
                    tokenlimit = getNextNonDelimiter(this.m_nextOffset_);
                } else {
                    tokenlimit = this.m_nextOffset_ + UTF16.getCharCount(c);
                    if (tokenlimit == this.m_length_) {
                        tokenlimit = -1;
                    }
                }
                if (tokenlimit < 0) {
                    result = this.m_source_.substring(this.m_nextOffset_);
                } else {
                    result = this.m_source_.substring(this.m_nextOffset_, tokenlimit);
                }
                this.m_nextOffset_ = tokenlimit;
                return result;
            } else {
                tokenlimit = getNextDelimiter(this.m_nextOffset_);
                if (tokenlimit < 0) {
                    result = this.m_source_.substring(this.m_nextOffset_);
                    this.m_nextOffset_ = tokenlimit;
                } else {
                    result = this.m_source_.substring(this.m_nextOffset_, tokenlimit);
                    this.m_nextOffset_ = getNextNonDelimiter(tokenlimit);
                }
                return result;
            }
        } else if (this.m_tokenOffset_ >= this.m_tokenSize_) {
            throw new NoSuchElementException("No more tokens in String");
        } else {
            if (this.m_tokenLimit_[this.m_tokenOffset_] >= 0) {
                result = this.m_source_.substring(this.m_tokenStart_[this.m_tokenOffset_], this.m_tokenLimit_[this.m_tokenOffset_]);
            } else {
                result = this.m_source_.substring(this.m_tokenStart_[this.m_tokenOffset_]);
            }
            this.m_tokenOffset_++;
            this.m_nextOffset_ = -1;
            if (this.m_tokenOffset_ < this.m_tokenSize_) {
                this.m_nextOffset_ = this.m_tokenStart_[this.m_tokenOffset_];
            }
            return result;
        }
    }

    public String nextToken(String delim) {
        this.m_delimiters_ = EMPTY_DELIMITER_;
        if (delim != null && delim.length() > 0) {
            this.m_delimiters_ = new UnicodeSet();
            this.m_delimiters_.addAll((CharSequence) delim);
        }
        return nextToken(this.m_delimiters_);
    }

    public String nextToken(UnicodeSet delim) {
        this.m_delimiters_ = delim;
        checkDelimiters();
        this.m_tokenOffset_ = -1;
        this.m_tokenSize_ = -1;
        if (!this.m_returnDelimiters_) {
            this.m_nextOffset_ = getNextNonDelimiter(this.m_nextOffset_);
        }
        return nextToken();
    }

    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    public Object nextElement() {
        return nextToken();
    }

    public int countTokens() {
        int result = 0;
        if (hasMoreTokens()) {
            if (this.m_tokenOffset_ >= 0) {
                return this.m_tokenSize_ - this.m_tokenOffset_;
            }
            if (this.m_tokenStart_ == null) {
                this.m_tokenStart_ = new int[100];
                this.m_tokenLimit_ = new int[100];
            }
            do {
                if (this.m_tokenStart_.length == result) {
                    int[] temptokenindex = this.m_tokenStart_;
                    int[] temptokensize = this.m_tokenLimit_;
                    int originalsize = temptokenindex.length;
                    int newsize = originalsize + 100;
                    this.m_tokenStart_ = new int[newsize];
                    this.m_tokenLimit_ = new int[newsize];
                    System.arraycopy(temptokenindex, 0, this.m_tokenStart_, 0, originalsize);
                    System.arraycopy(temptokensize, 0, this.m_tokenLimit_, 0, originalsize);
                }
                this.m_tokenStart_[result] = this.m_nextOffset_;
                if (this.m_returnDelimiters_) {
                    int c = UTF16.charAt(this.m_source_, this.m_nextOffset_);
                    boolean contains = this.delims == null ? this.m_delimiters_.contains(c) : c < this.delims.length ? this.delims[c] : false;
                    if (!contains) {
                        this.m_tokenLimit_[result] = getNextDelimiter(this.m_nextOffset_);
                    } else if (this.m_coalesceDelimiters_) {
                        this.m_tokenLimit_[result] = getNextNonDelimiter(this.m_nextOffset_);
                    } else {
                        int p = this.m_nextOffset_ + 1;
                        if (p == this.m_length_) {
                            p = -1;
                        }
                        this.m_tokenLimit_[result] = p;
                    }
                    this.m_nextOffset_ = this.m_tokenLimit_[result];
                } else {
                    this.m_tokenLimit_[result] = getNextDelimiter(this.m_nextOffset_);
                    this.m_nextOffset_ = getNextNonDelimiter(this.m_tokenLimit_[result]);
                }
                result++;
            } while (this.m_nextOffset_ >= 0);
            this.m_tokenOffset_ = 0;
            this.m_tokenSize_ = result;
            this.m_nextOffset_ = this.m_tokenStart_[0];
        }
        return result;
    }

    private int getNextDelimiter(int offset) {
        if (offset >= 0) {
            int result = offset;
            if (this.delims != null) {
                do {
                    int c = UTF16.charAt(this.m_source_, result);
                    if (c < this.delims.length && this.delims[c]) {
                        break;
                    }
                    result++;
                } while (result < this.m_length_);
            } else {
                while (true) {
                    if (!this.m_delimiters_.contains(UTF16.charAt(this.m_source_, result))) {
                        result++;
                        if (result >= this.m_length_) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (result < this.m_length_) {
                return result;
            }
        }
        return -1 - this.m_length_;
    }

    private int getNextNonDelimiter(int offset) {
        if (offset >= 0) {
            int result = offset;
            if (this.delims == null) {
                while (true) {
                    if (!this.m_delimiters_.contains(UTF16.charAt(this.m_source_, result))) {
                        break;
                    }
                    result++;
                    if (result >= this.m_length_) {
                        break;
                    }
                }
            } else {
                do {
                    int c = UTF16.charAt(this.m_source_, result);
                    if (!(c < this.delims.length ? this.delims[c] : false)) {
                        break;
                    }
                    result++;
                } while (result < this.m_length_);
            }
            if (result < this.m_length_) {
                return result;
            }
        }
        return -1 - this.m_length_;
    }

    void checkDelimiters() {
        if (this.m_delimiters_ == null || this.m_delimiters_.size() == 0) {
            this.delims = new boolean[0];
            return;
        }
        int maxChar = this.m_delimiters_.getRangeEnd(this.m_delimiters_.getRangeCount() - 1);
        if (maxChar < 127) {
            this.delims = new boolean[(maxChar + 1)];
            int i = 0;
            while (true) {
                int ch = this.m_delimiters_.charAt(i);
                if (-1 != ch) {
                    this.delims[ch] = true;
                    i++;
                } else {
                    return;
                }
            }
        }
        this.delims = null;
    }
}
