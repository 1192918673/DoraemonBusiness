package com.geeknewbee.doraemon.task;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 用于直播 的时候接受语言流 用于播放
 */
public class BluetoothTalkTask {
    private BlockingQueue<byte[]> audioData;
    private TalkThread talkThread;
    private AudioTrack track;
    private boolean stop = false;

    public BluetoothTalkTask(BlockingQueue<byte[]> blockingQueue) {
        this.audioData = blockingQueue;
    }

    public void start() {
        if (talkThread != null) {
            talkThread.cancel();
            talkThread = null;
        }
        this.stop = false;
        talkThread = new TalkThread();
        talkThread.start();
    }

    public void stop() {
        this.stop = true;
    }

    private class TalkThread extends Thread {
        @Override
        public void run() {
            super.run();
            int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
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
                    Log.d("PlayTask", "play data:" + (poll == null ? "null" : "have data"));
                    if (poll != null)
                        //然后将数据写入到AudioTrack中
                        track.write(poll, 0, poll.length);
                }
            } catch (Exception e) {
                Log.d("PlayTask", "Exception=" + e.getMessage());
            } finally {
                //播放结束
                track.stop();
            }
        }

        public void cancel() {
            stop = true;
        }
    }
}
