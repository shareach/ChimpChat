package edu.berkeley.wtchoi.cc.learnerImp;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/14/12
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */

import edu.berkeley.wtchoi.cc.util.gui.FileImage;
import edu.berkeley.wtchoi.cc.util.gui.ScaledFileImage;
import edu.berkeley.wtchoi.cc.util.gui.ScrollableFileLabel;

import java.awt.Dimension;

public class CTreeViewer implements Runnable{
    CTree tree;
    ScrollableFileLabel picture;
    JFrame frame;

    public CTreeViewer(CTree ctree){
        tree = ctree;

        FileImage fimage = new ScaledFileImage("/tmp/out.gif",0.8);
        picture = new ScrollableFileLabel(fimage);

        JScrollPane scroller = new JScrollPane(picture);
        scroller.setAutoscrolls(true);
        scroller.setPreferredSize(new Dimension(800,550));

        frame = new JFrame("TreeView");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane();
        frame.getContentPane().add(scroller);
        frame.setSize(800,600);

        //frame = new JFrame("TreeView");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.add(picture);
    }

    public void run(){
        frame.setVisible(true);
    }

    public void reload(){
        tree.drawTree("/tmp/out.gif");
        picture.reload();
        frame.repaint();
    }
}

