package com.food24.track.ui.shopping

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.food24.track.App
import com.food24.track.R
import com.food24.track.databinding.FragmentShoppingListBinding
import com.food24.track.databinding.SheetAddProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListFragment : Fragment() {

    private var _b: FragmentShoppingListBinding? = null
    private val b get() = _b!!

    private val expanded = mutableSetOf<String>()
    private var lastState: ShoppingUiState = ShoppingUiState()

    private val vm: ShoppingListViewModel by viewModels {
        val app = requireActivity().application as App
        ShoppingListViewModelFactory(app.db.shoppingDao())
    }

    private val adapter = ShoppingSectionAdapter(
        onToggle = { id, checked -> vm.toggle(id, checked) },
        onHeaderClick = { pos -> toggleHeader(pos) }
    )

    private var currentRows: List<Row> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentShoppingListBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.forceReseed()
        vm.normalizeIfAllSameName()

        b.recyclerCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ShoppingListFragment.adapter
            val space = (resources.displayMetrics.density * 10).toInt()
            addItemDecoration(CategorySpacingDecoration(space))
        }

        b.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        b.actionClearChecked.setOnClickListener { vm.clearChecked() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                lastState = st
                b.placeholder.isVisible = st.empty
                b.actionClearChecked.isEnabled = st.hasChecked
                currentRows = buildRows(st)
                adapter.submit(currentRows)
            }
        }

        b.btnAddProduct.setOnClickListener { showAddProductSheet() }
    }

    /** группируем элементы во «вложенные» ряды */
    private fun buildRows(st: ShoppingUiState): List<Row> {
        val catOrder = listOf(
            "Meat & Fish" to R.drawable.ic_cat_meat,
            "Vegetables & Fruits" to R.drawable.ic_cat_veggies,
            "Grains & Carbs" to R.drawable.ic_cat_grains,
            "Oils & Condiments" to R.drawable.ic_cat_oils,
            "Dairy & Eggs" to R.drawable.ic_cat_dairy,
            "Others" to R.drawable.ic_cat_other
        )
        val grouped = st.items.groupBy { it.category.ifBlank { "Others" } }
        val rows = mutableListOf<Row>()
        catOrder.forEach { (title, icon) ->
            val isExpanded = expanded.contains(title)
            rows += Row.Category(title, icon, isExpanded)
            if (isExpanded) {
                grouped[title].orEmpty().forEach { it ->
                    rows += Row.Item(it.id, it.name, it.amount, it.category, it.checked)
                }
            }
        }
        return rows
    }

    private fun toggleHeader(position: Int) {
        val row = currentRows.getOrNull(position) as? Row.Category ?: return
        if (expanded.contains(row.title)) expanded.remove(row.title) else expanded.add(row.title)
        currentRows = buildRows(lastState)
        adapter.submit(currentRows)
    }

    private fun showAddProductSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = SheetAddProductBinding.inflate(layoutInflater)

        // 1) заполняем spinner категориями из единого источника
        val catAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_selected_item,     // layout закрытого состояния (RelativeLayout)
            R.id.spinnerText,                   // <-- ВАЖНО: ID TextView внутри layout’а
            Categories.all
        )
        catAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        sheetBinding.inputCategorySpinner.adapter = catAdapter

        // 2) обработка кнопки Add
        sheetBinding.btnAdd.setOnClickListener {
            val name = sheetBinding.inputName.text?.toString().orEmpty()
            val qty  = sheetBinding.inputQty.text?.toString().orEmpty()
            val cat  = (sheetBinding.inputCategorySpinner.selectedItem as? String).orEmpty()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Enter product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.add(name, qty, cat)   // ViewModel нормализует категорию сам
            dialog.dismiss()
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
