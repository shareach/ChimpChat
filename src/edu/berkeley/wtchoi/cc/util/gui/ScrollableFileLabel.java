package edu.berkeley.wtchoi.cc.util.gui;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScrollableFileLabel extends ScrollableLabel{
    protected FileImage fimage;


    public ScrollableFileLabel(FileImage fi){
        super(null, 10);
        fimage = fi;
    }

    public void reload(){
        fimage.reload();
        super.setIcon(fimage.getIcon());
    }
}
