package edu.berkeley.wtchoi.cc.util.gui;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScaledFileImage extends FileImage{

    protected double scale;


    public ScaledFileImage(String path, double scale){
        super(path);
        this.scale = scale;
    }

    @Override
    protected BufferedImage filtering(){
        int iw = (int)(super.image.getWidth() * scale);
        int ih = (int)(super.image.getHeight() * scale);
        return ImageUtil.getScaleImage(super.image, iw, ih, false);
    }
}
