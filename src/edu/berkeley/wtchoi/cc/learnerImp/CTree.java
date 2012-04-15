package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.GraphViz;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
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

        Integer id;
        private CSet<ICommand> palette;
        private boolean isStopNode = false;

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

        private Node(Node p, ICommand i){
            parent = p;
            inputFromParent = i;
            children = new TreeMap<ICommand,Pair<Node,Observation>>();
            id = nidset++;
            leafSet.add(this);
        }

        private Node(){}

        public int compareTo(Node target){
            return id.compareTo(target.id);
        }
    }

    class MergeNode extends Node{
        private Node mergeTo;

        public MergeNode(Node target, Node to){
            parent = target.parent;
            inputFromParent = target.inputFromParent;
            tiFromParent = target.tiFromParent;
            id = target.id;
            mergeTo = to;

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
            o = oiter.next();
            Pair<Node,Observation> child = cur.children.get(i);
            if(o.isStopObservation()) child.fst.isStopNode = true;
            if(child.snd == null){
                child.setSecond(o);
                child.fst.palette = o.getPalette();
                child.fst.tiFromParent = o.getAugmentation();
                System.out.println("TI!:" + o.getAugmentation().didNothing());
                extend(child.fst);
            }
            if(o.isStopObservation()) break;
            cur = child.fst;
        }
    }

    public void buildInputPath(Node target, List<ICommand> lst){
        if(target == root) return;
        buildInputPath(target.parent, lst);
        lst.add(target.inputFromParent);
    }

    public void extend(Node target){
        leafSet.remove(target);
        if(target.isStopNode) return;

        for(ICommand i : target.palette){
            Node temp = new Node(target, i);
            target.children.put(i, new Pair<Node,Observation>(temp,null));
        }
        for(ICommand i : defaultPalette){
            Node temp = new Node(target, i);
            target.children.put(i, new Pair<Node,Observation>(temp,null));
        }
    }

    public final Set<Node> getLeafSet(){
        return leafSet;
    }

    public CList<ICommand> tryPruning(List<ICommand> ilst){
        Node target = getNode(ilst);

        CList<ICommand> rlst = new CVector<ICommand>();
        if(!target.tiFromParent.didNothing())
            return null;

        CSet<Node> candidates = new CSet<Node>();
        candidates.add(target.parent);

        CSet<Node> candidates2 = new CSet<Node>();

        while(!candidates.isEmpty()){
            Node candidate = candidates.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate);
                buildInputPath(candidate,rlst);
                return rlst;
            }

            if(candidate.parent != null && candidate.tiFromParent.didNothing()){
                candidates.add(candidate.parent);
                for(Pair<Node,Observation> ch: candidate.parent.children.values()){
                    if(ch.fst.id == candidate.id) continue;
                    if(ch.fst.tiFromParent.didNothing() && ! (ch.fst instanceof MergeNode))
                        candidates2.add(ch.fst);
                }
            }
        }

        while(!candidates2.isEmpty()){
            Node candidate = candidates2.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            for(Pair<Node,Observation> ch: candidate.children.values()){
                if(leafSet.contains(ch.fst)) continue;
                if(ch.fst.tiFromParent.didNothing() && ! (ch.fst instanceof MergeNode))
                    candidates2.add(ch.fst);
            }
        }
        return null;
    }

    private void doMerge(Node target, Node to){
        Node ghost = new MergeNode(target,to);
        target.parent.children.get(target.inputFromParent).setFirst(ghost);
        remove(target);
    }

    private void remove(Node n){
        leafSet.remove(n);
        for(Pair<Node,Observation> ch : n.children.values())
            remove(ch.getFirst());
    }

    public void drawTree(String path){
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        drawTree(root, gv);
        gv.addln(gv.end_graph());

        java.io.File out = new java.io.File(path);
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "gif"), out);
    }

    private void drawTree(Node n, GraphViz gv){
        String id1 = String.valueOf(n.id);
        if(!n.children.isEmpty())
            gv.addln(id1+" [style = bold, shape = circle];");
        else if (!n.isStopNode)
            gv.addln(id1+" [shape = point, color=gray];");


        for(ICommand i: n.children.keySet()){

            Node child =n.children.get(i).fst;
            String id2 = String.valueOf(child.id);
            if(leafSet.contains(child)){
                gv.addln(id1 + "->" + id2 + "[color=gray, fontsize=10, label=\""+ i +"\"];");
            }
            else{
                if(child instanceof MergeNode){
                    MergeNode node = (MergeNode) child;
                    if(child.tiFromParent.didNothing())
                        gv.addln(id1 + "->" + node.mergeTo.id + "[color = blue, label=\""+ i+"\"];");
                    else
                        gv.addln(id1 + "->" + node.mergeTo.id + "[label=\""+ i+"\"];");
                    continue;
                }
                else{
                    if(child.isStopNode) continue;
                    if(child.tiFromParent.didNothing()){
                            gv.addln(id1 + "->" + id2 + "[style=bold, color=blue, label=\""+ i+"\"];");
                    }
                    else{
                        gv.addln(id1 + "->" + id2 + "[style=bold, label=\""+ i+"\"];");
                    }
                }
            }
            drawTree(child, gv);
        }
    }

    TreeViewer<ICommand,Observation> viewer;
    public void startViewer(){
        viewer = new TreeViewer<ICommand, Observation>(this);
        SwingUtilities.invokeLater(viewer);
    }

    public void updateView(){
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
            int iw = image.getWidth();
            int ih = image.getHeight();
            int maxw = 800;
            int maxh = 800;
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