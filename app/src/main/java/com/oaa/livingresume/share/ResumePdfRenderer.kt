package com.oaa.livingresume.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders the Recruiter OAA Resume as a single A4 PDF page:
 * header + grade, 4-axis radar, pillar bars, badges, approved proofs,
 * growth summary and a footer QR code linking to the live resume.
 */
object ResumePdfRenderer {

    // Page: A4 at 72dpi in points
    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN = 40f

    private const val BG = 0xFF090D16.toInt()
    private const val CARD = 0xFF111A2C.toInt()
    private const val CYAN = 0xFF00E5FF.toInt()
    private const val GOLD = 0xFFFFD54F.toInt()
    private const val INDIGO = 0xFF6C63FF.toInt()
    private const val GREEN = 0xFF3DDC97.toInt()
    private const val TEXT = 0xFFE6EEF9.toInt()
    private const val TEXT_DIM = 0xFF8D9AB0.toInt()
    private const val OUTLINE = 0xFF1F2B45.toInt()

    private val pillarColors = mapOf(
        Pillar.ACADEMIC to CYAN,
        Pillar.ADAPTABILITY to INDIGO,
        Pillar.SOCIAL to GOLD,
        Pillar.PHYSICAL to GREEN,
    )

    private fun gradeColor(grade: Grade): Int = when (grade) {
        Grade.A_PLUS, Grade.A -> GOLD
        Grade.B_PLUS, Grade.B -> CYAN
        Grade.C -> 0xFFFFB74D.toInt()
        Grade.D -> 0xFFFF8A65.toInt()
        Grade.E -> 0xFFFF5C7A.toInt()
    }

    /**
     * Renders the PDF into the cache dir and returns the file.
     * Throws on failure so the caller can surface an error.
     */
    fun render(context: Context, student: Student, proofs: List<ProofSubmission>): File {
        val doc = android.graphics.pdf.PdfDocument()
        val page = doc.startPage(
            android.graphics.pdf.PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create(),
        )
        val canvas = page.canvas
        val mono = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val sans = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val sansBold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var y = drawHeader(canvas, student, sans, sansBold, monoBold)
        y = drawRadar(canvas, student, y)
        y = drawPillars(canvas, student, y, mono, sans)
        y = drawBadges(canvas, student, y, sans, mono)
        y = drawProofs(canvas, student, proofs, y, sans, mono)
        drawFooter(canvas, student, y, sans, mono)

        doc.finishPage(page)
        val dir = File(context.cacheDir, "resume").apply { mkdirs() }
        val out = File(dir, "oaa_resume_${student.slug}.pdf")
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
        return out
    }

