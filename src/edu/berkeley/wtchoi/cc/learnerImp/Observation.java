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

    private static Observation stopObservation = new Observation(new CSet<ICommand>(),new TransitionInfo());

    public Observation(CSet<ICommand> k, TransitionInfo a){
        palette = k;
        augmentation = a;
    }

    public int compareTo(Observation o){
        boolean f1 = (this == stopObservation);
        boolean f2 = (o == stopObservation);

        if(f1 && f2) return 0;
        if(f1 && !f2) return 1;
        if(f2 && !f1) return -1;

        return this.palette.compareTo(o.palette);
    }

    public CSet<ICommand> getPalette(){return palette;}
    public TransitionInfo getAugmentation(){ return augmentation;}

    public String toString(){
        return palette.toString()+augmentation.didNothing();
    }

    public static Observation getStopObservation(){
        return stopObservation;
    }

    public boolean isStopObservation(){
        return this == stopObservation;
    }

    public boolean equalsTo(Observation target){
        if(this.compareTo(target) == 0){
            return augmentation.equalsTo(target.augmentation);
        }
        return false;
    }
}
