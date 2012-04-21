package edu.berkeley.wtchoi.cc.learnerImp;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.Driver;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.CState;
import edu.berkeley.wtchoi.cc.util.IdentifierPool;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/21/12
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExpandToCommand extends ICommand{
    private static int typeint = IdentifierPool.getFreshInteger();


    public Integer typeint(){
        return typeint;
    }

    private Integer degree;

    public int compareSameType(ICommand t){
        ExpandToCommand target = (ExpandToCommand) t;
        return degree.compareTo(target.degree);
    }

    public void sendCommand(Driver drive){
        throw new RuntimeException("ExpandToCommand should escape it's boundary!");
    }

    public ExpandToCommand(int i){
        degree = i;
    }

    public CSet<CList<ICommand>> expand(CList<ICommand> prefix, CSet<ICommand> palette, CSet<ICommand> defaultPalette){
        CSet<CList<ICommand>> set = new CSet<CList<ICommand>>();
        for(ICommand cmd: palette){
            CList<ICommand> input = new CVector<ICommand>();
            input.addAll(prefix);
            input.add(cmd);
            set.add(input);
            if(degree != 1){
                CList<ICommand> input2 = new CVector<ICommand>();
                input2.add(new ExpandToCommand(degree - 1));
                input2.addAll(input);
                set.add(input2);
            }
        }
        for(ICommand cmd: defaultPalette){
            CList<ICommand> input = new CVector<ICommand>();
            input.addAll(prefix);
            input.add(cmd);
            set.add(input);
            if(degree != 1){
                CList<ICommand> input2 = new CVector<ICommand>();
                input2.add(new ExpandToCommand(degree - 1));
                input2.addAll(input);
                set.add(input2);
            }
        }
        return set;
    }

    public static CList<ICommand> getVector(int degree){
        CList<ICommand> list = new CVector<ICommand>();
        list.add(new ExpandToCommand(degree));
        return list;
    }
}
