package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.Exploring.ExploreResult;
import edu.berkeley.wtchoi.cc.Exploring.Explorer;
import edu.berkeley.wtchoi.cc.Exploring.Guide;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.Driver;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;

import edu.berkeley.wtchoi.cc.learnerImp.Observation;
import edu.berkeley.wtchoi.cc.learnerImp.TransitionInfo;

import java.util.List;
import java.util.LinkedList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.Exploring.ExploreRequest;


/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */


public class ExplorerImp implements Explorer<ICommand, Observation> {

    private static int preSearchBound = 3;
    private CList<ICommand> currentState;
    private Guide<ICommand,Observation> guide;

    private Driver<TransitionInfo> controller;
    private PointFactory pointFactory = TouchFactory.getInstance();

    public ExplorerImp(Driver<TransitionInfo> imp) {
        controller = imp;
        currentState = new CVector<ICommand>();
    }

    public void setGuide(Guide<ICommand,Observation> guide){
        this.guide = guide;
    }

    public List<ExploreResult<ICommand,Observation>> explore(ExploreRequest<ICommand> request) {
        if (request.input == null) return null;
        if (request.input.size() == 0) return null;

        List<ExploreResult<ICommand,Observation>> results = new LinkedList<ExploreResult<ICommand,Observation>>();

        if(!request.fromCurrentState){
            for(int i = 0 ; i < preSearchBound ; i++){
                if(controller.isStopState()) break;
                results.add(go(guide.recommand(currentState)));
            }
            controller.restartApp();
            currentState.clear();
        }

        results.add(go(request.input));
        return results;
    }

    private ExploreResult<ICommand,Observation> go(ICommand input){
        CVector<ICommand> tmp = new CVector<ICommand>();
        tmp.add(input);
        return go(tmp);
    }

    private ExploreResult<ICommand,Observation> go(CList<ICommand> input){
        CVector<Observation> output = new CVector<Observation>(input.size());
        CSet<ICommand> palette = null;
        CList<ICommand> startingState = new CVector<ICommand>(currentState);

        for (ICommand cmd : input) {
            boolean flag = controller.go(cmd);
            currentState.add(cmd);
            if (!flag){
                if(controller.isStopState()){
                    output.add(Observation.getStopObservation());
                    break;
                }
                if(controller.isErrorState()){
                    throw new RuntimeException("Cannot send command!");
                }
            }

            //Obtaining palette
            ViewInfo mv = controller.getCurrentView();
            //System.out.println(mv);
            palette = mv.getRepresentativePoints(pointFactory);

            //Obtaining transition information
            TransitionInfo ti = controller.getCurrentTransitionInfo();

            Observation state = new Observation(palette,ti);
            output.add(state);
        }
        return new ExploreResult<ICommand, Observation>(startingState,input,output);
    }

    public CList<ICommand> getIdleMachineState(){
        return currentState;
    }

    public boolean init() {
        //1. Initiate connection with application
        if (!controller.initiateApp()) return false;

        //2. Warming palette table for initial state
        ViewInfo view = controller.getCurrentView();
        if (view == null) return false;

        return true;
    }
}