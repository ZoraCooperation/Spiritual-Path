package com.xora.cooperation.ryan.spiritualpath

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xora.cooperation.ryan.spiritualpath.model.*
import com.xora.cooperation.ryan.spiritualpath.util.ConnectionLiveData
import com.xora.cooperation.ryan.spiritualpath.util.PermissionManager
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var connectionLiveData: ConnectionLiveData
    private lateinit var lottieLostConnectionAnimationView: LottieAnimationView
    private lateinit var lottieLoardingAnimationView: LottieAnimationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    //private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var postGridAdapter: PostGridAdapter
    private lateinit var nativePostGridAdapter: NativePostGridAdapter
    private lateinit var nativePostListAdapter: NativePostListAdapter

    private var gridView : Boolean = true
    private val globalPost = ArrayList<Post>()
    private val globalId = ArrayList<String>()

    companion object{
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val permissions = arrayOf<String?>(
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        lottieLoardingAnimationView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        if (item.itemId == R.id.nav_view){
            gridView = if (!gridView){
                val adLoader = AdLoader.Builder(this, getString(R.string.native_ad_id))
                    .forNativeAd { ad : NativeAd ->
                        lottieLoardingAnimationView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        gridLayoutManager = GridLayoutManager(this@MainActivity, 2)
                        nativePostGridAdapter = NativePostGridAdapter(addNullValueInsidePostGridArrayList(globalPost), addNullValueInsideGridArrayList(globalId), this@MainActivity)
                        nativePostGridAdapter.setNativeAds(ad)
                        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return when (nativePostGridAdapter.getItemViewType(position)) {
                                    nativePostGridAdapter.postType -> 1
                                    nativePostGridAdapter.adType -> 2
                                    else -> 1
                                }
                            }
                        }
                        recyclerView.layoutManager = gridLayoutManager
                        recyclerView.adapter = nativePostGridAdapter
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            lottieLoardingAnimationView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
                            postGridAdapter = PostGridAdapter(globalPost, globalId, this@MainActivity)
                            recyclerView.adapter = postGridAdapter
                        }
                    }).build()
                adLoader.loadAd(AdRequest.Builder().build())
                /*lottieLoardingAnimationView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                gridLayoutManager = GridLayoutManager(this@MainActivity, 2)
                postGridAdapter = PostGridAdapter(globalPost, globalId, this@MainActivity)
                recyclerView.layoutManager = gridLayoutManager
                recyclerView.adapter = postGridAdapter*/
                item.setIcon(R.drawable.distribute_vertical_white)
                //refresh()
                true
            }else{
                val adLoader = AdLoader.Builder(this, getString(R.string.native_ad_id))
                    .forNativeAd { ad : NativeAd ->
                        lottieLoardingAnimationView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        linearLayoutManager = LinearLayoutManager(this@MainActivity)
                        nativePostListAdapter = NativePostListAdapter(addNullValueInsidePostLinearArrayList(globalPost), addNullValueInsideLinearArrayList(globalId), this@MainActivity)
                        nativePostListAdapter.setNativeAds(ad)
                        recyclerView.layoutManager = linearLayoutManager
                        recyclerView.adapter = nativePostListAdapter
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            lottieLoardingAnimationView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                            postListAdapter = PostListAdapter(globalPost, globalId, this@MainActivity)
                            recyclerView.adapter = postListAdapter
                        }
                    }).build()
                adLoader.loadAd(AdRequest.Builder().build())
                /*lottieLoardingAnimationView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                linearLayoutManager = LinearLayoutManager(this@MainActivity)
                postListAdapter = PostListAdapter(globalPost, globalId, this@MainActivity)
                recyclerView.layoutManager = linearLayoutManager
                recyclerView.adapter = postListAdapter*/
                item.setIcon(R.drawable.ic_baseline_grid_on)
                //refresh()
                false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        if (gridView){
            menu.getItem(0).icon = ContextCompat.getDrawable(this, R.drawable.distribute_vertical_white)
        }else{
            menu.getItem(0).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_grid_on)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_home)

        recyclerView = findViewById(R.id.post_recyclerview)
        lottieLostConnectionAnimationView = findViewById(R.id.lost_connection)
        lottieLoardingAnimationView = findViewById(R.id.loading)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        toggle.drawerArrowDrawable.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        checkNetworkConnectionThenFetchData()
        MobileAds.initialize(this@MainActivity){}

        if (!PermissionManager.getInstance(this@MainActivity)!!
                .checkPermissions(permissions)){
            PermissionManager.getInstance(this@MainActivity)!!.askPermissions(
                this@MainActivity,
                permissions,
                PERMISSION_REQUEST_CODE
            )
        }else{
            Log.d("Tag:", "Permission already granted")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isGrid", gridView)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        gridView = savedInstanceState.getBoolean("isGrid", true)
    }
    @SuppressLint("InflateParams")
    private fun refresh(){
        val bindingDialog = layoutInflater.inflate(R.layout.custom_progress, null)
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(bindingDialog)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        Handler().postDelayed({
            checkNetworkConnectionThenFetchData()
            dialog.dismiss()
        }, 1000)
    }
    private fun termsAndConditions(){
        navigationView.menu.getItem(0).isChecked = true
        val url = "https://sites.google.com/view/spiritual-path-terms-condition/home"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }
    private fun shareWithFriends(){
        navigationView.menu.getItem(0).isChecked = true
        val appUrl = "https://play.google.com/store/apps/details?id=com.xora.cooperation.ryan.spiritualpath&pli=1"
        val shareMessage = "\nHey, I tried this pretty good app - Spiritual Path. Hope you will like it too. Inspire yourself by reading this quotes. Visit-\n$appUrl"
        Intent(Intent.ACTION_SEND).also {
            it.type = "text/plain"
            it.putExtra(Intent.EXTRA_SUBJECT,"Spiritual Path")
            it.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(it, "Share App"))
            //finish()
        }
    }
    private fun rateApp() {
        navigationView.menu.getItem(0).isChecked = true
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
            //finish()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Impossible to find an application for the market", Toast.LENGTH_LONG).show()
        }
    }
    private fun checkNetworkConnectionThenFetchData() {
        connectionLiveData = ConnectionLiveData(application)

        connectionLiveData.observe(this) { isConnected ->
            if (isConnected) {
                navigationView.menu.getItem(0).isChecked = true
                recyclerView.visibility = View.GONE
                lottieLoardingAnimationView.visibility = View.VISIBLE
                lottieLostConnectionAnimationView.visibility = View.GONE
                fetchData()
            } else {
                recyclerView.visibility = View.GONE
                lottieLoardingAnimationView.visibility = View.GONE
                lottieLostConnectionAnimationView.visibility = View.VISIBLE
                //fetchData()
            }
        }
    }

    private fun fetchData() {
        var i = 0
        val documentId = ArrayList<String>()

        FirebaseFirestore       //realtime update data when user data to images collection in firestore
            .getInstance()
            .collection("images")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                globalPost.clear()
                globalId.clear()
                documentId.clear()

                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                for (document in snapshot!!.documents){
                    i++
                    println("Document ID $i: " + document.id)
                    //val post = documents.toObjects(Post::class.java)
                    documentId.add(document.id)
                    //post as ArrayList<Post>
                }

                globalId.addAll(documentId)
                globalPost.addAll(snapshot.toObjects(Post::class.java))

                lottieLoardingAnimationView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                loadAd(globalPost, globalId)
                //setDataWithoutAdsToAdapter(globalPost, globalId)
                println("Document Size: ${globalId.size}")
                println("Post Size: ${globalPost.size}")
                println("After Document Size: ${addNullValueInsideGridArrayList(globalId).size}")
                println("After Post Size: ${addNullValueInsidePostGridArrayList(globalPost).size}")
            }
    }

    private fun setDataWithoutAdsToAdapter(globalPost: ArrayList<Post>, globalId: ArrayList<String>){
        lottieLoardingAnimationView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        if (!gridView){
            linearLayoutManager = LinearLayoutManager(this@MainActivity)
            postListAdapter = PostListAdapter(globalPost, globalId, this@MainActivity)
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = postListAdapter
        }else{
            gridLayoutManager = GridLayoutManager(this@MainActivity, 2)
            postGridAdapter = PostGridAdapter(globalPost, globalId, this@MainActivity)
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.adapter = postGridAdapter
        }
    }

    private fun loadAd(post: ArrayList<Post>, docId: ArrayList<String>){
        recyclerView.visibility = View.GONE
        lottieLoardingAnimationView.visibility = View.VISIBLE
        val adLoader = AdLoader.Builder(this, getString(R.string.native_ad_id))
            .forNativeAd { ad : NativeAd ->
                if (!gridView){
                    lottieLoardingAnimationView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    linearLayoutManager = LinearLayoutManager(this@MainActivity)
                    nativePostListAdapter = NativePostListAdapter(addNullValueInsidePostLinearArrayList(post), addNullValueInsideLinearArrayList(docId), this@MainActivity)
                    nativePostListAdapter.setNativeAds(ad)
                    recyclerView.layoutManager = linearLayoutManager
                    recyclerView.adapter = nativePostListAdapter
                }else{
                    lottieLoardingAnimationView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    gridLayoutManager = GridLayoutManager(this@MainActivity, 2)
                    nativePostGridAdapter = NativePostGridAdapter(addNullValueInsidePostGridArrayList(post), addNullValueInsideGridArrayList(docId), this@MainActivity)
                    nativePostGridAdapter.setNativeAds(ad)
                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (nativePostGridAdapter.getItemViewType(position)) {
                                nativePostGridAdapter.postType -> 1
                                nativePostGridAdapter.adType -> 2
                                else -> 1
                            }
                        }
                    }
                    recyclerView.layoutManager = gridLayoutManager
                    recyclerView.adapter = nativePostGridAdapter
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    setDataWithoutAdsToAdapter(post, docId)
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun addNullValueInsideGridArrayList(data: ArrayList<String>): ArrayList<String>{
        val newData = arrayListOf<String>()
        for (i in data.indices){
            if(i % 8 == 0){
                if(i !=0 && i != data.size - 1)
                    newData.add("null")
            }
            newData.add(data[i])
        }
        return newData
    }

    private fun addNullValueInsidePostGridArrayList(data: ArrayList<Post>): ArrayList<Post>{
        val newData = arrayListOf<Post>()
        for (i in data.indices){
            if(i % 8 == 0){
                if(i !=0 && i != data.size - 1)
                    newData.add(Post())
            }
            newData.add(data[i])
        }
        return newData
    }

    private fun addNullValueInsideLinearArrayList(data: ArrayList<String>): ArrayList<String>{
        val newData = arrayListOf<String>()
        for (i in data.indices){
            if(i % 4 == 0){
                if(i !=0 && i != data.size - 1)
                    newData.add("null")
            }
            newData.add(data[i])
        }
        return newData
    }

    private fun addNullValueInsidePostLinearArrayList(data: ArrayList<Post>): ArrayList<Post>{
        val newData = arrayListOf<Post>()
        for (i in data.indices){
            if(i % 4 == 0){
                if(i !=0 && i != data.size - 1)
                    newData.add(Post())
            }
            newData.add(data[i])
        }
        return newData
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_home -> return true
            R.id.nav_refresh -> refresh()
            R.id.nav_adblock -> println("Ad blocker")
            R.id.nav_terms_conditions -> termsAndConditions()
            R.id.nav_share -> shareWithFriends()
            R.id.nav_rate -> rateApp()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("InflateParams")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val isClick = true
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else if(isClick){
            val bindingDialog = layoutInflater.inflate(R.layout.exit_confirmation, null)
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(bindingDialog)
            dialog.setCancelable(true)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val exit = bindingDialog.findViewById<Button>(R.id.exit)
            val cancel = bindingDialog.findViewById<Button>(R.id.cancel)

            exit.setOnClickListener {
                exitProcess(0)
            }
            cancel.setOnClickListener {
                dialog.dismiss()
            }

        } else {
            super.onBackPressed()
        }
    }
}