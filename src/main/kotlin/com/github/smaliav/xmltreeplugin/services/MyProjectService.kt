package com.github.smaliav.xmltreeplugin.services

import com.intellij.openapi.project.Project
import com.github.smaliav.xmltreeplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
