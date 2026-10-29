package cl.duoc.clorocontrol

import android.app.Application
import cl.duoc.clorocontrol.data.local.CloroControlDatabase
import cl.duoc.clorocontrol.data.preferences.SessionStore
import cl.duoc.clorocontrol.data.repository.ChlorineRepository

class CloroControlApplication : Application() {
    val database by lazy { CloroControlDatabase.getInstance(this) }
    val repository by lazy { ChlorineRepository(database) }
    val sessionStore by lazy { SessionStore(this) }
}