    /** Shares the rendered PDF through the system share sheet. */
    fun share(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "OAA Living Resume - ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share resume PDF"))
    }

    // ---------------------------------------------------------------- header

    private fun drawHeader(
        canvas: Canvas,
        student: Student,
        sans: Typeface,
        sansBold: Typeface,
        monoBold: Typeface,
    ): Float {
        val paint = Paint().apply { color = BG }
        canvas.drawRect(0f, 0f, PAGE_W, PAGE_H, paint)

        // Accent bar
        paint.color = CYAN
        canvas.drawRect(MARGIN, 32f, MARGIN + 4f, 118f, paint)

        val t = Paint().apply { color = TEXT; typeface = sansBold; textSize = 26f }
        canvas.drawText(student.name, MARGIN + 14f, 56f, t)

        val sub = Paint().apply { color = TEXT_DIM; typeface = sans; textSize = 11f }
        canvas.drawText(student.branch, MARGIN + 14f, 74f, sub)
        canvas.drawText(
            "Roll ${student.rollNo}  |  Batch of ${student.gradYear}  |  ${student.college}",
            MARGIN + 14f, 89f, sub,
        )
        canvas.drawText(
            "Target: ${student.targetRole.label}  |  CGPA ${student.cgpa}",
            MARGIN + 14f, 104f, sub,
        )

        // Grade badge top-right
        val overall = OaaEngine.overallRounded(student.pillarScores)
        val grade = OaaEngine.gradeFor(overall)
        val badgePaint = Paint().apply {
            color = gradeColor(grade)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val badgeRect = RectF(PAGE_W - MARGIN - 92f, 34f, PAGE_W - MARGIN, 96f)
        canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)
        val g = Paint().apply {
            color = gradeColor(grade); typeface = monoBold; textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(grade.label, badgeRect.centerX(), 74f, g)
        val gv = Paint().apply {
            color = TEXT_DIM; textSize = 9f; textAlign = Paint.Align.CENTER; typeface = sans
        }
        canvas.drawText("$overall / 100", badgeRect.centerX(), 90f, gv)

        return 126f
    }

    // ----------------------------------------------------------------- radar

    private fun drawRadar(canvas: Canvas, student: Student, top: Float): Float {
        val height = 168f
        val card = Paint().apply { color = CARD }
        val rect = RectF(MARGIN, top, PAGE_W - MARGIN, top + height)
        canvas.drawRoundRect(rect, 8f, 8f, card)

        val label = Paint().apply {
            color = CYAN; textSize = 8f; typeface = sansBoldFake(); letterSpacing = 1.5f
        }
        canvas.drawText("4-AXIS OAA PROFILE", MARGIN + 12f, top + 18f, label)

        val center = PAGE_W / 2f
        val cy = top + height / 2f + 6f
        val radius = 58f
        val pillars = Pillar.entries

        fun vertex(index: Int, fraction: Float): Pair<Float, Float> {
            val angle = Math.toRadians(-90.0 + 90.0 * index)
            val r = radius * fraction
            return (center + r * cos(angle)).toFloat() to (cy + r * sin(angle)).toFloat()
        }

        val stroke = Paint().apply {
            color = OUTLINE; style = Paint.Style.STROKE; strokeWidth = 1f
        }
        listOf(0.33f, 0.66f, 1f).forEach { ring ->
            val p = Path()
            pillars.indices.forEach { i ->
                val (x, yv) = vertex(i, ring)
                if (i == 0) p.moveTo(x, yv) else p.lineTo(x, yv)
            }
            p.close()
            canvas.drawPath(p, stroke)
        }
        pillars.indices.forEach { i ->
            val (x, yv) = vertex(i, 1f)
            canvas.drawLine(center, cy, x, yv, stroke)
        }

        // Data polygon
        val data = Path()
        pillars.forEachIndexed { i, pillar ->
            val f = ((student.pillarScores[pillar] ?: 0.0) / 100.0).toFloat()
            val (x, yv) = vertex(i, f)
            if (i == 0) data.moveTo(x, yv) else data.lineTo(x, yv)
        }
        data.close()
        canvas.drawPath(
            data,
            Paint().apply { color = CYAN; style = Paint.Style.FILL; alpha = 46 },
        )
        canvas.drawPath(
            data,
            Paint().apply { color = CYAN; style = Paint.Style.STROKE; strokeWidth = 2f },
        )
        pillars.forEachIndexed { i, pillar ->
            val f = ((student.pillarScores[pillar] ?: 0.0) / 100.0).toFloat()
            val (x, yv) = vertex(i, f)
            canvas.drawCircle(
                x, yv, 3.5f,
                Paint().apply { color = pillarColors.getValue(pillar) },
            )
        }

        // Axis labels with scores
        val lp = Paint().apply { color = TEXT_DIM; textSize = 8.5f; textAlign = Paint.Align.CENTER }
        canvas.drawText("Academic ${scoreOf(student, Pillar.ACADEMIC)}", center, top + 34f, lp)
        canvas.drawText("Adapt ${scoreOf(student, Pillar.ADAPTABILITY)}", PAGE_W - MARGIN - 56f, cy + 3f, lp)
        canvas.drawText("Social ${scoreOf(student, Pillar.SOCIAL)}", center, top + height - 8f, lp)
        canvas.drawText("Fit ${scoreOf(student, Pillar.PHYSICAL)}", MARGIN + 56f, cy + 3f, lp)

        return top + height + 12f
    }

    private fun scoreOf(student: Student, pillar: Pillar): Int =
        (student.pillarScores[pillar] ?: 0.0).toInt()

    private fun sansBoldFake(): Typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

    // --------------------------------------------------------------- pillars

    private fun drawPillars(
        canvas: Canvas,
        student: Student,
        top: Float,
        mono: Typeface,
        sans: Typeface,
    ): Float {
        val rowH = 26f
        val height = rowH * 4 + 34f
        canvas.drawRect(MARGIN, top, PAGE_W - MARGIN, top + height, Paint().apply { color = CARD })

        val head = Paint().apply { color = CYAN; textSize = 8f; typeface = sansBoldFake() }
        canvas.drawText("PILLAR BREAKDOWN (WEIGHTED 35/30/20/15)", MARGIN + 12f, top + 18f, head)

        var y = top + 34f
        val barLeft = MARGIN + 150f
        val barRight = PAGE_W - MARGIN - 70f

        Pillar.entries.forEach { pillar ->
            val score = student.pillarScores[pillar] ?: 0.0
            val tint = pillarColors.getValue(pillar)

            val name = Paint().apply { color = TEXT; textSize = 9.5f; typeface = sans }
            canvas.drawText(
                "${pillar.short} ${pillar.weightPercent}%",
                MARGIN + 12f, y + 4f, name,
            )

            // Track
            canvas.drawRoundRect(
                RectF(barLeft, y - 4f, barRight, y + 4f),
                4f, 4f,
                Paint().apply { color = OUTLINE },
            )
            // Fill
            val w = (barRight - barLeft) * (score / 100.0).toFloat()
            if (w > 0f) {
                canvas.drawRoundRect(
                    RectF(barLeft, y - 4f, barLeft + w, y + 4f),
                    4f, 4f,
                    Paint().apply { color = tint },
                )
            }
            val valPaint = Paint().apply {
                color = tint; textSize = 9.5f; typeface = mono
                textAlign = Paint.Align.RIGHT
            }
            val grade = OaaEngine.gradeFor(score).label
            canvas.drawText("${score.toInt()} $grade", PAGE_W - MARGIN - 12f, y + 4f, valPaint)

            y += rowH
        }
        return top + height + 12f
    }

    // ---------------------------------------------------------------- badges

    private fun drawBadges(
        canvas: Canvas,
        student: Student,
        top: Float,
        sans: Typeface,
        mono: Typeface,
    ): Float {
        if (student.badges.isEmpty()) return top
        val head = Paint().apply { color = CYAN; textSize = 8f; typeface = sansBoldFake() }
        canvas.drawText("VERIFIED SKILL BADGES", MARGIN, top + 8f, head)

        var x = MARGIN
        var y = top + 24f
        val h = 18f
        student.badges.forEach { badge ->
            val textPaint = Paint().apply { color = TEXT; textSize = 8f; typeface = sans }
            val w = textPaint.measureText(badge.title) + 22f
            if (x + w > PAGE_W - MARGIN) {
                x = MARGIN
                y += h + 6f
            }
            val rect = RectF(x, y - 12f, x + w, y + 6f)
            canvas.drawRoundRect(
                rect, 9f, 9f,
                Paint().apply { color = CARD; style = Paint.Style.STROKE; strokeWidth = 1f; color = OUTLINE },
            )
            canvas.drawText(badge.title, x + 11f, y, textPaint)
            x += w + 8f
        }
        return y + 18f
    }

    // ---------------------------------------------------------------- proofs

    private fun drawProofs(
        canvas: Canvas,
        student: Student,
        proofs: List<ProofSubmission>,
        top: Float,
        sans: Typeface,
        mono: Typeface,
    ): Float {
        val approved = proofs.filter {
            it.studentId == student.id && it.status == ProofStatus.APPROVED
        }
        val head = Paint().apply { color = CYAN; textSize = 8f; typeface = sansBoldFake() }
        canvas.drawText("APPROVED PROOFS", MARGIN, top + 8f, head)

        var y = top + 24f
        if (approved.isEmpty()) {
            val dim = Paint().apply { color = TEXT_DIM; textSize = 9f; typeface = sans }
            canvas.drawText("No proofs verified yet.", MARGIN, y, dim)
            return y + 14f
        }
        approved.forEach { proof ->
            canvas.drawCircle(MARGIN + 3f, y - 3f, 3f, Paint().apply { color = GREEN })
            val t = Paint().apply { color = TEXT; textSize = 9f; typeface = sans }
            canvas.drawText(proof.title, MARGIN + 12f, y, t)
            val pts = Paint().apply {
                color = GREEN; textSize = 9f; typeface = mono; textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(
                "+${proof.points} ${proof.pillar.short}",
                PAGE_W - MARGIN, y, pts,
            )
            y += 14f
        }
        return y + 8f
    }

    // ---------------------------------------------------------------- footer

    private fun drawFooter(
        canvas: Canvas,
        student: Student,
        top: Float,
        sans: Typeface,
        mono: Typeface,
    ) {
        val y = min(top + 20f, PAGE_H - MARGIN - 74f)

        // Divider
        canvas.drawLine(
            MARGIN, y - 14f, PAGE_W - MARGIN, y - 14f,
            Paint().apply { color = OUTLINE; strokeWidth = 1f },
        )

        val url = "https://oaa.app/r/${student.slug}"
        val link = Paint().apply { color = CYAN; textSize = 10f; typeface = mono }
        canvas.drawText("Scan for the live resume", MARGIN, y + 4f, link)
        canvas.drawText(url, MARGIN, y + 20f, link.apply { textSize = 8.5f; color = TEXT_DIM })

        val note = Paint().apply { color = TEXT_DIM; textSize = 7.5f; typeface = sans }
        canvas.drawText(
            "Scores verified by faculty / club verifiers. Private Improve notes are never shared.",
            MARGIN, y + 40f, note,
        )
        canvas.drawText(
            "Generated by OAA Living Resume",
            MARGIN, y + 52f, note,
        )

        // QR code
        val qr: Bitmap? = QrCodeGenerator.encode(url, 240)
        if (qr != null) {
            val qrSize = 64f
            val dst = RectF(
                PAGE_W - MARGIN - qrSize, y - 10f,
                PAGE_W - MARGIN, y - 10f + qrSize,
            )
            // White plate behind QR
            canvas.drawRoundRect(
                RectF(dst.left - 4f, dst.top - 4f, dst.right + 4f, dst.bottom + 4f),
                6f, 6f,
                Paint().apply { color = Color.WHITE },
            )
            canvas.drawBitmap(qr, null, dst, Paint().apply { isFilterBitmap = true })
        }
    }
}
