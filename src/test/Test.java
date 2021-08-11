package test;

import java.util.ArrayList;
import java.util.List;
import portal.Level;
import renderer.Renderer;

/**
 *
 * @author Leo
 */
public class Test {

    public static void main(String[] args) {
        Level level = new Level("/res/house.obj", 20.0);
    }
    
    private static void test2() {
        double d = 0;
        double fov = Math.toRadians(60.0);
        
        d = 300.0 / Math.tan(fov / 2);
        
        double vfov = Math.tan(300.0 / d);
        
        System.out.println("d = " + d);
        System.out.println("vfov = " + Math.toDegrees(vfov));
    }
    
    private static void test() {
        Renderer renderer = new Renderer(800, 600);
        renderer.clearBuffers();
        List<double[]> triangle = new ArrayList<>();
        triangle.add(new double[] { -0.5, -0.5, 0.0, 1.0 });
        triangle.add(new double[] { -0.5,  0.5, 0.0, 1.0 });
        triangle.add(new double[] { -0.5, -0.5, 0.0, 1.0 });
        renderer.getModelTransform().rotateZ(Math.toRadians(45));
        //renderer.drawTriangle(triangle.get(0), triangle.get(1), triangle.get(2));
    }
    
}
