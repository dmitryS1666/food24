package com.food24.track.ui.shopping

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
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
        b.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerCategories.adapter = adapter

        // back
        b.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // clear checked
        b.actionClearChecked.setOnClickListener { vm.clearChecked() }

        // observe UI
        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                b.placeholder.visibility = if (st.empty) View.VISIBLE else View.GONE
                b.actionClearChecked.isEnabled = st.hasChecked
                currentRows = buildRows(st)
                adapter.submit(currentRows)
            }
        }

        // add product -> show bottom sheet
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
        val result = mutableListOf<Row>()

        catOrder.forEach { (title, icon) ->
            val expanded = true // можно хранить и восстанавливать из savedState
            result += Row.Category(title, icon, expanded)
            if (expanded) {
                grouped[title].orEmpty().forEach { it ->
                    result += Row.Item(
                        id = it.id, name = it.name, amount = it.amount,
                        category = it.category, checked = it.checked
                    )
                }
            }
        }
        return result
    }

    private fun toggleHeader(position: Int) {
        // простейший переключатель: меняем флаг и пересобираем список
        val mutable = currentRows.toMutableList()
        val header = mutable[position] as? Row.Category ?: return
        header.expanded = !header.expanded
        // пересоберём список: упростим — только меняем и перерисовываем
        adapter.notifyItemChanged(position)
    }

    private fun showAddProductSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = SheetAddProductBinding.inflate(layoutInflater)

        val categories = listOf(
            "Meat & Fish", "Vegetables & Fruits", "Grains & Carbs",
            "Oils & Condiments", "Dairy & Eggs", "Others"
        )
        sheetBinding.inputCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        )

        sheetBinding.btnAdd.setOnClickListener {
            val name = sheetBinding.inputName.text?.toString().orEmpty()
            val qty  = sheetBinding.inputQty.text?.toString().orEmpty()
            val cat  = sheetBinding.inputCategory.text?.toString().orEmpty()
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Enter product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.add(name, qty, cat)
            dialog.dismiss()
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
