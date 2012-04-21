package edu.berkeley.wtchoi.cc.Exploring;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/20/12
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;

public class ExploreResult<I extends Comparable<I>, O extends Comparable<O>> {
    public CList<I> startingState;
    public CList<I> input;
    public CList<O> output;
    public ExploreRequest<I> query;

    public ExploreResult(CList<I> startingInput, CList<I> input, CList<O> output){
        this.startingState = startingInput;
        this.input  = input;
        this.output = output;
        this.query  = null;
    }
}
