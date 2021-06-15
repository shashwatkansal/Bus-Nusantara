package com.example.busnusantara

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.location_info.view.*


class LocationInfoAdapter(
    private val locationInfo: List<LocationInfo>
) : RecyclerView.Adapter<LocationInfoAdapter.LocationInfoViewHolder>() {
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
                resources.getQuantityString(R.plurals.passenger_count, loc.passengerCount, loc.passengerCount)
        }
    }

    override fun getItemCount(): Int {
        return locationInfo.size
    }
}