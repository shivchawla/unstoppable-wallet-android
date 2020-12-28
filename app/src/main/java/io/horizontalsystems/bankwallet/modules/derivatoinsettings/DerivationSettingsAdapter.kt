package io.horizontalsystems.bankwallet.modules.derivatoinsettings

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.ui.extensions.SettingItemWithCheckmark
import io.horizontalsystems.views.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_derivation_settings.*

class DerivationSettingsAdapter(private val listener: Listener) : RecyclerView.Adapter<DerivationSettingsAdapter.DerivationSettingsItemViewHolder>() {

    interface Listener {
        fun onSettingClick(sectionIndex: Int, settingIndex: Int)
    }

    var items = listOf<DerivationSettingsModule.SectionItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DerivationSettingsItemViewHolder {
        return DerivationSettingsItemViewHolder(inflate(parent, DerivationSettingsItemViewHolder.layout, false), listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DerivationSettingsItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class DerivationSettingsItemViewHolder(override val containerView: View, private val listener: Listener)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val optionViewIds = listOf(R.id.option1, R.id.option2, R.id.option3)

        fun bind(viewItem: DerivationSettingsModule.SectionItem) {
            bipSectionHeader.text = viewItem.coinTypeName
            option3.isVisible = viewItem.viewItems.size == 3

            viewItem.viewItems.forEachIndexed { index, item ->
                containerView.findViewById<SettingItemWithCheckmark>(optionViewIds[index])?.apply {
                    bind(
                            item.title,
                            item.subtitle,
                            item.selected,
                            { listener.onSettingClick(bindingAdapterPosition, index) },
                            index == viewItem.viewItems.size - 1
                    )
                }
            }
        }

        companion object {
            const val layout = R.layout.view_holder_derivation_settings
        }
    }
}
