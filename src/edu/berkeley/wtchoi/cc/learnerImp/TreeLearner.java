package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.AppModel;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.learning.Learner;
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

public class TreeLearner implements Learner<ICommand, Observation, AppModel> {
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
    private State currentMachineState;
    private boolean askedRestartingQuestion;
    private boolean askedPreSearch;
    private boolean preSearchFailed = false;
    private final int preSearchBound = 2;

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
        currentMachineState = initState;
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

    public boolean getQuestion(CList<ICommand> q){
        boolean flag = getQuestionImp(q);
        updateView();
        return flag;
    }

    static int count = 0;
    public boolean getQuestionImp(CList<ICommand> questionVector) {
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

        if(!preSearchFailed){
            if(currentMachineState.depth() - stateUnderTesting.depth() < preSearchBound){
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

        while(true){
            if(uniquesToBeTested.isEmpty()) break;
            State state = peakState(uniquesToBeTested);
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
            stateUnderTesting = state;
            questionVector.addAll(state.getInput());
            questionVector.addAll(suffix);
            return pruneQuestion(questionVector);
        }

        while(true){
            if(frontiersToBeTested.isEmpty()) break;
            State state = peakState(frontiersToBeTested);
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
            stateUnderTesting = state;
            CVector<ICommand> tmp = new CVector<ICommand>();
            questionVector.addAll(state.getInput());
            questionVector.addAll(suffix);
            return pruneQuestion(questionVector);
        }

        return false;
    }

    private State peakState(CSet<State> set){
        if(set.contains(currentMachineState)) return currentMachineState;
        for(State s: set){
            if(currentMachineState.isPrefixOf(s)) return s;
        }
        return set.first();
    }

    private boolean pruneQuestion(CList<ICommand> question){
        if(!currentMachineState.isPrefixOf(question)){
            askedRestartingQuestion = true;
            return true;
        }
        askedRestartingQuestion = false;
        currentMachineState.removePrefixFrom(question);
        return false;
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

    public void learn(CList<ICommand> input, CList<Observation> output) {
        learnImp(input, output);
        //ctree.updateView();
    }

    public void learnImp(CList<ICommand> input, CList<Observation> output) {
        CList<ICommand> equalInput;
        State state;

        if(count == 3){
            count = count;
        }

        if(askedPreSearch){
            ctree.addPath(currentMachineState, input, output);
            equalInput = ctree.tryPruning(currentMachineState,input);
            state = ctree.getState(currentMachineState,input);
            if(state.isStopNode()){
                preSearchFailed = true;
            }
            //if(equalInput == null){
            //    preSearchCount--;
            //}
        }
        else{
            if(askedRestartingQuestion){
                ctree.addPath(input,output);
                equalInput = ctree.tryPruning(input);
                state = ctree.getState(input);
            }
            else{
                ctree.addPath(currentMachineState, input, output);
                equalInput = ctree.tryPruning(currentMachineState, input);
                state = ctree.getState(currentMachineState, input);
            }
            State sut = stateUnderTesting;
            if(frontierStates.contains(sut) && !remainedObservations.containsKey(sut))
                closeState(sut);
        }
        currentMachineState = state;
        if(equalInput == null){
            System.out.println("reached state : " + state);
        }
    }

    public void extendObservation(State state){
        CSet<ICommand> palette = ctree.getPalette(state);
        CSet<CList<ICommand>> observations = new CSet<CList<ICommand>>();
        buildObservation(observations, palette);
        remainedObservations.put(state, observations);
    }


    public void learnCounterExample(Pair<CList<ICommand>, CList<Observation>> ce) {
    } // Do nothing

    public AppModel getModel() {
        return null;
    }

    public void updateView(){
        ctree.updateView();
    }
}

