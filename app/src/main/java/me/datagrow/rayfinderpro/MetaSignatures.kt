package me.datagrow.rayfinderpro

import android.bluetooth.le.ScanResult
import android.os.ParcelUuid

/**
 * Empreintes publicitaires BLE des lunettes Meta (Ray-Ban Meta, Oakley Meta…).
 * Les adresses MAC sont aléatoires : on se fie aux identifiants publiés par le Bluetooth SIG.
 */
object MetaSignatures {

    // Company Identifiers (données "manufacturer specific")
    private val companyIds = mapOf(
        0x01AB to "Meta Platforms",
        0x058E to "Meta Platforms Technologies",
        0x0D53 to "Luxottica",
    )

    // UUID de service 16 bits attribués à Meta
    private val serviceUuids = listOf(0xFD5F, 0xFEB7, 0xFEB8).associate {
        ParcelUuid.fromString("0000%04X-0000-1000-8000-00805F9B34FB".format(it)) to "0x%04X".format(it)
    }

    private val nameHints = listOf("ray-ban", "rayban", "oakley", "meta")

    /** Renvoie la raison de la correspondance, ou null si l'appareil n'est pas identifié comme Meta. */
    fun match(result: ScanResult): String? {
        val record = result.scanRecord ?: return null

        val msd = record.manufacturerSpecificData
        for (i in 0 until msd.size()) {
            companyIds[msd.keyAt(i)]?.let { return "Fabricant : $it" }
        }

        val uuids = record.serviceUuids.orEmpty() + record.serviceData.keys
        uuids.firstNotNullOfOrNull { serviceUuids[it] }?.let { return "Service Meta $it" }

        val name = record.deviceName ?: return null
        if (nameHints.any { name.lowercase().contains(it) }) return "Nom : $name"
        return null
    }
}
