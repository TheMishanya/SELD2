package com.example.seld.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seld.R
import com.example.seld.databinding.FragmentHomeBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.util.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.*
import kotlinx.android.synthetic.main.changedevice.view.*
import kotlinx.android.synthetic.main.changedevice.view.nobutton
import kotlinx.android.synthetic.main.deletedevice.view.*
import kotlinx.android.synthetic.main.deletedevice.view.text1
import kotlinx.android.synthetic.main.infodevice.view.*
import kotlinx.android.synthetic.main.settings.view.*


class HomeFragment : Fragment() {

    private var timer: Timer? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val key = "key"
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if(sharedPreferences.contains(key) &&  sharedPreferences.getString("newtoken", "default_value").toString()=="1") {
                    add()
                }
            }
        }, 0, 1000)
        return root
    }
    var previousData: String = ""
    //вывод всех устройств
    @SuppressLint("SuspiciousIndentation")
    fun add(){
        val recyclerView = binding.devices
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val newData = getres()
        if((previousData!=newData && newData.contains("status")) ||(previousData!=newData &&newData=="[]")) {
            println("2222222222222222222222")
            var i = 1
            if (newData == "0000") {
            } else {
                val jsonArray = JSONArray(newData)
                println(newData)
                i = jsonArray.length()
                println(i)
                val itemList = mutableListOf<Item>()
                for (j in 0..i - 1) {
                    val jsonObject = jsonArray.getJSONObject(j)
                    val device_name = jsonObject.getString("device_name")
                    val status = jsonObject.getString("status")
                    var imagee: Int = 0
                    var statuss = ""
                    if (status == "Open") {
                        statuss = "Замок открыт!"
                        imagee = R.drawable.open1
                    }
                    if (status == "Close") {
                        statuss = "Замок закрыт!"
                        imagee = R.drawable.close1
                    }
                    val item = Item(
                        R.drawable.devise,
                        device_name,
                        "Умный электронный замок SELD",
                        statuss,
                        imagee
                    )
                    itemList.add(item)
                }
                requireActivity().runOnUiThread {
                    val adapter = RecyclerViewAdapter(itemList, this)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
        previousData=newData
    }
    //получение устройств для их вывода
    fun getres(): String = runBlocking {
        return@runBlocking withContext(Dispatchers.IO) {
            var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
            var Token = sharedPreferences.getString("key", "default_value").toString()
            Token="Token $Token"
            val apiUrl = "https://seld-lock.ru/api/v1.0/devices/"
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", Token)
                .build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    return@withContext response.body?.string() ?: "0000"
                } else {
                    return@withContext "0000"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext "0000"
            }
        }
    }
    //вывод устройств
    data class Item(val imageResId: Int, val text: String, val text1: String,val status: String,val imageRes: Int)
    class RecyclerViewAdapter(
        private val itemList: List<Item>,
        private val externalFunctionCaller: HomeFragment
    ) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = itemList[position]
            holder.bind(item)
        }
        override fun getItemCount(): Int {
            return itemList.size
        }
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val imageViewopenclose: ImageView = itemView.findViewById(R.id.imageView5)
            private val textView: TextView = itemView.findViewById(R.id.text_list_item)
            private val textView1: TextView = itemView.findViewById(R.id.tvDescription)
            private val textView2: TextView = itemView.findViewById(R.id.text_list_item2)
            init {
                itemView.setOnClickListener(this)
            }
            fun bind(item: Item) {
                imageView.setImageResource(item.imageResId)
                textView.text = item.text
                textView1.text = item.text1
                textView2.text = item.status
                imageViewopenclose.setImageResource(item.imageRes)
                itemView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        externalFunctionCaller.callLongPressFunction(position)
                    }
                    true
                }
            }
            override fun onClick(view: View?) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    externalFunctionCaller.callExternalFunction(position)
                }
            }
        }
    }
    //обработка быстрого нажатия на устройство
    private fun callExternalFunction(position: Int) {
        val newData = getres()
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        var Token = sharedPreferences.getString("key", "default_value").toString()
        Token="Token $Token"
        val jsonArray = JSONArray(newData)
        val jsonObject = jsonArray.getJSONObject(position)
        val serial_num = jsonObject.getString("serial_num")
        val status = jsonObject.getString("status")
        val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
        val client = OkHttpClient()
        var status1=""
        if(status=="Open") {
            status1="Close"
            requireActivity().runOnUiThread {
                val text = "Подождите, замок закрывается..."
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(requireContext(), text, duration)
                toast.show()
            }
        }
        else if(status=="Close") {
            status1="Open"
            requireActivity().runOnUiThread {
                val text = "Подождите, замок открывается..."
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(requireContext(), text, duration)
                toast.show()
            }
        }
        val requestBody = RequestBody.create( "application/json"?.toMediaTypeOrNull(), "{\"status\":\"$status1\"}")
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", Token)
            .patch(requestBody)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println(responseData)
            }
        })
    }
    //обработка долгого нажатия на устройство
    private fun callLongPressFunction(position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.infodevice, null)
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        println(position)
        var sharedPreferences =
            requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        var newData = getres()
        val jsonArray = JSONArray(newData)
        val jsonObject = jsonArray.getJSONObject(position)
        var device_name = jsonObject.getString("device_name")
        var serial_num = jsonObject.getString("serial_num")
        var adminn = jsonObject.getString("admin")
        val text1 = view.text1
        val textserialnum = view.textserialnum
        val text2 = view.text2
        val textdevicename = view.textdevicename
        val registerButton = view.registerButton
        val no = view.nobutton1
        val deletedevice = view.deletedevice
        val setting = view.setting
        requireActivity().runOnUiThread {
            text1.text = "Серийный номер:"
            textserialnum.text = serial_num
            text2.text = "Название устройства:"
            textdevicename.text = device_name
        }
        no.setOnClickListener {
            dialog.dismiss()
        }
        registerButton.setOnClickListener {
            val view = inflater.inflate(R.layout.changedevice, null)
            dialogBuilder.setView(view)
            val dialog1 = dialogBuilder.create()
            dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog1.show()
            val changeButton = view.changeButton
            val nobutton = view.nobutton
            val newdevicename = view.newdevicename
            nobutton.setOnClickListener {
                dialog1.dismiss()
            }
            changeButton.setOnClickListener {
                val newname = newdevicename.text.toString()
                if (newname.equals("")) {
                    requireActivity().runOnUiThread {
                        val text = "Введите новое название!"
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(requireContext(), text, duration)
                        toast.show()
                    }
                } else {
                    var Token = sharedPreferences.getString("key", "default_value").toString()
                    val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
                    val client = OkHttpClient()
                    Token = "Token $Token"
                    val requestBody = RequestBody.create(
                        "application/json"?.toMediaTypeOrNull(),
                        "{\"device_name\":\"$newname\"}"
                    )
                    println(requestBody)
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", Token)
                        .patch(requestBody)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseData = response.body?.string()
                            println(responseData)
                        }
                    })
                    dialog1.dismiss()
                    dialog.dismiss()
                }
            }
        }
        deletedevice.setOnClickListener {
            val view = inflater.inflate(R.layout.deletedevice, null)
            dialogBuilder.setView(view)
            val dialog2 = dialogBuilder.create()
            dialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog2.show()
            val okButton = view.okButton
            val nonobutton = view.nonobutton
            nonobutton.setOnClickListener {
                dialog2.dismiss()
            }
            okButton.setOnClickListener {
                var Token = sharedPreferences.getString("key", "default_value").toString()
                val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
                val client = OkHttpClient()
                Token = "Token $Token"
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", Token)
                    .delete()
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            requireActivity().runOnUiThread {
                                val text = "Вы успешно удалили устройство"
                                val duration = Toast.LENGTH_SHORT
                                val toast = Toast.makeText(requireContext(), text, duration)
                                toast.show()
                            }
                        } else {
                            println("DELETE request failed")
                        }
                        response.close()
                    }
                })
                dialog2.dismiss()
                dialog.dismiss()
            }
        }
        setting.setOnClickListener {
            val view = inflater.inflate(R.layout.settings, null)
            dialogBuilder.setView(view)
            val dialogset = dialogBuilder.create()
            dialogset.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogset.show()
            val admin = view.admin
            if (adminn == "On") {
                admin.isChecked = true
            }
            if (adminn == "Off") {
                admin.isChecked = false
            }
            val nobuttonset = view.nobuttonset
            val registerButtonset =
                view.registerButtonset//dfdfhdfhhdfhsxdhsththghgfgfgfgfhgfhfgdhgfh
            nobuttonset.setOnClickListener {
                dialogset.dismiss()
            }
            admin.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    var Token = sharedPreferences.getString("key", "default_value").toString()
                    val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
                    val client = OkHttpClient()
                    Token = "Token $Token"
                    val requestBody = RequestBody.create(
                        "application/json"?.toMediaTypeOrNull(),
                        "{\"admin\":\"On\"}"
                    )
                    println(requestBody)
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", Token)
                        .patch(requestBody)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseData = response.body?.string()
                            println(responseData)
                        }
                    })
                } else {
                    var Token = sharedPreferences.getString("key", "default_value").toString()
                    val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
                    val client = OkHttpClient()
                    Token = "Token $Token"
                    val requestBody = RequestBody.create(
                        "application/json"?.toMediaTypeOrNull(),
                        "{\"admin\":\"Off\"}"
                    )
                    println(requestBody)
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", Token)
                        .patch(requestBody)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseData = response.body?.string()
                            println(responseData)
                        }
                    })
                }
                newData = getres()
                val jsonArray = JSONArray(newData)
                val jsonObject = jsonArray.getJSONObject(position)
                device_name = jsonObject.getString("device_name")
                serial_num = jsonObject.getString("serial_num")
                adminn = jsonObject.getString("admin")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timer?.cancel()
    }

}