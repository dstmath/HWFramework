package android.filterpacks.imageproc;

import android.filterfw.core.FilterContext;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;

public class BlendFilter extends ImageCombineFilter {
    private final String mBlendShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float blend;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 colorL = texture2D(tex_sampler_0, v_texcoord);\n  vec4 colorR = texture2D(tex_sampler_1, v_texcoord);\n  float weight = colorR.a * blend;\n  gl_FragColor = mix(colorL, colorR, weight);\n}\n";

    public BlendFilter(String name) {
        super(name, new String[]{"left", "right"}, "blended", "blend");
    }

    protected Program getNativeProgram(FilterContext context) {
        throw new RuntimeException("TODO: Write native implementation for Blend!");
    }

    protected Program getShaderProgram(FilterContext context) {
        return new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float blend;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 colorL = texture2D(tex_sampler_0, v_texcoord);\n  vec4 colorR = texture2D(tex_sampler_1, v_texcoord);\n  float weight = colorR.a * blend;\n  gl_FragColor = mix(colorL, colorR, weight);\n}\n");
    }
}
