package renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Shader class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public abstract class Shader {
    
    protected final int dataSize;
    protected final int variableStartIndex;
    
    Edge e1;
    Edge e2;
    double[] deltaPerX;
    double[] curValuesX;
    List<double[]> verticesCache = new ArrayList<>();
    int verticesCacheIndex;
    
    public Shader(int dataSize, int variableStartIndex) {
        this.dataSize = dataSize;
        this.variableStartIndex = variableStartIndex;
        this.e1 = new Edge(dataSize, variableStartIndex);
        this.e2 = new Edge(dataSize, variableStartIndex);
        this.deltaPerX = new double[dataSize];
        this.curValuesX = new double[dataSize];
    }
    
    // invoked after perspective division
    public abstract void processVertexVariableAttributes(
                            Renderer renderer, double[] dataInOut);

    public abstract void processPixel(
                            int x, int y, Renderer renderer, double[] data);

    void initVerticesCache() {
        verticesCacheIndex = 0;
    }

    double[] getVertexFromCache() {
        double[] cachedVertex;
        if (verticesCacheIndex > (verticesCache.size() - 1)) {
            cachedVertex = new double[dataSize];
            verticesCache.add(cachedVertex);
            verticesCacheIndex++;
        }
        else {
            cachedVertex = verticesCache.get(verticesCacheIndex++);
        }
        return cachedVertex;
    }
    
}
