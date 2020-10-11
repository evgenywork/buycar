package com.devlabs.buycar.calculator.api;

import com.devlabs.buycar.calculator.data.CalculateRequest;
import com.devlabs.buycar.calculator.data.CalculateResponse;
import com.devlabs.buycar.calculator.data.PaymentsGraphRequest;
import com.devlabs.buycar.calculator.data.PaymentsGraphResponse;
import com.devlabs.buycar.retrofit.Defaults;
import com.devlabs.buycar.retrofit.NetworkService;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CalculatorApi {

    @POST("/vtb/hackathon/calculate")
    @Headers({Defaults.ACCEPT_JSON_HEADER,
            Defaults.CONTENT_JSON_HEADER,
              Defaults.API_KEY_HEADER})
    public Call<CalculateResponse> calculate(@Body CalculateRequest data);

    @POST("/vtb/hackathon/payments-graph")
    @Headers({Defaults.ACCEPT_JSON_HEADER,
            Defaults.CONTENT_JSON_HEADER,
            Defaults.API_KEY_HEADER})
    public Call<PaymentsGraphResponse> calculate(@Body PaymentsGraphRequest data);
}
