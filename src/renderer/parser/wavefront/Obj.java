package renderer.parser.wavefront;

import bsp3d.Triangle;
import java.util.ArrayList;
import java.util.List;
import renderer.Material;

/**
 *
 * @author leonardo
 */
public class Obj {
    
    public String name;
    public List<Triangle> faces = new ArrayList<>();
    public Material material;
    
}
