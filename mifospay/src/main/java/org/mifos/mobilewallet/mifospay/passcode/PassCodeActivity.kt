package org.mifos.mobilewallet.mifospay.passcode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import com.mifos.mobile.passcode.MifosPassCodeActivity
import com.mifos.mobile.passcode.utils.EncryptionUtil
import com.mifos.mobile.passcode.utils.PassCodeConstants
import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import org.mifos.mobilewallet.mifospay.R
import org.mifos.mobilewallet.mifospay.auth.LoginActivity
import org.mifos.mobilewallet.mifospay.home.ui.MainActivity
import org.mifos.mobilewallet.mifospay.receipt.ui.ReceiptActivity
import org.mifos.mobilewallet.mifospay.common.Constants

@AndroidEntryPoint
class PassCodeActivity : MifosPassCodeActivity() {

    private var deepLinkURI: String? = null
    private var currPass: String? = ""
    private var updatePassword = false
    private var isInitialScreen = false

    val viewModel: PassCodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // can't call getActivityComponent b/c PassCodeActivity class does not extend BaseActivity

        isInitialScreen = intent.getBooleanExtra(
            PassCodeConstants.PASSCODE_INITIAL_LOGIN,
            false
        )
        if (intent != null) {
            currPass = intent.getStringExtra(Constants.CURRENT_PASSCODE)
            updatePassword = intent.getBooleanExtra(Constants.UPDATE_PASSCODE, false)
        }
        ButterKnife.bind(this)
        deepLinkURI = intent.getStringExtra("uri")
    }

    override fun getLogo(): Int {
        return 0
    }

    override fun startNextActivity() {
        // authenticate user with saved Preferences
        if (deepLinkURI != null) {
            val uri = Uri.parse(deepLinkURI)
            val intent = Intent(this@PassCodeActivity, ReceiptActivity::class.java)
            intent.data = uri
            startActivity(intent)
        } else {
            val intent = Intent(this@PassCodeActivity, MainActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(intent)
        }
    }

    override fun startLoginActivity() {
        val builder = AlertDialog.Builder(this@PassCodeActivity)
        builder.setTitle(R.string.passcode_title)
        builder.setPositiveButton(R.string.yes) { dialog, which ->
            val intent = Intent(this@PassCodeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        val dialog = builder.create()
        dialog.show()
    }

    override fun showToaster(view: View, msg: Int) {
        Toast.makeText(applicationContext, getText(msg), Toast.LENGTH_SHORT).show()
    }

    override fun getEncryptionType(): Int {
        return EncryptionUtil.DEFAULT
    }

    private fun saveCurrentPasscode() {
        if (updatePassword && currPass?.isNotEmpty() == true) {
            val helper = PasscodePreferencesHelper(this)
            helper.savePassCode(currPass)
        }
    }

    override fun skip(v: View) {
        saveCurrentPasscode()
        if (isInitialScreen) {
            startNextActivity()
        }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveCurrentPasscode()
        finishAffinity()
    }
}