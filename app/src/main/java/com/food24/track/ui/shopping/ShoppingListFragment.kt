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
import com.food24.track.databinding.FragmentShoppingListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListFragment : Fragment() {

    private var _b: FragmentShoppingListBinding? = null
    private val b get() = _b!!

    private val vm: ShoppingListViewModel by viewModels {
        val app = requireActivity().application as App
        ShoppingListViewModelFactory(app.db.shoppingDao())
    }

    private val adapter = ShoppingAdapter { id, checked ->
        vm.toggle(id, checked)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentShoppingListBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // список
        b.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ShoppingListFragment.adapter
        }

        // категории (простые)
        val categories = listOf("Meat & Fish", "Vegetables & Fruits", "Grains & Carbs",
            "Oils & Condiments", "Dairy & Eggs", "Others")
        b.inputCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        )

        // add product
        b.btnAddProduct.setOnClickListener {
            val name = b.inputName.text?.toString().orEmpty()
            val qty  = b.inputQty.text?.toString().orEmpty()
            val cat  = b.inputCategory.text?.toString().orEmpty()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Enter product name", Toast.LENGTH_SHORT).show()
            } else {
                vm.add(name, qty, cat)   // <- теперь сигнатура совпадает
                b.inputName.text?.clear()
                b.inputQty.text?.clear()
                b.inputCategory.text?.clear()
            }
        }


        // clear checked
        b.actionClearChecked.setOnClickListener { vm.clearChecked() }
        // reset all
        b.btnReset.setOnClickListener { vm.resetAll() }

        // observe UI
        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                adapter.submit(st.items)
                b.actionClearChecked.isEnabled = st.hasChecked
                b.placeholder.visibility = if (st.empty) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
