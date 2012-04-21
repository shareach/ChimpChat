package edu.berkeley.wtchoi.cc.learnerImp;

import java.util.List;
import edu.berkeley.wtchoi.cc.Exploring.ExploreRequest;
import edu.berkeley.wtchoi.cc.Exploring.ExploreResult;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.Exploring.Guide;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import edu.berkeley.wtchoi.cc.learnerImp.CTree.State;

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
    private TreeMap<State,CSet<CList<ICommand>>> remainedObservations;
    //INVARIANT : state in stateToBeTested <==> state has remainedObservations

    private State stateUnderTesting;
    //private State currentMachineState;
    //private boolean askedRestartingQuestion;
    //private boolean askedPreSearch;
    //private boolean preSearchFailed = false;
    //private final int preSearchBound = 2;

    public TreeLearner(CSet<ICommand> initialPalette) {
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());
        defaultPalette.add(PushCommand.getBack());

        ctree = new CTree(initialPalette,defaultPalette);
        ctree.startViewer();

        uniqueStates = new CSet<State>();
        frontierStates = new CSet<State>();
        suffixes = new CSet<CList<ICommand>>();
        remainedObservations = new TreeMap<State, CSet<CList<ICommand>>>();
        uniquesToBeTested = new CSet<State>();
        frontiersToBeTested = new CSet<State>();

        State initState = ctree.getInitState();
        //currentMachineState = initState;
        stateUnderTesting = initState;

        //uniqueStates.add(initState);
        //uniquesToBeTested.add(initState);
        //extendUnique(initState);

        frontierStates.add(initState);
        frontiersToBeTested.add(initState);
        extendObservation(initState);
        initState.setColor("blue");
    }

    private void extendUnique(State state){
        CSet<ICommand> palette = ctree.getPalette(state);
        for(ICommand cmd:palette){
            State f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            frontierStates.add(f);
            frontiersToBeTested.add(f);
            extendObservation(f);
            f.setColor("blue");
        }
        for(ICommand cmd:defaultPalette){
            State f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            frontierStates.add(f);
            frontiersToBeTested.add(f);
            extendObservation(f);
            f.setColor("blue");
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


    public boolean learnedHypothesis() {
        return false;  // Do nothing
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
        //Manaul approach
        //---------------
        //questionVector.add(PushCommand.getMenu());
        //questionVector.add(new TouchCommand(476,799));
        //return pruneQuestion(questionVector);



        //Pure CTree approach
        //-------------------
        //if(ctree.getLeafSet().isEmpty()) return null;
        //ctree.buildInputPath(ctree.getLeafSet().iterator().next(), questionVector);
        //return pruneQuestion(questionVector);


        //L* with CTree approach
        //----------------------
        //Bounded State Pre-Search
        count = count;

        /*
        if(!preSearchFailed){
            if(currentMachineState.getDepth() - stateUnderTesting.getDepth() < preSearchBound){
                CList<ICommand> recommendation = ctree.recommendNext(currentMachineState);
                if(recommendation != null){
                    questionVector.addAll(recommendation);
                    askedPreSearch = true;
                    return false;
                }
            }
        }
        preSearchFailed = false;
        askedPreSearch = false;
        */

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
                    remainedObservations.remove(state);
                }
                continue;
            }
            if(observations.isEmpty()){
                uniquesToBeTested.remove(state);
                remainedObservations.remove(state);
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
                    remainedObservations.remove(state);
                    closeState(state);
                }
                continue;
            }
            if(ctree.visited(state,suffix)){
                if(observations.isEmpty()){
                    frontiersToBeTested.remove(state);
                    remainedObservations.remove(state);
                    closeState(state);
                }
                continue;
            }
            if(observations.isEmpty()){
                frontiersToBeTested.remove(state);
                remainedObservations.remove(state);
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
        stateUnderTesting = sut;
        CList<ICommand> question = new CVector<ICommand>();
        question.addAll(sut.getInput());
        question.addAll(suffix);

        if(!machineState.isPrefixOf(question)){
            //askedRestartingQuestion = true;
            return new ExploreRequest<ICommand>(false, question);
        }
        //askedRestartingQuestion = false;
        machineState.removePrefixFrom(question);
        return new ExploreRequest<ICommand>(true, question);
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
        if(remainedObservations.containsKey(s1))
            if(remainedObservations.containsKey(s2))
                throw new RuntimeException("Something is Wrong!");

        //if(s1.isStopNode() != s2.isStopNode()) return false;
        //if(s1.isStopNode() && s2.isStopNode()) return true;
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

    public void learn(List<ExploreResult<ICommand,Observation>> results) {
        for(ExploreResult<ICommand,Observation> result: results){
            learnImp(result);
        }
        //ctree.updateView();
    }

    private void learnImp(ExploreResult<ICommand,Observation> result) {
        CList<ICommand> equalInput;
        State state;

        if(count == 3){
            count = count;
        }

        //if(askedPreSearch){
        //    ctree.addPath(currentMachineState, input, output);
        //    equalInput = ctree.tryPruning(currentMachineState,input);
        //    state = ctree.getState(currentMachineState,input);
        //    if(state.isStopNode()){
        //        preSearchFailed = true;
        //    }
            //if(equalInput == null){
            //    preSearchCount--;
            //}
        //}
        //else{
        State startingState = ctree.getState(result.startingState);
        ctree.addPath(startingState,result.input,result.output);
        equalInput = ctree.tryPruning(startingState, result.input);
        state = ctree.getState(startingState, result.input);

        State sut = stateUnderTesting;
        if(frontierStates.contains(sut) && !remainedObservations.containsKey(sut))
            closeState(sut);
        //}
        //currentMachineState = state;
        if(equalInput == null){
            System.out.println("reached state : " + state);
        }
    }

    public CList<ICommand> recommand(CList<ICommand> statePrefix){
        return ctree.recommendNext(ctree.getState(statePrefix));
    }

    public void extendObservation(State state){
        CSet<ICommand> palette = ctree.getPalette(state);
        CSet<CList<ICommand>> observations = new CSet<CList<ICommand>>();
        buildObservation(observations, palette);
        remainedObservations.put(state, observations);
    }

    public void learnCounterExample(Pair<CList<ICommand>, CList<Observation>> ce) {
    } // Do nothing

    public void updateView(){
        ctree.updateView();
    }
}

