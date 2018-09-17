package android.opengl;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

abstract class GLWrapperBase implements GL, GL10, GL10Ext, GL11, GL11Ext, GL11ExtensionPack {
    protected GL10 mgl;
    protected GL10Ext mgl10Ext;
    protected GL11 mgl11;
    protected GL11Ext mgl11Ext;
    protected GL11ExtensionPack mgl11ExtensionPack;

    public GLWrapperBase(GL gl) {
        this.mgl = (GL10) gl;
        if (gl instanceof GL10Ext) {
            this.mgl10Ext = (GL10Ext) gl;
        }
        if (gl instanceof GL11) {
            this.mgl11 = (GL11) gl;
        }
        if (gl instanceof GL11Ext) {
            this.mgl11Ext = (GL11Ext) gl;
        }
        if (gl instanceof GL11ExtensionPack) {
            this.mgl11ExtensionPack = (GL11ExtensionPack) gl;
        }
    }
}
