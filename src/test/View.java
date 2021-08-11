package test;

import bsp3d.Node;
import bsp3d.Player;
import bsp3d.Triangle;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import math.Vec4;
import renderer.Renderer.ScanType;
import renderer.parser.wavefront.Obj;
import renderer.parser.wavefront.WavefrontParser;

/**
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class View extends Canvas {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Font font = new Font("Arial", Font.PLAIN, 20);
    
    private renderer.Renderer renderer;
    private boolean running;
    private BufferStrategy bs;
    private Graphics2D g2d;
    private int fps = 0;

    private Player playerPreviousPosition = new Player();
    private Player player = new Player();
    private Node bspNode;
    
    private double cameraMousePressedAngleX = 0;
    private double cameraMousePressedAngleY = 0;
    private double cameraTargetAngleX = 0;
    private double cameraTargetAngleY = 0;
    private double cameraAngleX = -0.199999999999933;
    private double cameraAngleY = 3.30000000000001;
    private final Vec4 cameraPosition = new Vec4(0.0, 0.0, 0.0, 1.0);
    
    private portal.Level level;
    
    private boolean usePortal = true;
    
    public void start() {
        renderer = new renderer.Renderer(WIDTH, HEIGHT);
        
        //renderer.getProjectionTransform().setPerspectiveProjection(Math.toRadians(60), 800);
        renderer.getProjectionTransform().setPerspectiveProjection(Math.toRadians(70), 4.0/3.0, 0.01, 10000.0);

        try {
            if (usePortal) {
                level = new portal.Level("/res/house2.obj", 20.0);
            }
            //level = new portal.Level("/res/test_portal_clip.obj", 20.0);
            
            //WavefrontParser.load(renderer.getMaterial().getShader(), "/res/Power Pro Stadium.obj", 100.0);
            //WavefrontParser.load("/res/quake2.obj", 100.0);
            //WavefrontParser.load("/res/house.obj", 20.0);
            //WavefrontParser.load(renderer.getMaterial().getShader(), "/res/test4.obj", 100.0);
            //WavefrontParser.load("/res/plane.obj", 100.0);
            if (!usePortal) {
                //WavefrontParser.load("/res/Doom_E1M1.obj", 20.0);
                //WavefrontParser.load("/res/house.obj", 20.0);
                WavefrontParser.load("/res/xxx.obj", 20.0);
                List<Triangle> allFaces = new ArrayList<>();
                for (Obj obj : WavefrontParser.objs) {
                    allFaces.addAll(obj.faces);
                }
            }
//
//            bspNode = new Node();
//            bspNode.preProcess(0, allFaces);
        } catch (Exception ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        createBufferStrategy(2);
        bs = getBufferStrategy();
        g2d = (Graphics2D) bs.getDrawGraphics();
        Thread mainLoopThread = new Thread(new MainLoop());
        //mainLoopThread.setDaemon(true);
        //mainLoopThread.setPriority(Thread.MIN_PRIORITY);
        mainLoopThread.start();
        
        addKeyListener(new KeyHandler());
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        
        System.out.println("isAccelerated=" + getGraphicsConfiguration().getImageCapabilities().isAccelerated());
        System.out.println("isTrueVolatile=" + getGraphicsConfiguration().getImageCapabilities().isTrueVolatile());
    }
    
    long deltaTime = 0;
    
    private class MainLoop implements Runnable {

        @Override
        public void run() {
            long previousTime2 = System.nanoTime(); 
            
            long previousTime = System.nanoTime();
            long unprocessedTime = 0;
            long timePerFrame = 1000000000 / 60;
            running = true;
            long startTime = 0;
            int fpsCount = 0;
            while (running) {
                //Time.update();
                
                long currentTime = System.nanoTime();
                double delta = (currentTime - previousTime) / 1000000000.0;
                unprocessedTime += (currentTime - previousTime);
                previousTime = currentTime;

                
                updateCamera(delta);
                
                while (unprocessedTime > timePerFrame) {
                    unprocessedTime -= timePerFrame;
                    
                    // fixed time step
                    delta = 1.0 / 60.0;
                    update(delta);
                    //long currentTime2 = System.nanoTime();
                    //delta = (currentTime2 - previousTime2) / 1000000000.0;
                    //previousTime2 = currentTime2;
                }
                
                // render to frame buffer
                renderer.getViewTransform().setIdentity();
                renderer.getViewTransform().rotateX(cameraAngleX + Math.PI * 2);
                renderer.getViewTransform().rotateY(-cameraAngleY + Math.PI * 0.5);
                renderer.getViewTransform().translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
                
                renderer.updateMvpMatrix();
                renderer.setScanType(ScanType.DEFAULT);
                renderer.clearBuffers();

                playerPreviousPosition.position.set(player.position);
                
                player.position.x = cameraPosition.x;
                player.position.y = cameraPosition.y;
                player.position.z = cameraPosition.z;
                
                if (usePortal) {
                    level.currentCell = level.currentCell.moveCamera(
                              playerPreviousPosition.position.x
                            , playerPreviousPosition.position.y
                            , playerPreviousPosition.position.z
                            , player.position.x
                            , player.position.y
                            , player.position.z);

                    System.out.println("camera position: " + cameraPosition + "");
                }
                
//                // draw rooms
//                bspNode.transverse(player, renderer);
//
//                // draw portal
//                for (Triangle triangle : portal) {
//                    if (renderer.isPortalVisible(triangle.getVa(), triangle.getVb(), triangle.getVc())) {
//                        System.out.println("portal is visible");
//                        break;
//                    }
//                    else {
//                        System.out.println("portal is not visible");
//                    }
//                }
                    
                if (usePortal) {
                    //System.out.println("current cell: " + currentCell.name);
                    level.currentCell.draw(renderer);
                }
                else {
                    for (int i = 0; i < WavefrontParser.objs.size(); i++) {
                        Obj obj = WavefrontParser.objs.get(i);
                        for (Triangle triangle : obj.faces) {
                            triangle.draw3D(renderer, null, null);
                        }
                    }
                }
                    

                //g2d = (Graphics2D) bs.getDrawGraphics();
                draw(g2d);
                bs.show();
                //g2d.dispose();
                
//                try {
//                    Thread.sleep(0);
//                } catch (InterruptedException ex) {
//                }
                
                fpsCount++;
                deltaTime = System.nanoTime() - startTime;
                if (deltaTime > 1000000000) {
                    startTime = System.nanoTime();
                    fps = fpsCount;
                    fpsCount = 0;
                }
            }
        }
        
    }
    
    //double angle = 3.73; // fica um vao entre 
    //double angle = 4.5990000000001565;
    //double angle = 0.17450000000003374;
    double angle = 0;
    
//        Vec4 a = new Vec4(200.0, 0.0, 0.0, 1.0);
//        Vec4 b = new Vec4(0.0, 0.0, 0.0, 1.0);
//        Vec4 c= new Vec4(0.0, -900.0, 0.0, 1.0);
    
    double[][] polygon = {
        { -400, -300.0, 0.0, 1.0, 0.0, 0.0 },
        { -400,  300.0, 0.0, 1.0, 0.0, 0.0 },
        {  400,  300.0, 0.0, 1.0, 0.0, 0.0 },
        {  400, -300.0, 0.0, 1.0, 0.0, 0.0 },
    };

    double z = 0.0;
    
    private void update(double delta) {
        
        renderer.getModelTransform().setIdentity();
        //renderer.getModelTransform().translate(0, 10, 0.61);
        //renderer.getModelTransform().rotateZ(angle * 0.9465);
        //renderer.getModelTransform().rotateX(angle * 1.1545);
        //renderer.getModelTransform().rotateY(angle * 1.7546);
        
//        List<double[]> quad = new ArrayList<>();
//        quad.add(polygon[0]);
//        quad.add(polygon[1]);
//        quad.add(polygon[2]);
//        quad.add(polygon[3]);
//        renderer.updateMvpMatrix();

        
//renderer.drawTriangle(polygon[0], polygon[1], polygon[2]);
//renderer.drawPolygon(quad);
        
        //angle = Math.toRadians(308);
        //angle += 0.001;
        //System.out.println("angle = " + angle);
    }
    
    private void updateCamera(double delta) {
        if (Mouse.pressed) {
            cameraTargetAngleY = cameraMousePressedAngleY + 0.025 * (Mouse.x - Mouse.pressedX);
            cameraTargetAngleX = cameraMousePressedAngleX - 0.025 * (Mouse.y - Mouse.pressedY);

            cameraAngleX += ((cameraTargetAngleX) - cameraAngleX) * 20.0 * delta;
            cameraAngleY += ((cameraTargetAngleY) - cameraAngleY) * 20.0 * delta;
        }

                
        double speed = 40.0 * delta;
        
        if (Keyboard.keyDown[37]) {
            cameraMove(speed, Math.toRadians(90));
        }
        else if (Keyboard.keyDown[39]) {
            cameraMove(-speed, Math.toRadians(90));
        }
        
        if (Keyboard.keyDown[38]) {
            cameraMove(-speed, 0);
        }
        else if (Keyboard.keyDown[40]) {
            cameraMove(speed, 0);
        }

        if (Keyboard.keyDown[KeyEvent.VK_Q]) {
            cameraPosition.y += speed;
        }
        else if (Keyboard.keyDown[KeyEvent.VK_A]) {
            cameraPosition.y -= speed;
        }

        if (Keyboard.keyDown[KeyEvent.VK_W]) {
            renderer.setWireframeEnabled(!renderer.isWireframeEnabled());
        }
    }
    
    private void cameraMove(double d, double dAngle) {
        double s = Math.sin(cameraAngleY + dAngle);
        double c = Math.cos(cameraAngleY + dAngle);
        cameraPosition.x += (c * d);
        cameraPosition.z += (s * d);
        cameraPosition.y -= d * Math.sin(cameraAngleX);
    }
    
    private void draw(Graphics2D g) {
        //Graphics2D rg2d = (Graphics2D) renderer.getColorBuffer().getG2D();
        //rg2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);        
        //rg2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 800, 600, 0, 0, 960, 720, null);
        
        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);        
        //g.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, null); 
        g.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, null);
        //g.drawImage(renderer.getLightBuffer().getColorBuffer(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, null);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("FPS: " + fps, 50, 50);
        g.setColor(Color.RED);
        g.drawLine(0, 599, 10, 599);
        g.drawLine(0, 0, 10, 0);
        //renderer.getColorBuffer().getG2D().drawString("FPS2: " + Time.fps, 50, 70);
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Mouse.pressed = true;
//            Mouse.pressedX = e.getX() - getWidth() * 0.5;
//            Mouse.pressedY = getHeight() * 0.5 - e.getY();
            Mouse.x = e.getX();
            Mouse.y = e.getY();
            Mouse.pressedX = e.getX();
            Mouse.pressedY = e.getY();
            cameraMousePressedAngleX = cameraAngleX;
            cameraMousePressedAngleY = cameraAngleY;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Mouse.pressed = false;
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            //Mouse.x = e.getX() - getWidth() * 0.5;
            //Mouse.y = getHeight() * 0.5 - e.getY();
            //Mouse.x = e.getX();
            //Mouse.y = e.getY();
            //Mouse.pressed = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            //Mouse.x = e.getX() - getWidth() * 0.5;
            //Mouse.y = getHeight() * 0.5 - e.getY();
            Mouse.x = e.getX();
            Mouse.y = e.getY();
        }
        
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            Keyboard.keyDown[e.getKeyCode()] = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Keyboard.keyDown[e.getKeyCode()] = false;
        }
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // activate opengl
                System.setProperty("sun.java2d.opengl", "False");
                
                View view = new View();
                view.setPreferredSize(new Dimension(WIDTH, HEIGHT));
                JFrame frame = new JFrame();
                frame.setTitle("");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(view);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                frame.setVisible(true);
                view.requestFocus();
                view.start();
            }
        });        
    }
    
}
