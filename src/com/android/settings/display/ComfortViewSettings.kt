package com.android.settings.display

import android.app.settings.SettingsEnums
import android.content.Context
import android.content.res.Resources
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings.System as SystemSettings
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.internal.R as InternalR
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settings.core.BasePreferenceController.AVAILABLE
import com.android.settings.core.BasePreferenceController.CONDITIONALLY_UNAVAILABLE
import com.android.settings.core.BasePreferenceController.UNSUPPORTED_ON_DEVICE
import com.android.settings.core.SliderPreferenceController
import com.android.settings.core.TogglePreferenceController
import com.android.settings.dashboard.DashboardFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable
import com.android.settingslib.widget.LayoutPreference
import com.android.settingslib.widget.SliderPreference
import kotlin.math.roundToInt

private const val KEY_COMFORT_VIEW_PREVIEW = "comfort_view_preview"
private const val KEY_COMFORT_VIEW_DYNAMIC = "comfort_view_dynamic"
private const val KEY_COMFORT_VIEW_INTENSITY = "comfort_view_intensity"
private const val KEY_PREVIEW_SELECTION_INDEX = "comfort_view_preview_selection_index"

private const val SETTING_CV_ENABLED = "cv_enabled"
private const val SETTING_CV_DYNAMIC_ENABLED = "cv_dynamic_enabled"
private const val SETTING_CV_PREFERRED_INTENSITY = "cv_preferred_intensity"
private const val CV_INTENSITY_SCALE = 1000f
private const val CV_INTENSITY_DEFAULT = 0.5f

private const val DOT_INDICATOR_SIZE = 12
private const val DOT_INDICATOR_LEFT_PADDING = 6
private const val DOT_INDICATOR_RIGHT_PADDING = 6

private val Context.comfortViewAvailabilityStatus: Int
    get() {
        if (!resources.getBoolean(InternalR.bool.config_cv_available)) {
            return UNSUPPORTED_ON_DEVICE
        }
        return try {
            if (display.isInternal) AVAILABLE else CONDITIONALLY_UNAVAILABLE
        } catch (_: UnsupportedOperationException) {
            AVAILABLE
        }
    }

private val Context.isComfortViewSettingsAvailable: Boolean
    get() = comfortViewAvailabilityStatus == AVAILABLE

private fun Context.currentUserId(): Int = UserHandle.myUserId()

private fun Context.isComfortViewEnabled(): Boolean =
    SystemSettings.getIntForUser(contentResolver, SETTING_CV_ENABLED, 0, currentUserId()) != 0

private fun Context.setComfortViewEnabled(enabled: Boolean): Boolean =
    SystemSettings.putIntForUser(
        contentResolver,
        SETTING_CV_ENABLED,
        if (enabled) 1 else 0,
        currentUserId(),
    )

private fun Context.isComfortViewDynamicEnabled(): Boolean =
    SystemSettings.getIntForUser(contentResolver, SETTING_CV_DYNAMIC_ENABLED, 1, currentUserId()) !=
        0

private fun Context.setComfortViewDynamicEnabled(enabled: Boolean): Boolean =
    SystemSettings.putIntForUser(
        contentResolver,
        SETTING_CV_DYNAMIC_ENABLED,
        if (enabled) 1 else 0,
        currentUserId(),
    )

private fun Context.getComfortViewPreferredIntensity(): Float =
    SystemSettings.getFloatForUser(
            contentResolver,
            SETTING_CV_PREFERRED_INTENSITY,
            CV_INTENSITY_DEFAULT,
            currentUserId(),
        )
        .coerceIn(0f, 1f)

private fun Context.setComfortViewPreferredIntensity(intensity: Float): Boolean =
    SystemSettings.putFloatForUser(
        contentResolver,
        SETTING_CV_PREFERRED_INTENSITY,
        intensity.coerceIn(0f, 1f),
        currentUserId(),
    )

private fun PreferenceScreen.updateComfortViewDependentPreferences(context: Context) {
    val comfortViewEnabled = context.isComfortViewEnabled()
    val dynamicEnabled = context.isComfortViewDynamicEnabled()
    findPreference<Preference>(KEY_COMFORT_VIEW_DYNAMIC)?.isEnabled = comfortViewEnabled
    findPreference<Preference>(KEY_COMFORT_VIEW_INTENSITY)?.isEnabled =
        comfortViewEnabled && !dynamicEnabled
}

