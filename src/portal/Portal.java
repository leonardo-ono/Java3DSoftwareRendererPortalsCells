package portal;

import bsp3d.Plane;
import bsp3d.Triangle;
import java.util.ArrayList;
import java.util.List;
import math.Vec3;
import renderer.Renderer;

/**
 * Portal class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Portal {
    
    public String name;
    public List<double[]> polygon = new ArrayList<>();
    public List<Triangle> polygon2 = new ArrayList<>(); // TODO: try to eliminate this later
    public Cell cellA;
    public Cell cellB;
    
    private final Plane plane = new Plane();
    private final Vec3 tmpA = new Vec3();
    private final Vec3 tmpB = new Vec3();

    public Portal(String name, List<Triangle> polygonOriginal) {
        this.name = name;
        this.polygon2.addAll(polygonOriginal);
        
        // TODO hardcoded to accept only quads as portals, fix it later ?
                
        polygon.add(polygonOriginal.get(1).getVa());
        polygon.add(polygonOriginal.get(1).getVb());
        polygon.add(polygonOriginal.get(1).getVc());
        polygon.add(polygonOriginal.get(0).getVc());
        plane.set(polygonOriginal.get(0));
//        for (double[] v : polygon) {
//            v[0] *= 1.001;
//            v[1] *= 1.001;
//            v[2] *= 1.001;
//        }
    }
    
    private boolean containsArrayVec(double[] a, List<double[]> polygon) {
        boolean contains = false;
        outer:
        for (double[] b : polygon) {
            for (int i = 0; i < b.length; i++) {
                contains = true;
                if (Math.abs(b[i] - a[i]) > 0.01) {
                    contains = false;
                    continue outer;
                }
            }
            if (contains) {
                break;
            }
        }
        return contains;
    }

    public void linkCells(Cell cellA, Cell cellB) {
        this.cellA = cellA;
        this.cellB = cellB;
        cellA.addPortal(this);
        cellB.addPortal(this);
    }
    
//    public void draw(Renderer renderer, Cell sourceCell) {
//        if (sourceCell == cellB) {
//            drawInternal(renderer, cellA);
//        }
//        else {
//            drawInternal(renderer, cellB);
//        }
//    }
//    
//    private void drawInternal(Renderer renderer, Cell cell) {
//        if (cell.alreadyDrawn()) {
//            return;
//        }
//        renderer.setWireframeEnabled(true);
//        if (renderer.isPortalVisible(polygon)) {
//            cell.draw(renderer);
//        }
//        renderer.setWireframeEnabled(false);
//    }
    
    // TODO
    public int screenSpaceVerticesCount = 0;
    public final List<double[]> screenSpacePolygon = new ArrayList<>();
    public double frontSign = 0;
    
    public boolean isVisible(Renderer renderer) {
        boolean visible = renderer.isPortalVisible(polygon);

        // make a copy of polygon in screen space
        // TODO: find another more decent way to get this ?
        List<double[]> portalScreenSpacePolygon = renderer.getLastScreenSpacePortal();
        screenSpaceVerticesCount = portalScreenSpacePolygon.size();
        screenSpacePolygon.clear();
        
        for (int i = 0; i < screenSpaceVerticesCount; i++) {
            double[] vertexOriginal = portalScreenSpacePolygon.get(i);
            double[] vertexCopy = new double[2];
            vertexCopy[0] = vertexOriginal[0];
            vertexCopy[1] = vertexOriginal[1];
            screenSpacePolygon.add(vertexCopy);
        }
        
        // TODO set front sign
        // portal can be backfaced, so when clipping against other polygons 
        // visible through this portal, it's necessary to keep this sign
        if (!portalScreenSpacePolygon.isEmpty()) {
            double[] va = portalScreenSpacePolygon.get(0);
            double[] vb = portalScreenSpacePolygon.get(1);
            double[] vc = portalScreenSpacePolygon.get(2);
            double x1 = va[0] - vb[0];
            double y1 = va[1] - vb[1];
            double x2 = vc[0] - vb[0];
            double y2 = vc[1] - vb[1];
            frontSign = Math.signum(x1 * y2 - x2 * y1);        
        }
        
        return visible;
    }

    public boolean crossed(double prevX, double prevY, double prevZ
                         , double currX, double currY, double currZ) {

        tmpA.set(prevX, prevY, prevZ);
        tmpB.set(currX, currY, currZ);
        boolean crossedA = plane.isFront(tmpA);
        boolean crossedB = plane.isFront(tmpB);
        return crossedA ^ crossedB;
    }

    public Cell getOppositeCell(Cell currentCell) {
        if (currentCell == cellB) {
            return cellA;
        }
        else {
            return cellB;
        }
    }

}
