package renderer;

/**
 * PortalScan class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class PortalScan extends Scan {

    private boolean visible;
    
    public PortalScan(Renderer renderer) {
        super(renderer);
    }

    @Override
    public void clear() {
        visible = false;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    @Override
    protected void drawTop() {
        for (int y = y1; y <= y2; y++) {
            initX();
            for (int x = x1; x < x2; x++) {
                if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                    visible = true;
                    return;
                }
                nextX();
            }
            e1.nextTopY();
            e2.nextTopY();
        }
    }

    @Override
    protected void drawBottom() {
        if (visible) {
            return;
        }
        for (int y = y1; y > y2; y--) {
            initX();
            for (int x = x1; x < x2; x++) {
                if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                    visible = true;
                    return;
                }
                nextX();
            }
            e1.nextBottomY();
            e2.nextBottomY();
        }
    }
    
    @Override
    protected void initX() {
        x1 = (int) e1.curValuesY[0];
        x2 = (int) e2.curValuesY[0];
        double nx = 1.0 / (x2 - x1 + 1e-200);
        shader.deltaPerX[2] = (e2.curValuesY[2] - e1.curValuesY[2]) * nx; //=1/z
        shader.curValuesX[2] = e1.curValuesY[2];
    }

    @Override
    protected void nextX() {
        shader.curValuesX[2] += shader.deltaPerX[2]; //=1/z
    }

}
