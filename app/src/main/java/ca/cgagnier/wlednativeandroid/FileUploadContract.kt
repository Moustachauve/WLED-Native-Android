package ca.cgagnier.wlednativeandroid

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class FileUploadContract(): ActivityResultContract<Int, FileUploadContractResult>() {

    override fun createIntent(context: Context, input: Int?): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/octet-stream"

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): FileUploadContractResult {
        return FileUploadContractResult(resultCode, intent)
    }
}