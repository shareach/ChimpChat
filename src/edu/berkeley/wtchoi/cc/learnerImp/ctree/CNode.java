package edu.berkeley.wtchoi.cc.learnerImp.ctree;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.learnerImp.Observation;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/20/12
 * Time: 10:16 PM
 * To change this template use File | Settings | File Templates.
 */
class CNode implements Comparable<CNode> {
    protected static Integer nidset = 0;

    final Integer id;

    CNode parent;
    ICommand inputFromParent;
    TransitionInfo tiFromParent;
    Map<ICommand,Pair<CNode,Observation>> children;

    String color = "gray";
    Integer depth;
    CSet<ICommand> palette;
    boolean isStopNode = false;

    CNode mergeTo;
    boolean permanentlyMerged = false;


    public CNode(){
        children = new TreeMap<ICommand,Pair<CNode,Observation>>();
        id = nidset++;
    }

    public int compareTo(CNode target){
        int f = depth.compareTo(target.depth);
        if(f != 0) return f;

        return id.compareTo(target.id);
    }

    public boolean isAncestorOf(CNode n){
        CNode cur = n;
        while(cur.parent != null){
            if(cur.id == this.id) return true;
            cur = cur.parent;
        }
        return false;
    }

    public void mergeTo(CNode target,  boolean temporalFlag){
        mergeTo = target;
        permanentlyMerged = temporalFlag;
    }

    public boolean isMerged(){
        return (mergeTo != null);
    }
}