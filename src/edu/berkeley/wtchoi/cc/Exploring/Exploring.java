package edu.berkeley.wtchoi.cc.Exploring;


import edu.berkeley.wtchoi.cc.util.datatype.CList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/23/12
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */

public class Exploring<I extends Comparable<I>, O extends Comparable<O>> {
    private Guide<I, O> guide;
    private Explorer<I, O> explorer;

    public Exploring(Guide<I, O> l, Explorer<I, O> t) {
        guide = l;
        explorer = t;
    }

    int resetCount = 0;

    public void run() {
        while (true) {
            CList<I> machineState = explorer.getIdleMachineState();
            ExploreRequest<I> request= guide.getRequest(machineState);

            resetCount  = !request.fromCurrentState?resetCount+1:resetCount;
            System.out.println("Number of Reset:"+resetCount);


            ExploreResult<I,O> result = explorer.explore(request);
            guide.learn(result);

            System.out.println("----------");
            //System.out.println(question);
            //System.out.println(answer);
        }
    }
}