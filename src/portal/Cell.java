package portal;

import bsp3d.Triangle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.Vec3;
import renderer.Renderer;
import renderer.parser.wavefront.Obj;
import renderer.parser.wavefront.WavefrontParser;

/**
 * Cell class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Cell {
    
    public String name;
    private static long drawnId = 0;
    private long drawn;
    
    private List<Portal> portals = new ArrayList<>();
    private List<Triangle> mesh = new ArrayList<>();

    public Cell(String name, List<Triangle> mesh) {
        this.name = name;
        this.mesh.addAll(mesh);
    }

    public Cell(String resource, double scale) {
        try {
            name = resource;
            WavefrontParser.load(resource, scale);
            for (Obj obj : WavefrontParser.objs) {
                mesh.addAll(obj.faces);
            }
        } catch (Exception ex) {
            Logger.getLogger(Cell.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    public void addPortal(Portal portal) {
        portals.add(portal);
    }
    
    public boolean alreadyDrawn() {
        return drawnId == drawn;
    }
    
    private List<Cell> cellsToDraw = new ArrayList<>();
    private List<Cell> cellsToDraw2 = new ArrayList<>();
    
    public void draw(Renderer renderer) {
        renderCullingPortal = null;
        drawnId++;
        cellsToDraw.clear();
        cellsToDraw2.clear();
        cellsToDraw.add(this);
        while (!cellsToDraw.isEmpty()) {
            cellsToDraw.forEach(cellToDraw -> 
                cellToDraw.drawInternal(renderer, cellsToDraw2));
            
            cellsToDraw.clear();
            cellsToDraw.addAll(cellsToDraw2);
            cellsToDraw2.clear();
        }
    }

    public Portal renderCullingPortal;
    
    private void drawInternal(Renderer renderer, List<Cell> cellsToDraw2) {
        if (drawn != drawnId && !renderer.getScan().isFinished()) {
            drawn = drawnId;
            mesh.forEach(triangle -> triangle.draw3D(renderer, null, renderCullingPortal)); 
            portals.forEach(portal -> {
                //boolean portalVisibleOriginal = renderer.isWireframeEnabled();
                //renderer.setWireframeEnabled(true);
                if (portal.isVisible(renderer)) {
                    if (!portal.cellA.alreadyDrawn()) {
                        cellsToDraw2.add(portal.cellA);
                        portal.cellA.renderCullingPortal = portal;
                    }
                    if (!portal.cellB.alreadyDrawn()) {
                        cellsToDraw2.add(portal.cellB);
                        portal.cellB.renderCullingPortal = portal;
                    }
                }
                //renderer.setWireframeEnabled(portalVisibleOriginal);
            }); 
        }
    }
    
    public Cell moveCamera(double prevX, double prevY, double prevZ
                                , double currX, double currY, double currZ) {
        
        for (Portal portal : portals) {
            if (portal.crossed(prevX, prevY, prevZ, currX, currY, currZ)) {
                return portal.getOppositeCell(this);
            }
        }
        return this;
    }


    private static List<Vec3> allPortalVertices = new ArrayList<>();
    private static List<Vec3> allPortalVerticesChecking = new ArrayList<>();
    private static List<Vec3> allCellVertices = new ArrayList<>();
    private static Vec3 tmpV = new Vec3();
    
    public boolean containsPortal(Portal portal) { 
        allPortalVertices.clear();
        allCellVertices.clear();
        allPortalVerticesChecking.clear();
        
        for (Triangle triangle : portal.polygon2) {
            allPortalVertices.add(triangle.a);
            allPortalVertices.add(triangle.b);
            allPortalVertices.add(triangle.c);
        }
        allPortalVerticesChecking.addAll(allPortalVertices);
                
        for (Triangle triangle : mesh) {
            allCellVertices.add(triangle.a);
            allCellVertices.add(triangle.b);
            allCellVertices.add(triangle.c);
        }
        
        outer:
        for (Vec3 vc : allCellVertices) {
            for (Vec3 vp : allPortalVertices) {
                tmpV.set(vc);
                tmpV.sub(vp);
                if (tmpV.getLength() < 0.1) {
                    allPortalVerticesChecking.remove(vp);
                    if (allPortalVerticesChecking.isEmpty()) {
                        break outer;
                    }
                    continue outer;
                }
            }
        }

        if (allPortalVerticesChecking.isEmpty()) {
            return true;
        }
        
        return false;
    }
    
}
