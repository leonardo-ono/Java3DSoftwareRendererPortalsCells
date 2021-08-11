package renderer.parser.wavefront;


import java.awt.Image;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import renderer.BasicShader;
import renderer.Material;
import renderer.Shader;
import renderer.Texture;


/**
 *
 * @author leonardo
 */
public class MaterialParser {

    private static String resourcePath = "/res/";
    public static Map<String, Material> materials = new HashMap<String, Material>();

    public static void load(String resource) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(MaterialParser.class.getResourceAsStream(resourcePath + resource)));
        String line = null;
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            if (line.startsWith("newmtl ")) {
                extractMaterial(br, line);
            }
        }
        br.close();
    }
    
    private static void extractMaterial(BufferedReader br, String line) throws Exception {
        String materialName = line.substring(7);
        Shader shader = new BasicShader();
        Material material = new Material(materialName, shader);
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.trim().isEmpty()) {
                break;
            }
            else if (line.startsWith("Ns ")) {
                material.ns = Double.parseDouble(line.substring(3));
            }
            else if (line.startsWith("Ka ")) {
                String[] values = line.substring(3).split("\\ ");
                material.ka.set(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), 0);
            }
            else if (line.startsWith("Kd ")) {
                String[] values = line.substring(3).split("\\ ");
                material.kd.set(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), 0);
            }
            else if (line.startsWith("Ks ")) {
                String[] values = line.substring(3).split("\\ ");
                material.ks.set(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), 0);
            }
            else if (line.startsWith("Ni ")) {
                material.ni = Double.parseDouble(line.substring(3));
            }
            else if (line.startsWith("d ")) {
                material.d = Double.parseDouble(line.substring(2));
            }
            else if (line.startsWith("illum ")) {
                material.illum = Double.parseDouble(line.substring(6));
            }
            else if (line.startsWith("map_Kd ")) {
                material.map_kd = new Texture("/res/bricks2.png"); //extractJustFilename(line));
                ((BasicShader) material.getShader()).setTexture(material.map_kd);
            }
            else if (line.startsWith("map_Ka ")) {
                material.map_ka = new Texture(extractJustFilename(line));
            }
        }
        materials.put(materialName, material);
    }

    private static String extractJustFilename(String line) {
        int i = line.lastIndexOf("\\");
        if (i<0) {
            i = line.lastIndexOf("/");
        }
        if (i >= 0) {
            line = line.substring(i);
        }
        else {
            line = line.substring(line.lastIndexOf(" ") + 1);
        }
        line = resourcePath + line;
        return line;
    }
    
}
