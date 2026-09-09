package me.kavishdevar.librepods.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kavishdevar.librepods.BuildConfig
import me.kavishdevar.librepods.R

@Composable
fun PrivacyPolicyPage(
    onForward: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(42.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.privacy_policy_last_updated),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.privacy_policy_overview),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.privacy_policy_overview_no_collection),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_overview_on_device),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_third_party_services),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.privacy_policy_contact_methods),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_email),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_email_details),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_email_edit_before_sending),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Discord",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_discord_link),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_discord_no_info),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "GitHub Issues",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_github_issue_prefill),
                style = MaterialTheme.typography.bodyMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    stringResource(R.string.privacy_policy_issue_version_info),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.privacy_policy_issue_device_info),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.privacy_policy_issue_android_build),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.privacy_policy_issue_install_source),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stringResource(R.string.privacy_policy_issue_not_automatic),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.privacy_policy_payments), style = MaterialTheme.typography.titleLarge
            )

            if (BuildConfig.PLAY_BUILD) {
                Text(
                    text = "Google Play", style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.privacy_policy_google_play_purchases),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.privacy_policy_google_play_verification),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "GitHub Sponsors", style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.privacy_policy_github_sponsors_link),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.privacy_policy_github_sponsors_shared_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stringResource(R.string.contact), style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.privacy_policy_contact_details),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onForward,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.i_agree),
                    style = MaterialTheme.typography.labelMediumEmphasized
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
