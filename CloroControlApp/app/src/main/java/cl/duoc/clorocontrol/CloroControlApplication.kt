package cl.duoc.clorocontrol

import android.app.Application
import cl.duoc.clorocontrol.data.local.CloroControlDatabase
import cl.duoc.clorocontrol.data.network.ConnectivityObserver
import cl.duoc.clorocontrol.data.preferences.SessionStore
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.data.sync.SyncScheduler

class CloroControlApplication : Application() {
    val database by lazy { CloroControlDatabase.getInstance(this) }
    val repository by lazy { ChlorineRepository(database, applicationContext) }
    val sessionStore by lazy { SessionStore(this) }
    val connectivityObserver by lazy { ConnectivityObserver(this) }

    override fun onCreate() {
        super.onCreate()
        SyncScheduler.enqueuePeriodic(this)
    }
}
