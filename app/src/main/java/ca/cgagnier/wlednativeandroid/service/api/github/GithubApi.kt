package ca.cgagnier.wlednativeandroid.service.api.github

import android.util.Log
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.PagedIterable


class GithubApi {
    private val github: GitHub = GitHub.connectAnonymously()
    private val repo: GHRepository = github.getUser(REPO_OWNER).getRepository(REPO_NAME);

    fun getAllReleases(): PagedIterable<GHRelease>? {
        Log.d(TAG, "retrieving latest release")
        val allReleases = repo.listReleases()
        Log.d(TAG, "found " + allReleases.count() + " releases")
        return allReleases
    }

    companion object {
        const val TAG = "github-release"
        const val REPO_OWNER = "Aircoookie"
        const val REPO_NAME = "WLED"
    }
}