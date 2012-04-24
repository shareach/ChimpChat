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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.icedrobot.daneel.DaneelException;

/**
 * Java version of dexdump.
 * Reads a dex file, decompiles using Daneel and dump the requested
 * information on the output.
 */
public class DexDump {

    private static enum DexOption {

        CHECKSUM("c", "verifies the checksum and prints the result"),
        HELP("h", "print a brief help");

        final private String option;
        final private String description;
        DexOption(String option, String description) {
            this.option = option;
            this.description = description;
        }

        /**
         * @return the option
         */
        public String getOption() {
            return option;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Configure the options that DexDump understands.
     * Please, refer to the Android version for the complete list.
     */
    static OptionParser parser;
    static {
        parser = new OptionParser() {
            {
                accepts(DexOption.HELP.getOption(),
                        DexOption.HELP.getDescription());
                accepts(DexOption.CHECKSUM.getOption(),
                        DexOption.CHECKSUM.getDescription());
            }
        };
    }

    private String[] args;
    private DexDump(String[] args) {
        this.args = args;
    }

    private void printHelp() {
        try {
            System.out.println("DexDump: [-c] ClassFile.dex");
            parser.printHelpOn(System.out);
        } catch (IOException ex) {
            throw new DaneelException(ex);
        }
    }

    private void checkOptions(File dexFile, OptionSet options) {
        if (options.has(DexOption.HELP.getOption())) {
            printHelp();

        } else if (options.has(DexOption.CHECKSUM.getOption())) {
            SHA1Checker checker = new SHA1Checker();
            try {
                checker.execute(dexFile);
                
            } catch (IOException ex) {
                Logger.getLogger(DexDump.class.getName()).log(Level.SEVERE,
                        "Unexpected Exception", ex);
            }
        }
    }

    private void execute() {
        
        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            printHelp();
            return;
        }

        List<String> files =  options.nonOptionArguments();
        if (files.size() != 1) {
            if (files.isEmpty()) {
                System.out.println("DexDump: no input file given");
            } else {
                System.out.println("DexDump: too many arguments");
            }
            printHelp();
            return;
        }

        File dexFile = new File(files.get(0));
        if (!dexFile.exists()) {
            System.out.println("DexDump: invalid input file: " + dexFile);
            printHelp();
            return;
        }

        checkOptions(dexFile, options);
    }

    public static void main(String[] args) {
        DexDump dexDump = new DexDump(args);
        dexDump.execute();

    }
}
