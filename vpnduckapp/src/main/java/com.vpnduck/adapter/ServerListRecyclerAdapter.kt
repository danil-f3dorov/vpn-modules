package com.vpnduck.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vpnduck.activity.HomeAppCompatActivity
import com.vpnduck.activity.SelectServerActivity
import com.vpnduck.databinding.ItemServerBinding
import common.util.validate.ValidateUtil.validateIfCityExist
import core.domain.model.Server
import core.domain.usecase.GetServerListUseCase
import core.util.parse.ParseFlag.findFlagForServer
import core.util.parse.ParseSpeed.convertSpeedForAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerListRecyclerAdapter(
    private val activity: SelectServerActivity,
    private val getServerListUseCase: GetServerListUseCase
) : RecyclerView.Adapter<ServerListRecyclerAdapter.ServerVH>(), SearchView.OnQueryTextListener {

    private var serverList: List<Server> = emptyList()
    private var filteredList: List<Server> = emptyList()

    init {
        activity.lifecycleScope.launch(Dispatchers.Default) {
            serverList = getServerListUseCase()
            filteredList = serverList
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerVH {
        val itemBinding: ItemServerBinding = ItemServerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServerVH(itemBinding, activity)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ServerVH, position: Int) {
        holder.bind(filteredList[position])
    }

    class ServerVH(
        private val itemBinding: ItemServerBinding, private val activity: SelectServerActivity
    ) : ViewHolder(itemBinding.root) {

        fun bind(server: Server) = with(itemBinding) {
            tvCityName.text = validateIfCityExist(server.country, server.city)
            tvIpAddressNotify.text = server.ip
            tvSpeed.text = convertSpeedForAdapter(server)
            ivCountryIcon.setImageDrawable(
                AppCompatResources.getDrawable(root.context, findFlagForServer(server))
            )
            root.setOnClickListener {
                val intent = Intent(itemView.context, HomeAppCompatActivity::class.java)
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
                server.country.contains(text, ignoreCase = true) || server.city.contains(
                    text, ignoreCase = true
                )
            }
            notifyDataSetChanged()
        }
        return true
    }
}