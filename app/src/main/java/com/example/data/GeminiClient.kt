package com.example.data

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateImage(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: ImageGenerateRequest
    ): ImageGenerateResponse
}

@Serializable
data class ImageGenerateRequest(
    val contents: List<ImageContent>,
    val generationConfig: ImageGenerationConfig
)

@Serializable
data class ImageContent(
    val parts: List<ImagePart>
)

@Serializable
data class ImagePart(
    val text: String
)

@Serializable
data class ImageGenerationConfig(
    val imageConfig: ImageSizeConfig,
    val responseModalities: List<String> = listOf("TEXT", "IMAGE")
)

@Serializable
data class ImageSizeConfig(
    val aspectRatio: String = "1:1",
    val imageSize: String
)

@Serializable
data class ImageGenerateResponse(
    val candidates: List<ImageCandidate>? = null
)

@Serializable
data class ImageCandidate(
    val content: ImageResponseContent? = null
)

@Serializable
data class ImageResponseContent(
    val parts: List<ImageResponsePart>? = null
)

@Serializable
data class ImageResponsePart(
    val text: String? = null,
    val inlineData: ImageInlineData? = null
)

@Serializable
data class ImageInlineData(
    val mimeType: String,
    val data: String
)

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
