package edu.berkeley.wtchoi.cc.learnerImp.ctree;

import com.sun.xml.internal.ws.handler.ServerLogicalHandlerTube;
import edu.berkeley.wtchoi.cc.driver.drone.SLog;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.CollectionUtil;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:35 PM
 * To change this template use File | Settings | File Templates.
 */

public class TransitionInfo implements Serializable
{
    private static final long serialVersionUID = -5186309675577891457L;

    private CVector<SLog> trace;

    public TransitionInfo(CVector<SLog> t){
        trace = t;
    }

    public boolean didNothing(){
        return trace.isEmpty();
    }

    public boolean equalsTo(TransitionInfo target){
        if(trace == null) return target.trace == null;
        else if(target.trace == null) return false;

        return 0 == CollectionUtil.compare(trace, target.trace, comparator);
    }

    private static Comparator<SLog> comparator = new Comparator<SLog>(){
        public int compare(SLog s1, SLog s2){
            return s1.pseudoCompareTo(s2);
        }
    };
}
