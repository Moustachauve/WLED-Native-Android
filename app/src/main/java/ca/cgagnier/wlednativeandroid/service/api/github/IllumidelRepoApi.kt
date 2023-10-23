package ca.cgagnier.wlednativeandroid.service.api.github

import android.content.Context
import ca.cgagnier.wlednativeandroid.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class IllumidelRepoApi(context: Context) : GithubApi(context, REPO_OWNER, REPO_NAME) {
    override fun getHttpOkClient(): OkHttpClient {
        val cache = Cache(context.cacheDir, 10 * 1024 * 1024) // 10MB cache
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor {
                val newRequest = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_ILLUMIDEL_KEY}")
                    .build()
                it.proceed(newRequest)
            })
            .cache(cache)
            .build()
    }

    companion object {
        const val REPO_OWNER = "Illumidel"
        const val REPO_NAME = "App-Releases"
    }
}