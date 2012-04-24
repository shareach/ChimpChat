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

package org.icedrobot.daneel.rewriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.icedrobot.daneel.loader.DaneelClassLoader;

/**
 * Run: java -cp classes:lib/smali-1.2.6.jar:lib/asm-debug-all-4.0.jar
 * org.icedrobot.daneel.rewriter.Main HelloWorld.dex HelloWorld
 */
public class Main {
    public static void main(String[] args) throws IOException,
            ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException {
        
        // enable verification by default
        System.setProperty("daneel.verify", "true");
        
        ClassLoader classLoader = new DaneelClassLoader(Main.class.getClassLoader(), new File(args[0]));
        Class<?> clazz = classLoader.loadClass(args[1]);
        Method method = clazz.getMethod("main", String[].class);
        try {
            method.invoke(null,
                    (Object) Arrays.asList(args).subList(2, args.length)
                            .toArray(new String[0]));
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        }
    }
}
