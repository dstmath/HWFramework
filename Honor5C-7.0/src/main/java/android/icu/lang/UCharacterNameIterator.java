package android.icu.lang;

import android.icu.impl.PropsVectors;
import android.icu.impl.UCharacterName;
import android.icu.util.ValueIterator;
import android.icu.util.ValueIterator.Element;

class UCharacterNameIterator implements ValueIterator {
    private static char[] GROUP_LENGTHS_;
    private static char[] GROUP_OFFSETS_;
    private int m_algorithmIndex_;
    private int m_choice_;
    private int m_current_;
    private int m_groupIndex_;
    private int m_limit_;
    private UCharacterName m_name_;
    private int m_start_;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacterNameIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UCharacterNameIterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacterNameIterator.<clinit>():void");
    }

    public boolean next(Element element) {
        if (this.m_current_ >= this.m_limit_) {
            return false;
        }
        if (this.m_choice_ == 0 || this.m_choice_ == 2) {
            int length = this.m_name_.getAlgorithmLength();
            if (this.m_algorithmIndex_ < length) {
                while (this.m_algorithmIndex_ < length && (this.m_algorithmIndex_ < 0 || this.m_name_.getAlgorithmEnd(this.m_algorithmIndex_) < this.m_current_)) {
                    this.m_algorithmIndex_++;
                }
                if (this.m_algorithmIndex_ < length) {
                    int start = this.m_name_.getAlgorithmStart(this.m_algorithmIndex_);
                    if (this.m_current_ < start) {
                        int end = start;
                        if (this.m_limit_ <= start) {
                            end = this.m_limit_;
                        }
                        if (!iterateGroup(element, end)) {
                            this.m_current_++;
                            return true;
                        }
                    }
                    if (this.m_current_ >= this.m_limit_) {
                        return false;
                    }
                    element.integer = this.m_current_;
                    element.value = this.m_name_.getAlgorithmName(this.m_algorithmIndex_, this.m_current_);
                    this.m_groupIndex_ = -1;
                    this.m_current_++;
                    return true;
                }
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

    public void setRange(int start, int limit) {
        if (start >= limit) {
            throw new IllegalArgumentException("start or limit has to be valid Unicode codepoints and start < limit");
        }
        if (start < 0) {
            this.m_start_ = 0;
        } else {
            this.m_start_ = start;
        }
        if (limit > PropsVectors.INITIAL_VALUE_CP) {
            this.m_limit_ = PropsVectors.INITIAL_VALUE_CP;
        } else {
            this.m_limit_ = limit;
        }
        this.m_current_ = this.m_start_;
    }

    protected UCharacterNameIterator(UCharacterName name, int choice) {
        this.m_groupIndex_ = -1;
        this.m_algorithmIndex_ = -1;
        if (name == null) {
            throw new IllegalArgumentException("UCharacterName name argument cannot be null. Missing unames.icu?");
        }
        this.m_name_ = name;
        this.m_choice_ = choice;
        this.m_start_ = 0;
        this.m_limit_ = PropsVectors.INITIAL_VALUE_CP;
        this.m_current_ = this.m_start_;
    }

    private boolean iterateSingleGroup(Element result, int limit) {
        synchronized (GROUP_OFFSETS_) {
            synchronized (GROUP_LENGTHS_) {
                int index = this.m_name_.getGroupLengths(this.m_groupIndex_, GROUP_OFFSETS_, GROUP_LENGTHS_);
                while (this.m_current_ < limit) {
                    int offset = UCharacterName.getGroupOffset(this.m_current_);
                    String name = this.m_name_.getGroupName(GROUP_OFFSETS_[offset] + index, GROUP_LENGTHS_[offset], this.m_choice_);
                    if ((name == null || name.length() == 0) && this.m_choice_ == 2) {
                        name = this.m_name_.getExtendedName(this.m_current_);
                    }
                    if (name == null || name.length() <= 0) {
                        this.m_current_++;
                    } else {
                        result.integer = this.m_current_;
                        result.value = name;
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private boolean iterateGroup(Element result, int limit) {
        if (this.m_groupIndex_ < 0) {
            this.m_groupIndex_ = this.m_name_.getGroup(this.m_current_);
        }
        while (this.m_groupIndex_ < this.m_name_.m_groupcount_ && this.m_current_ < limit) {
            int startMSB = UCharacterName.getCodepointMSB(this.m_current_);
            int gMSB = this.m_name_.getGroupMSB(this.m_groupIndex_);
            if (startMSB == gMSB) {
                if (startMSB == UCharacterName.getCodepointMSB(limit - 1)) {
                    return iterateSingleGroup(result, limit);
                }
                if (!iterateSingleGroup(result, UCharacterName.getGroupLimit(gMSB))) {
                    return false;
                }
                this.m_groupIndex_++;
            } else if (startMSB > gMSB) {
                this.m_groupIndex_++;
            } else {
                int gMIN = UCharacterName.getGroupMin(gMSB);
                if (gMIN > limit) {
                    gMIN = limit;
                }
                if (this.m_choice_ == 2 && !iterateExtended(result, gMIN)) {
                    return false;
                }
                this.m_current_ = gMIN;
            }
        }
        return true;
    }

    private boolean iterateExtended(Element result, int limit) {
        while (this.m_current_ < limit) {
            String name = this.m_name_.getExtendedOr10Name(this.m_current_);
            if (name == null || name.length() <= 0) {
                this.m_current_++;
            } else {
                result.integer = this.m_current_;
                result.value = name;
                return false;
            }
        }
        return true;
    }
}
