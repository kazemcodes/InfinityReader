

package org.ireader.core_api.source

import org.ireader.core_api.http.main.HttpClients
import org.ireader.core_api.prefs.PreferenceStore

class Dependencies(
    val httpClients: HttpClients,
    val preferences: PreferenceStore
)
