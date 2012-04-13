package edu.berkeley.wtchoi.cc.learning;

import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Model<I extends Comparable<I>, O extends Observation<O>> {
    public void printModel(Writer w);
}
