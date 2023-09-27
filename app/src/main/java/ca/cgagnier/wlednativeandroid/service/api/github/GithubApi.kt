package ca.cgagnier.wlednativeandroid.service.api.github

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.PagedIterable
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector


class GithubApi(context: Context) {
    private val github: GitHub
    private val repo: GHRepository

    init {
        val cache = Cache(context.cacheDir, 10 * 1024 * 1024) // 10MB cache
        val connector =
            OkHttpGitHubConnector(OkHttpClient.Builder().cache(cache).build(), CACHE_MAX_AGE)
        github = GitHubBuilder().withConnector(connector).build()
        val remainingLimit = github.rateLimit.getRemaining()
        Log.d(TAG, "Github API: $remainingLimit remaining calls")

        repo = github.getUser(REPO_OWNER).getRepository(REPO_NAME)
    }

    fun getAllReleases(): PagedIterable<GHRelease>? {
        Log.d(TAG, "retrieving latest release")
        val allReleases = repo.listReleases()
        Log.d(TAG, "found ${allReleases.count()} releases")
        return allReleases
    }

    companion object {
        const val TAG = "github-release"
        const val REPO_OWNER = "Aircoookie"
        const val REPO_NAME = "WLED"
        const val CACHE_MAX_AGE = 3600
    }
}