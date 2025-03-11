package com.android.settings.password.generate

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.app.admin.PasswordMetrics
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.android.internal.widget.LockPatternUtils
import com.android.settings.R
import com.android.settings.SettingsActivity
import com.android.settings.SetupWizardUtils
import com.android.settings.password.ChooseLockGeneric
import com.android.settings.password.ChooseLockPassword
import com.android.settings.password.ChooseLockSettingsHelper
import com.android.settings.password.ConfirmDeviceCredentialUtils
import com.android.settings.password.SetupChooseLockPassword
import com.google.android.setupcompat.util.WizardManagerHelper
import com.google.android.setupdesign.util.ThemeHelper
import kotlinx.coroutines.flow.filterNotNull

private const val TAG = "GenerateLockPassActivity"

class GenerateLockPasswordActivity : SettingsActivity() {
    companion object {
        /**
         * The original AOSP component to launch for the PIN/password input activity. Will be
         * used for final validation and also if the user wants to select their own password / PIN.
         */
        const val EXTRA_ORIGINAL_CLASS_COMPONENT = "original_class_component"

        /**
         * Gets the original AOSP intent to launch the activity that we intercepted the extras for.
         */
        fun getAospPasswordIntent(activity: Activity): Intent {
            val originalComponent: ComponentName? = activity.intent?.getParcelableExtra(
                EXTRA_ORIGINAL_CLASS_COMPONENT,
                ComponentName::class.java
            ).also { Log.d(TAG, "originalComponent: $it") }

            val intent = originalComponent?.let { Intent().setComponent(it) }
                ?: ChooseLockPassword.IntentBuilder(activity).build()
                    .also {
                        if (WizardManagerHelper.isAnySetupWizard(activity.intent)) {
                            // SetupChooseLockPassword will show Skip and Screen lock options
                            // buttons, but the Screen lock options button will take users back to
                            // this generate password flow
                            it.setClass(activity, SetupChooseLockPassword::class.java)
                        }
                    }

            // Allow ChooseLockPassword to get the original extras, since these extras were
            // originally meant for ChooseLockPassword. This will let ChooseLockPassword do
            // validation properly
            intent.putExtras(activity.intent)

            return intent
        }

        @JvmStatic
        fun maybeInterceptLaunchAttemptForOriginalLockPassword(
            context: Context,
            originalLockPasswordIntent: Intent
        ): Intent? {
            if (
                originalLockPasswordIntent.getBooleanExtra(
                    ChooseLockGeneric.CONFIRM_CREDENTIALS, true
                )
            ) {
                // Don't launch our generation activity if credentials need to be confirmed.
                // The various AOSP activities assume this is true if not present, but using the
                // ChooseLockPassword.IntentBuilder will set it to false explicitly.
                return null
            }

            val originalLaunchComponent: ComponentName = originalLockPasswordIntent.component
                ?: return null

            // Only launch GenerateLockPasswordActivity if the intent we're intercepting
            // is a subclass of ChooseLockPassword that is known to work. This is because we needed
            // to modify ChooseLockPassword to do validation, so we should only work with
            // ChooseLockPasswords.
            //
            // Strictly speaking, any subclass of ChooseLockPassword could override onCreate and
            // other methods that we modified. See isTestedChooseLockPasswordComponent for details
            if (!isTestedChooseLockPasswordComponent(context, originalLaunchComponent)) {
                return null
            }

            val generateLockPassIntent = Intent(context, GenerateLockPasswordActivity::class.java)
            // Take the extras meant for the original ChooseLockPassword activity
            // and forward it. Will be used to detect if it's for password or PIN, and
            // will also be forwarded to original lock password activity if user wants
            // to use their own password.
            return generateLockPassIntent.putExtras(originalLockPasswordIntent)
                .putExtra(EXTRA_ORIGINAL_CLASS_COMPONENT, originalLaunchComponent)
        }

        private fun isTestedChooseLockPasswordComponent(
            context: Context, originalLaunchComponent: ComponentName
        ): Boolean {
            val verified = sequence {
                yield(ChooseLockPassword::class.java)
                // For SetupWizard. This calls super methods correctly, and is tested to be
                // compatible.
                yield(SetupChooseLockPassword::class.java)
            }

            if (
                originalLaunchComponent.packageName == context.packageName &&
                verified.any { testedClass -> testedClass.name == originalLaunchComponent.className }
            ) {
                return true
            }

            // will always return false after this point; now just logging info for reference
            val originalLaunchClass: Class<*> = try {
                Class.forName(
                    originalLaunchComponent.className, true,
                    context.getClassLoader()
                )
            } catch (e: ClassNotFoundException) {
                Log.w(TAG, "class $originalLaunchComponent can't be found", e)
                return false
            }

            if (ChooseLockPassword::class.java.isAssignableFrom(originalLaunchClass)) {
                Log.w(TAG, "class $originalLaunchComponent extends ChooseLockPassword; needs to be tested")
            } else {
                Log.w(TAG, "class $originalLaunchComponent doesn't extend ChooseLockPassword")
            }

            return false
        }
    }

    private val viewModel: GenerateLockPasswordViewModel by viewModels(
        factoryProducer = { GenerateLockPasswordViewModel.Factory }
    )

    override fun getIntent(): Intent {
        val intent = Intent(super.getIntent())
        intent.putExtra(EXTRA_SHOW_FRAGMENT, GenerateLockPasswordHostFragment::class.java.name)
        return intent
    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return GenerateLockPasswordHostFragment::class.java.name == fragmentName
    }

