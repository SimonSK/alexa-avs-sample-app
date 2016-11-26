/** 
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *   http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package com.amazon.alexa.avs;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioCapture {
    private static AudioCapture sAudioCapture;
    private final TargetDataLine microphoneLine;
    private AudioFormat audioFormat;
    private AudioBufferThread thread;

    private static final int BUFFER_SIZE_IN_SECONDS = 6;

    private final int BUFFER_SIZE_IN_BYTES;

    private static final Logger log = LoggerFactory.getLogger(AudioCapture.class);

    public static AudioCapture getAudioHardware(final AudioFormat audioFormat,
            MicrophoneLineFactory microphoneLineFactory) throws LineUnavailableException {
        if (sAudioCapture == null) {
            sAudioCapture = new AudioCapture(audioFormat, microphoneLineFactory);
        }
        return sAudioCapture;
    }

    private AudioCapture(final AudioFormat audioFormat, MicrophoneLineFactory microphoneLineFactory)
            throws LineUnavailableException {
        super();
        this.audioFormat = audioFormat;
        microphoneLine = microphoneLineFactory.getMicrophone();
        if (microphoneLine == null) {
            throw new LineUnavailableException();
        }
        BUFFER_SIZE_IN_BYTES = (int) ((audioFormat.getSampleSizeInBits() * audioFormat.getSampleRate()) / 8
                * BUFFER_SIZE_IN_SECONDS);
    }

    public InputStream getAudioInputStream(final RecordingStateListener stateListener,
            final RecordingRMSListener rmsListener) throws LineUnavailableException, IOException {
        try {
            startCapture();
            PipedInputStream inputStream = new PipedInputStream(BUFFER_SIZE_IN_BYTES);
            thread = new AudioBufferThread(inputStream, stateListener, rmsListener);
            thread.start();
            return inputStream;
        } catch (LineUnavailableException | IOException e) {
            stopCapture();
            throw e;
        }
    }

    public void stopCapture() {
        microphoneLine.stop();
        microphoneLine.close();

    }

    private void startCapture() throws LineUnavailableException {
        microphoneLine.open(audioFormat);
        microphoneLine.start();
    }

    public int getAudioBufferSizeInBytes() {
        return BUFFER_SIZE_IN_BYTES;
    }

    private class AudioBufferThread extends Thread {

        private final AudioStateOutputStream audioStateOutputStream;

        public AudioBufferThread(PipedInputStream inputStream,
                RecordingStateListener recordingStateListener, RecordingRMSListener rmsListener)
                        throws IOException {
            audioStateOutputStream =
                    new AudioStateOutputStream(inputStream, recordingStateListener, rmsListener);
        }

        @Override
        public void run() {
            byte[] data;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (microphoneLine.isOpen()) {
                data = copyAudioBytesFromInputToOutput();
                try{
                    outputStream.write(data);
                } catch (IOException e) {
                    log.error("wtf unknown error", e);
                }
            }
            data = outputStream.toByteArray();
            InputStream b_in = new ByteArrayInputStream(data);
            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
            AudioInputStream stream = new AudioInputStream(b_in, format, data.length);
            // audio file path hardcoded FIX THIS
            File file = new File("/home/pi/Desktop/input.wav");
            try{
                file.createNewFile();
                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
            } catch (IOException e) {
                log.error("wtf unknown error", e);
            }
            closePipedOutputStream();
        }

        private byte[] copyAudioBytesFromInputToOutput() {
            byte[] data = new byte[microphoneLine.getBufferSize() / 5];
            int numBytesRead = microphoneLine.read(data, 0, data.length);
            try {
                audioStateOutputStream.write(data, 0, numBytesRead);
            } catch (IOException e) {
                stopCapture();
            }
            return data;
        }

        private void closePipedOutputStream() {
            try {
                audioStateOutputStream.close();
            } catch (IOException e) {
                log.error("Failed to close audio stream ", e);
            }
        }
    }

}
