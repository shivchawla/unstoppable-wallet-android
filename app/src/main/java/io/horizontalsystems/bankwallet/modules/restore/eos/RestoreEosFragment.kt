package io.horizontalsystems.bankwallet.modules.restore.eos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseFragment
import io.horizontalsystems.bankwallet.core.adapters.EosError
import io.horizontalsystems.bankwallet.core.utils.ModuleField
import io.horizontalsystems.bankwallet.core.utils.Utils
import io.horizontalsystems.bankwallet.modules.qrscanner.QRScannerActivity
import io.horizontalsystems.bankwallet.modules.restore.RestoreFragment
import io.horizontalsystems.bankwallet.ui.helpers.TextHelper
import io.horizontalsystems.core.CoreApp
import io.horizontalsystems.core.findNavController
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.core.helpers.KeyboardHelper
import io.horizontalsystems.eoskit.core.InvalidPrivateKey
import io.horizontalsystems.views.MultipleInputEditTextView
import kotlinx.android.synthetic.main.fragment_restore_eos.*
import kotlinx.android.synthetic.main.fragment_restore_eos.eosAccount
import kotlinx.android.synthetic.main.fragment_restore_eos.eosActivePrivateKey


class RestoreEosFragment : BaseFragment() {

    private lateinit var viewModel: RestoreEosViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restore_eos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuRestore -> {
                    viewModel.onProceed()
                    true
                }
                else -> false
            }
        }

        viewModel = ViewModelProvider(this, RestoreEosModule.Factory())
                .get(RestoreEosViewModel::class.java)

        eosAccount.btnText = getString(R.string.Send_Button_Paste)
        eosActivePrivateKey.btnText = getString(R.string.Send_Button_Paste)

        eosAccount.setListenerForTextInput(object : MultipleInputEditTextView.Listener{
            override fun beforeTextChanged() {
                checkKeyboard()
            }

            override fun onTextChanged(text: String) {
                viewModel.onEnterAccount(text)
            }
        })

        eosActivePrivateKey.setListenerForTextInput(object : MultipleInputEditTextView.Listener{
            override fun beforeTextChanged() {
                checkKeyboard()
            }

            override fun onTextChanged(text: String) {
                viewModel.onEnterPrivateKey(text)
            }
        })

        bindActions()
        observe()

        activity?.let {
            KeyboardHelper.showKeyboardDelayed(it, eosAccount, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(ModuleField.SCAN_ADDRESS)?.let {
                eosActivePrivateKey.text = it
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkKeyboard() {
        context?.let {
            if (Utils.isUsingCustomKeyboard(it) && !CoreApp.thirdKeyboardStorage.isThirdPartyKeyboardAllowed) {
                showCustomKeyboardAlert()
            }
        }
    }

    private fun observe() {
        viewModel.accountTypeLiveEvent.observe(viewLifecycleOwner, Observer { accountType ->
            hideKeyboard()
            setFragmentResult(RestoreFragment.accountTypeRequestKey, bundleOf(RestoreFragment.accountTypeBundleKey to accountType))
        })

        viewModel.errorLiveData.observe(viewLifecycleOwner, Observer {
            val error = when(it){
                is EosError.InvalidAccountName -> R.string.Restore_EosAccountIncorrect
                is InvalidPrivateKey -> R.string.Restore_EosKeyIncorrect
                else -> R.string.default_error_msg
            }
            HudHelper.showErrorMessage(this.requireView(), getString(error))
        })
    }

    private fun bindActions() {
        eosAccount.bind(onPaste = {
            val pasteText = TextHelper.getCopiedText()
            eosAccount.text = pasteText
        })
        eosActivePrivateKey.bind(
                onPaste = {
                    val pasteText = TextHelper.getCopiedText()
                    eosActivePrivateKey.text = pasteText
                },
                onScan = { activity?.let {
                    QRScannerActivity.start(this)
                } }
        )
    }

}
