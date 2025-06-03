package com.example.todo.activities


import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.todo.R
import com.example.todo.databinding.ActivityMainBinding
import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.core.content.getSystemService
import com.example.todo.data.sharedPreferences.WasPermissionShowedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navController = findNavController(R.id.main_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        checkForPermissions(Manifest.permission.POST_NOTIFICATIONS, "Notifications", 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notifications permission denied!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!WasPermissionShowedPreferences.loadWasPermissionShowedGranted(this)) {
            if (getSystemService<AlarmManager>()!!.canScheduleExactAlarms()) {
                Toast.makeText(this, "Alarms permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Alarms permission denied!", Toast.LENGTH_SHORT).show()
            }
            WasPermissionShowedPreferences.setWasPermissionShowedGranted(this, true)
        }

        checkForPermissions(Manifest.permission.SCHEDULE_EXACT_ALARM, "Alarms", 1)
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        if (requestCode == 0) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            )
                showDialog(
                    permission,
                    name,
                    requestCode
                )

        } else {
            if (!getSystemService<AlarmManager>()!!.canScheduleExactAlarms()) {
                WasPermissionShowedPreferences.setWasPermissionShowedGranted(this, false)

                showDialog(
                    permission,
                    name,
                    requestCode
                )
            }

        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder
            .apply {
                setMessage("Permission to access your $name is required to use this app")
                setTitle("Permission required")
                setPositiveButton("OK") { _, _ ->
                    if (requestCode == 0)
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(permission),
                            requestCode
                        )
                    else {
                        startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }
            }
            .create()
            .show()
    }
}