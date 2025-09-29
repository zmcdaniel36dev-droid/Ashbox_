package com.nullform.ashbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nullform.ashbox.ui.WebViewFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var chatBinding: FragmentChatBinding
    private val TAG: String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.chatViewModel = chatViewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        // The root of appBarMain is the CoordinatorLayout, which wraps the whole content area
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarMain.root) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply top padding to the toolbar to avoid the status bar content overlap
            binding.appBarMain.toolbar.setPadding(
                binding.appBarMain.toolbar.paddingLeft,
                systemBars.top,
                binding.appBarMain.toolbar.paddingRight,
                binding.appBarMain.toolbar.paddingBottom
            )

            // Apply bottom padding to the CoordinatorLayout to shrink it when the keyboard is open
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                ime.bottom
            )

            // Return the insets so that child views can also process them if they need to.
            // This is especially important for things like the navigation drawer.
            windowInsets
        }

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
            // Invalidate options menu to update visibility of "Buy me a coffee" button
            invalidateOptionsMenu()
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chat, R.id.nav_welcome, R.id.nav_models, R.id.nav_history, R.id.nav_settings, R.id.nav_licenses
                // R.id.webview has been removed from here so it gets a back arrow
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val buyCoffeeButton = menu?.findItem(R.id.buy_coffee_button)

        if (navController.currentDestination?.id == R.id.webview) {
            buyCoffeeButton?.isVisible = false
        } else {
            buyCoffeeButton?.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val TAG: String = "BuyMeACoffee Fail"
        Log.d(TAG, item.itemId.toString())
        return when (item.itemId) {
            R.id.buy_coffee_button -> {
                try{
                    val navController = findNavController(R.id.nav_host_fragment_content_main)
                    Log.d(TAG, navController.toString())
                    val args = Bundle().apply {
                        putString(WebViewFragment.URL_KEY, "https://buymeacoffee.com/zacharymcdaniel_nullform")
                    }
                    Log.d(TAG, args.toString())
                    navController.navigate(R.id.webview, args)
                    Log.d(TAG, "We made it to the end!")
                }catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)?.childFragmentManager?.primaryNavigationFragment

        if (currentFragment is WebViewFragment) {
            if (currentFragment.canWebViewGoBack()) {
                currentFragment.webViewGoBack()
                return true
            }
        }
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}