@SearchIndexable
class ComfortViewSettings : DashboardFragment() {
    private var arrowPrevious: View? = null
    private var arrowNext: View? = null
    private var dotIndicators: Array<ImageView> = emptyArray()
    private var previewPages: List<View> = emptyList()
    private var comfortViewSettingsObserver: ContentObserver? = null
    private var viewPager: ViewPager? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addColorPreview(savedInstanceState?.getInt(KEY_PREVIEW_SELECTION_INDEX) ?: 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_PREVIEW_SELECTION_INDEX, viewPager?.currentItem ?: 0)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        registerComfortViewSettingsObserver()
    }

    override fun onStop() {
        unregisterComfortViewSettingsObserver()
        super.onStop()
    }

    override fun getPreferenceScreenResId(): Int = R.xml.comfort_view_settings

    override fun getMetricsCategory(): Int = SettingsEnums.DISPLAY

    override fun getHelpResource(): Int = 0

    override fun getPreferenceScreenBindingKey(context: Context): String? = null

    override fun getLogTag(): String = TAG

    private fun addColorPreview(savedPosition: Int) {
        val preview = findPreference<LayoutPreference>(KEY_COMFORT_VIEW_PREVIEW) ?: return
        val pager = preview.findViewById<ViewPager>(R.id.viewpager) ?: return
        viewPager = pager

        val pageLayouts =
            intArrayOf(
                R.layout.color_mode_view1,
                R.layout.color_mode_view2,
                R.layout.color_mode_view3,
            )
        val inflatedPages = arrayOfNulls<View>(pageLayouts.size)
        val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        pageLayouts.forEachIndexed { index, layout ->
            val pageIndex = if (isRtl) pageLayouts.size - index - 1 else index
            inflatedPages[pageIndex] = layoutInflater.inflate(layout, null)
        }
        previewPages = inflatedPages.filterNotNull()
        pager.adapter = ColorPreviewPagerAdapter(previewPages)
        pager.setPageMargin(getColorPreviewPageMarginPx())

        arrowPrevious = preview.findViewById<View>(R.id.arrow_previous)?.apply {
            setOnClickListener { pager.setCurrentItem(pager.currentItem - 1, true) }
        }
        arrowNext = preview.findViewById<View>(R.id.arrow_next)?.apply {
            setOnClickListener { pager.setCurrentItem(pager.currentItem + 1, true) }
        }

        val indicatorContainer = preview.findViewById<ViewGroup>(R.id.viewGroup) ?: return
        indicatorContainer.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(previewPages.size)
        repeat(previewPages.size) { index ->
            val indicator =
                ImageView(requireContext()).apply {
                    layoutParams =
                        ViewGroup.MarginLayoutParams(
                            DOT_INDICATOR_SIZE,
                            DOT_INDICATOR_SIZE,
                        ).apply {
                            setMargins(
                                DOT_INDICATOR_LEFT_PADDING,
                                0,
                                DOT_INDICATOR_RIGHT_PADDING,
                                0,
                            )
                        }
                }
            val dotIndex = if (isRtl) previewPages.size - index - 1 else index
            indicators[dotIndex] = indicator
            indicatorContainer.addView(indicator)
        }
        dotIndicators = indicators.filterNotNull().toTypedArray()

        pager.addOnPageChangeListener(
            object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                    if (positionOffset != 0f) {
                        previewPages.forEach { it.visibility = View.VISIBLE }
                    } else {
                        previewPages.getOrNull(position)?.contentDescription =
                            getString(R.string.colors_viewpager_content_description)
                    }
                }

                override fun onPageSelected(position: Int) {
                    updatePreviewIndicator(position)
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            }
        )

        val initialPosition =
            savedPosition.takeIf { it in previewPages.indices }
                ?: if (isRtl) previewPages.lastIndex else 0
        pager.setCurrentItem(initialPosition, false)
        updatePreviewIndicator(initialPosition)
    }

    private fun registerComfortViewSettingsObserver() {
        if (comfortViewSettingsObserver != null) {
            return
        }
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    updatePreferenceStates()
                }
            }
        comfortViewSettingsObserver = observer
        val resolver = requireContext().contentResolver
        resolver.registerContentObserver(
            SystemSettings.getUriFor(SETTING_CV_ENABLED),
            false,
            observer,
            requireContext().currentUserId(),
        )
        resolver.registerContentObserver(
            SystemSettings.getUriFor(SETTING_CV_DYNAMIC_ENABLED),
            false,
            observer,
            requireContext().currentUserId(),
        )
        resolver.registerContentObserver(
            SystemSettings.getUriFor(SETTING_CV_PREFERRED_INTENSITY),
            false,
            observer,
            requireContext().currentUserId(),
        )
    }

    private fun unregisterComfortViewSettingsObserver() {
        comfortViewSettingsObserver?.let {
            requireContext().contentResolver.unregisterContentObserver(it)
            comfortViewSettingsObserver = null
        }
    }

    private fun getColorPreviewPageMarginPx(): Int {
        return try {
            val resolvedAttribute =
                requireContext()
                    .theme
                    .obtainStyledAttributes(
                        intArrayOf(android.R.attr.listPreferredItemPaddingStart)
                    )
            try {
                resolvedAttribute.getDimensionPixelSize(0, 0)
            } finally {
                resolvedAttribute.recycle()
            }
        } catch (_: NullPointerException) {
            defaultColorPreviewPageMarginPx()
        } catch (_: Resources.NotFoundException) {
            defaultColorPreviewPageMarginPx()
        }
    }

    private fun defaultColorPreviewPageMarginPx(): Int =
        (16 * resources.displayMetrics.density).toInt()

    private fun updatePreviewIndicator(position: Int) {
        previewPages.forEachIndexed { index, page ->
            val selected = position == index
            dotIndicators.getOrNull(index)?.setBackgroundResource(
                if (selected) {
                    R.drawable.ic_color_page_indicator_focused
                } else {
                    R.drawable.ic_color_page_indicator_unfocused
                }
            )
            page.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            if (selected) {
                page.contentDescription = getString(R.string.colors_viewpager_content_description)
            }
        }
        arrowPrevious?.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        arrowNext?.visibility =
            if (position == previewPages.lastIndex) View.INVISIBLE else View.VISIBLE
    }

    companion object {
        private const val TAG = "ComfortViewSettings"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.comfort_view_settings) {
                override fun isPageSearchEnabled(context: Context): Boolean =
                    context.isComfortViewSettingsAvailable
            }
    }
}

