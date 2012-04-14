package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Observation implements Comparable<Observation>{
    private CSet<ICommand> palette;
    private TransitionInfo augmentation;

    public Observation(CSet<ICommand> k, TransitionInfo a){
        palette = k;
        augmentation = a;
    }

    public int compareTo(Observation o){
        return this.palette.compareTo(o.palette);
    }

    public CSet<ICommand> getPalette(){return palette;}
    public TransitionInfo getAugmentation(){ return augmentation;}

    public String toString(){
        return palette.toString();
    }
}
