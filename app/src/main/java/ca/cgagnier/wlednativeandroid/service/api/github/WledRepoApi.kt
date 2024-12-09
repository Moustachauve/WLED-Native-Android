package ca.cgagnier.wlednativeandroid.service.api.github

import java.io.File

class WledRepoApi(cacheDir: File) : GithubApi(cacheDir, REPO_OWNER, REPO_NAME) {

    companion object {
        const val REPO_OWNER = "Aircoookie"
        const val REPO_NAME = "WLED"
    }
}