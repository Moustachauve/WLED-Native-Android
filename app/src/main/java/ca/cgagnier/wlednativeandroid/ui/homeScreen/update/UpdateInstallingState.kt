package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

data class UpdateInstallingState(
    val canDismiss: Boolean = false,
    val step: UpdateInstallingStep = UpdateInstallingStep.Installing,
)

sealed class UpdateInstallingStep() {
    data object Starting : UpdateInstallingStep()
    data object Downloading : UpdateInstallingStep()
    data object Installing : UpdateInstallingStep()
    data class Error(
        val error: String, val showError: Boolean = false
    ) : UpdateInstallingStep()

    data object Done : UpdateInstallingStep()
}