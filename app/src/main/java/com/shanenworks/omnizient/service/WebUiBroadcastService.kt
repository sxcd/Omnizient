package com.shanenworks.omnizient.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.shanenworks.omnizient.data.repository.DocumentRepository
import dagger.hilt.android.AndroidEntryPoint
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class WebUiBroadcastService : Service() {

    @Inject
    lateinit var documentRepository: DocumentRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var server: WebServer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            startWebServer()
        }
        return START_STICKY
    }

    private fun startWebServer() {
        server = WebServer(8080)
        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        if (::server.isInitialized) {
            server.stop()
        }
    }

    private inner class WebServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            return when {
                uri == "/" -> serveHomePage()
                uri == "/search" -> handleSearch(session)
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
            }
        }

        private fun serveHomePage(): Response {
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Omnizient Web UI</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }
                        h1 { color: #333; }
                        #search-form { margin-bottom: 20px; }
                        #search-input { width: 70%; padding: 10px; }
                        #search-button { padding: 10px 20px; }
                        #results { list-style-type: none; padding: 0; }
                        .result-item { background: #f0f0f0; margin-bottom: 10px; padding: 10px; }
                    </style>
                </head>
                <body>
                    <h1>Omnizient Web UI</h1>
                    <form id="search-form">
                        <input type="text" id="search-input" placeholder="Enter search query">
                        <button type="submit" id="search-button">Search</button>
                    </form>
                    <ul id="results"></ul>
                    <script>
                        document.getElementById('search-form').addEventListener('submit', function(e) {
                            e.preventDefault();
                            var query = document.getElementById('search-input').value;
                            fetch('/search?q=' + encodeURIComponent(query))
                                .then(response => response.json())
                                .then(data => {
                                    var resultsList = document.getElementById('results');
                                    resultsList.innerHTML = '';
                                    data.forEach(function(item) {
                                        var li = document.createElement('li');
                                        li.className = 'result-item';
                                        li.textContent = item.fileName + ' - ' + item.filePath;
                                        resultsList.appendChild(li);
                                    });
                                });
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()
            return newFixedLengthResponse(html)
        }

        private fun handleSearch(session: IHTTPSession): Response {
            val params = session.parameters
            val query = params["q"]?.get(0) ?: ""
            val results = runBlocking {
                documentRepository.searchDocuments(query).first()
            }
            val jsonResults = results.map { doc ->
                """{"fileName": "${doc.fileName}", "filePath": "${doc.filePath}"}"""
            }.joinToString(",", "[", "]")
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResults)
        }
    }
}