package com.shanenworks.omnizient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.shanenworks.omnizient.ui.theme.OmnizientTheme
import com.shanenworks.omnizient.ui.screens.HomeScreen
import com.shanenworks.omnizient.data.repository.DocumentRepository
import com.shanenworks.omnizient.service.IndexingService
import com.shanenworks.omnizient.service.WebUiBroadcastService
import com.shanenworks.omnizient.viewmodel.DocumentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var documentRepository: DocumentRepository

    private val viewModel: DocumentViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startFileSystemMapping()
        } else {
            showPermissionExplanationDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()

        setContent {
            OmnizientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onToggleWebUiBroadcasting = ::toggleWebUiBroadcasting
                    )
                }
            }
        }

        startIndexingService()
        observeViewModelEvents()
    }

    private fun observeViewModelEvents() {
        lifecycleScope.launch {
            viewModel.openFileEvent.collect { document ->
                openFile(document.filePath, document.mimeType)
            }
        }

        lifecycleScope.launch {
            viewModel.openDirectoryEvent.collect { document ->
                openDirectory(document.filePath)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                startFileSystemMapping()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionExplanationDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun startFileSystemMapping() {
        lifecycleScope.launch {
            documentRepository.mapFileSystem()
        }
    }

    private fun showPermissionExplanationDialog() {
        // Implement a dialog to explain the need for storage permission
        // and provide a button to open app settings
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun startIndexingService() {
        // Start the background service for indexing files and generating embeddings
        val indexingIntent = Intent(this, IndexingService::class.java)
        startService(indexingIntent)
    }

    private fun toggleWebUiBroadcasting(enabled: Boolean) {
        val webUiIntent = Intent(this, WebUiBroadcastService::class.java)
        if (enabled) {
            startService(webUiIntent)
        } else {
            stopService(webUiIntent)
        }
    }

    private fun openFile(filePath: String, mimeType: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Handle the error, maybe show a toast or update the UI
            e.printStackTrace()
        }
    }

    private fun openDirectory(directoryPath: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(directoryPath), "resource/folder")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Handle the error, maybe show a toast or update the UI
            e.printStackTrace()
        }
    }
}
