import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @GET("api/consultor2/WS_ITEVEBASA/matriculas")
    Call<List<Inspeccion>> getInspeccion(@Query("anyo") String anyo, @Query("estacion") String estacion);
}

