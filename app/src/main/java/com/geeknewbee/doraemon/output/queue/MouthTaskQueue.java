package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.entity.WeatherEntity;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemon.webservice.SoundService;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.MD5Util;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 声音 task queue
 */
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private volatile static MouthTaskQueue instance;
    private ITTS itts;
    private IMusicPlayer iMusicPlayer;

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
    }

    public static MouthTaskQueue getInstance() {
        if (instance == null) {
            synchronized (MouthTaskQueue.class) {
                if (instance == null) {
                    instance = new MouthTaskQueue();
                }
            }
        }
        return instance;
    }

    @Override
    public Boolean performTask(Command input) {
        switch (input.getType()) {
            case PLAY_SOUND:
                iMusicPlayer.stop();
                itts.talk(input.getContent());
                break;
            case PLAY_MUSIC:
                iMusicPlayer.stop();
                itts.talk("正在为您搜索音乐");
                iMusicPlayer.play(input.getContent());
                break;
            case WEATHER:
                //TODO 查询天气
                Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
                SoundService service = retrofit.create(SoundService.class);
                try {
                    String timeStamp = System.currentTimeMillis() / 1000 + "";
                    String cityId = input.getContent();
                    String token = "c4adee396b1260222696";
                    String key = MD5Util.md5(new String("mojisgbb20151207" + timeStamp + cityId));
                    // 查询天气
                    Response<WeatherEntity> weather = service.queryWeather(timeStamp, cityId, token, key).execute();
                    if (weather.isSuccessful() && weather.body() != null) {
                        String tips = weather.body().getData().getCondition().getTips();
                        itts.talk(tips);
                    } else
                        return null;
                } catch (IOException e) {
                    return null;
                }
                break;
        }
        return true;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }

    public void stop() {
        iMusicPlayer.stop();
        clearTasks();
    }
}
