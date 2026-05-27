package com.example.securedb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(private var categories: List<Category>, private val onItemClick: (Category) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val nameText: TextView = view.findViewById(R.id.itemTitle)
        val icon: ImageView = view.findViewById(R.id.itemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameText.text = category.name
        holder.icon.setImageResource(android.R.drawable.ic_menu_directions)
        
        ThemeManager.applyThemeToCard(holder.card)
        ThemeManager.applyFontToView(holder.nameText)
        holder.icon.setColorFilter(ThemeManager.getBtnColor(holder.itemView.context))
        
        holder.itemView.setOnClickListener { onItemClick(category) }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}

class EntryAdapter(private var entries: List<EntryModel>, private val onItemClick: (EntryModel) -> Unit) :
    RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val titleText: TextView = view.findViewById(R.id.itemTitle)
        val passwordText: TextView = view.findViewById(R.id.itemPassword)
        val icon: ImageView = view.findViewById(R.id.itemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.titleText.text = entry.title
        holder.passwordText.text = "••••••••"
        
        val iconRes = when (entry.iconId) {
            1 -> android.R.drawable.ic_dialog_email
            2 -> android.R.drawable.ic_menu_camera
            3 -> android.R.drawable.ic_menu_compass
            4 -> android.R.drawable.ic_menu_agenda
            else -> android.R.drawable.ic_lock_lock
        }
        holder.icon.setImageResource(iconRes)
        
        ThemeManager.applyThemeToCard(holder.card)
        ThemeManager.applyFontToView(holder.titleText)
        ThemeManager.applyFontToView(holder.passwordText)
        holder.icon.setColorFilter(ThemeManager.getBtnColor(holder.itemView.context))

        holder.itemView.setOnClickListener { onItemClick(entry) }
    }

    override fun getItemCount() = entries.size

    fun updateData(newEntries: List<EntryModel>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
