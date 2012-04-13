package edu.berkeley.wtchoi.cc.learning;

import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TeacherP<I extends Comparable<I>, O extends Observation<O>, M extends Model<I, O>> extends Teacher<I, O, M> {
    public CSet<I> getPalette(CList<I> input);
}
