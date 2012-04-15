package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.IDriver;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;

import edu.berkeley.wtchoi.cc.learnerImp.Observation;
import edu.berkeley.wtchoi.cc.learnerImp.TransitionInfo;
import edu.berkeley.wtchoi.cc.learning.Teacher;

import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;


/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */


public class TeacherImp implements Teacher<ICommand, Observation, AppModel> {

    private IDriver<TransitionInfo> controller;
    private PointFactory pointFactory = TouchFactory.getInstance();

    public TeacherImp(IDriver<TransitionInfo> imp) {
        controller = imp;
    }

    public Pair<CList<ICommand>, CList<Observation>> getCounterExample(AppModel model) {
        // TODO: implement getCounterExample query
        return null;
    }


    public CList<Observation> checkMembership(CList<ICommand> input, boolean requireRestart) {
        if (input == null) return null;
        if (input.size() == 0) return null;

        CVector<Observation> output = new CVector<Observation>(input.size());
        CSet<ICommand> palette = null;

        if(requireRestart) controller.restartApp();
        for (ICommand t : input) {
            if (!controller.go(t)){
                output.add(Observation.getStopObservation());
                return output;
            }

            //Obtaining palette
            ViewInfo mv = controller.getCurrentView();
            System.out.println(mv);
            palette = mv.getRepresentativePoints(pointFactory);

            //Obtaining transition information
            TransitionInfo ti = controller.getCurrentTransitionInfo();

            Observation state = new Observation(palette,ti);
            output.add(state);
        }
        return output;
    }

    public boolean init() {
        //1. Initiate connection with application
        if (!controller.connectToDevice()) return false;
        if (!controller.initiateApp()) return false;

        //2. Warming palette table for initial state
        ViewInfo view = controller.getCurrentView();
        if (view == null) return false;

        return true;
    }
}