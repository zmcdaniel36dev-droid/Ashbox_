package com.nullform.ashbox

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.nullform.ashbox.databinding.ActivityMainBinding
import com.nullform.ashbox.databinding.FragmentChatBinding
import com.nullform.ashbox.ui.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.zip.Inflater

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var chatBinding: FragmentChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.chatViewModel = chatViewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        chatBinding = FragmentChatBinding.inflate(layoutInflater)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.appBarMain.fab.setOnClickListener { view ->
            chatViewModel.startNewChatFromFab()
            navController.navigate(R.id.nav_chat)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id){
                R.id.nav_welcome -> {
                    binding.appBarMain.fab.show()
                }
                else -> {
                    binding.appBarMain.fab.hide()
                }
            }
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chat, R.id.nav_welcome, R.id.nav_models, R.id.nav_history, R.id.nav_settings, R.id.nav_licenses
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.buy_coffee_button -> {
                Toast.makeText(this, "Buy me a coffee", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}