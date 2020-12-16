package com.tiket.findgithubmember.model

import com.google.gson.annotations.SerializedName

data class ResponseUserModel(

    @SerializedName("total_count")
    val totalCount: Int?,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean?,
    val items: List<UserModel>

)