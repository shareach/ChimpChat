package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.GraphViz;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
class CTree{
    private static Integer nidset = 0;
    private CSet<ICommand> defaultPalette;

    class Node implements Comparable<Node> {

        private Integer id;
        private CSet<ICommand> palette;

        Node parent;
        ICommand inputFromParent;
        TransitionInfo tiFromParent;

        Map<ICommand,Pair<Node,Observation>> children;

        private Node(CSet<ICommand> palette){
            children = new TreeMap<ICommand,Pair<Node,Observation>>();
            id = nidset++;
            this.palette = palette;
            leafSet.add(this);
        }

        private Node(Node p, ICommand i, Observation o){
            parent = p;
            inputFromParent = i;

            if(o != null){
                tiFromParent = o.getAugmentation();
                palette = o.getPalette();
            }
            children = new TreeMap<ICommand,Pair<Node,Observation>>();
            id = nidset++;
            leafSet.add(this);
        }

        public int compareTo(Node target){
            return id.compareTo(target.id);
        }
    }

    private Node root;
    private Set<Node> leafSet;

    public CTree(CSet<ICommand> initialPalette, CSet<ICommand> defaultPalette){
        leafSet = new TreeSet<Node>();
        this.defaultPalette = defaultPalette;

        root = new Node(initialPalette);
        extend(root);
    }

    public Node getNode(List<ICommand> ilst){
        Node cur = root;
        for(ICommand i: ilst){
            if(!cur.children.containsKey(i)) return null;
            cur = cur.children.get(i).fst;
        }
        return cur;
    }

    public void addPath(List<ICommand> ilst, List<Observation> olst){
        Node cur = root;
        Iterator<Observation> oiter = olst.iterator();
        Observation o;

        for(ICommand i : ilst){
            o = oiter.hasNext() ? oiter.next() : null;
            if(!cur.children.containsKey(i)){
                Node temp = new Node(cur,i,o);
                extend(temp);
                cur = temp;
            }
            else{
                Pair<Node,Observation> child = cur.children.get(i);
                if(child.snd == null) child.setSecond(o);
                cur = child.fst;
            }
        }
        updateView();
    }

    public void buildInputPath(Node target, List<ICommand> lst){
        if(target == root) return;
        buildInputPath(target.parent, lst);
        lst.add(target.inputFromParent);
    }

    public CSet<ICommand> getLastView(List<ICommand> ilst){
        Node cur = root;
        for(ICommand i: ilst){
            Pair<Node,Observation> temp = cur.children.get(i);
            if(temp == null) throw new RuntimeException("No Transition!");
            cur = temp.fst;
        }
        return cur.palette;
    }


    public void extend(Node target){
        leafSet.remove(target);
        for(ICommand option : target.palette){
            Observation o = null;
            Node temp = new Node(target, option, o);
            target.children.put(option, new Pair<Node,Observation>(temp,o));
        }
        for(ICommand option : defaultPalette){
            Observation o = null;
            Node temp = new Node(target, option, o);
            target.children.put(option, new Pair<Node,Observation>(temp,o));
        }
        updateView();
    }

    public final Set<Node> getLeafSet(){
        return leafSet;
    }

    public void drawTree(String path){
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        drawTree(root, gv);
        gv.addln(gv.end_graph());

        java.io.File out = new java.io.File(path);
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(),"gif"), out);
    }

    private void drawTree(Node n, GraphViz gv){
        Integer id1 = n.id;
        if(!n.children.isEmpty())
            gv.addln(id1+" [style=bold];");

        for(ICommand i: n.children.keySet()){

            Node child =n.children.get(i).fst;
            Integer id2 = child.id;
            if(leafSet.contains(child)){
                gv.addln(id1 + "->" + id2 + "[label=\""+ i +"\"];");
            }
            else{
                gv.addln(id1 + "->" + id2 + "[label=\""+ i+"\"];");
            }
            drawTree(child, gv);
        }
    }

    TreeViewer<ICommand,Observation> viewer;
    public void startViewer(){
        viewer = new TreeViewer<ICommand, Observation>(this);
        SwingUtilities.invokeLater(viewer);
    }

    private void updateView(){
        if(viewer != null)
            viewer.reload();
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
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters

        }

    }

    static class TreeViewer<I,Observation> implements Runnable{
        CTree tree;
        ImagePanel panel;
        JFrame frame;

        public TreeViewer(CTree ctree){
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
    }
}
