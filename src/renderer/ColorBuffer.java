package renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * ColorBuffer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class ColorBuffer {

    private final BufferedImage colorBuffer;
    private final Graphics2D g2d;
    private final WritableRaster raster;
    private final int[] data;
    
    private final int width;
    private final int height;
    
    public ColorBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        
        colorBuffer = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        
        g2d = (Graphics2D) colorBuffer.getGraphics();
        raster = colorBuffer.getRaster();
        data = ((DataBufferInt) raster.getDataBuffer()).getData();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage getColorBuffer() {
        return colorBuffer;
    }

    public Graphics2D getG2D() {
        return g2d;
    }

    public void clear() {
        Arrays.fill(data, 0);
    }
    
    public void setPixel(int x, int y, int color) {
//        if (data[x + y * width] != 0) {
//            data[x + y * width] = 255;
//            return;
//        }
        data[x + y * width] = color;
    }
    
    public void setPixel(int x, int y, int r, int g, int b) {
        data[x + y * width] = b + (g << 8) + (r << 16) ;
    }

    public void setPixel(int x, int y, int[] c) {
        data[x + y * width] = c[2] + (c[1] << 8) + (c[0] << 16);
    }

    public int getPixel(int x, int y) {
        return data[x + y * width];
    }

    public void getPixel(int x, int y, int[] color) {
        int c = data[x + y * width];
        color[0] = (c & 0x00ff0000) >> 16;
        color[1] = (c & 0x0000ff00) >> 8;
        color[2] = (c & 0x000000ff);
    }
    
}
