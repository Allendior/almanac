package io.github.allendior.almanac

import android.app.Application
import androidx.work.Configuration
import io.github.allendior.almanac.data.PhotoStore
import io.github.allendior.almanac.data.PortraitRepository
import io.github.allendior.almanac.data.SettingsStore
import io.github.allendior.almanac.data.archive.ArchiveExporter
import io.github.allendior.almanac.data.archive.ArchiveImporter
import io.github.allendior.almanac.data.db.AlmanacDatabase
import io.github.allendior.almanac.notifications.ReminderWorkerFactory
import io.github.allendior.almanac.notifications.ensureReminderChannel

/**
 * Manual wiring, on purpose. The graph is six objects deep and lives entirely on the
 * device; a dependency-injection framework would add a build-time cost and a layer of
 * indirection for nothing.
 */
class AlmanacApp : Application(), Configuration.Provider {

    lateinit var container: Container
        private set

    override fun onCreate() {
        super.onCreate()
        container = Container(this)
        ensureReminderChannel(this)
    }

    // WorkManager's default initializer is removed from the manifest; it starts lazily
    // on first use and asks here for a factory that can build ReminderWorker with the
    // same repository and settings store as everything else, instead of a bare
    // no-arg constructor.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(ReminderWorkerFactory(container.repository, container.settings))
            .build()

    class Container(app: Application) {
        private val database = AlmanacDatabase.get(app)
        val photos = PhotoStore(app)
        val repository = PortraitRepository(database.portraitDao(), photos)
        val settings = SettingsStore(app)
        val exporter = ArchiveExporter(app, photos)
        val importer = ArchiveImporter(app, photos, database.portraitDao())
    }
}
