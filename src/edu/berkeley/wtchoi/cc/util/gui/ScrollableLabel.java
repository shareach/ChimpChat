package edu.berkeley.wtchoi.cc.util.gui;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */

//Combination of source from
// 1. http://www.java2s.com/Code/Java/Swing-JFC/GrabandDragimagescrolllabel.htm
// 2. http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/ScrollDemoProject/src/components/ScrollablePicture.java
public class ScrollableLabel extends JLabel
        implements Scrollable {

    private int maxUnitIncrement = 1;
    private boolean missingPicture = false;

    public ScrollableLabel(ImageIcon i, int m) {
        super(i);
        if (i == null) {
            missingPicture = true;
            setText("No picture found.");
            setHorizontalAlignment(CENTER);
            setOpaque(true);
            setBackground(Color.white);
        }
        maxUnitIncrement = m;

        MouseInputAdapter mia = new MouseInputAdapter() {
            int xDiff, yDiff;

            boolean isDragging;

            Container c;

            public void mouseDragged(MouseEvent e) {
                c = ScrollableLabel.this.getParent();
                if (c instanceof JViewport) {
                    JViewport jv = (JViewport) c;
                    Point p = jv.getViewPosition();
                    int newX = p.x - (e.getX() - xDiff);
                    int newY = p.y - (e.getY() - yDiff);

                    int maxX = ScrollableLabel.this.getWidth()
                            - jv.getWidth();
                    int maxY = ScrollableLabel.this.getHeight()
                            - jv.getHeight();
                    if (newX < 0)
                        newX = 0;
                    if (newX > maxX)
                        newX = maxX;
                    if (newY < 0)
                        newY = 0;
                    if (newY > maxY)
                        newY = maxY;

                    jv.setViewPosition(new Point(newX, newY));
                }
            }

            public void mousePressed(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                xDiff = e.getX();
                yDiff = e.getY();
            }

            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseMotionListener(mia);
        addMouseListener(mia);


        //Let the user scroll by dragging to outside the window.

        setAutoscrolls(true);
    }

    public Dimension getPreferredSize() {
        if (missingPicture) {
            return new Dimension(320, 480);
        } else {
            return super.getPreferredSize();
        }
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition -
                    (currentPosition / maxUnitIncrement)
                            * maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1)
                    * maxUnitIncrement
                    - currentPosition;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxUnitIncrement;
        } else {
            return visibleRect.height - maxUnitIncrement;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }

    public void setIcon(ImageIcon icon){
        super.setIcon(icon);
        missingPicture = false;
        setText("");
        setOpaque(false);
    }

}