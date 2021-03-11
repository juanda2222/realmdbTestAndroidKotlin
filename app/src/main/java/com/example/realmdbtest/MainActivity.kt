package com.example.realmdbtest

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class MainActivity : AppCompatActivity() {

    lateinit var uiThreadRealm: Realm
    lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {

        Realm.init(this) // context, usually an Activity or Application
        val appID : String = "dev-realm-tncst";
        app = App(AppConfiguration.Builder(appID)
            .build())


        val credentials: Credentials = Credentials.anonymous()

        
        app.loginAsync(credentials) {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                val user: User? = app.currentUser()
                val partitionValue: String = "My Project"
                val config = SyncConfiguration.Builder(user, partitionValue)
                    .build()
                uiThreadRealm = Realm.getInstance(config)
                addChangeListenerToRealm(uiThreadRealm)
                val task : FutureTask<String> = FutureTask(BackgroundQuickStart(app.currentUser()!!), "test")
                val executorService: ExecutorService = Executors.newFixedThreadPool(2)
                executorService.execute(task)
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: ${it.error}")
            }
        }






        val realmName: String = "My Project"
        val config = RealmConfiguration.Builder().name(realmName).build()
        val backgroundThreadRealm : Realm = Realm.getInstance(config)




        Log.d("TAG", "application started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            // all tasks in the realm
            val assetTypes : RealmResults<AssetType> = backgroundThreadRealm.where<AssetType>().findAll()
            println("just the assetType: $assetTypes")

            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    /**
     *  Additional function needed :
     */
    fun addChangeListenerToRealm(realm : Realm) {
        // all tasks in the realm
        val tasks : RealmResults<AssetType> = realm.where<AssetType>().findAllAsync()
        tasks.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<AssetType>> { collection, changeSet ->
            // process deletions in reverse order if maintaining parallel data structures so indices don't change as you iterate
            val deletions = changeSet.deletionRanges
            for (i in deletions.indices.reversed()) {
                val range = deletions[i]
                Log.v("QUICKSTART", "Deleted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }
            val insertions = changeSet.insertionRanges
            for (range in insertions) {
                Log.v("QUICKSTART", "Inserted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }
            val modifications = changeSet.changeRanges
            for (range in modifications) {
                Log.v("QUICKSTART", "Updated range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.d("TAG", "message from settings")
        return when (item.itemId) {
            R.id.action_settings -> true
            else ->
                super.onOptionsItemSelected(item)
        }
    }
}

class BackgroundQuickStart(val user: User) : Runnable {
    override fun run() {
        val partitionValue: String = "My Project"
        val config = SyncConfiguration.Builder(user, partitionValue)
            .build()
        val backgroundThreadRealm : Realm = Realm.getInstance(config)
        val task : AssetType = AssetType()
        backgroundThreadRealm.executeTransaction { transactionRealm ->
            transactionRealm.insert(task)
        }

        backgroundThreadRealm.close()
    }
}
