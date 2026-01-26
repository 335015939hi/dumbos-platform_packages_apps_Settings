package com.android.settings.network.telephony.carriersettingsoverride

import android.telephony.CarrierConfigManager
import com.android.settings.R
import com.android.settings.network.telephony.CarrierConfigRepository.KeyType

/**
 * This file contains all the possible carrier config override options that will appear in the UI
 * To add a new option:
 *
 * 1. Create a new object subclassing [ChangeableCarrierConfigOption].
 *    keysWithImportance specifies the keys that will be changed by the option.
 *    allPossibleConfigStates should enumerate all possible expected states for those keys. The "use
 *    default" option is added automatically elsewhere. You can mark user-selectable states that
 *    will be shown to the user; everything is selectable by default. You should also give human-
 *    readable strings for the options and the option title.
 *
 * 2. Add the new class to the allowedUserChangeableCarrierConfigFlags list below. This will allow
 *    the UI to detect the option and render it.
 *
 * 3. Consider bumping the value of MAX_OVERRIDES in SubscriptionManagerService on
 * frameworks/opt/telephony. TODO: Extract this into a constant that can be read from client code
 */

private val simpleUniformBoolStates: List<ConfigState> = listOf(
    ConfigState.Uniform(
        CarrierConfigTypedValue.Bool(true),
        R.string.carrier_settings_override_bool_true,
        R.string.carrier_settings_default_bool_true,
    ),
    ConfigState.Uniform(
        CarrierConfigTypedValue.Bool(false),
        R.string.carrier_settings_override_bool_false,
        R.string.carrier_settings_default_bool_false,
        isUserSelectable = false,
    ),
)

// Add new options here so that the UI can display it
val allowedUserChangeableCarrierConfigOptions: List<ChangeableCarrierConfigOption> by lazy {
    listOf(

        VoLTEAvailable,
        VoNREnabled,
        Enable5G,
        WiFiCallingAvailable,
        CrossSIMAvailable

    ).also { list ->
       list.onEach { flag ->
            flag.possibleConfigStates.forEach { state ->
                if (state is ConfigState.NonUniform) {
                    val left = state.stateValues.size
                    val right = flag.keys.size
                    check(left == right) {
                        "key size for ${flag.javaClass.simpleName} mismatch ($left != $right)"
                    }

                    if (state.stateValues.any { it is CarrierConfigTypedValue.AnyMatcher }) {
                        check(!state.isUserSelectable) {
                            "States with AnyMatcher should not be selectable"
                        }
                    }
                }

            }
        }
        val numTotalKeys = list.sumOf { it.keys.size }
        val max = 25
        check(numTotalKeys <= max) {
            "numTotalKeys $numTotalKeys exceeds max number of overrides $max"
        }
    }
}

data object VoLTEAvailable : ChangeableCarrierConfigOption(
    // The keys that will be edited in this option
    keysWithType = listOf(
        CarrierConfigManager.KEY_CARRIER_VOLTE_AVAILABLE_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL to KeyType.BOOLEAN,
    ),
    // All possible values for the keys. Each of these can be an option that the user can select
    allPossibleConfigStates = listOf(
        // Enabled
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true), // KEY_CARRIER_VOLTE_AVAILABLE_BOOL
                CarrierConfigTypedValue.Bool(false), // KEY_HIDE_ENHANCED_4G_LTE_BOOL
            ),
            R.string.carrier_settings_override_bool_true,
            R.string.carrier_settings_default_bool_true,
        ),
        // Disabled
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(false),
                CarrierConfigTypedValue.Bool(true),
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),

        // Other states not exposed for user selection just so that we have a description for them
        // Disabled
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(false),
                CarrierConfigTypedValue.Bool(false),
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
        // Disabled
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true),
                CarrierConfigTypedValue.Bool(true),
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
    )
) {
    override val titleStringRes = R.string.carrier_settings_override_volte_availability
    override val dialogDescriptionStringRes =
        R.string.carrier_settings_override_volte_availability_description
}

data object VoNREnabled : ChangeableCarrierConfigOption(
    keysWithType = listOf(
        CarrierConfigManager.KEY_VONR_ENABLED_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_VONR_SETTING_VISIBILITY_BOOL to KeyType.BOOLEAN,
    ),
    // Use uniform booleans (true true, false false), then cover other states not exposed for user
    // selection just so that we have a description for them
    allPossibleConfigStates = simpleUniformBoolStates + listOf(
        // Not exposed for user
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true),
                CarrierConfigTypedValue.Bool(false),
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(false),
                CarrierConfigTypedValue.Bool(true),
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
    )
) {
    override val titleStringRes = R.string.carrier_settings_override_vonr_enable
    override val dialogDescriptionStringRes =
        R.string.carrier_settings_override_vonr_enable_description
}

