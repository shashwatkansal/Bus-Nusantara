package com.example.busnusantara

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.util.Log.i
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.generated.model.Route

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.configure(applicationContext)
            Log.i("BN", "Initialized Amplify")
        } catch (failure: AmplifyException) {
            Log.e("BN", "Could not initialize Amplify", failure)
        }

        Amplify.DataStore.observe(Route::class.java,
            { Log.i("BN", "Observation began.") },
            { Log.i("BN", it.item().toString()) },
            { Log.e("BN", "Observation failed.", it) },
            { Log.i("BN", "Observation complete.") }
        )

        Amplify.DataStore.query(Route::class.java,
            { routes ->
                while (routes.hasNext()) {
                    val route: Route = routes.next()
                    Log.i("BN", "==== Routes ====")
                    Log.i("BN", "Name: ${route.destination}")
                    Log.i("BN", "Priority: ${route.stops}")
                }
            },
            { Log.e("BN", "Could not query DataStore", it) }
        )
    }
}