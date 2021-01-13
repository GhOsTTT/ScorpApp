package com.scorp.casestudy.ui.view

import android.os.Bundle
import android.util.ArrayMap
import androidx.appcompat.app.AppCompatActivity
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
    private var nextPagingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        dataSource = DataSource()
        setupUi()
        binding.isRecyclerViewVisible = true
    }

    private fun setupUi() {
        binding.userSwipeRefresh.isRefreshing = true
        val fetchCompletionHandler = object : FetchCompletionHandler {
            override fun invoke(p1: FetchResponse?, p2: FetchError?) {
                if(nextPagingId == null && !userList.isNullOrEmpty())
                    userList = ArrayMap<Int, Person>()
                if(p1?.people.isNullOrEmpty() && p2?.errorDescription.isNullOrEmpty()){
                    binding.isRecyclerViewVisible = false //according to business logic userList will set to emptyList
                    nextPagingId = p1?.next// people list is empty but nextPagingId is not null because of that i changed nextPagingId
                    //6) Pass successful response's `next` identifier into `fetch` method in order to get next "pages".
                    binding.userSwipeRefresh.isRefreshing = false
                    return
                }else if(p2?.errorDescription == "Internal Server Error" || p2?.errorDescription =="Parameter error"){
                    binding.showErrorMessage = false
                    Snackbar.make(binding.userRecyclerView, getString(R.string.error_message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry)) {
                            dataSource.fetch(nextPagingId, this)
                        }
                        .show()
                    binding.userSwipeRefresh.isRefreshing = false
                    return
                }/*else if(p2?.errorDescription =="Parameter error"){
                    binding.isRecyclerViewVisible = false

                    return
                }*/
                p1?.people?.forEach {
                   // println("Person Fetched = name = ${it.fullName} id =  ${it.id}")
                    userList[it.id] = it
                }
                println("Person Fetched = PeopleSize = ${p1?.people?.size ?: -1 } next =  ${p1?.next} error = ${p2?.errorDescription}")
                nextPagingId = p1?.next
                userAdapter.personModelList = userList
                userAdapter.notifyDataSetChanged()
                binding.userSwipeRefresh.isRefreshing = false
                binding.isRecyclerViewVisible = true
            }

        }
        binding.userSwipeRefresh.setOnRefreshListener {
            nextPagingId = null
            dataSource.fetch(nextPagingId, fetchCompletionHandler)
        }
        userAdapter = UserAdapter() { personItem ->

        }

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.userSwipeRefresh.isRefreshing = true
                        dataSource.fetch(nextPagingId, fetchCompletionHandler)
                    }
                }
            })
        }
        dataSource.fetch(nextPagingId, fetchCompletionHandler)
    }
}