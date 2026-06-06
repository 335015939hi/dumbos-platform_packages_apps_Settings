package com.android.settings.spa.app.appinfo

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.GosPackageState
import android.content.pm.GosPackageStateFlag
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.dx.mockito.inline.extended.ExtendedMockito.doReturn
import com.android.settings.R
import com.android.settingslib.spaprivileged.model.app.userHandle
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.MockitoSession
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@RunWith(AndroidJUnit4::class)
class AppMicSpoofingPreferenceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var gosPackageState: GosPackageState

    private lateinit var mockSession: MockitoSession

    private val context: Context = spy(ApplicationProvider.getApplicationContext()) {
        doNothing().whenever(mock).startActivityAsUser(any(), any())
    }

    @Before
    fun setUp() {
        mockSession =
            ExtendedMockito.mockitoSession()
                .initMocks(this)
                .mockStatic(GosPackageState::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()

        doReturn(gosPackageState).`when`<GosPackageState> {
            GosPackageState.get(
                PACKAGE_NAME,
                USER_ID
            )
        }
    }

    @After
    fun tearDown() {
        mockSession.finishMocking()
    }

    @Test
    fun disabledFlag_displayedWithSwitchOffSummary() {
        whenever(gosPackageState.hasFlag(GosPackageStateFlag.MIC_SPOOFING_ENABLED))
            .thenReturn(false)

        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.microphone_spoofing))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.switch_off_text))
            .assertIsDisplayed()
    }

    @Test
    fun enabledFlag_displayed() {
        whenever(gosPackageState.hasFlag(GosPackageStateFlag.MIC_SPOOFING_ENABLED))
            .thenReturn(true)

        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.microphone_spoofing))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.switch_on_text))
            .assertIsDisplayed()
    }

    @Test
    fun onClick_startsConfigActivityIntent() {
        whenever(gosPackageState.hasFlag(GosPackageStateFlag.MIC_SPOOFING_ENABLED))
            .thenReturn(true)

        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.microphone_spoofing))
            .performClick()

        verify(context).startActivityAsUser(
            argThat { intent: Intent? ->
                intent != null &&
                        intent.component?.packageName == PERMISSION_CONTROLLER_PKG &&
                        intent.component?.className ==
                        "$PERMISSION_CONTROLLER_PKG.micspoofing.MicSpoofingActivity" &&
                        intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME) == PACKAGE_NAME
            },
            eq(APP.userHandle),
        )
    }

    private fun setContent() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                AppMicSpoofingPreference(APP)
            }
        }
    }

    private companion object {
        const val USER_ID = 0
        const val APP_ID = 12345
        const val PACKAGE_NAME = "test.package"
        const val PERMISSION_CONTROLLER_PKG = "com.android.permissioncontroller"

        val APP = ApplicationInfo().apply {
            packageName = PACKAGE_NAME
            uid = android.os.UserHandle.getUid(USER_ID, APP_ID)
            flags = ApplicationInfo.FLAG_INSTALLED
        }
    }
}
