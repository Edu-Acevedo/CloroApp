package cl.duoc.clorocontrol.ui.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val MEASUREMENTS = "measurements"
    const val ALERTS = "alerts"
    const val HISTORY = "history"
    const val MEASUREMENT_FORM = "measurement_form"
    const val ACTION_FORM = "action_form/{alertId}"
    fun actionForm(alertId: Long): String = "action_form/$alertId"
}