data object Enable5G : ChangeableCarrierConfigOption(
    keysWithType = listOf(
        CarrierConfigManager.KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY to KeyType.INT_ARRAY,
    ),
    allPossibleConfigStates = listOf(
        // Enabled (i.e. all the 5G NR capabilities)
        ConfigState.Uniform(
            CarrierConfigTypedValue.IntegerArray(
                listOf(
                    CarrierConfigManager.CARRIER_NR_AVAILABILITY_NSA,
                    CarrierConfigManager.CARRIER_NR_AVAILABILITY_SA,
                )
            ),
            R.string.carrier_settings_override_5g_enabled_all_modes,
            R.string.carrier_settings_override_5g_default_all_modes,
        ),
        // Only NSA
        ConfigState.Uniform(
            CarrierConfigTypedValue.IntegerArray(
                listOf(CarrierConfigManager.CARRIER_NR_AVAILABILITY_NSA)
            ),
            R.string.carrier_settings_override_5g_enabled_nsa,
            R.string.carrier_settings_override_5g_default_nsa,
            isUserSelectable = false,
        ),
        // Only SA
        ConfigState.Uniform(
            CarrierConfigTypedValue.IntegerArray(
                listOf(CarrierConfigManager.CARRIER_NR_AVAILABILITY_SA)
            ),
            R.string.carrier_settings_override_5g_enabled_sa,
            R.string.carrier_settings_override_5g_default_sa,
            isUserSelectable = false,
        ),
        // Disabled (i.e. no 5G NR capabilities)
        ConfigState.Uniform(
            CarrierConfigTypedValue.IntegerArray(emptyList()),
            R.string.carrier_settings_override_5g_disabled,
            R.string.carrier_settings_default_5g_disabled,
            isUserSelectable = false,
        ),
    )
) {
    override val titleStringRes = R.string.carrier_settings_override_5g_title
    override val dialogDescriptionStringRes = R.string.carrier_settings_override_5g_description
}

data object WiFiCallingAvailable : ChangeableCarrierConfigOption(
    // The keys that will be edited in this option
    keysWithType = listOf(
        CarrierConfigManager.KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL to KeyType.BOOLEAN,
    ),
    allPossibleConfigStates = listOf(
        // Enabled
        ConfigState.Uniform(
            CarrierConfigTypedValue.Bool(true),
            R.string.carrier_settings_override_bool_true,
            R.string.carrier_settings_default_bool_true,
            summaryStringRes = R.string.carrier_settings_override_wfc_availability_on_summary
        ),
        // Not exposed for user. All of these are used to match with original carrier config values
        // Disabled
        ConfigState.Uniform(
            CarrierConfigTypedValue.Bool(false),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true), // KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL
                CarrierConfigTypedValue.Bool(true), // KEY_EDITABLE_WFC_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL
                CarrierConfigTypedValue.Bool(false), // KEY_EDITABLE_WFC_ROAMING_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL
            ),
            R.string.carrier_settings_override_wfc_availability_enabled_roaming_not_editable,
            R.string.carrier_settings_override_wfc_availability_enabled_roaming_not_editable,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true), // KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL
                CarrierConfigTypedValue.Bool(true), // KEY_EDITABLE_WFC_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_EDITABLE_WFC_ROAMING_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL
            ),
            R.string.carrier_settings_override_bool_true,
            R.string.carrier_settings_default_bool_true,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true), // KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL
                CarrierConfigTypedValue.Bool(false), // KEY_EDITABLE_WFC_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_EDITABLE_WFC_ROAMING_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL
            ),
            R.string.carrier_settings_override_wfc_availability_enabled_not_editable,
            R.string.carrier_settings_override_wfc_availability_enabled_not_editable,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(false), // KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_EDITABLE_WFC_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_EDITABLE_WFC_ROAMING_MODE_BOOL
                CarrierConfigTypedValue.AnyMatcher, // KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
    )
) {
    override val titleStringRes = R.string.carrier_settings_override_wfc_availability
    override val dialogDescriptionStringRes =
        R.string.carrier_settings_override_wfc_availability_description
}

data object CrossSIMAvailable : ChangeableCarrierConfigOption(
    keysWithType = listOf(
        CarrierConfigManager.KEY_CARRIER_CROSS_SIM_IMS_AVAILABLE_BOOL to KeyType.BOOLEAN,
        CarrierConfigManager.KEY_ENABLE_CROSS_SIM_CALLING_ON_OPPORTUNISTIC_DATA_BOOL to KeyType.BOOLEAN,
    ),
    // Use uniform booleans (true true, false false), then cover other states not exposed for user
    // selection just so that we have a description for them
    allPossibleConfigStates = simpleUniformBoolStates + listOf(
        // Not exposed for user
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(false),
                CarrierConfigTypedValue.AnyMatcher,
            ),
            R.string.carrier_settings_override_bool_false,
            R.string.carrier_settings_default_bool_false,
            isUserSelectable = false,
        ),
        ConfigState.NonUniform(
            listOf(
                CarrierConfigTypedValue.Bool(true),
                CarrierConfigTypedValue.AnyMatcher,
            ),
            R.string.carrier_settings_override_bool_true,
            R.string.carrier_settings_default_bool_true,
            isUserSelectable = false,
        ),
    )
) {
    override val titleStringRes = R.string.carrier_settings_override_cross_sim
    override val dialogDescriptionStringRes: Int
        get() = R.string.carrier_settings_override_cross_sim_description
}
