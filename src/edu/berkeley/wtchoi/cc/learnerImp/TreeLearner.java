package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.AppModel;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.learning.Learner;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

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


    public TreeLearner(CSet<ICommand> initialPalette) {
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());

        ctree = new CTree(initialPalette,defaultPalette);
        ctree.startViewer();
    }

    public boolean learnedHypothesis() {
        return false;  // Do nothing
    }

    public CList<ICommand> getQuestion() {
        //CVector<ICommand> temp = new CVector<ICommand>();
        //temp.add(PushCommand.getMenu());
        //temp.add(new TouchCommand(476,799));
        //return temp;

        if(ctree.getLeafSet().isEmpty()) return null;
        CVector<ICommand> question = new CVector<ICommand>();
        ctree.buildInputPath(ctree.getLeafSet().iterator().next(), question);
        return question;
    }

    public void learn(CList<ICommand> input, CList<Observation> output) {
        ctree.addPath(input,output);
        ctree.updateView();
        ctree.tryPruning(input);
        ctree.updateView();
    }

    public void learnCounterExample(Pair<CList<ICommand>, CList<Observation>> ce) {
    } // Do nothing

    public AppModel getModel() {
        return null;
    }
}

