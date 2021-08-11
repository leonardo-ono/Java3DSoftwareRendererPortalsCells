package renderer;

import math.Vec4;

/**
 * Material class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Material {
    
    protected String name;
    protected Shader shader;

    public double ns;
    public Vec4 ka = new Vec4();
    public Vec4 kd = new Vec4();
    public Vec4 ks = new Vec4();
    public double ni;
    public double d;
    public double illum;
    public Texture map_kd;
    public Texture map_ka;
    
    public Material(String name, Shader shader) {
        this.name = name;
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }
    
}
