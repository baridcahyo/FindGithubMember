package com.tiket.findgithubmember.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiket.findgithubmember.adapter.UserListAdapter
import com.tiket.findgithubmember.databinding.FragmentSearchBinding
import com.tiket.findgithubmember.helper.RecycleScrollListener
import com.tiket.findgithubmember.model.UserModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.tiket.findgithubmember.api.RequestAPI as Api

open class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    open var disposable: Disposable? = null
    private val user by lazy { Api.create() }
    private val delay: Long = 1200
    private val perPage: Int = 30
    private var adapter: UserListAdapter? = null
    private var totalPage: Int = 0
    private var currentPage: Int = 1
    private var nextPage: Boolean = false
    private var isSearch: Boolean = false
    private var queryCurrent: String = ""
    private var querySearched: Int = 0
    private var lastTextChanged: Long = 0

    @SuppressLint("WrongConstant")
    private var userLinearLayout = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.rvMember.layoutManager = userLinearLayout
        setListener()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListener() {
        binding.rvMember.addOnScrollListener(object : RecycleScrollListener(userLinearLayout) {
            override fun onLoadMore(current_page: Int) {
                if (nextPage) {
                    currentPage++
                    doSearch(queryCurrent, currentPage, perPage)
                }
            }
        })

        onTextChange()
    }

    private fun doInitList(data: List<UserModel>, q: String) {
        binding.errorLayout.visibility = View.GONE

        if (adapter == null) {
            adapter = UserListAdapter(data.toMutableList(), data.toMutableList())
            binding.rvMember.adapter = adapter
        } else {
            if (queryCurrent != q || (data.count() == 1 && currentPage == 1))
                adapter!!.clear()

            adapter!!.addAll(data)
            adapter!!.notifyItemRangeChanged(0, adapter!!.itemCount)
        }
    }

    private fun doClearList() {
        if (adapter != null) {
            binding.errorLayout.visibility = View.GONE
            queryCurrent = ""
            totalPage = 0
            currentPage = 1
            adapter!!.clear()
            adapter!!.notifyItemRangeChanged(0, 0)
        }
    }

    private fun doSearch(q: String, p: Int, pP: Int) {
        binding.textSearch.isEnabled = true
        if (!isSearch) {
            loading(true)
            disposable = user.getSearchUser(q, p, pP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        loading(false)
                        val count = result.items.count()
                        if (count > 0) {
                            totalPage += count
                            doInitList(result.items, q)
                            nextPage =
                                (result.totalCount!! > totalPage && result.totalCount > perPage)

                        } else {
                            doClearList()
                            doInitNotFound("404", "Data Not Found!")
                        }
                    },
                    {
                        loading(false)
                        doClearList()
                        doInitNotFound("403", "API rate limit")

                        Handler().postDelayed(
                            { doSearch(binding.textSearch.text.toString(), currentPage, perPage) },
                            40000
                        )

                        doStartTick()
                    }
                )
            queryCurrent = q
        }
    }

    private val handlerSearch = Runnable {
        if (System.currentTimeMillis() > lastTextChanged + delay - 500 && binding.textSearch.text.toString()
                .isNotEmpty()
        ) {
            doClearList()
            doSearch(binding.textSearch.text.toString(), currentPage, perPage)
        }
    }

    private fun onTextChange() {
        print("respond text change")
        binding.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty())
                    doClearList()
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    if (querySearched > binding.textSearch.text.toString().length)
                        view?.let { doSearchTick(it.handler, false) }
                    else {
                        if (adapter != null && totalPage > 1) {
                            if (!nextPage)
                                adapter?.filter?.filter(binding.textSearch.text.toString())
                            else
                                view?.let { doSearchTick(it.handler, true) }
                        } else {
                            view?.let { doSearchTick(it.handler, false) }
                        }
                    }
                } else {
                    doClearList()
                }

                querySearched = binding.textSearch.text.toString().length
            }
        })
    }

    private fun doSearchTick(handler: Handler, filtered: Boolean) {
        if (adapter != null && adapter?.itemCount!! > 0 && filtered) {
            adapter?.filter?.filter(binding.textSearch.text.toString())
            if (adapter?.itemCount!! == 0) {
                doClearList()
                doInitNotFound("404", "Data Not Found")
            }
        } else {
            lastTextChanged = System.currentTimeMillis()
            handler.postDelayed(handlerSearch, delay)
        }
    }

    private fun doStartTick() {
        binding.textSearch.isEnabled = false
        Handler().postDelayed({
            val count = Integer.parseInt(binding.textCode.text.toString()) - 1
            binding.textCode.text = count.toString()
            if (count > 0)
                doStartTick()
        }, 1000)
    }

    private fun loading(b: Boolean) {
        binding.errorLayout.visibility = View.GONE
        isSearch = b

        if (b)
            binding.loadingLayout.visibility = View.VISIBLE
        else
            binding.loadingLayout.visibility = View.GONE
    }

    private fun doInitNotFound(c: String, m: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.textCode.text = c
        binding.textDescription.text = m
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

}