/*
 * Daneel - Dalvik to Java bytecode compiler
 * Copyright (C) 2011  IcedRobot team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file is subject to the "Classpath" exception:
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under terms
 * of your choice, provided that you also meet, for each linked independent
 * module, the terms and conditions of the license of that module.  An
 * independent module is a module which is not derived from or based on
 * this library.  If you modify this library, you may extend this exception
 * to your version of the library, but you are not obligated to do so.  If
 * you do not wish to do so, delete this exception statement from your
 * version.
 */

package org.icedrobot.daneel.tools;

import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Simple tool to inspect the chain of class loaders acting in the system.
 */
public class ClassLoaderInspector {

    /**
     * Prints information about every class loader in the delegation chain
     * starting with the system class loader.
     * 
     * @param out The output stream to print to.
     */
    public void execute(PrintStream out) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        for (int clCount = 1; cl != null; cl = cl.getParent()) {
            out.printf("CL #%d: %s\n", clCount++, cl);
            if (cl instanceof URLClassLoader) {
                URLClassLoader ucl = (URLClassLoader) cl;
                out.printf("    instanceof URLClassLoader\n");
                out.printf("    search path:\n");
                int urlCount = 1;
                for (URL url : ucl.getURLs())
                    out.printf("        URL #%d: %s\n", urlCount++, url);
                out.printf("    end of search path.\n");
            }
        }
        out.printf("end of chain.\n");
    }

    public static void main(String[] args) {
        ClassLoaderInspector inspector = new ClassLoaderInspector();
        inspector.execute(System.out);
    }
}
