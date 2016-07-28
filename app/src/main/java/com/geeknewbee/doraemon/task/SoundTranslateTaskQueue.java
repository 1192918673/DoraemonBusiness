package com.geeknewbee.doraemon.task;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.http.BaseResponseBody;
import com.geeknewbee.doraemon.http.SoundService;
import com.geeknewbee.doraemon.util.RetrofitUtils;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 去服务器解析command
 */
public class SoundTranslateTaskQueue extends AbstractTaskQueue<String, List<Command>> {
    private volatile static SoundTranslateTaskQueue instance;
    private OnTranslatorListener translatorListener;

    public void setTranslatorListener(OnTranslatorListener translatorListener) {
        this.translatorListener = translatorListener;
    }

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

    public interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(List<Command> commands);
    }
}
