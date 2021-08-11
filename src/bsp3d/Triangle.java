package bsp3d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import math.Vec2;
import math.Vec3;
import portal.Portal;
import renderer.Material;
import renderer.Renderer;

/**
 *
 * @author Leo
 */
public class Triangle implements Comparable<Triangle>, Serializable {

    public Vec3 a;
    public Vec3 b;
    public Vec3 c;
    public Vec3 normal = new Vec3();
    
    public Vec2 uvA;
    public Vec2 uvB;
    public Vec2 uvC;
    
    public String materialId;
    public transient Material material;
    
    private final Vec3 p1Tmp = new Vec3();
    
    private static Stroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    
    private static Color[] colors = new Color[256];
    
    static {
        for (int c = 0; c < 256; c++) {
            colors[c] = new Color(c, c, c, 255);
        }
    }
    
    public Triangle(Vec3 a, Vec3 b, Vec3 c, Vec3 n, Vec2 uvA, Vec2 uvB, Vec2 uvC, Material material) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.normal = n;
        this.uvA = uvA;
        this.uvB = uvB;
        this.uvC = uvC;
        this.material = material;
        if (uvA == null || uvB == null || uvC == null || material == null || normal == null || a == null || b == null || c == null) {
            throw new RuntimeException("Triangle will null arguments in constructor !");
        }
    }
    
//    public Triangle(Vec3 a, Vec3 b, Vec3 c) {
//        this.a = a;
//        this.b = b;
//        this.c = c;
//
//        p1Tmp.set(a);
//        p1Tmp.sub(b);
//        normal.set(c);
//        normal.sub(b);
//        normal.cross(p1Tmp);
//    }
    
    private static Polygon polygon = new Polygon();
    
    public void draw(Graphics2D g) {
        polygon.reset();
        polygon.addPoint((int) (a.x + 0), (int) (a.y + 0));
        polygon.addPoint((int) (b.x + 0), (int) (b.y + 0));
        polygon.addPoint((int) (c.x + 0), (int) (c.y + 0));
        g.draw(polygon);
    }

    Vec3 wa = new Vec3();
    Vec3 wb = new Vec3();
    Vec3 wc = new Vec3();
    
    private static Plane plane = new Plane(new Vec3(0, 0, 1.0), new Vec3(0, 0, 0.01));
    Player playerTmp = new Player();

    double[] va = new double[6];
    double[] vb = new double[6];
    double[] vc = new double[6];
    
    public double[] getVa() {
        va[0] = a.x;
        va[1] = a.y;
        va[2] = a.z;
        va[3] = 1.0;
        va[4] = uvA.x;
        va[5] = uvA.y;
        return va;
    }

    public double[] getVb() {
        vb[0] = b.x;
        vb[1] = b.y;
        vb[2] = b.z;
        vb[3] = 1.0;
        vb[4] = uvB.x;
        vb[5] = uvB.y;
        return vb;
    }

    public double[] getVc() {
        vc[0] = c.x;
        vc[1] = c.y;
        vc[2] = c.z;
        vc[3] = 1.0;
        vc[4] = uvC.x;
        vc[5] = uvC.y;
        return vc;
    }
    
    
    
    public void draw3D(Renderer renderer, Player player, Portal renderCullingPortal) {
        renderer.setMaterial(material);
//        renderer.begin();
//                if (uvA != null) renderer.setTextureCoordinates(uvA.x, uvA.y);
//                //renderer.setTextureCoordinates(0, 0);
//                renderer.setNormal(-1, -1, 0);
//                renderer.setVertex(a.x, a.y, a.z);
//
//                if (uvB != null) renderer.setTextureCoordinates(uvB.x, uvB.y);
//                //renderer.setTextureCoordinates(1, 0);
//                renderer.setNormal(-1, 1, 0);
//                renderer.setVertex(b.x, b.y, b.z);
//
//                if (uvC != null) renderer.setTextureCoordinates(uvC.x, uvC.y);
//                //renderer.setTextureCoordinates(1, 1);
//                renderer.setNormal(1, 1, 0);
//                renderer.setVertex(c.x, c.y, c.z);
//                
//        renderer.end();        
        va[0] = a.x;
        va[1] = a.y;
        va[2] = a.z;
        va[3] = 1.0;
        va[4] = uvA.x;
        va[5] = uvA.y;

        vb[0] = b.x;
        vb[1] = b.y;
        vb[2] = b.z;
        vb[3] = 1.0;
        vb[4] = uvB.x;
        vb[5] = uvB.y;

        vc[0] = c.x;
        vc[1] = c.y;
        vc[2] = c.z;
        vc[3] = 1.0;
        vc[4] = uvC.x;
        vc[5] = uvC.y;
        
        polygonList.clear();
        polygonList.add(va);
        polygonList.add(vb);
        polygonList.add(vc);
        
        //renderer.drawTriangle(va, vb, vc);
        renderer.drawPolygon2(polygonList, null, renderCullingPortal);
        
    }
    
    private final List<double[]> polygonList = new ArrayList<>();
    
    @Override
    public String toString() {
        return "Triangle{" + "a=" + a + ", b=" + b + ", c=" + c + ", normal=" + normal + '}';
    }

    @Override
    public int compareTo(Triangle o) {
        return (int) Math.signum((o.a.z + o.b.z + o.c.z) / 3 - (a.z + b.z + c.z) / 3);
    }


}
