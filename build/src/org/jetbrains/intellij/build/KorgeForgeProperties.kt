// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.intellij.build

import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.intellij.build.io.copyDir
import org.jetbrains.intellij.build.io.copyFileToDir
import java.nio.file.Path

open class IdeaCommunityProperties(communityHomeDir: Path) : BaseIdeaCommunityProperties(communityHomeDir) {
  override val baseFileName: String
    get() = "korge"

  init {
    platformPrefix = "Idea"
    applicationInfoModule = "intellij.idea.community.customization"
    additionalIDEPropertiesFilePaths = persistentListOf(communityHomeDir.resolve("build/conf/KorgeForge.properties"))

    baseDownloadUrl = "https://download.jetbrains.com/idea/"
    //baseDownloadUrl = "https://forge.korge.org/download/"

    //val bundles = productLayout.bundledPluginModules
    //val plugins = productLayout.pluginLayouts



    val modulesToRemove = setOf<String>(
      "intellij.featuresTrainer",
      "intellij.javaFX.community",
      "intellij.lombok",
      "intellij.python.community.plugin",
      "intellij.vcs.hg",
      "intellij.vcs.svn",
      "intellij.devkit",
      "intellij.eclipse",
      "intellij.sh.python",
      "intellij.ant",
      "intellij.gdpr",
      //////////////
      //"intellij.vcs.gitlab",
      //"intellij.groovy",
      //"intellij.maven",
      //"org.jetbrains.plugins.gradle.analysis",
      //"intellij.gradle.dependencyUpdater",
      //"org.jetbrains.plugins.gradle.maven",
      //"intellij.gradle.java",
    )

    fun shouldRemoveModule(name: String): Boolean {
      return name in modulesToRemove// || name.contains(".android")
    }

    productLayout.bundledPluginModules.removeAll { shouldRemoveModule(it) }
    productLayout.pluginLayouts = productLayout.pluginLayouts.removeAll { shouldRemoveModule(it.mainModule) }

    for (bundledPlugin in productLayout.bundledPluginModules) {
      println("bundledPlugin=$bundledPlugin")
    }

    for (plugin in productLayout.pluginLayouts) {
      //plugin.pluginXmlPatcher
      println("plugin=$plugin")
    }

    //TODO()
  }

  /*override suspend fun getAdditionalPluginPaths(context: BuildContext): List<Path> {
    return listOf(
      //context.paths.projectHome.resolve("../korge-forge-plugin/build/distributions/KorgePlugin.zip")
      context.paths.projectHome.resolve("../korge-forge-plugin/build/distributions/KorgePlugin/KorgePlugin")
    )
  }*/
  override fun getSystemSelector(appInfo: ApplicationInfoProperties, buildNumber: String): String =
    "Korge${appInfo.majorVersion}.${appInfo.minorVersionMainPart}"
  override fun getBaseArtifactName(appInfo: ApplicationInfoProperties, buildNumber: String) = "korgeforge-$buildNumber"
  override fun getOutputDirectoryName(appInfo: ApplicationInfoProperties) = "korgeforge"

  override suspend fun copyAdditionalFiles(targetDir: Path, context: BuildContext) {
    copyFileToDir(context.paths.communityHomeDir.resolve("LICENSE.txt"), targetDir)
    copyFileToDir(context.paths.communityHomeDir.resolve("NOTICE.txt"), targetDir)

    copyDir(
      sourceDir = context.paths.communityHomeDir.resolve("build/conf/KorgeForge/common/bin"),
      targetDir = targetDir.resolve("bin"),
    )
    bundleExternalPlugins(context, targetDir)
  }

  override fun createWindowsCustomizer(projectHome: Path): WindowsDistributionCustomizer = CommunityWindowsDistributionCustomizerExt()
  override fun createLinuxCustomizer(projectHome: String): LinuxDistributionCustomizer = CommunityLinuxDistributionCustomizerExt()
  override fun createMacCustomizer(projectHome: Path): MacDistributionCustomizer = CommunityMacDistributionCustomizerExt()

  protected open inner class CommunityWindowsDistributionCustomizerExt : WindowsDistributionCustomizer() {
    init {
      icoPath = icoPath?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      icoPathForEAP = icoPathForEAP?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      installerImagesPath = installerImagesPath?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
    }

    override fun getFullNameIncludingEdition(appInfo: ApplicationInfoProperties) = "KorGE Forge"
    override fun getFullNameIncludingEditionAndVendor(appInfo: ApplicationInfoProperties) = "KorGE Forge"
    override fun getUninstallFeedbackPageUrl(appInfo: ApplicationInfoProperties): String =
      "https://forge.korge.org/uninstall/?edition=IC-${appInfo.majorVersion}.${appInfo.minorVersion}"
  }

  protected open inner class CommunityLinuxDistributionCustomizerExt : LinuxDistributionCustomizer() {
    init {
      iconPngPath = iconPngPath?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      iconPngPathForEAP = iconPngPathForEAP?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      snapName = "korge-forge"
      snapDescription = "KorGE Forge IDE to create VideoGames in Kotlin."
    }

    override fun getRootDirectoryName(appInfo: ApplicationInfoProperties, buildNumber: String) = "korge-$buildNumber"
  }

  protected open inner class CommunityMacDistributionCustomizerExt : MacDistributionCustomizer() {
    init {
      icnsPath = icnsPath?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      icnsPathForEAP = icnsPathForEAP?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
      urlSchemes = listOf("korgeforge")
      bundleIdentifier = "org.korge.forge"
      dmgImagePath = dmgImagePath?.let { Path.of(it.toString().replace("ideaCE", "KorgeForge")) }
    }
    override fun getRootDirectoryName(appInfo: ApplicationInfoProperties, buildNumber: String): String = when {
      appInfo.isEAP -> "KorGE Forge ${appInfo.majorVersion}.${appInfo.minorVersionMainPart} EAP.app"
      else -> "KorGE Forge.app"
    }
  }
}