package edu.berkeley.wtchoi.collection;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:52 PM
 * To change this template use File | Settings | File Templates.
 */
//Comparable Vector
public class CVector<T extends Comparable<T>> extends Vector<T> implements CList<T>, Serializable {
    private static final long serialVersionUID = -5186309675577891457L;

    public int compareTo(CList<T> target) {
        return CollectionUtil.compare(this, target);
    }

    public CVector(int size) {
        super(size);
    }

    public CVector() {
        super();
    }

    public CVector(Collection<T> collection) {
        super(collection);
    }

    public CVector(T[] array){
        super();
        for(T elt: array){
            this.add(elt);
        }
    }

    
    public void writeTo(Writer writer) throws IOException{
        CollectionUtil.writeTo(this,"[","; ","]",writer);
    }
    
    public String toString(){
        return CollectionUtil.stringOf(this,"[","; ","]") ;
    }
}
