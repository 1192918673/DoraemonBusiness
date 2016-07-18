package com.sangebaba.doraemon.business.task;

import com.sangebaba.doraemon.business.BuildConfig;
import com.sangebaba.doraemon.business.control.Command;
import com.sangebaba.doraemon.business.control.SoundTranslator;
import com.sangebaba.doraemon.business.http.BaseResponseBody;
import com.sangebaba.doraemon.business.http.SoundService;
import com.sangebaba.doraemon.business.task.base.Priority;
import com.sangebaba.doraemon.business.task.base.PriorityTask;
import com.sangebaba.doraemon.business.util.RetrofitUtils;

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
