package io.github.allendior.almanac

import android.app.Application
import io.github.allendior.almanac.data.PhotoStore
import io.github.allendior.almanac.data.PortraitRepository
import io.github.allendior.almanac.data.SettingsStore
import io.github.allendior.almanac.data.archive.ArchiveExporter
import io.github.allendior.almanac.data.archive.ArchiveImporter
import io.github.allendior.almanac.data.db.AlmanacDatabase

/**
 * Manual wiring, on purpose. The graph is six objects deep and lives entirely on the
 * device; a dependency-injection framework would add a build-time cost and a layer of
 * indirection for nothing.
 */
class AlmanacApp : Application() {

    lateinit var container: Container
        private set

    override fun onCreate() {
        super.onCreate()
        container = Container(this)
    }

    class Container(app: Application) {
        private val database = AlmanacDatabase.get(app)
        val photos = PhotoStore(app)
        val repository = PortraitRepository(database.portraitDao(), photos)
        val settings = SettingsStore(app)
        val exporter = ArchiveExporter(app, photos)
        val importer = ArchiveImporter(app, photos, database.portraitDao())
    }
}
