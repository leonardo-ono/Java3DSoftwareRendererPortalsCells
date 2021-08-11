package renderer;

/**
 * BasicMaterial class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class BasicMaterial extends Material {

    public BasicMaterial() {
        super("basic", new BasicShader());
    }
    
}
