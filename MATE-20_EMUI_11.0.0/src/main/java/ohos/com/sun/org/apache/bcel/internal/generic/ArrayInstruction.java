package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public abstract class ArrayInstruction extends Instruction implements ExceptionThrower, TypedInstruction {
    ArrayInstruction() {
    }

    protected ArrayInstruction(short s) {
        super(s, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return ExceptionConstants.EXCS_ARRAY_EXCEPTION;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0027  */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        short s = this.opcode;
        switch (s) {
            case 46:
                return Type.INT;
            case 47:
                return Type.LONG;
            case 48:
                return Type.FLOAT;
            case 49:
                return Type.DOUBLE;
            case 50:
                return Type.OBJECT;
            case 51:
                return Type.BYTE;
            case 52:
                return Type.CHAR;
            case 53:
                return Type.SHORT;
            default:
                switch (s) {
                    case 79:
                        break;
                    case 80:
                        break;
                    case 81:
                        break;
                    case 82:
                        break;
                    case 83:
                        break;
                    case 84:
                        break;
                    case 85:
                        break;
                    case 86:
                        break;
                    default:
                        throw new ClassGenException("Oops: unknown case in switch" + ((int) this.opcode));
                }
        }
    }
}
