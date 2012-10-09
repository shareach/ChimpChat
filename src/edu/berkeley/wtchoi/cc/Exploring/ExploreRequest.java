package edu.berkeley.wtchoi.cc.Exploring;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/20/12
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
import edu.berkeley.wtchoi.collection.CList;

public class ExploreRequest<I extends Comparable<I>> {
    public boolean fromCurrentState;
    public CList<I> input;

    public CList<I> sut;
    public CList<I> suffix;

    public ExploreRequest(boolean flag, CList<I> r, CList<I> sut, CList<I> suffix){
        fromCurrentState = flag;
        input = r;

        this.sut = sut;
        this.suffix = suffix;
    }
}
