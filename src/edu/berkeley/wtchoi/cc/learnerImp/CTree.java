package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.GraphViz;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import javax.swing.*;
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
    protected static Integer nidset = 0;
    protected CSet<ICommand> defaultPalette;

    private class Node implements Comparable<Node> {

        public String color = "gray";

        Integer id;
        Integer depth;
        private CSet<ICommand> palette;
        boolean isStopNode = false;

        Node parent;
        ICommand inputFromParent;
        TransitionInfo tiFromParent;

        Node mergeTo;
        boolean permanentlyMerged = false;

        Map<ICommand,Pair<Node,Observation>> children;

        private Node(CSet<ICommand> palette, int depth){
            children = new TreeMap<ICommand,Pair<Node,Observation>>();
            id = nidset++;
            this.palette = palette;
            this.depth = depth;

            leafSet.add(this);
        }

        private Node(Node p, ICommand i){
            parent = p;
            inputFromParent = i;
            children = new TreeMap<ICommand,Pair<Node,Observation>>();
            id = nidset++;
            depth = p.depth+1;
            if(depth > treeDepth) treeDepth = depth;

            leafSet.add(this);
        }

        private Node(){}

        public int compareTo(Node target){
            int f = depth.compareTo(target.depth);
            if(f == 0)
                return id.compareTo(target.id);
            return f;
        }

        public boolean isAncestorOf(Node n){
            Node cur = n;
            while(cur.parent != null){
                if(cur.id == this.id) return true;
                cur = cur.parent;
            }
            return false;
        }

        public void mergeTo(Node target,  boolean temporalFlag){
            mergeTo = target;
            permanentlyMerged = temporalFlag;
        }

        public boolean isMerged(){
            return (mergeTo != null);
        }
    }

    private  Node root;
    private Set<Node> leafSet;
    private int treeDepth;

    public CTree(CSet<ICommand> initialPalette, CSet<ICommand> defaultPalette){
        leafSet = new TreeSet<Node>();
        this.defaultPalette = defaultPalette;

        root = new Node(initialPalette,0);
        extend(root);
    }

    private Node getNode(List<ICommand> ilst){
        return getNode(root,ilst);
    }

    private Node getNode(Node startingNode, List<ICommand> ilst){
        Node cur = startingNode;
        for(ICommand i: ilst){
            if(!cur.children.containsKey(i)) return null;
            cur = cur.children.get(i).fst;
        }
        return cur;
    }

    public void addPath(List<ICommand> ilst, List<Observation> olst){
        addPathImp(root, ilst, olst);
    }

    public void addPath(State startingState, List<ICommand> ilst, List<Observation> olst){
        addPathImp(startingState.node, ilst, olst);
    }

    private void addPathImp(Node startingNode, List<ICommand> ilst, List<Observation> olst){
        Node cur = startingNode;
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

    private boolean buildInputPath(Node from, Node target, CList<ICommand> lst){
        if(target == from) return true;
        if(target == root) return false;
        if(buildInputPath(from, target.parent, lst)){
            lst.add(target.inputFromParent);
            return true;
        }
        else{
            return false;
        }
    }

    private void buildInputPath(Node target, CList<ICommand> lst){
        buildInputPath(root,target,lst);
    }

    private void extend(Node target){
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

    private final Set<Node> getLeafSet(){
        return leafSet;
    }

    public CList<ICommand> tryPruning(List<ICommand> ilst){
        return tryPruningImp(getNode(ilst));
    }

    public CList<ICommand> tryPruning(State startingState, List<ICommand> ilst){
        return tryPruningImp(getNode(startingState.node, ilst));
    }

    private CList<ICommand> tryPruningImp(Node target){
        CList<ICommand> rlst = new CVector<ICommand>();
        if(!target.tiFromParent.didNothing())
            return null;

        CSet<Node> candidates = new CSet<Node>();
        candidates.add(target.parent);

        CSet<Node> candidates2 = new CSet<Node>();

        while(!candidates.isEmpty()){
            Node candidate = candidates.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate,true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            if(candidate.parent != null && candidate.tiFromParent.didNothing()){
                candidates.add(candidate.parent);
                for(Pair<Node,Observation> ch: candidate.parent.children.values()){
                    if(ch.fst.id == candidate.id || leafSet.contains(ch.fst)) continue;
                    if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                        candidates2.add(ch.fst);
                }
            }
        }

        while(!candidates2.isEmpty()){
            Node candidate = candidates2.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate, true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            for(Pair<Node,Observation> ch: candidate.children.values()){
                if(leafSet.contains(ch.fst)) continue;
                if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                    candidates2.add(ch.fst);
            }
        }
        return null;
    }

    //merge for internal purpose
    private void doMerge(Node target, Node to, boolean temporalFlag){
        target.mergeTo(to,temporalFlag);
        if(temporalFlag) remove(target);
    }

    private void remove(Node n){
        if(n.isMerged()) return;
        leafSet.remove(n);
        for(Pair<Node,Observation> ch : n.children.values())
            remove(ch.getFirst());
    }


    //Interface for out side
    //----------------------
    class State implements Comparable<State>{
        Node node;
        CList<ICommand> input;

        CTree ctree;

        State(Node n,CTree t){node = n; input = new CVector<ICommand>(); ctree=t; this.normalize();}
        State(CList<ICommand> i, CTree t){node = root; input = i; ctree = t; this.normalize();}
        State(Node n, CList<ICommand> i, CTree t){ node = n; input = i; ctree = t; this.normalize();}

        public int compareTo(State st){  //TODO
            this.normalize();
            st.normalize();

            int f = this.node.compareTo(st.node);
            if(f != 0) return f;

            return this.input.compareTo(st.input);
        }

        void normalize(){
            if(input.isEmpty()) return;

            Node n = node;
            Iterator<ICommand> iter = input.iterator();
            while(iter.hasNext() && !leafSet.contains(n)){
                n = n.children.get(iter.next()).fst;
                if(n.isMerged()){
                    n = n.mergeTo;
                }
            }
            CList<ICommand> tmp = new CVector<ICommand>();
            while(iter.hasNext()) tmp.add(iter.next());

            this.node = n;
            this.input = tmp;
        }

        public void mergeTo(State target){
            ctree.doMerge(node, target.node, false);
        }

        public CList<ICommand> getInput(){
            CList<ICommand> temp = new CVector<ICommand>();
            buildInputPath(this.node,temp);
            temp.addAll((this.input));
            return temp;
        }

        public boolean isStopNode(){
            return node.isStopNode;
        }

        public String toString(){
            this.normalize();
            return String.valueOf(node.id);
        }

        public boolean isPrefixOf(CList<ICommand> input){
            Iterator<ICommand> iter = input.iterator();

            Node cur = root;
            Node target = this.node;
            while(iter.hasNext()){
                if(cur.compareTo(target) == 0) return true;

                ICommand i = iter.next();
                if(!cur.children.containsKey(i)) break;
                cur = cur.children.get(i).fst;
            }
            return false;
        }

        public boolean isPrefixOf(State target){
            return node.isAncestorOf(target.node);
        }

        public void removePrefixFrom(CList<ICommand> input){
            Iterator<ICommand> iter = input.iterator();
            CVector<ICommand> temp = new CVector<ICommand>();

            Node cur = root;
            Node target = this.node;
            while(iter.hasNext()){
                if(cur.compareTo(target) == 0) break;

                ICommand i = iter.next();
                if(!cur.children.containsKey(i)) return;
                cur = cur.children.get(i).fst;
            }
            if(!iter.hasNext()) return;

            while(iter.hasNext()){
                ICommand i = iter.next();
                temp.add(i);
                cur = cur.children.get(i).fst;
            }

            input.clear();
            input.addAll(temp);
        }

        public void setColor(String c){
            node.color = c;
        }

        public int getDepth(){
            return node.depth + input.size();
        }

        public State getMergeTo(){
            return new State(node.mergeTo, ctree);
        }
    }

    //State generators
    public State getInitState(){
        return new State(root,this);
    }

    public State getState(CList<ICommand> i){
        return new State(i,this);
    }

    public State getState(State s, CList<ICommand> input){
        CVector<ICommand> tmp = new CVector<ICommand>();
        tmp.addAll(s.input);
        tmp.addAll(input);
        return new State(s.node,tmp,this);
    }

    public State getState(State s, ICommand cmd){
        CVector<ICommand> input = new CVector<ICommand>();
        input.add(cmd);
        return getState(s,input);
    }

    //Utility Functions
    public CSet<ICommand> getPalette(State state){
        state.normalize();
        if(state.input.isEmpty()) return state.node.palette;
        return null;
    }

    public Observation getTransition(State state, ICommand cmd){
        state.normalize();
        Node n = state.node;

        if(leafSet.contains(n)) return null;
        if(!n.children.containsKey(cmd)) return null;
        return n.children.get(cmd).snd;

    }

    public CList<Observation> getTransition(State state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return null;

        Node cur = state.node;
        CList<Observation> output = new CVector<Observation>();
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return null;
            output.add(cur.children.get(i).snd);
            cur = cur.children.get(i).fst;
        }
        return output;
    }

    public boolean checkPossible(State state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return false;

        Node cur = state.node;
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return false;
            cur = cur.children.get(i).fst;
        }
        return true;
    }

    public boolean visited(State state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node)) return false;
        if(input.isEmpty()) return true;
        return !leafSet.contains(getNode(state.node,input));
    }

    //Assume input state is visited
    public CList<ICommand> recommendNext(State state){
        state.normalize();
        if(leafSet.contains(state.node)) return null;

        CList<ICommand> inputVector = new CVector<ICommand>();
        for(Node n : leafSet){
            if(buildInputPath(state.node, n, inputVector)) return inputVector;
            inputVector.clear();
        }
        return null;
    }

    public int depth(){
        return treeDepth;
    }


    //For Tree Visualization Part
    //----------------------------
    public void drawTree(String path){
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        drawTree(root, gv);
        gv.addln(gv.end_graph());

        java.io.File out = new java.io.File(path);
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "gif"), out);
    }

    protected void drawNode(Node n, GraphViz gv){
        String id1 = String.valueOf(n.id);
        if(!n.children.isEmpty())
            gv.addln(id1+" [style = bold, shape = circle, color="+n.color+"];");
        else if (!n.isStopNode)
            gv.addln(id1+" [shape = point, color=gray];");
    }

    protected void drawEdgeToChild(Node n, ICommand i, Node child, GraphViz gv){
        String id1 = String.valueOf(n.id);
        String id2 = String.valueOf(child.id);
        if(leafSet.contains(child)){
            if(n.color.equals("blue")){
                gv.addln(id1 + "->" + id2 + "[color=gray, fontsize=12, label=\""+ i+"\"];");
            }
            else{
                gv.addln(id1 + "->" + id2 + "[color=gray];");
            }
        }
        else{
            if(child.isMerged()){
                if(!child.permanentlyMerged){
                    gv.addln(id1 + "->" + child.mergeTo.id + "[color = green, fontsize=12, label=\""+ i+"\"];");
                }
                else if(child.tiFromParent.didNothing())
                    gv.addln(id1 + "->" + child.mergeTo.id + "[color = blue, fontsize=12, label=\""+ i+"\"];");
                else
                    gv.addln(id1 + "->" + child.mergeTo.id + "[label=\""+ i+"\"];");
            }
            else{
                if(child.isStopNode) return;
                if(child.tiFromParent.didNothing()){
                    gv.addln(id1 + "->" + id2 + "[style=bold, color=blue, label=\""+ i+"\"];");
                }
                else{
                    gv.addln(id1 + "->" + id2 + "[style=bold, label=\""+ i+"\"];");
                }
            }
        }
    }

    protected void drawTree(Node n, GraphViz gv){
        String id1 = String.valueOf(n.id);
        drawNode(n,gv);

        for(ICommand i: n.children.keySet()){
            Node child =n.children.get(i).fst;
            drawEdgeToChild(n,i,child,gv);
            if(child.isMerged()) continue;
            drawTree(child, gv);
        }
    }

    CTreeViewer viewer;
    public void startViewer(){
        viewer = new CTreeViewer(this);
        SwingUtilities.invokeLater(viewer);
    }

    public void updateView(){
        if(viewer != null)
            viewer.reload();
    }
}