package renderer;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import math.Mat4;
import portal.Portal;

/**
 * 3D Renderer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Renderer {

    private final int width;
    private final int height;
    private final int halfWidth;
    private final int halfHeight;
    private final ColorBuffer colorBuffer;
    private final LightBuffer lightBuffer;
    private final DepthBuffer depthBuffer;
    private Material material = new BasicMaterial();
    private final Transform modelTransform = new Transform();
    private final Transform viewTransform = new Transform();
    private final Transform projectionTransform = new Transform();
    private final Mat4 mvpMatrix = new Mat4();
    private boolean backfaceCullingEnabled = true;
    private boolean wireframeEnabled = false;
    
    public static enum ScanType { 
        DEFAULT, ZBUFFER, CBUFFER_BSP, CBUFFER_LINKED_LIST, PORTAL;
        
        Scan scan;
    }
    
    private ScanType scanType = ScanType.DEFAULT;
    
    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.halfWidth = width / 2;
        this.halfHeight = height / 2;
        colorBuffer = new ColorBuffer(width, height);
        lightBuffer = new LightBuffer(width, height);
        depthBuffer = new DepthBuffer(width, height);
        initScans();
    }
    
    private void initScans() {
        ScanType.DEFAULT.scan = new Scan(this);
        ScanType.ZBUFFER.scan = new ZbufferScan(this); 
        ScanType.CBUFFER_BSP.scan = new BspCBufferScan(this);
        ScanType.CBUFFER_LINKED_LIST.scan = new LinkedListCBufferScan(this);
        ScanType.PORTAL.scan = new PortalScan(this);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ColorBuffer getColorBuffer() {
        return colorBuffer;
    }

    public LightBuffer getLightBuffer() {
        return lightBuffer;
    }

    public DepthBuffer getDepthBuffer() {
        return depthBuffer;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Transform getModelTransform() {
        return modelTransform;
    }

    public Transform getViewTransform() {
        return viewTransform;
    }

    // convert from camera space to clipping space
    public Transform getProjectionTransform() {
        return projectionTransform;
    }

    public Mat4 getMvpMatrix() {
        return mvpMatrix;
    }

    public void updateMvpMatrix() {
        mvpMatrix.set(projectionTransform.getMatrix());
        mvpMatrix.multiply(viewTransform.getMatrix());
        mvpMatrix.multiply(modelTransform.getMatrix());
    }

    // note: becareful to not set the scan after calling this method
    public void clearBuffers() {
        colorBuffer.clear();
        //lightBuffer.clear();
        depthBuffer.clear();
        scanType.scan.clear();
    }

    public boolean isBackfaceCullingEnabled() {
        return backfaceCullingEnabled;
    }

    public void setBackfaceCullingEnabled(boolean backfaceCullingEnabled) {
        this.backfaceCullingEnabled = backfaceCullingEnabled;
    }
    
    // use after perspective division
    public boolean isBackFaced(double[] va, double[] vb, double[] vc) {
        double x1 = vb[0] - va[0];
        double y1 = vb[1] - va[1];
        double x2 = vc[0] - va[0];
        double y2 = vc[1] - va[1];
        double z = x1 * y2 - x2 * y1;
        return z > 0;
    }

    public boolean isWireframeEnabled() {
        return wireframeEnabled;
    }

    public void setWireframeEnabled(boolean wireframeEnabled) {
        this.wireframeEnabled = wireframeEnabled;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }
    
    public Scan getScan() {
        return scanType.scan;
    }
    
    // convert from clipping space to ndc (normalized device coordinate)
    // note: in this implementation, the z component is set with 1/z
    //       so it can be used in both z-buffer and texture mapping
    private void doPerspectiveDivision(double[] v) {
        double d = 1.0 / v[3];
        v[0] = v[0] * d;   //=x/w
        v[1] = v[1] * d;   //=y/w
        v[2] = 1.0 / v[2]; //=1/z
        //v[3] = v[3] * d;
    }

    // convert from ndc to screen space
    private void doScreenTransformation(double[] v) {
        v[0] = v[0] * halfWidth + halfWidth + 0.5;
        v[1] = v[1] * -halfHeight + halfHeight - 0.5;
    }
  
//    public boolean isPortalVisible(List<double[]> vertices) {
//        boolean portalVisible = false;
//        double[] va = vertices.get(0);
//        for (int i = 1; i <= vertices.size() - 2; i++) {
//            double[] vb = vertices.get(i);
//            double[] vc = vertices.get((i + 1) % vertices.size());
//            portalVisible |= isPortalVisible(va, vb, vc);
//        }
//        return portalVisible;
//    }
//  
//    // check if a portal is visible
//    public boolean isPortalVisible(double[] va, double[] vb, double[] vc) {
//        boolean previousBackfaceCullingEnabled = backfaceCullingEnabled;
//        backfaceCullingEnabled = false;
//        ScanType previousScanType = scanType;
//        scanType = ScanType.PORTAL;
//        PortalScan portalScan = (PortalScan) scanType.scan;
//        portalScan.clear();
//        drawTriangle(va, vb, vc);
//        scanType = previousScanType;
//        backfaceCullingEnabled = previousBackfaceCullingEnabled;
//        return portalScan.isVisible();
//    }
//  
    
    private final List<double[]> lastScreenSpacePortal = new ArrayList<>();

    public List<double[]> getLastScreenSpacePortal() {
        return lastScreenSpacePortal;
    }
    
    // check if a portal is visible
    public boolean isPortalVisible(List<double[]> vertices) {
        boolean previousBackfaceCullingEnabled = backfaceCullingEnabled;
        backfaceCullingEnabled = false;
        ScanType previousScanType = scanType;
        scanType = ScanType.PORTAL;
        PortalScan portalScan = (PortalScan) scanType.scan;
        portalScan.clear();
        drawPolygon2(vertices, lastScreenSpacePortal, null);
        scanType = previousScanType;
        backfaceCullingEnabled = previousBackfaceCullingEnabled;
        return portalScan.isVisible();
    }    
    
    // draw polygon in local space
//    public void drawPolygon(List<double[]> vertices) {
//        double[] va = vertices.get(0);
//        for (int i=1; i<=vertices.size() - 2; i++) {
//            double[] vb = vertices.get(i);
//            double[] vc = vertices.get((i + 1) % vertices.size());
//            drawTriangle(va, vb, vc);
//        }        
//    }
    
    
    // draw triangle in local space
//    public void drawTriangle(double[] va, double[] vb, double[] vc) {
//        Shader shader = material.getShader();
//        
//        shader.initVerticesCache();
//        
//        double[] vca = shader.getVertexFromCache();
//        double[] vcb = shader.getVertexFromCache();
//        double[] vcc = shader.getVertexFromCache();
//        
//        System.arraycopy(va, 0, vca, 0, va.length);
//        System.arraycopy(vb, 0, vcb, 0, vb.length);
//        System.arraycopy(vc, 0, vcc, 0, vc.length);
//        
//        for (int i = 0; i < shader.variableStartIndex; i += 4) {
//            mvpMatrix.multiply(vca, i);
//            mvpMatrix.multiply(vcb, i);
//            mvpMatrix.multiply(vcc, i);
//        }
//
//        // view frustrum culling / clipping
//        List<double[]> clippedOrCulled 
//                = clipAgainstViewFrustrum(shader, vca, vcb, vcc, va.length);
//        
//        if (clippedOrCulled.isEmpty()) {
//            return;
//        }
//
//        clippedOrCulled.forEach(v -> {
//            doPerspectiveDivision(v);
//            shader.processVertexVariableAttributes(this, v);
//            doScreenTransformation(v);
//        });
//        
//        vca = clippedOrCulled.get(0);
//        for (int i = 1; i <= clippedOrCulled.size() - 2; i++) {
//            vcb = clippedOrCulled.get(i);
//            vcc = clippedOrCulled.get((i + 1) % clippedOrCulled.size());
//
//            // backface culling
//            if (backfaceCullingEnabled && isBackFaced(vca, vcb, vcc)) {
//                return;
//            }
//                    
//            // draw triangle in screen space
//            rasterizeTriangle(vca, vcb, vcc);
//
//            if (wireframeEnabled) {
//                wireframePolygon.reset();
//                wireframePolygon.addPoint((int) vca[0], (int) vca[1]);
//                wireframePolygon.addPoint((int) vcb[0], (int) vcb[1]);
//                wireframePolygon.addPoint((int) vcc[0], (int) vcc[1]);
//                colorBuffer.getG2D().draw(wireframePolygon);
//            }
//        }        
//    }

    private final List<double[]> polygonCopy = new ArrayList<>();
        
    // draw triangle in local space
    public void drawPolygon2(List<double[]> polygon, List<double[]> screenSpacePolygon, Portal portal) {
        Shader shader = material.getShader();
        
        shader.initVerticesCache();
        
        polygonCopy.clear();
        int copyDataSize = polygon.get(0).length;
        
        for (double[] vOriginal : polygon) {
            double[] vCopy = shader.getVertexFromCache();
            System.arraycopy(vOriginal, 0, vCopy, 0, vOriginal.length);
            polygonCopy.add(vCopy);
        }
        
        for (double[] vCopy : polygonCopy) {
            for (int i = 0; i < shader.variableStartIndex; i += 4) {
                mvpMatrix.multiply(vCopy, i);
            }
        }
        
        // view frustrum culling / clipping
        List<double[]> clippedOrCulled 
                = clipAgainstViewFrustrum(shader, polygonCopy, copyDataSize);
        
        if (clippedOrCulled.isEmpty()) {
            return;
        }
        
        clippedOrCulled.forEach(v -> {
            doPerspectiveDivision(v);
            shader.processVertexVariableAttributes(this, v);
        });
        
        if (screenSpacePolygon != null) {
            screenSpacePolygon.clear();
            for (double[] v : clippedOrCulled) {
                screenSpacePolygon.add(new double[] { v[0], v[1]} );
            }
        }

        // portal culling / clipping
        if (portal != null) {
            
            clippedOrCulled = clipAgainstPortal(shader, portal, clippedOrCulled, copyDataSize);

            if (clippedOrCulled.isEmpty()) {
                return;
            }
            
        }
        
        clippedOrCulled.forEach(v -> {
            doScreenTransformation(v);
        });
        
        double[] vca = clippedOrCulled.get(0);
        for (int i = 1; i <= clippedOrCulled.size() - 2; i++) {
            double[] vcb = clippedOrCulled.get(i);
            double[] vcc = clippedOrCulled.get((i + 1) % clippedOrCulled.size());

            // backface culling
            if (backfaceCullingEnabled && isBackFaced(vca, vcb, vcc)) {
                return;
            }
                    
            // draw triangle in screen space
            rasterizeTriangle(vca, vcb, vcc);

            if (wireframeEnabled) {
                wireframePolygon.reset();
                wireframePolygon.addPoint((int) vca[0], (int) vca[1]);
                wireframePolygon.addPoint((int) vcb[0], (int) vcb[1]);
                wireframePolygon.addPoint((int) vcc[0], (int) vcc[1]);
                colorBuffer.getG2D().draw(wireframePolygon);
                
//                colorBuffer.getG2D().setColor(Color.RED);
//                colorBuffer.getG2D().fillOval((int) (vca[0] - 3), (int) (vca[1] - 3), 6, 6);
//                colorBuffer.getG2D().setColor(Color.GREEN);
//                colorBuffer.getG2D().fillOval((int) (vcb[0] - 4), (int) (vcb[1] - 4), 8, 8);
//                colorBuffer.getG2D().setColor(Color.BLUE);
//                colorBuffer.getG2D().fillOval((int) (vcc[0] - 5), (int) (vcc[1] - 5), 10, 10);
            }
        }        
    }
    
    private final Polygon wireframePolygon = new Polygon();

    // --- portal clipping / culling --
    

    private double crossProductArrayVec2D(double[] va, double [] vb, double [] vc) {
        double v1x = va[0] - vb[0];
        double v1y = va[1] - vb[1];
        double v2x = vc[0] - vb[0];
        double v2y = vc[1] - vb[1];
        return v1x * v2y - v2x * v1y;
    }
    
    private List<double[]> clipAgainstPortal(Shader shader, Portal portal, List<double[]> drawPolygon, int dataSize) {
//        portal.screenSpacePolygon.clear();
//        portal.screenSpacePolygon.add(new double[] { 0.0, 0.0 });
//        portal.screenSpacePolygon.add(new double[] { -1.0, 0.0 });
//        portal.screenSpacePolygon.add(new double[] { 0.0, 1.0 });
        //portal.screenSpaceVerticesCount = 3;
        List<double[]> portalPolygon = portal.screenSpacePolygon;
        List<double[]> a = new ArrayList<>();
        List<double[]> b = drawPolygon;
        List<double[]> r = new ArrayList<>();
        double portalCross =  crossProductArrayVec2D(portalPolygon.get(0), portalPolygon.get(1), portalPolygon.get(2));
        for (int i = 0; i < portal.screenSpaceVerticesCount; i++) {
            double[] portal1 = portalPolygon.get(i);
            double[] portal2 = portalPolygon.get((i + 1) % portal.screenSpaceVerticesCount);
            clipLine(shader, portal1, portal2, portalCross, b, r = a, dataSize);
            a = b;
            b = r;
        }
        return r;
    }
    
    private void clipLine(Shader shader, double[] portal1, double[] portal2, double portalCrossSign, List<double[]> polygonOriginal, List<double[]> clipped, int dataSize) {
        clipped.clear();
        for (int i = 0; i < polygonOriginal.size(); i++) {
            double[] poly1 = polygonOriginal.get(i);
            double[] poly2 = polygonOriginal.get((i + 1) % polygonOriginal.size());
            
            // perpendicular dot
            double perpDirX = -portalCrossSign * (portal1[1] - portal2[1]);
            double perpDirY = portalCrossSign * (portal1[0] - portal2[0]);

            // increase the size of portal a little bit to ensure
            // it will not have gap between consecutives cells 
            double perpDirLength = Math.sqrt(perpDirX * perpDirX + perpDirY * perpDirY);
            perpDirX = perpDirX / perpDirLength;
            perpDirY = perpDirY / perpDirLength;

            double vPolyPortalVaX = poly1[0] - portal1[0] + 0.005 * perpDirX;
            double vPolyPortalVaY = poly1[1] - portal1[1] + 0.005 * perpDirY;

            double vPolyPortalVbX = poly2[0] - portal1[0] + 0.005 * perpDirX;
            double vPolyPortalVbY = poly2[1] - portal1[1] + 0.005 * perpDirY;

            double dotVa = perpDirX * vPolyPortalVaX + perpDirY * vPolyPortalVaY;
            double dotVb = perpDirX * vPolyPortalVbX + perpDirY * vPolyPortalVbY;

            if (dotVa >= 0.0) {
                clipped.add(poly1);
            }

            if (dotVa * dotVb < 0.0) {
                double difX = poly2[0] - poly1[0];
                double difY = poly2[1] - poly1[1];
                double s = dotVa / (dotVa - dotVb);
                difX = difX * s;
                difY = difY * s;

                //double[] clippedVertex = { (poly1[0] + difX), (poly1[1] + difY) };
                //clipped.add(clippedVertex);

                double[] clippedVertex = shader.getVertexFromCache();
                lerpVertex(clippedVertex, poly1, poly2, s, dataSize);
                clipped.add(clippedVertex);
            }
        }
    }
//        
//    private List<double[]> clipAgainstPortal(Shader shader, List<double[]> original, Portal portal, int dataSize) {
//        
//        vertices1.clear();
//        vertices1.addAll(original);
//        
//        List<double[]> last = vertices2;
//                
//        for (int i = 0; i < portal.screenSpaceVerticesCount; i++) {
//            double[] la = portal.screenSpacePolygon.get(i);
//            double[] lb = portal.screenSpacePolygon.get((i + 1) % portal.screenSpaceVerticesCount);
//            
//            if ((i & 1) == 0) {
//                clipAgainstLine(shader, vertices1, vertices2, la, lb, portal.frontSign, dataSize);
//                last = vertices2;
//            }
//            else {
//                clipAgainstLine(shader, vertices2, vertices1, la, lb, portal.frontSign, dataSize);
//                last = vertices1;
//            }
//        }
//        return last;
//    }
//
//    // plane: 0=x, 1=y, 2=z
//    private void clipAgainstLine(Shader shader, List<double[]> original
//            , List<double[]> clipped, double[] la, double[] lb, double sign, int dataSize) {
//        
//        clipped.clear();
//        for (int i = 0; i < original.size(); i++) {
//            double[] a = original.get(i);
//            double[] b = original.get((i + 1) % original.size());
//
//            double lx = -(lb[1] - la[1]); // perpendicular
//            double ly = lb[0] - la[0];
//            
//            boolean aIsInside = isFront(lx, ly, a, sign);
//            boolean bIsInside = isFront(lx, ly, b, sign);
//            if (aIsInside) {
//                clipped.add(a);
//            }
//            if (aIsInside ^ bIsInside) {
//
//                double[] abVec = shader.getVertexFromCache();
//                abVec[0] = b[0] - a[0];
//                abVec[1] = b[1] - a[1];
//                double[] lbVec = shader.getVertexFromCache();
//                lbVec[0] = b[0] - la[0];
//                lbVec[1] = b[1] - la[1];
//                double ab2 = lx * abVec[0] + ly * abVec[1];
//                double lb2 = lx * lbVec[0] + ly * lbVec[1];
//                double p = 1.0 - (lb2 / ab2);
//                
//                double[] clippedVertex = shader.getVertexFromCache();
//                lerpVertex(clippedVertex, a, b, p, dataSize);
//                clipped.add(clippedVertex);
//            }
//        }
//    }
//
//    private boolean isFront(double lx, double ly, double[] v, double sign) {
//        if (sign >= 0) {
//            return lx * v[0] + ly * v[1] >= 0.0;
//        }
//        else {
//            return lx * v[0] + ly * v[1] < 0.0;
//        }
//    }
    
    // --- view frustrum clipping / homogeneous clipping --
    
    private List<double[]> vertices1 = new ArrayList<>();
    private List<double[]> vertices2 = new ArrayList<>();
    
    private List<double[]> clipAgainstViewFrustrum(Shader shader
            , double[] oa, double[] ob, double[] oc, int dataSize) {
        
        vertices1.clear();
        vertices1.add(oa);
        vertices1.add(ob);
        vertices1.add(oc);

        clipAgainstPlane(shader, vertices1, vertices2, 0, 1.0, dataSize); // r
        clipAgainstPlane(shader, vertices2, vertices1, 0, -1.0, dataSize); // l
        clipAgainstPlane(shader, vertices1, vertices2, 1, 1.0, dataSize); // t
        clipAgainstPlane(shader, vertices2, vertices1, 1, -1.0, dataSize); // b
        clipAgainstPlane(shader, vertices1, vertices2, 2, 1.0, dataSize); // n
        clipAgainstPlane(shader, vertices2, vertices1, 2, -1.0, dataSize); // f
        return vertices2;
    }
    
    private List<double[]> clipAgainstViewFrustrum(Shader shader
            , List<double[]> verticesOriginal, int dataSize) {
        
        clipAgainstPlane(shader, verticesOriginal, vertices2, 0, 1.0, dataSize); // r
        clipAgainstPlane(shader, vertices2, vertices1, 0, -1.0, dataSize); // l
        clipAgainstPlane(shader, vertices1, vertices2, 1, 1.0, dataSize); // t
        clipAgainstPlane(shader, vertices2, vertices1, 1, -1.0, dataSize); // b
        clipAgainstPlane(shader, vertices1, vertices2, 2, 1.0, dataSize); // n
        clipAgainstPlane(shader, vertices2, vertices1, 2, -1.0, dataSize); // f
        return vertices2;
    }
    
    // plane: 0=x, 1=y, 2=z
    private void clipAgainstPlane(Shader shader, List<double[]> original
            , List<double[]> clipped, int plane, double sign, int dataSize) {
        
        clipped.clear();
        for (int i = 0; i < original.size(); i++) {
            double[] a = original.get(i);
            double[] b = original.get((i + 1) % original.size());
            boolean aIsInside = sign * a[plane] <= a[3];
            boolean bIsInside = sign * b[plane] <= b[3];
            if (aIsInside) {
                clipped.add(a);
            }
            if (aIsInside ^ bIsInside) {
                double p = (a[3] - sign * a[plane]) 
                        / ((a[3] - b[3]) - sign * (a[plane] - b[plane]));
                
                double[] clippedVertex = shader.getVertexFromCache();
                lerpVertex(clippedVertex, a, b, p, dataSize);
                clipped.add(clippedVertex);
            }
        }
    }
    
    private void lerpVertex(
            double[] r, double a[], double b[], double p, int dataSize) {
        
        for (int i = 0; i < dataSize; i++) {
            r[i] = a[i] + p * (b[i] - a[i]);
        }
    }
    
    // --- triangle rasterizer ---

    private void rasterizeTriangle(double[] va, double[] vb, double[] vc) {
        
        // sort vertices by y in screen space
        if (va[1] > vb[1]) {
            double[] tmp = va;
            va = vb;
            vb = tmp;
        } 
        if (vb[1] > vc[1]) {
            double[] tmp = vb;
            vb = vc;
            vc = tmp;
        } 
        if (va[1] > vb[1]) {
            double[] tmp = va;
            va = vb;
            vb = tmp;
        } 
        
        Scan scan = scanType.scan;
        scan.init();
        Shader shader = material.getShader();

        // draw triangle top
        shader.e1.set(va, vb);
        shader.e2.set(va, vc);
        scan.setTop(shader.e1, shader.e2);
        scan.drawTop();

        // draw triangle bottom
        shader.e1.set(vc, va);
        shader.e2.set(vc, vb);
        scan.setBottom(shader.e1, shader.e2);
        scan.drawBottom();
    }
    
}
