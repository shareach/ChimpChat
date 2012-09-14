package edu.berkeley.wtchoi.cc.util.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileImage{
    protected String path;
    protected BufferedImage image;
    protected BufferedImage imageF;
    protected BufferedImage imageS;
    protected ImageIcon icon;

    protected int scaleX;
    protected int scaleY;
    protected boolean preserveRatio = true;
    protected boolean untouched = true;
    protected boolean scaleRequired = false;


    public FileImage(String path){
        this.path = path;
    }

    final public void reload(){
        reloadImage();
        icon = null;
    }

    private void reloadImage(){
        try{
            java.io.File file = new java.io.File(path);
            image = ImageIO.read(file);
            imageF = filtering();
            delayedScale();
        }
        catch(Exception e){}
    }

    private void delayedScale(){
        scaleRequired = true;
    }

    final public BufferedImage getImage(){
        if(scaleRequired){
            if(!untouched){
                imageS = ImageUtil.getScaleImage(imageF,scaleX,scaleY,preserveRatio);
            }else{
                imageS = imageF;
            }
            scaleRequired = false;
        }
        return imageS;
    }

    final public ImageIcon getIcon(){
        if(icon == null) icon = new ImageIcon(getImage());
        return icon;
    }

    final public void scale(int tx, int ty, boolean preserveRatio){
        scaleImage(tx,ty,preserveRatio);
        icon = null;
    }

    private void scaleImage(int tx, int ty, boolean  preserveRatio){
        scaleX = tx;
        scaleY = ty;
        this.preserveRatio = preserveRatio;
        untouched = false;

        delayedScale();
    }

    public void undoScale(){
        untouched = true;
        imageS = imageF;
        icon = null;
        scaleRequired = false;
    }

    final public int getWidth(){ return getImage().getWidth();}
    final public int getHeight(){ return getImage().getHeight();}

    protected BufferedImage filtering(){
        return image;
    }
}
