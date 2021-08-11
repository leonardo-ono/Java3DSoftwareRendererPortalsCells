package renderer;

/**
 * ZbufferScan class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class ZbufferScan extends Scan {

    int totalPixels;
    
    public ZbufferScan(Renderer renderer) {
        super(renderer);
    }
    
    @Override
    public void clear() {
        totalPixels = renderer.getWidth() * renderer.getHeight();
    }    
    
    @Override
    public boolean isFinished() {
        return totalPixels <= 0;
    }    
    
    @Override
    protected void drawScanlines(int y) {
        initX();
        for (int x = x1; x < x2; x++) {
            if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                totalPixels--;
                shader.processPixel(x, y, renderer, shader.curValuesX);
                depthBuffer.set(x, y, shader.curValuesX[2]);
            }
            nextX();
        }
    }
    
}
