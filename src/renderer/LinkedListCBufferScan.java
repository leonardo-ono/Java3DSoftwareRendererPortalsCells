package renderer;

import renderer.LinkedListCBufferScan.Span.Flag;

/**
 * LinkedListCBufferScan class.
 * 
 * Note: apparently faster than bsp when there are a lot of polygon
 *       but still very buggy ... needs to fix it later.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LinkedListCBufferScan extends Scan {

    /**
     * Span class.
     */
    static class Span {

        static int currentId = 0;
        final int id = ++currentId;

        public static enum Flag { START, END }
        Flag flag;

        int x1;
        int x2;

        Span left;
        Span right;

        Span(Flag flag) {
            this.flag = flag;
            if (flag == Flag.START) {
                set(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
            }
            else if (flag == Flag.END) {
                set(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }

        Span(int x1, int x2) {
            set(x1, x2);
        }

        private void set(int x1, int x2) {
            this.x1 = x1;
            this.x2 = x2;
        }

        boolean isInside(int x) {
            return x >= x1 && x <= x2;
        }

        void insertLeft(int x1, int x2) {
            Span span = getFromCache(x1, x2);
            if (left == null) {
                left = span;
                span.right = this;
            }
            else {
                Span previousLeft = left;
                left = span;
                span.right = this;
                span.left = previousLeft;
                previousLeft.right = span;
            }
        }

        void insertCircular(Span span) {
            if (left == null) {
                left = this;
            }
            if (right == null) {
                right = this;
            }
            Span lastSpan = left;
            lastSpan.right = span;
            span.left = lastSpan;
            span.right = this;
            left = span;
        }

        @Override
        public String toString() {
            return "Span{" + "id=" + id + '}';
        }

        // --- spans cache ---

        static Span cachedSpans = new Span(Flag.START);

        public static Span getFromCache(int x1, int x2) {
            if (cachedSpans.right == null) {
                return new Span(x1, x2);
            }
            else {
                Span cachedSpan = cachedSpans.right;
                cachedSpans.right = cachedSpan.right;
                cachedSpan.left = null;
                cachedSpan.right = null;
                cachedSpan.x1 = x1;
                cachedSpan.x2 = x2;
                return cachedSpan;
            }
        }

        public static void saveToCache(Span spans) {
            Span lastSpan = spans.left;
            if (lastSpan == null) {
                lastSpan = spans;
            }
            Span rightSpans = cachedSpans.right;
            cachedSpans.right = spans;
            spans.left = cachedSpans;
            lastSpan.right = rightSpans;
            if (rightSpans != null) {
                rightSpans.left = lastSpan;
            }
        }

    }

    /**
     * Scanline class.
     */
    public class Scanline {

        boolean full = false;

        Span rootSpan = new Span(Flag.START);
        Span endSpan = new Span(Flag.END);

        Span cbuffer = rootSpan;
        Span visibleSpans = null;

        int xMin;
        int xMax;

        Scanline(int xMin, int xMax) {
            rootSpan.right = endSpan;
            endSpan.left = rootSpan;
            this.xMin = xMin;
            this.xMax = xMax;
        }

        boolean isFull() {
            return full;
        }

        Span getCbuffer() {
            return cbuffer;
        }

        // --- visible spans ---

        void clearVisibleSpans() {
            if (visibleSpans != null) {
                Span.saveToCache(visibleSpans);
            }
            visibleSpans = null;
        }

        private void addVisibleSpan(int x1, int x2) {
            if (x1 > x2) {
                // ignore
            }
            else if (visibleSpans == null) {
                visibleSpans = Span.getFromCache(x1, x2);
            }
            else {
                Span visibleSpan = Span.getFromCache(x1, x2);
                visibleSpans.insertCircular(visibleSpan);
            }
        }

        // --- c-buffer ---

        void clear() {
            if (rootSpan.right != endSpan) {
                Span toCache1 = rootSpan.right;
                Span toCache2 = endSpan.left;
                toCache1.left = toCache2;
                toCache2.right = toCache1;
                Span.saveToCache(toCache1);
            }
            rootSpan.right = endSpan;
            endSpan.left = rootSpan;
            full = false;
        }

        Span insert(int x1, int x2) {
            clearVisibleSpans();
            Span span1 = null;
            Span currentSpan = rootSpan.right;
            while (currentSpan != null) {
                if (x2 < currentSpan.x1){
                    addVisibleSpan(x1, x2);
                    currentSpan.insertLeft(x1, x2);
                    return visibleSpans;
                }
                else if (currentSpan.isInside(x1) && currentSpan.isInside(x2)){
                    return null; // total occlusion
                }
                else if (x1 <= (currentSpan.x2 + 1)){
                    span1 = currentSpan;
                    if (x1 < currentSpan.x1) {
                        addVisibleSpan(x1, currentSpan.x1 - 1);
                        currentSpan.x1 = x1;
                    }
                    break;
                }
                currentSpan = currentSpan.right;
            }

            Span span2 = currentSpan;
            currentSpan = currentSpan.right;
            while (x2 >= (currentSpan.x1 - 1)) {
                addVisibleSpan(span2.x2 + 1, currentSpan.x1 - 1);
                span2 = currentSpan;
                currentSpan = currentSpan.right;
            }

            if (x2 > span2.x2) {
                addVisibleSpan(span2.x2 + 1, x2);
                span2.x2 = x2;
            }

            if (span1 != span2) {
                Span toCache = span1.right;
                span1.right = span2.right;
                span2.right.left = span1;
                span1.x2 = span2.x2;
                toCache.left = span2;
                span2.right = toCache;
                Span.saveToCache(toCache);
            }

            return visibleSpans;
        }

        void checkFull() {
            if (rootSpan.right != endSpan) {
                full = (rootSpan.right.x1 <= xMin + 1) 
                            && (rootSpan.right.x2 >= xMax - 1);
            }
        }

    }

    public int fullScansCount;
    public Scanline[] scans;
    
    public LinkedListCBufferScan(Renderer renderer) {
        super(renderer);
        scans = new Scanline[renderer.getHeight()];
        for (int i=0; i<scans.length; i++) {
            scans[i] = new Scanline(0, renderer.getWidth() - 2);
        }
    }
    
    @Override
    public void clear() {
        fullScansCount = scans.length;
        for (Scanline scan : scans) {
            scan.clear();
        }
    }

    @Override
    public boolean isFinished() {
        return fullScansCount < 2;
    }
    
    @Override
    protected void drawScanlines(int y) {
        if (y < 0) {
            return;
        }
        Scanline cbuffer = scans[y];
        if (!cbuffer.isFull()) {
            initX();
            Span visibleSpan = cbuffer.insert(x1, x2);
            if (visibleSpan != null) {
                nextX(visibleSpan.x1 - x1);
                Span firstVisibleSpan = visibleSpan;
                do {
                    for (int x = visibleSpan.x1; x <= visibleSpan.x2; x++) {
                        if (shader.curValuesX[2] > depthBuffer.get(x, y)) {
                            shader.processPixel(
                                    x, y, renderer, shader.curValuesX);
                            
                            depthBuffer.set(x, y, shader.curValuesX[2]);
                        }
                        nextX();
                    }
                    if (visibleSpan.right != null) {
                        nextX(visibleSpan.right.x1 - visibleSpan.x2);
                    }
                    visibleSpan = visibleSpan.right;
                }
                while (visibleSpan != null && visibleSpan != firstVisibleSpan);
            }
            cbuffer.checkFull();
            if (cbuffer.isFull()) {
                fullScansCount--;
            }
        }
    }    
    
}
