/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.PackageJson
import org.jetbrains.kotlin.gradle.targets.js.npm.fakePackageJsonValue
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.MayBeUpToDatePackageJsonTasksRegistry
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.KotlinCompilationNpmResolver
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.PACKAGE_JSON_UMBRELLA_TASK_NAME
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.getValue
import java.io.File

abstract class KotlinPackageJsonTask : DefaultTask() {

    init {
        onlyIf {
            npmResolutionManager.isConfiguringState()
        }
        outputs.upToDateWhen {
            // this way we will ensure that we need to resolve dependencies for the compilation in case of UP-TO-DATE task
            // used in KotlinCompilationNpmResolver#getResolutionOrResolveIfForced
            mayBeUpToDateTasksRegistry.get().markForNpmDependenciesResolve(it as KotlinPackageJsonTask)
            true
        }
    }

    @Transient
    private lateinit var nodeJs: NodeJsRootExtension

    private val npmResolutionManager by project.provider { nodeJs.npmResolutionManager }

    @Transient
    private lateinit var compilation: KotlinJsCompilation

    @get:Internal
    internal abstract val mayBeUpToDateTasksRegistry: Property<MayBeUpToDatePackageJsonTasksRegistry>

    private val compilationDisambiguatedName by lazy {
        compilation.disambiguatedName
    }

    @Input
    val projectPath = project.path

    private val compilationResolver
        get() = npmResolutionManager.resolver[projectPath][compilationDisambiguatedName]

    private val producer: KotlinCompilationNpmResolver.PackageJsonProducer
        get() = compilationResolver.packageJsonProducer

    @get:Input
    val packageJsonCustomFields: Map<String, Any?> by lazy {
        PackageJson(fakePackageJsonValue, fakePackageJsonValue)
            .apply {
                compilation.packageJsonHandlers.forEach { it() }
            }.customFields
    }

    private fun findDependentTasks(): Collection<Any> =
        producer.internalDependencies.map { dependency ->
            npmResolutionManager.resolver[dependency.projectPath][dependency.compilationName].npmProject.packageJsonTaskPath
        } + producer.internalCompositeDependencies.map { dependency ->
            dependency.includedBuild?.task(":$PACKAGE_JSON_UMBRELLA_TASK_NAME") ?: error("includedBuild instance is not available")
            dependency.includedBuild.task(":${RootPackageJsonTask.NAME}")
        }

    @get:Input
    internal val toolsNpmDependencies: List<String> by lazy {
        nodeJs.taskRequirements
            .getCompilationNpmRequirements(projectPath, compilationDisambiguatedName)
            .map { it.toString() }
            .sorted()
    }

    @get:Nested
    internal val producerInputs: KotlinCompilationNpmResolver.PackageJsonProducerInputs by lazy {
        producer.inputs
    }

    @get:OutputFile
    val packageJson: File by lazy {
        compilationResolver.npmProject.packageJsonFile
    }

    @TaskAction
    fun resolve() {
        compilationResolver.resolve()
    }

    companion object {
        fun create(compilation: KotlinJsCompilation): TaskProvider<KotlinPackageJsonTask> {
            val target = compilation.target
            val project = target.project
            val npmProject = compilation.npmProject
            val nodeJs = NodeJsRootPlugin.apply(project.rootProject)

            val npmCachesSetupTask = nodeJs.npmCachesSetupTaskProvider
            val packageJsonTaskName = npmProject.packageJsonTaskName
            val packageJsonUmbrella = nodeJs.packageJsonUmbrellaTaskProvider
            val packageJsonTask = project.registerTask<KotlinPackageJsonTask>(packageJsonTaskName) { task ->
                task.nodeJs = nodeJs
                task.compilation = compilation
                task.description = "Create package.json file for $compilation"
                task.group = NodeJsRootPlugin.TASKS_GROUP_NAME
                task.mayBeUpToDateTasksRegistry.apply {
                    set(MayBeUpToDatePackageJsonTasksRegistry.registerIfAbsent(project))
                    disallowChanges()
                }

                task.dependsOn(target.project.provider { task.findDependentTasks() })
                task.dependsOn(npmCachesSetupTask)
            }
            packageJsonUmbrella.configure { task ->
                task.inputs.file(packageJsonTask.map { it.packageJson })
            }

            nodeJs.rootPackageJsonTaskProvider!!.configure { it.mustRunAfter(packageJsonTask) }

            return packageJsonTask
        }
    }
}