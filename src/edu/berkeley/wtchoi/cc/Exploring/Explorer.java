package edu.berkeley.wtchoi.cc.Exploring;

import edu.berkeley.wtchoi.collection.CList;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/23/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */

//Explorer actually perform state exploration with respect to query
public interface Explorer<I extends Comparable<I>, O extends Comparable<O>> {
    public ExploreResult<I,O> explore(final ExploreRequest<I> request);
    public CList<I> getIdleMachineState();
}
