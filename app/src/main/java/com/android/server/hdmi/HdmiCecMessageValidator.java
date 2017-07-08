package com.android.server.hdmi;

import android.util.SparseArray;
import com.android.server.display.RampAnimator;

public final class HdmiCecMessageValidator {
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
    final SparseArray<ValidationInfo> mValidationInfo;

    interface ParameterValidator {
        int isValid(byte[] bArr);
    }

    private static class FixedLengthValidator implements ParameterValidator {
        private final int mLength;

        public FixedLengthValidator(int length) {
            this.mLength = length;
        }

        public int isValid(byte[] params) {
            return params.length < this.mLength ? HdmiCecMessageValidator.SRC_UNREGISTERED : HdmiCecMessageValidator.OK;
        }
    }

    private class PhysicalAddressValidator implements ParameterValidator {
        private PhysicalAddressValidator() {
        }

        public int isValid(byte[] params) {
            if (params.length < HdmiCecMessageValidator.ERROR_DESTINATION) {
                return HdmiCecMessageValidator.SRC_UNREGISTERED;
            }
            return HdmiCecMessageValidator.toErrorCode(HdmiCecMessageValidator.this.isValidPhysicalAddress(params, HdmiCecMessageValidator.OK));
        }
    }

    private class ReportPhysicalAddressValidator implements ParameterValidator {
        private ReportPhysicalAddressValidator() {
        }

        public int isValid(byte[] params) {
            boolean z = false;
            if (params.length < HdmiCecMessageValidator.ERROR_PARAMETER) {
                return HdmiCecMessageValidator.SRC_UNREGISTERED;
            }
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, HdmiCecMessageValidator.OK)) {
                z = HdmiCecMessageValidator.isValidType(params[HdmiCecMessageValidator.ERROR_DESTINATION]);
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }

    private class RoutingChangeValidator implements ParameterValidator {
        private RoutingChangeValidator() {
        }

        public int isValid(byte[] params) {
            boolean z = false;
            if (params.length < HdmiCecMessageValidator.SRC_UNREGISTERED) {
                return HdmiCecMessageValidator.SRC_UNREGISTERED;
            }
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, HdmiCecMessageValidator.OK)) {
                z = HdmiCecMessageValidator.this.isValidPhysicalAddress(params, HdmiCecMessageValidator.ERROR_DESTINATION);
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }

    private class SystemAudioModeRequestValidator extends PhysicalAddressValidator {
        private SystemAudioModeRequestValidator() {
            super(null);
        }

        public int isValid(byte[] params) {
            if (params.length == 0) {
                return HdmiCecMessageValidator.OK;
            }
            return super.isValid(params);
        }
    }

    private static class ValidationInfo {
        public final int addressType;
        public final ParameterValidator parameterValidator;

        public ValidationInfo(ParameterValidator validator, int type) {
            this.parameterValidator = validator;
            this.addressType = type;
        }
    }

    private static class VariableLengthValidator implements ParameterValidator {
        private final int mMaxLength;
        private final int mMinLength;

        public VariableLengthValidator(int minLength, int maxLength) {
            this.mMinLength = minLength;
            this.mMaxLength = maxLength;
        }

        public int isValid(byte[] params) {
            return params.length < this.mMinLength ? HdmiCecMessageValidator.SRC_UNREGISTERED : HdmiCecMessageValidator.OK;
        }
    }

