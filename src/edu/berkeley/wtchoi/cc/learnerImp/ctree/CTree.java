package edu.berkeley.wtchoi.cc.learnerImp.ctree;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.learnerImp.Observation;
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
public class CTree{
    protected CSet<ICommand> defaultPalette;

    CNode root;
    Set<CNode> leafSet;
    private int treeDepth;

    public CTree(CSet<ICommand> initialPalette, CSet<ICommand> defaultPalette){
        leafSet = new TreeSet<CNode>();
        this.defaultPalette = defaultPalette;

        root = new CNode();
        root.palette = initialPalette;
        root.depth = 0;
        leafSet.add(root);
        extend(root);
    }

    public CNode makeNode(CNode p, ICommand i){
        CNode n = new CNode();

        n.parent = p;
        n.inputFromParent = i;

        n.depth = p.depth+1;
        if(n.depth > treeDepth) treeDepth = n.depth;

        leafSet.add(n);

        return n;
    }

    private CNode getNode(CNode startingNode, List<ICommand> ilst){
        CNode cur = startingNode;
        for(ICommand i: ilst){
            if(!cur.children.containsKey(i)) return null;
            cur = cur.children.get(i).fst;
            if(cur.isMerged() && cur.permanentlyMerged)
                cur = cur.mergeTo;
        }
        return cur;
    }

    public void addPath(CState startingState, List<ICommand> ilst, List<Observation> olst){
        addPathImp(startingState.node, ilst, olst);
    }

    private void addPathImp(CNode startingNode, List<ICommand> ilst, List<Observation> olst){
        CNode cur = startingNode;
        Iterator<Observation> oiter = olst.iterator();
        Observation o;

        for(ICommand i : ilst){
            o = oiter.next();
            Pair<CNode,Observation> child = cur.children.get(i);
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

    boolean buildInputPath(CNode from, CNode target, CList<ICommand> lst){
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

    void buildInputPath(CNode target, CList<ICommand> lst){
        buildInputPath(root,target,lst);
    }

    private void extend(CNode target){
        leafSet.remove(target);
        if(target.isStopNode) return;

        for(ICommand i : target.palette){
            CNode temp = makeNode(target, i);
            target.children.put(i, new Pair<CNode,Observation>(temp,null));
        }
        for(ICommand i : defaultPalette){
            CNode temp = makeNode(target, i);
            target.children.put(i, new Pair<CNode,Observation>(temp,null));
        }
    }

    private final Set<CNode> getLeafSet(){
        return leafSet;
    }

    public CList<ICommand> tryPruning(CState startingState, List<ICommand> ilst){
        return tryPruningImp(getNode(startingState.node, ilst));
    }

    private CList<ICommand> tryPruningImp(CNode target){
        CList<ICommand> rlst = new CVector<ICommand>();
        if(!target.tiFromParent.didNothing())
            return null;

        CSet<CNode> candidates = new CSet<CNode>();
        candidates.add(target.parent);

        CSet<CNode> candidates2 = new CSet<CNode>();

        while(!candidates.isEmpty()){
            CNode candidate = candidates.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate,true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            if(candidate.parent != null && candidate.tiFromParent.didNothing()){
                candidates.add(candidate.parent);
                for(Pair<CNode,Observation> ch: candidate.parent.children.values()){
                    if(ch.fst.id == candidate.id || leafSet.contains(ch.fst)) continue;
                    if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                        candidates2.add(ch.fst);
                }
            }
        }

        while(!candidates2.isEmpty()){
            CNode candidate = candidates2.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate, true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            for(Pair<CNode,Observation> ch: candidate.children.values()){
                if(leafSet.contains(ch.fst)) continue;
                if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                    candidates2.add(ch.fst);
            }
        }
        return null;
    }

    //merge for internal purpose
    void doMerge(CNode target, CNode to, boolean temporalFlag){
        target.mergeTo(to,temporalFlag);
        if(temporalFlag) remove(target);
    }

    void split(CNode node){
        node.mergeTo = null;
    }

    private void remove(CNode n){
        if(n.isMerged()) return;
        leafSet.remove(n);
        for(Pair<CNode,Observation> ch : n.children.values())
            remove(ch.getFirst());
    }

    //CState generators
    public CState getInitState(){
        return new CState(root, null,this);
    }

    public CState getState(CList<ICommand> i){
        return new CState(null, i, this);
    }

    public CState getState(CState s, CList<ICommand> input){
        CVector<ICommand> tmp = new CVector<ICommand>();
        tmp.addAll(s.input);
        tmp.addAll(input);
        return new CState(s.node,tmp,this);
    }

    public CState getState(CState s, ICommand cmd){
        CVector<ICommand> input = new CVector<ICommand>();
        input.add(cmd);
        return getState(s,input);
    }

    //Utility Functions
    public CSet<ICommand> getPalette(CState state){
        state.normalize();
        if(state.input.isEmpty()) return state.node.palette;
        return null;
    }

    public Observation getTransition(CState state, ICommand cmd){
        state.normalize();
        CNode n = state.node;

        if(leafSet.contains(n)) return null;
        if(!n.children.containsKey(cmd)) return null;
        return n.children.get(cmd).snd;

    }

    public CList<Observation> getTransition(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return null;

        CNode cur = state.node;
        CList<Observation> output = new CVector<Observation>();
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return null;
            output.add(cur.children.get(i).snd);
            cur = cur.children.get(i).fst;
        }
        return output;
    }

    public boolean checkPossible(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return false;

        CNode cur = state.node;
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return false;
            cur = cur.children.get(i).fst;
        }
        return true;
    }

    public boolean visited(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node)) return false;
        if(input.isEmpty()) return true;
        return !leafSet.contains(getNode(state.node,input));
    }

    //Assume input state is visited
    private static final int recommandThreshold = 3;
    public CList<ICommand> recommendNext(CState state){
        state.normalize();
        if(leafSet.contains(state.node)) return null;

        CList<ICommand> inputVector = new CVector<ICommand>();
        for(CNode n : leafSet){
            if(buildInputPath(state.node, n, inputVector))
                if(n.depth - state.getDepth() < recommandThreshold)
                    return inputVector;
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

    protected void drawNode(CNode n, GraphViz gv){
        String id1 = String.valueOf(n.id);
        if(!n.children.isEmpty())
            gv.addln(id1+" [style = bold, shape = circle, color="+n.color+"];");
        else if (!n.isStopNode)
            gv.addln(id1+" [shape = point, color=gray];");
    }

    protected void drawEdgeToChild(CNode n, ICommand i, CNode child, GraphViz gv){
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

    protected void drawTree(CNode n, GraphViz gv){
        String id1 = String.valueOf(n.id);
        drawNode(n,gv);

        for(ICommand i: n.children.keySet()){
            CNode child =n.children.get(i).fst;
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