package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Random;
import ohos.ai.engine.resultcode.HwHiAIResultCode;
import ohos.devtools.JLogConstants;

/* access modifiers changed from: package-private */
public final class PrimeNumberSequenceGenerator {
    private static final int[] PRIMES = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, JLogConstants.JLID_OPENGL_JANK_FRAME_SKIP, JLogConstants.JLID_MANUAL_DUMP_LOG, JLogConstants.JLID_CAMERAALGO_BACKBINNING_END, JLogConstants.JLID_CAMERAAPP_CREATESESSION_BEGIN, JLogConstants.JLID_ACTIVITY_LAUNCHING_END, JLogConstants.JLID_MSG_HANDLE_TIMEOUT, JLogConstants.JLID_KERNEL_FREQUENT_LMK, JLogConstants.JLID_SYSTEM_SERVICE_FROZEN, JLogConstants.JLID_GAME_APPEND_NET_LATENCY, JLogConstants.JLID_APP_UNFRONZED_BEGIN, JLogConstants.JLID_OTHER_FIELD_EXCEPTION, JLogConstants.JLID_DECODE_BMP_TIMEOUT, 383, JLogConstants.JLID_LAUNCHER_SLIDE_FPS, JLogConstants.JLID_SCROLL_START, 401, JLogConstants.JLID_CYCLE_UPLOAD_EVENT, JLogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JLogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_START, JLogConstants.JLID_ABILITY_ONSTART, JLogConstants.JLID_ABILITY_ONACTIVE, JLogConstants.JLID_ABILITY_SHELL_CONNECT_LOCAL_ABILITY, JLogConstants.JLID_AGP_ANIMATION_JANKFRAME, JLogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN_OLD, JLogConstants.JLID_GAME_SETTING, JLogConstants.JLID_CAMERA_COLD_START_BEGIN, JLogConstants.JLID_CAMERA_HARD_START_BEGIN, JLogConstants.JLID_CAMERA_SWITCH_MODE_END, 479, 487, 491, 499, 503, 509, HwHiAIResultCode.AIRESULT_SERVICE_BIND_EXCEPTION_DISCONNECT, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727};

    PrimeNumberSequenceGenerator() {
    }

    static void generateSequence(int[] iArr) {
        Random random = new Random();
        for (int i = 0; i < iArr.length; i++) {
            int[] iArr2 = PRIMES;
            iArr[i] = iArr2[random.nextInt(iArr2.length)];
        }
    }
}
