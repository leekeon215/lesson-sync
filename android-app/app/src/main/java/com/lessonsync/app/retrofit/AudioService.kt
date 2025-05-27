import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AudioService {

    @Multipart
    @POST("lesson-summary")
    suspend fun processLessonCoroutine(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>
}
