package com.geeknewbee.doraemon.webservice;


import android.util.EventLogTags;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitHelper {
    public static <T> void sendRequest(Call<BaseResponseBody<T>> call, final RetrofitCallBack<T> callBack) {
        call.enqueue(new Callback<BaseResponseBody<T>>() {
            @Override
            public void onResponse(Call<BaseResponseBody<T>> call, Response<BaseResponseBody<T>> response) {
                if (callBack == null)
                    return;

                if (response.isSuccessful() && response.body().isSuccess()) {
                    callBack.onSuccess(response.body().getData());
                } else
                    callBack.onFailure(response.body().getMsg());
            }

            @Override
            public void onFailure(Call<BaseResponseBody<T>> call, Throwable t) {
                if (callBack == null)
                    return;

                boolean networkConnected = DeviceUtil.isNetworkConnected(BaseApplication.mContext);
                callBack.onFailure(networkConnected ?
                        t.getMessage() :
                        BaseApplication.mContext.getString(R.string.not_connect_to_network));
            }
        });
    }

}
