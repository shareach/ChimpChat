package edu.berkeley.wtchoi.collection;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CPair<A extends Comparable<A>, B extends Comparable<B>> extends Pair<A, B> implements Comparable<CPair<A, B>> {
    public int compareTo(CPair<A, B> target) {
        int temp = this.fst.compareTo(target.fst);
        if (temp != 0) return temp;
        return this.snd.compareTo(target.snd);
    }

    public CPair(A fst, B snd) {
        super(fst, snd);
    }
}