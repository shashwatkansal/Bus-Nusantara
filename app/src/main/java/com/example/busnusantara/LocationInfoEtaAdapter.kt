package com.example.busnusantara

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.location_info.view.*
import java.text.SimpleDateFormat
import java.util.*


class LocationInfoEtaAdapter(
    private val locationInfo: List<LocationInfo>
) : RecyclerView.Adapter<LocationInfoEtaAdapter.LocationInfoViewHolder>() {
    class LocationInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationInfoViewHolder {
        return LocationInfoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.location_info,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationInfoViewHolder, position: Int) {
        holder.itemView.apply {
            val loc = locationInfo[position]
            tvLocation.text = loc.locationName
            tvPassengerCount.text =
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(loc.eta)
        }
    }

    override fun getItemCount(): Int {
        return locationInfo.size
    }
}