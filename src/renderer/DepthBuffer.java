package renderer;

import java.util.Arrays;

/**
 * DepthBuffer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class DepthBuffer {

    private final double[] data;

    private final int width;
    private final int height;
    
    private double clearValue = Double.NEGATIVE_INFINITY;
            
    public DepthBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new double[width * height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getClearValue() {
        return clearValue;
    }

    public void setClearValue(float clearValue) {
        this.clearValue = clearValue;
    }
    
    public void clear() {
        Arrays.fill(data, clearValue);
    }
    
    public void set(int x, int y, double depth) {
        data[x + y * width] = depth;
    }
    
    public double get(int x, int y) {
        try {
            return data[x + y * width];
        }
        catch (Exception e) {
            return Double.POSITIVE_INFINITY;
        }
    }
    
}
