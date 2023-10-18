package ca.cgagnier.wlednativeandroid.service.api.github

import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GithubApiEndpoints {
    @GET("repos/{repoOwner}/{repoName}/releases")
    fun getAllReleases(
        @Path("repoOwner") repoOwner: String,
        @Path("repoName") repoName: String
    ): Call<List<Release>>


    @Streaming
    @GET
    suspend fun downloadReleaseBinary(@Url url: String): ResponseBody
}