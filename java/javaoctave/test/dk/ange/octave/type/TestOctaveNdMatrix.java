/*
 * Copyright 2007, 2008 Ange Optimization ApS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.ange.octave.type;

import junit.framework.TestCase;

/**
 * @author Esben Mose Hansen
 */
public class TestOctaveNdMatrix extends TestCase {

    /**
     */
    public void testGetAndSet() {
        final OctaveNdMatrix matrix = new OctaveNdMatrix(3, 6, 5, 4);
        matrix.set(2.0, 2, 5, 2, 3);
        for (int row = 1; row <= 3; row++) {
            for (int column = 1; column <= 6; column++) {
                for (int depth = 1; depth <= 5; depth++) {
                    for (int coffee = 1; coffee <= 4; coffee++) {
                        if (row == 2 && column == 5 && depth == 2 && coffee == 3) {
                            assertEquals(matrix.get(row, column, depth, coffee), 2.0);
                        } else {
                            assertEquals(matrix.get(row, column, depth, coffee), 0.0);
                        }
                    }
                }
            }
        }
        try {
            matrix.get(2, 3, 1, 0);
            fail("Attempt to get with a position that includes a 0 should fail");
        } catch (final IndexOutOfBoundsException e) {
            // ok
        }
        try {
            matrix.get(2, 3, 10, 3);
            fail("Attempt to get with a position that exceeds range should fail");
        } catch (final IndexOutOfBoundsException e) {
            // ok
        }
        try {
            matrix.get(2, 3, 2, 3, 4);
            fail("Attempt to get with a position that exceeds dimensions should fail");
        } catch (final IndexOutOfBoundsException e) {
            // ok
        }

    }

    /**
     */
    public void testSizeConstructor() {
        final OctaveNdMatrix matrix = new OctaveNdMatrix(3, 6, 5, 4);
        assertEquals(matrix.getSize().length, 4);
        assertEquals(matrix.getSize()[0], 3);
        assertEquals(matrix.getSize()[1], 6);
        assertEquals(matrix.getSize()[2], 5);
        assertEquals(matrix.getSize()[3], 4);

        final OctaveNdMatrix matrixEmpty = new OctaveNdMatrix(0, 0);
        assertEquals(matrixEmpty.getData().length, 0);

        try {
            new OctaveNdMatrix(1);
            fail("OctaveNdMatrixes does not support less than one dimension. ");
        } catch (final IllegalArgumentException e) {
            // OK
        }
    }

    /**
     */
    public void testDataSizeConstructor() {
        final double[] data = new double[2 * 3 * 4];
        for (int idx = 0; idx < data.length; idx++) {
            data[idx] = idx + 1.0;
        }
        final OctaveNdMatrix matrix = new OctaveNdMatrix(data, 2, 3, 4);
        double d = 1.0;
        for (int depth = 1; depth <= 4; depth++) {
            for (int column = 1; column <= 3; column++) {
                for (int row = 1; row <= 2; row++) {
                    assertEquals(d, matrix.get(row, column, depth));
                    d++;
                }
            }
        }

        // a larger data array is ok
        new OctaveNdMatrix(data, 2, 2, 4);

        try {
            new OctaveNdMatrix(data, 2, 4, 4);
            fail("should throw IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("length of data(24) is smaller than size([2, 4, 4])", e.getMessage());
        }
    }

    /**
     */
    public void testMakeCopy() {
        final double[] data = new double[2 * 3 * 4];
        for (int idx = 0; idx < data.length; idx++) {
            data[idx] = idx + 1.0;
        }
        final OctaveNdMatrix matrix = (new OctaveNdMatrix(data, 2, 3, 4)).makecopy();
        double d = 1.0;
        for (int depth = 1; depth <= 4; depth++) {
            for (int column = 1; column <= 3; column++) {
                for (int row = 1; row <= 2; row++) {
                    assertEquals(matrix.get(row, column, depth), d);
                    d++;
                }
            }
        }

    }

    /**
     * matrixzero doesn't work because of bug in octave
     */
    public void testGrowth() {
        final OctaveNdMatrix matrix = new OctaveNdMatrix(3, 3, 3, 3);
        matrix.set(42.0, 2, 2, 2, 2);
        matrix.set(1.0, 3, 2, 2, 2);
        matrix.set(2.0, 2, 3, 2, 2);
        matrix.set(3.0, 2, 2, 3, 2);
        matrix.set(4.0, 2, 2, 2, 3);
        assertEquals(42.0, matrix.get(2, 2, 2, 2));
        assertEquals(1.0, matrix.get(3, 2, 2, 2));
        assertEquals(2.0, matrix.get(2, 3, 2, 2));
        assertEquals(3.0, matrix.get(2, 2, 3, 2));
        assertEquals(4.0, matrix.get(2, 2, 2, 3));

        matrix.set(Math.PI, 4, 5, 7, 6);
        assertEquals(42.0, matrix.get(2, 2, 2, 2));
        assertEquals(1.0, matrix.get(3, 2, 2, 2));
        assertEquals(2.0, matrix.get(2, 3, 2, 2));
        assertEquals(3.0, matrix.get(2, 2, 3, 2));
        assertEquals(4.0, matrix.get(2, 2, 2, 3));
        assertEquals(Math.PI, matrix.get(4, 5, 7, 6));
    }

    /** */
    public void testResize() {
        final OctaveNdMatrix matrix = new OctaveNdMatrix(0, 4);
        assertEquals(2, matrix.getSize().length);
        assertEquals(0, matrix.getSize()[0]);
        assertEquals(4, matrix.getSize()[1]);
        // assertEquals(0, matrix.getData().length); is 0
        assertTrue(matrix.getData().length >= 0);

        matrix.set(42.0, 1, 1);
        assertEquals(42.0, matrix.get(1, 1));
        assertEquals(2, matrix.getSize().length);
        assertEquals(1, matrix.getSize()[0]);
        assertEquals(4, matrix.getSize()[1]);
        // assertEquals(4, matrix.getData().length); is 8
        assertTrue(matrix.getData().length >= 4);
    }

}
