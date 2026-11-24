package com.oaa.livingresume.ui.screens.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.audio.Sfx
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.supabase.displayNameOf
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.share.ResumePdfRenderer
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.InitialsAvatar
import com.oaa.livingresume.ui.components.PillarTag
import com.oaa.livingresume.ui.components.QrCodeImage
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.motion.playSfx
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DangerRed
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.GreenGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.NavySurface
import com.oaa.livingresume.ui.theme.OnAccent
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.viewmodel.OaaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tab 5 - Share & Verify:
 *  1. ZXing QR code + native ACTION_SEND share of the live Recruiter OAA Resume link
 *  2. "Submit New Proof" bottom sheet (pillar, title/link, certificate via GetContent)
 *  3. "Verifier Mode" toggle - faculty/club approval queue that recalculates scores live
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    viewModel: OaaViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var showSubmitSheet by remember { mutableStateOf(false) }
    var verifierMode by remember { mutableStateOf(false) }

    val resumeUrl = "https://oaa.app/r/${me.slug}"

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth().entrance(0)) {
                    Eyebrow("OAA // 証明 Share & Verify")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Share & Verify",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Your live recruiter resume, proof submissions and the verifier queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            item {
                Box(Modifier.fillMaxWidth().entrance(1)) {
                    QrShareCard(
                        me = me,
                        resumeUrl = resumeUrl,
                        proofs = state.submissions,
                        onShared = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        },
                    )
                }
            }

            item {
                Box(Modifier.fillMaxWidth().entrance(2)) {
                    SubmitProofCard(
                        pendingForMe = state.submissions.count {
                            it.studentId == me.id && it.status == ProofStatus.PENDING
                        },
                        onClick = { showSubmitSheet = true },
                    )
                }
            }

            item {
                Box(Modifier.fillMaxWidth().entrance(3)) {
                    VerifierCard(
                    verifierMode = verifierMode,
                    pending = state.pendingSubmissions,
                    approvedCount = state.submissions.count { it.status == ProofStatus.APPROVED },
                    rejectedCount = state.submissions.count { it.status == ProofStatus.REJECTED },
                    me = me,
                    onToggle = { verifierMode = it },
                    onApprove = { submission ->
                        viewModel.decide(submission.id, approve = true)
                        playSfx(Sfx.APPROVE)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch {
                            snackbarHostState.showSnackbar(approvalMessage(submission, me))
                        }
                    },
                    onReject = { submission ->
                        viewModel.decide(
                            submission.id,
                            approve = false,
                            note = "Rejected from verifier mode - ask for a clearer, verifiable link.",
                        )
                        playSfx(Sfx.REJECT)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            snackbarHostState.showSnackbar("Rejected: ${submission.title} - ${submission.studentName} notified.")
                        }
                    },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    if (showSubmitSheet) {
        SubmitProofSheet(
            onDismiss = { showSubmitSheet = false },
            onSubmit = { pillar, title, link, points, attachment ->
                viewModel.submitProof(pillar, title, link, points, attachment)
                playSfx(Sfx.SUBMIT)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showSubmitSheet = false
                scope.launch {
                    snackbarHostState.showSnackbar("Proof submitted - pending verifier approval")
                }
            },
        )
    }
}

/** Human-readable, score-aware approval message. */
private fun approvalMessage(submission: ProofSubmission, me: Student): String {
    return if (submission.studentId == me.id) {
        val scores = me.pillarScores.toMutableMap()
        scores[submission.pillar] =
            ((scores[submission.pillar] ?: 0.0) + submission.points).coerceAtMost(100.0)
        val newOverall = OaaEngine.round1(OaaEngine.overall(scores))
        val newGrade = OaaEngine.gradeFor(newOverall)
        "Approved +${submission.points} ${submission.pillar.short} -> " +
            "${scores[submission.pillar]?.toInt()} · overall $newOverall (${newGrade.label})"
    } else {
        "Approved +${submission.points} ${submission.pillar.short} for ${submission.studentName} - OAA recalculated."
    }
}

// ------------------------------------------------------------------ QR card

@Composable
private fun QrShareCard(
    me: Student,
    resumeUrl: String,
    proofs: List<ProofSubmission>,
    onShared: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pdfBusy by remember { mutableStateOf(false) }
    HudCard(borderColor = CyanGlow.copy(alpha = 0.45f)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Eyebrow("Recruiter OAA Resume")
            Spacer(Modifier.height(4.dp))
            Text(
                text = me.name,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = "${me.targetRole.label} · Batch of ${me.gradYear}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(Modifier.height(14.dp))
            QrCodeImage(content = resumeUrl, codeSize = 196.dp)

            Spacer(Modifier.height(10.dp))
            Text(
                text = resumeUrl,
                style = MaterialTheme.typography.labelLarge,
                color = CyanGlow,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Scanners see your verified OAA grade, radar chart, 4-year growth curve " +
                    "and approved proofs - never your private Improve notes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "My verified OAA Living Resume: $resumeUrl",
                            )
                        }
                        runCatching {
                            context.startActivity(Intent.createChooser(send, "Share resume via"))
                        }
                    },
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share link")
                }
                OutlinedButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OAA resume link", resumeUrl))
                    },
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy")
                }
            }

            Spacer(Modifier.height(10.dp))
            // Rendered A4 PDF shared through the system sheet (WhatsApp, LinkedIn, Gmail...)
            Button(
                onClick = {
                    if (pdfBusy) return@Button
                    pdfBusy = true
                    scope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.IO) {
                                val file = ResumePdfRenderer.render(context, me, proofs)
                                ResumePdfRenderer.share(context, file)
                            }
                        }
                        pdfBusy = false
                        onShared(
                            result.exceptionOrNull()?.message
                                ?: "PDF resume shared",
                        )
                    }
                },
                enabled = !pdfBusy,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GoldGlow,
                    contentColor = OnAccent,
                ),
            ) {
                if (pdfBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = OnAccent,
                    )
                } else {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share PDF resume", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ---------------------------------------------------------- submit proof card

@Composable
private fun SubmitProofCard(pendingForMe: Int, onClick: () -> Unit) {
    HudCard {
        SectionHeader(
            title = "Submit New Proof",
            subtitle = "Hackathon links, GitHub repos, club certificates, internships - " +
                "pick a pillar and a verifier will approve it.",
        )
        if (pendingForMe > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$pendingForMe of your submissions pending verification",
                style = MaterialTheme.typography.labelMedium,
                color = GoldGlow,
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Submit a proof")
        }
    }
}

// ------------------------------------------------------------- verifier queue

@Composable
private fun VerifierCard(
    verifierMode: Boolean,
    pending: List<ProofSubmission>,
    approvedCount: Int,
    rejectedCount: Int,
    me: Student,
    onToggle: (Boolean) -> Unit,
    onApprove: (ProofSubmission) -> Unit,
    onReject: (ProofSubmission) -> Unit,
) {
    HudCard(borderColor = if (verifierMode) CyanGlow.copy(alpha = 0.5f) else OutlineFaint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "VERIFIER_MODE // 待機中",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (verifierMode) CyanGlow else TextPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Preview the faculty / club-head approval queue. " +
                        "Approving recalculates the student's OAA score live.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.width(10.dp))
            Switch(
                checked = verifierMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = com.oaa.livingresume.ui.theme.DeepNavy,
                    checkedTrackColor = CyanGlow,
                    checkedBorderColor = CyanGlow,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = NavySurface,
                    uncheckedBorderColor = OutlineFaint,
                ),
            )
        }

        if (verifierMode) {
            Spacer(Modifier.height(12.dp))
            // Terminal status line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (pending.isNotEmpty()) GoldGlow else GreenGlow),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "> queue ${pending.size} pending :: " +
                        "$approvedCount approved :: $rejectedCount rejected",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(10.dp))

            if (pending.isEmpty()) {
                Text(
                    text = "> queue clear. awaiting submissions_",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = GreenGlow,
                )
            }

            pending.forEach { submission ->
                ProofQueueItem(
                    submission = submission,
                    isMine = submission.studentId == me.id,
                    onApprove = { onApprove(submission) },
                    onReject = { onReject(submission) },
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ProofQueueItem(
    submission: ProofSubmission,
    isMine: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.CutCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, OutlineFaint, androidx.compose.foundation.shape.CutCornerShape(10.dp))
            .padding(12.dp),
    ) {
        // Terminal-style request header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "> REQ ${submission.id.take(8).uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = CyanGlow.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                text = "${submission.points} PTS",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = GoldGlow,
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(name = submission.studentName, color = submission.pillar.accent, size = 34.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = submission.studentName,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        maxLines = 1,
                    )
                    if (isMine) {
                        Spacer(Modifier.width(6.dp))
                        Text("· you", style = MaterialTheme.typography.labelSmall, color = GoldGlow)
                    }
                }
                Text(
                    text = "${submission.submittedAt} · ${submission.pillar.short} +${submission.points}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            PillarTag(submission.pillar)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = submission.title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        if (submission.link != "-") {
            Spacer(Modifier.height(2.dp))
            Text(
                text = submission.link,
                style = MaterialTheme.typography.bodySmall,
                color = CyanGlow,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        submission.attachment?.let { name ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(name, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onApprove) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = GreenGlow,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "[ APPROVE ]",
                    color = GreenGlow,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
            OutlinedButton(onClick = onReject) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "[ REJECT ]",
                    color = DangerRed,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ----------------------------------------------------------- submission sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitProofSheet(
    onDismiss: () -> Unit,
    onSubmit: (pillar: Pillar, title: String, link: String, points: Int, attachment: String?) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pillar by remember { mutableStateOf(Pillar.ADAPTABILITY) }
    var title by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var points by remember { mutableIntStateOf(5) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = displayNameOf(context, uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavySurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Submit New Proof", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text(
                text = "Faculty / club heads verify it - approved proofs instantly recalculate your OAA score.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Text("Pillar", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Pillar.entries.forEach { p ->
                    FilterChip(
                        selected = pillar == p,
                        onClick = { pillar = p },
                        label = { Text(p.short) },
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (e.g. SIH 2026 Zonal Winner)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = proofFieldColors(),
            )
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Link (GitHub, Devfolio, certificate URL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = proofFieldColors(),
            )

            Text("Suggested points", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 8).forEach { value ->
                    FilterChip(
                        selected = points == value,
                        onClick = { points = value },
                        label = { Text("+$value") },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { picker.launch("*/*") }) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(attachmentName ?: "Attach certificate")
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (attachmentUri != null) "Ready" else "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            Button(
                onClick = {
                    onSubmit(pillar, title, link, points, attachmentName)
                    title = ""
                    link = ""
                    attachmentName = null
                    attachmentUri = null
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Verified, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Submit for verification")
            }
        }
    }
}

@Composable
private fun proofFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanGlow,
    unfocusedBorderColor = OutlineFaint,
    focusedLabelColor = CyanGlow,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanGlow,
)
