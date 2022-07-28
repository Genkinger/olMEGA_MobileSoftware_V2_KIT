package com.iha.olmega_mobilesoftware_v2

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.AppUpdaterUtils.UpdateListener
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import org.w3c.dom.Document
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class PreferencesActivity : PreferenceActivity() {
    private val TAG = this.javaClass.simpleName
    var isDeviceOwner = false
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        isDeviceOwner = intent.getBooleanExtra("isDeviceOwner", false)
        fragmentManager.beginTransaction().replace(android.R.id.content, Preferences()).commit()
    }

    // https://github.com/javiersantos/AppUpdater/issues/193#issuecomment-721537960
    //You write your code here when the download finished
    private fun DownloadUpdate(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("Download Update")
        request.allowScanningByMediaScanner()
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('?')))
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        manager.enqueue(request)
        Toast.makeText(this, "Download started. Please wait...", Toast.LENGTH_LONG).show()
    }

    private var onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val b = intent.extras
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(b!!.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))
            val cur = dm.query(query)
            var success = false
            if (cur.moveToFirst()) {
                val columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
                    val uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val myFile = File(uriString.replace("file://", ""))
                    if (myFile.isFile) {
                        LogIHAB.log("Installing Update '" + myFile.name + "'")
                        val returnIntent = Intent()
                        returnIntent.putExtra("installNewApp", myFile.toString())
                        this@PreferencesActivity.setResult(RESULT_OK, returnIntent)
                        finish()
                        success = true
                        context.unregisterReceiver(this)
                    }
                }
            }
            if (!success) Toast.makeText(applicationContext, "Download file not found. Please try again!", Toast.LENGTH_LONG).show()
        }
    }

    class Preferences : PreferenceFragment() {
        private val TAG = this.javaClass.simpleName
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            val tmp = activity as PreferencesActivity
            findPreference("AndroidID").summary = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            findPreference("disableDeviceAdmin").isEnabled = tmp.isDeviceOwner
            findPreference("checkForUpdate").isEnabled = com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings.exists()
            if (findPreference("checkForUpdate").isEnabled == false) findPreference("checkForUpdate").summary =
                com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings.absolutePath + " is missing!"
            //else if (isNetworkAvailable() == false)
            //    findPreference("checkForUpdate").setSummary("No active internet connection!");
            includeQuestList()
            includedAFExList()
            val DisabledeviceOwnerPref = findPreference("disableDeviceAdmin") as Preference
            DisabledeviceOwnerPref.onPreferenceClickListener = OnPreferenceClickListener { arg0: Preference? ->
                confirmDisableDeviceAdmin()
                true
            }
            /*
            Preference EnabledeviceOwnerPref = (Preference) findPreference("enableDeviceAdmin");
            EnabledeviceOwnerPref.setOnPreferenceClickListener(arg0 -> {
                confirmEnableDeviceAdmin();
                return true;
            });
             */
            val killAppAndServicePref = findPreference("killAppAndService") as Preference
            killAppAndServicePref.onPreferenceClickListener = OnPreferenceClickListener { arg0: Preference? ->
                confirmKillAppAndService()
                true
            }
            val LinkDevicePref = findPreference("LinkDevice") as Preference
            LinkDevicePref.onPreferenceClickListener = OnPreferenceClickListener { arg0: Preference? ->
                val intent = Intent(activity, LinkDeviceHelper::class.java)
                startActivityForResult(intent, ActiviyRequestCode.LinkDeviceHelper.ordinal)
                true
            }
            val VersionPref = findPreference("Version") as Preference
            VersionPref.summary = BuildConfig.VERSION_NAME
            val button = findPreference("checkForUpdate") as Preference
            if (button != null) {
                button.onPreferenceClickListener = OnPreferenceClickListener { arg0: Preference? ->
                    checkForUpdate()
                    true
                }
            }
        }

        private fun checkForUpdate() {
            if (isNetworkAvailable) {
                if (com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings.isFile) {
                    val appUpdater = AppUpdaterUtils(activity) //.setUpdateFrom(UpdateFrom.AMAZON)
                            //.setUpdateFrom(UpdateFrom.GITHUB)
                            //.setGitHubUserAndRepo("javiersantos", "AppUpdater")
                            //...
                            .withListener(object : UpdateListener {
                                override fun onSuccess(update: Update, isUpdateAvailable: Boolean) {
                                    if (isUpdateAvailable) {
                                        val alertDialogBuilder = AlertDialog.Builder(context)
                                        alertDialogBuilder.setTitle("New update available!")
                                        alertDialogBuilder
                                                .setMessage("""Update ${update.latestVersion} available to download!
${update.releaseNotes}""")
                                                .setCancelable(false)
                                                .setPositiveButton("Update") { dialog, id ->
                                                    val tmp = activity as PreferencesActivity
                                                    tmp.DownloadUpdate(update.urlToDownload.toString() + "?" + System.currentTimeMillis())
                                                }
                                                .setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }
                                        val alertDialog = alertDialogBuilder.create()
                                        alertDialog.show()
                                        /*
                                        Log.d("Latest Version", update.getLatestVersion());
                                        Log.d("Latest Version Code", update.getLatestVersionCode().toString());
                                        Log.d("Release notes", update.getReleaseNotes());
                                        Log.d("URL", update.getUrlToDownload().toString());
                                        Log.d("Is update available?", Boolean.toString(isUpdateAvailable));
                                         */
                                    } else {
                                        Toast.makeText(activity, "No update available!", Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onFailed(error: AppUpdaterError) {
                                    Toast.makeText(activity, "AppUpdater Error: Something went wrong", Toast.LENGTH_LONG).show()
                                    Log.d("AppUpdater Error", error.toString())
                                }
                            })
                    /*
                    AppUpdater appUpdater = new AppUpdater(getActivity())
                            .showAppUpdated(true)
                            .setCancelable(false)
                            .setButtonDoNotShowAgain(null)
                            .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    StartUpdate();
                                }
                            })
                            .setDisplay(Display.DIALOG);
                     */try {
                        val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings)
                        val elements = doc.getElementsByTagName("Source")
                        for (i in 0 until elements.length) {
                            val attributes = elements.item(i).attributes
                            if (attributes.getNamedItem("type").nodeValue == "XML") {
                                appUpdater.setUpdateFrom(UpdateFrom.XML)
                                appUpdater.setUpdateXML(attributes.getNamedItem("URL").nodeValue + "?" + System.currentTimeMillis())
                            } else if (attributes.getNamedItem("type").nodeValue == "JSON") {
                                appUpdater.setUpdateFrom(UpdateFrom.JSON)
                                appUpdater.setUpdateJSON(attributes.getNamedItem("URL").nodeValue + "?" + System.currentTimeMillis())
                            }
                        }
                        appUpdater.start()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "'" + com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings.absoluteFile + "' not valid!", Toast.LENGTH_SHORT).show()
                    }
                } else Toast.makeText(activity, "'" + com.iha.olmega_mobilesoftware_v2.Preferences.Companion.UdaterSettings.absoluteFile + "' does not exist!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(activity, "No active internet connection!", Toast.LENGTH_SHORT).show()
        }

        private val isNetworkAvailable: Boolean
            private get() {
                val connectivityManager = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }

        private fun includeQuestList() {
            // Scan file system for available questionnaires
            val fileIO = FileIO()
            val fileList = fileIO.scanQuestOptions()
            val listPreferenceQuest = findPreference("selectedQuest") as ListPreference
            // TODO: Isn't the second constraint enough?
            if (fileList != null && fileList.size > 0) {
                // Fill in menu contents
                listPreferenceQuest.entries = fileList
                listPreferenceQuest.entryValues = fileList
                listPreferenceQuest.setDefaultValue(fileList[0])
            } else {
                listPreferenceQuest.setSummary(R.string.noQuestionnaires)
                listPreferenceQuest.isSelectable = false
            }
        }

        private fun includedAFExList() {
            val directory: File = SystemStatus.Companion.AFExConfigFolder
            if (!directory.exists()) directory.mkdirs()
            val files = directory.listFiles()
            val fileList = arrayOfNulls<String>(files.size)
            for (i in files.indices) if (files[i].name.substring(files[i].name.lastIndexOf(".")).lowercase(Locale.getDefault()) == ".xml") fileList[i] = files[i].name
            val listPreference = findPreference("inputProfile") as ListPreference
            if (fileList != null && fileList.size > 0) {
                // Fill in menu contents
                listPreference.entries = fileList
                listPreference.entryValues = fileList
                listPreference.setDefaultValue(fileList[0])
            } else {
                listPreference.summary = "no AEFx-Settings in '" + SystemStatus.Companion.AFExConfigFolder + "'"
                listPreference.isSelectable = false
            }
        }

        private fun confirmDisableDeviceAdmin() {
            AlertDialog.Builder(activity, R.style.SwipeDialogTheme)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.deviceOwnerMessageDisable)
                    .setPositiveButton(R.string.deviceOwnerYes) { dialog, which ->
                        val returnIntent = Intent()
                        returnIntent.putExtra("killAppAndService", true)
                        returnIntent.putExtra("disableDeviceAdmin", true)
                        activity.setResult(RESULT_OK, returnIntent)
                        activity.finish()
                    }
                    .setNegativeButton(R.string.deviceOwnerNo) { dialog, which -> }
                    .setCancelable(false)
                    .show()
        }

        private fun confirmEnableDeviceAdmin() {
            AlertDialog.Builder(activity, R.style.SwipeDialogTheme)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.deviceOwnerMessageEnable)
                    .setPositiveButton(R.string.deviceOwnerYes) { dialog, which ->
                        val returnIntent = Intent()
                        returnIntent.putExtra("enableDeviceAdmin", true)
                        activity.setResult(RESULT_OK, returnIntent)
                        activity.finish()
                    }
                    .setNegativeButton(R.string.deviceOwnerNo) { dialog, which -> }
                    .setCancelable(false)
                    .show()
        }

        private fun confirmKillAppAndService() {
            AlertDialog.Builder(activity, R.style.SwipeDialogTheme)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.killAppAndServiceMessage)
                    .setPositiveButton(R.string.deviceOwnerYes) { dialog, which ->
                        val returnIntent = Intent()
                        returnIntent.putExtra("killAppAndService", true)
                        activity.setResult(RESULT_OK, returnIntent)
                        activity.finish()
                    }
                    .setNegativeButton(R.string.deviceOwnerNo) { dialog, which -> }
                    .setCancelable(false)
                    .show()
        }
    }
}