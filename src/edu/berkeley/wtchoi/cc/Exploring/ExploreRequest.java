package edu.berkeley.wtchoi.cc.Exploring;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/20/12
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
import edu.berkeley.wtchoi.cc.util.datatype.CList;

public class ExploreRequest<I extends Comparable<I>> {
    public boolean fromCurrentState;
    public CList<I> input;

    public ExploreRequest(boolean flag, CList<I> r){
        fromCurrentState = flag;
        input = r;
    }
}
