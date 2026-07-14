package com.example.businesscardholder.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ParsedCard(
    val contactName: String = "",
    val companyName: String = "",
    val jobTitle: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val rawText: String = ""
)

object OcrHelper {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val phoneRegex = Regex("(\\+?\\d[\\d\\s().-]{6,}\\d)")
    private val websiteRegex = Regex(
        "((https?://)?(www\\.)?[A-Za-z0-9-]+\\.(com|net|org|io|co|in|biz|info)(\\.[A-Za-z]{2})?)",
        RegexOption.IGNORE_CASE
    )
    private val jobTitleKeywords = listOf(
        "manager", "director", "ceo", "cto", "cfo", "founder", "co-founder", "president",
        "engineer", "developer", "designer", "consultant", "executive", "officer", "head",
        "lead", "owner", "sales", "marketing", "hr", "specialist", "analyst", "architect"
    )

    /** Runs on-device OCR on a captured bitmap. Fully offline. */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                cont.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    /**
     * Very simple heuristic parser for typical business card layouts.
     * This is a best-effort guess only — the app always shows the result
     * in an editable form so the user can correct anything.
     */
    fun parse(rawText: String): ParsedCard {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val email = emailRegex.find(rawText)?.value ?: ""
        val website = websiteRegex.find(rawText.replace(email, ""))?.value ?: ""
        val phone = phoneRegex.find(rawText)?.value?.trim() ?: ""

        var jobTitle = ""
        var contactName = ""
        var companyName = ""

        val remainingLines = lines.filterNot { line ->
            line.contains(email) && email.isNotEmpty() ||
                (website.isNotEmpty() && line.contains(website)) ||
                (phone.isNotEmpty() && line.contains(phone))
        }

        // Find a job title line by keyword match
        val jobLineIndex = remainingLines.indexOfFirst { line ->
            jobTitleKeywords.any { kw -> line.lowercase().contains(kw) }
        }
        if (jobLineIndex >= 0) {
            jobTitle = remainingLines[jobLineIndex]
        }

        // Heuristic: first remaining line (that isn't the job title) is often the person's name,
        // and the next distinct line is often the company name.
        val candidateLines = remainingLines.filterIndexed { idx, _ -> idx != jobLineIndex }
        if (candidateLines.isNotEmpty()) {
            contactName = candidateLines[0]
        }
        if (candidateLines.size > 1) {
            companyName = candidateLines[1]
        }

        // Address: any remaining line(s) not already used, joined together (best effort)
        val used = setOf(contactName, companyName, jobTitle)
        val address = remainingLines
            .filterNot { it in used }
            .joinToString(", ")

        return ParsedCard(
            contactName = contactName,
            companyName = companyName,
            jobTitle = jobTitle,
            phoneNumber = phone,
            email = email,
            website = website,
            address = address,
            rawText = rawText
        )
    }
}
