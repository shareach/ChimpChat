package edu.berkeley.wtchoi.cc.util.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ImageUtil {
    public static BufferedImage copyImage(BufferedImage target){
        ColorModel cm = target.getColorModel();
        boolean alpha = cm.isAlphaPremultiplied();
        WritableRaster raster = target.copyData(null);
        return new BufferedImage(cm, raster, alpha, null);
    }

    public static BufferedImage getScaleImage(BufferedImage image, int x, int y, boolean preserveRatio){
        if(image.getWidth() == x && image.getHeight() == y)
            return ImageUtil.copyImage(image) ;

        BufferedImage scaledImage = new BufferedImage(x,y, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        if(!preserveRatio){
            graphics2D.drawImage(image,0,0,x,y,null);
            return scaledImage;
        }

        int iw = image.getWidth();
        int ih = image.getHeight();
        double scaleX = (double) x / (double)iw;
        double scaleY = (double) y / (double)ih;
        double scale = Math.min(scaleX,scaleY);
        iw = (int)(scale * (double)iw);
        ih = (int)(scale * (double)ih);
        graphics2D.drawImage(image,0,0,iw,ih,null);
        return scaledImage;
    }
}
