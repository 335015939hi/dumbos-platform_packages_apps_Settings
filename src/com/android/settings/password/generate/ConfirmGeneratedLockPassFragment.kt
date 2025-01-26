package com.android.settings.password.generate

import android.graphics.Insets
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.Selection
import android.text.Spannable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.ImeAwareEditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.internal.widget.TextViewInputDisabler
import com.android.settings.R
import com.android.settings.password.PasswordRequirementAdapter
import com.google.android.setupcompat.template.FooterBarMixin
import com.google.android.setupcompat.template.FooterButton
import com.google.android.setupcompat.util.WizardManagerHelper
import com.google.android.setupdesign.GlifLayout
import kotlinx.coroutines.flow.filterNotNull

private const val TAG = "ConfirmGeneratedLockPassFragment"
private const val KEY_LAST_KNOWN_STAGE = "last_known_stage_number"
private const val KEY_AOSP_ACTIVITY_LAUNCHED = "aosp_activity_launched"

private const val DISPLAY_PIN_SIZE_SP = 24f
private const val DISPLAY_PASSPHRASE_SIZE_SP = 18f

class ConfirmGeneratedLockPassFragment : BaseLockPasswordGenerationFragment(
    R.layout.generate_lock_password_confirm
) {
    private var passwordRestrictionView: RecyclerView? = null
    private var passwordRequirementAdapter: PasswordRequirementAdapter? = null
    private var lastKnownStage: PassGenStage.Confirmation? = null
    private var passwordEntry: ImeAwareEditText? = null
    private var passwordEntryInputDisabler: TextViewInputDisabler? = null
    private var layout: GlifLayout? = null

    private fun isForPassphrase(): Boolean {
        return viewModel.passType.value == GenerateLockPasswordViewModel.PassType.Passphrase
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lastKnownStage?.let {
            outState.putInt(KEY_LAST_KNOWN_STAGE, it.stageNumber)
            if (it is PassGenStage.Confirmation.ConfirmWithAOSPActivity) {
                outState.putBoolean(KEY_AOSP_ACTIVITY_LAUNCHED, it.aospActivityLaunched)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lastKnownStage = savedInstanceState?.getInt(KEY_LAST_KNOWN_STAGE, -1)?.let {
            val aospSettingsLaunched = savedInstanceState.getBoolean(
                KEY_AOSP_ACTIVITY_LAUNCHED,
                false
            )
            PassGenStage.Confirmation.fromStageNumber(it, aospSettingsLaunched)
        }
    }

    override fun onResume() {
        super.onResume()
        passwordEntry?.apply {
            requestFocus()
            // scheduleShowSoftInput()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = view as GlifLayout

        val message = view.findViewById<TextView>(R.id.sud_layout_description)
        message.visibility = View.VISIBLE

        val headerLayout = view.findViewById<LinearLayout>(
            com.google.android.setupdesign.R.id.sud_layout_header
        )
        setupPasswordRequirementsView(headerLayout)
        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.confirmError) { error ->
            if (error != null) {
                (passwordEntry?.text as? Spannable)?.let { editable ->
                    Selection.setSelection(editable, 0, editable.length)
                }
            }

            val errors = buildList<String> {
                when (error) {
                    GenerateLockPasswordViewModel.ConfirmError.DOESNT_MATCH -> {
                        add(getString(
                            if (isForPassphrase()) {
                                R.string.lock_screen_generate_confirm_passphrases_dont_match
                            } else {
                                R.string.lockpassword_confirm_pins_dont_match
                            }
                        ))
                    }
                    GenerateLockPasswordViewModel.ConfirmError.TOO_SHORT -> {}
                    null -> {}
                }
            }.toTypedArray()

            passwordRequirementAdapter?.setRequirements(errors, false)
        }

        // Make the password container consume the optical insets so the edit text is aligned
        // with the sides of the parent visually.
        val container = view.findViewById<ViewGroup>(R.id.password_container)
        container.opticalInsets = Insets.NONE

        passwordEntry = view.findViewById<ImeAwareEditText>(R.id.password_entry)
        passwordEntryInputDisabler = TextViewInputDisabler(passwordEntry!!);

        passwordEntry?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setInputLength(s?.length ?: 0)
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
        passwordEntry?.setOnEditorActionListener { _, actionId, _ ->
            // Check if this was the result of hitting the enter or "done" key
            if (
                actionId == EditorInfo.IME_NULL ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT
            ) {
                viewModel.primaryButtonClicked(passwordEntry?.text)
                true
            } else {
                false
            }
        }

        val footerBarMixin = layout!!.getMixin(FooterBarMixin::class.java)
        footerBarMixin.primaryButton =
            FooterButton.Builder(requireContext())
                .setText(R.string.lock_screen_generate_options_next_button)
                .setListener { viewModel.primaryButtonClicked(passwordEntry?.text) }
                .setButtonType(FooterButton.ButtonType.NEXT)
                .setTheme(com.google.android.setupdesign.R.style.SudGlifButton_Primary)
                .build()
        footerBarMixin.secondaryButton =
            FooterButton.Builder(requireContext())
                .setText(R.string.lockpassword_clear_label)
                .setListener { passwordEntry?.setText("") }
                .setButtonType(FooterButton.ButtonType.CLEAR)
                .setTheme(com.google.android.setupdesign.R.style.SudGlifButton_Secondary)
                .build()

        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.passType.filterNotNull()) { passType ->
            layout?.icon = activity?.getDrawable(passType.iconRes)
        }
        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.isPrimaryButtonEnabled) { on ->
            footerBarMixin.primaryButton?.isEnabled = on
        }

        val autoPinConfirmText = view.findViewById<TextView>(R.id.auto_pin_confirm_security_message)
        val autoPinConfirmCheck = view.findViewById<CheckBox>(R.id.auto_pin_confirm_enabler)
        autoPinConfirmCheck.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.isAutoPinConfirm) { autoPinConfirm ->
            autoPinConfirmCheck.isChecked = autoPinConfirm
        }
        autoPinConfirmCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoPinConfirm(isChecked)
        }

        val showPasswordText = view.findViewById<TextView>(R.id.show_generated_text)

        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.stage.filterNotNull()) { stage ->
            val newStage = lastKnownStage != stage
            if (newStage) {
                passwordEntry?.setText("")
            }
            if (stage !is PassGenStage.Confirmation) return@repeatCollectOnLifecycle
            lastKnownStage = stage

            if (
                !isForPassphrase() &&
                stage is PassGenStage.Confirmation.ConfirmWithVisible &&
                // see SetupChooseLockPassword; it's disabled in there
                !WizardManagerHelper.isAnySetupWizard(activity?.intent)
            ) {
                autoPinConfirmText.visibility = View.VISIBLE
                autoPinConfirmCheck.visibility = View.VISIBLE
            } else {
                autoPinConfirmText.visibility = View.GONE
                autoPinConfirmCheck.visibility = View.GONE
            }

            footerBarMixin.primaryButton.text =
                getString(R.string.lock_screen_generate_options_next_button)

            showPasswordText.apply {
                if (stage == PassGenStage.Confirmation.ConfirmWithVisible) {
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                    text = ""
                }
            }



            when (stage) {
                PassGenStage.Confirmation.ConfirmWithVisible -> {
                    if (isForPassphrase()) {
                        layout?.setHeaderText(R.string.lock_screen_generate_confirm_title_passphrase)
                        message.setText(R.string.lock_screen_generate_confirm_desc_passphrase)
                    } else {
                        layout?.setHeaderText(R.string.lock_screen_generate_confirm_title_pin)
                        message.setText(R.string.lock_screen_generate_confirm_desc_pin)
                    }
                }
                PassGenStage.Confirmation.ConfirmWithoutVisible -> {
                    if (isForPassphrase()) {
                        layout?.setHeaderText(R.string.lock_screen_generate_confirm_again_title_passphrase)
                        message.setText(R.string.lock_screen_generate_confirm_again_desc_passphrase)
                    } else {
                        layout?.setHeaderText(R.string.lock_screen_generate_confirm_again_title_pin)
                        message.setText(R.string.lock_screen_generate_confirm_again_desc_pin)
                    }
                }
                is PassGenStage.Confirmation.ConfirmWithAOSPActivity -> {}
            }
            if (newStage) {
                layout?.let { it.announceForAccessibility(it.headerText) }
            }
        }

        viewLifecycleOwner.repeatCollectOnLifecycle(viewModel.selectedPassword) { selection ->
            val currentStage = viewModel.stage.value
            if (selection == null || currentStage != PassGenStage.Confirmation.ConfirmWithVisible) {
                showPasswordText.text = ""
                return@repeatCollectOnLifecycle
            }
            when (val password = viewModel.getPassword(selection)) {
                is GeneratedPassphrase -> {
                    showPasswordText.text = password.passphrase
                    showPasswordText.textSize = DISPLAY_PASSPHRASE_SIZE_SP
                }
                is GeneratedPin -> {
                    showPasswordText.text = password.pin
                    showPasswordText.textSize = DISPLAY_PIN_SIZE_SP
                }
                null -> {
                    showPasswordText.text = ""
                    showPasswordText.textSize = DISPLAY_PIN_SIZE_SP
                }
            }
        }

        viewLifecycleOwner.repeatCollectOnLifecycle(
            viewModel.passType.filterNotNull()
        ) { type ->
            passwordEntry?.inputType = when (type) {
                GenerateLockPasswordViewModel.PassType.Passphrase ->
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                GenerateLockPasswordViewModel.PassType.Pin ->
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
            // Can't set via XML since setInputType resets the fontFamily to null
            passwordEntry?.setTypeface(
                Typeface.create(
                    requireContext().getString(
                        com.android.internal.R.string.config_headlineFontFamily
                    ),
                    Typeface.NORMAL
                )
            )
        }
    }

    // from com/android/settings/password/ChooseLockPassword.java
    private fun setupPasswordRequirementsView(view: ViewGroup?) {
        view ?: return
        createHintMessageView(view)
        passwordRestrictionView?.setLayoutManager(LinearLayoutManager(activity))
        passwordRequirementAdapter = PasswordRequirementAdapter(activity)
        passwordRestrictionView?.setAdapter(passwordRequirementAdapter)
        view.addView(passwordRestrictionView)
    }

    // from com/android/settings/password/ChooseLockPassword.java
    private fun createHintMessageView(view: ViewGroup) {
        if (passwordRestrictionView != null) {
            return
        }

        val sucTitleView = view.findViewById<TextView>(R.id.suc_layout_title)
        val titleLayoutParams =
            sucTitleView.layoutParams as ViewGroup.MarginLayoutParams
        passwordRestrictionView = RecyclerView(requireContext()).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(
                titleLayoutParams.leftMargin, resources.getDimensionPixelSize(
                    R.dimen.password_requirement_view_margin_top
                ), titleLayoutParams.leftMargin, 0
            )
            setLayoutParams(lp)
        }
    }
}
