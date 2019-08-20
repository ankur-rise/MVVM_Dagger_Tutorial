package com.llm.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.llm.R
import com.llm.data.models.DeliveryItemDataModel
import com.llm.di.Injector
import com.llm.ui.adapter.DeliveryAdapter
import com.llm.ui.viewmodels.DeliveryItemsViewModel
import com.llm.ui.viewmodels.factory.ViewModelFactory
import javax.inject.Inject

class DeliveryListActivity : AppCompatActivity() {

    @Inject // cannot be private as dagger required this variable to access
    lateinit var factory: ViewModelFactory

    private val viewModel: DeliveryItemsViewModel by viewModels { factory }
    private lateinit var recyclerView: RecyclerView
    lateinit var refreshLayout: SwipeRefreshLayout
    private var adapter = DeliveryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_delivery_list)

        val component = Injector.inject()
        component.inject(this)
        recyclerView = findViewById(R.id.rl)
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(decoration)

        refreshLayout = findViewById(R.id.swipe_refresh)
        refreshLayout.setOnRefreshListener { refreshData() }


    }

    private fun refreshData() {
        viewModel.refreshData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.resultLiveData.observe(this, Observer<PagedList<DeliveryItemDataModel>> {
            Log.d("DeliveryListActivity", "list: ${it?.size}")
            if (refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = false
            }
            adapter.submitList(it)
        })

        viewModel.errLiveData.observe(this, Observer {

            Toast.makeText(this, it, Toast.LENGTH_LONG).show()

        })

        viewModel.errRefreshLiveData.observe(this, object :Observer<String>{
            override fun onChanged(str: String?) {
                if (refreshLayout.isRefreshing) {
                    refreshLayout.isRefreshing = false
                }
                Toast.makeText(this@DeliveryListActivity, str, Toast.LENGTH_LONG).show()
            }

        })

        viewModel.loadUser()

        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.refresh_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_refresh -> {
                refreshLayout.isRefreshing = true
                refreshData()
                return true
            }
        }

        return super.onOptionsItemSelected(item)

    }


}