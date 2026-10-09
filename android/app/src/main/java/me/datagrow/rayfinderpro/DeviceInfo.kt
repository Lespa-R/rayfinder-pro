package me.datagrow.rayfinderpro

import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid

/**
 * Ce que l'annonce Bluetooth d'un appareil permet de savoir sur lui.
 * Sans connexion à l'appareil : uniquement les données qu'il diffuse publiquement.
 */
data class DeviceInfo(
    val manufacturer: String? = null,
    val kind: String? = null,
    val services: List<String> = emptyList(),
    val txPower: Int? = null,
    val connectable: Boolean? = null,
    val rawManufacturer: String? = null,
) {
    /** Complète les champs manquants avec ceux d'une annonce précédente du même appareil. */
    fun mergedWith(prev: DeviceInfo?): DeviceInfo = if (prev == null) this else DeviceInfo(
        manufacturer = manufacturer ?: prev.manufacturer,
        kind = kind ?: prev.kind,
        services = (services + prev.services).distinct(),
        txPower = txPower ?: prev.txPower,
        connectable = connectable ?: prev.connectable,
        rawManufacturer = rawManufacturer ?: prev.rawManufacturer,
    )

    companion object {

        fun from(result: ScanResult): DeviceInfo {
            val record = result.scanRecord ?: return DeviceInfo(connectable = result.isConnectable)
            val msd = record.manufacturerSpecificData
            val companyId = if (msd.size() > 0) msd.keyAt(0) else null
            val payload = companyId?.let { msd.get(it) }
            val serviceIds = (record.serviceUuids.orEmpty() + record.serviceData.keys).mapNotNull { it.short16() }.distinct()

            val kind = appleKind(companyId, payload)
                ?: serviceIds.firstNotNullOfOrNull { SERVICE_KINDS[it] }
                ?: appearanceKind(record)
                ?: COMPANY_KINDS[companyId]

            return DeviceInfo(
                manufacturer = companyId?.let { COMPANIES[it] ?: "Fabricant n° 0x%04X".format(it) },
                kind = kind,
                services = serviceIds.map { SERVICE_NAMES[it] ?: "Service 0x%04X".format(it) },
                txPower = result.txPower.takeIf { it != ScanResult.TX_POWER_NOT_PRESENT }
                    ?: record.txPowerLevel.takeIf { it != Int.MIN_VALUE },
                connectable = result.isConnectable,
                rawManufacturer = payload?.let { bytes -> "%04X · ".format(companyId) + bytes.joinToString(" ") { "%02X".format(it) } },
            )
        }

        private fun ParcelUuid.short16(): Int? {
            val s = uuid.toString()
            return if (s.endsWith("-0000-1000-8000-00805f9b34fb")) s.substring(4, 8).toIntOrNull(16) else null
        }

        /** Apple encode le type d'annonce dans le premier octet de ses données fabricant. */
        private fun appleKind(companyId: Int?, payload: ByteArray?): String? {
            if (companyId != 0x004C || payload == null || payload.isEmpty()) return null
            return when (payload[0].toInt() and 0xFF) {
                0x02 -> "Balise iBeacon"
                0x05 -> "Appareil Apple (AirDrop)"
                0x07 -> "AirPods ou écouteurs Beats"
                0x09 -> "Récepteur AirPlay (Apple TV, enceinte)"
                0x0C -> "iPhone, iPad ou Mac (Handoff)"
                0x10 -> "iPhone, iPad ou Mac"
                0x12 -> "AirTag ou objet « Localiser »"
                else -> "Appareil Apple"
            }
        }

        /** Champ « Appearance » (type AD 0x19), absent de l'API ScanRecord : lu dans les octets bruts. */
        private fun appearanceKind(record: ScanRecord): String? {
            val bytes = record.bytes ?: return null
            var i = 0
            while (i < bytes.size) {
                val len = bytes[i].toInt() and 0xFF
                if (len == 0 || i + len >= bytes.size) break
                val type = bytes[i + 1].toInt() and 0xFF
                if (type == 0x19 && len >= 3) {
                    val value = (bytes[i + 2].toInt() and 0xFF) or ((bytes[i + 3].toInt() and 0xFF) shl 8)
                    return APPEARANCE[value shr 6]
                }
                i += len + 1
            }
            return null
        }

        // Identifiants fabricants officiels (Bluetooth SIG, « Company Identifiers »)
        private val COMPANIES = mapOf(
            0x0001 to "Nokia",
            0x0002 to "Intel",
            0x0006 to "Microsoft",
            0x000A to "Qualcomm (CSR)",
            0x000D to "Texas Instruments",
            0x000F to "Broadcom",
            0x001D to "Qualcomm",
            0x004C to "Apple",
            0x0057 to "Harman (JBL)",
            0x0059 to "Nordic Semiconductor",
            0x0075 to "Samsung",
            0x0087 to "Garmin",
            0x009E to "Bose",
            0x00E0 to "Google",
            0x012D to "Sony",
            0x0157 to "Huami (Amazfit)",
            0x0171 to "Amazon",
            0x01AB to "Meta",
            0x027D to "Huawei",
            0x02E5 to "Espressif",
            0x038F to "Xiaomi",
            0x0499 to "Ruuvi",
            0x058E to "Meta (Reality Labs)",
            0x0D53 to "Luxottica",
        )

        private val COMPANY_KINDS = mapOf(
            0x0006 to "PC Windows ou accessoire Microsoft",
            0x0075 to "Appareil Samsung Galaxy",
            0x00E0 to "Appareil Google",
            0x0087 to "Montre ou capteur Garmin",
            0x009E to "Casque ou enceinte Bose",
            0x0057 to "Casque ou enceinte JBL",
            0x012D to "Appareil Sony",
            0x0157 to "Montre ou bracelet Amazfit",
            0x0171 to "Appareil Amazon (Echo, Fire…)",
            0x027D to "Appareil Huawei",
            0x038F to "Appareil Xiaomi",
            0x0499 to "Capteur Ruuvi",
            0x02E5 to "Objet connecté (ESP32)",
        )

        // UUID de service 16 bits les plus courants
        private val SERVICE_NAMES = mapOf(
            0x1800 to "Accès générique",
            0x180A to "Informations sur l'appareil",
            0x180D to "Fréquence cardiaque",
            0x180F to "Batterie",
            0x1809 to "Thermomètre",
            0x1812 to "Clavier, souris ou manette (HID)",
            0x1814 to "Course à pied",
            0x1816 to "Vélo",
            0x181A to "Capteurs d'environnement",
            0x1826 to "Appareil de fitness",
            0xFD5F to "Meta",
            0xFEB7 to "Meta",
            0xFEB8 to "Meta",
            0xFD6F to "Notifications d'exposition",
            0xFE2C to "Google Fast Pair",
            0xFE9F to "Google",
            0xFE95 to "Xiaomi (MiBeacon)",
            0xFEAA to "Eddystone",
            0xFEEC to "Tile",
            0xFEED to "Tile",
        )

        private val SERVICE_KINDS = mapOf(
            0xFE2C to "Accessoire Android (écouteurs, enceinte…)",
            0xFD6F to "Smartphone (notifications d'exposition)",
            0xFEEC to "Traceur Tile",
            0xFEED to "Traceur Tile",
            0xFEAA to "Balise Eddystone",
            0xFE95 to "Appareil Xiaomi",
            0x180D to "Capteur de fréquence cardiaque",
            0x1812 to "Clavier, souris ou manette",
            0x1826 to "Appareil de fitness",
        )

        // Catégories « Appearance » (Bluetooth SIG, Assigned Numbers)
        private val APPEARANCE = mapOf(
            0x001 to "Téléphone",
            0x002 to "Ordinateur",
            0x003 to "Montre",
            0x004 to "Horloge",
            0x005 to "Écran",
            0x006 to "Télécommande",
            0x007 to "Lunettes",
            0x008 to "Étiquette (tag)",
            0x009 to "Porte-clés",
            0x00A to "Lecteur multimédia",
            0x00C to "Thermomètre",
            0x00D to "Capteur cardiaque",
            0x00F to "Clavier, souris ou manette",
            0x011 to "Capteur de course",
            0x012 to "Capteur de vélo",
            0x015 to "Capteur",
            0x01F to "Lampe connectée",
            0x021 to "Enceinte ou récepteur audio",
            0x022 to "Source audio",
            0x023 to "Véhicule",
            0x024 to "Électroménager",
            0x025 to "Écouteurs ou casque",
            0x027 to "Équipement audio-vidéo",
            0x029 to "Appareil auditif",
            0x02A to "Console ou manette de jeu",
            0x031 to "Oxymètre",
            0x032 to "Balance",
        )
    }
}
