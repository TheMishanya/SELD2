package com.example.seld

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.seld.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.about.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.android.synthetic.main.forregistr.view.*
import kotlinx.android.synthetic.main.forregistr.view.noobutton
import kotlinx.android.synthetic.main.forregistr.view.yesButton
import kotlinx.android.synthetic.main.forregistr2.view.*
import kotlinx.android.synthetic.main.registrationback.view.*
import kotlinx.android.synthetic.main.registrstion.view.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHeader = nav_view.getHeaderView(0)
        val button1 = navHeader.findViewById<Button>(R.id.button)
        val button2 = navHeader.findViewById<Button>(R.id.button2)
        val nickname = navHeader.findViewById<TextView>(R.id.textnickname)
        var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        var username = sharedPreferences.getString("username", "default_value").toString()
        nickname.text = "Логин: $username"
        button1.setOnClickListener {
            regorlogin("0")
            var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
            var username = sharedPreferences.getString("username", "default_value").toString()
        }
        button2.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.about, null)
            dialogBuilder.setView(view)
            val dialog3 = dialogBuilder.create()
            dialog3.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog3.show()
            val nobutton5 = view.nobutton5
            nobutton5.setOnClickListener {
                dialog3.dismiss()
            }
        }
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        //вход или регистрация
        if (!sharedPreferences.contains("key")) {
            regorlogin("1")
            var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
            var username = sharedPreferences.getString("username", "default_value").toString()
            nickname.text = "Логин: $username"
        }
        //получение токена каждый вход
        else {
            //удаление токена
            var Token = sharedPreferences.getString("key", "default_value").toString()
            Token = "Token $Token"
            val client = OkHttpClient()
            GlobalScope.launch(Dispatchers.IO) {
                val requestBody = """
            {
            }
            """.trimIndent()
                try {
                    println(requestBody)
                    val request = Request.Builder()
                        .url("https://seld-lock.ru/auth/token/logout/")
                        .post(
                            RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                requestBody
                            )
                        )
                        .addHeader("Authorization", Token)
                        .build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    response.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        val text = "Отсутствует подключение к интернету"
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(applicationContext, text, duration)
                        toast.show()
                    }
                }
            }
            //получение нового токена
            gettoken()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    fun gettoken(){
        var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "default_value").toString()
        val password = sharedPreferences.getString("password", "default_value").toString()
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val requestBody = """
                {
                "username": "$username",
                "password": "$password"
                }
                """.trimIndent()
            try {
                val request = Request.Builder()
                    .url("https://seld-lock.ru/auth/token/login")
                    .post(
                        RequestBody.create(
                            "application/json".toMediaTypeOrNull(),
                            requestBody
                        )
                    )
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                response.close()
                val jsonString = responseBody
                if (jsonString != null) {
                    val jsonObject = JSONObject(responseBody)
                    val token = jsonObject.getString("auth_token")
                    val editor = sharedPreferences.edit()
                    editor.putString("key", token)
                    editor.putString("newtoken", "1")
                    editor.apply()
                    println(token)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
                regorlogin("1")
            }
        }
    }
    fun regorlogin(set: String): String {
        var username=""
        var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.forregistr2, null)
        if(set=="1"){
            dialogBuilder.setCancelable(false)
        }
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val yesbutton = view.yesButton
        val noobutton = view.noobutton
        val nonoobutton = view.nonobutton1
        nonoobutton.setOnClickListener {
            dialog.dismiss()
        }
        //вход в акк
        yesbutton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.registrationback, null)
            if(set=="1"){
                dialogBuilder.setCancelable(false)
            }
            dialogBuilder.setView(view)
            val dialog1 = dialogBuilder.create()
            dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog1.show()
            val usernameEditText = view.Loginin
            val passwordEditText = view.Passwordin
            val registerButton = view.registerButtonin
            val nobutton = view.nobuttonin
            val texterror = view.texterrorin
            nobutton.setOnClickListener {
                dialog1.dismiss()
            }
            registerButton.setOnClickListener {
                username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                if (username.equals("") || password.equals("")) {
                    texterror.text = "Введите логин и пароль"
                } else {
                    val editor = sharedPreferences.edit()
                    val client = OkHttpClient()
                    GlobalScope.launch(Dispatchers.IO) {
                        val requestBody = """
                {
                "username": "$username",
                "password": "$password"
                }
                """.trimIndent()
                        try {
                            val request = Request.Builder()
                                .url("https://seld-lock.ru/auth/token/login")
                                .post(
                                    RequestBody.create(
                                        "application/json".toMediaTypeOrNull(),
                                        requestBody
                                    )
                                )
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            response.close()
                            val jsonString = responseBody
                            if (jsonString != null) {
                                val jsonObject = JSONObject(responseBody)
                                val token = jsonObject.getString("auth_token")
                                editor.putString("key", token)
                                editor.putString("username", username)
                                editor.putString("password", password)
                                editor.apply()
                                dialog.dismiss()
                                dialog1.dismiss()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            texterror.text = "Отсутствует подключение к интернету"
                        } catch (e: Exception) {
                            e.printStackTrace()
                            texterror.text="Логин или пароль неверный"
                        }
                    }
                }
            }
        }
        //регистрация
        noobutton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.registrstion, null)
            if(set=="1"){
                dialogBuilder.setCancelable(false)
            }
            dialogBuilder.setView(view)
            val dialog2 = dialogBuilder.create()
            dialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog2.show()
            val usernameEditText = view.Loginreg
            val passwordEditText = view.Passwordreg
            val passwordEditTextnext = view.Passwordnextreg
            val nobutton = view.nobuttonreg
            val registerButton = view.registerButtonreg
            val texterror = view.texterrorreg
            nobutton.setOnClickListener {
                dialog2.dismiss()
            }
            registerButton.setOnClickListener {
                username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val password2 = passwordEditTextnext.text.toString()
                if (username.equals("") || password.equals("")) {
                    texterror.text = "Введите логин и пароль"
                }
                else if(password!=password2)
                {
                    texterror.text = "Пароли не совпадают"
                }
                else {
                    val client = OkHttpClient()
                    GlobalScope.launch(Dispatchers.IO) {
                        val requestBody = """
            {
            "username": "$username",
            "password": "$password"
            }
            """.trimIndent()
                        try {
                            val request = Request.Builder()
                                .url("https://seld-lock.ru/api/v1.0/auth/users/")
                                .post(
                                    RequestBody.create(
                                        "application/json".toMediaTypeOrNull(),
                                        requestBody
                                    )
                                )
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            response.close()
                            val jsonString = responseBody
                            if (jsonString != null && jsonString.contains(username)) {
                                dialog2.dismiss()
                                dialog.dismiss()
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.putString("password", password)
                                editor.apply()
                                gettoken()
                                println(responseBody)
                            }
                            if (jsonString != null && jsonString.contains("A user with that username already exists.")) {
                                usernameEditText.text.clear()
                                passwordEditText.text.clear()
                                texterror.text = "Пользователь с таким логином уже существует"
                            }
                            else if (jsonString != null && jsonString.contains("This password is too short. It must contain at least 8 characters.")) {
                                passwordEditText.text.clear()
                                texterror.text = "Слишком короткий пароль"
                            }
                            else if (jsonString != null && jsonString.contains("This password is entirely numeric.")) {
                                passwordEditText.text.clear()
                                texterror.text = "Слишком простой пароль"
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            texterror.text = "Отсутствует подключение к интернету"
                        }
                    }
                }
            }
        }
        println("1111111111111111111111111111111")
        println(username)
        return username
    }
}


