package io.horizontalsystems.bankwallet.modules.balance.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.ui.extensions.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sync_error.*

class SyncErrorDialog(private val listener: Listener, private val coinName: String) : BaseBottomSheetDialogFragment() {

    interface Listener {
        fun onClickRetry()
        fun onClickChangeSource()
        fun onClickReport()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentView(R.layout.fragment_bottom_sync_error)

        setTitle(activity?.getString(R.string.BalanceSyncError_Title))
        setSubtitle(coinName)
        setHeaderIcon(R.drawable.ic_attention_red)

        bindActions()
    }

    private fun bindActions() {
        retryBtn.setOnClickListener {
            listener.onClickRetry()
            dismiss()
        }

        changeSourceBtn.setOnClickListener {
            listener.onClickChangeSource()
            dismiss()
        }

        reportBtn.setOnClickListener {
            listener.onClickReport()
            dismiss()
        }
    }

    companion object {
        fun show(activity: FragmentActivity, coinName: String, listener: Listener) {
            val fragment = SyncErrorDialog(listener, coinName)
            val transaction = activity.supportFragmentManager.beginTransaction()

            transaction.add(fragment, "bottom_sync_error")
            transaction.commitAllowingStateLoss()
        }
    }
}