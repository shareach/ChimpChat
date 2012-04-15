package edu.berkeley.wtchoi.cc.learnerImp;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/14/12
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CTreeViewer<I,Observation> implements Runnable{
    CTree tree;
    ImagePanel panel;
    JFrame frame;

    public CTreeViewer(CTree ctree){
        tree = ctree;
        frame = new JFrame("TreeView");
        panel = new ImagePanel("/tmp/out.gif");
        frame.add(panel);
    }

    public void run(){

        frame.setVisible(true);
    }

    public void reload(){
        tree.drawTree("/tmp/out.gif");
        panel.reload();
        frame.repaint();
    }

    static class ImagePanel extends JPanel{

        private BufferedImage image;
        private String path;

        public ImagePanel(String path) {
            this.path = path;
            reload();
        }

        public void reload(){
            try {
                image = ImageIO.read(new java.io.File(path));
            } catch (IOException ex) {
                // handle exception...
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            int iw = image.getWidth();
            int ih = image.getHeight();
            int maxw = 1000;
            int maxh = 1000;
            if(iw>maxw){
                ih = (int)(((double)maxw / (double)iw) * (double)ih);
                iw = maxw;
            }
            if(ih > maxh){
                iw = (int)(((double)maxh / (double)ih) * (double)iw);
                ih = maxh;
            }
            g.drawImage(image, 0, 0, iw, ih, null); // see javadoc for more info on the parameters
        }

    }
}
