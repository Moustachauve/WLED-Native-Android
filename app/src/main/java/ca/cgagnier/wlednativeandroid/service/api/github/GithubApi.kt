package ca.cgagnier.wlednativeandroid.service.api.github

import android.content.Context
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.net.UnknownHostException


abstract class GithubApi(
    val context: Context,
    private val repoOwner: String,
    private val repoName: String
) {
    fun getAllReleases(): List<Release>? {
        Log.d(TAG, "retrieving latest release")
        try {
            val api = getApi()
            val release = api.getAllReleases(repoOwner, repoName)
            val execute = release.execute()
            return execute.body()
        } catch (e: UnknownHostException) {
            Log.w(TAG, e.toString())
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        return null
    }

    suspend fun downloadReleaseBinary(
        asset: Asset,
        targetFile: File
    ): Flow<DownloadState> {
        val api = getApi()
        return api.downloadReleaseBinary(repoOwner, repoName, asset.assetId).saveFile(targetFile)
    }

    private suspend fun ResponseBody.saveFile(destinationFile: File): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0))

            try {
                byteStream().use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        val totalBytes = contentLength()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var progressBytes = 0L
                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            progressBytes += bytes
                            bytes = inputStream.read(buffer)
                            emit(DownloadState.Downloading(((progressBytes * 100) / totalBytes).toInt()))
                        }
                    }
                }
                emit(DownloadState.Finished)
            } catch (e: Exception) {
                emit(DownloadState.Failed(e))
            }
        }
            .flowOn(Dispatchers.IO).distinctUntilChanged()
    }

    protected open fun getApi(): GithubApiEndpoints {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getHttpOkClient())
            .build()
            .create(GithubApiEndpoints::class.java)
    }

    protected open fun getHttpOkClient(): OkHttpClient {
        val cache = Cache(context.cacheDir, 10 * 1024 * 1024) // 10MB cache
        return OkHttpClient.Builder()
            .cache(cache)
            .build()
    }

    companion object {
        const val TAG = "github-release"
        const val BASE_URL = "https://api.github.com"
    }
}