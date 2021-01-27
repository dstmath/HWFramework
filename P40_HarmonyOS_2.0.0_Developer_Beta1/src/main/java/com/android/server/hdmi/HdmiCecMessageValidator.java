package com.android.server.hdmi;

import android.util.SparseArray;
import com.android.server.display.FoldPolicy;
import com.android.server.display.color.DisplayTransformManager;

public class HdmiCecMessageValidator {
    private static final int DEST_ALL = 3;
    private static final int DEST_BROADCAST = 2;
    private static final int DEST_DIRECT = 1;
    static final int ERROR_DESTINATION = 2;
    static final int ERROR_PARAMETER = 3;
    static final int ERROR_PARAMETER_SHORT = 4;
    static final int ERROR_SOURCE = 1;
    static final int OK = 0;
    private static final int SRC_UNREGISTERED = 4;
    private static final String TAG = "HdmiCecMessageValidator";
    private final HdmiControlService mService;
    final SparseArray<ValidationInfo> mValidationInfo = new SparseArray<>();

    /* access modifiers changed from: package-private */
    public interface ParameterValidator {
        int isValid(byte[] bArr);
    }

    /* access modifiers changed from: private */
    public static class ValidationInfo {
        public final int addressType;
        public final ParameterValidator parameterValidator;

        public ValidationInfo(ParameterValidator validator, int type) {
            this.parameterValidator = validator;
            this.addressType = type;
        }
    }

    public HdmiCecMessageValidator(HdmiControlService service) {
        this.mService = service;
        PhysicalAddressValidator physicalAddressValidator = new PhysicalAddressValidator();
        addValidationInfo(130, physicalAddressValidator, 6);
        addValidationInfo(157, physicalAddressValidator, 1);
        addValidationInfo(132, new ReportPhysicalAddressValidator(), 6);
        addValidationInfo(128, new RoutingChangeValidator(), 6);
        addValidationInfo(129, physicalAddressValidator, 6);
        addValidationInfo(134, physicalAddressValidator, 2);
        addValidationInfo(112, new SystemAudioModeRequestValidator(), 1);
        FixedLengthValidator noneValidator = new FixedLengthValidator(0);
        addValidationInfo(255, noneValidator, 1);
        addValidationInfo(159, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2, noneValidator, 5);
        addValidationInfo(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, noneValidator, 1);
        addValidationInfo(143, noneValidator, 1);
        addValidationInfo(140, noneValidator, 5);
        addValidationInfo(70, noneValidator, 1);
        addValidationInfo(131, noneValidator, 5);
        addValidationInfo(DisplayTransformManager.LEVEL_COLOR_MATRIX_DISPLAY_WHITE_BALANCE, noneValidator, 1);
        addValidationInfo(4, noneValidator, 1);
        addValidationInfo(192, noneValidator, 1);
        addValidationInfo(11, noneValidator, 1);
        addValidationInfo(15, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, noneValidator, 1);
        addValidationInfo(196, noneValidator, 1);
        addValidationInfo(133, noneValidator, 6);
        addValidationInfo(54, noneValidator, 7);
        addValidationInfo(197, noneValidator, 1);
        addValidationInfo(13, noneValidator, 1);
        addValidationInfo(6, noneValidator, 1);
        addValidationInfo(5, noneValidator, 1);
        addValidationInfo(69, noneValidator, 1);
        addValidationInfo(139, noneValidator, 3);
        FixedLengthValidator oneByteValidator = new FixedLengthValidator(1);
        addValidationInfo(9, new VariableLengthValidator(1, 8), 1);
        addValidationInfo(10, oneByteValidator, 1);
        addValidationInfo(158, oneByteValidator, 1);
        addValidationInfo(50, new FixedLengthValidator(3), 2);
        VariableLengthValidator maxLengthValidator = new VariableLengthValidator(0, 14);
        addValidationInfo(135, new FixedLengthValidator(3), 2);
        addValidationInfo(137, new VariableLengthValidator(1, 14), 5);
        addValidationInfo(160, new VariableLengthValidator(4, 14), 7);
        addValidationInfo(138, maxLengthValidator, 7);
        addValidationInfo(100, maxLengthValidator, 1);
        addValidationInfo(71, maxLengthValidator, 1);
        addValidationInfo(141, oneByteValidator, 1);
        addValidationInfo(142, oneByteValidator, 1);
        addValidationInfo(68, new VariableLengthValidator(1, 2), 1);
        addValidationInfo(144, oneByteValidator, 1);
        addValidationInfo(0, new FixedLengthValidator(2), 1);
        addValidationInfo(FoldPolicy.NAV_BAR_HEIGHT, oneByteValidator, 1);
        addValidationInfo(163, new FixedLengthValidator(3), 1);
        addValidationInfo(164, oneByteValidator, 1);
        addValidationInfo(HdmiCecKeycode.CEC_KEYCODE_F2_RED, oneByteValidator, 3);
        addValidationInfo(126, oneByteValidator, 1);
        addValidationInfo(154, oneByteValidator, 1);
        addValidationInfo(248, maxLengthValidator, 6);
    }

