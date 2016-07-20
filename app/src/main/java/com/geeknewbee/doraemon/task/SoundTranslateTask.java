package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.SoundTranslator;
import com.geeknewbee.doraemon.http.BaseResponseBody;
import com.geeknewbee.doraemon.http.SoundService;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.task.base.PriorityTask;
import com.geeknewbee.doraemon.util.RetrofitUtils;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 声音翻译task
 */
public class SoundTranslateTask extends PriorityTask<String, Void, List<Command>> {
    private SoundTranslator.OnTranslatorListener translatorListener;

    public SoundTranslateTask(SoundTranslator.OnTranslatorListener translatorListener) {
        this(Priority.DEFAULT, translatorListener);
    }

    public SoundTranslateTask(Priority priority, SoundTranslator.OnTranslatorListener translatorListener) {
        super(priority);
        this.translatorListener = translatorListener;
    }

    @Override
    protected List<Command> performTask(String... params) {
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        SoundService service = retrofit.create(SoundService.class);
        try {
            Response<BaseResponseBody<List<Command>>> response = service.translateSound(params[0]).execute();
            if (response.isSuccessful() && response.body().isSuccess()) {
                return response.body().getData();
            } else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Command> commands) {
        super.onPostExecute(commands);
        translatorListener.onTranslateComplete(commands);
    }
}
