package com.food24.track.ui.shopping

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.R

class AddProductFragment : Fragment(R.layout.sheet_add_product) {

    private val vm: ShoppingListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val inputCategory = view.findViewById<Spinner>(R.id.inputCategorySpinner)

        // создаём адаптер (пока пустой)
        val categories = mutableListOf<String>()
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        inputCategory.setAdapter(adapter)

        // по фокусу/клику — раскрываем дропдаун
        inputCategory.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as AutoCompleteTextView).showDropDown()
        }

        // наблюдаем за данными и обновляем список категорий из текущих элементов
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collect { state ->
                val newCats = state.items
                    .map { it.category.trim() }
                    .filter { it.isNotBlank() }
                    .toSet()                 // уникальные
                    .toList()
                    .sorted()

                // обновляем адаптер, если изменилось
                if (newCats != categories) {
                    categories.clear()
                    categories.addAll(newCats)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}
