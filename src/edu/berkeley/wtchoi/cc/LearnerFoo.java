package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.learning.Learner;
import edu.berkeley.wtchoi.cc.learning.TeacherP;
import edu.berkeley.wtchoi.cc.util.GraphViz;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LearnerFoo implements Learner<ICommand, ViewState, AppModel> {
    private TeacherP<ICommand, ViewState, AppModel> teacher;
    private CTree<ICommand, ViewState> ctree;


    public LearnerFoo(TeacherP<ICommand, ViewState, AppModel> teacher) {
        this.teacher = teacher;
        ctree = new CTree<ICommand, ViewState>();
        CSet<ICommand> initialPalette = teacher.getPalette(new CVector());

        ctree.extend(ctree.getRoot(), initialPalette);
        ctree.startViewer();
    }

    private Collection<CList<ICommand>> makeInputs(CList<ICommand> prefix, CSet<ICommand> alphabet) {
        if (prefix == null) {
            prefix = new CVector<ICommand>();
        }

        Collection<CList<ICommand>> set = new TreeSet<CList<ICommand>>();

        for (ICommand t : alphabet) {
            CList<ICommand> new_input = new CVector<ICommand>(prefix);
            new_input.add(t);
            set.add(new_input);
        }

        return set;
    }

    public boolean learnedHypothesis() {
        return false;  // Do nothing
    }

    public CList<ICommand> getQuestion() {
        //CVector<ICommand> temp = new CVector<ICommand>();
        //temp.add(PushCommand.getMenu());
        //temp.add(new TouchCommand(476,799));
        //return temp;

        if(ctree.getLeafSet().isEmpty()) return null;
        CVector<ICommand> question = new CVector<ICommand>();
        ctree.buildInputPath(ctree.getLeafSet().iterator().next(), question);
        return question;
    }

    public void learn(CList<ICommand> input, CList<ViewState> output) {
        ctree.addPath(input,output);

        CSet<ICommand> palette = teacher.getPalette(input);
        ctree.extend(ctree.getNode(input), palette);
    }

    public void learnCounterExample(Pair<CList<ICommand>, CList<ViewState>> ce) {
    } // Do nothing

    public CList<ViewState> calculateTransition(CList<ICommand> input) {
        CVector<ViewState> lst = new CVector<ViewState>();
        ctree.getTransition(input, lst);
        return lst;
    }

    public AppModel getModel() {
        return null;
    }
}

class CTree<I,O>{
    private static Integer nidset = 0;

    class Node implements Comparable<Node> {

        private Integer id;
        Node parent;
        I inputFromParent;
        O outputFromParent;

        Map<I,Pair<Node,O>> children;

        private Node(){
            children = new TreeMap<I,Pair<Node,O>>();
            id = nidset++;
            leafSet.add(this);
        }

        private Node(Node p, I i, O o){
            parent = p;
            inputFromParent = i;
            outputFromParent = o;
            children = new TreeMap<I,Pair<Node,O>>();
            id = nidset++;
            leafSet.add(this);
        }

        public int compareTo(Node target){
            return id.compareTo(target.id);
        }
    }

    private Node root;
    private Set<Node> leafSet;

    public CTree(){
        leafSet = new TreeSet<Node>();
        root = new Node();
    }

    public Node getNode(List<I> ilst){
        Node cur = root;
        for(I i: ilst){
            if(!cur.children.containsKey(i)) return null;
            cur = cur.children.get(i).fst;
        }
        return cur;
    }

    public void addPath(List<I> ilst, List<O> olst){
        Node cur = root;
        Iterator<O> oiter = olst.iterator();
        O o;

        for(I i : ilst){
            o = oiter.hasNext() ? oiter.next() : null;
            if(!cur.children.containsKey(i)){
                Node temp = new Node(cur,i,o);
                cur.children.put(i, new Pair<Node,O>(temp,o));
                leafSet.remove(cur);
                cur = temp;
            }
            else{
                Pair<Node,O> child = cur.children.get(i);
                if(child.snd == null) child.setSecond(o);
                cur = child.fst;
            }
        }
        updateView();
    }

    public void buildInputPath(Node target, List<I> lst){
        if(target == root) return;
        buildInputPath(target.parent, lst);
        lst.add(target.inputFromParent);
    }

    public void getTransition(List<I> ilst, List<O> result){
        Node cur = root;
        for(I i: ilst){
            Pair<Node,O> temp = cur.children.get(i);
            if(temp == null) throw new RuntimeException("No Transition!");
            result.add(temp.snd);
            cur = temp.fst;
        }
        return;
    }


    public void extend(Node target, Collection<I> options){
        leafSet.remove(target);
        for(I option : options){
            O o = null;
            Node temp = new Node(target, option, o);
            target.children.put(option, new Pair<Node,O>(temp,o));
        }
        updateView();
    }

    public Node getRoot(){
        return root;
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

        for(I i: n.children.keySet()){

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

    TreeViewer<I,O> viewer;
    public void startViewer(){
        viewer = new TreeViewer<I, O>(this);
        SwingUtilities.invokeLater(viewer);
    }

    private void updateView(){
        if(viewer != null)
            viewer.reload();
    }

}

class ImagePanel extends JPanel{

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

class TreeViewer<I,O> implements Runnable{
    CTree<I,O> tree;
    ImagePanel panel;
    JFrame frame;

    public TreeViewer(CTree<I,O> ctree){
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