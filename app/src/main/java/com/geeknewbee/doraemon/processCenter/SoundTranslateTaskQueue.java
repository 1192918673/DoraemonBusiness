package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemon.webservice.SoundService;
import com.geeknewbee.doraemon.processcenter.ISoundTranslate;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 把声音string去服务器解析command ，这个是串行的任务队列。
 * 按照先来后到的顺序去执行。
 */
public class SoundTranslateTaskQueue extends AbstractTaskQueue<String, List<Command>> implements ISoundTranslate {
    private volatile static SoundTranslateTaskQueue instance;
    private OnTranslatorListener translatorListener;

    public static SoundTranslateTaskQueue getInstance() {
        if (instance == null) {
            synchronized (SoundTranslateTaskQueue.class) {
                if (instance == null) {
                    instance = new SoundTranslateTaskQueue();
                }
            }
        }
        return instance;
    }

    @Override
    public void setTranslatorListener(OnTranslatorListener translatorListener) {
        this.translatorListener = translatorListener;
    }

    @Override
    public List<Command> performTask(String s) {
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        SoundService service = retrofit.create(SoundService.class);
        try {
            Response<BaseResponseBody<List<Command>>> response = service.translateSound(s).execute();
            if (response.isSuccessful() && response.body().isSuccess()) {
                return response.body().getData();
            } else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onTaskComplete(List<Command> output) {
        if (translatorListener != null)
            translatorListener.onTranslateComplete(output);
    }

    @Override
    public void translateSound(String s) {
        addTask(s);
    }
}
