package fans.skin.expresstrade.managers.network;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.network.NetworkManager.*;
import fans.skin.expresstrade.managers.pref.PrefManager;
import fans.skin.expresstrade.models.*;

import java.io.*;
import java.lang.annotation.*;
import java.util.*;

import retrofit.*;
import retrofit.Call;
import retrofit.http.*;

import static fans.skin.expresstrade.App.logManager;
import static fans.skin.expresstrade.App.prefManager;

public class NetworkRetrofit {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Interfaces.Oauth mOauth;
    private Interfaces.TradeApi mTrade;
    private Interfaces.SF mSF;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public NetworkRetrofit() {

        Retrofit.Builder builder = new Retrofit
                .Builder()
                .addConverterFactory(GsonConverterFactory.create());

        this.mOauth = builder
                .baseUrl(App.URL_OPS_AUTH)
                .build()
                .create(Interfaces.Oauth.class);

        this.mTrade = builder
                .baseUrl(App.URL_TRADE_AUTH)
                .build()
                .create(Interfaces.TradeApi.class);

        this.mSF = builder
                .baseUrl("https://api.skin.fans/method/")
                .build()
                .create(Interfaces.SF.class);
    }

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    private static class Interfaces {

        public interface Oauth {
            @POST("access_token")
            @FormUrlEncoded
            Call<AuthorizeModel> authorize(@FieldMap Map<String, String> query, @Header("Authorization") String authorization);
        }

        public interface TradeApi {
            @GET("IUser/GetProfile/v1/")
            Call<ResponseModel<UserModel>> getProfile(@QueryMap Map<String, String> query, @Header("Authorization") String authorization);

            @GET("IUser/GetInventory/v1/")
            Call<ResponseModel<ItemModel.Items>> getInventory(@QueryMap Map<String, String> query, @Header("Authorization") String authorization);

            @GET("ITrade/GetUserInventory/v1/")
            Call<ResponseModel<ItemModel.Items>> getUserInventory(@QueryMap Map<String, String> query, @Header("Authorization") String authorization);

            @GET("ITrade/GetOffers/v1/")
            Call<ResponseModel<OfferModel.Offers>> getOffers(@QueryMap Map<String, String> query, @Header("Authorization") String authorization);

            @POST("ITrade/SendOffer/v1/")
            Call<ResponseModel<OfferModel.Trade>> sendOffer(@Body Map<String, String> query, @Header("Authorization") String authorization);

            @POST("ITrade/CancelOffer/v1/")
            Call<ResponseModel<OfferModel.Trade>> cancelOffer(@Body Map<String, String> query, @Header("Authorization") String authorization);

            @POST("ITrade/AcceptOffer/v1/")
            Call<ResponseModel<OfferModel.Trade>> acceptOffer(@Body Map<String, String> query, @Header("Authorization") String authorization);

            @POST("IUser/UpdateProfile/v1/")
            Call<ResponseModel<Object>> updateProfile(@Body Map<String, String> query, @Header("Authorization") String authorization);
        }

        public interface SF {
            @GET("expresstrade.getMap")
            Call<ResponseModel<MapModel>> getMap(@QueryMap Map<String, String> query);

            @GET("expresstrade.checkIn")
            Call<ResponseModel<CheckInModel>> checkIn(@QueryMap Map<String, String> query);

            @GET("expresstrade.sendLog")
            Call<ResponseModel<Object>> sendLog(@QueryMap Map<String, String> query);
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void request(
            MethodType method,
            NetworkQuery query,
            NetworkCallback networkCallback
    ) {
        HashMap<String, String> hashMap = query != null ? query.getQuery() : (new NetworkQuery()).getQuery();
        String header;

        switch (method) {
            case ACCOUNT_ACCESS_TOKEN:
            case ACCOUNT_REFRESH_TOKEN:
                assert query != null;
                header = "Basic " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_CLIENT_BASIC_TOKEN);
                mOauth.authorize(hashMap, header).enqueue(new HttpCallback<AuthorizeModel>(networkCallback));
                break;

            // ***
            case ACCOUNT_GET_PROFILE:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.getProfile(hashMap, header).enqueue(new HttpCallback<ResponseModel<UserModel>>(networkCallback));
                break;

            // ***
            case ACCOUNT_GET_INVENTORY:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.getInventory(hashMap, header).enqueue(new HttpCallback<ResponseModel<ItemModel.Items>>(networkCallback));
                break;

            // ***
            case USERS_GET_USER_INVENTORY:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.getUserInventory(hashMap, header).enqueue(new HttpCallback<ResponseModel<ItemModel.Items>>(networkCallback));
                break;

            // ***
            case TRADE_GET_OFFERS:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.getOffers(hashMap, header).enqueue(new HttpCallback<ResponseModel<OfferModel.Offers>>(networkCallback));
                break;

            // ***
            case TRADE_SEND_OFFER:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.sendOffer(hashMap, header).enqueue(new HttpCallback<ResponseModel<OfferModel.Trade>>(networkCallback));
                break;

            // ***
            case TRADE_CANCEL_OFFER:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.cancelOffer(hashMap, header).enqueue(new HttpCallback<ResponseModel<OfferModel.Trade>>(networkCallback));
                break;

            // ***
            case TRADE_ACCEPT_OFFER:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.acceptOffer(hashMap, header).enqueue(new HttpCallback<ResponseModel<OfferModel.Trade>>(networkCallback));
                break;
            // ***

            case ACCOUNT_UPDATE_PROFILE:
                header = "Bearer " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS);
                mTrade.updateProfile(hashMap, header).enqueue(new HttpCallback<ResponseModel<Object>>(networkCallback));
                break;
            // ***

            case SF_GET_MAP:
                mSF.getMap(hashMap).enqueue(new HttpCallback<ResponseModel<MapModel>>(networkCallback));
                break;

            case CHECK_IN:
                mSF.checkIn(hashMap).enqueue(new HttpCallback<ResponseModel<CheckInModel>>(networkCallback));
                break;

            case SEND_LOG:
                mSF.sendLog(hashMap).enqueue(new HttpCallback<ResponseModel<Object>>(networkCallback));
                break;
        }
    }

