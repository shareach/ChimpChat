package edu.berkeley.wtchoi.cc.driver;

import edu.berkeley.wtchoi.cc.util.datatype.CSet;

import java.lang.Comparable;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Collection;
import java.io.BufferedWriter;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;


public class ViewInfo implements Serializable, Comparable {
    /**
     *
     */
    private static final long serialVersionUID = -5186309675577891457L;

    //basic properties
    private int x; //left
    private int y; //top
    private int width;
    private int height;
    private int scrollX = 0; //View.mScrollX;
    private int scrollY = 0; //View.mScrollY;
    private int absoluteX = 0; //Absolute position on Screen;
    private int absoluteY = 0; //Absolute position on Screen;
    private LinkedList<ViewInfo> children;

    //additional properties
    private boolean visible = false;
    private boolean isEditText = false;
    private boolean hasFocus = false;
    private String textContent;
    private int id;

    @SuppressWarnings("unchecked")
    public ViewInfo(int ix, int iy, int iw, int ih, LinkedList<ViewInfo> ic) {
        x = ix;
        y = iy;
        width = iw;
        height = ih;


        if (ic != null) children = (LinkedList<ViewInfo>) ic.clone();
        else children = null;
    }

    public void setVisible(boolean flag){
        visible = flag;
    }

    public void setScroll(int x, int y){
        scrollX = x;
        scrollY = y;
    }

    public void setAbsolute(int x, int y){
        absoluteX = x;
        absoluteY = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return width;
    }

    public int getH() {
        return height;
    }

    public int getSX(){
        return scrollX;
    }

    public int getSY(){
        return scrollY;
    }

    public int getAX(){
        return absoluteX;
    }

    public int getAY(){
        return absoluteY;
    }


    public String toString() {
        java.io.StringWriter sw = new java.io.StringWriter();
        BufferedWriter buffer = new BufferedWriter(sw);
        try {
            ViewInfo.toString(buffer, this, 0);
            buffer.flush();
        } catch (Exception e) {
            System.out.println("Error occur!");
        }

        return sw.toString();
    }

    private static void toString(BufferedWriter buffer, ViewInfo mv, int depth) throws java.io.IOException {
        for (int i = 0; i < depth; i++) {
            buffer.write("  ");
        }

        buffer.write("<");
        buffer.write(Integer.toString(mv.x));
        buffer.write(",");
        buffer.write(Integer.toString(mv.y));
        buffer.write(",");
        buffer.write(Integer.toString(mv.width));
        buffer.write(",");
        buffer.write(Integer.toString(mv.height));
        buffer.write(",");
        buffer.write(Integer.toString(mv.scrollX));
        buffer.write(",");
        buffer.write(Integer.toString(mv.scrollY));
        buffer.write(",");
        buffer.write(Integer.toString(mv.absoluteX));
        buffer.write(",");
        buffer.write(Integer.toString(mv.absoluteY));
        buffer.write(">");
        buffer.newLine();

        if (mv.children == null) return;

        for (ViewInfo child : mv.children) {
            toString(buffer, child, depth + 1);
        }
    }


    //Function to collect representative click positions for each equivalence class
    public <T extends Comparable<T>> CSet<T> getRepresentativePoints(PointFactory<T> factory) {
        //Infer click points from view hierarchy
        TreeSet<Integer> grids_x = new TreeSet<Integer>();
        TreeSet<Integer> grids_y = new TreeSet<Integer>();
        this.collectAbsoluteGrid(grids_x, grids_y);

        extendGrids(grids_x);
        extendGrids(grids_y);

        Collection<T> values = generatePoints(grids_x, grids_y, factory).values();
        return new CSet<T>(values);
    }

    private void collectAbsoluteGrid(Collection<Integer> grids_x, Collection<Integer> grids_y){
        grids_x.add(this.absoluteX);
        grids_y.add(this.absoluteY);
        if(this.width > 0)
            grids_x.add(this.absoluteX + this.width - 1);

        if(this.height > 0)
            grids_y.add(this.absoluteY + this.height - 1);

        if(this.children != null)
            for(ViewInfo child : this.children){
                child.collectAbsoluteGrid(grids_x, grids_y);
            }
    }

