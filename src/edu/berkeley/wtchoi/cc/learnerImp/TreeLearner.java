package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.Exploring.ExploreRequest;
import edu.berkeley.wtchoi.cc.Exploring.ExploreResult;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.Exploring.Guide;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import java.util.TreeMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class TreeLearner implements Guide<ICommand, Observation> {
    private CTree ctree;
    private CSet<ICommand> defaultPalette;

    private CSet<State> uniqueStates; //S
    private CSet<State> frontierStates;  //SI

    private CSet<CList<ICommand>> suffixes; //E

    private CSet<State> uniquesToBeTested; //for implementation
    private CSet<State> frontiersToBeTested; //for implementation
    //INVARIANT : state in stateToBeTested <==> state has remainedObservations

    private TreeMap<State,CSet<CList<ICommand>>> remainedObservations;
    private TreeMap<State,CSet<CList<ICommand>>> fetchedObservations;


    public TreeLearner(CSet<ICommand> initialPalette) {
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());
        defaultPalette.add(PushCommand.getBack());

        ctree = new CTree(initialPalette,defaultPalette);
        ctree.startViewer();

        uniqueStates = new CSet<State>();
        frontierStates = new CSet<State>();
        suffixes = new CSet<CList<ICommand>>();

        uniquesToBeTested = new CSet<State>();
        frontiersToBeTested = new CSet<State>();
        remainedObservations = new TreeMap<State, CSet<CList<ICommand>>>();
        fetchedObservations = new TreeMap<State, CSet<CList<ICommand>>>();

        State initState = ctree.getInitState();
        addToFrontier(initState);
    }

    private void addToFrontier(State s){
        frontierStates.add(s);
        frontiersToBeTested.add(s);

        remainedObservations.put(s, new CSet<CList<ICommand>>());
        fetchedObservations.put(s, new CSet<CList<ICommand>>());

        CSet<ICommand> palette = ctree.getPalette(s);
        buildObservation(remainedObservations.get(s), palette);

        s.setColor("blue");
    }

    private void extendUnique(State state){
        CSet<ICommand> palette = ctree.getPalette(state);
        for(ICommand cmd:palette){
            State f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            addToFrontier(f);
        }
        for(ICommand cmd:defaultPalette){
            State f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            addToFrontier(f);
        }
    }

    private void buildObservation(CSet<CList<ICommand>> observations, CSet<ICommand> palette){
        for(ICommand cmd:palette){
            CVector<ICommand> tmp = new CVector<ICommand>();
            tmp.add(cmd);
            observations.add(tmp);
        }
        for(ICommand cmd:defaultPalette){
            CVector<ICommand> tmp = new CVector<ICommand>();
            tmp.add(cmd);
            observations.add(tmp);
        }
        observations.addAll(suffixes);
    }

    public ExploreRequest<ICommand> getRequest(CList<ICommand> machineState){
        ExploreRequest<ICommand> request = getQuestionImp(machineState);
        updateView();
        return request;
    }

    static int count = 0;
    public ExploreRequest<ICommand> getQuestionImp(CList<ICommand> machineStatePrefix) {
        System.out.println("search round : "+ ++count);
        if(count == 3){
            count = count;
        }

        State machineState = ctree.getState(machineStatePrefix);
        while(true){
            if(uniquesToBeTested.isEmpty()) break;
            State state = peakState(uniquesToBeTested, machineState);
            CSet<CList<ICommand>> observations = remainedObservations.get(state);
            CList<ICommand> suffix = observations.pollFirst();
            if(!ctree.checkPossible(state, suffix)){
                observations.remove(suffix);
                if(observations.isEmpty()){
                    uniquesToBeTested.remove(state);
                }
                continue;
            }
            if(observations.isEmpty()){
                uniquesToBeTested.remove(state);
            }
            if(ctree.visited(state, suffix)) continue;
            return buildRequest(machineState, state, suffix);
        }

        while(true){
            if(frontiersToBeTested.isEmpty()) break;
            State state = peakState(frontiersToBeTested, machineState);
            CSet<CList<ICommand>> observations = remainedObservations.get(state);
            CList<ICommand> suffix = observations.pollFirst();
            if(!ctree.checkPossible(state, suffix)){
                //observations.remove(suffix);
                if(observations.isEmpty()){
                    frontiersToBeTested.remove(state);
                    closeState(state);
                }
                continue;
            }
            if(ctree.visited(state,suffix)){
                if(observations.isEmpty()){
                    frontiersToBeTested.remove(state);
                    closeState(state);
                }
                continue;
            }
            if(observations.isEmpty()){
                frontiersToBeTested.remove(state);
            }
            return buildRequest(machineState, state, suffix);
        }

        return null;
    }

    private State peakState(CSet<State> set, State machineState){
        if(set.contains(machineState)) return machineState;
        for(State s: set){
            if(machineState.isPrefixOf(s)) return s;
        }
        return set.first();
    }

    private ExploreRequest<ICommand> buildRequest(State machineState, State sut, CList<ICommand> suffix){
        fetchedObservations.get(sut).add(suffix);

        CList<ICommand> question = new CVector<ICommand>();
        question.addAll(sut.getInput());
        question.addAll(suffix);

        if(!machineState.isPrefixOf(question)){
            return new ExploreRequest<ICommand>(false, question, sut.getInput(), suffix);
        }
        machineState.removePrefixFrom(question);
        return new ExploreRequest<ICommand>(true, question, sut.getInput(), suffix);
    }


    private void closeState(State state){
        if(state.isStopNode()){
            frontierStates.remove(state);
            return;
        }
        for(State ustate: uniqueStates){
            if(checkObservationalEquivalence(state, ustate)){
                state.mergeTo(ustate);
                return;
            }
        }
        frontierStates.remove(state);
        uniqueStates.add(state);
        extendUnique(state);
        state.setColor("black");
    }

    private boolean checkObservationalEquivalence(State s1, State s2){
        if(!checkFullyObserved(s1) || !checkFullyObserved(s2))
            throw new RuntimeException("Something is Wrong!");

        CSet<ICommand> p1 = ctree.getPalette(s1);
        CSet<ICommand> p2 = ctree.getPalette(s2);
        if(p1.compareTo(p2) != 0) return false;
        for(ICommand cmd: p1){
            Observation o1 = ctree.getTransition(s1,cmd);
            Observation o2 = ctree.getTransition(s2,cmd);
            if(!o1.equalsTo(o2)) return false;
        }
        for(ICommand cmd: defaultPalette){
            Observation o1 = ctree.getTransition(s1,cmd);
            Observation o2 = ctree.getTransition(s2,cmd);
            if(!o1.equalsTo(o2)) return false;
        }
        for(CList<ICommand> suffix: suffixes){
            CList<Observation> o1 = ctree.getTransition(s1,suffix);
            CList<Observation> o2 = ctree.getTransition(s2,suffix);
            if(o1 != o2) return false;
            if(o1 == null) continue;
            if(checkEquality(o1,o2)) return false;
        }
        return true;
    }

    private boolean checkEquality(CList<Observation> o1, CList<Observation> o2){
        if(o1.size() != o2.size()) return false;
        Iterator<Observation> i1 = o1.iterator();
        Iterator<Observation> i2 = o2.iterator();
        while(i1.hasNext()){
            if(i1.next().equalsTo(i2.next())) continue;
            return false;
        }
        return true;
    }

    public void learn(ExploreResult<ICommand,Observation> result) {
        learnImp(result);
        //ctree.updateView();
    }

    private void learnImp(ExploreResult<ICommand,Observation> result) {
        CList<ICommand> equalInput;
        State state;

        if(count == 3){
            count = count;
        }


        State startingState = ctree.getState(result.startingState);
        ctree.addPath(startingState,result.input,result.output);
        equalInput = ctree.tryPruning(startingState, result.input);
        state = ctree.getState(startingState, result.input);

        if(result.query != null){
            State sut = ctree.getState(result.query.sut);
            fetchedObservations.get(sut).remove(result.query.suffix);
            if(frontierStates.contains(sut) && checkFullyObserved(sut))
                closeState(sut);
        }

        if(equalInput == null){
            System.out.println("reached state : " + state);
        }

        updateView();
    }

    public CList<ICommand> recommand(CList<ICommand> statePrefix){
        return ctree.recommendNext(ctree.getState(statePrefix));
    }


    private boolean checkFullyObserved(State s){
        return remainedObservations.get(s).isEmpty() && fetchedObservations.get(s).isEmpty();
    }

    public void updateView(){
        ctree.updateView();
    }
}

