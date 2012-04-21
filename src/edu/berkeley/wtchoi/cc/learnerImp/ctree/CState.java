package edu.berkeley.wtchoi.cc.learnerImp.ctree;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;

import java.util.Iterator;


/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/20/12
 * Time: 10:12 PM
 * To change this template use File | Settings | File Templates.
 */
public //Interface for out side
        //----------------------
class CState implements Comparable<CState>{
    CNode node;
    CList<ICommand> input;

    CTree ctree;

    CState(CNode n, CList<ICommand> i, CTree t){
        node = (n == null) ? t.root  : n;
        input = (i == null) ? new CVector<ICommand>() : i;
        ctree = t;
        this.normalize();
    }

    public int compareTo(CState st){  //TODO
        this.normalize();
        st.normalize();

        int f = Integer.valueOf(this.getDepth()).compareTo(st.getDepth());
        if(f!=0) return f;

        f = this.node.compareTo(st.node);
        if(f != 0) return f;

        return this.input.compareTo(st.input);
    }

    void normalize(){
        if(input.isEmpty()) return;

        CNode n = node;
        Iterator<ICommand> iter = input.iterator();
        while(iter.hasNext() && !ctree.leafSet.contains(n)){
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

    public void mergeTo(CState target){
        ctree.doMerge(node, target.node, false);
    }

    public void split(){
        ctree.split(node);
    }

    public CList<ICommand> getInput(){
        CList<ICommand> temp = new CVector<ICommand>();
        ctree.buildInputPath(this.node, temp);
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

        CNode cur = ctree.root;
        CNode target = this.node;
        while(iter.hasNext()){
            if(cur.compareTo(target) == 0) return true;

            ICommand i = iter.next();
            if(!cur.children.containsKey(i)) break;
            cur = cur.children.get(i).fst;
        }
        return false;
    }

    public boolean isPrefixOf(CState target){
        return node.isAncestorOf(target.node);
    }

    public void removePrefixFrom(CList<ICommand> input){
        Iterator<ICommand> iter = input.iterator();
        CVector<ICommand> temp = new CVector<ICommand>();

        CNode cur = ctree.root;
        CNode target = this.node;
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

    public CState getMergeTo(){
        return new CState(node.mergeTo, null, ctree);
    }
}
