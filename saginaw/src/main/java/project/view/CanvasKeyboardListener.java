package project.view;

import project.controller.Controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CanvasKeyboardListener implements KeyListener {

    Controller controller;
    View view;

    public CanvasKeyboardListener(View view, Controller controller){
        this.view = view;
        this.controller = controller;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == (KeyEvent.VK_W)){
            controller.moveMap(0, 100);
        } else if(e.getKeyCode() == (KeyEvent.VK_S)){
            controller.moveMap(0, -100);
        } else if(e.getKeyCode() == (KeyEvent.VK_A)){
            controller.moveMap(-100, 0);
        } else if(e.getKeyCode() == (KeyEvent.VK_D)){
            controller.moveMap(100, 0);
        } else if(e.getKeyCode() == (KeyEvent.VK_Z)){
            controller.zoomOut();
        } else if(e.getKeyCode() == (KeyEvent.VK_C)){
            controller.zoomIn();
        } else if(e.getKeyCode() == (KeyEvent.VK_R)){
            controller.upDoug();
        } else if(e.getKeyCode() == (KeyEvent.VK_F)){
            controller.downDoug();
        } else if(e.getKeyCode() == (KeyEvent.VK_K)){
            controller.randomRoute();
        }
        this.view.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}