    //DEPRECATED: We have to consider Attached location and Scrolled location of each view
    //However, there is no way to collect Attached location information from outside.
    /*
    private void collectGrid(Collection<Integer> grids_x, Collection<Integer> grids_y) {
        collectGrid(grids_x, grids_y, 0, 0);
    }

    private void collectGrid(Collection<Integer> grids_x, Collection<Integer> grids_y, int px, int py) {
        int my_x = px + this.x;
        int my_y = py + this.y;

        //System.out.println("!!!" + this.y);
        //System.out.println("!!!" + py);

        grids_x.add(my_x);
        grids_x.add(my_x + this.width);
        grids_y.add(my_y);
        grids_y.add(my_y + this.height);

        if (this.children == null) return;
        for (ViewInfo child : this.children) {
            child.collectGrid(grids_x, grids_y, my_x, my_y);
        }
    }
    */



    public ViewInfo projectAbsolute(Integer ix, Integer iy){
        //Miss
        if(this.absoluteX > ix || this.absoluteX + this.width <= ix || this.absoluteY > iy || this.absoluteY + this.height <= iy)
            return null;

        //I'm hit and has child.
        if(this.children != null){
            //Assumption : children never intersect
            ViewInfo projected_child;
            for (ViewInfo child : children) {
                projected_child = child.projectAbsolute(ix, iy);
                if (projected_child != null)
                    return projected_child;
            }
        }

        //The point hit no child.
        //Thus return my self.
        if(this.visible)
            return this;
        return null;
    }
    //DEPRECATED: because of Scroll location and Attached location
    /*
    public ViewInfo project(Integer x, Integer y) {
        try {
            return this.project(x, y, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private ViewInfo project(Integer x, Integer y, int px, int py) {
        //System.out.println((px+x)+","+(y+py));
        int my_x = this.x + px;
        int my_y = this.y + py;

        //Assumption : parent include children
        if (my_x > x || my_x + this.width <= x)
            if (my_y > y || my_y + this.height <= y)
                return null;

        //If there is no children, just return myself
        if (children == null){
            if(this.visible)
                return this;
            return null;
        }

        //Assumption : children never intersect
        ViewInfo projected_child;
        for (ViewInfo child : children) {
            projected_child = child.project(x, y, my_x, my_y);
            if (projected_child != null)
                return projected_child;
        }

        //The point hit no child.
        //Thus return my self.
        return this;
    }
    */

    private static void extendGrids(TreeSet<Integer> grids) {
        TreeSet<Integer> inter_grids = new TreeSet<Integer>();

        Integer prev = 0;
        for (Integer cur : grids) {
            if (prev == 0 || prev + 1 == cur) {
                prev = cur;
                continue;
            }
            inter_grids.add((prev + cur) / 2);
            prev = cur;
        }
        grids.addAll(inter_grids);

    }

    private <T> Map<ViewInfo, T> generatePoints(TreeSet<Integer> grids_x, TreeSet<Integer> grids_y, PointFactory<T> factory) {
        TreeMap<ViewInfo, T> map = new TreeMap<ViewInfo, T>();
        ViewInfo hit;
        for (Integer x : grids_x) {
            for (Integer y : grids_y) {
                //System.out.println("("+x+","+y+")");
                hit = this.projectAbsolute(x, y);
                if (hit != null) map.put(hit, factory.get(x, y,hit));
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    LinkedList<ViewInfo> getChildren() {
        return (LinkedList<ViewInfo>) children.clone();
    }

    @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    public static interface PointFactory<T> {
        public T get(int x, int y, ViewInfo v);
    }

    public void setIsEditText(boolean f){
        isEditText = f;
    }

    public boolean isEditText(){
        return isEditText;
    }

    public void setTextContent(String s){
        textContent = s;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setFocus(boolean f){
        hasFocus = f;
    }

    public boolean hasFocus(){
        return this.hasFocus;
    }

    public int getId(){
        return this.id;
    }
}