    public HdmiCecMessageValidator(HdmiControlService service) {
        this.mValidationInfo = new SparseArray();
        this.mService = service;
        PhysicalAddressValidator physicalAddressValidator = new PhysicalAddressValidator();
        addValidationInfo(130, physicalAddressValidator, 6);
        addValidationInfo(157, physicalAddressValidator, ERROR_SOURCE);
        addValidationInfo(132, new ReportPhysicalAddressValidator(), 6);
        addValidationInfo(DumpState.DUMP_PACKAGES, new RoutingChangeValidator(), 6);
        addValidationInfo(129, physicalAddressValidator, 6);
        addValidationInfo(134, physicalAddressValidator, ERROR_DESTINATION);
        addValidationInfo(HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE, new SystemAudioModeRequestValidator(), ERROR_SOURCE);
        FixedLengthValidator noneValidator = new FixedLengthValidator(OK);
        addValidationInfo(RampAnimator.DEFAULT_MAX_BRIGHTNESS, noneValidator, ERROR_SOURCE);
        addValidationInfo(159, noneValidator, ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2, noneValidator, 5);
        addValidationInfo(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, noneValidator, ERROR_SOURCE);
        addValidationInfo(143, noneValidator, ERROR_SOURCE);
        addValidationInfo(140, noneValidator, 5);
        addValidationInfo(70, noneValidator, ERROR_SOURCE);
        addValidationInfo(131, noneValidator, 5);
        addValidationInfo(125, noneValidator, ERROR_SOURCE);
        addValidationInfo(SRC_UNREGISTERED, noneValidator, ERROR_SOURCE);
        addValidationInfo(192, noneValidator, ERROR_SOURCE);
        addValidationInfo(11, noneValidator, ERROR_SOURCE);
        addValidationInfo(15, noneValidator, ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS, noneValidator, ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, noneValidator, ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, noneValidator, ERROR_SOURCE);
        addValidationInfo(196, noneValidator, ERROR_SOURCE);
        addValidationInfo(133, noneValidator, 6);
        addValidationInfo(54, noneValidator, 7);
        addValidationInfo(197, noneValidator, ERROR_SOURCE);
        addValidationInfo(13, noneValidator, ERROR_SOURCE);
        addValidationInfo(6, noneValidator, ERROR_SOURCE);
        addValidationInfo(5, noneValidator, ERROR_SOURCE);
        addValidationInfo(69, noneValidator, ERROR_SOURCE);
        addValidationInfo(139, noneValidator, ERROR_PARAMETER);
        FixedLengthValidator oneByteValidator = new FixedLengthValidator(ERROR_SOURCE);
        addValidationInfo(9, new VariableLengthValidator(ERROR_SOURCE, 8), ERROR_SOURCE);
        addValidationInfo(10, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(158, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(50, new FixedLengthValidator(ERROR_PARAMETER), ERROR_DESTINATION);
        VariableLengthValidator maxLengthValidator = new VariableLengthValidator(OK, 14);
        addValidationInfo(135, new FixedLengthValidator(ERROR_PARAMETER), ERROR_DESTINATION);
        addValidationInfo(137, new VariableLengthValidator(ERROR_SOURCE, 14), 5);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER, new VariableLengthValidator(SRC_UNREGISTERED, 14), 7);
        addValidationInfo(138, maxLengthValidator, 7);
        addValidationInfo(100, maxLengthValidator, ERROR_SOURCE);
        addValidationInfo(71, maxLengthValidator, ERROR_SOURCE);
        addValidationInfo(141, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(142, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(68, new VariableLengthValidator(ERROR_SOURCE, ERROR_DESTINATION), ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(OK, new FixedLengthValidator(ERROR_DESTINATION), ERROR_SOURCE);
        addValidationInfo(122, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(163, new FixedLengthValidator(ERROR_PARAMETER), ERROR_SOURCE);
        addValidationInfo(164, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(HdmiCecKeycode.CEC_KEYCODE_F2_RED, oneByteValidator, ERROR_PARAMETER);
        addValidationInfo(126, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(154, oneByteValidator, ERROR_SOURCE);
        addValidationInfo(248, maxLengthValidator, 6);
    }

    private void addValidationInfo(int opcode, ParameterValidator validator, int addrType) {
        this.mValidationInfo.append(opcode, new ValidationInfo(validator, addrType));
    }

    int isValid(HdmiCecMessage message) {
        ValidationInfo info = (ValidationInfo) this.mValidationInfo.get(message.getOpcode());
        if (info == null) {
            HdmiLogger.warning("No validation information for the message: " + message, new Object[OK]);
            return OK;
        } else if (message.getSource() == 15 && (info.addressType & SRC_UNREGISTERED) == 0) {
            HdmiLogger.warning("Unexpected source: " + message, new Object[OK]);
            return ERROR_SOURCE;
        } else {
            if (message.getDestination() == 15) {
                if ((info.addressType & ERROR_DESTINATION) == 0) {
                    HdmiLogger.warning("Unexpected broadcast message: " + message, new Object[OK]);
                    return ERROR_DESTINATION;
                }
            } else if ((info.addressType & ERROR_SOURCE) == 0) {
                HdmiLogger.warning("Unexpected direct message: " + message, new Object[OK]);
                return ERROR_DESTINATION;
            }
            int errorCode = info.parameterValidator.isValid(message.getParams());
            if (errorCode == 0) {
                return OK;
            }
            HdmiLogger.warning("Unexpected parameters: " + message, new Object[OK]);
            return errorCode;
        }
    }

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
        if (type < 0 || type > 7 || type == ERROR_DESTINATION) {
            return false;
        }
        return true;
    }

    private static int toErrorCode(boolean success) {
        return success ? OK : ERROR_PARAMETER;
    }
}
