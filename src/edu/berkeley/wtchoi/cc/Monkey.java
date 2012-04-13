package edu.berkeley.wtchoi.cc;

import java.io.*;

import edu.berkeley.wtchoi.cc.driver.DriverImp;
import edu.berkeley.wtchoi.cc.driver.DriverImpOption;
import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.IDriver;
import edu.berkeley.wtchoi.cc.learning.*;
import edu.berkeley.wtchoi.cc.util.Logger;
import edu.berkeley.wtchoi.cc.util.LoggerImp;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.learning.Observation;
//import com.android.chimpchat.core.IChimpView;


public class Monkey {
    public static void main(String args[]) {

        Logger.init(new LoggerImp() {
            @Override
            public void log(String s) {
                System.out.println(s);
            }
        });

        DriverImpOption option = new DriverImpOption();
        option.fillFromEnvironmentVariables();
        
        IDriver controller = new DriverImp(option);
        MonkeyTeacher teacher = new MonkeyTeacher(controller);

        if(!teacher.init()) throw new RuntimeException("Cannot initialize teacher");

        LearnerFoo foo = new LearnerFoo(teacher);
        Learner<ICommand, ViewState, AppModel> learner = foo;// = new PaletteLearnerImp(teacher);

        Learning<ICommand, ViewState, AppModel> learning = new Learning<ICommand, ViewState, AppModel>(learner, teacher);
        learning.run();

        Model m = learner.getModel();
        m.printModel(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
}

//View State Information. We are going to use it as output character
class ViewState implements Observation<ViewState>{
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

class AppModel implements Model<ICommand, ViewState> { //TODO

    public void printModel(Writer w) {
    }
}

