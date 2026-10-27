package com.example.generator

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.Project
import com.example.model.ProjectFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExportManager {

    fun createProjectZip(context: Context, project: Project, files: List<ProjectFile>): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val sanitizedTitle = project.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val zipFile = File(exportDir, "Flota_${sanitizedTitle}_v${project.versionNumber}.zip")
            if (zipFile.exists()) zipFile.delete()

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                files.forEach { file ->
                    val entry = ZipEntry(file.path)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }

            if (zipFile.exists() && zipFile.length() > 0) {
                zipFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareProjectZip(context: Context, zipFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Flota Website Source Code: ${zipFile.name}")
                putExtra(Intent.EXTRA_TEXT, "Here is the complete full-stack website export created with Flota AI.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Source Code ZIP").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback: simple send without FileProvider if provider not declared
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Flota Website Source Code: ${zipFile.name}")
                putExtra(Intent.EXTRA_TEXT, "Exported: ${zipFile.name} (${zipFile.length()} bytes)")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Project").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
