package renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * BspCBufferScan class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class BspCBufferScan extends Scan {

    /**
     * Bsp front to back occlusion culling/clipping span (C-Buffer) node.
     */
    public static class CBufferNode {

        static final int INITIAL_NODES_CACHE_SIZE = 50;

        final CBufferNodesCache nodesCache;
        int start; 
        int end; 
        Integer partitionPoint; 
        boolean occluded;
        boolean partitioned;
        CBufferNode left;
        CBufferNode right;

        public CBufferNode(int start, int end) {
            this.start = start;
            this.end = end;
            this.nodesCache = new CBufferNodesCache(INITIAL_NODES_CACHE_SIZE);
        }

        private CBufferNode(CBufferNodesCache nodesCache) {
            this.nodesCache = nodesCache;
        }

        public void reset() {
            nodesCache.reset();
            reset(start, end);
        }

        private void reset(int start, int end) {
            this.start = start;
            this.end = end;
            partitionPoint = null; 
            occluded = false;
            partitioned = false;
            left = null;
            right = null;
        }

        public boolean isOccluded() {
            return occluded;
        }

        // note: result list is empty if the span is completely occluded.
        //       otherwise it returns a pair of numbers that 
        //       indicate the start and end point of entire or fragmented spans.
        public void addSpan(int start, int end, List<Integer> result) {
            if (occluded && start >= this.start && end <= this.end) {
                return;
            }
            if (start > this.end || end < this.start) {
                return;
            }
            if (start < this.start) {
                start = this.start;
            }
            if (end > this.end) {
                end = this.end;
            }

            // if the size of span is equal than the size 
            // of this partition and this is still not occluded,
            // then this can be marked as occluded and just return, 
            // BUT ONLY if this hasn't been partitioned yet.
            // if this is already partitioned, then it's necessary 
            // to keep the partition checks to find only parts of the 
            // span that are still visible.
            if (!occluded && !partitioned 
                    && start <= this.start && end >= this.end) {

                if (!result.isEmpty() 
                        && result.get(result.size() - 1) == start - 1) {

                    // merge continuos spans
                    result.remove(result.size() - 1);
                    result.add(end);
                }
                else {
                    result.add(start);
                    result.add(end);
                }
                occluded = true;
                return;
            }

            if (!partitioned) {
                if (partitionPoint == null) {
                    if (start == this.start) {
                        partitionPoint = end;
                    }
                    else {
                        partitionPoint = start - 1;
                    }
                }

                left = nodesCache.get(this.start, partitionPoint);
                right = nodesCache.get(partitionPoint + 1, this.end);
                partitioned = true;
            }

            if (start <= partitionPoint && end <= partitionPoint) {
                left.addSpan(start, end, result);
            }
            else if (start <= partitionPoint && end > partitionPoint) {
                left.addSpan(start, partitionPoint, result);
                right.addSpan(partitionPoint + 1, end, result);
            }
            else if (start > partitionPoint && end > partitionPoint) {
                right.addSpan(start, end, result);
            }

            if (left.occluded && right.occluded) {
                occluded = true;
            }
        }
        
    }
    
    /**
     * CBufferNodeCache class.
     */
    private static class CBufferNodesCache {

        final List<CBufferNode> cache = new ArrayList<>();
        int index = 0;

        CBufferNodesCache(int initialSize) {
            for (int i = 0; i < initialSize; i++) {
                cache.add(new CBufferNode(this));
            }
        }

        CBufferNode get(int start, int end) {
            if (index > cache.size() - 1) {
                cache.add(new CBufferNode(this));
            }
            CBufferNode node = cache.get(index++);
            node.reset(start, end);
            return node;
        }

        void reset() {
            index = 0;
        }

    }
        
    // ---
    
    public final CBufferNode[] cbufferNodes;
    private final int width;
    private final int height;
    public int fullScansCount;

    public BspCBufferScan(Renderer renderer) {
        super(renderer);
        this.width = renderer.getWidth();
        this.height = renderer.getHeight();
        cbufferNodes = new CBufferNode[height];
        for (int y = 0; y < height; y++) {
            cbufferNodes[y] = new CBufferNode(0, width - 1);
        }
    }

    @Override
    public void clear() {
        fullScansCount = cbufferNodes.length;
        for (int y = 0; y < height; y++) {
            cbufferNodes[y].reset();
        }
    }

    @Override
    public boolean isFinished() {
        return fullScansCount == 0;
    }
    
    private final List<Integer> resultTmp = new ArrayList<>();
    
    @Override
    protected void drawScanlines(int y) {
        if (y < 0) {
            return;
        }
        if (!cbufferNodes[y].isOccluded()) {
            initX();
            resultTmp.clear();
            cbufferNodes[y].addSpan(x1, x2, resultTmp);
            for (int i = 0; i < resultTmp.size(); i += 2) {
                int xa = resultTmp.get(i);
                int xb = resultTmp.get(i + 1);
                nextX(xa - x1);
                for (int x = xa; x <= xb; x++) {
                    //if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                        shader.processPixel(x, y, renderer, shader.curValuesX);
                        depthBuffer.set(x, y, shader.curValuesX[2]);
                    //}
                    nextX();
                }
            }
            if (cbufferNodes[y].isOccluded()) {
                fullScansCount--;
            }
        }
    }    

    @Override
    protected void initX() {
        x1 = (int) e1.curValuesY[0];
        x2 = (int) e2.curValuesY[0];
        double nx = 1.0 / (x2 - x1 + 1e-200);
        shader.deltaPerX[2] = (e2.curValuesY[2] - e1.curValuesY[2]) * nx; //=1/z
        for (int i = shader.variableStartIndex; i < shader.dataSize; i++) {
            shader.deltaPerX[i] = (e2.curValuesY[i] - e1.curValuesY[i]) * nx;
        }
    }
    
}
