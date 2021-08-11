package portal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import renderer.parser.wavefront.Obj;
import renderer.parser.wavefront.WavefrontParser;

/**
 * Level class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Level {

    private String name;
    private final List<Obj> objs = new ArrayList<>();
    private final List<Cell> cells = new ArrayList<>();
    private final List<Portal> portals = new ArrayList<>();
    public Cell currentCell;
    
    public Level(String resource, double scale) {
        try {
            name = resource;
            WavefrontParser.load(resource, scale);
            objs.addAll(WavefrontParser.objs);
            
            // create all portals and cells
            for (Obj obj : objs) {
                if (obj.name.startsWith("portal")) {
                    Portal portal = new Portal(obj.name, obj.faces);
                    portals.add(portal);
                }
                else {
                    Cell cell = new Cell(obj.name, obj.faces);
                    cells.add(cell);
                    
                    if (obj.name.contains("_start")) {
                        currentCell = cell;
                    }
                }
            }
            
            List<Cell> linkableCells = new ArrayList<>();
            // link cells and portals
            for (Portal portal : portals) {
                linkableCells.clear();
                for (Cell cell : cells) {
                    if (cell.containsPortal(portal)) {
                        linkableCells.add(cell);
                    }
                }
                
                if (linkableCells.size() != 2) {
                    throw new Exception("Portal " + portal.name + " linking wrong number of cells !");
                }
                
                portal.linkCells(linkableCells.get(0), linkableCells.get(1));
            }
        } catch (Exception ex) {
            Logger.getLogger(Cell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    
    
}
