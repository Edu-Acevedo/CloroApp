package cl.duoc.clorocontrol.data.evidence

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** Copia las evidencias al almacenamiento privado de la app para que sigan disponibles sin Internet. */
object EvidenceStorage {
    suspend fun saveBitmap(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val directory = evidenceDirectory(context)
        val target = File(directory, "camera_${UUID.randomUUID()}.jpg")
        FileOutputStream(target).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            output.flush()
        }
        target.absolutePath
    }

    suspend fun copyUri(context: Context, source: Uri): String = withContext(Dispatchers.IO) {
        val directory = evidenceDirectory(context)
        val extension = when (context.contentResolver.getType(source)?.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic", "image/heif" -> "heic"
            else -> "jpg"
        }
        val target = File(directory, "gallery_${UUID.randomUUID()}.$extension")
        val input = context.contentResolver.openInputStream(source)
            ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada.")
        input.use { inputStream ->
            target.outputStream().use { output -> inputStream.copyTo(output) }
        }
        target.absolutePath
    }

    private fun evidenceDirectory(context: Context): File =
        File(context.filesDir, "evidence").apply { mkdirs() }
}
