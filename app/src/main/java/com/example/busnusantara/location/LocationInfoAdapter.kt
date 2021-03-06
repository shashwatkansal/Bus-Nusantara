package com.example.busnusantara.location

import android.graphics.Color
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.busnusantara.R
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
            if (loc.passengerCount == 0) {
                tvLocation.paintFlags = tvLocation.paintFlags or STRIKE_THRU_TEXT_FLAG
                tvLocation.setTextColor(Color.GRAY)
                tvPassengerCount.setTextColor(Color.GRAY)
                tvPassengerCount.text =
                    resources.getQuantityString(
                        R.plurals.passenger_count,
                        loc.passengerCount,
                        loc.passengerCount
                    )
            } else if (locationInfo[position].passengerCount == -1) {
                tvPassengerCount.text = resources.getString(R.string.destination_point)
            } else {
                tvPassengerCount.text =
                    resources.getQuantityString(
                        R.plurals.passenger_count,
                        loc.passengerCount,
                        loc.passengerCount
                    )
            }
        }
    }

    override fun getItemCount(): Int {
        return locationInfo.size
    }
}