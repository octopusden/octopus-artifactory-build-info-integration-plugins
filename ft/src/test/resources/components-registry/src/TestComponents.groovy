dependency-ee {
    system = "CLASSIC"
    componentDisplayName = "Dependency EE"
    componentOwner = "Dependency Owner"
    releaseManager = "Dependency Manager"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/ee/dependency-ee.git"
    jira {
        projectKey = 'DEPS'
    }
    distribution {
        explicit = true
        external = true
    }
}

dependency-ei {
    system = "CLASSIC"
    componentDisplayName = "Dependency EI"
    componentOwner = "Dependency Owner"
    releaseManager = "Dependency Manager"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/ee/dependency-ei.git"
    jira {
        projectKey = 'DEPS'
    }
    distribution {
        explicit = true
        external = false
    }
}

dependency-ie {
    system = "CLASSIC"
    componentDisplayName = "Dependency IE"
    componentOwner = "Dependency Owner"
    releaseManager = "Dependency Manager"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/ee/dependency-ie.git"
    jira {
        projectKey = 'DEPS'
    }
    distribution {
        explicit = false
        external = true
    }
}

main-component {
    system = "CLASSIC"
    componentDisplayName = "Dependency IE"
    componentOwner = "Dependency Owner"
    releaseManager = "Dependency Manager"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/ee/dependency-ie.git"
    jira {
        projectKey = 'DEPS'
    }
}

