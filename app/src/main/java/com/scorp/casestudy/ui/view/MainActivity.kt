package com.scorp.casestudy.ui.view

import android.os.Bundle
import android.util.ArrayMap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.scorp.casestudy.R
import com.scorp.casestudy.databinding.ActivityMainBinding
import com.scorp.casestudy.ui.adapter.UserAdapter
import com.scorp.casestudy.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var dataSource: DataSource
    private var userList: ArrayMap<Int, Person> = ArrayMap<Int, Person>()
    private lateinit var  fetchCompletionHandler:FetchCompletionHandler
    private var nextPagingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        dataSource = DataSource()
        setupUi()
    }

    private fun setupUi() {
        binding.userSwipeRefresh.isRefreshing = true
        fetchCompletionHandler = object : FetchCompletionHandler {
            override fun invoke(fetchResponse: FetchResponse?, fetchError: FetchError?) {
                if(nextPagingId == null && !userList.isNullOrEmpty()) {
                    userList.clear()
                    userAdapter.notifyDataSetChanged()
                }
                if(fetchResponse?.people.isNullOrEmpty() && fetchError?.errorDescription.isNullOrEmpty()){
                    binding.isRecyclerViewVisible = true //according to business logic userList will set to emptyList
                    nextPagingId = fetchResponse?.next// people list is empty but nextPagingId is not null because of that i changed nextPagingId
                    //6) Pass successful response's `next` identifier into `fetch` method in order to get next "pages".
                    binding.userSwipeRefresh.isRefreshing = false
                    return
                }else if(fetchError?.errorDescription == "Internal Server Error" || fetchError?.errorDescription =="Parameter error"){
                    binding.showErrorMessage = false
                    Snackbar.make(binding.userRecyclerView, getString(R.string.error_message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry)) {
                            fetchData()
                        }
                        .show()
                    binding.userSwipeRefresh.isRefreshing = false
                    return
                }
                fetchResponse?.people?.forEach {
                    if(userList.indexOfKey(it.id)< 0){
                        userList[it.id] = it
                        println("notifyItemInserted ${userList.indexOfKey(it.id)} size = ${userList.size}")
                        userAdapter.notifyItemInserted(userList.indexOfKey(it.id))

                    }else{
                        userList[it.id] = it
                        println("notifyItemChanged")
                        userAdapter.notifyItemChanged(userList.indexOfKey(it.id))
                    }
                }
                nextPagingId = fetchResponse?.next
                binding.userSwipeRefresh.isRefreshing = false
                binding.isRecyclerViewVisible = false
            }

        }
        binding.userSwipeRefresh.setOnRefreshListener {
            nextPagingId = null
            fetchData()
        }
        userAdapter = UserAdapter() { personItem ->

        }
        userAdapter.personModelList = userList
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        fetchData()
                    }
                }
            })
        }
        fetchData()
    }

    private fun fetchData(){
        binding.userSwipeRefresh.isRefreshing = true
        dataSource.fetch(nextPagingId, fetchCompletionHandler)
    }
}