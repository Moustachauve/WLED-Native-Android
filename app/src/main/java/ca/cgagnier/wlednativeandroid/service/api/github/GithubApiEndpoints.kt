package ca.cgagnier.wlednativeandroid.service.api.github

import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiEndpoints {
    @GET("repos/{repoOwner}/{repoName}/releases")
    fun getAllReleases(
        @Path("repoOwner") repoOwner: String,
        @Path("repoName") repoName: String
    ): Call<List<Release>>
}