package renderer;

/**
 * Scan class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Scan {

    Renderer renderer;
    Shader shader;
    DepthBuffer depthBuffer;
    
    Edge e1;
    Edge e2;
    
    int x1, x2, y1, y2;
    
    public Scan(Renderer renderer) {
        this.renderer = renderer;
        this.depthBuffer = renderer.getDepthBuffer();
    }
    
    public void init() {
        this.shader = renderer.getMaterial().getShader();
    }

    public void clear() {
    }    
    
    public boolean isFinished() {
        return false;
    }    
    
    public void setTop(Edge e1, Edge e2) {
        this.e1 = e1;
        this.e2 = e2;
        this.y1 = e1.ay;
        this.y2 = e1.by;
        if (e1.deltaPerY[0] > e2.deltaPerY[0]) {
            swapEdges();
        }
    }
    
    public void setBottom(Edge e1, Edge e2) {
        this.e1 = e1;
        this.e2 = e2;
        this.y1 = e2.ay;
        this.y2 = e2.by;
        if (e2.deltaPerY[0] > e1.deltaPerY[0]) {
            swapEdges();
        }
    }

    protected void swapEdges() {
        Edge tmp = e1;
        e1 = e2;
        e2 = tmp;
    }
        
    protected void drawTop() {
        for (int y = y1; y <= y2; y++) {
            drawScanlines(y);
            e1.nextTopY();
            e2.nextTopY();
        }
    }

    protected void drawBottom() {
        for (int y = y1; y > y2; y--) {
            drawScanlines(y);
            e1.nextBottomY();
            e2.nextBottomY();
        }
    }
    
    protected void drawScanlines(int y) {
        initX();
        for (int x = x1; x < x2; x++) {
            if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                shader.processPixel(x, y, renderer, shader.curValuesX);
                depthBuffer.set(x, y, shader.curValuesX[2]);
            }
            nextX();
        }
    }
    
    protected void initX() {
        x1 = (int) e1.curValuesY[0];
        x2 = (int) e2.curValuesY[0];
        double nx = 1.0 / (x2 - x1 + 1e-200);
        shader.deltaPerX[2] = (e2.curValuesY[2] - e1.curValuesY[2]) * nx; //=1/z
        shader.curValuesX[2] = e1.curValuesY[2];
        for (int i = shader.variableStartIndex; i < shader.dataSize; i++) {
            shader.deltaPerX[i] = (e2.curValuesY[i] - e1.curValuesY[i]) * nx;
            shader.curValuesX[i] = e1.curValuesY[i];
        }
    }

    protected void nextX() {
        shader.curValuesX[2] += shader.deltaPerX[2]; //=1/z
        for (int i = shader.variableStartIndex; i < shader.dataSize; i++) {
            shader.curValuesX[i] += shader.deltaPerX[i];
        }
    }

    protected void nextX(int x) {
        shader.curValuesX[2] = e1.curValuesY[2] + x * shader.deltaPerX[2];//=1/z
        for (int i = shader.variableStartIndex; i < shader.dataSize; i++) {
            shader.curValuesX[i] = e1.curValuesY[i] + x * shader.deltaPerX[i];
        }
    }
    
}
