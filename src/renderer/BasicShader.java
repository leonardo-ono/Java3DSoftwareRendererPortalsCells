package renderer;

/**
 * BasicShader class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class BasicShader extends Shader {
    
    private static final int DATA_SIZE = 6;
    private static final int VAR_START_INDEX = 4;
    
    private Texture texture;
    
    public BasicShader() {
        super(DATA_SIZE, VAR_START_INDEX);
        texture = new Texture("/res/brick.jpg");
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void processVertexVariableAttributes(
                        Renderer renderer, double[] dataInOut) {
        
        double zInv = dataInOut[2];
        dataInOut[4] = (511.99 * dataInOut[4]) * zInv;
        dataInOut[5] = (511.99 * (1.0 - dataInOut[5])) * zInv;
    }
    

    private static final double INV_MAX_COLOR = 1.0 / 256.0;
    @Override
    public void processPixel(int x, int y, Renderer renderer, double[] data) {
        double z = 1.0 / data[2];
        double u = data[4] * z;
        double v = data[5] * z;
        int c = texture.getPixel(u, v);
//        if (z > 192) {
//            int r = (c >> 16);
//            int g = (c >> 8) & 255;
//            int b = c & 255;
//            r = (int) (r * (1.0 - z * INV_MAX_COLOR));
//            g = (int) (g * (1.0 - z * INV_MAX_COLOR));
//            b = (int) (b * (1.0 - z * INV_MAX_COLOR));
//            renderer.getColorBuffer().setPixel(x, y, r, g, b);
//        }
//        else {
            renderer.getColorBuffer().setPixel(x, y, c);
//        }
    }
    
}
