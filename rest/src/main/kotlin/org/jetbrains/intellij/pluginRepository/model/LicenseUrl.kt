package org.jetbrains.intellij.pluginRepository.model

class LicenseUrl private constructor(val url: String) {
  companion object {
    val JETBRAINS_TERM_OF_USE = LicenseUrl("https://jb.gg/legal/docs/toolbox/user.html")
    val APACHE_2_0 = LicenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
    val MIT = LicenseUrl("https://opensource.org/licenses/MIT")
    val GPL = LicenseUrl("https://www.gnu.org/licenses/gpl-3.0.en.html")
    val MOZILLA_2_0 = LicenseUrl("Mozilla Public License 2.0")
    val ECLIPSE_PUBLIC = LicenseUrl("https://www.eclipse.org/legal/epl-2.0/")
    val GNU_LESSER = LicenseUrl("https://www.gnu.org/licenses/lgpl-3.0.en.html")
    val BSD_3_CLAUSE = LicenseUrl("https://opensource.org/licenses/BSD-3-Clause")
    val BSD_2_CLAUSE = LicenseUrl("https://opensource.org/licenses/BSD-2-Clause")

    fun fromString(url: String) = LicenseUrl(url)
  }
}

