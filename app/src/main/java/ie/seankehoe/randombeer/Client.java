package ie.seankehoe.randombeer;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Sean Kehoe on 24/05/2017.
 */

//

public interface Client {
    @GET("/beer/random?key=c9dd2bfc86379070eaa22cf9bc8b953c")
    Call<List<BeerDB>> loadRandom();
}
