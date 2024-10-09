package com.shanenworks.omnizient.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.shanenworks.omnizient.data.repository.DocumentRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IndexingService : Service() {

    @Inject
    lateinit var documentRepository: DocumentRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            performIndexing()
        }
        return START_STICKY
    }

    private suspend fun performIndexing() {
        // Implement the indexing logic here
        // This should involve scanning the file system and updating the database
        documentRepository.mapFileSystem()
        // Add logic for generating embeddings and updating the vector database
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}