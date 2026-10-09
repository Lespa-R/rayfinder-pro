package me.datagrow.rayfinderpro

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

data class SeenDevice(
    val address: String,
    val name: String?,
    val metaReason: String?,
    val rssi: Int,
    val firstSeen: Long,
    val lastSeen: Long,
    val info: DeviceInfo = DeviceInfo(),
) {
    val isMeta get() = metaReason != null

    /** Ce qu'on sait de mieux : nom annoncé, sinon type, sinon fabricant. */
    val displayName: String
        get() = name
            ?: if (isMeta) "Lunettes Meta" else null
            ?: info.kind
            ?: info.manufacturer?.let { "Appareil $it" }
            ?: "Appareil anonyme"

    /** Au moins un indice sur l'identité de l'appareil. */
    val isIdentified get() = isMeta || name != null || info.kind != null || info.manufacturer != null
}

class BleScanner(private val context: Context) {

    private val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter

    private val _bluetoothOn = MutableStateFlow(adapter?.isEnabled == true)
    /** Suivi en direct : l'interface réagit dès que le Bluetooth est activé ou coupé. */
    val bluetoothOn: StateFlow<Boolean> = _bluetoothOn.asStateFlow()

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_ON -> {
                    _bluetoothOn.value = true
                    _error.value = null
                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    _bluetoothOn.value = false
                    // Le système arrête le scan de lui-même : on aligne l'état affiché
                    if (_scanning.value && !demoMode) _scanning.value = false
                }
            }
        }
    }
    private var receiverRegistered = false

    /** À appeler quand l'écran devient visible (onStart). */
    fun attach() {
        _bluetoothOn.value = adapter?.isEnabled == true
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                context, stateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        }
    }

    /** À appeler quand l'écran n'est plus visible (onStop). */
    fun detach() {
        stop()
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(stateReceiver) }
            receiverRegistered = false
        }
    }

    private val _devices = MutableStateFlow<Map<String, SeenDevice>>(emptyMap())
    val devices: StateFlow<Map<String, SeenDevice>> = _devices.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Appelé quand un appareil Meta apparaît (ou réapparaît après expiration). */
    var onNewMetaDevice: ((SeenDevice) -> Unit)? = null

    val hasBluetooth get() = adapter != null

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) = handle(result)
        override fun onBatchScanResults(results: MutableList<ScanResult>) = results.forEach(::handle)
        override fun onScanFailed(errorCode: Int) {
            _error.value = "Échec du scan Bluetooth (code $errorCode)"
            _scanning.value = false
        }
    }

    private fun handle(result: ScanResult) {
        val now = System.currentTimeMillis()
        val address = result.device.address
        var isNewMeta = false
        val updated = _devices.updateAndGet { current ->
            val prev = current[address]
            val reason = MetaSignatures.match(result) ?: prev?.metaReason
            isNewMeta = reason != null && prev?.isMeta != true
            current + (address to SeenDevice(
                address = address,
                name = result.scanRecord?.deviceName ?: prev?.name,
                metaReason = reason,
                rssi = result.rssi,
                firstSeen = prev?.firstSeen ?: now,
                lastSeen = now,
                info = DeviceInfo.from(result).mergedWith(prev?.info),
            ))
        }
        if (isNewMeta) updated[address]?.let { onNewMetaDevice?.invoke(it) }
    }

    @SuppressLint("MissingPermission") // vérifiée par l'UI avant l'appel
    fun start() {
        val scanner = adapter?.bluetoothLeScanner ?: run {
            _error.value = "Bluetooth désactivé"
            return
        }
        if (_scanning.value) return
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        _error.value = null
        runCatching { scanner.startScan(null, settings, callback) }
            .onSuccess { _scanning.value = true }
            .onFailure { _error.value = it.message ?: "Impossible de démarrer le scan" }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (!_scanning.value) return
        if (demoMode) {
            demoMode = false
            _scanning.value = false
            _devices.value = emptyMap()
            return
        }
        runCatching { adapter?.bluetoothLeScanner?.stopScan(callback) }
        _scanning.value = false
    }

    /**
     * Mode démo (versions de debug uniquement) : simule des appareils pour vérifier l'interface
     * et produire des captures d'écran, l'émulateur ne pouvant pas émettre de signaux BLE.
     */
    private var demoMode = false

    fun startDemo() {
        demoMode = true
        val now = System.currentTimeMillis()
        val demo = listOf(
            SeenDevice("D4:3A:1F:00:00:01", "Ray-Ban Meta", "Fabricant : Meta Platforms", -54, now, now,
                DeviceInfo("Meta", "Lunettes", listOf("Meta"), -12, true, "01AB · 02 15 4F")),
            SeenDevice("D4:3A:1F:00:00:02", null, "Service Meta 0xFD5F", -71, now, now,
                DeviceInfo(null, null, listOf("Meta"), null, false)),
            SeenDevice("A0:11:22:33:44:55", "Galaxy Buds2 Pro", null, -66, now, now,
                DeviceInfo("Samsung", "Écouteurs ou casque", listOf("Batterie"), -8, true)),
            SeenDevice("5C:E9:1E:10:20:30", null, null, -62, now, now,
                DeviceInfo("Apple", "iPhone, iPad ou Mac", emptyList(), 12, true, "004C · 10 05 1B 1C")),
            SeenDevice("6F:01:AA:BB:CC:DD", null, null, -78, now, now,
                DeviceInfo("Apple", "AirTag ou objet « Localiser »", emptyList(), null, false)),
            SeenDevice("4A:12:34:56:78:9A", null, null, -86, now, now),
        )
        _devices.value = demo.associateBy { it.address }
        _scanning.value = true
        demo.firstOrNull { it.isMeta }?.let { onNewMetaDevice?.invoke(it) }
    }

    /** Oublie les appareils non revus depuis [maxAgeMs]. */
    fun prune(maxAgeMs: Long) {
        if (demoMode) {
            // En démo : les appareils restent, leur signal varie légèrement
            val now = System.currentTimeMillis()
            _devices.update { map -> map.mapValues { (_, d) -> d.copy(rssi = (d.rssi + (-2..2).random()).coerceIn(-90, -45), lastSeen = now) } }
            return
        }
        val limit = System.currentTimeMillis() - maxAgeMs
        _devices.update { map -> map.filterValues { it.lastSeen >= limit } }
    }
}
