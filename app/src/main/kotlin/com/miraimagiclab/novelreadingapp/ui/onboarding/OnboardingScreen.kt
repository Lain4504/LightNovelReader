
package com.miraimagiclab.novelreadingapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.miraimagiclab.novelreadingapp.theme.LightNovelReaderTheme

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Light Novel Reader!")
        Button(onClick = onGetStarted) {
            Text("Get Started")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    LightNovelReaderTheme(
        darkMode = "FollowSystem",
        lightThemeName = "light_default",
        darkThemeName = "dark_default",
        appLocale = "vi-VN"
    ) {
        OnboardingScreen(onGetStarted = {})
    }
}