    // =============================================================================================
    // CLASS CALLBACK
    // =============================================================================================

    private final class HttpCallback<T> implements retrofit.Callback<T> {
        private NetworkCallback networkCallback;

        public HttpCallback(NetworkCallback cb) {
            networkCallback = cb;
        }

        @Override
        public void onResponse(retrofit.Response response, Retrofit retrofit) {


            App.logManager.debug("TEST RESULT " + response.code());

            if(response.code() == 405) {
                try {
                    App.logManager.debug(response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // on200, on400, on403, on500
            switch (response.code()) {
                // OK
                case 200:
                    switch (networkCallback.method) {
                        case ACCOUNT_ACCESS_TOKEN:
                        case ACCOUNT_REFRESH_TOKEN:

                        case ACCOUNT_GET_INVENTORY:
                        case ACCOUNT_GET_PROFILE:
                        case USERS_GET_USER_INVENTORY:
                        case TRADE_GET_OFFERS:
                        case TRADE_SEND_OFFER:
                        case TRADE_CANCEL_OFFER:
                        case TRADE_ACCEPT_OFFER:
                        case SF_GET_MAP:

                            networkCallback.on200(response.body());
                            break;

                        default:
                            networkCallback.on200(((ResponseModel) response.body()).response);
                            break;
                    }

                    break;

                // 403 Forbidden / No access to resources. That is, if not the correct ACCESS_TOKEN
                case 400:
                case 401:
                case 403:
                    App.logManager.debug("RESULT CODE " + response.code());

                    try {
                        ErrorModel error = (ErrorModel) retrofit
                                .responseConverter(ErrorModel.class, new Annotation[0])
                                .convert(response.errorBody());

                        if (error.status != null || networkCallback.method == MethodType.SF_GET_MAP)
                            networkCallback.on400(error);
                        else
                            networkCallback.on403(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                // Internal server error
                case 500:
                case 502:
                case 503:
                    App.logManager.error(response.code() + " " + response.message());
                    networkCallback.onFailure(StatusCode.STATUS_500);
                    break;
            }
        }

        @Override
        public void onFailure(Throwable t) {
            // onNE

            // FIXME PARALL ANSWER AND TRANSFER NECESSARY STATUS
            logManager.error("====");
            logManager.error(t.getMessage());
            logManager.error("====");

            networkCallback.onFailure(StatusCode.STATUS_NONETWORK);
        }
    }
}
