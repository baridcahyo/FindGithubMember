package com.tiket.findgithubmember.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiket.findgithubmember.R
import com.tiket.findgithubmember.databinding.UserListBinding
import com.tiket.findgithubmember.model.UserModel
import com.tiket.findgithubmember.view.ProfileMember

class UserListAdapter(
    var userList: MutableList<UserModel>,
    var userListFiltered: MutableList<UserModel>
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>(),
    Filterable {

    class ViewHolder(private val itemBinding: UserListBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(data: UserModel) {
            with(itemBinding) {
                Glide.with(itemView.context).load(data.avatarUrl)
                    .fitCenter()
                    .placeholder(R.drawable.ic_github)
                    .into(imagePhoto)

                itemBinding.textFullName.text = data.login
                itemBinding.textURL.text = data.htmlUrl

                /*Click Image...*/
                itemBinding.parentLayout.setOnClickListener {
                    val da = Intent(itemView.context, ProfileMember::class.java)
                    da.putExtra("htmlUrl", data.htmlUrl)
                    startActivity(itemView.context, da, null)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = UserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userListFiltered[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userListFiltered.size
    }

    fun clear() {
        userList.clear()
        userListFiltered.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<UserModel>) {
        userList.addAll(list)
        userListFiltered.addAll(list)
        notifyDataSetChanged()
    }

    fun add(user: UserModel) {
        userList.add(user)
        userListFiltered.add(user)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()

                userListFiltered = if (charString.isEmpty()) {
                    userList
                } else {
                    val filteredList: MutableList<UserModel> = arrayListOf()
                    for (row in userList) {
                        if (row.login!!.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }
                    }

                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = userListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                userListFiltered = filterResults.values as ArrayList<UserModel>
                notifyDataSetChanged()
            }
        }
    }

}