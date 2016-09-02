package com.geeknewbee.doraemon.output;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 接收蓝牙Audio流 并播放
 */
public class BluetoothTalkTask {
    private BlockingQueue<byte[]> audioData;
    private TalkThread talkThread;
    private boolean stop = false;
    private boolean hasStarted = false;

    public BluetoothTalkTask(BlockingQueue<byte[]> blockingQueue) {
        this.audioData = blockingQueue;
    }

    public synchronized boolean hasStarted() {
        return hasStarted;
    }

    public void start() {
        if (talkThread != null) {
            return;
        }

        this.stop = false;
        hasStarted = true;
        talkThread = new TalkThread();
        talkThread.start();
    }

    public void stop() {
        this.stop = true;
        if (talkThread != null) {
            talkThread.cancel();
            talkThread = null;
        }
    }

    private class TalkThread extends Thread {
        @Override
        public void run() {
            super.run();
            int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            AudioTrack track = null;
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
//				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //实例AudioTrack
                track = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                byte[] poll;
                while (!stop) {
                    poll = audioData.poll(100, TimeUnit.MILLISECONDS);
                    if (poll != null)
                        //然后将数据写入到AudioTrack中
                        track.write(poll, 0, poll.length);
                }
            } catch (Exception e) {
                Log.d("PlayTask", "Exception=" + e.getMessage());
            } finally {
                //播放结束
                if (track != null)
                    track.stop();
            }
        }

        public void cancel() {
            stop = true;
        }
    }
}
