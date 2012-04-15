package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.AppModel;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.learning.Learner;
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
public class TreeLearner implements Learner<ICommand, Observation, AppModel> {
    private CTree ctree;
    private CSet<ICommand> defaultPalette;

    private CSet<CList<ICommand>> uniqueStates; //S
    private CSet<CList<ICommand>> frontierStates;  //SI

    private CSet<CList<ICommand>> suffixes; //E

    private CSet<CList<ICommand>> uniquesToBeTested; //for implementation
    private CSet<CList<ICommand>> frontiersToBeTested; //for implementation
    private TreeMap<CList<ICommand>,CSet<CList<ICommand>>> remainedObservations;
    //INVARIANT : state in stateToBeTested <==> state has remainedObservations


    public TreeLearner(CSet<ICommand> initialPalette) {
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());
        defaultPalette.add(PushCommand.getBack());

        ctree = new CTree(initialPalette,defaultPalette);
        ctree.startViewer();

        uniqueStates = new CSet<CList<ICommand>>();
        frontierStates = new CSet<CList<ICommand>>();
        suffixes = new CSet<CList<ICommand>>();
        remainedObservations = new TreeMap<CList<ICommand>, CSet<CList<ICommand>>>();
        uniquesToBeTested = new CSet<CList<ICommand>>();
        frontiersToBeTested = new CSet<CList<ICommand>>();

        CList<ICommand> initState = new CVector<ICommand>();
        uniqueStates.add(initState);

        CSet<CList<ICommand>> observations = new CSet<CList<ICommand>>();
        buildObservation(observations, initialPalette);
        remainedObservations.put(initState, observations);

        uniquesToBeTested.add(initState);
        frontiersToBeTested.addAll(observations);
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

    public CList<ICommand> getQuestion() {
        //Manaul approach
        //---------------
        //CVector<ICommand> temp = new CVector<ICommand>();
        //temp.add(PushCommand.getMenu());
        //temp.add(new TouchCommand(476,799));
        //return temp;


        //Pure CTree approach
        //-------------------
        //if(ctree.getLeafSet().isEmpty()) return null;
        //CVector<ICommand> question = new CVector<ICommand>();
        //ctree.buildInputPath(ctree.getLeafSet().iterator().next(), question);
        //return question;

        //L* with CTree approach
        //----------------------
        while(true){
            if(uniquesToBeTested.isEmpty()) break;
            CList<ICommand> state = uniquesToBeTested.first();
            CSet<CList<ICommand>> observations = remainedObservations.get(state);
            CList<ICommand> suffix = observations.pollFirst();
            if(!ctree.chackPossible(state, suffix)){
                observations.remove(suffix);
                if(observations.isEmpty())
                    uniquesToBeTested.remove(state);
                continue;
            }
            CVector<ICommand> tmp = new CVector<ICommand>();
            tmp.addAll(state);
            tmp.addAll(suffix);
            return tmp;
        }

        while(true){
            if(frontiersToBeTested.isEmpty()) break;
            CList<ICommand> state = frontiersToBeTested.first();
            CSet<CList<ICommand>> observations = remainedObservations.get(state);
            CList<ICommand> suffix = observations.pollFirst();
            if(!ctree.chackPossible(state, suffix)){
                observations.remove(suffix);
                if(observations.isEmpty()){
                    frontiersToBeTested.remove(state);
                    closeState(state);
                }
                continue;
            }
            CVector<ICommand> tmp = new CVector<ICommand>();
            tmp.addAll(state);
            tmp.addAll(suffix);
            return tmp;
        }
        return null;
    }

    public void closeState(CList<ICommand> state){
        for(CList<ICommand> ustate: uniqueStates){
            if(checkObservationalEquivalence(state, ustate)) return;
        }
        frontierStates.remove(state);
        CSet<ICommand> palette = ctree.getPalette(state);
        for(ICommand cmd: palette){
            CList<ICommand> temp = new CVector<ICommand>();
            temp.addAll(state);
            temp.add(cmd);
            frontierStates.add(temp);
            remainedObservations.put(temp,new CSet<CList<ICommand>>());
            remainedObservations.get(temp).addAll()
        }
        for(ICommand cmd: defaultPalette){
            CList<ICommand> temp = new CVector<ICommand>();
            temp.addAll(state);
            temp.add(cmd);
            frontierStates.add(temp);
        }
        for(CList<ICommand> cmd: suffixes){
            CList<ICommand> temp = new CVector<ICommand>();
            temp.addAll(state);
            temp.addAll(cmd);
            frontierStates.add(temp);
        }

    }

    private boolean checkObservationalEquivalence(CList<ICommand> s1, CList<ICommand> s2){
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
            boolean f1 = ctree.checkPossible(s1,suffix);
            boolean f2 = ctree.checkPossible(s2,suffix);
            if(s1 != s2) return false;
            if(!f1 && !f2) continue;
            CList<Observation> o1 = ctree.getTransition(s1,suffix);
            CList<Observation> o2 = ctree.getTransition(s2,suffix);
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
        ctree.addPath(input,output);
        ctree.updateView();
        CList<ICommand> equalInput = ctree.tryPruning(input);

        if(equalInput != null){
            frontierStates.remove(input);
            frontiersToBeTested.remove(input);
        }

        CList<ICommand> sut = stateUnderTesting;
        if(frontierStates.contains(sut) && remainedObservations.get(sut).isEmpty())
            closeState(sut) ;

        ctree.updateView();
    }

    public void learnCounterExample(Pair<CList<ICommand>, CList<Observation>> ce) {
    } // Do nothing

    public AppModel getModel() {
        return null;
    }
}