    private void addValidationInfo(int opcode, ParameterValidator validator, int addrType) {
        this.mValidationInfo.append(opcode, new ValidationInfo(validator, addrType));
    }

    /* access modifiers changed from: package-private */
    public int isValid(HdmiCecMessage message) {
        ValidationInfo info = this.mValidationInfo.get(message.getOpcode());
        if (info == null) {
            HdmiLogger.warning("No validation information for the message: " + message, new Object[0]);
            return 0;
        } else if (message.getSource() == 15 && (info.addressType & 4) == 0) {
            HdmiLogger.warning("Unexpected source: " + message, new Object[0]);
            return 1;
        } else {
            if (message.getDestination() == 15) {
                if ((info.addressType & 2) == 0) {
                    HdmiLogger.warning("Unexpected broadcast message: " + message, new Object[0]);
                    return 2;
                }
            } else if ((info.addressType & 1) == 0) {
                HdmiLogger.warning("Unexpected direct message: " + message, new Object[0]);
                return 2;
            }
            int errorCode = info.parameterValidator.isValid(message.getParams());
            if (errorCode == 0) {
                return 0;
            }
            HdmiLogger.warning("Unexpected parameters: " + message, new Object[0]);
            return errorCode;
        }
    }

    private static class FixedLengthValidator implements ParameterValidator {
        private final int mLength;

        public FixedLengthValidator(int length) {
            this.mLength = length;
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            return params.length < this.mLength ? 4 : 0;
        }
    }

    private static class VariableLengthValidator implements ParameterValidator {
        private final int mMaxLength;
        private final int mMinLength;

        public VariableLengthValidator(int minLength, int maxLength) {
            this.mMinLength = minLength;
            this.mMaxLength = maxLength;
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            return params.length < this.mMinLength ? 4 : 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidPhysicalAddress(byte[] params, int offset) {
        if (!this.mService.isTvDevice()) {
            return true;
        }
        int path = HdmiUtils.twoBytesToInt(params, offset);
        if ((path == 65535 || path != this.mService.getPhysicalAddress()) && this.mService.pathToPortId(path) == -1) {
            return false;
        }
        return true;
    }

    static boolean isValidType(int type) {
        return type >= 0 && type <= 7 && type != 2;
    }

    /* access modifiers changed from: private */
    public static int toErrorCode(boolean success) {
        return success ? 0 : 3;
    }

    private class PhysicalAddressValidator implements ParameterValidator {
        private PhysicalAddressValidator() {
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            if (params.length < 2) {
                return 4;
            }
            return HdmiCecMessageValidator.toErrorCode(HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0));
        }
    }

    private class SystemAudioModeRequestValidator extends PhysicalAddressValidator {
        private SystemAudioModeRequestValidator() {
            super();
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.PhysicalAddressValidator, com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            if (params.length == 0) {
                return 0;
            }
            return super.isValid(params);
        }
    }

    private class ReportPhysicalAddressValidator implements ParameterValidator {
        private ReportPhysicalAddressValidator() {
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            if (params.length < 3) {
                return 4;
            }
            boolean z = false;
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0) && HdmiCecMessageValidator.isValidType(params[2])) {
                z = true;
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }

    private class RoutingChangeValidator implements ParameterValidator {
        private RoutingChangeValidator() {
        }

        @Override // com.android.server.hdmi.HdmiCecMessageValidator.ParameterValidator
        public int isValid(byte[] params) {
            if (params.length < 4) {
                return 4;
            }
            boolean z = false;
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0) && HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 2)) {
                z = true;
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }
}
