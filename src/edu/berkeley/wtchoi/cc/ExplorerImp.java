package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.Exploring.ExploreResult;
import edu.berkeley.wtchoi.cc.Exploring.Explorer;
import edu.berkeley.wtchoi.cc.Exploring.Guide;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.Driver;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;

import edu.berkeley.wtchoi.cc.learnerImp.Observation;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;

import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.cc.Exploring.ExploreRequest;


/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */


public class ExplorerImp implements Explorer<ICommand, Observation> {

    private static final int preSearchBound = 2;
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

    public ExploreResult<ICommand,Observation> explore(ExploreRequest<ICommand> request) {
        if (request.input == null) return null;
        if (request.input.size() == 0) return null;


        if(!request.fromCurrentState){
            for(int i = 0 ; i < preSearchBound ; i++){
                if(controller.isStopState()) break;
                CList<ICommand> recommendation = guide.recommend(currentState);
                if(recommendation == null) break;
                ExploreResult<ICommand,Observation> result = go(guide.recommend(currentState));
                guide.learn(result);
            }
            controller.restartApp();
            currentState.clear();
        }

        ExploreResult<ICommand,Observation> result =  go(request.input);
        result.query = request;
        return result;
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
        return new CVector<ICommand>(currentState);
    }

    public boolean init() {
        //1. Initiate connection with application
        if (!controller.initiateApp()) return false;
        System.out.println("App initialized");

        //2. Warming palette table for initial state
        ViewInfo view = controller.getCurrentView();
        if (view == null) return false;

        return true;
    }
}