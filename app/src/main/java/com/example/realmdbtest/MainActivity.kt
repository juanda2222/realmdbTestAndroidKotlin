package com.example.realmdbtest

// how to setup the auth using jwt
//https://medium.com/swlh/mongodb-realm-jwt-meta-data-and-custom-user-data-dc04d86bf542
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class MainActivity : AppCompatActivity() {

    lateinit var uiThreadRealm: Realm
    lateinit var app: App
    lateinit var assetToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // initialize the template ui
        Log.d("TAG", "application started")
        super.onCreate(savedInstanceState)


        // realm initialization
        Realm.init(this) // context, usually an Activity or Application
        val appID : String = "dev-realm-tncst";

        val handler =
                SyncSession.ClientResetHandler { session, error ->
                    Log.e("EXAMPLE", "Client Reset required for: ${session.configuration.serverUrl} for error: $error")
                }
        app = App(
                AppConfiguration.Builder(appID)
                        .defaultClientResetHandler(handler)
                        .build()
        )

        // authenticate to the custom asset backend:
        var accessToken: String
        val jsonobj = JSONObject()
        jsonobj.put("username", "demo1")
        jsonobj.put("password", "pwd")
        val queue = Volley.newRequestQueue(this)


        // Request the token for user autorization.
        val url = "https://dev.60hertz.io/api/asset/auth/login";
        val req = JsonObjectRequest(Request.Method.POST, url, jsonobj,
                Response.Listener {
                    response ->
                    println("-----> Auth response -> $response")
                    accessToken = response.get("token") as String


                    // chain the jwt get
                    val jwtUrl = "https://dev.60hertz.io/api/asset/jwt";
                    val jwtreq = object: JsonObjectRequest(Request.Method.GET, jwtUrl, null,
                            Response.Listener { response ->
                                println("------> Jwk response -> $response")
                                var signedJwt = response.get("accessToken") as String

                                // once we have the signed token we login to realm
                                // login to realm DB
                                val credentials: Credentials = Credentials.jwt(signedJwt)
                                var user: User? = null
                                app.loginAsync(credentials) {
                                    if (it.isSuccess) {
                                        Log.v("QUICKSTART", "Successfully authenticated with JWT.")
                                        val user: User? = app.currentUser()
                                        if (user != null) {
                                            println("------> Logged in user custom data: -> ${user.customData}")
                                            println("------> Logged in user id: -> ${user.id}")
                                            println("------> Logged in user device id: -> ${user.deviceId}")
                                            println("------> Logged in user profile: -> ${user.profile}")
                                            println("------> Logged in user state: -> ${user.state}")
                                        }
                                        val partitionValue: String = "demo-user-id"
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
                            },
                            Response.ErrorListener { error ->
                                println("=======> Jwk error -> $error")
                            })
                    {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Authorization"] = "Bearer $accessToken"
                            return headers
                        }
                    }
                    queue.add(jwtreq)


                }, Response.ErrorListener { error: VolleyError ->
            println("=====> Auth error $error.message")
        }
        )
        queue.add(req)

        // template related
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            // all tasks in the realm
            val assetTypes : RealmResults<assettype> = uiThreadRealm.where<assettype>().findAll()
            println("Read assetTypes: $assetTypes")
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
    fun addChangeListenerToRealm(realm : Realm) {
        // all assetType in the realm
        val assetTypeResult : RealmResults<assettype> = realm.where<assettype>().findAllAsync()
        assetTypeResult.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<assettype>> { collection, changeSet ->
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

    class BackgroundQuickStart(val user: User) : Runnable {
        override fun run() {
            val partitionValue: String = "demo-user-id"
            val config = SyncConfiguration.Builder(user, partitionValue)
                    .build()
            val backgroundThreadRealm : Realm = Realm.getInstance(config)
            val assetTypeCustom : assettype = assettype()
            assetTypeCustom.oid = "aaa2324rf"
            assetTypeCustom.org_id = "101"
            assetTypeCustom.name = "realm test"
            assetTypeCustom.modified = Date()

            backgroundThreadRealm.executeTransaction { transactionRealm ->
                transactionRealm.insert(assetTypeCustom)
            }
            // all tasks in the realm
            val assetTypes : RealmResults<assettype> = backgroundThreadRealm.where<assettype>().findAll()
            println(">>>>>>>> just the assetType: $assetTypes")


            // all modifications to a realm must happen inside of a write block
//            backgroundThreadRealm.executeTransaction { transactionRealm ->
//                val innerYetAnotherTask : AssetType = transactionRealm.where<AssetType>().equalTo("_id", yetAnotherTaskId).findFirst()!!
//                innerYetAnotherTask.deleteFromRealm()
//            }
            // because this background thread uses synchronous realm transactions, at this point all
            // transactions have completed and we can safely close the realm
            backgroundThreadRealm.close()
        }
    }
}