package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.Exploring.ExploreRequest;
import edu.berkeley.wtchoi.cc.Exploring.ExploreResult;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.Exploring.Guide;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.CState;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.CTree;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;

import java.util.TreeMap;

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

    private CSet<CState> uniqueStates; //S
    private CSet<CState> frontierStates;  //SI

    private CSet<CState> uniquesToBeTested; //for implementation
    private CSet<CState> frontiersToBeTested; //for implementation
    //INVARIANT : state in stateToBeTested <==> state has remainedObservations

    private TreeMap<CState,CSet<CList<ICommand>>> remainedObservations;
    private TreeMap<CState,CSet<CList<ICommand>>> fetchedObservations;

    private CSet<CState> pendingStates;
    private TreeMap<CState, Integer> observationDegree;
    private final int resumeThreshold = 4;

    private TreeMap<CSet<ICommand>, CSet<CList<ICommand>>> suffixes;

    CState stateOnResume;
    CState stateOnCompare;

    public TreeLearner(CSet<ICommand> initialPalette) {
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());
        defaultPalette.add(PushCommand.getBack());

        ctree = new CTree(initialPalette,defaultPalette);
        ctree.startViewer();

        uniqueStates = new CSet<CState>();
        frontierStates = new CSet<CState>();

        uniquesToBeTested = new CSet<CState>();
        frontiersToBeTested = new CSet<CState>();
        remainedObservations = new TreeMap<CState, CSet<CList<ICommand>>>();
        fetchedObservations = new TreeMap<CState, CSet<CList<ICommand>>>();

        pendingStates = new CSet<CState>();
        observationDegree = new TreeMap<CState, Integer>();

        suffixes = new TreeMap<CSet<ICommand>, CSet<CList<ICommand>>>();

        CState initState = ctree.getInitState();
        addToFrontier(initState);
    }

    private void addToFrontier(CState s){
        frontierStates.add(s);
        frontiersToBeTested.add(s);

        remainedObservations.put(s, new CSet<CList<ICommand>>());
        fetchedObservations.put(s, new CSet<CList<ICommand>>());

        CSet<ICommand> palette = ctree.getPalette(s);
        remainedObservations.get(s).add(ExpandToCommand.getVector(1));
        remainedObservations.get(s).addAll(suffixes.get(palette));
        observationDegree.put(s, 1);

        s.setColor("blue");
    }

    private void promoteToUnique(CState state){
        frontierStates.remove(state);
        observationDegree.remove(state);
        uniqueStates.add(state);

        CSet<ICommand> palette = ctree.getPalette(state);
        for(ICommand cmd:palette){
            CState f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            addToFrontier(f);
        }
        for(ICommand cmd:defaultPalette){
            CState f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            addToFrontier(f);
        }

        state.setColor("black");
    }

    public ExploreRequest<ICommand> getRequest(CList<ICommand> machineState){
        ExploreRequest<ICommand> request = getRequestImp(machineState);
        updateView();
        return request;
    }

    static int count = 0;
    public ExploreRequest<ICommand> getRequestImp(CList<ICommand> machineStatePrefix) {
        System.out.println("search round : "+ ++count);
        if(count == 3){
            count = count;
        }

        CState machineState = ctree.getState(machineStatePrefix);
        while(!uniquesToBeTested.isEmpty()){
            CState state = pickState(uniquesToBeTested, machineState);
            CList<ICommand> suffix = pollObservation(state);

            if(remainedObservations.get(state).isEmpty()){
                uniquesToBeTested.remove(state);
            }

            if(suffix == null) continue;

            return buildRequest(machineState, state, suffix);
        }


        if(tryResume()) return getRequestImp(machineStatePrefix);

        while(!frontiersToBeTested.isEmpty()){
            CState state = pickState(frontiersToBeTested, machineState);
            CList<ICommand> suffix = pollObservation(state);

            if(remainedObservations.get(state).isEmpty())
                frontiersToBeTested.remove(state);

            if(suffix == null){
                closeState(state);
                continue;
            }

            return buildRequest(machineState, state, suffix);
        }
        return null;
    }

    private boolean tryResume(){
        if(stateOnResume != null) return false;

        CState resumable = null;
        int degree = 1;

        for(CState state : pendingStates){
            degree = observationDegree.get(state);
            if(degree * resumeThreshold < ctree.depth() - state.getDepth()){
                resumable = state;
                break;
            }
        }
        if(resumable == null) return false;

        observationDegree.put(resumable, ++degree);

        frontiersToBeTested.add(resumable);
        remainedObservations.get(resumable).add(ExpandToCommand.getVector(degree));

        uniquesToBeTested.add(resumable.getMergeTo());
        remainedObservations.get(resumable.getMergeTo()).add(ExpandToCommand.getVector(degree));

        stateOnResume = resumable;
        stateOnCompare = resumable.getMergeTo();
        pendingStates.remove(resumable);
        resumable.split();

        return true;
    }

    private CList<ICommand> pollObservation(CState state){
        CList<ICommand> suffix;
        CSet<CList<ICommand>> observations = remainedObservations.get(state);
        while((suffix = observations.pollFirst()) != null){
            if(tryExpand(state, suffix)) continue;
            if(!ctree.checkPossible(state, suffix) || ctree.visited(state, suffix)) continue;
            break;
        }
        return suffix;
    }

    private boolean tryExpand(CState state, CList<ICommand> suffix){
        if(suffix.size() == 0) return false;

        ICommand lastCmd = suffix.get(0);
        if(!(lastCmd instanceof ExpandToCommand)) return false;

        ExpandToCommand expander = (ExpandToCommand) lastCmd;
        suffix.remove(0);

        CSet<ICommand> palette = ctree.getPalette(ctree.getState(state, suffix));
        remainedObservations.get(state).addAll(expander.expand(suffix, palette, defaultPalette));
        return true;
    }

    private CState pickState(CSet<CState> set, CState machineState){
        if(set.contains(machineState)) return machineState;
        for(CState s: set){
            if(machineState.isPrefixOf(s)) return s;
        }
        return set.first();
    }

    private ExploreRequest<ICommand> buildRequest(CState machineState, CState sut, CList<ICommand> suffix){
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


    private void closeState(CState state){
        if(state.isStopNode()){
            frontierStates.remove(state);
            return;
        }

        if(stateOnResume.compareTo(state) == 0){
            frontierStates.remove(state);
            int degree = observationDegree.get(state);
            if(checkObservationalEquivalence(stateOnCompare, state, degree)){
                state.mergeTo(stateOnCompare);
                pendingStates.add(state);
            }
            else{
                //TODO: get refutation
                //TODO: add to suffixes
                //TODO: request update for other uniques and frontier
                promoteToUnique(state);
            }
            stateOnResume = null;
            stateOnCompare = null;
            return;
        }
        else{

        }

        for(CState ustate: uniqueStates){
            if(checkObservationalEquivalence(ustate, state, 1)){
                state.mergeTo(ustate);
                pendingStates.add(state);
                return;
            }
        }
        promoteToUnique(state);
    }

    //TODO: comparison using suffix
    //TODO: refutation construction
    private boolean checkObservationalEquivalence(CState uniqueState, CState frontierState, int degree){
        if(!checkFullyObserved(uniqueState) || !checkFullyObserved(frontierState))
            throw new RuntimeException("Something is Wrong!");

        CSet<ICommand> p1 = ctree.getPalette(uniqueState);
        CSet<ICommand> p2 = ctree.getPalette(frontierState);
        if(p1.compareTo(p2) != 0) return false;

        if(!checkObservationalEquivalenceImp(uniqueState, p1, frontierState, p2, degree)) return false;
        return true;
    }

    private boolean checkObservationalEquivalenceImp(CState s1, CSet<ICommand> p1, CState s2, CSet<ICommand> p2, int degree){

        if(s1.isStopNode() && s2.isStopNode()) return true;
        if(s1.compareTo(s2) == 0) return true;

        System.out.println("(" + s1.toString() + "," + s2.toString() + ")"+ degree);
        if(degree == 0) return true;
        if(degree == 2) degree = degree;

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

        if(degree > 1){
            for(ICommand cmd : p1){
                CState ch1 = ctree.getState(s1, cmd);
                CState ch2 = ctree.getState(s2, cmd);
                CSet<ICommand> pch1 = ctree.getPalette(ch1);
                CSet<ICommand> pch2 = ctree.getPalette(ch2);
                if(! checkObservationalEquivalenceImp(ch1, pch1, ch2, pch2, degree - 1)) return false;
            }
            for(ICommand cmd : defaultPalette){
                CState ch1 = ctree.getState(s1, cmd);
                CState ch2 = ctree.getState(s2, cmd);
                CSet<ICommand> pch1 = ctree.getPalette(ch1);
                CSet<ICommand> pch2 = ctree.getPalette(ch2);
                if(! checkObservationalEquivalenceImp(ch1,pch1, ch2,pch2, degree - 1)) return false;
            }
        }
        return true;
    }

    public void learn(ExploreResult<ICommand,Observation> result) {
        learnImp(result);
        //ctree.updateView();
    }

    private void learnImp(ExploreResult<ICommand,Observation> result) {
        CList<ICommand> equalInput;
        CState state;

        if(count == 3){
            count = count;
        }


        CState startingState = ctree.getState(result.startingState);
        ctree.addPath(startingState,result.input,result.output);
        equalInput = ctree.tryPruning(startingState, result.input);
        state = ctree.getState(startingState, result.input);

        if(result.query != null){
            CState sut = ctree.getState(result.query.sut);
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


    private boolean checkFullyObserved(CState s){
        return remainedObservations.get(s).isEmpty() && fetchedObservations.get(s).isEmpty();
    }

    public void updateView(){
        ctree.updateView();
    }
}

