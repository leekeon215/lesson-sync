// android-app/app/src/main/java/com/lessonsync/app/retrofit/AudioService.kt
import com.lessonsync.app.entity.LessonData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AudioService {

    @Multipart
    @POST("lesson-summary")
    suspend fun processLesson( // 반환 타입을 Response<LessonData>로 변경
        @Part file: MultipartBody.Part
    ): Response<LessonData>
}