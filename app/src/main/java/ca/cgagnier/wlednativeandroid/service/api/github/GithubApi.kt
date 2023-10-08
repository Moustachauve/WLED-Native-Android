package ca.cgagnier.wlednativeandroid.service.api.github

import android.content.Context
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class GithubApi(val context: Context) {
    fun getAllReleases(): List<Release>? {
        Log.d(TAG, "retrieving latest release")
        try {
            val api = getApi()
            val release = api.getAllReleases(REPO_OWNER, REPO_NAME)
            val execute = release.execute()
            val body = execute.body()
            return body
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            throw Exception("Could not get all releases: ${e.toString()}")
        }
    }

    private fun getApi(): GithubApiEndpoints {
        val cache = Cache(context.cacheDir, 10 * 1024 * 1024) // 10MB cache
        val httpOkClient = OkHttpClient.Builder()
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpOkClient)
            .build()
            .create(GithubApiEndpoints::class.java)
    }

    companion object {
        const val TAG = "github-release"
        const val BASE_URL = "https://api.github.com"
        const val REPO_OWNER = "Aircoookie"
        const val REPO_NAME = "WLED"
    }
}