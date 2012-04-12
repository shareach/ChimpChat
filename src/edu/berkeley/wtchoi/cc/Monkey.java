package edu.berkeley.wtchoi.cc;

import java.io.*;

import edu.berkeley.wtchoi.cc.driver.DriverImp;
import edu.berkeley.wtchoi.cc.driver.DriverImpOption;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.IDriver;
import edu.berkeley.wtchoi.cc.learning.*;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
//import com.android.chimpchat.core.IChimpView;


public class Monkey {
    public static void main(String args[]) {

        DriverImpOption option = new DriverImpOption();
        option.fillFromEnvironmentVariables();
        
        IDriver controller = new DriverImp(option);
        MonkeyTeacher teacher = new MonkeyTeacher(controller);

        if(!teacher.init()) throw new RuntimeException("Cannot initialize teacher");

        Learner<ICommand, ViewState, AppModel> learner = new LearnerFoo(teacher);// = new PaletteLearnerImp(teacher);

        Learning<ICommand, ViewState, AppModel> learning = new Learning<ICommand, ViewState, AppModel>(learner, teacher);
        learning.run();

        Model m = learner.getModel();
        m.printModel(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
}

//View State Information. We are going to use it as output character
class ViewState implements Comparable<ViewState> {
    private CSet<ICommand> palette;

    public int compareTo(ViewState target) {
        return palette.compareTo(target.palette);
    }

    public ViewState(CSet<ICommand> palette) {
        this.palette = palette;
    }

    public String toString(){
        return palette.toString();
    }
}

class AppModel implements Model<ICommand, ViewState> { //TODO

    public void printModel(Writer w) {
    }
}

