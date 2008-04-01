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
/**
 * @author Kim Hansen
 */
package dk.ange.octave;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Thread that writes to the octave process
 */
final class InputThread extends Thread {

    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(InputThread.class);

    private final OctaveExec octaveExec;

    private final Writer processWriter;

    private WriteFunctor currentInput;

    private String currentSpacer;

    private boolean close = false;

    /**
     * @param octaveExec
     * @param processWriter
     * @return the constructed and started InputThread
     */
    public static InputThread factory(final OctaveExec octaveExec, final Writer processWriter) {
        final InputThread inputThread = new InputThread(octaveExec, processWriter);
        inputThread.setName(Thread.currentThread().getName() + "-InputThread");
        inputThread.start();
        return inputThread;
    }

    /**
     * @param processWriter
     */
    private InputThread(final OctaveExec octaveExec, final Writer processWriter) {
        this.octaveExec = octaveExec;
        this.processWriter = processWriter;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    if (!close && currentInput == null) {
                        this.wait();
                    }
                }
            } catch (final InterruptedException e) {
                log.error("wait() threw InterruptedException", e);
            }
            log.trace("Woke up");
            if (close) {
                break;
            }
            if (currentInput != null) {
                doWrite();
                clearCurrent();
            }
        }
    }

    private synchronized void clearCurrent() {
        if (currentInput == null) {
            throw new IllegalStateException("currentInput == null");
        }
        if (currentSpacer == null) {
            throw new IllegalStateException("currentSpacer == null");
        }
        currentInput = null;
        currentSpacer = null;
    }

    private void doWrite() {
        log.debug("Enter doWrite()");
        currentInput.doWrite(processWriter);
        try {
            processWriter.write("\nprintf(\"%s\\n\", \"" + currentSpacer + "\");\n");
            processWriter.flush();
            octaveExec.setExecuteState(OctaveExec.ExecuteState.WRITER_OK);
        } catch (final IOException e) {
            log.error("Unexpected IOException in InputThread", e);
        }
    }

    /**
     * @param input
     * @param spacer
     */
    public synchronized void startWrite(final Reader input, final String spacer) {
        if (input == null) {
            throw new NullPointerException("input == null");
        }
        if (spacer == null) {
            throw new NullPointerException("spacer == null");
        }
        if (currentInput != null) {
            throw new IllegalStateException("currentInput != null");
        }
        if (currentSpacer != null) {
            throw new IllegalStateException("currentSpacer != null");
        }
        log.trace("Start the InputThread " + this);
        currentInput = new InputWriteFunctor(input);
        currentSpacer = spacer;
        this.notifyAll();
    }

    /**
     * Ask thread to close
     */
    public synchronized void close() {
        close = true;
        this.notify();
    }

}
