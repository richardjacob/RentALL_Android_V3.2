package com.rentall.radicalstart.ui.profile.currency

import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rentall.radicalstart.GetCurrenciesListQuery
import com.rentall.radicalstart.databinding.ItemLanguageBinding
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.onClick
import java.util.*

class CurrencyAdapter(private val clickListener: (String) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    var items = emptyList<GetCurrenciesListQuery.Result>()
    var selectedItem = String()
    var languageName = String()
    var checkedPosition = -1

    fun setData(data: List<GetCurrenciesListQuery.Result>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLanguageBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CurrencyAdapter.ViewHolder, position: Int) = holder.bind(items[position], position, clickListener)

    inner class ViewHolder(val binding: ItemLanguageBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetCurrenciesListQuery.Result, position: Int, clickListener: (String) -> Unit) {
            binding.text = item.symbol() + " - " + BindingAdapters.getCurrencySymbol(item.symbol())
            if (selectedItem == item.symbol()) {
                binding.radiobtnLanguages.isChecked = true
                checkedPosition = position
            } else {
                binding.radiobtnLanguages.isChecked = false
            }
            binding.radiobtnLanguages.onClick {
                item.symbol()?.let {
                    selectedItem = it
                    notifyItemChanged(checkedPosition)
                    checkedPosition = position
                }
            }
            binding.executePendingBindings()
        }
    }

}
