package dk.ange.octave.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import dk.ange.octave.OctaveException;

/**
 * @author Kim Hansen
 * 
 * Common interface for the octave types.
 */
public abstract class OctaveType {

    /**
     * Write the definition of the variable to writer
     * 
     * @param writer
     *            Writer to write to
     * @param name
     *            name of variable
     * @throws IOException
     */
    abstract public void toOctave(Writer writer, String name) throws IOException;

    /**
     * @param name
     * @return Returns a Reader from which the octave input version of this object can be read.
     * @throws OctaveException
     */
    public Reader octaveReader(String name) throws OctaveException {
        PipedReader pipedReader = new PipedReader();
        PipedWriter pipedWriter = new PipedWriter();
        try {
            pipedWriter.connect(pipedReader);
        } catch (IOException e) {
            throw new OctaveException(e);
        }
        ToOctaveWriter toOctaveWriter = new ToOctaveWriter(this, pipedWriter, name);
        toOctaveWriter.start();
        return pipedReader;
    }

    private class ToOctaveWriter extends Thread {

        OctaveType octaveType;

        PipedWriter pipedWriter;

        String name;

        /**
         * @param octaveType
         * @param pipedWriter
         * @param name
         */
        public ToOctaveWriter(OctaveType octaveType, PipedWriter pipedWriter, String name) {
            this.octaveType = octaveType;
            this.pipedWriter = pipedWriter;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                octaveType.toOctave(pipedWriter, name);
                pipedWriter.close();
            } catch (IOException e1) {
                // Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }

    /**
     * @param name
     *            name of variable
     * @return Text to enter into octave to define the variable
     */
    public String toOctave(String name) {
        StringWriter writer = new StringWriter();
        try {
            toOctave(writer, name);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return writer.toString();
    }

    @Override
    public String toString() {
        return toOctave("ans");
    }

    /**
     * The text returned should be the part that defines the value of the variable, ie. what follows after the line "#
     * name: ...".
     * 
     * I don't use this function because I haven't found a way to use load from stdin. But if I could do that I would
     * find this way to do it to be much nice than the use of toOctave(name).
     * 
     * @param writer
     * @throws OctaveException
     */
    public void toText(@SuppressWarnings("unused")
    Writer writer) throws OctaveException {
        throw new OctaveException("Not implemented");
    }

    /**
     * @return Text to feed to 'load -text -' to define the variable
     * @throws OctaveException
     */
    public String toText() throws OctaveException {
        throw new OctaveException("Not implemented");
    }

    String readerReadLine(BufferedReader reader) throws OctaveException {
        try {
            String line = reader.readLine();
            if (line == null)
                throw new OctaveException("Pipe to octave-process broken");
            return line;
        } catch (IOException e) {
            throw new OctaveException(e);
        }
    }

    /**
     * This is almost the same as Double.parseDouble(), but it handles a few more versions of infinity
     * 
     * @param string
     * @return The parsed Double
     */
    protected double parseDouble(String string) {
        if ("Inf".equals(string)) {
            return Double.POSITIVE_INFINITY;
        }
        if ("-Inf".equals(string)) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.parseDouble(string);
    }

}
