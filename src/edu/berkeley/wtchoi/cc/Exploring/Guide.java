package edu.berkeley.wtchoi.cc.Exploring;


import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/23/12
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Guide<I extends Comparable<I>, O extends Comparable<O>> {
    public ExploreRequest<I> getRequest(CList<I> currentMachineState);
    public void learn(ExploreResult<I,O> report);
    public CList<ICommand> recommend(CList<I> currentMachineState);
}