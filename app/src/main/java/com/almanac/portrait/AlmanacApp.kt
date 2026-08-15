package com.almanac.portrait

import android.app.Application
import com.almanac.portrait.data.PhotoStore
import com.almanac.portrait.data.PortraitRepository
import com.almanac.portrait.data.SettingsStore
import com.almanac.portrait.data.archive.ArchiveExporter
import com.almanac.portrait.data.archive.ArchiveImporter
import com.almanac.portrait.data.db.AlmanacDatabase

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
