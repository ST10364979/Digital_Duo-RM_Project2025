package com.example.rm_project.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rm_project.R
import com.example.rm_project.models.ServiceItem

class ServiceAdapter(
    private val items: List<ServiceItem>
) : RecyclerView.Adapter<ServiceAdapter.ServiceVH>() {

    inner class ServiceVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val btnLearn: Button = view.findViewById(R.id.btnLearnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceVH(v)
    }

    override fun onBindViewHolder(holder: ServiceVH, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        holder.btnLearn.setOnClickListener { item.action.invoke() }
    }

    override fun getItemCount(): Int = items.size
}
