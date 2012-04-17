package edu.berkeley.wtchoi.cc.util.gui;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:52 PM
 * To change this template use File | Settings | File Templates.
 */
import javax.swing.JPanel;
import java.awt.*;

public class ScalingFilePanel extends JPanel{
    protected FileImage fimage;

    public ScalingFilePanel(FileImage fi){
        super();
        fimage = fi;
        this.setBackground(Color.WHITE);
    }

    public void reload(){
        fimage.reload();
    }

    @Override
    public void paintComponent(Graphics g){
        int tw = getWidth();
        int th = getHeight();
        fimage.undoScale();
        if(fimage.getWidth() >= tw || fimage.getHeight() >= th){
            fimage.scale(tw,th,true);
        }
        this.setBackground(Color.WHITE);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int x = (tw - fimage.getWidth())/2;
        int y = (th - fimage.getHeight())/2;
        g2.drawImage(fimage.getImage(), x,y, fimage.getWidth(), fimage.getHeight(), null);
        //TODO: Proper Centering
        //TODO: How to color backgroun properly?
    }
}
