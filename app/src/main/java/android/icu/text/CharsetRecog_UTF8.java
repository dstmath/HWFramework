package android.icu.text;

import dalvik.bytecode.Opcodes;
import org.w3c.dom.traversal.NodeFilter;

class CharsetRecog_UTF8 extends CharsetRecognizer {
    CharsetRecog_UTF8() {
    }

    String getName() {
        return "UTF-8";
    }

    CharsetMatch match(CharsetDetector det) {
        boolean hasBOM = false;
        int numValid = 0;
        int numInvalid = 0;
        byte[] input = det.fRawInput;
        if (det.fRawLength >= 3 && (input[0] & Opcodes.OP_CONST_CLASS_JUMBO) == Opcodes.OP_EXECUTE_INLINE_RANGE && (input[1] & Opcodes.OP_CONST_CLASS_JUMBO) == Opcodes.OP_ADD_LONG_2ADDR && (input[2] & Opcodes.OP_CONST_CLASS_JUMBO) == Opcodes.OP_REM_LONG_2ADDR) {
            hasBOM = true;
        }
        int i = 0;
        while (i < det.fRawLength) {
            int b = input[i];
            if ((b & NodeFilter.SHOW_COMMENT) != 0) {
                int trailBytes;
                if ((b & Opcodes.OP_SHL_INT_LIT8) == Opcodes.OP_AND_LONG_2ADDR) {
                    trailBytes = 1;
                } else if ((b & Opcodes.OP_INVOKE_DIRECT_EMPTY) == Opcodes.OP_SHL_INT_LIT8) {
                    trailBytes = 2;
                } else if ((b & Opcodes.OP_INVOKE_VIRTUAL_QUICK) == Opcodes.OP_INVOKE_DIRECT_EMPTY) {
                    trailBytes = 3;
                } else {
                    numInvalid++;
                }
                do {
                    i++;
                    if (i >= det.fRawLength) {
                        break;
                    } else if ((input[i] & Opcodes.OP_AND_LONG_2ADDR) != NodeFilter.SHOW_COMMENT) {
                        numInvalid++;
                        break;
                    } else {
                        trailBytes--;
                    }
                } while (trailBytes != 0);
                numValid++;
            }
            i++;
        }
        int confidence = 0;
        if (hasBOM && numInvalid == 0) {
            confidence = 100;
        } else if (hasBOM && numValid > numInvalid * 10) {
            confidence = 80;
        } else if (numValid > 3 && numInvalid == 0) {
            confidence = 100;
        } else if (numValid > 0 && numInvalid == 0) {
            confidence = 80;
        } else if (numValid == 0 && numInvalid == 0) {
            confidence = 15;
        } else if (numValid > numInvalid * 10) {
            confidence = 25;
        }
        if (confidence == 0) {
            return null;
        }
        return new CharsetMatch(det, this, confidence);
    }
}
