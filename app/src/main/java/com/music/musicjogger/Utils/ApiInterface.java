package com.music.musicjogger.Utils;
import com.music.musicjogger.models.ModelAudio;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiInterface {
    @GET
    Call<ModelAudio> audio(@Url String url
    );


}
