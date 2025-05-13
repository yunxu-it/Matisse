package com.zhihu.matisse.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zhihu.matisse.R

abstract class BaseAdapter<Data> : RecyclerView.Adapter<BaseDBViewHolder>() {

  sealed class Item
  class NormalItem<Data>(val data: Data) : Item()
  data object EmptyItem : Item()

  private val typeNormal = -1
  private val typeEmpty = -2

  private var listData: MutableList<Item>? = null

  protected var mOnItemClickListener: OnItemClickListener<Data>? = null

  fun setOnItemClickListener(listener: OnItemClickListener<Data>) {
    mOnItemClickListener = listener
  }

  interface OnItemClickListener<T> {
    fun onItemClick(view: View, data: T, position: Int)
  }

  override fun getItemViewType(position: Int): Int {
    return if (listData?.get(position) is EmptyItem) {
      typeEmpty
    } else {
      getDataViewType(position)
    }
  }

  /**
   * 用于用户继承实现不同类型的viewType
   */
  open fun getDataViewType(position: Int): Int {
    return typeNormal
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDBViewHolder {
    val mInflater = LayoutInflater.from(parent.context)
    val bindView = if (viewType == typeEmpty) {
      DataBindingUtil.inflate(mInflater, R.layout.layout_empty, parent, false)
    } else {
      DataBindingUtil.inflate<ViewDataBinding>(mInflater, itemLayoutId(viewType), parent, false)
    }
    return BaseDBViewHolder(bindView)
  }

  override fun onBindViewHolder(holder: BaseDBViewHolder, position: Int) {
    val itemData = listData!![position]
    val itemDataBinding = holder.dataBinding
    if (itemData is NormalItem<*>) {
      onBindingItemData(itemDataBinding, itemData.data as Data, position, holder)
    }
  }

  override fun getItemCount(): Int {
    return getListItem().size
  }

  protected abstract fun itemLayoutId(viewType: Int): Int

  protected open fun enableEmptyView(): Boolean {
    return false
  }

  protected open fun onBindingItemData(itemBinding: ViewDataBinding, itemData: Data, position: Int) {

  }

  protected open fun onBindingItemData(itemBinding: ViewDataBinding, itemData: Data, position: Int, holder: BaseDBViewHolder) {
    onBindingItemData(itemBinding, itemData, position)
  }

  fun getListData(): MutableList<Data> {
    return getListItem().filterIsInstance<NormalItem<*>>().map { it.data } as MutableList<Data>
  }

  private fun getListItem(): MutableList<Item> {
    return listData ?: mutableListOf<Item>().also { listData = it }
  }

  private fun addEmptyView(): Boolean {
    if (enableEmptyView()) {
      if (getListItem().isEmpty()) {
        getListItem().add(EmptyItem)
      }
      notifyDataSetChanged()
      return true
    } else {
      return false
    }
  }

  open fun setListData(listData: List<Data>?) {
    getListItem().apply {
      clear()
      addAll((listData ?: listOf()).map { NormalItem(it) })
    }
    if (!addEmptyView()) {
      notifyDataSetChanged()
    }
  }

  fun updateData(index: Int, data: Data) {
    try {
      getListItem()[index] = NormalItem(data)
      notifyItemChanged(index)
    } catch (ignore: Exception) {
    }
  }

  open fun addAllData(listData: List<Data>) {
    val index = getListItem().size
    addAllData(index, listData)
  }

  fun addAllData(index: Int, listData: List<Data>) {
    getListItem().apply {
      addAll(index, listData.map { NormalItem(it) })
    }
    notifyItemRangeChanged(index, listData.size)
  }

  open fun addData(data: Data) {
    val index = getListItem().size
    addData(index, data)
  }

  fun addData(index: Int, data: Data) {
    getListItem().add(index, NormalItem(data))
    notifyItemInserted(index)
  }

  fun deleteData(index: Int) {
    if (listData.isNullOrEmpty() || index > listData!!.size - 1) {
      return
    }
    listData!!.removeAt(index)
    if (!addEmptyView()) {
      notifyItemRemoved(index)
    }
  }
}
