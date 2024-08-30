package com.donkeyvpn.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.donkeyvpn.databinding.ItemServerBinding
import com.donkeyvpn.screens.home.HomeActivity
import com.donkeyvpn.screens.select_server.SelectServerActivity
import common.util.parse.ParseFlag.findFlagForServer
import common.util.parse.ParseSpeed.convertSpeedForAdapter
import common.util.validate.ValidateUtil.validateIfCityExist
import data.room.db.VpnDatabase
import data.room.entity.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerListRecyclerAdapter(private val activity: SelectServerActivity) :
    RecyclerView.Adapter<ServerListRecyclerAdapter.ServerVH>(), SearchView.OnQueryTextListener {

    private var serverList: List<Server> = emptyList()
    private var filteredList: List<Server> = emptyList()
    private var dao = VpnDatabase.getDataBase(activity).getServerDao()

    init {
        activity.lifecycleScope.launch(Dispatchers.Default) {
            serverList = dao.getServerList()
            filteredList = serverList
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerVH {
        val itemBinding: ItemServerBinding = ItemServerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServerVH(itemBinding, activity)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ServerVH, position: Int) {
        holder.bind(filteredList[position])
    }

    class ServerVH(private val itemBinding: ItemServerBinding, private val activity: SelectServerActivity) :
        ViewHolder(itemBinding.root) {

        fun bind(server: Server) = with(itemBinding) {
            tvCityName.text = validateIfCityExist(server.country, server.city)
            tvIpAddressNotify.text = server.ip
            tvSpeed.text = convertSpeedForAdapter(server)
            ivCountryIcon.setImageDrawable(AppCompatResources.getDrawable
                (root.context, findFlagForServer(server)))
            root.setOnClickListener {
                val intent = Intent(itemView.context, HomeActivity::class.java)
                intent.putExtra(Server::class.java.canonicalName, server)
                activity.requestPermissionLauncher.launch(intent)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { text ->
            filteredList = serverList.filter { server ->
                server.country.contains(text, ignoreCase = true) || server.city.contains(text, ignoreCase = true)
            }
            notifyDataSetChanged()
        }
        return true
    }
}