package wavefront;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import renderer.Material;
import renderer.Shader;

/**
 *
 * @author leonardo
 */
public class WavefrontParser2 {

//    public static class Face {
//        public Vec4[] vertex;
//        public Vec4[] normal;
//        public Vec2[] texture;
//
//        public Face(Vec4[] vertex, Vec4[] normal, Vec2[] vt) {
//            this.vertex = vertex;
//            this.normal = normal;
//            this.texture = vt;
//        }
//    }
    
    public static List<double[]> vertices = new ArrayList<>();
    public static List<double[]> normals = new ArrayList<>();
    public static List<double[]> textures = new ArrayList<>();
    
    public static Obj2 obj;
    public static List<Obj2> objs = new ArrayList<Obj2>();

    public static List<Obj2> load(Shader shader, String resource, double scaleFactor) throws Exception {
        objs.clear();
        vertices.clear();
        normals.clear();
        textures.clear();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(WavefrontParser2.class.getResourceAsStream(resource)));
        String line = null;
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            if (line.startsWith("mtllib ")) {
                //extractMaterial(shader, line);
            }
            if (line.startsWith("usemtl ")) {
                obj = new Obj2();
                objs.add(obj);
                String materialName = line.substring(7);
                Material material = MaterialParser2.materials.get(materialName);
                obj.material = material;
            }
            else if (line.startsWith("v ")) {
                extractVertex(line, vertices, scaleFactor);
            }
            else if (line.startsWith("vt ")) {
                extractVertexTexture(line, textures);
            }
            else if (line.startsWith("vn ")) {
                extractVertexNormal(line, normals);
            }
            else if (line.startsWith("f ")) {
                extractFace(line, vertices, normals, obj);
            }
        }
        br.close();
        
        int facesCount = 0;
        for (Obj2 obj : objs) {
            facesCount += obj.faces.size();
        }
        System.out.println("Faces count: " + facesCount);
        
        return objs;
    }

    private static void extractMaterial(Shader shader, String line) throws Exception {
        line = line.substring(7);
        MaterialParser2.load(shader, line);
    }
        
    private static void extractVertex(String line, List<double[]> vertices, double scaleFactor) {
        line = line.substring(2).trim();
        String[] v = line.split("\\ ");
        double[] vertex = {
                scaleFactor * Double.parseDouble(v[0]), 
                scaleFactor * Double.parseDouble(v[1]), 
                scaleFactor * Double.parseDouble(v[2]),
                1.0 };
        vertices.add(vertex);
    }

    private static void extractVertexTexture(String line, List<double[]> textures) {
        line = line.substring(3).trim();
        String[] v = line.split("\\ ");
        double[] texture = {
                Double.parseDouble(v[0]), 
                Double.parseDouble(v[1]) };
        textures.add(texture);
    }
    
    private static void extractVertexNormal(String line, List<double[]> normals) {
        line = line.substring(3).trim();
        String[] v = line.split("\\ ");
        double[] normal = {
                Double.parseDouble(v[0]), 
                Double.parseDouble(v[1]), 
                Double.parseDouble(v[2]),
                1.0 };
        normals.add(normal);
    }

    private static void extractFace(String line, List<double[]> vertices, List<double[]> normals, Obj2 obj) {
        List<List<double[]>> faces = obj.faces;
        line = line.substring(2).trim();
        String[] v = line.split("\\ ");
        String[] i1 = v[0].split("/");
        String[] i2 = v[1].split("/");
        String[] i3 = v[2].split("/");
        String[] i4 = null;
        
        double[] p1 = vertices.get(Integer.parseInt(i1[0]) - 1);
        double[] p2 = vertices.get(Integer.parseInt(i2[0]) - 1);
        double[] p3 = vertices.get(Integer.parseInt(i3[0]) - 1);
        double[] p4 = null;
        
        double[] n1 = new double[4];
        double[] n2 = new double[4];
        double[] n3 = new double[4];
        double[] n4 = new double[4];
        
        if (v.length > 3) {
            i4 = v[3].split("/");
            p4 = vertices.get(Integer.parseInt(i4[0]) - 1);
        }

        if (i1.length > 2) {
            n1 = normals.get(Integer.parseInt(i1[2]) - 1);
            n2 = normals.get(Integer.parseInt(i2[2]) - 1);
            n3 = normals.get(Integer.parseInt(i3[2]) - 1);
        }

        if (i1.length > 2 && v.length > 3) {
            n4 = normals.get(Integer.parseInt(i4[2]) - 1);
        }
        
        double[] t1 = new double[2];
        double[] t2 = new double[2];
        double[] t3 = new double[2];
        //double[] t4 = new double[2];
        
        if (!i1[1].trim().isEmpty()) {
            t1 = textures.get(Integer.parseInt(i1[1]) - 1);
            t2 = textures.get(Integer.parseInt(i2[1]) - 1);
            t3 = textures.get(Integer.parseInt(i3[1]) - 1);
//
//            if (v.length > 3) {
//                t4 = textures.get(Integer.parseInt(i4[1]) - 1);
//            }
        }
//        
//        Triangle face = new Triangle(new Vec4[] { p1, p2, p3 }, new Vec4[] { n1, n2, n3 }, new Vec2[] { t1, t2, t3 });
//        faces.add(face);
//
//        if (v.length > 3) {
//            face = new Face(new Vec4[] { p1, p3, p4 }, new Vec4[] { n1, n3, n4 }, new Vec2[] { t1, t3, t4 });
//            faces.add(face);
//        }


        //n1.add(n2);
        //n1.add(n3);
        //n1.normalize();
        //Triangle face = new Triangle(p1, p2, p3, n1, t1, t2, t3, obj.material);
        List<double[]> triangle = new ArrayList<>();
        
        double[] v1 = new double[6];
        v1[0] = p1[0];
        v1[1] = p1[1];
        v1[2] = p1[2];
        v1[3] = p1[3];
        v1[4] = t1[0];
        v1[5] = t1[1];

        double[] v2 = new double[6];
        v2[0] = p2[0];
        v2[1] = p2[1];
        v2[2] = p2[2];
        v2[3] = p2[3];
        v2[4] = t2[0];
        v2[5] = t2[1];

        double[] v3 = new double[6];
        v3[0] = p3[0];
        v3[1] = p3[1];
        v3[2] = p3[2];
        v3[3] = p3[3];
        v3[4] = t3[0];
        v3[5] = t3[1];
        
        triangle.add(v1);
        triangle.add(v2);
        triangle.add(v3);
        
        faces.add(triangle);

        if (v.length > 3) {
            //face = new Triangle(p1, p3, p4, n1);
            //faces.add(face);
            throw new RuntimeException("Wavefront with faces with more than 3 edges !");
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        //load("/res/luigi_circuit.obj", 1);
    }
    
}