class ComfortViewPreferenceController(private val context: Context, preferenceKey: String) :
    BasePreferenceController(context, preferenceKey),
    DefaultLifecycleObserver {

    private val comfortViewSettingsObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                refreshSummary(preference)
            }
        }
    private var preference: Preference? = null

    override fun getAvailabilityStatus(): Int = context.comfortViewAvailabilityStatus

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(getPreferenceKey())
    }

    override fun getSummary(): CharSequence {
        return if (context.isComfortViewEnabled()) {
            context.getString(R.string.comfort_filters_comfort_view_on_summary)
        } else {
            context.getString(R.string.comfort_filters_all_modes_off_summary)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        context.contentResolver.registerContentObserver(
            SystemSettings.getUriFor(SETTING_CV_ENABLED),
            false,
            comfortViewSettingsObserver,
            context.currentUserId(),
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        context.contentResolver.unregisterContentObserver(comfortViewSettingsObserver)
    }
}

class ComfortViewMainSwitchPreferenceController(
    private val context: Context,
    preferenceKey: String,
) : TogglePreferenceController(context, preferenceKey) {
    private var screen: PreferenceScreen? = null

    override fun getAvailabilityStatus(): Int = context.comfortViewAvailabilityStatus

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        this.screen = screen
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        screen?.updateComfortViewDependentPreferences(context)
    }

    override fun isChecked(): Boolean = context.isComfortViewEnabled()

    override fun setChecked(isChecked: Boolean): Boolean {
        val updated = context.setComfortViewEnabled(isChecked)
        if (updated) {
            screen?.updateComfortViewDependentPreferences(context)
        }
        return updated
    }

    override fun getSliceHighlightMenuRes(): Int = R.string.menu_key_display
}

class ComfortViewDynamicPreferenceController(
    private val context: Context,
    preferenceKey: String,
) : TogglePreferenceController(context, preferenceKey) {
    private var screen: PreferenceScreen? = null

    override fun getAvailabilityStatus(): Int = context.comfortViewAvailabilityStatus

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        this.screen = screen
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        preference.isEnabled = context.isComfortViewEnabled()
        screen?.updateComfortViewDependentPreferences(context)
    }

    override fun isChecked(): Boolean = context.isComfortViewDynamicEnabled()

    override fun setChecked(isChecked: Boolean): Boolean {
        val updated = context.setComfortViewDynamicEnabled(isChecked)
        if (updated) {
            screen?.updateComfortViewDependentPreferences(context)
        }
        return updated
    }

    override fun getSliceHighlightMenuRes(): Int = R.string.menu_key_display
}

class ComfortViewIntensityPreferenceController(
    private val context: Context,
    preferenceKey: String,
) : SliderPreferenceController(context, preferenceKey) {
    override fun getAvailabilityStatus(): Int = context.comfortViewAvailabilityStatus

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        screen.findPreference<SliderPreference>(getPreferenceKey())?.let {
            it.setUpdatesContinuously(true)
            it.max = max
            it.min = min
        }
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        preference.isEnabled =
            context.isComfortViewEnabled() && !context.isComfortViewDynamicEnabled()
    }

    override fun getSliderPosition(): Int =
        (context.getComfortViewPreferredIntensity() * CV_INTENSITY_SCALE)
            .roundToInt()
            .coerceIn(min, max)

    override fun setSliderPosition(position: Int): Boolean =
        context.setComfortViewPreferredIntensity(position.coerceIn(min, max) / CV_INTENSITY_SCALE)

    override fun getMax(): Int = CV_INTENSITY_SCALE.toInt()

    override fun getMin(): Int = 0

    override fun getSliceHighlightMenuRes(): Int = R.string.menu_key_display
}

private class ColorPreviewPagerAdapter(private val pages: List<View>) : PagerAdapter() {
    override fun getCount(): Int = pages.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val page = pages[position]
        container.addView(page)
        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun isViewFromObject(view: View, item: Any): Boolean = view == item
}
