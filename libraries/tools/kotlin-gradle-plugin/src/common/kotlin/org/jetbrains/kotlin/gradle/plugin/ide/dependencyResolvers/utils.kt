/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyResolution
import org.jetbrains.kotlin.gradle.plugin.mpp.metadataDependencyResolutionsOrEmpty
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.KotlinDependencyScope
import org.jetbrains.kotlin.gradle.plugin.sources.KotlinDependencyScope.*
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.packageFqName
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName

internal inline fun <reified T : MetadataDependencyResolution> KotlinSourceSet.resolveMetadata(): List<T> {
    if (this !is DefaultKotlinSourceSet) return emptyList()
    return compileDependenciesTransformation.metadataDependencyResolutionsOrEmpty.filterIsInstance<T>()
}
