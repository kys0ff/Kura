package off.kys.kura.features.main.data.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import off.kys.kura.features.main.data.Badge

object BadgeSerializer : KSerializer<Badge> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Badge", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Badge) {
        encoder.encodeInt(value.index)
    }

    override fun deserialize(decoder: Decoder): Badge {
        val index = decoder.decodeInt()
        return Badge.fromInt(index)
    }
}