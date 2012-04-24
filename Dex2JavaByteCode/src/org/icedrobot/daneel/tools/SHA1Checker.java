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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import org.icedrobot.daneel.DaneelException;
import org.icedrobot.daneel.dex.DexDumperHelper;
import org.icedrobot.daneel.dex.DexSharedSecrets;

/**
 * Checks the DEX SHA-1 signature.
 */
public class SHA1Checker {

    private static final int SKIP_TO = 8 + 4 + 20;

    private byte[] calculateSHASignature(ByteBuffer dexFileBuffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(dexFileBuffer);
            return md.digest();

        } catch (Exception ex) {
            throw new DaneelException(ex);
        }
    }

    private String convertToHex(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            hexString.append(Integer.toHexString(0xFF & data[i]));
        }
        return hexString.toString();
    }

    private boolean compareSignature(byte[] expected, byte[] found) {

        boolean result = false;
        String expectedString = convertToHex(expected);
        String foundString = convertToHex(found);

        if (!expectedString.equals(foundString)) {
            System.out.println("Checksum doesn't match!");
            System.out.println("found:\t\t" + expectedString);
            System.out.println("expected:\t" + foundString);
        } else {
            System.out.println("Checksum verified: " + foundString);
            result = true;
        }
        return result;
    }

    void execute(File dexFile) throws IOException {

        System.out.println("processing: " + dexFile);

        RandomAccessFile dexFileAccess = new RandomAccessFile(dexFile, "r");
        FileChannel dexFileChannel = dexFileAccess.getChannel();

        ByteBuffer dexFileBufferExpected =
                dexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                                   dexFile.length());

        DexDumperHelper helper = DexSharedSecrets.getDexDumper();
        byte[] hash = helper.getSignature(dexFileBufferExpected);

        ByteBuffer dexFileBufferFound = dexFileBufferExpected.duplicate();
        dexFileBufferFound.position(SKIP_TO);

        byte[] chash = calculateSHASignature(dexFileBufferFound);
        compareSignature(hash, chash);
    }

    public static void main(String [] args) {
        try {
            SHA1Checker checker = new SHA1Checker();

            String dexFileName = args[0];
            File dexFile = new File(dexFileName);

            checker.execute(dexFile);

        } catch (Exception ex) {
            throw new DaneelException(ex);
        }
    }
}