    override fun isToolbarEnabled(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(SetupWizardUtils.getTheme(this, intent))
        ThemeHelper.trySetDynamicColor(this)
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.content_parent).fitsSystemWindows = false
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        val passwordType = intent.getIntExtra(
            LockPatternUtils.PASSWORD_TYPE_KEY, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
        )
        val isAlphaMode = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == passwordType ||
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == passwordType ||
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX == passwordType

        // Following how ChooseLockPassword obtains these parameters. Need these parameters in order
        // to know what passwords are invalid during generation.
        val complexity = intent.getIntExtra(
            ChooseLockPassword.EXTRA_KEY_MIN_COMPLEXITY,
            DevicePolicyManager.PASSWORD_COMPLEXITY_NONE
        )
        val minMetrics: PasswordMetrics = intent.getParcelableExtra(
            ChooseLockPassword.EXTRA_KEY_MIN_METRICS
        ) ?: PasswordMetrics(LockPatternUtils.CREDENTIAL_TYPE_NONE)

        viewModel.setup(isAlphaMode, minMetrics, complexity)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Force a garbage collection immediately to remove remnant of user password shards
        // from memory.
        System.gc()
        System.runFinalization()
        System.gc()
    }

    class GenerateLockPasswordHostFragment : Fragment() {
        private val viewModel: GenerateLockPasswordViewModel by activityViewModels()

        private val aospConfirmPasswordLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "aospConfirmPasswordLauncher result: $result")
            ConfirmDeviceCredentialUtils.hideImeImmediately(requireActivity().window.decorView)
            if (result.resultCode == ChooseLockPassword.ChooseLockPasswordFragment.RESULT_FINISHED) {
                requireActivity().setResult(result.resultCode, result.data)
                requireActivity().finish()
            } else {
                viewModel.onAospConfirmationFailOrBackButton(result.data)
            }
        }

        private val aospEarlyValidatePasswordLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "aospEarlyValidatePasswordLauncher result: $result")
            ConfirmDeviceCredentialUtils.hideImeImmediately(requireActivity().window.decorView)
            viewModel.onAospValidation(result.data)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (activity !is GenerateLockPasswordActivity) {
                throw SecurityException("Fragment contained in wrong activity")
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.generate_lock_password_container, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            lifecycleScope.launchAndCollect(viewModel.aospEarlyValidationRequested) { requested ->
                if (requested) {
                    launchAospActivityForEarlyValidation()
                }
            }

            lifecycleScope.launchAndCollect(viewModel.stage.filterNotNull()) { newStage ->
                if (
                    newStage is PassGenStage.Confirmation.ConfirmWithAOSPActivity &&
                    !newStage.aospActivityLaunched
                ) {
                    launchAospActivityForConfirmAndSave()
                }

                val existingFragment = childFragmentManager.findFragmentByTag(
                    newStage.fragmentTag
                )
                if (existingFragment != null) {
                    return@launchAndCollect
                }

                val newFragment: Fragment = when (newStage) {
                    is PassGenStage.ChooseGeneratedOrManual -> GeneratedOrManualLockPasswordFragment()
                    is PassGenStage.ChooseParams -> LockPasswordGenerationParamsFragment()
                    is PassGenStage.ShowMultiple -> ShowGeneratedLockPassOptionsFragment()
                    is PassGenStage.Confirmation -> ConfirmGeneratedLockPassFragment()
                    PassGenStage.Quit -> {
                        requireActivity().finish()
                        return@launchAndCollect
                    }
                }

                val enter = if (newStage.isBackwards) {
                    com.google.android.setupdesign.R.anim.sud_slide_back_in
                } else {
                    com.google.android.setupdesign.R.anim.sud_slide_next_in
                }
                val exit = if (newStage.isBackwards) {
                    com.google.android.setupdesign.R.anim.sud_slide_back_out
                } else {
                    com.google.android.setupdesign.R.anim.sud_slide_next_out
                }

                childFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(enter, exit)
                    .replace(R.id.fragment_container_view, newFragment, newStage.fragmentTag)
                    .setReorderingAllowed(true)
                    .commit()
            }
        }

        private fun launchAospActivityForEarlyValidation() {
            val credential = (viewModel.selectedPassword.value
                    as? GenerateLockPasswordViewModel.Selection.ForConfirmation)
                ?.credential ?: return
            val intent = getAospPasswordIntent(requireActivity())
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION, true)
                // Note: We're giving ChooseLockPassword the generated password credential, so
                // it cannot be zeroized explicitly as the byte array contents will sit in the
                // intent. Just have to rely on garbage collection.
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION_GENERATED_PASSWORD, credential)
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION_VALIDATE_ONLY, true)

            aospEarlyValidatePasswordLauncher.launch(intent)
        }

        private fun launchAospActivityForConfirmAndSave() {
            val credential = (viewModel.selectedPassword.value
                    as? GenerateLockPasswordViewModel.Selection.ForConfirmation)
                ?.credential ?: return
            val autoPinConfirm = viewModel.isAutoPinConfirm.value
            val intent = getAospPasswordIntent(requireActivity())
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION, true)
                // Note: We're giving ChooseLockPassword the generated password credential, so
                // it cannot be zeroized explicitly as the byte array contents will sit in the
                // intent. Just have to rely on garbage collection.
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION_GENERATED_PASSWORD, credential)
                .putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FROM_PASSWORD_GENERATION_AUTO_PIN_CONFIRM, autoPinConfirm)

            aospConfirmPasswordLauncher.launch(intent)

            viewModel.onAospConfirmActivityLaunch()
        }
    }
}
