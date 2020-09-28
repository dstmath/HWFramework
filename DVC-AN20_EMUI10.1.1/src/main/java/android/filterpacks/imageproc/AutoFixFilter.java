package android.filterpacks.imageproc;

import android.app.slice.SliceItem;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.os.BatteryManager;
import android.provider.Downloads;
import android.util.JlogConstants;
import com.android.internal.logging.nano.MetricsProto;

public class AutoFixFilter extends Filter {
    private static final int[] normal_cdf = {9, 33, 50, 64, 75, 84, 92, 99, 106, 112, 117, 122, 126, 130, 134, 138, 142, 145, 148, 150, 154, 157, 159, 162, 164, 166, 169, 170, 173, 175, 177, 179, 180, 182, 184, 186, 188, 189, 190, 192, 194, 195, 197, 198, 199, 200, 202, 203, 205, 206, 207, 208, 209, 210, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 229, 230, 231, 232, 233, 234, 235, 236, 236, 237, 238, 239, 239, 240, 240, 242, 242, 243, 244, 245, 245, 246, 247, 247, 248, 249, 249, 250, 250, 251, 252, 253, 253, 254, 255, 255, 256, 256, 257, 258, 258, 259, 259, 259, 260, 261, 262, 262, 263, 263, 264, 264, 265, 265, 266, 267, 267, 268, 268, 269, 269, 269, 270, 270, 271, 272, 272, 273, 273, 274, 274, 275, 275, 276, 276, 277, 277, 277, 278, 278, 279, 279, 279, 280, 280, 281, 282, 282, 282, 283, 283, 284, 284, 285, 285, 285, 286, 286, 287, 287, 288, 288, 288, 289, 289, 289, 290, 290, 290, 291, 292, 292, 292, 293, 293, 294, 294, 294, 295, 295, 296, 296, 296, 297, 297, 297, 298, 298, 298, 299, 299, 299, 299, 300, 300, 301, 301, 302, 302, 302, 303, 303, 304, 304, 304, 305, 305, 305, 306, 306, 306, 307, 307, 307, 308, 308, 308, 309, 309, 309, 309, 310, 310, 310, 310, 311, 312, 312, 312, 313, 313, 313, 314, 314, 314, 315, 315, 315, 315, 316, 316, 316, 317, 317, 317, 318, 318, 318, 319, 319, 319, 319, 319, 320, 320, 320, 321, 321, 322, 322, 322, 323, 323, 323, 323, 324, 324, 324, 325, 325, 325, 325, 326, 326, 326, 327, 327, 327, 327, 328, 328, 328, 329, 329, 329, 329, 329, 330, 330, 330, 330, 331, 331, 332, 332, 332, 333, 333, 333, 333, 334, 334, 334, 334, 335, 335, 335, 336, 336, 336, 336, 337, 337, 337, 337, 338, 338, 338, 339, 339, 339, 339, 339, 339, 340, 340, 340, 340, 341, 341, 342, 342, 342, 342, 343, 343, 343, 344, 344, 344, 344, 345, 345, 345, 345, 346, 346, 346, 346, 347, 347, 347, 347, 348, 348, 348, 348, 349, 349, 349, 349, 349, 349, 350, 350, 350, 350, 351, 351, 352, 352, 352, 352, 353, 353, 353, 353, 354, 354, 354, 354, 355, 355, 355, 355, 356, 356, 356, 356, 357, 357, 357, 357, 358, 358, 358, 358, 359, 359, 359, 359, 359, 359, 359, 360, 360, 360, 360, 361, 361, 362, 362, 362, 362, 363, 363, 363, 363, 364, 364, 364, 364, 365, 365, 365, 365, 366, 366, 366, 366, 366, 367, 367, 367, 367, 368, 368, 368, 368, 369, 369, 369, 369, 369, 369, 370, 370, 370, 370, 370, 371, 371, 372, 372, 372, 372, 373, 373, 373, 373, 374, 374, 374, 374, 374, 375, 375, 375, 375, 376, 376, 376, 376, 377, 377, 377, 377, 378, 378, 378, 378, 378, 379, 379, 379, 379, 379, 379, 380, 380, 380, 380, 381, 381, 381, 382, 382, 382, 382, 383, 383, 383, 383, 384, 384, 384, 384, 385, 385, 385, 385, 385, 386, 386, 386, 386, 387, 387, 387, 387, 388, 388, 388, 388, 388, 389, 389, 389, 389, 389, 389, 390, 390, 390, 390, 391, 391, 392, 392, 392, 392, 392, 393, 393, 393, 393, 394, 394, 394, 394, 395, 395, 395, 395, 396, 396, 396, 396, 396, 397, 397, 397, 397, 398, 398, 398, 398, 399, 399, 399, 399, 399, 399, 400, 400, 400, 400, 400, 401, 401, 402, 402, 402, 402, 403, 403, 403, 403, 404, 404, 404, 404, 405, 405, 405, 405, 406, 406, 406, 406, 406, 407, 407, 407, 407, 408, 408, 408, 408, 409, 409, 409, 409, 409, 409, 410, 410, 410, 410, 411, 411, 412, 412, 412, 412, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_START, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_START, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_START, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_START, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_END, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_END, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_END, JlogConstants.JLID_INPUTMETHOD_HIDE_PANEL_END, JlogConstants.JLID_INPUTMETHOD_PRESS_KEY_START, JlogConstants.JLID_INPUTMETHOD_PRESS_KEY_START, JlogConstants.JLID_INPUTMETHOD_PRESS_KEY_START, JlogConstants.JLID_INPUTMETHOD_PRESS_KEY_START, 416, 416, 416, 416, JlogConstants.JLID_INPUTMETHOD_CANDIDATE_CLICK_START, JlogConstants.JLID_INPUTMETHOD_CANDIDATE_CLICK_START, JlogConstants.JLID_INPUTMETHOD_CANDIDATE_CLICK_START, JlogConstants.JLID_INPUTMETHOD_CANDIDATE_CLICK_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_MOVE_START, 420, 420, 420, 420, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_START, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_START, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_END, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_END, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_END, JlogConstants.JLID_INPUTMETHOD_SYMBOL_MOVE_END, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_START, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_START, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_START, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_START, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_END, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_END, JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_END, JlogConstants.JLID_ABILITY_SHELL_LOAD, JlogConstants.JLID_ABILITY_SHELL_LOAD, JlogConstants.JLID_ABILITY_SHELL_LOAD, JlogConstants.JLID_ABILITY_SHELL_LOAD, JlogConstants.JLID_ABILITY_SHELL_START, JlogConstants.JLID_ABILITY_SHELL_START, JlogConstants.JLID_ABILITY_SHELL_START, JlogConstants.JLID_ABILITY_SHELL_START, JlogConstants.JLID_ABILITY_SHELL_ACTIVE, JlogConstants.JLID_ABILITY_SHELL_ACTIVE, JlogConstants.JLID_ABILITY_SHELL_ACTIVE, JlogConstants.JLID_ABILITY_SHELL_ACTIVE, JlogConstants.JLID_ABILITY_SHELL_DESTORY, JlogConstants.JLID_ABILITY_SHELL_DESTORY, JlogConstants.JLID_ABILITY_SHELL_DESTORY, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_STOP, JlogConstants.JLID_ABILITY_SHELL_INACTIVE, JlogConstants.JLID_ABILITY_SHELL_INACTIVE, JlogConstants.JLID_ABILITY_SHELL_INACTIVE, JlogConstants.JLID_ABILITY_SHELL_INACTIVE, JlogConstants.JLID_ABILITY_ONSTART, JlogConstants.JLID_ABILITY_ONSTART, 432, 432, 432, JlogConstants.JLID_ABILITY_ONACTIVE, JlogConstants.JLID_ABILITY_ONACTIVE, JlogConstants.JLID_ABILITY_ONACTIVE, JlogConstants.JLID_ABILITY_ONACTIVE, JlogConstants.JLID_ABILITY_ONINACTIVE, JlogConstants.JLID_ABILITY_ONINACTIVE, JlogConstants.JLID_ABILITY_ONINACTIVE, JlogConstants.JLID_ABILITY_ONFOREGROUNG, JlogConstants.JLID_ABILITY_ONFOREGROUNG, JlogConstants.JLID_ABILITY_ONFOREGROUNG, JlogConstants.JLID_ABILITY_ONFOREGROUNG, JlogConstants.JLID_ABILITY_ONBACKGROUNG, JlogConstants.JLID_ABILITY_ONBACKGROUNG, JlogConstants.JLID_ABILITY_ONBACKGROUNG, JlogConstants.JLID_ABILITY_ONBACKGROUNG, JlogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_LOCAL_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_LOCAL_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_LOCAL_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_LOCAL_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, JlogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, 440, 440, 440, JlogConstants.JLID_AGP_SCROLL_STOP, JlogConstants.JLID_AGP_SCROLL_STOP, JlogConstants.JLID_AGP_JANK_FRAME_SKIP, JlogConstants.JLID_AGP_JANK_FRAME_SKIP, JlogConstants.JLID_AGP_JANK_FRAME_SKIP, JlogConstants.JLID_AGP_ANIMATION_JANKFRAME, JlogConstants.JLID_AGP_ANIMATION_JANKFRAME, JlogConstants.JLID_AGP_ANIMATION_JANKFRAME, JlogConstants.JLID_AGP_ANIMATION_JANKFRAME, JlogConstants.JLID_AGP_ACTIVITY_LAUNCHING_END, JlogConstants.JLID_AGP_ACTIVITY_LAUNCHING_END, JlogConstants.JLID_AGP_ACTIVITY_LAUNCHING_END, JlogConstants.JLID_AGP_SEQ_JANK_FRAME_SKIP, JlogConstants.JLID_AGP_SEQ_JANK_FRAME_SKIP, JlogConstants.JLID_AGP_SEQ_JANK_FRAME_SKIP, JlogConstants.JLID_COMMUNICATION_IPC_SEND_TIME, JlogConstants.JLID_COMMUNICATION_IPC_SEND_TIME, JlogConstants.JLID_COMMUNICATION_IPC_SEND_TIME, JlogConstants.JLID_COMMUNICATION_IPC_SEND_TIME, JlogConstants.JLID_IPC_COMMUNICATION_IPC_RECV_TIME, JlogConstants.JLID_IPC_COMMUNICATION_IPC_RECV_TIME, JlogConstants.JLID_IPC_COMMUNICATION_IPC_RECV_TIME, 448, 448, 448, JlogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, JlogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, JlogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, JlogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, JlogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, 450, 450, 450, 451, 451, 452, 452, 452, 453, 453, 453, 454, 454, 454, 455, 455, 455, 456, 456, 456, 457, 457, 457, 458, 458, 458, 459, 459, 459, 459, 460, 460, 460, 461, 461, 462, 462, 462, 463, 463, 463, 464, 464, 465, 465, 465, 466, 466, 466, 467, 467, 467, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_CANCEL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_CANCEL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CONFIRM, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CONFIRM, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CONFIRM, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CONFIRM, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_APPS_DELETION_FAIL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_DOWNLOADS_DELETION_FAIL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_DOWNLOADS_DELETION_FAIL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_DOWNLOADS_DELETION_FAIL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_PHOTOS_VIDEOS_DELETION_FAIL, MetricsProto.MetricsEvent.ACTION_DELETION_HELPER_PHOTOS_VIDEOS_DELETION_FAIL, MetricsProto.MetricsEvent.DASHBOARD_CONTAINER, MetricsProto.MetricsEvent.DASHBOARD_CONTAINER, MetricsProto.MetricsEvent.DASHBOARD_CONTAINER, MetricsProto.MetricsEvent.SUPPORT_FRAGMENT, MetricsProto.MetricsEvent.SUPPORT_FRAGMENT, MetricsProto.MetricsEvent.ACTION_SELECT_SUMMARY, MetricsProto.MetricsEvent.ACTION_SELECT_SUMMARY, MetricsProto.MetricsEvent.ACTION_SELECT_SUMMARY, MetricsProto.MetricsEvent.ACTION_SELECT_SUPPORT_FRAGMENT, MetricsProto.MetricsEvent.ACTION_SELECT_SUPPORT_FRAGMENT, MetricsProto.MetricsEvent.ACTION_SUPPORT_TIPS_AND_TRICKS, MetricsProto.MetricsEvent.ACTION_SUPPORT_TIPS_AND_TRICKS, MetricsProto.MetricsEvent.ACTION_SUPPORT_TIPS_AND_TRICKS, MetricsProto.MetricsEvent.ACTION_SUPPORT_HELP_AND_FEEDBACK, MetricsProto.MetricsEvent.ACTION_SUPPORT_HELP_AND_FEEDBACK, MetricsProto.MetricsEvent.ACTION_SUPPORT_HELP_AND_FEEDBACK, 480, 480, 480, MetricsProto.MetricsEvent.ACTION_SUPPORT_PHONE, MetricsProto.MetricsEvent.ACTION_SUPPORT_CHAT, MetricsProto.MetricsEvent.ACTION_SUPPORT_CHAT, MetricsProto.MetricsEvent.ACTION_SUPPORT_DISCLAIMER_CANCEL, MetricsProto.MetricsEvent.ACTION_SUPPORT_DISCLAIMER_CANCEL, MetricsProto.MetricsEvent.ACTION_SUPPORT_DISCLAIMER_OK, MetricsProto.MetricsEvent.ACTION_SUPPORT_DISCLAIMER_OK, MetricsProto.MetricsEvent.ACTION_SUPPORT_DISCLAIMER_OK, MetricsProto.MetricsEvent.ACTION_SUPPORT_DAIL_TOLLFREE, MetricsProto.MetricsEvent.ACTION_SUPPORT_DAIL_TOLLFREE, MetricsProto.MetricsEvent.ACTION_SUPPORT_VIEW_TRAVEL_ABROAD_DIALOG, MetricsProto.MetricsEvent.ACTION_SUPPORT_VIEW_TRAVEL_ABROAD_DIALOG, MetricsProto.MetricsEvent.ACTION_SUPPORT_DIAL_TOLLED, MetricsProto.MetricsEvent.ACTION_SUPPORT_DIAL_TOLLED, 488, 488, 488, 489, 489, 489, 490, 490, 491, 492, 492, 493, 493, 494, 494, 495, 495, 496, 496, Downloads.Impl.STATUS_TOO_MANY_REDIRECTS, Downloads.Impl.STATUS_TOO_MANY_REDIRECTS, 498, 498, 499, 499, 499, 500, 501, 502, 502, 503, 503, 504, 504, 505, 505, 506, 507, 507, 508, 508, 509, 509, 510, 510, 511, 512, 513, 513, 514, 515, 515, 516, 517, 517, 518, 519, 519, 519, 520, 521, 522, 523, 524, 524, 525, 526, 526, 527, 528, 529, 529, 530, 531, 532, 533, 534, 535, 535, 536, 537, 538, 539, 539, 540, 542, 543, 544, 545, 546, 547, 548, 549, 549, 550, 552, 553, 554, 555, 556, 558, 559, 559, 561, 562, 564, 565, 566, 568, 569, 570, 572, 574, 575, 577, 578, 579, 582, 583, 585, 587, 589, 590, 593, 595, 597, 599, 602, 604, 607, 609, 612, 615, 618, 620, 624, 628, 631, 635, 639, 644, 649, 654, 659, 666, 673, 680, 690, 700, 714};
    private final String mAutoFixShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float scale;\nuniform float shift_scale;\nuniform float hist_offset;\nuniform float hist_scale;\nuniform float density_offset;\nuniform float density_scale;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = dot(color.rgb, weights);\n  float mask_value = energy - 0.5;\n  float alpha;\n  if (mask_value > 0.0) {\n    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n  } else { \n    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n  }\n  float index = energy * hist_scale + hist_offset;\n  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n  float value = temp.g + temp.r * shift_scale;\n  index = value * density_scale + density_offset;\n  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n  value = temp.g + temp.r * shift_scale;\n  float dst_energy = energy * alpha + value * (1.0 - alpha);\n  float max_energy = energy / max(color.r, max(color.g, color.b));\n  if (dst_energy > max_energy) {\n    dst_energy = max_energy;\n  }\n  if (energy == 0.0) {\n    gl_FragColor = color;\n  } else {\n    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n  }\n}\n";
    private Frame mDensityFrame;
    private int mHeight = 0;
    private Frame mHistFrame;
    private Program mNativeProgram;
    @GenerateFieldPort(name = BatteryManager.EXTRA_SCALE)
    private float mScale;
    private Program mShaderProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    private int mWidth = 0;

    public AutoFixFilter(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3));
        addOutputBasedOnInput(SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE);
    }

    @Override // android.filterfw.core.Filter
    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void initProgram(FilterContext context, int target) {
        if (target == 3) {
            ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float scale;\nuniform float shift_scale;\nuniform float hist_offset;\nuniform float hist_scale;\nuniform float density_offset;\nuniform float density_scale;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = dot(color.rgb, weights);\n  float mask_value = energy - 0.5;\n  float alpha;\n  if (mask_value > 0.0) {\n    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n  } else { \n    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n  }\n  float index = energy * hist_scale + hist_offset;\n  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n  float value = temp.g + temp.r * shift_scale;\n  index = value * density_scale + density_offset;\n  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n  value = temp.g + temp.r * shift_scale;\n  float dst_energy = energy * alpha + value * (1.0 - alpha);\n  float max_energy = energy / max(color.r, max(color.g, color.b));\n  if (dst_energy > max_energy) {\n    dst_energy = max_energy;\n  }\n  if (energy == 0.0) {\n    gl_FragColor = color;\n  } else {\n    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n  }\n}\n");
            shaderProgram.setMaximumTileSize(this.mTileSize);
            this.mShaderProgram = shaderProgram;
            this.mTarget = target;
            return;
        }
        throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
    }

    private void initParameters() {
        this.mShaderProgram.setHostValue("shift_scale", Float.valueOf(0.00390625f));
        this.mShaderProgram.setHostValue("hist_offset", Float.valueOf(6.527415E-4f));
        this.mShaderProgram.setHostValue("hist_scale", Float.valueOf(0.99869454f));
        this.mShaderProgram.setHostValue("density_offset", Float.valueOf(4.8828125E-4f));
        this.mShaderProgram.setHostValue("density_scale", Float.valueOf(0.99902344f));
        this.mShaderProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale));
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Filter
    public void prepare(FilterContext context) {
        int[] densityTable = new int[1024];
        for (int i = 0; i < 1024; i++) {
            densityTable[i] = (int) ((((long) normal_cdf[i]) * 65535) / ((long) 766));
        }
        this.mDensityFrame = context.getFrameManager().newFrame(ImageFormat.create(1024, 1, 3, 3));
        this.mDensityFrame.setInts(densityTable);
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        Frame frame = this.mDensityFrame;
        if (frame != null) {
            frame.release();
            this.mDensityFrame = null;
        }
        Frame frame2 = this.mHistFrame;
        if (frame2 != null) {
            frame2.release();
            this.mHistFrame = null;
        }
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        Program program = this.mShaderProgram;
        if (program != null) {
            program.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale));
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame input = pullInput(SliceItem.FORMAT_IMAGE);
        FrameFormat inputFormat = input.getFormat();
        if (this.mShaderProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
            initParameters();
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            createHistogramFrame(context, this.mWidth, this.mHeight, input.getInts());
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mShaderProgram.process(new Frame[]{input, this.mHistFrame, this.mDensityFrame}, output);
        pushOutput(SliceItem.FORMAT_IMAGE, output);
        output.release();
    }

    private void createHistogramFrame(FilterContext context, int width, int height, int[] data) {
        int[] histArray = new int[766];
        int y_border_thickness = (int) (((float) height) * 0.05f);
        int x_border_thickness = (int) (((float) width) * 0.05f);
        int pixels = (width - (x_border_thickness * 2)) * (height - (y_border_thickness * 2));
        for (int y = y_border_thickness; y < height - y_border_thickness; y++) {
            for (int x = x_border_thickness; x < width - x_border_thickness; x++) {
                int index = (y * width) + x;
                int energy = (data[index] & 255) + ((data[index] >> 8) & 255) + ((data[index] >> 16) & 255);
                histArray[energy] = histArray[energy] + 1;
            }
        }
        for (int i = 1; i < 766; i++) {
            histArray[i] = histArray[i] + histArray[i - 1];
        }
        for (int i2 = 0; i2 < 766; i2++) {
            histArray[i2] = (int) ((((long) histArray[i2]) * 65535) / ((long) pixels));
        }
        FrameFormat shaderHistFormat = ImageFormat.create(766, 1, 3, 3);
        Frame frame = this.mHistFrame;
        if (frame != null) {
            frame.release();
        }
        this.mHistFrame = context.getFrameManager().newFrame(shaderHistFormat);
        this.mHistFrame.setInts(histArray);
    }
}
