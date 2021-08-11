package renderer;

/**
 * Edge class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Edge {

    final int dataSize;
    final int variableStartIndex;
    final double[] deltaPerY;
    final double[] curValuesY;
    int ay;
    int by;
    
    public Edge(int dataSize, int variableStartIndex) {
        this.dataSize = dataSize;
        this.variableStartIndex = variableStartIndex;
        deltaPerY = new double[dataSize];
        curValuesY = new double[dataSize];
    }

    public void set(double[] a, double[] b) {
        ay = (int) Math.floor(a[1]);
        by = (int) Math.floor(b[1]);
        double n = 1.0 / (by - ay + 1e-200);
        
        deltaPerY[0] = (b[0] - a[0]) * n; // = x/w
        deltaPerY[2] = (b[2] - a[2]) * n; // = 1/z
        curValuesY[0] = a[0];
        curValuesY[2] = a[2];
        
        for (int i = variableStartIndex; i < dataSize; i++) {
            deltaPerY[i] = (b[i] - a[i]) * n;
            curValuesY[i] = a[i];
        }
    }
    
    public void nextTopY() {
        curValuesY[0] += deltaPerY[0]; //=x/w
        curValuesY[2] += deltaPerY[2]; //=1/z
        
        for (int i = variableStartIndex; i < dataSize; i++) {
            curValuesY[i] += deltaPerY[i];
        }
    }

    public void nextBottomY() {
        curValuesY[0] -= deltaPerY[0]; //=x/w
        curValuesY[2] -= deltaPerY[2]; //=1/z
        
        for (int i = variableStartIndex; i < dataSize; i++) {
            curValuesY[i] -= deltaPerY[i];
        }
    }
    
}
