package com.filamentmate.app.data.printer.bambu

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * MQTT Client für Bambu Lab Drucker.
 * Verbindet sich über TLS mit dem lokalen MQTT-Broker des Druckers.
 */
@Singleton
class BambuMqttClient @Inject constructor(
    private val gson: Gson
) {
    companion object {
        private const val TAG = "BambuMqtt"
        private const val MQTT_PORT = 8883
        private const val MQTT_USERNAME = "bblp"
        private const val REPORT_TOPIC_TEMPLATE = "device/%s/report"
        private const val REQUEST_TOPIC_TEMPLATE = "device/%s/request"
    }
    
    private var mqttClient: MqttClient? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _reports = MutableSharedFlow<BambuReport>(replay = 1)
    val reports: SharedFlow<BambuReport> = _reports.asSharedFlow()
    
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()
    
    private var currentSerial: String? = null
    private var requestTopic: String? = null
    
    /**
     * Verbindet sich mit dem Bambu Lab Drucker.
     * @param ip IP-Adresse des Druckers
     * @param serialNumber Seriennummer des Druckers (15-stellig)
     * @param accessCode Access-Code aus den Drucker-Einstellungen
     */
    suspend fun connect(ip: String, serialNumber: String, accessCode: String) {
        try {
            disconnect()
            
            currentSerial = serialNumber
            val brokerUrl = "ssl://$ip:$MQTT_PORT"
            val clientId = "FilamentMate_${System.currentTimeMillis()}"
            
            Log.d(TAG, "Connecting to $brokerUrl with serial $serialNumber")
            
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence()).apply {
                setCallback(createCallback())
            }
            
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                userName = MQTT_USERNAME
                password = accessCode.toCharArray()
                connectionTimeout = 10
                keepAliveInterval = 30
                socketFactory = createTrustAllSslContext().socketFactory
            }
            
            mqttClient?.connect(options)
            
            // Subscribe to report topic
            val reportTopic = REPORT_TOPIC_TEMPLATE.format(serialNumber)
            requestTopic = REQUEST_TOPIC_TEMPLATE.format(serialNumber)
            
            mqttClient?.subscribe(reportTopic, 0)
            Log.i(TAG, "Connected and subscribed to $reportTopic")
            
            _isConnected.value = true
            
            // Request initial status
            requestFullStatus()
            
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}", e)
            _isConnected.value = false
            _errors.emit("Verbindungsfehler: ${e.message}")
            throw e
        }
    }
    
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Disconnect error: ${e.message}")
        } finally {
            mqttClient = null
            _isConnected.value = false
            currentSerial = null
            requestTopic = null
        }
    }
    
    /**
     * Fordert vollständigen Status vom Drucker an.
     */
    fun requestFullStatus() {
        val topic = requestTopic ?: return
        val payload = """{"pushing": {"sequence_id": "0", "command": "pushall"}}"""
        publish(topic, payload)
    }
    
    /**
     * Pausiert den aktuellen Druck.
     */
    fun pausePrint() {
        val topic = requestTopic ?: return
        val payload = """{"print": {"sequence_id": "0", "command": "pause"}}"""
        publish(topic, payload)
    }
    
    /**
     * Setzt den pausierten Druck fort.
     */
    fun resumePrint() {
        val topic = requestTopic ?: return
        val payload = """{"print": {"sequence_id": "0", "command": "resume"}}"""
        publish(topic, payload)
    }
    
    /**
     * Stoppt den aktuellen Druck.
     */
    fun stopPrint() {
        val topic = requestTopic ?: return
        val payload = """{"print": {"sequence_id": "0", "command": "stop"}}"""
        publish(topic, payload)
    }
    
    private fun publish(topic: String, payload: String) {
        try {
            val message = MqttMessage(payload.toByteArray()).apply {
                qos = 0
            }
            mqttClient?.publish(topic, message)
            Log.d(TAG, "Published to $topic: $payload")
        } catch (e: Exception) {
            Log.e(TAG, "Publish failed: ${e.message}", e)
        }
    }
    
    private fun createCallback() = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            Log.w(TAG, "Connection lost: ${cause?.message}")
            _isConnected.value = false
            scope.launch {
                _errors.emit("Verbindung verloren: ${cause?.message}")
            }
        }
        
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            val payload = message?.payload?.toString(Charsets.UTF_8) ?: return
            
            try {
                val report = gson.fromJson(payload, BambuReport::class.java)
                scope.launch {
                    _reports.emit(report)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse message: ${e.message}")
            }
        }
        
        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            // Not used for subscriptions
        }
    }
    
    /**
     * Erstellt einen SSL-Kontext, der alle Zertifikate akzeptiert.
     * Bambu Lab Drucker verwenden selbst-signierte Zertifikate.
     */
    private fun createTrustAllSslContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        
        return SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }
}
