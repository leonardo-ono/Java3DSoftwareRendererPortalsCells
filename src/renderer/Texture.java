package renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import static renderer.Texture.Extrapolation.*;
import static renderer.Texture.Filter.*;

/**
 * Texture class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Texture {

    public static enum Filter { BILINEAR, CLOSEST }
    public static enum Extrapolation { NONE, REPEAT, EXTEND, CLIP }
    
    private final String id;
    private BufferedImage image;
    private Raster textureRaster;
    private int[] textureData;
    private Filter filter = CLOSEST;
    private Extrapolation extrapolation = NONE;
    private int width;
    private int height;
    private double widthDouble;
    private double heightDouble;
    
    public Texture(String id) {
        this.id = id;
        try {
            BufferedImage imageTmp = ImageIO.read(
                    getClass().getResourceAsStream(id));
            
            BufferedImage image = new BufferedImage(imageTmp.getWidth()
                        , imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
            
            image.getGraphics().drawImage(imageTmp, 0, 0, null);
            setImageInternal(image);
        } catch (IOException ex) {
            Logger.getLogger(Texture.class.getName())
                                .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
    }

    public String getId() {
        return id;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        setImageInternal(image);
    }
    
    private void setImageInternal(BufferedImage image) {
        this.image = image;
        textureRaster = image.getRaster();
        textureData = ((DataBufferInt) textureRaster.getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        widthDouble = width - 1e-2;
        heightDouble = height - 1e-2;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Extrapolation getExtrapolation() {
        return extrapolation;
    }

    // ref.: https://www.soft8soft.com/reply/19163/
    public void setExtrapolation(Extrapolation extrapolation) {
        this.extrapolation = extrapolation;
    }
    
    public int getPixel(double s, double t) {
        switch (extrapolation) {
            case NONE ->   { return getPixelWithFilter(s, t); }
            case CLIP ->   { return getPixelClip(s, t); }
            case EXTEND -> { return getPixelExtend(s, t); }
            case REPEAT -> { return getPixelRepeat(s, t); }
        }
        return 0;
    }

    private int getPixelRepeat(double s, double t) {
        s -= (int) s;
        t -= (int) t;
        if (s < 0) {
            s += 1.0;
        }
        if (t < 0) {
            t += 1.0;
        }
        return getPixelWithFilter(s, t);
    }
    
    private int getPixelExtend(double s, double t) {
        if (s < 0) {
            s = 0.0;
        }
        else if (s > 1.0) {
            s = 1.0;
        }
        if (t < 0) {
            t = 0.0;
        }
        else if (t > 1.0) {
            t = 1.0;
        }
        return getPixelWithFilter(s, t);
    }

    private int getPixelClip(double s, double t) {
        if (s < 0 || t < 0 || s > 1.0 || t > 1.0) {
            return 0;
        }
        return getPixelWithFilter(s, t);
    }
    
    private int getPixelWithFilter(double s, double t) {
        switch (filter) {
            case CLOSEST ->  { return getPixelClosest(s, t); }
            case BILINEAR -> { return getPixelBilinearFilter(s, t); }
        }
        return 0;
    }
    
    int[] colors = new int[] { 1, 2, 3 };
    
    // test.: i change s & t with the texture width and height for testing
    //        apparently faster
    private int getPixelClosest(double s, double t) {
        //int tx = (int) (s * widthDouble * 0.01);
        //int ty = (int) ((1 - t) * heightDouble * 0.01);
        int tx = (int) s;
        int ty = (int) t;
        colors[2] = textureData[tx + ty * width];
        return colors[2];
    }
    
    // note: very slow !
    @Deprecated
    private int getPixelBilinearFilterSlow(double s, double t) {
        double txd = s * (widthDouble - 0.5);
        double tyd = (1.0 - t) * (heightDouble - 0.5);
        double txds = txd - (int) txd;
        double tyds = tyd - (int) tyd;
        int tx = (int) txd;
        int ty = (int) tyd;
        int tx2 = tx < width - 1 ? tx + 1 : tx;
        int ty2 = ty < height - 1 ? ty + 1 : ty;

        int colorY1 = lerpColor(textureData[tx + ty * width]
                            , textureData[tx2 + ty * width], txds);
        
        int colorY2 = lerpColor(textureData[tx + ty2 * width]
                            , textureData[tx2 + ty2 * width], txds);
        
        return lerpColor(colorY1, colorY2, tyds);
    }

    private int lerpColor(int c1, int c2, double p) {
        int r1 = c1 & 255;
        int g1 = (c1 >> 8) & 255;
        int b1 = (c1 >> 16) & 255;
        int r2 = c2 & 255;
        int g2 = (c2 >> 8) & 255;
        int b2 = (c2 >> 16) & 255;
        int rp = (int) (r1 + p * (r2 - r1));
        int gp = (int) (g1 + p * (g2 - g1));
        int bp = (int) (b1 + p * (b2 - b1));
        return rp + (gp << 8) + (bp << 16);
    }
    
    // little bit faster than previous method getPixelBilinearFilter()
    // ref.: https://en.wikipedia.org/wiki/Bilinear_interpolation
    private int getPixelBilinearFilter(double s, double t) {
        double txd = s * (widthDouble - 0.5);
        double tyd = (1.0 - t) * (heightDouble - 0.5);
        double txds = txd - (int) txd;
        double tyds = tyd - (int) tyd;
        int tx = (int) txd;
        int ty = (int) tyd;
        int tx2 = tx < width - 1 ? tx + 1 : tx;
        int ty2 = ty < height - 1 ? ty + 1 : ty;
        int c00 = textureData[tx + ty * width];
        int c01 = textureData[tx2 + ty * width];
        int c10 = textureData[tx + ty2 * width];
        int c11 = textureData[tx2 + ty2 * width];
        int r00 = (c00 >> 16) & 255;
        int g00 = (c00 >> 8) & 255;
        int b00 = c00 & 255;
        int r01 = (c01 >> 16) & 255;
        int g01 = (c01 >> 8) & 255;
        int b01 = c01 & 255;
        int r10 = (c10 >> 16) & 255;
        int g10 = (c10 >> 8) & 255;
        int b10 = c10 & 255;
        int r11 = (c11 >> 16) & 255;
        int g11 = (c11 >> 8) & 255;
        int b11 = c11 & 255;
        double d00 = ((1.0 - txds) * (1.0 - tyds));
        double d01 = (txds * (1.0 - tyds));
        double d10 = ((1.0 - txds) * tyds);
        double d11 = (txds * tyds);
        int rd = (int) (d00 * r00 + d01 * r01 + d10 * r10 + d11 * r11);  
        int gd = (int) (d00 * g00 + d01 * g01 + d10 * g10 + d11 * g11);  
        int bd = (int) (d00 * b00 + d01 * b01 + d10 * b10 + d11 * b11);
        return bd + (gd << 8) + (rd << 16);
    }
    
}
