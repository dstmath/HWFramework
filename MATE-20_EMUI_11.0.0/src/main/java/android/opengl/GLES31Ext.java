package android.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class GLES31Ext {
    public static final int GL_BLEND_ADVANCED_COHERENT_KHR = 37509;
    public static final int GL_BUFFER_KHR = 33504;
    public static final int GL_CLAMP_TO_BORDER_EXT = 33069;
    public static final int GL_COLORBURN_KHR = 37530;
    public static final int GL_COLORDODGE_KHR = 37529;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x10_KHR = 37819;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x5_KHR = 37816;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x6_KHR = 37817;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x8_KHR = 37818;
    public static final int GL_COMPRESSED_RGBA_ASTC_12x10_KHR = 37820;
    public static final int GL_COMPRESSED_RGBA_ASTC_12x12_KHR = 37821;
    public static final int GL_COMPRESSED_RGBA_ASTC_4x4_KHR = 37808;
    public static final int GL_COMPRESSED_RGBA_ASTC_5x4_KHR = 37809;
    public static final int GL_COMPRESSED_RGBA_ASTC_5x5_KHR = 37810;
    public static final int GL_COMPRESSED_RGBA_ASTC_6x5_KHR = 37811;
    public static final int GL_COMPRESSED_RGBA_ASTC_6x6_KHR = 37812;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x5_KHR = 37813;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x6_KHR = 37814;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x8_KHR = 37815;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = 37851;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = 37848;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = 37849;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = 37850;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = 37852;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = 37853;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 37840;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = 37841;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = 37842;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = 37843;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = 37844;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = 37845;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = 37846;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = 37847;
    public static final int GL_CONTEXT_FLAG_DEBUG_BIT_KHR = 2;
    public static final int GL_DARKEN_KHR = 37527;
    public static final int GL_DEBUG_CALLBACK_FUNCTION_KHR = 33348;
    public static final int GL_DEBUG_CALLBACK_USER_PARAM_KHR = 33349;
    public static final int GL_DEBUG_GROUP_STACK_DEPTH_KHR = 33389;
    public static final int GL_DEBUG_LOGGED_MESSAGES_KHR = 37189;
    public static final int GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH_KHR = 33347;
    public static final int GL_DEBUG_OUTPUT_KHR = 37600;
    public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS_KHR = 33346;
    public static final int GL_DEBUG_SEVERITY_HIGH_KHR = 37190;
    public static final int GL_DEBUG_SEVERITY_LOW_KHR = 37192;
    public static final int GL_DEBUG_SEVERITY_MEDIUM_KHR = 37191;
    public static final int GL_DEBUG_SEVERITY_NOTIFICATION_KHR = 33387;
    public static final int GL_DEBUG_SOURCE_API_KHR = 33350;
    public static final int GL_DEBUG_SOURCE_APPLICATION_KHR = 33354;
    public static final int GL_DEBUG_SOURCE_OTHER_KHR = 33355;
    public static final int GL_DEBUG_SOURCE_SHADER_COMPILER_KHR = 33352;
    public static final int GL_DEBUG_SOURCE_THIRD_PARTY_KHR = 33353;
    public static final int GL_DEBUG_SOURCE_WINDOW_SYSTEM_KHR = 33351;
    public static final int GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_KHR = 33357;
    public static final int GL_DEBUG_TYPE_ERROR_KHR = 33356;
    public static final int GL_DEBUG_TYPE_MARKER_KHR = 33384;
    public static final int GL_DEBUG_TYPE_OTHER_KHR = 33361;
    public static final int GL_DEBUG_TYPE_PERFORMANCE_KHR = 33360;
    public static final int GL_DEBUG_TYPE_POP_GROUP_KHR = 33386;
    public static final int GL_DEBUG_TYPE_PORTABILITY_KHR = 33359;
    public static final int GL_DEBUG_TYPE_PUSH_GROUP_KHR = 33385;
    public static final int GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_KHR = 33358;
    public static final int GL_DECODE_EXT = 35401;
    public static final int GL_DIFFERENCE_KHR = 37534;
    public static final int GL_EXCLUSION_KHR = 37536;
    public static final int GL_FIRST_VERTEX_CONVENTION_EXT = 36429;
    public static final int GL_FRACTIONAL_EVEN_EXT = 36476;
    public static final int GL_FRACTIONAL_ODD_EXT = 36475;
    public static final int GL_FRAGMENT_INTERPOLATION_OFFSET_BITS_OES = 36445;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_LAYERED_EXT = 36263;
    public static final int GL_FRAMEBUFFER_DEFAULT_LAYERS_EXT = 37650;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT = 36264;
    public static final int GL_GEOMETRY_LINKED_INPUT_TYPE_EXT = 35095;
    public static final int GL_GEOMETRY_LINKED_OUTPUT_TYPE_EXT = 35096;
    public static final int GL_GEOMETRY_LINKED_VERTICES_OUT_EXT = 35094;
    public static final int GL_GEOMETRY_SHADER_BIT_EXT = 4;
    public static final int GL_GEOMETRY_SHADER_EXT = 36313;
    public static final int GL_GEOMETRY_SHADER_INVOCATIONS_EXT = 34943;
    public static final int GL_HARDLIGHT_KHR = 37531;
    public static final int GL_HSL_COLOR_KHR = 37551;
    public static final int GL_HSL_HUE_KHR = 37549;
    public static final int GL_HSL_LUMINOSITY_KHR = 37552;
    public static final int GL_HSL_SATURATION_KHR = 37550;
    public static final int GL_IMAGE_BUFFER_EXT = 36945;
    public static final int GL_IMAGE_CUBE_MAP_ARRAY_EXT = 36948;
    public static final int GL_INT_IMAGE_BUFFER_EXT = 36956;
    public static final int GL_INT_IMAGE_CUBE_MAP_ARRAY_EXT = 36959;
    public static final int GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY_OES = 37132;
    public static final int GL_INT_SAMPLER_BUFFER_EXT = 36304;
    public static final int GL_INT_SAMPLER_CUBE_MAP_ARRAY_EXT = 36878;
    public static final int GL_ISOLINES_EXT = 36474;
    public static final int GL_IS_PER_PATCH_EXT = 37607;
    public static final int GL_LAST_VERTEX_CONVENTION_EXT = 36430;
    public static final int GL_LAYER_PROVOKING_VERTEX_EXT = 33374;
    public static final int GL_LIGHTEN_KHR = 37528;
    public static final int GL_LINES_ADJACENCY_EXT = 10;
    public static final int GL_LINE_STRIP_ADJACENCY_EXT = 11;
    public static final int GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS_EXT = 35378;
    public static final int GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS_EXT = 36382;
    public static final int GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS_EXT = 36383;
    public static final int GL_MAX_DEBUG_GROUP_STACK_DEPTH_KHR = 33388;
    public static final int GL_MAX_DEBUG_LOGGED_MESSAGES_KHR = 37188;
    public static final int GL_MAX_DEBUG_MESSAGE_LENGTH_KHR = 37187;
    public static final int GL_MAX_FRAGMENT_INTERPOLATION_OFFSET_OES = 36444;
    public static final int GL_MAX_FRAMEBUFFER_LAYERS_EXT = 37655;
    public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTERS_EXT = 37589;
    public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS_EXT = 37583;
    public static final int GL_MAX_GEOMETRY_IMAGE_UNIFORMS_EXT = 37069;
    public static final int GL_MAX_GEOMETRY_INPUT_COMPONENTS_EXT = 37155;
    public static final int GL_MAX_GEOMETRY_OUTPUT_COMPONENTS_EXT = 37156;
    public static final int GL_MAX_GEOMETRY_OUTPUT_VERTICES_EXT = 36320;
    public static final int GL_MAX_GEOMETRY_SHADER_INVOCATIONS_EXT = 36442;
    public static final int GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS_EXT = 37079;
    public static final int GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_EXT = 35881;
    public static final int GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_EXT = 36321;
    public static final int GL_MAX_GEOMETRY_UNIFORM_BLOCKS_EXT = 35372;
    public static final int GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_EXT = 36319;
    public static final int GL_MAX_LABEL_LENGTH_KHR = 33512;
    public static final int GL_MAX_PATCH_VERTICES_EXT = 36477;
    public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS_EXT = 37587;
    public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS_EXT = 37581;
    public static final int GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS_EXT = 37067;
    public static final int GL_MAX_TESS_CONTROL_INPUT_COMPONENTS_EXT = 34924;
    public static final int GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS_EXT = 36483;
    public static final int GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS_EXT = 37080;
    public static final int GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS_EXT = 36481;
    public static final int GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS_EXT = 36485;
    public static final int GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS_EXT = 36489;
    public static final int GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS_EXT = 36479;
    public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS_EXT = 37588;
    public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS_EXT = 37582;
    public static final int GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS_EXT = 37068;
    public static final int GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS_EXT = 34925;
    public static final int GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS_EXT = 36486;
    public static final int GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS_EXT = 37081;
    public static final int GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS_EXT = 36482;
    public static final int GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS_EXT = 36490;
    public static final int GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS_EXT = 36480;
    public static final int GL_MAX_TESS_GEN_LEVEL_EXT = 36478;
    public static final int GL_MAX_TESS_PATCH_COMPONENTS_EXT = 36484;
    public static final int GL_MAX_TEXTURE_BUFFER_SIZE_EXT = 35883;
    public static final int GL_MIN_FRAGMENT_INTERPOLATION_OFFSET_OES = 36443;
    public static final int GL_MIN_SAMPLE_SHADING_VALUE_OES = 35895;
    public static final int GL_MULTIPLY_KHR = 37524;
    public static final int GL_OVERLAY_KHR = 37526;
    public static final int GL_PATCHES_EXT = 14;
    public static final int GL_PATCH_VERTICES_EXT = 36466;
    public static final int GL_PRIMITIVES_GENERATED_EXT = 35975;
    public static final int GL_PRIMITIVE_BOUNDING_BOX_EXT = 37566;
    public static final int GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED = 33313;
    public static final int GL_PROGRAM_KHR = 33506;
    public static final int GL_QUADS_EXT = 7;
    public static final int GL_QUERY_KHR = 33507;
    public static final int GL_REFERENCED_BY_GEOMETRY_SHADER_EXT = 37641;
    public static final int GL_REFERENCED_BY_TESS_CONTROL_SHADER_EXT = 37639;
    public static final int GL_REFERENCED_BY_TESS_EVALUATION_SHADER_EXT = 37640;
    public static final int GL_SAMPLER_2D_MULTISAMPLE_ARRAY_OES = 37131;
    public static final int GL_SAMPLER_BUFFER_EXT = 36290;
    public static final int GL_SAMPLER_CUBE_MAP_ARRAY_EXT = 36876;
    public static final int GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW_EXT = 36877;
    public static final int GL_SAMPLER_KHR = 33510;
    public static final int GL_SAMPLE_SHADING_OES = 35894;
    public static final int GL_SCREEN_KHR = 37525;
    public static final int GL_SHADER_KHR = 33505;
    public static final int GL_SKIP_DECODE_EXT = 35402;
    public static final int GL_SOFTLIGHT_KHR = 37532;
    public static final int GL_STACK_OVERFLOW_KHR = 1283;
    public static final int GL_STACK_UNDERFLOW_KHR = 1284;
    public static final int GL_STENCIL_INDEX8_OES = 36168;
    public static final int GL_STENCIL_INDEX_OES = 6401;
    public static final int GL_TESS_CONTROL_OUTPUT_VERTICES_EXT = 36469;
    public static final int GL_TESS_CONTROL_SHADER_BIT_EXT = 8;
    public static final int GL_TESS_CONTROL_SHADER_EXT = 36488;
    public static final int GL_TESS_EVALUATION_SHADER_BIT_EXT = 16;
    public static final int GL_TESS_EVALUATION_SHADER_EXT = 36487;
    public static final int GL_TESS_GEN_MODE_EXT = 36470;
    public static final int GL_TESS_GEN_POINT_MODE_EXT = 36473;
    public static final int GL_TESS_GEN_SPACING_EXT = 36471;
    public static final int GL_TESS_GEN_VERTEX_ORDER_EXT = 36472;
    public static final int GL_TEXTURE_2D_MULTISAMPLE_ARRAY_OES = 37122;
    public static final int GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY_OES = 37125;
    public static final int GL_TEXTURE_BINDING_BUFFER_EXT = 35884;
    public static final int GL_TEXTURE_BINDING_CUBE_MAP_ARRAY_EXT = 36874;
    public static final int GL_TEXTURE_BORDER_COLOR_EXT = 4100;
    public static final int GL_TEXTURE_BUFFER_BINDING_EXT = 35882;
    public static final int GL_TEXTURE_BUFFER_DATA_STORE_BINDING_EXT = 35885;
    public static final int GL_TEXTURE_BUFFER_EXT = 35882;
    public static final int GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT = 37279;
    public static final int GL_TEXTURE_BUFFER_OFFSET_EXT = 37277;
    public static final int GL_TEXTURE_BUFFER_SIZE_EXT = 37278;
    public static final int GL_TEXTURE_CUBE_MAP_ARRAY_EXT = 36873;
    public static final int GL_TEXTURE_SRGB_DECODE_EXT = 35400;
    public static final int GL_TRIANGLES_ADJACENCY_EXT = 12;
    public static final int GL_TRIANGLE_STRIP_ADJACENCY_EXT = 13;
    public static final int GL_UNDEFINED_VERTEX_EXT = 33376;
    public static final int GL_UNSIGNED_INT_IMAGE_BUFFER_EXT = 36967;
    public static final int GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY_EXT = 36970;
    public static final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY_OES = 37133;
    public static final int GL_UNSIGNED_INT_SAMPLER_BUFFER_EXT = 36312;
    public static final int GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY_EXT = 36879;
    public static final int GL_VERTEX_ARRAY_KHR = 32884;

    public interface DebugProcKHR {
        void onMessage(int i, int i2, int i3, int i4, String str);
    }

    private static native void _nativeClassInit();

    public static native void glBlendBarrierKHR();

    public static native void glBlendEquationSeparateiEXT(int i, int i2, int i3);

    public static native void glBlendEquationiEXT(int i, int i2);

    public static native void glBlendFuncSeparateiEXT(int i, int i2, int i3, int i4, int i5);

    public static native void glBlendFunciEXT(int i, int i2, int i3);

    public static native void glColorMaskiEXT(int i, boolean z, boolean z2, boolean z3, boolean z4);

    public static native void glCopyImageSubDataEXT(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15);

    public static native void glDebugMessageCallbackKHR(DebugProcKHR debugProcKHR);

    public static native void glDebugMessageControlKHR(int i, int i2, int i3, int i4, IntBuffer intBuffer, boolean z);

    public static native void glDebugMessageControlKHR(int i, int i2, int i3, int i4, int[] iArr, int i5, boolean z);

    public static native void glDebugMessageInsertKHR(int i, int i2, int i3, int i4, String str);

    public static native void glDisableiEXT(int i, int i2);

    public static native void glEnableiEXT(int i, int i2);

    public static native void glFramebufferTextureEXT(int i, int i2, int i3, int i4);

    public static native DebugProcKHR glGetDebugMessageCallbackKHR();

    public static native int glGetDebugMessageLogKHR(int i, int i2, int[] iArr, int i3, int[] iArr2, int i4, int[] iArr3, int i5, int[] iArr4, int i6, int[] iArr5, int i7, byte[] bArr, int i8);

    public static native int glGetDebugMessageLogKHR(int i, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4, IntBuffer intBuffer5, ByteBuffer byteBuffer);

    public static native String[] glGetDebugMessageLogKHR(int i, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4);

    public static native String[] glGetDebugMessageLogKHR(int i, int[] iArr, int i2, int[] iArr2, int i3, int[] iArr3, int i4, int[] iArr4, int i5);

    public static native String glGetObjectLabelKHR(int i, int i2);

    public static native String glGetObjectPtrLabelKHR(long j);

    public static native void glGetSamplerParameterIivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glGetSamplerParameterIivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glGetSamplerParameterIuivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glGetSamplerParameterIuivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glGetTexParameterIivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glGetTexParameterIivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glGetTexParameterIuivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glGetTexParameterIuivEXT(int i, int i2, int[] iArr, int i3);

    public static native boolean glIsEnablediEXT(int i, int i2);

    public static native void glMinSampleShadingOES(float f);

    public static native void glObjectLabelKHR(int i, int i2, int i3, String str);

    public static native void glObjectPtrLabelKHR(long j, String str);

    public static native void glPatchParameteriEXT(int i, int i2);

    public static native void glPopDebugGroupKHR();

    public static native void glPrimitiveBoundingBoxEXT(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8);

    public static native void glPushDebugGroupKHR(int i, int i2, int i3, String str);

    public static native void glSamplerParameterIivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glSamplerParameterIivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glSamplerParameterIuivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glSamplerParameterIuivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glTexBufferEXT(int i, int i2, int i3);

    public static native void glTexBufferRangeEXT(int i, int i2, int i3, int i4, int i5);

    public static native void glTexParameterIivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glTexParameterIivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glTexParameterIuivEXT(int i, int i2, IntBuffer intBuffer);

    public static native void glTexParameterIuivEXT(int i, int i2, int[] iArr, int i3);

    public static native void glTexStorage3DMultisampleOES(int i, int i2, int i3, int i4, int i5, int i6, boolean z);

    static {
        _nativeClassInit();
    }

    private GLES31Ext() {
    }
}
