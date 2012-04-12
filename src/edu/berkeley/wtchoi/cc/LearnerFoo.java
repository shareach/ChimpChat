package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.learning.Learner;
import edu.berkeley.wtchoi.cc.learning.TeacherP;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LearnerFoo implements Learner<ICommand, ViewState, AppModel> {
    private TeacherP<ICommand, ViewState, AppModel> teacher;
    private Map<CList<ICommand>, CList<ViewState>> iomap;
    private TreeSet<CList<ICommand>> candidateSet;

    public LearnerFoo(TeacherP<ICommand, ViewState, AppModel> teacher) {
        this.teacher = teacher;
        iomap = new TreeMap<CList<ICommand>, CList<ViewState>>();
        candidateSet = new TreeSet<CList<ICommand>>();
        CSet<ICommand> initialPalette = teacher.getPalette(new CVector());
        candidateSet.addAll(makeInputs(new CVector(), initialPalette));
    }

    private Collection<CList<ICommand>> makeInputs(CList<ICommand> prefix, CSet<ICommand> alphabet) {
        if (prefix == null) {
            prefix = new CVector<ICommand>();
        }

        Collection<CList<ICommand>> set = new TreeSet<CList<ICommand>>();

        for (ICommand t : alphabet) {
            CList<ICommand> new_input = new CVector<ICommand>(prefix);
            new_input.add(t);
            set.add(new_input);
        }

        return set;
    }

    public boolean learnedHypothesis() {
        return false;  // Do nothing
    }

    public CList<ICommand> getQuestion() {
        //CVector<ICommand> temp = new CVector<ICommand>();
        //temp.add(PushCommand.getMenu());
        //temp.add(new TouchCommand(476,799));
        //return temp;

        if (candidateSet.isEmpty()) return null;
        return candidateSet.pollFirst();
    }

    public void learn(CList<ICommand> input, CList<ViewState> output) {
        if (!iomap.containsKey(input)) {
            CSet<ICommand> palette = teacher.getPalette(input);
            candidateSet.addAll(makeInputs(input, palette));
        }
        iomap.put(input, output);
    }

    public void learnCounterExample(Pair<CList<ICommand>, CList<ViewState>> ce) {
    } // Do nothing

    public CList<ViewState> calculateTransition(CList<ICommand> input) {
        return iomap.get(input);
    }

    public AppModel getModel() {
        return null;
    }
}