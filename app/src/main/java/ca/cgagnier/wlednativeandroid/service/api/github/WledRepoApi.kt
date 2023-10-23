package ca.cgagnier.wlednativeandroid.service.api.github

import android.content.Context

class WledRepoApi(context: Context) : GithubApi(context, REPO_OWNER, REPO_NAME) {

    companion object {
        const val REPO_OWNER = "Aircoookie"
        const val REPO_NAME = "WLED"
    }
}