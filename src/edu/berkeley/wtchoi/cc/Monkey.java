package edu.berkeley.wtchoi.cc;

import java.io.*;

import edu.berkeley.wtchoi.cc.driver.Driver;
import edu.berkeley.wtchoi.cc.driver.DriverOption;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.learnerImp.TreeLearner;
import edu.berkeley.wtchoi.cc.learnerImp.Observation;
import edu.berkeley.wtchoi.cc.learnerImp.TransitionInfo;
import edu.berkeley.wtchoi.cc.learning.*;
import edu.berkeley.wtchoi.cc.util.Logger;
import edu.berkeley.wtchoi.cc.util.LoggerImp;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;


public class Monkey {
    public static void main(String args[]) {

        Logger.init(new LoggerImp() {
            @Override
            public void log(String s) {
                System.out.println(s);
            }
        });

        DriverOption option = new DriverOption();
        option.fillFromEnvironmentVariables();
        Driver<TransitionInfo> controller = Driver.connectToDevice(option, ".*", 13337, TransitionInfo.class);
        if(controller == null) throw new RuntimeException("Cannot initiate driver");


        TeacherImp teacher = new TeacherImp(controller);

        if(!teacher.init()) throw new RuntimeException("Cannot initialize teacher");

        CSet<ICommand> initialPalette = controller.getCurrentView().getRepresentativePoints(TouchFactory.getInstance());
        TreeLearner foo = new TreeLearner(initialPalette);
        Learner<ICommand, Observation, AppModel> learner = foo;// = new PaletteLearnerImp(teacher);

        Learning<ICommand, Observation, AppModel> learning = new Learning<ICommand, Observation, AppModel>(learner, teacher);
        learning.run();

        Model m = learner.getModel();
        m.printModel(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
}



//View State Information. We are going to use it as output character
/*
class ViewState implements Comparable<ViewState>{

    private CSet<ICommand> palette;

    public int compareTo(ViewState target){
        return palette.compareTo(target.palette);
    }

    public ViewState(CSet<ICommand> palette) {
        this.palette = palette;
    }

    public String toString(){
        return palette.toString();
    }
}
*/

