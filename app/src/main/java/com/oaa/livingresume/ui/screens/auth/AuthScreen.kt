package com.oaa.livingresume.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DeepNavy
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.IndigoGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.OnAccent
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.DangerRed
import kotlin.math.cos
import kotlin.math.sin

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

/**
 * Tab-gated mock login screen: animated OAA logo, HUD credentials panel,
 * one-tap demo sign-in. Session persists via [com.oaa.livingresume.data.auth.SessionStore].
 */
@Composable
fun AuthScreen(
    authError: String?,
    authBusy: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onFieldChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val submit: () -> Unit = {
        onLogin(email.trim(), password)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        com.oaa.livingresume.ui.components.HudGridBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthLogo(Modifier.size(128.dp))

            Spacer(Modifier.height(16.dp))
            Text(
                text = "OAA",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanGlow,
                letterSpacing = 12.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "OVERALL ASSESSMENT OF ABILITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "LIVING RESUME · CLASS OF 2027",
                style = MaterialTheme.typography.labelSmall,
                color = GoldGlow,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(26.dp))
            HudCard(borderColor = OutlineFaint, background = NavyCard) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        onFieldChange()
                    },
                    label = { Text("College email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = CyanGlow) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors(),
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onFieldChange()
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = CyanGlow) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors(),
                )

                if (authError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed,
                    )
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = submit,
                    enabled = !authBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = CyanGlow,
                            spotColor = CyanGlow,
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = OnAccent,
                    ),
                ) {
                    if (authBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = OnAccent,
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.height(0.dp))
                        Text(
                            text = "SIGN IN",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        email = "aarav.sharma@vit.edu"
                        password = "demo1234"
                        onLogin("aarav.sharma@vit.edu", "demo1234")
                    },
                    enabled = !authBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldGlow.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "CONTINUE AS DEMO STUDENT",
                        color = GoldGlow,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Demo accounts: aarav.sharma@vit.edu · ishita.reddy@vit.edu\n(any password, 4+ characters)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Mock auth — nothing leaves this device",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.7f),
            )
        }
    }
}

/** Spinning diamond HUD logo with a pulsing core — reused by the boot splash. */
@Composable
fun AuthLogo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "auth-logo")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing)),
        label = "rotation",
    )
    val counterRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(9_000, easing = LinearEasing)),
        label = "counter-rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Faint axis cross
        drawLine(
            color = OutlineFaint,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = OutlineFaint,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // Outer diamond - slow clockwise spin
        drawPath(
            path = diamondPath(center, radius * 0.94f, rotation),
            color = CyanGlow,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        // Inner diamond - faster counter spin
        drawPath(
            path = diamondPath(center, radius * 0.62f, counterRotation),
            color = IndigoGlow,
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
        )
        // Core glow + node
        drawCircle(
            color = GoldGlow.copy(alpha = 0.22f),
            radius = 12.dp.toPx() * pulse,
            center = center,
        )
        drawCircle(
            color = GoldGlow.copy(alpha = 0.55f),
            radius = 7.dp.toPx() * pulse,
            center = center,
        )
        drawCircle(
            color = CyanGlow,
            radius = 4.5.dp.toPx(),
            center = center,
        )
    }
}

private fun diamondPath(center: Offset, radius: Float, rotationDeg: Float): Path {
    val path = Path()
    for (i in 0..3) {
        val angle = Math.toRadians(rotationDeg - 90.0 + 90.0 * i)
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanGlow,
    unfocusedBorderColor = OutlineFaint,
    focusedLabelColor = CyanGlow,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanGlow,
    focusedLeadingIconColor = CyanGlow,
    unfocusedLeadingIconColor = TextSecondary,
    focusedContainerColor = NavyCard,
    unfocusedContainerColor = NavyCard,
)
