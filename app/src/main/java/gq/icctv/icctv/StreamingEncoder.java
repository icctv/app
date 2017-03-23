package gq.icctv.icctv;

import android.hardware.Camera;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class StreamingEncoder implements Camera.PreviewCallback {

    private SurfaceView surfaceView;
    byte[] yuvFrame = new byte[1920 * 1280 * 2];
    ExecutorService threadPool;
    StreamingEncoderTask streamingEncoderTask;
    private ReentrantLock reentrantLock = new ReentrantLock();
    private boolean busy = false;

    public StreamingEncoder (SurfaceView s) {
        surfaceView = s;
        threadPool = Executors.newFixedThreadPool(3);
        streamingEncoderTask = new StreamingEncoderTask();
    }

    public void onPreviewFrame(byte[] frame, Camera camera) {
        reentrantLock.lock();
        addFrameToBuffer(frame);
        camera.addCallbackBuffer(frame);
        reentrantLock.unlock();
    }

    private void addFrameToBuffer(byte[] frame) {
        // Skip this frame if we're still busy encoding the last one
        if (busy) {
            return;
        } else {
            busy = true;
        }

        int width = surfaceView.getWidth();
        int height = surfaceView.getHeight();

        // TODO: Figure out why reference implementation copies more bytes here, as this triggers an ArrayIndexOutOfBounds Exception
        // int size = width * height + width * height / 2
        int size = width * height;

        // This is a weird hack as it still crashed on devices that have a high camera resolution
        if (size > frame.length) {
            size = frame.length;
        }

        System.arraycopy(frame, 0, yuvFrame, 0, size);

        threadPool.execute(streamingEncoderTask);
    }

    private class StreamingEncoderTask implements Runnable {

        @Override
        public void run() {
            busy = false;

            
            System.out.println(yuvFrame[0]);
        }
    }
}
